<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\MaintenanceState;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerAlert;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class ServerAdminController extends Controller
{
    /**
     * Display the primary Server Operations Dashboard.
     */
    public function index(): View
    {
        $serverStatus = ServerOpsService::getServerStatus();
        $metrics = ServerOpsService::getHealthMetrics();
        $bridgeHealth = ServerOpsService::getBridgeHealth();
        $maintenance = MaintenanceState::current();
        $activeAlerts = ServerAlert::unresolved()->orderBy('created_at', 'desc')->get();
        $recentActions = AuditLog::where('action', 'LIKE', 'SERVER_%')
            ->orderBy('created_at', 'desc')
            ->take(5)
            ->get();

        return view('apexsions-bridge::admin.server.index', [
            'serverStatus' => $serverStatus,
            'metrics' => $metrics,
            'bridgeHealth' => $bridgeHealth,
            'maintenance' => $maintenance,
            'activeAlerts' => $activeAlerts,
            'recentActions' => $recentActions,
            'allowedActions' => ServerOpsService::ALLOWED_ACTIONS,
            'mapUrl' => ServerMapService::getMapUrl(),
            'mapEnabled' => ServerMapService::isMapEnabled(),
            'mapHealthCheck' => ServerMapService::isHealthCheckEnabled(),
            'mapNavVisible' => ServerMapService::isNavigationVisible(),
            'mapOnline' => ServerMapService::isMapOnline(),
        ]);
    }

    /**
     * Display the Server Action History (Audit Log subset).
     */
    public function actions(): View
    {
        $actions = AuditLog::where('action', 'LIKE', 'SERVER_%')
            ->orderBy('created_at', 'desc')
            ->paginate(20);

        return view('apexsions-bridge::admin.server.actions', [
            'actions' => $actions,
        ]);
    }

    /**
     * Display Plugin Status Monitoring and capabilities matrix.
     */
    public function plugins(): View
    {
        $plugins = ServerOpsService::getPluginCatalog();
        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.server.plugins', [
            'plugins' => $plugins,
            'serverStatus' => $serverStatus,
        ]);
    }

    /**
     * Display Historical Telemetry Metrics.
     */
    public function metrics(Request $request): View
    {
        $hours = (int) $request->input('hours', 24);
        $hours = in_array($hours, [6, 12, 24, 48, 72], true) ? $hours : 24;

        $snapshots = ServerMetric::recent($hours)->get();
        $currentMetrics = ServerOpsService::getHealthMetrics();
        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.server.metrics', [
            'snapshots' => $snapshots,
            'currentMetrics' => $currentMetrics,
            'serverStatus' => $serverStatus,
            'selectedHours' => $hours,
        ]);
    }

    /**
     * Execute a safe, typed server operation.
     */
    public function executeAction(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'action_type' => ['required', 'string', 'in:BROADCAST,SAVE_WORLD,CLEAR_ITEMS,RELOAD_CONFIG,RELOAD_PLUGIN,CHAT_MUTE,CHAT_CLEAR,AH_CLEAR,TOGGLE_WAR'],
            'reason' => ['nullable', 'string', 'max:255'],
            'message' => ['nullable', 'string', 'max:256'],
            'plugin_name' => ['nullable', 'string', 'max:64'],
        ]);

        $params = [];
        if (!empty($validated['message'])) {
            $params['message'] = $validated['message'];
        }
        if (!empty($validated['plugin_name'])) {
            $params['plugin_name'] = $validated['plugin_name'];
        }

        $result = ServerOpsService::executeSafeAction(
            $validated['action_type'],
            $params,
            auth()->user(),
            $validated['reason'] ?? ''
        );

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }

    /**
     * Toggle Maintenance Mode status.
     */
    public function toggleMaintenance(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'is_enabled' => ['nullable'],
            'message' => ['nullable', 'string', 'max:255'],
            'reason' => ['nullable', 'string', 'max:255'],
            'allow_staff' => ['nullable'],
        ]);

        $isEnabled = $request->boolean('is_enabled');

        $result = ServerOpsService::toggleMaintenance(
            $isEnabled,
            $validated['message'] ?? 'Server sedang dalam pemeliharaan berkala.',
            $validated['reason'] ?? null,
            $request->boolean('allow_staff', true),
            auth()->user()
        );

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }

    /**
     * Acknowledge an active server alert.
     */
    public function acknowledgeAlert(int $id): RedirectResponse
    {
        $alert = ServerAlert::findOrFail($id);
        $staffName = auth()->user()?->name ?? 'Staff';

        $alert->acknowledge($staffName);

        return redirect()->back()->with('success', "Peringatan server #{$alert->id} telah dikonfirmasi oleh {$staffName}.");
    }

    /**
     * Resolve an active or acknowledged server alert.
     */
    public function resolveAlert(int $id): RedirectResponse
    {
        $alert = ServerAlert::findOrFail($id);
        $staffName = auth()->user()?->name ?? 'Staff';

        $alert->resolve($staffName);

        return redirect()->back()->with('success', "Peringatan server #{$alert->id} telah ditandai terselesaikan.");
    }

    /**
     * Update Server Map (BlueMap) configuration.
     */
    public function updateMapSettings(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'map_url' => ['required', 'url', 'max:255'],
            'map_enabled' => ['nullable'],
            'map_health_check' => ['nullable'],
            'map_nav_visible' => ['nullable'],
        ]);

        \Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService::updateSettings([
            'map_url' => $validated['map_url'],
            'map_enabled' => $request->boolean('map_enabled'),
            'map_health_check' => $request->boolean('map_health_check'),
            'map_nav_visible' => $request->boolean('map_nav_visible'),
        ]);

        return redirect()->back()->with('success', 'Konfigurasi Server Map (BlueMap) berhasil diperbarui.');
    }
}
