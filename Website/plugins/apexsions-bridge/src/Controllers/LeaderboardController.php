<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Contracts\View\View;
use Illuminate\Support\Facades\DB;

class LeaderboardController extends Controller
{
    /**
     * Display the official server leaderboards and kingdom standings.
     */
    public function index(): View
    {
        $baseQuery = MinecraftAccount::where('minecraft_username', 'not like', 'TruthTest%')
            ->whereNotNull('minecraft_username');

        $topLevels = (clone $baseQuery)
            ->orderByDesc('level')
            ->orderByDesc('xp')
            ->limit(10)
            ->get();

        $topBalances = (clone $baseQuery)
            ->orderByDesc('balance_rupiah')
            ->limit(10)
            ->get();

        // Kingdom faction distribution & power
        $kingdoms = [
            'ZENITHAR' => [
                'name' => 'Zenithar',
                'color' => '#f39c12',
                'tagline' => 'Celestial Gold & Solar Peaks',
                'count' => 0,
                'total_levels' => 0,
            ],
            'SOLTERRA' => [
                'name' => 'Solterra',
                'color' => '#e74c3c',
                'tagline' => 'Desert Citadels & Molten Fire',
                'count' => 0,
                'total_levels' => 0,
            ],
            'SYLVAMOOR' => [
                'name' => 'Sylvamoor',
                'color' => '#2ecc71',
                'tagline' => 'Verdant Deepwood & Nature Balance',
                'count' => 0,
                'total_levels' => 0,
            ],
        ];

        $kingdomData = (clone $baseQuery)
            ->whereIn('kingdom', ['ZENITHAR', 'SOLTERRA', 'SYLVAMOOR'])
            ->select('kingdom', DB::raw('count(*) as count'), DB::raw('sum(level) as total_levels'))
            ->groupBy('kingdom')
            ->get();

        foreach ($kingdomData as $row) {
            if (isset($kingdoms[$row->kingdom])) {
                $kingdoms[$row->kingdom]['count'] = (int) $row->count;
                $kingdoms[$row->kingdom]['total_levels'] = (int) $row->total_levels;
            }
        }

        return view('apexsions-bridge::leaderboard', [
            'topLevels' => $topLevels,
            'topBalances' => $topBalances,
            'kingdoms' => $kingdoms,
        ]);
    }
}
