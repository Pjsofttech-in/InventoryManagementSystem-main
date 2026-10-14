package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.service.ShipmentReceiptService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentReceiptController {

    private final ShipmentReceiptService receiptService;

    public ShipmentReceiptController(ShipmentReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /** Shipment receipt PDF. Opens in the browser; add ?download=true to force a download. */
    @GetMapping("/{shipmentId}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long shipmentId,
                                          @RequestParam(defaultValue = "false") boolean download) {
        byte[] pdf = receiptService.generateReceipt(shipmentId);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename("Shipment_Receipt_" + shipmentId + ".pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
