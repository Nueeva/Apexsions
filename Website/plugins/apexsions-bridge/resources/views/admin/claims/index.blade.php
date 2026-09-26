@extends('admin.layouts.admin')

@section('title', 'Land Claims, Pajak & Anti-Grief — Apexsions Admin')

@section('content')
<div class="container-fluid px-4 py-3" style="background: #0d0f12; min-height: 100vh; color: #e2e8f0; font-family: 'Outfit', sans-serif;">

    {{-- Header & Actions --}}
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom border-secondary border-opacity-25">
        <div>
            <div class="text-warning text-uppercase small fw-bold tracking-wider" style="letter-spacing: 1.5px;">
                <i class="bi bi-shield-shaded me-1"></i> Security, Territorial & Upkeep Engine
            </div>
            <h2 class="fw-bold mb-0 text-white" style="letter-spacing: -0.5px;">Land Claims, Pajak & Anti-Grief</h2>
            <p class="text-muted small mb-0">Inspeksi kedaulatan tanah, brankas pajak progresif, masa tenggang (grace period), dan proteksi teritori.</p>
        </div>
        <div class="d-flex gap-2">
            <form action="{{ route('apexsions-bridge.admin.claims.sync') }}" method="POST" class="d-inline">
                @csrf
                <button type="submit" class="btn btn-outline-info btn-sm">
                    <i class="bi bi-arrow-repeat me-1"></i> Sync In-Game
                </button>
            </form>
            <form action="{{ route('apexsions-bridge.admin.claims.collect-tax') }}" method="POST" class="d-inline">
                @csrf
                <button type="submit" class="btn btn-outline-warning btn-sm" onclick="return confirm('Jalankan siklus penagihan pajak wilayah sekarang?')">
                    <i class="bi bi-cash-stack me-1"></i> Tagih Pajak Sekarang
                </button>
            </form>
            <a href="https://web.apexsions.com/rules" target="_blank" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-book me-1"></i> Aturan Wilayah
            </a>
        </div>
    </div>

    {{-- Alert Messages --}}
    @if(session('success'))
        <div class="alert alert-success alert-dismissible fade show bg-success bg-opacity-10 border-success border-opacity-25 text-success mb-4" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif
    @if(session('error'))
        <div class="alert alert-danger alert-dismissible fade show bg-danger bg-opacity-10 border-danger border-opacity-25 text-danger mb-4" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ session('error') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    {{-- Metrics Cards --}}
    <div class="row g-3 mb-4">
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #f59e0b !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Total Chunks Terklaim</div>
                    <div class="fs-3 fw-bold text-warning">{{ number_format($totalClaims) }}</div>
                    <small class="text-muted">Wilayah terlindungi anti-grief</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #10b981 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Status Lunas & Aktif</div>
                    <div class="fs-3 fw-bold text-success">{{ number_format($activeClaimsCount) }}</div>
                    <small class="text-muted">Proteksi peradaban 100% aman</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid {{ $inGracePeriodCount > 0 ? '#ef4444' : '#64748b' }} !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Menunggak (Grace Period)</div>
                    <div class="fs-3 fw-bold {{ $inGracePeriodCount > 0 ? 'text-danger' : 'text-muted' }}">{{ number_format($inGracePeriodCount) }}</div>
                    <small class="text-muted">Tersisa waktu 72 jam sebelum disita</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #38bdf8 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Total Saldo Brankas</div>
                    <div class="fs-3 fw-bold text-info">Rp{{ number_format($totalVaultBalance, 0, ',', '.') }}</div>
                    <small class="text-muted">Dana cadangan sewa wilayah</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #a855f7 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Pemilik Tanah Aktif</div>
                    <div class="fs-3 fw-bold" style="color: #c084fc;">{{ number_format($uniqueOwners) }}</div>
                    <small class="text-muted">Warga pemegang hak wilayah</small>
                </div>
            </div>
        </div>
    </div>

    {{-- Filter & Search Form --}}
    <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px;">
        <div class="card-body p-3">
            <form action="{{ route('apexsions-bridge.admin.claims.index') }}" method="GET" class="row g-2 align-items-center">
                <div class="col-md-4">
                    <div class="input-group">
                        <span class="input-group-text bg-dark border-secondary text-muted"><i class="bi bi-search"></i></span>
                        <input type="text" name="search" class="form-control bg-dark text-white border-secondary" placeholder="Cari nama pemain, UUID, ID..." value="{{ $search }}">
                    </div>
                </div>
                <div class="col-md-2">
                    <select name="world" class="form-select bg-dark text-white border-secondary">
                        <option value="all" @if($selectedWorld === 'all') selected @endif>Semua Dunia</option>
                        @foreach($availableWorlds as $w)
                            <option value="{{ $w }}" @if($selectedWorld === $w) selected @endif>{{ $w }}</option>
                        @endforeach
                    </select>
                </div>
                <div class="col-md-2">
                    <select name="status" class="form-select bg-dark text-white border-secondary">
                        <option value="all" @if($selectedStatus === 'all') selected @endif>Semua Status</option>
                        <option value="ACTIVE" @if($selectedStatus === 'ACTIVE') selected @endif>Lunas & Aktif</option>
                        <option value="GRACE_PERIOD" @if($selectedStatus === 'GRACE_PERIOD') selected @endif>Menunggak (Grace Period)</option>
                        <option value="EXPIRED" @if($selectedStatus === 'EXPIRED') selected @endif>Kedaluwarsa</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <select name="kingdom" class="form-select bg-dark text-white border-secondary">
                        <option value="all" @if($selectedKingdom === 'all') selected @endif>Semua Kerajaan</option>
                        @foreach($availableKingdoms as $k)
                            <option value="{{ $k }}" @if($selectedKingdom === $k) selected @endif>{{ strtoupper($k) }}</option>
                        @endforeach
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                        <i class="bi bi-funnel me-1"></i> Filter
                    </button>
                </div>
            </form>
        </div>
    </div>

    {{-- Main Claims Table --}}
    <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px;">
        <div class="card-header bg-transparent border-0 pt-3 px-4 d-flex justify-content-between align-items-center">
            <h5 class="fw-bold text-white mb-0">
                <i class="bi bi-map me-2 text-warning"></i> Daftar Wilayah Terklaim
            </h5>
            <span class="badge bg-secondary">{{ $claims->total() }} Total Petak</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-dark table-hover mb-0 align-middle" style="background: transparent;">
                    <thead style="background: rgba(255, 255, 255, 0.03); border-bottom: 1px solid rgba(255, 255, 255, 0.1);">
                        <tr class="text-muted small text-uppercase">
                            <th class="ps-4">Pemilik Wilayah</th>
                            <th>Dimensi & Koordinat</th>
                            <th>Brankas & Upkeep</th>
                            <th>Kerajaan</th>
                            <th>Status Pembayaran</th>
                            <th class="text-end pe-4">Aksi Otoritas</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($claims as $claim)
                            <tr>
                                <td class="ps-4">
                                    <div class="d-flex align-items-center gap-2">
                                        <img src="https://mc-heads.net/avatar/{{ $claim->owner_name }}/32" alt="{{ $claim->owner_name }}" class="rounded" width="32" height="32">
                                        <div>
                                            <a href="{{ route('apexsions-bridge.admin.players.show', $claim->owner_name) }}" class="text-warning text-decoration-none fw-bold">
                                                {{ $claim->owner_name }}
                                            </a>
                                            <div class="text-muted small font-monospace" style="font-size: 0.75rem;">
                                                {{ substr($claim->owner_uuid, 0, 8) }}...
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <div>
                                        <span class="badge bg-dark border border-secondary text-info">
                                            <i class="bi bi-globe me-1"></i> {{ $claim->world }}
                                        </span>
                                        <div class="small text-white mt-1">
                                            Chunk: <strong>[{{ $claim->chunk_x }}, {{ $claim->chunk_z }}]</strong>
                                            <span class="text-muted">~ X: {{ $claim->getBlockCenterX() }}, Z: {{ $claim->getBlockCenterZ() }}</span>
                                        </div>
                                        <a href="{{ $claim->getBlueMapUrl() }}" target="_blank" class="small text-info text-decoration-none">
                                            <i class="bi bi-compass me-1"></i> Buka di Live Map
                                        </a>
                                    </div>
                                </td>
                                <td>
                                    <div>
                                        <div class="fw-bold text-success">
                                            Rp{{ number_format($claim->bank_balance, 0, ',', '.') }}
                                        </div>
                                        <div class="text-muted small">
                                            Pajak: <span class="text-warning">Rp{{ number_format($claim->daily_upkeep, 0, ',', '.') }}/hari</span>
                                        </div>
                                        <div class="small">
                                            @if($claim->daily_upkeep > 0)
                                                Bertahan: <span class="{{ $claim->days_remaining < 3 ? 'text-danger fw-bold' : 'text-info' }}">{{ $claim->days_remaining }} hari</span>
                                            @endif
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    @if($claim->kingdom_id)
                                        <span class="badge bg-primary bg-opacity-25 border border-primary text-primary">
                                            <i class="bi bi-flag-fill me-1"></i> {{ strtoupper($claim->kingdom_id) }}
                                        </span>
                                    @else
                                        <span class="badge bg-secondary bg-opacity-25 text-muted">Netral</span>
                                    @endif
                                </td>
                                <td>
                                    {!! $claim->status_badge !!}
                                    @if($claim->isInGracePeriod() && $claim->grace_period_until)
                                        <div class="text-danger small mt-1">
                                            Batas: {{ $claim->grace_period_until->diffForHumans() }}
                                        </div>
                                    @endif
                                </td>
                                <td class="text-end pe-4">
                                    <div class="d-flex justify-content-end gap-2">
                                        <button type="button" class="btn btn-sm btn-outline-success" data-bs-toggle="modal" data-bs-target="#depositModal-{{ $claim->id }}" title="Suntik Saldo Upkeep">
                                            <i class="bi bi-cash-coin me-1"></i> Setor
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#unclaimModal-{{ $claim->id }}" title="Cabut Klaim Wilayah">
                                            <i class="bi bi-trash3 me-1"></i> Cabut
                                        </button>
                                    </div>

                                    {{-- Modal Deposit Saldo --}}
                                    <div class="modal fade text-start" id="depositModal-{{ $claim->id }}" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content" style="background: #14171d; color: #fff; border: 1px solid rgba(245, 158, 11, 0.3);">
                                                <form action="{{ route('apexsions-bridge.admin.claims.deposit', $claim->id) }}" method="POST">
                                                    @csrf
                                                    <div class="modal-header border-secondary border-opacity-25">
                                                        <h5 class="modal-title fw-bold text-warning">
                                                            <i class="bi bi-cash-coin me-2"></i> Suntik Saldo Brankas Wilayah
                                                        </h5>
                                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p class="small text-muted mb-3">
                                                            Suntikkan dana administratif ke brankas klaim milik <strong class="text-warning">{{ $claim->owner_name }}</strong> untuk memperpanjang masa aktif proteksi.
                                                        </p>
                                                        <div class="mb-3">
                                                            <label class="form-label small text-muted">Nominal Suntikan Saldo (Rp)</label>
                                                            <input type="number" name="amount" class="form-control bg-dark text-white border-secondary" placeholder="Contoh: 10000" min="100" required>
                                                        </div>
                                                        <div class="d-flex gap-2">
                                                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="this.form.amount.value = 5000">Rp5.000</button>
                                                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="this.form.amount.value = 25000">Rp25.000</button>
                                                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="this.form.amount.value = 100000">Rp100.000</button>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer border-secondary border-opacity-25">
                                                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                                                        <button type="submit" class="btn btn-warning btn-sm fw-bold">Suntik Saldo Sekarang</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>

                                    {{-- Modal Force Unclaim --}}
                                    <div class="modal fade text-start" id="unclaimModal-{{ $claim->id }}" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content" style="background: #14171d; color: #fff; border: 1px solid rgba(239, 68, 68, 0.3);">
                                                <form action="{{ route('apexsions-bridge.admin.claims.unclaim', $claim->id) }}" method="POST">
                                                    @csrf
                                                    <div class="modal-header border-secondary border-opacity-25">
                                                        <h5 class="modal-title fw-bold text-danger">
                                                            <i class="bi bi-shield-x me-2"></i> Konfirmasi Pencabutan Klaim
                                                        </h5>
                                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <p class="small text-muted mb-3">
                                                            Apakah Anda yakin ingin mencabut hak kedaulatan tanah di chunk <strong class="text-white">[{{ $claim->chunk_x }}, {{ $claim->chunk_z }}]</strong> milik <strong class="text-warning">{{ $claim->owner_name }}</strong>? Tanah akan kembali menjadi alam liar bebas.
                                                        </p>
                                                        <div class="mb-3">
                                                            <label class="form-label small text-muted">Alasan Pencabutan</label>
                                                            <textarea name="reason" class="form-control bg-dark text-white border-secondary" rows="2" placeholder="Alasan administratif..."></textarea>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer border-secondary border-opacity-25">
                                                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                                                        <button type="submit" class="btn btn-danger btn-sm fw-bold">Cabut Klaim Otoritatif</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                    Tidak ada petak klaim wilayah yang ditemukan sesuai kriteria filter.
                                </td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
            @if($claims->hasPages())
                <div class="card-footer bg-transparent border-0 pt-3 px-4">
                    {{ $claims->links() }}
                </div>
            @endif
        </div>
    </div>

</div>
@endsection
