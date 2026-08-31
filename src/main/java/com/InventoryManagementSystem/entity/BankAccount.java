package com.InventoryManagementSystem.entity;

import com.InventoryManagementSystem.Enum.BankAccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "bank_accounts")
public class BankAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountName;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String upiId;
    @Enumerated(EnumType.STRING)
    private BankAccountType accountType = BankAccountType.SAVINGS;
    private Boolean acceptingPayments = true;
    private Boolean active = true;
    @Column(name = "is_default")
    private Boolean defaultAccount = false;
}
