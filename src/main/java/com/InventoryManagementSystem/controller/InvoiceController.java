//package com.InventoryManagementSystem.controller;
//
//
//
//import com.InventoryManagementSystem.entity.InventoryOrder;
//import com.InventoryManagementSystem.repository.OrderRepository;
//import com.InventoryManagementSystem.service.InvoicePdfService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.ByteArrayInputStream;
//
//@CrossOrigin(origins = {"http://localhost:5173", "https://pjsofttech.com"})
//@RestController
//@RequestMapping("/api/v1/invoices")
//public class InvoiceController {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private InvoicePdfService invoicePdfService;
//
//
//    @GetMapping("/print/{orderId}")
//    public ResponseEntity<InputStreamResource> printInvoice(@PathVariable Long orderId) {
//        InventoryOrder order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
//
//        ByteArrayInputStream bis = invoicePdfService.generateInvoicePdf(order);
//
//        HttpHeaders headers = new HttpHeaders();
//
//        headers.add("Content-Disposition", "inline; filename=Invoice_" + orderId + ".pdf");
//
//        return ResponseEntity
//                .ok()
//                .headers(headers)
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(new InputStreamResource(bis));
//    }
//}


package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.service.InvoicePdfService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://pjsofttech.com"
        }
)
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final OrderRepository orderRepository;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(
            OrderRepository orderRepository,
            InvoicePdfService invoicePdfService
    ) {
        this.orderRepository = orderRepository;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/print/{orderId}")
    public ResponseEntity<InputStreamResource> printInvoice(
            @PathVariable Long orderId
    ) {

        InventoryOrder order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found with ID: " +
                                                orderId
                                )
                        );

        ByteArrayInputStream invoiceStream =
                invoicePdfService.generateInvoicePdf(order);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=Invoice_" +
                        orderId +
                        ".pdf"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(
                        new InputStreamResource(
                                invoiceStream
                        )
                );
    }
}