package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.OrderGstSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryOrder;
import java.util.List;

public interface OrderService {

    InventoryOrder createOrder(InventoryOrder order);

    List<InventoryOrder> getAllOrders();

    InventoryOrder getOrderById(Long id);

    void deleteOrder(Long id);

    // Total GST across all orders (with CGST/SGST/UGST/IGST split) + overdue payment summary.
    OrderGstSummaryDTO getOrdersGstSummary();
}