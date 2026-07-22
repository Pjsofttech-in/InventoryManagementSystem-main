package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryProductType;
import com.InventoryManagementSystem.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController

public class ProductTypeController {

    @Autowired
    private ProductTypeService productTypeService;

    @PostMapping("/createProductType")
    public InventoryProductType createProductType(@RequestBody InventoryProductType productType)
    {
        return productTypeService.saveProductType(productType);
    }

    @GetMapping("/getAllProductTypes")
    public List<InventoryProductType> getAllProductTypes()
    {
        return productTypeService.getAllProductTypes();
    }

    @GetMapping("/getProductTypeById/{id}")
    public ResponseEntity<?> getProductTypeById(@PathVariable Long id)
    {
        InventoryProductType pt = productTypeService.getProductTypeById(id);

        if(pt == null)
        {
            return ResponseEntity.status(404).body("Product Type Not Found");
        }

        return ResponseEntity.ok(pt);
    }

    @DeleteMapping("/deleteProductType/{id}")
    public String deleteProductType(@PathVariable Long id)
    {
        productTypeService.deleteProductType(id);
        return "ProductType ID " + id + " deleted successfully";
    }
}