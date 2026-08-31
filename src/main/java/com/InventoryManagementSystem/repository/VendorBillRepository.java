package com.InventoryManagementSystem.repository;



import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {


    List<VendorBill> findByVendorIdAndIsDeletedFalseOrderByIdDesc(Long vendorId);

    List<VendorBill> findByIsDeletedFalseOrderByIdDesc();


    List<VendorBill> findByPaymentStatusAndIsDeletedFalseOrderByIdDesc(PaymentStatus paymentStatus);
}