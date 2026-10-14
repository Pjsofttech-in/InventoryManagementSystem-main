package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.VendorBillPaymentRequest;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorBillInstallment;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;

import java.util.List;

public interface VendorBillPaymentService {
    VendorPaymentHistory addPayment(Long vendorBillId, VendorBillPaymentRequest request);

    /** Same as above, but the payment settles the given installment of the bill's installment plan. */
    VendorPaymentHistory addPayment(Long vendorBillId, VendorBillPaymentRequest request, VendorBillInstallment installment);
    VendorPaymentHistory markPaymentDone(Long vendorBillId);
    List<VendorPaymentHistory> getPaymentHistory(Long vendorBillId);
    VendorBill getVendorBill(Long vendorBillId);
}
