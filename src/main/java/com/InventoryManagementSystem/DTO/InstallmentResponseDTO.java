package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One installment with what was paid against it. */
public record InstallmentResponseDTO(
        Long id,
        Integer installmentNumber,
        BigDecimal dueAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        LocalDate dueDate,
        InstallmentStatus status,
        boolean overdue,
        List<InstallmentPaymentDTO> payments
) {}
