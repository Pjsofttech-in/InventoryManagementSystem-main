package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_bill_id", nullable = false)
    @JsonIgnore
    private VendorBill vendorBill;

    // Set only when this payment settles an instalment of the bill.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    @JsonIgnore
    private VendorBillInstallment installment;

    @JsonProperty(value = "installmentId", access = JsonProperty.Access.READ_ONLY)
    public Long getInstallmentId() {
        return installment == null ? null : installment.getId();
    }

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String transactionReference;

    @Column(length = 2000)
    private String notes;
}
