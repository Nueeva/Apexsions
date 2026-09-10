<?php

require __DIR__ . '/../vendor/autoload.php';
$app = require __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\RankPurchase;
use Azuriom\Plugin\ApexsionsBridge\Models\RankRewardClaim;
use Azuriom\Plugin\ApexsionsBridge\Services\RankService;
use Azuriom\Plugin\ApexsionsBridge\Services\BattlepassDiscountService;
use Illuminate\Support\Facades\DB;

echo "=================================================================\n";
echo "       APEXSIONS COMPREHENSIVE SYSTEM VERIFICATION TEST          \n";
echo "=================================================================\n\n";

$testsPassed = 0;
$testsFailed = 0;

function assertTest(bool $condition, string $testName) {
    global $testsPassed, $testsFailed;
    if ($condition) {
        echo "  [PASS] {$testName}\n";
        $testsPassed++;
    } else {
        echo "  [FAIL] {$testName}\n";
        $testsFailed++;
    }
}

// -----------------------------------------------------------------------------
// 1. TEST BATTLEPASS DISCOUNT SERVICE & SERVER-SIDE CALCULATION
// -----------------------------------------------------------------------------
echo "[1] Testing Battlepass Discount Service (Server-Side Calculations)...\n";

$testAccountWanderer = new MinecraftAccount([
    'minecraft_username' => 'TestWanderer',
    'edition' => 'java',
    'rank' => 'wanderer',
    'rank_type' => 'permanent',
]);

$testAccountEmperorTrial = new MinecraftAccount([
    'minecraft_username' => 'TestEmperorTrial',
    'edition' => 'java',
    'rank' => 'emperor',
    'rank_type' => 'trial',
    'rank_expires_at' => now()->addDays(30),
]);

$testAccountEmperorPerm = new MinecraftAccount([
    'minecraft_username' => 'TestEmperorPerm',
    'edition' => 'java',
    'rank' => 'emperor',
    'rank_type' => 'permanent',
]);

$testAccountSionsPerm = new MinecraftAccount([
    'minecraft_username' => 'TestSionsPerm',
    'edition' => 'java',
    'rank' => 'sions',
    'rank_type' => 'permanent',
]);

// Test Wanderer on Sio Pass (Rp 45.000)
$resWanderer = BattlepassDiscountService::calculateDiscount($testAccountWanderer, 'Sio Pass', 45000);
assertTest(!$resWanderer['has_discount'], "Wanderer has no discount on Sio Pass");
assertTest($resWanderer['discounted_price'] == 45000, "Wanderer pays standard price Rp 45.000");
assertTest($resWanderer['whatsapp_message'] === "Min, aku mau beli Sio Pass yang harganya Rp.45.000", "Standard WhatsApp template for Wanderer");

// Test Emperor Trial on Sio Pass (Trial ranks do not get future BP discounts)
$resEmpTrial = BattlepassDiscountService::calculateDiscount($testAccountEmperorTrial, 'Sio Pass', 45000);
assertTest(!$resEmpTrial['has_discount'], "Emperor Trial does NOT get permanent future Battlepass discount");

// Test Emperor Permanent on Sio Pass (10% discount: Rp 45.000 -> Rp 40.500)
$resEmpPerm = BattlepassDiscountService::calculateDiscount($testAccountEmperorPerm, 'Sio Pass', 45000);
assertTest($resEmpPerm['has_discount'], "Emperor Permanent gets BattlePass discount");
assertTest($resEmpPerm['discount_percent'] === 10, "Emperor discount is 10%");
assertTest($resEmpPerm['discounted_price'] == 40500, "Emperor discounted price is Rp 40.500");
assertTest($resEmpPerm['savings'] == 4500, "Emperor savings is Rp 4.500");
assertTest($resEmpPerm['is_free_current_season'] === true, "Emperor gets free current season access");
assertTest($resEmpPerm['whatsapp_message'] === "Min, aku mau beli battlepass Sio Pass yang harganya Rp.40.500 karna aku udah beli rank Emperor jadi harganya segitu", "Emperor WhatsApp template format accurate");

// Test Emperor Permanent on Exsio Pass (Emperor discount is specifically for Sio Pass)
$resEmpExsio = BattlepassDiscountService::calculateDiscount($testAccountEmperorPerm, 'Exsio Pass', 85000);
assertTest(!$resEmpExsio['has_discount'], "Emperor discount does not apply to Exsio Pass");

// Test Sions Permanent on Sio Pass (15% discount: Rp 45.000 -> Rp 38.250)
$resSionsSio = BattlepassDiscountService::calculateDiscount($testAccountSionsPerm, 'Sio Pass', 45000);
assertTest($resSionsSio['has_discount'], "Sions Permanent gets discount on Sio Pass");
assertTest($resSionsSio['discount_percent'] === 15, "Sions discount is 15%");
assertTest($resSionsSio['discounted_price'] == 38250, "Sions discounted price for Sio Pass is Rp 38.250");
assertTest($resSionsSio['savings'] == 6750, "Sions savings on Sio Pass is Rp 6.750");
assertTest($resSionsSio['whatsapp_message'] === "Min, aku mau beli battlepass Sio Pass yang harganya Rp.38.250 karna aku udah beli rank Sions jadi harganya segitu", "Sions Sio Pass WhatsApp template format accurate");

// Test Sions Permanent on Exsio Pass (15% discount: Rp 85.000 -> Rp 72.250)
$resSionsExsio = BattlepassDiscountService::calculateDiscount($testAccountSionsPerm, 'Exsio Pass', 85000);
assertTest($resSionsExsio['has_discount'], "Sions Permanent gets discount on Exsio Pass");
assertTest($resSionsExsio['discount_percent'] === 15, "Sions discount is 15% on Exsio Pass");
assertTest($resSionsExsio['discounted_price'] == 72250, "Sions discounted price for Exsio Pass is Rp 72.250");
assertTest($resSionsExsio['savings'] == 12750, "Sions savings on Exsio Pass is Rp 12.750");
assertTest($resSionsExsio['whatsapp_message'] === "Min, aku mau beli battlepass Exsio Pass yang harganya Rp.72.250 karna aku udah beli rank Sions jadi harganya segitu", "Sions Exsio Pass WhatsApp template format accurate");

// Test Standard Product WhatsApp message
$resBooster = BattlepassDiscountService::calculateDiscount($testAccountSionsPerm, 'XP Booster 2x (3 Hari)', 15000);
assertTest(!$resBooster['has_discount'], "Booster has no Battlepass discount");
assertTest($resBooster['whatsapp_message'] === "Min, aku mau beli XP Booster 2x (3 Hari) yang harganya Rp.15.000", "Standard product WhatsApp format accurate");

// -----------------------------------------------------------------------------
// 2. TEST TRIAL EXPIRATION ENGINE
// -----------------------------------------------------------------------------
echo "\n[2] Testing Trial Expiration and Auto-Downgrade Engine...\n";

// Create a mock trial account that has expired
$testUuid = 'test-uuid-' . time();
$expiredAccount = MinecraftAccount::create([
    'minecraft_username' => 'ExpTester',
    'edition' => 'java',
    'minecraft_uuid' => $testUuid,
    'rank' => 'archon',
    'rank_type' => 'trial',
    'rank_expires_at' => now()->subDay(), // Expired yesterday
    'is_verified' => true,
]);

assertTest($expiredAccount->isTrialExpired(), "Account correctly recognized as expired trial");

// Run the expiration engine
$expiredCount = RankService::checkAndExpireTrials();
assertTest($expiredCount >= 1, "checkAndExpireTrials() successfully identified and processed expired trials");

$reloadedAccount = MinecraftAccount::where('minecraft_uuid', $testUuid)->first();
assertTest($reloadedAccount->rank === 'wanderer', "Expired trial was automatically downgraded to wanderer");
assertTest(strtoupper($reloadedAccount->rank_type) === 'PERMANENT', "Downgraded rank status set to permanent wanderer");

// -----------------------------------------------------------------------------
// 3. TEST ONE-TIME MONEY REWARD CLAIM & NON-DUPLICATION
// -----------------------------------------------------------------------------
echo "\n[3] Testing One-Time Money Reward Non-Duplication...\n";

// First claim of Archon reward (Rp 80.000)
$firstClaimResult = RankService::claimPermanentMoneyReward($reloadedAccount, 'archon', 80000, 101);
assertTest($firstClaimResult === true, "First permanent money reward claim successful");

$claimRecord = RankRewardClaim::where('minecraft_uuid', $testUuid)
    ->where('rank', 'archon')
    ->where('reward_type', 'MONEY_ONETIME')
    ->first();
assertTest($claimRecord !== null && $claimRecord->amount == 80000, "Claim record persisted with accurate amount");

// Attempt duplicate claim of Archon reward
$duplicateClaimResult = RankService::claimPermanentMoneyReward($reloadedAccount, 'archon', 80000, 102);
assertTest($duplicateClaimResult === false, "Duplicate permanent money reward claim prevented (Non-duplication enforced)");

// Upgrade to Sovereign: Player buys Sovereign (Rp 120.000)
// Must NOT inherit lower Ascendant (Rp 50.000) or Archon (Rp 80.000)
$sovereignClaimResult = RankService::claimPermanentMoneyReward($reloadedAccount, 'sovereign', 120000, 103);
assertTest($sovereignClaimResult === true, "Higher rank (Sovereign) money reward claim successful");

$ascendantDuplicateCheck = RankRewardClaim::where('minecraft_uuid', $testUuid)
    ->where('rank', 'ascendant')
    ->first();
assertTest($ascendantDuplicateCheck === null, "Lower rank (Ascendant) money reward was NOT accidentally given on Sovereign purchase");

// Clean up test data
if ($claimRecord) {
    $claimRecord->delete();
}
RankRewardClaim::where('minecraft_uuid', $testUuid)->delete();
$reloadedAccount->delete();

echo "\n=================================================================\n";
echo "  TEST SUMMARY: {$testsPassed} PASSED, {$testsFailed} FAILED\n";
echo "=================================================================\n";

if ($testsFailed > 0) {
    exit(1);
}
exit(0);
