package com.InventoryManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter
@Setter
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private InventoryProduct product;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private InventoryVendor vendor;

    private Integer previousStock;
    private Integer addedQuantity;
    private Integer newStock;

    // --- Pricing History for this Restock Batch ---
    private Double mrp;                      // Vendor MRP for this restock
    private Double vendorDiscountPercentage; // Vendor Discount % for this restock
    private Double storePrice;               // Calculated Purchase Price per unit for this batch (vendor discounted price)
    private Double productMrp;               // Actual Product MRP (real MRP printed on product) at this point in time
    private Double customerPrice;             // Final customer/selling price for this restock

    private String transactionType;          // e.g., "INITIAL_STOCK", "RESTOCK", "ORDER_DEDUCTION"
    private String remarks;

    private LocalDateTime createdAt;

    public StockHistory(InventoryProduct product, InventoryVendor vendor, Integer previousStock,
                        Integer addedQuantity, Integer newStock, Double mrp,
                        Double vendorDiscountPercentage, Double storePrice, Double productMrp, Double customerPrice,
                        String transactionType, String remarks) {
        this.product = product;
        this.vendor = vendor;
        this.previousStock = previousStock;
        this.addedQuantity = addedQuantity;
        this.newStock = newStock;
        this.mrp = mrp;
        this.vendorDiscountPercentage = vendorDiscountPercentage;
        this.storePrice = storePrice;
        this.productMrp = productMrp;
        this.customerPrice = customerPrice;
        this.transactionType = transactionType;
        this.remarks = remarks;
        this.createdAt = LocalDateTime.now();
    }
}