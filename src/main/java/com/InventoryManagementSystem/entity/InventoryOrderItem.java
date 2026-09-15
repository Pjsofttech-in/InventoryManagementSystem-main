//package com.InventoryManagementSystem.entity;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.SQLDelete;
//import org.hibernate.annotations.SQLRestriction;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "order_items")
//@SQLDelete(sql = "UPDATE order_items SET is_deleted = true WHERE id = ?")
//@SQLRestriction("is_deleted = false")
//public class InventoryOrderItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "is_deleted", nullable = false)
//    private boolean isDeleted = false;
//
//    @ManyToOne
//    @JoinColumn(name = "order_id")
//    @JsonBackReference
//    private InventoryOrder order;
//
//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private InventoryProduct product;
//
//    private Integer quantity;
//
//    private Double mrp;                          // Product Vendor MRP
//    private Double customerBasePrice;             // Base Customer Price (Product वरून fetch होणार)
//    private Double customerDiscountPercentage = 0.0; // Customer Discount %
//    private Double price;                        // Per Unit Final Selling Price (Discount नंतरची)
//    private Double discountAmount;               // Total Discount Amount (Qty साठी)
//    private Double gstPercentage;                // Applied GST %
//    private Double gstAmount;                    // Total GST Amount
//    private Double totalPrice;                   // Final Line Total Price
//
//    public InventoryOrderItem() {}
//
//    @PrePersist
//    @PreUpdate
//    public void calculateValues() {
//        if (product != null && quantity != null && quantity > 0) {
//
//            // 1. Product वरून Base Values Fetch करणे
//            this.mrp = (product.getMrp() != null) ? product.getMrp() : 0.0;
//            this.customerBasePrice = (product.getCustomerPrice() != null) ? product.getCustomerPrice() : 0.0;
//
//            if (this.customerDiscountPercentage == null) {
//                this.customerDiscountPercentage = 0.0;
//            }
//
//            // 2. Selling Price & Discount Calculation
//            if (this.price != null && this.price > 0 && (this.customerDiscountPercentage == 0.0)) {
//                // जर frontend वरून थेट Custom Price पाठवली असेल
//                if (this.customerBasePrice > this.price) {
//                    this.customerDiscountPercentage = ((this.customerBasePrice - this.price) / this.customerBasePrice) * 100.0;
//                }
//            } else {
//                // Discount Percentage वरून Selling Price calculate करणे
//                double discountPerUnit = (this.customerBasePrice * this.customerDiscountPercentage) / 100.0;
//                this.price = Math.max(0.0, this.customerBasePrice - discountPerUnit);
//            }
//
//            // 3. Total Discount Amount = (Base Price - Selling Price) * Quantity
//            this.discountAmount = (this.customerBasePrice - this.price) * this.quantity;
//
//            // 4. Dynamic GST Calculation
//            if (Boolean.TRUE.equals(product.getGstApplicable())) {
//                this.gstPercentage = (product.getTotalGstPercentage() != null) ? product.getTotalGstPercentage() : 0.0;
//                this.gstAmount = (this.price * this.quantity * this.gstPercentage) / 100.0;
//            } else {
//                this.gstPercentage = 0.0;
//                this.gstAmount = 0.0;
//            }
//
//            // 5. Line Item Grand Total
//            this.totalPrice = (this.price * this.quantity) + this.gstAmount;
//
//        } else {
//            this.mrp = 0.0;
//            this.customerBasePrice = 0.0;
//            this.customerDiscountPercentage = 0.0;
//            this.price = 0.0;
//            this.discountAmount = 0.0;
//            this.gstPercentage = 0.0;
//            this.gstAmount = 0.0;
//            this.totalPrice = 0.0;
//        }
//    }
//
//    public Double getCgst() {
//        return gstAmount != null ? gstAmount / 2.0 : 0.0;
//    }
//
//    public Double getSgst() {
//        return gstAmount != null ? gstAmount / 2.0 : 0.0;
//    }
//}


package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class InventoryOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private InventoryOrder order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private InventoryProduct product;

    private Integer quantity = 0;

    /*
     * Actual Product MRP (real MRP printed on the product), copied from the
     * product at the time of order so it stays fixed even if the product's
     * MRP changes later.
     */
    private Double productMrp = 0.0;

    /*
     * Original customer price before order discount.
     */
    private Double unitPrice = 0.0;

    /*
     * unitPrice * quantity
     *
     * Example:
     * 100 * 2 = 200
     */
    private Double subtotalAmount = 0.0;

    /*
     * Discount percentage applied specifically to this order.
     *
     * Important:
     * This is order discount, not permanent product discount.
     */
    private Double customerDiscountPercentage = 0.0;

    /*
     * Actual discount amount.
     *
     * Example:
     * Subtotal = 200
     * Discount 10%
     *
     * discountAmount = 20
     */
    private Double discountAmount = 0.0;

    /*
     * Amount after discount and before GST.
     *
     * Example:
     * 200 - 20 = 180
     */
    private Double taxableAmount = 0.0;

    /*
     * GST percentages copied at the time of order.
     *
     * We copy them because product GST may change later.
     * Old invoice must not change.
     */
    private Double cgstPercentage = 0.0;

    private Double sgstPercentage = 0.0;

    private Double ugstPercentage = 0.0;

    private Double igstPercentage = 0.0;

    private Double totalGstPercentage = 0.0;

    /*
     * Actual GST amounts.
     */
    private Double cgstAmount = 0.0;

    private Double sgstAmount = 0.0;

    private Double ugstAmount = 0.0;

    private Double igstAmount = 0.0;

    private Double totalGstAmount = 0.0;

    /*
     * Final item amount.
     *
     * taxableAmount + totalGstAmount
     */
    private Double totalPrice = 0.0;

    /*
     * Optional final unit price after discount.
     */
    private Double discountedUnitPrice = 0.0;

    public InventoryOrderItem() {
    }

    public void calculateValues() {

        int qty = this.quantity != null
                ? this.quantity
                : 0;

        if (qty <= 0) {
            resetAmounts();
            return;
        }

        /*
         * Price must come from product/database.
         * Never trust frontend price blindly.
         */
        double price = this.unitPrice != null
                ? this.unitPrice
                : 0.0;

        this.subtotalAmount = price * qty;

        double discountPercentage =
                this.customerDiscountPercentage != null
                        ? Math.max(
                        0.0,
                        Math.min(
                                100.0,
                                this.customerDiscountPercentage
                        )
                )
                        : 0.0;

        this.customerDiscountPercentage =
                discountPercentage;

        this.discountAmount =
                (this.subtotalAmount * discountPercentage)
                        / 100.0;

        this.taxableAmount =
                Math.max(
                        0.0,
                        this.subtotalAmount
                                - this.discountAmount
                );

        this.discountedUnitPrice =
                this.taxableAmount / qty;

        double cgstPct =
                this.cgstPercentage != null
                        ? this.cgstPercentage
                        : 0.0;

        double sgstPct =
                this.sgstPercentage != null
                        ? this.sgstPercentage
                        : 0.0;

        double ugstPct =
                this.ugstPercentage != null
                        ? this.ugstPercentage
                        : 0.0;

        double igstPct =
                this.igstPercentage != null
                        ? this.igstPercentage
                        : 0.0;

        this.cgstAmount =
                (this.taxableAmount * cgstPct)
                        / 100.0;

        this.sgstAmount =
                (this.taxableAmount * sgstPct)
                        / 100.0;

        this.ugstAmount =
                (this.taxableAmount * ugstPct)
                        / 100.0;

        this.igstAmount =
                (this.taxableAmount * igstPct)
                        / 100.0;

        this.totalGstPercentage =
                cgstPct
                        + sgstPct
                        + ugstPct
                        + igstPct;

        this.totalGstAmount =
                this.cgstAmount
                        + this.sgstAmount
                        + this.ugstAmount
                        + this.igstAmount;

        this.totalPrice =
                this.taxableAmount
                        + this.totalGstAmount;
    }

    private void resetAmounts() {

        this.subtotalAmount = 0.0;
        this.discountAmount = 0.0;
        this.taxableAmount = 0.0;
        this.discountedUnitPrice = 0.0;

        this.cgstAmount = 0.0;
        this.sgstAmount = 0.0;
        this.ugstAmount = 0.0;
        this.igstAmount = 0.0;

        this.totalGstAmount = 0.0;
        this.totalPrice = 0.0;
    }
}