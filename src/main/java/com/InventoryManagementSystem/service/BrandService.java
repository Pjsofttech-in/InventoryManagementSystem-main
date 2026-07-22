package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryBrand;

import java.util.List;

public interface BrandService {

    InventoryBrand saveBrand(InventoryBrand brand);

    List<InventoryBrand> getAllBrands();

    InventoryBrand getBrandById(Long id);

    void deleteBrand(Long id);
}