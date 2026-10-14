package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryShippedBy;
import com.InventoryManagementSystem.repository.ShippedByRepository;
import com.InventoryManagementSystem.service.ShippedByService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippedByServiceImpl implements ShippedByService {

    @Autowired
    private ShippedByRepository shippedByRepository;

    @Override
    public InventoryShippedBy saveShippedBy(InventoryShippedBy shippedBy) {
        if (shippedBy.getCompanyName() != null) {
            shippedBy.setCompanyName(shippedBy.getCompanyName().trim());
        }
        if (shippedBy.getTrackingLink() != null) {
            shippedBy.setTrackingLink(shippedBy.getTrackingLink().trim());
        }
        return shippedByRepository.save(shippedBy);
    }

    @Override
    public List<InventoryShippedBy> getAllShippedBy() {
        return shippedByRepository.findAll();
    }

    @Override
    public InventoryShippedBy getShippedById(Long id) {
        return shippedByRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipped By not found with ID: " + id));
    }

    @Override
    public void deleteShippedBy(Long id) {
        shippedByRepository.deleteById(id);
    }
}
