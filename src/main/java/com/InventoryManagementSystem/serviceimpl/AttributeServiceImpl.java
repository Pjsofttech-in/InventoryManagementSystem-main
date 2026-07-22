package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryAttribute;
import com.InventoryManagementSystem.entity.InventoryProductType;
import com.InventoryManagementSystem.repository.AttributeRepository;
import com.InventoryManagementSystem.repository.ProductTypeRepository;
import com.InventoryManagementSystem.service.AttributeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeServiceImpl implements AttributeService {

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Override
    public List<InventoryAttribute> getAttributesByProductType(Long productTypeId) {
        return attributeRepository.findAll()
                .stream()
                .filter(attribute ->
                        attribute.getProductType() != null &&
                                attribute.getProductType().getId().equals(productTypeId))
                .toList();
    }

    @Override
    public InventoryAttribute saveAttribute(InventoryAttribute attribute) {

        InventoryProductType productType = productTypeRepository
                .findById(attribute.getProductType().getId())
                .orElseThrow(() -> new RuntimeException("ProductType not found"));

        attribute.setProductType(productType);

        return attributeRepository.save(attribute);
    }
}