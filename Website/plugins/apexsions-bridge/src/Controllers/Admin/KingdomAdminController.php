<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\KingdomTreasury;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;
use Illuminate\View\View;

class KingdomAdminController extends Controller
{
    /**
     * Display the Kingdom & Territory War management hub.
     */
    public function index(): View
    {
        // 1. Kingdom Population Distribution
        $popZenithar = MinecraftAccount::where('kingdom', 'ZENITHAR')->count();
        $popSolterra = MinecraftAccount::where('kingdom', 'SOLTERRA')->count();
        $popSylvamoor = MinecraftAccount::where('kingdom', 'SYLVAMOOR')->count();
        $popAetherion = MinecraftAccount::where('kingdom', 'AETHERION')->count();
        $popNone = MinecraftAccount::whereNull('kingdom')->orWhere('kingdom', 'NONE')->orWhere('kingdom', '')->count();
        $totalCitizens = MinecraftAccount::count();

        // 2. Kingdom Treasuries
        $treasuries = [
            'ZENITHAR' => KingdomTreasury::firstOrCreate(
                ['kingdom_key' => 'zenithar'],
                ['kingdom_name' => 'Zenithar - The Golden Spire', 'currency' => 'rupiah', 'balance' => 2500000.0, 'total_tax_collected' => 500000.0]
            ),
            'SOLTERRA' => KingdomTreasury::firstOrCreate(
                ['kingdom_key' => 'solterra'],
                ['kingdom_name' => 'Solterra - The Sun Forged', 'currency' => 'rupiah', 'balance' => 2500000.0, 'total_tax_collected' => 500000.0]
            ),
            'SYLVAMOOR' => KingdomTreasury::firstOrCreate(
                ['kingdom_key' => 'sylvamoor'],
                ['kingdom_name' => 'Sylvamoor - The Emerald Canopy', 'currency' => 'rupiah', 'balance' => 2500000.0, 'total_tax_collected' => 500000.0]
            ),
        ];

        // 3. Active War State
        $activeWar = Cache::get('apexsions_active_war', null);

        // 4. Kingdom Monarchs
        $monarchs = Cache::get('apexsions_kingdom_monarchs', [
            'ZENITHAR' => null,
            'SOLTERRA' => null,
            'SYLVAMOOR' => null,
        ]);

        // 5. Recent Kingdom Audit Events
        $recentLogs = AuditLog::where(function ($q) {
            $q->where('action', 'LIKE', 'KINGDOM_%')
              ->orWhere('action', 'LIKE', 'WAR_%');
        })->orderBy('created_at', 'desc')->take(10)->get();

        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.kingdoms.index', [
            'popZenithar' => $popZenithar,
            'popSolterra' => $popSolterra,
            'popSylvamoor' => $popSylvamoor,
            'popAetherion' => $popAetherion,
            'popNone' => $popNone,
            'totalCitizens' => $totalCitizens,
            'treasuries' => $treasuries,
            'activeWar' => $activeWar,
            'monarchs' => $monarchs,
            'recentLogs' => $recentLogs,
            'serverStatus' => $serverStatus,
        ]);
    }

    /**
     * Launch an official Kingdom War.
     */
    public function startWar(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'kingdom_1' => ['required', 'string', 'in:SOLTERRA,ZENITHAR,SYLVAMOOR'],
            'kingdom_2' => ['required', 'string', 'in:SOLTERRA,ZENITHAR,SYLVAMOOR', 'different:kingdom_1'],
            'duration' => ['required', 'integer', 'min:5', 'max:120'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $k1 = strtoupper($validated['kingdom_1']);
        $k2 = strtoupper($validated['kingdom_2']);
        $duration = (int) $validated['duration'];
        $reason = $validated['reason'] ?: "Perang resmi dideklarasikan oleh Administrator Realm.";

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "ac war start {$k1} {$k2} {$duration}";

        // Schedule command delivery
        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'WAR_START_' . $k1 . '_' . $k2 . '_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'SERVER',
            'command' => $command,
            'status' => 'PENDING',
        ]);

        $warData = [
            'kingdom_1' => $k1,
            'kingdom_2' => $k2,
            'started_at' => now()->toIso8601String(),
            'expires_at' => now()->addMinutes($duration)->toIso8601String(),
            'duration_minutes' => $duration,
            'initiated_by' => $actorName,
            'reason' => $reason,
        ];
        Cache::put('apexsions_active_war', $warData, now()->addMinutes($duration));

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'WAR_START',
            'target_type' => 'SERVER',
            'target_id' => "{$k1}_VS_{$k2}",
            'target_name' => "Kingdom War: {$k1} vs {$k2}",
            'old_value' => 'PEACE',
            'new_value' => 'ACTIVE_WAR',
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => [
                'command' => $command,
                'delivery_id' => $delivery->id,
                'duration' => $duration,
            ],
        ]);

        return back()->with('success', "⚔ Deklarasi Perang Kerajaan ({$k1} vs {$k2}, {$duration} menit) berhasil dikirim ke server!");
    }

    /**
     * Stop the currently active Kingdom War immediately.
     */
    public function stopWar(Request $request): RedirectResponse
    {
        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';
        $command = "ac war stop";

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'WAR_STOP_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'SERVER',
            'command' => $command,
            'status' => 'PENDING',
        ]);

        $activeWar = Cache::get('apexsions_active_war');
        Cache::forget('apexsions_active_war');

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'WAR_STOP',
            'target_type' => 'SERVER',
            'target_id' => 'KINGDOM_WAR',
            'target_name' => 'Kingdom War Ceasefire',
            'old_value' => json_encode($activeWar),
            'new_value' => 'PEACE',
            'reason' => 'Perang dihentikan secara paksa oleh Administrator.',
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Gencatan senjata diberlakukan. Perang kerajaan berhasil dihentikan!");
    }

    /**
     * Appoint a player as the monarch (King) of a kingdom.
     */
    public function setKing(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'kingdom' => ['required', 'string', 'in:SOLTERRA,ZENITHAR,SYLVAMOOR'],
            'player_username' => ['required', 'string', 'max:32'],
        ]);

        $kingdom = strtoupper($validated['kingdom']);
        $player = trim($validated['player_username']);
        $command = "kingdom setking {$kingdom} {$player}";

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'SETKING_' . $kingdom . '_' . $player . '_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => $player,
            'command' => $command,
            'status' => 'PENDING',
        ]);

        // Update cached monarch list
        $monarchs = Cache::get('apexsions_kingdom_monarchs', [
            'ZENITHAR' => null,
            'SOLTERRA' => null,
            'SYLVAMOOR' => null,
        ]);
        $oldKing = $monarchs[$kingdom] ?? 'None';
        $monarchs[$kingdom] = $player;
        Cache::put('apexsions_kingdom_monarchs', $monarchs, now()->addDays(30));

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'KINGDOM_SET_KING',
            'target_type' => 'KINGDOM',
            'target_id' => $kingdom,
            'target_name' => "Raja {$kingdom}",
            'old_value' => (string) $oldKing,
            'new_value' => $player,
            'reason' => "Penunjukan Raja Kerajaan {$kingdom}",
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Pemain {$player} berhasil dinobatkan sebagai Penguasa / Raja Kerajaan {$kingdom}!");
    }

    /**
     * Unset monarch of a kingdom.
     */
    public function unsetKing(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'kingdom' => ['required', 'string', 'in:SOLTERRA,ZENITHAR,SYLVAMOOR'],
        ]);

        $kingdom = strtoupper($validated['kingdom']);
        $command = "kingdom unsetking {$kingdom}";

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Admin') : 'Console';

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'UNSETKING_' . $kingdom . '_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'SERVER',
            'command' => $command,
            'status' => 'PENDING',
        ]);

        $monarchs = Cache::get('apexsions_kingdom_monarchs', [
            'ZENITHAR' => null,
            'SOLTERRA' => null,
            'SYLVAMOOR' => null,
        ]);
        $oldKing = $monarchs[$kingdom] ?? 'None';
        $monarchs[$kingdom] = null;
        Cache::put('apexsions_kingdom_monarchs', $monarchs, now()->addDays(30));

        AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actor ? $actor->id : null,
            'actor_name' => $actorName,
            'action' => 'KINGDOM_UNSET_KING',
            'target_type' => 'KINGDOM',
            'target_id' => $kingdom,
            'target_name' => "Raja {$kingdom}",
            'old_value' => (string) $oldKing,
            'new_value' => 'VACANT',
            'reason' => "Pencopotan takhta kerajaan",
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => ['command' => $command, 'delivery_id' => $delivery->id],
        ]);

        return back()->with('success', "Takhta Kerajaan {$kingdom} berhasil dikosongkan.");
    }

    /**
     * Adjust Kingdom Treasury funds.
     */
    public function adjustTreasury(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'kingdom_key' => ['required', 'string', 'in:zenithar,solterra,sylvamoor'],
            'action' => ['required', 'string', 'in:add,subtract,set'],
            'amount' => ['required', 'numeric', 'min:0'],
            'reason' => ['nullable', 'string', 'max:250'],
        ]);

        $key = strtolower($validated['kingdom_key']);
        $action = $validated['action'];
        $amount = (float) $validated['amount'];
        $reason = $validated['reason'] ?: 'Penyesuaian kas perbendaharaan oleh Admin.';

        $treasury = KingdomTreasury::where('kingdom_key', $key)->firstOrFail();
        $oldBal = $treasury->balance;

        if ($action === 'add') {
            $newBal = $oldBal + $amount;
        } elseif ($action === 'subtract') {
            $newBal = max(0, $oldBal - $amount);
        } else {
            $newBal = $amount;
        }

        $treasury->update(['balance' => $newBal]);

        AuditLog::create([
            'action_id' => (string) Str::uuid(),
            'actor_type' => 'ADMIN',
            'actor_id' => $request->user() ? $request->user()->id : null,
            'actor_name' => $request->user() ? $request->user()->name : 'Admin',
            'action' => 'KINGDOM_TREASURY_ADJUST',
            'target_type' => 'TREASURY',
            'target_id' => $key,
            'target_name' => "Kas {$treasury->kingdom_name}",
            'old_value' => (string) $oldBal,
            'new_value' => (string) $newBal,
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'DELIVERED',
            'metadata' => ['action' => $action, 'amount' => $amount],
        ]);

        return back()->with('success', "Kas {$treasury->kingdom_name} berhasil diperbarui: Rp " . number_format($newBal, 0, ',', '.'));
    }
}
