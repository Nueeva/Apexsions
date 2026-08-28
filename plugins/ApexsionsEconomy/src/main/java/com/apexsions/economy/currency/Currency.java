package com.apexsions.economy.currency;

import org.bukkit.Material;

public interface Currency {

    String getId();

    String getDisplayName();

    String getSymbol();

    boolean isPrefix();

    double getStartingBalance();

    boolean isTransferable();

    Material getIcon();

    int getDecimalPlaces();
}
