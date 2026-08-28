package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopItem;
import com.apex.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
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
        for (int i = 0; i < 36; i++) {
            SELL_SLOTS.add(i);
        }
    }

    private boolean sold = false;

    public SellGuiMenu(ApexsionsShop plugin, Player player, ShopGui parent) {
        super(plugin, player, plugin.getConfig().getString("gui.title-sellgui", "<dark_gray>[ JUAL CEPAT (SELL GUI) ]</dark_gray>"), 54, parent);
    }

    public SellGuiMenu(ApexsionsShop plugin, Player player) {
        this(plugin, player, null);
    }

    @Override
    public void initialize() {
        // Row 4: Divider (slots 36..44)
        for (int i = 36; i < 45; i++) {
            setButton(i, new ShopGuiButton(new ShopItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("<dark_gray>▲ Taruh item di atas untuk dijual ▲</dark_gray>")
                    .build(), null));
        }

        // Row 5: Control Buttons (slots 45..53)
        setButton(45, new ShopGuiButton(new ShopItemBuilder(Material.ARROW)
                .name("<yellow>◀ BATAL & KEMBALI</yellow>")
                .build(), event -> {
            player.closeInventory();
        }));

        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.SUNFLOWER)
                .name("<gold><bold>HITUNG TOTAL NILAI JUAL</bold></gold>")
                .lore(List.of(
                        "<gray>Klik untuk merefresh kalkulasi total nilai.</gray>",
                        "<dark_aqua>Seluruh item yang valid akan dihitung nilainya.</dark_aqua>"
                ))
                .build(), event -> {
            updateInfoButton();
        }));

        setButton(53, new ShopGuiButton(new ShopItemBuilder(Material.EMERALD_BLOCK)
                .name("<green><bold>✔ KONFIRMASI JUAL SEMUA</bold></green>")
                .lore(List.of(
                        "<gray>Klik untuk menyelesaikan penjualan seluruh item.</gray>",
                        "<yellow>Uang akan langsung masuk ke saldo Rupiah!</yellow>"
                ))
                .build(), event -> {
            processSell();
        }));
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // If clicked inside GUI
        if (event.getClickedInventory() == event.getInventory()) {
            if (SELL_SLOTS.contains(rawSlot)) {
                // Allow placing/taking items freely inside the sell slots
                plugin.getServer().getScheduler().runTask(plugin, this::updateInfoButton);
                return;
            }
            // Control button clicked
            event.setCancelled(true);
            handleClick(event);
        } else {
            // Clicked inside player inventory (allow shift-click into sell slots)
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
            lore.add("<red>Item Tidak Valid: " + invalidItemCount + " (akan dikembalikan)</red>");
        }
        lore.add("<dark_gray>────────────────────────</dark_gray>");
        lore.add("<green>Total Diterima: <gold><bold>" + plugin.getEconomyHook().format(totalPayout) + "</bold></gold></green>");
        lore.add("<dark_gray>Termasuk Potongan Pajak: " + plugin.getEconomyHook().format(totalTax) + "</dark_gray>");
        lore.add(" ");
        lore.add("<yellow>Klik tombol hijau di kanan untuk konfirmasi ▶</yellow>");

        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.SUNFLOWER)
                .name("<gold><bold>TOTAL ESTIMASI NILAI JUAL</bold></gold>")
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

        // Return invalid items
        for (ItemStack invalid : invalidItems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(invalid);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
        }

        if (totalItemsSold > 0) {
            plugin.getEconomyHook().deposit(player, totalPayout);
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.sell-success", "<green>Berhasil menjual item!</green>")
                            .replace("%amount%", String.valueOf(totalItemsSold))
                            .replace("%item%", "Item")
                            .replace("%price%", plugin.getEconomyHook().format(totalPayout))
                            .replace("%tax%", plugin.getEconomyHook().format(totalTax))));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        } else {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.cannot-sell-item", "<red>Tidak ada item valid untuk dijual!</red>")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

        player.closeInventory();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!sold) {
            // Return all items in sell slots to player
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
