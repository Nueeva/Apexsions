package com.apex.economy.trade;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.service.CurrencyService;
import com.apex.economy.trade.gui.TradeMenu;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class TradeSession {

    public enum TradeState {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    private final ApexsionsEconomy plugin;
    private final Player player1;
    private final Player player2;
    private final TradeOffer offer1 = new TradeOffer();
    private final TradeOffer offer2 = new TradeOffer();
    private TradeState state = TradeState.ACTIVE;

    private TradeMenu menu1;
    private TradeMenu menu2;
    private boolean temporarilyClosing = false;

    public TradeSession(ApexsionsEconomy plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1;
        this.player2 = player2;
    }

    public void start() {
        this.menu1 = new TradeMenu(plugin, player1, this);
        this.menu2 = new TradeMenu(plugin, player2, this);
        menu1.open();
        menu2.open();
    }

    public synchronized double getTransportFee() {
        boolean same = plugin.getApexsionsCoreHook().isSameKingdom(player1.getUniqueId(), player2.getUniqueId());
        if (same) {
            return 0.0;
        }
        return plugin.getConfig().getDouble("trade.cross-kingdom-transport-fee", 5000.0);
    }

    public boolean isCrossKingdom() {
        return !plugin.getApexsionsCoreHook().isSameKingdom(player1.getUniqueId(), player2.getUniqueId());
    }

    public synchronized TradeOffer getOffer(Player player) {
        if (player.getUniqueId().equals(player1.getUniqueId())) return offer1;
        if (player.getUniqueId().equals(player2.getUniqueId())) return offer2;
        return null;
    }

    public synchronized TradeOffer getPartnerOffer(Player player) {
        if (player.getUniqueId().equals(player1.getUniqueId())) return offer2;
        if (player.getUniqueId().equals(player2.getUniqueId())) return offer1;
        return null;
    }

    public synchronized Player getPartner(Player player) {
        if (player.getUniqueId().equals(player1.getUniqueId())) return player2;
        if (player.getUniqueId().equals(player2.getUniqueId())) return player1;
        return null;
    }

    public synchronized boolean addItem(Player player, ItemStack item) {
        if (state != TradeState.ACTIVE || item == null) return false;
        TradeOffer offer = getOffer(player);
        if (offer == null || offer.isFull()) return false;

        boolean added = offer.addItem(item);
        if (added) {
            resetConfirmations();
            refreshBothGuis();
        }
        return added;
    }

    public synchronized ItemStack removeItem(Player player, int slotIndex) {
        if (state != TradeState.ACTIVE) return null;
        TradeOffer offer = getOffer(player);
        if (offer == null) return null;

        ItemStack removed = offer.removeItem(slotIndex);
        if (removed != null) {
            resetConfirmations();
            refreshBothGuis();
        }
        return removed;
    }

    public synchronized boolean setMoneyOffer(Player player, Currency currency, double amount) {
        if (state != TradeState.ACTIVE || currency == null || amount <= 0) return false;
        TradeOffer offer = getOffer(player);
        if (offer == null) return false;

        CurrencyService cs = plugin.getCurrencyService();
        double fee = (currency.getId().equalsIgnoreCase("rupiah")) ? getTransportFee() : 0.0;
        if (!cs.has(player.getUniqueId(), currency.getId(), amount + fee)) {
            player.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi untuk tawaran ini!");
            return false;
        }

        offer.setCurrency(currency);
        offer.setMoneyAmount(amount);
        resetConfirmations();
        refreshBothGuis();
        return true;
    }

    public synchronized void clearMoneyOffer(Player player) {
        if (state != TradeState.ACTIVE) return;
        TradeOffer offer = getOffer(player);
        if (offer != null) {
            offer.clearMoney();
            resetConfirmations();
            refreshBothGuis();
        }
    }

    public synchronized void toggleConfirm(Player player) {
        if (state != TradeState.ACTIVE) return;
        TradeOffer offer = getOffer(player);
        if (offer == null) return;

        if (offer.isConfirmed()) {
            offer.setConfirmed(false);
            refreshBothGuis();
        } else {
            CurrencyService cs = plugin.getCurrencyService();
            double fee = getTransportFee();

            // 1. Verify Transport Fee if Cross-Kingdom
            if (fee > 0) {
                double rupiahOffered = (offer.getCurrency() != null && offer.getCurrency().getId().equalsIgnoreCase("rupiah"))
                        ? offer.getMoneyAmount() : 0.0;
                if (!cs.has(player.getUniqueId(), "rupiah", rupiahOffered + fee)) {
                    player.sendMessage("§cSaldo Rupiah Anda tidak mencukupi untuk membayar biaya transportasi lintas-kerajaan (Rp " + String.format("%,.0f", fee) + ")!");
                    return;
                }
            }

            // 2. Verify balance if non-rupiah money is offered
            if (offer.getCurrency() != null && offer.getMoneyAmount() > 0 && !offer.getCurrency().getId().equalsIgnoreCase("rupiah")) {
                if (!cs.has(player.getUniqueId(), offer.getCurrency().getId(), offer.getMoneyAmount())) {
                    player.sendMessage("§cSaldo " + offer.getCurrency().getDisplayName() + " Anda tidak mencukupi untuk konfirmasi!");
                    return;
                }
            }

            offer.setConfirmed(true);
            refreshBothGuis();

            // If both players have confirmed, complete the trade
            if (offer1.isConfirmed() && offer2.isConfirmed()) {
                completeTrade();
            }
        }
    }

    private synchronized void resetConfirmations() {
        offer1.setConfirmed(false);
        offer2.setConfirmed(false);
    }

    public synchronized void completeTrade() {
        if (state != TradeState.ACTIVE) return;

        // 1. Verify online status
        if (!player1.isOnline() || !player2.isOnline()) {
            cancelTrade(null, "Salah satu pemain terputus dari server");
            return;
        }

        CurrencyService cs = plugin.getCurrencyService();
        double fee = getTransportFee();

        // 2. Verify balances + transport fees
        double p1RupiahRequired = ((offer1.getCurrency() != null && offer1.getCurrency().getId().equalsIgnoreCase("rupiah")) ? offer1.getMoneyAmount() : 0.0) + fee;
        if (!cs.has(player1.getUniqueId(), "rupiah", p1RupiahRequired)) {
            cancelTrade(player1, "Saldo Rupiah " + player1.getName() + " tidak mencukupi untuk biaya trade");
            return;
        }

        double p2RupiahRequired = ((offer2.getCurrency() != null && offer2.getCurrency().getId().equalsIgnoreCase("rupiah")) ? offer2.getMoneyAmount() : 0.0) + fee;
        if (!cs.has(player2.getUniqueId(), "rupiah", p2RupiahRequired)) {
            cancelTrade(player2, "Saldo Rupiah " + player2.getName() + " tidak mencukupi untuk biaya trade");
            return;
        }

        if (offer1.getCurrency() != null && !offer1.getCurrency().getId().equalsIgnoreCase("rupiah") && offer1.getMoneyAmount() > 0) {
            if (!cs.has(player1.getUniqueId(), offer1.getCurrency().getId(), offer1.getMoneyAmount())) {
                cancelTrade(player1, "Saldo " + offer1.getCurrency().getDisplayName() + " " + player1.getName() + " tidak mencukupi");
                return;
            }
        }

        if (offer2.getCurrency() != null && !offer2.getCurrency().getId().equalsIgnoreCase("rupiah") && offer2.getMoneyAmount() > 0) {
            if (!cs.has(player2.getUniqueId(), offer2.getCurrency().getId(), offer2.getMoneyAmount())) {
                cancelTrade(player2, "Saldo " + offer2.getCurrency().getDisplayName() + " " + player2.getName() + " tidak mencukupi");
                return;
            }
        }

        // Mark as completed
        state = TradeState.COMPLETED;
        this.temporarilyClosing = true;

        // 3. Deduct Cross-Kingdom Transport Fees if applicable
        if (fee > 0) {
            cs.removeBalance(player1.getUniqueId(), "rupiah", fee);
            cs.removeBalance(player2.getUniqueId(), "rupiah", fee);
        }

        // 4. Swap Currency Offers
        if (offer1.getCurrency() != null && offer1.getMoneyAmount() > 0) {
            cs.removeBalance(player1.getUniqueId(), offer1.getCurrency().getId(), offer1.getMoneyAmount());
            cs.addBalance(player2.getUniqueId(), offer1.getCurrency().getId(), offer1.getMoneyAmount());
        }

        if (offer2.getCurrency() != null && offer2.getMoneyAmount() > 0) {
            cs.removeBalance(player2.getUniqueId(), offer2.getCurrency().getId(), offer2.getMoneyAmount());
            cs.addBalance(player1.getUniqueId(), offer2.getCurrency().getId(), offer2.getMoneyAmount());
        }

        // 5. Swap Items (Player 1 receives Offer 2, Player 2 receives Offer 1)
        for (ItemStack item : offer2.getItems()) {
            if (item != null) {
                HashMap<Integer, ItemStack> overflow = player1.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player1.getWorld().dropItemNaturally(player1.getLocation(), drop);
                    }
                }
            }
        }

        for (ItemStack item : offer1.getItems()) {
            if (item != null) {
                HashMap<Integer, ItemStack> overflow = player2.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player2.getWorld().dropItemNaturally(player2.getLocation(), drop);
                    }
                }
            }
        }

        // 6. Sound & Messages
        try {
            player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player2.playSound(player2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (Throwable ignored) {}

        player1.sendMessage("§a§l=======================================");
        player1.sendMessage("§a[✔] Transaksi Barter dengan §e" + player2.getName() + " §aberhasil diselesaikan!");
        if (fee > 0) {
            player1.sendMessage("§e[Transportasi] Biaya lintas-kerajaan: §c-Rp " + String.format("%,.0f", fee));
        }
        sendTradeSummary(player1, offer1, offer2);
        player1.sendMessage("§a§l=======================================");

        player2.sendMessage("§a§l=======================================");
        player2.sendMessage("§a[✔] Transaksi Barter dengan §e" + player1.getName() + " §aberhasil diselesaikan!");
        if (fee > 0) {
            player2.sendMessage("§e[Transportasi] Biaya lintas-kerajaan: §c-Rp " + String.format("%,.0f", fee));
        }
        sendTradeSummary(player2, offer2, offer1);
        player2.sendMessage("§a§l=======================================");

        // Close Inventories
        player1.closeInventory();
        player2.closeInventory();

        plugin.getTradeManager().endSession(this);
    }

    private void sendTradeSummary(Player player, TradeOffer myOffer, TradeOffer theirOffer) {
        if (myOffer.getCurrency() != null && myOffer.getMoneyAmount() > 0) {
            player.sendMessage("§7Saldo Terkirim: §c-" + NumberFormatUtil.format(myOffer.getMoneyAmount(), myOffer.getCurrency()));
        }
        if (theirOffer.getCurrency() != null && theirOffer.getMoneyAmount() > 0) {
            player.sendMessage("§7Saldo Diterima: §a+" + NumberFormatUtil.format(theirOffer.getMoneyAmount(), theirOffer.getCurrency()));
        }
    }

    public synchronized void cancelTrade(Player canceller, String reason) {
        if (state != TradeState.ACTIVE) return;
        state = TradeState.CANCELLED;
        this.temporarilyClosing = true;

        // Refund Player 1 items
        for (ItemStack item : offer1.getItems()) {
            if (item != null && player1.isOnline()) {
                HashMap<Integer, ItemStack> overflow = player1.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player1.getWorld().dropItemNaturally(player1.getLocation(), drop);
                    }
                }
            }
        }

        // Refund Player 2 items
        for (ItemStack item : offer2.getItems()) {
            if (item != null && player2.isOnline()) {
                HashMap<Integer, ItemStack> overflow = player2.getInventory().addItem(item);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player2.getWorld().dropItemNaturally(player2.getLocation(), drop);
                    }
                }
            }
        }

        if (canceller != null) {
            String cancelMsg = "§cTrade dibatalkan oleh " + canceller.getName() + (reason != null ? ": " + reason : "");
            player1.sendMessage(cancelMsg);
            player2.sendMessage(cancelMsg);
        } else {
            String cancelMsg = "§cTrade dibatalkan" + (reason != null ? ": " + reason : "");
            if (player1.isOnline()) player1.sendMessage(cancelMsg);
            if (player2.isOnline()) player2.sendMessage(cancelMsg);
        }

        if (player1.isOnline()) player1.closeInventory();
        if (player2.isOnline()) player2.closeInventory();

        plugin.getTradeManager().endSession(this);
    }

    public synchronized void refreshBothGuis() {
        if (state != TradeState.ACTIVE) return;
        if (menu1 != null && player1.isOnline()) menu1.open();
        if (menu2 != null && player2.isOnline()) menu2.open();
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public TradeOffer getOffer1() { return offer1; }
    public TradeOffer getOffer2() { return offer2; }
    public TradeState getState() { return state; }
    public boolean isTemporarilyClosing() { return temporarilyClosing; }
    public void setTemporarilyClosing(boolean temporarilyClosing) { this.temporarilyClosing = temporarilyClosing; }
}
