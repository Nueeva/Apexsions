package com.apexsions.economy.trade;

import com.apexsions.economy.currency.Currency;
import org.bukkit.inventory.ItemStack;

public class TradeOffer {

    public static final int MAX_ITEMS = 12;
    private final ItemStack[] items = new ItemStack[MAX_ITEMS];
    private Currency currency;
    private double moneyAmount = 0.0;
    private boolean confirmed = false;

    public ItemStack[] getItems() {
        return items;
    }

    public ItemStack getItem(int index) {
        if (index >= 0 && index < MAX_ITEMS) {
            return items[index];
        }
        return null;
    }

    public boolean addItem(ItemStack item) {
        if (item == null) return false;
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (items[i] == null) {
                items[i] = item.clone();
                return true;
            }
        }
        return false;
    }

    public ItemStack removeItem(int index) {
        if (index >= 0 && index < MAX_ITEMS && items[index] != null) {
            ItemStack removed = items[index];
            items[index] = null;
            return removed;
        }
        return null;
    }

    public boolean isFull() {
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (items[i] == null) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (items[i] != null) return false;
        }
        return moneyAmount <= 0;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public double getMoneyAmount() {
        return moneyAmount;
    }

    public void setMoneyAmount(double moneyAmount) {
        this.moneyAmount = Math.max(0.0, moneyAmount);
    }

    public void clearMoney() {
        this.currency = null;
        this.moneyAmount = 0.0;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
