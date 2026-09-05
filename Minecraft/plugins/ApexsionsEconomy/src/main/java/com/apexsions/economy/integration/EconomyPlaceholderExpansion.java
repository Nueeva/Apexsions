package com.apexsions.economy.integration;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Native PlaceholderAPI Expansion for ApexsionsEconomy.
 */
public class EconomyPlaceholderExpansion extends PlaceholderExpansion {

    private final ApexsionsEconomy plugin;

    public EconomyPlaceholderExpansion(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionseconomy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ApexTeam";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || player.getUniqueId() == null) {
            return "0";
        }

        UUID uuid = player.getUniqueId();
        String param = params.toLowerCase().trim();

        // 1. Specific balance formatted (e.g. balance_rupiah_formatted, balance_diamond_formatted)
        if (param.startsWith("balance_") && param.endsWith("_formatted")) {
            String currId = param.substring("balance_".length(), param.length() - "_formatted".length());
            Currency currency = plugin.getCurrencyRegistry().get(currId);
            if (currency != null) {
                double bal = plugin.getCurrencyService().getBalance(uuid, currency.getId());
                return NumberFormatUtil.format(bal, currency);
            }
            return "0";
        }

        // 2. Specific raw balance (e.g. balance_rupiah, balance_diamond)
        if (param.startsWith("balance_")) {
            String currId = param.substring("balance_".length());
            Currency currency = plugin.getCurrencyRegistry().get(currId);
            if (currency != null) {
                double bal = plugin.getCurrencyService().getBalance(uuid, currency.getId());
                return String.format("%.0f", bal);
            }
            return "0";
        }

        // 3. Shorthand checks
        switch (param) {
            case "rupiah":
                return String.format("%.0f", plugin.getCurrencyService().getBalance(uuid, "rupiah"));
            case "rupiah_formatted":
                return NumberFormatUtil.format(plugin.getCurrencyService().getBalance(uuid, "rupiah"), plugin.getCurrencyRegistry().get("rupiah"));
            case "diamond":
                return String.format("%.0f", plugin.getCurrencyService().getBalance(uuid, "diamond"));
            case "diamond_formatted":
                return NumberFormatUtil.format(plugin.getCurrencyService().getBalance(uuid, "diamond"), plugin.getCurrencyRegistry().get("diamond"));
            case "active_auctions":
                return String.valueOf(plugin.getAuctionService().getActiveAuctions().size());
            default:
                return null;
        }
    }
}
