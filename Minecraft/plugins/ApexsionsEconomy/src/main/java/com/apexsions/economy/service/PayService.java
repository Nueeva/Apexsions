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

    /**
     * Transfers currency from sender to receiver with a kingdom tax.
     *
     * <p>Threading (M-19): no method-level {@code synchronized}. The principal
     * transfer goes through {@link CurrencyService#transferAtomic} (ordered
     * dual-account locking, no global bottleneck); the tax is settled
     * separately afterwards.</p>
     */
    public boolean transfer(Player sender, UUID receiverUuid, String receiverName, Currency currency, double amount) {
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
        String kingdomKey = null;

        if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
            var coreApi = com.apexsions.core.api.ApexsionsCoreProvider.get();
            var region = coreApi.getRegion(sender.getUniqueId());
            if (region != null) {
                kingdomKey = region.getKey().toLowerCase();
                String rKey = region.getKey().toUpperCase();
                taxPercent = switch (rKey) {
                    case "ZENITHAR" -> 6.0;
                    case "SOLTERRA" -> 8.0;
                    case "SYLVAMOOR" -> 6.0;
                    default -> 5.0;
                };
            }
        }

        double taxAmount = (amount * (taxPercent / 100.0));
        double netAmount = amount - taxAmount;

        // Atomic principal transfer: single dual-account-locked operation
        // (M-19) instead of separate has() -> removeBalance() -> addBalance().
        // transferAtomic re-validates the sender balance under the lock, so the
        // has() check above is only for the friendly message, not for safety.
        if (!cs.transferAtomic(sender.getUniqueId(), receiverUuid, currency.getId(), netAmount)) {
            sender.sendMessage("§cGagal memproses transfer! Periksa saldo Anda kembali.");
            return false;
        }

        // Settle the kingdom tax separately: it is collected from the sender
        // even when there is no kingdom to receive it (burned, as before), but
        // it is only deposited into the treasury when actually collected —
        // tax is never minted from nothing.
        if (taxAmount > 0) {
            if (cs.removeBalance(sender.getUniqueId(), currency.getId(), taxAmount)) {
                if (kingdomKey != null && !kingdomKey.equalsIgnoreCase("NONE")) {
                    plugin.getRepository().depositKingdomTreasury(kingdomKey, currency.getId(), taxAmount);
                }
            } else {
                plugin.getLogger().warning("Failed to collect transfer tax of " + taxAmount
                        + " " + currency.getId() + " from " + sender.getName()
                        + " after successful transfer; tax skipped.");
            }
        }

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
