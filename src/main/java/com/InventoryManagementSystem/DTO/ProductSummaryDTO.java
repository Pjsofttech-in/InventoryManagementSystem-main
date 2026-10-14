package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

/** Grand totals of the product table columns. */
@Getter
@Setter
public class ProductSummaryDTO {
    private int totalProducts;
    private int totalStock;           // sum of every product's totalStock (remaining + sold)
    private int totalRemainingStock;  // sum of remaining stock
    private int totalSold;            // sum of units sold
    private double totalBuyPriceAmount; // cost of stock on hand (remaining x buy price)
    private double totalAmount;       // value of stock on hand (remaining x selling price)
    private double totalSoldAmount;   // total sales value (after discount, before GST)
}
