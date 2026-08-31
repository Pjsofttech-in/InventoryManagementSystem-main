package com.InventoryManagementSystem.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // STORE INFORMATION
    private String storeName;
    private String address;
    private String phoneNumber;
    private String email;
    private String gstin;

    // Bank Details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String upiId;

    // Dynamic Terms & Conditions
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;
}
