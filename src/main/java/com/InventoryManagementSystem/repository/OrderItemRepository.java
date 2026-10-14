package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<InventoryOrderItem, Long> {
    @Query("SELECT oi FROM InventoryOrderItem oi WHERE oi.order.orderDate BETWEEN :start AND :end")
    List<InventoryOrderItem> findByOrderDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Units sold and sales value (after discount, before GST) per product, in ONE query.
     * Cancelled orders are excluded (their stock is restored); deleted orders are filtered by @SQLRestriction.
     * Row = [productId, SUM(quantity), SUM(taxableAmount)]
     */
    @Query("SELECT oi.product.id, COALESCE(SUM(oi.quantity), 0), COALESCE(SUM(oi.taxableAmount), 0.0) " +
            "FROM InventoryOrderItem oi JOIN oi.order o " +
            "WHERE o.status <> :cancelled GROUP BY oi.product.id")
    List<Object[]> findSalesTotalsPerProduct(@Param("cancelled") OrderStatus cancelled);
}