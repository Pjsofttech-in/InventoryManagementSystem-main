package com.InventoryManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDTO {
    private String productName;
    private Long totalQuantitySold;
}