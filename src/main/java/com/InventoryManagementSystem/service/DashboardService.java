package com.InventoryManagementSystem.service;
import com.InventoryManagementSystem.DTO.*;
import com.InventoryManagementSystem.entity.InventoryProduct;
import java.util.List;
public interface DashboardService {
 DashboardSummaryDTO getDashboardSummary();
 List<TopProductDTO> getTopSellingProducts(int limit);
 List<InventoryProduct> getLowStockAlerts(int threshold);
 List<RevenuePointDTO> getDailyRevenue(int days);
 List<OrderRevenueDTO> getOrderWiseRevenue(int limit);
 List<RevenuePointDTO> getPaymentStatusRevenue();
 List<AnalyticsPointDTO> getAnalytics(String range);
}
