<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Services\RankService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class RankAdminController extends Controller
{
    /**
     * Display the official rank hierarchy, metadata, and player counts.
     */
    public function index(): View
    {
        $ranks = RankService::getAllRanks();
        $playerCounts = RankService::getPlayerCounts();
        $totalPlayers = MinecraftAccount::count();

        return view('apexsions-bridge::admin.ranks.index', [
            'ranks' => $ranks,
            'playerCounts' => $playerCounts,
            'totalPlayers' => $totalPlayers,
        ]);
    }

    /**
     * Show detailed view of a specific rank and its member list.
     */
    public function show(string $rank_key, Request $request): View
    {
        $normalized = strtolower(trim($rank_key));
        $rank = RankService::getRank($normalized);

        if (!$rank) {
            abort(404, "Rank '{$rankKey}' tidak ditemukan.");
        }

        $search = trim((string) $request->input('q', ''));
        $query = MinecraftAccount::whereRaw('LOWER(rank) = ?', [$normalized]);

        if (!empty($search)) {
            $term = '%' . $search . '%';
            $query->where(function ($q) use ($term) {
                $q->where('minecraft_username', 'LIKE', $term)
                  ->orWhere('minecraft_uuid', 'LIKE', $term);
            });
        }

        $players = $query->orderBy('last_seen_at', 'desc')
            ->orderBy('id', 'desc')
            ->paginate(25)
            ->withQueryString();

        $allRanks = RankService::getAllRanks();

        return view('apexsions-bridge::admin.ranks.show', [
            'rank' => $rank,
            'players' => $players,
            'search' => $search,
            'allRanks' => $allRanks,
        ]);
    }

    /**
     * Handle rank assignment or modification form submission.
     */
    public function assign(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player_identifier' => ['required', 'string', 'max:100'],
            'rank' => ['required', 'string'],
            'reason' => ['required', 'string', 'min:3', 'max:250'],
        ]);

        $account = MinecraftAccount::where('minecraft_uuid', $validated['player_identifier'])
            ->orWhere('minecraft_username', $validated['player_identifier'])
            ->first();

        if (!$account) {
            return back()->with('error', "Pemain dengan identifier '{$validated['player_identifier']}' tidak ditemukan di basis data.");
        }

        $result = RankService::assignRank(
            $request->user(),
            $account,
            $validated['rank'],
            $validated['reason']
        );

        if ($result['success']) {
            return back()->with('success', $result['message']);
        }

        return back()->with('error', $result['message']);
    }
}
