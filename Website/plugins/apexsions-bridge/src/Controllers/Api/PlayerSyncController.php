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
            'unlocked_titles' => ['nullable', 'array'],
        ]);

        $uuid = $validated['player_uuid'];
        $username = $validated['player_username'];

        // Find linked account by UUID or username
        $account = MinecraftAccount::where('minecraft_uuid', $uuid)
            ->orWhere('minecraft_username', $username)
            ->first();

        if (!$account) {
            return response()->json([
                'status' => 'ignored',
                'message' => 'Player is not yet linked to an Apexsions web account.',
            ], 200);
        }

        $updateData = [
            'minecraft_uuid' => $uuid,
            'minecraft_username' => $username,
            'rank' => strtolower($validated['rank'] ?? 'wanderer'),
            'rank_display' => $validated['rank_display'] ?? ucfirst($validated['rank'] ?? 'Wanderer'),
            'kingdom' => strtoupper($validated['kingdom'] ?? 'NONE'),
            'kingdom_display' => $validated['kingdom_display'] ?? 'Belum Memilih',
            'level' => (int) ($validated['level'] ?? 1),
            'xp' => (int) ($validated['xp'] ?? 0),
            'required_xp' => (int) ($validated['required_xp'] ?? 100),
            'level_title' => $validated['level_title'] ?? null,
            'active_title' => $validated['active_title'] ?? null,
            'balance_rupiah' => (float) ($validated['balance_rupiah'] ?? 0),
            'balance_diamond' => (float) ($validated['balance_diamond'] ?? 0),
            'last_seen_at' => Carbon::now(),
        ];

        if (isset($validated['unlocked_titles'])) {
            $updateData['unlocked_titles'] = $validated['unlocked_titles'];
        }

        $account->update($updateData);

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
     * Map in-game LuckPerms rank to Azuriom Role.
     */
    protected function syncUserRole($user, string $rankKey): void
    {
        $roleNameMap = [
            'ancestor' => 'Ancestor',
            'architect' => 'Architect',
            'overseer' => 'Overseer',
            'warden' => 'Warden',
            'herald' => 'Herald',
            'sions' => 'Sions',
            'emperor' => 'Emperor',
            'sovereign' => 'Sovereign',
            'archon' => 'Archon',
            'ascendant' => 'Ascendant',
            'wanderer' => 'Wanderer',
        ];

        $targetRoleName = $roleNameMap[$rankKey] ?? null;
        if (!$targetRoleName) {
            return;
        }

        $role = Role::whereRaw('LOWER(name) = ?', [strtolower($targetRoleName)])->first();

        // If role doesn't exist yet, try creating it with standard user permissions or find fallback
        if (!$role && $rankKey !== 'wanderer') {
            try {
                $role = Role::create([
                    'name' => $targetRoleName,
                    'color' => '#f39c12',
                    'power' => 10,
                ]);
            } catch (\Throwable $e) {
                // Ignore role creation error if restricted
            }
        }

        if ($role && $user->role_id !== $role->id) {
            $user->role_id = $role->id;
            $user->save();
        }
    }
}
