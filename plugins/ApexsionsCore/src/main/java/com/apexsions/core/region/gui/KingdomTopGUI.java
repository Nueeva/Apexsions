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
 * Modern 54-Slot GUI Leaderboard displaying Top Kingdoms and Top Players.
 * Fully compatible with ajLeaderboards and DecentHolograms statistics.
 */
public class KingdomTopGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final String GUI_TITLE = "<gradient:#f39c12:#f1c40f><bold>👑 APEXSIONS LEADERBOARD</bold></gradient>";

    public KingdomTopGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(GUI_TITLE));

        // Background Filler
        ItemStack filler = createItem(Material.DARK_OAK_HANGING_SIGN, " ", null);
        ItemStack grayGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        ItemStack goldGlass = createItem(Material.ORANGE_STAINED_GLASS_PANE, " ", null);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, grayGlass);
        }

        // Header
        List<Component> headerLore = new ArrayList<>();
        headerLore.add(miniMessage.deserialize("<gray>Peringkat supremasi kerajaan dan pemain terkuat di server.</gray>"));
        headerLore.add(miniMessage.deserialize(""));
        headerLore.add(miniMessage.deserialize("<gold>⚔ Bersaing untuk kejayaan kerajaanmu!</gold>"));
        inv.setItem(4, createItem(Material.NETHER_STAR, "<gold><bold>🌟 HALL OF FAME</bold></gold>", headerLore));

        // 3 Kingdom Overview Badges (Slots 20, 22, 24)
        int[] kingdomSlots = {20, 22, 24};
        List<Region> regions = new ArrayList<>(plugin.getRegionManager().getRegions());
        for (int i = 0; i < Math.min(regions.size(), kingdomSlots.length); i++) {
            Region r = regions.get(i);
            List<Component> kLore = new ArrayList<>();
            kLore.add(miniMessage.deserialize("<dark_gray>Statistik Kerajaan</dark_gray>"));
            kLore.add(miniMessage.deserialize("<gray>Wilayah: <white>" + r.getWorldName() + "</white></gray>"));
            kLore.add(miniMessage.deserialize("<gray>Status Perang: " + (plugin.getWarManager().isWarActiveInTerritory(r) ? "<red><bold>SEDANG PERANG</bold></red>" : "<green>Damai</green>") + "</gray>"));
            kLore.add(miniMessage.deserialize(""));
            kLore.add(miniMessage.deserialize("<yellow>Klik untuk melihat profil kerajaan</yellow>"));

            Material icon = r.getKey().equalsIgnoreCase("ZENITHAR") ? Material.GOLD_BLOCK :
                    r.getKey().equalsIgnoreCase("SOLTERRA") ? Material.REDSTONE_BLOCK : Material.DIAMOND_BLOCK;

            inv.setItem(kingdomSlots[i], createItem(icon, "<gold><bold>" + r.getDisplayName() + "</bold></gold>", kLore));
        }

        // Top 5 Online Players Preview (Slots 29, 30, 31, 32, 33)
        List<Player> sortedPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        sortedPlayers.sort((p1, p2) -> {
            int lvl1 = plugin.getPlayerDataService().getCached(p1.getUniqueId()).map(PlayerData::getLevel).orElse(1);
            int lvl2 = plugin.getPlayerDataService().getCached(p2.getUniqueId()).map(PlayerData::getLevel).orElse(1);
            return Integer.compare(lvl2, lvl1);
        });

        int[] playerSlots = {29, 30, 31, 32, 33};
        for (int i = 0; i < playerSlots.length; i++) {
            if (i < sortedPlayers.size()) {
                Player topP = sortedPlayers.get(i);
                PlayerData data = plugin.getPlayerDataService().getCached(topP.getUniqueId()).orElse(null);
                int lvl = data != null ? data.getLevel() : 1;
                long xp = data != null ? data.getXp() : 0;
                String kName = data != null && data.hasRegion() ? data.getRegionId().toString() : "None";

                List<Component> pLore = new ArrayList<>();
                pLore.add(miniMessage.deserialize("<gray>Peringkat: <gold>#" + (i + 1) + "</gold></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Level: <yellow>Lv. " + lvl + "</yellow></gray>"));
                pLore.add(miniMessage.deserialize("<gray>Total XP: <aqua>" + xp + " XP</aqua></gray>"));
                pLore.add(miniMessage.deserialize(""));
                pLore.add(miniMessage.deserialize("<green>Status: Online</green>"));

                Material trophyMat = (i == 0) ? Material.TOTEM_OF_UNDYING :
                        (i == 1) ? Material.GOLDEN_HELMET :
                                (i == 2) ? Material.IRON_HELMET : Material.PLAYER_HEAD;

                inv.setItem(playerSlots[i], createItem(trophyMat, "<yellow><bold>" + topP.getName() + "</bold></yellow>", pLore));
            } else {
                List<Component> emptyLore = Collections.singletonList(miniMessage.deserialize("<dark_gray>Slot kosong</dark_gray>"));
                inv.setItem(playerSlots[i], createItem(Material.BARRIER, "<gray>Peringkat #" + (i + 1) + " (Kosong)</gray>", emptyLore));
            }
        }

        // Bottom Navigation (Slot 49: Back, Slot 53: Close)
        inv.setItem(49, createItem(Material.ARROW, "<red><bold>◀ KEMBALI KE PROFIL</bold></red>", Collections.singletonList(miniMessage.deserialize("<gray>Kembali ke menu profil kerajaan</gray>"))));
        inv.setItem(53, createItem(Material.BARRIER, "<red><bold>✖ TUTUP</bold></red>", null));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        if (!miniMessage.serialize(title).contains("APEXSIONS LEADERBOARD")) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            plugin.getKingdomProfileGUI().open(player);
        } else if (slot == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        } else if (slot == 20 || slot == 22 || slot == 24) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.5f);
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
