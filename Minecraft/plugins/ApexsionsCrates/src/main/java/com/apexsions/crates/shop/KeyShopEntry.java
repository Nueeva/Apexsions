package com.apexsions.crates.shop;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a Crate Key listed in the Crate Key Shop.
 */
public class KeyShopEntry {

    private final String keyId;
    private boolean enabled;
    private double price;
    private String currency;

    public KeyShopEntry(@NotNull String keyId, boolean enabled, double price, @NotNull String currency) {
        this.keyId = keyId.toLowerCase(Locale.ROOT);
        this.enabled = enabled;
        this.price = Math.max(0.0, price);
        this.currency = (currency == null || currency.isBlank()) ? "rupiah" : currency.toLowerCase(Locale.ROOT);
    }

    @NotNull
    public String getKeyId() {
        return keyId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Math.max(0.0, price);
    }

    @NotNull
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull String currency) {
        this.currency = (currency == null || currency.isBlank()) ? "rupiah" : currency.toLowerCase(Locale.ROOT);
    }

    public boolean isDiamond() {
        return "diamond".equalsIgnoreCase(this.currency);
    }

    public void cycleCurrency() {
        if ("diamond".equalsIgnoreCase(this.currency)) {
            this.currency = "rupiah";
        } else {
            this.currency = "diamond";
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("enabled", enabled);
        map.put("price", price);
        map.put("currency", currency);
        return map;
    }

    public static KeyShopEntry deserialize(@NotNull String keyId, @NotNull Map<String, Object> map) {
        boolean enabled = true;
        if (map.containsKey("enabled")) {
            Object val = map.get("enabled");
            if (val instanceof Boolean b) enabled = b;
            else if (val != null) enabled = Boolean.parseBoolean(val.toString());
        }

        double price = 25000.0;
        if (map.containsKey("price")) {
            Object val = map.get("price");
            if (val instanceof Number n) price = n.doubleValue();
            else if (val != null) {
                try { price = Double.parseDouble(val.toString()); } catch (NumberFormatException ignored) {}
            }
        }

        String currency = "rupiah";
        if (map.containsKey("currency") && map.get("currency") != null) {
            currency = map.get("currency").toString();
        }

        return new KeyShopEntry(keyId, enabled, price, currency);
    }
}
