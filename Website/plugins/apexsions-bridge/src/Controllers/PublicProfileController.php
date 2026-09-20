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

                $isBedrock = str_starts_with($pName, '.')
                    || str_starts_with($pName, '*')
                    || ($pUuid && str_starts_with($pUuid, '00000000-0000-0000-'));

                $account = MinecraftAccount::create([
                    'minecraft_username' => $pName,
                    'minecraft_uuid' => $pUuid,
                    'edition' => $isBedrock ? 'BEDROCK' : 'JAVA',
                    'auth_mode' => $isBedrock ? 'BEDROCK_FLOODGATE' : 'JAVA_ONLINE',
                    'floodgate_uuid' => $isBedrock ? $pUuid : null,
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
            'avatarBody' => $this->resolveAvatarBody($account),
        ]);
    }

    /**
     * Resolves the body-render image URL for the profile.
     *
     * Java Edition uses mc-heads by UUID/name. Bedrock Edition players have no
     * Mojang profile, so the real skin texture is resolved through the Geyser
     * skin API using the stored XUID, then rendered from the Mojang texture CDN.
     * The resolved texture is cached to avoid hammering the external API.
     */
    private function resolveAvatarBody(MinecraftAccount $account): string
    {
        $isBedrock = strtoupper($account->edition ?? '') === 'BEDROCK';

        if (!$isBedrock) {
            $id = $account->minecraft_uuid ?: $account->minecraft_username;
            return "https://mc-heads.net/body/{$id}/right";
        }

        $xuid = $account->xuid;
        if (!$xuid) {
            return 'https://mc-heads.net/body/MHF_Steve/right';
        }

        $texture = cache()->remember("apexsions.bedrock.skin.{$xuid}", now()->addHours(6), function () use ($xuid) {
            return $this->fetchGeyserTexture($xuid);
        });

        if (!$texture) {
            return 'https://mc-heads.net/body/MHF_Steve/right';
        }

        // mc-heads accepts a texture hash as the identifier and renders the body.
        return "https://mc-heads.net/body/{$texture}/right";
    }

    /**
     * Calls the Geyser skin API and returns the skin texture hash, or null.
     */
    private function fetchGeyserTexture(string $xuid): ?string
    {
        try {
            $response = \Illuminate\Support\Facades\Http::timeout(5)
                ->acceptJson()
                ->get("https://api.geysermc.org/v2/skin/{$xuid}");

            if (!$response->successful()) {
                return null;
            }

            $data = $response->json();
            return $data['texture_id'] ?? $data['texture'] ?? null;
        } catch (\Throwable $e) {
            report($e);
            return null;
        }
    }
}
