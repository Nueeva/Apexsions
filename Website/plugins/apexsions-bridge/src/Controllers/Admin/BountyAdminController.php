<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Bounty;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class BountyAdminController extends Controller
{
    /**
     * Admin oversight of active player bounties (mirrors the in-game snapshot).
     */
    public function index(): View
    {
        $bounties = Bounty::query()->top(200)->get();

        return view('apexsions-bridge::admin.bounties.index', [
            'bounties' => $bounties,
            'totalPool' => (float) Bounty::query()->sum('total_amount'),
            'lastSyncedAt' => Bounty::lastSyncedAt(),
        ]);
    }

    /**
     * Force-clear a target's bounty via the in-game console command.
     *
     * The snapshot row is intentionally NOT deleted here: the game remains the
     * source of truth and will prune the row itself once the clear succeeds and
     * the next snapshot is pushed.
     */
    public function clear(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'target_name' => ['required', 'string', 'max:64'],
        ]);

        $targetName = $validated['target_name'];
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Web Console';

        $bounty = Bounty::where('target_name', $targetName)->first();

        Delivery::create([
            'action_id' => (string) Str::uuid(),
            'idempotency_key' => 'BOUNTY_CLEAR_' . $targetName . '_' . time(),
            'player_uuid' => $bounty->target_uuid ?? '00000000-0000-0000-0000-000000000000',
            'player_username' => $targetName,
            'command' => "bounty admin clear {$targetName}",
            'status' => 'PENDING',
        ]);

        return back()->with('success', "Perintah pembersihan bounty untuk {$targetName} telah dikirim ke server (oleh {$actorName}).");
    }
}