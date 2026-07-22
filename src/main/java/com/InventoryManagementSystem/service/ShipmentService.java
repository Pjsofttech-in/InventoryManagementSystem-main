package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryShipment;

import java.util.List;

public interface ShipmentService {

    InventoryShipment saveShipment(InventoryShipment shipment);

    List<InventoryShipment> getAllShipments();

    InventoryShipment getShipmentById(Long id);

    void deleteShipment(Long id);
}