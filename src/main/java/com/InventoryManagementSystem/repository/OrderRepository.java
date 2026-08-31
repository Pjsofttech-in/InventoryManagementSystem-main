package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<InventoryOrder, Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM InventoryOrder o WHERE o.orderDate = :date")
    BigDecimal findTodaysSales(@Param("date") LocalDate date);
    @Query("SELECT COUNT(o) FROM InventoryOrder o WHERE o.orderDate = :date")
    Long countTodaysOrders(@Param("date") LocalDate date);
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM InventoryOrder o")
    BigDecimal findTotalRevenue();
    List<InventoryOrder> findByOrderDateBetweenOrderByOrderDateAscIdAsc(LocalDate start, LocalDate end);
    List<InventoryOrder> findTop10ByOrderByOrderDateDescIdDesc();
}
