package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController

public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/createPayment")
    public InventoryPayment createPayment(@RequestParam Long orderId,
                                          @RequestBody InventoryPayment payment)
    {
        return paymentService.createPayment(orderId, payment);
    }

    @GetMapping("/getAllPayments")
    public List<InventoryPayment> getAllPayments()
    {
        return paymentService.getAllPayments();
    }

    @GetMapping("/getPaymentById/{id}")
    public InventoryPayment getPaymentById(@PathVariable Long id)
    {
        return paymentService.getPaymentById(id);
    }

    @DeleteMapping("/deletePayment/{id}")
    public String deletePayment(@PathVariable Long id)
    {
        paymentService.deletePayment(id);
        return "Payment ID " + id + " deleted successfully";
    }
}