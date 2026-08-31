package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {
    private Long productId;
    private Integer quantity;
    private Double customerDiscountPercentage; // Customer Discount % (Optional)
    private Double price;                       // Custom Price (Optional)
}