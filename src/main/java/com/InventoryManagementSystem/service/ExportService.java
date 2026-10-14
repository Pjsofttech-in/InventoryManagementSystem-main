package com.InventoryManagementSystem.service;

import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.util.ExportFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Builds the downloadable PDF / CSV files. Callers pass the ALREADY FILTERED rows plus the filter
 * values that produced them (the filter values are only printed in the report header).
 *
 * @param format "csv" or "pdf"
 */
public interface ExportService {

    ExportFile exportOrders(List<InventoryOrder> orders, String format,
                            String search, String status, String paymentMethod,
                            LocalDate fromDate, LocalDate toDate);

    ExportFile exportShipments(List<InventoryShipment> shipments, String format,
                               String search, String status,
                               LocalDate fromDate, LocalDate toDate);

    ExportFile exportProducts(List<InventoryProduct> products, String format,
                              String productName, Long productTypeId, Long brandId,
                              Long vendorId, Long unitTypeId);
}
