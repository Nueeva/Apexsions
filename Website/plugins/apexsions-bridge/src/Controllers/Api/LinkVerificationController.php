<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Api;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Games\Minecraft\Servers\Protocol\MinecraftPing;
use Azuriom\Models\Server;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;

class LinkVerificationController extends Controller
{
    /**
     * Validate server API secret key.
     */
    protected function authenticateServer(Request $request): bool
    {
        $serverKey = setting('apexsions.server_api_key', 'apexsions_bridge_key_live_2026');
        $providedKey = (string) $request->header('X-Apexsions-Key', $request->input('api_key', ''));

        if (!empty($serverKey) && hash_equals($serverKey, $providedKey)) {
            return true;
        }

        // Secondary fallback to app.key or live key
        $liveKey = 'apexsions_bridge_key_live_2026';
        if (hash_equals($liveKey, $providedKey)) {
            return true;
        }

        $appKey = (string) config('app.key');
        if (!empty($appKey) && hash_equals($appKey, $providedKey)) {
            return true;
        }

        return false;
    }

    /**
     * Verify in-game linking request (/link <code>).
     */
    public function verify(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'pin' => ['required', 'string', 'size:6'],
            'player_uuid' => ['required', 'string', 'max:36'],
            'player_username' => ['required', 'string', 'max:32'],
            'is_bedrock' => ['nullable', 'boolean'],
        ]);

        $pin = $validated['pin'];
        $uuid = $validated['player_uuid'];
        $username = $validated['player_username'];
        $isBedrock = (bool) ($validated['is_bedrock'] ?? false);

        $account = MinecraftAccount::where('verification_code', $pin)
            ->where('verification_expires_at', '>', Carbon::now())
            ->first();

        if (!$account) {
            return response()->json([
                'status' => 'error',
                'message' => 'PIN verifikasi tidak valid atau telah kedaluwarsa.',
            ], 404);
        }

        // Validate username matches
        if (strcasecmp($account->minecraft_username, $username) !== 0) {
            Log::warning('[Apexsions Bridge] Username mismatch on link attempt', [
                'expected' => $account->minecraft_username,
                'received' => $username,
                'uuid' => $uuid,
            ]);
            return response()->json([
                'status' => 'error',
                'message' => 'Username in-game tidak sesuai dengan permintaan di web.',
            ], 422);
        }

        // Determine identity class per Blueprint §16 & §70
        $edition = $isBedrock ? 'BEDROCK' : 'JAVA';
        $authMode = $isBedrock ? 'BEDROCK_FLOODGATE' : 'JAVA_ONLINE';

        $account->update([
            'edition' => $edition,
            'auth_mode' => $authMode,
            'minecraft_uuid' => $uuid,
            'floodgate_uuid' => $isBedrock ? $uuid : null,
            'verified_at' => Carbon::now(),
            'verification_code' => null,
            'verification_expires_at' => null,
            'last_seen_at' => Carbon::now(),
        ]);

        Log::info('[Apexsions Bridge] Account linked successfully', [
            'user_id' => $account->user_id,
            'username' => $username,
            'uuid' => $uuid,
            'auth_mode' => $authMode,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Akun Minecraft berhasil ditautkan ke web!',
            'data' => [
                'user_id' => $account->user_id,
                'username' => $username,
                'uuid' => $uuid,
                'auth_mode' => $authMode,
            ],
        ]);
    }

    /**
     * Poll pending in-game command deliveries with atomic lease locking.
     */
    public function getPendingDeliveries(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        // Fetch PENDING deliveries or expired PROCESSING deliveries (lease timeout: 60s)
        $expiredThreshold = Carbon::now()->subSeconds(60);

        $deliveries = Delivery::where(function ($query) use ($expiredThreshold) {
                $query->where('status', 'PENDING')
                      ->orWhere(function ($q) use ($expiredThreshold) {
                          $q->where('status', 'PROCESSING')
                            ->where('locked_at', '<', $expiredThreshold);
                      });
            })
            ->orderBy('id', 'asc')
            ->limit(50)
            ->get();

        if ($deliveries->isNotEmpty()) {
            try {
                // Atomically lock delivery batch
                Delivery::whereIn('id', $deliveries->pluck('id'))
                    ->update([
                        'status' => 'PROCESSING',
                        'locked_at' => Carbon::now(),
                    ]);
            } catch (\Throwable $e) {
                Log::error('[Apexsions Bridge] Error lease-locking deliveries: ' . $e->getMessage());
            }
        }

        return response()->json([
            'status' => 'success',
            'deliveries' => $deliveries,
        ]);
    }

    /**
     * Mark delivery as executed or failed and resolve linked audit logs.
     */
    public function updateDeliveryStatus(Request $request, int $id): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'status' => ['required', 'in:DELIVERED,FAILED,QUEUED,SUCCESS,PROCESSING'],
            'error_message' => ['nullable', 'string', 'max:500'],
            'action_id' => ['nullable', 'string', 'max:64'],
        ]);

        $delivery = Delivery::find($id);
        if (!$delivery) {
            return response()->json(['error' => 'Delivery not found.'], 404);
        }

        $finalStatus = in_array($validated['status'], ['DELIVERED', 'SUCCESS'], true) ? 'DELIVERED' : 'FAILED';

        $delivery->update([
            'status' => $finalStatus,
            'locked_at' => null,
            'executed_at' => ($finalStatus === 'DELIVERED') ? Carbon::now() : null,
            'error_message' => $validated['error_message'] ?? null,
        ]);

        // If delivery or status payload has action_id, resolve associated AuditLog
        $actionId = !empty($validated['action_id']) ? $validated['action_id'] : $delivery->action_id;
        if (!empty($actionId)) {
            try {
                $audit = \Azuriom\Plugin\ApexsionsBridge\Models\AuditLog::where('action_id', $actionId)
                    ->orWhere('metadata->action_id', $actionId)
                    ->orWhere('metadata', 'LIKE', '%' . $actionId . '%')
                    ->first();
                if ($audit) {
                    if ($finalStatus === 'DELIVERED') {
                        \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::success($audit, $audit->new_value, [
                            'delivery_id' => $delivery->id,
                            'executed_at' => Carbon::now()->toIso8601String(),
                            'bridge_status' => 'DELIVERED',
                        ]);
                    } else {
                        \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::failed($audit, $validated['error_message'] ?? 'Execution failed in-game', [
                            'delivery_id' => $delivery->id,
                            'bridge_status' => 'FAILED',
                        ]);
                    }
                }
            } catch (\Throwable $e) {
                Log::warning('[Apexsions Bridge] Could not update linked audit log: ' . $e->getMessage());
            }
        }

        return response()->json(['status' => 'success']);
    }

    /**
     * Receive server heartbeat from ApexsionsCore.
     */
    public function heartbeat(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'online_players' => ['required', 'integer', 'min:0'],
            'max_players' => ['nullable', 'integer', 'min:1'],
            'players' => ['nullable', 'array'],
            'tps' => ['nullable', 'numeric'],
            'mspt' => ['nullable', 'numeric'],
            'version' => ['nullable', 'string', 'max:32'],
            'ram_used_mb' => ['nullable', 'integer'],
            'ram_max_mb' => ['nullable', 'integer'],
            'free_ram_mb' => ['nullable', 'integer'],
            'uptime_seconds' => ['nullable', 'integer'],
            'loaded_chunks' => ['nullable', 'integer'],
            'entities' => ['nullable', 'integer'],
            'maintenance' => ['nullable', 'boolean'],
            'plugins' => ['nullable', 'array'],
        ]);

        $data = [
            'online' => true,
            'players' => (int) $validated['online_players'],
            'max_players' => (int) ($validated['max_players'] ?? 500),
            'player_list' => $validated['players'] ?? [],
            'tps' => (float) ($validated['tps'] ?? 20.0),
            'mspt' => (float) ($validated['mspt'] ?? 15.0),
            'version' => $validated['version'] ?? '26.2',
            'ram_used_mb' => (int) ($validated['ram_used_mb'] ?? 0),
            'ram_max_mb' => (int) ($validated['ram_max_mb'] ?? 0),
            'free_ram_mb' => (int) ($validated['free_ram_mb'] ?? 0),
            'uptime_seconds' => (int) ($validated['uptime_seconds'] ?? 0),
            'loaded_chunks' => (int) ($validated['loaded_chunks'] ?? 0),
            'entities' => (int) ($validated['entities'] ?? 0),
            'maintenance' => (bool) ($validated['maintenance'] ?? false),
            'plugins' => $validated['plugins'] ?? [],
            'last_heartbeat' => now()->timestamp,
        ];

        Cache::put('apexsions.server_status', $data, now()->addMinutes(3));

        // Periodic historical metric persistence (rate limited to 1 snapshot / 60 seconds)
        try {
            $lastSnapshot = \Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric::latestSnapshot()->first();
            if (!$lastSnapshot || Carbon::now()->diffInSeconds($lastSnapshot->created_at) >= 60) {
                $serverStatus = \Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService::getServerStatus();
                \Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric::create([
                    'tps' => (float) ($validated['tps'] ?? 20.0),
                    'mspt' => (float) ($validated['mspt'] ?? 15.0),
                    'ram_used_mb' => (int) ($validated['ram_used_mb'] ?? 0),
                    'ram_max_mb' => (int) ($validated['ram_max_mb'] ?? 0),
                    'online_players' => (int) $validated['online_players'],
                    'max_players' => (int) ($validated['max_players'] ?? 500),
                    'loaded_chunks' => (int) ($validated['loaded_chunks'] ?? 0),
                    'entities' => (int) ($validated['entities'] ?? 0),
                    'server_status' => $serverStatus['status'] ?? 'ONLINE',
                    'created_at' => Carbon::now(),
                ]);
            }

            // Evaluate thresholds for persistent alerts
            \Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService::evaluateAlerts($data);

            // Synchronize custom plugins status and capabilities from telemetry
            if (!empty($validated['plugins'])) {
                \Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService::syncFromHeartbeat($validated['plugins']);
            }

            // Auto-check and expire trial ranks
            \Azuriom\Plugin\ApexsionsBridge\Services\RankService::checkAndExpireTrials();
        } catch (\Throwable $e) {
            Log::warning('[Apexsions Bridge] Error recording server metrics: ' . $e->getMessage());
        }

        return response()->json([
            'status' => 'success',
            'timestamp' => now()->timestamp,
        ]);
    }

    /**
     * Get live server status for the website UI.
     */
    public function status(): JsonResponse
    {
        $cached = Cache::get('apexsions.server_status');

        if ($cached !== null) {
            return response()->json($cached);
        }

        // 1. Check Azuriom Server model if registered in database
        try {
            $server = Server::where('home_display', true)->first() ?? Server::first();
            if ($server) {
                $data = $server->getData();
                if ($data !== null && isset($data['players'])) {
                    $payload = [
                        'online' => true,
                        'players' => (int) $data['players'],
                        'max_players' => (int) ($data['max_players'] ?? 500),
                        'player_list' => [],
                        'tps' => 20.0,
                        'version' => '26.2',
                        'ram_used_mb' => 0,
                        'ram_max_mb' => 0,
                        'uptime_seconds' => 0,
                        'loaded_chunks' => 0,
                        'entities' => 0,
                        'last_heartbeat' => now()->timestamp,
                    ];
                    return response()->json($payload);
                }
            }
        } catch (\Throwable $ignored) {
        }

        // 2. Direct socket ping to apexsions.my.id:32348 (cached 15s)
        $pingData = Cache::remember('apexsions.direct_socket_ping', now()->addSeconds(15), function () {
            try {
                $ping = new MinecraftPing('apexsions.my.id', 32348);
                $res = $ping->ping(3);
                $ping->close();

                return [
                    'online' => true,
                    'players' => (int) ($res['players']['online'] ?? 0),
                    'max_players' => (int) ($res['players']['max'] ?? 500),
                    'player_list' => array_column($res['players']['sample'] ?? [], 'name'),
                    'tps' => 20.0,
                    'version' => $res['version']['name'] ?? '26.2',
                    'ram_used_mb' => 0,
                    'ram_max_mb' => 0,
                    'uptime_seconds' => 0,
                    'loaded_chunks' => 0,
                    'entities' => 0,
                    'last_heartbeat' => now()->timestamp,
                ];
            } catch (\Throwable $e) {
                return null;
            }
        });

        if ($pingData !== null) {
            return response()->json($pingData);
        }

        return response()->json([
            'online' => false,
            'players' => 0,
            'max_players' => 500,
            'player_list' => [],
            'tps' => 20.0,
            'version' => '26.2',
            'ram_used_mb' => 0,
            'ram_max_mb' => 0,
            'uptime_seconds' => 0,
            'loaded_chunks' => 0,
            'entities' => 0,
            'last_heartbeat' => null,
        ]);
    }

    /**
     * Dispatch an in-game announcement broadcast to all players via WebBridge.
     */
    public function broadcast(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'message' => ['required', 'string', 'max:256'],
        ]);

        $senderName = auth()->user()?->name ?? 'Admin';
        $cleanMsg = trim(strip_tags($validated['message']));
        $actionId = (string) \Illuminate\Support\Str::uuid();

        // Dispatch broadcast with elegant MiniMessage golden styling
        $cmd = 'broadcast <gold><bold>[APEXSIONS PENGUMUMAN]</bold></gold> <yellow>' . addslashes($cleanMsg) . '</yellow> <gray>(oleh ' . addslashes($senderName) . ')</gray>';

        // Record audit start
        $audit = \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::start(
            'SERVER_BROADCAST',
            'GLOBAL',
            'ALL_PLAYERS',
            'ALL_PLAYERS',
            'Global announcement dispatched from Web Admin',
            null,
            [
                'action_id' => $actionId,
                'message' => $cleanMsg,
                'sender' => $senderName,
            ],
            'WEB'
        );

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'BC_' . time() . '_' . \Illuminate\Support\Str::random(6),
            'command' => $cmd,
            'status' => 'PENDING',
            'player_uuid' => 'GLOBAL',
            'player_username' => 'ALL_PLAYERS',
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Pengumuman berhasil dikirim ke antrean server Minecraft!',
            'action_id' => $actionId,
            'delivery_id' => $delivery->id,
        ]);
    }

    /**
     * Ingest in-game administrative actions into central audit log.
     */
    public function ingestAuditLog(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'actor_name' => ['required', 'string', 'max:64'],
            'actor_id' => ['nullable', 'string', 'max:64'],
            'actor_type' => ['nullable', 'string', 'max:32'],
            'action' => ['required', 'string', 'max:64'],
            'target_type' => ['nullable', 'string', 'max:32'],
            'target_id' => ['nullable', 'string', 'max:64'],
            'target_name' => ['nullable', 'string', 'max:64'],
            'old_value' => ['nullable'],
            'new_value' => ['nullable'],
            'reason' => ['nullable', 'string', 'max:255'],
            'status' => ['nullable', 'string', 'in:SUCCESS,FAILED,PENDING'],
            'metadata' => ['nullable', 'array'],
        ]);

        $log = \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::log([
            'actor_name' => $validated['actor_name'],
            'actor_id' => $validated['actor_id'] ?? null,
            'actor_type' => $validated['actor_type'] ?? 'STAFF',
            'action' => $validated['action'],
            'target_type' => $validated['target_type'] ?? 'PLAYER',
            'target_id' => $validated['target_id'] ?? null,
            'target_name' => $validated['target_name'] ?? null,
            'old_value' => $validated['old_value'] ?? null,
            'new_value' => $validated['new_value'] ?? null,
            'reason' => $validated['reason'] ?? 'In-game administrative action',
            'source' => 'INGAME',
            'status' => $validated['status'] ?? 'SUCCESS',
            'metadata' => $validated['metadata'] ?? [],
        ]);

        return response()->json([
            'status' => 'success',
            'log_id' => $log->id,
        ]);
    }

    /**
     * Ingest an in-game player report into the centralized Reports Center.
     */
    public function syncReport(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'in_game_report_id' => ['nullable', 'integer'],
            'reporter_uuid' => ['required', 'string', 'max:64'],
            'reporter_name' => ['required', 'string', 'max:64'],
            'reported_uuid' => ['required', 'string', 'max:64'],
            'reported_name' => ['required', 'string', 'max:64'],
            'reason' => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'server' => ['nullable', 'string', 'max:64'],
            'world' => ['nullable', 'string', 'max:64'],
            'status' => ['nullable', 'string', 'in:OPEN,CLAIMED,INVESTIGATING,RESOLVED,DISMISSED'],
            'priority' => ['nullable', 'string', 'in:LOW,MEDIUM,HIGH,CRITICAL'],
        ]);

        $report = \Azuriom\Plugin\ApexsionsBridge\Models\Report::create([
            'in_game_report_id' => $validated['in_game_report_id'] ?? null,
            'reporter_uuid' => $validated['reporter_uuid'],
            'reporter_name' => $validated['reporter_name'],
            'reported_uuid' => $validated['reported_uuid'],
            'reported_name' => $validated['reported_name'],
            'reason' => $validated['reason'],
            'description' => $validated['description'] ?? null,
            'server' => $validated['server'] ?? 'apexsions-survival',
            'world' => $validated['world'] ?? 'world',
            'status' => strtoupper($validated['status'] ?? 'OPEN'),
            'priority' => strtoupper($validated['priority'] ?? 'MEDIUM'),
        ]);

        return response()->json([
            'status' => 'success',
            'report_id' => $report->id,
        ]);
    }

    /**
     * Ingest an in-game punishment action into the centralized Moderation Center.
     */
    public function syncPunishment(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'action_id' => ['nullable', 'string', 'max:64'],
            'player_uuid' => ['required', 'string', 'max:64'],
            'player_name' => ['required', 'string', 'max:64'],
            'type' => ['required', 'string', 'in:WARN,MUTE,KICK,BAN'],
            'reason' => ['required', 'string'],
            'staff_name' => ['nullable', 'string', 'max:64'],
            'duration_seconds' => ['nullable', 'integer'],
        ]);

        $durationSec = $validated['duration_seconds'] ?? null;
        $expiresAt = ($durationSec && $durationSec > 0) ? Carbon::now()->addSeconds($durationSec) : null;

        $punishment = \Azuriom\Plugin\ApexsionsBridge\Models\Punishment::create([
            'action_id' => $validated['action_id'] ?? (string) \Illuminate\Support\Str::uuid(),
            'player_uuid' => $validated['player_uuid'],
            'player_name' => $validated['player_name'],
            'type' => strtoupper($validated['type']),
            'reason' => $validated['reason'],
            'staff_name' => $validated['staff_name'] ?? 'In-Game Staff',
            'duration_seconds' => $durationSec,
            'expires_at' => $expiresAt,
            'status' => 'ACTIVE',
            'source' => 'INGAME',
        ]);

        return response()->json([
            'status' => 'success',
            'punishment_id' => $punishment->id,
        ]);
    }

    /**
     * Ingest an in-game transaction record into the central Transaction Explorer.
     */
    public function syncTransaction(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'transaction_id' => ['nullable', 'string', 'max:64'],
            'type' => ['required', 'string', 'max:32'],
            'sender_uuid' => ['nullable', 'string', 'max:64'],
            'sender_name' => ['nullable', 'string', 'max:64'],
            'receiver_uuid' => ['nullable', 'string', 'max:64'],
            'receiver_name' => ['nullable', 'string', 'max:64'],
            'currency' => ['required', 'string', 'max:16'],
            'amount' => ['required', 'numeric', 'min:0'],
            'tax_amount' => ['nullable', 'numeric', 'min:0'],
            'net_amount' => ['nullable', 'numeric', 'min:0'],
            'reason' => ['nullable', 'string', 'max:255'],
            'status' => ['nullable', 'string', 'max:16'],
            'metadata' => ['nullable', 'array'],
        ]);

        $txId = $validated['transaction_id'] ?? (string) \Illuminate\Support\Str::uuid();
        $amount = (float) $validated['amount'];
        $tax = (float) ($validated['tax_amount'] ?? 0);
        $net = (float) ($validated['net_amount'] ?? ($amount - $tax));

        $tx = \Azuriom\Plugin\ApexsionsBridge\Models\Transaction::updateOrCreate(
            ['transaction_id' => $txId],
            [
                'type' => strtoupper($validated['type']),
                'sender_uuid' => $validated['sender_uuid'] ?? null,
                'sender_name' => $validated['sender_name'] ?? null,
                'receiver_uuid' => $validated['receiver_uuid'] ?? null,
                'receiver_name' => $validated['receiver_name'] ?? null,
                'currency' => strtolower($validated['currency']),
                'amount' => $amount,
                'tax_amount' => $tax,
                'net_amount' => $net,
                'reason' => $validated['reason'] ?? null,
                'status' => strtoupper($validated['status'] ?? 'COMPLETED'),
                'source' => 'IN_GAME',
                'metadata' => $validated['metadata'] ?? null,
            ]
        );

        return response()->json([
            'status' => 'success',
            'transaction_id' => $tx->transaction_id,
        ]);
    }

    /**
     * Ingest or update an in-game auction listing for the Auction Inspector.
     */
    public function syncAuction(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'auction_id' => ['required', 'string', 'max:64'],
            'seller_uuid' => ['required', 'string', 'max:64'],
            'seller_name' => ['required', 'string', 'max:64'],
            'currency' => ['required', 'string', 'max:16'],
            'price' => ['required', 'numeric', 'min:0'],
            'item_name' => ['required', 'string', 'max:128'],
            'item_data' => ['nullable', 'string'],
            'status' => ['required', 'string', 'max:16'],
            'buyer_uuid' => ['nullable', 'string', 'max:64'],
            'buyer_name' => ['nullable', 'string', 'max:64'],
            'expires_at' => ['nullable', 'date'],
        ]);

        $auction = \Azuriom\Plugin\ApexsionsBridge\Models\Auction::updateOrCreate(
            ['auction_id' => $validated['auction_id']],
            [
                'seller_uuid' => $validated['seller_uuid'],
                'seller_name' => $validated['seller_name'],
                'currency' => strtolower($validated['currency']),
                'price' => (float) $validated['price'],
                'item_name' => $validated['item_name'],
                'item_data' => $validated['item_data'] ?? null,
                'status' => strtoupper($validated['status']),
                'buyer_uuid' => $validated['buyer_uuid'] ?? null,
                'buyer_name' => $validated['buyer_name'] ?? null,
                'expires_at' => !empty($validated['expires_at']) ? Carbon::parse($validated['expires_at']) : null,
            ]
        );

        return response()->json([
            'status' => 'success',
            'auction_id' => $auction->auction_id,
        ]);
    }

    /**
     * Sync kingdom treasury balance.
     */
    public function syncKingdomTreasury(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'kingdom_key' => ['required', 'string', 'max:32'],
            'kingdom_name' => ['required', 'string', 'max:64'],
            'currency' => ['required', 'string', 'max:16'],
            'balance' => ['required', 'numeric', 'min:0'],
            'tax_collected' => ['nullable', 'numeric', 'min:0'],
        ]);

        $treasury = \Azuriom\Plugin\ApexsionsBridge\Models\KingdomTreasury::updateOrCreate(
            [
                'kingdom_key' => strtoupper($validated['kingdom_key']),
                'currency' => strtolower($validated['currency']),
            ],
            [
                'kingdom_name' => $validated['kingdom_name'],
                'balance' => (float) $validated['balance'],
                'total_tax_collected' => (float) ($validated['tax_collected'] ?? 0),
                'last_tax_collected_at' => now(),
            ]
        );

        return response()->json([
            'status' => 'success',
            'kingdom_key' => $treasury->kingdom_key,
        ]);
    }

    /**
     * Get registered custom plugins and capabilities.
     */
    public function getPlugins(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $plugins = \Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService::getAllPlugins();
        return response()->json([
            'status' => 'success',
            'count' => $plugins->count(),
            'plugins' => $plugins,
        ]);
    }

    /**
     * Receive dynamic plugin handshake registration from Minecraft server.
     */
    public function pluginHandshake(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'plugins' => ['required', 'array'],
        ]);

        \Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService::syncFromHeartbeat($validated['plugins']);

        return response()->json([
            'status' => 'success',
            'message' => 'Handshake plugin suite berhasil diproses.',
        ]);
    }

    /**
     * Ingest a unified event from Minecraft server or custom plugins into Event Intelligence.
     */
    public function syncEvent(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'event_id' => ['nullable', 'string', 'max:64'],
            'event_type' => ['required', 'string', 'max:64'],
            'source' => ['nullable', 'string', 'max:32'],
            'entity_type' => ['nullable', 'string', 'max:32'],
            'entity_id' => ['required', 'string', 'max:64'],
            'actor_type' => ['nullable', 'string', 'max:32'],
            'actor_id' => ['nullable', 'string', 'max:64'],
            'actor_name' => ['nullable', 'string', 'max:64'],
            'target_type' => ['nullable', 'string', 'max:32'],
            'target_id' => ['nullable', 'string', 'max:64'],
            'target_name' => ['nullable', 'string', 'max:64'],
            'severity' => ['nullable', 'string', 'max:16'],
            'correlation_id' => ['nullable', 'string', 'max:64'],
            'action_id' => ['nullable', 'string', 'max:64'],
            'metadata' => ['nullable', 'array'],
            'occurred_at' => ['nullable', 'date'],
        ]);

        // Security & Plugin Identity Check (Step 22 & Step 23)
        $source = strtoupper($validated['source'] ?? 'MINECRAFT');
        $pluginId = $validated['metadata']['plugin_id'] ?? null;
        if ($source === 'PLUGIN' && !empty($pluginId)) {
            $plugin = \Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin::where('plugin_id', $pluginId)->first();
            if (!$plugin) {
                return response()->json(['error' => "Plugin [{$pluginId}] tidak terdaftar di Apexsions Plugin Registry."], 403);
            }
        }

        // Duplicate & Replay check (Step 23)
        if (!empty($validated['event_id'])) {
            $existing = \Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent::where('event_id', $validated['event_id'])->first();
            if ($existing) {
                return response()->json([
                    'status' => 'duplicate',
                    'event_id' => $existing->event_id,
                    'incident_id' => $existing->incident_id,
                ]);
            }
        }

        $event = \Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService::ingest($validated);

        return response()->json([
            'status' => 'success',
            'event_id' => $event->event_id,
            'incident_id' => $event->incident_id,
        ]);
    }
}
