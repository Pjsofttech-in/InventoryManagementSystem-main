package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.ProductTypeWithSubCategoriesDTO;
import com.InventoryManagementSystem.entity.InventoryProductType;
import com.InventoryManagementSystem.entity.InventorySubCategory;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.repository.ProductTypeRepository;
import com.InventoryManagementSystem.repository.SubCategoryRepository;
import com.InventoryManagementSystem.service.SubCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;

    public SubCategoryServiceImpl(SubCategoryRepository subCategoryRepository,
                                  ProductTypeRepository productTypeRepository,
                                  ProductRepository productRepository) {
        this.subCategoryRepository = subCategoryRepository;
        this.productTypeRepository = productTypeRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public InventorySubCategory createSubCategory(InventorySubCategory request) {
        String name = cleanName(request);
        InventoryProductType type = findType(request);

        if (subCategoryRepository.existsByProductTypeIdAndSubCategoryNameIgnoreCase(type.getId(), name)) {
            throw new RuntimeException("Sub category '" + name + "' already exists in " + type.getTypeName());
        }
        InventorySubCategory sub = new InventorySubCategory();
        sub.setSubCategoryName(name);
        sub.setDescription(blankToNull(request.getDescription()));
        sub.setProductType(type);
        return subCategoryRepository.save(sub);
    }

    @Override
    @Transactional
    public InventorySubCategory updateSubCategory(Long id, InventorySubCategory request) {
        InventorySubCategory sub = getSubCategoryById(id);
        String name = cleanName(request);
        InventoryProductType type = request.getProductType() != null && request.getProductType().getId() != null
                ? findType(request) : sub.getProductType();

        if (subCategoryRepository.existsByProductTypeIdAndSubCategoryNameIgnoreCaseAndIdNot(type.getId(), name, id)) {
            throw new RuntimeException("Sub category '" + name + "' already exists in " + type.getTypeName());
        }
        // Products keep their sub category, so it may not move to another product type while in use.
        if (!type.getId().equals(sub.getProductType().getId()) && productRepository.countBySubCategoryId(id) > 0) {
            throw new RuntimeException("This sub category is used by products, so it cannot be moved to another product type");
        }
        sub.setSubCategoryName(name);
        sub.setDescription(blankToNull(request.getDescription()));
        sub.setProductType(type);
        return subCategoryRepository.save(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventorySubCategory> getAllSubCategories() {
        return subCategoryRepository.findAllByOrderByProductTypeTypeNameAscSubCategoryNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventorySubCategory> getSubCategoriesByProductType(Long productTypeId) {
        if (!productTypeRepository.existsById(productTypeId)) {
            throw new RuntimeException("ProductType not found with ID: " + productTypeId);
        }
        return subCategoryRepository.findByProductTypeIdOrderBySubCategoryNameAsc(productTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySubCategory getSubCategoryById(Long id) {
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub category not found with ID: " + id));
    }

    @Override
    @Transactional
    public void deleteSubCategory(Long id) {
        InventorySubCategory sub = getSubCategoryById(id);
        long used = productRepository.countBySubCategoryId(id);
        if (used > 0) {
            throw new RuntimeException("Sub category '" + sub.getSubCategoryName() + "' is used by " + used
                    + " product(s). Change those products first");
        }
        subCategoryRepository.delete(sub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductTypeWithSubCategoriesDTO> getProductTypesWithSubCategories() {
        List<InventorySubCategory> all = subCategoryRepository.findAllByOrderByProductTypeTypeNameAscSubCategoryNameAsc();
        return productTypeRepository.findAll().stream()
                .map(type -> new ProductTypeWithSubCategoriesDTO(type.getId(), type.getTypeName(),
                        all.stream()
                                .filter(s -> s.getProductType().getId().equals(type.getId()))
                                .map(s -> new ProductTypeWithSubCategoriesDTO.SubCategoryItem(
                                        s.getId(), s.getSubCategoryName(), s.getDescription()))
                                .toList()))
                .toList();
    }

    // ------------------------------------------------------------------ helpers

    private String cleanName(InventorySubCategory request) {
        if (request == null || request.getSubCategoryName() == null || request.getSubCategoryName().isBlank()) {
            throw new RuntimeException("Sub category name is required");
        }
        return request.getSubCategoryName().trim();
    }

    private InventoryProductType findType(InventorySubCategory request) {
        if (request.getProductType() == null || request.getProductType().getId() == null) {
            throw new RuntimeException("productType.id is required: a sub category belongs to a product type");
        }
        return productTypeRepository.findById(request.getProductType().getId())
                .orElseThrow(() -> new RuntimeException("ProductType not found with ID: " + request.getProductType().getId()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
