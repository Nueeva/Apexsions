<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\IntelligenceRule;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

class AnomalyDetectionEngine
{
    /**
     * Seed default explicit intelligence rules.
     */
    public static function seedDefaultRules(): void
    {
        $defaultRules = [
            'RULE_ECO_LARGE_TRANSFER' => [
                'name' => 'Transfer Rupiah Bernilai Ekstrem',
                'category' => 'ECONOMY',
                'description' => 'Mendeteksi mutasi transfer saldo rupiah yang melebihi batas wajar (> 50.000.000 Rp).',
                'severity' => 'HIGH',
                'condition_schema' => ['threshold_amount' => 50000000, 'currency' => 'rupiah'],
                'cooldown_minutes' => 10,
            ],
            'RULE_ECO_RAPID_TRANSACTIONS' => [
                'name' => 'Lonjakan Frekuensi Transaksi Pemain Tunggal',
                'category' => 'ECONOMY',
                'description' => 'Mendeteksi pemain yang melakukan lebih dari 5 transaksi transfer dalam kurun waktu 3 menit.',
                'severity' => 'HIGH',
                'condition_schema' => ['max_transfers' => 5, 'window_minutes' => 3],
                'cooldown_minutes' => 15,
            ],
            'RULE_SRV_LOW_TPS' => [
                'name' => 'Penurunan Performa TPS Kritis',
                'category' => 'SERVER',
                'description' => 'Mendeteksi server tick rate yang anjlok di bawah batas stabil kritis (< 15.0 TPS).',
                'severity' => 'CRITICAL',
                'condition_schema' => ['tps_threshold' => 15.0],
                'cooldown_minutes' => 15,
            ],
            'RULE_SRV_BRIDGE_TIMEOUT' => [
                'name' => 'Bridge Queue Delayed / Disconnected',
                'category' => 'BRIDGE',
                'description' => 'Mendeteksi antrean sinkronisasi instruksi bridge yang tertunda lebih dari 120 detik.',
                'severity' => 'HIGH',
                'condition_schema' => ['queue_latency_seconds' => 120],
                'cooldown_minutes' => 15,
            ],
            'RULE_PLG_DEGRADED' => [
                'name' => 'Custom Plugin Shift to ERROR / DISABLED',
                'category' => 'PLUGIN',
                'description' => 'Mendeteksi pergeseran kondisi runtime modul custom Apexsions ke status cacat atau nonaktif.',
                'severity' => 'CRITICAL',
                'condition_schema' => ['unhealthy_statuses' => ['ERROR', 'DISABLED']],
                'cooldown_minutes' => 20,
            ],
            'RULE_MOD_REPEATED_REPORTS' => [
                'name' => 'Pemain Menerima Laporan Berulang',
                'category' => 'MODERATION',
                'description' => 'Mendeteksi pemain yang memiliki 3 atau lebih laporan pelanggaran aktif yang belum selesai.',
                'severity' => 'MEDIUM',
                'condition_schema' => ['open_reports_threshold' => 3],
                'cooldown_minutes' => 30,
            ],
        ];

        foreach ($defaultRules as $ruleId => $def) {
            IntelligenceRule::firstOrCreate(
                ['rule_id' => $ruleId],
                [
                    'name' => $def['name'],
                    'category' => $def['category'],
                    'description' => $def['description'],
                    'severity' => $def['severity'],
                    'condition_schema' => $def['condition_schema'],
                    'cooldown_minutes' => $def['cooldown_minutes'],
                    'is_enabled' => true,
                ]
            );
        }
    }

    /**
     * Evaluate an incoming event against explicit active rules to generate or deduplicate incidents.
     */
    public static function evaluate(ApexsionsEvent $event): ?ApexsionsIncident
    {
        if (IntelligenceRule::count() === 0) {
            self::seedDefaultRules();
        }

        $rules = IntelligenceRule::enabled()->get();

        foreach ($rules as $rule) {
            $match = false;
            $title = '';
            $incidentType = $rule->category;
            $rootEntityType = $event->entity_type;
            $rootEntityId = $event->entity_id;
            $rootEntityName = $event->target_name ?: ($event->actor_name ?: $event->entity_id);
            $summary = '';

            $schema = $rule->condition_schema ?? [];

            // 1. Evaluate Economy Rules
            if ($rule->rule_id === 'RULE_ECO_LARGE_TRANSFER' && $event->event_type === 'ECONOMY_TRANSACTION') {
                $amount = (float) ($event->metadata['amount'] ?? 0);
                $currency = strtolower($event->metadata['currency'] ?? '');
                $threshold = (float) ($schema['threshold_amount'] ?? 50000000);

                if ($amount >= $threshold && $currency === 'rupiah') {
                    $match = true;
                    $title = sprintf("Mutasi Rupiah Ekstrem: Rp %s dari %s", number_format($amount, 0, ',', '.'), $event->actor_name ?? 'Unknown');
                    $summary = "Transfer bernilai Rp " . number_format($amount, 0, ',', '.') . " melewati ambang batas pemantauan Rp " . number_format($threshold, 0, ',', '.') . ".";
                }
            } elseif ($rule->rule_id === 'RULE_ECO_RAPID_TRANSACTIONS' && $event->event_type === 'ECONOMY_TRANSACTION') {
                $maxTransfers = (int) ($schema['max_transfers'] ?? 5);
                $windowMinutes = (int) ($schema['window_minutes'] ?? 3);

                if (!empty($event->actor_id)) {
                    $recentCount = ApexsionsEvent::where('event_type', 'ECONOMY_TRANSACTION')
                        ->where('actor_id', $event->actor_id)
                        ->where('occurred_at', '>=', Carbon::now()->subMinutes($windowMinutes))
                        ->count();

                    if ($recentCount >= $maxTransfers) {
                        $match = true;
                        $title = sprintf("Frekuensi Transaksi Mencurigakan: %s (%d transfer / %d m)", $event->actor_name ?? 'Player', $recentCount, $windowMinutes);
                        $summary = "Pemain melakukan {$recentCount} transfer saldo dalam waktu kurang dari {$windowMinutes} menit.";
                    }
                }
            }

            // 2. Evaluate Server Rules
            elseif ($rule->rule_id === 'RULE_SRV_LOW_TPS' && $event->event_type === 'SERVER_ALERT') {
                if (($event->metadata['alert_type'] ?? '') === 'LOW_TPS') {
                    $tps = (float) ($event->metadata['tps'] ?? 0);
                    $threshold = (float) ($schema['tps_threshold'] ?? 15.0);

                    if ($tps <= $threshold && $tps > 0) {
                        $match = true;
                        $title = sprintf("Penurunan Tick Rate Kritis: TPS %.1f", $tps);
                        $summary = "Server telemetry melaporkan TPS berada di angka {$tps}, di bawah batas kritis {$threshold}.";
                    }
                }
            } elseif ($rule->rule_id === 'RULE_SRV_BRIDGE_TIMEOUT' && $event->event_type === 'SERVER_ALERT') {
                if (($event->metadata['alert_type'] ?? '') === 'BRIDGE_DELAYED') {
                    $match = true;
                    $title = "Antrean WebBridge Mengalami Delay / Timeout";
                    $summary = "Instruksi sinkronisasi bridge tertunda lebih lama dari ambang toleransi.";
                }
            }

            // 3. Evaluate Plugin Health Rules
            elseif ($rule->rule_id === 'RULE_PLG_DEGRADED' && $event->event_type === 'PLUGIN_HEALTH_SHIFT') {
                $healthStatus = strtoupper($event->metadata['health_status'] ?? '');
                $unhealthy = $schema['unhealthy_statuses'] ?? ['ERROR', 'DISABLED'];

                if (in_array($healthStatus, $unhealthy)) {
                    $match = true;
                    $title = sprintf("Gangguan Modul Plugin: %s (%s)", $event->entity_id, $healthStatus);
                    $summary = "Status kesehatan plugin {$event->entity_id} bergeser ke kondisi abnormal ({$healthStatus}).";
                }
            }

            // 4. Evaluate Moderation Rules
            elseif ($rule->rule_id === 'RULE_MOD_REPEATED_REPORTS' && $event->event_type === 'PLAYER_REPORTED') {
                $targetId = $event->target_id;
                $threshold = (int) ($schema['open_reports_threshold'] ?? 3);

                if ($targetId) {
                    $openCount = ApexsionsEvent::where('event_type', 'PLAYER_REPORTED')
                        ->where('target_id', $targetId)
                        ->where('occurred_at', '>=', Carbon::now()->subHours(24))
                        ->count();

                    if ($openCount >= $threshold) {
                        $match = true;
                        $title = sprintf("Akumulasi Laporan Berulang: %s (%d laporan terbuka)", $event->target_name ?? 'Player', $openCount);
                        $summary = "Pemain telah dilaporkan sebanyak {$openCount} kali dalam kurun waktu 24 jam.";
                    }
                }
            }

            if ($match) {
                // 1. Incident Deduplication: Merge into active open incident if one exists for the same root entity
                $existingIncident = ApexsionsIncident::where('root_entity_id', $rootEntityId)
                    ->where('type', $incidentType)
                    ->whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
                    ->first();

                if ($existingIncident) {
                    $existingIncident->increment('occurrence_count');
                    $newSeverity = self::escalateSeverity(
                        $existingIncident->severity,
                        self::escalateSeverity($rule->severity, $event->severity)
                    );
                    $existingIncident->update([
                        'last_occurred_at' => Carbon::now(),
                        'severity' => $newSeverity,
                    ]);

                    $event->update(['incident_id' => $existingIncident->incident_id]);

                    return $existingIncident;
                }

                // 2. Check Cooldown to prevent spamming creation of new incidents
                $cooldownKey = "apexsions.rule_cd.{$rule->rule_id}.{$rootEntityId}";
                if (Cache::has($cooldownKey)) {
                    continue;
                }

                // 3. Create New Incident
                $incidentId = 'INC-' . date('Ymd') . '-' . strtoupper(Str::random(4));
                $initialSeverity = self::escalateSeverity($rule->severity, $event->severity);
                $incident = ApexsionsIncident::create([
                    'incident_id' => $incidentId,
                    'title' => $title,
                    'type' => $incidentType,
                    'severity' => $initialSeverity,
                    'status' => 'OPEN',
                    'source' => 'RULE_ENGINE',
                    'detected_at' => Carbon::now(),
                    'root_entity_type' => $rootEntityType,
                    'root_entity_id' => (string) $rootEntityId,
                    'root_entity_name' => $rootEntityName,
                    'correlation_summary' => $summary,
                    'occurrence_count' => 1,
                    'last_occurred_at' => Carbon::now(),
                    'metadata' => [
                        'triggered_by_rule' => $rule->rule_id,
                        'rule_name' => $rule->name,
                        'initial_event_id' => $event->event_id,
                    ],
                ]);

                $event->update(['incident_id' => $incident->incident_id]);
                Cache::put($cooldownKey, true, Carbon::now()->addMinutes($rule->cooldown_minutes));

                return $incident;
            }
        }

        return null;
    }

    /**
     * Escalate severity level if higher severity event is detected.
     */
    protected static function escalateSeverity(string $current, string $incoming): string
    {
        $weight = ['INFO' => 0, 'LOW' => 1, 'MEDIUM' => 2, 'HIGH' => 3, 'CRITICAL' => 4];
        $currentWeight = $weight[strtoupper($current)] ?? 1;
        $incomingWeight = $weight[strtoupper($incoming)] ?? 1;

        return $incomingWeight > $currentWeight ? strtoupper($incoming) : strtoupper($current);
    }
}
