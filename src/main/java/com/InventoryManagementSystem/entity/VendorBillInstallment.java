package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One scheduled instalment of a vendor bill (money the store will PAY to the vendor).
 * What was actually paid is stored as {@link VendorPaymentHistory} rows that point to this instalment,
 * so the bill's paid / due amounts keep working.
 */
@Entity
@Getter
@Setter
@Table(name = "vendor_bill_installments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_bill_id", "installment_number"}))
public class VendorBillInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_bill_id", nullable = false)
    @JsonIgnore
    private VendorBill vendorBill;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dueAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status = InstallmentStatus.PENDING;
}
