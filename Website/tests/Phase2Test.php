<?php

/**
 * Apexsions Phase 2 Automated Verification Test Suite
 * Validates:
 * [REPORTS]
 * 1. View reports list & metrics
 * 2. Search reports (by reported/reporter/reason)
 * 3. Filter reports (by status & priority)
 * 4. Open report detail & Player 360 linking
 * 5. Claim report (atomic transaction)
 * 6. Double claim collision (concurrency & race condition protection)
 * 7. Assign report to specific staff member
 * 8. Update report status & add staff note (with soft delete support)
 * 9. Resolve report
 * 10. Dismiss report
 * 
 * [MODERATION]
 * 11. View moderation center statistics & active punishments
 * 12. Invalid target validation
 * 13. Invalid / empty reason rejection
 * 14. Valid moderation action execution (warn, mute, kick, ban)
 * 15. Bridge success delivery creation with action_id
 * 16. Duplicate action_id idempotency rejection
 * 17. Pardon / unban / unmute flow
 * 
 * [AUDIT & INTEGRATION]
 * 18. Audit log for report claim
 * 19. Audit log for player ban & action ID correlation
 * 20. Audit log for pardon
 * 21. Player Management 360 Moderation Tab data hydration
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Azuriom\Plugin\ApexsionsBridge\Models\ReportNote;
use Azuriom\Plugin\ApexsionsBridge\Models\Punishment;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\ReportService;
use Azuriom\Plugin\ApexsionsBridge\Services\ModerationService;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ReportAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ModerationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

echo "==========================================================\n";
echo "   APEXSIONS PHASE 2 AUTOMATED VERIFICATION TEST SUITE   \n";
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
$staffA = User::firstOrCreate(['email' => 'staff_a@apexsions.my.id'], [
    'name' => 'StaffAlpha',
    'password' => bcrypt('Secret123!'),
]);
$staffB = User::firstOrCreate(['email' => 'staff_b@apexsions.my.id'], [
    'name' => 'StaffBeta',
    'password' => bcrypt('Secret123!'),
]);

$testPlayerUuid = '00000000-0000-0000-0000-000000000002';
$testReporterUuid = '00000000-0000-0000-0000-000000000001';

MinecraftAccount::updateOrCreate(['minecraft_uuid' => $testPlayerUuid], [
    'minecraft_username' => 'ToxicPlayer',
    'rank' => 'wanderer',
    'rank_display' => 'Wanderer',
    'level' => 12,
    'xp' => 5400,
    'balance_rupiah' => 1000.0,
    'created_at' => now(),
    'updated_at' => now(),
]);

MinecraftAccount::updateOrCreate(['minecraft_uuid' => $testReporterUuid], [
    'minecraft_username' => 'GoodCitizen',
    'rank' => 'archon',
    'rank_display' => 'Archon',
    'level' => 25,
    'xp' => 25000,
    'balance_rupiah' => 10000.0,
    'created_at' => now(),
    'updated_at' => now(),
]);

echo "\n--- Section 1: Reports Center Verification ---\n";

// 1. Create Reports for testing
$report1 = Report::create([
    'reporter_uuid' => $testReporterUuid,
    'reporter_name' => 'GoodCitizen',
    'reported_uuid' => $testPlayerUuid,
    'reported_name' => 'ToxicPlayer',
    'reason' => 'Severe toxicity and chat spamming in global channel',
    'description' => 'Was repeatedly insulting beginners near spawn coordinates.',
    'status' => 'OPEN',
    'priority' => 'HIGH',
    'server_origin' => 'Survival-Alpha',
    'location' => 'x: 120, y: 64, z: -350 (world)',
]);

$report2 = Report::create([
    'reporter_uuid' => $testReporterUuid,
    'reporter_name' => 'GoodCitizen',
    'reported_uuid' => $testPlayerUuid,
    'reported_name' => 'ToxicPlayer',
    'reason' => 'Suspected fly hack in PvP arena',
    'description' => 'Hovering over lava pit without elytra.',
    'status' => 'OPEN',
    'priority' => 'CRITICAL',
    'server_origin' => 'PvP-Arena',
]);

// 1. View Reports List & Metrics
$controller = app(ReportAdminController::class);
$request = Request::create('/admin/reports', 'GET');
$view = $controller->index($request);
$viewData = $view->getData();
assertTest("1. View reports index loads correctly with metrics", 
    isset($viewData['reports']) && isset($viewData['openCount']) && isset($viewData['claimedCount']), 
    "View missing reports or metrics data");

// 2. Search Reports
$searchRequest = Request::create('/admin/reports', 'GET', ['q' => 'fly hack']);
$searchView = $controller->index($searchRequest);
$searchResults = $searchView->getData()['reports'];
assertTest("2. Search reports by query ('fly hack') filters results", 
    $searchResults->count() >= 1 && $searchResults->contains('id', $report2->id), 
    "Report #{$report2->id} not found in search results");

// 3. Filter Reports by Status and Priority
$filterRequest = Request::create('/admin/reports', 'GET', ['status' => 'OPEN', 'priority' => 'CRITICAL']);
$filterView = $controller->index($filterRequest);
$filterResults = $filterView->getData()['reports'];
assertTest("3. Filter reports by priority ('CRITICAL') matches exact criteria", 
    $filterResults->every(fn($r) => $r->priority === 'CRITICAL'), 
    "Non-critical report present in critical filtered view");

// 4. Open Report Detail
$detailView = $controller->show($report1->id);
assertTest("4. Open report detail view resolves with relations", 
    isset($detailView->getData()['report']) && $detailView->getData()['report']->id === $report1->id, 
    "Report detail failed to load report model");

// 5. Claim Report
$claimResult = ReportService::claimReport($report1, $staffA);
assertTest("5. Staff A successfully claims open report (status => CLAIMED)", 
    $claimResult['success'] === true && $report1->fresh()->status === 'CLAIMED' && $report1->fresh()->assigned_staff_id === $staffA->id, 
    "Report status or assigned staff mismatch");

// 6. Double Claim Collision Prevention (Race Condition)
$claimResultB = ReportService::claimReport($report1->fresh(), $staffB);
assertTest("6. Staff B double-claim is rejected with collision protection", 
    $claimResultB['success'] === false && str_contains($claimResultB['message'], 'sudah diklaim'), 
    "Double claim did not fail as expected: " . json_encode($claimResultB));

// 7. Assign Report
ReportService::assignReport($report2, $staffB, $staffA);
assertTest("7. Assign report to specific staff member (Staff B)", 
    $report2->fresh()->assigned_staff_id === $staffB->id && $report2->fresh()->status === 'CLAIMED', 
    "Report 2 not assigned to Staff B");

// 8. Staff Notes & Soft Delete
$note = ReportService::addNote($report1, "Player confirmed breaking chat guidelines. Video evidence saved.", $staffA);
assertTest("8. Add internal staff note to report", 
    $note instanceof ReportNote && $report1->fresh()->notes->count() === 1, 
    "Failed to attach staff note");

$noteId = $note->id;
$note->delete(); // Soft delete
assertTest("8b. Staff notes support soft delete (preserves audit trail)", 
    ReportNote::withTrashed()->where('id', $noteId)->exists() && !ReportNote::where('id', $noteId)->exists(), 
    "Soft delete not properly configured on ReportNote model");

// 9. Resolve Report
ReportService::resolveReport($report1->fresh(), "Player warned and 1h mute issued", $staffA);
assertTest("9. Resolve report with resolution comment (status => RESOLVED)", 
    $report1->fresh()->status === 'RESOLVED' && $report1->fresh()->resolution === "Player warned and 1h mute issued", 
    "Report status was not updated to RESOLVED");

// 10. Dismiss Report
ReportService::dismissReport($report2->fresh(), "Insufficient evidence for fly hack", $staffB);
assertTest("10. Dismiss report with reason (status => DISMISSED)", 
    $report2->fresh()->status === 'DISMISSED' && $report2->fresh()->resolution === "Insufficient evidence for fly hack", 
    "Report status was not updated to DISMISSED");

echo "\n--- Section 2: Moderation Center & Service Verification ---\n";

// 11. View Moderation Center
$modController = app(ModerationAdminController::class);
$modRequest = Request::create('/admin/moderation', 'GET');
$modView = $modController->index($modRequest);
$modData = $modView->getData();
assertTest("11. Moderation Center view loads statistics and active punishments", 
    isset($modData['activePunishments']) && isset($modData['activeBans']) && isset($modData['totalWarns30d']), 
    "Moderation index view missing required data arrays");

// 12. Invalid Target Validation
$valRequestEmptyTarget = Request::create('/admin/moderation/actions', 'POST', [
    'player' => '',
    'type' => 'WARN',
    'reason' => 'Spamming',
]);
try {
    $modController->storeAction($valRequestEmptyTarget);
    assertTest("12. Moderation action rejects empty/invalid target player", false, "Allowed empty target");
} catch (\Illuminate\Validation\ValidationException $e) {
    assertTest("12. Moderation action rejects empty/invalid target player", isset($e->errors()['player']));
}

// 13. Invalid / Empty Reason Rejection
$valRequestEmptyReason = Request::create('/admin/moderation/actions', 'POST', [
    'player' => 'ToxicPlayer',
    'type' => 'WARN',
    'reason' => '',
]);
try {
    $modController->storeAction($valRequestEmptyReason);
    assertTest("13. Moderation action rejects empty reason", false, "Allowed empty reason");
} catch (\Illuminate\Validation\ValidationException $e) {
    assertTest("13. Moderation action rejects empty reason", isset($e->errors()['reason']));
}

// 14. Valid Actions: WARN, MUTE, BAN, KICK
$warnPunishment = ModerationService::warn($testPlayerUuid, 'ToxicPlayer', 'First verbal warning for disrespect', $staffA);
assertTest("14a. Execute WARN action successfully", 
    $warnPunishment && !empty($warnPunishment->action_id), 
    "Warn execution failed");

$mutePunishment = ModerationService::mute($testPlayerUuid, 'ToxicPlayer', 180, 'Spamming advertisement link', $staffA);
assertTest("14b. Execute MUTE action with duration (180 mins)", 
    $mutePunishment && $mutePunishment->duration_seconds === 10800, 
    "Mute execution failed or duration mismatch");

$kickPunishment = ModerationService::kick($testPlayerUuid, 'ToxicPlayer', 'AFK avoidance during combat', $staffA);
assertTest("14c. Execute KICK action successfully", 
    $kickPunishment && $kickPunishment->type === 'KICK' && $kickPunishment->status === 'ACTIVE', 
    "Kick execution failed");

$banPunishment = ModerationService::ban($testPlayerUuid, 'ToxicPlayer', 24, 'Severe game exploit and fly hack', $staffA);
assertTest("14d. Execute BAN action with duration (24 hours)", 
    $banPunishment && $banPunishment->type === 'BAN' && $banPunishment->status === 'ACTIVE', 
    "Ban execution failed or status not active");

// 15. Bridge Delivery Queueing & Action ID
$delivery = Delivery::where('action_id', $banPunishment->action_id)->first();
assertTest("15. Bridge delivery record created with matching action_id and reliable status PENDING", 
    $delivery !== null && $delivery->status === 'PENDING' && $delivery->action_id === $banPunishment->action_id, 
    "Delivery command missing or action_id decoupled");

// 16. Duplicate Action ID Idempotency Protection
try {
    Delivery::create([
        'action_id' => $banPunishment->action_id,
        'player_username' => 'ToxicPlayer',
        'command' => 'ban ToxicPlayer Duplicate test',
        'status' => 'PENDING',
    ]);
    assertTest("16. Idempotency prevents duplicate action_id via UNIQUE constraint", false, "Duplicate allowed");
} catch (\Illuminate\Database\QueryException $e) {
    assertTest("16. Idempotency prevents duplicate action_id via UNIQUE constraint", true);
}

// 17. Pardon / Unban flow
ModerationService::unban($testPlayerUuid, 'ToxicPlayer', 'Appeal approved by overseer team', $staffA);
assertTest("17. Unban / Pardon action deactivates active punishment", 
    $banPunishment->fresh()->status === 'PARDONED', 
    "Active ban was not updated to PARDONED");

echo "\n--- Section 3: Audit Log Lifecycle & Integration ---\n";

// 18. Check Audit Log for Report Claimed
$auditClaim = AuditLog::where('action', 'REPORT_CLAIMED')->where('target_id', (string)$report1->id)->first();
assertTest("18a. Audit Log recorded REPORT_CLAIMED with correct Actor and Target", 
    $auditClaim !== null && $auditClaim->actor_id === (string)$staffA->id && $auditClaim->source === 'WEB', 
    "REPORT_CLAIMED audit entry missing or attributes mismatch");

// 18b. Check Audit Log for Moderation Action
$auditBan = AuditLog::where('action', 'PLAYER_BANNED')->where('metadata->action_id', $banPunishment->action_id)->first();
assertTest("18b. Audit Log recorded PLAYER_BANNED with Action ID and details", 
    $auditBan !== null && $auditBan->target_name === 'ToxicPlayer' && $auditBan->actor_name === 'StaffAlpha', 
    "PLAYER_BANNED audit log entry mismatch");

// 19. Check Audit Log for Pardon
$auditPardon = AuditLog::where('action', 'PLAYER_UNBANNED')->where('target_name', 'ToxicPlayer')->first();
assertTest("19. Audit Log recorded PLAYER_UNBANNED upon unban", 
    $auditPardon !== null && $auditPardon->reason === 'Appeal approved by overseer team', 
    "PLAYER_UNBANNED audit log entry missing");

echo "\n--- Section 4: Player Management 360 Moderation Tab ---\n";

// 20. Hydration of Moderation data in PlayerAdminController
$playerController = app(PlayerAdminController::class);
$playerShowView = $playerController->show('ToxicPlayer');
$playerViewData = $playerShowView->getData();

assertTest("20a. Player 360 show view receives punishments collection", 
    isset($playerViewData['punishments']) && $playerViewData['punishments']->count() >= 3, 
    "Punishments collection empty or not passed to view");

assertTest("20b. Player 360 show view receives reportsAgainst collection", 
    isset($playerViewData['reportsAgainst']) && $playerViewData['reportsAgainst']->count() >= 2, 
    "Reports against collection empty or not passed to view");

$reporterShowView = $playerController->show('GoodCitizen');
$reporterViewData = $reporterShowView->getData();
assertTest("20c. Player 360 show view receives reportsCreated collection for reporter", 
    isset($reporterViewData['reportsCreated']) && $reporterViewData['reportsCreated']->count() >= 2, 
    "Reports created collection empty or not passed to view");

echo "\n==========================================================\n";
echo "           PHASE 2 TEST RESULTS SUMMARY                   \n";
echo "==========================================================\n";
echo " TOTAL TESTS PASSED: $passed\n";
echo " TOTAL TESTS FAILED: $failed\n";
echo "==========================================================\n";

exit($failed === 0 ? 0 : 1);
