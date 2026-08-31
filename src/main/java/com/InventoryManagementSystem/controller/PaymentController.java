package com.InventoryManagementSystem.controller;
import com.InventoryManagementSystem.entity.InventoryPayment; import com.InventoryManagementSystem.service.PaymentService; import org.springframework.web.bind.annotation.*; import java.util.List;
@CrossOrigin(origins={"http://localhost:5173","https://pjsofttech.com"}) @RestController
public class PaymentController { private final PaymentService paymentService; public PaymentController(PaymentService p){paymentService=p;}
 @PostMapping("/createPayment") public InventoryPayment createPayment(@RequestParam Long orderId,@RequestBody InventoryPayment payment){return paymentService.createPayment(orderId,payment);}
 @GetMapping("/getAllPayments") public List<InventoryPayment> all(){return paymentService.getAllPayments();}
 @GetMapping("/getPaymentsByOrder/{orderId}") public List<InventoryPayment> byOrder(@PathVariable Long orderId){return paymentService.getPaymentsByOrderId(orderId);}
 @GetMapping("/getPaymentById/{id}") public InventoryPayment byId(@PathVariable Long id){return paymentService.getPaymentById(id);}
 @DeleteMapping("/deletePayment/{id}") public String delete(@PathVariable Long id){paymentService.deletePayment(id);return "Payment ID "+id+" deleted successfully";}
}
