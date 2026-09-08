<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\PluginCapability;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginActionGateway;
use Azuriom\Plugin\ApexsionsBridge\Services\PluginRegistryService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class PluginAdminController extends Controller
{
    /**
     * Display a listing of all registered Apexsions custom plugins.
     */
    public function index(Request $request): View
    {
        $filters = [
            'type' => $request->input('type'),
            'status' => $request->input('status'),
            'integration' => $request->input('integration'),
        ];

        $plugins = PluginRegistryService::getAllPlugins($filters);

        $stats = [
            'total' => ApexsionsPlugin::count(),
            'healthy' => ApexsionsPlugin::where('status', 'ENABLED')->where('health_status', 'HEALTHY')->count(),
            'web_ready' => ApexsionsPlugin::where('integration_status', 'WEB_READY')->count(),
            'total_capabilities' => PluginCapability::where('status', 'AVAILABLE')->count(),
        ];

        AuditService::log([
            'action' => 'PLUGIN_VIEW',
            'target_type' => 'PLUGIN',
            'target_id' => 'GLOBAL',
            'target_name' => 'Realm Plugin Registry',
            'reason' => 'Staf membuka katalog custom plugins dan capabilities',
            'metadata' => ['filters' => $filters],
        ]);

        return view('apexsions-bridge::admin.plugins.index', [
            'plugins' => $plugins,
            'stats' => $stats,
            'filters' => $filters,
        ]);
    }

    /**
     * Display the specified custom plugin details, grouped capabilities, and health logs.
     */
    public function show(Request $request, string $plugin_id): View
    {
        $plugin = PluginRegistryService::getPlugin($plugin_id);

        if (!$plugin) {
            abort(404, "Plugin custom [{$plugin_id}] tidak ditemukan dalam registry.");
        }

        // Group capabilities by functional domain
        $groupedCapabilities = [
            'READ' => $plugin->capabilities->where('type', 'READ')->values(),
            'WRITE' => $plugin->capabilities->where('type', 'WRITE')->values(),
            'ACTION' => $plugin->capabilities->where('type', 'ACTION')->values(),
            'EVENT' => $plugin->capabilities->where('type', 'EVENT')->values(),
            'METRIC' => $plugin->capabilities->where('type', 'METRIC')->values(),
        ];

        // Filter supported action templates from PluginActionGateway
        $supportedActions = PluginActionGateway::ACTION_REGISTRY;

        AuditService::log([
            'action' => 'PLUGIN_VIEW',
            'target_type' => 'PLUGIN',
            'target_id' => $plugin->plugin_id,
            'target_name' => $plugin->name,
            'reason' => "Staf membuka detail arsitektur plugin {$plugin->name}",
            'metadata' => ['plugin_id' => $plugin->plugin_id, 'version' => $plugin->version],
        ]);

        return view('apexsions-bridge::admin.plugins.show', [
            'plugin' => $plugin,
            'groupedCapabilities' => $groupedCapabilities,
            'supportedActions' => $supportedActions,
            'healthHistory' => $plugin->healthHistory,
        ]);
    }

    /**
     * Execute a safe plugin action through the authorized gateway.
     */
    public function executeAction(Request $request, string $plugin_id): RedirectResponse
    {
        $validated = $request->validate([
            'capability_id' => ['required', 'string', 'max:64'],
            'reason' => ['nullable', 'string', 'max:255'],
            'params' => ['nullable', 'array'],
        ]);

        $result = PluginActionGateway::executeAction(
            $plugin_id,
            $validated['capability_id'],
            $validated['params'] ?? [],
            $request->user(),
            $validated['reason'] ?? ''
        );

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }
}
