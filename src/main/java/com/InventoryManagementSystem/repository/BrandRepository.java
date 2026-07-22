package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<InventoryBrand, Long> {

}