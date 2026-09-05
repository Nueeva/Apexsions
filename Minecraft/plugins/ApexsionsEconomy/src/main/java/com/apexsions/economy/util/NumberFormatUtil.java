package com.apexsions.economy.util;

import com.apexsions.economy.currency.Currency;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberFormatUtil {

    private static final DecimalFormatSymbols INDO_SYMBOLS = new DecimalFormatSymbols(new Locale("id", "ID"));

    private NumberFormatUtil() {}

    /**
     * Format raw amount into Indonesian compact notation (K, Jt, M, T).
     * Examples: 500 -> 500, 10000 -> 10K, 1500000 -> 1,5Jt, 2000000000 -> 2M, 1000000000000 -> 1T
     */
    public static String formatCompact(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) return "0";
        if (amount < 0) return "-" + formatCompact(-amount);

        if (amount >= 1_000_000_000_000.0) {
            return formatValue(amount / 1_000_000_000_000.0) + "T";
        }
        if (amount >= 1_000_000_000.0) {
            return formatValue(amount / 1_000_000_000.0) + "M";
        }
        if (amount >= 1_000_000.0) {
            return formatValue(amount / 1_000_000.0) + "Jt";
        }
        if (amount >= 1_000.0) {
            return formatValue(amount / 1_000.0) + "K";
        }

        // Less than 1,000
        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }
        DecimalFormat df = new DecimalFormat("#,##0.##", INDO_SYMBOLS);
        return df.format(amount);
    }

    private static String formatValue(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        DecimalFormat df = new DecimalFormat("0.#", INDO_SYMBOLS);
        return df.format(value);
    }

    /**
     * Full comma-separated standard format with currency symbol.
     */
    public static String formatFull(double amount, Currency currency) {
        DecimalFormat df = new DecimalFormat("#,##0", INDO_SYMBOLS);
        String num = df.format(amount);
        if (currency == null) return num;
        if (currency.isPrefix()) {
            return currency.getSymbol() + num;
        } else {
            return num + " " + currency.getSymbol();
        }
    }

    /**
     * Compact format with currency symbol (e.g. Rp1,5Jt or 10K Diamond).
     */
    public static String format(double amount, Currency currency) {
        String compact = formatCompact(amount);
        if (currency == null) return compact;
        if (currency.isPrefix()) {
            return currency.getSymbol() + compact;
        } else {
            return compact + " " + currency.getSymbol();
        }
    }

    /**
     * Parse human input (e.g. "10k", "1.5m", "1,5jt", "2t", "500") into double.
     */
    public static double parse(String input) throws NumberFormatException {
        if (input == null || input.isBlank()) throw new NumberFormatException("Input kosong");
        String clean = input.trim().toLowerCase().replace(" ", "").replace("rp", "").replace("diamond", "").replace("💎", "");

        double multiplier = 1.0;
        if (clean.endsWith("t")) {
            multiplier = 1_000_000_000_000.0;
            clean = clean.substring(0, clean.length() - 1);
        } else if (clean.endsWith("m")) {
            multiplier = 1_000_000_000.0;
            clean = clean.substring(0, clean.length() - 1);
        } else if (clean.endsWith("jt")) {
            multiplier = 1_000_000.0;
            clean = clean.substring(0, clean.length() - 2);
        } else if (clean.endsWith("j")) {
            multiplier = 1_000_000.0;
            clean = clean.substring(0, clean.length() - 1);
        } else if (clean.endsWith("k") || clean.endsWith("rb")) {
            multiplier = 1_000.0;
            clean = clean.endsWith("rb") ? clean.substring(0, clean.length() - 2) : clean.substring(0, clean.length() - 1);
        }

        clean = clean.replace(",", ".");
        double val = Double.parseDouble(clean);
        return val * multiplier;
    }
}
