package com.apexsions.battlepass.expshop.model;

import org.bukkit.entity.Player;

public class ExpPurchaseContext {

    private final Player player;
    private final ExpPackage expPackage;
    private final String currencyId;
    private final double price;

    public ExpPurchaseContext(Player player, ExpPackage expPackage, String currencyId, double price) {
        this.player = player;
        this.expPackage = expPackage;
        this.currencyId = currencyId;
        this.price = price;
    }

    public Player getPlayer() {
        return player;
    }

    public ExpPackage getExpPackage() {
        return expPackage;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public double getPrice() {
        return price;
    }
}
