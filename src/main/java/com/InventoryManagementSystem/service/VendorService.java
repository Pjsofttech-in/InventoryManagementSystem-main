package com.InventoryManagementSystem.service; import com.InventoryManagementSystem.entity.InventoryVendor; import java.util.List;
public interface VendorService {

    InventoryVendor saveVendor(InventoryVendor vendor);

    List<InventoryVendor> getAllVendors();

    InventoryVendor getVendorById(Long id);

    InventoryVendor updateVendor(Long id, InventoryVendor vendor);

    void deleteVendor(Long id);
}