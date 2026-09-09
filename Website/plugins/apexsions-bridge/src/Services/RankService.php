<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Support\Str;

class RankService
{
    /**
     * Source of truth definition of the 11 official server ranks from ranks.yml.
     */
    protected static array $ranks = [
        'ancestor' => [
            'key' => 'ancestor',
            'display_name' => 'The Ancestor',
            'badge' => '👑 ANCESTOR',
            'prefix' => '[👑 ANCESTOR] ',
            'color' => '#8B0000',
            'tier' => 'Tier V',
            'role' => 'The Ancestor / Owner / Founder (Apex)',
            'weight' => 100,
            'is_protected' => true,
            'is_default' => false,
            'description' => 'Gelar kehormatan tertinggi dan otoritas pendiri realm Apexsions. Terkunci khusus untuk Founder/Owner.',
        ],
        'architect' => [
            'key' => 'architect',
            'display_name' => 'Architect',
            'badge' => '📐 ARCHITECT',
            'prefix' => '[📐 ARCHITECT] ',
            'color' => '#8E2DE2',
            'tier' => 'Tier IV',
            'role' => 'Authority / Realm Architect',
            'weight' => 95,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Arsitek dan pembangun tatanan peradaban realm dengan wewenang sistem penuh.',
        ],
        'overseer' => [
            'key' => 'overseer',
            'display_name' => 'Overseer',
            'badge' => '👁 OVERSEER',
            'prefix' => '[👁 OVERSEER] ',
            'color' => '#FFD700',
            'tier' => 'Tier IV',
            'role' => 'Authority / Integrity & Balance',
            'weight' => 95,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Pengawas integritas, keadilan aturan, dan stabilitas makro peradaban.',
        ],
        'warden' => [
            'key' => 'warden',
            'display_name' => 'Warden',
            'badge' => '🛡 WARDEN',
            'prefix' => '[🛡 WARDEN] ',
            'color' => '#2a5298',
            'tier' => 'Tier III',
            'role' => 'Head Staff / Realm Administrator',
            'weight' => 90,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Komandan staf dan penegak keamanan sentral realm Apexsions.',
        ],
        'herald' => [
            'key' => 'herald',
            'display_name' => 'Herald',
            'badge' => '📜 HERALD',
            'prefix' => '[📜 HERALD] ',
            'color' => '#ff5858',
            'tier' => 'Tier III',
            'role' => 'Staff / Moderator / Helper',
            'weight' => 80,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Pemberita kerajaan, fasilitator komunitas, dan penegak moderasi lapangan.',
        ],
        'sions' => [
            'key' => 'sions',
            'display_name' => 'Sions',
            'badge' => '✦ SIONS ✦',
            'prefix' => '[✦ SIONS] ',
            'color' => '#00FFFF',
            'tier' => 'Tier II',
            'role' => 'Apex Donator / Highest Rank',
            'weight' => 70,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Pangkat prestise tertinggi donatur realm dengan privilese dan aura eksklusif.',
        ],
        'emperor' => [
            'key' => 'emperor',
            'display_name' => 'Emperor',
            'badge' => '⚔ EMPEROR',
            'prefix' => '[⚔ EMPEROR] ',
            'color' => '#e52d27',
            'tier' => 'Tier II',
            'role' => 'Donator Tier 4',
            'weight' => 60,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Kaisar terhormat pendukung peradaban Apexsions Tier 4.',
        ],
        'sovereign' => [
            'key' => 'sovereign',
            'display_name' => 'Sovereign',
            'badge' => '⚜ SOVEREIGN',
            'prefix' => '[⚜ SOVEREIGN] ',
            'color' => '#f1c40f',
            'tier' => 'Tier II',
            'role' => 'Donator Tier 3',
            'weight' => 50,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Penguasa bangsawan pendukung peradaban Apexsions Tier 3.',
        ],
        'archon' => [
            'key' => 'archon',
            'display_name' => 'Archon',
            'badge' => '💎 ARCHON',
            'prefix' => '[💎 ARCHON] ',
            'color' => '#00c6ff',
            'tier' => 'Tier II',
            'role' => 'Donator Tier 2',
            'weight' => 40,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Bangsawan terkemuka pendukung peradaban Apexsions Tier 2.',
        ],
        'ascendant' => [
            'key' => 'ascendant',
            'display_name' => 'Ascendant',
            'badge' => '☘ ASCENDANT',
            'prefix' => '[☘ ASCENDANT] ',
            'color' => '#38ef7d',
            'tier' => 'Tier II',
            'role' => 'Donator Tier 1',
            'weight' => 30,
            'is_protected' => false,
            'is_default' => false,
            'description' => 'Warga yang telah mencapai peningkatan status kehormatan Tier 1.',
        ],
        'wanderer' => [
            'key' => 'wanderer',
            'display_name' => 'Wanderer',
            'badge' => 'Wanderer',
            'prefix' => '[Wanderer] ',
            'color' => '#95a5a6',
            'tier' => 'Tier I',
            'role' => 'Default / Warga Baru (Foundation)',
            'weight' => 10,
            'is_protected' => false,
            'is_default' => true,
            'description' => 'Fondasi peradaban Apexsions. Rank standar bagi setiap warga baru yang menjejakkan kaki di realm.',
        ],
    ];

    /**
     * Get all rank definitions.
     */
    public static function getAllRanks(): array
    {
        return self::$ranks;
    }

    /**
     * Alias for getAllRanks.
     */
    public static function getRankHierarchy(): array
    {
        return self::getAllRanks();
    }

    /**
     * Get rank by key.
     */
    public static function getRank(?string $key): ?array
    {
        if (empty($key)) {
            return self::$ranks['wanderer'];
        }
        $normalized = strtolower(trim($key));
        return self::$ranks[$normalized] ?? null;
    }

    /**
     * Validate if a rank key is an official rank.
     */
    public static function isValidRank(string $key): bool
    {
        return isset(self::$ranks[strtolower(trim($key))]);
    }

    /**
     * Get the count of players holding each rank.
     *
     * @return array<string, int>
     */
    public static function getPlayerCounts(): array
    {
        $counts = [];
        foreach (array_keys(self::$ranks) as $key) {
            $counts[$key] = 0;
        }

        $results = MinecraftAccount::query()
            ->selectRaw('LOWER(rank) as rank_key, COUNT(*) as aggregate')
            ->groupBy('rank_key')
            ->pluck('aggregate', 'rank_key')
            ->all();

        foreach ($results as $rk => $cnt) {
            $normalized = strtolower(trim($rk));
            if (isset($counts[$normalized])) {
                $counts[$normalized] = (int) $cnt;
            } else {
                $counts['wanderer'] += (int) $cnt;
            }
        }

        return $counts;
    }

    /**
     * Execute safe rank assignment or change via LuckPerms delivery queue.
     *
     * @param mixed $actor The admin user performing the action
     * @param MinecraftAccount $account The target Minecraft player account
     * @param string $newRankKey The target rank identifier
     * @param string $reason The mandatory reason for the audit trail
     * @return array Result summary with status, message, and action_id
     */
    public static function assignRank($actor, MinecraftAccount $account, string $newRankKey, string $reason): array
    {
        $normalizedRank = strtolower(trim($newRankKey));

        if (!self::isValidRank($normalizedRank)) {
            return [
                'success' => false,
                'message' => "Rank '{$newRankKey}' tidak valid atau tidak terdaftar dalam hierarki resmi.",
            ];
        }

        $rankMeta = self::$ranks[$normalizedRank];

        // Guard against assigning protected rank if not owner
        if ($rankMeta['is_protected']) {
            $ownerUuid = config('apexsions.owner_uuid', '00000000-0000-0000-0000-000000000000');
            if ($account->minecraft_uuid !== $ownerUuid && strcasecmp($actor->email ?? '', 'nueeva@users.noreply.github.com') !== 0) {
                return [
                    'success' => false,
                    'message' => "Rank 'The Ancestor' berstatus PROTECTED dan hanya dapat dimiliki oleh pendiri realm.",
                ];
            }
        }

        $oldRankKey = strtolower(trim($account->rank ?: 'wanderer'));
        if ($oldRankKey === $normalizedRank) {
            return [
                'success' => false,
                'message' => "Pemain {$account->minecraft_username} sudah memiliki rank '{$rankMeta['display_name']}'.",
            ];
        }

        $actionId = (string) Str::uuid();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Console';
        $actorId = $actor ? ($actor->id ?? null) : null;
        $command = "lp user {$account->minecraft_username} parent set {$normalizedRank}";

        // 1. Create delivery entry for in-game execution
        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'RANK_' . $account->minecraft_uuid . '_' . $normalizedRank . '_' . time(),
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => $command,
            'status' => 'PENDING',
        ]);

        // 2. Optimistically update local database
        $account->update([
            'rank' => $normalizedRank,
            'rank_display' => $rankMeta['display_name'],
        ]);

        // 3. Record in Unified Audit Log
        $actionType = ($normalizedRank === 'wanderer') ? 'RANK_RESET' : 'RANK_CHANGE';
        $audit = AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => 'ADMIN',
            'actor_id' => $actorId,
            'actor_name' => $actorName,
            'action' => $actionType,
            'target_type' => 'PLAYER',
            'target_id' => $account->minecraft_uuid,
            'target_name' => $account->minecraft_username,
            'old_value' => $oldRankKey,
            'new_value' => $normalizedRank,
            'reason' => $reason,
            'source' => 'WEB',
            'status' => 'PENDING',
            'metadata' => [
                'action_id' => $actionId,
                'delivery_id' => $delivery->id,
                'rank_display' => $rankMeta['display_name'],
                'tier' => $rankMeta['tier'],
                'weight' => $rankMeta['weight'],
                'command' => $command,
            ],
        ]);

        return [
            'success' => true,
            'message' => "Rank pemain {$account->minecraft_username} berhasil diubah menjadi '{$rankMeta['display_name']}'! Perintah LuckPerms telah dijadwalkan ke server Minecraft.",
            'action_id' => $actionId,
            'delivery_id' => $delivery->id,
            'audit_id' => $audit->id,
            'new_rank' => $normalizedRank,
            'new_rank_display' => $rankMeta['display_name'],
        ];
    }
}
