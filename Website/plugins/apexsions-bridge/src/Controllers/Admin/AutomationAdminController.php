<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationExecution;
use Azuriom\Plugin\ApexsionsBridge\Models\AutomationPolicy;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Azuriom\Plugin\ApexsionsBridge\Services\SafeAutomationEngine;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\View\View;

class AutomationAdminController extends Controller
{
    /**
     * Display automation policies and execution history.
     */
    public function index(): View
    {
        if (AutomationPolicy::count() === 0) {
            SafeAutomationEngine::seedDefaultPolicies();
        }

        $policies = AutomationPolicy::withCount('executions')->get();

        $pendingApprovals = AutomationExecution::where('status', 'PENDING_APPROVAL')
            ->with('policy')
            ->latest()
            ->get();

        $recentExecutions = AutomationExecution::with('policy')
            ->latest()
            ->take(25)
            ->get();

        return view('apexsions-bridge::admin.automation.index', [
            'policies' => $policies,
            'pendingApprovals' => $pendingApprovals,
            'recentExecutions' => $recentExecutions,
        ]);
    }

    /**
     * Approve a pending execution action.
     */
    public function approve(string $id): RedirectResponse
    {
        $execution = AutomationExecution::where('execution_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $user = Auth::user();
        if (!$user) {
            return redirect()->back()->with('error', 'Otentikasi staf diperlukan.');
        }

        $success = SafeAutomationEngine::approve($execution, $user);

        if ($success) {
            return redirect()->back()->with('success', "Usulan aksi otomatis {$execution->execution_id} berhasil disetujui dan dieksekusi.");
        }

        return redirect()->back()->with('error', "Gagal memproses persetujuan untuk {$execution->execution_id}.");
    }

    /**
     * Reject a pending execution action.
     */
    public function reject(string $id, Request $request): RedirectResponse
    {
        $request->validate([
            'reason' => 'required|string|min:5|max:255',
        ]);

        $execution = AutomationExecution::where('execution_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $user = Auth::user();
        if (!$user) {
            return redirect()->back()->with('error', 'Otentikasi staf diperlukan.');
        }

        SafeAutomationEngine::reject($execution, $user, $request->input('reason'));

        return redirect()->back()->with('success', "Usulan aksi otomatis {$execution->execution_id} berhasil ditolak.");
    }

    /**
     * Toggle enabled status of an automation policy.
     */
    public function toggle(string $id): RedirectResponse
    {
        $policy = AutomationPolicy::where('policy_id', $id)
            ->orWhere('id', $id)
            ->firstOrFail();

        $newStatus = !$policy->is_enabled;
        $policy->update(['is_enabled' => $newStatus]);

        $user = Auth::user();
        AuditService::log([
            'action' => 'AUTOMATION_POLICY_TOGGLED',
            'category' => 'AUTOMATION',
            'target_type' => 'POLICY',
            'target_id' => $policy->policy_id,
            'staff_id' => $user ? (string) $user->id : 'SYSTEM',
            'staff_name' => $user ? $user->name : 'System Admin',
            'source' => 'WEB',
            'status' => 'SUCCESS',
            'metadata' => ['is_enabled' => $newStatus],
        ]);

        $label = $newStatus ? 'diaktifkan' : 'dinonaktifkan';
        return redirect()->back()->with('success', "Kebijakan otomasi {$policy->name} berhasil {$label}.");
    }
}
