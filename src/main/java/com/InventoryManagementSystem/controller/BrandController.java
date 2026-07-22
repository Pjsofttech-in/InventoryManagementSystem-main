package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryBrand;
import com.InventoryManagementSystem.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping("/createBrand")
    public InventoryBrand createBrand(@RequestBody InventoryBrand brand)
    {
        return brandService.saveBrand(brand);
    }

    @GetMapping("/getAllBrands")
    public List<InventoryBrand> getAllBrands()
    {
        return brandService.getAllBrands();
    }

    @GetMapping("/getBrandById/{id}")
    public InventoryBrand getBrandById(@PathVariable Long id)
    {
        return brandService.getBrandById(id);
    }

    @DeleteMapping("/deleteBrand/{id}")
    public String deleteBrand(@PathVariable Long id)
    {
        brandService.deleteBrand(id);
        return "Brand ID " + id + " deleted successfully";
    }
}