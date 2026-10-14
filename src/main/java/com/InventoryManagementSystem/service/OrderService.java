package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.OrderGstSummaryDTO;
import com.InventoryManagementSystem.DTO.UpdateOrderRequestDto;
import com.InventoryManagementSystem.entity.InventoryOrder;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    InventoryOrder createOrder(InventoryOrder order);

    List<InventoryOrder> getAllOrders();

    /**
     * Search + filter used by the Orders list AND the PDF/CSV export (so both always agree).
     * Every argument is optional; blank / null means "do not filter on this".
     *
     * @param search        text matched against order id, customer name, phone, email and city
     * @param status        OrderStatus name
     * @param paymentMethod PaymentMethod name (method of the newest received payment)
     * @param fromDate      order date from (inclusive)
     * @param toDate        order date to (inclusive)
     */
    List<InventoryOrder> filterOrders(String search, String status, String paymentMethod,
                                      LocalDate fromDate, LocalDate toDate);

    InventoryOrder getOrderById(Long id);

    InventoryOrder updateOrder(Long id, UpdateOrderRequestDto request);

    void deleteOrder(Long id);

    // Total GST across all orders (with CGST/SGST/UGST/IGST split) + overdue payment summary.
    OrderGstSummaryDTO getOrdersGstSummary();
}