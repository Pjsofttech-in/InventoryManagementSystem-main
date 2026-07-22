package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController

public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;


    @PostMapping("/createShipment")
    public InventoryShipment createShipment(@RequestBody InventoryShipment shipment)
    {
        return shipmentService.saveShipment(shipment);
    }

    @GetMapping("/getAllShipments")
    public List<InventoryShipment> getAllShipments()
    {
        return shipmentService.getAllShipments();
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