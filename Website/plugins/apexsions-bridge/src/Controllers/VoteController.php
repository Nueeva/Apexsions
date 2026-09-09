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
     * Display the official Apexsions Vote Realm page.
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
        if ($activeUsername) {
            foreach ($sites as $site) {
                $cd = $this->voteService->checkCooldown($site, $activeUsername, $linkedAccount?->minecraft_uuid);
                $cooldowns[$site->slug] = [
                    'ready' => $cd === null,
                    'cooldown_until' => $cd?->toIso8601String(),
                    'human_time' => $cd?->diffForHumans(),
                ];
            }
        }

        // Recent verified vote transactions (latest 10)
        $recentVotes = VoteTransaction::with('site')
            ->where('vote_status', 'VALID')
            ->orderBy('voted_at', 'desc')
            ->limit(10)
            ->get();

        // Personal vote history if user is logged in or username is set
        $personalHistory = collect();
        if ($activeUsername) {
            $personalHistory = VoteTransaction::with('site')
                ->where('player_username', $activeUsername)
                ->orderBy('voted_at', 'desc')
                ->limit(10)
                ->get();
        }

        return view('vote', compact('sites', 'linkedAccount', 'activeUsername', 'cooldowns', 'recentVotes', 'personalHistory'));
    }

    /**
     * Verify and claim vote reward for a specific voting platform.
     */
    public function verifyAndClaim(Request $request, string $siteSlug): JsonResponse
    {
        $validated = $request->validate([
            'username' => ['required', 'string', 'min:2', 'max:32', 'regex:/^[a-zA-Z0-9_.* ]+$/'],
        ]);

        $site = VotingSite::where('slug', $siteSlug)->where('is_active', true)->first();
        if (!$site) {
            return response()->json([
                'success' => false,
                'message' => 'Platform voting tidak ditemukan atau sedang dinonaktifkan.',
            ], 404);
        }

        $result = $this->voteService->processVoteReward(
            $site,
            $validated['username'],
            $request->ip(),
            'WEB_CLAIM'
        );

        if (!$result['success']) {
            return response()->json($result, ($result['duplicate'] ?? false) ? 429 : 422);
        }

        return response()->json($result);
    }

    /**
     * Handle inbound webhook/callback from voting sites.
     */
    public function handleCallback(Request $request, string $siteSlug): JsonResponse
    {
        $site = VotingSite::where('slug', $siteSlug)->where('is_active', true)->first();
        if (!$site) {
            return response()->json(['error' => 'Unknown site'], 404);
        }

        // Extract username from query or body (supports various platforms)
        $username = $request->input('username') ?: $request->input('user') ?: $request->input('nick');
        if (!$username) {
            return response()->json(['error' => 'Username parameter missing'], 400);
        }

        $result = $this->voteService->processVoteReward(
            $site,
            $username,
            $request->ip(),
            'WEBHOOK'
        );

        return response()->json($result, $result['success'] ? 200 : 400);
    }
}
