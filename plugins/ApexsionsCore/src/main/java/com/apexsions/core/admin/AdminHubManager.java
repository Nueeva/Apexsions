package com.apexsions.core.admin;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.gui.admin.*;
import com.apexsions.core.gui.warp.WarpAdminGUI;
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
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Warp Manager</yellow> <gray>(Daftar & Edit Warp)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Kingdom War</yellow> <gray>(Deklarasi & Status Perang)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Region Spawn</yellow> <gray>(Atur titik spawn kerajaan)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <yellow>Level & XP</yellow> <gray>(Formula & Level Reward)</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Panel Kontrol Core!</yellow>")
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

        // 2. Chat Fallback / Hook
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
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Staff Reports GUI</aqua> <gray>(Resolusi laporan pemain)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Global Mute</aqua> <gray>(Kunci/buka obrolan server)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Channel Controls</aqua> <gray>(Global, Kingdom, Staff)</gray>"),
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

        // 3. Economy Hook
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
                        mm.deserialize("<gray>Sistem moneter & pasar pemain:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Saldo Pemain</green> <gray>(Rupiah Rp & Diamond ♦)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Auction Vault</green> <gray>(Audit & bersihkan brankas AH)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <green>Trade Taxes</green> <gray>(Pajak perdagangan kerajaan)</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk melihat informasi Ekonomi!</yellow>")
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

        // 4. BattlePass Hook
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "battlepass"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#9b59b6:#8e44ad><bold>🎫 APEXSIONS BATTLEPASS</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.DRAGON_BREATH; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Visual Editor 54-slot & Manajemen Season:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Reward Editor</light_purple> <gray>(200 Level Hadiah)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Quests Pool</light_purple> <gray>(Harian, Mingguan, Bulanan)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <light_purple>Rotation Shop</light_purple> <gray>(Toko rotasi & XP-Shop)</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka Visual Editor (/abp)!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionsbattlepass.admin"; }

            @Override
            public int getPriority() { return 40; }

            @Override
            public void open(Player admin) {
                if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                    admin.performCommand("abp");
                } else {
                    admin.sendMessage(mm.deserialize("<red>Plugin ApexsionsBattlepass tidak aktif pada server ini.</red>"));
                }
            }
        });

        // 5. Dynamic Shop Hook
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "shop"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#e67e22:#d35400><bold>🛒 APEXSIONS SHOP</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.CHEST_MINECART; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Pasar dinamis & fluktuasi harga:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Tren Pasar</gold> <gray>(Pantau komoditas BOOM/DIP)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Price Clamping</gold> <gray>(Batas aman 50% - 200%)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <gold>Pajak Kerajaan</gold> <gray>(Pajak 10% per wilayah)</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk mengelola Toko Dinamis!</yellow>")
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

        // 6. Media & Banner Hook
        registerModule(new AdminModule() {
            @Override
            public String getId() { return "media"; }

            @Override
            public Component getDisplayName() {
                return mm.deserialize("<gradient:#1abc9c:#16a085><bold>🖼️ APEXSIONS MEDIA</bold></gradient>");
            }

            @Override
            public Material getIcon() { return Material.PAINTING; }

            @Override
            public List<Component> getDescription(Player player) {
                return List.of(
                        mm.deserialize("<gray>Render banner, logo & tautan interaktif:</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Banner List</aqua> <gray>(Daftar seluruh banner aktif)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Raytrace Glow</aqua> <gray>(Line-of-sight visual feedback)</gray>"),
                        mm.deserialize("<dark_gray>•</dark_gray> <aqua>Template Replikasi</aqua> <gray>(/media place & copy)</gray>"),
                        Component.empty(),
                        mm.deserialize("<yellow>▶ Klik untuk membuka daftar Banner!</yellow>")
                );
            }

            @Override
            public String getPermission() { return "apexsionsmedia.admin"; }

            @Override
            public int getPriority() { return 60; }

            @Override
            public void open(Player admin) {
                if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsMedia")) {
                    admin.performCommand("media admin");
                } else {
                    admin.sendMessage(mm.deserialize("<red>Plugin ApexsionsMedia tidak aktif pada server ini.</red>"));
                }
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
