package com.apexsions.fishing.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerVaultData {

    public static final int SLOTS_PER_PAGE = 45;
    public static final int MAX_PAGES = 30;

    private final UUID uuid;
    private int unlockedPages;
    private final Map<Integer, ItemStack[]> pageContents = new HashMap<>();

    public PlayerVaultData(UUID uuid, int unlockedPages) {
        this.uuid = uuid;
        this.unlockedPages = Math.max(1, Math.min(MAX_PAGES, unlockedPages));
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getUnlockedPages() {
        return unlockedPages;
    }

    public void setUnlockedPages(int unlockedPages) {
        this.unlockedPages = Math.max(1, Math.min(MAX_PAGES, unlockedPages));
    }

    public boolean unlockNextPage() {
        if (unlockedPages < MAX_PAGES) {
            unlockedPages++;
            return true;
        }
        return false;
    }

    public boolean isPageUnlocked(int page) {
        return page >= 1 && page <= unlockedPages;
    }

    public @NotNull ItemStack[] getPage(int page) {
        if (!pageContents.containsKey(page)) {
            pageContents.put(page, new ItemStack[SLOTS_PER_PAGE]);
        }
        return pageContents.get(page);
    }

    public void setPage(int page, @Nullable ItemStack[] items) {
        if (items == null) {
            pageContents.put(page, new ItemStack[SLOTS_PER_PAGE]);
            return;
        }
        ItemStack[] copy = new ItemStack[SLOTS_PER_PAGE];
        for (int i = 0; i < Math.min(items.length, SLOTS_PER_PAGE); i++) {
            copy[i] = items[i] != null ? items[i].clone() : null;
        }
        pageContents.put(page, copy);
    }

    public Map<Integer, ItemStack[]> getAllPages() {
        return pageContents;
    }

    public int countTotalStoredItems() {
        int count = 0;
        for (ItemStack[] items : pageContents.values()) {
            if (items != null) {
                for (ItemStack is : items) {
                    if (is != null && !is.getType().isAir()) {
                        count += is.getAmount();
                    }
                }
            }
        }
        return count;
    }
}
