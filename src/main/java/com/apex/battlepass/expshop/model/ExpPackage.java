package com.apex.battlepass.expshop.model;

import java.util.Collections;
import java.util.Map;

public class ExpPackage {

    private final String id;
    private final String displayName;
    private final int expAmount;
    private final Map<String, Double> prices;

    public ExpPackage(String id, String displayName, int expAmount, Map<String, Double> prices) {
        this.id = id;
        this.displayName = displayName != null ? displayName : ("+" + expAmount + " BP XP");
        this.expAmount = expAmount;
        this.prices = prices != null ? prices : Collections.emptyMap();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getExpAmount() {
        return expAmount;
    }

    public Map<String, Double> getPrices() {
        return prices;
    }

    public double getPrice(String currencyId) {
        return prices.getOrDefault(currencyId.toLowerCase(), 0.0);
    }
}
