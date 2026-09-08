<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;
use Illuminate\View\View;

class PlayerAdminController extends Controller
{
    /**
     * Display listing of players with search, rank, kingdom, and status filter.
     */
    public function index(Request $request): View
    {
        $search = trim((string) $request->input('q', ''));
        $rank = $request->input('rank');
        $kingdom = $request->input('kingdom');
        $status = $request->input('status'); // online, offline

        // Extract online players from cached telemetry
        $serverStatus = Cache::get('apexsions.server_status', []);
        $rawOnlineList = $serverStatus['player_list'] ?? [];
        $onlineUuids = [];
        $onlineNames = [];

        foreach ($rawOnlineList as $p) {
            if (is_array($p)) {
                if (!empty($p['uuid'])) $onlineUuids[] = strtolower($p['uuid']);
                if (!empty($p['name'])) $onlineNames[] = strtolower($p['name']);
            } elseif (is_string($p)) {
                $onlineNames[] = strtolower($p);
            }
        }

        $query = MinecraftAccount::query();

        if (!empty($search)) {
            $term = '%' . $search . '%';
            $query->where(function ($q) use ($term) {
                $q->where('minecraft_username', 'LIKE', $term)
                  ->orWhere('minecraft_uuid', 'LIKE', $term);
            });
        }

        if (!empty($rank) && $rank !== 'all') {
            $query->whereRaw('LOWER(rank) = ?', [strtolower($rank)]);
        }

        if (!empty($kingdom) && $kingdom !== 'all') {
            $query->whereRaw('UPPER(kingdom) = ?', [strtoupper($kingdom)]);
        }

        if ($status === 'online') {
            $query->where(function ($q) use ($onlineUuids, $onlineNames) {
                $q->whereIn('minecraft_uuid', $onlineUuids)
                  ->orWhereIn('minecraft_username', $onlineNames);
            });
        } elseif ($status === 'offline') {
            $query->where(function ($q) use ($onlineUuids, $onlineNames) {
                $q->whereNotIn('minecraft_uuid', $onlineUuids)
                  ->whereNotIn('minecraft_username', $onlineNames);
            });
        }

        $players = $query->orderBy('last_seen_at', 'desc')
            ->orderBy('id', 'desc')
            ->paginate(20)
            ->withQueryString();

        // Distinct filters
        $availableRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald', 'sions', 'emperor', 'sovereign', 'archon', 'ascendant', 'wanderer'];
        $availableKingdoms = ['ZENITHAR', 'SOLTERRA', 'SYLVAMOOR', 'NONE'];

        return view('apexsions-bridge::admin.players.index', [
            'players' => $players,
            'search' => $search,
            'selectedRank' => $rank,
            'selectedKingdom' => $kingdom,
            'selectedStatus' => $status,
            'onlineUuids' => $onlineUuids,
            'onlineNames' => $onlineNames,
            'availableRanks' => $availableRanks,
            'availableKingdoms' => $availableKingdoms,
            'totalCount' => MinecraftAccount::count(),
        ]);
    }

    /**
     * Show player 360 degree profile overview with modular tabs.
     */
    public function show(string $identifier): View
    {
        $account = MinecraftAccount::where('minecraft_uuid', $identifier)
            ->orWhere('minecraft_username', $identifier)
            ->orWhere('id', is_numeric($identifier) ? (int) $identifier : 0)
            ->firstOrFail();

        // Check online status & ping
        $serverStatus = Cache::get('apexsions.server_status', []);
        $rawOnlineList = $serverStatus['player_list'] ?? [];
        $isOnline = false;
        $playerPing = null;

        foreach ($rawOnlineList as $p) {
            if (is_array($p)) {
                $matchUuid = !empty($p['uuid']) && strcasecmp($p['uuid'], $account->minecraft_uuid) === 0;
                $matchName = !empty($p['name']) && strcasecmp($p['name'], $account->minecraft_username) === 0;
                if ($matchUuid || $matchName) {
                    $isOnline = true;
                    $playerPing = $p['ping'] ?? null;
                    break;
                }
            } elseif (is_string($p) && strcasecmp($p, $account->minecraft_username) === 0) {
                $isOnline = true;
                break;
            }
        }

        // Fetch recent audit logs targeting this player
        $auditLogs = AuditLog::where('target_id', $account->minecraft_uuid)
            ->orWhere('target_name', $account->minecraft_username)
            ->orderBy('id', 'desc')
            ->limit(15)
            ->get();

        // Record a read audit log for sensitive viewing if needed
        AuditService::log([
            'action' => 'PLAYER_VIEW',
            'target_type' => 'PLAYER',
            'target_id' => $account->minecraft_uuid,
            'target_name' => $account->minecraft_username,
            'reason' => 'Admin viewed player profile 360',
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'rank' => $account->rank,
                'kingdom' => $account->kingdom,
            ],
        ]);

        return view('apexsions-bridge::admin.players.show', [
            'account' => $account,
            'isOnline' => $isOnline,
            'playerPing' => $playerPing,
            'auditLogs' => $auditLogs,
        ]);
    }

    /**
     * Dispatch an idempotent safe administrative action to player.
     */
    public function executeAction(Request $request, string $identifier): RedirectResponse
    {
        $account = MinecraftAccount::where('minecraft_uuid', $identifier)
            ->orWhere('minecraft_username', $identifier)
            ->firstOrFail();

        $validated = $request->validate([
            'action_type' => ['required', 'string', 'in:DISPATCH_ALERT,TRIGGER_SYNC'],
            'message' => ['nullable', 'string', 'max:250'],
            'reason' => ['required', 'string', 'max:250'],
        ]);

        $actionType = $validated['action_type'];
        $reason = $validated['reason'];
        $actionId = (string) Str::uuid();

        if ($actionType === 'DISPATCH_ALERT') {
            $msg = trim($validated['message'] ?? 'Pesan resmi dari Administrator Apexsions.');
            $command = 'tellraw ' . $account->minecraft_username . ' ["",{"text":"[APEXSIONS ADMIN] ","color":"gold","bold":true},{"text":"' . addslashes($msg) . '","color":"yellow"}]';

            // Start audit log
            $audit = AuditService::start(
                'PLAYER_ACTION',
                'PLAYER',
                $account->minecraft_uuid,
                $account->minecraft_username,
                $reason,
                null,
                ['action_id' => $actionId, 'type' => 'DISPATCH_ALERT', 'message' => $msg],
                'WEB'
            );

            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'ALERT_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditService::success($audit, 'Delivery queued');

            return back()->with('success', 'Pesan resmi berhasil dikirim ke antrean pengiriman pemain!');
        }

        if ($actionType === 'TRIGGER_SYNC') {
            $command = 'ac sync ' . $account->minecraft_username;

            $audit = AuditService::start(
                'PLAYER_SYNC',
                'PLAYER',
                $account->minecraft_uuid,
                $account->minecraft_username,
                $reason,
                null,
                ['action_id' => $actionId, 'type' => 'TRIGGER_SYNC'],
                'WEB'
            );

            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'SYNC_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditService::success($audit, 'Sync request queued');

            return back()->with('success', 'Permintaan sinkronisasi paksa berhasil dijadwalkan!');
        }

        return back()->with('error', 'Aksi tidak didukung.');
    }
}
