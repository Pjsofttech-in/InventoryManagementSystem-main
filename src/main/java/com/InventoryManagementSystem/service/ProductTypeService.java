package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryProductType;

import java.util.List;

public interface ProductTypeService {

    InventoryProductType saveProductType(InventoryProductType productType);

    List<InventoryProductType> getAllProductTypes();

    InventoryProductType getProductTypeById(Long id);

    void deleteProductType(Long id);
}