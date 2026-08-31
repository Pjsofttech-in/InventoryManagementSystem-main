package com.InventoryManagementSystem.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
@Data @AllArgsConstructor
public class OrderRevenueDTO { private Long orderId; private String label; private BigDecimal totalAmount; private BigDecimal paidAmount; private BigDecimal pendingAmount; }
