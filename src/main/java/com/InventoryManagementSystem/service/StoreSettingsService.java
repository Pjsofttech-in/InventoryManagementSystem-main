package com.InventoryManagementSystem.service;



import com.InventoryManagementSystem.DTO.BankDetailsDTO;
import com.InventoryManagementSystem.DTO.StoreProfileDTO;
import com.InventoryManagementSystem.DTO.TermsAndConditionsDTO;
import com.InventoryManagementSystem.entity.StoreSettings;
import com.InventoryManagementSystem.DTO.BankAccountDTO;
import java.util.List;

public interface StoreSettingsService {
    StoreSettings getStoreSettings();


    BankDetailsDTO getBankDetails();
    StoreSettings updateBankDetails(BankDetailsDTO dto);


    TermsAndConditionsDTO getTermsAndConditions();
    StoreSettings updateTermsAndConditions(TermsAndConditionsDTO dto);


    StoreProfileDTO getStoreProfile();
    StoreSettings updateStoreProfile(StoreProfileDTO dto);

    List<BankAccountDTO> getBankAccounts();
    List<BankAccountDTO> getPaymentReceivingAccounts();
    BankAccountDTO createBankAccount(BankAccountDTO dto);
    BankAccountDTO updateBankAccount(Long id, BankAccountDTO dto);
    void deleteBankAccount(Long id);
}