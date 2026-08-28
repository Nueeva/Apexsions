package com.apexsions.battlepass.admin.gui.shop;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.shop.ItemRarity;
import com.apexsions.battlepass.shop.ShopCategory;
import com.apexsions.battlepass.shop.ShopItem;
import com.apexsions.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminShopItemListMenu extends Gui {

    private final ShopCategory category;

    private static final int[] CENTER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public AdminShopItemListMenu(ApexsionsBattlepass plugin, Player player, ShopCategory category, Gui parent) {
        super(plugin, player, "&8[ &4&lSHOP: &e" + category.name() + " &8]", 54, parent);
        this.category = category;
    }

    @Override
    public void initialize() {
        fillBorder();

        Map<String, ShopItem> items = plugin.getShopManager().getShopItems(category);

        // 1. Top Header Banner (Row 0)
        setButton(0, new GuiButton(new ItemBuilder(Material.CHEST)
                .name("&6&lKATALOG SHOP: &e" + category.name())
                .lore(List.of(
                        "&7Kategori: &f" + category.name(),
                        "&7Total Item: &e" + items.size() + " barang"
                ))
                .build()));

        setButton(4, new GuiButton(new ItemBuilder(Material.HOPPER)
                .name("&a&l[💡] DRAG & DROP ITEM KE SINI")
                .lore(List.of(
                        "&7Area tengah kosong untuk memasukkan item yang dijual.",
                        "&7● &fDrag & Drop &7item dari inventory Anda ke slot kosong.",
                        "&7● Atau &fShift-Klik &7item di inventory Anda.",
                        "&7● Klik item yang ada untuk atur Rarity, Currency, Harga, dll."
                ))
                .build()));

        setButton(8, new GuiButton(new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&lTOTAL: &e" + items.size() + " Item")
                .lore(List.of(
                        "&7Item di kategori ini akan dirotasi",
                        "&7secara otomatis saat refresh shop."
                ))
                .build()));

        // 2. Render Existing Shop Items
        int idx = 0;
        for (ShopItem item : items.values()) {
            if (idx >= CENTER_SLOTS.length) break;

            ItemStack is = item.toItemStack();
            boolean isStackable = is != null && is.getMaxStackSize() > 1;

            List<String> lore = new ArrayList<>();
            lore.add("&7Rarity: " + item.getRarity().getColor() + item.getRarity().getDisplayName());
            String priceDisplay;
            if ("rupiah".equalsIgnoreCase(item.getCurrencyType())) {
                priceDisplay = "Rp." + String.format("%,.0f", item.getPrice());
            } else {
                priceDisplay = String.format("%,.0f", item.getPrice()) + " Battle Coins";
            }
            lore.add("&7Mata Uang: &a" + ("rupiah".equalsIgnoreCase(item.getCurrencyType()) ? "Rupiah (Rp.)" : "Battle Coins"));
            lore.add("&7Harga: &e" + priceDisplay);
            lore.add("&7Batas Beli: &f" + (item.getPurchaseLimit() > 0 ? item.getPurchaseLimit() + "x" : "Tidak Terbatas"));
            lore.add(" ");
            lore.add("&e&l[KLIK UNTUK EDIT RARITY / HARGA / CURRENCY]");

            ItemStack display = new ItemBuilder(is != null ? is : new ItemStack(Material.STONE))
                    .lore(lore)
                    .build();

            int slot = CENTER_SLOTS[idx++];
            setButton(slot, new GuiButton(display, event -> {
                new AdminShopItemEditorMenu(plugin, player, category, item, this).open();
            }));
        }

        // 3. Set remaining empty center slots to listen for cursor drops
        while (idx < CENTER_SLOTS.length) {
            int emptySlot = CENTER_SLOTS[idx++];
            setButton(emptySlot, new GuiButton(null, event -> {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemStack dropped = event.getCursor().clone();
                    handleNewItemDrop(dropped);
                }
            }));
        }

        // 4. Navigation (Row 5)
        setButton(45, new BackButton(this, parent));

        setButton(49, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name("&a&l[➕] TAMBAH ITEM BARU")
                .lore(List.of(
                        "&7Buka editor untuk membuat item shop baru.",
                        " ",
                        "&aKlik untuk menambah item >"
                ))
                .build(), event -> {
            new AdminShopItemEditorMenu(plugin, player, category, null, this).open();
        }));

        setButton(53, new CloseButton());
    }

    @Override
    public void handleBottomInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            ItemStack item = event.getCurrentItem().clone();
            handleNewItemDrop(item);
        }
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
        if (event.getOldCursor() != null && event.getOldCursor().getType() != Material.AIR) {
            ItemStack item = event.getOldCursor().clone();
            handleNewItemDrop(item);
        }
    }

    private void handleNewItemDrop(ItemStack item) {
        String id = "item_" + System.currentTimeMillis() % 100000;
        String name = ItemSerializer.getItemDisplayName(item);
        String data = ItemSerializer.toBase64(item);
        ShopItem shopItem = new ShopItem(id, name, item.getType(), item.getAmount(), "BATTLE_COINS", 100.0, 0, List.of(), List.of(), ItemRarity.COMMON, "General", 10, data);
        plugin.getShopManager().addOrUpdateShopItem(category, shopItem);
        player.sendMessage("§aBerhasil menambahkan item §e" + name + " §ake kategori §e" + category.name() + "§a! Membuka editor item...");
        new AdminShopItemEditorMenu(plugin, player, category, shopItem, this).open();
    }
}
