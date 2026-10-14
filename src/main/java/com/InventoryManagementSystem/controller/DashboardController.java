package com.InventoryManagementSystem.controller;
import com.InventoryManagementSystem.DTO.*;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/dashboard")
public class DashboardController {
 private final DashboardService dashboardService; public DashboardController(DashboardService d){this.dashboardService=d;}
 @GetMapping("/summary") public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(){return ResponseEntity.ok(dashboardService.getDashboardSummary());}
 @GetMapping("/pending-orders") public ResponseEntity<PendingOrdersSummaryDTO> getPendingOrders(){return ResponseEntity.ok(dashboardService.getPendingOrdersSummary());}
 @GetMapping("/top-selling") public ResponseEntity<List<TopProductDTO>> getTopSellingProducts(@RequestParam(defaultValue="5") int limit){return ResponseEntity.ok(dashboardService.getTopSellingProducts(limit));}
 @GetMapping("/top-selling-categories") public ResponseEntity<List<TopCategoryDTO>> getTopSellingCategories(@RequestParam(defaultValue="5") int limit){return ResponseEntity.ok(dashboardService.getTopSellingCategories(limit));}
 @GetMapping("/low-stock") public ResponseEntity<List<InventoryProduct>> getLowStockAlerts(@RequestParam(defaultValue="5") int threshold){return ResponseEntity.ok(dashboardService.getLowStockAlerts(threshold));}
 @GetMapping("/daily-revenue") public ResponseEntity<List<RevenuePointDTO>> daily(@RequestParam(defaultValue="7") int days){return ResponseEntity.ok(dashboardService.getDailyRevenue(days));}
 @GetMapping("/order-wise-revenue") public ResponseEntity<List<OrderRevenueDTO>> orderWise(@RequestParam(defaultValue="10") int limit){return ResponseEntity.ok(dashboardService.getOrderWiseRevenue(limit));}
 @GetMapping("/payment-status-revenue") public ResponseEntity<List<RevenuePointDTO>> paymentStatus(){return ResponseEntity.ok(dashboardService.getPaymentStatusRevenue());}
 @GetMapping("/analytics") public ResponseEntity<List<AnalyticsPointDTO>> analytics(@RequestParam(defaultValue="7D") String range){return ResponseEntity.ok(dashboardService.getAnalytics(range));}
}
