package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryVendor;
import com.InventoryManagementSystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping("/createVendor")
    public InventoryVendor createVendor(@RequestBody InventoryVendor vendor)
    {
        return vendorService.saveVendor(vendor);
    }

    @GetMapping("/getallVendor")
    public List<InventoryVendor> getAllVendors()
    {
        return vendorService.getAllVendors();
    }

    @GetMapping("/getbyIdVendor/{id}")
    public InventoryVendor getVendorById(@PathVariable Long id)
    {
        return vendorService.getVendorById(id);
    }

    @PutMapping("/updateVendor/{id}")
    public InventoryVendor updateVendor(@PathVariable Long id, @RequestBody InventoryVendor vendor)
    {
        return vendorService.updateVendor(id, vendor);
    }

    @DeleteMapping("/deleteVendor/{id}")
    public ResponseEntity<String> deleteVendor(@PathVariable Long id)
    {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok("Vendor Deleted Successflly");
    }
}