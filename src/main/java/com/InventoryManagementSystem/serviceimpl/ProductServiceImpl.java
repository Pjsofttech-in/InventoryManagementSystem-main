package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.StockUpdateRequest;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.*;
import com.InventoryManagementSystem.DTO.ProductSummaryDTO;
import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.repository.*;
import com.InventoryManagementSystem.service.ProductService;
import com.InventoryManagementSystem.util.VendorPricingUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductTypeRepository productTypeRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private VendorRepository vendorRepository;
    @Autowired private UnitTypeRepository unitTypeRepository;
    @Autowired private SubCategoryRepository subCategoryRepository;
    @Autowired private StockHistoryRepository stockHistoryRepository;
    @Autowired private ProductStockBatchRepository stockBatchRepository;
    @Autowired private VendorBillRepository vendorBillRepository;
    @Autowired private VendorPaymentHistoryRepository vendorPaymentHistoryRepository;
    @Autowired private OrderItemRepository orderItemRepository;

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
        product.setSubCategory(resolveSubCategory(product, productType));
        product.setBrand(brand);
        product.setVendor(vendor);
        product.setUnitType(unitType);
        product.setStock(0); // stock is created only through a purchase batch

        // Vendor discount is on the ACTUAL product MRP: flat discount (Rs) OR % given, the other is calculated.
        // Vendor flat price (= purchase price) wins: MRP 1000, flat price 900 -> 10% / Rs 100 discount.
        boolean flatGiven = isPositive(product.getVendorFlatPrice()) || isPositive(product.getVendorDiscountAmount());
        VendorPricingUtil.Result pricing = VendorPricingUtil.resolve(
                product.getProductMrp(), product.getVendorDiscountPercentage(),
                product.getVendorDiscountAmount(), product.getVendorFlatPrice());
        // tell the entity which value the vendor gave so it keeps it exactly
        product.setVendorPricingMode(flatGiven ? "FLAT" : "PERCENT");
        product.setVendorDiscountPercentage(pricing.discountPercentage());
        product.setVendorDiscountAmount(pricing.discountAmount());
        product.setStorePrice(pricing.purchasePrice());
        product.calculatePricingAndTax();
        return enrich(productRepository.save(product));
    }

    /** Sub category is optional, but when sent it must exist and belong to the product's type. */
    private InventorySubCategory resolveSubCategory(InventoryProduct product, InventoryProductType productType) {
        if (product.getSubCategory() == null || product.getSubCategory().getId() == null) {
            return null;
        }
        InventorySubCategory sub = subCategoryRepository.findById(product.getSubCategory().getId())
                .orElseThrow(() -> new RuntimeException("Sub category not found with ID: " + product.getSubCategory().getId()));
        if (!sub.getProductType().getId().equals(productType.getId())) {
            throw new RuntimeException("Sub category '" + sub.getSubCategoryName() + "' does not belong to product type '"
                    + productType.getTypeName() + "'");
        }
        return sub;
    }

    private static boolean isPositive(Double v) {
        return v != null && v > 0;
    }

    // ---------------- Sales columns (totalSold, totalSoldAmount, totalStock) ----------------

    /** One grouped query for all products (no N+1): productId -> [unitsSold, soldAmount]. */
    private Map<Long, double[]> loadSalesStats() {
        Map<Long, double[]> stats = new HashMap<>();
        for (Object[] row : orderItemRepository.findSalesTotalsPerProduct(OrderStatus.CANCELLED)) {
            stats.put(((Number) row[0]).longValue(),
                    new double[]{((Number) row[1]).doubleValue(), ((Number) row[2]).doubleValue()});
        }
        return stats;
    }

    private void applyStats(InventoryProduct p, Map<Long, double[]> stats) {
        double[] s = stats.get(p.getId());
        p.applySalesStats(s == null ? 0 : (int) s[0], s == null ? 0.0 : VendorPricingUtil.round(s[1]));
    }

    private <C extends Collection<InventoryProduct>> C enrich(C products) {
        if (products.isEmpty()) return products;
        Map<Long, double[]> stats = loadSalesStats();
        products.forEach(p -> applyStats(p, stats));
        return products;
    }

    private InventoryProduct enrich(InventoryProduct product) {
        if (product == null) return null;
        enrich(List.of(product));
        return product;
    }

    @Override public List<InventoryProduct> getAllProducts() { return enrich(productRepository.findAllByOrderByIdDesc()); }
    @Override public InventoryProduct getProductById(Long id) { return enrich(productRepository.findById(id).orElse(null)); }

    /** Grand totals of every product column (stock, remaining, sold, stock value, sales value). */
    @Override
    public ProductSummaryDTO getProductsSummary() {
        List<InventoryProduct> products = getAllProducts();
        ProductSummaryDTO s = new ProductSummaryDTO();
        s.setTotalProducts(products.size());
        for (InventoryProduct p : products) {
            s.setTotalStock(s.getTotalStock() + p.getTotalStock());
            s.setTotalRemainingStock(s.getTotalRemainingStock() + p.getRemainingStock());
            s.setTotalSold(s.getTotalSold() + p.getTotalSold());
            s.setTotalBuyPriceAmount(s.getTotalBuyPriceAmount() + p.getBuyPriceTotal());
            s.setTotalAmount(s.getTotalAmount() + p.getTotalAmount());
            s.setTotalSoldAmount(s.getTotalSoldAmount() + p.getTotalSoldAmount());
        }
        s.setTotalBuyPriceAmount(VendorPricingUtil.round(s.getTotalBuyPriceAmount()));
        s.setTotalAmount(VendorPricingUtil.round(s.getTotalAmount()));
        s.setTotalSoldAmount(VendorPricingUtil.round(s.getTotalSoldAmount()));
        return s;
    }

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

        Double newProductMrp = request.getProductMrp() != null ? request.getProductMrp() : product.getProductMrp();
        Double newCustomerPrice = request.getCustomerPrice() != null ? request.getCustomerPrice() : product.getCustomerPrice();

        // Vendor discount is on the ACTUAL product MRP. Flat discount (Rs) OR %; the other is calculated.
        // If neither is sent, the product's last discount % is reused.
        Double flatDiscount = request.getVendorDiscountAmount();
        Double flatPrice = request.getVendorFlatPrice();   // vendor flat price = purchase price
        boolean hasFlat = isPositive(flatDiscount) || isPositive(flatPrice);
        Double discountIn = request.getVendorDiscountPercentage();
        if (!hasFlat && discountIn == null) discountIn = product.getVendorDiscountPercentage();
        VendorPricingUtil.Result pricing = VendorPricingUtil.resolve(newProductMrp, discountIn, flatDiscount, flatPrice);
        product.setVendorPricingMode(hasFlat ? "FLAT" : "PERCENT");

        Double newDiscount = pricing.discountPercentage();
        Double newStorePrice = pricing.purchasePrice();
        Double newMrp = request.getMrp() != null ? request.getMrp() : product.getMrp();

        // IMPORTANT: replace current prices with the new batch prices. No averaging.
        product.setVendor(vendor);
        product.setMrp(newMrp);
        product.setProductMrp(newProductMrp);
        product.setVendorDiscountPercentage(newDiscount);
        product.setVendorDiscountAmount(pricing.discountAmount());
        product.setStorePrice(newStorePrice);
        product.setCustomerPrice(newCustomerPrice);

        int previousStock = product.getStock() == null ? 0 : product.getStock();
        int newStock = previousStock + addedQty;
        product.setStock(newStock);
        product.calculatePricingAndTax();
        InventoryProduct updatedProduct = productRepository.save(product);

        BigDecimal purchasePrice = BigDecimal.valueOf(newStorePrice);
        BigDecimal sellingPrice = BigDecimal.valueOf(request.getSellingPrice() != null
                ? request.getSellingPrice() : newCustomerPrice);

        ProductStockBatch batch = ProductStockBatch.builder()
                .product(updatedProduct)
                .vendor(vendor)
                .batchNumber(request.getBatchNumber() != null && !request.getBatchNumber().isBlank()
                        ? request.getBatchNumber() : "BATCH-" + System.currentTimeMillis())
                .vendorMrp(BigDecimal.valueOf(newProductMrp == null ? 0 : newProductMrp))
                .vendorDiscountPercentage(BigDecimal.valueOf(newDiscount))
                .vendorDiscountAmount(BigDecimal.valueOf(pricing.discountAmount()))
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

        return enrich(updatedProduct);
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        try { return PaymentMethod.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    @Override public List<StockHistory> getProductStockHistory(Long productId) {
        return stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
    @Override public List<InventoryProduct> filterProducts(String productName, Long productTypeId, Long brandId, Long vendorId, Long unitTypeId, Long subCategoryId) {
        String searchName = (productName != null && !productName.trim().isEmpty()) ? productName.trim() : null;
        return enrich(productRepository.filterProducts(searchName, productTypeId, brandId, vendorId, unitTypeId, subCategoryId));
    }
    @Override public Page<InventoryProduct> getProductsPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InventoryProduct> page1 = productRepository.findAll(pageable);
        enrich(page1.getContent());
        return page1;
    }
}
