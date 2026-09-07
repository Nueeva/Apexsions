package com.apexsions.customenchants.gui.dialog;

import com.apexsions.core.kit.KitStatType;
import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.enchant.CustomEnchant;
import com.apexsions.customenchants.gui.AdminItemCreatorGUI;
import com.apexsions.customenchants.gui.input.NativeDialogAdapter;
import com.apexsions.customenchants.gui.input.NativeDialogAdapter.DialogButtonData;
import com.apexsions.customenchants.items.ColorUtil;
import com.apexsions.customenchants.tools.ToolStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * 100% Native Dialog GUI Orchestrator for all item editing operations.
 * Completely replaces all chest-inventory sub-GUIs with native Minecraft 1.21.4+ / 26.2
 * Dialogs (NightCore & Paper Dialog API) and Bedrock SimpleForms, featuring 3D item preview
 * at top-center and smooth, flicker-free interactive dialog transitions.
 */
public class ItemEditDialogFlow {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    // ==========================================================
    // 1. ROOT ITEM MODIFIER DIALOG
    // ==========================================================

    public static boolean openRoot(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot, AdminItemCreatorGUI creatorGUI) {
        if (item == null || player == null || !player.isOnline()) return false;
        if (creatorGUI != null) {
            creatorGUI.setNavigatingSubGUI(true);
        }
        if (player.getOpenInventory().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) {
            player.closeInventory();
        }

        Map<CustomEnchant, Integer> activeCE = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
        int activeVanilla = item.getEnchantments().size();
        boolean isArmor = AdminItemCreatorGUI.isArmor(item);
        boolean isTool = AdminItemCreatorGUI.isToolOrWeapon(item);

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Custom Enchants: <gold>").append(activeCE.size()).append(" sihir aktif</gold></gray>\n");
        desc.append("<gray>Vanilla Enchants: <aqua>").append(activeVanilla).append(" enchant aktif</aqua></gray>\n");

        if (isArmor) {
            String sName = getArmorSetName(item, creatorGUI);
            if (!sName.isBlank()) {
                desc.append("<gray>Armor Set Sinergi: <yellow>").append(sName).append("</yellow></gray>\n");
            }
        } else if (isTool) {
            String cName = (creatorGUI != null) ? creatorGUI.getGlobalSetName() : "";
            if (cName != null && !cName.isBlank()) {
                desc.append("<gray>Tool Set Sinergi: <yellow>").append(cName).append("</yellow></gray>\n");
            }
        }
        desc.append("<dark_gray>Pilih menu konfigurasi di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        buttons.add(new DialogButtonData("<purple><bold>🔮 KELOLA CUSTOM ENCHANTS</bold></purple>", "Buka katalog 182 Custom Enchantments",
                () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, "ALL", 1)));

        buttons.add(new DialogButtonData("<yellow><bold>📜 KELOLA VANILLA ENCHANTS</bold></yellow>", "Buka katalog sihir Vanilla Minecraft",
                () -> openVanillaEnchants(plugin, player, item, sourceSlot, creatorGUI, 1)));

        buttons.add(new DialogButtonData("<gold><bold>🏷 UBAH NAMA ITEM</bold></gold>", "Ubah nama item via GUI Dialog",
                () -> plugin.getItemRenameManager().startSession(
                        player,
                        "Masukkan nama baru untuk item ini (bisa menggunakan & atau MiniMessage):",
                        newName -> {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                meta.displayName(ColorUtil.parse(newName));
                                item.setItemMeta(meta);
                                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                                player.sendMessage(mm.deserialize("<green>✓ Nama item berhasil diubah!</green>"));
                            }
                            openRoot(plugin, player, item, sourceSlot, creatorGUI);
                        },
                        () -> openRoot(plugin, player, item, sourceSlot, creatorGUI)
                )));

        if (isArmor) {
            buttons.add(new DialogButtonData("<blue><bold>🛡 ATUR ARMOR SET BONUS</bold></blue>", "Konfigurasi efek sinergi 2-Piece dan 4-Piece Set",
                    () -> openArmorSetBonus(plugin, player, item, sourceSlot, creatorGUI)));
        }

        if (isTool) {
            boolean isSetBonusActive = (creatorGUI != null && creatorGUI.isSetBonusConfigured());
            if (isSetBonusActive) {
                String setName = (creatorGUI.getGlobalSetName() != null && !creatorGUI.getGlobalSetName().isBlank()) 
                        ? creatorGUI.getGlobalSetName() 
                        : "Set Armor";
                buttons.add(new DialogButtonData("<aqua><bold>⚔ ATUR TOOL SET BONUS</bold></aqua>", "Atur sinergi atribut tool dengan " + setName,
                        () -> openToolBonus(plugin, player, item, sourceSlot, creatorGUI)));
            } else {
                buttons.add(new DialogButtonData("<gray><italic>⚔ TOOL SET BONUS (NONAKTIF)</italic></gray>", "Aktifkan Bonus Set Armor di menu utama terlebih dahulu!", () -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(mm.deserialize("<red>⚠ Bonus Set Armor belum dinyalakan! Silakan atur dan aktifkan Bonus Set Armor terlebih dahulu di tombol Set Bonus (Slot 15) pada Creator Utama.</red>"));
                    openRoot(plugin, player, item, sourceSlot, creatorGUI);
                }));
            }
        }

        int totalActive = activeCE.size() + activeVanilla;
        if (totalActive > 0) {
            buttons.add(new DialogButtonData("<red><bold>✂ HAPUS ENCHANT TERTENTU</bold></red>", "Lepas sihir satu per satu (" + totalActive + " aktif)",
                    () -> openRemoveEnchants(plugin, player, item, sourceSlot, creatorGUI)));
        }

        buttons.add(new DialogButtonData("<red><bold>🗑 RESET SEMUA ENCHANT</bold></red>", "Hapus seluruh sihir dari item",
                () -> openResetConfirm(plugin, player, item, sourceSlot, creatorGUI)));

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE ITEM CREATOR</bold></gray>", "Simpan dan kembali ke creator utama", () -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            if (creatorGUI != null) {
                creatorGUI.updateItem(sourceSlot, item);
                creatorGUI.open();
            }
        });

        return NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#e74c3c:#f39c12><bold>🛠 EDIT ITEM & ENCHANTS 🛠</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 2. CUSTOM ENCHANTS CATALOG DIALOG
    // ==========================================================

    public static void openCustomEnchants(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                          AdminItemCreatorGUI creatorGUI, String tierFilter, int page) {
        if (item == null || player == null || !player.isOnline()) return;

        List<CustomEnchant> filtered = new ArrayList<>();
        for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
            if (!item.getType().isAir() && !ce.canApplyTo(item)) continue;
            if (!tierFilter.equalsIgnoreCase("ALL") && !tierFilter.equalsIgnoreCase("ATTACHED")) {
                if (!ce.getGroup().getId().equalsIgnoreCase(tierFilter)) continue;
            } else if (tierFilter.equalsIgnoreCase("ATTACHED")) {
                if (plugin.getEnchantmentRegistry().getEnchantLevel(item, ce) <= 0) continue;
            }
            filtered.add(ce);
        }

        filtered.sort((c1, c2) -> {
            int w1 = c1.getRarityWeight();
            int w2 = c2.getRarityWeight();
            if (w1 != w2) {
                return Integer.compare(w2, w1);
            }
            return c1.getDisplayName().compareToIgnoreCase(c2.getDisplayName());
        });

        int pageSize = 6;
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        int p = Math.max(1, Math.min(page, totalPages));

        String nextTier = getNextTierFilter(tierFilter);
        String tierDisplayName = tierFilter.equalsIgnoreCase("ATTACHED") ? "TERPASANG SAJA" : tierFilter.toUpperCase();

        List<DialogButtonData> buttons = new ArrayList<>();

        // Tier Cycle Filter Button
        buttons.add(new DialogButtonData(
                "<yellow>⚡ FILTER: <gold>" + tierDisplayName + "</gold></yellow>",
                "Klik untuk beralih filter tier berikutnya (" + nextTier + ")",
                () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, nextTier, 1)
        ));

        // Quick Toggle: View attached or view all
        if (tierFilter.equalsIgnoreCase("ATTACHED")) {
            buttons.add(new DialogButtonData("<aqua>🔍 TAMPILKAN SEMUA SIHIR</aqua>", "Tampilkan seluruh sihir yang cocok untuk item",
                    () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, "ALL", 1)));
        } else {
            buttons.add(new DialogButtonData("<green>✔ LIHAT SIHIR TERPASANG SAJA</green>", "Saring hanya sihir yang sedang aktif pada item",
                    () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, "ATTACHED", 1)));
        }

        // Add Enchantment buttons for the current page
        int startIndex = (p - 1) * pageSize;
        int endIndex = Math.min(p * pageSize, filtered.size());

        for (int i = startIndex; i < endIndex; i++) {
            CustomEnchant ce = filtered.get(i);
            int curLvl = plugin.getEnchantmentRegistry().getEnchantLevel(item, ce);
            String groupColor = (ce.getGroup() != null && ce.getGroup().getColor() != null && !ce.getGroup().getColor().isBlank())
                    ? ce.getGroup().getColor()
                    : "#f1c40f";
            String colorTag = "<color:" + groupColor + ">";
            String closeTag = "</color>";
            String label = curLvl > 0
                    ? "<green>✔ </green>" + colorTag + "<bold>" + ce.getDisplayName() + "</bold>" + closeTag + " <gold>Lv." + CustomEnchant.toRoman(curLvl) + "</gold>"
                    : colorTag + "✦ <bold>" + ce.getDisplayName() + "</bold>" + closeTag;
            String tooltip = "Tier: " + (ce.getGroup() != null ? ce.getGroup().getDisplayName() : "Unknown") + " | Max: " + ce.getMaxLevel()
                    + "\n" + ce.getDescription() + "\n▶ Klik untuk mengatur level";

            buttons.add(new DialogButtonData(label, tooltip,
                    () -> openCustomEnchantLevel(plugin, player, item, sourceSlot, creatorGUI, ce, tierFilter, p)));
        }

        // Pagination buttons
        if (p > 1) {
            buttons.add(new DialogButtonData("<yellow>⬅ Hal. Sebelumnya (" + (p - 1) + ")</yellow>", "Ke halaman sebelumnya",
                    () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, tierFilter, p - 1)));
        }
        if (p < totalPages) {
            buttons.add(new DialogButtonData("<yellow>Hal. Berikutnya (" + (p + 1) + ") ➡</yellow>", "Ke halaman berikutnya",
                    () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, tierFilter, p + 1)));
        }

        String desc = "<gray>Ditemukan <gold>" + filtered.size() + " sihir</gold> (Halaman <yellow>" + p + "/" + totalPages + "</yellow>).\n"
                + "Klik sihir di bawah untuk memasang atau mengubah tingkat levelnya:</gray>";

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE MENU EDIT ITEM</bold></gray>", "Kembali ke menu edit item utama",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#9b59b6:#e74c3c><bold>🔮 KATALOG CUSTOM ENCHANTS 🔮</bold></gradient>",
                desc,
                buttons,
                exitBtn,
                2
        );
    }

    private static String getNextTierFilter(String current) {
        String[] tiers = {"ALL", "SIMPLE", "UNIQUE", "ELITE", "ULTIMATE", "LEGENDARY", "FABLED", "ATTACHED"};
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i].equalsIgnoreCase(current)) {
                return tiers[(i + 1) % tiers.length];
            }
        }
        return "ALL";
    }

    // ==========================================================
    // 3. CUSTOM ENCHANT LEVEL PICKER DIALOG
    // ==========================================================

    public static void openCustomEnchantLevel(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                              AdminItemCreatorGUI creatorGUI, CustomEnchant ce, String prevTier, int prevPage) {
        if (item == null || player == null || !player.isOnline()) return;

        int curLvl = plugin.getEnchantmentRegistry().getEnchantLevel(item, ce);
        String groupColor = (ce.getGroup() != null && ce.getGroup().getColor() != null && !ce.getGroup().getColor().isBlank())
                ? ce.getGroup().getColor()
                : "#f1c40f";
        String colorTag = "<color:" + groupColor + ">";
        String closeTag = "</color>";

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Tier: </gray>").append(colorTag).append("<bold>").append(ce.getGroup().getDisplayName()).append("</bold>").append(closeTag).append("\n");
        desc.append("<gray>Target: <aqua>").append(ce.getAppliesTo()).append("</aqua></gray>\n");
        desc.append("<gray>Level Maks: <gold>").append(ce.getMaxLevel()).append("</gold></gray>\n");
        desc.append("<yellow>").append(ce.getDescription()).append("</yellow>\n");
        desc.append("<gray>Status: ").append(curLvl > 0 ? "<green>Terpasang Level " + CustomEnchant.toRoman(curLvl) + " (" + curLvl + ")</green>" : "<dark_gray>Belum Terpasang</dark_gray>").append("</gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        for (int lvl = 1; lvl <= ce.getMaxLevel(); lvl++) {
            final int selectedLevel = lvl;
            boolean isCurrent = (curLvl == selectedLevel);
            String label = isCurrent
                    ? "<green><bold>✔ Level " + CustomEnchant.toRoman(lvl) + " (" + lvl + ")</bold></green>"
                    : colorTag + "<bold>Level " + CustomEnchant.toRoman(lvl) + " (" + lvl + ")</bold>" + closeTag;
            String tooltip = isCurrent ? "Level ini sedang aktif pada item" : "Pasang sihir ini pada Level " + lvl;

            buttons.add(new DialogButtonData(label, tooltip, () -> {
                ItemStack updated = plugin.getEnchantmentRegistry().applyEnchant(item, ce, selectedLevel);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Berhasil memasang sihir <gold>" + ce.getDisplayName() + " " + CustomEnchant.toRoman(selectedLevel) + "</gold>!</green>"));
                openCustomEnchants(plugin, player, updated, sourceSlot, creatorGUI, prevTier, prevPage);
            }));
        }

        if (curLvl > 0) {
            buttons.add(new DialogButtonData("<red><bold>❌ HAPUS SIHIR INI</bold></red>", "Lepas sihir ini dari item", () -> {
                ItemStack updated = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Sihir <gold>" + ce.getDisplayName() + "</gold> berhasil dilepas!</yellow>"));
                openCustomEnchants(plugin, player, updated, sourceSlot, creatorGUI, prevTier, prevPage);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE DAFTAR SIHIR</bold></gray>", "Kembali ke katalog custom enchants",
                () -> openCustomEnchants(plugin, player, item, sourceSlot, creatorGUI, prevTier, prevPage));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                colorTag + "<bold>🔮 ATUR LEVEL: " + ce.getDisplayName().toUpperCase() + "</bold>" + closeTag,
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 4. VANILLA ENCHANTS CATALOG DIALOG
    // ==========================================================

    public static void openVanillaEnchants(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                           AdminItemCreatorGUI creatorGUI, int page) {
        if (item == null || player == null || !player.isOnline()) return;

        List<Enchantment> available = getApplicableVanillaEnchants(item);
        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil((double) available.size() / pageSize));
        int p = Math.max(1, Math.min(page, totalPages));

        List<DialogButtonData> buttons = new ArrayList<>();
        int startIndex = (p - 1) * pageSize;
        int endIndex = Math.min(p * pageSize, available.size());

        for (int i = startIndex; i < endIndex; i++) {
            Enchantment ve = available.get(i);
            int curLvl = item.getEnchantmentLevel(ve);
            String name = getVanillaName(ve);
            String label = curLvl > 0
                    ? "<green>✔ " + name + " <gold>" + CustomEnchant.toRoman(curLvl) + " (" + curLvl + ")</gold></green>"
                    : "<yellow>✦ " + name + "</yellow>";
            String tooltip = "Level Maks Vanilla: " + CustomEnchant.toRoman(ve.getMaxLevel()) + " (" + ve.getMaxLevel() + ")\n"
                    + "Pilihan Level: I s/d XX (Level 1 - 20)\n"
                    + (curLvl > 0 ? "Status: Level " + CustomEnchant.toRoman(curLvl) + " (" + curLvl + ") aktif\n" : "")
                    + "▶ Klik untuk memilih level";

            buttons.add(new DialogButtonData(label, tooltip,
                    () -> openVanillaEnchantLevel(plugin, player, item, sourceSlot, creatorGUI, ve, p)));
        }

        if (p > 1) {
            buttons.add(new DialogButtonData("<yellow>⬅ Hal. Sebelumnya (" + (p - 1) + ")</yellow>", "Ke halaman sebelumnya",
                    () -> openVanillaEnchants(plugin, player, item, sourceSlot, creatorGUI, p - 1)));
        }
        if (p < totalPages) {
            buttons.add(new DialogButtonData("<yellow>Hal. Berikutnya (" + (p + 1) + ") ➡</yellow>", "Ke halaman berikutnya",
                    () -> openVanillaEnchants(plugin, player, item, sourceSlot, creatorGUI, p + 1)));
        }

        String desc = "<gray>Ditemukan <gold>" + available.size() + " sihir Vanilla</gold> (Halaman <yellow>" + p + "/" + totalPages + "</yellow>).\n"
                + "Pilih sihir di bawah untuk memasang atau mengubah tingkat levelnya (Level I s/d XX):</gray>";

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE MENU EDIT ITEM</bold></gray>", "Kembali ke menu edit item utama",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#f1c40f:#e67e22><bold>📜 KATALOG VANILLA ENCHANTS 📜</bold></gradient>",
                desc,
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 5. VANILLA ENCHANT LEVEL PICKER DIALOG
    // ==========================================================

    public static void openVanillaEnchantLevel(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                               AdminItemCreatorGUI creatorGUI, Enchantment ve, int prevPage) {
        if (item == null || player == null || !player.isOnline()) return;

        int curLvl = item.getEnchantmentLevel(ve);
        String name = getVanillaName(ve);
        int maxVanillaLvl = Math.max(1, ve.getMaxLevel());
        int maxLvl = 20;

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Vanilla Enchant: <gold>").append(name).append("</gold></gray>\n");
        desc.append("<gray>Level Maks Vanilla: <aqua>").append(CustomEnchant.toRoman(maxVanillaLvl)).append(" (").append(maxVanillaLvl).append(")</aqua></gray>\n");
        desc.append("<gray>Pilihan Level: <gold>I s/d XX (Level 1 - 20)</gold></gray>\n");
        desc.append("<gray>Status: ").append(curLvl > 0 ? "<green>Terpasang Level " + CustomEnchant.toRoman(curLvl) + " (" + curLvl + ")</green>" : "<dark_gray>Belum Terpasang</dark_gray>").append("</gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        for (int lvl = 1; lvl <= maxLvl; lvl++) {
            final int selectedLevel = lvl;
            boolean isCurrent = (curLvl == selectedLevel);
            String roman = CustomEnchant.toRoman(lvl);
            String label = isCurrent
                    ? "<green><bold>✔ Level " + roman + " (" + lvl + ")</bold></green>"
                    : "<gold><bold>Level " + roman + " (" + lvl + ")</bold></gold>";
            String tooltip = isCurrent ? "Level " + roman + " (" + lvl + ") sedang aktif" : "Pasang enchant ini pada Level " + roman + " (" + lvl + ")";

            buttons.add(new DialogButtonData(label, tooltip, () -> {
                item.addUnsafeEnchantment(ve, selectedLevel);
                ItemStack updated = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>✓ Berhasil memasang enchant <gold>" + name + " " + roman + " (" + selectedLevel + ")</gold>!</green>"));
                openVanillaEnchants(plugin, player, updated, sourceSlot, creatorGUI, prevPage);
            }));
        }

        if (curLvl > 0) {
            buttons.add(new DialogButtonData("<red><bold>❌ HAPUS ENCHANT INI</bold></red>", "Lepas enchant ini dari item", () -> {
                item.removeEnchantment(ve);
                ItemStack updated = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Enchant <gold>" + name + "</gold> berhasil dilepas!</yellow>"));
                openVanillaEnchants(plugin, player, updated, sourceSlot, creatorGUI, prevPage);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE DAFTAR VANILLA</bold></gray>", "Kembali ke katalog vanilla enchants",
                () -> openVanillaEnchants(plugin, player, item, sourceSlot, creatorGUI, prevPage));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#f1c40f:#e67e22><bold>📜 ATUR LEVEL: " + name.toUpperCase() + " (I - XX)</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                4
        );
    }

    // ==========================================================
    // 6. SELECTIVE ENCHANT REMOVER DIALOG
    // ==========================================================

    public static void openRemoveEnchants(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                           AdminItemCreatorGUI creatorGUI) {
        if (item == null || player == null || !player.isOnline()) return;

        Map<CustomEnchant, Integer> activeCE = plugin.getEnchantmentRegistry().getEnchantsOnItem(item);
        Map<Enchantment, Integer> activeVanilla = item.getEnchantments();

        List<DialogButtonData> buttons = new ArrayList<>();

        for (Map.Entry<CustomEnchant, Integer> entry : activeCE.entrySet()) {
            CustomEnchant ce = entry.getKey();
            int lvl = entry.getValue();
            String groupColor = (ce.getGroup() != null && ce.getGroup().getColor() != null && !ce.getGroup().getColor().isBlank())
                    ? ce.getGroup().getColor()
                    : "#f1c40f";
            String label = "<red>✂ [Custom] </red><color:" + groupColor + "><bold>" + ce.getDisplayName() + "</bold></color> <gold>" + CustomEnchant.toRoman(lvl) + "</gold>";
            String tooltip = "Klik untuk menghapus sihir " + ce.getDisplayName() + " dari item";

            buttons.add(new DialogButtonData(label, tooltip, () -> {
                ItemStack updated = plugin.getEnchantmentRegistry().removeEnchant(item, ce);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Sihir <gold>" + ce.getDisplayName() + "</gold> berhasil dihapus!</yellow>"));
                openRemoveEnchants(plugin, player, updated, sourceSlot, creatorGUI);
            }));
        }

        for (Map.Entry<Enchantment, Integer> entry : activeVanilla.entrySet()) {
            Enchantment ve = entry.getKey();
            int lvl = entry.getValue();
            String name = getVanillaName(ve);
            String label = "<red>✂ [Vanilla] </red><yellow>" + name + " " + CustomEnchant.toRoman(lvl) + " (" + lvl + ")</yellow>";
            String tooltip = "Klik untuk menghapus enchant " + name + " " + CustomEnchant.toRoman(lvl) + " (" + lvl + ") dari item";

            buttons.add(new DialogButtonData(label, tooltip, () -> {
                item.removeEnchantment(ve);
                ItemStack updated = plugin.getEnchantmentRegistry().updateLoreAndGlint(item);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, updated);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Enchant <gold>" + name + "</gold> berhasil dihapus!</yellow>"));
                openRemoveEnchants(plugin, player, updated, sourceSlot, creatorGUI);
            }));
        }

        int total = activeCE.size() + activeVanilla.size();
        String desc = total > 0
                ? "<gray>Klik salah satu sihir di bawah untuk melepasnya dari item:</gray>"
                : "<gray>Tidak ada sihir atau enchant yang terpasang pada item ini.</gray>";

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ SELESAI & KEMBALI KE EDIT ITEM</bold></gray>", "Kembali ke menu edit item utama",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#e74c3c:#c0392b><bold>✂ HAPUS ENCHANT TERTENTU ✂</bold></gradient>",
                desc,
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 7. GLOBAL ARMOR SET BONUS DIALOG (CREATOR SLOT 15)
    // ==========================================================

    public static void openGlobalArmorSetBonus(ApexsionsCustomEnchantsPlugin plugin, Player player, AdminItemCreatorGUI creatorGUI) {
        if (player == null || !player.isOnline() || creatorGUI == null) return;
        creatorGUI.setNavigatingSubGUI(true);

        ItemStack ref = creatorGUI.getPlacedItems().get(AdminItemCreatorGUI.SLOT_HELMET);
        if (ref == null) {
            ref = creatorGUI.getPlacedItems().values().stream().findFirst().orElse(null);
        }
        if (ref == null) {
            ref = new ItemStack(Material.NETHER_STAR);
            ItemMeta m = ref.getItemMeta();
            if (m != null) {
                m.displayName(ColorUtil.parse("<gradient:#f1c40f:#e67e22><bold>ARMOR SET BONUS</bold></gradient>"));
                ref.setItemMeta(m);
            }
        }

        String setName = creatorGUI.getGlobalSetName();
        Map<KitStatType, Double> set2Stats = creatorGUI.getGlobalSet2Stats();
        Map<KitStatType, Double> set4Stats = creatorGUI.getGlobalSet4Stats();

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Nama Set Armor: ").append(!setName.isBlank() ? ColorUtil.toPlainText(setName) : "<gold>(Belum Diatur)</gold>").append("</gray>\n");
        desc.append("<gray>2-Piece (Half Set): ").append(set2Stats.isEmpty() ? "<dark_gray>Nonaktif</dark_gray>" : "<green>" + set2Stats.size() + " Efek Aktif</green>").append("</gray>\n");
        desc.append("<gray>4-Piece (Full Set): ").append(set4Stats.isEmpty() ? "<dark_gray>Nonaktif</dark_gray>" : "<green>" + set4Stats.size() + " Efek Aktif</green>").append("</gray>\n");
        desc.append("<dark_gray>Pilih opsi konfigurasi set bonus di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();

        buttons.add(new DialogButtonData("<gold><bold>🏷 UBAH NAMA SET ARMOR</bold></gold>", "Ubah nama set dan otomatis terapkan ke seluruh item", () -> {
            plugin.getItemRenameManager().startSession(
                    player,
                    "Masukkan nama dasar / prefix untuk seluruh set (contoh: &6&lPaladin atau <gradient:#e74c3c:#f39c12>Shadow</gradient>):",
                    newName -> {
                        creatorGUI.renameAllItems(newName);
                        openGlobalArmorSetBonus(plugin, player, creatorGUI);
                    },
                    () -> openGlobalArmorSetBonus(plugin, player, creatorGUI)
            );
        }));

        buttons.add(new DialogButtonData("<blue><bold>🛡 ATUR EFEK 2-PIECE (HALF SET)</bold></blue>", "Konfigurasi persentase stat untuk 2 potong armor",
                () -> openGlobalArmorPieceConfig(plugin, player, creatorGUI, 2)));

        buttons.add(new DialogButtonData("<purple><bold>👑 ATUR EFEK 4-PIECE (FULL SET)</bold></purple>", "Konfigurasi persentase stat untuk 4 potong armor",
                () -> openGlobalArmorPieceConfig(plugin, player, creatorGUI, 4)));

        if (!set2Stats.isEmpty() || !set4Stats.isEmpty()) {
            buttons.add(new DialogButtonData("<red><bold>✖ HAPUS SELURUH SET BONUS</bold></red>", "Hapus seluruh efek 2-piece dan 4-piece", () -> {
                creatorGUI.getGlobalSet2Stats().clear();
                creatorGUI.getGlobalSet4Stats().clear();
                creatorGUI.setSetBonusConfigured(false);
                creatorGUI.removeFullsetBonusFromAll();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Seluruh set bonus berhasil dihapus!</yellow>"));
                openGlobalArmorSetBonus(plugin, player, creatorGUI);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ SELESAI & KEMBALI KE ITEM CREATOR</bold></gray>", "Simpan dan kembali ke creator utama", () -> {
            creatorGUI.setSetBonusConfigured(!creatorGUI.getGlobalSet2Stats().isEmpty() || !creatorGUI.getGlobalSet4Stats().isEmpty());
            if (creatorGUI.isSetBonusConfigured()) {
                creatorGUI.checkAndApplyFullsetBonus();
            }
            creatorGUI.open();
        });

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                ref,
                "<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN ARMOR SET BONUS 🛡</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    public static void openGlobalArmorPieceConfig(ApexsionsCustomEnchantsPlugin plugin, Player player, AdminItemCreatorGUI creatorGUI, int pieceCount) {
        if (player == null || !player.isOnline() || creatorGUI == null) return;
        creatorGUI.setNavigatingSubGUI(true);

        ItemStack ref = creatorGUI.getPlacedItems().get(AdminItemCreatorGUI.SLOT_HELMET);
        if (ref == null) {
            ref = creatorGUI.getPlacedItems().values().stream().findFirst().orElse(null);
        }
        if (ref == null) {
            ref = new ItemStack(Material.NETHER_STAR);
            ItemMeta m = ref.getItemMeta();
            if (m != null) {
                m.displayName(ColorUtil.parse("<gradient:#f1c40f:#e67e22><bold>ARMOR SET BONUS</bold></gradient>"));
                ref.setItemMeta(m);
            }
        }

        String setName = creatorGUI.getGlobalSetName();
        Map<KitStatType, Double> pieceStats = (pieceCount == 4) ? creatorGUI.getGlobalSet4Stats() : creatorGUI.getGlobalSet2Stats();

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Mengatur bonus untuk <yellow>").append(pieceCount).append("-Piece (").append(pieceCount == 4 ? "Full Set" : "Half Set").append(")</yellow></gray>\n");
        desc.append("<gray>Set: <gold>").append(setName.isBlank() ? "Apexsions" : ColorUtil.toPlainText(setName)).append("</gold></gray>\n");
        desc.append("<dark_gray>Klik stat di bawah untuk mengatur nilainya:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        KitStatType[] types = {
                KitStatType.DAMAGE_REDUCTION,
                KitStatType.CRITICAL_DAMAGE_REDUCTION,
                KitStatType.DODGE_CHANCE,
                KitStatType.EXTRA_MAX_HEALTH,
                KitStatType.MOVEMENT_SPEED_BOOST
        };

        for (KitStatType st : types) {
            Double val = pieceStats.get(st);
            String valStr = (val != null) ? "<green>" + st.formatValue(val) + "</green>" : "<dark_gray>Nonaktif</dark_gray>";
            String label = "<aqua>● " + st.getDisplayName() + ": " + valStr + "</aqua>";
            String tooltip = "Klik untuk mengatur nilai " + st.getDisplayName();

            buttons.add(new DialogButtonData(label, tooltip,
                    () -> openGlobalStatValuePicker(plugin, player, creatorGUI, pieceCount, st)));
        }

        if (!pieceStats.isEmpty()) {
            buttons.add(new DialogButtonData("<red><bold>✖ KOSONGKAN EFEK " + pieceCount + "-PIECE</bold></red>", "Hapus seluruh efek pada tier ini", () -> {
                pieceStats.clear();
                creatorGUI.setSetBonusConfigured(!creatorGUI.getGlobalSet2Stats().isEmpty() || !creatorGUI.getGlobalSet4Stats().isEmpty());
                creatorGUI.checkAndApplyFullsetBonus();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE PENGATURAN SET BONUS</bold></gray>", "Kembali ke menu set bonus",
                () -> openGlobalArmorSetBonus(plugin, player, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                ref,
                "<gradient:#3498db:#2ecc71><bold>🛡 ATUR " + pieceCount + "-PIECE STATS 🛡</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    public static void openGlobalStatValuePicker(ApexsionsCustomEnchantsPlugin plugin, Player player, AdminItemCreatorGUI creatorGUI,
                                                 int pieceCount, KitStatType statType) {
        if (player == null || !player.isOnline() || creatorGUI == null) return;
        creatorGUI.setNavigatingSubGUI(true);

        ItemStack ref = creatorGUI.getPlacedItems().get(AdminItemCreatorGUI.SLOT_HELMET);
        if (ref == null) {
            ref = creatorGUI.getPlacedItems().values().stream().findFirst().orElse(null);
        }
        if (ref == null) {
            ref = new ItemStack(Material.NETHER_STAR);
            ItemMeta m = ref.getItemMeta();
            if (m != null) {
                m.displayName(ColorUtil.parse("<gradient:#f1c40f:#e67e22><bold>ARMOR SET BONUS</bold></gradient>"));
                ref.setItemMeta(m);
            }
        }

        Map<KitStatType, Double> pieceStats = (pieceCount == 4) ? creatorGUI.getGlobalSet4Stats() : creatorGUI.getGlobalSet2Stats();
        Double currentVal = pieceStats.get(statType);

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Stat: <aqua>").append(statType.getDisplayName()).append("</aqua></gray>\n");
        desc.append("<gray>Nilai Saat Ini: ").append(currentVal != null ? "<green>" + statType.formatValue(currentVal) + "</green>" : "<dark_gray>Nonaktif</dark_gray>").append("</gray>\n");
        desc.append("<dark_gray>Pilih salah satu preset nilai atau ketik nilai sendiri di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();

        if (statType == KitStatType.EXTRA_MAX_HEALTH) {
            double[] presets = {2.0, 4.0, 6.0, 8.0, 10.0, 14.0, 20.0};
            String[] names = {"+2 HP (1 Heart)", "+4 HP (2 Hearts)", "+6 HP (3 Hearts)", "+8 HP (4 Hearts)", "+10 HP (5 Hearts)", "+14 HP (7 Hearts)", "+20 HP (10 Hearts)"};
            for (int i = 0; i < presets.length; i++) {
                double val = presets[i];
                String name = names[i];
                buttons.add(new DialogButtonData("<gold><bold>" + name + "</bold></gold>", "Terapkan " + name, () -> {
                    pieceStats.put(statType, val);
                    creatorGUI.setSetBonusConfigured(true);
                    creatorGUI.checkAndApplyFullsetBonus();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount);
                }));
            }
        } else {
            double[] percentages = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0};
            for (double pct : percentages) {
                buttons.add(new DialogButtonData("<gold><bold>+" + (int) pct + "%</bold></gold>", "Terapkan nilai +" + (int) pct + "%", () -> {
                    pieceStats.put(statType, pct);
                    creatorGUI.setSetBonusConfigured(true);
                    creatorGUI.checkAndApplyFullsetBonus();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount);
                }));
            }
        }

        // Custom manual input via Dialog / Chat
        buttons.add(new DialogButtonData("<yellow><bold>✏ SET NILAI SENDIRI (MANUAL)</bold></yellow>", "Ketik nilai persentase atau angka sendiri secara bebas", () -> {
            plugin.getItemRenameManager().startSession(
                    player,
                    "Masukkan nilai angka/persentase untuk " + statType.getDisplayName() + " (contoh: 15 atau 22.5):",
                    rawInput -> {
                        try {
                            double customVal = Double.parseDouble(rawInput.trim().replace("%", ""));
                            if (customVal > 0) {
                                pieceStats.put(statType, customVal);
                                creatorGUI.setSetBonusConfigured(true);
                                creatorGUI.checkAndApplyFullsetBonus();
                                player.sendMessage(mm.deserialize("<green>✓ Nilai stat <gold>" + statType.getDisplayName() + "</gold> berhasil diatur ke <gold>" + statType.formatValue(customVal) + "</gold>!</green>"));
                            }
                        } catch (Exception e) {
                            player.sendMessage(mm.deserialize("<red>Format angka tidak valid! Harap masukkan angka yang valid.</red>"));
                        }
                        openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount);
                    },
                    () -> openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount)
            );
        }));

        if (currentVal != null) {
            buttons.add(new DialogButtonData("<red><bold>❌ NONAKTIFKAN STAT INI</bold></red>", "Hapus efek stat ini", () -> {
                pieceStats.remove(statType);
                creatorGUI.checkAndApplyFullsetBonus();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI</bold></gray>", "Kembali ke daftar stat",
                () -> openGlobalArmorPieceConfig(plugin, player, creatorGUI, pieceCount));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                ref,
                "<gradient:#f1c40f:#e67e22><bold>⚙ ATUR STAT: " + statType.getDisplayName().toUpperCase() + "</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 8. ARMOR SET BONUS DIALOG (PER-PIECE)
    // ==========================================================

    public static void openArmorSetBonus(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                         AdminItemCreatorGUI creatorGUI) {
        if (item == null || player == null || !player.isOnline()) return;

        String setName = getArmorSetName(item, creatorGUI);
        Map<KitStatType, Double> set2Stats = parseArmorStats(item, "set2_stats");
        Map<KitStatType, Double> set4Stats = parseArmorStats(item, "set4_stats");

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Nama Set Armor: <gold>").append(setName.isBlank() ? "(Belum Diatur)" : setName).append("</gold></gray>\n");
        desc.append("<gray>2-Piece (Half Set): ").append(set2Stats.isEmpty() ? "<dark_gray>Nonaktif</dark_gray>" : "<green>" + set2Stats.size() + " Efek Aktif</green>").append("</gray>\n");
        desc.append("<gray>4-Piece (Full Set): ").append(set4Stats.isEmpty() ? "<dark_gray>Nonaktif</dark_gray>" : "<green>" + set4Stats.size() + " Efek Aktif</green>").append("</gray>\n");
        desc.append("<dark_gray>Pilih opsi pengaturan di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();

        buttons.add(new DialogButtonData("<gold><bold>🏷 UBAH NAMA SET ARMOR</bold></gold>", "Ubah nama set yang mengikat bonus armor ini", () -> {
            plugin.getItemRenameManager().startSession(
                    player,
                    "Masukkan nama Set Armor (contoh: Paladin, Overlord, Shadow):",
                    newName -> {
                        String cleanName = (newName != null && !newName.isBlank()) ? newName.trim() : "Apexsions";
                        saveArmorSetBonusToItem(item, cleanName, set2Stats, set4Stats);
                        if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                        player.sendMessage(mm.deserialize("<green>✓ Nama Set Armor diubah menjadi </green>")
                                .append(ColorUtil.parse(cleanName))
                                .append(mm.deserialize("<green>!</green>")));
                        openArmorSetBonus(plugin, player, item, sourceSlot, creatorGUI);
                    },
                    () -> openArmorSetBonus(plugin, player, item, sourceSlot, creatorGUI)
            );
        }));

        buttons.add(new DialogButtonData("<blue><bold>🛡 ATUR EFEK 2-PIECE (HALF SET)</bold></blue>", "Konfigurasi persentase stat untuk 2 potong armor",
                () -> openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, 2)));

        buttons.add(new DialogButtonData("<purple><bold>👑 ATUR EFEK 4-PIECE (FULL SET)</bold></purple>", "Konfigurasi persentase stat untuk 4 potong armor",
                () -> openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, 4)));

        if (!set2Stats.isEmpty() || !set4Stats.isEmpty()) {
            buttons.add(new DialogButtonData("<red><bold>✖ HAPUS SELURUH SET BONUS</bold></red>", "Hapus seluruh efek 2-piece dan 4-piece dari armor ini", () -> {
                removeArmorSetBonusFromItem(item);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                player.sendMessage(mm.deserialize("<yellow>Seluruh set bonus berhasil dihapus dari item!</yellow>"));
                openArmorSetBonus(plugin, player, item, sourceSlot, creatorGUI);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ SELESAI & KEMBALI KE EDIT ITEM</bold></gray>", "Simpan dan kembali ke menu edit item",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#e74c3c:#f39c12><bold>🛡 PENGATURAN ARMOR SET BONUS 🛡</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 8. ARMOR PIECE STATS CONFIG DIALOG (2-PIECE OR 4-PIECE)
    // ==========================================================

    public static void openArmorPieceConfig(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                            AdminItemCreatorGUI creatorGUI, int pieceCount) {
        if (item == null || player == null || !player.isOnline()) return;

        String setName = getArmorSetName(item, creatorGUI);
        String pKey = (pieceCount == 4) ? "set4_stats" : "set2_stats";
        Map<KitStatType, Double> pieceStats = parseArmorStats(item, pKey);

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Mengatur bonus untuk <yellow>").append(pieceCount).append("-Piece (").append(pieceCount == 4 ? "Full Set" : "Half Set").append(")</yellow></gray>\n");
        desc.append("<gray>Set: <gold>").append(setName.isBlank() ? "Apexsions" : setName).append("</gold></gray>\n");
        desc.append("<dark_gray>Klik stat di bawah untuk mengatur persentase nilainya:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        KitStatType[] types = {
                KitStatType.DAMAGE_REDUCTION,
                KitStatType.CRITICAL_DAMAGE_REDUCTION,
                KitStatType.DODGE_CHANCE,
                KitStatType.EXTRA_MAX_HEALTH,
                KitStatType.MOVEMENT_SPEED_BOOST
        };

        for (KitStatType st : types) {
            Double val = pieceStats.get(st);
            String valStr = (val != null) ? "<green>" + st.formatValue(val) + "</green>" : "<dark_gray>Nonaktif</dark_gray>";
            String label = "<aqua>● " + st.getDisplayName() + ": " + valStr + "</aqua>";
            String tooltip = "Klik untuk mengatur nilai " + st.getDisplayName();

            buttons.add(new DialogButtonData(label, tooltip,
                    () -> openStatValuePicker(plugin, player, item, sourceSlot, creatorGUI, pieceCount, st)));
        }

        if (!pieceStats.isEmpty()) {
            buttons.add(new DialogButtonData("<red><bold>✖ KOSONGKAN EFEK " + pieceCount + "-PIECE</bold></red>", "Hapus seluruh efek pada tier ini", () -> {
                pieceStats.clear();
                saveArmorPieceStats(item, setName, pieceCount, pieceStats, creatorGUI);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI KE PENGATURAN SET BONUS</bold></gray>", "Kembali ke menu set bonus",
                () -> openArmorSetBonus(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#3498db:#2ecc71><bold>🛡 ATUR " + pieceCount + "-PIECE STATS 🛡</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 9. STAT VALUE PRESET PICKER DIALOG
    // ==========================================================

    public static void openStatValuePicker(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                           AdminItemCreatorGUI creatorGUI, int pieceCount, KitStatType statType) {
        if (item == null || player == null || !player.isOnline()) return;

        String setName = getArmorSetName(item, creatorGUI);
        String pKey = (pieceCount == 4) ? "set4_stats" : "set2_stats";
        Map<KitStatType, Double> pieceStats = parseArmorStats(item, pKey);
        Double currentVal = pieceStats.get(statType);

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Stat: <aqua>").append(statType.getDisplayName()).append("</aqua></gray>\n");
        desc.append("<gray>Nilai Saat Ini: ").append(currentVal != null ? "<green>" + statType.formatValue(currentVal) + "</green>" : "<dark_gray>Nonaktif</dark_gray>").append("</gray>\n");
        desc.append("<dark_gray>Pilih salah satu preset nilai di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();

        if (statType == KitStatType.EXTRA_MAX_HEALTH) {
            double[] presets = {2.0, 4.0, 6.0, 10.0, 20.0};
            String[] names = {"+2 HP (1 Heart)", "+4 HP (2 Hearts)", "+6 HP (3 Hearts)", "+10 HP (5 Hearts)", "+20 HP (10 Hearts)"};
            for (int i = 0; i < presets.length; i++) {
                double val = presets[i];
                String name = names[i];
                buttons.add(new DialogButtonData("<gold><bold>" + name + "</bold></gold>", "Terapkan " + name, () -> {
                    pieceStats.put(statType, val);
                    saveArmorPieceStats(item, setName, pieceCount, pieceStats, creatorGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount);
                }));
            }
        } else {
            double[] percentages = {5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0};
            for (double pct : percentages) {
                buttons.add(new DialogButtonData("<gold><bold>+" + (int) pct + "%</bold></gold>", "Terapkan nilai +" + (int) pct + "%", () -> {
                    pieceStats.put(statType, pct);
                    saveArmorPieceStats(item, setName, pieceCount, pieceStats, creatorGUI);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount);
                }));
            }
        }

        // Custom manual input via Dialog / Chat
        buttons.add(new DialogButtonData("<yellow><bold>✏ SET NILAI SENDIRI (MANUAL)</bold></yellow>", "Ketik nilai persentase atau angka sendiri secara bebas", () -> {
            plugin.getItemRenameManager().startSession(
                    player,
                    "Masukkan angka persentase/nilai (contoh: 15 atau 22.5):",
                    rawInput -> {
                        try {
                            double customVal = Double.parseDouble(rawInput.trim().replace("%", ""));
                            if (customVal > 0) {
                                pieceStats.put(statType, customVal);
                                saveArmorPieceStats(item, setName, pieceCount, pieceStats, creatorGUI);
                                player.sendMessage(mm.deserialize("<green>✓ Nilai stat diatur ke: <gold>" + statType.formatValue(customVal) + "</gold>!</green>"));
                            }
                        } catch (Exception e) {
                            player.sendMessage(mm.deserialize("<red>Angka tidak valid! Harap masukkan angka yang benar.</red>"));
                        }
                        openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount);
                    },
                    () -> openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount)
            );
        }));

        if (currentVal != null) {
            buttons.add(new DialogButtonData("<red><bold>❌ NONAKTIFKAN STAT INI</bold></red>", "Hapus efek stat ini", () -> {
                pieceStats.remove(statType);
                saveArmorPieceStats(item, setName, pieceCount, pieceStats, creatorGUI);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI</bold></gray>", "Kembali ke daftar stat",
                () -> openArmorPieceConfig(plugin, player, item, sourceSlot, creatorGUI, pieceCount));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#f1c40f:#e67e22><bold>⚙ ATUR STAT: " + statType.getDisplayName().toUpperCase() + "</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 10. TOOL / WEAPON SET BONUS DIALOG
    // ==========================================================

    public static void openToolBonus(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                     AdminItemCreatorGUI creatorGUI) {
        if (item == null || player == null || !player.isOnline()) return;

        String setName = (creatorGUI != null && !creatorGUI.getGlobalSetName().isBlank()) ? creatorGUI.getGlobalSetName() : "Apexsions";
        String setId = (creatorGUI != null && !creatorGUI.getGlobalSetId().isBlank()) ? creatorGUI.getGlobalSetId() : "apexsions";

        Map<ToolStatType, Double> activeStats = parseToolStats(item);

        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Set Armor Penyesuai: <gold>").append(setName).append("</gold></gray>\n");
        desc.append("<gray>ID Sinergi: <yellow>").append(setId).append("</yellow></gray>\n");
        desc.append("<gray>Bonus Aktif: ").append(activeStats.isEmpty() ? "<dark_gray>Kosong</dark_gray>" : "<green>" + activeStats.size() + " Atribut Aktif</green>").append("</gray>\n");
        desc.append("<dark_gray>Klik atribut di bawah untuk mengatur nilainya:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        List<ToolStatType> available = getAvailableStatsForItem(item);

        for (ToolStatType tst : available) {
            Double val = activeStats.get(tst);
            String valStr = (val != null && val > 0) ? "<green>" + tst.formatValue(val) + "</green>" : "<dark_gray>Nonaktif</dark_gray>";
            String label = "<aqua>● " + tst.getDisplayName() + ": " + valStr + "</aqua>";
            String tooltip = "Klik untuk atur nilai " + tst.getDisplayName();

            buttons.add(new DialogButtonData(label, tooltip, () -> {
                // Open Tool stat preset
                openToolStatPresets(plugin, player, item, sourceSlot, creatorGUI, tst, activeStats, setId, setName);
            }));
        }

        if (!activeStats.isEmpty()) {
            buttons.add(new DialogButtonData("<red><bold>✖ HAPUS SELURUH BONUS TOOL</bold></red>", "Hapus seluruh atribut bonus dari senjata/alat ini", () -> {
                removeToolBonusFromItem(item);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ SELESAI & KEMBALI KE EDIT ITEM</bold></gray>", "Simpan dan kembali ke menu edit item",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#3498db:#e67e22><bold>⚔ PENGATURAN TOOL SET BONUS ⚔</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    private static List<ToolStatType> getAvailableStatsForItem(ItemStack is) {
        List<ToolStatType> list = new ArrayList<>();
        if (is == null) return List.of(ToolStatType.values());

        String n = is.getType().name();
        boolean isSwordOrRanged = n.endsWith("_SWORD") || is.getType() == Material.BOW
                || is.getType() == Material.CROSSBOW || is.getType() == Material.TRIDENT || is.getType() == Material.MACE;
        boolean isAxe = n.endsWith("_AXE");

        if (isSwordOrRanged || isAxe) {
            list.add(ToolStatType.WEAPON_DAMAGE_BOOST);
            list.add(ToolStatType.ATTACK_SPEED_BOOST);
            list.add(ToolStatType.CRITICAL_DAMAGE_BOOST);
            list.add(ToolStatType.ATTACK_REACH_BOOST);
            list.add(ToolStatType.UNBREAKABLE_SET);
        }

        if (!isSwordOrRanged) {
            list.add(ToolStatType.MINING_REACH_BOOST);
            list.add(ToolStatType.EXP_MULTIPLIER);
            if (!list.contains(ToolStatType.UNBREAKABLE_SET)) {
                list.add(ToolStatType.UNBREAKABLE_SET);
            }
            list.add(ToolStatType.FATIGUE_IMMUNITY);
        }

        return list;
    }

    private static void openToolStatPresets(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                           AdminItemCreatorGUI creatorGUI, ToolStatType tst, Map<ToolStatType, Double> activeStats,
                                           String setId, String setName) {
        Double curVal = activeStats.get(tst);
        StringBuilder desc = new StringBuilder();
        desc.append("<gray>Atribut: <aqua>").append(tst.getDisplayName()).append("</aqua></gray>\n");
        desc.append("<gray>Nilai Saat Ini: ").append(curVal != null && curVal > 0 ? "<green>" + tst.formatValue(curVal) + "</green>" : "<dark_gray>Nonaktif</dark_gray>").append("</gray>\n");
        desc.append("<dark_gray>Pilih preset nilai di bawah:</dark_gray>");

        List<DialogButtonData> buttons = new ArrayList<>();
        if (tst == ToolStatType.UNBREAKABLE_SET || tst == ToolStatType.FATIGUE_IMMUNITY) {
            boolean isAktif = (curVal != null && curVal > 0.0);
            String toggleLabel = isAktif ? "<green><bold>STATUS: AKTIF [KLIK UNTUK MATIKAN]</bold></green>" : "<red><bold>STATUS: NONAKTIF [KLIK UNTUK AKTIFKAN]</bold></red>";
            buttons.add(new DialogButtonData(toggleLabel, "Klik untuk toggle status aktif/nonaktif", () -> {
                if (isAktif) {
                    activeStats.remove(tst);
                } else {
                    activeStats.put(tst, 1.0);
                }
                saveToolBonusToItem(item, setId, setName, activeStats);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
            }));
        } else {
            double[] options = switch (tst) {
                case ATTACK_REACH_BOOST, MINING_REACH_BOOST -> new double[]{1.0, 2.0};
                case EXP_MULTIPLIER -> new double[]{25.0, 50.0, 75.0, 100.0};
                default -> new double[]{10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0};
            };
            for (double p : options) {
                buttons.add(new DialogButtonData("<gold><bold>" + tst.formatValue(p) + "</bold></gold>", "Terapkan nilai " + tst.formatValue(p), () -> {
                    activeStats.put(tst, p);
                    saveToolBonusToItem(item, setId, setName, activeStats);
                    if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
                }));
            }

            buttons.add(new DialogButtonData("<yellow><bold>✏ KETIK NILAI MANUAL</bold></yellow>", "Ketik nilai numerik secara spesifik", () -> {
                plugin.getItemRenameManager().startSession(
                        player,
                        "Masukkan angka untuk " + tst.getDisplayName() + " (contoh: 15.5):",
                        raw -> {
                            try {
                                double val = Double.parseDouble(raw.trim().replace("%", ""));
                                if (val > 0) {
                                    activeStats.put(tst, val);
                                    saveToolBonusToItem(item, setId, setName, activeStats);
                                    if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                                    player.sendMessage(mm.deserialize("<green>✓ Nilai diatur ke: <gold>" + tst.formatValue(val) + "</gold>!</green>"));
                                }
                            } catch (Exception ignored) {}
                            openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
                        },
                        () -> openToolBonus(plugin, player, item, sourceSlot, creatorGUI)
                );
            }));
        }

        if (curVal != null && curVal > 0) {
            buttons.add(new DialogButtonData("<red><bold>❌ NONAKTIFKAN ATRIBUT INI</bold></red>", "Hapus atribut ini", () -> {
                activeStats.remove(tst);
                saveToolBonusToItem(item, setId, setName, activeStats);
                if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, item);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                openToolBonus(plugin, player, item, sourceSlot, creatorGUI);
            }));
        }

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ KEMBALI</bold></gray>", "Kembali ke pengaturan tool bonus",
                () -> openToolBonus(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<gradient:#f1c40f:#e67e22><bold>⚙ ATUR: " + tst.getDisplayName().toUpperCase() + "</bold></gradient>",
                desc.toString(),
                buttons,
                exitBtn,
                2
        );
    }

    // ==========================================================
    // 11. RESET ALL CONFIRMATION DIALOG
    // ==========================================================

    public static void openResetConfirm(ApexsionsCustomEnchantsPlugin plugin, Player player, ItemStack item, int sourceSlot,
                                        AdminItemCreatorGUI creatorGUI) {
        if (item == null || player == null || !player.isOnline()) return;

        String desc = "<gray>Apakah kamu yakin ingin menghapus <red><bold>SELURUH</bold></red> custom enchants dan vanilla enchants dari item ini?</gray>";

        List<DialogButtonData> buttons = new ArrayList<>();
        buttons.add(new DialogButtonData("<red><bold>✔ YA, RESET SELURUH ENCHANT</bold></red>", "Hapus seluruh sihir dari item ini sekarang", () -> {
            ItemStack current = item;
            for (CustomEnchant ce : plugin.getEnchantmentRegistry().getAllEnchantments()) {
                current = plugin.getEnchantmentRegistry().removeEnchant(current, ce);
            }
            for (Enchantment ve : new ArrayList<>(current.getEnchantments().keySet())) {
                current.removeEnchantment(ve);
            }
            ItemStack finalItem = plugin.getEnchantmentRegistry().updateLoreAndGlint(current);
            if (creatorGUI != null) creatorGUI.updateItem(sourceSlot, finalItem);
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<yellow>Seluruh enchantment berhasil dihapus dari item!</yellow>"));
            openRoot(plugin, player, finalItem, sourceSlot, creatorGUI);
        }));

        DialogButtonData exitBtn = new DialogButtonData("<gray><bold>⬅ BATALKAN & KEMBALI</bold></gray>", "Batalkan dan kembali ke menu edit item",
                () -> openRoot(plugin, player, item, sourceSlot, creatorGUI));

        NativeDialogAdapter.showMultiActionDialog(
                plugin,
                player,
                item,
                "<red><bold>🗑 KONFIRMASI RESET ENCHANT</bold></red>",
                desc,
                buttons,
                exitBtn,
                1
        );
    }

    // ==========================================================
    // HELPER METHODS: VANILLA FORMATTING & COMPATIBILITY
    // ==========================================================

    public static String getVanillaName(Enchantment ench) {
        if (ench == null) return "Unknown";
        String key = ench.getKey().getKey();
        return switch (key.toLowerCase()) {
            case "sharpness" -> "Sharpness";
            case "smite" -> "Smite";
            case "bane_of_arthropods" -> "Bane of Arthropods";
            case "knockback" -> "Knockback";
            case "fire_aspect" -> "Fire Aspect";
            case "looting" -> "Looting";
            case "sweeping_edge", "sweeping" -> "Sweeping Edge";
            case "unbreaking" -> "Unbreaking";
            case "mending" -> "Mending";
            case "efficiency" -> "Efficiency";
            case "silk_touch" -> "Silk Touch";
            case "fortune" -> "Fortune";
            case "power" -> "Power";
            case "flame" -> "Flame";
            case "punch" -> "Punch";
            case "infinity" -> "Infinity";
            case "protection" -> "Protection";
            case "fire_protection" -> "Fire Protection";
            case "feather_falling" -> "Feather Falling";
            case "blast_protection" -> "Blast Protection";
            case "projectile_protection" -> "Projectile Protection";
            case "respiration" -> "Respiration";
            case "aqua_affinity" -> "Aqua Affinity";
            case "thorns" -> "Thorns";
            case "depth_strider" -> "Depth Strider";
            case "frost_walker" -> "Frost Walker";
            case "soul_speed" -> "Soul Speed";
            case "swift_sneak" -> "Swift Sneak";
            case "loyalty" -> "Loyalty";
            case "impaling" -> "Impaling";
            case "riptide" -> "Riptide";
            case "channeling" -> "Channeling";
            case "multishot" -> "Multishot";
            case "quick_charge" -> "Quick Charge";
            case "piercing" -> "Piercing";
            case "density" -> "Density";
            case "breach" -> "Breach";
            case "wind_burst" -> "Wind Burst";
            default -> {
                String[] parts = key.split("_");
                StringBuilder sb = new StringBuilder();
                for (String p : parts) {
                    if (!sb.isEmpty()) sb.append(" ");
                    if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase());
                }
                yield sb.toString();
            }
        };
    }

    public static List<Enchantment> getApplicableVanillaEnchants(ItemStack item) {
        List<Enchantment> list = new ArrayList<>();
        for (Enchantment e : Registry.ENCHANTMENT) {
            if (!e.getKey().getNamespace().equalsIgnoreCase("minecraft")) continue;
            boolean compatible = e.canEnchantItem(item);
            if (!compatible && item != null) {
                Material m = item.getType();
                String k = e.getKey().getKey();
                if (m == Material.ELYTRA && (k.equals("unbreaking") || k.equals("mending"))) {
                    compatible = true;
                } else if (m == Material.SHEARS && (k.equals("efficiency") || k.equals("unbreaking") || k.equals("silk_touch") || k.equals("mending"))) {
                    compatible = true;
                } else if (m == Material.SHIELD && (k.equals("unbreaking") || k.equals("mending"))) {
                    compatible = true;
                }
            }
            if (compatible) list.add(e);
        }
        list.sort(Comparator.comparing(ItemEditDialogFlow::getVanillaName));
        return list;
    }

    // ==========================================================
    // HELPER METHODS: PERSISTENCE & LORE SERIALIZATION
    // ==========================================================

    private static String getArmorSetName(ItemStack item, AdminItemCreatorGUI creatorGUI) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String s = meta.getPersistentDataContainer().get(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING);
                if (s != null && !s.isBlank()) return s;
            }
        }
        return (creatorGUI != null) ? creatorGUI.getEffectiveSetName() : "Apexsions";
    }

    private static Map<KitStatType, Double> parseArmorStats(ItemStack item, String key) {
        Map<KitStatType, Double> map = new LinkedHashMap<>();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String raw = meta.getPersistentDataContainer().get(new NamespacedKey("apexsions", key), PersistentDataType.STRING);
                if (raw != null && !raw.isBlank()) {
                    for (String part : raw.split(";")) {
                        String[] kv = part.split(":");
                        if (kv.length == 2) {
                            try {
                                KitStatType st = KitStatType.valueOf(kv[0].trim());
                                double val = Double.parseDouble(kv[1].trim());
                                map.put(st, val);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return map;
    }

    private static void saveArmorPieceStats(ItemStack item, String setName, int pieceCount, Map<KitStatType, Double> pieceStats,
                                            AdminItemCreatorGUI creatorGUI) {
        Map<KitStatType, Double> set2 = (pieceCount == 2) ? pieceStats : parseArmorStats(item, "set2_stats");
        Map<KitStatType, Double> set4 = (pieceCount == 4) ? pieceStats : parseArmorStats(item, "set4_stats");
        saveArmorSetBonusToItem(item, setName, set2, set4);
        if (creatorGUI != null) {
            if (pieceCount == 2) {
                creatorGUI.getGlobalSet2Stats().clear();
                creatorGUI.getGlobalSet2Stats().putAll(pieceStats);
            } else if (pieceCount == 4) {
                creatorGUI.getGlobalSet4Stats().clear();
                creatorGUI.getGlobalSet4Stats().putAll(pieceStats);
            }
            creatorGUI.setSetBonusConfigured(!creatorGUI.getGlobalSet2Stats().isEmpty() || !creatorGUI.getGlobalSet4Stats().isEmpty());
            creatorGUI.checkAndApplyFullsetBonus();
        }
    }

    private static void saveArmorSetBonusToItem(ItemStack item, String setName, Map<KitStatType, Double> set2Stats, Map<KitStatType, Double> set4Stats) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String cleanName = (setName != null && !setName.isBlank()) ? setName : "Apexsions";
        String plain = AdminItemCreatorGUI.getPlainTextSafe(cleanName);
        String setId = plain.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        if (setId.isBlank()) setId = "apexsions";

        pdc.set(new NamespacedKey("apexsions", "set_id"), PersistentDataType.STRING, setId);
        pdc.set(new NamespacedKey("apexsions", "set_name"), PersistentDataType.STRING, cleanName);

        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : set2Stats.entrySet()) {
            if (!sb2.isEmpty()) sb2.append(";");
            sb2.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set2_stats"), PersistentDataType.STRING, sb2.toString());

        StringBuilder sb4 = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : set4Stats.entrySet()) {
            if (!sb4.isEmpty()) sb4.append(";");
            sb4.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set4_stats"), PersistentDataType.STRING, sb4.toString());

        Map<KitStatType, Double> legacyMap = !set4Stats.isEmpty() ? set4Stats : set2Stats;
        StringBuilder sbLegacy = new StringBuilder();
        for (Map.Entry<KitStatType, Double> e : legacyMap.entrySet()) {
            if (!sbLegacy.isEmpty()) sbLegacy.append(";");
            sbLegacy.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "set_stats"), PersistentDataType.STRING, sbLegacy.toString());
        pdc.set(new NamespacedKey("apexsions", "set_req"), PersistentDataType.INTEGER, !set4Stats.isEmpty() ? 4 : 2);

        AdminItemCreatorGUI.cleanSetBonusLore(meta);

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        String displayHeader = plain.toUpperCase();
        if (displayHeader.isBlank()) displayHeader = "APEXSIONS";
        lore.add(mm.deserialize("<gold><bold>★ SET BONUS: <yellow>" + displayHeader + "</yellow> ★</bold></gold>"));
        if (!set2Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <yellow>2 Pieces (Half Set)</yellow></gray>"));
            for (Map.Entry<KitStatType, Double> e : set2Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }
        if (!set4Stats.isEmpty()) {
            lore.add(mm.deserialize("<gray>Syarat: <yellow>4 Pieces (Full Set)</yellow></gray>"));
            for (Map.Entry<KitStatType, Double> e : set4Stats.entrySet()) {
                lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
            }
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private static void removeArmorSetBonusFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(new NamespacedKey("apexsions", "set_id"));
        pdc.remove(new NamespacedKey("apexsions", "set_name"));
        pdc.remove(new NamespacedKey("apexsions", "set_req"));
        pdc.remove(new NamespacedKey("apexsions", "set_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set2_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set4_stats"));
        pdc.remove(new NamespacedKey("apexsions", "set_type"));
        pdc.remove(new NamespacedKey("apexsions", "set_val"));
        AdminItemCreatorGUI.cleanSetBonusLore(meta);
        item.setItemMeta(meta);
    }

    private static Map<ToolStatType, Double> parseToolStats(ItemStack item) {
        Map<ToolStatType, Double> map = new LinkedHashMap<>();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String raw = meta.getPersistentDataContainer().get(new NamespacedKey("apexsions", "tool_stats"), PersistentDataType.STRING);
                if (raw != null && !raw.isBlank()) {
                    for (String p : raw.split(";")) {
                        String[] kv = p.split(":");
                        if (kv.length == 2) {
                            try {
                                ToolStatType st = ToolStatType.valueOf(kv[0].trim());
                                double val = Double.parseDouble(kv[1].trim());
                                map.put(st, val);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        }
        return map;
    }

    private static void saveToolBonusToItem(ItemStack item, String setId, String setName, Map<ToolStatType, Double> activeStats) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("apexsions", "tool_bonus"), PersistentDataType.STRING, (setId != null && !setId.isBlank()) ? setId : "apexsions");

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ToolStatType, Double> e : activeStats.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(e.getKey().name()).append(":").append(e.getValue());
        }
        pdc.set(new NamespacedKey("apexsions", "tool_stats"), PersistentDataType.STRING, sb.toString());

        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("TOOL SET BONUS") || plain.contains("WEAPON SET BONUS") || plain.contains("Syarat: Memakai Set Armor");
        });

        lore.add(Component.empty());
        String headerTitle = AdminItemCreatorGUI.isWeapon(item) ? "WEAPON SET BONUS" : "TOOL SET BONUS";
        lore.add(mm.deserialize("<gradient:#e74c3c:#f39c12><bold>★ " + headerTitle + (setName.isBlank() ? "" : ": " + setName.toUpperCase()) + " ★</bold></gradient>"));
        lore.add(mm.deserialize("<gray>Syarat: <gold>Memakai Set Armor " + (setName.isBlank() ? "Terkait" : setName) + "</gold></gray>"));
        for (Map.Entry<ToolStatType, Double> e : activeStats.entrySet()) {
            lore.add(mm.deserialize("<gray>  ● Efek: <aqua>" + e.getKey().getDisplayName() + " " + e.getKey().formatValue(e.getValue()) + "</aqua></gray>"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private static void removeToolBonusFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.remove(new NamespacedKey("apexsions", "tool_bonus"));
        pdc.remove(new NamespacedKey("apexsions", "tool_stats"));
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> {
            String plain = mm.serialize(c);
            return plain.contains("TOOL SET BONUS") || plain.contains("WEAPON SET BONUS") || plain.contains("Syarat: Memakai Set Armor");
        });
        meta.lore(lore);
        item.setItemMeta(meta);
    }
}
