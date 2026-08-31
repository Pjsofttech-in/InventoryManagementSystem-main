package com.InventoryManagementSystem.serviceimpl;





import com.InventoryManagementSystem.DTO.BankDetailsDTO;
import com.InventoryManagementSystem.DTO.StoreProfileDTO;
import com.InventoryManagementSystem.DTO.TermsAndConditionsDTO;
import com.InventoryManagementSystem.entity.StoreSettings;
import com.InventoryManagementSystem.entity.BankAccount;
import com.InventoryManagementSystem.DTO.BankAccountDTO;
import com.InventoryManagementSystem.repository.BankAccountRepository;
import java.util.List;
import com.InventoryManagementSystem.repository.StoreSettingsRepository;
import com.InventoryManagementSystem.service.StoreSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreSettingsServiceImpl implements StoreSettingsService {

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public StoreSettings getStoreSettings() {
        return storeSettingsRepository.findAll().stream().findFirst()
                .orElseGet(() -> storeSettingsRepository.save(
                        StoreSettings.builder()
                                .storeName("My Store")
                                .termsAndConditions("1. Goods once sold will not be taken back.")
                                .build()
                ));
    }

    // --- BANK DETAILS IMPLEMENTATION ---
    @Override
    public BankDetailsDTO getBankDetails() {
        StoreSettings settings = getStoreSettings();
        BankDetailsDTO dto = new BankDetailsDTO();
        dto.setBankName(settings.getBankName());
        dto.setAccountNumber(settings.getAccountNumber());
        dto.setIfscCode(settings.getIfscCode());
        dto.setBranchName(settings.getBranchName());
        dto.setUpiId(settings.getUpiId());
        return dto;
    }

    @Override
    public StoreSettings updateBankDetails(BankDetailsDTO dto) {
        StoreSettings settings = getStoreSettings();
        settings.setBankName(dto.getBankName());
        settings.setAccountNumber(dto.getAccountNumber());
        settings.setIfscCode(dto.getIfscCode());
        settings.setBranchName(dto.getBranchName());
        settings.setUpiId(dto.getUpiId());
        return storeSettingsRepository.save(settings);
    }

    // --- TERMS & CONDITIONS IMPLEMENTATION ---
    @Override
    public TermsAndConditionsDTO getTermsAndConditions() {
        StoreSettings settings = getStoreSettings();
        TermsAndConditionsDTO dto = new TermsAndConditionsDTO();
        dto.setTermsAndConditions(settings.getTermsAndConditions());
        return dto;
    }

    @Override
    public StoreSettings updateTermsAndConditions(TermsAndConditionsDTO dto) {
        StoreSettings settings = getStoreSettings();
        settings.setTermsAndConditions(dto.getTermsAndConditions());
        return storeSettingsRepository.save(settings);
    }

    // --- STORE PROFILE IMPLEMENTATION ---
    @Override
    public StoreProfileDTO getStoreProfile() {
        StoreSettings settings = getStoreSettings();
        StoreProfileDTO dto = new StoreProfileDTO();
        dto.setStoreName(settings.getStoreName());
        dto.setAddress(settings.getAddress());
        dto.setPhoneNumber(settings.getPhoneNumber());
        dto.setEmail(settings.getEmail());
        dto.setGstin(settings.getGstin());
        return dto;
    }

    @Override
    public StoreSettings updateStoreProfile(StoreProfileDTO dto) {
        StoreSettings settings = getStoreSettings();
        settings.setStoreName(dto.getStoreName());
        settings.setAddress(dto.getAddress());
        settings.setPhoneNumber(dto.getPhoneNumber());
        settings.setEmail(dto.getEmail());
        settings.setGstin(dto.getGstin());
        return storeSettingsRepository.save(settings);
    }
    @Override
    public List<BankAccountDTO> getBankAccounts() { return bankAccountRepository.findAllByOrderByDefaultAccountDescIdDesc().stream().map(this::toDto).toList(); }
    @Override
    public List<BankAccountDTO> getPaymentReceivingAccounts() { return bankAccountRepository.findByActiveTrueAndAcceptingPaymentsTrueOrderByDefaultAccountDescIdDesc().stream().map(this::toDto).toList(); }
    @Override
    public BankAccountDTO createBankAccount(BankAccountDTO dto) { BankAccount account = new BankAccount(); copy(dto, account); if (Boolean.TRUE.equals(account.getDefaultAccount())) clearDefault(); return toDto(bankAccountRepository.save(account)); }
    @Override
    public BankAccountDTO updateBankAccount(Long id, BankAccountDTO dto) { BankAccount account = bankAccountRepository.findById(id).orElseThrow(() -> new RuntimeException("Bank account not found")); copy(dto, account); if (Boolean.TRUE.equals(account.getDefaultAccount())) clearDefaultExcept(id); return toDto(bankAccountRepository.save(account)); }
    @Override
    public void deleteBankAccount(Long id) { bankAccountRepository.deleteById(id); }
    private void clearDefault(){ bankAccountRepository.findAll().forEach(a->{ if(Boolean.TRUE.equals(a.getDefaultAccount())){a.setDefaultAccount(false); bankAccountRepository.save(a);} }); }
    private void clearDefaultExcept(Long id){ bankAccountRepository.findAll().forEach(a->{ if(!a.getId().equals(id)&&Boolean.TRUE.equals(a.getDefaultAccount())){a.setDefaultAccount(false); bankAccountRepository.save(a);} }); }
    private void copy(BankAccountDTO d, BankAccount a){ a.setAccountName(d.getAccountName());a.setBankName(d.getBankName());a.setAccountNumber(d.getAccountNumber());a.setIfscCode(d.getIfscCode());a.setBranchName(d.getBranchName());a.setUpiId(d.getUpiId());a.setAccountType(d.getAccountType());a.setAcceptingPayments(d.getAcceptingPayments()==null?true:d.getAcceptingPayments());a.setActive(d.getActive()==null?true:d.getActive());a.setDefaultAccount(d.getDefaultAccount()!=null&&d.getDefaultAccount()); }
    private BankAccountDTO toDto(BankAccount a){ BankAccountDTO d=new BankAccountDTO();d.setId(a.getId());d.setAccountName(a.getAccountName());d.setBankName(a.getBankName());d.setAccountNumber(a.getAccountNumber());d.setIfscCode(a.getIfscCode());d.setBranchName(a.getBranchName());d.setUpiId(a.getUpiId());d.setAccountType(a.getAccountType());d.setAcceptingPayments(a.getAcceptingPayments());d.setActive(a.getActive());d.setDefaultAccount(a.getDefaultAccount());return d; }

}
