package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<InventoryAttribute, Long> {
}