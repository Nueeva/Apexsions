<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationExecution;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationPolicy;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

class SafeAutomationEngine
{
    public const WHITELISTED_ACTIONS = [
        'NOTIFY',
        'CREATE_INCIDENT',
        'UPDATE_INCIDENT',
        'CREATE_ALERT',
        'SAFE_ACTION',
        'REQUIRES_APPROVAL',
    ];

    /**
     * Seed default safe automation policies.
     */
    public static function seedDefaultPolicies(): void
    {
        $defaultPolicies = [
            'AUTO_SERVER_OFFLINE_NOTIFY' => [
                'name' => 'Notifikasi Otomatis Kegagalan Server Offline',
                'trigger_type' => 'SERVER_OFFLINE',
                'condition_schema' => [],
                'action_type' => 'NOTIFY',
                'action_payload' => ['title' => 'Peringatan Server Offline', 'severity' => 'CRITICAL'],
                'severity' => 'CRITICAL',
                'cooldown_minutes' => 10,
                'approval_required' => false,
            ],
            'AUTO_PLUGIN_ERROR_ALERT' => [
                'name' => 'Peringatan Degradasi Modul Plugin Custom',
                'trigger_type' => 'PLUGIN_STATUS_CHANGE',
                'condition_schema' => ['status' => 'ERROR'],
                'action_type' => 'NOTIFY',
                'action_payload' => ['title' => 'Degradasi Kesehatan Plugin', 'severity' => 'HIGH'],
                'severity' => 'HIGH',
                'cooldown_minutes' => 15,
                'approval_required' => false,
            ],
            'AUTO_CRITICAL_INCIDENT_ESCALATE' => [
                'name' => 'Eskalasi Notifikasi Insiden Kritis Baru',
                'trigger_type' => 'INCIDENT_CREATED',
                'condition_schema' => ['severity' => 'CRITICAL'],
                'action_type' => 'NOTIFY',
                'action_payload' => ['title' => 'Eskalasi Insiden Kritis Baru', 'severity' => 'CRITICAL'],
                'severity' => 'CRITICAL',
                'cooldown_minutes' => 15,
                'approval_required' => false,
            ],
            'AUTO_SAFE_RELOAD_ON_DEGRADED' => [
                'name' => 'Usulan Reload Konfigurasi Saat Degradasi Terdeteksi',
                'trigger_type' => 'PLUGIN_DEGRADED',
                'condition_schema' => ['plugin_id' => 'apexsions-core'],
                'action_type' => 'SAFE_ACTION',
                'action_payload' => ['plugin_id' => 'apexsions-core', 'action_name' => 'core.reload'],
                'severity' => 'MEDIUM',
                'cooldown_minutes' => 30,
                'approval_required' => true, // Approval Gate strictly required!
            ],
        ];

        foreach ($defaultPolicies as $policyId => $def) {
            AutomationPolicy::firstOrCreate(
                ['policy_id' => $policyId],
                [
                    'name' => $def['name'],
                    'trigger_type' => $def['trigger_type'],
                    'condition_schema' => $def['condition_schema'],
                    'action_type' => $def['action_type'],
                    'action_payload' => $def['action_payload'],
                    'severity' => $def['severity'],
                    'cooldown_minutes' => $def['cooldown_minutes'],
                    'approval_required' => $def['approval_required'],
                    'is_enabled' => true,
                ]
            );
        }
    }

    /**
     * Handle an event trigger through the safe automation framework.
     */
    public static function handleEvent(ApexsionsEvent $event, int $depth = 1): array
    {
        // 1. Loop Prevention: Max execution depth check
        if ($depth > 2) {
            return [
                'status' => 'SUPPRESSED',
                'reason' => 'Loop Prevention: Max execution depth exceeded',
            ];
        }

        // 2. Loop Prevention: Do not process events emitted directly by AUTOMATION
        if ($event->source === 'AUTOMATION') {
            return [
                'status' => 'SUPPRESSED',
                'reason' => 'Loop Prevention: Automation-originated event discarded',
            ];
        }

        if (AutomationPolicy::count() === 0) {
            self::seedDefaultPolicies();
        }

        $results = [];
        $policies = AutomationPolicy::enabled()->get();

        foreach ($policies as $policy) {
            if (!self::matchesEventTrigger($policy, $event)) {
                continue;
            }

            // Cooldown check
            $cdKey = "auto_cd_{$policy->policy_id}_{$event->entity_id}";
            if (Cache::has($cdKey)) {
                $results[] = [
                    'policy_id' => $policy->policy_id,
                    'status' => 'SUPPRESSED',
                    'reason' => 'Cooldown active',
                ];
                continue;
            }

            $execution = self::processPolicy($policy, $event->event_id, $event->incident_id, [
                'source' => $event->source,
                'entity_type' => $event->entity_type,
                'entity_id' => $event->entity_id,
                'severity' => $event->severity,
                'title' => "Otomasi: {$policy->name}",
                'message' => "Dipicu oleh event {$event->event_type} [{$event->event_id}].",
            ], $depth);

            Cache::put($cdKey, true, Carbon::now()->addMinutes($policy->cooldown_minutes));
            $results[] = $execution;
        }

        return $results;
    }

    /**
     * Handle an incident trigger through safe automation.
     */
    public static function handleIncident(ApexsionsIncident $incident, int $depth = 1): array
    {
        if ($depth > 2) {
            return [
                'status' => 'SUPPRESSED',
                'reason' => 'Loop Prevention: Max execution depth exceeded',
            ];
        }

        if (AutomationPolicy::count() === 0) {
            self::seedDefaultPolicies();
        }

        $results = [];
        $policies = AutomationPolicy::enabled()->where('trigger_type', 'INCIDENT_CREATED')->get();

        foreach ($policies as $policy) {
            $schema = $policy->condition_schema ?? [];
            if (isset($schema['severity']) && strtoupper($schema['severity']) !== strtoupper($incident->severity)) {
                continue;
            }

            $cdKey = "auto_cd_{$policy->policy_id}_{$incident->incident_id}";
            if (Cache::has($cdKey)) {
                continue;
            }

            $execution = self::processPolicy($policy, null, $incident->incident_id, [
                'source' => 'INCIDENT',
                'entity_type' => $incident->root_entity_type,
                'entity_id' => $incident->root_entity_id,
                'severity' => $incident->severity,
                'title' => "Insiden Baru: {$incident->title}",
                'message' => "Insiden {$incident->incident_id} ({$incident->type}) terdeteksi dengan keparahan {$incident->severity}.",
            ], $depth);

            Cache::put($cdKey, true, Carbon::now()->addMinutes($policy->cooldown_minutes));
            $results[] = $execution;
        }

        return $results;
    }

    /**
     * Process policy execution or route into Approval Gate.
     */
    protected static function processPolicy(AutomationPolicy $policy, ?string $eventId, ?string $incidentId, array $context, int $depth): AutomationExecution
    {
        $executionId = 'exec_' . Str::uuid();
        $actionType = strtoupper($policy->action_type);

        // Security check: Action MUST be strictly in whitelist
        if (!in_array($actionType, self::WHITELISTED_ACTIONS, true)) {
            return AutomationExecution::create([
                'execution_id' => $executionId,
                'policy_id' => $policy->policy_id,
                'trigger_event_id' => $eventId,
                'trigger_incident_id' => $incidentId,
                'action_type' => $actionType,
                'status' => 'REJECTED',
                'rejection_reason' => 'Security Guard: Action type not whitelisted',
                'execution_depth' => $depth,
            ]);
        }

        // Approval Gate check: if approval is required, hold in PENDING_APPROVAL
        if ($policy->approval_required) {
            $execution = AutomationExecution::create([
                'execution_id' => $executionId,
                'policy_id' => $policy->policy_id,
                'trigger_event_id' => $eventId,
                'trigger_incident_id' => $incidentId,
                'action_type' => $actionType,
                'status' => 'PENDING_APPROVAL',
                'execution_depth' => $depth,
                'metadata' => [
                    'proposed_payload' => $policy->action_payload,
                    'context' => $context,
                ],
            ]);

            // Notify staff that an action is pending approval
            $processed = NotificationPolicyService::process([
                'type' => 'APPROVAL_REQUIRED',
                'severity' => 'HIGH',
                'title' => "Persetujuan Diperlukan: {$policy->name}",
                'message' => "Tindakan otomatis [{$actionType}] memerlukan konfirmasi admin sebelum eksekusi.",
                'source' => 'AUTOMATION',
                'entity_type' => 'EXECUTION',
                'entity_id' => $execution->execution_id,
                'incident_id' => $incidentId,
                'metadata' => ['execution_id' => $execution->execution_id],
            ]);
            NotificationDispatcherService::dispatch($processed['notification'], $processed['channels'], $processed['is_duplicate']);

            AuditService::log([
                'action' => 'AUTOMATION_PROPOSED',
                'category' => 'AUTOMATION',
                'target_type' => 'POLICY',
                'target_id' => $policy->policy_id,
                'source' => 'SYSTEM',
                'status' => 'PENDING_APPROVAL',
                'metadata' => ['execution_id' => $execution->execution_id, 'action_type' => $actionType],
            ]);

            return $execution;
        }

        // Direct Execution for non-approval-required safe actions
        return self::executeAction($executionId, $policy, $eventId, $incidentId, $context, $depth);
    }

    /**
     * Execute a safe whitelisted action.
     */
    protected static function executeAction(string $executionId, AutomationPolicy $policy, ?string $eventId, ?string $incidentId, array $context, int $depth): AutomationExecution
    {
        $actionType = strtoupper($policy->action_type);
        $resultSummary = '';

        if ($actionType === 'NOTIFY') {
            $processed = NotificationPolicyService::process([
                'type' => 'AUTOMATION_ALERT',
                'severity' => $policy->severity,
                'title' => $context['title'] ?? $policy->name,
                'message' => $context['message'] ?? 'Alert dipicu oleh kebijakan otomasi.',
                'source' => 'AUTOMATION',
                'entity_type' => $context['entity_type'] ?? 'SYSTEM',
                'entity_id' => $context['entity_id'] ?? 'SYSTEM',
                'event_id' => $eventId,
                'incident_id' => $incidentId,
                'metadata' => ['policy_id' => $policy->policy_id],
            ]);
            NotificationDispatcherService::dispatch($processed['notification'], $processed['channels'], $processed['is_duplicate']);
            $resultSummary = "Notifikasi berhasil diproses ke channel: " . implode(', ', $processed['channels']);
        } elseif ($actionType === 'SAFE_ACTION') {
            $payload = $policy->action_payload ?? [];
            $pluginId = $payload['plugin_id'] ?? 'apexsions-core';
            $actionName = $payload['action_name'] ?? 'core.reload';

            $adminUser = User::whereHas('role', function ($q) {
                $q->where('is_admin', true);
            })->first();

            if ($adminUser) {
                $actionResult = PluginActionGateway::executeAction(
                    $pluginId,
                    $actionName,
                    [],
                    $adminUser,
                    "Automated safe execution via policy {$policy->policy_id}"
                );
                $resultSummary = ($actionResult['success'] ?? false)
                    ? "Safe action {$actionName} executed successfully with Action ID {$actionResult['action_id']}."
                    : "Safe action failed: " . ($actionResult['error'] ?? 'Unknown');
            } else {
                $resultSummary = "Safe action skipped: No authoritative admin user found.";
            }
        } else {
            $resultSummary = "Aksi {$actionType} selesai dieksekusi.";
        }

        $execution = AutomationExecution::create([
            'execution_id' => $executionId,
            'policy_id' => $policy->policy_id,
            'trigger_event_id' => $eventId,
            'trigger_incident_id' => $incidentId,
            'action_type' => $actionType,
            'status' => 'EXECUTED',
            'result_summary' => $resultSummary,
            'execution_depth' => $depth,
        ]);

        AuditService::log([
            'action' => 'AUTOMATION_EXECUTED',
            'category' => 'AUTOMATION',
            'target_type' => 'POLICY',
            'target_id' => $policy->policy_id,
            'source' => 'SYSTEM',
            'status' => 'SUCCESS',
            'metadata' => [
                'execution_id' => $execution->execution_id,
                'action_type' => $actionType,
                'result_summary' => $resultSummary,
            ],
        ]);

        return $execution;
    }

    /**
     * Admin staff approval of a pending execution.
     */
    public static function approve(AutomationExecution $execution, User $staff): bool
    {
        if ($execution->status !== 'PENDING_APPROVAL') {
            return false;
        }

        $policy = $execution->policy;
        if (!$policy) {
            return false;
        }

        $execution->update([
            'status' => 'APPROVED',
            'approved_by' => (string) $staff->id,
            'approved_at' => Carbon::now(),
        ]);

        // Execute action
        $payload = $policy->action_payload ?? [];
        $pluginId = $payload['plugin_id'] ?? 'apexsions-core';
        $actionName = $payload['action_name'] ?? 'core.reload';

        $actionResult = PluginActionGateway::executeAction(
            $pluginId,
            $actionName,
            [],
            $staff,
            "Approved execution {$execution->execution_id} by {$staff->name}"
        );

        $success = ($actionResult['success'] ?? false);
        $execution->update([
            'status' => $success ? 'EXECUTED' : 'FAILED',
            'result_summary' => $success
                ? "Disetujui oleh {$staff->name}. Aksi diproses dengan Action ID {$actionResult['action_id']}."
                : "Eksekusi gagal: " . ($actionResult['error'] ?? 'Unknown'),
        ]);

        AuditService::log([
            'action' => 'AUTOMATION_APPROVED',
            'category' => 'AUTOMATION',
            'target_type' => 'EXECUTION',
            'target_id' => $execution->execution_id,
            'staff_id' => (string) $staff->id,
            'staff_name' => $staff->name,
            'source' => 'WEB',
            'status' => $success ? 'SUCCESS' : 'FAILED',
            'metadata' => ['policy_id' => $policy->policy_id, 'action_type' => $policy->action_type],
        ]);

        return true;
    }

    /**
     * Admin staff rejection of a pending execution.
     */
    public static function reject(AutomationExecution $execution, User $staff, string $reason): bool
    {
        if ($execution->status !== 'PENDING_APPROVAL') {
            return false;
        }

        $execution->update([
            'status' => 'REJECTED',
            'approved_by' => (string) $staff->id,
            'approved_at' => Carbon::now(),
            'rejection_reason' => $reason,
            'result_summary' => "Ditolak oleh {$staff->name}: {$reason}",
        ]);

        AuditService::log([
            'action' => 'AUTOMATION_REJECTED',
            'category' => 'AUTOMATION',
            'target_type' => 'EXECUTION',
            'target_id' => $execution->execution_id,
            'staff_id' => (string) $staff->id,
            'staff_name' => $staff->name,
            'source' => 'WEB',
            'status' => 'REJECTED',
            'metadata' => ['reason' => $reason],
        ]);

        return true;
    }

    /**
     * Match incoming event against policy trigger configuration.
     */
    protected static function matchesEventTrigger(AutomationPolicy $policy, ApexsionsEvent $event): bool
    {
        if ($policy->trigger_type === 'SERVER_OFFLINE' && $event->event_type === 'SERVER_OFFLINE') {
            return true;
        }

        if ($policy->trigger_type === 'PLUGIN_STATUS_CHANGE' && $event->event_type === 'PLUGIN_STATUS_CHANGE') {
            $schema = $policy->condition_schema ?? [];
            if (!empty($schema['status'])) {
                return strtoupper($event->metadata['status'] ?? '') === strtoupper($schema['status']);
            }
            return true;
        }

        if ($policy->trigger_type === 'PLUGIN_DEGRADED' && in_array($event->event_type, ['PLUGIN_ERROR', 'PLUGIN_DEGRADED'], true)) {
            $schema = $policy->condition_schema ?? [];
            if (!empty($schema['plugin_id'])) {
                $eventPlugin = $event->metadata['plugin_id'] ?? $event->entity_id;
                return $eventPlugin === $schema['plugin_id'];
            }
            return true;
        }

        if ($policy->trigger_type === 'EVENT_SEVERITY' && strtoupper($event->severity) === strtoupper($policy->severity)) {
            return true;
        }

        return false;
    }
}
