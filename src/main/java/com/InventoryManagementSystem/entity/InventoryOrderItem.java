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

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private InventoryOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private InventoryProduct product;

    private Integer quantity;

    private Double price;       // price per unit
    private Double gstAmount;   // total GST
    private Double totalPrice;  // final total

    public InventoryOrderItem() {}

    @PrePersist
    @PreUpdate
    public void calculateValues() {

        if (product != null && quantity != null && quantity > 0) {

            // 1. Get price
            this.price = (product.getPrice() != null) ? product.getPrice() : 0.0;

            // 2. Calculate GST
            if (Boolean.TRUE.equals(product.getGstApplicable())
                    && product.getGstPercentage() != null) {

                this.gstAmount = (price * quantity * product.getGstPercentage()) / 100;
            } else {
                this.gstAmount = 0.0;
            }

            // 3. Total price
            this.totalPrice = (price * quantity) + gstAmount;

        } else {
            this.price = 0.0;
            this.gstAmount = 0.0;
            this.totalPrice = 0.0;
        }
    }

    // 💸 Discount Calculation (MRP - Price)
    @Transient
    public Double getDiscountAmount() {
        if (product != null && product.getMrp() != null && price != null && quantity != null) {
            return (product.getMrp() - price) * quantity;
        }
        return 0.0;
    }

    // 🇮🇳 GST Split
    @Transient
    public Double getCgst() {
        return gstAmount != null ? gstAmount / 2 : 0.0;
    }

    @Transient
    public Double getSgst() {
        return gstAmount != null ? gstAmount / 2 : 0.0;
    }
}

