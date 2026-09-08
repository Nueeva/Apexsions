@extends('admin.layouts.admin')

@section('title', 'Auction Inspector — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-shop text-warning me-2"></i>Auction Inspector
            </h2>
            <p class="text-white-50 small mb-0">Pengawasan lot lelang in-game, investigasi kecurangan ekonomi, dan penindakan karantina berbasis bukti.</p>
        </div>
        <div>
            <a href="{{ route('apexsions-bridge.admin.economy.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-arrow-left me-1"></i>Kembali ke Overview
            </a>
        </div>
    </div>

    <!-- Summary Widgets -->
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <span class="text-white-50 small fw-bold text-uppercase">Lelang Aktif</span>
                <h3 class="h4 fw-bold text-success mb-0 mt-1">{{ number_format($activeCount, 0, ',', '.') }}</h3>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <span class="text-white-50 small fw-bold text-uppercase">Dalam Karantina (Quarantined)</span>
                <h3 class="h4 fw-bold text-danger mb-0 mt-1">{{ number_format($quarantinedCount, 0, ',', '.') }}</h3>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <span class="text-white-50 small fw-bold text-uppercase">Total Riwayat Lot</span>
                <h3 class="h4 fw-bold text-warning mb-0 mt-1">{{ number_format($totalCount, 0, ',', '.') }}</h3>
            </div>
        </div>
    </div>

    <!-- Filters -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-3">
            <form action="{{ route('apexsions-bridge.admin.economy.auctions.index') }}" method="GET" class="row g-2 align-items-end">
                <div class="col-md-5">
                    <label class="form-label text-white-50 small mb-1">Pencarian</label>
                    <input type="text" name="q" value="{{ $search }}" class="form-control form-control-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25" placeholder="Cari nama item, penjual, atau ID lelang...">
                </div>
                <div class="col-md-3">
                    <label class="form-label text-white-50 small mb-1">Status</label>
                    <select name="status" class="form-select form-select-sm bg-black bg-opacity-50 text-white border-secondary border-opacity-25">
                        <option value="all" {{ $selectedStatus === 'all' ? 'selected' : '' }}>Semua Status</option>
                        <option value="ACTIVE" {{ $selectedStatus === 'ACTIVE' ? 'selected' : '' }}>ACTIVE (Aktif)</option>
                        <option value="QUARANTINED" {{ $selectedStatus === 'QUARANTINED' ? 'selected' : '' }}>QUARANTINED (Karantina)</option>
                        <option value="SOLD" {{ $selectedStatus === 'SOLD' ? 'selected' : '' }}>SOLD (Terjual)</option>
                        <option value="EXPIRED" {{ $selectedStatus === 'EXPIRED' ? 'selected' : '' }}>EXPIRED (Kedaluwarsa)</option>
                        <option value="CANCELLED" {{ $selectedStatus === 'CANCELLED' ? 'selected' : '' }}>CANCELLED (Batal)</option>
                    </select>
                </div>
                <div class="col-md-4 d-flex gap-2">
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark flex-grow-1">
                        <i class="bi bi-funnel-fill me-1"></i>Filter
                    </button>
                    <a href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-counterclockwise"></i>
                    </a>
                </div>
            </form>
        </div>
    </div>

    <!-- Auctions Table -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-body p-0">
            @if($auctions->isEmpty())
                <div class="p-5 text-center text-white-50">
                    <i class="bi bi-shop-window fs-1 d-block mb-3 text-muted"></i>
                    <h5 class="h6 text-white mb-1">Tidak Ada Lot Lelang Ditemukan</h5>
                    <p class="small text-muted mb-0">Belum ada listing lelang yang sesuai dengan filter pencarian Anda.</p>
                </div>
            @else
                <div class="table-responsive">
                    <table class="table table-dark table-hover mb-0 align-middle">
                        <thead>
                            <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                                <th class="ps-3">ID Lot</th>
                                <th>Item</th>
                                <th>Penjual</th>
                                <th>Harga</th>
                                <th>Status</th>
                                <th>Kedaluwarsa</th>
                                <th class="pe-3 text-end">Aksi</th>
                            </tr>
                        </thead>
                        <tbody>
                            @foreach($auctions as $auc)
                                <tr>
                                    <td class="ps-3 font-monospace text-white small">
                                        #{{ $auc->auction_id }}
                                    </td>
                                    <td class="fw-bold text-white">
                                        <i class="bi bi-box-seam text-warning me-1"></i>
                                        {{ $auc->item_name }}
                                    </td>
                                    <td>
                                        <a href="{{ route('apexsions-bridge.admin.players.show', $auc->seller_name) }}" class="text-info text-decoration-none small fw-medium">
                                            {{ $auc->seller_name }}
                                        </a>
                                    </td>
                                    <td class="fw-bold {{ $auc->currency === 'diamond' ? 'text-info' : 'text-warning' }}">
                                        {{ $auc->currency === 'diamond' ? number_format($auc->price, 0) . ' 💎' : 'Rp ' . number_format($auc->price, 0, ',', '.') }}
                                    </td>
                                    <td>
                                        @if($auc->status === 'ACTIVE')
                                            <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">ACTIVE</span>
                                        @elseif($auc->status === 'QUARANTINED')
                                            <span class="badge bg-danger bg-opacity-25 text-danger border border-danger">QUARANTINED</span>
                                        @elseif($auc->status === 'SOLD')
                                            <span class="badge bg-info bg-opacity-10 text-info border border-info border-opacity-25">SOLD</span>
                                        @else
                                            <span class="badge bg-secondary bg-opacity-25 text-white-50">{{ $auc->status }}</span>
                                        @endif
                                    </td>
                                    <td class="text-white-50 small font-monospace">
                                        {{ $auc->expires_at ? $auc->expires_at->diffForHumans() : '-' }}
                                    </td>
                                    <td class="pe-3 text-end">
                                        <a href="{{ route('apexsions-bridge.admin.economy.auctions.show', $auc->id) }}" class="btn btn-outline-warning btn-sm py-1 px-2" style="font-size: 0.75rem;">
                                            <i class="bi bi-search me-1"></i>Detail
                                        </a>
                                    </td>
                                </tr>
                            @endforeach
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <div class="p-3 border-top border-secondary border-opacity-25 d-flex justify-content-end">
                    {{ $auctions->links() }}
                </div>
            @endif
        </div>
    </div>
</div>
@endsection
