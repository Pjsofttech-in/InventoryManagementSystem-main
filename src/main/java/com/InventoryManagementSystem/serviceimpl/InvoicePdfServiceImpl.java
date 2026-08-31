package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.StoreSettings;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.BankAccountRepository;
import com.InventoryManagementSystem.entity.BankAccount;
import com.InventoryManagementSystem.service.InvoicePdfService;
import com.InventoryManagementSystem.service.StoreSettingsService;
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
import java.util.List;
import java.util.Optional;

@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final StoreSettingsService storeSettingsService;
    private final PaymentRepository paymentRepository;
    private final BankAccountRepository bankAccountRepository;

    public InvoicePdfServiceImpl(
            StoreSettingsService storeSettingsService,
            PaymentRepository paymentRepository,
            BankAccountRepository bankAccountRepository
    ) {
        this.storeSettingsService = storeSettingsService;
        this.paymentRepository = paymentRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public ByteArrayInputStream generateInvoicePdf(
            InventoryOrder order
    ) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        30,
                        30,
                        30,
                        30
                );

        try {

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            /*
             * =========================================================
             * STORE SETTINGS
             * =========================================================
             */
            StoreSettings storeSettings =
                    storeSettingsService.getStoreSettings();

            /*
             * =========================================================
             * PAYMENT DETAILS FROM DATABASE
             * =========================================================
             */
            List<InventoryPayment> payments =
                    paymentRepository
                            .findByOrderIdOrderByPaymentDateDesc(
                                    order.getId()
                            );

            double paidAmount = payments.stream()
                    .filter(payment ->
                            payment.getPaymentStatus()
                                    == PaymentStatus.PAID
                                    ||
                                    payment.getPaymentStatus()
                                            == PaymentStatus.PARTIAL
                    )
                    .mapToDouble(payment ->
                            value(payment.getAmount())
                    )
                    .sum();

            PaymentStatus paymentStatus =
                    calculatePaymentStatus(
                            order.getTotalAmount(),
                            paidAmount,
                            payments
                    );

            double dueAmount =
                    Math.max(
                            0.0,
                            value(order.getTotalAmount())
                                    - paidAmount
                    );

            /*
             * =========================================================
             * FONTS
             * =========================================================
             */
            Font storeNameFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            Font.BOLD,
                            Color.BLACK
                    );

            Font invoiceTitleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            15,
                            Font.BOLD,
                            Color.BLACK
                    );

            Font headingFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            Font.BOLD,
                            Color.BLACK
                    );

            Font normalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            9,
                            Font.NORMAL,
                            Color.BLACK
                    );

            Font smallFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            8,
                            Font.NORMAL,
                            Color.DARK_GRAY
                    );

            Font tableHeaderFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            8,
                            Font.BOLD,
                            Color.WHITE
                    );

            /*
             * =========================================================
             * STORE HEADER
             * =========================================================
             */
            String storeName =
                    storeSettings != null
                            && storeSettings.getStoreName() != null
                            ? storeSettings.getStoreName()
                            : "INVENTORY STORE";

            Paragraph storeNameParagraph =
                    new Paragraph(
                            storeName,
                            storeNameFont
                    );

            storeNameParagraph.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(storeNameParagraph);

            StringBuilder storeInfo =
                    new StringBuilder();

            if (
                    storeSettings != null
                            && storeSettings.getAddress() != null
            ) {
                storeInfo.append(
                        storeSettings.getAddress()
                );
            }

            if (
                    storeSettings != null
                            && storeSettings.getPhoneNumber() != null
            ) {
                if (storeInfo.length() > 0) {
                    storeInfo.append("\n");
                }

                storeInfo.append(
                        "Phone: "
                ).append(
                        storeSettings.getPhoneNumber()
                );
            }

            if (
                    storeSettings != null
                            && storeSettings.getEmail() != null
            ) {
                if (storeInfo.length() > 0) {
                    storeInfo.append(" | ");
                }

                storeInfo.append(
                        "Email: "
                ).append(
                        storeSettings.getEmail()
                );
            }

            if (
                    storeSettings != null
                            && storeSettings.getGstin() != null
            ) {
                storeInfo.append(
                        "\nGSTIN: "
                ).append(
                        storeSettings.getGstin()
                );
            }

            Paragraph storeInfoParagraph =
                    new Paragraph(
                            storeInfo.toString(),
                            smallFont
                    );

            storeInfoParagraph.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(storeInfoParagraph);

            document.add(
                    new Paragraph("\n")
            );

            /*
             * =========================================================
             * INVOICE TITLE
             * =========================================================
             */
            Paragraph invoiceTitle =
                    new Paragraph(
                            "TAX INVOICE",
                            invoiceTitleFont
                    );

            invoiceTitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(invoiceTitle);

            document.add(
                    new Paragraph("\n")
            );

            /*
             * =========================================================
             * INVOICE + CUSTOMER INFORMATION
             * =========================================================
             */
            PdfPTable infoTable =
                    new PdfPTable(2);

            infoTable.setWidthPercentage(100);

            infoTable.setWidths(
                    new float[]{1f, 1f}
            );

            /*
             * LEFT SIDE - INVOICE DETAILS
             */
            PdfPCell invoiceCell =
                    new PdfPCell();

            invoiceCell.setPadding(8);

            invoiceCell.addElement(
                    createLabelValueParagraph(
                            "Invoice No",
                            "INV-" + order.getId(),
                            headingFont,
                            normalFont
                    )
            );

            invoiceCell.addElement(
                    createLabelValueParagraph(
                            "Order Date",
                            order.getOrderDate() != null
                                    ? order.getOrderDate().toString()
                                    : "N/A",
                            headingFont,
                            normalFont
                    )
            );

            invoiceCell.addElement(
                    createLabelValueParagraph(
                            "Order Time",
                            order.getOrderTime() != null
                                    ? order.getOrderTime().toString()
                                    : "N/A",
                            headingFont,
                            normalFont
                    )
            );

            invoiceCell.addElement(
                    createLabelValueParagraph(
                            "Order Status",
                            order.getStatus() != null
                                    ? order.getStatus().name()
                                    : "N/A",
                            headingFont,
                            normalFont
                    )
            );

            infoTable.addCell(invoiceCell);

            /*
             * RIGHT SIDE - CUSTOMER DETAILS
             */
            PdfPCell customerCell =
                    new PdfPCell();

            customerCell.setPadding(8);

            Paragraph customerHeading =
                    new Paragraph(
                            "Bill To",
                            headingFont
                    );

            customerCell.addElement(
                    customerHeading
            );

            if (order.getCustomer() != null) {

                String customerName =
                        order.getCustomer().getCustomerName() != null
                                ? order.getCustomer().getCustomerName()
                                : "Customer";

                customerCell.addElement(
                        new Paragraph(
                                customerName,
                                normalFont
                        )
                );

                if (
                        order.getCustomer().getPhone() != null
                ) {
                    customerCell.addElement(
                            new Paragraph(
                                    "Phone: "
                                            + order.getCustomer().getPhone(),
                                    normalFont
                            )
                    );
                }

                if (
                        order.getCustomer().getEmail() != null
                ) {
                    customerCell.addElement(
                            new Paragraph(
                                    "Email: "
                                            + order.getCustomer().getEmail(),
                                    normalFont
                            )
                    );
                }

            } else {

                customerCell.addElement(
                        new Paragraph(
                                "Customer not available",
                                normalFont
                        )
                );
            }

            infoTable.addCell(customerCell);

            document.add(infoTable);

            document.add(
                    new Paragraph("\n")
            );

            /*
             * =========================================================
             * PRODUCT TABLE
             * =========================================================
             */
            PdfPTable itemTable =
                    new PdfPTable(7);

            itemTable.setWidthPercentage(100);

            itemTable.setWidths(
                    new float[]{
                            3.2f,
                            0.7f,
                            1.2f,
                            1.2f,
                            1.2f,
                            1.3f,
                            1.4f
                    }
            );

            addTableHeader(
                    itemTable,
                    "Product",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "Qty",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "Rate",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "Subtotal",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "Discount",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "GST",
                    tableHeaderFont
            );

            addTableHeader(
                    itemTable,
                    "Total",
                    tableHeaderFont
            );

            /*
             * =========================================================
             * ORDER ITEMS
             * =========================================================
             */
            if (
                    order.getOrderItems() != null
                            && !order.getOrderItems().isEmpty()
            ) {

                for (
                        InventoryOrderItem item :
                        order.getOrderItems()
                ) {

                    /*
                     * Product name
                     */
                    String productName =
                            item.getProduct() != null
                                    && item.getProduct().getProductName() != null
                                    ? item.getProduct().getProductName()
                                    : "Product";

                    /*
                     * Main product name cell
                     */
                    PdfPCell productCell =
                            new PdfPCell(
                                    new Phrase(
                                            productName,
                                            normalFont
                                    )
                            );

                    productCell.setPadding(5);

                    itemTable.addCell(productCell);

                    /*
                     * Quantity
                     */
                    itemTable.addCell(
                            createTableCell(
                                    String.valueOf(
                                            item.getQuantity() != null
                                                    ? item.getQuantity()
                                                    : 0
                                    ),
                                    normalFont,
                                    Element.ALIGN_CENTER
                            )
                    );

                    /*
                     * Original Rate
                     */
                    itemTable.addCell(
                            createTableCell(
                                    money(
                                            item.getUnitPrice()
                                    ),
                                    normalFont,
                                    Element.ALIGN_RIGHT
                            )
                    );

                    /*
                     * Subtotal
                     */
                    itemTable.addCell(
                            createTableCell(
                                    money(
                                            item.getSubtotalAmount()
                                    ),
                                    normalFont,
                                    Element.ALIGN_RIGHT
                            )
                    );

                    /*
                     * Discount
                     *
                     * Example:
                     * 10%
                     * - ₹100
                     */
                    String discountText =
                            formatPercentage(
                                    item.getCustomerDiscountPercentage()
                            )
                                    + "\n- "
                                    + money(
                                    item.getDiscountAmount()
                            );

                    itemTable.addCell(
                            createTableCell(
                                    discountText,
                                    normalFont,
                                    Element.ALIGN_RIGHT
                            )
                    );

                    /*
                     * GST DETAILS
                     *
                     * CGST 9% = ₹81
                     * SGST 9% = ₹81
                     */
                    String gstText =
                            buildGstText(item);

                    itemTable.addCell(
                            createTableCell(
                                    gstText,
                                    smallFont,
                                    Element.ALIGN_RIGHT
                            )
                    );

                    /*
                     * Final Item Total
                     */
                    itemTable.addCell(
                            createTableCell(
                                    money(
                                            item.getTotalPrice()
                                    ),
                                    headingFont,
                                    Element.ALIGN_RIGHT
                            )
                    );
                }
            }

            document.add(itemTable);

            document.add(
                    new Paragraph("\n")
            );

            /*
             * =========================================================
             * ORDER SUMMARY
             * =========================================================
             */
            PdfPTable summaryTable =
                    new PdfPTable(2);

            summaryTable.setWidthPercentage(55);

            summaryTable.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            summaryTable.setWidths(
                    new float[]{1.5f, 1f}
            );

            addSummaryRow(
                    summaryTable,
                    "Subtotal",
                    money(
                            order.getSubtotalAmount()
                    ),
                    normalFont,
                    normalFont
            );

            addSummaryRow(
                    summaryTable,
                    "Total Discount",
                    "- " + money(
                            order.getTotalDiscountAmount()
                    ),
                    normalFont,
                    normalFont
            );

            addSummaryRow(
                    summaryTable,
                    "Taxable Amount",
                    money(
                            order.getTaxableAmount()
                    ),
                    normalFont,
                    normalFont
            );

            /*
             * GST BREAKUP
             */
            if (value(order.getTotalCgstAmount()) > 0) {

                addSummaryRow(
                        summaryTable,
                        "Total CGST",
                        money(
                                order.getTotalCgstAmount()
                        ),
                        normalFont,
                        normalFont
                );
            }

            if (value(order.getTotalSgstAmount()) > 0) {

                addSummaryRow(
                        summaryTable,
                        "Total SGST",
                        money(
                                order.getTotalSgstAmount()
                        ),
                        normalFont,
                        normalFont
                );
            }

            if (value(order.getTotalUgstAmount()) > 0) {

                addSummaryRow(
                        summaryTable,
                        "Total UGST",
                        money(
                                order.getTotalUgstAmount()
                        ),
                        normalFont,
                        normalFont
                );
            }

            if (value(order.getTotalIgstAmount()) > 0) {

                addSummaryRow(
                        summaryTable,
                        "Total IGST",
                        money(
                                order.getTotalIgstAmount()
                        ),
                        normalFont,
                        normalFont
                );
            }

            addSummaryRow(
                    summaryTable,
                    "Total GST",
                    money(
                            order.getTotalGstAmount()
                    ),
                    headingFont,
                    headingFont
            );

            addSummaryRow(
                    summaryTable,
                    "GRAND TOTAL",
                    money(
                            order.getTotalAmount()
                    ),
                    headingFont,
                    headingFont
            );

            document.add(summaryTable);

            document.add(
                    new Paragraph("\n")
            );

            /*
             * =========================================================
             * PAYMENT DETAILS
             * =========================================================
             */
            Paragraph paymentHeading =
                    new Paragraph(
                            "Payment Details",
                            headingFont
                    );

            document.add(paymentHeading);

            PdfPTable paymentTable =
                    new PdfPTable(2);

            paymentTable.setWidthPercentage(100);

            paymentTable.setWidths(
                    new float[]{1.5f, 1f}
            );

            addSummaryRow(
                    paymentTable,
                    "Payment Status",
                    paymentStatus.name(),
                    normalFont,
                    headingFont
            );

            addSummaryRow(
                    paymentTable,
                    "Paid Amount",
                    money(paidAmount),
                    normalFont,
                    normalFont
            );

            addSummaryRow(
                    paymentTable,
                    "Due Amount",
                    money(dueAmount),
                    normalFont,
                    headingFont
            );

            document.add(paymentTable);

            /*
             * =========================================================
             * ACCEPTING PAYMENT ACCOUNTS
             * =========================================================
             */
            List<BankAccount> receivingAccounts = bankAccountRepository
                    .findByActiveTrueAndAcceptingPaymentsTrueOrderByDefaultAccountDescIdDesc();

            if (!receivingAccounts.isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Bank / Payment Details", headingFont));
                for (BankAccount account : receivingAccounts) {
                    StringBuilder details = new StringBuilder();
                    if (account.getAccountName() != null) details.append(account.getAccountName()).append("\n");
                    if (account.getBankName() != null) details.append("Bank: ").append(account.getBankName()).append(" | ");
                    if (account.getAccountType() != null) details.append("Type: ").append(account.getAccountType()).append("\n");
                    if (account.getAccountNumber() != null) details.append("Account Number: ").append(account.getAccountNumber()).append("\n");
                    if (account.getIfscCode() != null) details.append("IFSC: ").append(account.getIfscCode()).append("\n");
                    if (account.getBranchName() != null) details.append("Branch: ").append(account.getBranchName()).append("\n");
                    if (account.getUpiId() != null) details.append("UPI: ").append(account.getUpiId());
                    document.add(new Paragraph(details.toString(), smallFont));
                    document.add(new Paragraph("\n"));
                }
            }

            /*
             * =========================================================
             * TERMS AND CONDITIONS
             * =========================================================
             */
            document.add(
                    new Paragraph("\n")
            );

            Paragraph termsHeading =
                    new Paragraph(
                            "Terms & Conditions",
                            headingFont
                    );

            document.add(termsHeading);

            String termsAndConditions =
                    storeSettings != null
                            && storeSettings.getTermsAndConditions() != null
                            && !storeSettings
                            .getTermsAndConditions()
                            .trim()
                            .isEmpty()
                            ? storeSettings
                            .getTermsAndConditions()
                            : "Thank you for your business.";

            document.add(
                    new Paragraph(
                            termsAndConditions,
                            smallFont
                    )
            );

            /*
             * =========================================================
             * FOOTER
             * =========================================================
             */
            document.add(
                    new Paragraph("\n")
            );

            Paragraph footer =
                    new Paragraph(
                            "This is a computer-generated invoice.",
                            smallFont
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(footer);

            document.close();

        } catch (Exception exception) {

            if (document.isOpen()) {
                document.close();
            }

            throw new RuntimeException(
                    "Failed to generate invoice PDF",
                    exception
            );
        }

        return new ByteArrayInputStream(
                outputStream.toByteArray()
        );
    }

    /*
     * =============================================================
     * HELPER METHODS
     * =============================================================
     */

    private double value(Double value) {
        return value != null ? value : 0.0;
    }

    private String money(Double amount) {

        return "₹ "
                + String.format(
                "%.2f",
                value(amount)
        );
    }

    private String money(double amount) {

        return "₹ "
                + String.format(
                "%.2f",
                amount
        );
    }

    private String formatPercentage(
            Double percentage
    ) {

        double value =
                percentage != null
                        ? percentage
                        : 0.0;

        return String.format(
                "%.2f%%",
                value
        );
    }

    /*
     * Creates:
     *
     * Label: Value
     */
    private Paragraph createLabelValueParagraph(
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        Paragraph paragraph =
                new Paragraph();

        paragraph.add(
                new Phrase(
                        label + ": ",
                        labelFont
                )
        );

        paragraph.add(
                new Phrase(
                        value,
                        valueFont
                )
        );

        return paragraph;
    }

    private void addTableHeader(
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

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(6);

        cell.setBackgroundColor(
                new Color(
                        51,
                        65,
                        85
                )
        );

        table.addCell(cell);
    }

    private PdfPCell createTableCell(
            String text,
            Font font,
            int alignment
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                font
                        )
                );

        cell.setHorizontalAlignment(
                alignment
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(5);

        return cell;
    }

    private void addSummaryRow(
            PdfPTable table,
            String label,
            String amount,
            Font labelFont,
            Font amountFont
    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont
                        )
                );

        labelCell.setPadding(5);

        PdfPCell amountCell =
                new PdfPCell(
                        new Phrase(
                                amount,
                                amountFont
                        )
                );

        amountCell.setPadding(5);

        amountCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        table.addCell(labelCell);

        table.addCell(amountCell);
    }

    /*
     * Creates GST details for one item.
     *
     * Example:
     *
     * CGST 9.00%
     * ₹ 81.00
     *
     * SGST 9.00%
     * ₹ 81.00
     *
     * Total: ₹ 162.00
     */
    private String buildGstText(
            InventoryOrderItem item
    ) {

        StringBuilder gstText =
                new StringBuilder();

        boolean hasGst = false;

        /*
         * CGST
         */
        if (
                value(item.getCgstPercentage()) > 0
                        || value(item.getCgstAmount()) > 0
        ) {

            gstText.append("CGST ")
                    .append(
                            formatPercentage(
                                    item.getCgstPercentage()
                            )
                    )
                    .append("\n")
                    .append(
                            money(
                                    item.getCgstAmount()
                            )
                    );

            hasGst = true;
        }

        /*
         * SGST
         */
        if (
                value(item.getSgstPercentage()) > 0
                        || value(item.getSgstAmount()) > 0
        ) {

            if (hasGst) {
                gstText.append("\n");
            }

            gstText.append("SGST ")
                    .append(
                            formatPercentage(
                                    item.getSgstPercentage()
                            )
                    )
                    .append("\n")
                    .append(
                            money(
                                    item.getSgstAmount()
                            )
                    );

            hasGst = true;
        }

        /*
         * UGST
         */
        if (
                value(item.getUgstPercentage()) > 0
                        || value(item.getUgstAmount()) > 0
        ) {

            if (hasGst) {
                gstText.append("\n");
            }

            gstText.append("UGST ")
                    .append(
                            formatPercentage(
                                    item.getUgstPercentage()
                            )
                    )
                    .append("\n")
                    .append(
                            money(
                                    item.getUgstAmount()
                            )
                    );

            hasGst = true;
        }

        /*
         * IGST
         */
        if (
                value(item.getIgstPercentage()) > 0
                        || value(item.getIgstAmount()) > 0
        ) {

            if (hasGst) {
                gstText.append("\n");
            }

            gstText.append("IGST ")
                    .append(
                            formatPercentage(
                                    item.getIgstPercentage()
                            )
                    )
                    .append("\n")
                    .append(
                            money(
                                    item.getIgstAmount()
                            )
                    );

            hasGst = true;
        }

        /*
         * No GST
         */
        if (!hasGst) {
            return "No GST";
        }

        /*
         * Total GST
         */
        gstText.append("\nTotal GST")
                .append("\n")
                .append(
                        money(
                                item.getTotalGstAmount()
                        )
                );

        return gstText.toString();
    }

    /*
     * Payment status is calculated from database values.
     *
     * Rules:
     *
     * No payment / ₹0 paid
     *      -> UNPAID
     *
     * Paid amount < Order Total
     *      -> PARTIAL
     *
     * Paid amount >= Order Total
     *      -> PAID
     */
    private PaymentStatus calculatePaymentStatus(
            Double orderTotal,
            double paidAmount,
            List<InventoryPayment> payments
    ) {

        double total =
                value(orderTotal);

        /*
         * If no payment exists
         */
        if (
                payments == null
                        || payments.isEmpty()
        ) {
            return PaymentStatus.UNPAID;
        }

        /*
         * Nothing actually paid
         */
        if (paidAmount <= 0) {
            return PaymentStatus.UNPAID;
        }

        /*
         * Full amount paid
         */
        if (paidAmount >= total) {
            return PaymentStatus.PAID;
        }

        /*
         * Some amount paid
         */
        return PaymentStatus.PARTIAL;
    }
}