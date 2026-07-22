package com.InventoryManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "product_type_attributes")
public class InventoryProductTypeAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_type_id")
    private InventoryProductType productType;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private InventoryAttribute attribute;
}