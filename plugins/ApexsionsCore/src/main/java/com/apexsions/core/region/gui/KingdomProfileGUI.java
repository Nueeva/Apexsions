package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.KingdomProfileHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Modern 45-slot Chest GUI displaying a player's kingdom profile, XP progress,
 * level titles, rewards shortcut, XP guide directory, teleportation, and leaderboard.
 */
public class KingdomProfileGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KingdomProfileGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Data profilmu sedang dimuat. Silakan coba sesaat lagi.</red>"));
            return;
        }

        PlayerData data = dataOpt.get();
        KingdomProfileHolder holder = new KingdomProfileHolder();
        String titleStr = plugin.getConfigManager().getGuiConfig().getString("kingdom-profile.title", "<dark_gray><bold>👑 PROFIL & STATISTIK KERAJAAN 👑</bold></dark_gray>");
        Component title = miniMessage.deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(holder, 45, title);
        holder.setInventory(inv);

        // 1. Decorative borders
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack cyanAccent = createItem(Material.CYAN_STAINED_GLASS_PANE, "<dark_aqua>⚔</dark_aqua>");

        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }
        inv.setItem(0, cyanAccent);
        inv.setItem(8, cyanAccent);
        inv.setItem(36, cyanAccent);
        inv.setItem(44, cyanAccent);

        // 2. Slot 13: Player Skull with detailed stats
        int level = data.getLevel();
        long xp = data.getXp();
        long nextXp = plugin.getLevelFormula().getXpForLevel(level + 1);
        int percent = (int) Math.min(100, Math.max(0, (xp * 100) / Math.max(1, nextXp)));
        String progressBar = buildProgressBar(percent);
        String levelTitle = plugin.getLevelManager().getLevelTitle(player.getUniqueId());
        int unclaimedCount = plugin.getRewardManager().getUnclaimedCount(data);

        Optional<Region> regionOpt = data.getRegionId() != null ? plugin.getRegionManager().getRegion(data.getRegionId()) : Optional.empty();
        String kingdomDisplay = regionOpt.map(Region::getDisplayName).orElse("<gray>Belum Memilih</gray>");

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(miniMessage.deserialize("<gold><bold>Profil: " + player.getName() + "</bold></gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Level: <gold><bold>" + level + "</bold></gold> <dark_gray>/ 100</dark_gray></gray>"));
            lore.add(miniMessage.deserialize("<gray>Gelar: <yellow>" + levelTitle + "</yellow></gray>"));
            lore.add(miniMessage.deserialize("<gray>EXP: <yellow>" + xp + "</yellow><dark_gray>/</dark_gray><gold>" + (nextXp == Long.MAX_VALUE ? "MAX" : nextXp) + "</gold> <gray>(" + percent + "%)</gray></gray>"));
            lore.add(miniMessage.deserialize("<gray>Progres: </gray>" + progressBar));
            lore.add(miniMessage.deserialize("<gray>Kerajaan: " + kingdomDisplay + "</gray>"));
            if (unclaimedCount > 0) {
                lore.add(miniMessage.deserialize("<green>⚡ " + unclaimedCount + " Hadiah Siap Diambil</green>"));
            } else {
                lore.add(miniMessage.deserialize("<gray>Hadiah: <green>Sudah Diambil</green></gray>"));
            }
            skullMeta.lore(lore);
            skull.setItemMeta(skullMeta);
        }
        inv.setItem(13, skull);

        // Count online players in kingdom
        long onlineKingdomCount = 0;
        if (regionOpt.isPresent()) {
            Region r = regionOpt.get();
            onlineKingdomCount = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> plugin.getApi().getPlayerRegionKey(p.getUniqueId()).equalsIgnoreCase(r.getKey()))
                    .count();
        }

        // 3. Slot 20: Kingdom Card
        if (regionOpt.isPresent()) {
            Region reg = regionOpt.get();
            Material iconMat = switch (reg.getKey()) {
                case "ZENITHAR" -> Material.GOLD_BLOCK;
                case "SOLTERRA" -> Material.REDSTONE_BLOCK;
                case "SYLVAMOOR" -> Material.DIAMOND_BLOCK;
                default -> Material.EMERALD_BLOCK;
            };

            ItemStack kItem = createItem(iconMat, reg.getDisplayName(),
                    "<gray>Status: <green>Warga Terdaftar</green></gray>",
                    "<gray>Warga Online: <yellow>" + onlineKingdomCount + " Pemain</yellow></gray>",
                    "<gray>Ibukota: <yellow>" + reg.getWorldName() + " (" + (int)(double)reg.getSpawnX() + ", " + (int)(double)reg.getSpawnY() + ", " + (int)(double)reg.getSpawnZ() + ")</yellow></gray>",
                    "<gray>Status Wilayah: " + (plugin.getWarManager().isWarActiveInTerritory(reg) ? "<red><bold>SEDANG PERANG</bold></red>" : "<green>Damai</green>") + "</gray>");
            inv.setItem(20, kItem);
        } else {
            ItemStack chooseItem = createItem(Material.ARMOR_STAND,
                    "<yellow><bold>Pilih Kerajaan</bold></yellow>",
                    "<gray>Belum memilih kerajaan.",
                    "<gold>» Klik untuk memilih</gold>");
            inv.setItem(20, chooseItem);
        }

        // 4. Slot 22: Level Rewards & Milestone Button
        ItemStack rewardsBtn = new ItemStack(unclaimedCount > 0 ? Material.ENDER_CHEST : Material.CHEST);
        ItemMeta rMeta = rewardsBtn.getItemMeta();
        if (rMeta != null) {
            rMeta.displayName(miniMessage.deserialize("<gold><bold>Hadiah Level</bold></gold>"));
            List<Component> rLore = new ArrayList<>();
            rLore.add(miniMessage.deserialize("<gray>Hadiah Level 1–100</gray>"));
            if (unclaimedCount > 0) {
                rLore.add(miniMessage.deserialize("<green><bold>⚡ " + unclaimedCount + " Hadiah Menunggumu!</bold></green>"));
                rMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                rMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                rLore.add(miniMessage.deserialize("<gray>Semua hadiah sudah diambil.</gray>"));
            }
            rLore.add(miniMessage.deserialize("<yellow>» Klik untuk buka menu</yellow>"));
            rMeta.lore(rLore);
            rMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            rewardsBtn.setItemMeta(rMeta);
        }
        inv.setItem(22, rewardsBtn);

        // 5. Slot 24: Fast Travel to Kingdom Capital
        if (regionOpt.isPresent()) {
            ItemStack tpBtn = createItem(Material.ENDER_PEARL,
                    "<aqua><bold>Warp Ibukota</bold></aqua>",
                    "<gray>Teleport ke ibukota kerajaan.",
                    "<aqua>» Klik untuk teleport</aqua>");
            inv.setItem(24, tpBtn);
        } else {
            ItemStack lockedTp = createItem(Material.BARRIER,
                    "<red><bold>Warp Terkunci</bold></red>",
                    "<gray>Pilih kerajaan terlebih dahulu.</gray>");
            inv.setItem(24, lockedTp);
        }

        // 6. Slot 29: Leaderboard / Hall of Fame
        ItemStack topBtn = createItem(Material.NETHER_STAR,
                "<gradient:#f1c40f:#e67e22><bold>Papan Peringkat</bold></gradient>",
                "<gray>Lihat top level & pemain terkuat.",
                "<gold>» Klik untuk buka leaderboard</gold>");
        inv.setItem(29, topBtn);

        // 7. Slot 31: XP Guide Directory
        ItemStack xpGuideBtn = createItem(Material.KNOWLEDGE_BOOK,
                "<green><bold>Panduan XP</bold></green>",
                "<gray>Daftar 13 sumber perolehan XP.",
                "<green>» Klik untuk buka</green>");
        inv.setItem(31, xpGuideBtn);

        // 8. Slot 33: Random Teleport (RTP)
        ItemStack rtpBtn = createItem(Material.COMPASS,
                "<light_purple><bold>Random Teleport (RTP)</bold></light_purple>",
                "<gray>Teleportasi acak di dalam wilayah kerajaan.",
                "<gray>Syarat: <white>Wajib berada di dalam wilayah</white></gray>",
                "<light_purple>» Klik untuk RTP</light_purple>");
        inv.setItem(33, rtpBtn);

        // 9. Slot 40: Close Button
        ItemStack closeBtn = createItem(Material.BARRIER, "<red><bold>Tutup</bold></red>");
        inv.setItem(40, closeBtn);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof KingdomProfileHolder)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 45) return;

        // Kingdom choose (Slot 20)
        if (slot == 20) {
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isPresent() && !dataOpt.get().hasRegion()) {
                plugin.getRegionSelectionGUI().open(player);
            }
            return;
        }

        // Level Rewards Menu (Slot 22)
        if (slot == 22) {
            plugin.getLevelRewardsGUI().open(player, 1);
            return;
        }

        // Teleport to Kingdom Capital (Slot 24)
        if (slot == 24) {
            Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (dataOpt.isPresent() && dataOpt.get().hasRegion()) {
                player.closeInventory();
                plugin.getRegionTeleportService().teleportToRegion(player);
            }
            return;
        }

        // Leaderboard (Slot 29)
        if (slot == 29) {
            player.closeInventory();
            plugin.getKingdomTopGUI().open(player);
            return;
        }

        // XP Guide (Slot 31)
        if (slot == 31) {
            plugin.getXpGuideGUI().open(player);
            return;
        }

        // RTP (Slot 33)
        if (slot == 33) {
            player.closeInventory();
            plugin.getKingdomRtpService().executeRtp(player);
            return;
        }

        // Close Menu (Slot 40)
        if (slot == 40) {
            player.closeInventory();
        }
    }

    private String buildProgressBar(int percent) {
        int totalBars = 15;
        int filled = (percent * totalBars) / 100;
        StringBuilder sb = new StringBuilder("<yellow>");
        for (int i = 0; i < filled; i++) sb.append("█");
        sb.append("</yellow><dark_gray>");
        for (int i = filled; i < totalBars; i++) sb.append("░");
        sb.append("</dark_gray>");
        return sb.toString();
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            if (lore.length > 0) {
                List<Component> compLore = new ArrayList<>();
                for (String l : lore) {
                    compLore.add(miniMessage.deserialize(l));
                }
                meta.lore(compLore);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
