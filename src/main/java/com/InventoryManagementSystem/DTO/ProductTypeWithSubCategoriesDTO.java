package com.InventoryManagementSystem.DTO;

import java.util.List;

/** A product type (category) together with its sub categories - for cascading dropdowns. */
public record ProductTypeWithSubCategoriesDTO(
        Long id,
        String typeName,
        List<SubCategoryItem> subCategories
) {
    public record SubCategoryItem(Long id, String subCategoryName, String description) {}
}
