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
 *
 * <p>Read path is cache-only (M-1): {@link #getBalance} never blocks the calling
 * thread. On a cache miss it kicks off an asynchronous load and returns the
 * currency's starting balance as a provisional value; the cache self-heals once
 * the load completes. Balance-mutating methods use an internal blocking load
 * ({@link #getBalanceBlocking}) under the account lock so a cold cache can never
 * corrupt a mutation.</p>
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

    /**
     * Returns the cached balance for a player. Never blocks.
     *
     * <p>On a cache miss an asynchronous load is triggered (fire-and-forget) and
     * the currency's starting balance is returned as a provisional value. Callers
     * that mutate balances must NOT rely on this for the authoritative figure;
     * the mutation methods below load the real balance under the account lock.</p>
     */
    public double getBalance(@NotNull UUID uuid, @NotNull String currencyId) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        Map<String, Double> userMap = balanceCache.get(uuid);
        if (userMap != null) {
            Double cached = userMap.get(key);
            if (cached != null) {
                return cached;
            }
        }

        // Cache miss: refresh asynchronously, return provisional starting balance.
        loadBalanceAsync(uuid, key);
        return getStartingBalance(key);
    }

    /**
     * Blocking balance load used ONLY by the mutation methods below.
     * Runs under the account lock; never called from a cache-only read path.
     */
    private double getBalanceBlocking(@NotNull UUID uuid, @NotNull String currencyId) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        Map<String, Double> userMap = balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        if (userMap.containsKey(key)) {
            return userMap.get(key);
        }

        double starting = getStartingBalance(key);

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
        double starting = getStartingBalance(key);

        return plugin.getRepository().loadBalance(uuid, key, starting).thenApply(bal -> {
            // putIfAbsent: a late async load must never clobber a newer value
            // written by a mutation that ran while the load was in flight.
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).putIfAbsent(key, bal);
            return bal;
        });
    }

    /**
     * Kicks off asynchronous balance preloads for every registered currency.
     * Fire-and-forget: never blocks the calling thread (M-1).
     */
    public void preloadBalances(@NotNull UUID uuid) {
        for (Currency currency : plugin.getCurrencyRegistry().getAll()) {
            loadBalanceAsync(uuid, currency.getId());
        }
    }

    public void setBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        String key = currencyId.toLowerCase(Locale.ROOT);
        double safeAmount = Math.max(0.0, amount);

        lockManager.executeWithAccountLock(uuid, () -> {
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, safeAmount);
            plugin.getRepository().saveBalance(uuid, key, safeAmount);
            // M-6: audit trail (best-effort async; failure is logged, never breaks the mutation)
            plugin.getRepository().logTransaction(null, uuid, key, safeAmount, "SET", "balance set to " + safeAmount);
        });
    }

    public void addBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        if (amount <= 0.0) return;
        String key = currencyId.toLowerCase(Locale.ROOT);

        lockManager.executeWithAccountLock(uuid, () -> {
            double current = getBalanceBlocking(uuid, key);
            double newBalance = current + amount;
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, newBalance);
            plugin.getRepository().saveBalance(uuid, key, newBalance);
            // M-6: audit trail (best-effort async; failure is logged, never breaks the mutation)
            plugin.getRepository().logTransaction(null, uuid, key, amount, "ADD",
                    "added " + amount + ", new balance " + newBalance);
        });
    }

    public boolean removeBalance(@NotNull UUID uuid, @NotNull String currencyId, double amount) {
        if (amount <= 0.0) return false;
        String key = currencyId.toLowerCase(Locale.ROOT);

        return lockManager.executeWithAccountLock(uuid, () -> {
            double current = getBalanceBlocking(uuid, key);
            if (current < amount) {
                return false;
            }
            double newBalance = current - amount;
            balanceCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(key, newBalance);
            plugin.getRepository().saveBalance(uuid, key, newBalance);
            // M-6: audit trail (best-effort async; failure is logged, never breaks the mutation)
            plugin.getRepository().logTransaction(uuid, null, key, amount, "REMOVE",
                    "removed " + amount + ", new balance " + newBalance);
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
            double senderBal = getBalanceBlocking(senderUuid, key);
            if (senderBal < amount) {
                return false;
            }

            double receiverBal = getBalanceBlocking(receiverUuid, key);

            double newSenderBal = senderBal - amount;
            double newReceiverBal = receiverBal + amount;

            balanceCache.computeIfAbsent(senderUuid, k -> new ConcurrentHashMap<>()).put(key, newSenderBal);
            balanceCache.computeIfAbsent(receiverUuid, k -> new ConcurrentHashMap<>()).put(key, newReceiverBal);

            plugin.getRepository().saveBalance(senderUuid, key, newSenderBal);
            plugin.getRepository().saveBalance(receiverUuid, key, newReceiverBal);
            // M-6: audit trail (best-effort async; failure is logged, never breaks the mutation)
            plugin.getRepository().logTransaction(senderUuid, receiverUuid, key, amount, "TRANSFER",
                    "transfer of " + amount);
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
