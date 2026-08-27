package com.apex.economy.service;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
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
            sender.sendMessage("Â§cAnda tidak dapat mentransfer saldo ke diri sendiri!");
            return false;
        }

        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            sender.sendMessage("Â§cJumlah transfer tidak valid!");
            return false;
        }

        if (!currency.isTransferable()) {
            sender.sendMessage("Â§cMata uang " + currency.getDisplayName() + " tidak dapat ditransfer antar pemain!");
            return false;
        }

        CurrencyService cs = plugin.getCurrencyService();
        if (!cs.has(sender.getUniqueId(), currency.getId(), amount)) {
            sender.sendMessage("Â§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi untuk mentransfer sejumlah itu!");
            return false;
        }

        // Atomic transfer
        cs.removeBalance(sender.getUniqueId(), currency.getId(), amount);
        cs.addBalance(receiverUuid, currency.getId(), amount);

        String formatted = NumberFormatUtil.format(amount, currency);

        // Sender notification
        sender.sendMessage("Â§a[âœ”] Berhasil mentransfer Â§e" + formatted + " Â§akepada Â§e" + receiverName + "Â§a.");

        // Receiver notification if online
        Player targetPlayer = Bukkit.getPlayer(receiverUuid);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage("Â§a[âœ”] Anda menerima kiriman saldo Â§e" + formatted + " Â§adari Â§e" + sender.getName() + "Â§a.");
        }

        return true;
    }
}
