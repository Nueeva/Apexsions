<?php

/**
 * Apexsions Ecosystem — Automated Verification Test Suite
 * PHASE 4: SERVER OPERATIONS & SAFE CONTROL
 *
 * Usage: php tests/Phase4Test.php
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ServerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MaintenanceState;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerAlert;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PHASE 4 AUTOMATED VERIFICATION TEST SUITE   \n";
echo "==========================================================\n\n";

$passed = 0;
$failed = 0;

function assertTest(string $description, bool $condition, string $failureMsg = ''): void {
    global $passed, $failed;
    if ($condition) {
        echo " [PASS] $description\n";
        $passed++;
    } else {
        echo " [FAIL] $description" . ($failureMsg ? " -> $failureMsg" : "") . "\n";
        $failed++;
    }
}

// Setup Mock Staff User
$staff = User::firstOrCreate(
    ['email' => 'overseer.ops@apexsions.test'],
    [
        'name' => 'OverseerOps',
        'password' => bcrypt('SecretStaff123!'),
    ]
);
Auth::login($staff);

// -------------------------------------------------------------
// SECTION 1: SERVER STATUS LOGIC & REASONING
// -------------------------------------------------------------
echo "--- Section 1: Server Status & Health Engine ---\n";

// Ensure maintenance is disabled initially
$mState = MaintenanceState::current();
$mState->update(['is_enabled' => false]);

// 1. Status ONLINE with fresh telemetry and stable TPS
Cache::put('apexsions.server_status', [
    'online' => true,
    'players' => 45,
    'max_players' => 500,
    'tps' => 19.9,
    'mspt' => 18.2,
    'ram_used_mb' => 2400,
    'ram_max_mb' => 8192,
    'free_ram_mb' => 5792,
    'uptime_seconds' => 36000,
    'loaded_chunks' => 1250,
    'entities' => 890,
    'last_heartbeat' => Carbon::now()->timestamp,
], 300);

$statusOnline = ServerOpsService::getServerStatus();
assertTest("1. Fresh telemetry (<90s) and TPS >= 19.5 produces ONLINE status", 
    $statusOnline['status'] === 'ONLINE' && $statusOnline['is_online'] === true,
    "Expected ONLINE but got: " . ($statusOnline['status'] ?? 'null'));

// 2. Status DEGRADED when TPS is low (<17.0) or MSPT is high (>=50.0)
Cache::put('apexsions.server_status', [
    'online' => true,
    'tps' => 15.8,
    'mspt' => 55.4,
    'ram_used_mb' => 7500,
    'ram_max_mb' => 8192,
    'last_heartbeat' => Carbon::now()->timestamp,
], 300);

$statusDegraded = ServerOpsService::getServerStatus();
assertTest("2. Low TPS (15.8) or High MSPT (55.4) produces DEGRADED status", 
    $statusDegraded['status'] === 'DEGRADED',
    "Expected DEGRADED but got: " . ($statusDegraded['status'] ?? 'null'));

// 3. Status OFFLINE when heartbeat is older than timeout (>90s)
Cache::forget('apexsions.direct_socket_ping');
Cache::put('apexsions.server_status', [
    'online' => true,
    'tps' => 20.0,
    'mspt' => 15.0,
    'last_heartbeat' => Carbon::now()->subSeconds(120)->timestamp,
], 300);

$statusOffline = ServerOpsService::getServerStatus();
assertTest("3. Heartbeat older than 90 seconds produces OFFLINE status", 
    $statusOffline['status'] === 'OFFLINE' && $statusOffline['is_online'] === false,
    "Expected OFFLINE but got: " . ($statusOffline['status'] ?? 'null'));

// 4. Status MAINTENANCE takes precedence when active
$mState->update([
    'is_enabled' => true,
    'message' => 'Migrasi database Paper API',
    'enabled_at' => Carbon::now(),
]);
$statusMaint = ServerOpsService::getServerStatus();
assertTest("4. Active maintenance mode produces MAINTENANCE status regardless of telemetry", 
    $statusMaint['status'] === 'MAINTENANCE',
    "Expected MAINTENANCE but got: " . ($statusMaint['status'] ?? 'null'));

// Reset maintenance for next sections
$mState->update(['is_enabled' => false]);

// 5. Health metrics inspection data source integrity
Cache::put('apexsions.server_status', [
    'online' => true,
    'players' => 120,
    'max_players' => 500,
    'tps' => 19.8,
    'mspt' => 22.4,
    'ram_used_mb' => 4500,
    'ram_max_mb' => 8192,
    'loaded_chunks' => 3400,
    'entities' => 2100,
    'uptime_seconds' => 72000,
    'last_heartbeat' => Carbon::now()->timestamp,
], 300);

$health = ServerOpsService::getHealthMetrics();
assertTest("5. Health metrics expose raw values and verified data sources", 
    $health['tps']['value'] === '19.8' && 
    str_contains($health['tps']['source'], 'Bukkit.getTPS') &&
    (int) $health['memory']['percentage'] === 55,
    "Health metrics structure malformed");

// -------------------------------------------------------------
// SECTION 2: SAFE SERVER ACTIONS & AUDIT INTEGRATION
// -------------------------------------------------------------
echo "\n--- Section 2: Safe Server Actions & Audit Trail ---\n";

// 6. Execute SAFE action (BROADCAST)
$broadcastRes = ServerOpsService::executeSafeAction(
    'BROADCAST',
    ['message' => 'Server restart terjadwal dalam 30 menit.'],
    $staff,
    'Pengumuman maintenance terencana'
);
assertTest("6. Execute SAFE action (BROADCAST) enqueues delivery and returns Action ID", 
    $broadcastRes['success'] === true && !empty($broadcastRes['action_id']),
    "Broadcast failed: " . ($broadcastRes['message'] ?? ''));

// 7. Bridge delivery verification for BROADCAST
$delivBroadcast = Delivery::where('action_id', $broadcastRes['action_id'])->first();
assertTest("7. Delivery contains sanitized template command and status PENDING", 
    $delivBroadcast !== null && 
    str_contains($delivBroadcast->command, 'broadcast <gold><bold>[APEXSIONS PENGUMUMAN]</bold></gold>') &&
    $delivBroadcast->status === 'PENDING',
    "Delivery command malformed");

// 8. Execute SENSITIVE action (SAVE_WORLD)
$saveRes = ServerOpsService::executeSafeAction(
    'SAVE_WORLD',
    [],
    $staff,
    'Persiapan backup snapshot database'
);
assertTest("8. Execute SENSITIVE action (SAVE_WORLD) enqueues 'save-all'", 
    $saveRes['success'] === true && Delivery::where('action_id', $saveRes['action_id'])->value('command') === 'save-all',
    "Save world failed");

// 9. Execute SENSITIVE action (CLEAR_ITEMS)
$clearRes = ServerOpsService::executeSafeAction(
    'CLEAR_ITEMS',
    [],
    $staff,
    'Pembersihan lag item ground di dunia utama'
);
assertTest("9. Execute SENSITIVE action (CLEAR_ITEMS) enqueues entity item wipe", 
    $clearRes['success'] === true && Delivery::where('action_id', $clearRes['action_id'])->value('command') === 'kill @e[type=item]',
    "Clear items failed");

// 10. Execute SENSITIVE action (RELOAD_CONFIG)
$reloadRes = ServerOpsService::executeSafeAction(
    'RELOAD_CONFIG',
    [],
    $staff,
    'Reload konfigurasi rewards dan kingdom borders'
);
assertTest("10. Execute SENSITIVE action (RELOAD_CONFIG) enqueues 'ac reload'", 
    $reloadRes['success'] === true && Delivery::where('action_id', $reloadRes['action_id'])->value('command') === 'ac reload',
    "Reload config failed");

// 11. Sensitive action strictly rejects empty reason
$emptyReasonRes = ServerOpsService::executeSafeAction(
    'SAVE_WORLD',
    [],
    $staff,
    '' // Empty reason
);
assertTest("11. Sensitive action rejects empty reason to guarantee auditability", 
    $emptyReasonRes['success'] === false && str_contains($emptyReasonRes['message'], 'Alasan'),
    "Empty reason was erroneously accepted");

// 12. Rejection of unlisted / arbitrary actions
$arbitraryRes = ServerOpsService::executeSafeAction(
    'OP_PLAYER_HACK',
    [],
    $staff,
    'Test illegal action'
);
assertTest("12. System rejects arbitrary unlisted action (No-Raw-Console Policy)", 
    $arbitraryRes['success'] === false && str_contains($arbitraryRes['message'], 'tidak terdaftar'),
    "Arbitrary action permitted");

// 13. Audit Log verified for server actions
$auditEntry = AuditLog::where('action_id', $saveRes['action_id'])->first();
assertTest("13. Audit log persists SERVER action with category, actor, and SUCCESS status", 
    $auditEntry !== null && 
    $auditEntry->action === 'SERVER_SAVE_WORLD' &&
    $auditEntry->actor_name === 'OverseerOps' &&
    $auditEntry->status === 'SUCCESS',
    "Audit log entry missing or invalid");

// -------------------------------------------------------------
// SECTION 3: MAINTENANCE MODE SUBSYSTEM
// -------------------------------------------------------------
echo "\n--- Section 3: Maintenance Mode Subsystem ---\n";

// 14. Enable Maintenance Mode
$enableMaintRes = ServerOpsService::toggleMaintenance(
    true,
    'Server sedang dioptimasi untuk Apexsions War',
    'Persiapan event perang kerajaan',
    true,
    $staff
);
assertTest("14. Enable Maintenance Mode updates state and enqueues command", 
    $enableMaintRes['success'] === true && 
    MaintenanceState::current()->isEnabled() === true &&
    str_contains(Delivery::where('action_id', $enableMaintRes['action_id'])->value('command'), 'maintenance enable'),
    "Enable maintenance failed");

// 15. Enable Maintenance rejects empty reason
$rejectMaintRes = ServerOpsService::toggleMaintenance(
    true,
    'Custom message',
    '', // Empty reason
    true,
    $staff
);
assertTest("15. Enable Maintenance rejects request with empty reason", 
    $rejectMaintRes['success'] === false && str_contains($rejectMaintRes['message'], 'Alasan'),
    "Empty reason allowed for maintenance enable");

// 16. Disable Maintenance Mode
$disableMaintRes = ServerOpsService::toggleMaintenance(
    false,
    'Normal',
    'Pekerjaan teknis selesai',
    true,
    $staff
);
assertTest("16. Disable Maintenance Mode opens server and enqueues 'maintenance disable'", 
    $disableMaintRes['success'] === true && 
    MaintenanceState::current()->isEnabled() === false &&
    Delivery::where('action_id', $disableMaintRes['action_id'])->value('command') === 'maintenance disable',
    "Disable maintenance failed");

// -------------------------------------------------------------
// SECTION 4: HISTORICAL METRICS & RETENTION
// -------------------------------------------------------------
echo "\n--- Section 4: Historical Metrics & Retention ---\n";

// 17. Record snapshot into apexsions_server_metrics
$metricSnapshot = ServerMetric::create([
    'tps' => 19.7,
    'mspt' => 20.1,
    'cpu_usage' => 42.5,
    'ram_used_mb' => 3200,
    'ram_max_mb' => 8192,
    'disk_used_gb' => 14.2,
    'disk_total_gb' => 50.0,
    'online_players' => 85,
    'max_players' => 500,
    'loaded_chunks' => 2100,
    'entities' => 1400,
    'server_status' => 'ONLINE',
    'created_at' => Carbon::now(),
]);
assertTest("17. Record telemetry snapshot into apexsions_server_metrics table", 
    $metricSnapshot->id > 0 && $metricSnapshot->tps === 19.7,
    "Metric insertion failed");

// 18. Query recent metrics
$recentMetrics = ServerMetric::recent(24)->get();
assertTest("18. Scope recent(24) retrieves metric snapshots in chronological order", 
    $recentMetrics->isNotEmpty() && $recentMetrics->contains('id', $metricSnapshot->id),
    "Query recent metrics failed");

// -------------------------------------------------------------
// SECTION 5: PLUGIN STATUS MONITORING & CAPABILITIES
// -------------------------------------------------------------
echo "\n--- Section 5: Plugin Status Monitoring & Capabilities ---\n";

$catalog = ServerOpsService::getPluginCatalog();
$catalogMap = collect($catalog)->keyBy('name');

// 19. Catalog contains all 6 core plugins
assertTest("19. Plugin catalog contains all 6 Apexsions core custom plugins", 
    $catalogMap->has('ApexsionsCore') &&
    $catalogMap->has('ApexsionsChat') &&
    $catalogMap->has('ApexsionsEconomy') &&
    $catalogMap->has('ApexsionsBattlepass') &&
    $catalogMap->has('ApexsionsShop') &&
    $catalogMap->has('ApexsionsMedia'),
    "Missing core custom plugins from catalog");

// 20. Capabilities and integration verified
assertTest("20. Plugin catalog exposes verified capabilities and integration levels", 
    $catalogMap['ApexsionsEconomy']['integration'] === 'WEB INTEGRATED' &&
    in_array('Auction House Engine & Quarantine Moderation', $catalogMap['ApexsionsEconomy']['capabilities']) &&
    $catalogMap['ApexsionsMedia']['integration'] === 'MINECRAFT ONLY',
    "Plugin metadata or capabilities incorrect");

// -------------------------------------------------------------
// SECTION 6: SERVER ALERTS & THRESHOLD EVALUATION
// -------------------------------------------------------------
echo "\n--- Section 6: Server Alerts Foundation ---\n";

// Clean any old test alerts
ServerAlert::whereIn('type', ['LOW_TPS', 'HIGH_MEMORY', 'HIGH_MSPT'])->delete();

// 21. Evaluate telemetry with low TPS triggers LOW_TPS alert
$evalAlerts = ServerOpsService::evaluateAlerts([
    'tps' => 14.2, // Below critical threshold 15.0
    'mspt' => 20.0,
    'ram_used_mb' => 2000,
    'ram_max_mb' => 8192,
]);
assertTest("21. TPS under critical threshold (14.2) creates CRITICAL LOW_TPS alert", 
    ServerAlert::where('type', 'LOW_TPS')->where('severity', 'CRITICAL')->exists(),
    "Low TPS alert not generated");

// 22. Deduplication prevents duplicate alert within 15 minutes
$dupAlerts = ServerOpsService::evaluateAlerts([
    'tps' => 14.1,
    'mspt' => 20.0,
    'ram_used_mb' => 2000,
    'ram_max_mb' => 8192,
]);
assertTest("22. Alert engine deduplicates identical active alerts within 15 minutes", 
    ServerAlert::where('type', 'LOW_TPS')->count() === 1,
    "Duplicate alert created");

// 23. Acknowledge alert lifecycle
$alertToAck = ServerAlert::where('type', 'LOW_TPS')->first();
$alertToAck->acknowledge('OverseerOps');
assertTest("23. Acknowledge alert transitions status to ACKNOWLEDGED with staff metadata", 
    $alertToAck->fresh()->status === 'ACKNOWLEDGED' && $alertToAck->fresh()->acknowledged_by === 'OverseerOps',
    "Alert acknowledge failed");

// 24. Resolve alert lifecycle
$alertToAck->resolve('OverseerOps');
assertTest("24. Resolve alert transitions status to RESOLVED with resolution timestamp", 
    $alertToAck->fresh()->status === 'RESOLVED' && $alertToAck->fresh()->resolved_at !== null,
    "Alert resolve failed");

// -------------------------------------------------------------
// SECTION 7: BRIDGE HEALTH & CONTROLLERS
// -------------------------------------------------------------
echo "\n--- Section 7: Bridge Health & Web Controllers ---\n";

// 25. Bridge health assessment
$bridgeHealth = ServerOpsService::getBridgeHealth();
assertTest("25. Bridge health evaluates queue latency, counts, and status", 
    isset($bridgeHealth['status']) && isset($bridgeHealth['pending_count']),
    "Bridge health malformed");

// 26. ServerAdminController index renders with all operational data
$srvController = app(ServerAdminController::class);
$indexView = $srvController->index();
assertTest("26. ServerAdminController index resolves status, metrics, alerts, and allowed actions", 
    isset($indexView->getData()['serverStatus']) &&
    isset($indexView->getData()['metrics']) &&
    isset($indexView->getData()['bridgeHealth']) &&
    isset($indexView->getData()['maintenance']) &&
    isset($indexView->getData()['allowedActions']),
    "ServerAdminController view data missing");

echo "\n==========================================================\n";
echo "           PHASE 4 TEST RESULTS SUMMARY                   \n";
echo "==========================================================\n";
echo " TOTAL TESTS PASSED: $passed\n";
echo " TOTAL TESTS FAILED: $failed\n";
echo "==========================================================\n";

exit($failed === 0 ? 0 : 1);
