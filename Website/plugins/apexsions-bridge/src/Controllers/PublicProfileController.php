<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;

class PublicProfileController extends Controller
{
    /**
     * Show public character inspection page for a player.
     * Accepts Mojang/Bedrock UUID, account ID, or legacy username (redirected to unique ID for privacy).
     */
    public function show(string $identifier)
    {
        $account = MinecraftAccount::where(function ($query) use ($identifier) {
                $query->where('minecraft_uuid', $identifier)
                      ->orWhere('id', $identifier)
                      ->orWhere('minecraft_username', $identifier);
            })
            ->whereNotNull('verified_at')
            ->firstOrFail();

        // Determine canonical unique identifier (UUID if present, otherwise database ID)
        $canonicalId = $account->minecraft_uuid ?: (string) $account->id;

        // If accessed by username and canonical ID differs, 301 redirect for privacy and URL standardization
        if (strcasecmp($identifier, $account->minecraft_username) === 0 && (string) $canonicalId !== (string) $identifier) {
            return redirect()->route('apexsions-bridge.player.show', ['identifier' => $canonicalId], 301);
        }

        return view('apexsions-bridge::public-profile', [
            'account' => $account,
        ]);
    }
}
