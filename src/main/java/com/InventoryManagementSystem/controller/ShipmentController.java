package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.ShipmentSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.service.ExportService;
import com.InventoryManagementSystem.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController

public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private ExportService exportService;


    @PostMapping("/createShipment")
    public InventoryShipment createShipment(@RequestBody InventoryShipment shipment)
    {
        return shipmentService.saveShipment(shipment);
    }

    @GetMapping("/getAllShipments")
    public List<InventoryShipment> getAllShipments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
    {
        boolean noFilter = (search == null || search.isBlank())
                && (status == null || status.isBlank())
                && fromDate == null && toDate == null;

        return noFilter
                ? shipmentService.getAllShipments()
                : shipmentService.filterShipments(search, status, fromDate, toDate);
    }

    // Download the (filtered) shipments as PDF or CSV. Takes the same filters as /getAllShipments.
    //    e.g. GET /exportShipments?format=csv&status=DELIVERED&fromDate=2026-10-01
    @GetMapping("/exportShipments")
    public ResponseEntity<byte[]> exportShipments(
            @RequestParam String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
    {
        List<InventoryShipment> shipments = shipmentService.filterShipments(search, status, fromDate, toDate);
        return exportService
                .exportShipments(shipments, format, search, status, fromDate, toDate)
                .toResponse();
    }

    // Grand total of shipment cost (cancelled shipments excluded)
    @GetMapping("/getShipmentsSummary")
    public ShipmentSummaryDTO getShipmentsSummary()
    {
        return shipmentService.getShipmentsSummary();
    }

    @GetMapping("/getShipmentById/{id}")
    public InventoryShipment getShipmentById(@PathVariable Long id)
    {
        return shipmentService.getShipmentById(id);
    }

    @DeleteMapping("/deleteShipment/{id}")
    public void deleteShipment(@PathVariable Long id)
    {
        shipmentService.deleteShipment(id);
    }
}