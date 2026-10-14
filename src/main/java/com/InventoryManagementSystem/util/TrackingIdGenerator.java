package com.InventoryManagementSystem.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Creates shipment tracking ids like TRK261011-K7QX4M (TRK + yyMMdd + 6 random characters). */
public final class TrackingIdGenerator {

    // No 0/O/1/I so an id can be read out over the phone without mistakes.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyMMdd");

    private TrackingIdGenerator() {}

    public static String next() {
        StringBuilder sb = new StringBuilder("TRK").append(LocalDate.now().format(DAY)).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
