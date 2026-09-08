<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\Punishment;
use Azuriom\Plugin\ApexsionsBridge\Models\Report;
use Azuriom\Plugin\ApexsionsBridge\Services\ModerationService;
use Carbon\Carbon;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class ModerationAdminController extends Controller
{
    /**
     * Display Moderation Center dashboard overview.
     */
    public function index(Request $request): View
    {
        $typeFilter = $request->input('type', 'all');

        $activeQuery = Punishment::active()->orderBy('created_at', 'desc');
        if (!empty($typeFilter) && $typeFilter !== 'all') {
            $activeQuery->filterType($typeFilter);
        }
        $activePunishments = $activeQuery->paginate(15, ['*'], 'active_page')->withQueryString();

        $recentPunishments = Punishment::orderBy('created_at', 'desc')->limit(20)->get();

        // Statistics
        $activeBans = Punishment::active()->where('type', 'BAN')->count();
        $activeMutes = Punishment::active()->where('type', 'MUTE')->count();
        $totalWarns30d = Punishment::where('type', 'WARN')->where('created_at', '>=', Carbon::now()->subDays(30))->count();
        $openReports = Report::whereIn('status', ['OPEN', 'REVIEWING'])->count();
        $recentActions24h = Punishment::where('created_at', '>=', Carbon::now()->subDay())->count();

        return view('apexsions-bridge::admin.moderation.index', [
            'activePunishments' => $activePunishments,
            'recentPunishments' => $recentPunishments,
            'selectedType' => $typeFilter,
            'activeBans' => $activeBans,
            'activeMutes' => $activeMutes,
            'totalWarns30d' => $totalWarns30d,
            'openReports' => $openReports,
            'recentActions24h' => $recentActions24h,
        ]);
    }

    /**
     * Execute a staff moderation action (WARN, MUTE, KICK, BAN).
     */
    public function storeAction(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player' => ['required', 'string', 'min:2', 'max:64'],
            'type' => ['required', 'in:WARN,MUTE,KICK,BAN'],
            'reason' => ['required', 'string', 'min:3', 'max:500'],
            'duration' => ['nullable', 'integer', 'min:1'],
        ]);

        $identifier = trim($validated['player']);
        $account = MinecraftAccount::where('minecraft_uuid', $identifier)
            ->orWhere('minecraft_username', $identifier)
            ->first();

        $playerUuid = $account ? $account->minecraft_uuid : (str_contains($identifier, '-') ? $identifier : (string) \Illuminate\Support\Str::uuid());
        $playerName = $account ? $account->minecraft_username : $identifier;

        $type = strtoupper($validated['type']);
        $reason = trim($validated['reason']);
        $duration = !empty($validated['duration']) ? (int) $validated['duration'] : null;
        $staff = $request->user();

        switch ($type) {
            case 'WARN':
                ModerationService::warn($playerUuid, $playerName, $reason, $staff);
                $msg = "Peringatan resmi berhasil dikirim ke {$playerName}.";
                break;
            case 'MUTE':
                ModerationService::mute($playerUuid, $playerName, $duration ?? 10, $reason, $staff);
                $durStr = $duration ? "selama {$duration} menit" : "permanen";
                $msg = "Pemain {$playerName} berhasil di-mute {$durStr}.";
                break;
            case 'KICK':
                ModerationService::kick($playerUuid, $playerName, $reason, $staff);
                $msg = "Pemain {$playerName} berhasil di-kick dari server.";
                break;
            case 'BAN':
                ModerationService::ban($playerUuid, $playerName, $duration, $reason, $staff);
                $durStr = $duration ? "selama {$duration} jam" : "secara permanen";
                $msg = "Pemain {$playerName} berhasil di-ban {$durStr}.";
                break;
            default:
                return redirect()->back()->with('error', 'Tipe hukuman tidak valid.');
        }

        return redirect()->back()->with('success', $msg);
    }

    /**
     * Pardon / revoke an active punishment.
     */
    public function pardon(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'reason' => ['nullable', 'string', 'max:255'],
        ]);

        $punishment = Punishment::findOrFail($id);
        $reason = $validated['reason'] ?? 'Dicabut oleh Administrator via Web Dashboard';

        ModerationService::pardon($punishment, $reason, $request->user());

        return redirect()->back()->with('success', "Sanksi #{$id} ({$punishment->type} untuk {$punishment->player_name}) berhasil dicabut.");
    }
}
