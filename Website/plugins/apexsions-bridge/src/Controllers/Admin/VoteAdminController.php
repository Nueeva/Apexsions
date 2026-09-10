<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\VoteTransaction;
use Azuriom\Plugin\ApexsionsBridge\Models\VotingSite;
use Azuriom\Plugin\ApexsionsBridge\Services\VoteService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class VoteAdminController extends Controller
{
    protected VoteService $voteService;

    public function __construct(VoteService $voteService)
    {
        $this->voteService = $voteService;
    }

    /**
     * Display vote management dashboard and transactions ledger.
     */
    public function index(Request $request)
    {
        // 1. Core Metrics
        $totalVotes = VoteTransaction::where('vote_status', 'VALID')->count();
        $votesToday = VoteTransaction::where('vote_status', 'VALID')->whereDate('voted_at', today())->count();
        $votesThisWeek = VoteTransaction::where('vote_status', 'VALID')->where('voted_at', '>=', now()->startOfWeek())->count();
        $votesThisMonth = VoteTransaction::where('vote_status', 'VALID')->where('voted_at', '>=', now()->startOfMonth())->count();
        $uniqueVoters = VoteTransaction::where('vote_status', 'VALID')->distinct('player_username')->count('player_username');
        $rewardsDelivered = VoteTransaction::where('reward_status', 'REWARDED')->count();
        $failedRewards = VoteTransaction::whereIn('reward_status', ['FAILED', 'PARTIAL'])->count();

        // 2. Query Builder with Filters
        $query = VoteTransaction::with(['site', 'keysDelivery', 'moneyDelivery'])->orderBy('voted_at', 'desc');

        if ($request->filled('player')) {
            $query->where('player_username', 'like', '%' . trim($request->input('player')) . '%');
        }

        if ($request->filled('site')) {
            $query->where('site_slug', $request->input('site'));
        }

        if ($request->filled('status')) {
            $query->where('reward_status', $request->input('status'));
        }

        if ($request->filled('date')) {
            $query->whereDate('voted_at', $request->input('date'));
        }

        $transactions = $query->paginate(20)->withQueryString();
        $sites = VotingSite::all();

        return view('apexsions-bridge::admin.votes.index', compact(
            'totalVotes',
            'votesToday',
            'votesThisWeek',
            'votesThisMonth',
            'uniqueVoters',
            'rewardsDelivered',
            'failedRewards',
            'transactions',
            'sites'
        ));
    }

    /**
     * Display details of a specific vote transaction.
     */
    public function show(int $id)
    {
        $transaction = VoteTransaction::with(['site', 'keysDelivery', 'moneyDelivery'])->findOrFail($id);
        return response()->json($transaction);
    }

    /**
     * Safely retry full reward distribution for a failed or partial vote transaction.
     */
    public function retryReward(Request $request, int $id): RedirectResponse
    {
        $transaction = VoteTransaction::findOrFail($id);
        $actorName = Auth::user()?->name ?: 'Administrator';

        $result = $this->voteService->retryFullReward($transaction, $actorName);

        if (!$result['success']) {
            return back()->with('error', $result['message']);
        }

        return back()->with('success', $result['message']);
    }

    /**
     * Granular retry: Retry key delivery only.
     */
    public function retryKey(Request $request, int $id): RedirectResponse
    {
        $transaction = VoteTransaction::findOrFail($id);
        $actorName = Auth::user()?->name ?: 'Administrator';

        $result = $this->voteService->retryKeyDelivery($transaction, $actorName);

        if (!$result['success']) {
            return back()->with('error', $result['message']);
        }

        return back()->with('success', $result['message']);
    }

    /**
     * Granular retry: Retry money delivery only.
     */
    public function retryMoney(Request $request, int $id): RedirectResponse
    {
        $transaction = VoteTransaction::findOrFail($id);
        $actorName = Auth::user()?->name ?: 'Administrator';

        $result = $this->voteService->retryMoneyDelivery($transaction, $actorName);

        if (!$result['success']) {
            return back()->with('error', $result['message']);
        }

        return back()->with('success', $result['message']);
    }

    /**
     * Trigger manual polling on voting sites.
     */
    public function triggerPoll(Request $request): RedirectResponse
    {
        $sites = VotingSite::where('is_active', true)->whereNotNull('api_key')->get();
        $totalProcessed = 0;

        foreach ($sites as $site) {
            $res = $this->voteService->pollExternalVotes($site);
            if (($res['status'] ?? '') === 'success') {
                $totalProcessed += ($res['processed'] ?? 0);
            }
        }

        return back()->with('success', "Sinkronisasi platform selesai! {$totalProcessed} transaksi suara baru berhasil diproses.");
    }

    /**
     * Toggle active / disabled status of a voting platform.
     */
    public function toggleSite(Request $request, int $id): RedirectResponse
    {
        $site = VotingSite::findOrFail($id);
        $site->is_active = !$site->is_active;
        $site->save();

        $status = $site->is_active ? 'diaktifkan' : 'dinonaktifkan';
        return back()->with('success', "Platform voting {$site->name} berhasil {$status}!");
    }

    /**
     * Update voting platform configuration (API key, server ID, URL, cooldown).
     */
    public function updateSite(Request $request, int $id): RedirectResponse
    {
        $site = VotingSite::findOrFail($id);

        $validated = $request->validate([
            'name' => ['required', 'string', 'max:100'],
            'vote_url' => ['required', 'url', 'max:255'],
            'server_id' => ['nullable', 'string', 'max:100'],
            'api_key' => ['nullable', 'string', 'max:255'],
            'cooldown_hours' => ['required', 'integer', 'min:1', 'max:168'],
        ]);

        $site->name = $validated['name'];
        $site->vote_url = $validated['vote_url'];
        $site->server_id = $validated['server_id'] ?: null;
        $site->api_key = $validated['api_key'] ?: null;
        $site->cooldown_hours = $validated['cooldown_hours'];
        if ($request->has('has_active_toggle')) {
            $site->is_active = $request->boolean('is_active');
        }
        $site->save();

        return back()->with('success', "Pengaturan platform {$site->name} berhasil diperbarui!");
    }
}
