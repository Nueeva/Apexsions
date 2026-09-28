<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Models\Setting;
use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Azuriom\Plugin\ApexsionsBridge\Models\MaintenanceState;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerAlert;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerMetric;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\PterodactylService;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\JsonResponse;
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
            'pterodactylConfigured' => PterodactylService::isConfigured(),
            'pterodactylPanelUrl' => PterodactylService::getPanelUrl(),
            'pterodactylServerId' => PterodactylService::getServerId(),
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

    /**
     * Display the Live Server Console & Terminal.
     */
    public function console(): View
    {
        $serverStatus = ServerOpsService::getServerStatus();
        $isConfigured = PterodactylService::isConfigured();
        $resources = $isConfigured ? PterodactylService::getResources() : [];

        $quickCommands = [
            ['label' => 'TPS & Lag Meter', 'cmd' => 'tps', 'icon' => 'bi-speedometer2', 'class' => 'btn-outline-warning'],
            ['label' => 'Spark Profiler (1m)', 'cmd' => 'spark profiler --timeout 60', 'icon' => 'bi-lightning-charge', 'class' => 'btn-outline-info'],
            ['label' => 'Save World Chunks', 'cmd' => 'save-all', 'icon' => 'bi-save', 'class' => 'btn-outline-success'],
            ['label' => 'List Online Players', 'cmd' => 'list', 'icon' => 'bi-people', 'class' => 'btn-outline-light'],
            ['label' => 'Check Whitelist', 'cmd' => 'whitelist list', 'icon' => 'bi-shield-check', 'class' => 'btn-outline-secondary'],
            ['label' => 'Paper Version', 'cmd' => 'version', 'icon' => 'bi-info-circle', 'class' => 'btn-outline-light'],
            ['label' => 'Clear Ground Items', 'cmd' => 'kill @e[type=item]', 'icon' => 'bi-trash', 'class' => 'btn-outline-danger'],
        ];

        return view('apexsions-bridge::admin.server.console', [
            'serverStatus' => $serverStatus,
            'isConfigured' => $isConfigured,
            'panelUrl' => PterodactylService::getPanelUrl(),
            'serverId' => PterodactylService::getServerId(),
            'resources' => $resources,
            'quickCommands' => $quickCommands,
        ]);
    }

    /**
     * Send a console command via Pterodactyl API.
     */
    public function sendCommand(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'command' => ['required', 'string', 'max:500'],
        ]);

        $cmd = trim($validated['command']);

        $res = PterodactylService::sendCommand($cmd);

        AuditService::log([
            'action' => 'SERVER_CONSOLE_COMMAND',
            'target_type' => 'SERVER',
            'target_name' => 'Minecraft Realm Console',
            'new_value' => $cmd,
            'source' => 'WEB_CONSOLE',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => [
                'command' => $cmd,
                'result' => $res,
            ],
        ]);

        return response()->json($res);
    }

    /**
     * Get WebSocket credentials for live streaming console.
     */
    public function getWebsocketToken(): JsonResponse
    {
        $creds = PterodactylService::getWebsocketCredentials();
        return response()->json($creds);
    }

    /**
     * Send a power signal (start, restart, stop, kill).
     */
    public function sendPowerSignal(Request $request)
    {
        $validated = $request->validate([
            'signal' => ['required', 'string', 'in:start,restart,stop,kill'],
        ]);

        $signal = $validated['signal'];
        $res = PterodactylService::sendPowerSignal($signal);

        AuditService::log([
            'action' => 'SERVER_POWER_' . strtoupper($signal),
            'target_type' => 'SERVER',
            'target_name' => 'Minecraft Realm Server',
            'new_value' => $signal,
            'source' => 'WEB_PANEL',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => [
                'signal' => $signal,
                'result' => $res,
            ],
        ]);

        if ($request->wantsJson()) {
            return response()->json($res);
        }

        if ($res['success']) {
            return redirect()->back()->with('success', "Signal daya [{$signal}] berhasil dikirim ke server hosting.");
        }

        return redirect()->back()->with('error', "Gagal mengirim signal daya: " . ($res['error'] ?? 'Unknown error'));
    }

    /**
     * Display the Pterodactyl Web File Manager.
     */
    public function files(Request $request): View
    {
        $isConfigured = PterodactylService::isConfigured();
        $directory = $request->input('directory', '/');
        // Sanitize directory
        $directory = '/' . ltrim(str_replace(['../', '..\\'], '', $directory), '/');

        $fileData = $isConfigured ? PterodactylService::listFiles($directory) : ['success' => false, 'files' => []];

        return view('apexsions-bridge::admin.server.files', [
            'isConfigured' => $isConfigured,
            'currentDirectory' => $directory,
            'files' => $fileData['files'] ?? [],
            'fetchError' => $fileData['error'] ?? null,
            'panelUrl' => PterodactylService::getPanelUrl(),
            'serverId' => PterodactylService::getServerId(),
        ]);
    }

    /**
     * Get file content for in-browser editing.
     */
    public function getFileContent(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'file' => ['required', 'string'],
        ]);

        $res = PterodactylService::getFileContents($validated['file']);
        return response()->json($res);
    }

    /**
     * Save edited file content via Pterodactyl API.
     */
    public function saveFileContent(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'file' => ['required', 'string'],
            'content' => ['present', 'string'],
        ]);

        $res = PterodactylService::writeFileContents($validated['file'], $validated['content']);

        AuditService::log([
            'action' => 'SERVER_FILE_EDIT',
            'target_type' => 'FILE',
            'target_name' => $validated['file'],
            'source' => 'WEB_FILE_MANAGER',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => [
                'file' => $validated['file'],
                'size' => strlen($validated['content']),
            ],
        ]);

        return response()->json($res);
    }

    /**
     * Upload a file directly to the server.
     */
    public function uploadFile(Request $request)
    {
        $request->validate([
            'directory' => ['required', 'string'],
            'file' => ['required', 'file', 'max:102400'], // max 100MB
        ]);

        $uploadedFile = $request->file('file');
        $directory = $request->input('directory', '/');
        $filename = $uploadedFile->getClientOriginalName();
        $content = file_get_contents($uploadedFile->getRealPath());

        $res = PterodactylService::uploadFile($directory, $filename, $content);

        AuditService::log([
            'action' => 'SERVER_FILE_UPLOAD',
            'target_type' => 'FILE',
            'target_name' => $directory . '/' . $filename,
            'source' => 'WEB_FILE_MANAGER',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => [
                'filename' => $filename,
                'directory' => $directory,
                'size' => strlen($content),
            ],
        ]);

        if ($request->wantsJson()) {
            return response()->json($res);
        }

        if ($res['success']) {
            return redirect()->back()->with('success', "File [{$filename}] berhasil diunggah ke [{$directory}].");
        }

        return redirect()->back()->with('error', "Gagal mengunggah file: " . ($res['error'] ?? 'Unknown error'));
    }

    /**
     * Delete files or folders on the server.
     */
    public function deleteFile(Request $request)
    {
        $validated = $request->validate([
            'root' => ['required', 'string'],
            'files' => ['required', 'array'],
        ]);

        $res = PterodactylService::deleteFiles($validated['root'], $validated['files']);

        AuditService::log([
            'action' => 'SERVER_FILE_DELETE',
            'target_type' => 'FILE',
            'target_name' => implode(', ', $validated['files']),
            'source' => 'WEB_FILE_MANAGER',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => $validated,
        ]);

        if ($request->wantsJson()) {
            return response()->json($res);
        }

        if ($res['success']) {
            return redirect()->back()->with('success', 'File / folder terpilih berhasil dihapus.');
        }

        return redirect()->back()->with('error', 'Gagal menghapus file: ' . ($res['error'] ?? 'Unknown error'));
    }

    /**
     * Create a new folder on the server.
     */
    public function createFolder(Request $request)
    {
        $validated = $request->validate([
            'root' => ['required', 'string'],
            'name' => ['required', 'string', 'max:100'],
        ]);

        $res = PterodactylService::createFolder($validated['root'], $validated['name']);

        AuditService::log([
            'action' => 'SERVER_FOLDER_CREATE',
            'target_type' => 'FOLDER',
            'target_name' => $validated['root'] . '/' . $validated['name'],
            'source' => 'WEB_FILE_MANAGER',
            'status' => $res['success'] ? 'SUCCESS' : 'FAILED',
            'metadata' => $validated,
        ]);

        if ($request->wantsJson()) {
            return response()->json($res);
        }

        if ($res['success']) {
            return redirect()->back()->with('success', "Folder [{$validated['name']}] berhasil dibuat.");
        }

        return redirect()->back()->with('error', 'Gagal membuat folder: ' . ($res['error'] ?? 'Unknown error'));
    }

    /**
     * Update Pterodactyl API settings.
     */
    public function updatePterodactylSettings(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'pterodactyl_url' => ['required', 'url', 'max:255'],
            'pterodactyl_api_key' => ['nullable', 'string', 'max:255'],
            'pterodactyl_server_id' => ['required', 'string', 'max:64'],
        ]);

        $settings = [
            'apexsions.pterodactyl_url' => rtrim($validated['pterodactyl_url'], '/'),
            'apexsions.pterodactyl_server_id' => trim($validated['pterodactyl_server_id']),
        ];

        if (!empty($validated['pterodactyl_api_key'])) {
            $settings['apexsions.pterodactyl_api_key'] = trim($validated['pterodactyl_api_key']);
        }

        Setting::updateSettings($settings);

        return redirect()->back()->with('success', 'Konfigurasi Pterodactyl API berhasil disimpan.');
    }
}
