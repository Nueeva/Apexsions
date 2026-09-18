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

class CrateAdminController extends Controller
{
    /**
     * Display the Crates & Loot Drop administration desk.
     */
    public function index(): View
    {
        // 1. Official Server Crates
        $crates = [
            'novice' => [
                'id' => 'novice',
                'name' => 'Novice Crate (Peti Pemula)',
                'icon' => 'CHEST',
                'badge' => 'Tier I',
                'badge_class' => 'bg-secondary',
                'price_rupiah' => 15000,
                'price_diamond' => 15,
                'pity_threshold' => 10,
                'description' => 'Peti fondasi warga baru dengan drop mineral besi, makanan, dan perlengkapan dasar.',
            ],
            'luxury' => [
                'id' => 'luxury',
                'name' => 'Luxury Crate (Peti Kemewahan)',
                'icon' => 'ENDER_CHEST',
                'badge' => 'Tier II',
                'badge_class' => 'bg-warning text-dark',
                'price_rupiah' => 50000,
                'price_diamond' => 50,
                'pity_threshold' => 25,
                'description' => 'Peti bangsawan dengan drop buku sihir custom enchants, diamond, dan kosmetik partikel.',
            ],
            'apex' => [
                'id' => 'apex',
                'name' => 'Apex Crate (Mahkota Peradaban)',
                'icon' => 'NETHERITE_BLOCK',
                'badge' => 'Tier III (Apex)',
                'badge_class' => 'bg-danger',
                'price_rupiah' => 150000,
                'price_diamond' => 150,
                'pity_threshold' => 50,
                'description' => 'Peti tertinggi dengan drop artefak mitologi, armor set kaisar, dan title gelar eksklusif.',
            ],
        ];

        // 2. Recent Key Distribution Logs
        $recentLogs = AuditLog::where('action', 'LIKE', 'CRATE_%')
            ->orderBy('created_at', 'desc')
            ->take(15)
            ->get();

        $totalKeysDistributed = AuditLog::where('action', 'CRATE_KEY_DISPENSE')
            ->count();

        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.crates.index', [
            'crates' => $crates,
            'recentLogs' => $recentLogs,
            'totalKeysDistributed' => $totalKeysDistributed,
            'serverStatus' => $serverStatus,
        ]);
    }

    /**
     * Dispense or manage crate keys for a player.
     */
    public function manageKey(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player_username' => ['required', 'string', 'max:32'],
            'crate_id' => ['required', 'string', 'in:novice,luxury,apex'],
            'action' => ['required', 'string', 'in:give,take,set'],
            'amount' => ['required', 'integer', 'min:1', 'max:100'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $player = trim($validated['player_username']);
        $crateId = strtolower($validated['crate_id']);
        $action = strtolower($validated['action']);
        $amount = (int) $validated['amount'];
        $reason = $validated['reason'] ?: "Pemberian kunci peti ({$action} {$amount}x {$crateId}) oleh Admin";

        $account = MinecraftAccount::where('minecraft_username', $player)->first();
        $targetUuid = $account ? $account->minecraft_uuid : '00000000-0000-0000-0000-000000000000';

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "crate key {$action} {$player} {$crateId} {$amount}";

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'CRATE_KEY_' . strtoupper($action) . '_' . $player . '_' . $crateId . '_' . time(),
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
            'action' => 'CRATE_KEY_DISPENSE',
            'target_type' => 'PLAYER',
            'target_id' => $targetUuid,
            'target_name' => $player,
            'old_value' => 'N/A',
            'new_value' => "{$action} {$amount} {$crateId} key(s)",
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => [
                'command' => $command,
                'crate_id' => $crateId,
                'amount' => $amount,
                'action' => $action,
                'delivery_id' => $delivery->id,
            ],
        ]);

        return back()->with('success', "Perintah kunci ({$command}) berhasil dijadwalkan ke server!");
    }

    /**
     * Reload the Crates plugin configuration on the server.
     */
    public function reload(Request $request): RedirectResponse
    {
        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "crate reload";

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'CRATE_RELOAD_' . time(),
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
            'action' => 'CRATE_RELOAD',
            'target_type' => 'SERVER',
            'target_id' => 'ApexsionsCrates',
            'target_name' => 'Crates Engine Reload',
            'old_value' => 'N/A',
            'new_value' => 'RELOADED',
            'reason' => 'Admin manual reload via WebBridge',
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Konfigurasi dan probabilitas Crates berhasil di-reload!");
    }
}
