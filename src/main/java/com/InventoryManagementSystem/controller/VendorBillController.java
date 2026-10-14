package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.VendorBillDetailDTO;
import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.DTO.VendorBillSummaryDTO;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorBillRepository;
import com.InventoryManagementSystem.service.VendorBillPaymentService;
import com.InventoryManagementSystem.service.VendorBillSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-bills")
@CrossOrigin(origins = "*")
public class VendorBillController {

    @Autowired
    private VendorBillRepository vendorBillRepository;

    @Autowired
    private VendorBillPaymentService vendorBillPaymentService;

    @Autowired
    private VendorBillSummaryService vendorBillSummaryService;

    /** Vendor Bills page: one row per vendor with total / paid / remaining. */
    @GetMapping("/summary")
    public List<VendorBillSummaryDTO> getVendorSummaries() {
        return vendorBillSummaryService.getVendorSummaries();
    }

    /** "View details" button: vendor totals + all bills + installment history. */
    @GetMapping("/vendor/{vendorId}/detail")
    public VendorBillDetailDTO getVendorDetail(@PathVariable Long vendorId) {
        return vendorBillSummaryService.getVendorDetail(vendorId);
    }

    @GetMapping
    public List<VendorBill> getAll() {
        return vendorBillRepository.findByIsDeletedFalseOrderByIdDesc();
    }

    @GetMapping("/{id}")
    public VendorBill getById(@PathVariable Long id) {
        return vendorBillPaymentService.getVendorBill(id);
    }

    @GetMapping("/vendor/{vendorId}")
    public List<VendorBill> getByVendor(@PathVariable Long vendorId) {
        return vendorBillRepository.findByVendorIdAndIsDeletedFalseOrderByIdDesc(vendorId);
    }

    @GetMapping("/{id}/payments")
    public List<VendorPaymentHistory> getPaymentHistory(@PathVariable Long id) {
        return vendorBillPaymentService.getPaymentHistory(id);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<VendorPaymentHistory> addPayment(
            @PathVariable Long id,
            @RequestBody VendorBillPaymentRequest request
    ) {
        VendorPaymentHistory payment = vendorBillPaymentService.addPayment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<VendorPaymentHistory> markPaymentDone(@PathVariable Long id) {
        VendorPaymentHistory payment = vendorBillPaymentService.markPaymentDone(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
