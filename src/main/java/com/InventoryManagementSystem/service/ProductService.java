package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.StockUpdateRequest;
import com.InventoryManagementSystem.DTO.ProductSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.StockHistory;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    InventoryProduct saveProduct(InventoryProduct product);

    List<InventoryProduct> getAllProducts();

    List<InventoryProduct> filterProducts(String productName, Long productTypeId, Long brandId, Long vendorId, Long unitTypeId, Long subCategoryId);

    InventoryProduct getProductById(Long id);

    void deleteProduct(Long id);

    InventoryProduct updateProductStock(Long productId, StockUpdateRequest request);

    List<StockHistory> getProductStockHistory(Long productId);

    // Grand totals of all product columns (stock, remaining, sold, amounts)
    ProductSummaryDTO getProductsSummary();

    // Pagination & Sorting साठी नवीन मेथड
    Page<InventoryProduct> getProductsPaged(int page, int size, String sortBy, String sortDir);
}