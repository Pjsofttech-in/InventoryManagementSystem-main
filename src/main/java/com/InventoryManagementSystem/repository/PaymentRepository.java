package com.InventoryManagementSystem.repository;
import com.InventoryManagementSystem.entity.InventoryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<InventoryPayment, Long> {
    List<InventoryPayment> findByOrderIdOrderByPaymentDateDesc(Long orderId);
    // Bulk load (one query for many orders) - avoids a query per order.
    List<InventoryPayment> findByOrderIdInOrderByPaymentDateDesc(Collection<Long> orderIds);

    // Payments that settled the given instalments (paid amounts of an installment plan).
    @Query("SELECT p FROM InventoryPayment p WHERE p.installment.id IN :installmentIds")
    List<InventoryPayment> findByInstallmentIdIn(@Param("installmentIds") Collection<Long> installmentIds);
    Optional<InventoryPayment> findTopByOrderIdOrderByPaymentDateDesc(Long orderId);
    List<InventoryPayment> findByPaymentDateBetween(LocalDate start, LocalDate end);
}
