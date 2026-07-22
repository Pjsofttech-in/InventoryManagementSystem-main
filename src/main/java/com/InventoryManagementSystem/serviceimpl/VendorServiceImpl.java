package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.InventoryVendor;
import com.InventoryManagementSystem.repository.VendorRepository;
import com.InventoryManagementSystem.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Override
    public InventoryVendor saveVendor(InventoryVendor vendor) {
        return vendorRepository.save(vendor);
    }

    @Override
    public List<InventoryVendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Override
    public InventoryVendor getVendorById(Long id) {
        return vendorRepository.findById(id).orElse(null);
    }

    @Override
    public InventoryVendor updateVendor(Long id, InventoryVendor vendor) {

        InventoryVendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor Not Found: " + id));


        existing.setVendorName(vendor.getVendorName());
        existing.setCompanyName(vendor.getCompanyName());
        existing.setGstNo(vendor.getGstNo());
        existing.setLandmark(vendor.getLandmark());
        existing.setCity(vendor.getCity());
        existing.setTaluka(vendor.getTaluka());
        existing.setState(vendor.getState());
        existing.setPincode(vendor.getPincode());
        existing.setEmail(vendor.getEmail());
        existing.setPhone(vendor.getPhone());

        return vendorRepository.save(existing);
    }
    @Override
    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }
}
