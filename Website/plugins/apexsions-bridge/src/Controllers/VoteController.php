<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\VoteTransaction;
use Azuriom\Plugin\ApexsionsBridge\Models\VotingSite;
use Azuriom\Plugin\ApexsionsBridge\Services\VoteService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class VoteController extends Controller
{
    protected VoteService $voteService;

    public function __construct(VoteService $voteService)
    {
        $this->voteService = $voteService;
    }

    /**
     * Display the official Apexsions Vote Realm portal.
     * Purely for:
     * - Voting links
     * - Player vote statistics & streak
     * - Personal vote history & status
     * - Server vote statistics
     */
    public function index(Request $request)
    {
        $sites = VotingSite::where('is_active', true)->get();

        // Check if authenticated user has a linked Minecraft account
        $user = Auth::user();
        $linkedAccount = null;
        if ($user) {
            $linkedAccount = MinecraftAccount::where('user_id', $user->id)->first();
        }

        $activeUsername = $request->input('username') ?: ($linkedAccount?->minecraft_username ?: null);

        $cooldowns = [];
        $voterStats = [
            'total' => 0,
            'this_month' => 0,
            'this_week' => 0,
            'today' => 0,
            'streak' => 0,
            'last_voted_at' => null,
        ];

        if ($activeUsername) {
            foreach ($sites as $site) {
                $cd = $this->voteService->checkCooldown($site, $activeUsername, $linkedAccount?->minecraft_uuid);
                $cooldowns[$site->slug] = [
                    'ready' => $cd === null,
                    'cooldown_until' => $cd?->toIso8601String(),
                    'human_time' => $cd?->diffForHumans(),
                ];
            }

            $voterStats = $this->voteService->calculateVoterStats($activeUsername, $linkedAccount?->minecraft_uuid);
        }

        // Server-wide vote metrics
        $serverTotalVotes = VoteTransaction::where('vote_status', 'VALID')->count();
        $serverVotesToday = VoteTransaction::where('vote_status', 'VALID')->whereDate('voted_at', today())->count();
        $serverVotesMonth = VoteTransaction::where('vote_status', 'VALID')->where('voted_at', '>=', now()->startOfMonth())->count();

        // Recent verified vote transactions across realm (latest 10)
        $recentVotes = VoteTransaction::with('site')
            ->where('vote_status', 'VALID')
            ->orderBy('voted_at', 'desc')
            ->limit(10)
            ->get();

        // Personal vote history if user is logged in or username is set
        $personalHistory = collect();
        if ($activeUsername) {
            $cleanUsername = ltrim($activeUsername, '.');
            $personalHistory = VoteTransaction::with(['site', 'keysDelivery', 'moneyDelivery'])
                ->where(function ($q) use ($activeUsername, $cleanUsername, $linkedAccount) {
                    $q->whereIn('player_username', [$activeUsername, $cleanUsername, '.' . $cleanUsername]);
                    if ($linkedAccount?->minecraft_uuid) {
                        $q->orWhere('player_uuid', $linkedAccount->minecraft_uuid);
                    }
                })
                ->orderBy('voted_at', 'desc')
                ->limit(15)
                ->get();
        }

        return view('vote', compact(
            'sites',
            'linkedAccount',
            'activeUsername',
            'cooldowns',
            'voterStats',
            'serverTotalVotes',
            'serverVotesToday',
            'serverVotesMonth',
            'recentVotes',
            'personalHistory'
        ));
    }

    /**
     * Handle inbound webhook/callback from voting sites (POST and GET).
     */
    public function handleCallback(Request $request, string $siteSlug): JsonResponse
    {
        $site = VotingSite::where('slug', $siteSlug)->where('is_active', true)->first();
        if (!$site) {
            // Check if it is a generic votifier inbound callback
            if ($siteSlug === 'votifier') {
                $site = VotingSite::where('slug', 'minecraft-mp')->first() ?: VotingSite::first();
            } else {
                return response()->json(['error' => 'Unknown or inactive voting site.'], 404);
            }
        }

        // Extract username from query or body (supports various platforms: username, user, nick, player)
        $username = $request->input('username') ?: $request->input('user') ?: $request->input('nick') ?: $request->input('player');
        if (!$username) {
            return response()->json(['error' => 'Username parameter missing.'], 400);
        }

        $externalId = $request->input('vote_id') ?: $request->input('id') ?: null;
        $ip = $request->input('ip') ?: $request->input('address') ?: $request->ip();

        $result = $this->voteService->processVoteReward(
            $site,
            $username,
            $ip,
            'CALLBACK',
            $externalId
        );

        return response()->json($result, $result['success'] ? 200 : 400);
    }

    /**
     * Troubleshoot endpoint: Check vote status or trigger an immediate platform check
     * without requiring manual claim to receive rewards.
     */
    public function checkStatus(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'username' => ['required', 'string', 'min:2', 'max:32', 'regex:/^[a-zA-Z0-9_.* ]+$/'],
            'site_slug' => ['nullable', 'string', 'max:64'],
        ]);

        $username = trim($validated['username']);
        $cleanUsername = ltrim($username, '.');

        // Check if already recorded recently in transactions
        $latestTx = VoteTransaction::with(['site', 'keysDelivery', 'moneyDelivery'])
            ->whereIn('player_username', [$username, $cleanUsername, '.' . $cleanUsername])
            ->orderBy('voted_at', 'desc')
            ->first();

        if ($latestTx && $latestTx->voted_at->isToday()) {
            return response()->json([
                'status' => 'FOUND',
                'reward_status' => $latestTx->reward_status,
                'voted_at' => $latestTx->voted_at->toIso8601String(),
                'platform' => $latestTx->site?->name ?: $latestTx->site_slug,
                'message' => "Vote Anda di {$latestTx->site_slug} telah tercatat dengan status imbalan: {$latestTx->reward_status}.",
                'transaction' => $latestTx,
            ]);
        }

        // If site specified, attempt an auto-poll check
        $siteSlug = $validated['site_slug'] ?? 'minecraft-mp';
        $site = VotingSite::where('slug', $siteSlug)->where('is_active', true)->first();

        if ($site && !empty($site->api_key)) {
            $verification = $this->voteService->verifyWithPlatform($site, $username, $request->ip());

            if ($verification['valid']) {
                $reward = $this->voteService->processVoteReward($site, $username, $request->ip(), 'STATUS_CHECK');
                return response()->json([
                    'status' => 'REWARDED',
                    'message' => 'Vote sah Anda berhasil diverifikasi dan imbalan otomatis dikirimkan ke server!',
                    'reward' => $reward,
                ]);
            } else {
                return response()->json([
                    'status' => $verification['status'] ?? 'WAITING',
                    'message' => $verification['message'],
                ]);
            }
        }

        return response()->json([
            'status' => 'WAITING',
            'message' => 'Vote Anda sedang dalam antrean sinkronisasi platform. Hadiah akan otomatis masuk begitu data terkonfirmasi.',
        ]);
    }
}
