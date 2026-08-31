package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.entity.ProductStockBatch;
import com.InventoryManagementSystem.entity.VendorBill;
import com.InventoryManagementSystem.entity.VendorPaymentHistory;
import com.InventoryManagementSystem.repository.VendorPaymentHistoryRepository;
import com.InventoryManagementSystem.service.VendorBillPdfService;
import com.lowagie.text.Document;
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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class VendorBillPdfServiceImpl implements VendorBillPdfService {

    private final VendorPaymentHistoryRepository vendorPaymentHistoryRepository;

    public VendorBillPdfServiceImpl(
            VendorPaymentHistoryRepository vendorPaymentHistoryRepository
    ) {
        this.vendorPaymentHistoryRepository = vendorPaymentHistoryRepository;
    }

    @Override
    public ByteArrayInputStream generateVendorBillPdf(VendorBill bill) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(
                PageSize.A4,
                30,
                30,
                30,
                30
        );

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            /*
             * =====================================================
             * FONTS
             * =====================================================
             */

            Font title = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18,
                    Font.BOLD
            );

            Font section = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    11,
                    Font.BOLD
            );

            Font normal = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    9,
                    Font.NORMAL
            );

            Font bold = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    9,
                    Font.BOLD
            );

            Font header = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    8,
                    Font.BOLD,
                    Color.WHITE
            );

            /*
             * =====================================================
             * TITLE
             * =====================================================
             */

            Paragraph heading = new Paragraph(
                    "VENDOR PURCHASE BILL",
                    title
            );

            heading.setAlignment(Element.ALIGN_CENTER);

            document.add(heading);

            document.add(new Paragraph("\n"));

            /*
             * =====================================================
             * BILL INFORMATION
             * =====================================================
             */

            PdfPTable info = new PdfPTable(2);

            info.setWidthPercentage(100);

            info.setWidths(
                    new float[]{1f, 1f}
            );

            info.addCell(
                    infoCell(
                            "Bill Number",
                            bill.getBillNumber(),
                            bold,
                            normal
                    )
            );

            info.addCell(
                    infoCell(
                            "Vendor",
                            bill.getVendor() != null
                                    ? bill.getVendor().getVendorName()
                                    : "N/A",
                            bold,
                            normal
                    )
            );

            info.addCell(
                    infoCell(
                            "Bill Date",
                            bill.getBillDate() != null
                                    ? String.valueOf(bill.getBillDate())
                                    : "N/A",
                            bold,
                            normal
                    )
            );

            info.addCell(
                    infoCell(
                            "Due Date",
                            bill.getDueDate() != null
                                    ? String.valueOf(bill.getDueDate())
                                    : "N/A",
                            bold,
                            normal
                    )
            );

            info.addCell(
                    infoCell(
                            "Payment Status",
                            bill.getPaymentStatus() != null
                                    ? String.valueOf(
                                    bill.getPaymentStatus()
                            )
                                    : "N/A",
                            bold,
                            normal
                    )
            );

            info.addCell(
                    infoCell(
                            "Outstanding Due",
                            money(bill.getDueAmount()),
                            bold,
                            normal
                    )
            );

            document.add(info);

            document.add(new Paragraph("\n"));

            /*
             * =====================================================
             * WHAT THIS BILL IS FOR
             * =====================================================
             */

            document.add(
                    new Paragraph(
                            "What this bill is for",
                            section
                    )
            );

            ProductStockBatch batch = bill.getStockBatch();

            PdfPTable purchase = new PdfPTable(6);

            purchase.setWidthPercentage(100);

            purchase.setWidths(
                    new float[]{
                            2.2f,
                            1.1f,
                            0.8f,
                            1.2f,
                            1.2f,
                            1.3f
                    }
            );

            addHeader(
                    purchase,
                    "Product",
                    header
            );

            addHeader(
                    purchase,
                    "Batch",
                    header
            );

            addHeader(
                    purchase,
                    "Qty",
                    header
            );

            addHeader(
                    purchase,
                    "Unit Purchase",
                    header
            );

            addHeader(
                    purchase,
                    "Received",
                    header
            );

            addHeader(
                    purchase,
                    "Total",
                    header
            );

            String productName =
                    batch != null
                            && batch.getProduct() != null
                            && batch.getProduct().getProductName() != null
                            ? batch.getProduct().getProductName()
                            : "N/A";

            addCell(
                    purchase,
                    productName,
                    normal,
                    Element.ALIGN_LEFT
            );

            addCell(
                    purchase,
                    batch != null
                            ? safe(batch.getBatchNumber())
                            : "N/A",
                    normal,
                    Element.ALIGN_LEFT
            );

            addCell(
                    purchase,
                    batch != null
                            && batch.getInitialQuantity() != null
                            ? String.valueOf(
                            batch.getInitialQuantity()
                    )
                            : "0",
                    normal,
                    Element.ALIGN_CENTER
            );

            /*
             * If purchasePrice is BigDecimal, this works directly.
             * If your purchasePrice is Double, tell me and this
             * method needs a small change.
             */

            addCell(
                    purchase,
                    batch != null
                            ? money(batch.getPurchasePrice())
                            : money(BigDecimal.ZERO),
                    normal,
                    Element.ALIGN_RIGHT
            );

            addCell(
                    purchase,
                    batch != null
                            && batch.getReceivedDate() != null
                            ? String.valueOf(
                            batch.getReceivedDate()
                    )
                            : "N/A",
                    normal,
                    Element.ALIGN_CENTER
            );

            addCell(
                    purchase,
                    money(bill.getTotalAmount()),
                    bold,
                    Element.ALIGN_RIGHT
            );

            document.add(purchase);

            document.add(new Paragraph("\n"));

            /*
             * =====================================================
             * BILL SUMMARY
             * =====================================================
             */

            PdfPTable summary = new PdfPTable(2);

            summary.setWidthPercentage(55);

            summary.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            summary.setWidths(
                    new float[]{1.4f, 1f}
            );

            addSummary(
                    summary,
                    "Total Purchase Amount",
                    money(bill.getTotalAmount()),
                    normal,
                    bold
            );

            addSummary(
                    summary,
                    "Amount Paid",
                    money(bill.getPaidAmount()),
                    normal,
                    bold
            );

            addSummary(
                    summary,
                    "Amount Due",
                    money(bill.getDueAmount()),
                    normal,
                    bold
            );

            addSummary(
                    summary,
                    "Payment Status",
                    bill.getPaymentStatus() != null
                            ? String.valueOf(
                            bill.getPaymentStatus()
                    )
                            : "N/A",
                    normal,
                    bold
            );

            document.add(summary);

            document.add(new Paragraph("\n"));

            /*
             * =====================================================
             * PAYMENT HISTORY
             * =====================================================
             */

            List<VendorPaymentHistory> history =
                    vendorPaymentHistoryRepository
                            .findByVendorBillIdOrderByPaymentDateDescIdDesc(
                                    bill.getId()
                            );

            document.add(
                    new Paragraph(
                            "Payment History - What Was Paid And Why",
                            section
                    )
            );

            if (history == null || history.isEmpty()) {

                document.add(
                        new Paragraph(
                                "No payment has been recorded for this bill yet.",
                                normal
                        )
                );

            } else {

                PdfPTable payments = new PdfPTable(5);

                payments.setWidthPercentage(100);

                payments.setWidths(
                        new float[]{
                                1.4f,
                                1.2f,
                                1.3f,
                                1.4f,
                                2.7f
                        }
                );

                addHeader(
                        payments,
                        "Date",
                        header
                );

                addHeader(
                        payments,
                        "Amount",
                        header
                );

                addHeader(
                        payments,
                        "Method",
                        header
                );

                addHeader(
                        payments,
                        "Reference",
                        header
                );

                addHeader(
                        payments,
                        "Purpose / Notes",
                        header
                );

                for (VendorPaymentHistory payment : history) {

                    addCell(
                            payments,
                            payment.getPaymentDate() != null
                                    ? String.valueOf(
                                    payment.getPaymentDate()
                            )
                                    : "N/A",
                            normal,
                            Element.ALIGN_LEFT
                    );

                    addCell(
                            payments,
                            money(
                                    payment.getAmountPaid()
                            ),
                            bold,
                            Element.ALIGN_RIGHT
                    );

                    addCell(
                            payments,
                            payment.getPaymentMethod() != null
                                    ? payment
                                    .getPaymentMethod()
                                    .name()
                                    : "N/A",
                            normal,
                            Element.ALIGN_CENTER
                    );

                    addCell(
                            payments,
                            safe(
                                    payment.getTransactionReference()
                            ),
                            normal,
                            Element.ALIGN_LEFT
                    );

                    addCell(
                            payments,
                            safe(
                                    payment.getNotes()
                            ),
                            normal,
                            Element.ALIGN_LEFT
                    );
                }

                document.add(payments);
            }

            /*
             * =====================================================
             * FOOTER
             * =====================================================
             */

            document.add(
                    new Paragraph(
                            "\nThis is a system-generated vendor purchase bill.",
                            normal
                    )
            );

            document.close();

        } catch (Exception e) {

            if (document.isOpen()) {
                document.close();
            }

            throw new RuntimeException(
                    "Unable to generate vendor bill PDF",
                    e
            );
        }

        return new ByteArrayInputStream(
                out.toByteArray()
        );
    }

    /*
     * =========================================================
     * HELPER METHODS
     * =========================================================
     */

    private PdfPCell infoCell(
            String label,
            String value,
            Font bold,
            Font normal
    ) {

        PdfPCell cell = new PdfPCell();

        cell.setPadding(7);

        Paragraph paragraph = new Paragraph();

        paragraph.add(
                new Phrase(
                        label + ": ",
                        bold
                )
        );

        paragraph.add(
                new Phrase(
                        value != null
                                ? value
                                : "N/A",
                        normal
                )
        );

        cell.addElement(paragraph);

        return cell;
    }

    private void addHeader(
            PdfPTable table,
            String text,
            Font font
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                font
                        )
                );

        cell.setBackgroundColor(
                new Color(
                        51,
                        65,
                        85
                )
        );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(6);

        table.addCell(cell);
    }

    private void addCell(
            PdfPTable table,
            String text,
            Font font,
            int alignment
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text != null
                                        ? text
                                        : "N/A",
                                font
                        )
                );

        cell.setPadding(5);

        cell.setHorizontalAlignment(
                alignment
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }

    private void addSummary(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell left =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        PdfPCell right =
                new PdfPCell(
                        new Phrase(
                                value,
                                valueFont
                        )
                );

        left.setPadding(5);

        right.setPadding(5);

        right.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        table.addCell(left);

        table.addCell(right);
    }

    private String money(BigDecimal amount) {

        BigDecimal safeAmount =
                amount != null
                        ? amount
                        : BigDecimal.ZERO;

        return "Rs. "
                + safeAmount
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private String safe(String value) {

        return value == null
                || value.isBlank()
                ? "-"
                : value;
    }
}