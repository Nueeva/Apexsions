<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Carbon\Carbon;
use Illuminate\Support\Collection;
use Illuminate\Support\Str;

class EventIntelligenceService
{
    /**
     * Ingest and normalize an event into the unified event ledger.
     */
    public static function ingest(array $data): ApexsionsEvent
    {
        $eventId = $data['event_id'] ?? ('evt_' . Str::uuid());
        $now = Carbon::now();

        $event = ApexsionsEvent::create([
            'event_id' => $eventId,
            'event_type' => strtoupper($data['event_type'] ?? 'GENERIC_EVENT'),
            'source' => strtoupper($data['source'] ?? 'SYSTEM'),
            'entity_type' => strtoupper($data['entity_type'] ?? 'PLAYER'),
            'entity_id' => (string) ($data['entity_id'] ?? 'SYSTEM'),
            'actor_type' => strtoupper($data['actor_type'] ?? 'SYSTEM'),
            'actor_id' => isset($data['actor_id']) ? (string) $data['actor_id'] : null,
            'actor_name' => $data['actor_name'] ?? null,
            'target_type' => isset($data['target_type']) ? strtoupper($data['target_type']) : null,
            'target_id' => isset($data['target_id']) ? (string) $data['target_id'] : null,
            'target_name' => $data['target_name'] ?? null,
            'severity' => strtoupper($data['severity'] ?? 'INFO'),
            'correlation_id' => $data['correlation_id'] ?? null,
            'action_id' => $data['action_id'] ?? null,
            'incident_id' => $data['incident_id'] ?? null,
            'metadata' => $data['metadata'] ?? [],
            'occurred_at' => isset($data['occurred_at']) ? Carbon::parse($data['occurred_at']) : $now,
            'received_at' => $now,
        ]);

        // Evaluate against explicit rule engine for anomaly detection
        AnomalyDetectionEngine::evaluate($event);

        return $event;
    }

    /**
     * Find correlated events for a given target event, classified by confidence.
     */
    public static function correlate(ApexsionsEvent $event): Collection
    {
        $results = collect();
        $seenIds = [$event->id];

        // 1. STRONG Correlation: Shared authoritative identifier (action_id or correlation_id)
        if (!empty($event->action_id)) {
            $strongByAction = ApexsionsEvent::where('action_id', $event->action_id)
                ->whereNotIn('id', $seenIds)
                ->get();

            foreach ($strongByAction as $e) {
                $results->push([
                    'event' => $e,
                    'confidence' => 'STRONG',
                    'reason' => "Identifier unik administratif (Action ID: {$event->action_id}) identik.",
                ]);
                $seenIds[] = $e->id;
            }
        }

        if (!empty($event->correlation_id)) {
            $strongByCorr = ApexsionsEvent::where('correlation_id', $event->correlation_id)
                ->whereNotIn('id', $seenIds)
                ->get();

            foreach ($strongByCorr as $e) {
                $results->push([
                    'event' => $e,
                    'confidence' => 'STRONG',
                    'reason' => "Correlation ID identik ({$event->correlation_id}).",
                ]);
                $seenIds[] = $e->id;
            }
        }

        // 2. MEDIUM Correlation: Domain-Aware Contextual Window for Shared Entity ID
        if (!empty($event->entity_id) && $event->entity_id !== 'SYSTEM' && $event->entity_id !== 'SERVER') {
            $windowHours = self::getDomainCorrelationWindowHours($event->event_type);
            $domainLabel = self::getDomainLabel($event->event_type);

            $mediumEvents = ApexsionsEvent::where(function ($q) use ($event) {
                    $q->where('entity_id', $event->entity_id)
                      ->orWhere('target_id', $event->entity_id)
                      ->orWhere('actor_id', $event->entity_id);
                })
                ->whereNotIn('id', $seenIds)
                ->where('occurred_at', '>=', $event->occurred_at->copy()->subHours($windowHours))
                ->where('occurred_at', '<=', $event->occurred_at->copy()->addHours($windowHours))
                ->take(15)
                ->get();

            foreach ($mediumEvents as $e) {
                $results->push([
                    'event' => $e,
                    'confidence' => 'MEDIUM',
                    'reason' => "[Domain: {$domainLabel}] Entitas terlibat sama ({$event->entity_id}) dalam jendela kontekstual {$windowHours} jam.",
                ]);
                $seenIds[] = $e->id;
            }
        }

        // 3. WEAK Correlation: Temporal proximity only (+/- 5 minutes)
        $weakEvents = ApexsionsEvent::whereNotIn('id', $seenIds)
            ->where('occurred_at', '>=', $event->occurred_at->copy()->subMinutes(5))
            ->where('occurred_at', '<=', $event->occurred_at->copy()->addMinutes(5))
            ->take(5)
            ->get();

        foreach ($weakEvents as $e) {
            $results->push([
                'event' => $e,
                'confidence' => 'WEAK',
                'reason' => "Kedekatan jendela waktu (+/- 5 menit). Tidak boleh dijadikan bukti konklusif.",
            ]);
            $seenIds[] = $e->id;
        }

        return $results;
    }

    /**
     * Resolve contextual correlation window duration in hours based on domain event type.
     */
    public static function getDomainCorrelationWindowHours(string $eventType): int
    {
        $eventType = strtoupper($eventType);

        if (str_starts_with($eventType, 'ECONOMY_') || str_starts_with($eventType, 'AUCTION_') || str_starts_with($eventType, 'TREASURY_')) {
            return 2; // Economy burst window
        }

        if (str_starts_with($eventType, 'PLAYER_REPORTED') || str_starts_with($eventType, 'PUNISHMENT_') || str_starts_with($eventType, 'REPORT_')) {
            return 24; // Moderation pattern window
        }

        if (str_starts_with($eventType, 'SERVER_') || str_starts_with($eventType, 'PLUGIN_') || str_starts_with($eventType, 'BRIDGE_')) {
            return 1; // Infrastructure crash / degradation window
        }

        return 6; // General player / system event default window
    }

    /**
     * Resolve human-readable domain label for correlation explanations.
     */
    public static function getDomainLabel(string $eventType): string
    {
        $eventType = strtoupper($eventType);

        if (str_starts_with($eventType, 'ECONOMY_') || str_starts_with($eventType, 'AUCTION_') || str_starts_with($eventType, 'TREASURY_')) {
            return 'ECONOMY';
        }

        if (str_starts_with($eventType, 'PLAYER_REPORTED') || str_starts_with($eventType, 'PUNISHMENT_') || str_starts_with($eventType, 'REPORT_')) {
            return 'MODERATION';
        }

        if (str_starts_with($eventType, 'SERVER_') || str_starts_with($eventType, 'PLUGIN_') || str_starts_with($eventType, 'BRIDGE_')) {
            return 'SERVER';
        }

        return 'GENERAL';
    }
}
