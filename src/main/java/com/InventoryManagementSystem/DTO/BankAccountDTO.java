package com.InventoryManagementSystem.DTO;
import com.InventoryManagementSystem.Enum.BankAccountType;
import lombok.Data;
@Data
public class BankAccountDTO {
    private Long id;
    private String accountName;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String upiId;
    private BankAccountType accountType;
    private Boolean acceptingPayments = true;
    private Boolean active = true;
    private Boolean defaultAccount = false;
}
