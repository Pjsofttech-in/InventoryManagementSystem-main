package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.ShipmentSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryShipment;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentService {

    InventoryShipment saveShipment(InventoryShipment shipment);

    List<InventoryShipment> getAllShipments();

    /**
     * Search + filter used by the Shipments PDF/CSV export (and available to the list).
     * Every argument is optional.
     *
     * @param search   text matched against shipment name/id, tracking id, order id, customer name/mobile, courier, employee
     * @param status   ShipmentStatus name
     * @param fromDate shipment date from (inclusive)
     * @param toDate   shipment date to (inclusive)
     */
    List<InventoryShipment> filterShipments(String search, String status, LocalDate fromDate, LocalDate toDate);

    InventoryShipment getShipmentById(Long id);

    void deleteShipment(Long id);

    // Grand total of shipment cost across all shipments (cancelled shipments excluded)
    ShipmentSummaryDTO getShipmentsSummary();
}