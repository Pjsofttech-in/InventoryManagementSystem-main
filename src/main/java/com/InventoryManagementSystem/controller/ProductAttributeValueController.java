package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.ProductAttributeValueResponseDTO;
import com.InventoryManagementSystem.entity.InventoryProductAttributeValue;
import com.InventoryManagementSystem.service.ProductAttributeValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
    public class ProductAttributeValueController {

    @Autowired
    private ProductAttributeValueService service;

    @PostMapping("/createProductAttributeValue")
    public ProductAttributeValueResponseDTO create(
            @RequestBody InventoryProductAttributeValue value) {
        return service.save(value);
    }
}


