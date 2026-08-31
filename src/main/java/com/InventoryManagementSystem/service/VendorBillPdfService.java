package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.VendorBill;
import java.io.ByteArrayInputStream;

public interface VendorBillPdfService {
    ByteArrayInputStream generateVendorBillPdf(VendorBill bill);
}
