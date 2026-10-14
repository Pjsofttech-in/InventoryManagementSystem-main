package com.InventoryManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full detail for one vendor: totals + every bill + every installment paid on each bill.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBillDetailDTO {

    private VendorBillSummaryDTO summary;
    private List<BillDetail> bills;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillDetail {
        private Long id;
        private String billNumber;
        private LocalDate billDate;
        private LocalDate dueDate;

        private String productName;
        private String batchNumber;
        private Integer quantity;
        private BigDecimal unitPrice;

        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private String paymentStatus;

        /** Installments in the order they were paid (installment #1 first). */
        private List<InstallmentDetail> installments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstallmentDetail {
        private Long id;
        private int installmentNo;
        private BigDecimal amountPaid;
        /** Bill balance still remaining after this installment. */
        private BigDecimal balanceAfter;
        private LocalDateTime paymentDate;
        private String paymentMethod;
        private String transactionReference;
        private String notes;
    }
}
