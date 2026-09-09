@extends('admin.layouts.admin')

@section('title', 'Rank Management')

@section('content')
<div class="mb-4">
    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="badge bg-warning text-dark fw-bold px-2 py-1">SOURCE OF TRUTH</span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1.5px;">
                        👑 RANK MANAGEMENT & HIERARCHY
                    </h3>
                </div>
                <p class="text-muted small mb-0" style="max-width: 750px;">
                    Manajemen resmi 11 rank realm Apexsions yang terhubung langsung dengan LuckPerms server Minecraft (<code class="text-warning">ranks.yml</code>). Penugasan atau pengubahan rank dieksekusi secara instan dan tercatat di Unified Audit Log.
                </p>
            </div>
            <div class="d-flex align-items-center gap-2">
                <button type="button" class="btn btn-warning fw-bold px-3 shadow-sm" data-bs-toggle="modal" data-bs-target="#assignRankModal">
                    <i class="bi bi-person-plus-fill me-1"></i> Ubah Rank Pemain
                </button>
            </div>
        </div>
    </div>
</div>

<!-- KPI Cards -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Total Warga Realm</span>
                <i class="bi bi-people-fill text-warning fs-5"></i>
            </div>
            <h3 class="fw-bold mb-0">{{ number_format($totalPlayers) }}</h3>
            <small class="text-muted">Akun Minecraft terdaftar</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Total Rank Resmi</span>
                <i class="bi bi-diagram-3-fill text-info fs-5"></i>
            </div>
            <h3 class="fw-bold text-info mb-0">{{ count($ranks) }} Ranks</h3>
            <small class="text-muted">5 Tingkatan (Tier I - V)</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Otoritas Tertinggi</span>
                <i class="bi bi-shield-check text-danger fs-5"></i>
            </div>
            <h3 class="fw-bold text-danger mb-0">The Ancestor</h3>
            <small class="text-muted">Weight 100 &bull; Founder Protected</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Rank Standar Warga</span>
                <i class="bi bi-compass text-secondary fs-5"></i>
            </div>
            <h3 class="fw-bold mb-0">Wanderer</h3>
            <small class="text-muted">{{ number_format($playerCounts['wanderer'] ?? 0) }} Warga Baru Aktif</small>
        </div>
    </div>
</div>

<!-- Main Ranks Table Card -->
<div class="card shadow-sm mb-4">
    <div class="card-header py-3 px-4 d-flex justify-content-between align-items-center">
        <h5 class="mb-0 fw-bold" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
            <i class="bi bi-trophy-fill text-warning me-2"></i> Hirarki & Daftar Rank Resmi
        </h5>
        <span class="badge bg-secondary">Synchronized with LuckPerms</span>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0" style="font-size: 0.88rem;">
            <thead class="text-muted text-uppercase" style="font-size: 0.74rem;">
                <tr>
                    <th class="ps-4">Tingkat</th>
                    <th>Nama Rank</th>
                    <th>Badge / In-Game Display</th>
                    <th>Bobot (Weight)</th>
                    <th>Role & Fungsi</th>
                    <th class="text-center">Jumlah Pemain</th>
                    <th class="text-end pe-4">Aksi</th>
                </tr>
            </thead>
            <tbody>
                @foreach($ranks as $rankKey => $r)
                    <tr>
                        <td class="ps-4">
                            @if($r['tier'] === 'Tier V')
                                <span class="badge bg-danger text-white fw-bold px-2 py-1">TIER V</span>
                            @elseif($r['tier'] === 'Tier IV')
                                <span class="badge bg-primary text-white fw-bold px-2 py-1">TIER IV</span>
                            @elseif($r['tier'] === 'Tier III')
                                <span class="badge bg-info text-dark fw-bold px-2 py-1">TIER III</span>
                            @elseif($r['tier'] === 'Tier II')
                                <span class="badge bg-warning text-dark fw-bold px-2 py-1">TIER II</span>
                            @else
                                <span class="badge bg-secondary text-light fw-bold px-2 py-1">TIER I</span>
                            @endif
                        </td>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <span class="rounded-circle d-inline-block" style="width: 10px; height: 10px; background-color: {{ $r['color'] }}; box-shadow: 0 0 6px {{ $r['color'] }};"></span>
                                <div>
                                    <strong class="text-white d-block" style="font-size: 0.95rem;">{{ $r['display_name'] }}</strong>
                                    <code class="text-muted small">{{ $r['key'] }}</code>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span class="badge px-3 py-1 font-monospace fw-bold" style="background: rgba(0,0,0,0.6); border: 1px solid {{ $r['color'] }}; color: {{ $r['color'] }}; text-shadow: 0 0 8px {{ $r['color'] }};">
                                {{ $r['badge'] }}
                            </span>
                        </td>
                        <td>
                            <span class="badge bg-dark border border-secondary text-warning fw-bold px-2 py-1">
                                {{ $r['weight'] }}
                            </span>
                        </td>
                        <td>
                            <div style="max-width: 320px;">
                                <div class="text-white small fw-semibold">{{ $r['role'] }}</div>
                                <small class="text-muted d-block text-truncate" title="{{ $r['description'] }}">{{ $r['description'] }}</small>
                            </div>
                        </td>
                        <td class="text-center">
                            <span class="badge bg-dark border border-secondary text-light px-3 py-1 fw-bold fs-6">
                                {{ number_format($playerCounts[$rankKey] ?? 0) }}
                            </span>
                        </td>
                        <td class="text-end pe-4">
                            <a href="{{ route('apexsions-bridge.admin.ranks.show', $rankKey) }}" class="btn btn-sm btn-outline-warning">
                                <i class="bi bi-people me-1"></i> Lihat Warga
                            </a>
                        </td>
                    </tr>
                @endforeach
            </tbody>
        </table>
    </div>
</div>

<!-- Modal: Assign Rank -->
<div class="modal fade" id="assignRankModal" tabindex="-1" aria-labelledby="assignRankModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning" id="assignRankModalLabel" style="font-family: 'Cinzel', serif;">
                    <i class="bi bi-person-badge-fill me-2"></i> Ubah / Berikan Rank Pemain
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.ranks.assign') }}" method="POST">
                @csrf
                <div class="modal-body">
                    <p class="text-muted small mb-3">
                        Aksi ini akan mengirim perintah LuckPerms (<code class="text-warning">lp user &lt;player&gt; parent set &lt;rank&gt;</code>) ke server Minecraft melalui antrean WebBridge, memperbarui nametag, serta mencatat perubahan di Unified Audit Log.
                    </p>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Username atau UUID Pemain <span class="text-danger">*</span></label>
                        <input type="text" name="player_identifier" class="form-control" placeholder="Contoh: Steve atau 069a79f4-44e9..." required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Pilih Rank Baru <span class="text-danger">*</span></label>
                        <select name="rank" class="form-select" required>
                            <option value="" disabled selected>-- Pilih Rank Resmi --</option>
                            @foreach($ranks as $rKey => $rVal)
                                <option value="{{ $rKey }}">
                                    [{{ $rVal['tier'] }}] {{ $rVal['display_name'] }} (Weight: {{ $rVal['weight'] }})
                                    @if($rVal['is_protected']) [PROTECTED] @endif
                                </option>
                            @endforeach
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Perubahan / Penugasan <span class="text-danger">*</span></label>
                        <textarea name="reason" class="form-control" rows="3" placeholder="Contoh: Donasi Webstore Tier 2, Promosi Staff, atau Keputusan Admin..." required maxlength="250"></textarea>
                        <small class="text-muted">Alasan ini wajib diisi untuk integritas audit trail realm.</small>
                    </div>

                    <div class="alert alert-warning py-2 small mb-0">
                        <i class="bi bi-shield-exclamation me-1"></i> Pastikan identitas pemain dan rank sudah benar sebelum mengeksekusi.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">
                        <i class="bi bi-check-circle me-1"></i> Terapkan Rank
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
