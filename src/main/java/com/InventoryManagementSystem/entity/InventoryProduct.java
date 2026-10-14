//package com.InventoryManagementSystem.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.SQLDelete;
//import org.hibernate.annotations.SQLRestriction;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "products")
//@SQLDelete(sql = "UPDATE products SET active = false WHERE id = ?")
//@SQLRestriction("active = true")
//public class InventoryProduct {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String productName;
//
//    // --- Pricing Structure with Default Values ---
//    private Double mrp = 0.0;                      // Legacy vendor-quoted MRP (kept for old data, no longer used in discount maths)
//    private Double productMrp = 0.0;               // Actual Product MRP (real MRP printed on the product) - vendor discount is applied on THIS
//    private Double vendorDiscountPercentage = 0.0; // Vendor discount % on productMrp (given or calculated from flat price)
//    private Double vendorDiscountAmount = 0.0;     // Vendor discount in rupees per unit (given or calculated from %)
//    private Double storePrice = 0.0;               // Vendor purchase price per unit = productMrp - vendorDiscountAmount
//    private Double customerPrice = 0.0;            // Store Price / Selling Price (current base store/customer price)
//
//    // --- Dynamic GST Structure ---
//    private Boolean gstApplicable = false;
//
//    private Boolean applyCgst = false;
//    private Double cgstPercentage = 0.0;
//
//    private Boolean applySgst = false;
//    private Double sgstPercentage = 0.0;
//
//    private Boolean applyUgst = false;
//    private Double ugstPercentage = 0.0;
//
//    private Boolean applyIgst = false;
//    private Double igstPercentage = 0.0;
//
//    private Double totalGstPercentage = 0.0;
//
//    private LocalDate productAddedDate;
//    private Integer stock = 0;
//
//    @Column(name = "active", nullable = false)
//    private Boolean active = true;
//
//    @Transient
//    private Boolean available;
//
//    @Transient
//    private Double totalAmount; // remainingStock * customerPrice (value of stock on hand)
//
//    @Transient
//    private Double buyPriceTotal; // remainingStock * storePrice (cost of stock on hand at buy price)
//
//    // --- Stock / sales columns (computed, not stored: always in sync with orders) ---
//    @Transient
//    private Integer totalStock = 0;      // remainingStock + totalSold
//    @Transient
//    private Integer remainingStock = 0;  // same as stock
//    @Transient
//    private Integer totalSold = 0;       // units sold in active (non-cancelled) orders
//    @Transient
//    private Double totalSoldAmount = 0.0; // sales value (after discount, before GST) of sold units
//
//    /** Fills sales figures (one grouped query for all products, see ProductServiceImpl). */
//    public void applySalesStats(int sold, double soldAmount) {
//        this.totalSold = sold;
//        this.totalSoldAmount = soldAmount;
//        this.totalStock = (this.stock == null ? 0 : this.stock) + sold;
//    }
//
//    @PrePersist
//    @PreUpdate
//    @PostLoad
////    public void calculatePricingAndTax() {
////        // Safe null-checks
////        if (this.mrp == null) this.mrp = 0.0;
////        if (this.vendorDiscountPercentage == null) this.vendorDiscountPercentage = 0.0;
////        if (this.productMrp == null) this.productMrp = 0.0;
////        if (this.customerPrice == null) this.customerPrice = 0.0;
////        if (this.stock == null) this.stock = 0;
////
////        // 1. Stock Availability Check
////        this.available = (this.stock > 0);
////
////        // 2. Clamp vendor discount percentage between 0% and 100%
////        double discountPct = Math.max(0.0, Math.min(100.0, this.vendorDiscountPercentage));
////
////        // 3. Store price is the explicit purchase price set by the stock API (never averaged).
////        // Fallback only: derive it from the ACTUAL product MRP and the vendor discount.
////        if (this.vendorDiscountAmount == null) this.vendorDiscountAmount = 0.0;
//////        if (this.storePrice == null) {
//////            this.vendorDiscountAmount = (this.productMrp * discountPct) / 100.0;
//////            this.storePrice = Math.max(0.0, this.productMrp - this.vendorDiscountAmount);
//////        }
//////        if (this.storePrice == null) {
//////            this.vendorDiscountAmount = (this.productMrp * discountPct) / 100.0;
//////            this.storePrice = Math.max(0.0, this.productMrp - this.vendorDiscountAmount);
//////        }
////
//////        new UPDATED CODE
////
////        // CASE A: जर Store Price (Vendor Flat Price) दिले असेल तर
////        if (this.storePrice != null && this.storePrice > 0 && this.productMrp > 0) {
////
////            // Vendor Discount Amount = MRP - Store Price
////            this.vendorDiscountAmount = Math.max(0.0, this.productMrp - this.storePrice);
////
////            // Calculate Discount Percentage = (Discount Amount / MRP) * 100
////            this.vendorDiscountPercentage = Math.round(((this.vendorDiscountAmount / this.productMrp) * 100.0) * 100.0) / 100.0;
////
////        }
////        // CASE B: जर Store Price नसेल तर Discount % वरून कॅल्क्युलेट करा (Fallback Logic)
////        else if (this.productMrp > 0) {
////
////            this.vendorDiscountAmount = (this.productMrp * discountPct) / 100.0;
////            this.storePrice = Math.max(0.0, this.productMrp - this.vendorDiscountAmount);
////
////        } else {
////            if (this.storePrice == null) this.storePrice = 0.0;
////        }
////
////
////        // 4. Value of stock on hand + remaining stock column
////        this.remainingStock = this.stock;
////        this.totalAmount = (double) this.stock * this.customerPrice;
////        this.buyPriceTotal = Math.round((double) this.stock * (this.storePrice == null ? 0.0 : this.storePrice) * 100.0) / 100.0;
////        this.totalStock = this.stock + (this.totalSold == null ? 0 : this.totalSold);
////
////        // 5. Dynamic GST Calculation
////        double calculatedGst = 0.0;
////        if (Boolean.TRUE.equals(this.gstApplicable)) {
////            if (Boolean.TRUE.equals(this.applyCgst) && this.cgstPercentage != null) {
////                calculatedGst += this.cgstPercentage;
////            }
////            if (Boolean.TRUE.equals(this.applySgst) && this.sgstPercentage != null) {
////                calculatedGst += this.sgstPercentage;
////            }
////            if (Boolean.TRUE.equals(this.applyUgst) && this.ugstPercentage != null) {
////                calculatedGst += this.ugstPercentage;
////            }
////            if (Boolean.TRUE.equals(this.applyIgst) && this.igstPercentage != null) {
////                calculatedGst += this.igstPercentage;
////            }
////        }
////        this.totalGstPercentage = calculatedGst;
////    }
//
//
//
//    @ManyToOne
//    @JoinColumn(name = "product_type_id")
//    private InventoryProductType productType;
//
//    @ManyToOne
//    private InventoryBrand brand;
//
//    @ManyToOne
//    private InventoryVendor vendor;
//
//    @ManyToOne
//    private InventoryUnitType unitType;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    @JsonIgnore
//    private List<InventoryProductAttributeValue> attributes;
//
//    public InventoryProduct() {
//        this.productAddedDate = LocalDate.now();
//        this.active = true;
//    }
//}
//
//


package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
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

    // ============================================================
    // PRICING
    // ============================================================

    /**
     * Legacy vendor-quoted MRP.
     * Kept for old database records.
     */
    private Double mrp = 0.0;

    /**
     * Actual MRP printed on the product.
     * Vendor discount is calculated on this value.
     */
    private Double productMrp = 0.0;

    /**
     * Vendor discount percentage.
     *
     * Example:
     * productMrp = 1000
     * discount = 10%
     */
    private Double vendorDiscountPercentage = 0.0;

    /**
     * Vendor discount amount per unit.
     *
     * Example:
     * 1000 - 900 = 100
     */
    private Double vendorDiscountAmount = 0.0;

    /**
     * Vendor purchase price / store purchase price.
     *
     * Example:
     * MRP = 1000
     * Discount = 100
     * storePrice = 900
     */
    private Double storePrice = 0.0;

    /**
     * Customer selling price.
     */
    private Double customerPrice = 0.0;

    /**
     * Pricing mode sent by frontend.
     *
     * FLAT    = vendor gives a flat purchase price
     * PERCENT = vendor gives a discount percentage
     *
     * This field is NOT stored in database.
     */
    @Transient
    private String vendorPricingMode;

    /**
     * Vendor flat price per unit (= purchase price). Not stored; input only.
     *
     * Example: productMrp = 1000, vendorFlatPrice = 900
     *          -> discount amount = 100, discount % = 10, storePrice = 900
     */
    @Transient
    private Double vendorFlatPrice;


    // ============================================================
    // GST
    // ============================================================

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


    // ============================================================
    // STOCK
    // ============================================================

    private LocalDate productAddedDate;

    private Integer stock = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;


    // ============================================================
    // TRANSIENT FIELDS
    // ============================================================

    @Transient
    private Boolean available;

    @Transient
    private Double totalAmount;

    @Transient
    private Double buyPriceTotal;

    @Transient
    private Integer totalStock = 0;

    @Transient
    private Integer remainingStock = 0;

    @Transient
    private Integer totalSold = 0;

    @Transient
    private Double totalSoldAmount = 0.0;


    // ============================================================
    // SALES STATS
    // ============================================================

    /**
     * Fills sales figures.
     */
    public void applySalesStats(int sold, double soldAmount) {

        this.totalSold = sold;
        this.totalSoldAmount = soldAmount;

        this.totalStock =
                (this.stock == null ? 0 : this.stock) + sold;
    }


    // ============================================================
    // PRICING CALCULATION
    // ============================================================

    /**
     * Calculates pricing and GST.
     *
     * IMPORTANT:
     *
     * @PrePersist
     * @PreUpdate
     *
     * are used for pricing calculation.
     *
     * @PostLoad is intentionally NOT used for pricing calculation.
     *
     * This prevents existing database values from being changed
     * just because the entity was loaded.
     */
    @PrePersist
    @PreUpdate
    public void calculatePricingAndTax() {

        // --------------------------------------------------------
        // NULL SAFETY
        // --------------------------------------------------------

        if (this.mrp == null) {
            this.mrp = 0.0;
        }

        if (this.productMrp == null) {
            this.productMrp = 0.0;
        }

        if (this.vendorDiscountPercentage == null) {
            this.vendorDiscountPercentage = 0.0;
        }

        if (this.vendorDiscountAmount == null) {
            this.vendorDiscountAmount = 0.0;
        }

        if (this.storePrice == null) {
            this.storePrice = 0.0;
        }

        if (this.customerPrice == null) {
            this.customerPrice = 0.0;
        }

        if (this.stock == null) {
            this.stock = 0;
        }

        // --------------------------------------------------------
        // STOCK AVAILABILITY
        // --------------------------------------------------------

        this.available = this.stock > 0;


        // --------------------------------------------------------
        // PRODUCT MRP VALIDATION
        // --------------------------------------------------------

        double actualMrp = Math.max(0.0, this.productMrp);


        // --------------------------------------------------------
        // DISCOUNT PERCENTAGE
        // --------------------------------------------------------

        double discountPct =
                Math.max(
                        0.0,
                        Math.min(
                                100.0,
                                this.vendorDiscountPercentage
                        )
                );


        // ========================================================
        // PRICING MODE
        // ========================================================

        String mode = this.vendorPricingMode == null
                ? ""
                : this.vendorPricingMode.trim().toUpperCase();


        // ========================================================
        // CASE 1: FLAT PRICE
        // ========================================================

        /*
         * Example:
         *
         * productMrp = 1000
         * storePrice = 900
         * vendorPricingMode = FLAT
         *
         * Result:
         *
         * discountAmount = 100
         * discountPercentage = 10
         * storePrice = 900
         */

        if ("FLAT".equals(mode)) {

            if (actualMrp > 0) {

                // Store price must not be greater than MRP
                double flatPrice =
                        Math.max(
                                0.0,
                                Math.min(
                                        actualMrp,
                                        this.storePrice
                                )
                        );

                this.storePrice = round2(flatPrice);

                // MRP - Purchase Price
                this.vendorDiscountAmount =
                        round2(
                                Math.max(
                                        0.0,
                                        actualMrp - this.storePrice
                                )
                        );

                // Discount %
                this.vendorDiscountPercentage =
                        round2(
                                (this.vendorDiscountAmount
                                        / actualMrp) * 100.0
                        );
            }
            else {

                this.storePrice = 0.0;
                this.vendorDiscountAmount = 0.0;
                this.vendorDiscountPercentage = 0.0;
            }
        }


        // ========================================================
        // CASE 2: PERCENTAGE PRICE
        // ========================================================

        /*
         * Example:
         *
         * productMrp = 1000
         * vendorPricingMode = PERCENT
         * vendorDiscountPercentage = 10
         *
         * Result:
         *
         * discountAmount = 100
         * storePrice = 900
         * discountPercentage = 10
         */

        else if ("PERCENT".equals(mode)
                || "PERCENTAGE".equals(mode)) {

            if (actualMrp > 0) {

                // Calculate discount amount
                this.vendorDiscountAmount =
                        round2(
                                (actualMrp * discountPct) / 100.0
                        );

                // Calculate purchase/store price
                this.storePrice =
                        round2(
                                Math.max(
                                        0.0,
                                        actualMrp
                                                - this.vendorDiscountAmount
                                )
                        );

                // Keep validated percentage
                this.vendorDiscountPercentage = discountPct;
            }
            else {

                this.storePrice = 0.0;
                this.vendorDiscountAmount = 0.0;
                this.vendorDiscountPercentage = 0.0;
            }
        }


        // ========================================================
        // CASE 3: BACKWARD COMPATIBILITY
        // ========================================================

        /*
         * If frontend does not send vendorPricingMode,
         * try to determine the pricing mode from the values.
         *
         * This is mainly for old API requests.
         */

        else {

            // ----------------------------------------------------
            // Stored MRP / discount / purchase price already agree
            // (e.g. product loaded from DB and saved again after a sale).
            // Keep the stored purchase price so it never drifts by paise.
            // ----------------------------------------------------

            if (actualMrp > 0
                    && this.storePrice > 0
                    && this.vendorDiscountAmount > 0
                    && Math.abs((actualMrp - this.storePrice)
                            - this.vendorDiscountAmount) <= 0.01) {

                this.vendorDiscountPercentage =
                        round2((this.vendorDiscountAmount / actualMrp) * 100.0);
            }

            // ----------------------------------------------------
            // Existing flat price was provided
            // ----------------------------------------------------

            else if (actualMrp > 0
                    && this.storePrice > 0
                    && this.storePrice < actualMrp
                    && discountPct == 0.0) {

                this.storePrice =
                        round2(
                                Math.min(
                                        actualMrp,
                                        this.storePrice
                                )
                        );

                this.vendorDiscountAmount =
                        round2(
                                actualMrp - this.storePrice
                        );

                this.vendorDiscountPercentage =
                        round2(
                                (this.vendorDiscountAmount
                                        / actualMrp) * 100.0
                        );
            }

            // ----------------------------------------------------
            // Percentage was provided
            // ----------------------------------------------------

            else if (actualMrp > 0 && discountPct > 0) {

                this.vendorDiscountAmount =
                        round2(
                                (actualMrp * discountPct) / 100.0
                        );

                this.storePrice =
                        round2(
                                Math.max(
                                        0.0,
                                        actualMrp
                                                - this.vendorDiscountAmount
                                )
                        );

                this.vendorDiscountPercentage = discountPct;
            }

            // ----------------------------------------------------
            // No discount
            // ----------------------------------------------------

            else {

                if (actualMrp > 0) {
                    this.storePrice = actualMrp;
                }
                else {
                    this.storePrice = 0.0;
                }

                this.vendorDiscountAmount = 0.0;
                this.vendorDiscountPercentage = 0.0;
            }
        }


        // ========================================================
        // STOCK TOTALS
        // ========================================================

        this.remainingStock = this.stock;

        this.totalAmount =
                (double) this.stock * this.customerPrice;

        this.buyPriceTotal =
                round2(
                        (double) this.stock
                                * (this.storePrice == null
                                ? 0.0
                                : this.storePrice)
                );

        this.totalStock =
                this.stock
                        + (this.totalSold == null
                        ? 0
                        : this.totalSold);


        // ========================================================
        // GST CALCULATION
        // ========================================================

        double calculatedGst = 0.0;

        if (Boolean.TRUE.equals(this.gstApplicable)) {

            if (Boolean.TRUE.equals(this.applyCgst)
                    && this.cgstPercentage != null) {

                calculatedGst += this.cgstPercentage;
            }

            if (Boolean.TRUE.equals(this.applySgst)
                    && this.sgstPercentage != null) {

                calculatedGst += this.sgstPercentage;
            }

            if (Boolean.TRUE.equals(this.applyUgst)
                    && this.ugstPercentage != null) {

                calculatedGst += this.ugstPercentage;
            }

            if (Boolean.TRUE.equals(this.applyIgst)
                    && this.igstPercentage != null) {

                calculatedGst += this.igstPercentage;
            }
        }

        this.totalGstPercentage =
                round2(calculatedGst);
    }


    // ============================================================
    // DERIVED STOCK FIELDS (recalculated every time a product is loaded)
    // ============================================================

    /**
     * available / remainingStock / totalAmount (stock value at selling price) /
     * buyPriceTotal (stock value at purchase price) are @Transient, so they are
     * NOT in the database. Without this @PostLoad they stay empty when products
     * are read from the DB (list page), which made "Avl Stock" and "Stock Amt" show 0.
     *
     * Only derived values are set here. Stored prices are never changed on load.
     */
    @PostLoad
    public void calculateDerivedStockFields() {

        int currentStock = this.stock == null ? 0 : this.stock;
        double sellPrice = this.customerPrice == null ? 0.0 : this.customerPrice;
        double buyPrice = this.storePrice == null ? 0.0 : this.storePrice;

        this.available = currentStock > 0;
        this.remainingStock = currentStock;
        this.totalAmount = round2(currentStock * sellPrice);
        this.buyPriceTotal = round2(currentStock * buyPrice);
        this.totalStock = currentStock + (this.totalSold == null ? 0 : this.totalSold);
    }


    // ============================================================
    // ROUNDING HELPER
    // ============================================================

    private double round2(double value) {

        return Math.round(value * 100.0) / 100.0;
    }


    // ============================================================
    // RELATIONSHIPS
    // ============================================================

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private InventoryProductType productType;

    // Optional: must belong to productType (checked when the product is saved).
    @ManyToOne
    @JoinColumn(name = "sub_category_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private InventorySubCategory subCategory;

    @ManyToOne
    private InventoryBrand brand;

    @ManyToOne
    private InventoryVendor vendor;

    @ManyToOne
    private InventoryUnitType unitType;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private List<InventoryProductAttributeValue> attributes;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public InventoryProduct() {

        this.productAddedDate = LocalDate.now();
        this.active = true;

        this.mrp = 0.0;
        this.productMrp = 0.0;
        this.vendorDiscountPercentage = 0.0;
        this.vendorDiscountAmount = 0.0;
        this.storePrice = 0.0;
        this.customerPrice = 0.0;

        this.stock = 0;
    }
}

