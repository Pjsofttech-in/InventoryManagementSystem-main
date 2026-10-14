package com.InventoryManagementSystem.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A whole installment plan.
 * referenceType = ORDER (customer pays the store) or VENDOR_BILL (store pays a vendor); referenceId is the order / bill id.
 */
public record InstallmentPlanResponseDTO(
        String referenceType,
        Long referenceId,
        String partyName,            // customer or vendor name
        BigDecimal planAmount,       // sum of all installments
        BigDecimal paidAmount,       // paid against the installments
        BigDecimal pendingAmount,
        int totalInstallments,
        int paidInstallments,
        BigDecimal overdueAmount,
        LocalDate nextDueDate,       // earliest unpaid installment
        List<InstallmentResponseDTO> installments
) {}
