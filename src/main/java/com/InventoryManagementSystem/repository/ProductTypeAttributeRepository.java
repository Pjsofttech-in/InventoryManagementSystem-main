package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryProductTypeAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTypeAttributeRepository extends JpaRepository<InventoryProductTypeAttribute, Long> {

    List<InventoryProductTypeAttribute> findByProductTypeId(Long productTypeId);
}