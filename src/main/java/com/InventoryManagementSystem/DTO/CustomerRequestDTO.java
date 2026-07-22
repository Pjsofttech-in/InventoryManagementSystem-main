package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDTO {

    private String customerName;
    private String landmark;
    private String city;
    private String taluka;
    private String pincode;
    private String email;
    private String phone;
    private String state;
    private Long vendorId;
}