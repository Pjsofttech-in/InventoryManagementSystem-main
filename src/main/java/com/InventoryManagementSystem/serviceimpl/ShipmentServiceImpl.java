package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.ShipmentRepository;
import com.InventoryManagementSystem.service.ShipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public InventoryShipment saveShipment(InventoryShipment shipment) {
        if (shipment == null || shipment.getOrder() == null || shipment.getOrder().getId() == null) {
            throw new RuntimeException("Order is required to create a shipment");
        }

        Long orderId = shipment.getOrder().getId();
        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("A shipment already exists for order ID: " + orderId);
        }

        InventoryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        shipment.setOrder(order);

        if (shipment.getShipmentDate() == null) {
            shipment.setShipmentDate(LocalDate.now());
        }

        if (shipment.getStatus() == null) {
            shipment.setStatus(com.InventoryManagementSystem.Enum.ShipmentStatus.PREPARING);
        }

        // Prefer order/customer data so the shipment cannot be created for the wrong customer.
        if (order.getCustomer() != null) {
            if (isBlank(shipment.getCustomerName())) {
                shipment.setCustomerName(order.getCustomer().getCustomerName());
            }
            if (isBlank(shipment.getCustomerMobile())) {
                shipment.setCustomerMobile(order.getCustomer().getPhone());
            }
            if (isBlank(shipment.getCustomerAddress())) {
                shipment.setCustomerAddress(buildCustomerAddress(order));
            }
            if (isBlank(shipment.getShipmentAddress())) {
                shipment.setShipmentAddress(buildCustomerAddress(order));
            }
        }

        return shipmentRepository.save(shipment);
    }

    @Override
    public List<InventoryShipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Override
    public InventoryShipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + id));
    }

    @Override
    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }

    private String buildCustomerAddress(InventoryOrder order) {
        if (order.getCustomer() == null) return null;
        return java.util.stream.Stream.of(
                        order.getCustomer().getLandmark(),
                        order.getCustomer().getCity(),
                        order.getCustomer().getTaluka(),
                        order.getCustomer().getState() != null ? order.getCustomer().getState().name() : null,
                        order.getCustomer().getPincode()
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
