package com.apexsions.economy.service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Concurrency & Transaction Lock Manager.
 * Prevents race conditions, negative balances, item duplication exploits,
 * and deadlocks via strict Keyed ReentrantLocks and Ordered Multi-Account Locking.
 */
public class TransactionLockManager {

    private final ConcurrentHashMap<UUID, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> resourceLocks = new ConcurrentHashMap<>();

    public ReentrantLock getAccountLock(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return accountLocks.computeIfAbsent(uuid, k -> new ReentrantLock(true));
    }

    public ReentrantLock getResourceLock(String key) {
        Objects.requireNonNull(key, "Resource key cannot be null");
        return resourceLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }

    /**
     * Executes an action inside a single account lock.
     */
    public void executeWithAccountLock(UUID uuid, Runnable action) {
        ReentrantLock lock = getAccountLock(uuid);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes a supplier function inside a single account lock.
     */
    public <T> T executeWithAccountLock(UUID uuid, Supplier<T> supplier) {
        ReentrantLock lock = getAccountLock(uuid);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes an action inside two account locks simultaneously.
     * Uses deterministic UUID ordering (lock hierarchy) to mathematically prevent Deadlocks!
     */
    public void executeWithDualAccountLock(UUID first, UUID second, Runnable action) {
        Objects.requireNonNull(first, "First UUID cannot be null");
        Objects.requireNonNull(second, "Second UUID cannot be null");

        if (first.equals(second)) {
            executeWithAccountLock(first, action);
            return;
        }

        UUID primary = first.compareTo(second) < 0 ? first : second;
        UUID secondary = primary.equals(first) ? second : first;

        ReentrantLock lock1 = getAccountLock(primary);
        ReentrantLock lock2 = getAccountLock(secondary);

        lock1.lock();
        try {
            lock2.lock();
            try {
                action.run();
            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }

    /**
     * Executes a supplier function inside two account locks with Deadlock-prevention.
     */
    public <T> T executeWithDualAccountLock(UUID first, UUID second, Supplier<T> supplier) {
        Objects.requireNonNull(first, "First UUID cannot be null");
        Objects.requireNonNull(second, "Second UUID cannot be null");

        if (first.equals(second)) {
            return executeWithAccountLock(first, supplier);
        }

        UUID primary = first.compareTo(second) < 0 ? first : second;
        UUID secondary = primary.equals(first) ? second : first;

        ReentrantLock lock1 = getAccountLock(primary);
        ReentrantLock lock2 = getAccountLock(secondary);

        lock1.lock();
        try {
            lock2.lock();
            try {
                return supplier.get();
            } finally {
                lock2.unlock();
            }
        } finally {
            lock1.unlock();
        }
    }

    /**
     * Executes an action inside a named resource lock (e.g. auctionId, tradeSessionId).
     */
    public void executeWithResourceLock(String key, Runnable action) {
        ReentrantLock lock = getResourceLock(key);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes a supplier function inside a named resource lock.
     */
    public <T> T executeWithResourceLock(String key, Supplier<T> supplier) {
        ReentrantLock lock = getResourceLock(key);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
}
