<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Auction;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\KingdomTreasury;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\Transaction;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class EconomyAdminService
{
    /**
     * Compile verified metrics for the Economy Inspector.
     * Differentiates calculated vs real-time sources, avoiding synthetic data.
     *
     * @return array<string, mixed>
     */
    public static function getEconomyOverview(): array
    {
        $now = Carbon::now()->toIso8601String();

        // 1. Currency Supply Aggregation (from synchronized player accounts)
        $totalRupiah = (float) MinecraftAccount::sum('balance_rupiah');
        $totalDiamond = (float) MinecraftAccount::sum('balance_diamond');
        $totalApexCoins = (float) MinecraftAccount::sum('apex_coins');
        $activeHolders = MinecraftAccount::where('balance_rupiah', '>', 0)
            ->orWhere('balance_diamond', '>', 0)
            ->count();

        // 2. Transaction Flow Metrics
        $transactionCount = Transaction::count();
        $volumeRupiah = (float) Transaction::where('currency', 'rupiah')
            ->where('status', 'COMPLETED')
            ->sum('amount');
        $volumeDiamond = (float) Transaction::where('currency', 'diamond')
            ->where('status', 'COMPLETED')
            ->sum('amount');
        $taxCollectedRupiah = (float) Transaction::where('currency', 'rupiah')
            ->where('status', 'COMPLETED')
            ->sum('tax_amount');

        // 3. Auction House Metrics
        $activeAuctions = Auction::where('status', 'ACTIVE')->count();
        $quarantinedAuctions = Auction::where('status', 'QUARANTINED')->count();
        $totalAuctions = Auction::count();

        // 4. Kingdom Treasury Reserves
        $treasuryReserves = (float) KingdomTreasury::where('currency', 'rupiah')->sum('balance');
        $kingdoms = KingdomTreasury::orderBy('balance', 'desc')->get();

        return [
            'metrics' => [
                'total_rupiah_supply' => [
                    'label' => 'Total Pasokan Rupiah',
                    'value' => $totalRupiah,
                    'formatted' => 'Rp ' . number_format($totalRupiah, 0, ',', '.'),
                    'source' => 'Calculated: SUM(minecraft_accounts.balance_rupiah)',
                    'last_updated' => $now,
                ],
                'total_diamond_supply' => [
                    'label' => 'Total Pasokan Diamond',
                    'value' => $totalDiamond,
                    'formatted' => number_format($totalDiamond, 0, ',', '.') . ' 💎',
                    'source' => 'Calculated: SUM(minecraft_accounts.balance_diamond)',
                    'last_updated' => $now,
                ],
                'total_apex_coins' => [
                    'label' => 'Apex Coins Beredar',
                    'value' => $totalApexCoins,
                    'formatted' => number_format($totalApexCoins, 0, ',', '.') . ' AC',
                    'source' => 'Calculated: SUM(minecraft_accounts.apex_coins)',
                    'last_updated' => $now,
                ],
                'active_currency_holders' => [
                    'label' => 'Akun Bersaldo Aktif',
                    'value' => $activeHolders,
                    'formatted' => number_format($activeHolders, 0, ',', '.') . ' Pemain',
                    'source' => 'Calculated: COUNT(balance > 0)',
                    'last_updated' => $now,
                ],
                'transaction_count' => [
                    'label' => 'Total Transaksi Tercatat',
                    'value' => $transactionCount,
                    'formatted' => number_format($transactionCount, 0, ',', '.'),
                    'source' => 'Real-time: COUNT(apexsions_transactions)',
                    'last_updated' => $now,
                ],
                'volume_rupiah' => [
                    'label' => 'Volume Transaksi Rupiah',
                    'value' => $volumeRupiah,
                    'formatted' => 'Rp ' . number_format($volumeRupiah, 0, ',', '.'),
                    'source' => 'Calculated: SUM(completed transactions rupiah)',
                    'last_updated' => $now,
                ],
                'tax_collected_rupiah' => [
                    'label' => 'Total Pajak Transaksi Kerajaan',
                    'value' => $taxCollectedRupiah,
                    'formatted' => 'Rp ' . number_format($taxCollectedRupiah, 0, ',', '.'),
                    'source' => 'Calculated: SUM(tax_amount)',
                    'last_updated' => $now,
                ],
                'active_auctions' => [
                    'label' => 'Lelang Aktif',
                    'value' => $activeAuctions,
                    'formatted' => number_format($activeAuctions, 0, ',', '.') . ' Lot',
                    'source' => 'Real-time: COUNT(status=ACTIVE)',
                    'last_updated' => $now,
                ],
                'quarantined_auctions' => [
                    'label' => 'Lelang Dikarantina',
                    'value' => $quarantinedAuctions,
                    'formatted' => number_format($quarantinedAuctions, 0, ',', '.') . ' Lot',
                    'source' => 'Real-time: COUNT(status=QUARANTINED)',
                    'last_updated' => $now,
                ],
                'treasury_reserves' => [
                    'label' => 'Cadangan Kas Kerajaan',
                    'value' => $treasuryReserves,
                    'formatted' => 'Rp ' . number_format($treasuryReserves, 0, ',', '.'),
                    'source' => 'Aggregated: SUM(apexsions_kingdom_treasury.balance)',
                    'last_updated' => $now,
                ],
            ],
            'kingdoms' => $kingdoms,
            'recent_transactions' => Transaction::orderBy('created_at', 'desc')->limit(10)->get(),
            'recent_actions' => Delivery::whereIn('command', function ($q) {
                $q->select('command')->from('deliveries')->where('command', 'like', 'ecoadmin%');
            })->orderBy('created_at', 'desc')->limit(10)->get(),
        ];
    }

    /**
     * Secure, auditable balance adjustment for a player.
     * Restricts free arbitrary "set balance" by enforcing typed GIVE or DEDUCT operations with mandatory reasons.
     *
     * @return array{success: bool, message: string, action_id?: string, transaction?: Transaction}
     */
    public static function adjustBalance(
        MinecraftAccount $account,
        string $currency,
        float $amount,
        string $direction,
        string $reason,
        ?User $staff = null
    ): array {
        $currency = strtolower($currency);
        $direction = strtoupper($direction);
        $amount = abs($amount);

        if (!in_array($currency, ['rupiah', 'diamond', 'apex_coins'], true)) {
            return ['success' => false, 'message' => "Mata uang '{$currency}' tidak valid."];
        }

        if (!in_array($direction, ['GIVE', 'DEDUCT'], true)) {
            return ['success' => false, 'message' => "Arah penyesuaian harus bernilai GIVE atau DEDUCT."];
        }

        if ($amount <= 0 || is_nan($amount) || is_infinite($amount)) {
            return ['success' => false, 'message' => 'Jumlah penyesuaian harus berupa angka positif valid.'];
        }

        if (empty(trim($reason))) {
            return ['success' => false, 'message' => 'Alasan penyesuaian saldo wajib diisi untuk keperluan audit trail.'];
        }

        $balanceField = match ($currency) {
            'rupiah' => 'balance_rupiah',
            'diamond' => 'balance_diamond',
            'apex_coins' => 'apex_coins',
        };

        $currentBalance = (float) ($account->{$balanceField} ?? 0);

        if ($direction === 'DEDUCT' && $currentBalance < $amount) {
            return [
                'success' => false,
                'message' => "Saldo {$currency} pemain saat ini ({$currentBalance}) tidak mencukupi untuk dikurangi sebesar {$amount}.",
            ];
        }

        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';
        $newBalance = $direction === 'GIVE' ? ($currentBalance + $amount) : ($currentBalance - $amount);

        return DB::transaction(function () use (
            $account,
            $currency,
            $amount,
            $direction,
            $reason,
            $actionId,
            $staffId,
            $staffName,
            $balanceField,
            $currentBalance,
            $newBalance
        ) {
            // 1. Audit Log (PENDING)
            $audit = AuditService::start(
                'ECONOMY_BALANCE_ADJUSTED',
                'PLAYER',
                $account->minecraft_uuid,
                $account->minecraft_username,
                $reason,
                [$balanceField => $currentBalance],
                [
                    'action_id' => $actionId,
                    'staff' => $staffName,
                    'staff_id' => $staffId,
                    'direction' => $direction,
                    'amount' => $amount,
                    'currency' => $currency,
                    'new_balance' => $newBalance,
                ],
                'WEB'
            );

            // 2. Update local cached balance
            $account->update([
                $balanceField => $newBalance,
            ]);

            // 3. Record in apexsions_transactions ledger
            $transaction = Transaction::create([
                'transaction_id' => (string) Str::uuid(),
                'type' => 'ADMIN_ADJUST',
                'sender_uuid' => $direction === 'GIVE' ? null : $account->minecraft_uuid,
                'sender_name' => $direction === 'GIVE' ? "Staff: {$staffName}" : $account->minecraft_username,
                'receiver_uuid' => $direction === 'GIVE' ? $account->minecraft_uuid : null,
                'receiver_name' => $direction === 'GIVE' ? $account->minecraft_username : "Staff: {$staffName}",
                'currency' => $currency,
                'amount' => $amount,
                'tax_amount' => 0,
                'net_amount' => $amount,
                'reason' => "[{$direction}] {$reason}",
                'status' => 'COMPLETED',
                'source' => 'WEB_DASHBOARD',
                'action_id' => $actionId,
                'metadata' => [
                    'staff_id' => $staffId,
                    'staff_name' => $staffName,
                    'direction' => $direction,
                    'old_balance' => $currentBalance,
                    'new_balance' => $newBalance,
                ],
            ]);

            // 4. Enqueue template bridge command for Minecraft server
            $cmdAction = $direction === 'GIVE' ? 'give' : 'take';
            $command = "ecoadmin {$cmdAction} {$account->minecraft_username} {$amount} {$currency}";

            Delivery::create([
                'action_id' => $actionId,
                'player_uuid' => $account->minecraft_uuid,
                'player_username' => $account->minecraft_username,
                'command' => $command,
                'status' => 'PENDING',
            ]);

            // 5. Complete Audit Log
            AuditService::success($audit, [$balanceField => $newBalance]);

            return [
                'success' => true,
                'message' => "Berhasil {$cmdAction} {$amount} {$currency} untuk {$account->minecraft_username}.",
                'action_id' => $actionId,
                'transaction' => $transaction,
                'new_balance' => $newBalance,
            ];
        });
    }

    /**
     * Quarantine an auction listing to halt transactions without deleting evidence.
     *
     * @return array{success: bool, message: string}
     */
    public static function quarantineAuction(Auction $auction, string $reason, User $staff): array
    {
        if (in_array($auction->status, ['SOLD', 'CANCELLED'], true)) {
            return ['success' => false, 'message' => "Lelang sudah berstatus {$auction->status} dan tidak dapat dikarantina."];
        }

        if (empty(trim($reason))) {
            return ['success' => false, 'message' => 'Alasan karantina lelang wajib diisi.'];
        }

        $oldStatus = $auction->status;
        $actionId = (string) Str::uuid();

        return DB::transaction(function () use ($auction, $reason, $staff, $oldStatus, $actionId) {
            $auction->update([
                'status' => 'QUARANTINED',
                'quarantined_by' => $staff->name,
                'quarantine_reason' => trim($reason),
                'quarantined_at' => now(),
            ]);

            AuditService::log([
                'actor_type' => 'STAFF',
                'actor_id' => (string) $staff->id,
                'actor_name' => $staff->name,
                'action' => 'AUCTION_QUARANTINED',
                'target_type' => 'AUCTION',
                'target_id' => $auction->auction_id,
                'target_name' => "Auction #{$auction->auction_id} ({$auction->item_name})",
                'old_value' => ['status' => $oldStatus],
                'new_value' => ['status' => 'QUARANTINED', 'reason' => $reason],
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'SUCCESS',
            ]);

            // Queue command to cancel active listing in-game
            Delivery::create([
                'action_id' => $actionId,
                'player_uuid' => $auction->seller_uuid,
                'player_username' => $auction->seller_name,
                'command' => "ah admin cancel {$auction->auction_id}",
                'status' => 'PENDING',
            ]);

            return [
                'success' => true,
                'message' => "Lelang #{$auction->auction_id} ({$auction->item_name}) berhasil dipindahkan ke status QUARANTINED.",
            ];
        });
    }

    /**
     * Cancel an auction listing and return item to seller safely.
     *
     * @return array{success: bool, message: string}
     */
    public static function cancelAuction(Auction $auction, string $reason, User $staff): array
    {
        if ($auction->status === 'CANCELLED' || $auction->status === 'SOLD') {
            return ['success' => false, 'message' => "Lelang sudah berstatus {$auction->status}."];
        }

        $oldStatus = $auction->status;
        $actionId = (string) Str::uuid();

        return DB::transaction(function () use ($auction, $reason, $staff, $oldStatus, $actionId) {
            $auction->update([
                'status' => 'CANCELLED',
            ]);

            AuditService::log([
                'actor_type' => 'STAFF',
                'actor_id' => (string) $staff->id,
                'actor_name' => $staff->name,
                'action' => 'AUCTION_CANCELLED',
                'target_type' => 'AUCTION',
                'target_id' => $auction->auction_id,
                'target_name' => "Auction #{$auction->auction_id} ({$auction->item_name})",
                'old_value' => ['status' => $oldStatus],
                'new_value' => ['status' => 'CANCELLED'],
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'SUCCESS',
            ]);

            Delivery::create([
                'action_id' => $actionId,
                'player_uuid' => $auction->seller_uuid,
                'player_username' => $auction->seller_name,
                'command' => "ah admin cancel {$auction->auction_id}",
                'status' => 'PENDING',
            ]);

            return [
                'success' => true,
                'message' => "Lelang #{$auction->auction_id} berhasil dibatalkan dan dikembalikan ke antrean pengembalian in-game.",
            ];
        });
    }
}
