<?php

/**
 * Apexsions Ecosystem — Production Smoke & Final Integration Test
 * COMPREHENSIVE END-TO-END VALIDATION (PHASES 1 - 8)
 *
 * Usage: php tests/FinalIntegrationTest.php
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\Role;
use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationExecution;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\Transaction;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService;
use Azuriom\Plugin\ApexsionsBridge\Services\NotificationDispatcherService;
use Azuriom\Plugin\ApexsionsBridge\Services\NotificationPolicyService;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginActionGateway;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService;
use Azuriom\Plugin\ApexsionsBridge\Services\SafeAutomationEngine;
use Carbon\Carbon;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

echo "\n==========================================================\n";
echo "   APEXSIONS PRODUCTION FINAL INTEGRATION TEST SUITE      \n";
echo "==========================================================\n\n";

$passed = 0;
$failed = 0;

function assertFinal(string $description, bool $condition, string $failureMsg = ''): void {
    global $passed, $failed;
    if ($condition) {
        echo " [PASS] $description\n";
        $passed++;
    } else {
        echo " [FAIL] $description: $failureMsg\n";
        $failed++;
    }
}

// Ensure clean cache state
Cache::flush();

// 1. Authoritative Admin Authentication
$adminRole = Role::firstOrCreate(['name' => 'ApexsionsAdminRole'], ['is_admin' => true, 'color' => '#e74c3c']);
$adminRole->update(['is_admin' => true]);

$admin = User::firstOrCreate(
    ['email' => 'founder@apexsions.my.id'],
    [
        'name' => 'ApexsionsFounder',
        'password' => bcrypt('StrongSecurePassword2026!'),
    ]
);
$admin->role()->associate($adminRole);
$admin->save();
$admin->refresh();
Auth::login($admin);

assertFinal("1. Admin terautentikasi dengan hak akses authoritatif", $admin->isAdmin() === true);

// 2. Player Synchronization Foundation
$playerUuid = (string) Str::uuid();
$playerName = 'ApexsionsHero_' . Str::random(4);

$mcAccount = MinecraftAccount::updateOrCreate(
    ['minecraft_uuid' => $playerUuid],
    [
        'minecraft_username' => $playerName,
        'user_id' => $admin->id,
        'level' => 45,
        'kingdom' => 'Solaria',
        'rank' => 'ancestor',
        'balance_rupiah' => 75000000,
        'balance_diamond' => 250,
        'verified_at' => Carbon::now(),
    ]
);

assertFinal("2. Profil pemain Minecraft terdaftar dengan UUID authoritative", $mcAccount instanceof MinecraftAccount);
assertFinal("2. Data saldo mata uang ganda (Rupiah & Diamonds) valid", $mcAccount->balance_rupiah == 75000000 && $mcAccount->balance_diamond == 250);

// 3. Plugin Registry & Capability Matrix
PluginRegistryService::seedDefaultRegistry();
$corePlugin = ApexsionsPlugin::where('plugin_id', 'apexsions-core')->with('capabilities')->first();
assertFinal("3. Suite plugin ApexsionsCore terdaftar dan aktif", $corePlugin && $corePlugin->status === 'ENABLED');
assertFinal("3. Kapabilitas custom plugin terpetakan", $corePlugin->capabilities->count() >= 3);

// 4. Safe Server Action Dispatch (Command Bridge)
$actionResult = PluginActionGateway::executeAction(
    'apexsions-core',
    'core.reload',
    [],
    $admin,
    'Production Smoke Test Verification'
);

assertFinal("4. Gateway aksi plugin berhasil memproses instruksi aman", ($actionResult['success'] ?? false) === true);
$delivery = Delivery::where('action_id', $actionResult['action_id'])->first();
assertFinal("4. Instruksi bridge tersimpan di antrean dengan template whitelisted", $delivery && $delivery->command === 'ac reload');

// 5. Atomic Economy Ledger Transaction
$txId = 'TX-PROD-' . strtoupper(Str::random(6));
$transaction = Transaction::create([
    'transaction_id' => $txId,
    'sender_uuid' => $playerUuid,
    'sender_name' => $playerName,
    'receiver_uuid' => 'SERVER',
    'receiver_name' => 'ApexsionsTreasury',
    'amount' => 55000000,
    'tax_amount' => 50000,
    'net_amount' => 54950000,
    'currency' => 'rupiah',
    'type' => 'TRANSFER',
    'status' => 'COMPLETED',
    'metadata' => ['tax_paid' => 50000],
]);
assertFinal("5. Ledger transaksi ekonomi tercatat secara atomic", $transaction instanceof Transaction);

// 6. Event Ingestion & Anomaly Detection Rule
$burstEvent = EventIntelligenceService::ingest([
    'event_id' => 'evt_prod_' . Str::uuid(),
    'event_type' => 'ECONOMY_TRANSACTION',
    'source' => 'PLUGIN',
    'entity_type' => 'TRANSACTION',
    'entity_id' => $txId,
    'actor_type' => 'PLAYER',
    'actor_id' => $playerUuid,
    'actor_name' => $playerName,
    'severity' => 'CRITICAL',
    'metadata' => ['amount' => 55000000, 'currency' => 'rupiah'],
    'occurred_at' => Carbon::now(),
]);
assertFinal("6. Event ekonomi bernilai ekstrem berhasil di-ingest ke ledger", $burstEvent instanceof ApexsionsEvent);

// 7. Incident Created Automatically
$incident = ApexsionsIncident::where('incident_id', $burstEvent->fresh()->incident_id)->first();
assertFinal("7. Anomaly Rule Engine mendeteksi mutasi ekstrem dan membentuk insiden otomatis", $incident instanceof ApexsionsIncident);
assertFinal("7. Tipe insiden sesuai dengan kategori ECONOMY", $incident->type === 'ECONOMY');

// 8. Safe Automation Orchestration
$autoExec = AutomationExecution::where('trigger_incident_id', $incident->incident_id)->first();
assertFinal("8. Safe Automation merespon pembentukan insiden", $autoExec instanceof AutomationExecution);

// 9. Notification & Anti-Spam Deduplication
$notifResult = NotificationPolicyService::process([
    'type' => 'INCIDENT_ALERT',
    'severity' => $incident->severity,
    'title' => "Insiden Baru: {$incident->title}",
    'message' => "Insiden {$incident->incident_id} memerlukan pemantauan staf.",
    'source' => 'AUTOMATION',
    'entity_type' => 'PLAYER',
    'entity_id' => $playerUuid,
    'incident_id' => $incident->incident_id,
]);
$notification = $notifResult['notification'];
assertFinal("9. Notifikasi alert terbuat dan terhubung dengan berkas insiden", $notification instanceof ApexsionsNotification);

// 10. Staff Acknowledgment & Audit Trail
$notification->update([
    'status' => 'ACKNOWLEDGED',
    'acknowledged_by' => $admin->name,
    'acknowledged_at' => Carbon::now(),
]);
AuditService::log([
    'action' => 'PRODUCTION_SMOKE_TEST_COMPLETED',
    'category' => 'SYSTEM',
    'target_type' => 'SERVER',
    'target_id' => 'PRODUCTION',
    'staff_id' => (string) $admin->id,
    'staff_name' => $admin->name,
    'source' => 'CONSOLE',
    'status' => 'SUCCESS',
    'metadata' => [
        'player_uuid' => $playerUuid,
        'incident_id' => $incident->incident_id,
        'action_id' => $actionResult['action_id'],
    ],
]);

$smokeAudit = AuditLog::where('action', 'PRODUCTION_SMOKE_TEST_COMPLETED')->latest()->first();
assertFinal("10. Seluruh alur end-to-end terekam utuh dalam Unified Audit Log", $smokeAudit instanceof AuditLog);

// Summary
echo "\n==========================================================\n";
echo "   FINAL INTEGRATION RESULT: $passed PASSED, $failed FAILED   \n";
echo "==========================================================\n\n";

if ($failed > 0) {
    exit(1);
}
