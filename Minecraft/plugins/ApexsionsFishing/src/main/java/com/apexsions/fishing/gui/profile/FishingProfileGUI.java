package com.apexsions.fishing.gui.profile;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.gui.FishSellGUI;
import com.apexsions.fishing.gui.FishingVaultGUI;
import com.apexsions.fishing.gui.RodShopGUI;
import com.apexsions.fishing.model.PlayerFishingStats;
import com.apexsions.fishing.model.PlayerVaultData;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Player Fishing Profile & Hub GUI.
 * Shows player fishing statistics and acts as the central launcher for Vault, Journal, Top Leaderboard, Shop, and Sell.
 */
public class FishingProfileGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public static final int SLOT_STATS = 13;
    public static final int SLOT_VAULT = 20;
    public static final int SLOT_JOURNAL = 21;
    public static final int SLOT_LEADERBOARD = 22;
    public static final int SLOT_ROD_SHOP = 23;
    public static final int SLOT_SELL = 24;
    public static final int SLOT_CLOSE = 31;

    public FishingProfileGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<aqua><bold>Profil Memancing</bold></aqua> <dark_gray>●</dark_gray> <yellow>" + player.getName() + "</yellow>"));
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

        // Dark aqua & black glass panes
        ItemStack bgGlass = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, bgGlass);
            }
        }

        PlayerFishingStats stats = plugin.getVaultStorageManager().getStats(player.getUniqueId());
        PlayerVaultData vaultData = plugin.getVaultStorageManager().getVaultData(player.getUniqueId());

        // Central Slot 13: Player Skull with Stats
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sMeta = (SkullMeta) skull.getItemMeta();
        if (sMeta != null) {
            sMeta.setOwningPlayer(player);
            sMeta.displayName(mm.deserialize("<gradient:#ffaa00:#ffd700><bold>✦ REKOR PEMANCING: " + player.getName().toUpperCase() + " ✦</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<dark_gray>Statistik resmi pemancingan Apexsions</dark_gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<yellow>● Total Ikan Ditangkap:</yellow> <white><bold>" + String.format("%,d", stats.getTotalFishCaught()) + " Ekor</bold></white>"));
            lore.add(mm.deserialize("<yellow>● Total Bobot Ditimbang:</yellow> <gold><bold>" + String.format("%.2f", stats.getTotalWeightCaught()) + " kg</bold></gold>"));
            lore.add(mm.deserialize("<yellow>● Tangkapan Terberat (PB):</yellow> <aqua><bold>" + String.format("%.2f", stats.getHeaviestFishWeight()) + " kg</bold></aqua> <gray>(" + stats.getHeaviestFishName() + ")</gray>"));
            lore.add(mm.deserialize("<yellow>● Tangkapan Rahasia (SECRET):</yellow> <light_purple><bold>" + stats.getSecretCatches() + " Spesies</bold></light_purple>"));
            lore.add(mm.deserialize("<yellow>● Kapasitas Brankas:</yellow> <green><bold>" + vaultData.getUnlockedPages() + " / 30 Halaman</bold></green>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<gradient:#00c6ff:#0072ff>\"Setiap tarikan kail membawa legenda baru di Apexsions.\"</gradient>"));
            sMeta.lore(lore);
            skull.setItemMeta(sMeta);
        }
        inventory.setItem(SLOT_STATS, skull);

        // Slot 20: Buka Brankas Mancing
        inventory.setItem(SLOT_VAULT, createGuiItem(Material.CHEST,
                "<gold><bold>📦 Brankas Penyimpanan Mancing</bold></gold>",
                List.of(
                        "<gray>Akses brankas khusus hasil memancing,</gray>",
                        "<gray>lengkap dengan sistem multi-halaman!</gray>",
                        "",
                        "<yellow>▶ Klik untuk membuka brankas</yellow>"
                )));

        // Slot 21: Jurnal Ikan (Fish-o-pedia)
        inventory.setItem(SLOT_JOURNAL, createGuiItem(Material.WRITABLE_BOOK,
                "<dark_aqua><bold>📖 Jurnal Ikan (Fish-o-pedia)</bold></dark_aqua>",
                List.of(
                        "<gray>Lihat seluruh katalog spesies ikan,</gray>",
                        "<gray>pantau rekor pribadi dan temukan ikan rahasia!</gray>",
                        "",
                        "<yellow>▶ Klik untuk membuka jurnal</yellow>"
                )));

        // Slot 22: Papan Peringkat (Leaderboard)
        inventory.setItem(SLOT_LEADERBOARD, createGuiItem(Material.GOLDEN_HELMET,
                "<gradient:#f1c40f:#e67e22><bold>🏆 Papan Peringkat Nelayan</bold></gradient>",
                List.of(
                        "<gray>Cek daftar 10 pemancing terhebat</gray>",
                        "<gray>berdasarkan bobot terberat & tangkapan terbanyak!</gray>",
                        "",
                        "<yellow>▶ Klik untuk cek leaderboard</yellow>"
                )));

        // Slot 23: Toko Pancingan Resmi
        inventory.setItem(SLOT_ROD_SHOP, createGuiItem(Material.FISHING_ROD,
                "<gradient:#00c6ff:#0072ff><bold>🎣 Toko Pancingan Auto-Catch</bold></gradient>",
                List.of(
                        "<gray>Beli pancingan canggih auto-catch</gray>",
                        "<gray>dan pancingan keberuntungan tinggi!</gray>",
                        "",
                        "<yellow>▶ Klik untuk buka toko pancingan</yellow>"
                )));

        // Slot 24: Jual Ikan ke Nelayan
        inventory.setItem(SLOT_SELL, createGuiItem(Material.RAW_GOLD,
                "<gradient:#ffd700:#ffa500><bold>⚖ Jual Hasil Tangkapan</bold></gradient>",
                List.of(
                        "<gray>Tukarkan ikan segarmu menjadi Rupiah</gray>",
                        "<gray>dengan harga dinamis berdasarkan bobot!</gray>",
                        "",
                        "<yellow>▶ Klik untuk buka meja jual</yellow>"
                )));

        // Slot 31: Close
        inventory.setItem(SLOT_CLOSE, createGuiItem(Material.BARRIER,
                "<red><bold>✖ Tutup Menu</bold></red>",
                List.of("<gray>Klik untuk keluar.</gray>")));
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        switch (slot) {
            case SLOT_VAULT -> {
                new FishingVaultGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_JOURNAL -> {
                new FishingJournalGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            }
            case SLOT_LEADERBOARD -> {
                new FishingLeaderboardGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_ROD_SHOP -> {
                new RodShopGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_SELL -> {
                new FishSellGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_CLOSE -> {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
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
