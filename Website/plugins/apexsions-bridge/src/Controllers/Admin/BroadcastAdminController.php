<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MaintenanceState;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\View\View;

class BroadcastAdminController extends Controller
{
    /**
     * Display the Live Broadcast & Emergency Lockdown dispatch desk.
     */
    public function index(): View
    {
        $maintenance = MaintenanceState::current();
        $serverStatus = ServerOpsService::getServerStatus();

        $recentBroadcasts = AuditLog::where('action', 'LIKE', 'BROADCAST_%')
            ->orWhere('action', 'LIKE', 'SERVER_LOCKDOWN_%')
            ->orderBy('created_at', 'desc')
            ->take(15)
            ->get();

        return view('apexsions-bridge::admin.broadcast.index', [
            'maintenance' => $maintenance,
            'serverStatus' => $serverStatus,
            'recentBroadcasts' => $recentBroadcasts,
        ]);
    }

    /**
     * Dispatch a live real-time announcement to players.
     */
    public function dispatchBroadcast(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'broadcast_type' => ['required', 'string', 'in:CHAT,TITLE,ACTIONBAR'],
            'target_audience' => ['required', 'string', 'in:GLOBAL,SOLTERRA,ZENITHAR,SYLVAMOOR'],
            'sound_effect' => ['nullable', 'string', 'in:NONE,DING,LEVELUP,DRAGON,BELL'],
            'message' => ['required', 'string', 'max:250'],
            'subtitle' => ['nullable', 'string', 'max:250'],
        ]);

        $type = $validated['broadcast_type'];
        $target = $validated['target_audience'];
        $sound = $validated['sound_effect'] ?? 'DING';
        $message = trim($validated['message']);
        $subtitle = trim($validated['subtitle'] ?? '');

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';

        // Target selector
        $selector = '@a';

        // 1. Play Sound Command (if configured)
        if ($sound !== 'NONE') {
            $soundMap = [
                'DING' => 'minecraft:entity.experience_orb.pickup',
                'LEVELUP' => 'minecraft:ui.toast.challenge_complete',
                'DRAGON' => 'minecraft:entity.ender_dragon.growl',
                'BELL' => 'minecraft:block.bell.use',
            ];
            $soundName = $soundMap[$sound] ?? 'minecraft:entity.experience_orb.pickup';
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'SOUND_' . $actionId,
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:playsound {$soundName} master {$selector}",
                'status' => 'PENDING',
            ]);
        }

        // 2. Dispatch Broadcast Command
        if ($type === 'TITLE') {
            // Title + Subtitle
            $cleanTitle = addslashes($message);
            $cleanSub = addslashes($subtitle);
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'TITLE_T_' . $actionId,
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:title {$selector} times 10 70 20",
                'status' => 'PENDING',
            ]);
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'TITLE_MSG_' . $actionId,
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:title {$selector} title [\"\",{\"text\":\"✦ APEXSIONS ✦\\n\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"{$cleanTitle}\",\"bold\":true,\"color\":\"yellow\"}]",
                'status' => 'PENDING',
            ]);
            if (!empty($cleanSub)) {
                Delivery::create([
                    'action_id' => $actionId,
                    'idempotency_key' => 'TITLE_SUB_' . $actionId,
                    'player_uuid' => '00000000-0000-0000-0000-000000000000',
                    'player_username' => 'SERVER',
                    'command' => "minecraft:title {$selector} subtitle [\"\",{\"text\":\"{$cleanSub}\",\"color\":\"gray\"}]",
                    'status' => 'PENDING',
                ]);
            }
        } elseif ($type === 'ACTIONBAR') {
            $cleanMsg = addslashes($message);
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'ACTBAR_' . $actionId,
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:title {$selector} actionbar [\"\",{\"text\":\"✦ \",\"color\":\"gold\"},{\"text\":\"{$cleanMsg}\",\"color\":\"aqua\",\"bold\":true}]",
                'status' => 'PENDING',
            ]);
        } else {
            // Standard Chat Announcement
            $cleanMsg = addslashes($message);
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'CHAT_' . $actionId,
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:tellraw {$selector} [\"\",{\"text\":\"\\n[APEXSIONS ANNOUNCEMENT] \",\"bold\":true,\"color\":\"gold\"},{\"text\":\"{$cleanMsg}\\n\",\"color\":\"yellow\"}]",
                'status' => 'PENDING',
            ]);
        }

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'BROADCAST_DISPATCH',
            'target_type' => 'SERVER',
            'target_id' => $target,
            'target_name' => "Siaran {$type} ({$target})",
            'old_value' => 'N/A',
            'new_value' => $message,
            'reason' => "Siaran langsung dari Web Admin",
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => [
                'type' => $type,
                'target' => $target,
                'sound' => $sound,
                'message' => $message,
                'subtitle' => $subtitle,
            ],
        ]);

        return back()->with('success', "Siaran langsung ({$type}) berhasil dikirim ke layar pemain in-game!");
    }

    /**
     * Emergency Server Lockdown toggle (Kill-switch).
     */
    public function toggleLockdown(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'enable' => ['required', 'boolean'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $enable = (bool) $validated['enable'];
        $reason = $validated['reason'] ?: 'Protokol Darurat Administrator (Investigasi Keamanan / Maintenance).';

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';

        $state = MaintenanceState::current();
        $state->update([
            'is_enabled' => $enable,
            'reason' => $reason,
            'enabled_by' => $actorName,
            'enabled_at' => $enable ? now() : null,
            'allow_staff' => true,
        ]);

        if ($enable) {
            // Broadcast emergency alert & kick non-staff
            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'LOCKDOWN_ALERT_' . time(),
                'player_uuid' => '00000000-0000-0000-0000-000000000000',
                'player_username' => 'SERVER',
                'command' => "minecraft:title @a title [\"\",{\"text\":\"⚠ SERVER LOCKDOWN ⚠\",\"bold\":true,\"color\":\"red\"}]",
                'status' => 'PENDING',
            ]);
        }

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => $enable ? 'SERVER_LOCKDOWN_ENABLE' : 'SERVER_LOCKDOWN_DISABLE',
            'target_type' => 'SERVER',
            'target_id' => 'EMERGENCY_LOCKDOWN',
            'target_name' => 'Server Maintenance Protocol',
            'old_value' => $enable ? 'OFF' : 'ON',
            'new_value' => $enable ? 'ON' : 'OFF',
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'DELIVERED',
            'metadata' => ['lockdown' => $enable, 'reason' => $reason],
        ]);

        $msg = $enable
            ? "⚠ PROTOKOL DARURAT AKTIF: Server berada dalam mode Lockdown / Maintenance!"
            : "Mode Lockdown dicabut. Server kembali normal untuk seluruh warga.";

        return back()->with('success', $msg);
    }
}
