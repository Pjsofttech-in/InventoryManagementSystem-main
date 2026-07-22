package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryProduct;
import java.util.List;

public interface ProductService {

    InventoryProduct saveProduct(InventoryProduct product);

    List<InventoryProduct> getAllProducts();

    InventoryProduct getProductById(Long id);

    void deleteProduct(Long id);
}