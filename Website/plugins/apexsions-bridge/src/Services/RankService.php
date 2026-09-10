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
     * One-time money rewards for permanent ranks (strictly non-inheritable and non-duplicable).
     */
    protected static array $rankRewards = [
        'ascendant' => 50000.0,
        'archon' => 80000.0,
        'sovereign' => 120000.0,
        'emperor' => 180000.0,
        'sions' => 300000.0,
    ];

    /**
     * Get one-time money reward amount for a rank.
     */
    public static function getRankRewardAmount(string $rankKey): float
    {
        return self::$rankRewards[strtolower(trim($rankKey))] ?? 0.0;
    }

    /**
     * Check if a player has already claimed the one-time money reward for a rank.
     */
    public static function hasClaimedReward(string $uuid, string $rankKey): bool
    {
        return \Azuriom\Plugin\ApexsionsBridge\Models\RankRewardClaim::where('minecraft_uuid', $uuid)
            ->where('rank', strtolower(trim($rankKey)))
            ->where('reward_type', 'MONEY_ONETIME')
            ->exists();
    }

    /**
     * Execute safe rank assignment or change via LuckPerms delivery queue.
     *
     * @param mixed $actor The admin user performing the action
     * @param MinecraftAccount $account The target Minecraft player account
     * @param string $newRankKey The target rank identifier
     * @param string $reason The mandatory reason for the audit trail
     * @param string $rankType 'PERMANENT' or 'TRIAL'
     * @param int|null $durationDays 30, 90, or null
     * @param float $pricePaid Price paid if from purchase
     * @param string $source 'WEB', 'ADMIN', 'WEBSTORE'
     * @return array Result summary with status, message, and action_id
     */
    public static function assignRank(
        $actor,
        MinecraftAccount $account,
        string $newRankKey,
        string $reason,
        string $rankType = 'PERMANENT',
        ?int $durationDays = null,
        float $pricePaid = 0.0,
        string $source = 'WEB'
    ): array {
        $normalizedRank = strtolower(trim($newRankKey));
        $normalizedType = strtoupper(trim($rankType)) === 'TRIAL' ? 'TRIAL' : 'PERMANENT';

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
        $oldRankType = strtoupper(trim($account->rank_type ?: 'PERMANENT'));

        if ($oldRankKey === $normalizedRank && $oldRankType === $normalizedType && $normalizedType === 'PERMANENT') {
            return [
                'success' => false,
                'message' => "Pemain {$account->minecraft_username} sudah memiliki rank '{$rankMeta['display_name']}' (Permanen).",
            ];
        }

        $actionId = (string) Str::uuid();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Console';
        $actorId = $actor ? ($actor->id ?? null) : null;

        $expiresAt = null;
        if ($normalizedType === 'TRIAL' && $durationDays && $durationDays > 0) {
            $expiresAt = now()->addDays($durationDays);
        }

        // 1. Create delivery command for LuckPerms
        $commands = [];
        $commands[] = "lp user {$account->minecraft_username} parent set {$normalizedRank}";

        if ($normalizedType === 'PERMANENT') {
            $commands[] = "lp user {$account->minecraft_username} permission set apexsions.rank.permanent true";
            $commands[] = "lp user {$account->minecraft_username} permission unset apexsions.rank.trial";
        } else {
            $commands[] = "lp user {$account->minecraft_username} permission set apexsions.rank.trial true";
            $commands[] = "lp user {$account->minecraft_username} permission unset apexsions.rank.permanent";
        }

        $compoundCommand = implode('; ', $commands);

        $delivery = Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'RANK_' . $account->minecraft_uuid . '_' . $normalizedRank . '_' . time(),
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => $compoundCommand,
            'status' => 'PENDING',
        ]);

        // 2. Update local MinecraftAccount model
        $accountUpdates = [
            'rank' => $normalizedRank,
            'rank_display' => $rankMeta['display_name'],
            'rank_type' => $normalizedType,
            'rank_expires_at' => $expiresAt,
        ];

        // BattlePass perks for Emperor and Sions Permanent
        if ($normalizedType === 'PERMANENT') {
            if ($normalizedRank === 'emperor') {
                $accountUpdates['battlepass_has_premium'] = true;
                $accountUpdates['battlepass_pass_name'] = 'Sio Pass';
            } elseif ($normalizedRank === 'sions') {
                $accountUpdates['battlepass_has_premium'] = true;
                $accountUpdates['battlepass_pass_name'] = 'Exsio Pass';
            }
        }

        $account->update($accountUpdates);

        // 3. Record purchase history
        $purchase = \Azuriom\Plugin\ApexsionsBridge\Models\RankPurchase::create([
            'user_id' => $account->user_id,
            'minecraft_account_id' => $account->id,
            'minecraft_uuid' => $account->minecraft_uuid,
            'minecraft_username' => $account->minecraft_username,
            'rank' => $normalizedRank,
            'rank_type' => $normalizedType,
            'duration_days' => $durationDays,
            'price_paid' => $pricePaid,
            'status' => 'ACTIVE',
            'started_at' => now(),
            'expires_at' => $expiresAt,
            'source' => $source,
            'notes' => $reason,
        ]);

        // 4. One-time rank money reward (ONLY for Permanent ranks, strictly for the purchased rank, no duplicates!)
        $rewardGranted = 0.0;
        if ($normalizedType === 'PERMANENT' && isset(self::$rankRewards[$normalizedRank])) {
            $rewardAmount = (float) self::$rankRewards[$normalizedRank];
            if (self::claimPermanentMoneyReward($account, $normalizedRank, $rewardAmount, $purchase->id ?? null)) {
                $rewardGranted = $rewardAmount;
            }
        }

        // 5. Record in Unified Audit Log
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
            'old_value' => $oldRankKey . ' (' . $oldRankType . ')',
            'new_value' => $normalizedRank . ' (' . $normalizedType . ')',
            'reason' => $reason,
            'source' => $source,
            'status' => 'PENDING',
            'metadata' => [
                'action_id' => $actionId,
                'delivery_id' => $delivery->id,
                'purchase_id' => $purchase->id,
                'rank_type' => $normalizedType,
                'duration_days' => $durationDays,
                'expires_at' => $expiresAt ? $expiresAt->toIso8601String() : null,
                'reward_granted' => $rewardGranted,
                'rank_display' => $rankMeta['display_name'],
                'tier' => $rankMeta['tier'],
                'weight' => $rankMeta['weight'],
                'command' => $compoundCommand,
            ],
        ]);

        $statusMsg = "Rank {$account->minecraft_username} berhasil disetel ke '{$rankMeta['display_name']}' [{$normalizedType}]!";
        if ($rewardGranted > 0) {
            $statusMsg .= " Bonus uang reward satu kali Rp." . number_format($rewardGranted, 0, ',', '.') . " telah dikreditkan.";
        }

        return [
            'success' => true,
            'message' => $statusMsg,
            'action_id' => $actionId,
            'delivery_id' => $delivery->id,
            'audit_id' => $audit->id,
            'new_rank' => $normalizedRank,
            'new_rank_display' => $rankMeta['display_name'],
            'rank_type' => $normalizedType,
            'reward_granted' => $rewardGranted,
        ];
    }

    /**
     * Safely claim permanent one-time money reward ensuring strict non-duplication.
     */
    public static function claimPermanentMoneyReward(MinecraftAccount $account, string $rank, float $amount, $purchaseId = null): bool
    {
        $normalizedRank = strtolower(trim($rank));
        if (self::hasClaimedReward($account->minecraft_uuid, $normalizedRank)) {
            return false;
        }

        $actionId = (string) Str::uuid();

        try {
            \Azuriom\Plugin\ApexsionsBridge\Models\RankRewardClaim::create([
                'minecraft_uuid' => $account->minecraft_uuid,
                'minecraft_username' => $account->minecraft_username,
                'rank' => $normalizedRank,
                'reward_type' => 'MONEY_ONETIME',
                'amount' => $amount,
                'transaction_reference' => $actionId,
                'claimed_at' => now(),
            ]);

            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'REWARD_' . $account->minecraft_uuid . '_' . $normalizedRank . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => "eco give {$account->minecraft_username} {$amount}",
                'status' => 'PENDING',
            ]);

            $account->increment('balance_rupiah', $amount);

            return true;
        } catch (\Exception $e) {
            // Duplicate unique constraint prevented exploit
            return false;
        }
    }

    /**
     * Check and expire trial ranks across all accounts.
     */
    public static function checkAndExpireTrials(): int
    {
        $expiredAccounts = MinecraftAccount::where(function ($q) {
                $q->where('rank_type', 'TRIAL')->orWhere('rank_type', 'trial');
            })
            ->whereNotNull('rank_expires_at')
            ->where('rank_expires_at', '<=', now())
            ->where('rank', '!=', 'wanderer')
            ->get();

        $count = 0;
        foreach ($expiredAccounts as $account) {
            $oldRank = $account->rank;
            $actionId = (string) Str::uuid();

            // Set back to wanderer in LuckPerms
            $command = "lp user {$account->minecraft_username} parent set wanderer; lp user {$account->minecraft_username} permission unset apexsions.rank.trial; lp user {$account->minecraft_username} permission unset apexsions.rank.permanent";

            Delivery::create([
                'action_id' => $actionId,
                'idempotency_key' => 'RANK_EXPIRE_' . $account->minecraft_uuid . '_' . time(),
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            // Update account
            $account->update([
                'rank' => 'wanderer',
                'rank_display' => 'Wanderer',
                'rank_type' => 'PERMANENT',
                'rank_expires_at' => null,
            ]);

            // Mark purchase as expired
            \Azuriom\Plugin\ApexsionsBridge\Models\RankPurchase::where('minecraft_account_id', $account->id)
                ->where(function ($q) {
                    $q->where('rank_type', 'TRIAL')->orWhere('rank_type', 'trial');
                })
                ->where('status', 'ACTIVE')
                ->update(['status' => 'EXPIRED']);

            // Audit
            AuditLog::create([
                'action_id' => $actionId,
                'actor_type' => 'SYSTEM',
                'actor_name' => 'Trial Expiration Daemon',
                'action' => 'RANK_EXPIRED',
                'target_type' => 'PLAYER',
                'target_id' => $account->minecraft_uuid,
                'target_name' => $account->minecraft_username,
                'old_value' => $oldRank . ' (TRIAL)',
                'new_value' => 'wanderer (PERMANENT)',
                'reason' => 'Masa aktif trial rank telah habis.',
                'source' => 'SYSTEM',
                'status' => 'SUCCESS',
            ]);

            $count++;
        }

        return $count;
    }
}
