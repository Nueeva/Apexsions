@extends('admin.layouts.admin')

@section('title', 'Detail Lelang #' . $auction->auction_id . ' — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Breadcrumb & Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1 small">
                    <li class="breadcrumb-item"><a href="{{ route('apexsions-bridge.admin.economy.index') }}" class="text-warning text-decoration-none">Economy</a></li>
                    <li class="breadcrumb-item"><a href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}" class="text-warning text-decoration-none">Auctions</a></li>
                    <li class="breadcrumb-item active text-white-50" aria-current="page">#{{ $auction->auction_id }}</li>
                </ol>
            </nav>
            <h2 class="h4 fw-bold text-white mb-0 font-monospace">
                <i class="bi bi-shop text-warning me-2"></i>Lelang Lot #{{ $auction->auction_id }}
            </h2>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-arrow-left me-1"></i>Kembali
            </a>
            @if($auction->status === 'ACTIVE')
                <button type="button" class="btn btn-danger btn-sm shadow-sm fw-bold" data-bs-toggle="modal" data-bs-target="#quarantineModal">
                    <i class="bi bi-shield-slash me-1"></i>Karantina Lot
                </button>
                <button type="button" class="btn btn-outline-danger btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#cancelModal">
                    <i class="bi bi-x-circle me-1"></i>Batalkan Lot
                </button>
            @endif
        </div>
    </div>

    @if($auction->status === 'QUARANTINED')
        <div class="alert alert-danger bg-danger bg-opacity-10 border border-danger border-opacity-50 text-white mb-4 shadow-sm">
            <div class="d-flex align-items-center">
                <i class="bi bi-shield-exclamation fs-3 text-danger me-3"></i>
                <div>
                    <h5 class="h6 fw-bold text-danger mb-1">LOT LELANG INI SEDANG DALAM KARANTINA PENGAWASAN</h5>
                    <p class="small mb-1">Lelang dinonaktifkan dari listing pasar untuk mencegah sirkulasi barang atau transaksi yang mencurigakan.</p>
                    <div class="small text-white-50 font-monospace">
                        Petugas: <strong>{{ $auction->quarantined_by }}</strong> | Alasan: <em>"{{ $auction->quarantine_reason }}"</em> | Waktu: {{ $auction->quarantined_at ? $auction->quarantined_at->format('Y-m-d H:i:s') : '-' }}
                    </div>
                </div>
            </div>
        </div>
    @endif

    <div class="row g-4 mb-4">
        <!-- Item Details -->
        <div class="col-lg-7">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-box-seam text-warning me-2"></i>Spesifikasi Item Lelang
                    </h5>
                    @if($auction->status === 'ACTIVE')
                        <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">ACTIVE</span>
                    @elseif($auction->status === 'QUARANTINED')
                        <span class="badge bg-danger bg-opacity-25 text-danger border border-danger">QUARANTINED</span>
                    @elseif($auction->status === 'SOLD')
                        <span class="badge bg-info bg-opacity-10 text-info border border-info border-opacity-25">SOLD</span>
                    @else
                        <span class="badge bg-secondary bg-opacity-25 text-white-50">{{ $auction->status }}</span>
                    @endif
                </div>
                <div class="card-body p-4">
                    <div class="d-flex align-items-center mb-4">
                        <div class="bg-black bg-opacity-50 p-3 rounded border border-warning border-opacity-25 me-3">
                            <i class="bi bi-gem fs-2 text-warning"></i>
                        </div>
                        <div>
                            <h3 class="h4 fw-bold text-white mb-1">{{ $auction->item_name }}</h3>
                            <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace">Lot ID: {{ $auction->auction_id }}</span>
                        </div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Harga Penawaran</span>
                            <h4 class="fw-bold {{ $auction->currency === 'diamond' ? 'text-info' : 'text-warning' }} mb-0">
                                {{ $auction->currency === 'diamond' ? number_format($auction->price, 0) . ' 💎' : 'Rp ' . number_format($auction->price, 0, ',', '.') }}
                            </h4>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Mata Uang Transaksi</span>
                            <span class="text-uppercase fw-bold text-white font-monospace">{{ $auction->currency }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Waktu Didaftarkan</span>
                            <span class="text-white small font-monospace">{{ $auction->created_at->format('Y-m-d H:i:s') }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Batas Kedaluwarsa</span>
                            <span class="text-white small font-monospace">{{ $auction->expires_at ? $auction->expires_at->format('Y-m-d H:i:s') : 'Tidak Ada' }}</span>
                        </div>
                    </div>

                    @if($auction->item_data)
                        <div class="mt-3">
                            <span class="text-white-50 small d-block fw-bold mb-1">Bukkit NBT / Serialized Data Snapshot:</span>
                            <pre class="bg-black bg-opacity-50 p-3 rounded border border-secondary border-opacity-25 text-white-50 small font-monospace mb-0" style="max-height: 150px; overflow-y: auto;">{{ $auction->item_data }}</pre>
                        </div>
                    @endif
                </div>
            </div>
        </div>

        <!-- Participants Context -->
        <div class="col-lg-5">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-person-lines-fill text-warning me-2"></i>Pihak Penjual & Pembeli
                    </h5>
                </div>
                <div class="card-body p-4">
                    <!-- Seller -->
                    <div class="p-3 bg-black bg-opacity-25 rounded border border-secondary border-opacity-25 mb-3">
                        <span class="badge bg-secondary bg-opacity-50 text-white-50 mb-1">PENJUAL (SELLER)</span>
                        <h5 class="h6 text-white mb-1 fw-bold">{{ $auction->seller_name }}</h5>
                        <code class="text-white-50 small d-block mb-2">{{ $auction->seller_uuid }}</code>
                        <a href="{{ route('apexsions-bridge.admin.players.show', $auction->seller_name) }}" class="btn btn-outline-warning btn-sm">
                            <i class="bi bi-person-badge me-1"></i>Buka Profil Penjual 360°
                        </a>
                    </div>

                    <!-- Buyer if sold -->
                    @if($auction->buyer_name)
                        <div class="p-3 bg-black bg-opacity-25 rounded border border-secondary border-opacity-25">
                            <span class="badge bg-success bg-opacity-25 text-success mb-1">PEMBELI (BUYER)</span>
                            <h5 class="h6 text-white mb-1 fw-bold">{{ $auction->buyer_name }}</h5>
                            <code class="text-white-50 small d-block mb-2">{{ $auction->buyer_uuid ?? '-' }}</code>
                            <a href="{{ route('apexsions-bridge.admin.players.show', $auction->buyer_name) }}" class="btn btn-outline-success btn-sm">
                                <i class="bi bi-person-badge me-1"></i>Buka Profil Pembeli 360°
                            </a>
                        </div>
                    @endif
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Karantina -->
<div class="modal fade" id="quarantineModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-white border-secondary border-opacity-25">
            <form action="{{ route('apexsions-bridge.admin.economy.auctions.quarantine', $auction->id) }}" method="POST">
                @csrf
                <div class="modal-header border-bottom border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-danger h6">
                        <i class="bi bi-shield-slash me-2"></i>Karantina Lot Lelang
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <p class="small text-white-50 mb-3">Tindakan ini akan mencabut listing dari marketplace game server dan menandai lot sebagai <strong>QUARANTINED</strong> guna investigasi kecurangan atau duplikasi item.</p>
                    <label class="form-label text-white-50 small fw-bold">Alasan Karantina (Wajib Diisi)</label>
                    <textarea name="reason" class="form-control bg-black bg-opacity-50 text-white border-secondary border-opacity-25" rows="3" placeholder="Contoh: Terindikasi NBT dupe hack pada senjata lore" required></textarea>
                </div>
                <div class="modal-footer border-top border-secondary border-opacity-25">
                    <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger btn-sm fw-bold">
                        <i class="bi bi-shield-slash me-1"></i>Karantina Sekarang
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Batal -->
<div class="modal fade" id="cancelModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark text-white border-secondary border-opacity-25">
            <form action="{{ route('apexsions-bridge.admin.economy.auctions.cancel', $auction->id) }}" method="POST">
                @csrf
                <div class="modal-header border-bottom border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-warning h6">
                        <i class="bi bi-x-circle me-2"></i>Batalkan Lot Lelang
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <p class="small text-white-50 mb-3">Item akan dibatalkan dari pelelangan dan dapat diambil kembali oleh pemilik aslinya.</p>
                    <label class="form-label text-white-50 small fw-bold">Alasan Pembatalan (Wajib Diisi)</label>
                    <textarea name="reason" class="form-control bg-black bg-opacity-50 text-white border-secondary border-opacity-25" rows="3" placeholder="Contoh: Pembatalan administratif atas permintaan pemain" required></textarea>
                </div>
                <div class="modal-footer border-top border-secondary border-opacity-25">
                    <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-dismiss="modal">Tutup</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">
                        <i class="bi bi-check-circle me-1"></i>Batalkan Lelang
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
