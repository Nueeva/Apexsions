@extends('admin.layouts.admin')

@section('title', 'Reports Center — Apexsions Admin')

@section('content')
<div class="container-fluid px-4 py-3" style="background: #0d0f12; min-height: 100vh; color: #e2e8f0; font-family: 'Outfit', sans-serif;">

    {{-- Breadcrumb & Title --}}
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom border-secondary border-opacity-25">
        <div>
            <div class="text-warning text-uppercase small fw-bold tracking-wider" style="letter-spacing: 1.5px;">
                <i class="bi bi-shield-exclamation me-1"></i> Operations & Security
            </div>
            <h2 class="fw-bold mb-0 text-white" style="letter-spacing: -0.5px;">Reports Center</h2>
            <p class="text-muted small mb-0">Manajemen laporan pelanggaran pemain, antrean investigasi, dan penindakan staf.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.moderation.index') }}" class="btn btn-outline-warning btn-sm">
                <i class="bi bi-shield-shaded me-1"></i> Moderation Center
            </a>
            <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-journal-text me-1"></i> Unified Audit Logs
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

    {{-- Metric Stat Counters --}}
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #f59e0b !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Tiket Terbuka (Open)</div>
                    <div class="fs-3 fw-bold text-warning">{{ number_format($openCount) }}</div>
                    <small class="text-muted">Menunggu investigasi</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #3b82f6 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Sedang Ditangani (Claimed)</div>
                    <div class="fs-3 fw-bold text-info">{{ number_format($claimedCount) }}</div>
                    <small class="text-muted">Telah diambil staf</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #10b981 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Selesai (Resolved)</div>
                    <div class="fs-3 fw-bold text-success">{{ number_format($resolvedCount) }}</div>
                    <small class="text-muted">Tindakan telah diambil</small>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #8b5cf6 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Total Seluruh Tiket</div>
                    <div class="fs-3 fw-bold text-white">{{ number_format($totalCount) }}</div>
                    <small class="text-muted">Riwayat keseluruhan</small>
                </div>
            </div>
        </div>
    </div>

    {{-- Filter and Search Bar --}}
    <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px;">
        <div class="card-body p-3">
            <form action="{{ route('apexsions-bridge.admin.reports.index') }}" method="GET" class="row g-2 align-items-center">
                {{-- Search Box --}}
                <div class="col-md-4">
                    <div class="input-group input-group-sm">
                        <span class="input-group-text border-0" style="background: #1a1e26; color: #94a3b8;">
                            <i class="bi bi-search"></i>
                        </span>
                        <input type="text" name="q" value="{{ $search }}" class="form-control border-0" 
                               placeholder="Cari ID, pelapor, terlapor, atau alasan..." 
                               style="background: #1a1e26; color: #f8fafc;">
                    </div>
                </div>

                {{-- Status Filter --}}
                <div class="col-md-3">
                    <select name="status" class="form-select form-select-sm border-0" style="background: #1a1e26; color: #f8fafc;">
                        <option value="all" {{ $selectedStatus === 'all' ? 'selected' : '' }}>Semua Status</option>
                        <option value="OPEN" {{ $selectedStatus === 'OPEN' ? 'selected' : '' }}>OPEN (Belum Ditangani)</option>
                        <option value="CLAIMED" {{ $selectedStatus === 'CLAIMED' ? 'selected' : '' }}>CLAIMED (Diklaim Staf)</option>
                        <option value="INVESTIGATING" {{ $selectedStatus === 'INVESTIGATING' ? 'selected' : '' }}>INVESTIGATING (Sedang Diinvestigasi)</option>
                        <option value="RESOLVED" {{ $selectedStatus === 'RESOLVED' ? 'selected' : '' }}>RESOLVED (Selesai)</option>
                        <option value="DISMISSED" {{ $selectedStatus === 'DISMISSED' ? 'selected' : '' }}>DISMISSED (Ditolak)</option>
                    </select>
                </div>

                {{-- Priority Filter --}}
                <div class="col-md-3">
                    <select name="priority" class="form-select form-select-sm border-0" style="background: #1a1e26; color: #f8fafc;">
                        <option value="all" {{ $selectedPriority === 'all' ? 'selected' : '' }}>Semua Prioritas</option>
                        <option value="LOW" {{ $selectedPriority === 'LOW' ? 'selected' : '' }}>Prioritas: LOW</option>
                        <option value="MEDIUM" {{ $selectedPriority === 'MEDIUM' ? 'selected' : '' }}>Prioritas: MEDIUM</option>
                        <option value="HIGH" {{ $selectedPriority === 'HIGH' ? 'selected' : '' }}>Prioritas: HIGH</option>
                        <option value="CRITICAL" {{ $selectedPriority === 'CRITICAL' ? 'selected' : '' }}>Prioritas: CRITICAL</option>
                    </select>
                </div>

                {{-- Submit Buttons --}}
                <div class="col-md-2 d-flex gap-1">
                    <button type="submit" class="btn btn-warning btn-sm w-100 fw-bold" style="background: linear-gradient(135deg, #d97706, #f59e0b); border: none;">
                        Filter
                    </button>
                    @if(!empty($search) || $selectedStatus !== 'all' || $selectedPriority !== 'all')
                        <a href="{{ route('apexsions-bridge.admin.reports.index') }}" class="btn btn-secondary btn-sm" title="Reset Filter">
                            <i class="bi bi-arrow-counterclockwise"></i>
                        </a>
                    @endif
                </div>
            </form>
        </div>
    </div>

    {{-- Reports Data Table --}}
    <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; overflow: hidden;">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="color: #cbd5e1; border-color: rgba(255,255,255,0.05);">
                <thead style="background: #1a1e26; color: #94a3b8; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px;">
                    <tr>
                        <th class="ps-4">Tiket</th>
                        <th>Pelapor</th>
                        <th>Pemain Terlapor</th>
                        <th>Alasan Laporan</th>
                        <th>Prioritas</th>
                        <th>Status</th>
                        <th>Staf Penangan</th>
                        <th>Waktu</th>
                        <th class="text-end pe-4">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($reports as $r)
                        <tr>
                            {{-- ID --}}
                            <td class="ps-4">
                                <span class="fw-bold text-white font-monospace">#{{ $r->id }}</span>
                                @if($r->in_game_report_id)
                                    <div class="small text-muted" style="font-size: 0.7rem;">In-Game #{{ $r->in_game_report_id }}</div>
                                @endif
                            </td>

                            {{-- Reporter --}}
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <img src="https://crafatar.com/avatars/{{ $r->reporter_uuid }}?size=24&overlay=true" 
                                         alt="{{ $r->reporter_name }}" class="rounded" width="24" height="24"
                                         onerror="this.src='https://crafatar.com/avatars/steve?size=24&overlay=true'">
                                    <a href="{{ route('apexsions-bridge.admin.players.show', $r->reporter_uuid) }}" class="text-decoration-none text-light fw-medium">
                                        {{ $r->reporter_name }}
                                    </a>
                                </div>
                            </td>

                            {{-- Reported Player --}}
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <img src="https://crafatar.com/avatars/{{ $r->reported_uuid }}?size=24&overlay=true" 
                                         alt="{{ $r->reported_name }}" class="rounded" width="24" height="24"
                                         onerror="this.src='https://crafatar.com/avatars/steve?size=24&overlay=true'">
                                    <a href="{{ route('apexsions-bridge.admin.players.show', $r->reported_uuid) }}" class="text-decoration-none text-warning fw-bold">
                                        {{ $r->reported_name }}
                                    </a>
                                </div>
                            </td>

                            {{-- Reason --}}
                            <td style="max-width: 260px;">
                                <div class="text-truncate text-white" title="{{ $r->reason }}">
                                    {{ $r->reason }}
                                </div>
                                @if($r->notes->count() > 0)
                                    <span class="badge bg-secondary bg-opacity-25 text-info" style="font-size: 0.65rem;">
                                        <i class="bi bi-chat-left-text me-1"></i>{{ $r->notes->count() }} Catatan
                                    </span>
                                @endif
                            </td>

                            {{-- Priority --}}
                            <td>
                                @php
                                    $pColor = match($r->priority) {
                                        'CRITICAL' => 'danger',
                                        'HIGH' => 'warning',
                                        'MEDIUM' => 'info',
                                        default => 'secondary'
                                    };
                                @endphp
                                <span class="badge bg-{{ $pColor }} bg-opacity-15 text-{{ $pColor }} border border-{{ $pColor }} border-opacity-25" style="font-size: 0.7rem;">
                                    {{ $r->priority }}
                                </span>
                            </td>

                            {{-- Status --}}
                            <td>
                                @php
                                    $sColor = match($r->status) {
                                        'OPEN' => 'warning',
                                        'CLAIMED' => 'primary',
                                        'INVESTIGATING' => 'info',
                                        'RESOLVED' => 'success',
                                        'DISMISSED' => 'secondary',
                                        default => 'light'
                                    };
                                @endphp
                                <span class="badge bg-{{ $sColor }} bg-opacity-15 text-{{ $sColor }} border border-{{ $sColor }} border-opacity-25" style="font-size: 0.75rem;">
                                    ● {{ $r->status }}
                                </span>
                            </td>

                            {{-- Assigned Staff --}}
                            <td>
                                @if($r->assigned_staff_name)
                                    <span class="text-info small fw-medium">
                                        <i class="bi bi-person-badge me-1"></i>{{ $r->assigned_staff_name }}
                                    </span>
                                @else
                                    <span class="text-muted small fst-italic">Belum ada staf</span>
                                @endif
                            </td>

                            {{-- Time --}}
                            <td>
                                <span class="small text-muted" title="{{ $r->created_at->toIso8601String() }}">
                                    {{ $r->created_at->diffForHumans() }}
                                </span>
                            </td>

                            {{-- Action --}}
                            <td class="text-end pe-4">
                                <a href="{{ route('apexsions-bridge.admin.reports.show', $r->id) }}" class="btn btn-sm btn-outline-warning" style="border-radius: 6px;">
                                    <i class="bi bi-search me-1"></i> Periksa
                                </a>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="9" class="text-center py-5 text-muted">
                                <i class="bi bi-shield-check fs-1 d-block mb-2 text-success opacity-50"></i>
                                Tidak ada laporan pelanggaran yang cocok dengan kriteria filter saat ini.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>

        {{-- Pagination --}}
        @if($reports->hasPages())
            <div class="card-footer border-0 p-3" style="background: #101317;">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="small text-muted">
                        Menampilkan {{ $reports->firstItem() }} - {{ $reports->lastItem() }} dari {{ $reports->total() }} laporan
                    </div>
                    <div>
                        {{ $reports->links() }}
                    </div>
                </div>
            </div>
        @endif
    </div>

</div>
@endsection
