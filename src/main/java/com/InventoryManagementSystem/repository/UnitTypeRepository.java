package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitTypeRepository extends JpaRepository<InventoryUnitType, Long> {
}