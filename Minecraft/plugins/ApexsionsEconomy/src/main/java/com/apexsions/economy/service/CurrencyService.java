package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, Atomic Multi-Currency Management Service.
 */
public class CurrencyService {

    private final ApexsionsEconomy plugin;
    private final TransactionLockManager lockManager;
    private final Map<UUID, Map<String, Double>> balanceCache = new ConcurrentHashMap<>();

    public CurrencyService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
        this.lockManager = new TransactionLockManager();
    }

    public TransactionLockManager getLockManager() {
        return lockManager;
    }

    public double getBalance(@NotNull UUID uuid, @NotNull String currencyId) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        Map<String, Double> userMap = balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        if (userMap.containsKey(key)) {
            return userMap.get(key);
        }

        Currency currency = plugin.getCurrencyRegistry().get(key);
        double starting = (currency != null) ? currency.getStartingBalance() : 0.0;

        return lockManager.executeWithAccountLock(uuid, () -> {
            if (userMap.containsKey(key)) {
                return userMap.get(key);
            }
            try {
                double bal = plugin.getRepository().loadBalance(uuid, key, starting).join();
                userMap.put(key, bal);
                return bal;
            } catch (Exception e) {
                return starting;
            }
        });
    }

    public CompletableFuture<Double> loadBalanceAsync(@NotNull UUID uuid, @NotNull String currencyId) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        Currency currency = plugin.getCurrencyRegistry().get(key);
        double starting = (currency != null) ? currency.getStartingBalance() : 0.0;

        return plugin.getRepository().loadBalance(uuid, key, starting).thenApply(bal -> {
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, bal);
            return bal;
        });
    }

    public void setBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        double safeAmount = Math.max(0.0, amount);

        lockManager.executeWithAccountLock(uuid, () -> {
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, safeAmount);
            plugin.getRepository().saveBalance(uuid, key, safeAmount);
        });
    }

    public void addBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        if (amount <= 0.0) return;
        String key = currencyId.toLowerCase(Locale.ROOT);

        lockManager.executeWithAccountLock(uuid, () -> {
            double current = getBalance(uuid, key);
            double newBalance = current + amount;
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, newBalance);
            plugin.getRepository().saveBalance(uuid, key, newBalance);
        });
    }

    public boolean removeBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        if (amount <= 0.0) return false;
        String key = currencyId.toLowerCase(Locale.ROOT);

        return lockManager.executeWithAccountLock(uuid, () -> {
            double current = getBalance(uuid, key);
            if (current < amount) {
                return false;
            }
            double newBalance = current - amount;
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, newBalance);
            plugin.getRepository().saveBalance(uuid, key, newBalance);
            return true;
        });
    }

    /**
     * Executes atomic multi-party currency transfer with deadlock prevention.
     */
    public boolean transferAtomic(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull String currencyId, double amount) {
        if (amount <= 0.0 || senderUuid.equals(receiverUuid)) return false;
        String key = currencyId.toLowerCase(Locale.ROOT);

        return lockManager.executeWithDualAccountLock(senderUuid, receiverUuid, () -> {
            double senderBal = getBalance(senderUuid, key);
            if (senderBal < amount) {
                return false;
            }

            double receiverBal = getBalance(receiverUuid, key);

            double newSenderBal = senderBal - amount;
            double newReceiverBal = receiverBal + amount;

            balanceCache.computeIfAbsent(senderUuid, k -> new ConcurrentHashMap<>()).put(key, newSenderBal);
            balanceCache.computeIfAbsent(receiverUuid, k -> new ConcurrentHashMap<>()).put(key, newReceiverBal);

            plugin.getRepository().saveBalance(senderUuid, key, newSenderBal);
            plugin.getRepository().saveBalance(receiverUuid, key, newReceiverBal);
            return true;
        });
    }

    public boolean has(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        return getBalance(uuid, currencyId) >= amount;
    }

    public double getStartingBalance(@NotNull String currencyId) {
        Currency currency = plugin.getCurrencyRegistry().get(currencyId.toLowerCase(Locale.ROOT));
        return (currency != null) ? currency.getStartingBalance() : 0.0;
    }

    public void invalidateCache(@NotNull UUID uuid) {
        balanceCache.remove(uuid);
    }
}
