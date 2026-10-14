package com.InventoryManagementSystem.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Renders an {@link ExportTable} as a landscape A4 PDF report (OpenPDF, same library as the invoices). */
public final class PdfTableExportUtil {

    private static final Color HEADER_BG = new Color(30, 64, 175);
    private static final Color ZEBRA_BG = new Color(241, 245, 249);
    private static final Color TOTAL_BG = new Color(226, 232, 240);
    private static final Color BORDER = new Color(203, 213, 225);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(15, 23, 42));
    private static final Font META_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(71, 85, 105));
    private static final Font HEAD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(15, 23, 42));
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new Color(15, 23, 42));
    private static final Font FOOT_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(100, 116, 139));

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private PdfTableExportUtil() {}

    public static byte[] render(ExportTable t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 24, 24, 28, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.open();

            document.add(new Paragraph(t.getTitle(), TITLE_FONT));

            Paragraph meta = new Paragraph();
            meta.setFont(META_FONT);
            meta.setSpacingBefore(2);
            meta.setSpacingAfter(8);
            meta.add(new Phrase("Generated: " + LocalDateTime.now().format(STAMP)
                    + "   |   Records: " + t.getRows().size() + "\n", META_FONT));
            if (t.getFilterLines().isEmpty()) {
                meta.add(new Phrase("Filters: none (all records)", META_FONT));
            } else {
                meta.add(new Phrase("Filters: " + String.join("   |   ", t.getFilterLines()), META_FONT));
            }
            document.add(meta);

            PdfPTable table = new PdfPTable(t.getColumnWidths());
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            for (int c = 0; c < t.getHeaders().size(); c++) {
                PdfPCell cell = cell(t.getHeaders().get(c), HEAD_FONT, HEADER_BG,
                        t.getRightAligned().contains(c) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (t.getRows().isEmpty()) {
                PdfPCell empty = cell("No records match the selected filters.", BODY_FONT, null, Element.ALIGN_CENTER);
                empty.setColspan(t.getHeaders().size());
                empty.setPadding(14);
                table.addCell(empty);
            } else {
                int rowNo = 0;
                for (List<String> row : t.getRows()) {
                    Color bg = (rowNo++ % 2 == 1) ? ZEBRA_BG : null;
                    for (int c = 0; c < row.size(); c++) {
                        table.addCell(cell(row.get(c), BODY_FONT, bg,
                                t.getRightAligned().contains(c) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
                    }
                }
                if (t.getTotalsRow() != null) {
                    List<String> totals = t.getTotalsRow();
                    for (int c = 0; c < totals.size(); c++) {
                        table.addCell(cell(totals.get(c), BOLD_FONT, TOTAL_BG,
                                t.getRightAligned().contains(c) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
                    }
                }
            }

            document.add(table);
            document.close();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate PDF: " + ex.getMessage(), ex);
        }

        return out.toByteArray();
    }

    private static PdfPCell cell(String text, Font font, Color background, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(3);
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(0.5f);
        if (background != null) cell.setBackgroundColor(background);
        return cell;
    }

    /** "Page N" at the bottom-right of every page. */
    private static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), FOOT_FONT),
                    document.right(),
                    document.bottom() - 14,
                    0);
        }
    }
}
