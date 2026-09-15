package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<InventoryOrderItem, Long> {
    @Query("SELECT oi FROM InventoryOrderItem oi WHERE oi.order.orderDate BETWEEN :start AND :end")
    List<InventoryOrderItem> findByOrderDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}