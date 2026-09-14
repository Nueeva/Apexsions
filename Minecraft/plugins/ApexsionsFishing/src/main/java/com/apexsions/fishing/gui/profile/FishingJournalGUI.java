package com.apexsions.fishing.gui.profile;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishingLootItem;
import com.apexsions.fishing.model.PlayerFishingStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fish-o-pedia / Fishing Journal GUI.
 * Shows player's discovery progression across all fish species in the realm.
 */
public class FishingJournalGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public static final int SLOT_HEADER = 4;
    public static final int SLOT_BACK = 49;

    public FishingJournalGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<dark_aqua><bold>Jurnal Tangkapan Ikan (Fish-o-pedia)</bold></dark_aqua>"));
        render();
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void render() {
        inventory.clear();

        // Border
        ItemStack borderGlass = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderGlass);
            }
        }

        PlayerFishingStats stats = plugin.getVaultStorageManager().getStats(player.getUniqueId());
        List<FishingLootItem> speciesList = plugin.getLootGenerator().getAllFishSpecies();

        int discoveredCount = 0;
        for (FishingLootItem item : speciesList) {
            if (stats.getPersonalBestPerSpecies().containsKey(item.getId().toLowerCase())) {
                discoveredCount++;
            }
        }

        int totalSpecies = speciesList.size();
        int progressPct = totalSpecies > 0 ? (int) Math.round((double) discoveredCount / totalSpecies * 100.0) : 0;

        // Header slot 4
        inventory.setItem(SLOT_HEADER, createGuiItem(Material.WRITABLE_BOOK,
                "<gradient:#00f2fe:#4facfe><bold>Koleksi Spesies Ikan Apexsions</bold></gradient>",
                List.of(
                        "<gray>Progres Penemuan: <gold><bold>" + discoveredCount + " / " + totalSpecies + " Spesies (" + progressPct + "%)</bold></gold></gray>",
                        "",
                        "<dark_gray>Tangkap semua spesies legendaris dan rahasia</dark_gray>",
                        "<dark_gray>untuk melengkapi seluruh ensiklopedia laut!</dark_gray>"
                )));

        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < speciesList.size(); i++) {
            if (i >= contentSlots.length) break;
            int slot = contentSlots[i];
            FishingLootItem species = speciesList.get(i);
            boolean discovered = stats.getPersonalBestPerSpecies().containsKey(species.getId().toLowerCase());

            if (discovered) {
                double pb = stats.getPersonalBestPerSpecies().getOrDefault(species.getId().toLowerCase(), 0.0);
                ItemStack item = new ItemStack(species.getMaterial());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(mm.deserialize(species.getDisplayName()));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(mm.deserialize("<yellow>✦ Tingkat:</yellow> ").append(species.getRarity().getFormattedComponent()));
                    lore.add(mm.deserialize("<green>✔ Status: SUDAH DITEMUKAN</green>"));
                    lore.add(mm.deserialize("<yellow>● Rekor Bobot Terbaik (PB):</yellow> <gold><bold>" + String.format("%.2f", pb) + " kg</bold></gold>"));
                    lore.add(mm.deserialize("<yellow>● Kisaran Bobot Alami:</yellow> <gray>" + species.getMinWeight() + " - " + species.getMaxWeight() + " kg</gray>"));
                    lore.add(mm.deserialize("<yellow>● Harga Pokok:</yellow> <gold>Rp " + String.format("%,d", (long) species.getBasePrice()) + "</gold>"));
                    if (!species.getDescription().isEmpty()) {
                        lore.add(Component.empty());
                        lore.add(mm.deserialize("<dark_gray>\"" + species.getDescription() + "\"</dark_gray>"));
                    }
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot, item);
            } else {
                // Not discovered
                inventory.setItem(slot, createGuiItem(Material.GRAY_DYE,
                        "<dark_gray><bold>??? (Belum Ditemukan)</bold></dark_gray>",
                        List.of(
                                "",
                                "<yellow>✦ Tingkat:</yellow> " + species.getRarity().getFormatTag() + species.getRarity().getName() + (species.getRarity().getFormatTag().startsWith("<") ? "</" + species.getRarity().getFormatTag().substring(1) : ""),
                                "<red>✖ Status: Belum Ditemukan</red>",
                                "",
                                "<dark_gray>Pancinglah di perairan Apexsions</dark_gray>",
                                "<dark_gray>untuk menangkap dan mencatat spesies ini!</dark_gray>"
                        )));
            }
        }

        // Back button at Slot 49
        inventory.setItem(SLOT_BACK, createGuiItem(Material.ARROW,
                "<yellow><bold>◀ Kembali ke Profil</bold></yellow>",
                List.of("<gray>Klik untuk kembali ke profil memancing.</gray>")));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == SLOT_BACK) {
            new FishingProfileGUI(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
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
