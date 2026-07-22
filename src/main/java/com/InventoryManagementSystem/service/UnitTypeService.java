package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryUnitType;

import java.util.List;

public interface UnitTypeService {

    InventoryUnitType saveUnitType(InventoryUnitType unitType);

    List<InventoryUnitType> getAllUnitTypes();

    InventoryUnitType getUnitTypeById(Long id);

    void deleteUnitType(Long id);
}