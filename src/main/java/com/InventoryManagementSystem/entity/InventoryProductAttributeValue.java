package com.InventoryManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product_attribute_values")
public class InventoryProductAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private InventoryProduct product;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private InventoryAttribute attribute;

    private String value;
}