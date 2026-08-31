package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;

import java.util.List;

public interface VendorBillPaymentService {
    VendorPaymentHistory addPayment(Long vendorBillId, VendorBillPaymentRequest request);
    List<VendorPaymentHistory> getPaymentHistory(Long vendorBillId);
    VendorBill getVendorBill(Long vendorBillId);
}
