package com.InventoryManagementSystem.repository;

import com.InventoryManagementSystem.entity.InventoryShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<InventoryShipment, Long>, JpaSpecificationExecutor<InventoryShipment> {
    // @Query because InventoryShipment has a getOrderId() getter, which confuses derived-query parsing.
    @Query("SELECT COUNT(s) > 0 FROM InventoryShipment s WHERE s.order.id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);

    boolean existsByTrackingId(String trackingId);
}
