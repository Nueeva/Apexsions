<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\IncidentNote;
use Carbon\Carbon;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\Auth;

class IncidentService
{
    /**
     * Assign or reassign staff member to an incident.
     */
    public static function assignStaff(ApexsionsIncident $incident, ?string $staffName, ?string $reason = null): ApexsionsIncident
    {
        $actor = Auth::user();
        $oldAssigned = $incident->assigned_to;
        $now = Carbon::now();

        $incident->update([
            'assigned_to' => $staffName,
            'assigned_at' => $staffName ? $now : null,
        ]);

        AuditService::log([
            'action' => 'INCIDENT_ASSIGN',
            'target_type' => 'INCIDENT',
            'target_id' => $incident->incident_id,
            'target_name' => $incident->title,
            'old_value' => $oldAssigned,
            'new_value' => $staffName ?: 'UNASSIGNED',
            'reason' => $reason ?: ($staffName ? "Assigned to {$staffName}" : "Staff assignment removed"),
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'incident_id' => $incident->incident_id,
                'staff' => $staffName,
            ],
        ]);

        if (!empty($reason)) {
            self::addNote(
                $incident,
                "Penugasan staff diubah dari [" . ($oldAssigned ?: 'None') . "] ke [" . ($staffName ?: 'Unassigned') . "]. Alasan: {$reason}",
                $actor ? (string) $actor->id : 'SYSTEM',
                $actor ? $actor->name : 'System'
            );
        }

        return $incident;
    }

    /**
     * Update incident lifecycle status.
     */
    public static function updateStatus(ApexsionsIncident $incident, string $status, ?string $reason = null): ApexsionsIncident
    {
        $status = strtoupper($status);
        $validStatuses = ['OPEN', 'INVESTIGATING', 'MITIGATED', 'RESOLVED', 'CLOSED'];

        if (!in_array($status, $validStatuses, true)) {
            throw new \InvalidArgumentException("Status incident tidak valid: {$status}");
        }

        $actor = Auth::user();
        $oldStatus = $incident->status;
        $now = Carbon::now();

        $updateData = ['status' => $status];
        if ($status === 'RESOLVED' && empty($incident->resolved_at)) {
            $updateData['resolved_at'] = $now;
        }

        $incident->update($updateData);

        AuditService::log([
            'action' => 'INCIDENT_STATUS_CHANGE',
            'target_type' => 'INCIDENT',
            'target_id' => $incident->incident_id,
            'target_name' => $incident->title,
            'old_value' => $oldStatus,
            'new_value' => $status,
            'reason' => $reason ?: "Status incident ditransisikan ke {$status}",
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'incident_id' => $incident->incident_id,
                'transition' => "{$oldStatus} -> {$status}",
            ],
        ]);

        if (!empty($reason)) {
            self::addNote(
                $incident,
                "Status ditransisikan dari {$oldStatus} ke {$status}. Catatan: {$reason}",
                $actor ? (string) $actor->id : 'SYSTEM',
                $actor ? $actor->name : 'System'
            );
        }

        return $incident;
    }

    /**
     * Add an internal investigation note to an incident.
     */
    public static function addNote(
        ApexsionsIncident $incident,
        string $content,
        ?string $staffId = null,
        ?string $staffName = null,
        ?string $relatedEventId = null
    ): IncidentNote {
        $actor = Auth::user();
        $staffId = $staffId ?: ($actor ? (string) $actor->id : 'SYSTEM');
        $staffName = $staffName ?: ($actor ? $actor->name : 'System');

        $note = IncidentNote::create([
            'incident_id' => $incident->incident_id,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'content' => trim($content),
            'related_event_id' => $relatedEventId,
        ]);

        AuditService::log([
            'action' => 'INCIDENT_NOTE_ADDED',
            'target_type' => 'INCIDENT',
            'target_id' => $incident->incident_id,
            'target_name' => $incident->title,
            'old_value' => null,
            'new_value' => 'Note #' . $note->id,
            'reason' => 'Catatan investigasi baru ditambahkan',
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'incident_id' => $incident->incident_id,
                'note_id' => $note->id,
                'author' => $staffName,
                'related_event_id' => $relatedEventId,
            ],
        ]);

        return $note;
    }

    /**
     * Compile unified investigation timeline aggregating direct events, correlated events, audit logs, and notes.
     */
    public static function getTimeline(ApexsionsIncident $incident): Collection
    {
        $timeline = collect();

        // 1. Direct events linked to this incident
        $directEvents = ApexsionsEvent::where('incident_id', $incident->incident_id)->get();
        foreach ($directEvents as $event) {
            $timeline->push([
                'id' => 'evt_' . $event->id,
                'type' => 'EVENT',
                'source' => $event->source,
                'category' => $event->event_type,
                'title' => "Event: {$event->event_type}",
                'actor' => $event->actor_name ?: $event->actor_id ?: 'System',
                'target' => $event->target_name ?: $event->target_id,
                'severity' => $event->severity,
                'correlation_confidence' => 'STRONG',
                'details' => $event->metadata,
                'timestamp' => $event->occurred_at,
                'badge_class' => match ($event->severity) {
                    'CRITICAL' => 'badge bg-danger',
                    'HIGH' => 'badge bg-warning text-dark',
                    'MEDIUM' => 'badge bg-primary',
                    default => 'badge bg-info',
                },
            ]);
        }

        // 2. Correlated events for root entity within a 24-hour window
        if (!empty($incident->root_entity_id) && $incident->root_entity_id !== 'SYSTEM') {
            $correlatedEvents = ApexsionsEvent::where('entity_id', $incident->root_entity_id)
                ->where(function ($q) use ($incident) {
                    $q->whereNull('incident_id')
                      ->orWhere('incident_id', '!=', $incident->incident_id);
                })
                ->where('occurred_at', '>=', $incident->detected_at->copy()->subHours(24))
                ->where('occurred_at', '<=', $incident->detected_at->copy()->addHours(24))
                ->take(15)
                ->get();

            foreach ($correlatedEvents as $ce) {
                $timeline->push([
                    'id' => 'cor_evt_' . $ce->id,
                    'type' => 'CORRELATED_EVENT',
                    'source' => $ce->source,
                    'category' => $ce->event_type,
                    'title' => "Terkorelasi: {$ce->event_type} ({$ce->entity_id})",
                    'actor' => $ce->actor_name ?: $ce->actor_id ?: 'System',
                    'target' => $ce->target_name ?: $ce->target_id,
                    'severity' => $ce->severity,
                    'correlation_confidence' => 'MEDIUM',
                    'details' => $ce->metadata,
                    'timestamp' => $ce->occurred_at,
                    'badge_class' => 'badge bg-secondary',
                ]);
            }
        }

        // 3. Incident Investigation Notes
        $notes = $incident->notes()->get();
        foreach ($notes as $note) {
            $timeline->push([
                'id' => 'note_' . $note->id,
                'type' => 'NOTE',
                'source' => 'INVESTIGATOR',
                'category' => 'STAFF_NOTE',
                'title' => "Catatan Investigasi oleh {$note->staff_name}",
                'actor' => $note->staff_name,
                'target' => null,
                'severity' => 'INFO',
                'correlation_confidence' => 'DIRECT',
                'details' => ['content' => $note->content, 'related_event_id' => $note->related_event_id],
                'timestamp' => $note->created_at,
                'badge_class' => 'badge bg-dark',
            ]);
        }

        // 4. Audit Log Entries referencing the incident or root entity
        $auditLogs = AuditLog::where('target_id', $incident->incident_id)
            ->orWhere(function ($q) use ($incident) {
                if (!empty($incident->root_entity_id) && $incident->root_entity_id !== 'SYSTEM') {
                    $q->where('target_id', $incident->root_entity_id);
                }
            })
            ->take(15)
            ->get();

        foreach ($auditLogs as $log) {
            $timeline->push([
                'id' => 'audit_' . $log->id,
                'type' => 'AUDIT',
                'source' => $log->source,
                'category' => $log->action,
                'title' => "Audit: {$log->action}",
                'actor' => $log->actor_name,
                'target' => $log->target_name ?: $log->target_id,
                'severity' => 'INFO',
                'correlation_confidence' => 'STRONG',
                'details' => [
                    'old' => $log->old_value,
                    'new' => $log->new_value,
                    'reason' => $log->reason,
                ],
                'timestamp' => $log->created_at,
                'badge_class' => 'badge bg-light text-dark border',
            ]);
        }

        // Sort descending by timestamp
        return $timeline->sortByDesc('timestamp')->values();
    }
}
