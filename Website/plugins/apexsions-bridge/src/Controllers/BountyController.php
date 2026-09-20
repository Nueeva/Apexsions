<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Bounty;
use Illuminate\Contracts\View\View;

class BountyController extends Controller
{
    /**
     * Public bounty board: active hunted players ordered by reward size.
     *
     * Data is a mirror of the Minecraft server snapshot, refreshed whenever
     * the in-game bounty set changes.
     */
    public function index(): View
    {
        $bounties = Bounty::query()->top(50)->get();
        $totalPool = (float) Bounty::query()->sum('total_amount');

        return view('apexsions-bridge::bounties', [
            'bounties' => $bounties,
            'totalPool' => $totalPool,
            'lastSyncedAt' => Bounty::lastSyncedAt(),
        ]);
    }
}