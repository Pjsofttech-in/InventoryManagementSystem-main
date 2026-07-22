package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<InventoryOrderItem, Long> {
}