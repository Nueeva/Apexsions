<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Azuriom\Plugin\ApexsionsBridge\Services\ReportService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class ReportAdminController extends Controller
{
    /**
     * Display reports listing with multi-criteria filters and pagination.
     */
    public function index(Request $request): View
    {
        $search = trim((string) $request->input('q', ''));
        $status = $request->input('status', 'all');
        $priority = $request->input('priority', 'all');

        $query = Report::query()->with(['notes', 'assignedStaff']);

        if (!empty($search)) {
            $query->search($search);
        }

        if (!empty($status) && $status !== 'all') {
            $query->filterStatus($status);
        }

        if (!empty($priority) && $priority !== 'all') {
            $query->filterPriority($priority);
        }

        $reports = $query->orderBy('created_at', 'desc')
            ->paginate(15)
            ->withQueryString();

        $openCount = Report::whereIn('status', ['OPEN', 'REVIEWING'])->count();
        $claimedCount = Report::where('status', 'CLAIMED')->count();
        $investigatingCount = Report::where('status', 'INVESTIGATING')->count();
        $resolvedCount = Report::where('status', 'RESOLVED')->count();

        return view('apexsions-bridge::admin.reports.index', [
            'reports' => $reports,
            'search' => $search,
            'selectedStatus' => $status,
            'selectedPriority' => $priority,
            'openCount' => $openCount,
            'claimedCount' => $claimedCount,
            'investigatingCount' => $investigatingCount,
            'resolvedCount' => $resolvedCount,
            'totalCount' => Report::count(),
        ]);
    }

    /**
     * Display specific report details, player context, and staff notes.
     */
    public function show(int $id): View
    {
        $report = Report::with(['notes', 'assignedStaff', 'reportedAccount'])->findOrFail($id);
        $allStaff = User::whereHas('role', function ($q) {
            $q->where('power', '>', 0);
        })->orWhere('is_admin', true)->get();

        return view('apexsions-bridge::admin.reports.show', [
            'report' => $report,
            'allStaff' => $allStaff,
        ]);
    }

    /**
     * Atomically claim a report.
     */
    public function claim(Request $request, int $id): RedirectResponse
    {
        $report = Report::findOrFail($id);
        $result = ReportService::claimReport($report, $request->user());

        if ($result['success']) {
            return redirect()->route('apexsions-bridge.admin.reports.show', $id)
                ->with('success', $result['message']);
        }

        return redirect()->route('apexsions-bridge.admin.reports.show', $id)
            ->with('error', $result['message']);
    }

    /**
     * Assign report to a staff member.
     */
    public function assign(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'staff_id' => ['required', 'exists:users,id'],
        ]);

        $report = Report::findOrFail($id);
        $targetStaff = User::findOrFail($validated['staff_id']);

        ReportService::assignReport($report, $targetStaff, $request->user());

        return redirect()->route('apexsions-bridge.admin.reports.show', $id)
            ->with('success', "Laporan berhasil ditugaskan ke {$targetStaff->name}.");
    }

    /**
     * Update report status (OPEN, CLAIMED, INVESTIGATING, RESOLVED, DISMISSED).
     */
    public function updateStatus(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'status' => ['required', 'in:OPEN,CLAIMED,INVESTIGATING,RESOLVED,DISMISSED'],
            'resolution' => ['nullable', 'string', 'max:1000'],
        ]);

        $report = Report::findOrFail($id);
        ReportService::updateStatus($report, $validated['status'], $validated['resolution'] ?? null, $request->user());

        return redirect()->route('apexsions-bridge.admin.reports.show', $id)
            ->with('success', "Status laporan berhasil diperbarui menjadi {$validated['status']}.");
    }

    /**
     * Add an internal staff note to a report.
     */
    public function addNote(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'note' => ['required', 'string', 'min:3', 'max:2000'],
        ]);

        $report = Report::findOrFail($id);
        ReportService::addNote($report, $validated['note'], $request->user());

        return redirect()->route('apexsions-bridge.admin.reports.show', $id)
            ->with('success', 'Catatan staf berhasil ditambahkan.');
    }
}
