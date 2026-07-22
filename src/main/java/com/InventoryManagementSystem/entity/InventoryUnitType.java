package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "unit_types")
public class InventoryUnitType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitType;

    @OneToMany(mappedBy = "unitType")
    @JsonIgnore
    private List<InventoryProduct> products;

    public InventoryUnitType() {}
}