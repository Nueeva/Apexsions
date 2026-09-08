<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\EventIntelligenceService;
use Azuriom\Plugin\ApexsionsBridge\Services\IncidentService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class IncidentAdminController extends Controller
{
    /**
     * Display a listing of incidents with filters and pagination.
     */
    public function index(Request $request): View
    {
        $status = $request->input('status', 'all');
        $severity = $request->input('severity', 'all');
        $type = $request->input('type', 'all');
        $search = trim((string) $request->input('q', ''));

        $query = ApexsionsIncident::query();

        if ($status !== 'all' && !empty($status)) {
            if ($status === 'ACTIVE') {
                $query->whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED']);
            } else {
                $query->where('status', $status);
            }
        }

        if ($severity !== 'all' && !empty($severity)) {
            $query->where('severity', $severity);
        }

        if ($type !== 'all' && !empty($type)) {
            $query->where('type', $type);
        }

        if (!empty($search)) {
            $query->where(function ($q) use ($search) {
                $q->where('incident_id', 'like', "%{$search}%")
                  ->orWhere('title', 'like', "%{$search}%")
                  ->orWhere('root_entity_id', 'like', "%{$search}%")
                  ->orWhere('root_entity_name', 'like', "%{$search}%")
                  ->orWhere('assigned_to', 'like', "%{$search}%");
            });
        }

        $incidents = $query->orderByRaw("CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END ASC")
            ->orderBy('last_occurred_at', 'desc')
            ->paginate(15)
            ->withQueryString();

        $counts = [
            'total' => ApexsionsIncident::count(),
            'active' => ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])->count(),
            'critical' => ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])->where('severity', 'CRITICAL')->count(),
            'high' => ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])->where('severity', 'HIGH')->count(),
            'medium' => ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])->where('severity', 'MEDIUM')->count(),
            'resolved' => ApexsionsIncident::whereIn('status', ['RESOLVED', 'CLOSED'])->count(),
        ];

        return view('apexsions-bridge::admin.incidents.index', [
            'incidents' => $incidents,
            'counts' => $counts,
            'selectedStatus' => $status,
            'selectedSeverity' => $severity,
            'selectedType' => $type,
            'search' => $search,
        ]);
    }

    /**
     * Display the specified incident, investigation timeline, and correlated data.
     */
    public function show(Request $request, string $id): View
    {
        $incident = ApexsionsIncident::where('incident_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $timeline = IncidentService::getTimeline($incident);

        // Fetch correlated events from the primary linked event if available
        $primaryEvent = $incident->events()->first();
        $correlatedEvents = $primaryEvent ? EventIntelligenceService::correlate($primaryEvent) : collect();

        AuditService::log([
            'action' => 'INCIDENT_VIEW',
            'target_type' => 'INCIDENT',
            'target_id' => $incident->incident_id,
            'target_name' => $incident->title,
            'reason' => 'Staf membuka lembar investigasi insiden',
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'incident_id' => $incident->incident_id,
                'severity' => $incident->severity,
                'status' => $incident->status,
            ],
        ]);

        return view('apexsions-bridge::admin.incidents.show', [
            'incident' => $incident,
            'timeline' => $timeline,
            'correlatedEvents' => $correlatedEvents,
            'primaryEvent' => $primaryEvent,
        ]);
    }

    /**
     * Assign or unassign a staff member to an incident.
     */
    public function assign(Request $request, string $id): RedirectResponse
    {
        $incident = ApexsionsIncident::where('incident_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $request->validate([
            'staff_name' => 'nullable|string|max:64',
            'reason' => 'nullable|string|max:255',
        ]);

        IncidentService::assignStaff(
            $incident,
            $request->input('staff_name'),
            $request->input('reason')
        );

        return redirect()->route('admin.incidents.show', $incident->incident_id)
            ->with('success', "Penugasan staf untuk insiden [{$incident->incident_id}] berhasil diperbarui.");
    }

    /**
     * Update incident lifecycle status.
     */
    public function status(Request $request, string $id): RedirectResponse
    {
        $incident = ApexsionsIncident::where('incident_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $request->validate([
            'status' => 'required|string|in:OPEN,INVESTIGATING,MITIGATED,RESOLVED,CLOSED',
            'reason' => 'nullable|string|max:500',
        ]);

        $newStatus = strtoupper($request->input('status'));

        IncidentService::updateStatus(
            $incident,
            $newStatus,
            $request->input('reason')
        );

        return redirect()->route('admin.incidents.show', $incident->incident_id)
            ->with('success', "Status insiden [{$incident->incident_id}] berhasil diperbarui ke {$newStatus}.");
    }

    /**
     * Add a staff investigation note to an incident.
     */
    public function note(Request $request, string $id): RedirectResponse
    {
        $incident = ApexsionsIncident::where('incident_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $request->validate([
            'content' => 'required|string|min:3|max:2000',
            'related_event_id' => 'nullable|string|max:64',
        ]);

        IncidentService::addNote(
            $incident,
            $request->input('content'),
            null,
            null,
            $request->input('related_event_id')
        );

        return redirect()->route('admin.incidents.show', $incident->incident_id)
            ->with('success', "Catatan investigasi berhasil dicatat ke dalam berkas insiden.");
    }
}
