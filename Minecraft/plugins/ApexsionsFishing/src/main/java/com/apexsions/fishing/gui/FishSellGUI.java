package com.apexsions.fishing.gui;

import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.fishing.ApexsionsFishing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Fish Selling GUI.
 * Strictly accepts ONLY fish items (CatchType.FISH).
 * Rejects junk, treasure, and non-fishing items.
 * Computes dynamic payouts based on weight and rarity.
 */
public class FishSellGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public static final int DROP_SLOTS_COUNT = 45; // Slots 0 to 44
    public static final int SLOT_BACK = 45;
    public static final int SLOT_QUICK_SELL = 47;
    public static final int SLOT_APPRAISAL = 49;
    public static final int SLOT_CONFIRM_SELL = 51;
    public static final int SLOT_CANCEL = 53;

    public FishSellGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gold><bold>Pelelangan & Penjualan Ikan Segar</bold></gold>"));
        renderBottomBar();
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void renderBottomBar() {
        // Back Button
        inventory.setItem(SLOT_BACK, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali ke Brankas</bold></yellow>",
                List.of("<gray>Klik untuk kembali ke penyimpanan.</gray>")));

        // Quick Sell All Fish from Player Inventory
        inventory.setItem(SLOT_QUICK_SELL, createGuiItem(Material.GOLD_BLOCK,
                "<gradient:#ffd700:#ffa500><bold>⚡ JUAL CEPAT SEMUA IKAN</bold></gradient>",
                List.of(
                        "<gray>Otomatis mencari dan menjual seluruh</gray>",
                        "<gray>ikan segar yang ada di inventory kamu.</gray>",
                        "",
                        "<yellow>▶ Klik untuk jual instan dari inventory!</yellow>"
                )));

        // Appraisal Status
        updateAppraisal();

        // Confirm Sell Box
        inventory.setItem(SLOT_CONFIRM_SELL, createGuiItem(Material.LIME_CONCRETE,
                "<green><bold>✔ KONFIRMASI JUAL IKAN DI KOTAK</bold></green>",
                List.of(
                        "<gray>Jual semua ikan yang kamu letakkan</gray>",
                        "<gray>pada kotak di atas dan terima Rupiah.</gray>",
                        "",
                        "<green>▶ Klik untuk konfirmasi penjualan!</green>"
                )));

        // Cancel / Return All
        inventory.setItem(SLOT_CANCEL, createGuiItem(Material.RED_CONCRETE,
                "<red><bold>✖ KEMBALIKAN SEMUA IKAN</bold></red>",
                List.of("<gray>Kembalikan ikan di kotak ke inventory.</gray>")));

        // Fill decorative slots in row 6 (slots 46, 48, 50, 52)
        ItemStack grayGlass = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        int[] decors = {46, 48, 50, 52};
        for (int d : decors) {
            inventory.setItem(d, grayGlass);
        }
    }

    public void updateAppraisal() {
        int fishCount = 0;
        double totalWeight = 0.0;
        double totalPrice = 0.0;

        for (int i = 0; i < DROP_SLOTS_COUNT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir() && plugin.getLootGenerator().isFish(item)) {
                fishCount += item.getAmount();
                totalWeight += plugin.getLootGenerator().getFishWeight(item) * item.getAmount();
                totalPrice += plugin.getLootGenerator().getFishPrice(item);
            }
        }

        inventory.setItem(SLOT_APPRAISAL, createGuiItem(Material.EMERALD,
                "<gradient:#00ff87:#60efff><bold>⚖ PENAKSIRAN HARGA IKAN</bold></gradient>",
                List.of(
                        "<gray>Ikan di Kotak: <gold><bold>" + fishCount + " ekor</bold></gold></gray>",
                        "<gray>Total Bobot: <yellow><bold>" + String.format("%.2f", totalWeight) + " kg</bold></yellow></gray>",
                        "",
                        "<yellow>Total Pendapatan: <gold><bold>Rp " + String.format("%,d", (long) totalPrice) + "</bold></gold></yellow>"
                )));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // 1. Clicks on Bottom Action Bar (Slots 45-53)
        if (rawSlot >= DROP_SLOTS_COUNT && rawSlot < 54) {
            event.setCancelled(true);
            handleActionBarClick(rawSlot);
            return;
        }

        // 2. Placing item into Top Drop Slots (Slots 0-44)
        if (rawSlot < DROP_SLOTS_COUNT) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && !cursorItem.getType().isAir()) {
                if (!plugin.getLootGenerator().isFish(cursorItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Nelayan hanya menerima ikan segar! Sampah laut dan barang lain tidak dapat dijual di sini.</red>"));
                    return;
                }
            }

            // Check Number Key swap (1-9 hotbar buttons)
            if (event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                    if (hotbarItem != null && !hotbarItem.getType().isAir() && !plugin.getLootGenerator().isFish(hotbarItem)) {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        player.sendMessage(mm.deserialize("<red>⚠ Barang dari hotbar bukan ikan segar!</red>"));
                        return;
                    }
                }
            }

            // Check Double Click / Collect to cursor
            if (event.getClick() == org.bukkit.event.inventory.ClickType.DOUBLE_CLICK) {
                if (cursorItem != null && !cursorItem.getType().isAir() && !plugin.getLootGenerator().isFish(cursorItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // 3. Shift-Clicking from player inventory into sell box
        if (event.isShiftClick() && event.getView().getBottomInventory().equals(event.getClickedInventory())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                if (!plugin.getLootGenerator().isFish(clickedItem)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Barang ini bukan ikan segar dan tidak dapat dijual ke Nelayan!</red>"));
                    return;
                }
            }
        }

        // Update appraisal on next tick
        Bukkit.getScheduler().runTask(plugin, this::updateAppraisal);
    }

    public void handleDrag(@NotNull InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= DROP_SLOTS_COUNT && rawSlot < 54) {
                event.setCancelled(true);
                return;
            }
            if (rawSlot < DROP_SLOTS_COUNT) {
                ItemStack item = event.getOldCursor();
                if (!plugin.getLootGenerator().isFish(item)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Nelayan hanya menerima ikan segar!</red>"));
                    return;
                }
            }
        }
        Bukkit.getScheduler().runTask(plugin, this::updateAppraisal);
    }

    public void handleClose(@NotNull InventoryCloseEvent event) {
        returnRemainingItems();
    }

    private void handleActionBarClick(int slot) {
        switch (slot) {
            case SLOT_BACK -> {
                returnRemainingItems();
                new FishingVaultGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_QUICK_SELL -> {
                quickSellAllFromInventory();
            }
            case SLOT_CONFIRM_SELL -> {
                confirmSellBox();
            }
            case SLOT_CANCEL -> {
                returnRemainingItems();
                updateAppraisal();
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                player.sendMessage(mm.deserialize("<yellow>Semua ikan di kotak telah dikembalikan ke inventory Anda.</yellow>"));
            }
        }
    }

    private void confirmSellBox() {
        int count = 0;
        double totalRevenue = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < DROP_SLOTS_COUNT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir() && plugin.getLootGenerator().isFish(item)) {
                count += item.getAmount();
                totalWeight += plugin.getLootGenerator().getFishWeight(item) * item.getAmount();
                totalRevenue += plugin.getLootGenerator().getFishPrice(item);
                inventory.setItem(i, null);
            }
        }

        if (count == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Letakkan ikan hasil tangkapan di kotak atas terlebih dahulu untuk dijual.</yellow>"));
            return;
        }

        if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
            com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", totalRevenue);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>SUKSES TERJUAL!</bold> Anda menjual <gold>" + count + " ekor ikan</gold> (" + String.format("%.2f", totalWeight) + " kg) seharga <yellow><bold>Rp " + String.format("%,d", (long) totalRevenue) + "</bold></yellow>!</green>"));

        updateAppraisal();
    }

    private void quickSellAllFromInventory() {
        int count = 0;
        double totalRevenue = 0.0;
        double totalWeight = 0.0;

        Inventory pInv = player.getInventory();
        for (int i = 0; i < pInv.getSize(); i++) {
            ItemStack item = pInv.getItem(i);
            if (item != null && !item.getType().isAir() && plugin.getLootGenerator().isFish(item)) {
                count += item.getAmount();
                totalWeight += plugin.getLootGenerator().getFishWeight(item) * item.getAmount();
                totalRevenue += plugin.getLootGenerator().getFishPrice(item);
                pInv.setItem(i, null);
            }
        }

        if (count == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Tidak ditemukan ikan segar di inventory Anda untuk dijual.</yellow>"));
            return;
        }

        if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
            com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", totalRevenue);
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        player.sendMessage(mm.deserialize("<green><bold>JUAL CEPAT BERHASIL!</bold> Terjual <gold>" + count + " ekor ikan</gold> (" + String.format("%.2f", totalWeight) + " kg) dari inventory seharga <yellow><bold>Rp " + String.format("%,d", (long) totalRevenue) + "</bold></yellow>!</green>"));
    }

    private void returnRemainingItems() {
        for (int i = 0; i < DROP_SLOTS_COUNT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                inventory.setItem(i, null);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
    }

    private ItemStack createGuiItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(mm.deserialize(l));
            }
            meta.lore(compLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
