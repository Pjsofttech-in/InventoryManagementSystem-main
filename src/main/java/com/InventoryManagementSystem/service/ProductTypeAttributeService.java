package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryProductTypeAttribute;
import java.util.List;

public interface ProductTypeAttributeService {

    InventoryProductTypeAttribute save(InventoryProductTypeAttribute pta);

    List<InventoryProductTypeAttribute> getAll();

    InventoryProductTypeAttribute getById(Long id);

    void delete(Long id);
}