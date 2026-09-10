<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\VoteTransaction;
use Azuriom\Plugin\ApexsionsBridge\Models\VotingSite;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class VoteService
{
    /**
     * Check if a player is currently in cooldown for the given voting site.
     * Returns Carbon instance when next vote is available, or null if ready to vote.
     */
    public function checkCooldown(VotingSite $site, string $username, ?string $uuid = null): ?Carbon
    {
        $cooldownHours = $site->cooldown_hours ?: 24;
        $cleanUsername = ltrim($username, '.');
        $variants = array_values(array_unique([$username, $cleanUsername, '.' . $cleanUsername]));

        $lastVote = VoteTransaction::where('site_slug', $site->slug)
            ->where(function ($q) use ($variants, $uuid) {
                $q->whereIn('player_username', $variants);
                if ($uuid) {
                    $q->orWhere('player_uuid', $uuid);
                }
            })
            ->where('vote_status', 'VALID')
            ->where('voted_at', '>=', now()->subHours($cooldownHours))
            ->orderBy('voted_at', 'desc')
            ->first();

        if (!$lastVote) {
            return null;
        }

        return $lastVote->voted_at->copy()->addHours($cooldownHours);
    }

    /**
     * Poll external votes automatically from voting platform APIs (Minecraft-MP, TopG, etc.).
     * Detects new votes, triggers auto-rewards, and confirms claiming on the remote platform.
     */
    public function pollExternalVotes(VotingSite $site): array
    {
        if (!$site->is_active) {
            return ['status' => 'skipped', 'message' => "Platform {$site->name} sedang dinonaktifkan."];
        }

        $processedCount = 0;
        $duplicateCount = 0;
        $failedCount = 0;

        // 1. Polling for Minecraft-MP
        if ($site->slug === 'minecraft-mp' && !empty($site->api_key)) {
            try {
                // Fetch daily votes list (or current month votes)
                $votesUrl = "https://minecraft-mp.com/api/?object=servers&element=votes&key={$site->api_key}&format=json";
                $resp = Http::timeout(10)->get($votesUrl);

                if ($resp->successful()) {
                    $data = $resp->json();
                    $votes = $data['votes'] ?? [];

                    foreach ($votes as $vote) {
                        $nickname = trim($vote['nickname'] ?? '');
                        $voteDateStr = trim($vote['date'] ?? '');
                        if (empty($nickname)) {
                            continue;
                        }

                        $votedAt = !empty($voteDateStr) ? Carbon::parse($voteDateStr) : now();
                        $externalVoteId = 'mcmp_' . md5($nickname . '_' . $votedAt->format('Y-m-d_H:i:s'));

                        // Check if this specific vote has already been rewarded or recorded
                        $exists = VoteTransaction::where('external_vote_id', $externalVoteId)
                            ->orWhere(function ($q) use ($site, $nickname, $votedAt) {
                                $cleanName = ltrim($nickname, '.');
                                $q->where('site_slug', $site->slug)
                                    ->whereIn('player_username', [$nickname, $cleanName, '.' . $cleanName])
                                    ->whereBetween('voted_at', [$votedAt->copy()->subHours(2), $votedAt->copy()->addHours(2)]);
                            })
                            ->exists();

                        if ($exists) {
                            $duplicateCount++;
                            continue;
                        }

                        // Process auto-reward atomically
                        $rewardResult = $this->processVoteReward(
                            $site,
                            $nickname,
                            null,
                            'AUTO_POLL',
                            $externalVoteId,
                            $votedAt
                        );

                        if ($rewardResult['success']) {
                            $processedCount++;
                            // Confirm claim on Minecraft-MP API
                            try {
                                $claimUrl = "https://minecraft-mp.com/api/?action=post&object=votes&element=claim&key={$site->api_key}&username=" . urlencode($nickname);
                                Http::timeout(5)->get($claimUrl);
                            } catch (\Throwable $e) {
                                Log::info("[VoteService] Auto-claim notice for {$nickname} on Minecraft-MP: " . $e->getMessage());
                            }
                        } else {
                            if (!empty($rewardResult['duplicate'])) {
                                $duplicateCount++;
                            } else {
                                $failedCount++;
                            }
                        }
                    }
                }
            } catch (\Throwable $e) {
                Log::warning("[VoteService] Minecraft-MP polling error: " . $e->getMessage());
                return ['status' => 'error', 'message' => $e->getMessage()];
            }
        }

        return [
            'status' => 'success',
            'site' => $site->slug,
            'processed' => $processedCount,
            'duplicates' => $duplicateCount,
            'failed' => $failedCount,
        ];
    }

    /**
     * Verify vote with external voting platform API for status/troubleshooting queries.
     */
    public function verifyWithPlatform(VotingSite $site, string $username, ?string $ip = null): array
    {
        // 1. Minecraft-MP API verification
        if ($site->slug === 'minecraft-mp' && !empty($site->api_key)) {
            try {
                $clean = ltrim($username, '.');
                $namesToCheck = array_unique([$username, $clean, '.' . $clean]);

                foreach ($namesToCheck as $candidate) {
                    $checkUrl = "https://minecraft-mp.com/api/?object=votes&element=claim&key={$site->api_key}&username=" . urlencode($candidate);
                    $resp = Http::timeout(5)->get($checkUrl);

                    if ($resp->successful()) {
                        $body = trim($resp->body());
                        if ($body === '1') {
                            // Unclaimed vote exists
                            $postUrl = "https://minecraft-mp.com/api/?action=post&object=votes&element=claim&key={$site->api_key}&username=" . urlencode($candidate);
                            Http::timeout(5)->get($postUrl);
                            return ['valid' => true, 'status' => 'VALID', 'message' => 'Vote sah terverifikasi via Minecraft-MP API.'];
                        } elseif ($body === '2') {
                            // Already claimed today
                            return ['valid' => false, 'status' => 'ALREADY_CLAIMED', 'message' => 'Minecraft-MP melaporkan bahwa vote untuk hari ini telah selesai diproses.'];
                        }
                    }
                }

                return [
                    'valid' => false,
                    'status' => 'UNCONFIRMED',
                    'message' => 'Vote sedang menunggu sinkronisasi dari platform Minecraft-MP. Hadiah akan otomatis masuk begitu platform merilis data.',
                ];
            } catch (\Throwable $e) {
                Log::warning("VoteService: Minecraft-MP API check failed: " . $e->getMessage());
                return [
                    'valid' => false,
                    'status' => 'PLATFORM_DELAY',
                    'message' => 'Layanan Minecraft-MP sedang mengalami delay. Sistem auto-reward akan mencoba kembali di latar belakang.',
                ];
            }
        }

        // 2. TopG Global API verification
        if ($site->slug === 'topg' && !empty($site->api_key)) {
            try {
                $checkUrl = "https://topg.org/api/status/{$site->api_key}/" . urlencode($username);
                $resp = Http::timeout(5)->get($checkUrl);

                if ($resp->successful() && trim($resp->body()) === '1') {
                    return ['valid' => true, 'status' => 'VALID', 'message' => 'Vote sah terverifikasi via TopG API.'];
                }

                return [
                    'valid' => false,
                    'status' => 'UNCONFIRMED',
                    'message' => 'TopG belum mencatat suara untuk username ini.',
                ];
            } catch (\Throwable $e) {
                Log::warning("VoteService: TopG API check failed: " . $e->getMessage());
            }
        }

        return ['valid' => true, 'status' => 'VALID', 'message' => 'Vote dikonfirmasi melalui platform dispatch.'];
    }

    /**
     * Process an authentic vote reward transaction atomically.
     * Guaranteed Rewards:
     * - 3x Vote Crate Keys via `crates key give <player> vote 3`
     * - Rp 1.000 Currency via `ecoadmin give <player> 1000 rupiah`
     * - In-game tellraw confirmation alert.
     */
    public function processVoteReward(
        VotingSite $site,
        string $username,
        ?string $ip = null,
        string $source = 'AUTO_POLL',
        ?string $externalVoteId = null,
        ?Carbon $votedAt = null
    ): array {
        $username = trim($username);
        if (empty($username) || !preg_match('/^\.?[a-zA-Z0-9_]{2,32}$/', $username)) {
            return [
                'success' => false,
                'message' => 'Username Minecraft tidak valid.',
            ];
        }

        $votedAt = $votedAt ?: now();

        // Crossplay identity resolution
        $cleanUsername = ltrim($username, '.');
        $account = MinecraftAccount::where('minecraft_username', $username)
            ->orWhere('minecraft_username', '.' . $cleanUsername)
            ->orWhere('minecraft_username', $cleanUsername)
            ->first();

        if ($account) {
            $username = $account->minecraft_username;
            $uuid = $account->minecraft_uuid;
        } else {
            $uuid = null;
        }

        // Anti-duplicate cooldown verification
        $cooldownUntil = $this->checkCooldown($site, $username, $uuid);
        if ($cooldownUntil !== null) {
            $formattedTime = $cooldownUntil->diffForHumans();
            return [
                'success' => false,
                'duplicate' => true,
                'cooldown_until' => $cooldownUntil->toIso8601String(),
                'message' => "Suara untuk {$username} di {$site->name} sudah pernah diproses. Suara berikutnya dapat diberikan {$formattedTime}.",
            ];
        }

        $voteUuid = 'VOTE_' . Str::upper(Str::random(16));
        $externalId = $externalVoteId ?: ($site->slug . '_' . md5($username . '_' . $votedAt->format('Y-m-d_H')));
        $idempotencyHash = hash('sha256', "VOTE_{$site->slug}_{$username}_" . $externalId);

        return DB::transaction(function () use ($site, $username, $uuid, $ip, $voteUuid, $externalId, $idempotencyHash, $source, $votedAt) {
            // Lock and double check idempotency inside transaction
            $existing = VoteTransaction::where('idempotency_hash', $idempotencyHash)
                ->orWhere('external_vote_id', $externalId)
                ->first();

            if ($existing) {
                return [
                    'success' => false,
                    'duplicate' => true,
                    'message' => 'Transaksi suara ini telah tercatat dan sedang diproses.',
                ];
            }

            // 1. Enqueue 3x Vote Crate Keys Delivery
            $keyCommand = "crates key give {$username} vote 3";
            $keyDelivery = Delivery::create([
                'action_id' => $voteUuid . '_KEY',
                'idempotency_key' => 'VOTE_KEY_' . $username . '_' . time() . '_' . Str::random(6),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => $keyCommand,
                'status' => 'PENDING',
            ]);

            // 2. Enqueue Rp 1.000 Currency Delivery
            $moneyCommand = "ecoadmin give {$username} 1000 rupiah";
            $moneyDelivery = Delivery::create([
                'action_id' => $voteUuid . '_MONEY',
                'idempotency_key' => 'VOTE_MONEY_' . $username . '_' . time() . '_' . Str::random(6),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => $moneyCommand,
                'status' => 'PENDING',
            ]);

            // 3. Enqueue In-Game Tellraw Alert Delivery
            $tellrawPayload = json_encode([
                "",
                ["text" => "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", "color" => "gold"],
                ["text" => "           APEXSIONS VOTE REWARD\n", "color" => "yellow", "bold" => true],
                ["text" => "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", "color" => "gold"],
                ["text" => "Terima kasih telah mendukung kedaulatan Apexsions!\n\n", "color" => "white"],
                ["text" => "+ 3x Vote Crate Keys\n", "color" => "gold", "bold" => true],
                ["text" => "+ Rp 1.000 Saldo Peradaban\n\n", "color" => "green", "bold" => true],
                ["text" => "Gunakan ", "color" => "gray"],
                ["text" => "/vote ", "color" => "aqua", "bold" => true],
                ["text" => "untuk mendukung server kembali.\n", "color" => "gray"],
                ["text" => "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "color" => "gold"],
            ], JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);

            Delivery::create([
                'action_id' => $voteUuid . '_ALERT',
                'idempotency_key' => 'VOTE_ALERT_' . $username . '_' . time() . '_' . Str::random(6),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => "minecraft:tellraw {$username} {$tellrawPayload}",
                'status' => 'PENDING',
            ]);

            // 4. Record Vote Transaction Ledger
            $tx = VoteTransaction::create([
                'vote_uuid' => $voteUuid,
                'external_vote_id' => $externalId,
                'site_id' => $site->id,
                'site_slug' => $site->slug,
                'player_uuid' => $uuid,
                'player_username' => $username,
                'ip_address' => $ip,
                'idempotency_hash' => $idempotencyHash,
                'voted_at' => $votedAt,
                'verified_at' => now(),
                'vote_status' => 'VALID',
                'external_status' => $source,
                'reward_status' => 'PENDING',
                'keys_amount' => 3,
                'money_amount' => 1000.00,
                'keys_delivery_id' => $keyDelivery->id,
                'money_delivery_id' => $moneyDelivery->id,
                'rewarded_at' => null,
                'retry_count' => 0,
            ]);

            // 5. Record Unified Audit Log
            AuditLog::create([
                'action_id' => $voteUuid,
                'actor_type' => 'SYSTEM',
                'actor_id' => null,
                'actor_name' => 'Vote Bridge (' . $site->name . ')',
                'action' => 'VOTE_REWARD',
                'target_type' => 'PLAYER',
                'target_id' => $uuid,
                'target_name' => $username,
                'old_value' => null,
                'new_value' => '3x Vote Keys + Rp 1.000',
                'reason' => 'Official vote confirmation on ' . $site->name . ' [' . $source . ']',
                'source' => 'WEB_BRIDGE',
                'status' => 'PENDING',
                'metadata' => [
                    'vote_uuid' => $voteUuid,
                    'external_vote_id' => $externalId,
                    'site_slug' => $site->slug,
                    'key_delivery_id' => $keyDelivery->id,
                    'money_delivery_id' => $moneyDelivery->id,
                    'source' => $source,
                ],
            ]);

            return [
                'success' => true,
                'vote_uuid' => $voteUuid,
                'external_vote_id' => $externalId,
                'keys_amount' => 3,
                'money_amount' => 1000.00,
                'message' => "Suara sah di {$site->name} berhasil dikonfirmasi! 3x Vote Keys dan Rp 1.000 telah dimasukkan ke antrean pengiriman akun {$username}.",
            ];
        });
    }

    /**
     * Granular retry: Retry only key delivery.
     * Idempotent: checks if keys were already delivered.
     */
    public function retryKeyDelivery(VoteTransaction $tx, string $actorName): array
    {
        if ($tx->keys_delivery_id) {
            $existingDelivery = Delivery::find($tx->keys_delivery_id);
            if ($existingDelivery && $existingDelivery->status === 'DELIVERED') {
                return [
                    'success' => false,
                    'message' => "Kunci peti untuk transaksi ini telah berstatus DELIVERED sebelumnya.",
                ];
            }
        }

        $username = $tx->player_username;
        $uuid = $tx->player_uuid;

        $keyDelivery = Delivery::create([
            'action_id' => $tx->vote_uuid . '_KEY_RETRY_' . time(),
            'idempotency_key' => 'RETRY_KEY_' . $username . '_' . time() . '_' . Str::random(4),
            'player_uuid' => $uuid,
            'player_username' => $username,
            'command' => "crates key give {$username} vote 3",
            'status' => 'PENDING',
        ]);

        $tx->update([
            'keys_delivery_id' => $keyDelivery->id,
            'reward_status' => 'PENDING',
            'retry_count' => $tx->retry_count + 1,
            'failure_reason' => null,
        ]);

        $this->logAdminRetry($tx, $actorName, 'VOTE_KEY_RETRY', 'Kunci Peti (3x Keys)');

        return [
            'success' => true,
            'message' => "Pengiriman 3x Vote Keys untuk {$username} berhasil dimasukkan ulang ke antrean!",
        ];
    }

    /**
     * Granular retry: Retry only money delivery.
     * Idempotent: checks if money was already delivered.
     */
    public function retryMoneyDelivery(VoteTransaction $tx, string $actorName): array
    {
        if ($tx->money_delivery_id) {
            $existingDelivery = Delivery::find($tx->money_delivery_id);
            if ($existingDelivery && $existingDelivery->status === 'DELIVERED') {
                return [
                    'success' => false,
                    'message' => "Saldo uang untuk transaksi ini telah berstatus DELIVERED sebelumnya.",
                ];
            }
        }

        $username = $tx->player_username;
        $uuid = $tx->player_uuid;

        $moneyDelivery = Delivery::create([
            'action_id' => $tx->vote_uuid . '_MONEY_RETRY_' . time(),
            'idempotency_key' => 'RETRY_MONEY_' . $username . '_' . time() . '_' . Str::random(4),
            'player_uuid' => $uuid,
            'player_username' => $username,
            'command' => "ecoadmin give {$username} 1000 rupiah",
            'status' => 'PENDING',
        ]);

        $tx->update([
            'money_delivery_id' => $moneyDelivery->id,
            'reward_status' => 'PENDING',
            'retry_count' => $tx->retry_count + 1,
            'failure_reason' => null,
        ]);

        $this->logAdminRetry($tx, $actorName, 'VOTE_MONEY_RETRY', 'Saldo Uang (Rp 1.000)');

        return [
            'success' => true,
            'message' => "Pengiriman Rp 1.000 untuk {$username} berhasil dimasukkan ulang ke antrean!",
        ];
    }

    /**
     * Full retry: Safely retries only what is missing or failed (keys, money, or both).
     */
    public function retryFullReward(VoteTransaction $tx, string $actorName): array
    {
        if ($tx->reward_status === 'REWARDED') {
            return [
                'success' => false,
                'message' => 'Transaksi ini telah berstatus REWARDED dan tidak memerlukan retry.',
            ];
        }

        $username = $tx->player_username;
        $uuid = $tx->player_uuid;

        $retriedItems = [];

        // Check keys
        $requeueKey = true;
        if ($tx->keys_delivery_id) {
            $kd = Delivery::find($tx->keys_delivery_id);
            if ($kd && $kd->status === 'DELIVERED') {
                $requeueKey = false;
            }
        }

        if ($requeueKey) {
            $keyDelivery = Delivery::create([
                'action_id' => $tx->vote_uuid . '_KEY_RETRY_' . time(),
                'idempotency_key' => 'RETRY_KEY_' . $username . '_' . time() . '_' . Str::random(4),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => "crates key give {$username} vote 3",
                'status' => 'PENDING',
            ]);
            $tx->keys_delivery_id = $keyDelivery->id;
            $retriedItems[] = '3x Keys';
        }

        // Check money
        $requeueMoney = true;
        if ($tx->money_delivery_id) {
            $md = Delivery::find($tx->money_delivery_id);
            if ($md && $md->status === 'DELIVERED') {
                $requeueMoney = false;
            }
        }

        if ($requeueMoney) {
            $moneyDelivery = Delivery::create([
                'action_id' => $tx->vote_uuid . '_MONEY_RETRY_' . time(),
                'idempotency_key' => 'RETRY_MONEY_' . $username . '_' . time() . '_' . Str::random(4),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => "ecoadmin give {$username} 1000 rupiah",
                'status' => 'PENDING',
            ]);
            $tx->money_delivery_id = $moneyDelivery->id;
            $retriedItems[] = 'Rp 1.000';
        }

        if (empty($retriedItems)) {
            return [
                'success' => false,
                'message' => 'Seluruh komponen hadiah sudah berhasil dikirimkan sebelumnya.',
            ];
        }

        $tx->reward_status = 'PENDING';
        $tx->rewarded_at = null;
        $tx->retry_count = $tx->retry_count + 1;
        $tx->failure_reason = null;
        $tx->save();

        $this->logAdminRetry($tx, $actorName, 'VOTE_REWARD_RETRY', implode(' + ', $retriedItems));

        return [
            'success' => true,
            'message' => "Imbalan (" . implode(' + ', $retriedItems) . ") untuk {$username} berhasil dikirim ulang secara aman!",
        ];
    }

    protected function logAdminRetry(VoteTransaction $tx, string $actorName, string $action, string $newValue): void
    {
        AuditLog::create([
            'action_id' => $tx->vote_uuid . '_RETRY_' . time(),
            'actor_type' => 'ADMIN',
            'actor_id' => null,
            'actor_name' => $actorName,
            'action' => $action,
            'target_type' => 'PLAYER',
            'target_id' => $tx->player_uuid,
            'target_name' => $tx->player_username,
            'old_value' => 'FAILED',
            'new_value' => $newValue,
            'reason' => "Admin manual retry ({$action}) for Vote " . $tx->vote_uuid,
            'source' => 'WEB_ADMIN',
            'status' => 'PENDING',
            'metadata' => [
                'vote_id' => $tx->id,
                'vote_uuid' => $tx->vote_uuid,
                'retry_count' => $tx->retry_count,
            ],
        ]);
    }

    /**
     * Calculate comprehensive voter statistics for a specific player.
     */
    public function calculateVoterStats(string $username, ?string $uuid = null): array
    {
        $cleanUsername = ltrim($username, '.');
        $variants = array_values(array_unique([$username, $cleanUsername, '.' . $cleanUsername]));

        $baseQuery = VoteTransaction::where('vote_status', 'VALID')
            ->where(function ($q) use ($variants, $uuid) {
                $q->whereIn('player_username', $variants);
                if ($uuid) {
                    $q->orWhere('player_uuid', $uuid);
                }
            });

        $totalVotes = (clone $baseQuery)->count();
        $votesThisMonth = (clone $baseQuery)->where('voted_at', '>=', now()->startOfMonth())->count();
        $votesThisWeek = (clone $baseQuery)->where('voted_at', '>=', now()->startOfWeek())->count();
        $votesToday = (clone $baseQuery)->where('voted_at', '>=', now()->startOfDay())->count();

        // Calculate active daily voting streak
        $streak = 0;
        $checkDate = now()->startOfDay();

        // If not voted today yet, check if voted yesterday to keep streak active
        $votedToday = (clone $baseQuery)->whereBetween('voted_at', [$checkDate, now()])->exists();
        if (!$votedToday) {
            $checkDate = $checkDate->subDay();
        }

        while (true) {
            $start = $checkDate->copy()->startOfDay();
            $end = $checkDate->copy()->endOfDay();
            $hasVote = (clone $baseQuery)->whereBetween('voted_at', [$start, $end])->exists();
            if ($hasVote) {
                $streak++;
                $checkDate->subDay();
                if ($streak > 365) break; // sanity limit
            } else {
                break;
            }
        }

        $lastVote = (clone $baseQuery)->orderBy('voted_at', 'desc')->first();

        return [
            'total' => $totalVotes,
            'this_month' => $votesThisMonth,
            'this_week' => $votesThisWeek,
            'today' => $votesToday,
            'streak' => $streak,
            'last_voted_at' => $lastVote?->voted_at,
        ];
    }
}
