package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<InventoryVendor, Long> {
}