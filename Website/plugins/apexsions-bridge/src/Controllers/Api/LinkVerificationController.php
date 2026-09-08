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
            // Atomically lock delivery batch
            Delivery::whereIn('id', $deliveries->pluck('id'))
                ->update([
                    'status' => 'PROCESSING',
                    'locked_at' => Carbon::now(),
                ]);
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
                $audit = \Azuriom\Plugin\ApexsionsBridge\Models\AuditLog::where('metadata->action_id', $actionId)
                    ->orWhere('metadata', 'LIKE', '%' . $actionId . '%')
                    ->first();
                if ($audit) {
                    if ($finalStatus === 'DELIVERED') {
                        \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::success($audit, 'Executed in-game via Bridge', [
                            'delivery_id' => $delivery->id,
                            'executed_at' => Carbon::now()->toIso8601String(),
                        ]);
                    } else {
                        \Azuriom\Plugin\ApexsionsBridge\Services\AuditService::failed($audit, $validated['error_message'] ?? 'Execution failed in-game', [
                            'delivery_id' => $delivery->id,
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
            'version' => ['nullable', 'string', 'max:32'],
            'ram_used_mb' => ['nullable', 'integer'],
            'ram_max_mb' => ['nullable', 'integer'],
            'free_ram_mb' => ['nullable', 'integer'],
            'uptime_seconds' => ['nullable', 'integer'],
            'loaded_chunks' => ['nullable', 'integer'],
            'entities' => ['nullable', 'integer'],
        ]);

        $data = [
            'online' => true,
            'players' => (int) $validated['online_players'],
            'max_players' => (int) ($validated['max_players'] ?? 500),
            'player_list' => $validated['players'] ?? [],
            'tps' => (float) ($validated['tps'] ?? 20.0),
            'version' => $validated['version'] ?? '26.2',
            'ram_used_mb' => (int) ($validated['ram_used_mb'] ?? 0),
            'ram_max_mb' => (int) ($validated['ram_max_mb'] ?? 0),
            'free_ram_mb' => (int) ($validated['free_ram_mb'] ?? 0),
            'uptime_seconds' => (int) ($validated['uptime_seconds'] ?? 0),
            'loaded_chunks' => (int) ($validated['loaded_chunks'] ?? 0),
            'entities' => (int) ($validated['entities'] ?? 0),
            'last_heartbeat' => now()->timestamp,
        ];

        Cache::put('apexsions.server_status', $data, now()->addMinutes(3));

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
}
