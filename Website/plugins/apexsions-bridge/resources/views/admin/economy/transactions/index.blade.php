@extends('admin.layouts.admin')

@section('title', 'Transaction Explorer — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-receipt-cutoff text-warning me-2"></i>Transaction Explorer
            </h2>
            <p class="text-white-50 small mb-0">Pelacakan aliran mata uang antar pemain, transaksi lelang, dan mutasi saldo administratif secara atomik.</p>
        </div>
        <div>
            <a href="{{ route('apexsions-bridge.admin.economy.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-arrow-left me-1"></i>Kembali ke Overview
            </a>
        </div>
    </div>

    <!-- Filter Card -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-3">
            <form action="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" method="GET" class="row g-2 align-items-end">
                <div class="col-md-3">
                    <label class="form-label text-white-50 small mb-1">Pencarian</label>
                    <input type="text" name="q" value="{{ $search }}" class="form-control form-control-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25" placeholder="Player / TX ID / Alasan...">
                </div>
                <div class="col-md-2">
                    <label class="form-label text-white-50 small mb-1">Tipe Mutasi</label>
                    <select name="type" class="form-select form-select-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25">
                        <option value="all" {{ $selectedType === 'all' ? 'selected' : '' }}>Semua Tipe</option>
                        <option value="TRANSFER" {{ $selectedType === 'TRANSFER' ? 'selected' : '' }}>TRANSFER</option>
                        <option value="AUCTION_BUY" {{ $selectedType === 'AUCTION_BUY' ? 'selected' : '' }}>AUCTION_BUY</option>
                        <option value="ADMIN_ADJUST" {{ $selectedType === 'ADMIN_ADJUST' ? 'selected' : '' }}>ADMIN_ADJUST</option>
                        <option value="TAX_TREASURY" {{ $selectedType === 'TAX_TREASURY' ? 'selected' : '' }}>TAX_TREASURY</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label text-white-50 small mb-1">Mata Uang</label>
                    <select name="currency" class="form-select form-select-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25">
                        <option value="all" {{ $selectedCurrency === 'all' ? 'selected' : '' }}>Semua Mata Uang</option>
                        <option value="rupiah" {{ $selectedCurrency === 'rupiah' ? 'selected' : '' }}>Rupiah (Rp)</option>
                        <option value="diamond" {{ $selectedCurrency === 'diamond' ? 'selected' : '' }}>Diamond (💎)</option>
                        <option value="apex_coins" {{ $selectedCurrency === 'apex_coins' ? 'selected' : '' }}>Apex Coins (AC)</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label text-white-50 small mb-1">Status</label>
                    <select name="status" class="form-select form-select-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25">
                        <option value="all" {{ $selectedStatus === 'all' ? 'selected' : '' }}>Semua Status</option>
                        <option value="COMPLETED" {{ $selectedStatus === 'COMPLETED' ? 'selected' : '' }}>COMPLETED</option>
                        <option value="PENDING" {{ $selectedStatus === 'PENDING' ? 'selected' : '' }}>PENDING</option>
                        <option value="FAILED" {{ $selectedStatus === 'FAILED' ? 'selected' : '' }}>FAILED</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex gap-2">
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark flex-grow-1">
                        <i class="bi bi-funnel-fill me-1"></i>Filter
                    </button>
                    <a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-counterclockwise"></i>
                    </a>
                </div>
            </form>
        </div>
    </div>

    <!-- Transactions Table -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
            <span class="text-white-50 small">Total: <strong>{{ number_format($totalCount, 0, ',', '.') }}</strong> transaksi</span>
        </div>
        <div class="card-body p-0">
            @if($transactions->isEmpty())
                <div class="p-5 text-center text-white-50">
                    <i class="bi bi-receipt-cutoff fs-1 d-block mb-3 text-muted"></i>
                    <h5 class="h6 text-white mb-1">Tidak Ada Transaksi Ditemukan</h5>
                    <p class="small text-muted mb-0">Belum ada catatan mutasi yang memenuhi kriteria pencarian Anda.</p>
                </div>
            @else
                <div class="table-responsive">
                    <table class="table table-dark table-hover mb-0 align-middle">
                        <thead>
                            <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                                <th class="ps-3">ID Transaksi</th>
                                <th>Waktu</th>
                                <th>Tipe</th>
                                <th>Dari (Sender)</th>
                                <th>Kepada (Receiver)</th>
                                <th>Jumlah</th>
                                <th>Pajak</th>
                                <th>Status</th>
                                <th class="pe-3 text-end">Aksi</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($transactions as $tx)
                                <tr>
                                    <td class="ps-3 font-monospace text-white small">
                                        <span title="{{ $tx->transaction_id }}">#{{ substr($tx->transaction_id, 0, 8) }}</span>
                                    </td>
                                    <td class="text-white-50 small font-monospace">
                                        {{ $tx->created_at->format('Y-m-d H:i') }}
                                    </td>
                                    <td>
                                        @php
                                            $badgeClass = match($tx->type) {
                                                'TRANSFER' => 'bg-info bg-opacity-10 text-info border border-info border-opacity-25',
                                                'AUCTION_BUY' => 'bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25',
                                                'ADMIN_ADJUST' => 'bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25',
                                                'TAX_TREASURY' => 'bg-secondary bg-opacity-25 text-white-50',
                                                default => 'bg-secondary text-white'
                                            };
                                        @endphp
                                        <span class="badge {{ $badgeClass }} font-monospace" style="font-size: 0.72rem;">
                                            {{ $tx->type }}
                                        </span>
                                    </td>
                                    <td>
                                        @if($tx->sender_name)
                                            <a href="{{ route('apexsions-bridge.admin.players.show', $tx->sender_name) }}" class="text-info text-decoration-none small fw-medium">
                                                {{ $tx->sender_name }}
                                            </a>
                                        @else
                                            <span class="text-white-50 small font-monospace">SYSTEM</span>
                                        @endif
                                    </td>
                                    <td>
                                        @if($tx->receiver_name)
                                            <a href="{{ route('apexsions-bridge.admin.players.show', $tx->receiver_name) }}" class="text-success text-decoration-none small fw-medium">
                                                {{ $tx->receiver_name }}
                                            </a>
                                        @else
                                            <span class="text-white-50 small font-monospace">SYSTEM</span>
                                        @endif
                                    </td>
                                    <td class="fw-bold {{ $tx->currency === 'diamond' ? 'text-info' : 'text-warning' }}">
                                        {{ $tx->currency === 'diamond' ? number_format($tx->amount, 0) . ' 💎' : 'Rp ' . number_format($tx->amount, 0, ',', '.') }}
                                    </td>
                                    <td class="text-white-50 small font-monospace">
                                        {{ $tx->tax_amount > 0 ? 'Rp ' . number_format($tx->tax_amount, 0, ',', '.') : '-' }}
                                    </td>
                                    <td>
                                        @if($tx->status === 'COMPLETED')
                                            <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">COMPLETED</span>
                                        @elseif($tx->status === 'PENDING')
                                            <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25">PENDING</span>
                                        @else
                                            <span class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25">{{ $tx->status }}</span>
                                        @endif
                                    </td>
                                    <td class="pe-3 text-end">
                                        <a href="{{ route('apexsions-bridge.admin.economy.transactions.show', $tx->id) }}" class="btn btn-outline-warning btn-sm py-1 px-2" style="font-size: 0.75rem;">
                                            <i class="bi bi-search me-1"></i>Investigate
                                        </a>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <div class="p-3 border-top border-secondary border-opacity-25 d-flex justify-content-end">
                    {{ $transactions->links() }}
                </div>
            @endif
        </div>
    </div>
</div>
@endsection
