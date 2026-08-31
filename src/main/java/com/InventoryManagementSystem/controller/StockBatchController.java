package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.StockPurchaseRequestDTO;
import com.InventoryManagementSystem.entity.ProductStockBatch;
import com.InventoryManagementSystem.service.StockBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-batches")
@CrossOrigin(origins = "*")
public class StockBatchController {

    @Autowired
    private StockBatchService stockBatchService;

    @PostMapping("/purchase")
    public ResponseEntity<ProductStockBatch> purchaseStock(@RequestBody StockPurchaseRequestDTO request) {
        ProductStockBatch createdBatch = stockBatchService.purchaseStock(request);
        return new ResponseEntity<>(createdBatch, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductStockBatch>> getAllBatches() {
        return ResponseEntity.ok(stockBatchService.getAllBatches());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductStockBatch>> getBatchesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockBatchService.getBatchesByProductId(productId));
    }

    @GetMapping("/product/{productId}/active")
    public ResponseEntity<List<ProductStockBatch>> getActiveBatchesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockBatchService.getActiveBatchesByProductId(productId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ProductStockBatch>> getExpiringBatches(
            @RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(stockBatchService.getExpiringBatches(days));
    }

    @DeleteMapping("/{batchId}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long batchId) {
        stockBatchService.softDeleteBatch(batchId);
        return ResponseEntity.noContent().build();
    }
}