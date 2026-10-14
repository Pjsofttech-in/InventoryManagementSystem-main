package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;

import java.util.List;

/** Installments for VENDOR bills: the store pays the vendor in parts. */
public interface VendorBillInstallmentService {

    /** Splits the still-unpaid amount of the vendor bill into installments. */
    InstallmentPlanResponseDTO createPlan(Long vendorBillId, InstallmentPlanRequestDTO request);

    InstallmentPlanResponseDTO getPlan(Long vendorBillId);

    /** Removes the plan (allowed only while no installment has been paid). */
    void deletePlan(Long vendorBillId);

    /** Pays one installment to the vendor; also becomes a normal payment of the bill. */
    InstallmentPlanResponseDTO payInstallment(Long installmentId, InstallmentPaymentRequestDTO request);

    /** Open installments to pay: overdue only, or everything due within the next {@code days} days. */
    List<InstallmentDueDTO> getDueInstallments(boolean overdueOnly, int days);

    InstallmentSummaryDTO getSummary();
}
