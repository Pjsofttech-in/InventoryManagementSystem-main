package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.StockUpdateRequest;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.*;
import com.InventoryManagementSystem.repository.*;
import com.InventoryManagementSystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductTypeRepository productTypeRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private UnitTypeRepository unitTypeRepository;
    @Autowired private StockHistoryRepository stockHistoryRepository;
    @Autowired private ProductStockBatchRepository stockBatchRepository;
    @Autowired private VendorBillRepository vendorBillRepository;
    @Autowired private VendorPaymentHistoryRepository vendorPaymentHistoryRepository;

    @Override
    @Transactional
    public InventoryProduct saveProduct(InventoryProduct product) {
        InventoryProductType productType = productTypeRepository.findById(product.getProductType().getId())
                .orElseThrow(() -> new RuntimeException("ProductType not found"));
        InventoryBrand brand = brandRepository.findById(product.getBrand().getId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        InventoryVendor vendor = vendorRepository.findById(product.getVendor().getId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        InventoryUnitType unitType = unitTypeRepository.findById(product.getUnitType().getId())
                .orElseThrow(() -> new RuntimeException("UnitType not found"));

        product.setProductType(productType);
        product.setBrand(brand);
        product.setVendor(vendor);
        product.setUnitType(unitType);
        product.setStock(0); // stock is created only through a purchase batch
        if (product.getStorePrice() == null) product.setStorePrice(calculateStorePrice(product.getMrp(), product.getVendorDiscountPercentage()));
        product.calculatePricingAndTax();
        return productRepository.save(product);
    }

    private double calculateStorePrice(Double mrp, Double discount) {
        double m = mrp == null ? 0.0 : mrp;
        double d = Math.max(0.0, Math.min(100.0, discount == null ? 0.0 : discount));
        return Math.max(0.0, m - ((m * d) / 100.0));
    }

    @Override public List<InventoryProduct> getAllProducts() { return productRepository.findAllByOrderByIdDesc(); }
    @Override public InventoryProduct getProductById(Long id) { return productRepository.findById(id).orElse(null); }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        InventoryProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        List<StockHistory> historyList = stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(id);
        if (!historyList.isEmpty()) stockHistoryRepository.deleteAll(historyList);
        productRepository.delete(product);
    }

    /**
     * Restock is a new batch. Existing stock is never repriced or averaged.
     * The product's current prices are replaced with the prices supplied for this new batch,
     * while the batch keeps its own purchase/selling price for history and FIFO consumption.
     */
    @Override
    @Transactional
    public InventoryProduct updateProductStock(Long productId, StockUpdateRequest request) {
        InventoryProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        int addedQty = request.getAddedQuantity() == null ? 0 : request.getAddedQuantity();
        if (addedQty <= 0) throw new RuntimeException("Added quantity must be greater than 0");

        InventoryVendor vendor = product.getVendor();
        if (request.getVendorId() != null) {
            vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + request.getVendorId()));
        }

        Double newMrp = request.getMrp() != null ? request.getMrp() : product.getMrp();
        Double newDiscount = request.getVendorDiscountPercentage() != null
                ? request.getVendorDiscountPercentage() : product.getVendorDiscountPercentage();
        Double newStorePrice = request.getStorePrice() != null
                ? request.getStorePrice()
                : (request.getPurchasePrice() != null ? request.getPurchasePrice()
                : calculateStorePrice(newMrp, newDiscount));
        Double newProductMrp = request.getProductMrp() != null ? request.getProductMrp() : product.getProductMrp();
        Double newCustomerPrice = request.getCustomerPrice() != null ? request.getCustomerPrice() : product.getCustomerPrice();

        // IMPORTANT: replace current prices with the new batch prices. No averaging.
        product.setVendor(vendor);
        product.setMrp(newMrp);
        product.setVendorDiscountPercentage(newDiscount);
        product.setStorePrice(newStorePrice);
        product.setProductMrp(newProductMrp);
        product.setCustomerPrice(newCustomerPrice);

        int previousStock = product.getStock() == null ? 0 : product.getStock();
        int newStock = previousStock + addedQty;
        product.setStock(newStock);
        product.calculatePricingAndTax();
        InventoryProduct updatedProduct = productRepository.save(product);

        BigDecimal purchasePrice = BigDecimal.valueOf(request.getPurchasePrice() != null
                ? request.getPurchasePrice() : newStorePrice);
        BigDecimal sellingPrice = BigDecimal.valueOf(request.getSellingPrice() != null
                ? request.getSellingPrice() : newCustomerPrice);

        ProductStockBatch batch = ProductStockBatch.builder()
                .product(updatedProduct)
                .vendor(vendor)
                .batchNumber(request.getBatchNumber() != null && !request.getBatchNumber().isBlank()
                        ? request.getBatchNumber() : "BATCH-" + System.currentTimeMillis())
                .vendorMrp(BigDecimal.valueOf(newMrp == null ? 0 : newMrp))
                .vendorDiscountPercentage(BigDecimal.valueOf(newDiscount == null ? 0 : newDiscount))
                .purchasePrice(purchasePrice)
                .productMrp(BigDecimal.valueOf(newProductMrp == null ? 0 : newProductMrp))
                .sellingPrice(sellingPrice)
                .initialQuantity(addedQty)
                .availableQuantity(addedQty)
                .receivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now())
                .isDeleted(false)
                .build();
        stockBatchRepository.save(batch);

        StockHistory history = new StockHistory(
                updatedProduct, vendor, previousStock, addedQty, newStock,
                newMrp, newDiscount, newStorePrice, newProductMrp, newCustomerPrice,
                "RESTOCK", request.getRemarks());
        stockHistoryRepository.save(history);

        // Every stock purchase gets a vendor bill.
        BigDecimal totalBill = purchasePrice.multiply(BigDecimal.valueOf(addedQty));
        BigDecimal paidNow = BigDecimal.valueOf(Math.max(0.0, request.getAmountPaidNow() == null ? 0.0 : request.getAmountPaidNow()));
        if (paidNow.compareTo(totalBill) > 0) paidNow = totalBill;
        BigDecimal due = totalBill.subtract(paidNow);
        PaymentStatus paymentStatus = due.signum() == 0 ? PaymentStatus.PAID
                : (paidNow.signum() > 0 ? PaymentStatus.PARTIAL : PaymentStatus.UNPAID);

        VendorBill bill = VendorBill.builder()
                .billNumber("VBILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .vendor(vendor)
                .stockBatch(batch)
                .totalAmount(totalBill)
                .paidAmount(paidNow)
                .dueAmount(due)
                .billDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .paymentStatus(paymentStatus)
                .isDeleted(false)
                .build();
        VendorBill savedBill = vendorBillRepository.save(bill);

        if (paidNow.signum() > 0) {
            VendorPaymentHistory payment = VendorPaymentHistory.builder()
                    .vendorBill(savedBill)
                    .amountPaid(paidNow)
                    .paymentDate(LocalDateTime.now())
                    .paymentMethod(parsePaymentMethod(request.getPaymentMethod()))
                    .transactionReference(request.getTransactionRef())
                    .notes(request.getRemarks())
                    .build();
            vendorPaymentHistoryRepository.save(payment);
        }

        return updatedProduct;
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        try { return PaymentMethod.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    @Override public List<StockHistory> getProductStockHistory(Long productId) {
        return stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
    @Override public List<InventoryProduct> filterProducts(String productName, Long productTypeId, Long brandId, Long vendorId, Long unitTypeId) {
        String searchName = (productName != null && !productName.trim().isEmpty()) ? productName.trim() : null;
        return productRepository.filterProducts(searchName, productTypeId, brandId, vendorId, unitTypeId);
    }
    @Override public Page<InventoryProduct> getProductsPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }
}
