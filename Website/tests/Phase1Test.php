<?php

/**
 * Apexsions Phase 1 Verification Test Suite
 * Validates:
 * 1. Unauthorized access
 * 2. Authorized access
 * 3. Audit log creation & credential sanitization
 * 4. Audit log failed action
 * 5. Player search
 * 6. Player not found
 * 7. Empty data
 * 8. Bridge lease lock & timeout
 * 9. Duplicate action ID & idempotency
 * 10. Failed bridge action & audit integration
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuditLogController;
use Azuriom\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use Illuminate\Support\Str;

echo "==========================================================\n";
echo "   APEXSIONS PHASE 1 AUTOMATED VERIFICATION TEST SUITE   \n";
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
// TEST 1: Unauthorized Access on API
// -------------------------------------------------------------
echo "\n--- Group 1: Security & Route Protection ---\n";
$requestNoKey = Request::create('/api/apexsions-bridge/deliveries/pending', 'GET');
$apiController = app(LinkVerificationController::class);
$responseNoKey = $apiController->getPendingDeliveries($requestNoKey);
assertTest("1. API rejects request without valid secret key (HTTP 401 Unauthorized)", 
    in_array($responseNoKey->getStatusCode(), [401, 403]), 
    "Status was: " . $responseNoKey->getStatusCode());

// -------------------------------------------------------------
// TEST 2: Authorized Access with Key
// -------------------------------------------------------------
$requestWithKey = Request::create('/api/apexsions-bridge/deliveries/pending', 'GET');
$requestWithKey->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$responseWithKey = $apiController->getPendingDeliveries($requestWithKey);
assertTest("2. API accepts request with valid X-Apexsions-Key", 
    $responseWithKey->getStatusCode() === 200, 
    "Status was: " . $responseWithKey->getStatusCode());

// -------------------------------------------------------------
// TEST 3: Audit Log Creation & Sensitive Data Redaction
// -------------------------------------------------------------
echo "\n--- Group 2: Unified Audit Log Integrity ---\n";
$auditService = app(AuditService::class);
$testActor = (object)['id' => 999, 'name' => 'SystemTester'];

$logEntry = $auditService->log([
    'actor_type'  => 'ADMIN',
    'actor_id'    => $testActor->id,
    'actor_name'  => $testActor->name,
    'action'      => 'CONFIG_ACTION',
    'target_type' => 'SYSTEM',
    'target_id'   => 'DATABASE',
    'target_name' => 'HikariPool',
    'old_value'   => ['password' => 'SuperSecret123!', 'pool_size' => 10],
    'new_value'   => ['password' => 'NewSecret456!', 'pool_size' => 20],
    'reason'      => 'Automated test of audit log creation',
    'source'      => 'WEB',
    'status'      => 'SUCCESS',
    'metadata'    => ['api_key' => 'live_key_xyz', 'node' => 'vps1']
]);

$freshLog = AuditLog::find($logEntry->id);
$oldValJson = json_encode($freshLog->old_value);
$newValJson = json_encode($freshLog->new_value);
$metaJson = json_encode($freshLog->metadata);

$isRedacted = !str_contains($oldValJson, 'SuperSecret123!') 
           && !str_contains($newValJson, 'NewSecret456!') 
           && !str_contains($metaJson, 'live_key_xyz')
           && str_contains($oldValJson, '[REDACTED]');

assertTest("3. Audit log persists and automatically redacts passwords/secrets/keys", 
    $freshLog && $isRedacted, 
    "Redaction failed: oldVal=$oldValJson");

// -------------------------------------------------------------
// TEST 4: Audit Log Failed Action Handling
// -------------------------------------------------------------
$pendingLog = $auditService->start(
    'SERVER_ACTION',
    'SYSTEM',
    'AuthMeBridge',
    'AuthMeBridge',
    'Testing failed action recording',
    null,
    [],
    'SYSTEM'
);
$auditService->failed($pendingLog, 'Authentication handshake failed with remote socket');
$failedLog = AuditLog::find($pendingLog->id);

assertTest("4. Audit log correctly marks failed actions with status FAILED and records error diagnostics", 
    $failedLog && $failedLog->status === 'FAILED' && isset($failedLog->metadata['error']), 
    "Status was: " . ($failedLog ? $failedLog->status : 'null'));

// -------------------------------------------------------------
// TEST 5: Player Search (UUID / Username / Rank)
// -------------------------------------------------------------
echo "\n--- Group 3: Player Management Foundation ---\n";
// Create test minecraft account record if none exists
$testUuid = (string) Str::uuid();
$testAccount = \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::updateOrCreate(
    ['minecraft_uuid' => $testUuid],
    [
        'minecraft_username' => 'TestPlayer_Phase1',
        'rank' => 'archon',
        'rank_display' => 'Archon',
        'level' => 45,
        'xp' => 12500,
        'balance_rupiah' => 500000.0,
        'balance_diamond' => 120.0,
        'kingdom' => 'SOLTERRA',
        'created_at' => now(),
        'updated_at' => now()
    ]
);

$playerController = app(PlayerAdminController::class);

$searchByNameReq = Request::create('/admin/players', 'GET', ['q' => 'TestPlayer_Phase1']);
$viewName = $playerController->index($searchByNameReq);
$nameMatches = $viewName->getData()['players']->contains('minecraft_username', 'TestPlayer_Phase1');

$searchByUuidReq = Request::create('/admin/players', 'GET', ['q' => $testUuid]);
$viewUuid = $playerController->index($searchByUuidReq);
$uuidMatches = $viewUuid->getData()['players']->contains('minecraft_uuid', $testUuid);

assertTest("5. Player search matches username and UUID correctly", 
    $nameMatches && $uuidMatches, 
    "Name match: " . ($nameMatches ? 'yes' : 'no') . ", UUID match: " . ($uuidMatches ? 'yes' : 'no'));

// -------------------------------------------------------------
// TEST 6: Player Not Found
// -------------------------------------------------------------
$randomUuid = (string) Str::uuid();
$notFoundHandled = false;
try {
    $playerController->show($randomUuid);
} catch (\Illuminate\Database\Eloquent\ModelNotFoundException | \Symfony\Component\HttpKernel\Exception\NotFoundHttpException $e) {
    $notFoundHandled = true;
}

assertTest("6. Player detail returns HTTP 404 / ModelNotFoundException for non-existent player identifier", 
    $notFoundHandled, 
    "Expected 404 exception was not thrown");

// -------------------------------------------------------------
// TEST 7: Empty Data & Filter Handling
// -------------------------------------------------------------
$emptyFilterReq = Request::create('/admin/players', 'GET', ['rank' => 'non_existent_rank_999']);
$viewEmpty = $playerController->index($emptyFilterReq);
$isEmpty = $viewEmpty->getData()['players']->isEmpty();

assertTest("7. Empty filter query gracefully produces empty collection without error", 
    $isEmpty, 
    "Collection was not empty");

// -------------------------------------------------------------
// TEST 8: Bridge Delivery Lease Locking (PENDING -> PROCESSING)
// -------------------------------------------------------------
echo "\n--- Group 4: Bridge Action Reliability & Idempotency ---\n";
$actionId1 = (string) Str::uuid();
$delivery1 = Delivery::create([
    'action_id'       => $actionId1,
    'player_uuid'     => $testUuid,
    'player_username' => 'TestPlayer_Phase1',
    'command'         => 'eco give TestPlayer_Phase1 1000',
    'status'          => 'PENDING',
    'attempts'        => 0
]);

// Poll 1: delivery should be fetched and locked
$pollReq1 = Request::create('/api/apexsions-bridge/deliveries/pending', 'GET');
$pollReq1->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$pollRes1 = $apiController->getPendingDeliveries($pollReq1);
$data1 = json_decode($pollRes1->getContent(), true);

$delivery1Fresh = Delivery::find($delivery1->id);
$isLocked = $delivery1Fresh->status === 'PROCESSING' && $delivery1Fresh->locked_at !== null;

// Poll 2 immediately: delivery1 is in PROCESSING lease, so it MUST NOT be returned again!
$pollRes2 = $apiController->getPendingDeliveries($pollReq1);
$data2 = json_decode($pollRes2->getContent(), true);
$returnedAgain = collect($data2['deliveries'])->contains('id', $delivery1->id);

assertTest("8. Delivery lease lock changes status to PROCESSING and prevents immediate re-polling", 
    $isLocked && !$returnedAgain, 
    "isLocked=" . ($isLocked ? 'yes' : 'no') . ", returnedAgain=" . ($returnedAgain ? 'yes' : 'no'));

// -------------------------------------------------------------
// TEST 9: Duplicate Action ID Handling & Idempotency
// -------------------------------------------------------------
$dupExceptionCaught = false;
try {
    Delivery::create([
        'action_id'       => $actionId1, // DUPLICATE!
        'player_uuid'     => $testUuid,
        'player_username' => 'TestPlayer_Phase1',
        'command'         => 'eco give TestPlayer_Phase1 1000',
        'status'          => 'PENDING'
    ]);
} catch (\Illuminate\Database\QueryException $e) {
    $dupExceptionCaught = true;
}

assertTest("9. Database schema rejects duplicate action_id ensuring transaction idempotency", 
    $dupExceptionCaught, 
    "Duplicate action_id was accepted into deliveries table!");

// -------------------------------------------------------------
// TEST 10: Failed Bridge Action & Audit Log State Integration
// -------------------------------------------------------------
$auditAction = $auditService->start(
    'ECONOMY_ACTION',
    'PLAYER',
    $testUuid,
    'TestPlayer_Phase1',
    'Test bridge status callback',
    null,
    [],
    'WEB'
);

// Report delivery status as FAILED with error message
$statusReportReq = Request::create('/api/apexsions-bridge/deliveries/' . $delivery1->id . '/status', 'POST', [
    'status'        => 'FAILED',
    'error_message' => 'Player wallet locked by concurrent in-game auction',
    'action_id'     => $auditAction->action_id
]);
$statusReportReq->headers->set('X-Apexsions-Key', 'apexsions_bridge_key_live_2026');
$statusReportRes = $apiController->updateDeliveryStatus($statusReportReq, $delivery1->id);

$deliveryFinal = Delivery::find($delivery1->id);
$auditFinal = AuditLog::find($auditAction->id);

$deliveryMarkedFailed = $deliveryFinal->status === 'FAILED' && str_contains($deliveryFinal->error_message, 'Player wallet locked');
$auditMarkedFailed = $auditFinal && $auditFinal->status === 'FAILED' && isset($auditFinal->metadata['error']);

assertTest("10. Failed delivery reports error and automatically transitions linked AuditLog to FAILED", 
    $deliveryMarkedFailed && $auditMarkedFailed, 
    "deliveryStatus=" . $deliveryFinal->status . ", auditStatus=" . ($auditFinal ? $auditFinal->status : 'null'));

// Clean up test records
$testAccount->delete();
$delivery1->delete();

echo "\n==========================================================\n";
echo " TEST SUMMARY: $passed PASSED, $failed FAILED\n";
echo "==========================================================\n\n";

exit($failed > 0 ? 1 : 0);
