package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "vendors")
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

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InventoryProduct> products;

    public InventoryVendor() {}
}