package com.InventoryManagementSystem.util;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/** Renders an {@link ExportTable} as RFC-4180 CSV (UTF-8 with BOM so Excel opens it correctly). */
public final class CsvExportUtil {

    private static final Pattern SAFE_NUMBER_OR_PHONE = Pattern.compile("[+-]?[\\d.\\s-]+");

    private CsvExportUtil() {}

    public static byte[] render(ExportTable table) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        appendRow(sb, table.getHeaders());
        for (List<String> row : table.getRows()) {
            appendRow(sb, row);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendRow(StringBuilder sb, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(cells.get(i)));
        }
        sb.append("\r\n");
    }

    private static String escape(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String value = raw;

        // CSV-injection guard: a text cell starting with = + - @ would be run as a formula by Excel.
        if (startsLikeFormula(value)) {
            value = "'" + value;
        }

        boolean needsQuotes = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (needsQuotes) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static boolean startsLikeFormula(String value) {
        char first = value.charAt(0);
        if (first == '=' || first == '@' || first == '\t' || first == '\r') return true;
        if (first == '+' || first == '-') {
            // plain numbers (-12.50) and phone numbers (+91 98765 43210) are fine
            return !SAFE_NUMBER_OR_PHONE.matcher(value).matches();
        }
        return false;
    }
}
