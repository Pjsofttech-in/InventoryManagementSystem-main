package com.InventoryManagementSystem.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Format-neutral table used by every export (orders, shipments, products).
 * The CSV and PDF renderers both draw from this one object, so both files
 * always contain exactly the same rows.
 */
public class ExportTable {

    private final String title;
    private final List<String> filterLines;
    private final List<String> headers;
    private final float[] columnWidths;       // relative widths used by the PDF
    private final Set<Integer> rightAligned;  // numeric columns (PDF only)
    private final List<List<String>> rows = new ArrayList<>();
    private List<String> totalsRow;           // optional, PDF only

    public ExportTable(String title,
                       List<String> filterLines,
                       List<String> headers,
                       float[] columnWidths,
                       Set<Integer> rightAligned) {
        if (headers.size() != columnWidths.length) {
            throw new IllegalArgumentException("Header count and column width count must match");
        }
        this.title = title;
        this.filterLines = filterLines;
        this.headers = headers;
        this.columnWidths = columnWidths;
        this.rightAligned = rightAligned;
    }

    public void addRow(List<String> row) {
        if (row.size() != headers.size()) {
            throw new IllegalArgumentException("Row has " + row.size() + " cells, expected " + headers.size());
        }
        rows.add(row);
    }

    public void setTotalsRow(List<String> totalsRow) {
        this.totalsRow = totalsRow;
    }

    public String getTitle() { return title; }
    public List<String> getFilterLines() { return filterLines; }
    public List<String> getHeaders() { return headers; }
    public float[] getColumnWidths() { return columnWidths; }
    public Set<Integer> getRightAligned() { return rightAligned; }
    public List<List<String>> getRows() { return rows; }
    public List<String> getTotalsRow() { return totalsRow; }

    // ---- small cell formatters shared by the export builders ----

    public static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** Plain 2-decimal number (no currency symbol / grouping, so it stays a number in Excel). */
    public static String money(Number value) {
        return String.format(Locale.US, "%.2f", value == null ? 0.0 : value.doubleValue());
    }
}
