package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorBillRepository;
import com.InventoryManagementSystem.service.VendorBillPaymentService;
import com.InventoryManagementSystem.service.VendorBillPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/vendor-bills")
@CrossOrigin(origins = "*")
public class VendorBillController {

    @Autowired
    private VendorBillRepository vendorBillRepository;

    @Autowired
    private VendorBillPdfService vendorBillPdfService;

    @Autowired
    private VendorBillPaymentService vendorBillPaymentService;

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

    @GetMapping("/print/{id}")
    public ResponseEntity<InputStreamResource> print(@PathVariable Long id) {
        VendorBill bill = vendorBillPaymentService.getVendorBill(id);
        ByteArrayInputStream pdf = vendorBillPdfService.generateVendorBillPdf(bill);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=VendorBill_" + bill.getBillNumber() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}
