package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class InventoryPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiving_bank_account_id")
    private BankAccount receivingAccount;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private InventoryOrder order;

    // Set only by the installment service (never from the request body): the instalment this payment settles.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_installment_id")
    @JsonIgnore
    private OrderInstallment installment;

    // Bank / UPI reference of the payment (online gateway payments, e.g. Razorpay, will use this later).
    @Column(name = "transaction_reference")
    private String transactionReference;

    @JsonProperty(value = "installmentId", access = JsonProperty.Access.READ_ONLY)
    public Long getInstallmentId() {
        return installment == null ? null : installment.getId();
    }

    public InventoryPayment() {
        this.paymentDate = LocalDate.now();
    }
}