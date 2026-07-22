package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "shipments")
public class InventoryShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", unique = true)
    private String trackingId;

    private String trackingLink;

    private String shipmentName;

    private LocalDate shipmentDate;

    private String shipmentAddress;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    private String customerName;
    private String customerMobile;
    private String customerAddress;

    private String companyName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private InventoryOrder order;

    public InventoryShipment() {
        this.shipmentDate = LocalDate.now(); // auto date
    }
}