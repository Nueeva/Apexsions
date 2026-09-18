package com.apexsions.core.region.gui;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.admin.MasterAdminGUI;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 27-slot Celestial Portal GUI specifically designed for Upper Realm
 * (The Aetherial Conclave) staff members to navigate, teleport, and inspect
 * all mortal kingdoms and dimensions seamlessly.
 */
public class ConclaveNavigationGUI implements Listener, InventoryHolder {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private Player viewingPlayer;
    private Inventory inventory;

    public ConclaveNavigationGUI(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public ConclaveNavigationGUI(ApexsionsCorePlugin plugin, Player player) {
        this.plugin = plugin;
        this.viewingPlayer = player;
        this.inventory = Bukkit.createInventory(this, 27, mm.deserialize("<gradient:#00f2fe:#4facfe><bold>✦ THE AETHERIAL CONCLAVE PORTAL ✦</bold></gradient>"));
        buildGUI(player);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory != null ? inventory : Bukkit.createInventory(null, 27);
    }

    public void open(Player player) {
        new ConclaveNavigationGUI(plugin, player).show();
    }

    private void show() {
        if (viewingPlayer != null && inventory != null) {
            viewingPlayer.openInventory(inventory);
            viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);
        }
    }

    public void buildGUI(Player player) {
        inventory.clear();

        ItemStack border = createGlass(Material.BLACK_STAINED_GLASS_PANE, "<dark_gray> </dark_gray>");
        ItemStack cyanDecor = createGlass(Material.CYAN_STAINED_GLASS_PANE, "<aqua>✦</aqua>");

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(0, cyanDecor);
        inventory.setItem(8, cyanDecor);
        inventory.setItem(18, cyanDecor);
        inventory.setItem(26, cyanDecor);

        // Header Slot 4: Aether Citadel / Spawn Lobby
        inventory.setItem(4, createItem(Material.BEACON,
                "<gradient:#00f2fe:#4facfe><bold>🏛 THE AETHER CITADEL / LOBBY 🏛</bold></gradient>",
                List.of(
                        "<gray>Pusat dimensi atas pengawas semesta.</gray>",
                        "<dark_gray>•</dark_gray> <white>Titik pusat perjumpaan antardimensi</white>",
                        "",
                        "<yellow>▶ Klik untuk Teleportasi ke Lobby Utama</yellow>"
                )));

        // Slot 10: Zenithar Capital Citadel
        inventory.setItem(10, createItem(Material.GOLD_BLOCK,
                "<gradient:#f1c40f:#e67e22><bold>👑 IBUKOTA ZENITHAR</bold></gradient>",
                List.of(
                        "<gray>Solarium Spire Citadel (Timur / Zenith)</gray>",
                        "<gray>Pusat dinasti bangsawan & kavaleri cakrawala.</gray>",
                        "",
                        "<yellow>▶ [Klik Kiri] Teleportasi ke Ibukota</yellow>",
                        "<aqua>▶ [Klik Kanan] Random Teleport (RTP) di Zenithar</aqua>"
                )));

        // Slot 12: Solterra Bastion
        inventory.setItem(12, createItem(Material.REDSTONE_BLOCK,
                "<gradient:#e74c3c:#c0392b><bold>🔥 IBUKOTA SOLTERRA</bold></gradient>",
                List.of(
                        "<gray>Ignis Bastion Fortress (Selatan / Magma)</gray>",
                        "<gray>Markas magician tempur & tentara cadas.</gray>",
                        "",
                        "<yellow>▶ [Klik Kiri] Teleportasi ke Ibukota</yellow>",
                        "<aqua>▶ [Klik Kanan] Random Teleport (RTP) di Solterra</aqua>"
                )));

        // Slot 14: Sylvamoor Eldergrove
        inventory.setItem(14, createItem(Material.EMERALD_BLOCK,
                "<gradient:#2ecc71:#27ae60><bold>🌿 IBUKOTA SYLVAMOOR</bold></gradient>",
                List.of(
                        "<gray>Eldergrove Sanctuary (Barat / Rimba)</gray>",
                        "<gray>Pusat peradaban petani & pejuang kanopi.</gray>",
                        "",
                        "<yellow>▶ [Klik Kiri] Teleportasi ke Ibukota</yellow>",
                        "<aqua>▶ [Klik Kanan] Random Teleport (RTP) di Sylvamoor</aqua>"
                )));

        // Slot 16: Terra Interdicta (Ancient Sions Ruins)
        inventory.setItem(16, createItem(Material.CRYING_OBSIDIAN,
                "<gradient:#8e44ad:#9b59b6><bold>🌌 TERRA INTERDICTA</bold></gradient>",
                List.of(
                        "<gray>Reruntuhan Kuno Sions (Zona Bahaya Terlarang)</gray>",
                        "<gray>Pusat anomali temporal & peninggalan kuno.</gray>",
                        "",
                        "<yellow>▶ Klik untuk Teleportasi ke Sions</yellow>"
                )));

        // Slot 19: Smart Kingdom RTP Dispatcher
        inventory.setItem(19, createItem(Material.COMPASS,
                "<gradient:#3498db:#2980b9><bold>🧭 SMART KINGDOM RTP</bold></gradient>",
                List.of(
                        "<gray>Teleportasi acak di teritori tempat Anda berdiri,</gray>",
                        "<gray>atau acak di wilayah kerajaan mortal.</gray>",
                        "",
                        "<yellow>▶ Klik untuk RTP Cerdas</yellow>"
                )));

        // Slot 22: Mortal Incarnation / Emulation Mode
        String emulated = plugin.getMortalEmulationManager() != null
                ? plugin.getMortalEmulationManager().getEmulatedKingdom(player.getUniqueId()).orElse("NONE")
                : "NONE";
        String statusDisplay = emulated.equalsIgnoreCase("NONE")
                ? "<aqua>Murni Aetherion (Upper Realm)</aqua>"
                : "<gold><bold>" + emulated + "</bold></gold> <gray>(Mode Penyamaran)</gray>";

        inventory.setItem(22, createItem(Material.PLAYER_HEAD,
                "<gradient:#9b59b6:#3498db><bold>🎭 SIMULASI WARGA FANA</bold></gradient>",
                List.of(
                        "<gray>Status Saat Ini: " + statusDisplay + "</gray>",
                        "<gray>Uji mekanik, buff/debuff, & toko dari kacamata fana.</gray>",
                        "",
                        "<yellow>▶ [Klik Kiri] Siklus Kerajaan (Zenithar ➔ Solterra ➔ Sylvamoor)</yellow>",
                        "<red>▶ [Klik Kanan] Matikan Mode Simulasi (Kembali ke Aetherion)</red>"
                )));

        // Slot 25: Master Admin Hub
        inventory.setItem(25, createItem(Material.NETHER_STAR,
                "<gradient:#e74c3c:#f39c12><bold>⚙ MASTER ADMIN HUB</bold></gradient>",
                List.of(
                        "<gray>Buka panel kontrol administrasi sentral.</gray>",
                        "",
                        "<yellow>▶ Klik untuk Membuka /admin</yellow>"
                )));
    }

    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ConclaveNavigationGUI gui) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            boolean isRightClick = event.isRightClick();

            if (slot == 4) { // Lobby
                player.closeInventory();
                Location lobby = gui.plugin.getConfigManager().getLobbyLocation();
                if (lobby != null) {
                    player.teleportAsync(lobby);
                    player.sendMessage(gui.mm.deserialize("<green>✓ Berhasil teleportasi ke <aqua>The Aether Citadel / Spawn Lobby</aqua>!</green>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
                } else {
                    player.sendMessage(gui.mm.deserialize("<red>✕ Titik spawn lobby belum diatur! Gunakan /ac setlobby.</red>"));
                }
                return;
            }

            if (slot == 10) { // Zenithar
                player.closeInventory();
                if (isRightClick) {
                    gui.plugin.getKingdomRtpService().executeRtpTargeted(player, "ZENITHAR");
                } else {
                    gui.teleportCapital(player, "ZENITHAR");
                }
                return;
            }

            if (slot == 12) { // Solterra
                player.closeInventory();
                if (isRightClick) {
                    gui.plugin.getKingdomRtpService().executeRtpTargeted(player, "SOLTERRA");
                } else {
                    gui.teleportCapital(player, "SOLTERRA");
                }
                return;
            }

            if (slot == 14) { // Sylvamoor
                player.closeInventory();
                if (isRightClick) {
                    gui.plugin.getKingdomRtpService().executeRtpTargeted(player, "SYLVAMOOR");
                } else {
                    gui.teleportCapital(player, "SYLVAMOOR");
                }
                return;
            }

            if (slot == 16) { // Terra Interdicta (Sions)
                player.closeInventory();
                gui.teleportCapital(player, "SIONS");
                return;
            }

            if (slot == 19) { // Smart RTP
                player.closeInventory();
                gui.plugin.getKingdomRtpService().executeRtp(player);
                return;
            }

            if (slot == 22) { // Mortal Emulation
                if (gui.plugin.getMortalEmulationManager() == null) return;
                if (isRightClick) {
                    gui.plugin.getMortalEmulationManager().clearEmulation(player);
                } else {
                    String cur = gui.plugin.getMortalEmulationManager().getEmulatedKingdom(player.getUniqueId()).orElse("NONE");
                    String next = switch (cur.toUpperCase()) {
                        case "ZENITHAR" -> "SOLTERRA";
                        case "SOLTERRA" -> "SYLVAMOOR";
                        default -> "ZENITHAR";
                    };
                    gui.plugin.getMortalEmulationManager().setEmulation(player, next);
                }
                // Refresh menu
                gui.buildGUI(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                return;
            }

            if (slot == 25) { // Master Admin Hub
                player.closeInventory();
                new MasterAdminGUI(gui.plugin, player).open();
            }
        }
    }

    private void teleportCapital(Player player, String kingdomKey) {
        Optional<Region> rOpt = plugin.getRegionManager().getRegion(kingdomKey.toUpperCase());
        if (rOpt.isPresent()) {
            plugin.getRegionTeleportService().teleport(player, rOpt.get());
        } else {
            player.sendMessage(mm.deserialize("<red>✕ Kerajaan " + kingdomKey + " tidak ditemukan atau spawn belum diatur! Gunakan /ac setspawn " + kingdomKey + "</red>"));
        }
    }
}
