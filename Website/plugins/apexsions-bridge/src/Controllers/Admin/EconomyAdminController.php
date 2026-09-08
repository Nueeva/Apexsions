<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\EconomyAdminService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class EconomyAdminController extends Controller
{
    /**
     * Display economy inspector overview with verified data points and treasury status.
     */
    public function index(): View
    {
        $overview = EconomyAdminService::getEconomyOverview();

        return view('apexsions-bridge::admin.economy.index', [
            'metrics' => $overview['metrics'],
            'kingdoms' => $overview['kingdoms'],
            'recentTransactions' => $overview['recent_transactions'],
            'recentActions' => $overview['recent_actions'],
        ]);
    }

    /**
     * Submit a controlled balance adjustment (GIVE / DEDUCT).
     */
    public function adjustBalance(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player' => ['required', 'string', 'min:2', 'max:64'],
            'currency' => ['required', 'in:rupiah,diamond,apex_coins'],
            'direction' => ['required', 'in:GIVE,DEDUCT'],
            'amount' => ['required', 'numeric', 'min:0.01'],
            'reason' => ['required', 'string', 'min:5', 'max:255'],
        ]);

        $identifier = trim($validated['player']);
        $account = MinecraftAccount::where('minecraft_uuid', $identifier)
            ->orWhere('minecraft_username', $identifier)
            ->first();

        if (!$account) {
            return redirect()->back()->with('error', "Pemain '{$identifier}' tidak ditemukan dalam database akun Minecraft.");
        }

        $result = EconomyAdminService::adjustBalance(
            $account,
            $validated['currency'],
            (float) $validated['amount'],
            $validated['direction'],
            $validated['reason'],
            $request->user()
        );

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }
}
