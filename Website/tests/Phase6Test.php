<?php

/**
 * Apexsions Ecosystem — Automated Verification Test Suite
 * PHASE 6: INTELLIGENCE, INCIDENT & INVESTIGATION SYSTEM
 *
 * Usage: php tests/Phase6Test.php
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IncidentAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IntelligenceAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\IncidentNote;
use Azuriom\Plugin\ApexsionsBridge\Models\IntelligenceRule;
use Azuriom\Plugin\ApexsionsBridge\Services\AnomalyDetectionEngine;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService;
use Azuriom\Plugin\ApexsionsBridge\Services\IncidentService;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PHASE 6 AUTOMATED VERIFICATION TEST SUITE   \n";
echo "==========================================================\n\n";

$passed = 0;
$failed = 0;

function assertTest(string $description, bool $condition, string $failureMsg = ''): void {
    global $passed, $failed;
    if ($condition) {
        echo " [PASS] $description\n";
        $passed++;
    } else {
        echo " [FAIL] $description: $failureMsg\n";
        $failed++;
    }
}

// Setup Mock Admin User
$admin = User::firstOrCreate(
    ['email' => 'admin_phase6@apexsions.test'],
    [
        'name' => 'Phase6TestAdmin',
        'password' => bcrypt('secret123'),
        'role_id' => 1,
    ]
);
Auth::login($admin);

// Seed default explicit rules
AnomalyDetectionEngine::seedDefaultRules();

// ---------------------------------------------------------------------
// SECTION 1: UNIFIED EVENT INGESTION & NORMALIZATION
// ---------------------------------------------------------------------
echo "\n--- SECTION 1: UNIFIED EVENT INGESTION & NORMALIZATION ---\n";

$eventId1 = 'evt_test_' . Str::uuid();
$event1 = EventIntelligenceService::ingest([
    'event_id' => $eventId1,
    'event_type' => 'PLAYER_LOGIN',
    'source' => 'MINECRAFT',
    'entity_type' => 'PLAYER',
    'entity_id' => 'uuid-player-alpha',
    'actor_type' => 'PLAYER',
    'actor_id' => 'uuid-player-alpha',
    'actor_name' => 'PlayerAlpha',
    'severity' => 'INFO',
    'correlation_id' => 'corr-session-001',
    'action_id' => 'act-auth-100',
    'metadata' => ['ip' => '127.0.0.1', 'client' => 'Vanilla 1.20'],
    'occurred_at' => Carbon::now()->subMinutes(10),
]);

assertTest("Event berhasil di-ingest ke database ledger", $event1 instanceof ApexsionsEvent);
assertTest("Event ID tersimpan secara tepat", $event1->event_id === $eventId1);
assertTest("Event Type ternormalisasi ke uppercase", $event1->event_type === 'PLAYER_LOGIN');
assertTest("Source ternormalisasi ke MINECRAFT", $event1->source === 'MINECRAFT');
assertTest("Received_at tercatat oleh server secara authoritative", $event1->received_at instanceof Carbon);
assertTest("Metadata tersimpan dalam format array terstruktur", is_array($event1->metadata) && $event1->metadata['ip'] === '127.0.0.1');

// ---------------------------------------------------------------------
// SECTION 2: EVENT CORRELATION CONFIDENCE ENGINE
// ---------------------------------------------------------------------
echo "\n--- SECTION 2: EVENT CORRELATION CONFIDENCE ENGINE ---\n";

// Event with identical Action ID -> STRONG Correlation
$eventStrong = EventIntelligenceService::ingest([
    'event_id' => 'evt_test_strong_' . Str::uuid(),
    'event_type' => 'AUTHENTICATION_VERIFY',
    'source' => 'WEB',
    'entity_type' => 'PLAYER',
    'entity_id' => 'uuid-player-alpha',
    'action_id' => 'act-auth-100', // Matches $event1->action_id
    'severity' => 'INFO',
    'occurred_at' => Carbon::now()->subMinutes(9),
]);

// Event with same Player UUID within 24h -> MEDIUM Correlation
$eventMedium = EventIntelligenceService::ingest([
    'event_id' => 'evt_test_medium_' . Str::uuid(),
    'event_type' => 'CHAT_MESSAGE',
    'source' => 'MINECRAFT',
    'entity_type' => 'PLAYER',
    'entity_id' => 'uuid-player-alpha',
    'actor_id' => 'uuid-player-alpha',
    'actor_name' => 'PlayerAlpha',
    'severity' => 'INFO',
    'occurred_at' => Carbon::now()->subMinutes(8),
]);

// Event from a completely different entity within 5 minutes -> WEAK Correlation
$eventWeak = EventIntelligenceService::ingest([
    'event_id' => 'evt_test_weak_' . Str::uuid(),
    'event_type' => 'SERVER_PING',
    'source' => 'SYSTEM',
    'entity_type' => 'SERVER',
    'entity_id' => 'SRV-01',
    'severity' => 'LOW',
    'occurred_at' => Carbon::now()->subMinutes(11), // Within 1 minute of event1
]);

$correlations = EventIntelligenceService::correlate($event1);

$hasStrong = $correlations->contains(function ($item) {
    return $item['confidence'] === 'STRONG';
});
$hasMedium = $correlations->contains(function ($item) {
    return $item['confidence'] === 'MEDIUM';
});
$hasWeak = $correlations->contains(function ($item) {
    return $item['confidence'] === 'WEAK';
});

assertTest("Korelasi STRONG terdeteksi berdasarkan kesamaan Action ID", $hasStrong);
assertTest("Korelasi MEDIUM terdeteksi berdasarkan kesamaan Player UUID dalam jendela 24 jam", $hasMedium);
assertTest("Korelasi WEAK terdeteksi berdasarkan kedekatan temporal (+/- 5m)", $hasWeak);

$weakItem = $correlations->firstWhere('confidence', 'WEAK');
assertTest("Korelasi WEAK memuat klausul peringatan non-konklusif", str_contains($weakItem['reason'] ?? '', 'Tidak boleh dijadikan bukti konklusif'));

// ---------------------------------------------------------------------
// SECTION 3: RULE-BASED ANOMALY DETECTION & COOLDOWN
// ---------------------------------------------------------------------
echo "\n--- SECTION 3: RULE-BASED ANOMALY DETECTION & COOLDOWN ---\n";

Cache::flush(); // Clear previous cooldowns

$testEntityId = 'tx-extreme-' . Str::random(8);

// 1. Economy Rule: Transfer Rupiah Ekstrem (> 50.000.000 Rp)
$ecoExtremeEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_eco_extreme_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'source' => 'MINECRAFT',
    'entity_type' => 'TRANSACTION',
    'entity_id' => $testEntityId,
    'actor_type' => 'PLAYER',
    'actor_id' => 'uuid-whale-01',
    'actor_name' => 'WhaleTrader',
    'target_type' => 'PLAYER',
    'target_id' => 'uuid-smurf-01',
    'target_name' => 'SmurfAccount',
    'severity' => 'HIGH',
    'metadata' => [
        'amount' => 75000000,
        'currency' => 'rupiah',
        'type' => 'TRANSFER',
    ],
    'occurred_at' => Carbon::now(),
]);

$incidentEco = ApexsionsIncident::where('root_entity_id', $testEntityId)->first();

assertTest("Rule RULE_ECO_LARGE_TRANSFER memicu terbentuknya insiden otomatis", $incidentEco instanceof ApexsionsIncident);
assertTest("Tipe insiden adalah ECONOMY", $incidentEco->type === 'ECONOMY');
assertTest("Severity insiden awal adalah HIGH", $incidentEco->severity === 'HIGH');
assertTest("Status awal insiden adalah OPEN", $incidentEco->status === 'OPEN');
assertTest("Event terkait ditautkan dengan incident_id", $ecoExtremeEvent->fresh()->incident_id === $incidentEco->incident_id);

// 2. Incident Deduplication Test: Subsequent matching event within cooldown merges into same incident
$ecoSubsequentEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_eco_subsequent_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'source' => 'MINECRAFT',
    'entity_type' => 'TRANSACTION',
    'entity_id' => $testEntityId,
    'actor_type' => 'PLAYER',
    'actor_id' => 'uuid-whale-01',
    'actor_name' => 'WhaleTrader',
    'severity' => 'CRITICAL',
    'metadata' => [
        'amount' => 90000000,
        'currency' => 'rupiah',
    ],
    'occurred_at' => Carbon::now(),
]);

$incidentRefreshed = $incidentEco->fresh();
$totalIncidentsWithThisEntity = ApexsionsIncident::where('root_entity_id', $testEntityId)->count();

assertTest("Deduplikasi insiden mencegah pembuatan insiden ganda untuk entitas yang sama", $totalIncidentsWithThisEntity === 1);
assertTest("Occurrence count bertambah menjadi 2x", $incidentRefreshed->occurrence_count === 2);
assertTest("Severity dieskalasi ke CRITICAL saat event berbobot lebih tinggi terdeteksi", $incidentRefreshed->severity === 'CRITICAL');

// 3. Server Rule: Sustained Low TPS (< 15.0)
$testServerId = 'SRV-' . Str::random(6);
$lowTpsEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_srv_tps_' . Str::uuid(),
    'event_type' => 'SERVER_ALERT',
    'source' => 'SYSTEM',
    'entity_type' => 'SERVER',
    'entity_id' => $testServerId,
    'severity' => 'CRITICAL',
    'metadata' => [
        'alert_type' => 'LOW_TPS',
        'tps' => 11.4,
    ],
    'occurred_at' => Carbon::now(),
]);

$incidentTps = ApexsionsIncident::where('root_entity_id', $testServerId)->where('type', 'SERVER')->first();
assertTest("Rule RULE_SRV_LOW_TPS berhasil memicu insiden SERVER", $incidentTps instanceof ApexsionsIncident);
assertTest("Severity insiden server bernilai CRITICAL", $incidentTps->severity === 'CRITICAL');

// 4. Plugin Rule: Degraded status
$testPluginId = 'ApexsionsPlg' . Str::random(4);
$plgDegradedEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_plg_deg_' . Str::uuid(),
    'event_type' => 'PLUGIN_HEALTH_SHIFT',
    'source' => 'PLUGIN',
    'entity_type' => 'PLUGIN',
    'entity_id' => $testPluginId,
    'severity' => 'CRITICAL',
    'metadata' => [
        'plugin_id' => $testPluginId,
        'health_status' => 'ERROR',
    ],
    'occurred_at' => Carbon::now(),
]);

$incidentPlugin = ApexsionsIncident::where('root_entity_id', $testPluginId)->where('type', 'PLUGIN')->first();
assertTest("Rule RULE_PLG_DEGRADED berhasil mendeteksi pergeseran status plugin ke ERROR", $incidentPlugin instanceof ApexsionsIncident);

// ---------------------------------------------------------------------
// SECTION 4: INCIDENT MANAGEMENT & INVESTIGATION WORKFLOW
// ---------------------------------------------------------------------
echo "\n--- SECTION 4: INCIDENT MANAGEMENT & INVESTIGATION WORKFLOW ---\n";

// 1. Staff Assignment
$assignedIncident = IncidentService::assignStaff($incidentEco, 'StaffInvestigator', 'Ditugaskan untuk audit mutasi saldo');
assertTest("Staff berhasil ditugaskan ke insiden", $assignedIncident->assigned_to === 'StaffInvestigator');
assertTest("Timestamp assigned_at terisi otomatis", $assignedIncident->assigned_at instanceof Carbon);

$assignAudit = AuditLog::where('action', 'INCIDENT_ASSIGN')->where('target_id', $incidentEco->incident_id)->first();
assertTest("Aksi penugasan staf tercatat ke dalam Unified Audit Log", $assignAudit instanceof AuditLog);

// 2. Add Internal Investigation Notes
$note = IncidentService::addNote(
    $incidentEco,
    "Konfirmasi transfer dilakukan karena transaksi pelelangan item legendaris sah di in-game.",
    (string) $admin->id,
    $admin->name,
    $ecoExtremeEvent->event_id
);

assertTest("Catatan investigasi berhasil dibuat", $note instanceof IncidentNote);
assertTest("Penulis catatan investigasi tercatat secara authoritatif", $note->staff_name === $admin->name);
assertTest("Catatan tertaut dengan event ID relevan", $note->related_event_id === $ecoExtremeEvent->event_id);

// 3. Status Transitions: OPEN -> INVESTIGATING -> RESOLVED
$investigating = IncidentService::updateStatus($incidentEco, 'INVESTIGATING', 'Sedang memvalidasi inventory penerima');
assertTest("Transisi status ke INVESTIGATING berhasil", $investigating->status === 'INVESTIGATING');

$resolved = IncidentService::updateStatus($incidentEco, 'RESOLVED', 'Semua saldo dan item telah diverifikasi sah');
assertTest("Transisi status ke RESOLVED berhasil", $resolved->status === 'RESOLVED');
assertTest("Timestamp resolved_at terisi otomatis saat status RESOLVED", $resolved->resolved_at instanceof Carbon);

// 4. Investigation Timeline Compilation
$timeline = IncidentService::getTimeline($incidentEco);
assertTest("Timeline kronologi investigasi terpadu berhasil dikompilasi", $timeline->isNotEmpty());

$hasEventInTimeline = $timeline->contains(fn($i) => $i['type'] === 'EVENT');
$hasNoteInTimeline = $timeline->contains(fn($i) => $i['type'] === 'NOTE');
$hasAuditInTimeline = $timeline->contains(fn($i) => $i['type'] === 'AUDIT');

assertTest("Timeline memuat entri tipe EVENT", $hasEventInTimeline);
assertTest("Timeline memuat entri tipe NOTE", $hasNoteInTimeline);
assertTest("Timeline memuat entri tipe AUDIT", $hasAuditInTimeline);

// ---------------------------------------------------------------------
// SECTION 5: API ENDPOINTS & SECURITY (LINKVERIFICATIONCONTROLLER)
// ---------------------------------------------------------------------
echo "\n--- SECTION 5: API ENDPOINTS & SECURITY ---\n";

$apiController = app(LinkVerificationController::class);

// 1. Unauthorized API call without secret key
$unauthRequest = Request::create('/api/apexsions-bridge/events/sync', 'POST', [
    'event_type' => 'TEST_EVENT',
    'entity_id' => 'test-id',
]);
$unauthResponse = $apiController->syncEvent($unauthRequest);
assertTest("Request API event sync tanpa API key ditolak (HTTP 401)", $unauthResponse->getStatusCode() === 401);

// 2. Authorized API call
$authRequest = Request::create('/api/apexsions-bridge/events/sync', 'POST', [
    'api_key' => 'apexsions_bridge_key_live_2026',
    'event_id' => 'evt_api_auth_' . Str::uuid(),
    'event_type' => 'AUCTION_CREATED',
    'source' => 'MINECRAFT',
    'entity_type' => 'AUCTION',
    'entity_id' => 'auc-api-101',
    'actor_type' => 'PLAYER',
    'actor_id' => 'uuid-seller-01',
    'actor_name' => 'SellerPro',
    'severity' => 'INFO',
    'metadata' => ['item' => 'NETHERITE_SWORD', 'price' => 2500000],
]);
$authResponse = $apiController->syncEvent($authRequest);
$authData = $authResponse->getData(true);
assertTest("Request API event sync dengan kunci valid diterima (HTTP 200)", $authResponse->getStatusCode() === 200 && ($authData['status'] ?? '') === 'success');

// 3. Security: Plugin Identity Spoofing Check
$fakePluginRequest = Request::create('/api/apexsions-bridge/events/sync', 'POST', [
    'api_key' => 'apexsions_bridge_key_live_2026',
    'event_type' => 'PLUGIN_CUSTOM_EVENT',
    'source' => 'PLUGIN',
    'entity_type' => 'PLUGIN',
    'entity_id' => 'HackerPlugin',
    'metadata' => ['plugin_id' => 'MaliciousPluginUnregistered'],
]);
$fakePluginResponse = $apiController->syncEvent($fakePluginRequest);
assertTest("Plugin tanpa identitas resmi di Plugin Registry ditolak (HTTP 403)", $fakePluginResponse->getStatusCode() === 403);

// 4. Duplicate Event Idempotence
$dupEventId = 'evt_dup_' . Str::uuid();
$reqFirst = Request::create('/api/apexsions-bridge/events/sync', 'POST', [
    'api_key' => 'apexsions_bridge_key_live_2026',
    'event_id' => $dupEventId,
    'event_type' => 'COMMAND_EXECUTE',
    'entity_id' => 'cmd-01',
]);
$apiController->syncEvent($reqFirst);

$reqSecond = Request::create('/api/apexsions-bridge/events/sync', 'POST', [
    'api_key' => 'apexsions_bridge_key_live_2026',
    'event_id' => $dupEventId,
    'event_type' => 'COMMAND_EXECUTE',
    'entity_id' => 'cmd-01',
]);
$resSecond = $apiController->syncEvent($reqSecond);
$dataSecond = $resSecond->getData(true);
assertTest("Duplicate event replay ditangani secara idempoten tanpa duplikasi", ($dataSecond['status'] ?? '') === 'duplicate');

// ---------------------------------------------------------------------
// SECTION 6: WEB ADMIN CONTROLLERS & VIEWS
// ---------------------------------------------------------------------
echo "\n--- SECTION 6: WEB ADMIN CONTROLLERS & VIEWS ---\n";

// 1. IntelligenceAdminController@index
$intelController = app(IntelligenceAdminController::class);
$viewIntel = $intelController->index(Request::create('/admin/intelligence', 'GET'));
assertTest("Controller IntelligenceAdminController@index mengembalikan view valid", $viewIntel instanceof \Illuminate\View\View);
assertTest("View intelligence index memuat data metrik insiden aktif", isset($viewIntel->getData()['openIncidentsCount']));
assertTest("View intelligence index memuat rule engine terdaftar", isset($viewIntel->getData()['rules']));

// 2. IncidentAdminController@index
$incidentController = app(IncidentAdminController::class);
$viewIncidentList = $incidentController->index(Request::create('/admin/incidents', 'GET'));
assertTest("Controller IncidentAdminController@index mengembalikan view valid", $viewIncidentList instanceof \Illuminate\View\View);
assertTest("View incidents index memuat koleksi insiden terpaginasi", isset($viewIncidentList->getData()['incidents']));

// 3. IncidentAdminController@show
$viewIncidentShow = $incidentController->show(Request::create('/admin/incidents/' . $incidentEco->incident_id, 'GET'), $incidentEco->incident_id);
assertTest("Controller IncidentAdminController@show mengembalikan lembar investigasi", $viewIncidentShow instanceof \Illuminate\View\View);
assertTest("View incident show memuat timeline investigasi terpadu", isset($viewIncidentShow->getData()['timeline']));
assertTest("View incident show memuat analisis korelasi kejadian", isset($viewIncidentShow->getData()['correlatedEvents']));

// ---------------------------------------------------------------------
// SUMMARY
// ---------------------------------------------------------------------
echo "\n==========================================================\n";
echo "   PHASE 6 TEST RESULT: $passed PASSED, $failed FAILED   \n";
echo "==========================================================\n\n";

if ($failed > 0) {
    exit(1);
}
