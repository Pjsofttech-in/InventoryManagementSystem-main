package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of the "installments to collect / to pay" follow-up list. */
public record InstallmentDueDTO(
        Long installmentId,
        String referenceType,        // ORDER | VENDOR_BILL
        Long referenceId,            // order id / vendor bill id
        String referenceLabel,       // "#12" or the vendor bill number
        String partyName,            // customer or vendor
        String partyPhone,
        Integer installmentNumber,
        BigDecimal dueAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        LocalDate dueDate,
        InstallmentStatus status,
        boolean overdue,
        long daysOverdue
) {}
