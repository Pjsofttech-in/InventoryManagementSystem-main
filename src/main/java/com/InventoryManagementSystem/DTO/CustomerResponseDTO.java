package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponseDTO {

    private Long id;
    private String customerName;
    private String city;
    private String state;
    private String email;
}