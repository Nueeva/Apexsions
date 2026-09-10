<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MaintenanceState;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerAlert;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class ServerOpsService
{
    /**
     * Thresholds configuration for server health evaluations.
     */
    public const TPS_WARNING_THRESHOLD = 18.0;
    public const TPS_CRITICAL_THRESHOLD = 15.0;
    public const MSPT_WARNING_THRESHOLD = 45.0;
    public const MSPT_CRITICAL_THRESHOLD = 50.0;
    public const RAM_WARNING_RATIO = 0.85;
    public const RAM_CRITICAL_RATIO = 0.95;
    public const HEARTBEAT_TIMEOUT_SECONDS = 90;
    public const BRIDGE_DELAYED_SECONDS = 180;

    /**
     * Whitelist of safe administrative server actions.
     */
    public const ALLOWED_ACTIONS = [
        'BROADCAST' => [
            'classification' => 'SAFE',
            'label' => 'Broadcast Pengumuman',
            'command_template' => 'broadcast <gold><bold>[APEXSIONS PENGUMUMAN]</bold></gold> <yellow>%s</yellow> <gray>(oleh %s)</gray>',
        ],
        'SAVE_WORLD' => [
            'classification' => 'SENSITIVE',
            'label' => 'Save World Chunks',
            'command_template' => 'save-all',
        ],
        'CLEAR_ITEMS' => [
            'classification' => 'SENSITIVE',
            'label' => 'Pembersihan Sampah Entitas / Item',
            'command_template' => 'kill @e[type=item]',
        ],
        'RELOAD_CONFIG' => [
            'classification' => 'SENSITIVE',
            'label' => 'Reload Konfigurasi Core',
            'command_template' => 'ac reload',
        ],
        'RELOAD_PLUGIN' => [
            'classification' => 'SENSITIVE',
            'label' => 'Reload Plugin Tertarget',
            'command_template' => 'ac reload',
        ],
        'CHAT_MUTE' => [
            'classification' => 'SENSITIVE',
            'label' => 'Toggle Global Chat Mute',
            'command_template' => 'apexsionschat mute',
        ],
        'CHAT_CLEAR' => [
            'classification' => 'SENSITIVE',
            'label' => 'Bersihkan Riwayat Chat Buffer',
            'command_template' => 'apexsionschat clear',
        ],
        'AH_CLEAR' => [
            'classification' => 'SENSITIVE',
            'label' => 'Bersihkan Lelang Expired (Auction House)',
            'command_template' => 'ah admin clear',
        ],
        'TOGGLE_WAR' => [
            'classification' => 'SENSITIVE',
            'label' => 'Toggle Status Perang Kerajaan',
            'command_template' => 'ac togglewar',
        ],
        'ENABLE_MAINTENANCE' => [
            'classification' => 'SENSITIVE',
            'label' => 'Aktifkan Maintenance Mode',
            'command_template' => 'maintenance enable %s',
        ],
        'DISABLE_MAINTENANCE' => [
            'classification' => 'SENSITIVE',
            'label' => 'Nonaktifkan Maintenance Mode',
            'command_template' => 'maintenance disable',
        ],
    ];

    /**
     * Determine current authoritative server status.
     * Status: ONLINE, DEGRADED, OFFLINE, or MAINTENANCE.
     */
    public static function getServerStatus(): array
    {
        // 1. Check if Maintenance Mode is active
        $maintenance = MaintenanceState::current();
        if ($maintenance->isEnabled()) {
            return [
                'status' => 'MAINTENANCE',
                'badge_class' => 'bg-warning text-dark',
                'label' => 'MAINTENANCE MODE',
                'description' => 'Akses pemain dibatasi untuk pekerjaan teknis terencana.',
                'source' => 'Maintenance Subsystem (ApexsionsCore)',
                'last_updated' => $maintenance->enabled_at ? $maintenance->enabled_at->diffForHumans() : 'Baru saja',
                'is_online' => true,
            ];
        }

        // 2. Inspect live telemetry cache
        $telemetry = Cache::get('apexsions.server_status');
        $now = Carbon::now()->timestamp;

        if ($telemetry !== null) {
            $lastHeartbeat = (int) ($telemetry['last_heartbeat'] ?? 0);
            $ageSeconds = max(0, $now - $lastHeartbeat);

            if ($ageSeconds <= self::HEARTBEAT_TIMEOUT_SECONDS) {
                $tps = (float) ($telemetry['tps'] ?? 20.0);
                $mspt = (float) ($telemetry['mspt'] ?? 15.0);

                if ($tps < self::TPS_CRITICAL_THRESHOLD || $mspt >= self::MSPT_CRITICAL_THRESHOLD) {
                    return [
                        'status' => 'DEGRADED',
                        'badge_class' => 'bg-danger text-white',
                        'label' => 'PERFORMA DEGRADED',
                        'description' => sprintf('TPS rendah (%.1f) atau beban tick engine tinggi (%.1f ms).', $tps, $mspt),
                        'source' => 'Minecraft Telemetry (WebBridge)',
                        'last_updated' => sprintf('%d detik yang lalu', $ageSeconds),
                        'is_online' => true,
                    ];
                }

                if ($tps < self::TPS_WARNING_THRESHOLD || $mspt >= self::MSPT_WARNING_THRESHOLD) {
                    return [
                        'status' => 'DEGRADED',
                        'badge_class' => 'bg-warning text-dark',
                        'label' => 'PERFORMA PERINGATAN',
                        'description' => sprintf('TPS berada di bawah standar (%.1f).', $tps),
                        'source' => 'Minecraft Telemetry (WebBridge)',
                        'last_updated' => sprintf('%d detik yang lalu', $ageSeconds),
                        'is_online' => true,
                    ];
                }

                return [
                    'status' => 'ONLINE',
                    'badge_class' => 'bg-success text-white',
                    'label' => 'REALM ONLINE',
                    'description' => 'Seluruh sistem Paper API dan engine berjalan stabil.',
                    'source' => 'Minecraft Telemetry (WebBridge)',
                    'last_updated' => sprintf('%d detik yang lalu', $ageSeconds),
                    'is_online' => true,
                ];
            }
        }

        // 3. Fallback: Socket Ping check
        $socketPing = Cache::get('apexsions.direct_socket_ping');
        if ($socketPing !== null && !empty($socketPing['online'])) {
            return [
                'status' => 'ONLINE',
                'badge_class' => 'bg-info text-dark',
                'label' => 'ONLINE (SOCKET PING)',
                'description' => 'Merespons ping soket (telemetri bridge sedang sinkronisasi).',
                'source' => 'Direct Socket Ping (Port 32348)',
                'last_updated' => 'Baru saja',
                'is_online' => true,
            ];
        }

        return [
            'status' => 'OFFLINE',
            'badge_class' => 'bg-secondary text-white',
            'label' => 'SERVER OFFLINE',
            'description' => 'Tidak ada sinyal heartbeat telemetri dalam 90 detik terakhir.',
            'source' => 'Heartbeat Monitor',
            'last_updated' => 'Tidak aktif',
            'is_online' => false,
        ];
    }

    /**
     * Retrieve structured server health metrics with explicit data sources.
     */
    public static function getHealthMetrics(): array
    {
        $telemetry = Cache::get('apexsions.server_status', []);
        $now = Carbon::now();

        $tps = (float) ($telemetry['tps'] ?? 20.0);
        $mspt = (float) ($telemetry['mspt'] ?? 15.2);
        $onlinePlayers = (int) ($telemetry['players'] ?? ($telemetry['online_players'] ?? 0));
        $maxPlayers = (int) ($telemetry['max_players'] ?? 500);
        $ramUsed = (int) ($telemetry['ram_used_mb'] ?? 0);
        $ramMax = (int) ($telemetry['ram_max_mb'] ?? 1);
        $ramFree = (int) ($telemetry['free_ram_mb'] ?? 0);
        $chunks = (int) ($telemetry['loaded_chunks'] ?? 0);
        $entities = (int) ($telemetry['entities'] ?? 0);
        $uptimeSeconds = (int) ($telemetry['uptime_seconds'] ?? 0);
        $cpuUsage = isset($telemetry['cpu_usage']) ? (float) $telemetry['cpu_usage'] : null;

        // Host Disk metrics
        $diskUsedGb = null;
        $diskTotalGb = null;
        $diskPct = 0;
        try {
            $freeBytes = @disk_free_space(base_path());
            $totalBytes = @disk_total_space(base_path());
            if ($freeBytes !== false && $totalBytes !== false && $totalBytes > 0) {
                $diskTotalGb = round($totalBytes / (1024 * 1024 * 1024), 1);
                $diskUsedGb = round(($totalBytes - $freeBytes) / (1024 * 1024 * 1024), 1);
                $diskPct = round(($diskUsedGb / $diskTotalGb) * 100);
            }
        } catch (\Throwable $ignored) {}

        $ramPct = $ramMax > 0 ? min(100, round(($ramUsed / $ramMax) * 100)) : 0;
        $tpsStability = min(100, round(($tps / 20.0) * 100));

        return [
            'tps' => [
                'name' => 'TPS (Ticks Per Second)',
                'value' => number_format($tps, 1),
                'raw' => $tps,
                'target' => '20.0',
                'stability_pct' => $tpsStability,
                'status' => $tps >= 19.5 ? 'HEALTHY' : ($tps >= 17.0 ? 'WARNING' : 'CRITICAL'),
                'source' => 'Bukkit.getTPS()[0] (Paper API)',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'mspt' => [
                'name' => 'MSPT (Tick Duration)',
                'value' => number_format($mspt, 1) . ' ms',
                'raw' => $mspt,
                'target' => '< 50.0 ms',
                'status' => $mspt < 40.0 ? 'HEALTHY' : ($mspt < 50.0 ? 'WARNING' : 'CRITICAL'),
                'source' => 'Paper Server AverageTickTime',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'memory' => [
                'name' => 'Alokasi RAM JVM',
                'value' => number_format($ramUsed) . ' MB',
                'used_mb' => $ramUsed,
                'max_mb' => $ramMax,
                'free_mb' => $ramFree,
                'percentage' => $ramPct,
                'status' => $ramPct < 85 ? 'HEALTHY' : ($ramPct < 95 ? 'WARNING' : 'CRITICAL'),
                'source' => 'JVM Runtime.getRuntime()',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'cpu' => [
                'name' => 'Beban CPU Host / Process',
                'value' => $cpuUsage !== null ? number_format($cpuUsage, 1) . '%' : 'N/A',
                'raw' => $cpuUsage,
                'status' => $cpuUsage === null ? 'UNKNOWN' : ($cpuUsage < 70 ? 'HEALTHY' : ($cpuUsage < 85 ? 'WARNING' : 'CRITICAL')),
                'source' => 'OperatingSystemMXBean',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'disk' => [
                'name' => 'Penyimpanan Disk VPS',
                'value' => $diskUsedGb !== null ? "{$diskUsedGb} GB / {$diskTotalGb} GB" : 'N/A',
                'percentage' => $diskPct,
                'status' => $diskPct < 80 ? 'HEALTHY' : ($diskPct < 90 ? 'WARNING' : 'CRITICAL'),
                'source' => 'Host Filesystem (VPS PHP Engine)',
                'last_updated' => 'Live server check',
            ],
            'players' => [
                'name' => 'Pemain Aktif',
                'value' => "{$onlinePlayers} / {$maxPlayers}",
                'count' => $onlinePlayers,
                'max' => $maxPlayers,
                'percentage' => $maxPlayers > 0 ? round(($onlinePlayers / $maxPlayers) * 100) : 0,
                'roster' => $telemetry['player_list'] ?? [],
                'source' => 'Bukkit.getOnlinePlayers()',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'world' => [
                'name' => 'Chunk & Entitas Dunia',
                'chunks' => $chunks,
                'entities' => $entities,
                'value' => number_format($chunks) . ' chunks, ' . number_format($entities) . ' entitas',
                'source' => 'World.getLoadedChunks() & getEntities()',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
            'uptime' => [
                'name' => 'Durasi Nyala (Uptime)',
                'seconds' => $uptimeSeconds,
                'formatted' => self::formatUptime($uptimeSeconds),
                'source' => 'ManagementFactory.getRuntimeMXBean()',
                'last_updated' => 'Real-time telemetry (30s poll)',
            ],
        ];
    }

    /**
     * Evaluate the health and latency of the WebBridge queue.
     */
    public static function getBridgeHealth(): array
    {
        $pendingCount = Delivery::where('status', 'PENDING')->count();
        $processingCount = Delivery::where('status', 'PROCESSING')->count();
        $deliveredToday = Delivery::where('status', 'DELIVERED')
            ->where('created_at', '>=', Carbon::today())
            ->count();
        $failedToday = Delivery::where('status', 'FAILED')
            ->where('created_at', '>=', Carbon::today())
            ->count();

        $oldestPending = Delivery::where('status', 'PENDING')
            ->orderBy('created_at', 'asc')
            ->first();

        $oldestPendingAge = $oldestPending ? Carbon::now()->diffInSeconds($oldestPending->created_at) : 0;

        $status = 'CONNECTED';
        $badgeClass = 'bg-success text-white';
        $message = 'Antrean bridge sinkron dan beroperasi optimal.';

        if ($oldestPendingAge >= self::BRIDGE_DELAYED_SECONDS) {
            $status = 'DELAYED';
            $badgeClass = 'bg-warning text-dark';
            $message = sprintf('Ada instruksi tertunda selama %d detik di antrean.', $oldestPendingAge);
        } elseif ($pendingCount > 15) {
            $status = 'DELAYED';
            $badgeClass = 'bg-warning text-dark';
            $message = 'Volume antrean tinggi, memproses perintah berkala.';
        }

        $lastHeartbeat = Cache::get('apexsions.server_status')['last_heartbeat'] ?? null;
        if ($lastHeartbeat !== null && (Carbon::now()->timestamp - $lastHeartbeat) > 300 && $pendingCount > 0) {
            $status = 'DISCONNECTED';
            $badgeClass = 'bg-danger text-white';
            $message = 'Server in-game tidak merespons polling bridge.';
        }

        return [
            'status' => $status,
            'badge_class' => $badgeClass,
            'message' => $message,
            'pending_count' => $pendingCount,
            'processing_count' => $processingCount,
            'delivered_today' => $deliveredToday,
            'failed_today' => $failedToday,
            'oldest_pending_age_seconds' => $oldestPendingAge,
            'last_sync' => $lastHeartbeat ? Carbon::createFromTimestamp($lastHeartbeat)->diffForHumans() : 'Belum pernah',
        ];
    }

    /**
     * Execute a typed, safe administrative server action.
     */
    public static function executeSafeAction(
        string $actionType,
        array $params,
        ?User $actor,
        string $reason
    ): array {
        $actionType = strtoupper($actionType);

        if (!isset(self::ALLOWED_ACTIONS[$actionType])) {
            return [
                'success' => false,
                'message' => "Tindakan server [{$actionType}] tidak terdaftar atau tidak diizinkan.",
            ];
        }

        $actionDef = self::ALLOWED_ACTIONS[$actionType];
        $actorName = $actor ? $actor->name : 'System';
        $actorId = $actor ? (string) $actor->id : '1';

        // Sensitive actions strictly mandate an audit reason
        if ($actionDef['classification'] === 'SENSITIVE' && empty(trim($reason))) {
            return [
                'success' => false,
                'message' => "Tindakan [{$actionDef['label']}] tergolong sensitif. Alasan tindakan administratif wajib diisi.",
            ];
        }

        $actionId = (string) Str::uuid();

        // 1. Build template command safely
        $command = '';
        switch ($actionType) {
            case 'BROADCAST':
                $message = trim(strip_tags($params['message'] ?? ''));
                if (empty($message)) {
                    return ['success' => false, 'message' => 'Pesan pengumuman tidak boleh kosong.'];
                }
                $command = sprintf($actionDef['command_template'], addslashes($message), addslashes($actorName));
                break;

            case 'SAVE_WORLD':
            case 'CLEAR_ITEMS':
            case 'RELOAD_CONFIG':
            case 'CHAT_MUTE':
            case 'CHAT_CLEAR':
            case 'AH_CLEAR':
            case 'TOGGLE_WAR':
                $command = $actionDef['command_template'];
                break;

            case 'RELOAD_PLUGIN':
                $targetPlugin = preg_replace('/[^a-zA-Z0-9_-]/', '', $params['plugin_name'] ?? 'ApexsionsCore');
                $pluginCmdMap = [
                    'ApexsionsCore' => 'ac reload',
                    'ApexsionsChat' => 'apexsionschat reload',
                    'ApexsionsEconomy' => 'eco reload',
                    'ApexsionsBattlepass' => 'abp reload',
                    'ApexsionsShop' => 'shop reload',
                    'ApexsionsMedia' => 'media reload',
                ];
                $command = $pluginCmdMap[$targetPlugin] ?? 'ac reload';
                break;

            case 'ENABLE_MAINTENANCE':
                $reasonEscaped = preg_replace('/[^\w\s\.\-]/', '', $reason);
                $command = sprintf($actionDef['command_template'], $reasonEscaped ?: 'Maintenance');
                break;

            case 'DISABLE_MAINTENANCE':
                $command = $actionDef['command_template'];
                break;
        }

        // 2. Start Audit Log
        $audit = AuditService::start(
            'SERVER_' . $actionType,
            'SERVER',
            'GLOBAL',
            'Realm Server Cluster',
            $reason ?: 'Tindakan operasional server berkala',
            null,
            [
                'action_id' => $actionId,
                'action_type' => $actionType,
                'action_label' => $actionDef['label'],
                'classification' => $actionDef['classification'],
                'command' => $command,
                'staff' => $actorName,
                'staff_id' => $actorId,
                'params' => $params,
            ],
            'WEB'
        );

        // 3. Enqueue Delivery with Unique action_id
        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'SRV_' . time() . '_' . Str::random(8),
            'command' => $command,
            'status' => 'PENDING',
            'player_uuid' => 'SERVER',
            'player_username' => 'CONSOLE',
        ]);

        // 4. Associate Delivery ID with Audit Log (retains PENDING status until in-game execution reports back)
        if ($audit) {
            $meta = is_array($audit->metadata) ? $audit->metadata : json_decode($audit->metadata ?? '[]', true);
            $meta['delivery_id'] = $delivery->id;
            $meta['action_id'] = $actionId;
            $audit->update(['metadata' => $meta]);
        }

        return [
            'success' => true,
            'action_id' => $actionId,
            'delivery_id' => $delivery->id,
            'message' => "Tindakan [{$actionDef['label']}] berhasil dijadwalkan ke antrean server (Action ID: {$actionId}).",
        ];
    }

    /**
     * Toggle Maintenance Mode status and synchronize with in-game bridge.
     */
    public static function toggleMaintenance(
        bool $enable,
        string $message,
        ?string $reason,
        bool $allowStaff,
        ?User $actor
    ): array {
        if ($enable && empty(trim($reason ?? ''))) {
            return [
                'success' => false,
                'message' => 'Alasan aktivasi Maintenance Mode wajib disertakan untuk rekam jejak audit.',
            ];
        }

        $actorName = $actor ? $actor->name : 'System';

        $state = MaintenanceState::current();
        $state->update([
            'is_enabled' => $enable,
            'message' => $message ?: 'Server sedang dalam pemeliharaan berkala. Silakan kembali beberapa saat lagi.',
            'reason' => $reason,
            'enabled_by' => $enable ? $actorName : null,
            'enabled_at' => $enable ? Carbon::now() : null,
            'allow_staff' => $allowStaff,
        ]);

        // Trigger safe bridge action
        $actionType = $enable ? 'ENABLE_MAINTENANCE' : 'DISABLE_MAINTENANCE';
        $res = self::executeSafeAction(
            $actionType,
            ['message' => $message, 'allow_staff' => $allowStaff],
            $actor,
            $reason ?: ($enable ? 'Maintenance mode diaktifkan' : 'Maintenance mode dinonaktifkan')
        );

        return [
            'success' => true,
            'is_enabled' => $enable,
            'message' => $enable 
                ? 'Maintenance Mode BERHASIL DIAKTIFKAN. Pemain non-staf akan dibatasi saat masuk.'
                : 'Maintenance Mode DINONAKTIFKAN. Server kembali dibuka normal untuk seluruh pemain.',
            'action_id' => $res['action_id'] ?? null,
        ];
    }

    /**
     * Get monitored catalog and capabilities of Apexsions custom plugins suite.
     */
    public static function getPluginCatalog(): array
    {
        $telemetry = Cache::get('apexsions.server_status', []);
        $samplePlugins = $telemetry['plugins'] ?? [];
        $sampleMap = [];
        foreach ($samplePlugins as $p) {
            if (isset($p['name'])) {
                $sampleMap[$p['name']] = $p;
            }
        }

        $coreSuite = [
            'ApexsionsCore' => [
                'name' => 'ApexsionsCore',
                'description' => 'Pilar utama fondasi peradaban, wilayah, level titles, XP, RTP, WebBridge, dan maintenance.',
                'version' => '1.0.0',
                'integration' => 'WEB INTEGRATED',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Kingdom Territory & War System',
                    'Region & Border Enforcement',
                    'Progression, Levels & Rewards Engine',
                    'WebBridge Telemetry & Heartbeat Daemon',
                    'Maintenance Mode & Login Guard',
                    'Sions Temporal Reconstruction Controller',
                ],
            ],
            'ApexsionsChat' => [
                'name' => 'ApexsionsChat',
                'description' => 'Sistem obrolan MiniMessage, moderasi real-time, laporan staf, profil sosial, dan game.',
                'version' => '1.0.0',
                'integration' => 'WEB INTEGRATED',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Adventure MiniMessage Formatting Engine',
                    'Channel-based Chat Architecture (Global, Kingdom, Staff)',
                    'In-Game Staff Reports Desk (Claim, Notes, Resolve)',
                    'Automated Anti-Ad & Profanity Filter',
                    'Player Social Profiles & Identity Badges',
                ],
            ],
            'ApexsionsEconomy' => [
                'name' => 'ApexsionsEconomy',
                'description' => 'Sistem moneter multi-currency, rumah lelang, karantina lelang, dan perbankan.',
                'version' => '1.0.0',
                'integration' => 'WEB INTEGRATED',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Multi-Currency Ledger (Rupiah & Diamond Commodity)',
                    'Atomic Transactions & Balance Mutex',
                    'Auction House Engine & Quarantine Moderation',
                    'Kingdom Tax Treasury Collections',
                    'P2P Player Transfers with Tax Deduction',
                ],
            ],
            'ApexsionsBattlepass' => [
                'name' => 'ApexsionsBattlepass',
                'description' => 'Sistem quest berkala, battlepass musiman, dan toko berputar.',
                'version' => '1.0.0',
                'integration' => 'WEB INTEGRATED',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Daily & Weekly Modular Quest Engine',
                    'Dual Progression Pass Tracks (Free / Apex)',
                    'Rotating Battlepass Exchange Shop',
                    'Apex Coins Web Synchronization',
                ],
            ],
            'ApexsionsShop' => [
                'name' => 'ApexsionsShop',
                'description' => 'Toko komoditas kerajaan dinamis dan tren pasar peradaban.',
                'version' => '1.0.0',
                'integration' => 'PARTIAL',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Dynamic Pricing Engine based on Supply & Demand',
                    'NPC Kingdom Trading Hubs',
                    'Global Marketplace Trends & Tax Ingestion',
                ],
            ],
            'ApexsionsMedia' => [
                'name' => 'ApexsionsMedia',
                'description' => 'Visualisasi cinematic, banner animasi, dan raytrace hover glow.',
                'version' => '1.0.0',
                'integration' => 'MINECRAFT ONLY',
                'default_status' => 'HEALTHY',
                'capabilities' => [
                    'Interactive Cinematic Holographic Banners',
                    'Raytrace Cursor Hover Glow & Particles',
                    'Direct Client URL Trigger Actions',
                ],
            ],
        ];

        $result = [];
        foreach ($coreSuite as $key => $meta) {
            $status = $meta['default_status'];
            $version = $meta['version'];
            if (isset($sampleMap[$key])) {
                $status = !empty($sampleMap[$key]['enabled']) ? 'HEALTHY' : 'DISABLED';
                $version = $sampleMap[$key]['version'] ?? $version;
            }

            $result[] = [
                'name' => $meta['name'],
                'description' => $meta['description'],
                'version' => $version,
                'status' => $status,
                'integration' => $meta['integration'],
                'capabilities' => $meta['capabilities'],
                'badge_class' => $status === 'HEALTHY' ? 'bg-success' : 'bg-danger',
            ];
        }

        return $result;
    }

    /**
     * Evaluate live telemetry against thresholds to generate persistent dashboard alerts.
     */
    public static function evaluateAlerts(array $telemetry = []): array
    {
        if (empty($telemetry)) {
            $telemetry = Cache::get('apexsions.server_status', []);
        }

        if (empty($telemetry)) {
            return [];
        }

        $createdAlerts = [];
        $tps = (float) ($telemetry['tps'] ?? 20.0);
        $mspt = (float) ($telemetry['mspt'] ?? 15.0);
        $ramUsed = (int) ($telemetry['ram_used_mb'] ?? 0);
        $ramMax = (int) ($telemetry['ram_max_mb'] ?? 1);

        // 1. Check Low TPS
        if ($tps < self::TPS_CRITICAL_THRESHOLD) {
            $createdAlerts[] = self::recordAlert(
                'LOW_TPS',
                'CRITICAL',
                sprintf('TPS server merosot tajam ke angka %.1f (Batas Kritis: %.1f). Server berisiko freeze.', $tps, self::TPS_CRITICAL_THRESHOLD),
                ['tps' => $tps]
            );
        } elseif ($tps < self::TPS_WARNING_THRESHOLD) {
            $createdAlerts[] = self::recordAlert(
                'LOW_TPS',
                'WARNING',
                sprintf('TPS server turun ke angka %.1f (Standar: 20.0). Periksa beban entitas.', $tps),
                ['tps' => $tps]
            );
        }

        // 2. Check High MSPT
        if ($mspt >= self::MSPT_CRITICAL_THRESHOLD) {
            $createdAlerts[] = self::recordAlert(
                'HIGH_MSPT',
                'CRITICAL',
                sprintf('MSPT server mencapai %.1f ms (Batas: 50.0 ms). Ticks melebihi alokasi waktu.', $mspt),
                ['mspt' => $mspt]
            );
        } elseif ($mspt >= self::MSPT_WARNING_THRESHOLD) {
            $createdAlerts[] = self::recordAlert(
                'HIGH_MSPT',
                'WARNING',
                sprintf('MSPT server meningkat ke %.1f ms.', $mspt),
                ['mspt' => $mspt]
            );
        }

        // 3. Check High RAM
        if ($ramMax > 0) {
            $ratio = $ramUsed / $ramMax;
            if ($ratio >= self::RAM_CRITICAL_RATIO) {
                $createdAlerts[] = self::recordAlert(
                    'HIGH_MEMORY',
                    'CRITICAL',
                    sprintf('Alokasi RAM JVM mencapai %.1f%% (%d MB / %d MB). Berisiko OutOfMemoryError.', $ratio * 100, $ramUsed, $ramMax),
                    ['ram_used' => $ramUsed, 'ram_max' => $ramMax]
                );
            } elseif ($ratio >= self::RAM_WARNING_RATIO) {
                $createdAlerts[] = self::recordAlert(
                    'HIGH_MEMORY',
                    'WARNING',
                    sprintf('Alokasi RAM JVM mencapai %.1f%% (%d MB / %d MB).', $ratio * 100, $ramUsed, $ramMax),
                    ['ram_used' => $ramUsed, 'ram_max' => $ramMax]
                );
            }
        }

        return array_filter($createdAlerts);
    }

    /**
     * Record a deduped server alert if not actively acknowledged within 15 minutes.
     */
    protected static function recordAlert(string $type, string $severity, string $message, array $meta = []): ?ServerAlert
    {
        // Deduplication: do not create identical active alert if one exists created within last 15 mins
        $recent = ServerAlert::where('type', $type)
            ->whereIn('status', ['ACTIVE', 'ACKNOWLEDGED'])
            ->where('created_at', '>=', Carbon::now()->subMinutes(15))
            ->first();

        if ($recent) {
            return null;
        }

        return ServerAlert::create([
            'type' => $type,
            'severity' => $severity,
            'message' => $message,
            'status' => 'ACTIVE',
            'metadata' => $meta,
        ]);
    }

    /**
     * Format uptime in seconds to human readable string.
     */
    protected static function formatUptime(int $seconds): string
    {
        if ($seconds <= 0) {
            return 'Baru saja';
        }
        $d = floor($seconds / 86400);
        $h = floor(($seconds % 86400) / 3600);
        $m = floor(($seconds % 3600) / 60);
        $s = $seconds % 60;

        if ($d > 0) return "{$d}h {$h}j {$m}m";
        if ($h > 0) return "{$h}j {$m}m {$s}d";
        return "{$m}m {$s}d";
    }
}
