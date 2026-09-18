<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Services\MarketAdminService;
use Azuriom\Plugin\ApexsionsBridge\Services\ServerOpsService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class MarketAdminController extends Controller
{
    /**
     * Display the Kingdom Market & Shop administration desk.
     */
    public function index(Request $request): View
    {
        $settings = MarketAdminService::getSettings();
        $categories = MarketAdminService::getItemsByCategory();
        $allItems = MarketAdminService::getItems();

        $totalItems = count($allItems);
        $totalBuy = array_sum(array_column($allItems, 'buy_price'));
        $totalSell = array_sum(array_column($allItems, 'sell_price'));
        $avgSellRatio = $totalBuy > 0 ? round(($totalSell / $totalBuy) * 100, 1) : 20.0;
        $activeCategory = $request->input('category', 'ores');

        // Server bridge status
        $serverStatus = ServerOpsService::getServerStatus();

        return view('apexsions-bridge::admin.market.index', [
            'settings' => $settings,
            'categories' => $categories,
            'allItems' => $allItems,
            'totalItems' => $totalItems,
            'avgSellRatio' => $avgSellRatio,
            'activeCategory' => $activeCategory,
            'serverStatus' => $serverStatus,
        ]);
    }

    /**
     * Update Kingdom market modifiers, tax rates, and ore ratios.
     */
    public function updateKingdoms(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'kingdoms' => ['required', 'array'],
            'kingdoms.SOLTERRA.tax_percent' => ['required', 'numeric', 'min:0', 'max:100'],
            'kingdoms.SOLTERRA.ores_buy_multiplier' => ['required', 'numeric', 'min:0.1', 'max:5.0'],
            'kingdoms.SOLTERRA.ores_sell_ratio' => ['required', 'numeric', 'min:0.01', 'max:1.0'],
            'kingdoms.ZENITHAR.tax_percent' => ['required', 'numeric', 'min:0', 'max:100'],
            'kingdoms.ZENITHAR.blocks_buy_multiplier' => ['required', 'numeric', 'min:0.1', 'max:5.0'],
            'kingdoms.ZENITHAR.volatility_multiplier' => ['required', 'numeric', 'min:0.1', 'max:5.0'],
            'kingdoms.SYLVAMOOR.tax_percent' => ['required', 'numeric', 'min:0', 'max:100'],
            'kingdoms.SYLVAMOOR.farming_buy_multiplier' => ['required', 'numeric', 'min:0.1', 'max:5.0'],
            'kingdoms.SYLVAMOOR.dyes_buy_multiplier' => ['required', 'numeric', 'min:0.1', 'max:5.0'],
        ]);

        $result = MarketAdminService::updateKingdoms($validated['kingdoms'], $request->user());

        return redirect()->route('apexsions-bridge.admin.market.index', ['tab' => 'kingdoms'])
            ->with('success', $result['message']);
    }

    /**
     * Update dynamic market saturation, weather, and clamping bounds.
     */
    public function updateDynamics(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'supply_market.enabled' => ['nullable'],
            'supply_market.sensitivity' => ['required', 'numeric', 'min:0.001', 'max:0.5'],
            'supply_market.base_volume_threshold' => ['required', 'integer', 'min:64', 'max:100000'],
            'supply_market.max_saturation_drop' => ['required', 'numeric', 'min:0.01', 'max:0.8'],
            'supply_market.min_sell_multiplier' => ['required', 'numeric', 'min:0.1', 'max:1.0'],
            'supply_market.recovery_interval_minutes' => ['required', 'integer', 'min:1', 'max:120'],
            'supply_market.recovery_percent_per_interval' => ['required', 'numeric', 'min:1', 'max:100'],
            'clamping.min_buy_ratio' => ['required', 'numeric', 'min:0.1', 'max:1.0'],
            'clamping.max_buy_ratio' => ['required', 'numeric', 'min:1.0', 'max:3.0'],
            'clamping.min_sell_ratio' => ['required', 'numeric', 'min:0.1', 'max:1.0'],
            'clamping.max_sell_ratio' => ['required', 'numeric', 'min:1.0', 'max:3.0'],
            'weather.enabled' => ['nullable'],
            'weather.clear.farming_sell_multiplier' => ['required', 'numeric', 'min:0.1', 'max:3.0'],
            'weather.thunder.mob_sell_multiplier' => ['required', 'numeric', 'min:0.1', 'max:3.0'],
        ]);

        $payload = [
            'supply_market' => [
                'enabled' => $request->has('supply_market.enabled'),
                'sensitivity' => (float) $validated['supply_market']['sensitivity'],
                'base_volume_threshold' => (int) $validated['supply_market']['base_volume_threshold'],
                'max_saturation_drop' => (float) $validated['supply_market']['max_saturation_drop'],
                'min_sell_multiplier' => (float) $validated['supply_market']['min_sell_multiplier'],
                'recovery_interval_minutes' => (int) $validated['supply_market']['recovery_interval_minutes'],
                'recovery_percent_per_interval' => (float) $validated['supply_market']['recovery_percent_per_interval'],
            ],
            'clamping' => [
                'min_buy_ratio' => (float) $validated['clamping']['min_buy_ratio'],
                'max_buy_ratio' => (float) $validated['clamping']['max_buy_ratio'],
                'min_sell_ratio' => (float) $validated['clamping']['min_sell_ratio'],
                'max_sell_ratio' => (float) $validated['clamping']['max_sell_ratio'],
            ],
            'weather' => [
                'enabled' => $request->has('weather.enabled'),
                'clear' => ['farming_sell_multiplier' => (float) $validated['weather']['clear']['farming_sell_multiplier']],
                'thunder' => ['mob_sell_multiplier' => (float) $validated['weather']['thunder']['mob_sell_multiplier']],
            ],
        ];

        $result = MarketAdminService::updateDynamics($payload, $request->user());

        return redirect()->route('apexsions-bridge.admin.market.index', ['tab' => 'dynamics'])
            ->with('success', $result['message']);
    }

    /**
     * Update an individual shop item's pricing or availability.
     */
    public function updateItem(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'item_id' => ['required', 'string', 'max:64'],
            'buy_price' => ['required', 'numeric', 'min:0.1'],
            'sell_price' => ['required', 'numeric', 'min:0.05'],
            'buy_enabled' => ['nullable'],
            'display_name' => ['nullable', 'string', 'max:64'],
        ]);

        $payload = [
            'buy_price' => (float) $validated['buy_price'],
            'sell_price' => (float) $validated['sell_price'],
            'buy_enabled' => $request->has('buy_enabled'),
            'display_name' => $validated['display_name'] ?? null,
        ];

        $result = MarketAdminService::updateItem($validated['item_id'], $payload, $request->user());

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }

    /**
     * Execute batch price adjustment across a category or entire catalog.
     */
    public function batchUpdateItems(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'category' => ['required', 'string'],
            'type' => ['required', 'in:ratio,percent'],
            'value' => ['required', 'numeric'],
        ]);

        $result = MarketAdminService::batchUpdateItems($validated, $request->user());

        return redirect()->route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => $validated['category']])
            ->with('success', $result['message']);
    }

    /**
     * Trigger immediate live WebBridge sync to game server.
     */
    public function syncNow(Request $request): RedirectResponse
    {
        MarketAdminService::dispatchServerReload('Manual live reload dispatched by ' . ($request->user() ? $request->user()->name : 'Admin'));

        return redirect()->back()->with('success', 'Perintah reload pasar telah dikirim ke antrean WebBridge server Minecraft.');
    }

    /**
     * Reset market settings to official defaults.
     */
    public function resetDefaults(Request $request): RedirectResponse
    {
        $result = MarketAdminService::resetDefaults($request->user());

        return redirect()->route('apexsions-bridge.admin.market.index')
            ->with('success', $result['message']);
    }

    /**
     * API Endpoint for Minecraft server to fetch latest dynamic market configuration.
     */
    public function getShopConfig(Request $request): JsonResponse
    {
        $apiKey = $request->header('X-Apexsions-Key');
        $expectedKey = config('plugins.apexsions-bridge.api_key', 'apexsions_bridge_key_live_2026');

        if ($apiKey && $apiKey !== $expectedKey) {
            return response()->json(['status' => 'error', 'message' => 'Invalid API key.'], 401);
        }

        $config = MarketAdminService::compileFullConfig();

        return response()->json($config);
    }
}
