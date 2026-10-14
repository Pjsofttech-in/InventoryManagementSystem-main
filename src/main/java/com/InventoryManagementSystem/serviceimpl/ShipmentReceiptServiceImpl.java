package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.Enum.ShipmentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryShipment;
import com.InventoryManagementSystem.entity.StoreSettings;
import com.InventoryManagementSystem.repository.ShipmentRepository;
import com.InventoryManagementSystem.service.ShipmentReceiptService;
import com.InventoryManagementSystem.service.StoreSettingsService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ShipmentReceiptServiceImpl implements ShipmentReceiptService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Color LINE = new Color(203, 213, 225);
    private static final Color HEAD_BG = new Color(30, 64, 175);

    private final ShipmentRepository shipmentRepository;
    private final StoreSettingsService storeSettingsService;

    public ShipmentReceiptServiceImpl(ShipmentRepository shipmentRepository, StoreSettingsService storeSettingsService) {
        this.shipmentRepository = shipmentRepository;
        this.storeSettingsService = storeSettingsService;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceipt(Long shipmentId) {
        InventoryShipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));
        StoreSettings store = storeSettingsService.getStoreSettings();

        Font storeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, Color.BLACK);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.DARK_GRAY);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ---- store header
            Paragraph name = new Paragraph(text(store == null ? null : store.getStoreName(), "INVENTORY STORE"), storeFont);
            name.setAlignment(Element.ALIGN_CENTER);
            document.add(name);

            StringBuilder info = new StringBuilder();
            if (store != null) {
                append(info, "\n", store.getAddress());
                append(info, "\n", store.getPhoneNumber() == null ? null : "Phone: " + store.getPhoneNumber());
                append(info, " | ", store.getEmail() == null ? null : "Email: " + store.getEmail());
                append(info, "\n", store.getGstin() == null ? null : "GSTIN: " + store.getGstin());
            }
            Paragraph storeInfo = new Paragraph(info.toString().trim(), smallFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            Paragraph title = new Paragraph("SHIPMENT RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(14);
            title.setSpacingAfter(10);
            document.add(title);

            // ---- shipment details (2 label/value pairs per row)
            PdfPTable details = new PdfPTable(new float[]{1.2f, 2f, 1.2f, 2f});
            details.setWidthPercentage(100);
            row(details, "Receipt No", String.format(Locale.ROOT, "SR-%06d", shipment.getId()),
                    "Date", shipment.getShipmentDate() == null ? "-" : shipment.getShipmentDate().format(DATE), labelFont, valueFont);
            row(details, "Tracking ID", text(shipment.getTrackingId(), "-"),
                    "Status", label(shipment.getStatus()), labelFont, valueFont);
            row(details, "Courier", text(shipment.getCompanyName(), "-"),
                    "Shipment", text(shipment.getShipmentName(), "-"), labelFont, valueFont);
            row(details, "Dispatched by", text(shipment.getEmployeeName(), "-"),
                    "Order No", shipment.getOrderId() == null ? "-" : "#" + shipment.getOrderId(), labelFont, valueFont);
            document.add(details);

            if (shipment.getTrackingLink() != null && !shipment.getTrackingLink().isBlank()) {
                Paragraph link = new Paragraph("Track your parcel: " + shipment.getTrackingLink(), smallFont);
                link.setSpacingBefore(4);
                document.add(link);
            }

            // ---- customer
            section(document, "DELIVER TO", labelFont);
            document.add(new Paragraph(text(shipment.getCustomerName(), "-"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Color.BLACK)));
            if (shipment.getCustomerMobile() != null && !shipment.getCustomerMobile().isBlank()) {
                document.add(new Paragraph("Mobile: " + shipment.getCustomerMobile(), valueFont));
            }
            String address = shipment.getShipmentAddress() != null && !shipment.getShipmentAddress().isBlank()
                    ? shipment.getShipmentAddress() : shipment.getCustomerAddress();
            document.add(new Paragraph(text(address, "-"), valueFont));

            // ---- parcel contents
            section(document, "PARCEL CONTENTS", labelFont);
            PdfPTable items = new PdfPTable(new float[]{0.6f, 5f, 1.2f});
            items.setWidthPercentage(100);
            items.setHeaderRows(1);
            items.addCell(cell("#", headFont, HEAD_BG, Element.ALIGN_LEFT));
            items.addCell(cell("Product", headFont, HEAD_BG, Element.ALIGN_LEFT));
            items.addCell(cell("Qty", headFont, HEAD_BG, Element.ALIGN_RIGHT));

            InventoryOrder order = shipment.getOrder();
            int no = 0, totalQty = 0;
            if (order != null && order.getOrderItems() != null) {
                for (InventoryOrderItem item : order.getOrderItems()) {
                    int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                    totalQty += qty;
                    items.addCell(cell(String.valueOf(++no), valueFont, null, Element.ALIGN_LEFT));
                    items.addCell(cell(item.getProduct() == null ? "-" : text(item.getProduct().getProductName(), "-"), valueFont, null, Element.ALIGN_LEFT));
                    items.addCell(cell(String.valueOf(qty), valueFont, null, Element.ALIGN_RIGHT));
                }
            }
            if (no == 0) {
                items.addCell(cell("", valueFont, null, Element.ALIGN_LEFT));
                items.addCell(cell("No items found for this order", valueFont, null, Element.ALIGN_LEFT));
                items.addCell(cell("", valueFont, null, Element.ALIGN_RIGHT));
            }
            document.add(items);

            // ---- totals
            PdfPTable totals = new PdfPTable(new float[]{3f, 1.4f});
            totals.setWidthPercentage(45);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(10);
            totals.addCell(cell("Total items / quantity", labelFont, null, Element.ALIGN_LEFT));
            totals.addCell(cell(no + " / " + totalQty, valueFont, null, Element.ALIGN_RIGHT));
            totals.addCell(cell("Shipping charge (Rs.)", labelFont, null, Element.ALIGN_LEFT));
            totals.addCell(cell(String.format(Locale.ROOT, "%.2f", shipment.getShipmentCost() == null ? 0.0 : shipment.getShipmentCost()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Color.BLACK), null, Element.ALIGN_RIGHT));
            document.add(totals);

            // ---- signatures
            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            sign.setSpacingBefore(46);
            sign.addCell(signCell("Dispatched by (signature)", smallFont, Element.ALIGN_LEFT));
            sign.addCell(signCell("Received by (signature)", smallFont, Element.ALIGN_RIGHT));
            document.add(sign);

            Paragraph footer = new Paragraph("This is a computer generated shipment receipt.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(18);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Unable to generate shipment receipt", e);
        }
        return out.toByteArray();
    }

    // ------------------------------------------------------------------ helpers

    private void row(PdfPTable table, String l1, String v1, String l2, String v2, Font labelFont, Font valueFont) {
        table.addCell(cell(l1, labelFont, null, Element.ALIGN_LEFT));
        table.addCell(cell(v1, valueFont, null, Element.ALIGN_LEFT));
        table.addCell(cell(l2, labelFont, null, Element.ALIGN_LEFT));
        table.addCell(cell(v2, valueFont, null, Element.ALIGN_LEFT));
    }

    private void section(Document document, String heading, Font font) throws DocumentException {
        Paragraph p = new Paragraph(heading, font);
        p.setSpacingBefore(14);
        p.setSpacingAfter(3);
        document.add(p);
    }

    private PdfPCell cell(String value, Font font, Color background, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(4);
        cell.setBorderColor(LINE);
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(align);
        if (background != null) cell.setBackgroundColor(background);
        return cell;
    }

    private PdfPCell signCell(String caption, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase("______________________________\n" + caption, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private void append(StringBuilder sb, String separator, String value) {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append(separator);
        sb.append(value.trim());
    }

    private String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String label(ShipmentStatus status) {
        return status == null ? "-" : status.name().replace('_', ' ');
    }
}
