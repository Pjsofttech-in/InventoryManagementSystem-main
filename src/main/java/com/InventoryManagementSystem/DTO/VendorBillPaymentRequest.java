package com.InventoryManagementSystem.DTO;

import com.InventoryManagementSystem.Enum.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VendorBillPaymentRequest {
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String notes;
    private LocalDateTime paymentDate;
}
