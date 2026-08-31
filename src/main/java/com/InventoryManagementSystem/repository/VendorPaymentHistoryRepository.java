package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPaymentHistoryRepository extends JpaRepository<VendorPaymentHistory, Long> {
    List<VendorPaymentHistory> findByVendorBillIdOrderByPaymentDateDescIdDesc(Long vendorBillId);
    List<VendorPaymentHistory> findByVendorBillId(Long vendorBillId);
}
