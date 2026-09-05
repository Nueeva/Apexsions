package com.apexsions.economy.trade;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.service.CurrencyService;
import com.apexsions.economy.trade.gui.TradeMenu;
import com.apexsions.economy.util.NumberFormatUtil;
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
        if (!cs.has(player.getUniqueId(), currency.getId(), amount)) {
            player.sendMessage("§cSaldo " + currency.getDisplayName() + " Anda tidak mencukupi!");
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
            // Verify balance if money is offered
            if (offer.getCurrency() != null && offer.getMoneyAmount() > 0) {
                CurrencyService cs = plugin.getCurrencyService();
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

        // 2. Verify balances
        if (offer1.getCurrency() != null && offer1.getMoneyAmount() > 0) {
            if (!cs.has(player1.getUniqueId(), offer1.getCurrency().getId(), offer1.getMoneyAmount())) {
                cancelTrade(player1, "Saldo " + player1.getName() + " tidak mencukupi");
                return;
            }
        }

        if (offer2.getCurrency() != null && offer2.getMoneyAmount() > 0) {
            if (!cs.has(player2.getUniqueId(), offer2.getCurrency().getId(), offer2.getMoneyAmount())) {
                cancelTrade(player2, "Saldo " + player2.getName() + " tidak mencukupi");
                return;
            }
        }

        // Mark as completed
        state = TradeState.COMPLETED;
        this.temporarilyClosing = true;

        plugin.getCurrencyService().getLockManager().executeWithDualAccountLock(player1.getUniqueId(), player2.getUniqueId(), () -> {
            // 3. Swap Currency
            if (offer1.getCurrency() != null && offer1.getMoneyAmount() > 0) {
                cs.transferAtomic(player1.getUniqueId(), player2.getUniqueId(), offer1.getCurrency().getId(), offer1.getMoneyAmount());
            }

            if (offer2.getCurrency() != null && offer2.getMoneyAmount() > 0) {
                cs.transferAtomic(player2.getUniqueId(), player1.getUniqueId(), offer2.getCurrency().getId(), offer2.getMoneyAmount());
            }

            // 4. Swap Items (Player 1 receives Offer 2, Player 2 receives Offer 1)
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
        });

        // 5. Sound & Messages
        try {
            player1.playSound(player1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player2.playSound(player2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (Throwable ignored) {}

        player1.sendMessage("§a§l=======================================");
        player1.sendMessage("§a[✔] Transaksi Trade dengan §e" + player2.getName() + " §aberhasil diselesaikan!");
        sendTradeSummary(player1, offer1, offer2);
        player1.sendMessage("§a§l=======================================");

        player2.sendMessage("§a§l=======================================");
        player2.sendMessage("§a[✔] Transaksi Trade dengan §e" + player1.getName() + " §aberhasil diselesaikan!");
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

        String cancelMsg = "§c[✖] Trade telah dibatalkan" 
                + (canceller != null ? " oleh §e" + canceller.getName() : "") 
                + (reason != null ? " §7(" + reason + ")" : ".");

        if (player1.isOnline()) player1.sendMessage(cancelMsg);
        if (player2.isOnline()) player2.sendMessage(cancelMsg);

        if (player1.isOnline()) player1.closeInventory();
        if (player2.isOnline()) player2.closeInventory();

        plugin.getTradeManager().endSession(this);
    }

    public void refreshBothGuis() {
        if (state != TradeState.ACTIVE) return;
        if (menu1 != null && player1.isOnline()) {
            menu1.refreshContents();
        }
        if (menu2 != null && player2.isOnline()) {
            menu2.refreshContents();
        }
    }

    public boolean isTemporarilyClosing() {
        return temporarilyClosing;
    }

    public void setTemporarilyClosing(boolean temporarilyClosing) {
        this.temporarilyClosing = temporarilyClosing;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public TradeOffer getOffer1() {
        return offer1;
    }

    public TradeOffer getOffer2() {
        return offer2;
    }

    public TradeState getState() {
        return state;
    }

    public boolean containsPlayer(UUID uuid) {
        return (player1 != null && player1.getUniqueId().equals(uuid)) || (player2 != null && player2.getUniqueId().equals(uuid));
    }
}
