package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.repository.ShipmentRepository;
import com.InventoryManagementSystem.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Override
    public InventoryShipment saveShipment(InventoryShipment shipment) {

        if (shipment.getShipmentDate() == null) {
            shipment.setShipmentDate(LocalDate.now());
        }

        return shipmentRepository.save(shipment);
    }

    @Override
    public List<InventoryShipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    public InventoryShipment getShipmentById(Long id) {
        return shipmentRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }
}