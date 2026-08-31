package com.InventoryManagementSystem.DTO;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal todaysSales;
    private BigDecimal sevenDaysSales;
    private BigDecimal thirtyDaysSales;
    private BigDecimal oneYearSales;
    private BigDecimal totalRevenue;
    private BigDecimal paidRevenue;
    private BigDecimal pendingRevenue;
    private Long totalOrdersToday;
    private Integer totalStockQuantity;
    private Long lowStockCount;
}
