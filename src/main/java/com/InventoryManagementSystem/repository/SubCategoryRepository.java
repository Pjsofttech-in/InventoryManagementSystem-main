package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventorySubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<InventorySubCategory, Long> {

    List<InventorySubCategory> findAllByOrderByProductTypeTypeNameAscSubCategoryNameAsc();

    List<InventorySubCategory> findByProductTypeIdOrderBySubCategoryNameAsc(Long productTypeId);

    boolean existsByProductTypeIdAndSubCategoryNameIgnoreCase(Long productTypeId, String subCategoryName);

    boolean existsByProductTypeIdAndSubCategoryNameIgnoreCaseAndIdNot(Long productTypeId, String subCategoryName, Long id);

    boolean existsByProductTypeId(Long productTypeId);
}
