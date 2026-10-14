package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;

import java.util.List;

/** Installments for CUSTOMER orders: the customer pays the store in parts. */
public interface OrderInstallmentService {

    /** Splits the still-unpaid amount of the order into installments. */
    InstallmentPlanResponseDTO createPlan(Long orderId, InstallmentPlanRequestDTO request);

    InstallmentPlanResponseDTO getPlan(Long orderId);

    /** Removes the plan (allowed only while no installment has been paid). */
    void deletePlan(Long orderId);

    /** Records a (partial or full) payment of one installment; also becomes a normal payment of the order. */
    InstallmentPlanResponseDTO payInstallment(Long installmentId, InstallmentPaymentRequestDTO request);

    /** Open installments to collect: overdue only, or everything due within the next {@code days} days. */
    List<InstallmentDueDTO> getDueInstallments(boolean overdueOnly, int days);

    InstallmentSummaryDTO getSummary();
}
