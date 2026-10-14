package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Body of "pay this installment" (customer paying the store, or the store paying a vendor). */
@Getter
@Setter
public class InstallmentPaymentRequestDTO {
    private BigDecimal amount;
    private LocalDate paymentDate;              // default: today
    private PaymentMethod paymentMethod;
    private Long receivingAccountId;            // customer installments only: store bank account that received it
    private String transactionReference;        // UPI / bank / cheque reference
    private String notes;                       // vendor installments only
}
