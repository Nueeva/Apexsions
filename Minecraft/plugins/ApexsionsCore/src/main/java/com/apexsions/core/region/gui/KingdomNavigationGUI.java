package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.gui.holder.KingdomNavHolder;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modern 27-slot Chest GUI providing quick navigation between Kingdom Capital spawn,
 * Random Teleport (RTP), and Kingdom Profile inspection.
 */
public class KingdomNavigationGUI implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KingdomNavigationGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player)) {
                player.sendMessage(miniMessage.deserialize("<gradient:#00f2fe:#4facfe><bold>✦ THE AETHERIAL CONCLAVE ✦</bold></gradient> <dark_gray>➔</dark_gray> <aqua>Sebagai entitas transenden Aetherion, Anda tidak terikat oleh ibukota fana tunggal. Silakan gunakan <gold>/lobby</gold> atau teleportasi admin.</aqua>"));
                return;
            }
            player.sendMessage(miniMessage.deserialize("<gradient:#f39c12:#f1c40f><bold>APEXSIONS REALM</bold></gradient> <dark_gray>»</dark_gray> <yellow>Anda belum memilih kerajaan! Membuka menu pemilihan kerajaan...</yellow>"));
            plugin.getRegionSelectionGUI().open(player);
            return;
        }

        PlayerData data = dataOpt.get();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
        if (regionOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Data kerajaan kamu tidak ditemukan.</red>"));
            return;
        }

        Region region = regionOpt.get();
        KingdomNavHolder holder = new KingdomNavHolder();
        String titleStr = plugin.getConfigManager().getGuiConfig().getString("kingdom-navigation.title", "<dark_gray><bold>⚔ MENU KERAJAAN ⚔</bold></dark_gray>");
        Component title = miniMessage.deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        // 1. Theme-based accent panes
        Material accentMat = switch (region.getKey()) {
            case "ZENITHAR" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "SOLTERRA" -> Material.RED_STAINED_GLASS_PANE;
            case "SYLVAMOOR" -> Material.CYAN_STAINED_GLASS_PANE;
            case "SIONS" -> Material.PURPLE_STAINED_GLASS_PANE;
            default -> Material.ORANGE_STAINED_GLASS_PANE;
        };

        ItemStack darkGlass = createItem(Material.GRAY_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>");
        ItemStack accentGlass = createItem(accentMat, "<gold>⚜</gold>");

        // Fill background
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkGlass);
        }

        // Accents and borders
        inv.setItem(0, accentGlass);
        inv.setItem(8, accentGlass);
        inv.setItem(18, accentGlass);
        inv.setItem(26, accentGlass);

        inv.setItem(1, blackGlass);
        inv.setItem(7, blackGlass);
        inv.setItem(9, blackGlass);
        inv.setItem(17, blackGlass);
        inv.setItem(19, blackGlass);
        inv.setItem(25, blackGlass);

        // 2. Slot 11: Balik ke Ibukota Kerajaan
        String coordsStr = "";
        if (region.getSpawnX() != null && region.getSpawnY() != null && region.getSpawnZ() != null) {
            coordsStr = "<gray>Koordinat: <aqua>[" + (int)(double)region.getSpawnX() + ", "
                    + (int)(double)region.getSpawnY() + ", "
                    + (int)(double)region.getSpawnZ() + "]</aqua></gray>";
        }

        ItemStack capitalBtn = createItemWithGlow(Material.LODESTONE,
                "<gradient:#ffeaa7:#f39c12><bold>🏰 BALIK KE IBUKOTA</bold></gradient>",
                "<gray>Ibukota: <gold><bold>" + region.getDisplayName() + "</bold></gold></gray>",
                "<gray>Dunia: <white>" + region.getWorldName() + "</white></gray>",
                coordsStr,
                "",
                "<gray>Teleportasi instan dan aman langsung</gray>",
                "<gray>ke titik spawn pusat istana kerajaanmu.</gray>",
                "",
                "<green>▶ Klik untuk pulang ke Ibukota!</green>");
        inv.setItem(11, capitalBtn);

        // 3. Slot 13: Profil & Status Kerajaan
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(miniMessage.deserialize("<gradient:#ffd700:#ffa502><bold>👑 STATUS KERAJAAN</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Warga: <white>" + player.getName() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Kerajaan: <gold><bold>" + region.getDisplayName() + "</bold></gold></gray>"));
            lore.add(miniMessage.deserialize("<gray>Level Progresi: <gold><bold>" + data.getLevel() + "</bold></gold> <dark_gray>/ 100</dark_gray></gray>"));
            String kingName = plugin.getConfigManager().getKingdomKing(region.getKey());
            if (kingName == null || kingName.isBlank()) kingName = "Belum Ditunjuk";
            lore.add(miniMessage.deserialize("<gray>Raja Saat Ini: <yellow>" + kingName + "</yellow></gray>"));
            boolean isWar = plugin.getWarManager() != null && plugin.getWarManager().isWarActiveInTerritory(region);
            lore.add(miniMessage.deserialize("<gray>Status Wilayah: " + (isWar ? "<red><bold>SEDANG PERANG</bold></red>" : "<green>Damai</green>") + "</gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<yellow>» Klik untuk buka Profil & Statistik lengkap</yellow>"));
            skullMeta.lore(lore);
            skull.setItemMeta(skullMeta);
        }
        inv.setItem(13, skull);

        // 4. Slot 15: Random Teleport (RTP)
        ItemStack rtpBtn = createItemWithGlow(Material.COMPASS,
                "<gradient:#3498db:#9b59b6><bold>🎲 RANDOM TELEPORT (RTP)</bold></gradient>",
                "<gray>Teleportasi acak ke lokasi alam liar</gray>",
                "<gray>di dalam teritorial <gold>" + region.getDisplayName() + "</gold>.</gray>",
                "",
                "<gray>Area Diizinkan: <white>Wilayah Kerajaan & Lobby</white></gray>",
                "<gray>Keamanan: <aqua>Pencarian Blok Aman & Asinkron</aqua></gray>",
                "",
                "<light_purple>▶ Klik untuk menjelajah secara acak!</light_purple>");
        inv.setItem(15, rtpBtn);

        // 5. Slot 22: Tombol Tutup
        ItemStack closeBtn = createItem(Material.BARRIER,
                "<red><bold>✖ Tutup</bold></red>",
                "<gray>Klik untuk menutup menu.</gray>");
        inv.setItem(22, closeBtn);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KingdomNavHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            player.closeInventory();
            return;
        }

        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(dataOpt.get().getRegionId());
        if (regionOpt.isEmpty()) {
            player.closeInventory();
            return;
        }

        Region region = regionOpt.get();

        if (slot == 11) { // Balik ke Ibukota Kerajaan
            player.closeInventory();

            // Combat tag check
            if (plugin.getCombatTagService() != null && plugin.getCombatTagService().isCombatTagged(player.getUniqueId())) {
                long remaining = plugin.getCombatTagService().getRemainingSeconds(player.getUniqueId());
                player.sendMessage(miniMessage.deserialize("<red>⚔ Kamu sedang dalam mode tempur (Combat Tag: <yellow>" + remaining + "s</yellow>)! Teleportasi diblokir.</red>"));
                return;
            }

            // War check
            if (plugin.getWarManager() != null && plugin.getWarManager().isWarActiveInTerritory(region)) {
                player.sendMessage(miniMessage.deserialize("<dark_red>⚔ Wilayah kerajaan sedang dalam keadaan PERANG (WAR)! Teleportasi dinonaktifkan sementara.</dark_red>"));
                return;
            }

            plugin.getRegionTeleportService().teleport(player, region);
            return;
        }

        if (slot == 13) { // Buka Profil Kerajaan Lengkap
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            plugin.getKingdomProfileGUI().open(player);
            return;
        }

        if (slot == 15) { // Random Teleport (RTP)
            player.closeInventory();
            plugin.getKingdomRtpService().executeRtp(player);
            return;
        }

        if (slot == 22) { // Tutup
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KingdomNavHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(name));
            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    if (line == null || line.isBlank()) {
                        loreList.add(Component.empty());
                    } else {
                        loreList.add(miniMessage.deserialize(line));
                    }
                }
                meta.lore(loreList);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItemWithGlow(Material mat, String name, String... lore) {
        ItemStack item = createItem(mat, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
