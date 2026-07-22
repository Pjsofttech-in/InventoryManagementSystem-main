package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController

public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/createOrder")
    public InventoryOrder createOrder(@RequestBody InventoryOrder order)
    {
        return orderService.createOrder(order);
    }

    @GetMapping("/getAllOrders")
    public List<InventoryOrder> getAllOrders()
    {
        return orderService.getAllOrders();
    }

    @GetMapping("/getOrderById/{id}")
    public InventoryOrder getOrderById(@PathVariable Long id)
    {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/deleteOrder/{id}")
    public void deleteOrder(@PathVariable Long id)
    {
        orderService.deleteOrder(id);
    }
}