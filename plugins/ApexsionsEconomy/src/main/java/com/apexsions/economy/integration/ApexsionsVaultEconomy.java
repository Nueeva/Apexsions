package com.apexsions.economy.integration;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Official Vault Economy Implementation for ApexsionsEconomy.
 * Seamlessly integrates Rupiah currency into all Vault-compatible plugins and GUIs.
 */
public class ApexsionsVaultEconomy extends AbstractEconomy {

    private final ApexsionsEconomy plugin;
    private final String currencyId = "rupiah";

    public ApexsionsVaultEconomy(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    private Currency getRupiahCurrency() {
        Currency c = plugin.getCurrencyRegistry().get(currencyId);
        if (c == null) {
            return plugin.getCurrencyRegistry().getDefault();
        }
        return c;
    }

    @Override
    public boolean isEnabled() {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "ApexsionsEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        Currency c = getRupiahCurrency();
        if (c != null) {
            return c.isPrefix() ? c.getSymbol() + String.format("%,.0f", amount) : String.format("%,.0f", amount) + " " + c.getSymbol();
        }
        return "Rp " + String.format("%,.0f", amount);
    }

    @Override
    public String currencyNamePlural() {
        Currency c = getRupiahCurrency();
        return c != null ? c.getDisplayName() : "Rupiah";
    }

    @Override
    public String currencyNameSingular() {
        Currency c = getRupiahCurrency();
        return c != null ? c.getDisplayName() : "Rupiah";
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return hasAccount(op);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return getBalance(op);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null) return 0.0;
        return plugin.getCurrencyService().getBalance(player.getUniqueId(), currencyId);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return has(op, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if (player == null) return false;
        return plugin.getCurrencyService().has(player.getUniqueId(), currencyId, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(op, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null");
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }

        UUID uuid = player.getUniqueId();
        if (!plugin.getCurrencyService().has(uuid, currencyId, amount)) {
            double current = plugin.getCurrencyService().getBalance(uuid, currencyId);
            return new EconomyResponse(0, current, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        boolean success = plugin.getCurrencyService().removeBalance(uuid, currencyId, amount);
        double newBal = plugin.getCurrencyService().getBalance(uuid, currencyId);
        if (success) {
            return new EconomyResponse(amount, newBal, EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(0, newBal, EconomyResponse.ResponseType.FAILURE, "Transaction failed");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(op, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null");
        }
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Amount cannot be negative");
        }

        UUID uuid = player.getUniqueId();
        plugin.getCurrencyService().addBalance(uuid, currencyId, amount);
        double newBal = plugin.getCurrencyService().getBalance(uuid, currencyId);
        return new EconomyResponse(amount, newBal, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    // Bank Methods (Unsupported / Graceful Fallback)
    @Override public EconomyResponse createBank(String name, String player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse deleteBank(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse bankBalance(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse bankHas(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported"); }
    @Override public List<String> getBanks() { return Collections.emptyList(); }
}
