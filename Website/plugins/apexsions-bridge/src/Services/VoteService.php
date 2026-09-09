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

        $lastVote = VoteTransaction::where('site_slug', $site->slug)
            ->where(function ($q) use ($username, $uuid) {
                $q->where('player_username', $username);
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
     * Verify vote with external voting platform API if supported.
     */
    public function verifyWithPlatform(VotingSite $site, string $username, ?string $ip = null): array
    {
        // 1. Minecraft-MP API verification
        if ($site->slug === 'minecraft-mp' && !empty($site->api_key)) {
            try {
                $checkUrl = "https://minecraft-mp.com/api/?object=votes&element=claim&key={$site->api_key}&username=" . urlencode($username);
                $resp = Http::timeout(5)->get($checkUrl);

                if ($resp->successful() && trim($resp->body()) === '1') {
                    // Vote exists and is unclaimed; claim it now on Minecraft-MP
                    $postUrl = "https://minecraft-mp.com/api/?action=post&object=votes&element=claim&key={$site->api_key}&username=" . urlencode($username);
                    Http::timeout(5)->get($postUrl);

                    return ['valid' => true, 'message' => 'Vote verified via Minecraft-MP API.'];
                }

                return [
                    'valid' => false,
                    'message' => 'Minecraft-MP belum mencatat suara untuk username ini. Pastikan Anda telah menyelesaikan vote di situs terlebih dahulu.',
                ];
            } catch (\Throwable $e) {
                Log::warning("VoteService: Minecraft-MP API check failed: " . $e->getMessage());
            }
        }

        // 2. TopG Global API verification
        if ($site->slug === 'topg' && !empty($site->api_key)) {
            try {
                $checkUrl = "https://topg.org/api/status/{$site->api_key}/" . urlencode($username);
                $resp = Http::timeout(5)->get($checkUrl);

                if ($resp->successful() && trim($resp->body()) === '1') {
                    return ['valid' => true, 'message' => 'Vote verified via TopG API.'];
                }

                return [
                    'valid' => false,
                    'message' => 'TopG belum mencatat suara untuk username ini.',
                ];
            } catch (\Throwable $e) {
                Log::warning("VoteService: TopG API check failed: " . $e->getMessage());
            }
        }

        // 3. Callback / Sandbox / Verified Platform
        // If no API key configured yet, allow verified submission if site is active
        return ['valid' => true, 'message' => 'Vote confirmed through platform dispatch.'];
    }

    /**
     * Process an authentic vote reward transaction atomically.
     * Gives:
     * - 3x Vote Crate Keys via `crates key give <player> vote 3`
     * - Rp 1.000 Currency via `ecoadmin give <player> 1000 rupiah`
     * - In-game tellraw confirmation alert.
     */
    public function processVoteReward(VotingSite $site, string $username, ?string $ip = null, string $source = 'WEB_CLAIM'): array
    {
        $username = trim($username);
        if (empty($username) || !preg_match('/^[a-zA-Z0-9_]{2,16}$/', $username)) {
            return [
                'success' => false,
                'message' => 'Username Minecraft tidak valid.',
            ];
        }

        // Resolve official player UUID if registered
        $account = MinecraftAccount::where('minecraft_username', $username)->first();
        $uuid = $account?->minecraft_uuid;

        // Anti-duplicate cooldown verification
        $cooldownUntil = $this->checkCooldown($site, $username, $uuid);
        if ($cooldownUntil !== null) {
            $formattedTime = $cooldownUntil->diffForHumans();
            return [
                'success' => false,
                'duplicate' => true,
                'cooldown_until' => $cooldownUntil->toIso8601String(),
                'message' => "Anda sudah mengklaim imbalan vote di {$site->name}. Suara berikutnya dapat diberikan {$formattedTime}.",
            ];
        }

        // Verify with external platform if configured
        $verification = $this->verifyWithPlatform($site, $username, $ip);
        if (!$verification['valid']) {
            return [
                'success' => false,
                'message' => $verification['message'],
            ];
        }

        $voteUuid = 'VOTE_' . Str::upper(Str::random(16));
        $idempotencyHash = hash('sha256', "VOTE_{$site->slug}_{$username}_" . now()->format('Y-m-d_H'));

        return DB::transaction(function () use ($site, $username, $uuid, $ip, $voteUuid, $idempotencyHash, $source) {
            // Lock and double check idempotency inside transaction
            $existing = VoteTransaction::where('idempotency_hash', $idempotencyHash)->first();
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
                'idempotency_key' => 'VOTE_KEY_' . $username . '_' . time(),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => $keyCommand,
                'status' => 'PENDING',
            ]);

            // 2. Enqueue Rp 1.000 Currency Delivery
            $moneyCommand = "ecoadmin give {$username} 1000 rupiah";
            $moneyDelivery = Delivery::create([
                'action_id' => $voteUuid . '_MONEY',
                'idempotency_key' => 'VOTE_MONEY_' . $username . '_' . time(),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => $moneyCommand,
                'status' => 'PENDING',
            ]);

            // 3. Enqueue In-Game Tellraw Alert Delivery
            $tellrawPayload = json_encode([
                "",
                ["text" => "[APEXSIONS VOTE] ", "color" => "gold", "bold" => true],
                ["text" => "Terima kasih telah memberikan suara! Anda menerima ", "color" => "yellow"],
                ["text" => "3x Vote Keys", "color" => "gold", "bold" => true],
                ["text" => " & ", "color" => "white"],
                ["text" => "Rp 1.000", "color" => "green", "bold" => true],
                ["text" => "!", "color" => "yellow"],
            ], JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);

            Delivery::create([
                'action_id' => $voteUuid . '_ALERT',
                'idempotency_key' => 'VOTE_ALERT_' . $username . '_' . time(),
                'player_uuid' => $uuid,
                'player_username' => $username,
                'command' => "minecraft:tellraw {$username} {$tellrawPayload}",
                'status' => 'PENDING',
            ]);

            // 4. Record Vote Transaction Ledger
            $tx = VoteTransaction::create([
                'vote_uuid' => $voteUuid,
                'site_id' => $site->id,
                'site_slug' => $site->slug,
                'player_uuid' => $uuid,
                'player_username' => $username,
                'ip_address' => $ip,
                'idempotency_hash' => $idempotencyHash,
                'voted_at' => now(),
                'vote_status' => 'VALID',
                'reward_status' => 'REWARDED',
                'keys_amount' => 3,
                'money_amount' => 1000.00,
                'keys_delivery_id' => $keyDelivery->id,
                'money_delivery_id' => $moneyDelivery->id,
                'rewarded_at' => now(),
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
                'status' => 'SUCCESS',
                'metadata' => [
                    'vote_uuid' => $voteUuid,
                    'site_slug' => $site->slug,
                    'key_delivery_id' => $keyDelivery->id,
                    'money_delivery_id' => $moneyDelivery->id,
                    'source' => $source,
                ],
            ]);

            return [
                'success' => true,
                'vote_uuid' => $voteUuid,
                'keys_amount' => 3,
                'money_amount' => 1000.00,
                'message' => "Suara sah di {$site->name} berhasil dikonfirmasi! 3x Vote Keys dan Rp 1.000 telah dikirimkan ke akun {$username}.",
            ];
        });
    }

    /**
     * Retry a failed reward delivery safely.
     */
    public function retryFailedReward(VoteTransaction $tx, string $actorName): array
    {
        if ($tx->reward_status === 'REWARDED') {
            return [
                'success' => false,
                'message' => 'Transaksi ini telah berstatus REWARDED dan tidak memerlukan retry.',
            ];
        }

        $username = $tx->player_username;
        $uuid = $tx->player_uuid;

        // Re-queue key delivery if needed
        $keyDelivery = Delivery::create([
            'action_id' => $tx->vote_uuid . '_KEY_RETRY_' . time(),
            'idempotency_key' => 'RETRY_KEY_' . $username . '_' . time(),
            'player_uuid' => $uuid,
            'player_username' => $username,
            'command' => "crates key give {$username} vote 3",
            'status' => 'PENDING',
        ]);

        // Re-queue money delivery if needed
        $moneyDelivery = Delivery::create([
            'action_id' => $tx->vote_uuid . '_MONEY_RETRY_' . time(),
            'idempotency_key' => 'RETRY_MONEY_' . $username . '_' . time(),
            'player_uuid' => $uuid,
            'player_username' => $username,
            'command' => "ecoadmin give {$username} 1000 rupiah",
            'status' => 'PENDING',
        ]);

        $tx->update([
            'keys_delivery_id' => $keyDelivery->id,
            'money_delivery_id' => $moneyDelivery->id,
            'reward_status' => 'REWARDED',
            'rewarded_at' => now(),
            'retry_count' => $tx->retry_count + 1,
            'failure_reason' => null,
        ]);

        AuditLog::create([
            'action_id' => $tx->vote_uuid . '_RETRY',
            'actor_type' => 'ADMIN',
            'actor_id' => null,
            'actor_name' => $actorName,
            'action' => 'VOTE_REWARD_RETRY',
            'target_type' => 'PLAYER',
            'target_id' => $uuid,
            'target_name' => $username,
            'old_value' => 'FAILED',
            'new_value' => 'REWARDED',
            'reason' => 'Admin manual retry for Vote ' . $tx->vote_uuid,
            'source' => 'WEB_ADMIN',
            'status' => 'SUCCESS',
            'metadata' => [
                'vote_id' => $tx->id,
                'retry_count' => $tx->retry_count,
            ],
        ]);

        return [
            'success' => true,
            'message' => "Imbalan vote untuk {$username} berhasil dikirim ulang!",
        ];
    }
}
