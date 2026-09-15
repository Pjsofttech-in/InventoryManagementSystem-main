package com.InventoryManagementSystem.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockPurchaseRequestDTO {
    private Long productId;
    private Long vendorId;
    private String batchNumber;
    private BigDecimal vendorMrp;
    private BigDecimal vendorDiscountPercentage;
    private BigDecimal purchasePrice; // final vendor price after discount
    private BigDecimal productMrp; // Actual Product MRP (real MRP printed on product)
    private BigDecimal sellingPrice; // final store/customer price
    private Integer quantity;
    private LocalDate receivedDate; // Fixed: Added receivedDate field
        private LocalDate dueDate;
    private BigDecimal amountPaidNow; // सुरुवातीला किती जमा केले
    private String paymentMethod;
    private String transactionRef;
}