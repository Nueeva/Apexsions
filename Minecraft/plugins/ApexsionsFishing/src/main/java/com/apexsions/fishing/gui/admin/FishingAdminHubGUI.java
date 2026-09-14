package com.apexsions.fishing.gui.admin;

import com.apexsions.fishing.ApexsionsFishing;
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
 * Master Admin Hub GUI for ApexsionsFishing (/fish admin).
 */
public class FishingAdminHubGUI implements InventoryHolder {

    private final ApexsionsFishing plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public static final int SLOT_ROD_CREATOR = 11;
    public static final int SLOT_ROD_CATALOG = 12;
    public static final int SLOT_LOOT_TABLE = 13;
    public static final int SLOT_VAULT_PRICES = 14;
    public static final int SLOT_AFK_SETTINGS = 15;
    public static final int SLOT_RELOAD = 22;
    public static final int SLOT_CLOSE = 31;

    public FishingAdminHubGUI(@NotNull ApexsionsFishing plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, mm.deserialize("<dark_red><bold>Master Admin Hub Memancing</bold></dark_red>"));
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
        ItemStack borderGlass = createGuiItem(Material.RED_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderGlass);
            }
        }

        // Slot 11: Dedicated Rod Creator
        inventory.setItem(SLOT_ROD_CREATOR, createGuiItem(Material.FISHING_ROD,
                "<gradient:#00c6ff:#0072ff><bold>🛠 Editor & Pembuat Pancingan</bold></gradient>",
                List.of(
                        "<gray>Rakit pancingan kustom dengan pengaturan:</gray>",
                        "<yellow>● Auto-catch toggle, Luck %, Bobot %</yellow>",
                        "<yellow>● Strike Speed, Syarat Level, Unbreakable</yellow>",
                        "",
                        "<yellow>▶ Klik untuk buka Rod Creator</yellow>"
                )));

        // Slot 12: Rod Catalog Quick Give
        inventory.setItem(SLOT_ROD_CATALOG, createGuiItem(Material.CHEST,
                "<gold><bold>📦 Katalog Pancingan Resmi</bold></gold>",
                List.of(
                        "<gray>Total Pancingan Terdaftar: <yellow><bold>" + plugin.getRodManager().getAllRods().size() + " Jenis</bold></yellow></gray>",
                        "",
                        "<yellow>▶ Klik untuk mengambil pancingan katalog</yellow>"
                )));

        // Slot 13: Loot Table Inspector
        inventory.setItem(SLOT_LOOT_TABLE, createGuiItem(Material.TROPICAL_FISH,
                "<aqua><bold>🐟 Inspektur Tabel Loot Mancing</bold></aqua>",
                List.of(
                        "<gray>Total Spesies Ikan: <yellow>" + plugin.getLootGenerator().getAllFishSpecies().size() + "</yellow></gray>",
                        "<gray>Tingkat Rarity: <gold>Common s/d Secret</gold></gray>",
                        "",
                        "<gray>Dikelola melalui berkas <white>loot.yml</white></gray>"
                )));

        // Slot 14: Vault Price Inspector
        inventory.setItem(SLOT_VAULT_PRICES, createGuiItem(Material.EMERALD,
                "<green><bold>💰 Pengaturan Harga Brankas</bold></green>",
                List.of(
                        "<gray>Maksimal Kapasitas: <aqua>30 Halaman</aqua></gray>",
                        "<gray>Halaman 2-5: <yellow>Rupiah / Diamond</yellow></gray>",
                        "<gray>Halaman 6-30: <blue>Diamond Only</blue></gray>",
                        "",
                        "<gray>Dikelola melalui <white>vault-prices.yml</white></gray>"
                )));

        // Slot 15: Global AFK Fishing Settings
        int defaultDelay = plugin.getConfig().getInt("settings.afk-fishing.default-catch-delay-seconds", 20);
        boolean autoVault = plugin.getConfig().getBoolean("settings.afk-fishing.auto-deposit-to-vault-if-full", true);
        inventory.setItem(SLOT_AFK_SETTINGS, createGuiItem(Material.CLOCK,
                "<light_purple><bold>⚙ Pengaturan AFK Fishing Global</bold></light_purple>",
                List.of(
                        "<gray>Default Strike Speed: <aqua>" + defaultDelay + " Detik</aqua></gray>",
                        "<gray>Auto-Deposit ke Brankas jika Inventory Penuh: " + (autoVault ? "<green>AKTIF</green>" : "<red>NON-AKTIF</red>") + "</gray>",
                        "",
                        "<gray>Dikelola melalui <white>config.yml</white></gray>"
                )));

        // Slot 22: Reload
        inventory.setItem(SLOT_RELOAD, createGuiItem(Material.NETHER_STAR,
                "<gradient:#ffaa00:#ffd700><bold>🔄 Muat Ulang Konfigurasi (Reload)</bold></gradient>",
                List.of(
                        "<gray>Muat ulang config.yml, loot.yml,</gray>",
                        "<gray>rods.yml, dan vault-prices.yml.</gray>",
                        "",
                        "<yellow>▶ Klik untuk reload sekarang</yellow>"
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
            case SLOT_ROD_CREATOR -> {
                new AdminRodCreatorGUI(plugin, player).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            case SLOT_ROD_CATALOG -> {
                // Give standard or lucky rod
                for (var rod : plugin.getRodManager().getAllRods()) {
                    player.getInventory().addItem(plugin.getRodManager().createRod(rod));
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
                player.sendMessage(mm.deserialize("<green>Semua pancingan resmi katalog berhasil diberikan ke inventory Anda!</green>"));
            }
            case SLOT_RELOAD -> {
                plugin.reloadConfig();
                plugin.getLootGenerator().reload();
                plugin.getRodManager().reload();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
                player.sendMessage(mm.deserialize("<green><bold>RELOAD SUKSES!</bold> Seluruh konfigurasi ApexsionsFishing telah diperbarui.</green>"));
                render();
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
