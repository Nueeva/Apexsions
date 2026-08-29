package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Modern 54-Slot Kingdom-Specific Leaderboard GUI.
 * Shows top ranking players only for the player's chosen kingdom.
 */
public class KingdomTopGUI implements Listener {

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
        String titleStr = "<gradient:#f39c12:#f1c40f><bold>👑 TOP KERAJAAN " + region.getKey().toUpperCase() + "</bold></gradient>";
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(titleStr));

        // Background Filler
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, grayGlass);
        }

        // Header: Kingdom Banner (Slot 4)
        Material kIcon = region.getKey().equalsIgnoreCase("ZENITHAR") ? Material.GOLD_BLOCK :
                region.getKey().equalsIgnoreCase("SOLTERRA") ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;

        List<Component> headerLore = new ArrayList<>();
        headerLore.add(miniMessage.deserialize("<gray>Peringkat supremasi dan pahlawan terkuat di kerajaan <yellow>" + region.getDisplayName() + "</yellow>.</gray>"));
        headerLore.add(miniMessage.deserialize(""));
        headerLore.add(miniMessage.deserialize("<gray>Wilayah Teritori: <white>" + region.getWorldName() + "</white></gray>"));
        headerLore.add(miniMessage.deserialize("<gray>Status Perang: " + (plugin.getWarManager().isWarActiveInTerritory(region) ? "<red><bold>⚔ SEDANG PERANG</bold></red>" : "<green>Damai</green>") + "</gray>"));
        headerLore.add(miniMessage.deserialize(""));
        headerLore.add(miniMessage.deserialize("<gold>⚔ Kejayaan dan kemakmuran abadi bagi " + region.getDisplayName() + "!</gold>"));
        inv.setItem(4, createItem(kIcon, "<gold><bold>👑 KERAJAAN " + region.getDisplayName().toUpperCase() + "</bold></gold>", headerLore));

        // Top 10 Kingdom Player Slots (20..24, 29..33)
        int[] playerSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

        for (int i = 0; i < playerSlots.length; i++) {
            int slot = playerSlots[i];
            int rankNum = i + 1;

            if (i < topPlayers.size()) {
                PlayerData topP = topPlayers.get(i);
                boolean isOnline = Bukkit.getPlayer(topP.getUuid()) != null;

                List<Component> pLore = new ArrayList<>();
                pLore.add(miniMessage.deserialize("<gray>Peringkat: <gold>#" + rankNum + "</gold></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Level Karakter: <yellow>Lv. " + topP.getLevel() + "</yellow></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Total XP: <aqua>" + String.format("%,d", topP.getXp()) + " XP</aqua></gray>"));
                pLore.add(miniMessage.deserialize(""));
                pLore.add(miniMessage.deserialize("<gray>Status: " + (isOnline ? "<green>● Online</green>" : "<dark_gray>○ Offline</dark_gray>") + "</gray>"));

                Material trophyMat;
                if (rankNum == 1) trophyMat = Material.TOTEM_OF_UNDYING;
                else if (rankNum == 2) trophyMat = Material.GOLDEN_HELMET;
                else if (rankNum == 3) trophyMat = Material.IRON_HELMET;
                else trophyMat = Material.PLAYER_HEAD;

                inv.setItem(slot, createItem(trophyMat, "<yellow><bold>#" + rankNum + " " + topP.getUsername() + "</bold></yellow>", pLore));
            } else {
                List<Component> emptyLore = Collections.singletonList(miniMessage.deserialize("<dark_gray>Belum ada pemain di slot peringkat ini.</dark_gray>"));
                inv.setItem(slot, createItem(Material.BARRIER, "<gray>Peringkat #" + rankNum + " (Kosong)</gray>", emptyLore));
            }
        }

        // Slot 47: Player's Own Rank Card
        List<Component> myRankLore = new ArrayList<>();
        myRankLore.add(miniMessage.deserialize("<gray>Kerajaan: <yellow>" + region.getDisplayName() + "</yellow></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Peringkat Kamu: <gold>#" + playerRank + "</gold></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Level: <yellow>Lv. " + pData.getLevel() + "</yellow></gray>"));
        myRankLore.add(miniMessage.deserialize("<gray>Total XP: <aqua>" + String.format("%,d", pData.getXp()) + " XP</aqua></gray>"));
        myRankLore.add(miniMessage.deserialize(""));
        myRankLore.add(miniMessage.deserialize("<gold>Terus tingkatkan level dan kumpulkan XP!</gold>"));
        inv.setItem(47, createItem(Material.BOOK, "<green><bold>✦ PERINGKAT PRIBADI KAMU ✦</bold></green>", myRankLore));

        // Bottom Navigation (Slot 49: Back to Profile, Slot 53: Close)
        inv.setItem(49, createItem(Material.ARROW, "<red><bold>◀ KEMBALI KE PROFIL</bold></red>", Collections.singletonList(miniMessage.deserialize("<gray>Kembali ke menu profil kerajaan</gray>"))));
        inv.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.3f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        String serialized = miniMessage.serialize(title);
        if (!serialized.contains("TOP KERAJAAN") && !serialized.contains("APEXSIONS LEADERBOARD")) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            plugin.getKingdomProfileGUI().open(player);
        } else if (slot == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        } else if (slot >= 20 && slot <= 33) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
        }
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
