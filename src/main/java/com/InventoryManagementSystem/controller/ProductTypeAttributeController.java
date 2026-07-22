package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryProductTypeAttribute;
import com.InventoryManagementSystem.service.ProductTypeAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class ProductTypeAttributeController {

    @Autowired
    private ProductTypeAttributeService service;

    @PostMapping("/createProductTypeAttribute")
    public InventoryProductTypeAttribute create(@RequestBody InventoryProductTypeAttribute pta)
    {
        return service.save(pta);
    }

    @GetMapping("/getAllProductTypeAttribute")
    public List<InventoryProductTypeAttribute> getAll()
    {
        return service.getAll();
    }

    @GetMapping("/getByIdProductTypeAttribute/{id}")
    public InventoryProductTypeAttribute getById(@PathVariable Long id)
    {
        return service.getById(id);
    }

    @DeleteMapping("/deleteProductTypeAttribute/{id}")
    public String delete(@PathVariable Long id)
    {
        service.delete(id);
        return " ProductTypeAttribute ID " + id + " deleted successfully";
    }
}