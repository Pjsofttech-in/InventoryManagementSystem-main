package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One scheduled instalment of a customer order (money the store will RECEIVE).
 * What was actually paid is stored as {@link InventoryPayment} rows that point to this instalment,
 * so the normal order paid / pending amounts keep working.
 */
@Entity
@Getter
@Setter
@Table(name = "order_installments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "installment_number"}))
public class OrderInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private InventoryOrder order;

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
