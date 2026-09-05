package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayService {

    private final ApexsionsEconomy plugin;

    public PayService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    public synchronized boolean transfer(Player sender, UUID receiverUuid, String receiverName, Currency currency, double amount) {
        if (sender == null || receiverUuid == null || currency == null) return false;

        if (sender.getUniqueId().equals(receiverUuid)) {
            sender.sendMessage("§cAnda tidak dapat mentransfer saldo ke diri sendiri!");
            return false;
        }

        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage("§cJumlah transfer tidak valid!");
            return false;
        }

        if (!currency.isTransferable()) {
            sender.sendMessage("§cMata uang " + currency.getDisplayName() + " tidak dapat ditransfer antar pemain!");
            return false;
        }

        CurrencyService cs = plugin.getCurrencyService();
        if (!cs.has(sender.getUniqueId(), currency.getId(), amount)) {
            sender.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi untuk mentransfer sejumlah itu!");
            return false;
        }

        // Calculate Kingdom Transaction Tax
        double taxPercent = 5.0; // Default 5% transaction tax
        String kingdomName = "Umum";

        if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
            var coreApi = com.apexsions.core.api.ApexsionsCoreProvider.get();
            var region = coreApi.getRegion(sender.getUniqueId());
            if (region != null) {
                kingdomName = region.getDisplayName();
                String rKey = region.getKey().toUpperCase();
                taxPercent = switch (rKey) {
                    case "ZENITHAR" -> 10.0;
                    case "SOLTERRA" -> 8.0;
                    case "SYLVAMOOR" -> 6.0;
                    default -> 5.0;
                };
            }
        }

        double taxAmount = (amount * (taxPercent / 100.0));
        double netAmount = amount - taxAmount;

        // Atomic Transaction: withdraw gross from sender, deposit net to receiver
        if (!cs.removeBalance(sender.getUniqueId(), currency.getId(), amount)) {
            sender.sendMessage("§cGagal memproses transfer! Periksa saldo Anda kembali.");
            return false;
        }

        cs.addBalance(receiverUuid, currency.getId(), netAmount);

        String grossFormatted = NumberFormatUtil.format(amount, currency);
        String netFormatted = NumberFormatUtil.format(netAmount, currency);
        String taxFormatted = NumberFormatUtil.format(taxAmount, currency);

        // Sender notification
        sender.sendMessage("§a[✔] Berhasil mentransfer §e" + grossFormatted + " §akepada §e" + receiverName + "§a.");
        if (taxAmount > 0) {
            sender.sendMessage("§7(Potongan Pajak Kerajaan: §c" + taxFormatted + " §8[" + String.format("%.1f", taxPercent) + "%]§7)");
        }

        // Receiver notification if online
        Player targetPlayer = Bukkit.getPlayer(receiverUuid);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage("§a[✔] Anda menerima kiriman saldo bersih §e" + netFormatted + " §a(setelah potongan pajak kerajaan) dari §e" + sender.getName() + "§a.");
        }

        return true;
    }
}
