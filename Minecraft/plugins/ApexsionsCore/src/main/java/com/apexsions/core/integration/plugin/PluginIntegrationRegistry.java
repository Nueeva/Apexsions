package com.apexsions.core.integration.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Central registry in ApexsionsCore for discovering, tracking, and evaluating custom plugin capabilities.
 */
public class PluginIntegrationRegistry {
    private static final PluginIntegrationRegistry INSTANCE = new PluginIntegrationRegistry();

    private final Map<String, PluginIntegrationManifest> manifests = new LinkedHashMap<>();

    private PluginIntegrationRegistry() {
        registerDefaults();
    }

    public static PluginIntegrationRegistry getInstance() {
        return INSTANCE;
    }

    private void registerDefaults() {
        // 1. ApexsionsCore
        manifests.put("ApexsionsCore", new PluginIntegrationManifest(
                "apexsions-core", "ApexsionsCore", "1.0.0", "CORE", "WEB_READY",
                "Pilar utama peradaban, wilayah, level titles, XP, RTP, WebBridge, dan maintenance.",
                List.of(
                        new PluginCapability("core.region.read", "Kingdom Borders", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Membaca data wilayah kerajaan", false, false),
                        new PluginCapability("core.level.read", "Player Progression", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Membaca level dan XP", false, false),
                        new PluginCapability("core.maintenance.manage", "Maintenance Control", "ACTION", "ADMIN", "AVAILABLE", "Buka/tutup gerbang pemeliharaan", true, true),
                        new PluginCapability("core.broadcast", "Global Broadcast", "ACTION", "ADMIN", "AVAILABLE", "Kirim pengumuman resmi", false, true),
                        new PluginCapability("core.reload", "Reload Core", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang konfigurasi core", true, true)
                )
        ));

        // 2. ApexsionsChat
        manifests.put("ApexsionsChat", new PluginIntegrationManifest(
                "apexsions-chat", "ApexsionsChat", "1.0.0", "CHAT", "WEB_READY",
                "Chat MiniMessage, moderasi real-time, laporan staf, profil sosial, dan game.",
                List.of(
                        new PluginCapability("chat.channels.read", "Channels Monitor", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Status channel chat", false, false),
                        new PluginCapability("chat.reports.read", "Reports Desk", "READ", "ADMIN", "AVAILABLE", "Daftar laporan pelanggaran", false, false),
                        new PluginCapability("chat.moderation.punish", "Moderation Action", "ACTION", "ADMIN", "AVAILABLE", "Sanksi kick, mute, warn, ban", true, true),
                        new PluginCapability("chat.reload", "Reload Chat", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang filter sensor kata", true, true)
                )
        ));

        // 3. ApexsionsEconomy
        manifests.put("ApexsionsEconomy", new PluginIntegrationManifest(
                "apexsions-economy", "ApexsionsEconomy", "1.0.0", "ECONOMY", "WEB_READY",
                "Sistem moneter multi-currency, rumah lelang escrow, dan perbankan.",
                List.of(
                        new PluginCapability("economy.balance.read", "Balance Ledger", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Saldo Rupiah & Diamond", false, false),
                        new PluginCapability("economy.transactions.read", "Transaction Explorer", "READ", "ADMIN", "AVAILABLE", "Audit mutasi saldo", false, false),
                        new PluginCapability("economy.auction.quarantine", "Auction Quarantine", "ACTION", "ADMIN", "AVAILABLE", "Bekukan lot lelang mencurigakan", true, true),
                        new PluginCapability("economy.reload", "Reload Economy", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang sistem ekonomi", true, true)
                )
        ));

        // 4. ApexsionsBattlepass
        manifests.put("ApexsionsBattlepass", new PluginIntegrationManifest(
                "apexsions-battlepass", "ApexsionsBattlepass", "1.0.0", "GAMEPLAY", "WEB_READY",
                "Sistem quest berkala, battlepass musiman dual-track, dan toko berputar.",
                List.of(
                        new PluginCapability("battlepass.progress.read", "Tier Progress", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Poin dan level battlepass", false, false),
                        new PluginCapability("battlepass.reload", "Reload Battlepass", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang konfigurasi quest", true, true)
                )
        ));

        // 5. ApexsionsShop
        manifests.put("ApexsionsShop", new PluginIntegrationManifest(
                "apexsions-shop", "ApexsionsShop", "1.0.0", "ECONOMY", "PARTIAL",
                "Toko komoditas kerajaan dinamis dan tren pasar peradaban.",
                List.of(
                        new PluginCapability("shop.prices.read", "Price Index", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Harga komoditas dinamis", false, false),
                        new PluginCapability("shop.reload", "Reload Shop", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang harga komoditas", true, true)
                )
        ));

        // 6. ApexsionsMedia
        manifests.put("ApexsionsMedia", new PluginIntegrationManifest(
                "apexsions-media", "ApexsionsMedia", "1.0.0", "COSMETIC", "PARTIAL",
                "Banner interaktif, logo peta, raytrace hover glow, dan creator verification suite.",
                List.of(
                        new PluginCapability("media.banners.read", "Media Banners", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Daftar banner interaktif", false, false),
                        new PluginCapability("media.reload", "Reload Media", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang banner display", true, true)
                )
        ));

        // 7. ApexsionsCrates
        manifests.put("ApexsionsCrates", new PluginIntegrationManifest(
                "apexsions-crates", "ApexsionsCrates", "1.0.0", "GAMEPLAY", "MINECRAFT_ONLY",
                "Luxury Crate, Key, and Milestone Progression Ecosystem.",
                List.of(
                        new PluginCapability("crates.catalog.read", "Crates Catalog", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Katalog peti dan kunci", false, false),
                        new PluginCapability("crates.reload", "Reload Crates", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang sistem peti", true, true)
                )
        ));

        // 8. ApexsionsCustomEnchants
        manifests.put("ApexsionsCustomEnchants", new PluginIntegrationManifest(
                "apexsions-customenchants", "ApexsionsCustomEnchants", "1.0.0", "COMBAT", "MINECRAFT_ONLY",
                "Luxury Custom Enchantments Plugin for Apexsions.",
                List.of(
                        new PluginCapability("enchants.catalog.read", "Enchantments Catalog", "READ", "PUBLIC_INTERNAL", "AVAILABLE", "Katalog sihir kustom", false, false),
                        new PluginCapability("enchants.reload", "Reload Enchants", "ACTION", "ADMIN", "AVAILABLE", "Muat ulang sihir kustom", true, true)
                )
        ));
    }

    /**
     * Builds the JSON array representing all tracked plugins, their live Bukkit enabled state, and declared capabilities.
     */
    public String buildPluginsTelemetryJson() {
        StringBuilder sb = new StringBuilder("[");
        List<String> items = new ArrayList<>();

        for (Map.Entry<String, PluginIntegrationManifest> entry : manifests.entrySet()) {
            String pluginName = entry.getKey();
            PluginIntegrationManifest manifest = entry.getValue();

            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
            boolean isEnabled = bukkitPlugin != null && bukkitPlugin.isEnabled();
            String health = isEnabled ? "HEALTHY" : (bukkitPlugin != null ? "DISABLED" : "UNKNOWN");

            items.add(manifest.toJson(isEnabled, health));
        }

        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
