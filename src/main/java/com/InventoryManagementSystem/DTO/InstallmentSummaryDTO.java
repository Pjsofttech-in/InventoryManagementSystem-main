package com.InventoryManagementSystem.DTO;

import java.math.BigDecimal;

/** Dashboard numbers of one side (money to collect from customers, or money to pay to vendors). */
public record InstallmentSummaryDTO(
        BigDecimal totalPending,         // all unpaid installment money
        long openInstallments,
        BigDecimal overdueAmount,
        long overdueInstallments,
        BigDecimal dueToday,
        BigDecimal dueNext7Days          // tomorrow .. today+7
) {
    public static InstallmentSummaryDTO empty() {
        return new InstallmentSummaryDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
