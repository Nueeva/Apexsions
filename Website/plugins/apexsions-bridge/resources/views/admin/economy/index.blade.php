@extends('admin.layouts.admin')

@section('title', 'Economy Inspector — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-cash-stack text-warning me-2"></i>Economy Inspector
            </h2>
            <p class="text-white-50 small mb-0">Pemantauan pasokan moneter terverifikasi, cadangan kas kerajaan, dan integritas aliran ekonomi Apexsions.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-receipt-cutoff me-1"></i>Transaction Explorer
            </a>
            <a href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-shop me-1"></i>Auction Inspector
            </a>
            <button type="button" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="modal" data-bs-target="#adjustBalanceModal">
                <i class="bi bi-shield-plus me-1"></i>Penyesuaian Saldo Terkontrol
            </button>
        </div>
    </div>

    <!-- Overview Metric Cards -->
    <div class="row g-3 mb-4">
        @foreach($metrics as $key => $metric)
            <div class="col-xl-3 col-md-6">
                <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                    <div class="card-body p-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-white-50 small fw-medium text-uppercase">{{ $metric['label'] }}</span>
                            <i class="bi bi-info-circle text-muted" title="{{ $metric['source'] }}" data-bs-toggle="tooltip"></i>
                        </div>
                        <h3 class="h4 fw-bold text-warning mb-1">{{ $metric['formatted'] }}</h3>
                        <div class="d-flex justify-content-between align-items-center pt-2 border-top border-secondary border-opacity-10 mt-2">
                            <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace" style="font-size: 0.7rem;">
                                {{ $metric['source'] }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        @endforeach
    </div>

    <div class="row g-4 mb-4">
        <!-- Kingdom Treasury Reserves -->
        <div class="col-lg-5">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-bank2 text-warning me-2"></i>Cadangan Kas Kerajaan (Kingdom Treasury)
                    </h5>
                    <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25 font-monospace">
                        Pajak Otomatis
                    </span>
                </div>
                <div class="card-body p-0">
                    @if($kingdoms->isEmpty())
                        <div class="p-4 text-center text-white-50">
                            <i class="bi bi-inbox fs-2 d-block mb-2 text-muted"></i>
                            <span>Belum ada data kas kerajaan tersinkronisasi.</span>
                        </div>
                    @else
                        <div class="table-responsive">
                            <table class="table table-dark table-hover mb-0 align-middle">
                                <thead>
                                    <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                                        <th class="ps-3">Kerajaan</th>
                                        <th>Saldo Kas</th>
                                        <th>Total Terkumpul</th>
                                        <th class="pe-3 text-end">Update</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @foreach($kingdoms as $k)
                                        <tr>
                                            <td class="ps-3 fw-bold text-white">
                                                <span class="badge bg-secondary bg-opacity-25 text-warning me-1">{{ $k->kingdom_key }}</span>
                                                {{ $k->kingdom_name }}
                                            </td>
                                            <td class="text-warning fw-bold">
                                                Rp {{ number_format($k->balance, 0, ',', '.') }}
                                            </td>
                                            <td class="text-white-50">
                                                Rp {{ number_format($k->total_tax_collected, 0, ',', '.') }}
                                            </td>
                                            <td class="pe-3 text-end text-white-50 small font-monospace">
                                                {{ $k->last_tax_collected_at ? $k->last_tax_collected_at->diffForHumans() : '-' }}
                                            </td>
                                        </tr>
                                    @endforeach
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </div>
        </div>

        <!-- Recent Transactions -->
        <div class="col-lg-7">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-clock-history text-warning me-2"></i>Aktivitas Transaksi Terkini
                    </h5>
                    <a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" class="btn btn-sm btn-link text-warning text-decoration-none p-0">
                        Lihat Semua <i class="bi bi-arrow-right"></i>
                    </a>
                </div>
                <div class="card-body p-0">
                    @if($recentTransactions->isEmpty())
                        <div class="p-4 text-center text-white-50">
                            <i class="bi bi-receipt fs-2 d-block mb-2 text-muted"></i>
                            <span>Belum ada riwayat transaksi yang tercatat di ledger web.</span>
                        </div>
                    @else
                        <div class="table-responsive">
                            <table class="table table-dark table-hover mb-0 align-middle">
                                <thead>
                                    <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                                        <th class="ps-3">Waktu</th>
                                        <th>Tipe</th>
                                        <th>Pihak Terlibat</th>
                                        <th>Jumlah</th>
                                        <th class="pe-3 text-end">Aksi</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @foreach($recentTransactions as $tx)
                                        <tr>
                                            <td class="ps-3 text-white-50 small font-monospace">
                                                {{ $tx->created_at->format('H:i:s d/m') }}
                                            </td>
                                            <td>
                                                <span class="badge bg-secondary bg-opacity-50 text-white font-monospace" style="font-size: 0.75rem;">
                                                    {{ $tx->type }}
                                                </span>
                                            </td>
                                            <td>
                                                <div class="small">
                                                    <span class="text-info">{{ $tx->sender_name ?? 'System' }}</span>
                                                    <i class="bi bi-arrow-right text-muted mx-1" style="font-size: 0.75rem;"></i>
                                                    <span class="text-success">{{ $tx->receiver_name ?? 'System' }}</span>
                                                </div>
                                            </td>
                                            <td class="fw-bold {{ $tx->currency === 'diamond' ? 'text-info' : 'text-warning' }}">
                                                {{ $tx->currency === 'diamond' ? number_format($tx->amount, 0) . ' 💎' : 'Rp ' . number_format($tx->amount, 0, ',', '.') }}
                                            </td>
                                            <td class="pe-3 text-end">
                                                <a href="{{ route('apexsions-bridge.admin.economy.transactions.show', $tx->id) }}" class="btn btn-outline-secondary btn-sm py-0 px-2" style="font-size: 0.75rem;">
                                                    Trace
                                                </a>
                                            </td>
                                        </tr>
                                    @endforeach
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal: Penyesuaian Saldo Terkontrol -->
<div class="modal fade" id="adjustBalanceModal" tabindex="-1" aria-labelledby="adjustBalanceModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-white border-secondary border-opacity-25 shadow-lg">
            <form action="{{ route('apexsions-bridge.admin.economy.adjust') }}" method="POST">
                @csrf
                <div class="modal-header border-bottom border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-warning h6" id="adjustBalanceModalLabel">
                        <i class="bi bi-shield-plus me-2"></i>Penyesuaian Saldo Terkontrol
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="alert alert-secondary bg-black bg-opacity-25 border-warning border-opacity-25 text-white-50 small mb-3">
                        <i class="bi bi-shield-lock-fill text-warning me-1"></i>
                        Seluruh tindakan penyesuaian saldo bersifat permanen, tercatat dalam <strong>Unified Audit Log</strong>, dan diverifikasi dengan <code>action_id</code> unik.
                    </div>

                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Pemain Sasaran (Username / UUID)</label>
                        <input type="text" name="player" class="form-control bg-black bg-opacity-50 text-white border-secondary border-opacity-25" placeholder="Contoh: Steve atau 00000000-..." required>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label text-white-50 small fw-bold">Mata Uang</label>
                            <select name="currency" class="form-select bg-black bg-opacity-50 text-white border-secondary border-opacity-25" required>
                                <option value="rupiah">Rupiah (Rp)</option>
                                <option value="diamond">Diamond (💎)</option>
                                <option value="apex_coins">Apex Coins (AC)</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-white-50 small fw-bold">Aksi</label>
                            <select name="direction" class="form-select bg-black bg-opacity-50 text-white border-secondary border-opacity-25" required>
                                <option value="GIVE">GIVE (Tambah Saldo)</option>
                                <option value="DEDUCT">DEDUCT (Kurangi Saldo)</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label text-white-50 small fw-bold">Jumlah</label>
                        <input type="number" step="0.01" min="0.01" name="amount" class="form-control bg-black bg-opacity-50 text-white border-secondary border-opacity-25" placeholder="10000" required>
                    </div>

                    <div class="mb-0">
                        <label class="form-label text-white-50 small fw-bold">Alasan Penyesuaian (Wajib Diisi)</label>
                        <textarea name="reason" class="form-control bg-black bg-opacity-50 text-white border-secondary border-opacity-25" rows="2" placeholder="Contoh: Kompensasi rollback server akibat maintenance" required></textarea>
                    </div>
                </div>
                <div class="modal-footer border-top border-secondary border-opacity-25">
                    <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">
                        <i class="bi bi-check-circle me-1"></i>Eksekusi Penyesuaian
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
