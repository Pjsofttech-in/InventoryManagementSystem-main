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
import com.InventoryManagementSystem.DTO.UpdateOrderRequestDto;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.service.ExportService;
import com.InventoryManagementSystem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
//@RequestMapping("/api/orders") // Good practice to use base mapping
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ExportService exportService;


    @PostMapping("/createOrder")
    public ResponseEntity<InventoryOrder> createOrder(@RequestBody InventoryOrder order) {
        InventoryOrder createdOrder = orderService.createOrder(order);

        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // 2. Get All Orders - optionally searched / filtered (all params optional; none = every order).
    //    e.g. GET /getAllOrders?search=ravi&status=PENDING&paymentMethod=UPI&fromDate=2026-10-01&toDate=2026-10-31
    @GetMapping("/getAllOrders")
    public ResponseEntity<List<InventoryOrder>> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        boolean noFilter = isBlank(search) && isBlank(status) && isBlank(paymentMethod)
                && fromDate == null && toDate == null;

        List<InventoryOrder> orders = noFilter
                ? orderService.getAllOrders()
                : orderService.filterOrders(search, status, paymentMethod, fromDate, toDate);
        return ResponseEntity.ok(orders);
    }

    // Download the (filtered) orders as PDF or CSV. Takes the SAME filters as /getAllOrders,
    // so the file contains exactly the rows the list shows.
    //    e.g. GET /exportOrders?format=pdf&search=ravi&status=PENDING
    @GetMapping("/exportOrders")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<InventoryOrder> orders = orderService.filterOrders(search, status, paymentMethod, fromDate, toDate);
        return exportService
                .exportOrders(orders, format, search, status, paymentMethod, fromDate, toDate)
                .toResponse();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // 3. Get Order By ID
    @GetMapping("/getOrderById/{id}")
    public ResponseEntity<InventoryOrder> getOrderById(@PathVariable Long id) {
        InventoryOrder order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // 4. Update Order
    @PutMapping("/updateOrder/{id}")
    public ResponseEntity<InventoryOrder> updateOrder(
            @PathVariable Long id,
            @RequestBody UpdateOrderRequestDto request
    ) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
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