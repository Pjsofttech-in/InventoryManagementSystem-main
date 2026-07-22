package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryAttribute;
import com.InventoryManagementSystem.entity.InventoryProductType;
import com.InventoryManagementSystem.entity.InventoryProductTypeAttribute;
import com.InventoryManagementSystem.repository.AttributeRepository;
import com.InventoryManagementSystem.repository.ProductTypeAttributeRepository;
import com.InventoryManagementSystem.repository.ProductTypeRepository;
import com.InventoryManagementSystem.service.ProductTypeAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductTypeAttributeServiceImpl implements ProductTypeAttributeService {

    @Autowired
    private ProductTypeAttributeRepository repository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Override
    public InventoryProductTypeAttribute save(InventoryProductTypeAttribute pta) {

        Long productTypeId = pta.getProductType().getId();
        Long attributeId = pta.getAttribute().getId();


        InventoryProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new RuntimeException("ProductType not found"));

        InventoryAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));


        pta.setProductType(productType);
        pta.setAttribute(attribute);

        return repository.save(pta);
    }

    @Override
    public List<InventoryProductTypeAttribute> getAll() {
        return repository.findAll();
    }

    @Override
    public InventoryProductTypeAttribute getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
