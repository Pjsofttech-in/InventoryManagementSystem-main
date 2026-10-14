package com.InventoryManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One row per vendor on the "Vendor Bills" page:
 * vendor name + total to pay + how much is already paid + how much is remaining.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBillSummaryDTO {
    private Long vendorId;
    private String vendorName;
    private String companyName;
    private String phone;
    private String email;

    private int totalBills;
    private int paidBills;
    private int pendingBills;

    /** Total amount we have to pay this vendor (sum of all bills). */
    private BigDecimal totalAmount;
    /** Amount already paid (all installments together). */
    private BigDecimal paidAmount;
    /** Amount still remaining to be paid. */
    private BigDecimal remainingAmount;

    /** Number of payments (installments) made to this vendor so far. */
    private int installmentCount;
    /** true when the vendor is being paid in parts (something paid, something still remaining). */
    private boolean paidInInstallments;

    /** PAID / PARTIAL / UNPAID */
    private String paymentStatus;

    private LocalDateTime lastPaymentDate;
}
