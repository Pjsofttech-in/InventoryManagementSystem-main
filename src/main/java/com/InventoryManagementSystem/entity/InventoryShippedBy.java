package com.InventoryManagementSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "shipped_by")
public class InventoryShippedBy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Courier / Company Name
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "tracking_link", nullable = false)
    private String trackingLink;

    @OneToMany(mappedBy = "shippedBy")
    @JsonIgnore
    private List<InventoryShipment> shipments;

    public InventoryShippedBy() {}
}
