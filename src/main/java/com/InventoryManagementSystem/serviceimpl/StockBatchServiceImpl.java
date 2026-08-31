package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.StockPurchaseRequestDTO;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.entity.*;
import com.InventoryManagementSystem.repository.*;
import com.InventoryManagementSystem.service.StockBatchService;
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

        ProductStockBatch batch = ProductStockBatch.builder()
                .product(product)
                .vendor(vendor)
                .batchNumber(request.getBatchNumber() != null && !request.getBatchNumber().isBlank()
                        ? request.getBatchNumber()
                        : "BATCH-" + System.currentTimeMillis())
                .vendorMrp(request.getVendorMrp() == null ? BigDecimal.ZERO : request.getVendorMrp())
                .vendorDiscountPercentage(request.getVendorDiscountPercentage() == null ? BigDecimal.ZERO : request.getVendorDiscountPercentage())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
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
        if (request.getVendorMrp() != null) product.setMrp(request.getVendorMrp().doubleValue());
        if (request.getVendorDiscountPercentage() != null) product.setVendorDiscountPercentage(request.getVendorDiscountPercentage().doubleValue());
        if (request.getPurchasePrice() != null) product.setStorePrice(request.getPurchasePrice().doubleValue());
        if (request.getSellingPrice() != null) product.setCustomerPrice(request.getSellingPrice().doubleValue());
        productRepository.save(product);

        StockHistory history = new StockHistory(
                product, vendor, currentTotal, request.getQuantity(), currentTotal + request.getQuantity(),
                product.getMrp(), product.getVendorDiscountPercentage(), product.getStorePrice(), product.getCustomerPrice(),
                "RESTOCK", "Stock batch purchased");
        stockHistoryRepository.save(history);

        // 2. Generate Vendor Bill
        BigDecimal totalBillAmount = request.getPurchasePrice().multiply(BigDecimal.valueOf(request.getQuantity()));
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