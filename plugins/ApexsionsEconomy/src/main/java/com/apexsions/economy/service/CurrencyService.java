package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyService {

    private final ApexsionsEconomy plugin;
    private final Map<UUID, Map<String, Double>> balanceCache = new ConcurrentHashMap<>();

    public CurrencyService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    public double getBalance(UUID uuid, String currencyId) {
        currencyId = currencyId.toLowerCase();
        Map<String, Double> userMap = balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        if (userMap.containsKey(currencyId)) {
            return userMap.get(currencyId);
        }

        Currency currency = plugin.getCurrencyRegistry().get(currencyId);
        double starting = (currency != null) ? currency.getStartingBalance() : 0.0;

        try {
            double bal = plugin.getRepository().loadBalance(uuid, currencyId, starting).get();
            userMap.put(currencyId, bal);
            return bal;
        } catch (Exception e) {
            return starting;
        }
    }

    public void setBalance(UUID uuid, String currencyId, double amount) {
        currencyId = currencyId.toLowerCase();
        amount = Math.max(0, amount);
        balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(currencyId, amount);
        plugin.getRepository().saveBalance(uuid, currencyId, amount);
    }

    public void addBalance(UUID uuid, String currencyId, double amount) {
        if (amount <= 0) return;
        double current = getBalance(uuid, currencyId);
        setBalance(uuid, currencyId, current + amount);
    }

    public boolean removeBalance(UUID uuid, String currencyId, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid, currencyId);
        if (current < amount) return false;
        setBalance(uuid, currencyId, current - amount);
        return true;
    }

    public boolean has(UUID uuid, String currencyId, double amount) {
        return getBalance(uuid, currencyId) >= amount;
    }

    public double getStartingBalance(String currencyId) {
        Currency currency = plugin.getCurrencyRegistry().get(currencyId);
        return (currency != null) ? currency.getStartingBalance() : 0.0;
    }
}
