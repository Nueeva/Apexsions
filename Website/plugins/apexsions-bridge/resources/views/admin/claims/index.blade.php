@extends('admin.layouts.admin')

@section('title', 'Land Claims & Anti-Grief — Apexsions Admin')

@section('content')
<div class="container-fluid px-4 py-3" style="background: #0d0f12; min-height: 100vh; color: #e2e8f0; font-family: 'Outfit', sans-serif;">

    {{-- Header & Actions --}}
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom border-secondary border-opacity-25">
        <div>
            <div class="text-warning text-uppercase small fw-bold tracking-wider" style="letter-spacing: 1.5px;">
                <i class="bi bi-shield-shaded me-1"></i> Security & Territory Engine
            </div>
            <h2 class="fw-bold mb-0 text-white" style="letter-spacing: -0.5px;">Land Claims & Anti-Grief</h2>
            <p class="text-muted small mb-0">Inspeksi wilayah kedaulatan pemain, chunk terlindungi, dan manajemen anti-griefing realm.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="https://web.apexsions.my.id/rules" target="_blank" class="btn btn-outline-info btn-sm">
                <i class="bi bi-book me-1"></i> Regulasi Wilayah
            </a>
            <a href="{{ route('apexsions-bridge.admin.moderation.index') }}" class="btn btn-outline-warning btn-sm">
                <i class="bi bi-hammer me-1"></i> Moderation Center
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
                    <div class="text-muted small text-uppercase fw-bold">Pemilik Tanah Aktif</div>
                    <div class="fs-3 fw-bold text-success">{{ number_format($uniqueOwners) }}</div>
                    <small class="text-muted">Pemain memiliki klaim</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #38bdf8 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Dunia Terproteksi</div>
                    <div class="fs-3 fw-bold text-info">{{ count($availableWorlds) }}</div>
                    <small class="text-muted">Dimensi aktif</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #a855f7 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Proteksi Aktif</div>
                    <div class="fs-3 fw-bold text-purple" style="color: #c084fc;">100% SECURE</div>
                    <small class="text-muted">Blocks, Containers, Redstone, Exploits</small>
                </div>
            </div>
        </div>
    </div>

    {{-- Filter & Search Form --}}
    <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px;">
        <div class="card-body p-3">
            <form action="{{ route('apexsions-bridge.admin.claims.index') }}" method="GET" class="row g-2 align-items-center">
                <div class="col-md-5">
                    <div class="input-group">
                        <span class="input-group-text bg-dark border-secondary text-muted"><i class="bi bi-search"></i></span>
                        <input type="text" name="search" class="form-control bg-dark text-white border-secondary" placeholder="Cari nama pemain, UUID, atau ID klaim..." value="{{ $search }}">
                    </div>
                </div>
                <div class="col-md-3">
                    <select name="world" class="form-select bg-dark text-white border-secondary">
                        <option value="all" @if($selectedWorld === 'all') selected @endif>Semua Dunia (All Worlds)</option>
                        @foreach($availableWorlds as $w)
                            <option value="{{ $w }}" @if($selectedWorld === $w) selected @endif>{{ $w }}</option>
                        @endforeach
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                        <i class="bi bi-funnel-fill me-1"></i> Filter
                    </button>
                </div>
                <div class="col-md-2">
                    <a href="{{ route('apexsions-bridge.admin.claims.index') }}" class="btn btn-outline-secondary w-100">
                        Reset
                    </a>
                </div>
            </form>
        </div>
    </div>

    {{-- Claims Table --}}
    <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; overflow: hidden;">
        <div class="card-header border-0 py-3 px-4 d-flex justify-content-between align-items-center" style="background: #1a1e26;">
            <div>
                <h5 class="fw-bold text-white mb-0">
                    <i class="bi bi-geo-alt-fill me-2 text-warning"></i> Daftar Tanah Terklaim
                </h5>
                <small class="text-muted">Total {{ $claims->total() }} wilayah tercatat di server.</small>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle" style="background: transparent;">
                <thead style="background: #101217; color: #94a3b8; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px;">
                    <tr>
                        <th class="ps-4">Pemilik Tanah</th>
                        <th>Dunia (World)</th>
                        <th>Chunk Coord</th>
                        <th>Blok Pusat (X, Z)</th>
                        <th>Anggota Trust</th>
                        <th>Tgl Klaim</th>
                        <th class="text-end pe-4">Aksi Otoritatif</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($claims as $claim)
                        <tr>
                            <td class="ps-4">
                                <div class="d-flex align-items-center gap-2">
                                    <img src="https://mc-heads.net/avatar/{{ $claim->owner_name }}/28" alt="{{ $claim->owner_name }}" class="rounded shadow-sm" style="width: 28px; height: 28px;">
                                    <div>
                                        <a href="{{ route('apexsions-bridge.admin.players.show', $claim->owner_name) }}" class="fw-bold text-warning text-decoration-none">
                                            {{ $claim->owner_name }}
                                        </a>
                                        <div class="text-muted small font-monospace">{{ substr($claim->owner_uuid, 0, 8) }}...</div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-25 text-light border border-secondary border-opacity-25">
                                    <i class="bi bi-globe2 me-1 text-info"></i> {{ $claim->world }}
                                </span>
                            </td>
                            <td>
                                <span class="font-monospace text-warning">
                                    [{{ $claim->chunk_x }}, {{ $claim->chunk_z }}]
                                </span>
                            </td>
                            <td>
                                <span class="font-monospace text-muted small">
                                    X: {{ ($claim->chunk_x * 16) + 8 }}, Z: {{ ($claim->chunk_z * 16) + 8 }}
                                </span>
                            </td>
                            <td>
                                <span class="badge bg-info bg-opacity-10 text-info border border-info border-opacity-25">
                                    <i class="bi bi-people-fill me-1"></i> {{ $claim->trusted_count }} dipercaya
                                </span>
                            </td>
                            <td class="text-muted small">
                                {{ $claim->created_at->format('d M Y, H:i') }}
                            </td>
                            <td class="text-end pe-4">
                                <div class="d-flex justify-content-end gap-2">
                                    <form action="{{ route('apexsions-bridge.admin.claims.unclaim', $claim->id) }}" method="POST" onsubmit="return confirm('Apakah Anda yakin ingin MENCABUT paksa klaim tanah ini? Chunk akan kembali netral dan proteksi anti-grief akan dinonaktifkan.');">
                                        @csrf
                                        <button type="submit" class="btn btn-outline-danger btn-sm" title="Cabut Klaim Tanah (Force Unclaim)">
                                            <i class="bi bi-trash3-fill me-1"></i> Unclaim
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" class="text-center py-5 text-muted">
                                <i class="bi bi-geo-alt fs-1 d-block mb-2 opacity-50"></i>
                                Tidak ada data klaim tanah yang cocok dengan kriteria pencarian.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        @if($claims->hasPages())
            <div class="card-footer border-0 py-3 px-4" style="background: #1a1e26;">
                {{ $claims->links() }}
            </div>
        @endif
    </div>

</div>
@endsection
