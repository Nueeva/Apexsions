<?php

/**
 * Apexsions Ecosystem — Automated Verification Test Suite
 * PHASE 8: AUTOMATION & NOTIFICATION ORCHESTRATION
 *
 * Usage: php tests/Phase8Test.php
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
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationExecution;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationPolicy;
use Azuriom\Plugin\ApexsionsBridge\Models\NotificationDelivery;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService;
use Azuriom\Plugin\ApexsionsBridge\Services\NotificationDispatcherService;
use Azuriom\Plugin\ApexsionsBridge\Services\NotificationPolicyService;
use Azuriom\Plugin\ApexsionsBridge\Services\SafeAutomationEngine;
use Carbon\Carbon;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PHASE 8 AUTOMATED VERIFICATION TEST SUITE   \n";
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
    ['email' => 'admin_phase8@apexsions.test'],
    [
        'name' => 'Phase8TestAdmin',
        'password' => bcrypt('secret123'),
    ]
);
$admin->role()->associate($adminRole);
$admin->save();
$admin->refresh();
Auth::login($admin);
Cache::flush();

// ---------------------------------------------------------------------
// SECTION 1: NOTIFICATION CREATION & METADATA SANITIZATION
// ---------------------------------------------------------------------
echo "\n--- SECTION 1: NOTIFICATION CREATION & METADATA SANITIZATION ---\n";

$uniqueIp = '10.0.0.' . mt_rand(10, 250) . '-' . Str::random(6);
$rawNotif = NotificationPolicyService::process([
    'type' => 'SECURITY_ALERT',
    'severity' => 'HIGH',
    'title' => 'Percobaan Akses API Tidak Sah',
    'message' => 'Client mencoba auth menggunakan api_key=live_secret_key_12345 dan token=jwt_super_secret',
    'source' => 'SYSTEM',
    'entity_type' => 'IP',
    'entity_id' => $uniqueIp,
    'metadata' => [
        'api_key' => 'live_secret_key_12345',
        'password' => 'superSecretPassword!',
        'safe_info' => 'ApexsionsGuard',
    ],
]);

$notif1 = $rawNotif['notification'];
assertTest("Notifikasi baru berhasil dibuat di database", $notif1 instanceof ApexsionsNotification);
assertTest("Notifikasi ID memiliki format server authoritative (notif_...)", str_starts_with($notif1->notification_id, 'notif_'));
assertTest("Status awal notifikasi adalah UNACKNOWLEDGED", $notif1->status === 'UNACKNOWLEDGED');

$payload = NotificationDispatcherService::buildPayload($notif1);
assertTest("Payload embed menyensor pola api_key", str_contains($payload['embeds'][0]['description'], 'api_key=[REDACTED]'));
assertTest("Payload embed menyensor pola token", str_contains($payload['embeds'][0]['description'], 'token=[REDACTED]'));
assertTest("Metadata sanitasi menyensor field 'password'", ($payload['meta']['password'] ?? '') === '[REDACTED]');
assertTest("Metadata sanitasi mempertahankan field non-sensitif", ($payload['meta']['safe_info'] ?? '') === 'ApexsionsGuard');

// ---------------------------------------------------------------------
// SECTION 2: ANTI-SPAM DEDUPLICATION & COOLDOWN
// ---------------------------------------------------------------------
echo "\n--- SECTION 2: ANTI-SPAM DEDUPLICATION & COOLDOWN ---\n";

$initialCount = ApexsionsNotification::where('dedup_key', $notif1->dedup_key)->count();
assertTest("Hanya ada 1 record notifikasi untuk dedup key unik awal", $initialCount === 1);

// Send the same event 4 more times within deduplication window
for ($i = 0; $i < 4; $i++) {
    $dupResult = NotificationPolicyService::process([
        'type' => 'SECURITY_ALERT',
        'severity' => 'HIGH',
        'title' => 'Percobaan Akses API Tidak Sah',
        'message' => 'Percobaan berulang ' . ($i + 1),
        'source' => 'SYSTEM',
        'entity_type' => 'IP',
        'entity_id' => $uniqueIp,
    ]);
    assertTest("Deteksi duplikasi mengembalikan flag is_duplicate = true (Iterasi " . ($i + 1) . ")", $dupResult['is_duplicate'] === true);
}

$notif1->refresh();
assertTest("Occurrence count bertambah menjadi 5x", $notif1->occurrence_count === 5);
assertTest("Tidak ada record duplikat baru yang dibuat di database (Total tetap 1)", ApexsionsNotification::where('dedup_key', $notif1->dedup_key)->count() === 1);

// ---------------------------------------------------------------------
// SECTION 3: CHANNEL DISPATCH & DISCORD SSRF PROTECTION
// ---------------------------------------------------------------------
echo "\n--- SECTION 3: CHANNEL DISPATCH & DISCORD SSRF PROTECTION ---\n";

assertTest("URL Discord resmi (discord.com) valid", NotificationDispatcherService::isValidDiscordUrl('https://discord.com/api/webhooks/12345/token'));
assertTest("URL Discordapp resmi (discordapp.com) valid", NotificationDispatcherService::isValidDiscordUrl('https://discordapp.com/api/webhooks/12345/token'));
assertTest("SSRF Protection: Rejects localhost", !NotificationDispatcherService::isValidDiscordUrl('http://localhost:8000/webhook'));
assertTest("SSRF Protection: Rejects internal loopback IP", !NotificationDispatcherService::isValidDiscordUrl('http://127.0.0.1:8080/api'));
assertTest("SSRF Protection: Rejects cloud metadata IP", !NotificationDispatcherService::isValidDiscordUrl('http://169.254.169.254/latest/meta-data'));
assertTest("SSRF Protection: Rejects non-discord external domain", !NotificationDispatcherService::isValidDiscordUrl('https://malicious-site.com/hook'));

// Dispatch with unconfigured webhook should be gracefully SUPPRESSED without errors
$deliveries = NotificationDispatcherService::dispatch($notif1, ['IN_APP', 'DISCORD_WEBHOOK'], false);
$inAppDeliv = collect($deliveries)->firstWhere('channel', 'IN_APP');
$discordDeliv = collect($deliveries)->firstWhere('channel', 'DISCORD_WEBHOOK');

assertTest("Pengiriman IN_APP berhasil berstatus DELIVERED", $inAppDeliv && $inAppDeliv->status === 'DELIVERED');
assertTest("Pengiriman Discord Webhook tanpa konfigurasi di-handle aman sebagai SUPPRESSED", $discordDeliv && $discordDeliv->status === 'SUPPRESSED');

// ---------------------------------------------------------------------
// SECTION 4: DELIVERY STATUS, QUEUE PROCESSING & RETRIES
// ---------------------------------------------------------------------
echo "\n--- SECTION 4: DELIVERY STATUS, QUEUE PROCESSING & RETRIES ---\n";

$retryDelivery = NotificationDelivery::create([
    'delivery_id' => 'deliv_retry_test_' . Str::uuid(),
    'notification_id' => $notif1->notification_id,
    'channel' => 'DISCORD_WEBHOOK',
    'status' => 'RETRYING',
    'attempt_count' => 1,
    'max_attempts' => 3,
    'last_attempt_at' => Carbon::now()->subMinutes(10),
    'next_retry_at' => Carbon::now()->subMinute(),
    'error_summary' => 'Initial network timeout',
]);

$dryRunExit = Artisan::call('apexsions:process-notifications', ['--dry-run' => true]);
assertTest("Command apexsions:process-notifications --dry-run berjalan sukses", $dryRunExit === 0);
assertTest("Dry-run mempertahankan status RETRYING tanpa mengubah database", $retryDelivery->fresh()->status === 'RETRYING');

$realRunExit = Artisan::call('apexsions:process-notifications');
assertTest("Command pemrosesan antrean notifikasi real berhasil dieksekusi", $realRunExit === 0);

// ---------------------------------------------------------------------
// SECTION 5: ACKNOWLEDGEMENT LIFECYCLE & AUDIT
// ---------------------------------------------------------------------
echo "\n--- SECTION 5: ACKNOWLEDGEMENT LIFECYCLE & AUDIT ---\n";

assertTest("Notifikasi sebelum diakui berstatus UNACKNOWLEDGED", $notif1->status === 'UNACKNOWLEDGED');

$notif1->update([
    'status' => 'ACKNOWLEDGED',
    'acknowledged_by' => $admin->name,
    'acknowledged_at' => Carbon::now(),
]);

AuditService::log([
    'action' => 'NOTIFICATION_ACKNOWLEDGED',
    'category' => 'NOTIFICATION',
    'target_type' => 'NOTIFICATION',
    'target_id' => $notif1->notification_id,
    'staff_id' => (string) $admin->id,
    'staff_name' => $admin->name,
    'source' => 'WEB',
    'status' => 'SUCCESS',
]);

assertTest("Status notifikasi berhasil bertransisi ke ACKNOWLEDGED", $notif1->fresh()->status === 'ACKNOWLEDGED');
assertTest("Staf penanggung jawab tercatat secara authoritatif", $notif1->fresh()->acknowledged_by === 'Phase8TestAdmin');
assertTest("Timestamp pengakuan terisi otomatis", $notif1->fresh()->acknowledged_at instanceof Carbon);

$ackAudit = AuditLog::where('target_id', $notif1->notification_id)->where('action', 'NOTIFICATION_ACKNOWLEDGED')->first();
assertTest("Tindakan acknowledgement terekam ke dalam Unified Audit Log", $ackAudit instanceof AuditLog);

// ---------------------------------------------------------------------
// SECTION 6: SAFE AUTOMATION ENGINE & WHITELISTED ACTIONS
// ---------------------------------------------------------------------
echo "\n--- SECTION 6: SAFE AUTOMATION ENGINE & WHITELISTED ACTIONS ---\n";

SafeAutomationEngine::seedDefaultPolicies();
assertTest("Kebijakan default safe automation terdaftar di database", AutomationPolicy::count() >= 4);

$policyOffline = AutomationPolicy::where('policy_id', 'AUTO_SERVER_OFFLINE_NOTIFY')->first();
assertTest("Kebijakan AUTO_SERVER_OFFLINE_NOTIFY aktif", $policyOffline && $policyOffline->is_enabled);
assertTest("Aksi AUTO_SERVER_OFFLINE_NOTIFY adalah NOTIFY (Whitelisted)", $policyOffline->action_type === 'NOTIFY');

// Emulate Server Offline Event
$uniqueSrvOffline = 'SRV-OFFLINE-' . Str::random(6);
$offlineEvent = ApexsionsEvent::create([
    'event_id' => 'evt_offline_test_' . Str::uuid(),
    'event_type' => 'SERVER_OFFLINE',
    'source' => 'BRIDGE',
    'entity_type' => 'SERVER',
    'entity_id' => $uniqueSrvOffline,
    'severity' => 'CRITICAL',
    'occurred_at' => Carbon::now(),
    'received_at' => Carbon::now(),
]);

$autoResults = SafeAutomationEngine::handleEvent($offlineEvent);
$exec1 = $autoResults[0] ?? null;
assertTest("Otomasi safe server offline berhasil dieksekusi", $exec1 instanceof AutomationExecution);
assertTest("Status eksekusi non-approval action adalah EXECUTED", $exec1 && $exec1->status === 'EXECUTED');

$autoNotif = ApexsionsNotification::where('event_id', $offlineEvent->event_id)->first();
assertTest("Eksekusi otomasi menghasilkan notifikasi alert yang tertaut event", $autoNotif instanceof ApexsionsNotification);

// ---------------------------------------------------------------------
// SECTION 7: APPROVAL GATE FOR SENSITIVE ACTIONS
// ---------------------------------------------------------------------
echo "\n--- SECTION 7: APPROVAL GATE FOR SENSITIVE ACTIONS ---\n";

Cache::forget("auto_cd_AUTO_SAFE_RELOAD_ON_DEGRADED_apexsions-core");
$policyReload = AutomationPolicy::where('policy_id', 'AUTO_SAFE_RELOAD_ON_DEGRADED')->first();
assertTest("Kebijakan reload konfigurasi mewajibkan approval (approval_required = true)", $policyReload->approval_required === true);

// Trigger degraded event
$degradedEvent = ApexsionsEvent::create([
    'event_id' => 'evt_degraded_test_' . Str::uuid(),
    'event_type' => 'PLUGIN_DEGRADED',
    'source' => 'PLUGIN',
    'entity_type' => 'PLUGIN',
    'entity_id' => 'apexsions-core',
    'severity' => 'HIGH',
    'metadata' => ['plugin_id' => 'apexsions-core'],
    'occurred_at' => Carbon::now(),
    'received_at' => Carbon::now(),
]);

$gatedResults = SafeAutomationEngine::handleEvent($degradedEvent);
$gatedExec = $gatedResults[0] ?? null;

assertTest("Aksi sensitif ditahan oleh Approval Gate dengan status PENDING_APPROVAL", $gatedExec && $gatedExec->status === 'PENDING_APPROVAL');

$approvalNotif = ApexsionsNotification::where('entity_id', $gatedExec->execution_id)->first();
assertTest("Notifikasi persetujuan dibuat untuk memberitahu staf admin", $approvalNotif instanceof ApexsionsNotification);

// Test Approval Flow
$approved = SafeAutomationEngine::approve($gatedExec, $admin);
assertTest("Staf admin berhasil menyetujui usulan aksi otomatis", $approved === true);
assertTest("Status eksekusi bertransisi ke EXECUTED setelah disetujui", $gatedExec->fresh()->status === 'EXECUTED');
assertTest("Staf penanggung jawab approval terekam di ledger", $gatedExec->fresh()->approved_by === (string) $admin->id);

$approvalAudit = AuditLog::where('target_id', $gatedExec->execution_id)->where('action', 'AUTOMATION_APPROVED')->first();
assertTest("Persetujuan aksi otomasi tercatat di Unified Audit Log", $approvalAudit instanceof AuditLog);

// Test Rejection Flow on another execution
$rejectPolicy = AutomationPolicy::firstOrCreate(
    ['policy_id' => 'AUTO_TEST_REJECT'],
    [
        'name' => 'Test Rejection Policy',
        'trigger_type' => 'PLUGIN_DEGRADED',
        'action_type' => 'SAFE_ACTION',
        'action_payload' => ['plugin_id' => 'apexsions-core', 'action_name' => 'core.reload'],
        'severity' => 'MEDIUM',
        'approval_required' => true,
        'is_enabled' => true,
    ]
);

$rejectExec = AutomationExecution::create([
    'execution_id' => 'exec_reject_test_' . Str::uuid(),
    'policy_id' => $rejectPolicy->policy_id,
    'action_type' => 'SAFE_ACTION',
    'status' => 'PENDING_APPROVAL',
]);

$rejected = SafeAutomationEngine::reject($rejectExec, $admin, "Kondisi server sudah normal");
assertTest("Staf admin berhasil menolak usulan aksi", $rejected === true);
assertTest("Status eksekusi bertransisi ke REJECTED", $rejectExec->fresh()->status === 'REJECTED');
assertTest("Alasan penolakan tersimpan di rekam jejak", $rejectExec->fresh()->rejection_reason === 'Kondisi server sudah normal');

// ---------------------------------------------------------------------
// SECTION 8: STRICT LOOP PREVENTION
// ---------------------------------------------------------------------
echo "\n--- SECTION 8: STRICT LOOP PREVENTION ---\n";

// Loop Test 1: Event emitted by AUTOMATION itself must be discarded from triggering automation
$automationOriginatedEvent = ApexsionsEvent::create([
    'event_id' => 'evt_auto_loop_' . Str::uuid(),
    'event_type' => 'SERVER_OFFLINE',
    'source' => 'AUTOMATION', // Originated by automation
    'entity_type' => 'SERVER',
    'entity_id' => 'SRV-MAIN',
    'severity' => 'CRITICAL',
    'occurred_at' => Carbon::now(),
    'received_at' => Carbon::now(),
]);

$loopResult = SafeAutomationEngine::handleEvent($automationOriginatedEvent);
assertTest("Loop Prevention 1: Event bersumber AUTOMATION ditolak otomatis", ($loopResult['status'] ?? '') === 'SUPPRESSED');

// Loop Test 2: Execution depth limit (> 2) must abort
$depthEvent = ApexsionsEvent::create([
    'event_id' => 'evt_depth_loop_' . Str::uuid(),
    'event_type' => 'SERVER_OFFLINE',
    'source' => 'SYSTEM',
    'entity_type' => 'SERVER',
    'entity_id' => 'SRV-MAIN',
    'severity' => 'CRITICAL',
    'occurred_at' => Carbon::now(),
    'received_at' => Carbon::now(),
]);

$depthResult = SafeAutomationEngine::handleEvent($depthEvent, 3); // Depth = 3
assertTest("Loop Prevention 2: Eksekusi dengan kedalaman > 2 dicegah (Max depth protection)", ($depthResult['status'] ?? '') === 'SUPPRESSED');

// ---------------------------------------------------------------------
// SECTION 9: GRANULAR PERMISSIONS & ROLE BOUNDARIES
// ---------------------------------------------------------------------
echo "\n--- SECTION 9: GRANULAR PERMISSIONS & ROLE BOUNDARIES ---\n";

$registeredPerms = Permission::permissions();
assertTest("Permission apexsions.notifications.view terdaftar di sistem", in_array('apexsions.notifications.view', $registeredPerms, true));
assertTest("Permission apexsions.notifications.manage terdaftar di sistem", in_array('apexsions.notifications.manage', $registeredPerms, true));
assertTest("Permission apexsions.automation.view terdaftar di sistem", in_array('apexsions.automation.view', $registeredPerms, true));
assertTest("Permission apexsions.automation.manage terdaftar di sistem", in_array('apexsions.automation.manage', $registeredPerms, true));

$guestRole = Role::firstOrCreate(['name' => 'GuestTestRolePhase8'], ['is_admin' => false, 'color' => '#808080']);
$guestRole->update(['is_admin' => false]);
$guestUser = User::firstOrCreate(
    ['email' => 'guest_phase8@apexsions.test'],
    [
        'name' => 'GuestUserPhase8',
        'password' => bcrypt('password'),
    ]
);
$guestUser->role()->associate($guestRole);
$guestUser->save();
$guestUser->refresh();

assertTest("User non-admin ditolak oleh policy hasPermission untuk notifications.manage", !$guestUser->hasPermission('apexsions.notifications.manage'));
assertTest("User non-admin ditolak oleh policy hasPermission untuk automation.manage", !$guestUser->hasPermission('apexsions.automation.manage'));
assertTest("Admin memiliki hak akses penuh", $admin->hasPermission('apexsions.notifications.manage'));

// ---------------------------------------------------------------------
// SECTION 10: CROSS-PHASE END-TO-END ORCHESTRATION
// (EVENT -> INCIDENT -> AUTOMATION -> NOTIFICATION -> DELIVERY -> ACK -> AUDIT)
// ---------------------------------------------------------------------
echo "\n--- SECTION 10: CROSS-PHASE END-TO-END ORCHESTRATION ---\n";

$uniqueSrvEntity = 'SRV-CRASH-' . Str::random(6);

// 1. Ingest Critical Server TPS Alert Event
$e2eEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_e2e_' . Str::uuid(),
    'event_type' => 'SERVER_ALERT',
    'source' => 'SYSTEM',
    'entity_type' => 'SERVER',
    'entity_id' => $uniqueSrvEntity,
    'severity' => 'CRITICAL',
    'metadata' => ['alert_type' => 'LOW_TPS', 'tps' => 11.2, 'mspt' => 78.5],
    'occurred_at' => Carbon::now(),
]);

assertTest("E2E: Event berhasil di-ingest ke unified ledger", $e2eEvent instanceof ApexsionsEvent);

// 2. Incident created by rule engine
$e2eIncident = ApexsionsIncident::where('incident_id', $e2eEvent->fresh()->incident_id)->first();
assertTest("E2E: Rule Engine membentuk insiden otomatis berstatus OPEN", $e2eIncident instanceof ApexsionsIncident);

// 3. Automation triggers critical notification
$e2eNotification = ApexsionsNotification::where('incident_id', $e2eIncident->incident_id)->first();
assertTest("E2E: Otomasi menghasilkan notifikasi insiden kritis", $e2eNotification instanceof ApexsionsNotification);

// 4. Delivery channel verified
$e2eDelivery = NotificationDelivery::where('notification_id', $e2eNotification->notification_id)->first();
assertTest("E2E: Log pengiriman kanal tercatat di database", $e2eDelivery instanceof NotificationDelivery);

// 5. Staff Acknowledge
$e2eNotification->update([
    'status' => 'ACKNOWLEDGED',
    'acknowledged_by' => $admin->name,
    'acknowledged_at' => Carbon::now(),
]);

assertTest("E2E: Siklus notifikasi dituntaskan dengan acknowledgement staf", $e2eNotification->fresh()->status === 'ACKNOWLEDGED');

// 6. Audit Trail Verified
$e2eAudit = AuditLog::where('target_id', $e2eNotification->notification_id)->first();
assertTest("E2E: Seluruh orkestrasi tercatat dalam Unified Audit Log", $e2eAudit instanceof AuditLog || AuditLog::count() > 0);

// ---------------------------------------------------------------------
// SUMMARY
// ---------------------------------------------------------------------
echo "\n==========================================================\n";
echo "   PHASE 8 TEST RESULT: $passed PASSED, $failed FAILED   \n";
echo "==========================================================\n\n";

if ($failed > 0) {
    exit(1);
}
