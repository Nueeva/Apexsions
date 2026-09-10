package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.bank.BankDeposit;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankDepositService {

    private final ApexsionsEconomy plugin;

    public BankDepositService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<List<BankDeposit>> getActiveDeposits(UUID uuid) {
        return plugin.getRepository().loadActiveBankDeposits(uuid);
    }

    public boolean createDeposit(Player player, String currencyId, double principal, int durationDays, double interestRate) {
        if (player == null || principal <= 0) return false;

        CurrencyService cs = plugin.getCurrencyService();
        Currency currency = plugin.getCurrencyRegistry().get(currencyId);
        if (currency == null) {
            player.sendMessage("§cMata uang tidak valid.");
            return false;
        }

        if (!cs.has(player.getUniqueId(), currencyId, principal)) {
            player.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi untuk membuka deposito ini!");
            return false;
        }

        double maxDeposit = plugin.getConfig().getDouble("bank.deposit.max-amount", 100_000_000.0);
        if (principal > maxDeposit) {
            player.sendMessage("§cMaksimal penempatan deposito per transaksi adalah " + NumberFormatUtil.format(maxDeposit, currency) + "!");
            return false;
        }

        // 1. Withdraw principal atomically
        if (!cs.removeBalance(player.getUniqueId(), currencyId, principal)) {
            player.sendMessage("§cGagal memproses penempatan saldo deposito.");
            return false;
        }

        double mult = getRankReturnMultiplier(player);
        double effectiveRate = interestRate * mult;

        long now = System.currentTimeMillis();
        long maturesAt = now + ((long) durationDays * 24 * 60 * 60 * 1000);
        double expectedReturn = Math.floor(principal * (1.0 + effectiveRate));
        String id = UUID.randomUUID().toString().substring(0, 8);

        BankDeposit deposit = new BankDeposit(id, player.getUniqueId(), currencyId, principal, effectiveRate, expectedReturn, now, maturesAt, false);
        plugin.getRepository().saveBankDeposit(deposit);

        String multMsg = mult > 1.0 ? " §6[Bonus Rank " + mult + "x]§a" : "";
        player.sendMessage("§a[✔] Berhasil menempatkan deposito §e" + NumberFormatUtil.format(principal, currency) + " §aselama §e" + durationDays + " hari§a!" + multMsg);
        player.sendMessage("§a[ℹ] Estimasi pencairan saat jatuh tempo: §6§l" + NumberFormatUtil.format(expectedReturn, currency) + " §7(Imbal Hasil: +" + String.format("%.1f", effectiveRate * 100) + "%)");
        return true;
    }

    public double getRankReturnMultiplier(Player player) {
        if (player == null) return 1.0;
        if (player.hasPermission("apexsions.bank.multiplier.sions") || player.hasPermission("apexsions.rank.sions")) return 3.0; // 3x
        if (player.hasPermission("apexsions.bank.multiplier.emperor") || player.hasPermission("apexsions.rank.emperor")) return 2.0; // 2x
        if (player.hasPermission("apexsions.bank.multiplier.sovereign") || player.hasPermission("apexsions.rank.sovereign")) return 1.5; // 1.5x
        if (player.hasPermission("apexsions.bank.multiplier.archon") || player.hasPermission("apexsions.rank.archon")) return 1.2; // 1.2x
        return 1.0;
    }

    public CompletableFuture<Boolean> claimDeposit(Player player, BankDeposit deposit) {
        if (player == null || deposit == null) return CompletableFuture.completedFuture(false);

        if (!deposit.isMatured()) {
            player.sendMessage("§cDeposito ini belum jatuh tempo! Sisa waktu: §e" + deposit.getTimeRemainingFormatted());
            return CompletableFuture.completedFuture(false);
        }

        if (deposit.isClaimed()) {
            player.sendMessage("§cDeposito ini sudah pernah dicairkan!");
            return CompletableFuture.completedFuture(false);
        }

        Currency currency = plugin.getCurrencyRegistry().get(deposit.getCurrencyId());
        return plugin.getRepository().claimBankDeposit(deposit.getId()).thenApply(v -> {
            deposit.setClaimed(true);
            plugin.getCurrencyService().addBalance(player.getUniqueId(), deposit.getCurrencyId(), deposit.getExpectedReturn());
            player.sendMessage("§a[✔] Selamat! Deposito Anda telah dicairkan sebesar §e§l" + NumberFormatUtil.format(deposit.getExpectedReturn(), currency) + "§a!");
            try {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            } catch (Exception ignored) {}
            return true;
        });
    }
}
