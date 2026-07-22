package com.InventoryManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductAttributeValueResponseDTO {

    private Long id;
    private ProductDTO product;
    private AttributeDTO attribute;
    private String value;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ProductDTO {
        private Long id;
        private String productName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AttributeDTO {
        private Long id;
        private String name;
    }
}