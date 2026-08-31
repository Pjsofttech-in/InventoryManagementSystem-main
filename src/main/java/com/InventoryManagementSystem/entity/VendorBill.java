package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vendor_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String billNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
    private InventoryVendor vendor;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_batch_id", unique = true)
    @JsonIgnoreProperties({"product", "vendor", "hibernateLazyInitializer", "handler"})
    private ProductStockBatch stockBatch;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private BigDecimal dueAmount;

    private LocalDate billDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Builder.Default
    private Boolean isDeleted = false;

    @Transient
    public String getPurchaseProductName() {
        if (stockBatch == null || stockBatch.getProduct() == null) return null;
        return stockBatch.getProduct().getProductName();
    }

    @Transient
    public Integer getPurchaseQuantity() {
        return stockBatch != null ? stockBatch.getInitialQuantity() : null;
    }

    @Transient
    public BigDecimal getPurchaseUnitPrice() {
        return stockBatch != null ? stockBatch.getPurchasePrice() : null;
    }

    @Transient
    public String getPurchaseBatchNumber() {
        return stockBatch != null ? stockBatch.getBatchNumber() : null;
    }
}

