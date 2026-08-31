package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.StockPurchaseRequestDTO;
import com.InventoryManagementSystem.entity.ProductStockBatch;

import java.util.List;

public interface StockBatchService {

    ProductStockBatch purchaseStock(StockPurchaseRequestDTO request);

    List<ProductStockBatch> getBatchesByProductId(Long productId);

    List<ProductStockBatch> getActiveBatchesByProductId(Long productId);

    List<ProductStockBatch> getExpiringBatches(Integer daysThreshold);

    List<ProductStockBatch> getAllBatches();

    void softDeleteBatch(Long batchId);
}