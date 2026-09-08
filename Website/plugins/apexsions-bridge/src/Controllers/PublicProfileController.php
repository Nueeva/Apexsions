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
            ->first();

        // If not found in database, check if player is online in live telemetry cache or has valid UUID
        if (!$account) {
            $cacheData = cache()->get('apexsions.server_status');
            $foundPlayer = null;
            $isUuid = preg_match('/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i', $identifier);

            if ($cacheData && !empty($cacheData['player_list'])) {
                foreach ($cacheData['player_list'] as $p) {
                    if (is_array($p)) {
                        if ((isset($p['uuid']) && strcasecmp($p['uuid'], $identifier) === 0) ||
                            (isset($p['name']) && strcasecmp($p['name'], $identifier) === 0)) {
                            $foundPlayer = $p;
                            break;
                        }
                    } elseif (is_string($p) && strcasecmp($p, $identifier) === 0) {
                        $foundPlayer = ['name' => $p, 'uuid' => null];
                        break;
                    }
                }
            }

            if ($foundPlayer || $isUuid) {
                $pName = $foundPlayer['name'] ?? ($isUuid ? 'Player' : $identifier);
                $pUuid = $foundPlayer['uuid'] ?? ($isUuid ? $identifier : null);

                $account = MinecraftAccount::create([
                    'minecraft_username' => $pName,
                    'minecraft_uuid' => $pUuid,
                    'edition' => 'JAVA',
                    'auth_mode' => 'JAVA_ONLINE',
                    'rank' => 'wanderer',
                    'rank_display' => 'Wanderer',
                    'kingdom' => 'NONE',
                    'kingdom_display' => 'Belum Memilih',
                    'level' => 1,
                    'xp' => 0,
                    'required_xp' => 500,
                    'balance_rupiah' => 0,
                    'balance_diamond' => 0,
                    'battlepass_tier' => 1,
                    'battlepass_xp' => 0,
                    'battlepass_required_xp' => 100,
                    'battlepass_has_premium' => false,
                    'apex_coins' => 0,
                    'last_seen_at' => now(),
                    'user_id' => null,
                    'verified_at' => null,
                ]);
            } else {
                abort(404, 'Karakter peradaban tidak ditemukan di Apexsions.');
            }
        }

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
