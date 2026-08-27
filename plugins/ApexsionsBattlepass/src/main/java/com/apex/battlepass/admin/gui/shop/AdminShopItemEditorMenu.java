package com.apex.battlepass.admin.gui.shop;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.core.Gui;
import com.apex.battlepass.gui.core.GuiButton;
import com.apex.battlepass.gui.navigation.BackButton;
import com.apex.battlepass.gui.navigation.CloseButton;
import com.apex.battlepass.gui.util.ItemBuilder;
import com.apex.battlepass.shop.ItemRarity;
import com.apex.battlepass.shop.ShopCategory;
import com.apex.battlepass.shop.ShopItem;
import com.apex.battlepass.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AdminShopItemEditorMenu extends Gui {

    private final ShopCategory category;
    private final ShopItem originalItem;

    private String itemId;
    private String displayName;
    private Material material;
    private int amount;
    private String currencyType;
    private double price;
    private int purchaseLimit;
    private ItemRarity rarity;
    private String categoryTag;
    private String itemData;

    public AdminShopItemEditorMenu(ApexsionsBattlepass plugin, Player player, ShopCategory category, ShopItem originalItem, Gui parent) {
        super(plugin, player, "&8[ &4&lEDIT ITEM SHOP: &e" + category.name() + " &8]", 54, parent);
        this.category = category;
        this.originalItem = originalItem;

        if (originalItem != null) {
            this.itemId = originalItem.getId();
            this.displayName = originalItem.getDisplayName();
            this.material = originalItem.getMaterial();
            this.amount = originalItem.getAmount();
            this.currencyType = originalItem.getCurrencyType();
            this.price = originalItem.getPrice();
            this.purchaseLimit = originalItem.getPurchaseLimit();
            this.rarity = originalItem.getRarity();
            this.categoryTag = originalItem.getCategoryTag();
            this.itemData = originalItem.getItemData();
        } else {
            this.itemId = "item_" + System.currentTimeMillis() % 100000;
            this.displayName = "Item Baru";
            this.material = Material.DIAMOND;
            this.amount = 1;
            this.currencyType = "BATTLE_COINS";
            this.price = 100.0;
            this.purchaseLimit = 0;
            this.rarity = ItemRarity.COMMON;
            this.categoryTag = "General";
            this.itemData = null;
        }
    }

    @Override
    public void initialize() {
        fillBorder();

        ItemStack previewStack = (itemData != null && !itemData.isBlank()) ? ItemSerializer.fromBase64(itemData) : new ItemStack(material, amount);
        boolean isStackable = previewStack != null && previewStack.getMaxStackSize() > 1;

        String priceDisplay;
        if ("RUPIAH".equalsIgnoreCase(currencyType)) {
            priceDisplay = "Rp." + String.format("%,.0f", price);
        } else {
            priceDisplay = String.format("%,.0f", price) + " Battle Coins";
        }

        // 1. Overview Banner (Slot 4)
        setButton(4, new GuiButton(new ItemBuilder(previewStack != null ? previewStack : new ItemStack(Material.CHEST))
                .name(rarity.getColor() + "&l" + displayName)
                .lore(List.of(
                        "&7ID: &8" + itemId,
                        "&7Kategori: &f" + category.name(),
                        "&7Rarity: " + rarity.getColor() + rarity.getDisplayName(),
                        "&7Jumlah: &a" + amount + "x &7(" + (isStackable ? "&aStackable" : "&cNon-stackable") + "&7)",
                        "&7Mata Uang: &e" + ("RUPIAH".equalsIgnoreCase(currencyType) ? "Rupiah (Rp.)" : "Battle Coins"),
                        "&7Harga: &e" + priceDisplay,
                        "&7Batas Beli: &f" + (purchaseLimit > 0 ? purchaseLimit + "x" : "Tidak Terbatas"),
                        " ",
                        "&7Klik tombol di bawah untuk mengubah properti item."
                ))
                .build()));

        // 2. Stackable & Amount Setting (Slot 19)
        if (isStackable) {
            setButton(19, new GuiButton(new ItemBuilder(Material.ANVIL)
                    .name("&e&l[🔢] UBAH JUMLAH ITEM (Saat ini: x" + amount + ")")
                    .lore(List.of(
                            "&7Item ini &adapat di-stack&7.",
                            "&7Maksimum stack: &f" + previewStack.getMaxStackSize(),
                            " ",
                            "&eKlik untuk mengubah jumlah via chat >"
                    ))
                    .build(), event -> {
                plugin.getChatInputManager().startNumericInput(player, "Masukkan jumlah item baru (1 - " + previewStack.getMaxStackSize() + "):", newAmount -> {
                    this.amount = newAmount;
                    if (this.itemData != null && !this.itemData.isBlank()) {
                        ItemStack is = ItemSerializer.fromBase64(this.itemData);
                        if (is != null) {
                            is.setAmount(newAmount);
                            this.itemData = ItemSerializer.toBase64(is);
                        }
                    }
                    open();
                }, this::open, 1, previewStack.getMaxStackSize());
            }));
        } else {
            setButton(19, new GuiButton(new ItemBuilder(Material.BARRIER)
                    .name("&c&l[🔒] JUMLAH TERKUNCI (x1)")
                    .lore(List.of(
                            "&7Item ini &ctidak dapat di-stack&7",
                            "&7(seperti senjata, armor, alat, totem, dll).",
                            "&7Jumlah otomatis terkunci pada &f1x&7."
                    ))
                    .build()));
        }

        // 3. Rarity Selector (100% GUI Button - Slot 21)
        setButton(21, new GuiButton(new ItemBuilder(Material.AMETHYST_SHARD)
                .name("&d&l[✨] PILIH RARITY: " + rarity.getColor() + rarity.getDisplayName())
                .lore(List.of(
                        "&7Tingkat kelangkaan item di shop:",
                        "&8- &fCOMMON",
                        "&8- &aUNCOMMON",
                        "&8- &9RARE",
                        "&8- &5EPIC",
                        "&8- &6LEGENDARY",
                        "&8- &dMYTHIC",
                        " ",
                        "&dKlik untuk beralih ke Rarity berikutnya >"
                ))
                .build(), event -> {
            ItemRarity[] rarities = ItemRarity.values();
            int next = (rarity.ordinal() + 1) % rarities.length;
            this.rarity = rarities[next];
            open();
        }));

        // 4. Currency Selector (100% GUI Button - Slot 23)
        Material currIcon = "RUPIAH".equalsIgnoreCase(currencyType) ? Material.EMERALD : Material.SUNFLOWER;
        setButton(23, new GuiButton(new ItemBuilder(currIcon)
                .name("&a&l[🪙] PILIH MATA UANG: &e" + ("RUPIAH".equalsIgnoreCase(currencyType) ? "RUPIAH (Rp.)" : "BATTLE_COINS"))
                .lore(List.of(
                        "&7Pilihan mata uang pembelian:",
                        "&8- &aRUPIAH (Rp.)",
                        "&8- &eBATTLE_COINS",
                        " ",
                        "&aKlik untuk beralih antara Rupiah dan Battle Coins >"
                ))
                .build(), event -> {
            String[] currencies = { "RUPIAH", "BATTLE_COINS" };
            int next = 0;
            for (int i = 0; i < currencies.length; i++) {
                if (currencies[i].equalsIgnoreCase(currencyType)) {
                    next = (i + 1) % currencies.length;
                    break;
                }
            }
            this.currencyType = currencies[next];
            open();
        }));

        // 5. Price Setting (Slot 25)
        setButton(25, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name("&e&l[💰] ATUR HARGA: &f" + priceDisplay)
                .lore(List.of(
                        "&7Harga pembelian item di katalog shop.",
                        " ",
                        "&eKlik untuk memasukkan harga via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startDoubleInput(player, "Masukkan nominal harga baru:", newPrice -> {
                this.price = newPrice;
                open();
            }, this::open, 0, 1000000000);
        }));

        // 6. Purchase Limit (Slot 29)
        setButton(29, new GuiButton(new ItemBuilder(Material.REDSTONE)
                .name("&c&l[🔒] BATAS BELI: &f" + (purchaseLimit > 0 ? purchaseLimit + "x" : "Tidak Terbatas"))
                .lore(List.of(
                        "&7Batas pembelian per pemain (0 = tidak terbatas).",
                        " ",
                        "&eKlik untuk mengubah via chat >"
                ))
                .build(), event -> {
            plugin.getChatInputManager().startNumericInput(player, "Masukkan batas pembelian (0 untuk unlimited):", limit -> {
                this.purchaseLimit = limit;
                open();
            }, this::open, 0, 1000);
        }));

        // 7. Delete Item from Shop (Slot 33)
        if (originalItem != null) {
            setButton(33, new GuiButton(new ItemBuilder(Material.TNT)
                    .name("&4&l[✖] HAPUS DARI SHOP")
                    .lore(List.of(
                            "&7Hapus item ini dari katalog " + category.name() + ".",
                            " ",
                            "&cKlik untuk menghapus >"
                    ))
                    .build(), event -> {
                plugin.getShopManager().deleteShopItem(category, itemId);
                player.sendMessage("§cItem berhasil dihapus dari katalog " + category.name() + "!");
                if (parent != null) parent.open();
            }));
        }

        // 8. Bottom Navigation Controls (Row 5)
        setButton(45, new BackButton(this, parent));

        setButton(49, new GuiButton(new ItemBuilder(Material.LIME_CONCRETE)
                .name("&a&l[✔] SIMPAN PERUBAHAN ITEM")
                .lore(List.of(
                        "&7Simpan item ini ke katalog &e" + category.name() + "&7.",
                        " ",
                        "&aKlik untuk menyimpan >"
                ))
                .build(), event -> {
            ShopItem it = new ShopItem(itemId, displayName, material, amount, currencyType, price, purchaseLimit, List.of(), List.of(), rarity, categoryTag, 10, itemData);
            plugin.getShopManager().addOrUpdateShopItem(category, it);
            player.sendMessage("§aBerhasil menyimpan item §e" + displayName + " §ake katalog §e" + category.name() + "§a!");
            if (parent != null) parent.open();
        }));

        setButton(53, new CloseButton());
    }
}
