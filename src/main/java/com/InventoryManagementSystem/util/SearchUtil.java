package com.InventoryManagementSystem.util;

/** Helpers for building safe "contains" search patterns. */
public final class SearchUtil {

    private SearchUtil() {}

    /** Trimmed search text, or null when blank. A leading '#' (as in "#12") is ignored. */
    public static String clean(String term) {
        if (term == null) return null;
        String t = term.trim();
        while (t.startsWith("#")) t = t.substring(1).trim();
        return t.isEmpty() ? null : t;
    }

    /** Lower-case %term% pattern with LIKE wildcards in the user's text escaped (escape char is '\'). */
    public static String likePattern(String cleanTerm) {
        String escaped = cleanTerm.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
