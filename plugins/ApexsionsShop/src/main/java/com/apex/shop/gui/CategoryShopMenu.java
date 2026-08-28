package com.apex.shop.gui;

import com.apex.shop.ApexsionsShop;
import com.apex.shop.category.ShopCategory;
import com.apex.shop.category.ShopItem;
import com.apex.shop.dynamic.DynamicPriceCalculator.PriceResult;
import com.apex.shop.gui.core.ShopGui;
import com.apex.shop.gui.core.ShopGuiButton;
import com.apex.shop.gui.core.ShopItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CategoryShopMenu extends ShopGui {

    private static final int ITEMS_PER_PAGE = 28;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final ShopCategory category;
    private final int page;

    public CategoryShopMenu(ApexsionsShop plugin, Player player, ShopCategory category, ShopGui parent, int page) {
        super(plugin, player, plugin.getConfig().getString("gui.title-category", "<dark_gray>[ TOKO: %category% ]</dark_gray>")
                .replace("%category%", category.getDisplayName()), 54, parent);
        this.category = category;
        this.page = Math.max(1, page);
    }

    @Override
    public void initialize() {
        fillBorder();

        List<ShopItem> allItems = plugin.getItemRegistry().getItemsByCategory(category);
        int maxPages = Math.max(1, (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE));
        int validPage = Math.max(1, Math.min(maxPages, page));

        int startIndex = (validPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allItems.size());

        // Header Category Info (Slot 4)
        double balance = plugin.getEconomyHook().getBalance(player);
        setButton(4, new ShopGuiButton(new ShopItemBuilder(category.getIcon())
                .name("<gold><bold>" + category.getDisplayName() + "</bold></gold>")
                .lore(List.of(
                        category.getDescription(),
                        "<gray>Saldo Kamu: <yellow>" + plugin.getEconomyHook().format(balance) + "</yellow></gray>",
                        "<gray>Pajak Aktif: <red>" + String.format("%.1f", plugin.getTaxService().getTaxPercent(player)) + "%</red></gray>"
                ))
                .build()));

        // Populate Items in Grid
        int slotIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (slotIdx >= SLOTS.length) break;
            ShopItem item = allItems.get(i);
            int currentSlot = SLOTS[slotIdx++];

            PriceResult buy1 = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 1);
            PriceResult buy64 = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, 64);
            PriceResult sell1 = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 1);
            PriceResult sell64 = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, 64);

            List<String> lore = new ArrayList<>();
            lore.add("<dark_gray>────────────────────────</dark_gray>");
            lore.add("<green>Beli 1x: <gold>" + plugin.getEconomyHook().format(buy1.finalTotalPrice()) + "</gold></green>");
            lore.add("<green>Beli 64x: <gold>" + plugin.getEconomyHook().format(buy64.finalTotalPrice()) + "</gold></green>");
            lore.add(" ");
            lore.add("<red>Jual 1x: <gold>" + plugin.getEconomyHook().format(sell1.finalTotalPrice()) + "</gold></red>");
            lore.add("<red>Jual 64x: <gold>" + plugin.getEconomyHook().format(sell64.finalTotalPrice()) + "</gold></red>");
            lore.add("<dark_gray>────────────────────────</dark_gray>");

            // Dynamic factors info
            if (buy1.weatherMultiplier() != 1.0 || buy1.kingdomMultiplier() != 1.0 || sell1.weatherMultiplier() != 1.0) {
                lore.add("<aqua>⚡ Pengaruh Pasar Dinamis Aktif</aqua>");
            }
            if (plugin.getConfig().getBoolean("tax.show-tax-in-lore", true)) {
                lore.add("<dark_gray>Termasuk Pajak Kerajaan (" + String.format("%.1f", buy1.taxPercent()) + "%)</dark_gray>");
            }
            lore.add(" ");
            lore.add("<yellow>● Klik Kiri:</yellow> <gray>Beli 1x</gray>");
            lore.add("<yellow>● Shift + Klik Kiri:</yellow> <gray>Beli 64x</gray>");
            lore.add("<yellow>● Klik Kanan:</yellow> <gray>Jual 1x</gray>");
            lore.add("<yellow>● Shift + Klik Kanan:</yellow> <gray>Jual 64x</gray>");
            lore.add("<yellow>● Klik Tengah / Drop:</yellow> <gray>Pilih Jumlah</gray>");

            ItemStack displayItem = new ShopItemBuilder(item.getMaterial())
                    .name(item.getDisplayName())
                    .lore(lore)
                    .hideAttributes()
                    .build();

            setButton(currentSlot, new ShopGuiButton(displayItem, event -> {
                org.bukkit.event.inventory.ClickType clickType = event.getClick();
                if (clickType == org.bukkit.event.inventory.ClickType.MIDDLE || clickType == org.bukkit.event.inventory.ClickType.DROP || clickType == org.bukkit.event.inventory.ClickType.CONTROL_DROP) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                    new QuantitySelectMenu(plugin, player, item, this).open();
                    return;
                }

                if (event.isLeftClick()) {
                    int qty = event.isShiftClick() ? 64 : 1;
                    executeBuy(item, qty);
                } else if (event.isRightClick()) {
                    int qty = event.isShiftClick() ? 64 : 1;
                    executeSell(item, qty);
                }
            }));
        }

        // Navigation (Row 5)
        setButton(45, new ShopGuiButton(new ShopItemBuilder(Material.ARROW)
                .name("<yellow>◀ KEMBALI KE MENU UTAMA</yellow>")
                .build(), event -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            if (parent != null) parent.open();
            else new ShopMainMenu(plugin, player).open();
        }));

        if (validPage > 1) {
            setButton(48, new ShopGuiButton(new ShopItemBuilder(Material.PAPER)
                    .name("<yellow>◀ Halaman " + (validPage - 1) + "</yellow>")
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, parent, validPage - 1).open();
            }));
        }

        setButton(49, new ShopGuiButton(new ShopItemBuilder(Material.MAP)
                .name("<gray>Halaman <yellow>" + validPage + "</yellow> / <white>" + maxPages + "</white></gray>")
                .build()));

        if (validPage < maxPages) {
            setButton(50, new ShopGuiButton(new ShopItemBuilder(Material.PAPER)
                    .name("<yellow>Halaman " + (validPage + 1) + " ▶</yellow>")
                    .build(), event -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                new CategoryShopMenu(plugin, player, category, parent, validPage + 1).open();
            }));
        }

        setButton(53, new ShopGuiButton(new ShopItemBuilder(Material.BARRIER)
                .name("<red><bold>TUTUP</bold></red>")
                .build(), event -> player.closeInventory()));
    }

    private void executeBuy(ShopItem item, int quantity) {
        PriceResult result = plugin.getDynamicPriceCalculator().calculateBuyPrice(item, player, quantity);
        double totalCost = result.finalTotalPrice();

        if (!plugin.getEconomyHook().has(player, totalCost)) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.not-enough-money", "<red>Saldo tidak cukup!</red>")
                            .replace("%required%", plugin.getEconomyHook().format(totalCost))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check Inventory Space
        ItemStack toAdd = new ItemStack(item.getMaterial(), quantity);
        if (!hasEnoughSpace(player, toAdd)) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.inventory-full", "<red>Inventori penuh!</red>")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (plugin.getEconomyHook().withdraw(player, totalCost)) {
            player.getInventory().addItem(toAdd);
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.buy-success", "<green>Beli berhasil!</green>")
                            .replace("%amount%", String.valueOf(quantity))
                            .replace("%item%", item.getDisplayName())
                            .replace("%price%", plugin.getEconomyHook().format(totalCost))
                            .replace("%tax%", plugin.getEconomyHook().format(result.taxAmount()))));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.3f);
            // Refresh Menu
            open();
        }
    }

    private void executeSell(ShopItem item, int quantity) {
        int playerHas = countItems(player, item.getMaterial());
        if (playerHas <= 0) {
            player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                    plugin.getConfig().getString("messages.not-enough-items", "<red>Kamu tidak memiliki item!</red>")
                            .replace("%item%", item.getDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int actualQuantity = Math.min(quantity, playerHas);
        PriceResult result = plugin.getDynamicPriceCalculator().calculateSellPrice(item, player, actualQuantity);
        double payout = result.finalTotalPrice();

        removeItems(player, item.getMaterial(), actualQuantity);
        plugin.getEconomyHook().deposit(player, payout);

        player.sendMessage(MM.deserialize(plugin.getConfig().getString("messages.prefix", "") +
                plugin.getConfig().getString("messages.sell-success", "<green>Jual berhasil!</green>")
                        .replace("%amount%", String.valueOf(actualQuantity))
                        .replace("%item%", item.getDisplayName())
                        .replace("%price%", plugin.getEconomyHook().format(payout))
                        .replace("%tax%", plugin.getEconomyHook().format(result.taxAmount()))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.8f);
        // Refresh Menu
        open();
    }

    private boolean hasEnoughSpace(Player player, ItemStack item) {
        int free = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is == null || is.getType() == Material.AIR) {
                free += item.getMaxStackSize();
            } else if (is.isSimilar(item)) {
                free += Math.max(0, is.getMaxStackSize() - is.getAmount());
            }
        }
        return free >= item.getAmount();
    }

    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is != null && is.getType() == material) {
                count += is.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == material) {
                if (is.getAmount() <= remaining) {
                    remaining -= is.getAmount();
                    contents[i] = null;
                } else {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                    break;
                }
            }
        }
        player.getInventory().setStorageContents(contents);
    }
}
