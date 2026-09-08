<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Carbon\Carbon;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\View\View;

class NotificationAdminController extends Controller
{
    /**
     * Display listing of admin notifications.
     */
    public function index(Request $request): View
    {
        $status = $request->input('status', 'UNACKNOWLEDGED');
        $severity = $request->input('severity', 'all');
        $type = $request->input('type', 'all');
        $search = trim((string) $request->input('q', ''));

        $query = ApexsionsNotification::query();

        if ($status !== 'all' && !empty($status)) {
            $query->where('status', $status);
        }

        if ($severity !== 'all' && !empty($severity)) {
            $query->where('severity', $severity);
        }

        if ($type !== 'all' && !empty($type)) {
            $query->where('type', $type);
        }

        if (!empty($search)) {
            $query->where(function ($q) use ($search) {
                $q->where('notification_id', 'like', "%{$search}%")
                  ->orWhere('title', 'like', "%{$search}%")
                  ->orWhere('message', 'like', "%{$search}%")
                  ->orWhere('entity_id', 'like', "%{$search}%");
            });
        }

        $notifications = $query->orderByRaw("CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END ASC")
            ->orderBy('last_occurred_at', 'desc')
            ->paginate(20)
            ->withQueryString();

        $stats = [
            'unacknowledged' => ApexsionsNotification::where('status', 'UNACKNOWLEDGED')->count(),
            'critical' => ApexsionsNotification::where('severity', 'CRITICAL')->where('status', 'UNACKNOWLEDGED')->count(),
            'high' => ApexsionsNotification::where('severity', 'HIGH')->where('status', 'UNACKNOWLEDGED')->count(),
            'total' => ApexsionsNotification::count(),
        ];

        return view('apexsions-bridge::admin.notifications.index', [
            'notifications' => $notifications,
            'stats' => $stats,
            'currentStatus' => $status,
            'currentSeverity' => $severity,
            'currentType' => $type,
            'search' => $search,
        ]);
    }

    /**
     * Display detailed notification dossier.
     */
    public function show(string $id): View
    {
        $notification = ApexsionsNotification::where('notification_id', $id)
            ->orWhere('id', $id)
            ->with(['deliveries', 'incident', 'event'])
            ->firstOrFail();

        return view('apexsions-bridge::admin.notifications.show', [
            'notification' => $notification,
        ]);
    }

    /**
     * Acknowledge an alert notification.
     */
    public function acknowledge(string $id, Request $request): RedirectResponse
    {
        $notification = ApexsionsNotification::where('notification_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $user = Auth::user();
        $staffId = $user ? (string) $user->id : 'SYSTEM';
        $staffName = $user ? $user->name : 'System Admin';

        $notification->update([
            'status' => 'ACKNOWLEDGED',
            'acknowledged_by' => $staffName,
            'acknowledged_at' => Carbon::now(),
        ]);

        AuditService::log([
            'action' => 'NOTIFICATION_ACKNOWLEDGED',
            'category' => 'NOTIFICATION',
            'target_type' => 'NOTIFICATION',
            'target_id' => $notification->notification_id,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => [
                'type' => $notification->type,
                'severity' => $notification->severity,
                'incident_id' => $notification->incident_id,
            ],
        ]);

        return redirect()->back()->with('success', "Notifikasi {$notification->notification_id} berhasil ditandai telah diakui (Acknowledged).");
    }
}
