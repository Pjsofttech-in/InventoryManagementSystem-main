package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;
import com.InventoryManagementSystem.service.OrderInstallmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Customer installments: a customer pays an order in parts. */
@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
@RequestMapping("/api/order-installments")
public class OrderInstallmentController {

    private final OrderInstallmentService service;

    public OrderInstallmentController(OrderInstallmentService service) {
        this.service = service;
    }

    /** Put an order on an installment plan (covers the amount still unpaid). */
    @PostMapping("/order/{orderId}")
    public ResponseEntity<InstallmentPlanResponseDTO> createPlan(@PathVariable Long orderId,
                                                                 @RequestBody InstallmentPlanRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPlan(orderId, request));
    }

    @GetMapping("/order/{orderId}")
    public InstallmentPlanResponseDTO getPlan(@PathVariable Long orderId) {
        return service.getPlan(orderId);
    }

    /** Remove the plan (only while nothing has been paid against it). */
    @DeleteMapping("/order/{orderId}")
    public Map<String, String> deletePlan(@PathVariable Long orderId) {
        service.deletePlan(orderId);
        return Map.of("message", "Installment plan of order " + orderId + " removed");
    }

    /** Customer pays (part of) one installment. */
    @PostMapping("/{installmentId}/pay")
    public ResponseEntity<InstallmentPlanResponseDTO> pay(@PathVariable Long installmentId,
                                                          @RequestBody InstallmentPaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.payInstallment(installmentId, request));
    }

    /** Follow-up list: overdueOnly=true -> only overdue ones, else everything due within the next {days} days. */
    @GetMapping("/due")
    public List<InstallmentDueDTO> due(@RequestParam(defaultValue = "false") boolean overdueOnly,
                                       @RequestParam(defaultValue = "30") int days) {
        return service.getDueInstallments(overdueOnly, days);
    }

    @GetMapping("/summary")
    public InstallmentSummaryDTO summary() {
        return service.getSummary();
    }
}
