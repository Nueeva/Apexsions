@extends('admin.layouts.admin')

@section('title', 'Crates & Key Dispenser — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-box2-fill text-warning me-2"></i>Crates & Key Dispenser Desk
            </h2>
            <p class="text-white-50 small mb-0">
                Katalog peti hadiah resmi server, distribusi kunci peti pemain (online & offline), dan inspeksi pity counter.
            </p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <button type="button" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="modal" data-bs-target="#dispenseKeyModal">
                <i class="bi bi-key-fill me-1"></i>Beri Kunci Pemain
            </button>
            <form action="{{ route('apexsions-bridge.admin.crates.reload') }}" method="POST" class="d-inline">
                @csrf
                <button type="submit" class="btn btn-outline-secondary btn-sm shadow-sm" title="Reload konfigurasi dan probabilitas Crates di game server">
                    <i class="bi bi-arrow-repeat me-1"></i>Reload Crates Engine
                </button>
            </form>
        </div>
    </div>

    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2 text-success"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    @if(session('error'))
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2 text-danger"></i>{{ session('error') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <!-- Crates Catalog Cards -->
    <div class="row g-4 mb-4">
        @foreach($crates as $cKey => $crate)
            <div class="col-md-4">
                <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                    <div class="card-header border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center bg-black bg-opacity-25">
                        <span class="fw-bold text-white"><i class="bi bi-box-seam text-warning me-2"></i>{{ $crate['name'] }}</span>
                        <span class="badge {{ $crate['badge_class'] }}">{{ $crate['badge'] }}</span>
                    </div>
                    <div class="card-body">
                        <p class="text-white-50 small mb-3">{{ $crate['description'] }}</p>
                        <div class="bg-secondary bg-opacity-10 p-2 rounded mb-3">
                            <div class="d-flex justify-content-between text-white-50 small mb-1">
                                <span>Harga Kunci Toko:</span>
                                <span class="fw-bold text-warning">Rp {{ number_format($crate['price_rupiah'], 0, ',', '.') }} / {{ $crate['price_diamond'] }} 💎</span>
                            </div>
                            <div class="d-flex justify-content-between text-white-50 small">
                                <span>Pity Guaranteed:</span>
                                <span class="fw-bold text-info">{{ $crate['pity_threshold'] }}x Buka</span>
                            </div>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline-warning w-100 open-dispense-btn" 
                                data-bs-toggle="modal" data-bs-target="#dispenseKeyModal" 
                                data-crate="{{ $crate['id'] }}" data-cratename="{{ $crate['name'] }}">
                            <i class="bi bi-key me-1"></i>Beri Kunci {{ $crate['id'] }}
                        </button>
                    </div>
                </div>
            </div>
        @endforeach
    </div>

    <!-- Recent Key Deliveries Table -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
            <h5 class="card-title h6 fw-bold text-white mb-0">
                <i class="bi bi-clock-history text-warning me-2"></i>Log Distribusi Kunci Terkini
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-dark table-hover align-middle mb-0">
                    <thead class="table-secondary bg-opacity-10 text-uppercase small">
                        <tr>
                            <th class="ps-3">Waktu</th>
                            <th>Aktor</th>
                            <th>Target Pemain</th>
                            <th>Aksi Kunci</th>
                            <th>Alasan</th>
                            <th class="pe-3 text-end">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($recentLogs as $log)
                            <tr>
                                <td class="ps-3 small text-white-50">{{ $log->created_at->format('d M H:i:s') }}</td>
                                <td><span class="badge bg-secondary">{{ $log->actor_name ?? 'System' }}</span></td>
                                <td class="fw-semibold text-white">{{ $log->target_name }}</td>
                                <td><span class="badge bg-warning text-dark">{{ $log->new_value }}</span></td>
                                <td class="small text-white-50">{{ $log->reason }}</td>
                                <td class="pe-3 text-end">
                                    <span class="badge bg-success bg-opacity-25 text-success">{{ $log->status }}</span>
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="6" class="text-center py-4 text-white-50">Belum ada distribusi kunci tercatat.</td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal Dispense Key -->
<div class="modal fade" id="dispenseKeyModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-warning text-white">
            <form action="{{ route('apexsions-bridge.admin.crates.manage-key') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-warning"><i class="bi bi-key-fill me-2"></i>Kirim Kunci Peti Pemain</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Username Pemain</label>
                        <input type="text" name="player_username" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Contoh: NuevaID" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Jenis Peti (Crate ID)</label>
                        <select id="modalCrateSelect" name="crate_id" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="novice">Novice Crate (Peti Pemula)</option>
                            <option value="luxury">Luxury Crate (Peti Kemewahan)</option>
                            <option value="apex">Apex Crate (Mahkota Peradaban)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Aksi</label>
                        <select name="action" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="give">Beri Kunci Tambahan (give)</option>
                            <option value="take">Tarik / Kurangi Kunci (take)</option>
                            <option value="set">Atur Jumlah Kunci Pasti (set)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Jumlah Kunci</label>
                        <input type="number" name="amount" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" value="1" min="1" max="100" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Alasan Pemberian</label>
                        <input type="text" name="reason" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Kompensasi bug / Hadiah Event Web">
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Kirim Kunci</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const dispenseModal = document.getElementById('dispenseKeyModal');
    if (dispenseModal) {
        dispenseModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            if (button && button.hasAttribute('data-crate')) {
                const crateId = button.getAttribute('data-crate');
                document.getElementById('modalCrateSelect').value = crateId;
            }
        });
    }
});
</script>
@endsection
