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

    private Long vendorId;
    private String vendorName;
    private String companyName;
}