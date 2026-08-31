package com.InventoryManagementSystem.DTO;

import lombok.Data;

@Data
public class BankDetailsDTO {
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String upiId;
}