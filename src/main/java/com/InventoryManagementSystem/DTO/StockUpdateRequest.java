package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StockUpdateRequest {
    private Integer addedQuantity;
    private Long vendorId;

    // Current product prices after this restock (these are NOT averaged with old prices).
    private Double mrp;                 // Legacy, ignored for discount maths
    private Double productMrp;          // Actual Product MRP - vendor discount is applied on this
    // Vendor gives ONE of these two; the other is calculated. Flat discount wins if both are sent.
    private Double vendorDiscountPercentage; // e.g. 10 -> 10% off productMrp
    private Double vendorDiscountAmount;     // flat discount in rupees per unit, e.g. 100 -> purchase price 900
    private Double vendorFlatPrice;           // vendor flat price per unit = purchase price, e.g. MRP 1000 and flat price 900 -> 10 percent / Rs 100 discount
    private Double storePrice;          // ignored on input: purchase price is always calculated
    private Double customerPrice;       // Store Price / Selling Price

    // New stock batch details.
    private String batchNumber;
    private LocalDate receivedDate;
        private LocalDate dueDate;
    private Double purchasePrice;
    private Double sellingPrice;

    // Vendor bill/payment details.
    private Double amountPaidNow;
    private String paymentMethod;
    private String transactionRef;

    private String remarks;
}
