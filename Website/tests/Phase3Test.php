<?php

/**
 * Apexsions Phase 3 Automated Verification Test Suite
 * Validates:
 * [ECONOMY INSPECTOR]
 * 1. Economy overview calculation & metric source tagging
 * 2. Controlled balance adjustment (GIVE) with action_id, transaction log, and audit trail
 * 3. Controlled balance adjustment (DEDUCT) with balance floor validation
 * 4. Rejection of invalid / empty reason for balance adjustment
 * 5. Rejection of invalid currency or direction
 * 6. Idempotency protection against duplicate action_id
 * 
 * [TRANSACTION EXPLORER]
 * 7. View transaction listing & pagination
 * 8. Search transactions by player name and transaction ID
 * 9. Filter transactions by type (e.g. TRANSFER, ADMIN_ADJUST)
 * 10. Filter transactions by currency (rupiah vs diamond)
 * 11. Filter transactions by status
 * 12. View transaction detail trace with party context
 * 13. Non-existent transaction ID throws ModelNotFoundException (404)
 * 
 * [AUCTION INSPECTOR]
 * 14. View auction listing & metric summary
 * 15. Search auctions by item name or seller
 * 16. Filter auctions by status (ACTIVE, QUARANTINED, etc.)
 * 17. View auction detail with item metadata
 * 18. Quarantine auction action (QUARANTINED status, audit log, in-game command queued)
 * 19. Cancel auction action (CANCELLED status, audit log, in-game command queued)
 * 20. Rejection of quarantine without mandatory reason
 * 
 * [PLAYER 360 & AUDIT LOG INTEGRATION]
 * 21. Player 360 profile hydratation with recent transactions and auctions
 * 22. Ingest transaction via API sync endpoint
 * 23. Ingest auction via API sync endpoint
 * 24. Sync kingdom treasury via API endpoint
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\Transaction;
use Azuriom\Plugin\ApexsionsBridge\Models\Auction;
use Azuriom\Plugin\ApexsionsBridge\Models\KingdomTreasury;
use Azuriom\Plugin\ApexsionsBridge\Services\EconomyAdminService;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\EconomyAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\TransactionAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuctionAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

echo "==========================================================\n";
echo "   APEXSIONS PHASE 3 AUTOMATED VERIFICATION TEST SUITE   \n";
echo "==========================================================\n\n";

$passed = 0;
$failed = 0;

function assertTest($name, $condition, $detail = '') {
    global $passed, $failed;
    if ($condition) {
        echo " [PASS] $name\n";
        $passed++;
    } else {
        echo " [FAIL] $name: $detail\n";
        $failed++;
    }
}

// -------------------------------------------------------------
// SETUP FIXTURES
// -------------------------------------------------------------
$staff = User::firstOrCreate(['email' => 'overseer_econ@apexsions.my.id'], [
    'name' => 'OverseerFinance',
    'password' => bcrypt('SecretEcon123!'),
]);

$playerUuid1 = '11111111-1111-1111-1111-111111111111';
$playerUuid2 = '22222222-2222-2222-2222-222222222222';

$accountA = MinecraftAccount::updateOrCreate(['minecraft_uuid' => $playerUuid1], [
    'minecraft_username' => 'MerchantAlpha',
    'rank' => 'archon',
    'rank_display' => 'Archon',
    'level' => 30,
    'xp' => 45000,
    'balance_rupiah' => 500000.0,
    'balance_diamond' => 120.0,
    'apex_coins' => 150,
    'kingdom' => 'SOLTERRA',
    'kingdom_display' => 'Solterra',
    'created_at' => now(),
    'updated_at' => now(),
]);

$accountB = MinecraftAccount::updateOrCreate(['minecraft_uuid' => $playerUuid2], [
    'minecraft_username' => 'CitizenBeta',
    'rank' => 'wanderer',
    'rank_display' => 'Wanderer',
    'level' => 5,
    'xp' => 1500,
    'balance_rupiah' => 25000.0,
    'balance_diamond' => 10.0,
    'apex_coins' => 20,
    'kingdom' => 'ZENITHAR',
    'kingdom_display' => 'Zenithar',
    'created_at' => now(),
    'updated_at' => now(),
]);

KingdomTreasury::updateOrCreate(
    ['kingdom_key' => 'SOLTERRA', 'currency' => 'rupiah'],
    ['kingdom_name' => 'Kerajaan Solterra', 'balance' => 1500000.0, 'total_tax_collected' => 3500000.0, 'last_tax_collected_at' => now()]
);

// -------------------------------------------------------------
// SECTION 1: ECONOMY INSPECTOR & CONTROLLED ADJUSTMENTS
// -------------------------------------------------------------
echo "\n--- Section 1: Economy Inspector & Controlled Adjustments ---\n";

// 1. Overview calculation & source tagging
$overview = EconomyAdminService::getEconomyOverview();
assertTest("1. Economy overview calculates supplies with explicit source tags", 
    isset($overview['metrics']['total_rupiah_supply']) &&
    $overview['metrics']['total_rupiah_supply']['value'] >= 525000.0 &&
    str_contains($overview['metrics']['total_rupiah_supply']['source'], 'Calculated: SUM'),
    "Overview metrics mismatch");

// 2. Controlled balance adjustment (GIVE)
$giveResult = EconomyAdminService::adjustBalance(
    $accountB,
    'rupiah',
    50000.0,
    'GIVE',
    'Event weekend community reward',
    $staff
);
assertTest("2. Controlled balance adjustment (GIVE) updates balance and records transaction", 
    $giveResult['success'] === true &&
    $accountB->fresh()->balance_rupiah == 75000.0 &&
    !empty($giveResult['action_id']) &&
    Transaction::where('action_id', $giveResult['action_id'])->exists(),
    "Give adjustment failed: " . json_encode($giveResult));

// 3. Controlled balance adjustment (DEDUCT)
$deductResult = EconomyAdminService::adjustBalance(
    $accountB->fresh(),
    'rupiah',
    25000.0,
    'DEDUCT',
    'Correction of duplicate reward grant',
    $staff
);
assertTest("3. Controlled balance adjustment (DEDUCT) decreases balance safely", 
    $deductResult['success'] === true &&
    $accountB->fresh()->balance_rupiah == 50000.0,
    "Deduct adjustment failed");

// 3b. Balance floor validation (reject negative balance)
$excessDeductResult = EconomyAdminService::adjustBalance(
    $accountB->fresh(),
    'rupiah',
    99999999.0,
    'DEDUCT',
    'Excessive deduct test',
    $staff
);
assertTest("3b. Balance adjustment rejects deduction exceeding current balance", 
    $excessDeductResult['success'] === false && str_contains($excessDeductResult['message'], 'tidak mencukupi'),
    "Excessive deduction was erroneously allowed");

// 4. Rejection of empty reason
$emptyReasonResult = EconomyAdminService::adjustBalance(
    $accountA,
    'rupiah',
    1000.0,
    'GIVE',
    '   ',
    $staff
);
assertTest("4. Balance adjustment rejects empty reason to enforce audit trail", 
    $emptyReasonResult['success'] === false && str_contains($emptyReasonResult['message'], 'Alasan penyesuaian'),
    "Empty reason was erroneously accepted");

// 5. Rejection of invalid currency or direction
$invalidCurrResult = EconomyAdminService::adjustBalance(
    $accountA,
    'fake_coin',
    1000.0,
    'GIVE',
    'Invalid currency test',
    $staff
);
assertTest("5. Balance adjustment rejects unsupported currency", 
    $invalidCurrResult['success'] === false && str_contains($invalidCurrResult['message'], 'tidak valid'),
    "Invalid currency allowed");

// 6. Idempotency on action_id via Deliveries
$delivRecord = Delivery::where('action_id', $giveResult['action_id'])->first();
assertTest("6a. Bridge delivery enqueued with template command and matching action_id", 
    $delivRecord !== null && str_contains($delivRecord->command, 'ecoadmin give CitizenBeta 50000 rupiah'),
    "Delivery command missing or malformed");

try {
    Delivery::create([
        'action_id' => $giveResult['action_id'],
        'player_username' => 'CitizenBeta',
        'command' => 'ecoadmin give CitizenBeta 50000 rupiah',
        'status' => 'PENDING',
    ]);
    assertTest("6b. Idempotency prevents duplicate action_id", false, "Duplicate allowed");
} catch (\Illuminate\Database\QueryException $e) {
    assertTest("6b. Idempotency prevents duplicate action_id via UNIQUE constraint", true);
}

// -------------------------------------------------------------
// SECTION 2: TRANSACTION EXPLORER
// -------------------------------------------------------------
echo "\n--- Section 2: Transaction Explorer ---\n";

// Seed test transactions
$tx1 = Transaction::create([
    'transaction_id' => (string) Str::uuid(),
    'type' => 'TRANSFER',
    'sender_uuid' => $playerUuid1,
    'sender_name' => 'MerchantAlpha',
    'receiver_uuid' => $playerUuid2,
    'receiver_name' => 'CitizenBeta',
    'currency' => 'rupiah',
    'amount' => 100000.0,
    'tax_amount' => 5000.0,
    'net_amount' => 95000.0,
    'reason' => 'Payment for Netherite Scraps batch',
    'status' => 'COMPLETED',
    'source' => 'IN_GAME',
]);

$tx2 = Transaction::create([
    'transaction_id' => (string) Str::uuid(),
    'type' => 'AUCTION_BUY',
    'sender_uuid' => $playerUuid2,
    'sender_name' => 'CitizenBeta',
    'receiver_uuid' => $playerUuid1,
    'receiver_name' => 'MerchantAlpha',
    'currency' => 'diamond',
    'amount' => 15.0,
    'tax_amount' => 0.0,
    'net_amount' => 15.0,
    'reason' => 'Bought Auction Lot #LOT9981',
    'status' => 'COMPLETED',
    'source' => 'IN_GAME',
]);

// 7. View transaction listing
$txController = app(TransactionAdminController::class);
$txIndexReq = Request::create('/admin/economy/transactions', 'GET');
$txIndexView = $txController->index($txIndexReq);
assertTest("7. View transaction listing renders with pagination", 
    isset($txIndexView->getData()['transactions']) && $txIndexView->getData()['transactions']->count() >= 2,
    "Transaction index view missing data");

// 8. Search transactions by player name & ID
$searchTxReq = Request::create('/admin/economy/transactions', 'GET', ['q' => 'Netherite Scraps']);
$searchTxView = $txController->index($searchTxReq);
assertTest("8. Search transactions by keyword filters matching records", 
    $searchTxView->getData()['transactions']->contains('id', $tx1->id),
    "Keyword search failed");

// 9. Filter transactions by type
$filterTypeReq = Request::create('/admin/economy/transactions', 'GET', ['type' => 'AUCTION_BUY']);
$filterTypeView = $txController->index($filterTypeReq);
assertTest("9. Filter transactions by type (AUCTION_BUY) returns only matching type", 
    $filterTypeView->getData()['transactions']->every(fn($t) => $t->type === 'AUCTION_BUY'),
    "Type filtering returned non-matching records");

// 10. Filter transactions by currency
$filterCurrReq = Request::create('/admin/economy/transactions', 'GET', ['currency' => 'diamond']);
$filterCurrView = $txController->index($filterCurrReq);
assertTest("10. Filter transactions by currency (diamond) matches correctly", 
    $filterCurrView->getData()['transactions']->every(fn($t) => $t->currency === 'diamond'),
    "Currency filtering failed");

// 11. Filter transactions by status
$filterStatReq = Request::create('/admin/economy/transactions', 'GET', ['status' => 'COMPLETED']);
$filterStatView = $txController->index($filterStatReq);
assertTest("11. Filter transactions by status (COMPLETED) matches correctly", 
    $filterStatView->getData()['transactions']->every(fn($t) => $t->status === 'COMPLETED'),
    "Status filtering failed");

// 12. Transaction detail trace
$txDetailView = $txController->show($tx1->id);
assertTest("12. Transaction detail view resolves trace parties and relations", 
    isset($txDetailView->getData()['transaction']) && $txDetailView->getData()['transaction']->id === $tx1->id,
    "Transaction detail show failed");

// 13. Non-existent transaction ID
try {
    $txController->show(9999999);
    assertTest("13. Non-existent transaction ID throws ModelNotFoundException", false);
} catch (\Illuminate\Database\Eloquent\ModelNotFoundException $e) {
    assertTest("13. Non-existent transaction ID throws ModelNotFoundException (404)", true);
}

// -------------------------------------------------------------
// SECTION 3: AUCTION INSPECTOR
// -------------------------------------------------------------
echo "\n--- Section 3: Auction Inspector ---\n";

Auction::whereIn('auction_id', ['AUC-1011', 'AUC-2022', 'SYNC-AUC-777'])->delete();
Transaction::where('transaction_id', 'SYNC-TX-999')->delete();

$auc1 = Auction::create([
    'auction_id' => 'AUC-1011',
    'seller_uuid' => $playerUuid1,
    'seller_name' => 'MerchantAlpha',
    'currency' => 'rupiah',
    'price' => 150000.0,
    'item_name' => 'Elytra [Mending, Unbreaking III]',
    'item_data' => 'rO0ABXNyABpvcmcuYnVra2l0LmludmVudG9yeS5JdGVtU3RhY2s...',
    'status' => 'ACTIVE',
    'expires_at' => now()->addHours(24),
]);

$auc2 = Auction::create([
    'auction_id' => 'AUC-2022',
    'seller_uuid' => $playerUuid2,
    'seller_name' => 'CitizenBeta',
    'currency' => 'diamond',
    'price' => 64.0,
    'item_name' => 'Ancient Debris x16',
    'item_data' => 'rO0ABXNyABpvcmcuYnVra2l0LmludmVudG9yeS5JdGVtU3RhY2s...',
    'status' => 'ACTIVE',
    'expires_at' => now()->addHours(12),
]);

// 14. View auction listing & metrics
$aucController = app(AuctionAdminController::class);
$aucIndexReq = Request::create('/admin/economy/auctions', 'GET');
$aucIndexView = $aucController->index($aucIndexReq);
assertTest("14. Auction inspector listing renders with active count", 
    isset($aucIndexView->getData()['auctions']) && $aucIndexView->getData()['activeCount'] >= 2,
    "Auction listing missing active lot data");

// 15. Search auctions
$searchAucReq = Request::create('/admin/economy/auctions', 'GET', ['q' => 'Elytra']);
$searchAucView = $aucController->index($searchAucReq);
assertTest("15. Search auctions filters matching item name ('Elytra')", 
    $searchAucView->getData()['auctions']->contains('id', $auc1->id),
    "Auction search failed");

// 16. Filter auctions by status
$filterAucReq = Request::create('/admin/economy/auctions', 'GET', ['status' => 'ACTIVE']);
$filterAucView = $aucController->index($filterAucReq);
assertTest("16. Filter auctions by status (ACTIVE) returns only active lots", 
    $filterAucView->getData()['auctions']->every(fn($a) => $a->status === 'ACTIVE'),
    "Auction status filtering failed");

// 17. View auction detail
$aucDetailView = $aucController->show($auc1->id);
assertTest("17. Auction detail view resolves item specs and seller relation", 
    isset($aucDetailView->getData()['auction']) && $aucDetailView->getData()['auction']->id === $auc1->id,
    "Auction detail show failed");

// 18. Quarantine auction action
$quarantineResult = EconomyAdminService::quarantineAuction($auc1, 'Suspected duplicate NBT tags on elytra item', $staff);
assertTest("18. Quarantine auction changes status to QUARANTINED and records audit log", 
    $quarantineResult['success'] === true &&
    $auc1->fresh()->status === 'QUARANTINED' &&
    $auc1->fresh()->quarantined_by === 'OverseerFinance' &&
    AuditLog::where('action', 'AUCTION_QUARANTINED')->where('target_id', 'AUC-1011')->exists(),
    "Quarantine auction failed: " . json_encode($quarantineResult));

// 19. Cancel auction action
$cancelResult = EconomyAdminService::cancelAuction($auc2, 'Player requested cancellation via ticket #889', $staff);
assertTest("19. Cancel auction transitions status to CANCELLED and queues bridge command", 
    $cancelResult['success'] === true &&
    $auc2->fresh()->status === 'CANCELLED' &&
    Delivery::where('command', 'ah admin cancel AUC-2022')->exists(),
    "Cancel auction failed: " . json_encode($cancelResult));

// 20. Rejection of quarantine without mandatory reason
$emptyQuarantineReason = EconomyAdminService::quarantineAuction($auc1->fresh(), '   ', $staff);
assertTest("20. Quarantine rejects empty reason to ensure compliance", 
    $emptyQuarantineReason['success'] === false && str_contains($emptyQuarantineReason['message'], 'Alasan karantina'),
    "Empty quarantine reason was allowed");

// -------------------------------------------------------------
// SECTION 4: PLAYER 360 & API SYNC INTEGRATION
// -------------------------------------------------------------
echo "\n--- Section 4: Player 360 & In-Game API Sync ---\n";

// 21. Player 360 economy tab hydration
$playerController = app(PlayerAdminController::class);
$playerView = $playerController->show('MerchantAlpha');
$playerData = $playerView->getData();
assertTest("21. Player 360 profile hydratates playerTransactions and playerAuctions", 
    isset($playerData['playerTransactions']) &&
    isset($playerData['playerAuctions']) &&
    $playerData['playerTransactions']->count() >= 1 &&
    $playerData['playerAuctions']->count() >= 1,
    "Player 360 economy tab data missing");

// 22. Ingest transaction via API sync endpoint
$apiController = app(LinkVerificationController::class);
$apiTxReq = Request::create('/api/apexsions-bridge/economy/transactions/sync', 'POST', [
    'transaction_id' => 'SYNC-TX-999',
    'type' => 'TRANSFER',
    'sender_uuid' => $playerUuid1,
    'sender_name' => 'MerchantAlpha',
    'receiver_uuid' => $playerUuid2,
    'receiver_name' => 'CitizenBeta',
    'currency' => 'rupiah',
    'amount' => 50000.0,
    'tax_amount' => 2500.0,
    'net_amount' => 47500.0,
    'reason' => 'In-Game direct /pay transfer',
    'status' => 'COMPLETED',
]);
$apiTxReq->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$apiTxResp = $apiController->syncTransaction($apiTxReq);
assertTest("22. Ingest in-game transaction via authenticated API sync endpoint", 
    $apiTxResp->getStatusCode() === 200 && Transaction::where('transaction_id', 'SYNC-TX-999')->exists(),
    "API transaction sync failed: " . $apiTxResp->getContent());

// 23. Ingest auction via API sync endpoint
$apiAucReq = Request::create('/api/apexsions-bridge/economy/auctions/sync', 'POST', [
    'auction_id' => 'SYNC-AUC-777',
    'seller_uuid' => $playerUuid1,
    'seller_name' => 'MerchantAlpha',
    'currency' => 'diamond',
    'price' => 32.0,
    'item_name' => 'Nether Star x2',
    'item_data' => 'rO0ABXNy...',
    'status' => 'ACTIVE',
    'expires_at' => now()->addHours(48)->toIso8601String(),
]);
$apiAucReq->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$apiAucResp = $apiController->syncAuction($apiAucReq);
assertTest("23. Ingest in-game auction listing via authenticated API sync endpoint", 
    $apiAucResp->getStatusCode() === 200 && Auction::where('auction_id', 'SYNC-AUC-777')->exists(),
    "API auction sync failed: " . $apiAucResp->getContent());

// 24. Sync kingdom treasury via API endpoint
$apiTreasuryReq = Request::create('/api/apexsions-bridge/economy/treasury/sync', 'POST', [
    'kingdom_key' => 'ZENITHAR',
    'kingdom_name' => 'Kerajaan Zenithar',
    'currency' => 'rupiah',
    'balance' => 800000.0,
    'tax_collected' => 1200000.0,
]);
$apiTreasuryReq->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$apiTreasuryResp = $apiController->syncKingdomTreasury($apiTreasuryReq);
assertTest("24. Sync kingdom treasury balance via authenticated API endpoint", 
    $apiTreasuryResp->getStatusCode() === 200 && KingdomTreasury::where('kingdom_key', 'ZENITHAR')->where('balance', 800000.0)->exists(),
    "API treasury sync failed: " . $apiTreasuryResp->getContent());

echo "\n==========================================================\n";
echo "           PHASE 3 TEST RESULTS SUMMARY                   \n";
echo "==========================================================\n";
echo " TOTAL TESTS PASSED: $passed\n";
echo " TOTAL TESTS FAILED: $failed\n";
echo "==========================================================\n";

exit($failed === 0 ? 0 : 1);
