package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.StockUpdateRequest;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.StockHistory;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    InventoryProduct saveProduct(InventoryProduct product);

    List<InventoryProduct> getAllProducts();

    List<InventoryProduct> filterProducts(String productName, Long productTypeId, Long brandId, Long vendorId, Long unitTypeId);

    InventoryProduct getProductById(Long id);

    void deleteProduct(Long id);

    InventoryProduct updateProductStock(Long productId, StockUpdateRequest request);

    List<StockHistory> getProductStockHistory(Long productId);

    // Pagination & Sorting साठी नवीन मेथड
    Page<InventoryProduct> getProductsPaged(int page, int size, String sortBy, String sortDir);
}