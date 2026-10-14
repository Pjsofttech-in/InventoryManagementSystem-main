package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryShippedBy;
import com.InventoryManagementSystem.service.ShippedByService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
public class ShippedByController {

    @Autowired
    private ShippedByService shippedByService;

    // Create a Courier / Company Name + Tracking Link entry
    @PostMapping("/createShippedBy")
    public InventoryShippedBy createShippedBy(@RequestBody InventoryShippedBy shippedBy)
    {
        return shippedByService.saveShippedBy(shippedBy);
    }

    @GetMapping("/getAllShippedBy")
    public List<InventoryShippedBy> getAllShippedBy()
    {
        return shippedByService.getAllShippedBy();
    }

    @GetMapping("/getShippedById/{id}")
    public InventoryShippedBy getShippedById(@PathVariable Long id)
    {
        return shippedByService.getShippedById(id);
    }

    @DeleteMapping("/deleteShippedBy/{id}")
    public String deleteShippedBy(@PathVariable Long id)
    {
        shippedByService.deleteShippedBy(id);
        return "ShippedBy ID " + id + " deleted successfully";
    }
}
