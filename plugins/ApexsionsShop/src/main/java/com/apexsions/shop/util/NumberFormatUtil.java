package com.apexsions.shop.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberFormatUtil {

    private static final DecimalFormat DF;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN); // uses dots for thousands
        DF = new DecimalFormat("#,##0", symbols);
    }

    private NumberFormatUtil() {}

    public static String formatRupiah(double amount, String symbol) {
        if (symbol == null) symbol = "Rp ";
        return symbol + DF.format(amount);
    }

    public static String formatRupiah(double amount) {
        return formatRupiah(amount, "Rp ");
    }
}
