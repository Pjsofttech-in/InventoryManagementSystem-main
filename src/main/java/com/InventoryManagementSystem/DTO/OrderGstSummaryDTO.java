package com.InventoryManagementSystem.DTO;

import lombok.Getter;
import lombok.Setter;

/*
 * Grand-total summary shown on the Orders page (cancelled orders are not counted in the money totals):
 * - Total order amount, paid amount and pending amount across all orders.
 * - Total GST across every order (with CGST/SGST/UGST/IGST split).
 * - How many orders currently have an overdue payment, and how much money
 *   that overdue amount adds up to.
 */
@Getter
@Setter
public class OrderGstSummaryDTO {
    private int totalOrders;
    private int cancelledOrdersCount;
    private Double totalOrderAmount = 0.0;
    private Double totalPaidAmount = 0.0;
    private Double totalPendingAmount = 0.0;
    private Double totalGstAmount = 0.0;
    private Double totalCgstAmount = 0.0;
    private Double totalSgstAmount = 0.0;
    private Double totalUgstAmount = 0.0;
    private Double totalIgstAmount = 0.0;
    private int overdueOrdersCount;
    private Double totalOverdueAmount = 0.0;
}
