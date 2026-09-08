<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginCapability;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginHealthRecord;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Collection;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

class PluginRegistryService
{
    /**
     * Cache key and TTL for plugins registry.
     */
    public const CACHE_KEY = 'apexsions.plugin_registry';
    public const CACHE_TTL_MINUTES = 10;

    /**
     * Default catalog of Apexsions official custom plugin suite.
     */
    public const DEFAULT_CATALOG = [
        'apexsions-core' => [
            'name' => 'ApexsionsCore',
            'version' => '1.0.0',
            'type' => 'CORE',
            'integration_status' => 'WEB_READY',
            'description' => 'Pilar utama fondasi peradaban, wilayah kerajaan, level titles, XP, RTP, WebBridge, dan maintenance.',
            'dependencies' => ['LuckPerms', 'Vault', 'PlaceholderAPI', 'BlueMap'],
            'metadata' => [
                'main_class' => 'com.apexsions.core.ApexsionsCorePlugin',
                'api_provider' => 'com.apexsions.core.api.ApexsionsCoreProvider',
                'authors' => ['Antigravity', 'Apexsions Development Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'core.region.read', 'name' => 'Kingdom Territories & Borders', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Membaca data wilayah, bioma, dan polygon kerajaan aktif.'],
                ['capability_id' => 'core.level.read', 'name' => 'Player Level & Progression', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Membaca riwayat level, total XP, dan gelar peradaban pemain.'],
                ['capability_id' => 'core.level.adjust', 'name' => 'Modify Player Level / XP', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'description' => 'Menetapkan level atau menambah XP karakter pemain.'],
                ['capability_id' => 'core.maintenance.read', 'name' => 'Maintenance Gate Status', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Membaca status buka/tutup gerbang pemeliharaan server.'],
                ['capability_id' => 'core.maintenance.manage', 'name' => 'Toggle Maintenance Mode', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Mengontrol gerbang pemeliharaan dan pesan penolakan login.'],
                ['capability_id' => 'core.broadcast', 'name' => 'Global In-Game Announcement', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => false, 'requires_confirmation' => true, 'description' => 'Mengirim pengumuman resmi ke seluruh channel in-game.'],
                ['capability_id' => 'core.reload', 'name' => 'Reload Core Configurations', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang konfigurasi ranks, rewards, dan regions.'],
                ['capability_id' => 'core.player_level_up', 'name' => 'Player Level Up Dispatcher', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event pemicu saat pemain mencapai level baru.'],
                ['capability_id' => 'core.telemetry.metrics', 'name' => 'Server Telemetry Stream', 'type' => 'METRIC', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Aliran telemetri TPS, MSPT, RAM, Chunks, dan Entitas.'],
            ],
        ],
        'apexsions-chat' => [
            'name' => 'ApexsionsChat',
            'version' => '1.0.0',
            'type' => 'CHAT',
            'integration_status' => 'WEB_READY',
            'description' => 'Sistem obrolan MiniMessage, moderasi real-time, laporan staf in-game, profil sosial, dan game.',
            'dependencies' => ['ApexsionsCore', 'LuckPerms', 'PlaceholderAPI'],
            'metadata' => [
                'main_class' => 'com.apexsions.chat.ApexsionsChatPlugin',
                'api_provider' => 'com.apexsions.chat.api.ApexsionsChatProvider',
                'authors' => ['Antigravity', 'Apexsions Development Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'chat.channels.read', 'name' => 'Chat Channels Monitor', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat status channel chat Global, Kingdom, dan Staff.'],
                ['capability_id' => 'chat.reports.read', 'name' => 'In-Game Reports Inspector', 'type' => 'READ', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'description' => 'Memeriksa laporan pelanggaran aturan yang dibuat pemain.'],
                ['capability_id' => 'chat.reports.manage', 'name' => 'Staff Reports Desk Action', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'description' => 'Klaim, delegasikan, atau selesaikan laporan staf in-game.'],
                ['capability_id' => 'chat.moderation.punish', 'name' => 'Execute In-Game Moderation', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Menjatuhkan sanksi kick, mute, warn, atau ban.'],
                ['capability_id' => 'chat.reload', 'name' => 'Reload Chat Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang filter sensor kata dan konfigurasi channel.'],
                ['capability_id' => 'chat.report_created', 'name' => 'Report Ingestion Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat laporan pelanggaran baru diajukan.'],
                ['capability_id' => 'chat.activity.metrics', 'name' => 'Chat Volume & Filter Metrics', 'type' => 'METRIC', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Metrik frekuensi pesan dan pelanggaran filter sensor.'],
            ],
        ],
        'apexsions-economy' => [
            'name' => 'ApexsionsEconomy',
            'version' => '1.0.0',
            'type' => 'ECONOMY',
            'integration_status' => 'WEB_READY',
            'description' => 'Sistem moneter multi-currency (Rupiah & Diamond), rumah lelang escrow, karantina lelang, dan perbankan.',
            'dependencies' => ['ApexsionsCore', 'Vault', 'PlaceholderAPI'],
            'metadata' => [
                'main_class' => 'com.apexsions.economy.ApexsionsEconomy',
                'api_provider' => 'com.apexsions.economy.api.ApexsionsEconomyProvider',
                'authors' => ['ApexTeam', 'Apexsions Development Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'economy.balance.read', 'name' => 'Multi-Currency Balance Inspector', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat saldo Rupiah dan Diamond komoditas seluruh akun.'],
                ['capability_id' => 'economy.transactions.read', 'name' => 'Transaction Audit Explorer', 'type' => 'READ', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'description' => 'Menelusuri mutasi dan transfer transaksi ekonomi beruntun.'],
                ['capability_id' => 'economy.auctions.read', 'name' => 'Auction House Live Feed', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat daftar lelang aktif, kedaluwarsa, dan terjual.'],
                ['capability_id' => 'economy.balance.adjust', 'name' => 'Controlled Balance Adjustment', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Penyesuaian saldo administratif terkontrol (give/take/set).'],
                ['capability_id' => 'economy.auction.quarantine', 'name' => 'Auction Item Quarantine', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Membekukan lot lelang mencurigakan untuk investigasi.'],
                ['capability_id' => 'economy.auction.cancel', 'name' => 'Cancel Auction Listing', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Membatalkan listing lelang dan mengembalikan barang.'],
                ['capability_id' => 'economy.reload', 'name' => 'Reload Economy Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang konfigurasi mata uang dan tarif pajak pasar.'],
                ['capability_id' => 'economy.transaction_completed', 'name' => 'Transaction Complete Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat transaksi barter atau pembayaran berhasil.'],
                ['capability_id' => 'economy.supply.metrics', 'name' => 'Money Supply & Velocity Metrics', 'type' => 'METRIC', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Metrik total uang beredar dan cadangan kas kerajaan.'],
            ],
        ],
        'apexsions-battlepass' => [
            'name' => 'ApexsionsBattlepass',
            'version' => '1.0.0',
            'type' => 'GAMEPLAY',
            'integration_status' => 'WEB_READY',
            'description' => 'Sistem quest berkala, battlepass musiman dual-track (Free/Apex), dan toko berputar.',
            'dependencies' => ['ApexsionsCore', 'ApexsionsEconomy', 'Vault', 'PlaceholderAPI'],
            'metadata' => [
                'main_class' => 'com.apexsions.battlepass.ApexsionsBattlepass',
                'api_provider' => 'com.apexsions.battlepass.api.ApexsionsBattlepassProvider',
                'authors' => ['ApexTeam', 'Apexsions Development Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'battlepass.progress.read', 'name' => 'BattlePass Tier Progress', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat level pass aktif, poin terkumpul, dan Apex Coins.'],
                ['capability_id' => 'battlepass.quests.read', 'name' => 'Quest Catalog Inspector', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Memeriksa daftar quest harian dan mingguan yang aktif.'],
                ['capability_id' => 'battlepass.tier.grant', 'name' => 'Grant Premium Pass Tier', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'description' => 'Memberikan akses pass Premium/VIP ke pemain terpilih.'],
                ['capability_id' => 'battlepass.season.manage', 'name' => 'BattlePass Season Manager', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memulai atau mereset musim BattlePass baru.'],
                ['capability_id' => 'battlepass.reload', 'name' => 'Reload BattlePass Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang daftar quest, reward tiers, dan exchange shop.'],
                ['capability_id' => 'battlepass.tier_unlocked', 'name' => 'Pass Tier Unlock Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat pemain berhasil membuka tingkatan reward baru.'],
                ['capability_id' => 'battlepass.completion.metrics', 'name' => 'Quest Completion Velocity', 'type' => 'METRIC', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Metrik penyelesaian misi harian dan partisipasi musiman.'],
            ],
        ],
        'apexsions-shop' => [
            'name' => 'ApexsionsShop',
            'version' => '1.0.0',
            'type' => 'ECONOMY',
            'integration_status' => 'PARTIAL',
            'description' => 'Toko komoditas kerajaan dinamis dan tren pasar peradaban berdasarkan penawaran dan permintaan.',
            'dependencies' => ['ApexsionsCore', 'ApexsionsEconomy', 'Vault'],
            'metadata' => [
                'main_class' => 'com.apexsions.shop.ApexsionsShop',
                'api_provider' => 'com.apexsions.shop.api.ApexsionsShopProvider',
                'authors' => ['Nueeva', 'Apexsions Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'shop.prices.read', 'name' => 'Dynamic Price Index Feed', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat harga komoditas dan multiplier pasar terkini.'],
                ['capability_id' => 'shop.categories.read', 'name' => 'Shop Categories Catalog', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Memeriksa kategori belanja item dan hasil tambang.'],
                ['capability_id' => 'shop.market.reset', 'name' => 'Reset Supply/Demand Index', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Mereset kurva penawaran-permintaan komoditas ke baseline.'],
                ['capability_id' => 'shop.reload', 'name' => 'Reload Shop Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang daftar item, harga dasar, dan tarif pajak NPC.'],
                ['capability_id' => 'shop.tax_collected', 'name' => 'Shop Tax Collection Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat transaksi toko menyetorkan pajak kerajaan.'],
                ['capability_id' => 'shop.volume.metrics', 'name' => 'Trading Volume Metrics', 'type' => 'METRIC', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Metrik volume penjualan harian dan komoditas terlaris.'],
            ],
        ],
        'apexsions-media' => [
            'name' => 'ApexsionsMedia',
            'version' => '1.0.0',
            'type' => 'COSMETIC',
            'integration_status' => 'PARTIAL',
            'description' => 'Visualisasi banner interaktif cinematic, logo peta, raytrace hover glow, dan creator verification suite.',
            'dependencies' => ['ApexsionsCore'],
            'metadata' => [
                'main_class' => 'com.apexsions.media.ApexsionsMediaPlugin',
                'api_provider' => 'com.apexsions.media.api.ApexsionsMediaProvider',
                'authors' => ['Antigravity', 'Apexsions Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'media.banners.read', 'name' => 'Interactive Banners Catalog', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat daftar display banner interaktif di seluruh realm.'],
                ['capability_id' => 'media.creators.read', 'name' => 'Content Creator Profiles', 'type' => 'READ', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'description' => 'Memeriksa status verifikasi kreator konten in-game.'],
                ['capability_id' => 'media.creators.verify', 'name' => 'Creator Verification Action', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'description' => 'Memberikan badge status verifikasi kreator resmi.'],
                ['capability_id' => 'media.reload', 'name' => 'Reload Media Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang posisi banner hologram dan link aksi.'],
                ['capability_id' => 'media.banner_interacted', 'name' => 'Banner Click Interaction Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat pemain mengklik atau menatap banner interaktif.'],
            ],
        ],
        'apexsions-crates' => [
            'name' => 'ApexsionsCrates',
            'version' => '1.0.0',
            'type' => 'GAMEPLAY',
            'integration_status' => 'MINECRAFT_ONLY',
            'description' => 'Luxury Crate, Key, and Milestone Progression Ecosystem with physical and virtual crates.',
            'dependencies' => ['nightcore', 'ApexsionsCore', 'ApexsionsEconomy', 'PlaceholderAPI'],
            'metadata' => [
                'main_class' => 'com.apexsions.crates.ApexsionsCratesPlugin',
                'api_provider' => 'In-Plugin Registries & Hooks',
                'authors' => ['Apexsions Development Team', 'NightExpress'],
            ],
            'capabilities' => [
                ['capability_id' => 'crates.catalog.read', 'name' => 'Crates & Keys Catalog', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat daftar tipe peti peradaban dan kunci eksklusif.'],
                ['capability_id' => 'crates.keys.grant', 'name' => 'Grant Crate Keys Action', 'type' => 'WRITE', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'description' => 'Memberikan kunci peti hadiah kepada pemain terpilih.'],
                ['capability_id' => 'crates.reload', 'name' => 'Reload Crates Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang reward tabel dan animasi pembukaan peti.'],
                ['capability_id' => 'crates.reward_obtained', 'name' => 'Crate Open Reward Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat pemain membuka peti dan memperoleh hadiah.'],
            ],
        ],
        'apexsions-customenchants' => [
            'name' => 'ApexsionsCustomEnchants',
            'version' => '1.0.0',
            'type' => 'COMBAT',
            'integration_status' => 'MINECRAFT_ONLY',
            'description' => 'Luxury Custom Enchantments Plugin for Apexsions with Dual Enchanter and Admin Preset Tools.',
            'dependencies' => ['ApexsionsCore', 'ApexsionsEconomy', 'Vault'],
            'metadata' => [
                'main_class' => 'com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin',
                'api_provider' => 'In-Plugin Registries & Hooks',
                'authors' => ['Nueeva', 'Apexsions Team'],
            ],
            'capabilities' => [
                ['capability_id' => 'enchants.catalog.read', 'name' => 'Enchantments Catalog', 'type' => 'READ', 'access' => 'PUBLIC_INTERNAL', 'status' => 'AVAILABLE', 'description' => 'Melihat daftar buku sihir kustom dan tingkatan tier sihir.'],
                ['capability_id' => 'enchants.presets.read', 'name' => 'Armor & Tool Presets Inspector', 'type' => 'READ', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'description' => 'Memeriksa template preset kit persenjataan sihir tersimpan.'],
                ['capability_id' => 'enchants.reload', 'name' => 'Reload Enchants Configuration', 'type' => 'ACTION', 'access' => 'ADMIN', 'status' => 'AVAILABLE', 'requires_reason' => true, 'requires_confirmation' => true, 'description' => 'Memuat ulang parameter tarif enchanter dan atribut sihir.'],
                ['capability_id' => 'enchants.applied', 'name' => 'Custom Enchant Applied Event', 'type' => 'EVENT', 'access' => 'SYSTEM', 'status' => 'AVAILABLE', 'description' => 'Event saat buku sihir custom berhasil ditempa pada perlengkapan.'],
            ],
        ],
    ];

    /**
     * Seed or synchronize default custom plugins and capabilities into database.
     */
    public static function seedDefaultRegistry(): void
    {
        foreach (self::DEFAULT_CATALOG as $pluginId => $data) {
            $plugin = ApexsionsPlugin::firstOrCreate(
                ['plugin_id' => $pluginId],
                [
                    'name' => $data['name'],
                    'version' => $data['version'],
                    'type' => $data['type'],
                    'integration_status' => $data['integration_status'],
                    'status' => 'ENABLED',
                    'health_status' => 'HEALTHY',
                    'description' => $data['description'],
                    'dependencies' => $data['dependencies'] ?? [],
                    'metadata' => $data['metadata'] ?? [],
                    'last_heartbeat_at' => Carbon::now(),
                ]
            );

            if (isset($data['capabilities']) && is_array($data['capabilities'])) {
                foreach ($data['capabilities'] as $cap) {
                    PluginCapability::firstOrCreate(
                        [
                            'plugin_id' => $pluginId,
                            'capability_id' => $cap['capability_id'],
                        ],
                        [
                            'name' => $cap['name'],
                            'type' => $cap['type'] ?? 'READ',
                            'access' => $cap['access'] ?? 'ADMIN',
                            'status' => $cap['status'] ?? 'AVAILABLE',
                            'description' => $cap['description'] ?? null,
                            'requires_reason' => $cap['requires_reason'] ?? false,
                            'requires_confirmation' => $cap['requires_confirmation'] ?? false,
                            'input_schema' => $cap['input_schema'] ?? null,
                        ]
                    );
                }
            }
        }

        Cache::forget(self::CACHE_KEY);
    }

    /**
     * Synchronize plugin states dynamically from Minecraft WebBridge heartbeat telemetry.
     */
    public static function syncFromHeartbeat(array $pluginsTelemetry): void
    {
        if (empty($pluginsTelemetry)) {
            return;
        }

        // Ensure database has baseline seeded
        if (ApexsionsPlugin::count() === 0) {
            self::seedDefaultRegistry();
        }

        $now = Carbon::now();

        foreach ($pluginsTelemetry as $p) {
            $name = $p['name'] ?? '';
            if (empty($name)) {
                continue;
            }

            // Map plugin name to registered plugin_id
            $pluginId = 'apexsions-' . strtolower(str_replace('Apexsions', '', $name));
            if ($pluginId === 'apexsions-') {
                $pluginId = 'apexsions-core';
            }

            $plugin = ApexsionsPlugin::where('plugin_id', $pluginId)
                ->orWhere('name', $name)
                ->first();

            if (!$plugin) {
                // Auto-register newly discovered Apexsions plugin
                $plugin = ApexsionsPlugin::create([
                    'plugin_id' => $pluginId,
                    'name' => $name,
                    'version' => $p['version'] ?? '1.0.0',
                    'type' => 'UTILITY',
                    'status' => !empty($p['enabled']) ? 'ENABLED' : 'DISABLED',
                    'integration_status' => 'PARTIAL',
                    'health_status' => !empty($p['enabled']) ? 'HEALTHY' : 'DISABLED',
                    'description' => "Plugin custom {$name} terdeteksi dari telemetri in-game.",
                    'last_heartbeat_at' => $now,
                ]);
            } else {
                $isEnabled = !empty($p['enabled']);
                $prevHealth = $plugin->health_status;
                $newHealth = $isEnabled ? 'HEALTHY' : 'DISABLED';

                $plugin->update([
                    'version' => $p['version'] ?? $plugin->version,
                    'status' => $isEnabled ? 'ENABLED' : 'DISABLED',
                    'health_status' => $newHealth,
                    'last_heartbeat_at' => $now,
                ]);

                // Rate-limited health log (1 per 10 minutes or upon health status shift)
                if ($prevHealth !== $newHealth) {
                    PluginHealthRecord::create([
                        'plugin_id' => $plugin->plugin_id,
                        'status' => $plugin->status,
                        'health_status' => $newHealth,
                        'error_category' => $isEnabled ? null : 'RUNTIME',
                        'error_summary' => $isEnabled ? 'Plugin pulih dan beroperasi normal.' : 'Plugin dilaporkan nonaktif di server.',
                        'created_at' => $now,
                    ]);
                }
            }
        }

        Cache::forget(self::CACHE_KEY);
    }

    /**
     * Retrieve all registered plugins with optional filtering.
     */
    public static function getAllPlugins(array $filters = []): Collection
    {
        if (ApexsionsPlugin::count() === 0) {
            self::seedDefaultRegistry();
        }

        $query = ApexsionsPlugin::with('capabilities');

        if (!empty($filters['type'])) {
            $query->byType($filters['type']);
        }

        if (!empty($filters['status'])) {
            $query->where('status', strtoupper($filters['status']));
        }

        if (!empty($filters['integration'])) {
            $query->byIntegration($filters['integration']);
        }

        return $query->orderBy('name', 'asc')->get();
    }

    /**
     * Get detailed plugin by its ID.
     */
    public static function getPlugin(string $pluginId): ?ApexsionsPlugin
    {
        if (ApexsionsPlugin::count() === 0) {
            self::seedDefaultRegistry();
        }

        return ApexsionsPlugin::with(['capabilities', 'healthHistory' => function ($q) {
            $q->take(15);
        }])->where('plugin_id', $pluginId)->first();
    }

    /**
     * Check if a specific capability is available on a plugin.
     */
    public static function hasCapability(string $pluginId, string $capabilityId): bool
    {
        return PluginCapability::where('plugin_id', $pluginId)
            ->where('capability_id', $capabilityId)
            ->where('status', 'AVAILABLE')
            ->exists();
    }
}
