package com.apexsions.economy.currency;

import org.bukkit.Material;

public class DiamondCurrency implements Currency {

    @Override
    public String getId() {
        return "diamond";
    }

    @Override
    public String getDisplayName() {
        return "Diamond";
    }

    @Override
    public String getSymbol() {
        return "Diamond";
    }

    @Override
    public boolean isPrefix() {
        return false;
    }

    @Override
    public double getStartingBalance() {
        return 0.0;
    }

    @Override
    public boolean isTransferable() {
        return true;
    }

    @Override
    public Material getIcon() {
        return Material.DIAMOND;
    }

    @Override
    public int getDecimalPlaces() {
        return 0;
    }
}
