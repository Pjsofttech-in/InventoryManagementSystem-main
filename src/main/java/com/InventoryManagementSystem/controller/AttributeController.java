package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryAttribute;
import com.InventoryManagementSystem.service.AttributeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class AttributeController
{

    @Autowired
    private AttributeService service;

    @PostMapping("/createAttribute")
    public InventoryAttribute createAttribute(@RequestBody InventoryAttribute attribute)
    {
        return service.saveAttribute(attribute);
    }


    @GetMapping("/by-product-type/{id}")
    public List<InventoryAttribute> getByProductType(@PathVariable Long id)
    {
        return service.getAttributesByProductType(id);
    }
}