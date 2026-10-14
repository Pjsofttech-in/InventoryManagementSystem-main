package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.DTO.VendorBillDetailDTO;
import com.InventoryManagementSystem.DTO.VendorBillSummaryDTO;

import java.util.List;

public interface VendorBillSummaryService {

    /** One row per vendor: total to pay, paid so far, remaining. */
    List<VendorBillSummaryDTO> getVendorSummaries();

    /** Vendor totals + all bills + installment history for one vendor. */
    VendorBillDetailDTO getVendorDetail(Long vendorId);
}
