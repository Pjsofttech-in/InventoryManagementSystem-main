package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryShippedBy;

import java.util.List;

public interface ShippedByService {

    InventoryShippedBy saveShippedBy(InventoryShippedBy shippedBy);

    List<InventoryShippedBy> getAllShippedBy();

    InventoryShippedBy getShippedById(Long id);

    void deleteShippedBy(Long id);
}
