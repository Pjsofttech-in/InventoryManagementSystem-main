package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryBrand;
import com.InventoryManagementSystem.repository.BrandRepository;
import com.InventoryManagementSystem.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public InventoryBrand saveBrand(InventoryBrand brand)
    {
        return brandRepository.save(brand);
    }

    @Override
    public List<InventoryBrand> getAllBrands()
    {
        return brandRepository.findAll();
    }

    @Override
    public InventoryBrand getBrandById(Long id)
    {
        return brandRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteBrand(Long id)
    {
        brandRepository.deleteById(id);
    }
}