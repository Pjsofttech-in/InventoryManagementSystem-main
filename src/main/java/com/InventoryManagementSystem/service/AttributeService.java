package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryAttribute;
import java.util.List;

public interface AttributeService {

    List<InventoryAttribute> getAttributesByProductType(Long productTypeId);

    InventoryAttribute saveAttribute(InventoryAttribute attribute);
}
