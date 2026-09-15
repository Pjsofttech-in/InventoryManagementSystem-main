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
    private Double mrp;                 // Vendor MRP
    private Double vendorDiscountPercentage;
    private Double storePrice;          // Vendor Discounted Price (purchase price)
    private Double productMrp;          // Actual Product MRP (real MRP printed on product)
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
