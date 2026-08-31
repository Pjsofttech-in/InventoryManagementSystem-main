package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.StockUpdateRequest;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.StockHistory;
import com.InventoryManagementSystem.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<InventoryProduct> createProduct(@RequestBody InventoryProduct product) {
        InventoryProduct savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InventoryProduct>> getProducts(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long productTypeId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long unitTypeId
    ) {
        List<InventoryProduct> products = productService.filterProducts(
                productName, productTypeId, brandId, vendorId, unitTypeId
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryProduct> getProductById(@PathVariable Long id) {
        InventoryProduct product = productService.getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product ID " + id + " deactivated successfully"));
    }

    @PutMapping("/{id}/add-stock")
    public ResponseEntity<InventoryProduct> updateStock(
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {

        InventoryProduct updatedProduct = productService.updateProductStock(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{id}/stock-history")
    public ResponseEntity<List<StockHistory>> getStockHistory(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductStockHistory(id));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<InventoryProduct>> getProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(productService.getProductsPaged(page, size, sortBy, sortDir));
    }
}