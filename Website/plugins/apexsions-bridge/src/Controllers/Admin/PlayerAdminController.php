<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Auction;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\Punishment;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Azuriom\Plugin\ApexsionsBridge\Models\Transaction;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\RankService;
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
        $availableRanks = array_keys(RankService::getAllRanks());
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
        $isOnline = $this->isPlayerOnline($account);
        $playerPing = $this->getPlayerPing($account);

        // Fetch recent audit logs targeting this player
        $auditLogs = AuditLog::where('target_id', $account->minecraft_uuid)
            ->orWhere('target_name', $account->minecraft_username)
            ->orderBy('id', 'desc')
            ->limit(15)
            ->get();

        // Fetch rank history specifically for this player
        $rankHistory = AuditLog::where('target_id', $account->minecraft_uuid)
            ->whereIn('action', ['RANK_ASSIGN', 'RANK_CHANGE', 'RANK_RESET'])
            ->orderBy('id', 'desc')
            ->limit(15)
            ->get();

        // Fetch moderation punishments for this player
        $punishments = Punishment::where('player_uuid', $account->minecraft_uuid)
            ->orWhere('player_name', $account->minecraft_username)
            ->orderBy('created_at', 'desc')
            ->get();

        // Fetch reports filed against this player
        $reportsAgainst = Report::where('reported_uuid', $account->minecraft_uuid)
            ->orWhere('reported_name', $account->minecraft_username)
            ->orderBy('created_at', 'desc')
            ->get();

        // Fetch reports created by this player
        $reportsCreated = Report::where('reporter_uuid', $account->minecraft_uuid)
            ->orWhere('reporter_name', $account->minecraft_username)
            ->orderBy('created_at', 'desc')
            ->get();

        // Fetch economy transactions for this player
        $playerTransactions = Transaction::where('sender_uuid', $account->minecraft_uuid)
            ->orWhere('sender_name', $account->minecraft_username)
            ->orWhere('receiver_uuid', $account->minecraft_uuid)
            ->orWhere('receiver_name', $account->minecraft_username)
            ->orderBy('created_at', 'desc')
            ->limit(10)
            ->get();

        // Fetch auction listings for this player
        $playerAuctions = Auction::where('seller_uuid', $account->minecraft_uuid)
            ->orWhere('seller_name', $account->minecraft_username)
            ->orderBy('created_at', 'desc')
            ->limit(10)
            ->get();

        $allRanks = RankService::getAllRanks();
        $currentRankMeta = RankService::getRank($account->rank);

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
            'rankHistory' => $rankHistory,
            'punishments' => $punishments,
            'reportsAgainst' => $reportsAgainst,
            'reportsCreated' => $reportsCreated,
            'playerTransactions' => $playerTransactions,
            'playerAuctions' => $playerAuctions,
            'allRanks' => $allRanks,
            'currentRankMeta' => $currentRankMeta,
        ]);
    }

    /**
     * Dispatch an idempotent safe administrative action to player.
     * Mapped from Minecraft AdminGUI / PlayerInspectorGUI.
     */
    public function executeAction(Request $request, string $identifier): RedirectResponse
    {
        $account = MinecraftAccount::where('minecraft_uuid', $identifier)
            ->orWhere('minecraft_username', $identifier)
            ->firstOrFail();

        $validated = $request->validate([
            'action_type' => [
                'required',
                'string',
                'in:ASSIGN_RANK,RESET_RANK,ADJUST_BALANCE,SET_LEVEL,ADD_XP,SET_KINGDOM,RESET_KINGDOM,KICK_PLAYER,HEAL_FEED,BATTLEPASS_PASS,BATTLEPASS_TIER,DISPATCH_ALERT,TRIGGER_SYNC,SET_GAMEMODE,APPOINT_KING,REVOKE_KING',
            ],
            'reason' => ['required', 'string', 'min:3', 'max:250'],
            'message' => ['nullable', 'string', 'max:250'],
            'rank' => ['nullable', 'string'],
            'currency' => ['nullable', 'string', 'in:rupiah,diamond'],
            'sub_type' => ['nullable', 'string', 'in:give,take,set'],
            'amount' => ['nullable', 'numeric', 'min:0'],
            'level' => ['nullable', 'integer', 'min:1', 'max:100'],
            'xp_amount' => ['nullable', 'integer', 'min:1'],
            'kingdom' => ['nullable', 'string', 'in:ZENITHAR,SOLTERRA,SYLVAMOOR'],
            'pass_type' => ['nullable', 'string', 'in:sio,exsio'],
            'tier' => ['nullable', 'integer', 'min:1', 'max:100'],
            'gamemode' => ['nullable', 'string', 'in:SURVIVAL,CREATIVE,ADVENTURE,SPECTATOR'],
        ]);

        $actionType = $validated['action_type'];
        $reason = $validated['reason'];
        $isOnline = $this->isPlayerOnline($account);

        // Guard against actions that require player to be online
        $onlineRequiredActions = ['KICK_PLAYER', 'HEAL_FEED', 'DISPATCH_ALERT', 'SET_GAMEMODE'];
        if (in_array($actionType, $onlineRequiredActions, true) && !$isOnline) {
            return back()->with('error', "Pemain {$account->minecraft_username} sedang OFFLINE. Aksi ini hanya dapat dijalankan saat pemain berada di dalam server.");
        }

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Console';
        $actorId = $actor ? ($actor->id ?? null) : null;

        // 1. ASSIGN / CHANGE RANK
        if ($actionType === 'ASSIGN_RANK') {
            if (empty($validated['rank'])) {
                return back()->with('error', 'Pilihan rank wajib ditentukan.');
            }
            $result = RankService::assignRank($actor, $account, $validated['rank'], $reason);
            return back()->with($result['success'] ? 'success' : 'error', $result['message']);
        }

        // 2. RESET RANK TO WANDERER
        if ($actionType === 'RESET_RANK') {
            $result = RankService::assignRank($actor, $account, 'wanderer', $reason);
            return back()->with($result['success'] ? 'success' : 'error', $result['message']);
        }

        // 3. ADJUST ECONOMY BALANCE (Rupiah / Diamond)
        if ($actionType === 'ADJUST_BALANCE') {
            $currency = $validated['currency'] ?? 'rupiah';
            $subType = $validated['sub_type'] ?? 'give';
            $amount = (float) ($validated['amount'] ?? 0);

            if ($amount <= 0 && $subType !== 'set') {
                return back()->with('error', 'Jumlah nominal harus lebih dari 0.');
            }

            $command = "ecoadmin {$subType} {$account->minecraft_username} {$amount} {$currency}";

            // Optimistically update local account cache
            if ($currency === 'rupiah') {
                $oldBal = $account->balance_rupiah;
                if ($subType === 'give') $newBal = $oldBal + $amount;
                elseif ($subType === 'take') $newBal = max(0, $oldBal - $amount);
                else $newBal = max(0, $amount);
                $account->update(['balance_rupiah' => $newBal]);
            } else {
                $oldBal = $account->balance_diamond;
                if ($subType === 'give') $newBal = $oldBal + $amount;
                elseif ($subType === 'take') $newBal = max(0, $oldBal - $amount);
                else $newBal = max(0, $amount);
                $account->update(['balance_diamond' => $newBal]);
            }

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'ECO_' . $account->minecraft_uuid . '_' . $currency . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'ECONOMY_ADJUST',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => (string) $oldBal,
                'new_value' => (string) $newBal,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => [
                    'action_id' => $actionId,
                    'currency' => $currency,
                    'sub_type' => $subType,
                    'amount' => $amount,
                    'delivery_id' => $delivery->id,
                    'command' => $command,
                ],
            ]);

            $currLabel = ($currency === 'rupiah') ? 'Rp ' . number_format($amount, 0, ',', '.') : number_format($amount, 0, ',', '.') . ' 💎';
            return back()->with('success', "Penyesuaian saldo ({$subType} {$currLabel}) berhasil dijadwalkan ke server!");
        }

        // 4. SET LEVEL (1 - 100)
        if ($actionType === 'SET_LEVEL') {
            $newLevel = (int) ($validated['level'] ?? 1);
            $oldLevel = $account->level;
            $command = "ac setlevel {$account->minecraft_username} {$newLevel}";

            $account->update(['level' => $newLevel]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'LVL_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'LEVEL_SET',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => (string) $oldLevel,
                'new_value' => (string) $newLevel,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', "Level karakter {$account->minecraft_username} berhasil diatur ke Lv. {$newLevel}!");
        }

        // 5. ADD PROGRESSION XP
        if ($actionType === 'ADD_XP') {
            $xpAmount = (int) ($validated['xp_amount'] ?? 1000);
            $command = "ac addxp {$account->minecraft_username} {$xpAmount}";

            $account->update(['xp' => $account->xp + $xpAmount]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'XP_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'XP_ADD',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => (string) $account->xp,
                'new_value' => (string) ($account->xp + $xpAmount),
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'amount' => $xpAmount, 'command' => $command],
            ]);

            return back()->with('success', "Berhasil menambahkan " . number_format($xpAmount) . " XP ke progresi {$account->minecraft_username}!");
        }

        // 6. SET KINGDOM ALLEGIANCE
        if ($actionType === 'SET_KINGDOM') {
            $kingdom = strtoupper($validated['kingdom'] ?? 'ZENITHAR');
            $command = "ac setkingdom {$account->minecraft_username} {$kingdom}";

            $oldKingdom = $account->kingdom;
            $account->update([
                'kingdom' => $kingdom,
                'kingdom_display' => ucfirst(strtolower($kingdom)),
            ]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'KINGDOM_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'KINGDOM_CHANGE',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => $oldKingdom ?: 'NONE',
                'new_value' => $kingdom,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', "Afiliasi kerajaan {$account->minecraft_username} berhasil diubah ke {$kingdom}!");
        }

        // 7. RESET KINGDOM ALLEGIANCE
        if ($actionType === 'RESET_KINGDOM') {
            $command = "ac resetkingdom {$account->minecraft_username}";

            $oldKingdom = $account->kingdom;
            $account->update([
                'kingdom' => 'NONE',
                'kingdom_display' => 'Belum Memilih',
            ]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'RESET_K_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'KINGDOM_RESET',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => $oldKingdom ?: 'NONE',
                'new_value' => 'NONE',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', "Afiliasi kerajaan {$account->minecraft_username} berhasil di-reset (Belum Memilih)!");
        }

        // 8. KICK PLAYER (Online only)
        if ($actionType === 'KICK_PLAYER') {
            $kickReason = !empty($reason) ? $reason : 'Keputusan Administrator Realm.';
            $command = "kick {$account->minecraft_username} {$kickReason}";

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'KICK_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'PLAYER_KICK',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => 'ONLINE',
                'new_value' => 'KICKED',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', "Perintah kick untuk {$account->minecraft_username} berhasil dikirim ke server!");
        }

        // 9. HEAL & FEED (Online only)
        if ($actionType === 'HEAL_FEED') {
            $command = "minecraft:effect give {$account->minecraft_username} instant_health 1 255 true";

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'HEAL_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'PLAYER_HEAL',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => 'NORMAL',
                'new_value' => 'RESTORED',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', "Pemulihan kesehatan & lapar untuk {$account->minecraft_username} berhasil dijadwalkan!");
        }

        // 10. GIVE BATTLEPASS PASS (Sio / Exsio)
        if ($actionType === 'BATTLEPASS_PASS') {
            $passType = strtolower($validated['pass_type'] ?? 'sio');
            $command = "abp givepass {$account->minecraft_username} {$passType}";

            $account->update([
                'battlepass_has_premium' => true,
                'battlepass_pass_name' => strtoupper($passType) . ' Pass',
            ]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'BP_PASS_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'BATTLEPASS_PASS_GIVE',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => $account->battlepass_pass_name ?: 'Free Track',
                'new_value' => strtoupper($passType) . ' Pass',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'pass_type' => $passType, 'command' => $command],
            ]);

            return back()->with('success', "Pemberian " . strtoupper($passType) . " BattlePass untuk {$account->minecraft_username} berhasil dijadwalkan!");
        }

        // 11. SET BATTLEPASS TIER
        if ($actionType === 'BATTLEPASS_TIER') {
            $tier = (int) ($validated['tier'] ?? 1);
            $command = "abp setlevel {$account->minecraft_username} {$tier}";

            $oldTier = $account->battlepass_tier;
            $account->update(['battlepass_tier' => $tier]);

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'BP_TIER_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'BATTLEPASS_TIER_SET',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => (string) $oldTier,
                'new_value' => (string) $tier,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'tier' => $tier, 'command' => $command],
            ]);

            return back()->with('success', "Tier BattlePass {$account->minecraft_username} berhasil diatur ke Tier {$tier}!");
        }

        // 12. DISPATCH ALERT (tellraw)
        if ($actionType === 'DISPATCH_ALERT') {
            $msg = trim($validated['message'] ?? 'Pesan resmi dari Administrator Apexsions.');
            $command = 'tellraw ' . $account->minecraft_username . ' ["",{"text":"[APEXSIONS ADMIN] ","color":"gold","bold":true},{"text":"' . addslashes($msg) . '","color":"yellow"}]';

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'ALERT_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'PLAYER_ACTION',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => null,
                'new_value' => $msg,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'type' => 'DISPATCH_ALERT', 'message' => $msg],
            ]);

            return back()->with('success', 'Pesan resmi berhasil dikirim ke pemain!');
        }

        // 13. TRIGGER RE-SYNC
        if ($actionType === 'TRIGGER_SYNC') {
            $command = 'ac sync ' . $account->minecraft_username;

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'SYNC_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'PLAYER_SYNC',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => null,
                'new_value' => 'TRIGGERED',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'command' => $command],
            ]);

            return back()->with('success', 'Permintaan sinkronisasi paksa berhasil dijadwalkan!');
        }

        // 14. SET GAMEMODE (Online only: Survival, Creative, Adventure, Spectator)
        if ($actionType === 'SET_GAMEMODE') {
            $mode = strtoupper($validated['gamemode'] ?? 'SURVIVAL');
            $command = 'gamemode ' . strtolower($mode) . ' ' . $account->minecraft_username;

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'GM_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'PLAYER_GAMEMODE_CHANGE',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => null,
                'new_value' => $mode,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'mode' => $mode, 'command' => $command],
            ]);

            return back()->with('success', "Perubahan GameMode ({$mode}) untuk {$account->minecraft_username} berhasil dijadwalkan!");
        }

        // 15. APPOINT MONARCH / KING
        if ($actionType === 'APPOINT_KING') {
            $kingdom = strtoupper($validated['kingdom'] ?? ($account->kingdom ?: 'ZENITHAR'));
            if ($kingdom === 'NONE' || empty($kingdom)) {
                $kingdom = 'ZENITHAR';
            }
            $command = "kingdom setking {$kingdom} {$account->minecraft_username}";

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'KING_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'MONARCH_APPOINT',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => 'Rakyat Biasa',
                'new_value' => 'Raja ' . $kingdom,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'kingdom' => $kingdom, 'command' => $command],
            ]);

            return back()->with('success', "Penobatan {$account->minecraft_username} sebagai Raja Kerajaan {$kingdom} berhasil dijadwalkan!");
        }

        // 16. REVOKE MONARCH / KING
        if ($actionType === 'REVOKE_KING') {
            $kingdom = strtoupper($validated['kingdom'] ?? ($account->kingdom ?: 'ZENITHAR'));
            if ($kingdom === 'NONE' || empty($kingdom)) {
                $kingdom = 'ZENITHAR';
            }
            $command = "kingdom setking {$kingdom} Belum Ditunjuk";

            $delivery = Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'REVOKE_KING_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'ADMIN',
                'actor_id' => $actorId,
                'actor_name' => $actorName,
                'action' => 'MONARCH_REVOKE',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => 'Raja ' . $kingdom,
                'new_value' => 'Rakyat Biasa',
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'PENDING',
                'metadata' => ['delivery_id' => $delivery->id, 'kingdom' => $kingdom, 'command' => $command],
            ]);

            return back()->with('success', "Pencabutan status Raja Kerajaan {$kingdom} berhasil dijadwalkan!");
        }

        return back()->with('error', 'Aksi tidak didukung.');
    }

    /**
     * Helper to resolve if a player is currently online.
     */
    protected function isPlayerOnline(MinecraftAccount $account): bool
    {
        $serverStatus = Cache::get('apexsions.server_status', []);
        $rawOnlineList = $serverStatus['player_list'] ?? [];

        foreach ($rawOnlineList as $p) {
            if (is_array($p)) {
                $matchUuid = !empty($p['uuid']) && strcasecmp($p['uuid'], $account->minecraft_uuid) === 0;
                $matchName = !empty($p['name']) && strcasecmp($p['name'], $account->minecraft_username) === 0;
                if ($matchUuid || $matchName) {
                    return true;
                }
            } elseif (is_string($p) && strcasecmp($p, $account->minecraft_username) === 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper to resolve ping for online player.
     */
    protected function getPlayerPing(MinecraftAccount $account): ?int
    {
        $serverStatus = Cache::get('apexsions.server_status', []);
        $rawOnlineList = $serverStatus['player_list'] ?? [];

        foreach ($rawOnlineList as $p) {
            if (is_array($p)) {
                $matchUuid = !empty($p['uuid']) && strcasecmp($p['uuid'], $account->minecraft_uuid) === 0;
                $matchName = !empty($p['name']) && strcasecmp($p['name'], $account->minecraft_username) === 0;
                if ($matchUuid || $matchName) {
                    return $p['ping'] ?? null;
                }
            }
        }

        return null;
    }
}
