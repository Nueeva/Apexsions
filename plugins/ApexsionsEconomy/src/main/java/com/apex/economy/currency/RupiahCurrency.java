package com.apex.economy.currency;

import org.bukkit.Material;

public class RupiahCurrency implements Currency {

    @Override
    public String getId() {
        return "rupiah";
    }

    @Override
    public String getDisplayName() {
        return "Rupiah";
    }

    @Override
    public String getSymbol() {
        return "Rp.";
    }

    @Override
    public boolean isPrefix() {
        return true;
    }

    @Override
    public double getStartingBalance() {
        return 1000.0;
    }

    @Override
    public boolean isTransferable() {
        return true;
    }

    @Override
    public Material getIcon() {
        return Material.EMERALD;
    }

    @Override
    public int getDecimalPlaces() {
        return 0;
    }
}
