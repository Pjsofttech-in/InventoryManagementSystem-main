package com.InventoryManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopCategoryDTO {
    private String categoryName;
    private Long totalQuantitySold;
}
