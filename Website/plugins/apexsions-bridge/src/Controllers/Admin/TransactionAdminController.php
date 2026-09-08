<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers\Admin;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Transaction;
use Illuminate\Http\Request;
use Illuminate\View\View;

class TransactionAdminController extends Controller
{
    /**
     * Display Transaction Explorer with multi-criteria filters.
     */
    public function index(Request $request): View
    {
        $search = trim((string) $request->input('q', ''));
        $type = $request->input('type', 'all');
        $currency = $request->input('currency', 'all');
        $status = $request->input('status', 'all');
        $fromDate = $request->input('from');
        $toDate = $request->input('to');
        $minAmount = $request->filled('min_amount') ? (float) $request->input('min_amount') : null;
        $maxAmount = $request->filled('max_amount') ? (float) $request->input('max_amount') : null;

        $query = Transaction::query()->with(['senderAccount', 'receiverAccount']);

        if (!empty($search)) {
            $query->search($search);
        }

        if ($type !== 'all' && !empty($type)) {
            $query->filterType($type);
        }

        if ($currency !== 'all' && !empty($currency)) {
            $query->filterCurrency($currency);
        }

        if ($status !== 'all' && !empty($status)) {
            $query->filterStatus($status);
        }

        $query->dateRange($fromDate, $toDate);
        $query->amountRange($minAmount, $maxAmount);

        $transactions = $query->orderBy('created_at', 'desc')
            ->paginate(20)
            ->withQueryString();

        return view('apexsions-bridge::admin.economy.transactions.index', [
            'transactions' => $transactions,
            'search' => $search,
            'selectedType' => $type,
            'selectedCurrency' => $currency,
            'selectedStatus' => $status,
            'fromDate' => $fromDate,
            'toDate' => $toDate,
            'minAmount' => $minAmount,
            'maxAmount' => $maxAmount,
            'totalCount' => Transaction::count(),
        ]);
    }

    /**
     * Display detailed trace of a specific transaction.
     */
    public function show(int $id): View
    {
        $transaction = Transaction::with(['senderAccount', 'receiverAccount', 'auditLog'])->findOrFail($id);

        return view('apexsions-bridge::admin.economy.transactions.show', [
            'transaction' => $transaction,
        ]);
    }
}
