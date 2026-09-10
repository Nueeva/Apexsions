<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\RankConfig;
use Azuriom\Plugin\ApexsionsBridge\Models\RankPurchase;
use Azuriom\Plugin\ApexsionsBridge\Models\RankRewardClaim;
use Illuminate\Support\Str;

class RankService
{
    /**
     * Fallback definitions of the 11 official server ranks from ranks.yml.
     */
    protected static array $fallbackRanks = [
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
     * Get all rank definitions, combining dynamic database configs with fallback definitions.
     */
    public static function getAllRanks(): array
    {
        $ranks = self::$fallbackRanks;

        try {
            $configs = RankConfig::where('is_active', true)->orderBy('order_index', 'asc')->get();
            foreach ($configs as $cfg) {
                $k = strtolower(trim($cfg->rank_key));
                $ranks[$k] = [
                    'key' => $k,
                    'display_name' => $cfg->display_name,
                    'badge' => $cfg->badge ?? $cfg->display_name,
                    'prefix' => $cfg->prefix ?? "[{$cfg->display_name}] ",
                    'color' => $cfg->color,
                    'tier' => $cfg->tier,
                    'role' => $cfg->role ?? ($cfg->tier . ' Civilized Rank'),
                    'weight' => (int) $cfg->weight,
                    'is_protected' => false,
                    'is_default' => ($k === 'wanderer'),
                    'description' => $cfg->description,
                    'banner_image' => $cfg->banner_image,
                    'card_image' => $cfg->card_image,
                    'price_trial_30' => (float) $cfg->price_trial_30,
                    'price_trial_90' => (float) $cfg->price_trial_90,
                    'price_permanent' => (float) $cfg->price_permanent,
                    'price_upgrade_override' => $cfg->price_upgrade_override ? (float) $cfg->price_upgrade_override : null,
                    'money_reward_permanent' => (float) $cfg->money_reward_permanent,
                    'benefit_max_homes' => (int) $cfg->benefit_max_homes,
                    'benefit_max_auctions' => (int) $cfg->benefit_max_auctions,
                    'benefit_max_enchants' => (int) $cfg->benefit_max_enchants,
                    'benefit_rtp_cooldown' => (int) $cfg->benefit_rtp_cooldown,
                    'benefit_shop_sell_bonus' => (float) $cfg->benefit_shop_sell_bonus,
                    'benefit_xp_bonus' => (float) $cfg->benefit_xp_bonus,
                    'benefit_bank_multiplier' => (float) $cfg->benefit_bank_multiplier,
                    'benefit_feed_cooldown' => $cfg->benefit_feed_cooldown,
                    'benefit_commands' => $cfg->benefit_commands ?? [],
                    'benefit_kits' => $cfg->benefit_kits ?? [],
                    'benefit_nick_permission' => $cfg->benefit_nick_permission ?? 'none',
                    'benefit_battlepass_unlock' => $cfg->benefit_battlepass_unlock,
                    'benefit_battlepass_discount' => (float) $cfg->benefit_battlepass_discount,
                ];
            }
        } catch (\Throwable $e) {
            // Fallback if DB table not yet migrated or query fails
        }

        return $ranks;
    }

    /**
     * Get rank by key.
     */
    public static function getRank(?string $key): ?array
    {
        if (empty($key)) {
            return self::getAllRanks()['wanderer'] ?? self::$fallbackRanks['wanderer'];
        }
        $normalized = strtolower(trim($key));
        $all = self::getAllRanks();
        return $all[$normalized] ?? (self::$fallbackRanks[$normalized] ?? null);
    }

    /**
     * Validate if a rank key is an official rank.
     */
    public static function isValidRank(string $key): bool
    {
        $all = self::getAllRanks();
        return isset($all[strtolower(trim($key))]);
    }

    /**
     * Get rank hierarchy weight.
     */
    public static function getRankWeight(string $rankKey): int
    {
        $meta = self::getRank($rankKey);
        return $meta['weight'] ?? 10;
    }

    /**
     * Get the count of players holding each rank.
     */
    public static function getPlayerCounts(): array
    {
        $ranks = self::getAllRanks();
        $counts = [];
        foreach (array_keys($ranks) as $key) {
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
     * Get one-time money reward amount for a rank.
     */
    public static function getRankRewardAmount(string $rankKey): float
    {
        $rank = self::getRank($rankKey);
        if ($rank && isset($rank['money_reward_permanent']) && $rank['money_reward_permanent'] > 0) {
            return (float) $rank['money_reward_permanent'];
        }

        $fallbackRewards = [
            'ascendant' => 50000.0,
            'archon' => 80000.0,
            'sovereign' => 120000.0,
            'emperor' => 180000.0,
            'sions' => 300000.0,
        ];

        return $fallbackRewards[strtolower(trim($rankKey))] ?? 0.0;
    }

    /**
     * Check if a player has already claimed the one-time money reward for a rank.
     */
    public static function hasClaimedReward(string $uuid, string $rankKey): bool
    {
        return RankRewardClaim::where('minecraft_uuid', $uuid)
            ->where('rank', strtolower(trim($rankKey)))
            ->where('reward_type', 'MONEY_ONETIME')
            ->exists();
    }

    /**
     * Calculate Rank Upgrade eligibility and price for a player account.
     *
     * Rule:
     * - Only PERMANENT ranks can upgrade.
     * - Target rank must be higher in weight than current rank.
     * - Upgrade price = Target Permanent Price - Current Permanent Price (or custom override if set).
     */
    public static function calculateUpgradePrice(?MinecraftAccount $account, string $targetRankKey): array
    {
        $normalizedTarget = strtolower(trim($targetRankKey));
        $targetMeta = self::getRank($normalizedTarget);

        if (!$targetMeta) {
            return [
                'eligible' => false,
                'message' => "Rank target '{$targetRankKey}' tidak ditemukan.",
                'upgrade_price' => 0.0,
            ];
        }

        if (!$account) {
            return [
                'eligible' => false,
                'message' => 'Akun Minecraft belum terhubung.',
                'upgrade_price' => (float) ($targetMeta['price_permanent'] ?? 0),
            ];
        }

        $currentRankKey = strtolower(trim($account->rank ?? 'wanderer'));
        $currentRankType = strtoupper(trim($account->rank_type ?? 'PERMANENT'));
        $currentMeta = self::getRank($currentRankKey);

        // 1. Must be Permanent rank to upgrade
        if ($currentRankType !== 'PERMANENT') {
            return [
                'eligible' => false,
                'message' => 'Hanya pemain dengan status rank Permanen yang berhak melakukan Upgrade Rank.',
                'current_rank' => $currentRankKey,
                'target_rank' => $normalizedTarget,
                'upgrade_price' => (float) ($targetMeta['price_permanent'] ?? 0),
            ];
        }

        $currentWeight = $currentMeta['weight'] ?? 10;
        $targetWeight = $targetMeta['weight'] ?? 10;

        // 2. Target rank must be higher than current rank
        if ($targetWeight <= $currentWeight) {
            return [
                'eligible' => false,
                'message' => "Anda sudah memiliki rank '{$currentMeta['display_name']}' atau rank yang lebih tinggi.",
                'current_rank' => $currentRankKey,
                'target_rank' => $normalizedTarget,
                'upgrade_price' => 0.0,
            ];
        }

        $currentPermanentPrice = (float) ($currentMeta['price_permanent'] ?? 0);
        $targetPermanentPrice = (float) ($targetMeta['price_permanent'] ?? 0);

        // Check if admin has set a custom upgrade price override
        if (!empty($targetMeta['price_upgrade_override']) && $targetMeta['price_upgrade_override'] > 0) {
            $upgradePrice = (float) $targetMeta['price_upgrade_override'];
        } else {
            $upgradePrice = max(0.0, $targetPermanentPrice - $currentPermanentPrice);
        }

        $targetReward = self::getRankRewardAmount($normalizedTarget);

        return [
            'eligible' => true,
            'current_rank' => $currentRankKey,
            'current_rank_display' => $currentMeta['display_name'] ?? ucfirst($currentRankKey),
            'target_rank' => $normalizedTarget,
            'target_rank_display' => $targetMeta['display_name'] ?? ucfirst($normalizedTarget),
            'current_rank_price' => $currentPermanentPrice,
            'target_rank_price' => $targetPermanentPrice,
            'upgrade_price' => $upgradePrice,
            'money_reward' => $targetReward,
            'message' => "Memenuhi syarat upgrade dari {$currentMeta['display_name']} ke {$targetMeta['display_name']}.",
        ];
    }

    /**
     * Execute a validated Rank Upgrade.
     */
    public static function executeRankUpgrade(
        $actor,
        MinecraftAccount $account,
        string $targetRankKey,
        float $pricePaid = 0.0,
        string $source = 'WEB'
    ): array {
        $calculation = self::calculateUpgradePrice($account, $targetRankKey);
        if (!$calculation['eligible']) {
            return [
                'success' => false,
                'message' => $calculation['message'],
            ];
        }

        $oldRank = $account->rank;
        $targetRank = strtolower(trim($targetRankKey));
        $reason = "Upgrade Rank dari {$calculation['current_rank_display']} ke {$calculation['target_rank_display']}";

        // Assign the new rank as Permanent
        $result = self::assignRank(
            $actor,
            $account,
            $targetRank,
            $reason,
            'PERMANENT',
            null,
            $pricePaid,
            $source
        );

        if ($result['success']) {
            // Update purchase record with upgrade metadata
            if (!empty($result['purchase_id'])) {
                RankPurchase::where('id', $result['purchase_id'])->update([
                    'is_upgrade' => true,
                    'previous_rank' => $oldRank,
                    'normal_price' => $calculation['target_rank_price'],
                    'discount_amount' => max(0.0, $calculation['target_rank_price'] - $pricePaid),
                    'payment_method' => 'WHATSAPP_MANUAL',
                    'sync_status' => 'DELIVERED',
                ]);
            }
        }

        return $result;
    }

    /**
     * Execute safe rank assignment or change via LuckPerms delivery queue.
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

        $rankMeta = self::getRank($normalizedRank);

        // Guard against assigning protected rank if not owner
        if (!empty($rankMeta['is_protected'])) {
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

        $compoundCommand = implode("\n", $commands);

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
            'rank_display' => $rankMeta['display_name'] ?? ucfirst($normalizedRank),
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
        $purchase = RankPurchase::create([
            'user_id' => $account->user_id,
            'minecraft_account_id' => $account->id,
            'minecraft_uuid' => $account->minecraft_uuid,
            'minecraft_username' => $account->minecraft_username,
            'rank' => $normalizedRank,
            'rank_type' => $normalizedType,
            'duration_days' => $durationDays,
            'price_paid' => $pricePaid,
            'normal_price' => (float) ($rankMeta['price_permanent'] ?? $pricePaid),
            'discount_amount' => 0.0,
            'status' => 'ACTIVE',
            'started_at' => now(),
            'expires_at' => $expiresAt,
            'source' => $source,
            'payment_method' => 'WHATSAPP_MANUAL',
            'sync_status' => 'DELIVERED',
            'delivery_id' => $delivery->id,
            'notes' => $reason,
        ]);

        // 4. One-time rank money reward (ONLY for Permanent ranks, strictly for the purchased rank, no duplicates!)
        $rewardGranted = 0.0;
        if ($normalizedType === 'PERMANENT') {
            $rewardAmount = self::getRankRewardAmount($normalizedRank);
            if ($rewardAmount > 0 && self::claimPermanentMoneyReward($account, $normalizedRank, $rewardAmount, $purchase->id ?? null)) {
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
                'rank_display' => $rankMeta['display_name'] ?? ucfirst($normalizedRank),
                'tier' => $rankMeta['tier'] ?? 'Tier II',
                'weight' => $rankMeta['weight'] ?? 10,
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
            'purchase_id' => $purchase->id,
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
            RankRewardClaim::create([
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
        } catch (\Throwable $e) {
            // Duplicate unique constraint prevented exploit
            return false;
        }
    }

    /**
     * Check and expire trial ranks across all accounts.
     * Restores player's previous permanent rank if they had one, otherwise resets to wanderer.
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

            // Find highest permanent rank previously owned
            $prevPermanent = RankPurchase::where('minecraft_account_id', $account->id)
                ->where(function ($q) {
                    $q->where('rank_type', 'PERMANENT')->orWhere('rank_type', 'permanent');
                })
                ->where('status', 'ACTIVE')
                ->orderBy('id', 'desc')
                ->first();

            $restoreRank = $prevPermanent ? strtolower(trim($prevPermanent->rank)) : 'wanderer';
            $restoreRankMeta = self::getRank($restoreRank);
            $restoreDisplayName = $restoreRankMeta['display_name'] ?? ucfirst($restoreRank);

            // Set back in LuckPerms
            $expireCommands = [];
            $expireCommands[] = "lp user {$account->minecraft_username} parent set {$restoreRank}";
            $expireCommands[] = "lp user {$account->minecraft_username} permission unset apexsions.rank.trial";
            if ($restoreRank !== 'wanderer') {
                $expireCommands[] = "lp user {$account->minecraft_username} permission set apexsions.rank.permanent true";
            } else {
                $expireCommands[] = "lp user {$account->minecraft_username} permission unset apexsions.rank.permanent";
            }
            $command = implode("\n", $expireCommands);

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
                'rank' => $restoreRank,
                'rank_display' => $restoreDisplayName,
                'rank_type' => 'PERMANENT',
                'rank_expires_at' => null,
            ]);

            // Mark trial purchase as expired
            RankPurchase::where('minecraft_account_id', $account->id)
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
                'new_value' => $restoreRank . ' (PERMANENT)',
                'reason' => "Masa aktif trial rank telah habis. Dikembalikan ke {$restoreDisplayName}.",
                'source' => 'SYSTEM',
                'status' => 'SUCCESS',
            ]);

            $count++;
        }

        return $count;
    }
}
