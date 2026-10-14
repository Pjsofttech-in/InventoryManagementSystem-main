package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.InventoryManagementSystem.entity.VendorBillInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendorBillInstallmentRepository extends JpaRepository<VendorBillInstallment, Long> {

    List<VendorBillInstallment> findByVendorBillIdOrderByInstallmentNumberAsc(Long vendorBillId);

    boolean existsByVendorBillId(Long vendorBillId);

    void deleteByVendorBillId(Long vendorBillId);

    /** Open (not fully paid) instalments of bills that are not deleted, earliest due date first. */
    @Query("SELECT i FROM VendorBillInstallment i JOIN FETCH i.vendorBill b JOIN FETCH b.vendor " +
            "WHERE i.status <> :paid AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND i.dueDate <= :until ORDER BY i.dueDate ASC, i.id ASC")
    List<VendorBillInstallment> findOpenDueUntil(@Param("paid") InstallmentStatus paid, @Param("until") LocalDate until);
}
