package com.InventoryManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Dynamic sub category that belongs to ONE product type (category).
 * Example: product type "Electronics" -> sub categories "Mobiles", "Laptops", "Accessories".
 * Deleting is a soft delete, so old products never lose their link.
 */
@Entity
@Getter
@Setter
@Table(name = "product_sub_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_type_id", "sub_category_name"}))
@SQLDelete(sql = "UPDATE product_sub_categories SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class InventorySubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_category_name", nullable = false)
    private String subCategoryName;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_type_id", nullable = false)
    private InventoryProductType productType;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public InventorySubCategory() {}
}
