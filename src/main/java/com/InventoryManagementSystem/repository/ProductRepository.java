package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<InventoryProduct, Long>,
        JpaSpecificationExecutor<InventoryProduct> {
}