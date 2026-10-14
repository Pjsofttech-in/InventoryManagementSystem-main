package com.InventoryManagementSystem.Enum;

/** Status of ONE installment (not of the whole order / bill). Overdue is calculated from the due date. */
public enum InstallmentStatus {
    PENDING,   // nothing paid yet
    PARTIAL,   // some money paid, some still due
    PAID       // fully paid
}
