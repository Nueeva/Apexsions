package com.apexsions.core.gui.rank;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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

public class RankListGUI implements InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RankListGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ DAFTAR RANK APEXSIONS ✦</bold></gradient>"));
        buildGUI();
    }

    public void open() {
        buildGUI();
        player.openInventory(inventory);
    }

    public void buildGUI() {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack innerBg = createGlass(Material.GRAY_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack goldDecor = createGlass(Material.YELLOW_STAINED_GLASS_PANE, "<gold>✦</gold>");

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            } else {
                inventory.setItem(i, innerBg);
            }
        }
        inventory.setItem(1, goldDecor);
        inventory.setItem(7, goldDecor);
        inventory.setItem(46, goldDecor);
        inventory.setItem(52, goldDecor);

        String currentRank = plugin.getLuckPermsHook() != null ? plugin.getLuckPermsHook().getPlayerRank(player) : "Wanderer";
        String currentRankDisplay = plugin.getLuckPermsHook() != null ? plugin.getLuckPermsHook().getPlayerRankDisplayName(player) : "Wanderer";

        // Header Slot 4: Overview
        ItemStack header = new ItemStack(Material.NETHER_STAR);
        ItemMeta hMeta = header.getItemMeta();
        if (hMeta != null) {
            hMeta.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 HIERARKI & RANK APEXSIONS 👑</bold></gradient>"));
            hMeta.lore(List.of(
                    mm.deserialize("<gray>Tatanan 5 Tingkat & 11 Kasta Sosial Peradaban.</gray>"),
                    Component.empty(),
                    mm.deserialize("<gray>Rank Anda Saat Ini:</gray> " + currentRankDisplay),
                    mm.deserialize("<gray>Store Resmi:</gray> <yellow>store.apexsions.net</yellow>"),
                    Component.empty(),
                    mm.deserialize("<yellow>Klik rank donatur untuk melihat info toko web!</yellow>")
            ));
            header.setItemMeta(hMeta);
        }
        inventory.setItem(4, header);

        // Row 1: Tingkat V — Puncak Kedaulatan (The Ancestor - Weight 100)
        inventory.setItem(13, createRankCard("ancestor", Material.NETHER_STAR, "<gradient:#8B0000:#FF0000><bold>[👑 ANCESTOR]</bold></gradient>", "100",
                "Tingkat V — Pendiri & Tahta Tertinggi Peradaban",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat V (Puncak Kedaulatan)</gold>",
                        "<gray>Hak Akses:</gray> <red>Full Console & Server Authority</red>",
                        "<gray>Fokus:</gray> <yellow>Pengembangan & Arah Strategis Apexsions</yellow>"
                ), currentRank));

        // Row 2: Tingkat IV — Dewan Otoritas (Architect & Overseer - Weight 95, Kedudukan Setara)
        inventory.setItem(21, createRankCard("architect", Material.AMETHYST_CLUSTER, "<gradient:#8E2DE2:#4A00E0><bold>[📐 ARCHITECT]</bold></gradient>", "95",
                "Tingkat IV — Perancang & Arsitek Realm",
                List.of(
                        "<gray>Tingkat:</gray> <light_purple>Tingkat IV (Dewan Otoritas - Setara)</light_purple>",
                        "<gray>Hak Akses:</gray> <light_purple>Otoritas Cetak Biru & Pembangunan Realm</light_purple>",
                        "<gray>Fokus:</gray> <yellow>Desain Tata Ruang, Estetika & Kedaulatan Kota</yellow>"
                ), currentRank));

        inventory.setItem(23, createRankCard("overseer", Material.ENDER_EYE, "<gradient:#FFD700:#FFA500><bold>[👁 OVERSEER]</bold></gradient>", "95",
                "Tingkat IV — Pengawas Kedaulatan Realm",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat IV (Dewan Otoritas - Setara)</gold>",
                        "<gray>Hak Akses:</gray> <gold>Audit Kedaulatan, Transaksi & Keadilan</gold>",
                        "<gray>Fokus:</gray> <yellow>Stabilitas Ekonomi & Integritas Peradaban</yellow>"
                ), currentRank));

        // Row 3: Tingkat III — Administrasi & Penegak Hukum (Warden & Herald - Weight 90 & 80)
        inventory.setItem(30, createRankCard("warden", Material.SHIELD, "<gradient:#1e3c72:#2a5298><bold>[🛡 WARDEN]</bold></gradient>", "90",
                "Tingkat III — Head Staff & Administrator",
                List.of(
                        "<gray>Tingkat:</gray> <aqua>Tingkat III (Administrasi & Penegak Hukum)</aqua>",
                        "<gray>Hak Akses:</gray> <aqua>Master Admin Panel & Manajemen Server</aqua>",
                        "<gray>Fokus:</gray> <yellow>Penegakan Aturan & Pengawasan Sistem</yellow>"
                ), currentRank));

        inventory.setItem(32, createRankCard("herald", Material.WRITABLE_BOOK, "<gradient:#f857a6:#ff5858><bold>[📜 HERALD]</bold></gradient>", "80",
                "Tingkat III — Staff & Moderator / Helper",
                List.of(
                        "<gray>Tingkat:</gray> <light_purple>Tingkat III (Administrasi & Penegak Hukum)</light_purple>",
                        "<gray>Hak Akses:</gray> <light_purple>Moderasi Chat, Laporan Tiket, & Event</light_purple>",
                        "<gray>Fokus:</gray> <yellow>Membantu Warga & Menjaga Kenyamanan Realm</yellow>"
                ), currentRank));

        // Row 4: Tingkat II — Ordo Bangsawan (Sions to Ascendant - Weight 70 to 30)
        inventory.setItem(38, createRankCard("sions", Material.BEACON, "<gradient:#00FFFF:#FFD700><bold>[✦ SIONS ✦]</bold></gradient>", "70",
                "Tingkat II — Apex Donator (Sultan)",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat II (Ordo Bangsawan)</gold>",
                        "<green>✔</green> <yellow>Slot Auction House: 20 Barang</yellow>",
                        "<green>✔</green> <yellow>Bonus XP & Multi-Currency: +50% Boost</yellow>",
                        "<green>✔</green> <yellow>Akses Title Eksklusif & Partikel Cahaya Dewa</yellow>",
                        "<green>✔</green> <yellow>RTP Cooldown: Instan (0 Detik)</yellow>",
                        "<green>✔</green> <yellow>Akses /fly di Wilayah Kerajaan Sendiri</yellow>"
                ), currentRank));

        inventory.setItem(39, createRankCard("emperor", Material.NETHERITE_SWORD, "<gradient:#e52d27:#b31217><bold>[⚔ EMPEROR]</bold></gradient>", "60",
                "Tingkat II — Donator Tier 4",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat II (Ordo Bangsawan)</gold>",
                        "<green>✔</green> <yellow>Slot Auction House: 15 Barang</yellow>",
                        "<green>✔</green> <yellow>Bonus XP & Multi-Currency: +35% Boost</yellow>",
                        "<green>✔</green> <yellow>Akses Warna Chat Kustom & Glow Partikel</yellow>",
                        "<green>✔</green> <yellow>RTP Cooldown: 10 Detik</yellow>"
                ), currentRank));

        inventory.setItem(40, createRankCard("sovereign", Material.GOLD_BLOCK, "<gradient:#f39c12:#f1c40f><bold>[⚜ SOVEREIGN]</bold></gradient>", "50",
                "Tingkat II — Donator Tier 3",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat II (Ordo Bangsawan)</gold>",
                        "<green>✔</green> <yellow>Slot Auction House: 10 Barang</yellow>",
                        "<green>✔</green> <yellow>Bonus XP & Multi-Currency: +25% Boost</yellow>",
                        "<green>✔</green> <yellow>Akses Emote & Animasi Rank Mewah</yellow>",
                        "<green>✔</green> <yellow>RTP Cooldown: 20 Detik</yellow>"
                ), currentRank));

        inventory.setItem(41, createRankCard("archon", Material.DIAMOND, "<gradient:#00c6ff:#0072ff><bold>[💎 ARCHON]</bold></gradient>", "40",
                "Tingkat II — Donator Tier 2",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat II (Ordo Bangsawan)</gold>",
                        "<green>✔</green> <yellow>Slot Auction House: 8 Barang</yellow>",
                        "<green>✔</green> <yellow>Bonus XP & Multi-Currency: +15% Boost</yellow>",
                        "<green>✔</green> <yellow>RTP Cooldown: 30 Detik</yellow>"
                ), currentRank));

        inventory.setItem(42, createRankCard("ascendant", Material.EMERALD, "<gradient:#11998e:#38ef7d><bold>[☘ ASCENDANT]</bold></gradient>", "30",
                "Tingkat II — Donator Tier 1",
                List.of(
                        "<gray>Tingkat:</gray> <gold>Tingkat II (Ordo Bangsawan)</gold>",
                        "<green>✔</green> <yellow>Slot Auction House: 6 Barang</yellow>",
                        "<green>✔</green> <yellow>Bonus XP & Multi-Currency: +10% Boost</yellow>",
                        "<green>✔</green> <yellow>RTP Cooldown: 45 Detik</yellow>"
                ), currentRank));

        // Row 5: Tingkat I — Fondasi Peradaban (Wanderer - Weight 10), Close Button, & Panduan
        inventory.setItem(47, createRankCard("wanderer", Material.COMPASS, "<gradient:#bdc3c7:#7f8c8d>[Wanderer]</gradient>", "10",
                "Tingkat I — Fondasi Peradaban (Warga Baru)",
                List.of(
                        "<gray>Tingkat:</gray> <gray>Tingkat I (Fondasi Peradaban)</gray>",
                        "<green>✔</green> <gray>Akses Penuh Seluruh Fitur Kerajaan & Quest</gray>",
                        "<green>✔</green> <gray>Slot Auction House: 4 Barang</gray>",
                        "<green>✔</green> <gray>RTP Cooldown Standar: 60 Detik</gray>"
                ), currentRank));

        // Close Button (Slot 49)
        ItemStack closeBtn = createActionItem(Material.BARRIER, "<red><bold>✖ TUTUP MENU</bold></red>", List.of("<gray>Klik untuk menutup menu rank.</gray>"));
        inventory.setItem(49, closeBtn);

        // Panduan Kasta (Slot 51)
        ItemStack guideBtn = createActionItem(Material.BOOK, "<gradient:#f1c40f:#e67e22><bold>📖 PANDUAN 5 TINGKAT KASTA</bold></gradient>", List.of(
                "<gray>Klik untuk mencetak ringkasan</gray>",
                "<gray>struktur 5 tingkat kasta ke chat.</gray>"
        ));
        inventory.setItem(51, guideBtn);
    }

    private ItemStack createRankCard(String rankKey, Material icon, String title, String weight, String subtitle, List<String> perks, String playerRank) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(title));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Jabatan / Peran:</gray> <white>" + subtitle + "</white>"));
            lore.add(mm.deserialize("<gray>Bobot Hierarki:</gray> <yellow>" + weight + "</yellow>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<gold><bold>Keuntungan & Hak Istimewa:</bold></gold>"));
            for (String perk : perks) {
                lore.add(mm.deserialize(" " + perk));
            }
            lore.add(Component.empty());

            boolean isCurrent = playerRank.equalsIgnoreCase(rankKey) || (rankKey.equalsIgnoreCase("wanderer") && playerRank.equalsIgnoreCase("default"));
            if (isCurrent) {
                lore.add(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>● RANK ANDA SAAT INI ●</bold></gradient>"));
                meta.setEnchantmentGlintOverride(true);
            } else if (isDonatorRank(rankKey)) {
                lore.add(mm.deserialize("<yellow>▶ Klik untuk buka info store donasi!</yellow>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isDonatorRank(String rankKey) {
        return switch (rankKey.toLowerCase()) {
            case "sions", "emperor", "sovereign", "archon", "ascendant" -> true;
            default -> false;
        };
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);

        if (slot >= 38 && slot <= 42) {
            player.closeInventory();
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
            player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>🛒 PORTAL TOKO RESMI APEXSIONS 🛒</bold></gradient>"));
            player.sendMessage(mm.deserialize("<gray>Dukung server dan dapatkan rank donatur eksklusif di:</gray>"));
            player.sendMessage(mm.deserialize("<yellow><bold><click:open_url:'https://store.apexsions.net'><hover:show_text:'<green>Klik untuk membuka store.apexsions.net</green>'>▶ https://store.apexsions.net ◀</click></bold></yellow>"));
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
            return;
        }

        if (slot == 51) {
            player.closeInventory();
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
            player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>📖 TATANAN 5 TINGKAT KASTA APEXSIONS 📖</bold></gradient>"));
            player.sendMessage(mm.deserialize("<gold>● Tingkat V (Puncak Kedaulatan):</gold> <white>The Ancestor (Weight 100)</white>"));
            player.sendMessage(mm.deserialize("<light_purple>● Tingkat IV (Dewan Otoritas):</light_purple> <white>Architect & Overseer (Weight 95 - Setara)</white>"));
            player.sendMessage(mm.deserialize("<aqua>● Tingkat III (Administrasi & Staf):</aqua> <white>Warden (Weight 90) & Herald (Weight 80)</white>"));
            player.sendMessage(mm.deserialize("<yellow>● Tingkat II (Ordo Bangsawan):</yellow> <white>Sions (70), Emperor (60), Sovereign (50), Archon (40), Ascendant (30)</white>"));
            player.sendMessage(mm.deserialize("<gray>● Tingkat I (Fondasi Peradaban):</gray> <white>Wanderer (Weight 10 - Default)</white>"));
            player.sendMessage(mm.deserialize("<dark_gray><strikethrough>────────────────────────────────────────</strikethrough></dark_gray>"));
        }
    }

    private ItemStack createActionItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            List<Component> components = new ArrayList<>();
            for (String l : loreLines) {
                components.add(mm.deserialize(l));
            }
            meta.lore(components);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGlass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
