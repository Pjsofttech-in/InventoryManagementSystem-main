package com.InventoryManagementSystem.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockPurchaseRequestDTO {
    private Long productId;
    private Long vendorId;
    private String batchNumber;
    private BigDecimal vendorMrp; // legacy, ignored for discount maths
    private BigDecimal productMrp; // Actual Product MRP - vendor discount is applied on this
    // Vendor gives ONE of these two; the other is calculated. Flat discount wins if both are sent.
    private BigDecimal vendorDiscountPercentage; // e.g. 10 -> 10% off productMrp
    private BigDecimal vendorDiscountAmount;     // flat discount in rupees per unit, e.g. 100 -> purchase price 900
    private BigDecimal vendorFlatPrice;           // vendor flat price per unit = purchase price, e.g. MRP 1000 and flat price 900 -> 10 percent / Rs 100 discount
    private BigDecimal purchasePrice; // ignored on input: purchase price is always calculated
    private BigDecimal sellingPrice; // final store/customer price
    private Integer quantity;
    private LocalDate receivedDate; // Fixed: Added receivedDate field
        private LocalDate dueDate;
    private BigDecimal amountPaidNow; // सुरुवातीला किती जमा केले
    private String paymentMethod;
    private String transactionRef;
}