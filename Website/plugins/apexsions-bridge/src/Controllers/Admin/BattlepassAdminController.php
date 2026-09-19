<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\View\View;

class BattlepassAdminController extends Controller
{
    /**
     * Display the BattlePass Season & Pass administration hub.
     */
    public function index(): View
    {
        // 1. Pass Distribution Statistics
        $totalPlayers = MinecraftAccount::count();
        $exsioPassCount = MinecraftAccount::where('battlepass_pass_name', 'LIKE', '%EXSIO%')->count();
        $sioPassCount = MinecraftAccount::where('battlepass_pass_name', 'LIKE', '%SIO%')
            ->where('battlepass_pass_name', 'NOT LIKE', '%EXSIO%')
            ->count();
        $freePassCount = max(0, $totalPlayers - ($exsioPassCount + $sioPassCount));

        $activeParticipants = MinecraftAccount::where('battlepass_tier', '>', 0)
            ->orWhere('battlepass_xp', '>', 0)
            ->count();

        // 2. Top Level Players
        $topPlayers = MinecraftAccount::where('battlepass_tier', '>', 0)
            ->orderBy('battlepass_tier', 'desc')
            ->orderBy('battlepass_xp', 'desc')
            ->take(10)
            ->get();

        // 3. Season Details (Season I: The Dawn of Ascendance)
        $season = [
            'name' => 'Season I: The Dawn of Ascendance',
            'status' => 'ACTIVE',
            'start_date' => '2026-09-01',
            'end_date' => '2026-11-30',
            'days_remaining' => 72,
            'current_week' => 3,
            'current_month' => 1,
            'max_level' => 200,
        ];

        // 4. Recent BattlePass logs
        $recentLogs = AuditLog::where('action', 'LIKE', 'BATTLEPASS_%')
            ->orderBy('created_at', 'desc')
            ->take(10)
            ->get();

        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.battlepass.index', [
            'totalPlayers' => $totalPlayers,
            'exsioPassCount' => $exsioPassCount,
            'sioPassCount' => $sioPassCount,
            'freePassCount' => $freePassCount,
            'activeParticipants' => $activeParticipants,
            'topPlayers' => $topPlayers,
            'season' => $season,
            'recentLogs' => $recentLogs,
            'serverStatus' => $serverStatus,
        ]);
    }

    /**
     * Issue a Sio or Exsio pass to a player.
     */
    public function givePass(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player_username' => ['required', 'string', 'max:32'],
            'pass_type' => ['required', 'string', 'in:sio,exsio'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $player = trim($validated['player_username']);
        $passType = strtolower($validated['pass_type']);
        $reason = $validated['reason'] ?: 'Pemberian pass BattlePass resmi oleh Admin.';

        $account = MinecraftAccount::where('minecraft_username', $player)->first();
        $targetUuid = $account ? $account->minecraft_uuid : '00000000-0000-0000-0000-000000000000';
        $oldPass = $account ? ($account->battlepass_pass_name ?: 'Free Track') : 'Unknown';

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "abp givepass {$player} {$passType}";

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'BP_PASS_' . $player . '_' . $passType . '_' . time(),
            'player_uuid' => $targetUuid,
            'player_username' => $player,
            'command' => $command,
            'status' => 'PENDING',
        ]);

        if ($account) {
            $account->update([
                'battlepass_has_premium' => true,
                'battlepass_pass_name' => strtoupper($passType) . ' Pass',
            ]);
        }

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'BATTLEPASS_PASS_GIVE',
            'target_type' => 'PLAYER',
            'target_id' => $targetUuid,
            'target_name' => $player,
            'old_value' => $oldPass,
            'new_value' => strtoupper($passType) . ' Pass',
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => [
                'command' => $command,
                'pass_type' => $passType,
                'delivery_id' => $delivery->id,
            ],
        ]);

        return back()->with('success', "Tiket " . strtoupper($passType) . " Pass berhasil diberikan kepada {$player}!");
    }

    /**
     * Adjust a player's BattlePass level or XP.
     */
    public function adjustProgress(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player_username' => ['required', 'string', 'max:32'],
            'action' => ['required', 'string', 'in:setlevel,addxp,reset'],
            'value' => ['nullable', 'integer', 'min:0', 'max:1000000'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $player = trim($validated['player_username']);
        $action = $validated['action'];
        $val = (int) ($validated['value'] ?? 1);
        $reason = $validated['reason'] ?: "Penyesuaian progresi BattlePass ({$action})";

        $account = MinecraftAccount::where('minecraft_username', $player)->first();
        $targetUuid = $account ? $account->minecraft_uuid : '00000000-0000-0000-0000-000000000000';

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';

        if ($action === 'setlevel') {
            $command = "abp setlevel {$player} {$val}";
            if ($account) $account->update(['battlepass_tier' => $val]);
        } elseif ($action === 'addxp') {
            $command = "abp addxp {$player} {$val}";
            if ($account) $account->increment('battlepass_xp', $val);
        } else {
            $command = "abp reset {$player}";
            if ($account) $account->update(['battlepass_tier' => 0, 'battlepass_xp' => 0, 'battlepass_has_premium' => false, 'battlepass_pass_name' => null]);
        }

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'BP_' . strtoupper($action) . '_' . $player . '_' . time(),
            'player_uuid' => $targetUuid,
            'player_username' => $player,
            'command' => $command,
            'status' => 'PENDING',
        ]);

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'BATTLEPASS_ADJUST',
            'target_type' => 'PLAYER',
            'target_id' => $targetUuid,
            'target_name' => $player,
            'old_value' => 'N/A',
            'new_value' => "{$action}: {$val}",
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Perintah BattlePass ({$command}) berhasil dikirim ke server!");
    }

    /**
     * Reload the Battlepass plugin configuration on the server.
     */
    public function reload(Request $request): RedirectResponse
    {
        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "abp reload";

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'BP_RELOAD_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'SERVER',
            'command' => $command,
            'status' => 'PENDING',
        ]);

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'BATTLEPASS_RELOAD',
            'target_type' => 'SERVER',
            'target_id' => 'ApexsionsBattlepass',
            'target_name' => 'BattlePass Reload',
            'old_value' => 'N/A',
            'new_value' => 'RELOADED',
            'reason' => 'Admin manual reload via WebBridge',
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Konfigurasi BattlePass di-reload secara instan di game server!");
    }
}
