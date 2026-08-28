package com.apexsions.shop.gui;

import com.apexsions.shop.ApexsionsShop;
import com.apexsions.shop.category.ShopItem;
import com.apexsions.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apexsions.shop.gui.core.ShopGui;
import com.apexsions.shop.gui.core.ShopGuiButton;
import com.apexsions.shop.gui.core.ShopItemBuilder;
import com.apexsions.shop.gui.navigation.BackButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SellGuiMenu extends ShopGui {

    private static final Set<Integer> SELL_SLOTS = new HashSet<>();
    static {
        // Slots 0 to 44 (Rows 0, 1, 2, 3, 4) are genuine empty input slots for player items
        for (int i = 0; i < 45; i++) {
            SELL_SLOTS.add(i);
        }
    }

    private boolean sold = false;

    public SellGuiMenu(ApexsionsShop plugin, Player player, ShopGui parent) {
        super(plugin, player, plugin.getConfigManager().getGuiConfig().getString("titles.sell-gui", "<dark_gray>[ JUAL CEPAT (SELL GUI) ]</dark_gray>"), 54, parent);
    }

    public SellGuiMenu(ApexsionsShop plugin, Player player) {
        this(plugin, player, null);
    }

    @Override
    public void open() {
        buttons.clear();
        inventory.clear();
        initialize();
        // Do NOT auto-fill input slots (0..44) with filler panes! Keep them open for item input
        for (Map.Entry<Integer, ShopGuiButton> entry : buttons.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < size && entry.getValue() != null) {
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }
        player.openInventory(inventory);
    }

    @Override
    public void initialize() {
        // Row 5: Bottom Control & Divider Row (slots 45..53)
        Material dividerMat = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack divider = new ShopItemBuilder(dividerMat).name("<dark_gray> </dark_gray>").build();

        for (int i = 45; i < 54; i++) {
            setButton(i, new ShopGuiButton(divider, null));
        }

        // Slot 45: Back / Cancel Button
        setButton(45, new BackButton(this, parent));

        // Slot 49: Live Price Estimation & Instructions
        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.SUNFLOWER)
                .name("<gold><bold>HITUNG TOTAL NILAI JUAL</bold></gold>")
                .lore(List.of(
                        "<gray>Taruh item yang ingin dijual pada slot kosong di atas.</gray>",
                        "<gray>Klik tombol ini untuk melihat estimasi hasil penjualan.</gray>"
                ))
                .build(), event -> updateInfoButton()));

        // Slot 53: Confirm Sell Button
        setButton(53, new ShopGuiButton(new ShopItemBuilder(Material.EMERALD_BLOCK)
                .name("<green><bold>✔ KONFIRMASI JUAL SEMUA</bold></green>")
                .lore(List.of(
                        "<gray>Klik untuk menjual seluruh item yang ada di atas.</gray>",
                        "<yellow>Hasil penjualan langsung masuk ke saldo Rupiah!</yellow>"
                ))
                .build(), event -> processSell()));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // If clicked inside the top inventory (Sell GUI)
        if (event.getClickedInventory() == event.getInventory()) {
            if (SELL_SLOTS.contains(rawSlot)) {
                // Allow placing and taking items freely inside sell slots
                plugin.getServer().getScheduler().runTask(plugin, this::updateInfoButton);
                return;
            }
            // Control button clicked on bottom row (45..53)
            event.setCancelled(true);
            handleClick(event);
        } else {
            // Clicked inside bottom inventory (Player inventory) -> Allow moving items to sell slots
            plugin.getServer().getScheduler().runTask(plugin, this::updateInfoButton);
        }
    }

    private void updateInfoButton() {
        double totalPayout = 0.0;
        double totalTax = 0.0;
        int validItemCount = 0;
        int invalidItemCount = 0;

        for (int slot : SELL_SLOTS) {
            ItemStack is = inventory.getItem(slot);
            if (is == null || is.getType() == Material.AIR) continue;

            ShopItem shopItem = plugin.getItemRegistry().getItem(is.getType());
            if (shopItem != null) {
                PriceResult res = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, is.getAmount());
                totalPayout += res.finalTotalPrice();
                totalTax += res.taxAmount();
                validItemCount += is.getAmount();
            } else {
                invalidItemCount += is.getAmount();
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Item Siap Jual: <yellow>" + validItemCount + " butir</yellow></gray>");
        if (invalidItemCount > 0) {
            lore.add("<red>Item Tidak Terdaftar: " + invalidItemCount + " (akan dikembalikan)</red>");
        }
        lore.add("<dark_gray>────────────────────────</dark_gray>");
        lore.add("<green>Total Diterima: <gold><bold>" + plugin.getEconomyHook().format(totalPayout) + "</bold></gold></green>");
        lore.add("<dark_gray>Potongan Pajak Kerajaan: " + plugin.getEconomyHook().format(totalTax) + "</dark_gray>");
        lore.add(" ");
        lore.add("<yellow>Sentuh / Klik tombol hijau di kanan untuk konfirmasi ▶</yellow>");

        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.SUNFLOWER)
                .name("<gold><bold>ESTIMASI NILAI JUAL: " + plugin.getEconomyHook().format(totalPayout) + "</bold></gold>")
                .lore(lore)
                .glow()
                .build(), event -> updateInfoButton()));
    }

    private void processSell() {
        double totalPayout = 0.0;
        double totalTax = 0.0;
        int totalItemsSold = 0;
        List<ItemStack> invalidItems = new ArrayList<>();

        for (int slot : SELL_SLOTS) {
            ItemStack is = inventory.getItem(slot);
            if (is == null || is.getType() == Material.AIR) continue;

            ShopItem shopItem = plugin.getItemRegistry().getItem(is.getType());
            if (shopItem != null) {
                PriceResult res = plugin.getDynamicPriceCalculator().calculateSellPrice(shopItem, player, is.getAmount());
                totalPayout += res.finalTotalPrice();
                totalTax += res.taxAmount();
                totalItemsSold += is.getAmount();
                inventory.setItem(slot, null);
            } else {
                invalidItems.add(is);
                inventory.setItem(slot, null);
            }
        }

        sold = true;

        // Return non-sellable items
        for (ItemStack invalid : invalidItems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(invalid);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
        }

        if (totalItemsSold > 0) {
            plugin.getEconomyHook().deposit(player, totalPayout);
            player.sendMessage(MM.deserialize(plugin.getConfigManager().getMessage("sell-success", "<green>Berhasil menjual item!</green>")
                    .replace("%amount%", String.valueOf(totalItemsSold))
                    .replace("%item%", "Item")
                    .replace("%price%", plugin.getEconomyHook().format(totalPayout))
                    .replace("%tax%", plugin.getEconomyHook().format(totalTax))));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        } else {
            player.sendMessage(MM.deserialize(plugin.getConfigManager().getMessage("cannot-sell-item", "<red>Tidak ada item valid untuk dijual!</red>")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

        player.closeInventory();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!sold) {
            // Return all items in sell slots to player safely
            for (int slot : SELL_SLOTS) {
                ItemStack is = inventory.getItem(slot);
                if (is != null && is.getType() != Material.AIR) {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(is);
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItem(player.getLocation(), drop);
                    }
                    inventory.setItem(slot, null);
                }
            }
        }
    }
}
