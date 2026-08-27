package com.apex.economy.api;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public final class ApexsionsEconomyAPI {

    private ApexsionsEconomyAPI() {}

    private static ApexsionsEconomy getPlugin() {
        return ApexsionsEconomy.getInstance();
    }

    public static double getBalance(UUID uuid, String currencyId) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return 0.0;
        return plugin.getCurrencyService().getBalance(uuid, currencyId);
    }

    public static boolean has(UUID uuid, String currencyId, double amount) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return false;
        return plugin.getCurrencyService().has(uuid, currencyId, amount);
    }

    public static void deposit(UUID uuid, String currencyId, double amount) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin != null) {
            plugin.getCurrencyService().addBalance(uuid, currencyId, amount);
        }
    }

    public static boolean withdraw(UUID uuid, String currencyId, double amount) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return false;
        return plugin.getCurrencyService().removeBalance(uuid, currencyId, amount);
    }

    public static boolean transfer(UUID senderUuid, UUID receiverUuid, String currencyId, double amount) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return false;
        Currency curr = plugin.getCurrencyRegistry().get(currencyId);
        if (curr == null || !curr.isTransferable()) return false;
        if (!plugin.getCurrencyService().has(senderUuid, currencyId, amount)) return false;

        plugin.getCurrencyService().removeBalance(senderUuid, currencyId, amount);
        plugin.getCurrencyService().addBalance(receiverUuid, currencyId, amount);
        return true;
    }

    public static Collection<Currency> getCurrencies() {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return Collections.emptyList();
        return plugin.getCurrencyRegistry().getAll();
    }

    public static Currency getCurrency(String id) {
        ApexsionsEconomy plugin = getPlugin();
        if (plugin == null) return null;
        return plugin.getCurrencyRegistry().get(id);
    }

    public static String format(double amount, String currencyId) {
        Currency c = getCurrency(currencyId);
        return NumberFormatUtil.format(amount, c);
    }

    public static String formatCompact(double amount) {
        return NumberFormatUtil.formatCompact(amount);
    }
}
