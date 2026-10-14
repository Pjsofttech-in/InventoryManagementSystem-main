//package com.InventoryManagementSystem.DTO;
//
//import lombok.Builder;
//import lombok.Data;
//import java.math.BigDecimal;
//
//@Data
//@Builder
//public class DashboardSummaryDTO {
//    private BigDecimal todaysSales;
//    private BigDecimal sevenDaysSales;
//    private BigDecimal thirtyDaysSales;
//    private BigDecimal oneYearSales;
//    private BigDecimal totalRevenue;
//    private BigDecimal paidRevenue;
//    private BigDecimal pendingRevenue;
//    private Long totalOrdersToday;
//    private Integer totalStockQuantity;
//    private Long lowStockCount;
//}

package com.InventoryManagementSystem.DTO;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal todaysSales;
    private Long todaysOrdersCount;

    private BigDecimal sevenDaysSales;
    private Long sevenDaysOrdersCount;

    private BigDecimal thirtyDaysSales;
    private Long thirtyDaysOrdersCount;

    private BigDecimal oneYearSales;
    private Long oneYearOrdersCount;

    private BigDecimal totalRevenue;
    private Long totalOrdersCount;

    private BigDecimal paidRevenue;
    private BigDecimal pendingRevenue;
    private Long totalOrdersToday;
    private Integer totalStockQuantity;
    private Long lowStockCount;

    // How much customers still owe for orders (total, overdue, unpaid / partial counts)
    private PendingOrdersSummaryDTO pendingOrders;

    // Installment money still to collect from customers / still to pay to vendors
    private InstallmentSummaryDTO customerInstallments;
    private InstallmentSummaryDTO vendorInstallments;
}
