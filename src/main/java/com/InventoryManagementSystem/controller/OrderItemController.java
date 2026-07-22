package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController

public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;


    @PostMapping("/createOrderItem")
    public InventoryOrderItem createOrderItem(@RequestParam Long orderId,
                                              @RequestParam Long productId,
                                              @RequestParam Integer quantity)
    {
        return orderItemService.createOrderItem(orderId, productId, quantity);
    }

    @GetMapping("/getAllOrderItems")
    public List<InventoryOrderItem> getAllOrderItems()

    {
        return orderItemService.getAllOrderItems();
    }

    @GetMapping("/getOrderItemById/{id}")
    public InventoryOrderItem getOrderItemById(@PathVariable Long id)
    {
        return orderItemService.getOrderItemById(id);
    }

    @DeleteMapping("/deleteOrderItem/{id}")
    public String deleteOrderItem(@PathVariable Long id)
    {
        orderItemService.deleteOrderItem(id);
        return "OrderItem ID " + id + " deleted successfully";
    }
}