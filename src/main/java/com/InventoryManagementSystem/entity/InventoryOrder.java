package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class InventoryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate orderDate;

    private LocalTime orderTime;

    /*
     * Total before discount and GST.
     *
     * Example:
     * Product A = 1000
     * Product B = 500
     *
     * subtotalAmount = 1500
     */
    private Double subtotalAmount = 0.0;

    /*
     * Total customer discount.
     *
     * Example:
     * Discount on Product A = 100
     *
     * totalDiscountAmount = 100
     */
    private Double totalDiscountAmount = 0.0;

    /*
     * Amount after discount but before GST.
     *
     * Example:
     * Subtotal = 1500
     * Discount = 100
     *
     * taxableAmount = 1400
     */
    private Double taxableAmount = 0.0;

    /*
     * Total GST of all order items.
     */
    private Double totalGstAmount = 0.0;

    /*
     * Final amount:
     *
     * taxableAmount + totalGstAmount
     */
    private Double totalAmount = 0.0;

    /*
     * GST breakup for complete order.
     */
    private Double totalCgstAmount = 0.0;

    private Double totalSgstAmount = 0.0;

    private Double totalUgstAmount = 0.0;

    private Double totalIgstAmount = 0.0;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /*
     * Date by which the customer must complete payment for this order.
     * Optional: if not sent by the frontend it stays null and no overdue
     * check is done for this order.
     */
    private LocalDate paymentDueDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<InventoryOrderItem> orderItems;

    @OneToOne(
            mappedBy = "order",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private InventoryShipment shipment;

    @Transient
    private Double paidAmount = 0.0;

    @Transient
    private Double pendingAmount = 0.0;

    @Transient
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Transient
    private PaymentMethod paymentMethod;

    /*
     * True when paymentDueDate has passed and payment is still pending.
     * Calculated fresh every time the order is fetched.
     */
    @Transient
    private Boolean paymentOverdue = false;

    /*
     * How many days past paymentDueDate the pending payment is.
     * 0 when not overdue.
     */
    @Transient
    private Long overdueDays = 0L;

    public InventoryOrder() {
    }

    @PrePersist
    @PreUpdate
    public void calculateGrandTotal() {

        if (orderItems == null || orderItems.isEmpty()) {

            this.subtotalAmount = 0.0;
            this.totalDiscountAmount = 0.0;
            this.taxableAmount = 0.0;
            this.totalGstAmount = 0.0;
            this.totalAmount = 0.0;

            this.totalCgstAmount = 0.0;
            this.totalSgstAmount = 0.0;
            this.totalUgstAmount = 0.0;
            this.totalIgstAmount = 0.0;

            return;
        }

        this.subtotalAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getSubtotalAmount() != null
                                ? item.getSubtotalAmount()
                                : 0.0
                )
                .sum();

        this.totalDiscountAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getDiscountAmount() != null
                                ? item.getDiscountAmount()
                                : 0.0
                )
                .sum();

        this.taxableAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getTaxableAmount() != null
                                ? item.getTaxableAmount()
                                : 0.0
                )
                .sum();

        this.totalGstAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getTotalGstAmount() != null
                                ? item.getTotalGstAmount()
                                : 0.0
                )
                .sum();

        this.totalCgstAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getCgstAmount() != null
                                ? item.getCgstAmount()
                                : 0.0
                )
                .sum();

        this.totalSgstAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getSgstAmount() != null
                                ? item.getSgstAmount()
                                : 0.0
                )
                .sum();

        this.totalUgstAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getUgstAmount() != null
                                ? item.getUgstAmount()
                                : 0.0
                )
                .sum();

        this.totalIgstAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getIgstAmount() != null
                                ? item.getIgstAmount()
                                : 0.0
                )
                .sum();

        this.totalAmount = orderItems.stream()
                .mapToDouble(item ->
                        item.getTotalPrice() != null
                                ? item.getTotalPrice()
                                : 0.0
                )
                .sum();
    }
}