package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_stock_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER + @JsonIgnore: the frontend already knows which product this batch belongs to
    // (it's requested via /api/stock-batches/product/{productId}), so we never serialize it.
    // This also avoids any LazyInitializationException risk during JSON serialization.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private InventoryProduct product;

    // EAGER so the vendor name is always safely available for the batch/history views,
    // regardless of when Jackson serializes this entity relative to the Hibernate session.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private InventoryVendor vendor;

    @Column(nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private BigDecimal vendorMrp;

    @Column(nullable = false)
    private BigDecimal vendorDiscountPercentage;

    // Vendor discount in rupees per unit (on productMrp).
    private BigDecimal vendorDiscountAmount;

    @Column(nullable = false)
    private BigDecimal purchasePrice;

    // Actual Product MRP (real MRP printed on the product) at the time of this batch.
    private BigDecimal productMrp;

    @Column(nullable = false)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private Integer initialQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    private LocalDate receivedDate;

    @Builder.Default
    private Boolean isDeleted = false;
}