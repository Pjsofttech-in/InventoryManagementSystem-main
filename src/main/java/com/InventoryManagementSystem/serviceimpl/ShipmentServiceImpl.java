package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.ShipmentSummaryDTO;
import com.InventoryManagementSystem.Enum.ShipmentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.entity.InventoryShippedBy;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.ShipmentRepository;
import com.InventoryManagementSystem.repository.ShippedByRepository;
import com.InventoryManagementSystem.service.ShipmentService;
import com.InventoryManagementSystem.util.SearchUtil;
import com.InventoryManagementSystem.util.TrackingIdGenerator;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShippedByRepository shippedByRepository;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            ShippedByRepository shippedByRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.shippedByRepository = shippedByRepository;
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
        shipment.setTrackingId(resolveTrackingId(shipment.getTrackingId()));

        // Dynamically fetch Courier/Company Name & Tracking Link from the selected
        // "Shipped By" master record, so the shipment always reflects the latest data.
        if (shipment.getShippedBy() != null && shipment.getShippedBy().getId() != null) {
            InventoryShippedBy shippedBy = shippedByRepository.findById(shipment.getShippedBy().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Shipped By not found with ID: " + shipment.getShippedBy().getId()));

            shipment.setShippedBy(shippedBy);
            shipment.setCompanyName(shippedBy.getCompanyName());
            shipment.setTrackingLink(shippedBy.getTrackingLink());
        }

        if (shipment.getShipmentDate() == null) {
            shipment.setShipmentDate(LocalDate.now());
        }

        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PREPARING);
        }

        // Shipment cost: required money to send the parcel. Never negative, default 0.
        if (shipment.getShipmentCost() == null) {
            shipment.setShipmentCost(0.0);
        }
        if (shipment.getShipmentCost() < 0) {
            throw new RuntimeException("Shipment cost cannot be negative");
        }
        shipment.setShipmentCost(Math.round(shipment.getShipmentCost() * 100.0) / 100.0);

        if (shipment.getEmployeeName() != null) {
            shipment.setEmployeeName(shipment.getEmployeeName().trim());
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
    @Transactional(readOnly = true)
    public List<InventoryShipment> filterShipments(String search, String status, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new RuntimeException("From date cannot be after To date");
        }

        final String term = SearchUtil.clean(search);

        final ShipmentStatus shipmentStatus;
        if (status == null || status.isBlank()) {
            shipmentStatus = null;
        } else {
            try {
                shipmentStatus = ShipmentStatus.valueOf(status.trim().toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid shipment status: " + status);
            }
        }

        Specification<InventoryShipment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null) {
                String like = SearchUtil.likePattern(term);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("shipmentName")), like, '\\'),
                        cb.like(cb.lower(root.get("trackingId")), like, '\\'),
                        cb.like(cb.lower(root.get("customerName")), like, '\\'),
                        cb.like(cb.lower(root.get("customerMobile")), like, '\\'),
                        cb.like(cb.lower(root.get("companyName")), like, '\\'),
                        cb.like(cb.lower(root.get("employeeName")), like, '\\'),
                        cb.like(root.get("order").get("id").as(String.class), like, '\\'),
                        cb.like(root.get("id").as(String.class), like, '\\')
                ));
            }
            if (shipmentStatus != null) {
                predicates.add(cb.equal(root.get("status"), shipmentStatus));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("shipmentDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("shipmentDate"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return shipmentRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
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

    @Override
    @Transactional(readOnly = true)
    public ShipmentSummaryDTO getShipmentsSummary() {
        List<InventoryShipment> shipments = shipmentRepository.findAll();
        ShipmentSummaryDTO summary = new ShipmentSummaryDTO();
        double totalCost = 0.0;
        int cancelled = 0;
        for (InventoryShipment s : shipments) {
            if (s.getStatus() == ShipmentStatus.CANCELLED) {
                cancelled++;
                continue;
            }
            totalCost += s.getShipmentCost() == null ? 0.0 : s.getShipmentCost();
        }
        summary.setTotalShipments(shipments.size());
        summary.setCancelledShipments(cancelled);
        summary.setTotalShipmentCost(Math.round(totalCost * 100.0) / 100.0);
        return summary;
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

    /** Auto generates the tracking id when none is sent; a manually sent one must still be unique. */
    private String resolveTrackingId(String requested) {
        String trackingId = requested == null ? "" : requested.trim();
        if (!trackingId.isEmpty()) {
            if (shipmentRepository.existsByTrackingId(trackingId)) {
                throw new RuntimeException("Tracking ID already used by another shipment: " + trackingId);
            }
            return trackingId;
        }
        for (int attempt = 0; attempt < 10; attempt++) {
            String generated = TrackingIdGenerator.next();
            if (!shipmentRepository.existsByTrackingId(generated)) {
                return generated;
            }
        }
        throw new RuntimeException("Could not generate a unique tracking ID, please try again");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
