package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.StockPurchaseRequestDTO;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.entity.*;
import com.InventoryManagementSystem.repository.*;
import com.InventoryManagementSystem.service.StockBatchService;
import com.InventoryManagementSystem.util.VendorPricingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StockBatchServiceImpl implements StockBatchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductStockBatchRepository stockBatchRepository;

    @Autowired
    private VendorBillRepository vendorBillRepository;

    @Autowired
    private VendorPaymentHistoryRepository vendorPaymentHistoryRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Override
    @Transactional
    public ProductStockBatch purchaseStock(StockPurchaseRequestDTO request) {
        InventoryProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));

        InventoryVendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found with ID: " + request.getVendorId()));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Vendor discount is on the ACTUAL product MRP: flat discount (Rs) OR % given, the other is calculated.
        Double productMrp = request.getProductMrp() != null ? request.getProductMrp().doubleValue() : product.getProductMrp();
        Double flatDiscount = request.getVendorDiscountAmount() != null ? request.getVendorDiscountAmount().doubleValue() : null;
        Double flatPrice = request.getVendorFlatPrice() != null ? request.getVendorFlatPrice().doubleValue() : null; // vendor flat price = purchase price
        Double pctIn = request.getVendorDiscountPercentage() != null ? request.getVendorDiscountPercentage().doubleValue() : null;
        boolean hasFlat = (flatDiscount != null && flatDiscount > 0) || (flatPrice != null && flatPrice > 0);
        if (!hasFlat && pctIn == null) pctIn = product.getVendorDiscountPercentage();
        VendorPricingUtil.Result pricing = VendorPricingUtil.resolve(productMrp, pctIn, flatDiscount, flatPrice);
        product.setVendorPricingMode(hasFlat ? "FLAT" : "PERCENT");
        BigDecimal purchasePrice = BigDecimal.valueOf(pricing.purchasePrice());
        BigDecimal sellingPrice = request.getSellingPrice() != null
                ? request.getSellingPrice() : BigDecimal.valueOf(product.getCustomerPrice());

        ProductStockBatch batch = ProductStockBatch.builder()
                .product(product)
                .vendor(vendor)
                .batchNumber(request.getBatchNumber() != null && !request.getBatchNumber().isBlank()
                        ? request.getBatchNumber()
                        : "BATCH-" + System.currentTimeMillis())
                .vendorMrp(BigDecimal.valueOf(productMrp))
                .vendorDiscountPercentage(BigDecimal.valueOf(pricing.discountPercentage()))
                .vendorDiscountAmount(BigDecimal.valueOf(pricing.discountAmount()))
                .purchasePrice(purchasePrice)
                .productMrp(BigDecimal.valueOf(productMrp))
                .sellingPrice(sellingPrice)
                .initialQuantity(request.getQuantity())
                .availableQuantity(request.getQuantity())
                .receivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now())
                .isDeleted(false)
                .build();

        ProductStockBatch savedBatch = stockBatchRepository.save(batch);

        // 1. Update product stock and replace current prices with this new batch's prices.
        int currentTotal = product.getStock() != null ? product.getStock() : 0;
        product.setStock(currentTotal + request.getQuantity());
        product.setVendor(vendor);
        product.setProductMrp(productMrp);
        product.setVendorDiscountPercentage(pricing.discountPercentage());
        product.setVendorDiscountAmount(pricing.discountAmount());
        product.setStorePrice(pricing.purchasePrice());
        product.setCustomerPrice(sellingPrice.doubleValue());
        productRepository.save(product);

        StockHistory history = new StockHistory(
                product, vendor, currentTotal, request.getQuantity(), currentTotal + request.getQuantity(),
                product.getMrp(), product.getVendorDiscountPercentage(), product.getStorePrice(), product.getProductMrp(), product.getCustomerPrice(),
                "RESTOCK", "Stock batch purchased");
        stockHistoryRepository.save(history);

        // 2. Generate Vendor Bill
        BigDecimal totalBillAmount = purchasePrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal paidNow = request.getAmountPaidNow() != null ? request.getAmountPaidNow() : BigDecimal.ZERO;
        BigDecimal due = totalBillAmount.subtract(paidNow);

        PaymentStatus status = due.compareTo(BigDecimal.ZERO) <= 0 ? PaymentStatus.PAID :
                (paidNow.compareTo(BigDecimal.ZERO) > 0 ? PaymentStatus.PARTIAL : PaymentStatus.UNPAID);

        VendorBill bill = VendorBill.builder()
                .billNumber("VBILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .vendor(vendor)
                .stockBatch(savedBatch)
                .totalAmount(totalBillAmount)
                .paidAmount(paidNow)
                .dueAmount(due)
                .billDate(LocalDate.now())
                .paymentStatus(status)
                .isDeleted(false)
                .build();

        VendorBill savedBill = vendorBillRepository.save(bill);

        // 3. Record Vendor Payment History if initial payment made
        if (paidNow.compareTo(BigDecimal.ZERO) > 0) {
            VendorPaymentHistory paymentHistory = VendorPaymentHistory.builder()
                    .vendorBill(savedBill)
                    .amountPaid(paidNow)
                    .paymentDate(LocalDateTime.now())
                    .paymentMethod(parsePaymentMethod(request.getPaymentMethod()))
                    .transactionReference(request.getTransactionRef())
                    .build();
            vendorPaymentHistoryRepository.save(paymentHistory);
        }

        return savedBatch;
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) return null;
        try { return PaymentMethod.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    @Override
    public List<ProductStockBatch> getBatchesByProductId(Long productId) {
        return stockBatchRepository.findByProductIdAndIsDeletedFalse(productId);
    }

    @Override
    public List<ProductStockBatch> getActiveBatchesByProductId(Long productId) {
        return stockBatchRepository.findByProductIdAndAvailableQuantityGreaterThanAndIsDeletedFalseOrderByReceivedDateAsc(productId, 0);
    }

    @Override
    public List<ProductStockBatch> getExpiringBatches(Integer daysThreshold) {
        return List.of(); // expiry is not tracked for stock updates in this design
    }

    @Override
    public List<ProductStockBatch> getAllBatches() {
        return stockBatchRepository.findByIsDeletedFalse();
    }

    @Override
    @Transactional
    public void softDeleteBatch(Long batchId) {
        ProductStockBatch batch = stockBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Stock batch not found with ID: " + batchId));
        batch.setIsDeleted(true);
        stockBatchRepository.save(batch);
    }
}