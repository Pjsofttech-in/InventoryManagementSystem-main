package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One payment made against an installment. */
public record InstallmentPaymentDTO(
        Long id,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentMethod paymentMethod,
        String transactionReference,
        String notes
) {}
