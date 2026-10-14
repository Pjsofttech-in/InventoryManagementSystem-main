package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Turns the open installments of one side (customers or vendors) into the dashboard numbers. */
final class InstallmentSummaryCalculator {

    /** An open installment: money still pending and its due date. */
    record Item(BigDecimal pending, LocalDate dueDate) {}

    private InstallmentSummaryCalculator() {}

    static InstallmentSummaryDTO summarize(List<Item> items) {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        BigDecimal total = BigDecimal.ZERO, overdue = BigDecimal.ZERO, dueToday = BigDecimal.ZERO, next7 = BigDecimal.ZERO;
        long open = 0, overdueCount = 0;

        for (Item item : items) {
            if (item.pending().signum() <= 0) continue;
            open++;
            total = total.add(item.pending());
            if (item.dueDate().isBefore(today)) {
                overdue = overdue.add(item.pending());
                overdueCount++;
            } else if (item.dueDate().isEqual(today)) {
                dueToday = dueToday.add(item.pending());
            } else if (!item.dueDate().isAfter(in7Days)) {
                next7 = next7.add(item.pending());
            }
        }
        return new InstallmentSummaryDTO(total, open, overdue, overdueCount, dueToday, next7);
    }
}
