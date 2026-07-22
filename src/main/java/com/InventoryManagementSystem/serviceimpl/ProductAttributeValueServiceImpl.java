package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.ProductAttributeValueResponseDTO;
import com.InventoryManagementSystem.entity.InventoryAttribute;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.InventoryProductAttributeValue;
import com.InventoryManagementSystem.repository.AttributeRepository;
import com.InventoryManagementSystem.repository.ProductAttributeValueRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.ProductAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {

    @Autowired
    private ProductAttributeValueRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Override
    public ProductAttributeValueResponseDTO save(InventoryProductAttributeValue pav) {

        InventoryProduct product = productRepository.findById(
                pav.getProduct().getId()
        ).orElseThrow(() -> new RuntimeException("Product not found"));

        InventoryAttribute attribute = attributeRepository.findById(
                pav.getAttribute().getId()
        ).orElseThrow(() -> new RuntimeException("Attribute not found"));

        pav.setProduct(product);
        pav.setAttribute(attribute);

        InventoryProductAttributeValue saved = repository.save(pav);

        return new ProductAttributeValueResponseDTO(
                saved.getId(),
                new ProductAttributeValueResponseDTO.ProductDTO(
                        product.getId(),
                        product.getProductName()
                ),
                new ProductAttributeValueResponseDTO.AttributeDTO(
                        attribute.getId(),
                        attribute.getName()
                ),
                saved.getValue()
        );
    }
}