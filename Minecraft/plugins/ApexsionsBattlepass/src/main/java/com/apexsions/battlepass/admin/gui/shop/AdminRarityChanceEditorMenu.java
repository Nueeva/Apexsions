package com.apexsions.battlepass.admin.gui.shop;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.gui.core.Gui;
import com.apexsions.battlepass.gui.core.GuiButton;
import com.apexsions.battlepass.gui.navigation.BackButton;
import com.apexsions.battlepass.gui.navigation.CloseButton;
import com.apexsions.battlepass.gui.util.ItemBuilder;
import com.apexsions.battlepass.shop.ItemRarity;
import com.apexsions.battlepass.shop.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AdminRarityChanceEditorMenu extends Gui {

    private final ShopCategory category;
    private final Map<ItemRarity, Double> chances = new EnumMap<>(ItemRarity.class);

    public AdminRarityChanceEditorMenu(ApexsionsBattlepass plugin, Player player, ShopCategory category, Gui parent) {
        super(plugin, player, "&8[ &4&lRARITY CHANCES: &e" + category.name() + " &8]", 45, parent);
        this.category = category;
        this.chances.putAll(plugin.getRarityChanceService().getChances(category));
    }

    @Override
    public void initialize() {
        fillBackground();

        // 1. Category Switcher Tabs (Slots 1, 2, 3)
        setButton(1, new GuiButton(new ItemBuilder(Material.EMERALD)
                .name(category == ShopCategory.DAILY ? "&a&l[✔] DAILY SHOP" : "&a&lDAILY SHOP")
                .lore(List.of("&7Atur peluang rarity Daily Shop.", " ", "&eKlik untuk memilih >"))
                .build(), event -> {
            new AdminRarityChanceEditorMenu(plugin, player, ShopCategory.DAILY, parent).open();
        }));

        setButton(2, new GuiButton(new ItemBuilder(Material.GOLD_INGOT)
                .name(category == ShopCategory.WEEKLY ? "&e&l[✔] WEEKLY SHOP" : "&e&lWEEKLY SHOP")
                .lore(List.of("&7Atur peluang rarity Weekly Shop.", " ", "&eKlik untuk memilih >"))
                .build(), event -> {
            new AdminRarityChanceEditorMenu(plugin, player, ShopCategory.WEEKLY, parent).open();
        }));

        setButton(3, new GuiButton(new ItemBuilder(Material.DIAMOND)
                .name(category == ShopCategory.MONTHLY ? "&d&l[✔] MONTHLY SHOP" : "&d&lMONTHLY SHOP")
                .lore(List.of("&7Atur peluang rarity Monthly Shop.", " ", "&eKlik untuk memilih >"))
                .build(), event -> {
            new AdminRarityChanceEditorMenu(plugin, player, ShopCategory.MONTHLY, parent).open();
        }));

        // 2. Overview & Validation Status Card (Slot 5)
        double sum = chances.values().stream().mapToDouble(Double::doubleValue).sum();
        boolean isValid = Math.abs(sum - 100.0) < 0.1;

        setButton(5, new GuiButton(new ItemBuilder(isValid ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .name("&e&lSTATUS VALIDASI TOTAL: " + (isValid ? "&a" + String.format("%.1f%%", sum) + " [VALID]" : "&c" + String.format("%.1f%%", sum) + " [TIDAK VALID]"))
                .lore(List.of(
                        "&7Kategori: &f" + category.name(),
                        "&7Total Persentase: " + (isValid ? "&a" : "&c") + String.format("%.1f%%", sum),
                        " ",
                        isValid ? "&a✔ Total sudah tepat 100% dan siap disimpan." : "&c✖ Total seluruh peluang WAJIB 100.0%!"
                ))
                .build()));

        // 3. Render 6 Rarities (Slots 19, 20, 21, 22, 23, 24)
        ItemRarity[] rarities = ItemRarity.values();
        int[] slots = { 19, 20, 21, 22, 23, 24 };

        Material[] icons = {
                Material.GRAY_DYE,
                Material.LIME_DYE,
                Material.LIGHT_BLUE_DYE,
                Material.PURPLE_DYE,
                Material.GOLD_INGOT,
                Material.NETHER_STAR
        };

        for (int i = 0; i < rarities.length && i < slots.length; i++) {
            ItemRarity r = rarities[i];
            double pct = chances.getOrDefault(r, 0.0);

            setButton(slots[i], new GuiButton(new ItemBuilder(icons[i])
                    .name(r.getColor() + "&l" + r.getDisplayName() + " &f- &a" + String.format("%.1f%%", pct))
                    .lore(List.of(
                            "&7Peluang Saat Ini: &e" + String.format("%.1f%%", pct),
                            " ",
                            "&eKlik untuk mengubah persentase via chat >"
                    ))
                    .build(), event -> {
                plugin.getChatInputManager().startDoubleInput(player, "Masukkan persentase (%) peluang untuk " + r.getDisplayName() + " (0 - 100):", newPct -> {
                    chances.put(r, newPct);
                    open();
                }, this::open, 0, 100);
            }));
        }

        // 4. Save Button (Slot 31)
        setButton(31, new GuiButton(new ItemBuilder(isValid ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE)
                .name(isValid ? "&a&l[✔] SIMPAN PERUBAHAN CHANCE" : "&c&l[✖] TOTAL HARUS 100.0%")
                .lore(List.of(
                        "&7Simpan konfigurasi peluang kelangkaan ini.",
                        " ",
                        isValid ? "&aKlik untuk simpan ke konfigurasi >" : "&cPerbaiki persentase hingga total 100.0%!"
                ))
                .build(), event -> {
            if (!isValid) {
                player.sendMessage("§c[!] Gagal menyimpan: Total seluruh persentase harus tepat 100.0% (Saat ini: " + String.format("%.1f%%", sum) + ")!");
                return;
            }
            for (Map.Entry<ItemRarity, Double> e : chances.entrySet()) {
                plugin.getRarityChanceService().setChance(category, e.getKey(), e.getValue());
            }
            plugin.getRarityChanceService().saveChances();
            player.sendMessage("§aBerhasil menyimpan konfigurasi rarity chances untuk §e" + category.name() + "§a!");
            if (parent != null) parent.open();
        }));

        // Navigation
        setButton(36, new BackButton(this, parent));
        setButton(44, new CloseButton());
    }
}
