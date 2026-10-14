package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.ProductTypeWithSubCategoriesDTO;
import com.InventoryManagementSystem.entity.InventorySubCategory;
import com.InventoryManagementSystem.service.SubCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Dynamic sub categories of a product type (category). */
@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    public SubCategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @PostMapping("/createSubCategory")
    public InventorySubCategory createSubCategory(@RequestBody InventorySubCategory subCategory) {
        return subCategoryService.createSubCategory(subCategory);
    }

    @PutMapping("/updateSubCategory/{id}")
    public InventorySubCategory updateSubCategory(@PathVariable Long id, @RequestBody InventorySubCategory subCategory) {
        return subCategoryService.updateSubCategory(id, subCategory);
    }

    @GetMapping("/getAllSubCategories")
    public List<InventorySubCategory> getAllSubCategories() {
        return subCategoryService.getAllSubCategories();
    }

    /** Sub categories of one product type, e.g. Electronics -> Mobiles, Laptops ... */
    @GetMapping("/getSubCategoriesByProductType/{productTypeId}")
    public List<InventorySubCategory> getSubCategoriesByProductType(@PathVariable Long productTypeId) {
        return subCategoryService.getSubCategoriesByProductType(productTypeId);
    }

    @GetMapping("/getSubCategoryById/{id}")
    public InventorySubCategory getSubCategoryById(@PathVariable Long id) {
        return subCategoryService.getSubCategoryById(id);
    }

    @DeleteMapping("/deleteSubCategory/{id}")
    public String deleteSubCategory(@PathVariable Long id) {
        subCategoryService.deleteSubCategory(id);
        return "Sub category ID " + id + " deleted successfully";
    }

    /** Every product type with its sub categories - one call for cascading dropdowns. */
    @GetMapping("/getProductTypesWithSubCategories")
    public List<ProductTypeWithSubCategoriesDTO> getProductTypesWithSubCategories() {
        return subCategoryService.getProductTypesWithSubCategories();
    }
}
