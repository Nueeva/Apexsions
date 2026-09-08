<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Azuriom\Plugin\ApexsionsBridge\Models\ReportNote;
use Illuminate\Support\Facades\DB;

class ReportService
{
    /**
     * Atomically claim a report for a staff member.
     * Guaranteed safe against concurrent double-claim race conditions.
     *
     * @return array ['success' => bool, 'message' => string]
     */
    public static function claimReport(Report $report, User $staff): array
    {
        return DB::transaction(function () use ($report, $staff) {
            // Atomic conditional update ensures only one concurrent claim succeeds
            $affected = Report::where('id', $report->id)
                ->where(function ($q) {
                    $q->whereNull('assigned_staff_id')
                      ->whereIn('status', ['OPEN', 'REVIEWING']);
                })
                ->update([
                    'status' => 'CLAIMED',
                    'assigned_staff_id' => $staff->id,
                    'assigned_staff_name' => $staff->name,
                    'assigned_at' => now(),
                    'updated_at' => now(),
                ]);

            if ($affected === 0) {
                $fresh = Report::find($report->id);
                $claimer = $fresh ? ($fresh->assigned_staff_name ?? 'staf lain') : 'staf lain';
                return [
                    'success' => false,
                    'message' => "Laporan #{$report->id} sudah diklaim lebih dulu oleh {$claimer}.",
                ];
            }

            // Record Unified Audit Log
            AuditService::log([
                'actor_type' => 'STAFF',
                'actor_id' => (string) $staff->id,
                'actor_name' => $staff->name,
                'action' => 'REPORT_CLAIMED',
                'target_type' => 'REPORT',
                'target_id' => (string) $report->id,
                'target_name' => "Report #{$report->id} ({$report->reported_name})",
                'old_value' => ['status' => $report->status, 'assigned_staff' => null],
                'new_value' => ['status' => 'CLAIMED', 'assigned_staff' => $staff->name],
                'reason' => "Staff {$staff->name} mengklaim penanganan laporan #{$report->id}",
                'source' => 'WEB',
                'status' => 'SUCCESS',
            ]);

            return [
                'success' => true,
                'message' => "Berhasil mengklaim penanganan Laporan #{$report->id}.",
            ];
        });
    }

    /**
     * Assign report to a specific staff member.
     */
    public static function assignReport(Report $report, User $targetStaff, User $actor): void
    {
        $oldStaff = $report->assigned_staff_name ?? 'None';

        $report->update([
            'status' => 'CLAIMED',
            'assigned_staff_id' => $targetStaff->id,
            'assigned_staff_name' => $targetStaff->name,
            'assigned_at' => now(),
        ]);

        AuditService::log([
            'actor_type' => 'STAFF',
            'actor_id' => (string) $actor->id,
            'actor_name' => $actor->name,
            'action' => 'REPORT_ASSIGNED',
            'target_type' => 'REPORT',
            'target_id' => (string) $report->id,
            'target_name' => "Report #{$report->id}",
            'old_value' => ['assigned_staff' => $oldStaff],
            'new_value' => ['assigned_staff' => $targetStaff->name],
            'reason' => "Laporan ditugaskan ke {$targetStaff->name} oleh {$actor->name}",
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);
    }

    /**
     * Transition report status (OPEN, CLAIMED, INVESTIGATING, RESOLVED, DISMISSED).
     */
    public static function updateStatus(Report $report, string $status, ?string $resolution, User $actor): void
    {
        $status = strtoupper($status);
        $oldStatus = $report->status;

        $updates = [
            'status' => $status,
        ];

        if (in_array($status, ['RESOLVED', 'DISMISSED'], true)) {
            $updates['resolution'] = $resolution;
            $updates['resolved_at'] = now();
        }

        $report->update($updates);

        AuditService::log([
            'actor_type' => 'STAFF',
            'actor_id' => (string) $actor->id,
            'actor_name' => $actor->name,
            'action' => 'REPORT_STATUS_CHANGED',
            'target_type' => 'REPORT',
            'target_id' => (string) $report->id,
            'target_name' => "Report #{$report->id}",
            'old_value' => ['status' => $oldStatus],
            'new_value' => ['status' => $status, 'resolution' => $resolution],
            'reason' => $resolution ?? "Status laporan diubah menjadi {$status}",
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);
    }

    /**
     * Add internal staff note to a report.
     */
    public static function addNote(Report $report, string $noteText, User $author): ReportNote
    {
        $note = ReportNote::create([
            'report_id' => $report->id,
            'author_id' => $author->id,
            'author_name' => $author->name,
            'note' => trim($noteText),
        ]);

        AuditService::log([
            'actor_type' => 'STAFF',
            'actor_id' => (string) $author->id,
            'actor_name' => $author->name,
            'action' => 'STAFF_NOTE_CREATED',
            'target_type' => 'REPORT',
            'target_id' => (string) $report->id,
            'target_name' => "Report #{$report->id} Note #{$note->id}",
            'new_value' => ['note' => substr($note->note, 0, 150)],
            'reason' => "Staff menambahkan catatan internal pada laporan #{$report->id}",
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        return $note;
    }

    /**
     * Resolve report with resolution message.
     */
    public static function resolveReport(Report $report, string $resolution, User $actor): void
    {
        self::updateStatus($report, 'RESOLVED', $resolution, $actor);
    }

    /**
     * Dismiss report with reason.
     */
    public static function dismissReport(Report $report, string $reason, User $actor): void
    {
        self::updateStatus($report, 'DISMISSED', $reason, $actor);
    }
}
