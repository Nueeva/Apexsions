<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Claim;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Services\AuditService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\View\View;

class ClaimAdminController extends Controller
{
    /**
     * Display Land Claims & Anti-Grief Territory overview.
     */
    public function index(Request $request): View
    {
        $search = trim($request->input('search', ''));
        $world = $request->input('world', 'all');

        $query = Claim::query();

        if (!empty($search)) {
            $query->where(function ($q) use ($search) {
                $q->where('owner_name', 'like', "%{$search}%")
                  ->orWhere('owner_uuid', 'like', "%{$search}%")
                  ->orWhere('claim_id', 'like', "%{$search}%");
            });
        }

        if (!empty($world) && $world !== 'all') {
            $query->where('world', $world);
        }

        $claims = $query->orderBy('created_at', 'desc')->paginate(20)->withQueryString();

        // Statistics
        $totalClaims = Claim::count();
        $uniqueOwners = Claim::distinct('owner_uuid')->count('owner_uuid');
        $availableWorlds = Claim::distinct('world')->pluck('world')->toArray();
        if (empty($availableWorlds)) {
            $availableWorlds = ['world', 'world_nether', 'world_the_end'];
        }

        $topOwners = Claim::selectRaw('owner_name, owner_uuid, count(*) as count')
            ->groupBy('owner_name', 'owner_uuid')
            ->orderByDesc('count')
            ->limit(5)
            ->get();

        return view('apexsions-bridge::admin.claims.index', [
            'claims' => $claims,
            'totalClaims' => $totalClaims,
            'uniqueOwners' => $uniqueOwners,
            'availableWorlds' => $availableWorlds,
            'selectedWorld' => $world,
            'search' => $search,
            'topOwners' => $topOwners,
        ]);
    }

    /**
     * Administratively revoke and force-unclaim a chunk.
     */
    public function unclaim(Request $request, int $id): RedirectResponse
    {
        $claim = Claim::findOrFail($id);
        $reason = $request->input('reason', 'Pencabutan otoritatif oleh Administrator Web.');
        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Web Console';

        // Dispatch console command to Minecraft server
        $cmd = "claim admin unclaim {$claim->world} {$claim->chunk_x} {$claim->chunk_z}";

        Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'UNCLAIM_' . $claim->claim_id . '_' . time(),
            'player_uuid' => $claim->owner_uuid,
            'player_username' => $claim->owner_name,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        AuditService::start(
            'CLAIM_REVOKED',
            'TERRITORY',
            $claim->claim_id,
            "Chunk [{$claim->chunk_x}, {$claim->chunk_z}] ({$claim->world})",
            $reason,
            $claim->owner_name,
            [
                'action_id' => $actionId,
                'world' => $claim->world,
                'chunk_x' => $claim->chunk_x,
                'chunk_z' => $claim->chunk_z,
                'owner_uuid' => $claim->owner_uuid,
                'owner_name' => $claim->owner_name,
                'staff' => $actorName,
            ],
            'WEB'
        );

        $details = "Chunk [{$claim->chunk_x}, {$claim->chunk_z}] milik {$claim->owner_name}";
        $claim->delete();

        return redirect()->back()->with('success', "Klaim tanah {$details} berhasil dicabut dan dikembalikan ke status netral.");
    }
}
