<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsEvent;
use Illuminate\Contracts\View\View;

class WorldFeedController extends Controller
{
    /**
     * Public "Chronicles of Apexsions" live feed.
     *
     * Renders recent player-facing world events (PvP kills, bounty claims,
     * boss takedowns, seasonal moments) that the game server publishes to the
     * unified event ledger.
     */
    public function index(): View
    {
        $events = ApexsionsEvent::query()
            ->where('source', 'MINECRAFT')
            ->whereIn('event_type', [
                'PLAYER_KILL',
                'BOUNTY_CLAIMED',
                'KINGDOM_WAR',
                'BOSS_KILL',
                'WONDER_COMPLETED',
                'WORLD_MILESTONE',
            ])
            ->orderByDesc('occurred_at')
            ->limit(50)
            ->get();

        return view('apexsions-bridge::world-feed', [
            'events' => $events,
        ]);
    }
}