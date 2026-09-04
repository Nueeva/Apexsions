package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.KingdomTopHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Modern 54-Slot Kingdom Leaderboard GUI with Symmetrical Pyramid Layout.
 * Matches BattlePass Leaderboard styling, with kingdom-colored top block and player heads.
 */
public class KingdomTopGUI implements Listener {

    // 10-slot symmetrical pyramid (identical to BattlePassLeaderboardMenu):
    // Row 1 (1 slot: 13), Row 2 (2 slots: 21, 23), Row 3 (3 slots: 29, 31, 33), Row 4 (4 slots: 37, 39, 41, 43)
    private static final int[] PYRAMID_SLOTS = { 13, 21, 23, 29, 31, 33, 37, 39, 41, 43 };

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KingdomTopGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerData pData = plugin.getPlayerDataService().getCached(player.getUniqueId()).orElse(null);
        if (pData == null || !pData.hasRegion()) {
            player.sendMessage(miniMessage.deserialize("<red><bold>⚔ KERAJAAN DIPERLUKAN:</bold> Anda harus memilih kerajaan terlebih dahulu untuk melihat leaderboard! Gunakan <yellow>/kingdom choose</yellow>.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        Region region = plugin.getRegionManager().getRegionById(pData.getRegionId()).orElse(null);
        if (region == null) {
            player.sendMessage(miniMessage.deserialize("<red>Data wilayah kerajaan kamu tidak ditemukan.</red>"));
            return;
        }

        // Asynchronously query database for top 10 players and player's own rank
        plugin.getPlayerRepository().getTopPlayersByRegionAsync(region.getId(), 10).thenAcceptBoth(
                plugin.getPlayerRepository().getPlayerRankInRegionAsync(player.getUniqueId(), region.getId()),
                (topPlayers, playerRank) -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!player.isOnline()) return;
                        renderAndOpen(player, pData, region, topPlayers, playerRank);
                    });
                }
        );
    }

    private void renderAndOpen(Player player, PlayerData pData, Region region, List<PlayerData> topPlayers, int playerRank) {
        String titleStr = "<gradient:#f39c12:#f1c40f><bold>👑 TOP KERAJAAN " + region.getKey().toUpperCase(Locale.ROOT) + "</bold></gradient>";
        KingdomTopHolder holder = new KingdomTopHolder(region.getId(), region.getKey());
        Inventory inv = Bukkit.createInventory(holder, 54, miniMessage.deserialize(titleStr));
        holder.setInventory(inv);

        // 1. Background Fillers
        Material accentGlassMat = resolveKingdomGlass(region);
        ItemStack darkGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        ItemStack accentGlass = createItem(accentGlassMat, " ", null);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, darkGlass);
        }

        // Accent borders (top row around slot 4, bottom row around slot 49)
        inv.setItem(3, accentGlass);
        inv.setItem(5, accentGlass);
        inv.setItem(48, accentGlass);
        inv.setItem(50, accentGlass);

        // Frame around pyramid slots with accent glass
        int[] accentSlots = {22, 30, 32, 38, 40, 42};
        for (int s : accentSlots) {
            inv.setItem(s, accentGlass);
        }

        // 2. Top Center: Kingdom Block (Slot 4 - Color matching the kingdom)
        Material kIcon = resolveKingdomBlock(region);
        boolean warActive = plugin.getWarManager().isWarActiveInTerritory(region);

        List<Component> headerLore = new ArrayList<>();
        headerLore.add(miniMessage.deserialize("<gray>Pusat kekuasaan dan kejayaan <yellow>" + region.getDisplayName() + "</yellow>.</gray>"));
        headerLore.add(miniMessage.deserialize(""));
        headerLore.add(miniMessage.deserialize("<gray>Wilayah Teritori: <white>" + region.getWorldName() + "</white></gray>"));
        headerLore.add(miniMessage.deserialize("<gray>Status Perang: " + (warActive ? "<red><bold>⚔ SEDANG PERANG</bold></red>" : "<green>Damai & Berdaulat</green>") + "</gray>"));
        headerLore.add(miniMessage.deserialize(""));
        headerLore.add(miniMessage.deserialize("<gold>⚔ Kejayaan dan kemakmuran abadi bagi " + region.getDisplayName() + "!</gold>"));
        inv.setItem(4, createItem(kIcon, "<gradient:#ffeaa7:#ffd700><bold>👑 KERAJAAN " + region.getDisplayName().toUpperCase(Locale.ROOT) + "</bold></gradient>", headerLore));

        // 3. Leaderboard Info Banner (Slot 0)
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(miniMessage.deserialize("<gray>Peringkat pahlawan terkuat dan tertinggi</gray>"));
        infoLore.add(miniMessage.deserialize("<gray>berdasarkan perolehan Level & EXP di <yellow>" + region.getDisplayName() + "</yellow>.</gray>"));
        infoLore.add(miniMessage.deserialize(""));
        infoLore.add(miniMessage.deserialize("<gray>Total Peserta: <aqua>Top 10 Pahlawan</aqua></gray>"));
        infoLore.add(miniMessage.deserialize("<gray>Peringkat Anda: <yellow>" + (playerRank > 0 ? "#" + playerRank : "Belum Masuk Peringkat") + "</yellow></gray>"));
        inv.setItem(0, createItem(Material.NETHER_STAR, "<gradient:#f6d365:#fda085><bold>👑 TOP KERAJAAN RANKINGS</bold></gradient>", infoLore));

        // 4. Territory Status Banner (Slot 8)
        List<Component> statusLore = new ArrayList<>();
        statusLore.add(miniMessage.deserialize("<gray>Wilayah: <white>" + region.getWorldName() + "</white></gray>"));
        statusLore.add(miniMessage.deserialize("<gray>Situasi: " + (warActive ? "<red>Siaga Perang (Wartime)</red>" : "<green>Wilayah Terlindungi (Aman)</green>") + "</gray>"));
        statusLore.add(miniMessage.deserialize(""));
        statusLore.add(miniMessage.deserialize("<gray>Peringkat Kamu: <gold>" + (playerRank > 0 ? "#" + playerRank : "-") + "</gold></gray>"));
        inv.setItem(8, createItem(Material.CLOCK, "<gold><bold>⚔ STATUS KERAJAAN</bold></gold>", statusLore));

        // 5. Render 10-Slot Symmetrical Pyramid with Player Heads
        for (int i = 0; i < PYRAMID_SLOTS.length; i++) {
            int slot = PYRAMID_SLOTS[i];
            int rankNum = i + 1;

            if (i < topPlayers.size()) {
                PlayerData topP = topPlayers.get(i);
                Player onlineP = Bukkit.getPlayer(topP.getUuid());
                plugin.getLevelManager().reconcileLevel(topP, onlineP);
                boolean isOnline = onlineP != null;
                long reqXp = plugin.getLevelFormula().getRequiredXpForNextLevel(topP.getLevel());

                String rankPrefix = switch (rankNum) {
                    case 1 -> "<gradient:#f6d365:#fda085><bold>🥇 #1 </bold></gradient>";
                    case 2 -> "<gradient:#e0e0e0:#f5f5f5><bold>🥈 #2 </bold></gradient>";
                    case 3 -> "<gradient:#cd7f32:#e67e22><bold>🥉 #3 </bold></gradient>";
                    default -> "<yellow><bold>#" + rankNum + " </bold></yellow>";
                };

                String title = plugin.getLevelTitleResolver() != null ? plugin.getLevelTitleResolver().resolveTitle(region, topP.getLevel()) : "Citizen";

                List<Component> pLore = new ArrayList<>();
                pLore.add(miniMessage.deserialize("<gray>Peringkat: <gold>#" + rankNum + "</gold></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Gelar: <yellow>" + title + "</yellow></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Level Karakter: <gold><bold>Lv. " + topP.getLevel() + "</bold></gold></gray>"));
                pLore.add(miniMessage.deserialize("<gray>EXP: <aqua>" + String.format("%,d", topP.getXp()) + " / " + (reqXp == Long.MAX_VALUE ? "MAX" : String.format("%,d", reqXp)) + " XP</aqua></gray>"));
                pLore.add(miniMessage.deserialize(""));
                pLore.add(miniMessage.deserialize("<gray>Status: " + (isOnline ? "<green>● Online</green>" : "<dark_gray>○ Offline</dark_gray>") + "</gray>"));

                OfflinePlayer op = Bukkit.getOfflinePlayer(topP.getUuid());
                ItemStack head = createPlayerHead(op, rankPrefix + "<white><bold>" + topP.getUsername() + "</bold></white>", pLore);
                inv.setItem(slot, head);
            } else {
                List<Component> emptyLore = Collections.singletonList(miniMessage.deserialize("<dark_gray>Belum ada pemain di peringkat ini.</dark_gray>"));
                inv.setItem(slot, createItem(Material.SKELETON_SKULL, "<dark_gray>Peringkat #" + rankNum + " (Kosong)</dark_gray>", emptyLore));
            }
        }

        // 6. Navigation Controls (Row 5)
        // Slot 45: Back Button
        inv.setItem(45, createItem(Material.ARROW, "<red><bold>◀ KEMBALI KE PROFIL</bold></red>",
                Collections.singletonList(miniMessage.deserialize("<gray>Kembali ke menu profil kerajaan</gray>"))));

        // Slot 49: Player's Own Rank Card (Player Head)
        List<Component> myRankLore = new ArrayList<>();
        myRankLore.add(miniMessage.deserialize("<gray>Nama: <white>" + player.getName() + "</white></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Kerajaan: <yellow>" + region.getDisplayName() + "</yellow></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Peringkat Anda: <gold><bold>" + (playerRank > 0 ? "#" + playerRank : "Belum Masuk Peringkat") + "</bold></gold></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Level: <yellow>Lv. " + pData.getLevel() + "</yellow></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Total EXP: <aqua>" + String.format("%,d", pData.getXp()) + " XP</aqua></gray>"));
        myRankLore.add(miniMessage.deserialize(""));
        myRankLore.add(miniMessage.deserialize("<gold>Terus tingkatkan level dan kumpulkan XP!</gold>"));
        inv.setItem(49, createPlayerHead(player, "<green><bold>✦ PERINGKAT PRIBADI KAMU ✦</bold></green>", myRankLore));

        // Slot 53: Close Button
        inv.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>",
                Collections.singletonList(miniMessage.deserialize("<gray>Tutup menu leaderboard</gray>"))));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.3f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof KingdomTopHolder)) return;

        // Absolute protection: Cancel any and all click interactions inside this GUI
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return; // Clicked in player's lower inventory, cancelled already
        }

        if (rawSlot == 45) {
            player.closeInventory();
            plugin.getKingdomProfileGUI().open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        } else if (rawSlot == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        } else if (rawSlot == 49) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
        } else {
            for (int pSlot : PYRAMID_SLOTS) {
                if (rawSlot == pSlot) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KingdomTopHolder) {
            event.setCancelled(true);
        }
    }

    private Material resolveKingdomBlock(Region region) {
        String key = region.getKey().toUpperCase(Locale.ROOT);
        // Check configuration first
        if (plugin.getConfigManager().getSection("regions") != null) {
            String iconName = plugin.getConfigManager().getSection("regions").getString(key + ".icon");
            if (iconName != null) {
                Material mat = Material.matchMaterial(iconName);
                if (mat != null) return mat;
            }
        }
        if (key.contains("ZENITHAR")) return Material.GOLD_BLOCK;
        if (key.contains("SOLTERRA")) return Material.REDSTONE_BLOCK;
        if (key.contains("SYLVAMOOR")) return Material.DIAMOND_BLOCK;
        return Material.EMERALD_BLOCK;
    }

    private Material resolveKingdomGlass(Region region) {
        String key = region.getKey().toUpperCase(Locale.ROOT);
        if (key.contains("ZENITHAR")) return Material.YELLOW_STAINED_GLASS_PANE;
        if (key.contains("SOLTERRA")) return Material.RED_STAINED_GLASS_PANE;
        if (key.contains("SYLVAMOOR")) return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        return Material.CYAN_STAINED_GLASS_PANE;
    }

    private ItemStack createPlayerHead(OfflinePlayer op, String name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            if (op != null) {
                meta.setOwningPlayer(op);
            }
            if (name != null) {
                meta.displayName(miniMessage.deserialize(name));
            }
            if (lore != null) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(miniMessage.deserialize(name));
            }
            if (lore != null) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
