<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Contracts\View\View;

class PublicProfileController extends Controller
{
    /**
     * Show public character inspection page for a player.
     */
    public function show(string $username): View
    {
        $account = MinecraftAccount::where('minecraft_username', $username)
            ->whereNotNull('verified_at')
            ->firstOrFail();

        return view('apexsions-bridge::public-profile', [
            'account' => $account,
        ]);
    }
}
