package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.ProductStockBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockBatchRepository extends JpaRepository<ProductStockBatch, Long> {

    List<ProductStockBatch> findByProductIdAndIsDeletedFalse(Long productId);

    List<ProductStockBatch> findByVendorIdAndIsDeletedFalse(Long vendorId);

    List<ProductStockBatch> findByIsDeletedFalse();

    // FIFO query for active stock batches
    List<ProductStockBatch> findByProductIdAndAvailableQuantityGreaterThanAndIsDeletedFalseOrderByReceivedDateAsc(Long productId, Integer minQty);

    // All active batches, including fully consumed batches. Required when stock
    // is restored after an order update/delete/cancellation.
    List<ProductStockBatch> findByProductIdAndIsDeletedFalseOrderByReceivedDateAsc(Long productId);
}