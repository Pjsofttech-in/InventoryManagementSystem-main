package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<InventoryProductAttributeValue, Long> {
}