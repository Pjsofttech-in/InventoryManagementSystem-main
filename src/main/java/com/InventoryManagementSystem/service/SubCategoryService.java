package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.ProductTypeWithSubCategoriesDTO;
import com.InventoryManagementSystem.entity.InventorySubCategory;

import java.util.List;

public interface SubCategoryService {

    InventorySubCategory createSubCategory(InventorySubCategory subCategory);

    InventorySubCategory updateSubCategory(Long id, InventorySubCategory subCategory);

    List<InventorySubCategory> getAllSubCategories();

    List<InventorySubCategory> getSubCategoriesByProductType(Long productTypeId);

    InventorySubCategory getSubCategoryById(Long id);

    /** Soft delete; refused while active products still use the sub category. */
    void deleteSubCategory(Long id);

    /** Every product type with its sub categories (for cascading dropdowns). */
    List<ProductTypeWithSubCategoriesDTO> getProductTypesWithSubCategories();
}
