package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.ShipmentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Cost required to send the parcel (courier / freight charge) in rupees.
    private Double shipmentCost = 0.0;

    // Employee who handed over / shipped the parcel to the courier.
    private String employeeName;

    private LocalDate shipmentDate;
    private String shipmentAddress;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    private String customerName;
    private String customerMobile;
    private String customerAddress;
    private String companyName;

    // Master "Shipped By" record this shipment's courier/tracking info was fetched from.
    // companyName/trackingLink above are kept in sync with this record for display & history.
    @ManyToOne
    @JoinColumn(name = "shipped_by_id")
    private InventoryShippedBy shippedBy;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonBackReference
    private InventoryOrder order;

    public InventoryShipment() {
        this.shipmentDate = LocalDate.now();
    }

    /** The order itself is not serialized (back reference), so lists / receipts get its id here. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getOrderId() {
        return order == null ? null : order.getId();
    }
}
