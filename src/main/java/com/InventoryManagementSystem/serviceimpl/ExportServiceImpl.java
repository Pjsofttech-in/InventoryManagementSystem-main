package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.Enum.ShipmentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.repository.BrandRepository;
import com.InventoryManagementSystem.repository.ProductTypeRepository;
import com.InventoryManagementSystem.repository.UnitTypeRepository;
import com.InventoryManagementSystem.repository.VendorRepository;
import com.InventoryManagementSystem.service.ExportService;
import com.InventoryManagementSystem.util.CsvExportUtil;
import com.InventoryManagementSystem.util.ExportFile;
import com.InventoryManagementSystem.util.ExportTable;
import com.InventoryManagementSystem.util.PdfTableExportUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.InventoryManagementSystem.util.ExportTable.money;
import static com.InventoryManagementSystem.util.ExportTable.text;

@Service
public class ExportServiceImpl implements ExportService {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ProductTypeRepository productTypeRepository;
    private final BrandRepository brandRepository;
    private final VendorRepository vendorRepository;
    private final UnitTypeRepository unitTypeRepository;

    public ExportServiceImpl(ProductTypeRepository productTypeRepository,
                             BrandRepository brandRepository,
                             VendorRepository vendorRepository,
                             UnitTypeRepository unitTypeRepository) {
        this.productTypeRepository = productTypeRepository;
        this.brandRepository = brandRepository;
        this.vendorRepository = vendorRepository;
        this.unitTypeRepository = unitTypeRepository;
    }

    // ------------------------------------------------------------------ Orders

    @Override
    @Transactional(readOnly = true)
    public ExportFile exportOrders(List<InventoryOrder> orders, String format,
                                   String search, String status, String paymentMethod,
                                   LocalDate fromDate, LocalDate toDate) {

        List<String> filters = new ArrayList<>();
        addFilter(filters, "Search", search);
        addFilter(filters, "Status", label(status));
        addFilter(filters, "Payment method", label(paymentMethod));
        addFilter(filters, "Order date", range(fromDate, toDate));

        ExportTable table = new ExportTable(
                "Orders Report",
                filters,
                List.of("Order ID", "Date", "Customer", "Phone", "Status", "Payment Status", "Payment Method",
                        "Subtotal (Rs)", "Discount (Rs)", "GST (Rs)", "Total (Rs)", "Paid (Rs)", "Pending (Rs)", "Due Date"),
                new float[]{5, 8, 15, 10, 8, 8, 9, 8, 7, 7, 8, 8, 8, 8},
                Set.of(7, 8, 9, 10, 11, 12));

        double subtotal = 0, discount = 0, gst = 0, total = 0, paid = 0, pending = 0;

        for (InventoryOrder o : orders) {
            boolean cancelled = o.getStatus() == OrderStatus.CANCELLED;
            table.addRow(List.of(
                    text(o.getId()),
                    text(o.getOrderDate()),
                    text(o.getCustomer() != null ? o.getCustomer().getCustomerName() : null),
                    text(o.getCustomer() != null ? o.getCustomer().getPhone() : null),
                    text(o.getStatus()),
                    text(o.getPaymentStatus()),
                    text(o.getPaymentMethod()),
                    money(o.getSubtotalAmount()),
                    money(o.getTotalDiscountAmount()),
                    money(o.getTotalGstAmount()),
                    money(o.getTotalAmount()),
                    money(o.getPaidAmount()),
                    money(o.getPendingAmount()),
                    text(o.getPaymentDueDate())
            ));

            // Same rule as the on-screen totals: cancelled orders are listed but not counted in the money.
            if (!cancelled) {
                subtotal += nz(o.getSubtotalAmount());
                discount += nz(o.getTotalDiscountAmount());
                gst += nz(o.getTotalGstAmount());
                total += nz(o.getTotalAmount());
                paid += nz(o.getPaidAmount());
                pending += nz(o.getPendingAmount());
            }
        }

        table.setTotalsRow(List.of("TOTAL", "", "(excl. cancelled)", "", "", "", "",
                money(subtotal), money(discount), money(gst), money(total), money(paid), money(pending), ""));

        return render(table, format, "orders");
    }

    // --------------------------------------------------------------- Shipments

    @Override
    @Transactional(readOnly = true)
    public ExportFile exportShipments(List<InventoryShipment> shipments, String format,
                                      String search, String status,
                                      LocalDate fromDate, LocalDate toDate) {

        List<String> filters = new ArrayList<>();
        addFilter(filters, "Search", search);
        addFilter(filters, "Status", label(status));
        addFilter(filters, "Shipment date", range(fromDate, toDate));

        ExportTable table = new ExportTable(
                "Shipments Report",
                filters,
                List.of("ID", "Shipment", "Tracking ID", "Order ID", "Customer", "Mobile", "Courier",
                        "Employee", "Date", "Status", "Address", "Cost (Rs)"),
                new float[]{4, 11, 11, 5, 12, 9, 10, 9, 8, 8, 20, 7},
                Set.of(11));

        double cost = 0;
        for (InventoryShipment s : shipments) {
            table.addRow(List.of(
                    text(s.getId()),
                    text(s.getShipmentName()),
                    text(s.getTrackingId()),
                    text(s.getOrder() != null ? s.getOrder().getId() : null),
                    text(s.getCustomerName()),
                    text(s.getCustomerMobile()),
                    text(s.getCompanyName()),
                    text(s.getEmployeeName()),
                    text(s.getShipmentDate()),
                    text(s.getStatus()),
                    text(s.getShipmentAddress()),
                    money(s.getShipmentCost())
            ));
            if (s.getStatus() != ShipmentStatus.CANCELLED) {
                cost += nz(s.getShipmentCost());
            }
        }

        table.setTotalsRow(List.of("TOTAL", "", "", "", "", "", "", "", "", "", "(excl. cancelled)", money(cost)));

        return render(table, format, "shipments");
    }

    // ---------------------------------------------------------------- Products

    @Override
    @Transactional(readOnly = true)
    public ExportFile exportProducts(List<InventoryProduct> products, String format,
                                     String productName, Long productTypeId, Long brandId,
                                     Long vendorId, Long unitTypeId) {

        List<String> filters = new ArrayList<>();
        addFilter(filters, "Search", productName);
        if (productTypeId != null) {
            addFilter(filters, "Type", productTypeRepository.findById(productTypeId)
                    .map(t -> t.getTypeName()).orElse("#" + productTypeId));
        }
        if (brandId != null) {
            addFilter(filters, "Brand", brandRepository.findById(brandId)
                    .map(b -> b.getBrandName()).orElse("#" + brandId));
        }
        if (vendorId != null) {
            addFilter(filters, "Vendor", vendorRepository.findById(vendorId)
                    .map(v -> v.getVendorName()).orElse("#" + vendorId));
        }
        if (unitTypeId != null) {
            addFilter(filters, "Unit", unitTypeRepository.findById(unitTypeId)
                    .map(u -> u.getUnitType()).orElse("#" + unitTypeId));
        }

        ExportTable table = new ExportTable(
                "Products Report",
                filters,
                List.of("#", "Product", "Type", "Brand", "Vendor", "Unit", "MRP (Rs)", "Buy Price (Rs)",
                        "Sell Price (Rs)", "GST %", "Total Stock", "Avl Stock", "Sold",
                        "Buy Total (Rs)", "Stock Amt (Rs)", "Sold Amt (Rs)"),
                new float[]{3, 13, 8, 8, 9, 5, 6, 7, 7, 4, 5, 5, 4, 8, 8, 8},
                Set.of(6, 7, 8, 9, 10, 11, 12, 13, 14, 15));

        int totalStock = 0, remaining = 0, sold = 0;
        double buyTotal = 0, stockAmt = 0, soldAmt = 0;
        int serial = 0;

        for (InventoryProduct p : products) {
            int pTotalStock = p.getTotalStock() != null ? p.getTotalStock() : nzInt(p.getStock());
            int pRemaining = p.getRemainingStock() != null ? p.getRemainingStock() : nzInt(p.getStock());
            int pSold = nzInt(p.getTotalSold());
            double pBuyTotal = p.getBuyPriceTotal() != null ? p.getBuyPriceTotal() : nz(p.getStorePrice()) * nzInt(p.getStock());
            double pStockAmt = nz(p.getTotalAmount());
            double pSoldAmt = nz(p.getTotalSoldAmount());
            double gstPct = Boolean.TRUE.equals(p.getGstApplicable()) ? nz(p.getTotalGstPercentage()) : 0.0;

            table.addRow(List.of(
                    String.valueOf(++serial),
                    text(p.getProductName()),
                    text(p.getProductType() != null ? p.getProductType().getTypeName() : null),
                    text(p.getBrand() != null ? p.getBrand().getBrandName() : null),
                    text(p.getVendor() != null ? p.getVendor().getVendorName() : null),
                    text(p.getUnitType() != null ? p.getUnitType().getUnitType() : null),
                    money(p.getProductMrp()),
                    money(p.getStorePrice()),
                    money(p.getCustomerPrice()),
                    money(gstPct),
                    String.valueOf(pTotalStock),
                    String.valueOf(pRemaining),
                    String.valueOf(pSold),
                    money(pBuyTotal),
                    money(pStockAmt),
                    money(pSoldAmt)
            ));

            totalStock += pTotalStock;
            remaining += pRemaining;
            sold += pSold;
            buyTotal += pBuyTotal;
            stockAmt += pStockAmt;
            soldAmt += pSoldAmt;
        }

        table.setTotalsRow(List.of("", "TOTAL", "", "", "", "", "", "", "", "",
                String.valueOf(totalStock), String.valueOf(remaining), String.valueOf(sold),
                money(buyTotal), money(stockAmt), money(soldAmt)));

        return render(table, format, "products");
    }

    // ----------------------------------------------------------------- helpers

    private ExportFile render(ExportTable table, String format, String baseName) {
        String f = format == null ? "" : format.trim().toLowerCase();
        String stamp = LocalDateTime.now().format(FILE_STAMP);
        return switch (f) {
            case "csv" -> new ExportFile(CsvExportUtil.render(table), CSV, baseName + "_" + stamp + ".csv");
            case "pdf" -> new ExportFile(PdfTableExportUtil.render(table), MediaType.APPLICATION_PDF,
                    baseName + "_" + stamp + ".pdf");
            default -> throw new RuntimeException("Unsupported export format '" + format + "'. Use csv or pdf.");
        };
    }

    private static void addFilter(List<String> lines, String name, String value) {
        if (value != null && !value.isBlank()) lines.add(name + ": " + value.trim());
    }

    /** "IN_TRANSIT" -> "In transit" */
    private static String label(String enumName) {
        if (enumName == null || enumName.isBlank()) return null;
        String s = enumName.trim().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String range(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (from != null && to != null) return from + " to " + to;
        return from != null ? "from " + from : "up to " + to;
    }

    private static double nz(Double v) { return v == null ? 0.0 : v; }
    private static int nzInt(Integer v) { return v == null ? 0 : v; }
}
