<?php

/**
 * Apexsions Ecosystem — Automated Verification Test Suite
 * PHASE 5: CUSTOM PLUGIN CONTROL & CAPABILITY SYSTEM
 *
 * Usage: php tests/Phase5Test.php
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PluginAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginCapability;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginHealthRecord;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginActionGateway;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PHASE 5 AUTOMATED VERIFICATION TEST SUITE   \n";
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
    ['email' => 'admin_phase5@apexsions.test'],
    [
        'name' => 'Phase5TestAdmin',
        'password' => bcrypt('secret123'),
        'role_id' => 1,
    ]
);
Auth::login($admin);

// Ensure clean registry state
PluginRegistryService::seedDefaultRegistry();

// ---------------------------------------------------------------------
// SECTION 1: CUSTOM PLUGIN REGISTRY FOUNDATION
// ---------------------------------------------------------------------
echo "\n--- Section 1: Custom Plugin Registry Foundation ---\n";

$allPlugins = PluginRegistryService::getAllPlugins();
assertTest(
    '1. Registry contains all 8 official Apexsions custom plugins',
    $allPlugins->count() === 8,
    "Expected 8 plugins, found: " . $allPlugins->count()
);

$expectedPluginIds = [
    'apexsions-core', 'apexsions-chat', 'apexsions-economy', 'apexsions-battlepass',
    'apexsions-shop', 'apexsions-media', 'apexsions-crates', 'apexsions-customenchants'
];
$foundIds = $allPlugins->pluck('plugin_id')->toArray();
$missingIds = array_diff($expectedPluginIds, $foundIds);
assertTest(
    '2. All 8 plugin IDs match official ecosystem naming conventions',
    empty($missingIds),
    "Missing IDs: " . implode(', ', $missingIds)
);

$corePlugins = PluginRegistryService::getAllPlugins(['type' => 'CORE']);
assertTest(
    '3. Filter plugins by domain type (CORE) returns ApexsionsCore',
    $corePlugins->count() === 1 && $corePlugins->first()->name === 'ApexsionsCore'
);

$webReadyPlugins = PluginRegistryService::getAllPlugins(['integration' => 'WEB_READY']);
assertTest(
    '4. Filter by integration tier (WEB_READY) returns 4 core web-connected plugins',
    $webReadyPlugins->count() === 4,
    "Count: " . $webReadyPlugins->count()
);

$economyPlugin = PluginRegistryService::getPlugin('apexsions-economy');
assertTest(
    '5. Plugin metadata resolves main class, authors, and dependencies',
    $economyPlugin !== null &&
    isset($economyPlugin->metadata['main_class']) &&
    $economyPlugin->metadata['main_class'] === 'com.apexsions.economy.ApexsionsEconomy' &&
    in_array('Vault', $economyPlugin->dependencies)
);

// ---------------------------------------------------------------------
// SECTION 2: PLUGIN CAPABILITY REGISTRY
// ---------------------------------------------------------------------
echo "\n--- Section 2: Plugin Capability Registry ---\n";

$totalCapabilities = PluginCapability::count();
assertTest(
    '6. Total declared capabilities across custom plugins suite > 30',
    $totalCapabilities >= 30,
    "Total capabilities: " . $totalCapabilities
);

$readCaps = PluginCapability::where('type', 'READ')->count();
$writeCaps = PluginCapability::where('type', 'WRITE')->count();
$actionCaps = PluginCapability::where('type', 'ACTION')->count();
$eventCaps = PluginCapability::where('type', 'EVENT')->count();
$metricCaps = PluginCapability::where('type', 'METRIC')->count();
assertTest(
    '7. Capabilities categorized into READ, WRITE, ACTION, EVENT, and METRIC domains',
    $readCaps > 0 && $writeCaps > 0 && $actionCaps > 0 && $eventCaps > 0 && $metricCaps > 0,
    "Counts: R:$readCaps, W:$writeCaps, A:$actionCaps, E:$eventCaps, M:$metricCaps"
);

assertTest(
    '8. Query capability availability via hasCapability() returns true for valid capability',
    PluginRegistryService::hasCapability('apexsions-economy', 'economy.balance.adjust') === true
);

assertTest(
    '9. Query non-existent capability via hasCapability() returns false',
    PluginRegistryService::hasCapability('apexsions-economy', 'economy.arbitrary.hack') === false
);

$maintenanceCap = PluginCapability::where('plugin_id', 'apexsions-core')
    ->where('capability_id', 'core.maintenance.manage')
    ->first();
assertTest(
    '10. Sensitive action capabilities mandate audit reason and confirmation requirements',
    $maintenanceCap !== null && $maintenanceCap->requires_reason === true && $maintenanceCap->requires_confirmation === true
);

// ---------------------------------------------------------------------
// SECTION 3: PLUGIN HEALTH & TELEMETRY SYNCHRONIZATION
// ---------------------------------------------------------------------
echo "\n--- Section 3: Plugin Health & Telemetry Synchronization ---\n";

$telemetrySample = [
    ['name' => 'ApexsionsCore', 'version' => '1.0.1', 'enabled' => true],
    ['name' => 'ApexsionsEconomy', 'version' => '1.0.0', 'enabled' => false],
];

PluginRegistryService::syncFromHeartbeat($telemetrySample);

$updatedCore = PluginRegistryService::getPlugin('apexsions-core');
assertTest(
    '11. Sync telemetry updates plugin version and last heartbeat timestamp',
    $updatedCore->version === '1.0.1' && $updatedCore->last_heartbeat_at !== null
);

$updatedEco = PluginRegistryService::getPlugin('apexsions-economy');
assertTest(
    '12. Syncing disabled plugin shifts status to DISABLED and health to DISABLED',
    $updatedEco->status === 'DISABLED' && $updatedEco->health_status === 'DISABLED'
);

$healthRecord = PluginHealthRecord::where('plugin_id', 'apexsions-economy')->latest('created_at')->first();
assertTest(
    '13. Health status shift creates immutable diagnostic snapshot in health history',
    $healthRecord !== null && $healthRecord->health_status === 'DISABLED' && $healthRecord->error_category === 'RUNTIME'
);

// Restore economy plugin to enabled for remaining tests
PluginRegistryService::syncFromHeartbeat([
    ['name' => 'ApexsionsEconomy', 'version' => '1.0.0', 'enabled' => true],
]);
$restoredEco = PluginRegistryService::getPlugin('apexsions-economy');
assertTest(
    '14. Telemetry recovery restores plugin to ENABLED and HEALTHY status',
    $restoredEco->isHealthy() === true
);

// ---------------------------------------------------------------------
// SECTION 4: CAPABILITY-BASED AUTHORIZATION & SAFETY
// ---------------------------------------------------------------------
echo "\n--- Section 4: Capability-Based Authorization & Safety ---\n";

$resMissingPlugin = PluginActionGateway::executeAction('unknown-plugin', 'core.reload', [], $admin, 'Test reason');
assertTest(
    '15. Gateway rejects execution on non-existent plugin ID',
    $resMissingPlugin['success'] === false
);

// Temporarily set a plugin to disabled
$core = PluginRegistryService::getPlugin('apexsions-core');
$core->update(['status' => 'DISABLED']);
$resDisabled = PluginActionGateway::executeAction('apexsions-core', 'core.reload', [], $admin, 'Test reason');
assertTest(
    '16. Gateway rejects action execution on DISABLED plugin',
    $resDisabled['success'] === false
);
$core->update(['status' => 'ENABLED', 'health_status' => 'HEALTHY']);

$resMissingCap = PluginActionGateway::executeAction('apexsions-core', 'non_existent_cap', [], $admin, 'Test reason');
assertTest(
    '17. Gateway rejects execution when capability is not registered on plugin',
    $resMissingCap['success'] === false
);

$resReadCap = PluginActionGateway::executeAction('apexsions-core', 'core.region.read', [], $admin, 'Test reason');
assertTest(
    '18. Gateway rejects executing READ/EVENT/METRIC capabilities as an operational action',
    $resReadCap['success'] === false
);

$resEmptyReason = PluginActionGateway::executeAction('apexsions-core', 'core.reload', [], $admin, '');
assertTest(
    '19. Gateway rejects sensitive action when mandatory audit reason is missing',
    $resEmptyReason['success'] === false
);

$resUnlistedAction = PluginActionGateway::executeAction('apexsions-core', 'core.level.adjust', [], $admin, 'Valid reason');
assertTest(
    '20. Gateway rejects actions without verified execution template (No-Arbitrary-Command)',
    $resUnlistedAction['success'] === false
);

// ---------------------------------------------------------------------
// SECTION 5: PLUGIN ACTION GATEWAY & AUDIT TRAIL
// ---------------------------------------------------------------------
echo "\n--- Section 5: Plugin Action Gateway & Unified Audit Trail ---\n";

$actionRes = PluginActionGateway::executeAction(
    'apexsions-core',
    'core.reload',
    [],
    $admin,
    'Memuat ulang file konfigurasi ranks dan regions setelah pembaruan'
);
assertTest(
    '21. Execute authorized action (core.reload) succeeds and returns Action ID',
    $actionRes['success'] === true && !empty($actionRes['action_id'])
);

$actionId = $actionRes['action_id'];
$delivery = Delivery::where('action_id', $actionId)->first();
assertTest(
    '22. Action enqueues delivery with matching Action ID, status PENDING, and template command',
    $delivery !== null && $delivery->status === 'PENDING' && $delivery->command === 'ac reload'
);

$audit = AuditLog::where('target_id', 'apexsions-core')->latest('id')->first();
assertTest(
    '23. Unified Audit Log records PLUGIN_ACTION_REQUESTED with category PLUGIN and staff details',
    $audit !== null && $audit->target_type === 'PLUGIN' && $audit->status === 'SUCCESS' && $audit->action === 'PLUGIN_ACTION_REQUESTED'
);

// Test idempotency
$duplicateThrown = false;
try {
    Delivery::create([
        'action_id' => $actionId,
        'idempotency_key' => 'DUP_' . Str::random(8),
        'command' => 'ac reload',
        'status' => 'PENDING',
        'player_uuid' => 'SERVER',
    ]);
} catch (\Illuminate\Database\QueryException $e) {
    $duplicateThrown = true;
}
assertTest(
    '24. Duplicate action_id insertion is blocked by UNIQUE constraint (Idempotency Guarantee)',
    $duplicateThrown === true
);

// ---------------------------------------------------------------------
// SECTION 6: WEB CONTROLLERS & API ENDPOINTS
// ---------------------------------------------------------------------
echo "\n--- Section 6: Web Controllers & API Endpoints ---\n";

$controller = new PluginAdminController();

// 1. Index View
$indexView = $controller->index(new Request());
assertTest(
    '25. PluginAdminController index view resolves with plugins collection and summary stats',
    $indexView->name() === 'apexsions-bridge::admin.plugins.index' &&
    isset($indexView->getData()['plugins']) &&
    isset($indexView->getData()['stats'])
);

// 2. Show View
$showView = $controller->show(new Request(), 'apexsions-core');
assertTest(
    '26. PluginAdminController show view resolves grouped capabilities and health history',
    $showView->name() === 'apexsions-bridge::admin.plugins.show' &&
    isset($showView->getData()['groupedCapabilities']) &&
    isset($showView->getData()['healthHistory'])
);

// 3. Non-existent Plugin 404
$threw404 = false;
try {
    $controller->show(new Request(), 'non-existent-plugin-id');
} catch (\Symfony\Component\HttpKernel\Exception\HttpException $e) {
    $threw404 = ($e->getStatusCode() === 404);
}
assertTest(
    '27. Non-existent plugin ID in show route gracefully throws HTTP 404',
    $threw404 === true
);

// 4. API Endpoints
$apiController = new LinkVerificationController();

// Mock request with valid server key
$validReq = Request::create('/api/apexsions-bridge/plugins', 'GET');
$validReq->headers->set('X-Apexsions-Key', config('apexsions.bridge_key', 'apexsions_bridge_key_live_2026'));

$apiResponse = $apiController->getPlugins($validReq);
$apiData = $apiResponse->getData(true);
assertTest(
    '28. Authenticated GET /api/apexsions-bridge/plugins returns JSON collection with status success',
    $apiResponse->getStatusCode() === 200 &&
    ($apiData['status'] ?? '') === 'success' &&
    ($apiData['count'] ?? 0) === 8
);

// Handshake endpoint
$handshakeReq = Request::create('/api/apexsions-bridge/plugins/handshake', 'POST', [
    'plugins' => [
        ['name' => 'ApexsionsCore', 'version' => '1.0.2', 'enabled' => true],
    ],
]);
$handshakeReq->headers->set('X-Apexsions-Key', config('apexsions.bridge_key', 'apexsions_bridge_key_live_2026'));

$handshakeRes = $apiController->pluginHandshake($handshakeReq);
$handshakeData = $handshakeRes->getData(true);
assertTest(
    '29. Authenticated POST /api/apexsions-bridge/plugins/handshake ingests dynamic plugin states',
    $handshakeRes->getStatusCode() === 200 && ($handshakeData['status'] ?? '') === 'success'
);

echo "\n==========================================================\n";
echo "           PHASE 5 TEST RESULTS SUMMARY                   \n";
echo "==========================================================\n";
echo " TOTAL TESTS PASSED: $passed\n";
echo " TOTAL TESTS FAILED: $failed\n";
echo "==========================================================\n\n";

if ($failed > 0) {
    exit(1);
}
exit(0);
