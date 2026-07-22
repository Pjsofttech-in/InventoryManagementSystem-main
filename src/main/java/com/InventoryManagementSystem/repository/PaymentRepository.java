package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<InventoryPayment, Long> {
}