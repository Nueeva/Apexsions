<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class AuditLogController extends Controller
{
    /**
     * Display a listing of audit logs with filters and search.
     */
    public function index(Request $request): View
    {
        $search = $request->input('search');
        $action = $request->input('action');
        $source = $request->input('source');
        $status = $request->input('status');
        $from = $request->input('from');
        $to = $request->input('to');

        $logs = AuditLog::query()
            ->search($search)
            ->filterAction($action)
            ->filterSource($source)
            ->filterStatus($status)
            ->filterDate($from, $to)
            ->orderBy('id', 'desc')
            ->paginate(25)
            ->withQueryString();

        // Get distinct action types and sources for filter dropdowns
        $availableActions = AuditLog::select('action')->distinct()->pluck('action');
        $availableSources = ['WEB', 'INGAME', 'API', 'SYSTEM'];
        $availableStatuses = ['SUCCESS', 'PENDING', 'FAILED'];

        return view('apexsions-bridge::admin.audit-logs.index', [
            'logs' => $logs,
            'search' => $search,
            'selectedAction' => $action,
            'selectedSource' => $source,
            'selectedStatus' => $status,
            'selectedFrom' => $from,
            'selectedTo' => $to,
            'availableActions' => $availableActions,
            'availableSources' => $availableSources,
            'availableStatuses' => $availableStatuses,
        ]);
    }

    /**
     * Get specific audit log detail as JSON for the detail modal.
     */
    public function show(int $id): JsonResponse
    {
        $log = AuditLog::findOrFail($id);

        return response()->json([
            'status' => 'success',
            'data' => $log,
        ]);
    }
}
