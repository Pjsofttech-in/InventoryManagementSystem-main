package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryProductType;
import com.InventoryManagementSystem.repository.ProductTypeRepository;
import com.InventoryManagementSystem.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Override
    public InventoryProductType saveProductType(InventoryProductType productType) {
        return productTypeRepository.save(productType);
    }

    @Override
    public List<InventoryProductType> getAllProductTypes() {
        return productTypeRepository.findAll();
    }

    @Override
    public InventoryProductType getProductTypeById(Long id) {
        return productTypeRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteProductType(Long id) {
        productTypeRepository.deleteById(id);
    }
}