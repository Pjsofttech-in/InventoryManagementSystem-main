package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryOrderItem;
import java.util.List;

public interface OrderItemService {

    InventoryOrderItem createOrderItem(Long orderId, Long productId, Integer quantity);

    List<InventoryOrderItem> getAllOrderItems();

    InventoryOrderItem getOrderItemById(Long id);

    void deleteOrderItem(Long id);
}