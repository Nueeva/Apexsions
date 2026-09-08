<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Auction;
use Azuriom\Plugin\ApexsionsBridge\Services\EconomyAdminService;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\View\View;

class AuctionAdminController extends Controller
{
    /**
     * Display Auction Inspector listing.
     */
    public function index(Request $request): View
    {
        $search = trim((string) $request->input('q', ''));
        $status = $request->input('status', 'all');
        $currency = $request->input('currency', 'all');

        $query = Auction::query()->with(['sellerAccount', 'buyerAccount']);

        if (!empty($search)) {
            $query->search($search);
        }

        if ($status !== 'all' && !empty($status)) {
            $query->filterStatus($status);
        }

        if ($currency !== 'all' && !empty($currency)) {
            $query->filterCurrency($currency);
        }

        $auctions = $query->orderBy('created_at', 'desc')
            ->paginate(20)
            ->withQueryString();

        return view('apexsions-bridge::admin.economy.auctions.index', [
            'auctions' => $auctions,
            'search' => $search,
            'selectedStatus' => $status,
            'selectedCurrency' => $currency,
            'activeCount' => Auction::where('status', 'ACTIVE')->count(),
            'quarantinedCount' => Auction::where('status', 'QUARANTINED')->count(),
            'totalCount' => Auction::count(),
        ]);
    }

    /**
     * Display detailed auction listing.
     */
    public function show(int $id): View
    {
        $auction = Auction::with(['sellerAccount', 'buyerAccount'])->findOrFail($id);

        return view('apexsions-bridge::admin.economy.auctions.show', [
            'auction' => $auction,
        ]);
    }

    /**
     * Quarantine an auction listing pending fraud or item dupe investigation.
     */
    public function quarantine(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'reason' => ['required', 'string', 'min:5', 'max:255'],
        ]);

        $auction = Auction::findOrFail($id);
        $result = EconomyAdminService::quarantineAuction($auction, $validated['reason'], $request->user());

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }

    /**
     * Cancel an auction listing and safely return items.
     */
    public function cancel(Request $request, int $id): RedirectResponse
    {
        $validated = $request->validate([
            'reason' => ['required', 'string', 'min:5', 'max:255'],
        ]);

        $auction = Auction::findOrFail($id);
        $result = EconomyAdminService::cancelAuction($auction, $validated['reason'], $request->user());

        if (!$result['success']) {
            return redirect()->back()->with('error', $result['message']);
        }

        return redirect()->back()->with('success', $result['message']);
    }
}
