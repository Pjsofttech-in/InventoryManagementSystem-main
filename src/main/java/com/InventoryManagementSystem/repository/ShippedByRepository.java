package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryShippedBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippedByRepository extends JpaRepository<InventoryShippedBy, Long> {
}
