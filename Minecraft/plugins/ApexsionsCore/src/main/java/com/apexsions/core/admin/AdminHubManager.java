package com.apexsions.core.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.admin.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry and manager for the Master Admin Hub system.
 */
public class AdminHubManager {

    private final ApexsionsCorePlugin plugin;
    private final Map<String, AdminModule> modules = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AdminHubManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        registerDefaultCoreModules();
    }

    private void registerDefaultCoreModules() {
        // 1. ApexsionsCore Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "core"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 APEXSIONS CORE</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.NETHER_STAR; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Kontrol sentral kerajaan, perang, dan navigasi:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Warp Manager & Penobatan Raja</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Kingdom War & Teritori</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Region Spawns & Lobby</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Kingdom RTP System</yellow>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Core!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionscore.admin"; }

            @Override
            public int getPriority() { return 10; }

            @Override
            public void open(Player admin) {
                new CoreAdminSubGUI(plugin, admin).open();
            }
        });

        // 2. Player Manager Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "players"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#3498db:#9b59b6><bold>👤 PLAYER MANAGER</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.PLAYER_HEAD; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Akses penuh data pemain & moderasi:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Daftar Pemain Online & Filter</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Edit Saldo Rupiah & Diamond</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Edit Level & XP Progresi</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Ganti Kerajaan & Sanksi (Mute/Kick/Ban)</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Teleport & Live Inv Viewer</aqua>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Player Manager!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionscore.admin"; }

            @Override
            public int getPriority() { return 15; }

            @Override
            public void open(Player admin) {
                new PlayerManagerGUI(plugin, admin).open();
            }
        });

        // 3. Chat Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "chat"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#3498db:#2980b9><bold>💬 APEXSIONS CHAT</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.WRITABLE_BOOK; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Manajemen obrolan & moderasi staf:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Staff Reports Inbox (/reports)</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Global Mute & Clear Chat</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Server Broadcast Announcement</aqua>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Trigger Chat Mini-Game</aqua>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Chat!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionschat.staff.reports"; }

            @Override
            public int getPriority() { return 20; }

            @Override
            public void open(Player admin) {
                new ChatAdminSubGUI(plugin, admin).open();
            }
        });

        // 4. Economy Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "economy"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#2ecc71:#27ae60><bold>💰 APEXSIONS ECONOMY</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.EMERALD; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Multi-Currency, Bank & Lelang:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Transfer Saldo Rupiah & Diamond</green>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Pasar Lelang Global (/ah)</green>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Bersihkan Lelang Expired</green>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Top Saldo Terkaya Server</green>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Ekonomi!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionseconomy.admin"; }

            @Override
            public int getPriority() { return 30; }

            @Override
            public void open(Player admin) {
                new EconomyAdminSubGUI(plugin, admin).open();
            }
        });

        // 5. BattlePass Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "battlepass"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#9b59b6:#8e44ad><bold>📜 APEXSIONS BATTLEPASS</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.ENCHANTED_BOOK; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Progresi musim & quest harian/mingguan:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Berikan Premium Pass ke Pemain</light_purple>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Reset Quest Harian / Mingguan</light_purple>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Force Rotasi Toko BattlePass</light_purple>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Set Tier BP Pemain</light_purple>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel BattlePass!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionsbattlepass.admin"; }

            @Override
            public int getPriority() { return 40; }

            @Override
            public void open(Player admin) {
                new BattlePassAdminSubGUI(plugin, admin).open();
            }
        });

        // 6. Dynamic Shop Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "shop"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#e67e22:#d35400><bold>🛒 APEXSIONS SHOP</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.CHEST; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Pasar dinamis & komoditas kerajaan:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Inspeksi Toko Utama (/shop)</gold>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Status Mob Drops Langka (Sell-Only)</gold>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Picu Market Boom / Resesi</gold>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Reset Fluktuasi Harga Dinamis</gold>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Shop!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionsshop.admin"; }

            @Override
            public int getPriority() { return 50; }

            @Override
            public void open(Player admin) {
                new ShopAdminSubGUI(plugin, admin).open();
            }
        });

        // 7. Media Module
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "media"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#e67e22:#f39c12><bold>🖼️ APEXSIONS MEDIA</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.PAINTING; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Display visual banner & holografi:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Buat Banner Interaktif Baru</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Daftar Banner Aktif</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Atur Aksi Tautan Klik URL</yellow>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Toggle Raytrace Hover Glow</yellow>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Media!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionsmedia.admin"; }

            @Override
            public int getPriority() { return 60; }

            @Override
            public void open(Player admin) {
                new MediaAdminSubGUI(plugin, admin).open();
            }
        });
    }

    public void registerModule(AdminModule module) {
        if (module != null && module.getId() != null) {
            modules.put(module.getId().toLowerCase(Locale.ROOT), module);
        }
    }

    public void unregisterModule(String moduleId) {
        if (moduleId != null) {
            modules.remove(moduleId.toLowerCase(Locale.ROOT));
        }
    }

    public Optional<AdminModule> getModule(String moduleId) {
        if (moduleId == null) return Optional.empty();
        return Optional.ofNullable(modules.get(moduleId.toLowerCase(Locale.ROOT)));
    }

    public List<AdminModule> getAllModules() {
        List<AdminModule> list = new ArrayList<>(modules.values());
        list.sort(Comparator.comparingInt(AdminModule::getPriority));
        return list;
    }

    public void openHub(Player player) {
        new MasterAdminGUI(plugin, player).open();
    }
}
