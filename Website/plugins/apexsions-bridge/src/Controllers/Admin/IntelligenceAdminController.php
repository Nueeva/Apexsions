<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsIncident;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsPlugin;
use Azuriom\Plugin\ApexsionsBridge\Models\IntelligenceRule;
use Azuriom\Plugin\ApexsionsBridge\Models\ServerAlert;
use Azuriom\Plugin\ApexsionsBridge\Services\AnomalyDetectionEngine;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\View\View;

class IntelligenceAdminController extends Controller
{
    /**
     * Display the Intelligence & Anomaly Operations Dashboard.
     */
    public function index(Request $request): View
    {
        // Ensure default explicit intelligence rules exist
        if (IntelligenceRule::count() === 0) {
            AnomalyDetectionEngine::seedDefaultRules();
        }

        $now = Carbon::now();
        $past24h = $now->copy()->subHours(24);

        // Incident metrics
        $openIncidentsCount = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])->count();
        $criticalCount = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
            ->where('severity', 'CRITICAL')
            ->count();
        $highCount = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
            ->where('severity', 'HIGH')
            ->count();
        $mediumCount = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
            ->where('severity', 'MEDIUM')
            ->count();
        $lowCount = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
            ->where('severity', 'LOW')
            ->count();
        $resolvedCount = ApexsionsIncident::whereIn('status', ['RESOLVED', 'CLOSED'])->count();

        // Event telemetry in past 24 hours
        $eventsPast24h = ApexsionsEvent::where('occurred_at', '>=', $past24h)->count();

        // Prioritized active incidents (CRITICAL -> HIGH -> MEDIUM -> LOW)
        $activeIncidents = ApexsionsIncident::whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED'])
            ->orderByRaw("CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END ASC")
            ->orderBy('last_occurred_at', 'desc')
            ->take(10)
            ->get();

        // Recent normalized events
        $recentEvents = ApexsionsEvent::orderBy('occurred_at', 'desc')
            ->take(15)
            ->get();

        // Active Intelligence Rules
        $rules = IntelligenceRule::orderBy('category')->orderBy('name')->get();

        // Unresolved Server Alerts
        $activeAlerts = ServerAlert::whereNull('resolved_at')
            ->orderBy('created_at', 'desc')
            ->take(5)
            ->get();

        // Degraded custom plugins
        $degradedPlugins = ApexsionsPlugin::where('health_status', '!=', 'HEALTHY')
            ->orWhere('status', '!=', 'ENABLED')
            ->take(5)
            ->get();

        AuditService::log([
            'action' => 'INTELLIGENCE_VIEW',
            'target_type' => 'INTELLIGENCE',
            'target_id' => 'GLOBAL',
            'target_name' => 'Intelligence Operations Desk',
            'reason' => 'Staf memantau dashboard intelijen, anomali, dan insiden aktif',
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        return view('apexsions-bridge::admin.intelligence.index', [
            'openIncidentsCount' => $openIncidentsCount,
            'criticalCount' => $criticalCount,
            'highCount' => $highCount,
            'mediumCount' => $mediumCount,
            'lowCount' => $lowCount,
            'resolvedCount' => $resolvedCount,
            'eventsPast24h' => $eventsPast24h,
            'activeIncidents' => $activeIncidents,
            'recentEvents' => $recentEvents,
            'rules' => $rules,
            'activeAlerts' => $activeAlerts,
            'degradedPlugins' => $degradedPlugins,
        ]);
    }
}
