package com.InventoryManagementSystem.controller;




import com.InventoryManagementSystem.DTO.BankDetailsDTO;
import com.InventoryManagementSystem.DTO.BankAccountDTO;
import java.util.List;
import com.InventoryManagementSystem.DTO.StoreProfileDTO;
import com.InventoryManagementSystem.DTO.TermsAndConditionsDTO;
import com.InventoryManagementSystem.entity.StoreSettings;
import com.InventoryManagementSystem.service.StoreSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/store-settings")
public class StoreSettingsController {

    @Autowired
    private StoreSettingsService storeSettingsService;


    @GetMapping("/profile")
    public ResponseEntity<StoreProfileDTO> getStoreProfile() {
        return ResponseEntity.ok(storeSettingsService.getStoreProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<StoreSettings> updateStoreProfile(@RequestBody StoreProfileDTO dto) {
        return ResponseEntity.ok(storeSettingsService.updateStoreProfile(dto));
    }


    @GetMapping("/bank-details")
    public ResponseEntity<BankDetailsDTO> getBankDetails() {
        return ResponseEntity.ok(storeSettingsService.getBankDetails());
    }

    @PutMapping("/bank-details")
    public ResponseEntity<StoreSettings> updateBankDetails(@RequestBody BankDetailsDTO dto) {
        return ResponseEntity.ok(storeSettingsService.updateBankDetails(dto));
    }


    @GetMapping("/bank-accounts")
    public ResponseEntity<List<BankAccountDTO>> getBankAccounts() { return ResponseEntity.ok(storeSettingsService.getBankAccounts()); }

    @GetMapping("/bank-accounts/payment-receiving")
    public ResponseEntity<List<BankAccountDTO>> getPaymentReceivingAccounts() { return ResponseEntity.ok(storeSettingsService.getPaymentReceivingAccounts()); }

    @PostMapping("/bank-accounts")
    public ResponseEntity<BankAccountDTO> createBankAccount(@RequestBody BankAccountDTO dto) { return ResponseEntity.ok(storeSettingsService.createBankAccount(dto)); }

    @PutMapping("/bank-accounts/{id}")
    public ResponseEntity<BankAccountDTO> updateBankAccount(@PathVariable Long id, @RequestBody BankAccountDTO dto) { return ResponseEntity.ok(storeSettingsService.updateBankAccount(id, dto)); }

    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long id) { storeSettingsService.deleteBankAccount(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/terms-and-conditions")
    public ResponseEntity<TermsAndConditionsDTO> getTermsAndConditions() {
        return ResponseEntity.ok(storeSettingsService.getTermsAndConditions());
    }

    @PutMapping("/terms-and-conditions")
    public ResponseEntity<StoreSettings> updateTermsAndConditions(@RequestBody TermsAndConditionsDTO dto) {
        return ResponseEntity.ok(storeSettingsService.updateTermsAndConditions(dto));
    }
}