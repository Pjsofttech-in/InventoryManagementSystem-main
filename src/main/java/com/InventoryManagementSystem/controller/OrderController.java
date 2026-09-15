//package com.InventoryManagementSystem.controller;
//
//import com.InventoryManagementSystem.entity.InventoryOrder;
//import com.InventoryManagementSystem.service.OrderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
//@RestController
//public class OrderController {
//
//    @Autowired
//    private OrderService orderService;
//
//    @PostMapping("/createOrder")
//    public InventoryOrder createOrder(@RequestBody InventoryOrder order) {
//        return orderService.createOrder(order);
//    }
//
//    @GetMapping("/getAllOrders")
//    public List<InventoryOrder> getAllOrders() {
//        return orderService.getAllOrders();
//    }
//
//    @GetMapping("/getOrderById/{id}")
//    public InventoryOrder getOrderById(@PathVariable Long id) {
//        return orderService.getOrderById(id);
//    }
//
//    @DeleteMapping("/deleteOrder/{id}")
//    public String deleteOrder(@PathVariable Long id) {
//        orderService.deleteOrder(id);
//        return "Order ID " + id + " soft-deleted successfully";
//    }
//}


package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.OrderGstSummaryDTO;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
//@RequestMapping("/api/orders") // Good practice to use base mapping
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/createOrder")
    public ResponseEntity<InventoryOrder> createOrder(@RequestBody InventoryOrder order) {
        InventoryOrder createdOrder = orderService.createOrder(order);

        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // 2. Get All Orders
    @GetMapping("/getAllOrders")
    public ResponseEntity<List<InventoryOrder>> getAllOrders() {
        List<InventoryOrder> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // 3. Get Order By ID
    @GetMapping("/getOrderById/{id}")
    public ResponseEntity<InventoryOrder> getOrderById(@PathVariable Long id) {
        InventoryOrder order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // 4. Delete Order
    @DeleteMapping("/deleteOrder/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok("Order ID " + id + " soft-deleted successfully");
    }

    // 5. GST grand-total across all orders + overdue payment summary (for the Orders page header/footer).
    @GetMapping("/getOrdersGstSummary")
    public ResponseEntity<OrderGstSummaryDTO> getOrdersGstSummary() {
        return ResponseEntity.ok(orderService.getOrdersGstSummary());
    }
}