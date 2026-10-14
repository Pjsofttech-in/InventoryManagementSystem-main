package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPaymentHistoryRepository extends JpaRepository<VendorPaymentHistory, Long> {
    List<VendorPaymentHistory> findByVendorBillIdOrderByPaymentDateDescIdDesc(Long vendorBillId);
    List<VendorPaymentHistory> findByVendorBillId(Long vendorBillId);

    // Used by the vendor bill summary/detail screens to load all installments in one query
    List<VendorPaymentHistory> findByVendorBillIdIn(java.util.Collection<Long> vendorBillIds);

    // Payments that settled the given instalments (paid amounts of an installment plan).
    @Query("SELECT h FROM VendorPaymentHistory h WHERE h.installment.id IN :installmentIds")
    List<VendorPaymentHistory> findByInstallmentIdIn(@Param("installmentIds") java.util.Collection<Long> installmentIds);
}
