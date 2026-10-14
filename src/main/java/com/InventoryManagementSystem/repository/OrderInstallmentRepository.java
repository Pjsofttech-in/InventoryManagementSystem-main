package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.Enum.InstallmentStatus;
import com.InventoryManagementSystem.entity.OrderInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderInstallmentRepository extends JpaRepository<OrderInstallment, Long> {

    List<OrderInstallment> findByOrderIdOrderByInstallmentNumberAsc(Long orderId);

    boolean existsByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    /** Open (not fully paid) instalments of orders that are not cancelled, earliest due date first. */
    @Query("SELECT i FROM OrderInstallment i JOIN FETCH i.order o LEFT JOIN FETCH o.customer " +
            "WHERE i.status <> :paid AND o.status <> com.InventoryManagementSystem.Enum.OrderStatus.CANCELLED " +
            "AND i.dueDate <= :until ORDER BY i.dueDate ASC, i.id ASC")
    List<OrderInstallment> findOpenDueUntil(@Param("paid") InstallmentStatus paid, @Param("until") LocalDate until);
}
