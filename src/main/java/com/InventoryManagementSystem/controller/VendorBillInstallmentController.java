package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.InstallmentDueDTO;
import com.InventoryManagementSystem.DTO.InstallmentPaymentRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanRequestDTO;
import com.InventoryManagementSystem.DTO.InstallmentPlanResponseDTO;
import com.InventoryManagementSystem.DTO.InstallmentSummaryDTO;
import com.InventoryManagementSystem.service.VendorBillInstallmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Vendor installments: the store pays a vendor bill in parts. */
@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
@RequestMapping("/api/vendor-bill-installments")
public class VendorBillInstallmentController {

    private final VendorBillInstallmentService service;

    public VendorBillInstallmentController(VendorBillInstallmentService service) {
        this.service = service;
    }

    /** Put a vendor bill on an installment plan (covers the amount still due). */
    @PostMapping("/bill/{vendorBillId}")
    public ResponseEntity<InstallmentPlanResponseDTO> createPlan(@PathVariable Long vendorBillId,
                                                                 @RequestBody InstallmentPlanRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPlan(vendorBillId, request));
    }

    @GetMapping("/bill/{vendorBillId}")
    public InstallmentPlanResponseDTO getPlan(@PathVariable Long vendorBillId) {
        return service.getPlan(vendorBillId);
    }

    /** Remove the plan (only while nothing has been paid against it). */
    @DeleteMapping("/bill/{vendorBillId}")
    public Map<String, String> deletePlan(@PathVariable Long vendorBillId) {
        service.deletePlan(vendorBillId);
        return Map.of("message", "Installment plan of vendor bill " + vendorBillId + " removed");
    }

    /** Store pays (part of) one installment to the vendor. */
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
