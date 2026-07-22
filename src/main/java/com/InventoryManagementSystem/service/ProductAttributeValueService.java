package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.ProductAttributeValueResponseDTO;
import com.InventoryManagementSystem.entity.InventoryProductAttributeValue;

public interface ProductAttributeValueService {
    ProductAttributeValueResponseDTO save(InventoryProductAttributeValue value);
}