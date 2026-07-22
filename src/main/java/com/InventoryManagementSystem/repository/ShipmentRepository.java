package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<InventoryShipment, Long> {
}