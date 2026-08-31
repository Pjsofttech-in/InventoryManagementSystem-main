package com.InventoryManagementSystem.service;



import com.InventoryManagementSystem.entity.InventoryOrder;
import java.io.ByteArrayInputStream;

public interface InvoicePdfService {
    ByteArrayInputStream generateInvoicePdf(InventoryOrder order);
}