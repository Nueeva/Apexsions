<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Models\Setting;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\Plugin\ApexsionsBridge\Models\RankConfig;
use Azuriom\Plugin\ApexsionsBridge\Models\RankPurchase;
use Azuriom\Plugin\ApexsionsBridge\Services\RankService;
use Azuriom\PluginShop\Models\Package;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class RankAdminController extends Controller
{
    /**
     * Display the official rank hierarchy, metadata, and player counts.
     */
    public function index(): View
    {
        RankConfig::seedDefaultsIfEmpty();
        $ranks = RankService::getAllRanks();
        $playerCounts = RankService::getPlayerCounts();
        $totalPlayers = MinecraftAccount::count();
        $trialCount = MinecraftAccount::where(function ($q) {
            $q->where('rank_type', 'TRIAL')->orWhere('rank_type', 'trial');
        })->where('rank', '!=', 'wanderer')->count();

        $recentPurchases = RankPurchase::orderBy('id', 'desc')->limit(5)->get();

        return view('apexsions-bridge::admin.ranks.index', [
            'ranks' => $ranks,
            'playerCounts' => $playerCounts,
            'totalPlayers' => $totalPlayers,
            'trialCount' => $trialCount,
            'recentPurchases' => $recentPurchases,
        ]);
    }

    /**
     * Show form to edit rank configuration, pricing, and benefits.
     */
    public function edit(string $rank_key): View
    {
        RankConfig::seedDefaultsIfEmpty();
        $normalized = strtolower(trim($rank_key));
        $config = RankConfig::where('rank_key', $normalized)->first();
        $defaults = RankConfig::getDefaultsForRank($normalized) ?? [];

        if (!$config) {
            $meta = RankService::getRank($normalized);
            if (!$meta && empty($defaults)) {
                abort(404, "Rank '{$rank_key}' tidak ditemukan.");
            }

            $createData = array_merge([
                'rank_key' => $normalized,
                'display_name' => $meta['display_name'] ?? ucfirst($normalized),
                'badge' => $meta['badge'] ?? null,
                'prefix' => $meta['prefix'] ?? null,
                'color' => $meta['color'] ?? '#ffd700',
                'tier' => $meta['tier'] ?? 'Tier II',
                'weight' => $meta['weight'] ?? 10,
                'order_index' => 10,
                'is_active' => true,
                'is_buyable' => false,
                'description' => $meta['description'] ?? null,
            ], $defaults);

            $config = RankConfig::create($createData);
        } else {
            // Fill any null/empty in-memory properties from official defaults
            foreach ($defaults as $k => $v) {
                if ((is_null($config->{$k}) || $config->{$k} === '') && !is_null($v)) {
                    $config->{$k} = $v;
                }
            }
        }

        return view('apexsions-bridge::admin.ranks.edit', [
            'config' => $config,
            'defaults' => $defaults,
            'rank_key' => $normalized,
        ]);
    }

    /**
     * Update rank configuration, pricing, and in-game benefits.
     */
    public function update(Request $request, string $rank_key): RedirectResponse
    {
        $normalized = strtolower(trim($rank_key));
        $config = RankConfig::where('rank_key', $normalized)->firstOrFail();

        $validated = $request->validate([
            'display_name' => ['required', 'string', 'max:64'],
            'badge' => ['nullable', 'string', 'max:64'],
            'prefix' => ['nullable', 'string', 'max:64'],
            'color' => ['required', 'string', 'max:16'],
            'tier' => ['required', 'string', 'max:32'],
            'weight' => ['required', 'integer', 'min:0', 'max:100'],
            'order_index' => ['required', 'integer', 'min:0'],
            'is_active' => ['nullable', 'boolean'],
            'is_buyable' => ['nullable', 'boolean'],
            'banner_image' => ['nullable', 'string', 'max:255'],
            'description' => ['nullable', 'string'],

            // Pricing
            'price_trial_30' => ['required', 'numeric', 'min:0'],
            'price_trial_90' => ['required', 'numeric', 'min:0'],
            'price_permanent' => ['required', 'numeric', 'min:0'],
            'price_upgrade_override' => ['nullable', 'numeric', 'min:0'],
            'discount_percent' => ['nullable', 'numeric', 'min:0', 'max:100'],
            'money_reward_permanent' => ['required', 'numeric', 'min:0'],

            // Benefits
            'benefit_max_homes' => ['required', 'integer', 'min:1', 'max:100'],
            'benefit_max_auctions' => ['required', 'integer', 'min:1', 'max:100'],
            'benefit_max_enchants' => ['required', 'integer', 'min:1', 'max:100'],
            'benefit_rtp_cooldown' => ['required', 'integer', 'min:5', 'max:3600'],
            'benefit_shop_sell_bonus' => ['required', 'numeric', 'min:0', 'max:100'],
            'benefit_xp_bonus' => ['required', 'numeric', 'min:0', 'max:100'],
            'benefit_bank_multiplier' => ['required', 'numeric', 'min:1.0', 'max:10.0'],
            'benefit_feed_cooldown' => ['nullable', 'integer', 'min:0'],
            'benefit_commands_raw' => ['nullable', 'string'],
            'benefit_kits_raw' => ['nullable', 'string'],
            'benefit_nick_permission' => ['required', 'string', 'in:none,no_color,solid_color,all_colors'],
            'benefit_battlepass_unlock' => ['nullable', 'string', 'in:sio,exsio'],
            'benefit_battlepass_discount' => ['nullable', 'numeric', 'min:0', 'max:100'],
        ]);

        // Parse commands and kits arrays
        $commands = [];
        if (!empty($validated['benefit_commands_raw'])) {
            $lines = preg_split('/[\r\n,]+/', $validated['benefit_commands_raw']);
            foreach ($lines as $line) {
                $trim = trim($line);
                if (!empty($trim)) {
                    $commands[] = $trim;
                }
            }
        }

        $kits = [];
        if (!empty($validated['benefit_kits_raw'])) {
            $lines = preg_split('/[\r\n,]+/', $validated['benefit_kits_raw']);
            foreach ($lines as $line) {
                $trim = trim($line);
                if (!empty($trim)) {
                    $kits[] = $trim;
                }
            }
        }

        $config->update([
            'display_name' => $validated['display_name'],
            'badge' => $validated['badge'],
            'prefix' => $validated['prefix'],
            'color' => $validated['color'],
            'tier' => $validated['tier'],
            'weight' => (int) $validated['weight'],
            'order_index' => (int) $validated['order_index'],
            'is_active' => $request->has('is_active'),
            'is_buyable' => $request->has('is_buyable'),
            'banner_image' => $validated['banner_image'],
            'description' => $validated['description'],
            'price_trial_30' => (float) $validated['price_trial_30'],
            'price_trial_90' => (float) $validated['price_trial_90'],
            'price_permanent' => (float) $validated['price_permanent'],
            'price_upgrade_override' => !empty($validated['price_upgrade_override']) ? (float) $validated['price_upgrade_override'] : null,
            'discount_percent' => (float) ($validated['discount_percent'] ?? 0),
            'money_reward_permanent' => (float) $validated['money_reward_permanent'],
            'benefit_max_homes' => (int) $validated['benefit_max_homes'],
            'benefit_max_auctions' => (int) $validated['benefit_max_auctions'],
            'benefit_max_enchants' => (int) $validated['benefit_max_enchants'],
            'benefit_rtp_cooldown' => (int) $validated['benefit_rtp_cooldown'],
            'benefit_shop_sell_bonus' => (float) $validated['benefit_shop_sell_bonus'],
            'benefit_xp_bonus' => (float) $validated['benefit_xp_bonus'],
            'benefit_bank_multiplier' => (float) $validated['benefit_bank_multiplier'],
            'benefit_feed_cooldown' => !empty($validated['benefit_feed_cooldown']) ? (int) $validated['benefit_feed_cooldown'] : null,
            'benefit_commands' => $commands,
            'benefit_kits' => $kits,
            'benefit_nick_permission' => $validated['benefit_nick_permission'],
            'benefit_battlepass_unlock' => $validated['benefit_battlepass_unlock'] ?? null,
            'benefit_battlepass_discount' => (float) ($validated['benefit_battlepass_discount'] ?? 0),
        ]);

        // Sync with Azuriom Shop package prices if installed
        try {
            if (class_exists(Package::class)) {
                Package::where('name', 'LIKE', "{$validated['display_name']} (Trial 30 Hari)%")->update(['price' => $validated['price_trial_30']]);
                Package::where('name', 'LIKE', "{$validated['display_name']} (Trial 90 Hari)%")->update(['price' => $validated['price_trial_90']]);
                Package::where('name', 'LIKE', "{$validated['display_name']} (Permanen)%")->update(['price' => $validated['price_permanent']]);
            }
        } catch (\Throwable $ignored) {}

        return redirect()->route('apexsions-bridge.admin.ranks.index')->with('success', "Konfigurasi rank '{$config->display_name}' berhasil diperbarui.");
    }

    /**
     * Show general settings for WhatsApp integration and BattlePass discounts.
     */
    public function settings(): View
    {
        return view('apexsions-bridge::admin.ranks.settings', [
            'whatsapp_numbers' => setting('apexsions.whatsapp.admin_numbers', '6281212994597,6285883161047,6287729112281'),
            'template_standard' => setting('apexsions.whatsapp.template_standard', 'Min, aku mau beli {nama produk} yang harganya Rp.{harga produk}'),
            'template_battlepass_discount' => setting('apexsions.whatsapp.template_battlepass_discount', 'Min, aku mau beli battlepass {nama pass} yang harganya Rp.{harga produk} karna aku udah beli rank {rank} jadi harganya segitu'),
            'template_upgrade' => setting('apexsions.whatsapp.template_upgrade', 'Min, aku mau upgrade rank dari {rank lama} ke {rank baru} yang harganya Rp.{harga upgrade}'),
            'battlepass_season' => setting('apexsions.battlepass.season', '1'),
            'emperor_discount' => setting('apexsions.battlepass.emperor_discount', '10'),
            'sions_discount' => setting('apexsions.battlepass.sions_discount', '15'),
            'sio_price' => setting('apexsions.battlepass.sio_price', '45000'),
            'exsio_price' => setting('apexsions.battlepass.exsio_price', '85000'),
        ]);
    }

    /**
     * Save WhatsApp and BattlePass global settings.
     */
    public function updateSettings(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'whatsapp_numbers' => ['required', 'string'],
            'template_standard' => ['required', 'string'],
            'template_battlepass_discount' => ['required', 'string'],
            'template_upgrade' => ['required', 'string'],
            'battlepass_season' => ['required', 'string', 'max:16'],
            'emperor_discount' => ['required', 'numeric', 'min:0', 'max:100'],
            'sions_discount' => ['required', 'numeric', 'min:0', 'max:100'],
            'sio_price' => ['required', 'numeric', 'min:0'],
            'exsio_price' => ['required', 'numeric', 'min:0'],
        ]);

        Setting::updateSettings([
            'apexsions.whatsapp.admin_numbers' => $validated['whatsapp_numbers'],
            'apexsions.whatsapp.template_standard' => $validated['template_standard'],
            'apexsions.whatsapp.template_battlepass_discount' => $validated['template_battlepass_discount'],
            'apexsions.whatsapp.template_upgrade' => $validated['template_upgrade'],
            'apexsions.battlepass.season' => $validated['battlepass_season'],
            'apexsions.battlepass.emperor_discount' => $validated['emperor_discount'],
            'apexsions.battlepass.sions_discount' => $validated['sions_discount'],
            'apexsions.battlepass.sio_price' => $validated['sio_price'],
            'apexsions.battlepass.exsio_price' => $validated['exsio_price'],
        ]);

        // Also update Shop package prices for Sio Pass and Exsio Pass
        try {
            if (class_exists(Package::class)) {
                Package::where('name', 'LIKE', 'Sio Pass%')->update(['price' => $validated['sio_price']]);
                Package::where('name', 'LIKE', 'Exsio Pass%')->update(['price' => $validated['exsio_price']]);
            }
        } catch (\Throwable $ignored) {}

        return back()->with('success', 'Pengaturan WhatsApp dan BattlePass berhasil diperbarui.');
    }

    /**
     * Show transaction, purchase, and rank upgrade history.
     */
    public function purchases(Request $request): View
    {
        $query = RankPurchase::with('minecraftAccount');

        if ($request->filled('rank')) {
            $query->where('rank', strtolower(trim($request->input('rank'))));
        }

        if ($request->filled('rank_type')) {
            $query->where('rank_type', strtoupper(trim($request->input('rank_type'))));
        }

        if ($request->filled('is_upgrade')) {
            $query->where('is_upgrade', (bool) $request->input('is_upgrade'));
        }

        if ($request->filled('search')) {
            $term = '%' . trim($request->input('search')) . '%';
            $query->where(function ($q) use ($term) {
                $q->where('minecraft_username', 'LIKE', $term)
                  ->orWhere('minecraft_uuid', 'LIKE', $term)
                  ->orWhere('notes', 'LIKE', $term);
            });
        }

        $purchases = $query->orderBy('id', 'desc')->paginate(25)->withQueryString();
        $ranks = RankService::getAllRanks();

        return view('apexsions-bridge::admin.ranks.purchases', [
            'purchases' => $purchases,
            'ranks' => $ranks,
        ]);
    }

    /**
     * Show detailed view of a specific rank and its member list.
     */
    public function show(string $rank_key, Request $request): View
    {
        $normalized = strtolower(trim($rank_key));
        $rank = RankService::getRank($normalized);

        if (!$rank) {
            abort(404, "Rank '{$rank_key}' tidak ditemukan.");
        }

        $search = trim((string) $request->input('q', ''));
        $query = MinecraftAccount::whereRaw('LOWER(rank) = ?', [$normalized]);

        if (!empty($search)) {
            $term = '%' . $search . '%';
            $query->where(function ($q) use ($term) {
                $q->where('minecraft_username', 'LIKE', $term)
                  ->orWhere('minecraft_uuid', 'LIKE', $term);
            });
        }

        $players = $query->orderBy('last_seen_at', 'desc')
            ->orderBy('id', 'desc')
            ->paginate(25)
            ->withQueryString();

        $allRanks = RankService::getAllRanks();

        return view('apexsions-bridge::admin.ranks.show', [
            'rank' => $rank,
            'players' => $players,
            'search' => $search,
            'allRanks' => $allRanks,
        ]);
    }

    /**
     * Handle rank assignment, upgrade, or manual grant form submission.
     */
    public function assign(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'player_identifier' => ['required', 'string', 'max:100'],
            'rank' => ['required', 'string'],
            'rank_type' => ['nullable', 'string', 'in:PERMANENT,TRIAL,permanent,trial'],
            'duration_days' => ['nullable', 'integer', 'min:1', 'max:365'],
            'reason' => ['required', 'string', 'min:3', 'max:250'],
            'is_upgrade' => ['nullable', 'boolean'],
        ]);

        $account = MinecraftAccount::where('minecraft_uuid', $validated['player_identifier'])
            ->orWhere('minecraft_username', $validated['player_identifier'])
            ->first();

        if (!$account) {
            return back()->with('error', "Pemain dengan identifier '{$validated['player_identifier']}' tidak ditemukan di basis data.");
        }

        $targetRank = strtolower(trim($validated['rank']));
        $rankType = strtoupper($validated['rank_type'] ?? 'PERMANENT');

        if ($request->boolean('is_upgrade')) {
            $result = RankService::executeRankUpgrade(
                $request->user(),
                $account,
                $targetRank,
                0.0,
                'ADMIN_UPGRADE'
            );
        } else {
            $durationDays = ($rankType === 'TRIAL') ? (int) ($validated['duration_days'] ?? 30) : null;
            $result = RankService::assignRank(
                $request->user(),
                $account,
                $targetRank,
                $validated['reason'],
                $rankType,
                $durationDays,
                0.0,
                'ADMIN'
            );
        }

        if ($result['success']) {
            return back()->with('success', $result['message']);
        }

        return back()->with('error', $result['message']);
    }

    /**
     * Trigger manual trial rank expiration check.
     */
    public function expireTrials(): RedirectResponse
    {
        $expiredCount = RankService::checkAndExpireTrials();

        return back()->with('success', "Pemeriksaan selesai. Sebanyak {$expiredCount} rank trial yang kedaluwarsa telah diproses dan dikembalikan ke rank semestinya.");
    }
}
