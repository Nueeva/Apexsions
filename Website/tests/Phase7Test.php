<?php

/**
 * Apexsions Ecosystem — Automated Verification Test Suite
 * PHASE 7: ARCHITECTURE HARDENING & PRODUCTION READINESS
 *
 * Usage: php tests/Phase7Test.php
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\Permission;
use Azuriom\Models\Role;
use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService;
use Azuriom\Plugin\ApexsionsBridge\Services\IncidentService;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginActionGateway;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService;
use Carbon\Carbon;
use Illuminate\Console\Scheduling\Schedule;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PHASE 7 AUTOMATED VERIFICATION TEST SUITE   \n";
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

// Setup dedicated Admin Role and User
$role1 = Role::find(1);
if ($role1) {
    $role1->update(['is_admin' => false]);
}

$adminRole = Role::firstOrCreate(['name' => 'ApexsionsAdminRole'], ['is_admin' => true, 'color' => '#e74c3c']);
$adminRole->update(['is_admin' => true]);

$admin = User::firstOrCreate(
    ['email' => 'admin_phase7@apexsions.test'],
    [
        'name' => 'Phase7TestAdmin',
        'password' => bcrypt('secret123'),
    ]
);
$admin->role()->associate($adminRole);
$admin->save();
$admin->refresh();
Auth::login($admin);

// ---------------------------------------------------------------------
// SECTION 1: DATA RETENTION & SAFE EVENT PURGE ENGINE
// ---------------------------------------------------------------------
echo "\n--- SECTION 1: DATA RETENTION & SAFE EVENT PURGE ENGINE ---\n";

$oldTimestamp = Carbon::now()->subDays(45);

// 1. Create candidate unlinked event (Eligible for cleanup)
$unlinkedOldEvent = ApexsionsEvent::create([
    'event_id' => 'evt_purge_candidate_' . Str::uuid(),
    'event_type' => 'PLAYER_PING',
    'source' => 'SYSTEM',
    'entity_type' => 'PLAYER',
    'entity_id' => 'uuid-purge-01',
    'severity' => 'INFO',
    'occurred_at' => $oldTimestamp,
    'received_at' => $oldTimestamp,
]);

// 2. Create old event LINKED to an incident (MUST NEVER BE PURGED)
$incidentId = 'INC-' . date('Ymd') . '-' . strtoupper(Str::random(4));
$incidentProtected = ApexsionsIncident::create([
    'incident_id' => $incidentId,
    'title' => 'Protected Incident for Retention Test',
    'type' => 'SECURITY',
    'severity' => 'HIGH',
    'status' => 'INVESTIGATING',
    'source' => 'RULE_ENGINE',
    'detected_at' => $oldTimestamp,
    'root_entity_type' => 'PLAYER',
    'root_entity_id' => 'uuid-protected-01',
    'root_entity_name' => 'ProtectedPlayer',
]);

$linkedOldEvent = ApexsionsEvent::create([
    'event_id' => 'evt_protected_linked_' . Str::uuid(),
    'event_type' => 'PLAYER_REPORTED',
    'source' => 'MINECRAFT',
    'entity_type' => 'PLAYER',
    'entity_id' => 'uuid-protected-01',
    'severity' => 'MEDIUM',
    'incident_id' => $incidentProtected->incident_id,
    'occurred_at' => $oldTimestamp,
    'received_at' => $oldTimestamp,
]);

// 3. Create old event with CRITICAL severity (MUST NEVER BE PURGED)
$criticalOldEvent = ApexsionsEvent::create([
    'event_id' => 'evt_protected_critical_' . Str::uuid(),
    'event_type' => 'SERVER_ALERT',
    'source' => 'SYSTEM',
    'entity_type' => 'SERVER',
    'entity_id' => 'SRV-MAIN',
    'severity' => 'CRITICAL',
    'occurred_at' => $oldTimestamp,
    'received_at' => $oldTimestamp,
]);

// Test 1: Dry run execution
$exitDryRun = Artisan::call('apexsions:clean-events', ['--days' => 30, '--dry-run' => true]);
assertTest("Command apexsions:clean-events --dry-run berhasil dieksekusi", $exitDryRun === 0);
assertTest("Dry run tidak menghapus event apapun dari database", ApexsionsEvent::where('id', $unlinkedOldEvent->id)->exists());

// Test 2: Real execution
$exitReal = Artisan::call('apexsions:clean-events', ['--days' => 30]);
assertTest("Command pembersihan real berhasil dieksekusi", $exitReal === 0);
assertTest("Event biasa usang yang tidak tertaut insiden berhasil dibersihkan", !ApexsionsEvent::where('id', $unlinkedOldEvent->id)->exists());
assertTest("Event usang yang TERTAUT INSIDEN DILINDUNGI dan tidak dihapus", ApexsionsEvent::where('id', $linkedOldEvent->id)->exists());
assertTest("Event usang dengan SEVERITY CRITICAL DILINDUNGI dan tidak dihapus", ApexsionsEvent::where('id', $criticalOldEvent->id)->exists());

$cleanupAudit = AuditLog::where('action', 'DATA_RETENTION_CLEANUP')->latest()->first();
assertTest("Aktivitas pembersihan data retensi tercatat ke dalam Unified Audit Log", $cleanupAudit instanceof AuditLog);

// ---------------------------------------------------------------------
// SECTION 2: SCHEDULER & BACKGROUND JOB VERIFICATION
// ---------------------------------------------------------------------
echo "\n--- SECTION 2: SCHEDULER & BACKGROUND JOB VERIFICATION ---\n";

$schedule = app(Schedule::class);
$scheduledEvents = collect($schedule->events());

$hasCleanEventsScheduled = $scheduledEvents->contains(function ($event) {
    return str_contains($event->command ?? '', 'apexsions:clean-events');
});

assertTest("Task apexsions:clean-events terdaftar secara resmi di Laravel Scheduler", $hasCleanEventsScheduled);

// ---------------------------------------------------------------------
// SECTION 3: DOMAIN-AWARE EVENT CORRELATION HARDENING
// ---------------------------------------------------------------------
echo "\n--- SECTION 3: DOMAIN-AWARE EVENT CORRELATION HARDENING ---\n";

assertTest("Domain ECONOMY menggunakan jendela korelasi 2 jam", EventIntelligenceService::getDomainCorrelationWindowHours('ECONOMY_TRANSACTION') === 2);
assertTest("Domain MODERATION menggunakan jendela korelasi 24 jam", EventIntelligenceService::getDomainCorrelationWindowHours('PLAYER_REPORTED') === 24);
assertTest("Domain SERVER menggunakan jendela korelasi 1 jam", EventIntelligenceService::getDomainCorrelationWindowHours('SERVER_ALERT') === 1);
assertTest("Domain GENERAL menggunakan jendela default 6 jam", EventIntelligenceService::getDomainCorrelationWindowHours('PLAYER_LOGIN') === 6);

// Test correlation output label
$economyEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_eco_win_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'entity_id' => 'uuid-trader-99',
    'source' => 'MINECRAFT',
    'severity' => 'INFO',
    'metadata' => ['amount' => 1000, 'currency' => 'rupiah'],
    'occurred_at' => Carbon::now()->subMinutes(30),
]);

$recentTx = EventIntelligenceService::ingest([
    'event_id' => 'evt_eco_win2_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'entity_id' => 'uuid-trader-99',
    'source' => 'MINECRAFT',
    'severity' => 'INFO',
    'metadata' => ['amount' => 500, 'currency' => 'rupiah'],
    'occurred_at' => Carbon::now()->subMinutes(10),
]);

$corrs = EventIntelligenceService::correlate($economyEvent);
$mediumCorr = $corrs->firstWhere('confidence', 'MEDIUM');
assertTest("Korelasi MEDIUM menyertakan label domain kontekstual [Domain: ECONOMY]", str_contains($mediumCorr['reason'] ?? '', '[Domain: ECONOMY]'));

// ---------------------------------------------------------------------
// SECTION 4: GRANULAR PERMISSION HARDENING
// ---------------------------------------------------------------------
echo "\n--- SECTION 4: GRANULAR PERMISSION HARDENING ---\n";

$registeredPerms = Permission::permissions();
assertTest("Permission apexsions.economy.manage terdaftar di sistem", in_array('apexsions.economy.manage', $registeredPerms, true));
assertTest("Permission apexsions.server.manage terdaftar di sistem", in_array('apexsions.server.manage', $registeredPerms, true));
assertTest("Permission apexsions.incidents.manage terdaftar di sistem", in_array('apexsions.incidents.manage', $registeredPerms, true));
assertTest("Permission apexsions.plugins.manage terdaftar di sistem", in_array('apexsions.plugins.manage', $registeredPerms, true));

// Test Permission Boundary: Non-admin user with no permissions
$guestRole = Role::firstOrCreate(['name' => 'GuestTestRole'], ['is_admin' => false, 'color' => '#808080']);
$guestRole->update(['is_admin' => false]);
$guestUser = User::firstOrCreate(
    ['email' => 'guest_test@apexsions.test'],
    [
        'name' => 'GuestUser',
        'password' => bcrypt('password'),
    ]
);
$guestUser->role()->associate($guestRole);
$guestUser->save();
$guestUser->refresh();

$admin->refresh();
assertTest("User non-admin tanpa permission ditolak oleh policy hasPermission", !$guestUser->hasPermission('apexsions.economy.manage'));
assertTest("Admin memiliki hak akses menyeluruh via is_admin", $admin->hasPermission('apexsions.economy.manage'));

// ---------------------------------------------------------------------
// SECTION 5: CROSS-PHASE INTEGRATION FLOW 1 (PLUGIN -> EVENT -> RULE -> INCIDENT -> AUDIT)
// ---------------------------------------------------------------------
echo "\n--- SECTION 5: CROSS-PHASE INTEGRATION FLOW 1 ---\n";

// Ensure clean plugin registration
PluginRegistryService::seedDefaultRegistry();
$corePlugin = ApexsionsPlugin::where('plugin_id', 'apexsions-core')->first();
assertTest("Plugin suite ApexsionsCore aktif di registry", $corePlugin && $corePlugin->status === 'ENABLED');

// Emulate plugin emitting a high-severity event
$uniqueWhaleUuid = 'uuid-whale-' . Str::random(6);
$crossEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_cross_flow_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'source' => 'PLUGIN',
    'entity_type' => 'TRANSACTION',
    'entity_id' => 'tx-cross-' . Str::random(6),
    'actor_type' => 'PLAYER',
    'actor_id' => $uniqueWhaleUuid,
    'actor_name' => 'CrossWhale',
    'severity' => 'HIGH',
    'metadata' => [
        'plugin_id' => 'apexsions-core',
        'amount' => 85000000,
        'currency' => 'rupiah',
    ],
    'occurred_at' => Carbon::now(),
]);

// Rule should trigger and link incident
assertTest("Event dari plugin berhasil memicu pembentukan insiden otomatis", !empty($crossEvent->fresh()->incident_id));
$incidentCross = ApexsionsIncident::where('incident_id', $crossEvent->fresh()->incident_id)->first();
assertTest("Insiden tertaut dengan root entity transaksi", $incidentCross instanceof ApexsionsIncident);

// Add investigation note
$noteCross = IncidentService::addNote($incidentCross, "Verifikasi integritas aliran transaksi cross-phase berhasil.", (string) $admin->id, $admin->name);
assertTest("Staf berhasil mendokumentasikan temuan investigasi", $noteCross instanceof \Azuriom\Plugin\ApexsionsBridge\Models\IncidentNote);

// Transition status to RESOLVED
IncidentService::updateStatus($incidentCross, 'RESOLVED', 'Semua saldo terbukti valid');
assertTest("Status insiden berhasil dituntaskan ke RESOLVED", $incidentCross->fresh()->status === 'RESOLVED');

// Check audit log
$auditCross = AuditLog::where('target_id', $incidentCross->incident_id)->where('action', 'INCIDENT_STATUS_CHANGE')->first();
assertTest("Seluruh rantai siklus investigasi terekam utuh di Unified Audit Log", $auditCross instanceof AuditLog);

// ---------------------------------------------------------------------
// SECTION 6: CROSS-PHASE INTEGRATION FLOW 2 (ADMIN ACTION -> BRIDGE RELIABILITY -> AUDIT)
// ---------------------------------------------------------------------
echo "\n--- SECTION 6: CROSS-PHASE INTEGRATION FLOW 2 ---\n";

Auth::login($admin);
$actionResult = PluginActionGateway::executeAction(
    'apexsions-core',
    'core.reload',
    [],
    $admin,
    'Hardening verification test of action dispatch'
);

assertTest("Gateway aksi plugin berhasil memproses request", ($actionResult['success'] ?? false) === true);
assertTest("Action ID tergenerasi secara unik dari server", !empty($actionResult['action_id']));

$delivery = Delivery::where('action_id', $actionResult['action_id'])->first();
assertTest("Instruksi bridge tersimpan di antrean dengan status PENDING", $delivery && $delivery->status === 'PENDING');
assertTest("Payload bridge memuat template command aman (bukan arbitrary command)", $delivery && $delivery->command === 'ac reload');

// Race condition / Idempotency protection check: duplicate Action ID delivery must fail
$isDuplicateBlocked = false;
try {
    Delivery::create([
        'action_id' => $actionResult['action_id'], // Duplicate unique key
        'player_uuid' => 'SERVER',
        'command' => 'ac reload',
        'status' => 'PENDING',
    ]);
} catch (\Illuminate\Database\QueryException $e) {
    $isDuplicateBlocked = true;
}

assertTest("Proteksi race condition & replay: duplikasi Action ID diblokir oleh UNIQUE constraint database", $isDuplicateBlocked);

// ---------------------------------------------------------------------
// SECTION 7: RESILIENCE & SENSITIVE METADATA SANITIZATION
// ---------------------------------------------------------------------
echo "\n--- SECTION 7: RESILIENCE & SENSITIVE METADATA SANITIZATION ---\n";

$sanitizedLog = AuditService::log([
    'action' => 'CREDENTIAL_UPDATE_TEST',
    'target_type' => 'PLAYER',
    'target_id' => 'uuid-test',
    'source' => 'WEB',
    'status' => 'SUCCESS',
    'metadata' => [
        'password' => 'superSecretPassword123!',
        'api_key' => 'live_secret_key_abcdef',
        'token' => 'jwt_secret_token',
        'safe_field' => 'visibleValue',
    ],
]);

assertTest("Sanitizer menyensor field 'password'", $sanitizedLog->metadata['password'] === '[REDACTED]');
assertTest("Sanitizer menyensor field 'api_key'", $sanitizedLog->metadata['api_key'] === '[REDACTED]');
assertTest("Sanitizer menyensor field 'token'", $sanitizedLog->metadata['token'] === '[REDACTED]');
assertTest("Sanitizer mempertahankan field metadata non-sensitif", $sanitizedLog->metadata['safe_field'] === 'visibleValue');

// ---------------------------------------------------------------------
// SUMMARY
// ---------------------------------------------------------------------
echo "\n==========================================================\n";
echo "   PHASE 7 TEST RESULT: $passed PASSED, $failed FAILED   \n";
echo "==========================================================\n\n";

if ($failed > 0) {
    exit(1);
}
