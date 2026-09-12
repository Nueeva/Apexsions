<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Api;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Models\Role;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class PlayerSyncController extends Controller
{
    /**
     * Authenticate Minecraft server via pre-shared bridge secret.
     */
    protected function authenticateServer(Request $request): bool
    {
        $serverKey = $request->header('X-Apexsions-Key');
        $expectedKey = config('apexsions-bridge.server_key', env('APEXSIONS_BRIDGE_KEY', 'apexsions_bridge_key_live_2026'));

        return !empty($serverKey) && hash_equals($expectedKey, $serverKey);
    }

    /**
     * Sync in-game player statistics and sync Azuriom web roles.
     */
    public function sync(Request $request): JsonResponse
    {
        if (!$this->authenticateServer($request)) {
            return response()->json(['error' => 'Unauthorized server request.'], 401);
        }

        $validated = $request->validate([
            'player_uuid' => ['required', 'string', 'max:36'],
            'player_username' => ['required', 'string', 'max:32'],
            'rank' => ['nullable', 'string', 'max:32'],
            'rank_display' => ['nullable', 'string', 'max:64'],
            'kingdom' => ['nullable', 'string', 'max:32'],
            'kingdom_display' => ['nullable', 'string', 'max:64'],
            'level' => ['nullable', 'integer', 'min:1'],
            'xp' => ['nullable', 'integer', 'min:0'],
            'required_xp' => ['nullable', 'integer', 'min:0'],
            'level_title' => ['nullable', 'string', 'max:64'],
            'active_title' => ['nullable', 'string', 'max:128'],
            'balance_rupiah' => ['nullable', 'numeric', 'min:0'],
            'balance_diamond' => ['nullable', 'numeric', 'min:0'],
            'battlepass_tier' => ['nullable', 'integer', 'min:1'],
            'battlepass_xp' => ['nullable', 'integer', 'min:0'],
            'battlepass_required_xp' => ['nullable', 'integer', 'min:0'],
            'battlepass_has_premium' => ['nullable', 'boolean'],
            'battlepass_pass_name' => ['nullable', 'string', 'max:64'],
            'apex_coins' => ['nullable', 'numeric', 'min:0'],
            'unlocked_titles' => ['nullable', 'array'],
            'is_bedrock' => ['nullable', 'boolean'],
            'edition' => ['nullable', 'string', 'max:16'],
            'auth_mode' => ['nullable', 'string', 'max:32'],
        ]);

        $uuid = $validated['player_uuid'];
        $username = $validated['player_username'];

        // Auto-detect Bedrock edition based on explicit flag, username prefix, or Floodgate UUID format
        $isBedrock = (bool) ($request->input('is_bedrock') ?? false)
            || strtoupper($request->input('edition') ?? '') === 'BEDROCK'
            || str_starts_with($username, '.')
            || str_starts_with($username, '*')
            || str_starts_with($uuid, '00000000-0000-0000-');

        $edition = $isBedrock ? 'BEDROCK' : 'JAVA';
        $authMode = $isBedrock ? 'BEDROCK_FLOODGATE' : 'JAVA_ONLINE';

        // Find linked account by UUID or username
        $account = MinecraftAccount::where('minecraft_uuid', $uuid)
            ->orWhere('minecraft_username', $username)
            ->first();

        if (!$account) {
            $account = new MinecraftAccount();
            $account->edition = $edition;
            $account->auth_mode = $authMode;
            $account->user_id = null;
            $account->verified_at = null;
        }

        // Auto-check expired trials
        \Azuriom\Plugin\ApexsionsBridge\Services\RankService::checkAndExpireTrials();

        $inGameRank = strtolower($validated['rank'] ?? 'wanderer');
        $inGameRankMeta = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::getRank($inGameRank);
        $inGameWeight = $inGameRankMeta['weight'] ?? 10;

        $currentWebRank = strtolower($account->rank ?? 'wanderer');
        $currentWebMeta = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::getRank($currentWebRank);
        $currentWebWeight = $currentWebMeta['weight'] ?? 10;
        $isWebPermanent = $account->isPermanentRank();

        // Determine authoritative rank:
        if ($currentWebWeight > $inGameWeight && ($isWebPermanent || !$account->isRankExpired())) {
            $finalRank = $currentWebRank;
            $finalRankDisplay = $currentWebMeta['display_name'] ?? ucfirst($currentWebRank);

            // Re-deliver command if no pending/processing delivery exists
            $hasPending = \Azuriom\Plugin\ApexsionsBridge\Models\Delivery::where('player_uuid', $uuid)
                ->whereIn('status', ['PENDING', 'PROCESSING'])
                ->where('command', 'LIKE', "%parent set {$finalRank}%")
                ->exists();

            if (!$hasPending) {
                \Azuriom\Plugin\ApexsionsBridge\Models\Delivery::create([
                    'action_id' => (string) \Illuminate\Support\Str::uuid(),
                    'idempotency_key' => 'AUTOSYNC_' . $uuid . '_' . $finalRank . '_' . time(),
                    'player_uuid' => $uuid,
                    'player_username' => $username,
                    'command' => "lp user {$username} parent set {$finalRank}",
                    'status' => 'PENDING',
                ]);
            }
        } else {
            $finalRank = $inGameRank;
            $finalRankDisplay = $validated['rank_display'] ?? ($inGameRankMeta['display_name'] ?? ucfirst($inGameRank));
        }

        $staffRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald'];
        $syncedKingdom = strtoupper($validated['kingdom'] ?? 'NONE');
        $syncedKingdomDisplay = $validated['kingdom_display'] ?? 'Belum Memilih';

        // Sions adalah reruntuhan kuno terlarang (Terra Interdicta) dan bukan kerajaan aktif
        if ($syncedKingdom === 'SIONS') {
            $syncedKingdom = 'NONE';
            $syncedKingdomDisplay = 'Belum Memilih';
        }

        // Entitas The Aetherial Conclave (Dimensi Atas) fallback ke Aetherion
        if (in_array(strtolower($finalRank), $staffRanks, true) && ($syncedKingdom === 'NONE' || empty($syncedKingdom) || in_array($syncedKingdom, ['ZENITHAR', 'SOLTERRA', 'SYLVAMOOR'], true))) {
            $syncedKingdom = 'AETHERION';
            $syncedKingdomDisplay = 'Aetherion (The Conclave)';
        }

        $updateData = [
            'minecraft_uuid' => $uuid,
            'minecraft_username' => $username,
            'edition' => $edition,
            'auth_mode' => $authMode,
            'floodgate_uuid' => $isBedrock ? $uuid : ($account->floodgate_uuid ?? null),
            'rank' => $finalRank,
            'rank_display' => $finalRankDisplay,
            'kingdom' => $syncedKingdom,
            'kingdom_display' => $syncedKingdomDisplay,
            'level' => (int) ($validated['level'] ?? 1),
            'xp' => (int) ($validated['xp'] ?? 0),
            'required_xp' => (int) ($validated['required_xp'] ?? 100),
            'level_title' => isset($validated['level_title']) ? trim(preg_replace('/<[^>]*>/', '', $validated['level_title'])) : null,
            'active_title' => isset($validated['active_title']) ? trim(preg_replace('/<[^>]*>/', '', $validated['active_title'])) : null,
            'balance_rupiah' => (float) ($validated['balance_rupiah'] ?? 0),
            'balance_diamond' => (float) ($validated['balance_diamond'] ?? 0),
            'battlepass_tier' => (int) ($validated['battlepass_tier'] ?? 1),
            'battlepass_xp' => (int) ($validated['battlepass_xp'] ?? 0),
            'battlepass_required_xp' => (int) ($validated['battlepass_required_xp'] ?? 100),
            'battlepass_has_premium' => (bool) ($validated['battlepass_has_premium'] ?? false),
            'apex_coins' => (int) ($validated['apex_coins'] ?? 0),
            'last_seen_at' => Carbon::now(),
        ];

        if (!empty($validated['battlepass_pass_name'])) {
            $updateData['battlepass_pass_name'] = trim($validated['battlepass_pass_name']);
        }

        if (isset($validated['unlocked_titles'])) {
            $updateData['unlocked_titles'] = $validated['unlocked_titles'];
        }

        $account->fill($updateData)->save();

        // Auto-sync Azuriom Role if user exists
        if ($account->user) {
            $this->syncUserRole($account->user, $updateData['rank']);
        }

        Log::info('[Apexsions Bridge] Player stats synchronized', [
            'username' => $username,
            'uuid' => $uuid,
            'rank' => $updateData['rank'],
            'level' => $updateData['level'],
            'kingdom' => $updateData['kingdom'],
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Statistik pemain berhasil diperbarui di web platform.',
        ]);
    }

    /**
     * Map in-game LuckPerms rank to Azuriom Role with official power and colors.
     */
    protected function syncUserRole($user, string $rankKey): void
    {
        $rankConfigMap = [
            'ancestor' => ['name' => 'Ancestor', 'power' => 100, 'color' => '#eab308'],
            'architect' => ['name' => 'Architect', 'power' => 95, 'color' => '#00ffff'],
            'overseer' => ['name' => 'Overseer', 'power' => 95, 'color' => '#9333ea'],
            'warden' => ['name' => 'Warden', 'power' => 90, 'color' => '#3b82f6'],
            'herald' => ['name' => 'Herald', 'power' => 80, 'color' => '#10b981'],
            'sions' => ['name' => 'Sions', 'power' => 70, 'color' => '#f43f5e'],
            'emperor' => ['name' => 'Emperor', 'power' => 60, 'color' => '#ec4899'],
            'sovereign' => ['name' => 'Sovereign', 'power' => 50, 'color' => '#a855f7'],
            'archon' => ['name' => 'Archon', 'power' => 40, 'color' => '#06b6d4'],
            'ascendant' => ['name' => 'Ascendant', 'power' => 30, 'color' => '#22c55e'],
            'wanderer' => ['name' => 'Wanderer', 'power' => 10, 'color' => '#9ca3af'],
        ];

        $cfg = $rankConfigMap[strtolower($rankKey)] ?? null;
        if (!$cfg) {
            return;
        }

        $targetRoleName = $cfg['name'];
        $role = Role::whereRaw('LOWER(name) = ?', [strtolower($targetRoleName)])->first();

        if (!$role) {
            try {
                $role = Role::create([
                    'name' => $targetRoleName,
                    'color' => $cfg['color'],
                    'power' => $cfg['power'],
                ]);
            } catch (\Throwable $e) {
                Log::warning('[Apexsions Bridge] Could not create role ' . $targetRoleName . ': ' . $e->getMessage());
            }
        }

        if ($role && $user->role_id !== $role->id) {
            $user->role_id = $role->id;
            $user->save();
        }
    }
}
