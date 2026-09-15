package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

/*
 * Grand-total summary shown on the Orders page:
 * - Total GST collected across every order (with CGST/SGST/UGST/IGST split).
 * - How many orders currently have an overdue payment, and how much money
 *   that overdue amount adds up to.
 */
@Getter
@Setter
public class OrderGstSummaryDTO {
    private int totalOrders;
    private Double totalGstAmount = 0.0;
    private Double totalCgstAmount = 0.0;
    private Double totalSgstAmount = 0.0;
    private Double totalUgstAmount = 0.0;
    private Double totalIgstAmount = 0.0;
    private int overdueOrdersCount;
    private Double totalOverdueAmount = 0.0;
}
