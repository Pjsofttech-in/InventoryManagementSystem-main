package com.InventoryManagementSystem.repository;
import com.InventoryManagementSystem.entity.InventoryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<InventoryPayment, Long> {
    List<InventoryPayment> findByOrderIdOrderByPaymentDateDesc(Long orderId);
    Optional<InventoryPayment> findTopByOrderIdOrderByPaymentDateDesc(Long orderId);
    List<InventoryPayment> findByPaymentDateBetween(LocalDate start, LocalDate end);
}
