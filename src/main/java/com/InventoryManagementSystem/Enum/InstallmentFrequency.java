package com.InventoryManagementSystem.Enum;

import java.time.LocalDate;

/** Gap between two installments when the schedule is generated automatically. */
public enum InstallmentFrequency {
    DAILY, WEEKLY, FORTNIGHTLY, MONTHLY;

    /** Due date of the installment with the given 0-based index, counted from the first due date. */
    public LocalDate dueDate(LocalDate first, int index) {
        return switch (this) {
            case DAILY -> first.plusDays(index);
            case WEEKLY -> first.plusWeeks(index);
            case FORTNIGHTLY -> first.plusWeeks(2L * index);
            case MONTHLY -> first.plusMonths(index);
        };
    }
}
