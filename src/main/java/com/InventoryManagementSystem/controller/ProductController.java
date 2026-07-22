package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/createProduct")
    public InventoryProduct createProduct(@RequestBody InventoryProduct product)
    {
        return productService.saveProduct(product);
    }

    @GetMapping("/getAllProducts")
    public List<InventoryProduct> getAllProducts()
    {
        return productService.getAllProducts();
    }

    @GetMapping("/getProductById/{id}")
    public InventoryProduct getProductById(@PathVariable Long id)
    {
        return productService.getProductById(id);
    }

    @DeleteMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable Long id)
    {
        productService.deleteProduct(id);
        return "Product ID " + id + " deleted successfully";
    }
}