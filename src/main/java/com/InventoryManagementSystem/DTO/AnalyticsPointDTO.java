package com.InventoryManagementSystem.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AnalyticsPointDTO {
    private String label;
    /** Total order amount in this period (same value as orderAmount, kept for the Revenue chart). */
    private BigDecimal revenue;
    private Long unitsSold;
    /** Total amount of orders placed in this period. */
    private BigDecimal orderAmount;
    /** Part of those orders that has been paid. */
    private BigDecimal paidAmount;
    /** Part of those orders that is still pending. */
    private BigDecimal pendingAmount;
    /** Number of (non-cancelled) orders placed in this period. */
    private Long ordersCount;
}