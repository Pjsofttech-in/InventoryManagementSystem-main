package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class InventoryProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    // --- Pricing Structure with Default Values ---
    private Double mrp = 0.0;                      // Original Vendor MRP
    private Double vendorDiscountPercentage = 0.0; // Vendor Discount % on MRP
    private Double storePrice = 0.0;               // Purchase Price after vendor discount
    private Double customerPrice = 0.0;            // Current base store/customer price (final price shown to customer)

    // --- Dynamic GST Structure ---
    private Boolean gstApplicable = false;

    private Boolean applyCgst = false;
    private Double cgstPercentage = 0.0;

    private Boolean applySgst = false;
    private Double sgstPercentage = 0.0;

    private Boolean applyUgst = false;
    private Double ugstPercentage = 0.0;

    private Boolean applyIgst = false;
    private Double igstPercentage = 0.0;

    private Double totalGstPercentage = 0.0;

    private LocalDate productAddedDate;
    private Integer stock = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Transient
    private Boolean available;

    @Transient
    private Double totalAmount; // stock * customerPrice

    @PrePersist
    @PreUpdate
    @PostLoad
    public void calculatePricingAndTax() {
        // Safe null-checks
        if (this.mrp == null) this.mrp = 0.0;
        if (this.vendorDiscountPercentage == null) this.vendorDiscountPercentage = 0.0;
        if (this.customerPrice == null) this.customerPrice = 0.0;
        if (this.stock == null) this.stock = 0;

        // 1. Stock Availability Check
        this.available = (this.stock > 0);

        // 2. Clamp vendor discount percentage between 0% and 100%
        double discountPct = Math.max(0.0, Math.min(100.0, this.vendorDiscountPercentage));

        // 3. Store price is an explicit current purchase price.
        // Do NOT average/recalculate it from the old MRP/discount when stock is restocked.
        // The stock-update API sets the new store price for the new batch.
        if (this.storePrice == null) {
            if (this.mrp > 0) {
                double discountAmt = (this.mrp * discountPct) / 100.0;
                this.storePrice = Math.max(0.0, this.mrp - discountAmt);
            } else {
                this.storePrice = 0.0;
            }
        }

        // 4. Total Valuation of stock
        this.totalAmount = (double) this.stock * this.customerPrice;

        // 5. Dynamic GST Calculation
        double calculatedGst = 0.0;
        if (Boolean.TRUE.equals(this.gstApplicable)) {
            if (Boolean.TRUE.equals(this.applyCgst) && this.cgstPercentage != null) {
                calculatedGst += this.cgstPercentage;
            }
            if (Boolean.TRUE.equals(this.applySgst) && this.sgstPercentage != null) {
                calculatedGst += this.sgstPercentage;
            }
            if (Boolean.TRUE.equals(this.applyUgst) && this.ugstPercentage != null) {
                calculatedGst += this.ugstPercentage;
            }
            if (Boolean.TRUE.equals(this.applyIgst) && this.igstPercentage != null) {
                calculatedGst += this.igstPercentage;
            }
        }
        this.totalGstPercentage = calculatedGst;
    }

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private InventoryProductType productType;

    @ManyToOne
    private InventoryBrand brand;

    @ManyToOne
    private InventoryVendor vendor;

    @ManyToOne
    private InventoryUnitType unitType;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InventoryProductAttributeValue> attributes;

    public InventoryProduct() {
        this.productAddedDate = LocalDate.now();
        this.active = true;
    }
}