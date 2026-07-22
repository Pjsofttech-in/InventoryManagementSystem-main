package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeRepository extends JpaRepository<InventoryProductType, Long> {
}