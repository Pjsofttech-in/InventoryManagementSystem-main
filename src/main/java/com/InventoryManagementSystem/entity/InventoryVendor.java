package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "vendors")
// Overrides physical DELETE with an UPDATE query
@SQLDelete(sql = "UPDATE vendors SET deleted = true WHERE id = ?")
// Automatically filters out deleted records for all SELECT queries
@SQLRestriction("deleted = false")
public class InventoryVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vendorName;

    private String companyName;

    @Column(unique = true)
    private String gstNo;

    private String landmark;
    private String city;
    private String taluka;

    @Enumerated(EnumType.STRING)
    private State state;

    private String pincode;

    @Column(unique = true)
    private String email;

    private String phone;

    // Soft delete status flag
    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InventoryProduct> products;

    public InventoryVendor() {}
}