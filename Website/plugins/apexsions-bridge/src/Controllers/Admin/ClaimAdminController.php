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
     * Display Land Claims, Progressive Tax & Anti-Grief Territory overview.
     */
    public function index(Request $request): View
    {
        $search = trim($request->input('search', ''));
        $world = $request->input('world', 'all');
        $status = $request->input('status', 'all');
        $kingdom = $request->input('kingdom', 'all');

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

        if (!empty($status) && $status !== 'all') {
            $query->where('status', $status);
        }

        if (!empty($kingdom) && $kingdom !== 'all') {
            $query->where('kingdom_id', $kingdom);
        }

        $claims = $query->orderBy('created_at', 'desc')->paginate(20)->withQueryString();

        // Statistics
        $totalClaims = Claim::count();
        $activeClaimsCount = Claim::where('status', 'ACTIVE')->count();
        $inGracePeriodCount = Claim::where('status', 'GRACE_PERIOD')->count();
        $totalVaultBalance = Claim::sum('bank_balance');
        $uniqueOwners = Claim::distinct('owner_uuid')->count('owner_uuid');

        $availableWorlds = Claim::distinct('world')->pluck('world')->toArray();
        if (empty($availableWorlds)) {
            $availableWorlds = ['world', 'world_nether', 'world_the_end'];
        }

        $availableKingdoms = Claim::whereNotNull('kingdom_id')->where('kingdom_id', '!=', '')->distinct('kingdom_id')->pluck('kingdom_id')->toArray();

        $topOwners = Claim::selectRaw('owner_name, owner_uuid, count(*) as count, sum(bank_balance) as total_balance')
            ->groupBy('owner_name', 'owner_uuid')
            ->orderByDesc('count')
            ->limit(5)
            ->get();

        return view('apexsions-bridge::admin.claims.index', [
            'claims' => $claims,
            'totalClaims' => $totalClaims,
            'activeClaimsCount' => $activeClaimsCount,
            'inGracePeriodCount' => $inGracePeriodCount,
            'totalVaultBalance' => $totalVaultBalance,
            'uniqueOwners' => $uniqueOwners,
            'availableWorlds' => $availableWorlds,
            'selectedWorld' => $world,
            'availableKingdoms' => $availableKingdoms,
            'selectedKingdom' => $kingdom,
            'selectedStatus' => $status,
            'search' => $search,
            'topOwners' => $topOwners,
        ]);
    }

    /**
     * Administratively deposit funds into a territory's bank vault.
     */
    public function deposit(Request $request, int $id): RedirectResponse
    {
        $claim = Claim::findOrFail($id);
        $amount = (float) $request->input('amount', 0);

        if ($amount <= 0) {
            return redirect()->back()->with('error', 'Nominal suntikan dana harus lebih besar dari 0!');
        }

        $actionId = (string) Str::uuid();
        $actor = $request->user();
        $actorName = $actor ? ($actor->name ?? 'Administrator') : 'Web Console';

        $cmd = "claim admin deposit {$claim->owner_name} {$amount}";

        Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'CLAIM_DEP_' . $claim->claim_id . '_' . time(),
            'player_uuid' => $claim->owner_uuid,
            'player_username' => $claim->owner_name,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        $claim->increment('bank_balance', $amount);
        if ($claim->status === 'GRACE_PERIOD' && $claim->bank_balance >= $claim->daily_upkeep) {
            $claim->update(['status' => 'ACTIVE', 'grace_period_until' => null]);
        }

        AuditService::start(
            'CLAIM_DEPOSIT_INJECTED',
            'TERRITORY',
            $claim->claim_id,
            "Chunk [{$claim->chunk_x}, {$claim->chunk_z}] ({$claim->world})",
            "Suntikan dana Rp" . number_format($amount, 0, ',', '.') . " ke brankas klaim {$claim->owner_name}.",
            $claim->owner_name,
            ['amount' => $amount, 'new_balance' => $claim->bank_balance]
        );

        return redirect()->back()->with('success', "Berhasil menyuntikkan dana Rp" . number_format($amount, 0, ',', '.') . " ke brankas wilayah {$claim->owner_name}. Perintah game server telah dikirim!");
    }

    /**
     * Administratively trigger tax collection cycle on game server.
     */
    public function collectTax(Request $request): RedirectResponse
    {
        $actionId = (string) Str::uuid();
        $cmd = "claim admin collecttax";

        Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'TAX_CYCLE_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'CONSOLE',
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        AuditService::start(
            'CLAIM_TAX_CYCLE_TRIGGERED',
            'SYSTEM',
            $actionId,
            'Semua Wilayah',
            'Eksekusi manual siklus penagihan pajak wilayah dari Web Admin.',
            'SYSTEM',
            []
        );

        return redirect()->back()->with('success', "Perintah penagihan pajak seluruh wilayah telah dikirim ke game server!");
    }

    /**
     * Administratively sync all claims from in-game to web.
     */
    public function sync(Request $request): RedirectResponse
    {
        $actionId = (string) Str::uuid();
        $cmd = "claim admin sync";

        Delivery::create([
            'action_id' => $actionId,
            'idempotency_key' => 'CLAIM_SYNC_' . time(),
            'player_uuid' => '00000000-0000-0000-0000-000000000000',
            'player_username' => 'CONSOLE',
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        return redirect()->back()->with('success', "Permintaan sinkronisasi wilayah telah dikirim ke game server!");
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
            ]
        );

        $claim->delete();

        return redirect()->back()->with('success', "Klaim chunk [{$claim->chunk_x}, {$claim->chunk_z}] milik {$claim->owner_name} telah dicabut secara otoritatif!");
    }
}
