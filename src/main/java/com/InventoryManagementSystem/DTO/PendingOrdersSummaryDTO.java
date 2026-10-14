package com.InventoryManagementSystem.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Dashboard block: how much money customers still owe for orders. Cancelled orders are ignored. */
@Data
@Builder
public class PendingOrdersSummaryDTO {
    private BigDecimal totalPendingAmount;      // sum of (order total - paid) over all orders
    private Long pendingOrdersCount;            // orders with something left to pay
    private Long unpaidOrdersCount;             // nothing paid yet
    private Long partiallyPaidOrdersCount;      // some paid, some pending
    private BigDecimal overduePendingAmount;    // pending amount of orders whose payment due date has passed
    private Long overdueOrdersCount;
    private BigDecimal pendingToday;            // pending amount of orders placed today
}
