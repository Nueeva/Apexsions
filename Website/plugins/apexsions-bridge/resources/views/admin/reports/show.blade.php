@extends('admin.layouts.admin')

@section('title', 'Laporan #' . $report->id . ' — Apexsions Admin')

@section('content')
<div class="container-fluid px-4 py-3" style="background: #0d0f12; min-height: 100vh; color: #e2e8f0; font-family: 'Outfit', sans-serif;">

    {{-- Breadcrumbs & Navigation --}}
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom border-secondary border-opacity-25">
        <div>
            <div class="text-warning text-uppercase small fw-bold tracking-wider" style="letter-spacing: 1.5px;">
                <a href="{{ route('apexsions-bridge.admin.reports.index') }}" class="text-decoration-none text-muted">
                    <i class="bi bi-arrow-left me-1"></i> Reports Center
                </a>
                <span class="mx-2 text-muted">/</span> Tiket #{{ $report->id }}
            </div>
            <h2 class="fw-bold mb-0 text-white">Laporan Pelanggaran #{{ $report->id }}</h2>
        </div>
        <div class="d-flex gap-2 align-items-center">
            @php
                $sColor = match($report->status) {
                    'OPEN' => 'warning',
                    'CLAIMED' => 'primary',
                    'INVESTIGATING' => 'info',
                    'RESOLVED' => 'success',
                    'DISMISSED' => 'secondary',
                    default => 'light'
                };
            @endphp
            <span class="badge bg-{{ $sColor }} bg-opacity-20 text-{{ $sColor }} border border-{{ $sColor }} border-opacity-25 px-3 py-2 fs-6">
                ● Status: {{ $report->status }}
            </span>
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

    <div class="row g-4">
        {{-- Left Column: Report Information & Notes --}}
        <div class="col-lg-8">
            {{-- Main Details Card --}}
            <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px;">
                <div class="card-header border-0 py-3 px-4" style="background: #1a1e26; border-radius: 12px 12px 0 0;">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="fw-bold text-white fs-5">
                            <i class="bi bi-file-earmark-text me-2 text-warning"></i> Detail Insiden
                        </div>
                        <span class="badge bg-secondary bg-opacity-25 text-light">
                            Prioritas: {{ $report->priority }}
                        </span>
                    </div>
                </div>
                <div class="card-body p-4">
                    <div class="mb-4">
                        <label class="text-muted small text-uppercase fw-bold mb-1">Alasan Pelanggaran</label>
                        <div class="fs-5 fw-bold text-warning p-3 rounded" style="background: #101317; border-left: 4px solid #f59e0b;">
                            {{ $report->reason }}
                        </div>
                    </div>

                    @if($report->description)
                        <div class="mb-4">
                            <label class="text-muted small text-uppercase fw-bold mb-1">Keterangan / Bukti Tambahan</label>
                            <div class="p-3 rounded text-white" style="background: #101317; white-space: pre-wrap;">{{ $report->description }}</div>
                        </div>
                    @endif

                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="text-muted small text-uppercase fw-bold mb-1">Server Target</label>
                            <div class="text-white fw-medium font-monospace">{{ $report->server }}</div>
                        </div>
                        <div class="col-md-4">
                            <label class="text-muted small text-uppercase fw-bold mb-1">Dunia TKP</label>
                            <div class="text-white fw-medium font-monospace">{{ $report->world }}</div>
                        </div>
                        <div class="col-md-4">
                            <label class="text-muted small text-uppercase fw-bold mb-1">Waktu Dilaporkan</label>
                            <div class="text-white fw-medium" title="{{ $report->created_at->toIso8601String() }}">
                                {{ $report->created_at->format('d M Y, H:i:s') }}
                                <span class="text-muted small">({{ $report->created_at->diffForHumans() }})</span>
                            </div>
                        </div>
                    </div>

                    {{-- Resolution section if resolved --}}
                    @if(in_array($report->status, ['RESOLVED', 'DISMISSED']) && $report->resolution)
                        <div class="mt-4 p-3 rounded" style="background: #0f1f1a; border: 1px solid rgba(16, 185, 129, 0.2);">
                            <div class="text-success fw-bold small text-uppercase mb-1">
                                <i class="bi bi-check2-circle me-1"></i> Keputusan Akhir Penanganan
                            </div>
                            <div class="text-white">{{ $report->resolution }}</div>
                            <div class="small text-muted mt-2">
                                Diselesaikan pada {{ $report->resolved_at ? $report->resolved_at->format('d M Y, H:i') : '-' }}
                            </div>
                        </div>
                    @endif
                </div>
            </div>

            {{-- Staff Notes Section --}}
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px;">
                <div class="card-header border-0 py-3 px-4" style="background: #1a1e26; border-radius: 12px 12px 0 0;">
                    <div class="fw-bold text-white fs-5">
                        <i class="bi bi-chat-left-quote me-2 text-info"></i> Catatan Internal Staf ({{ $report->notes->count() }})
                    </div>
                </div>
                <div class="card-body p-4">
                    {{-- Note Composer --}}
                    <form action="{{ route('apexsions-bridge.admin.reports.notes.store', $report->id) }}" method="POST" class="mb-4">
                        @csrf
                        <div class="mb-2">
                            <textarea name="note" rows="3" class="form-control border-0" 
                                      placeholder="Tuliskan catatan investigasi, barang bukti, atau hasil interogasi internal..." 
                                      style="background: #101317; color: #f8fafc; border-radius: 8px;" required></textarea>
                        </div>
                        <div class="d-flex justify-content-end">
                            <button type="submit" class="btn btn-info btn-sm fw-bold">
                                <i class="bi bi-plus-circle me-1"></i> Tambahkan Catatan
                            </button>
                        </div>
                    </form>

                    {{-- Notes Timeline --}}
                    <div class="d-flex flex-column gap-3">
                        @forelse($report->notes as $note)
                            <div class="p-3 rounded" style="background: #1a1e26; border-left: 3px solid #38bdf8;">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="fw-bold text-info small">
                                        <i class="bi bi-person-badge me-1"></i> {{ $note->author_name }}
                                    </span>
                                    <span class="text-muted small">
                                        {{ $note->created_at->diffForHumans() }}
                                    </span>
                                </div>
                                <div class="text-white" style="white-space: pre-wrap;">{{ $note->note }}</div>
                            </div>
                        @empty
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-journal-text fs-4 d-block mb-1 opacity-50"></i>
                                Belum ada catatan internal pada laporan ini.
                            </div>
                        @endforelse
                    </div>
                </div>
            </div>
        </div>

        {{-- Right Column: Player Context & Operational Actions --}}
        <div class="col-lg-4">
            {{-- Reported Player Context Card --}}
            <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px; border-top: 4px solid #ef4444 !important;">
                <div class="card-body p-4 text-center">
                    <div class="text-danger small text-uppercase fw-bold mb-2">Pemain Terlapor</div>
                    <img src="https://crafatar.com/avatars/{{ $report->reported_uuid }}?size=64&overlay=true" 
                         alt="{{ $report->reported_name }}" class="rounded shadow mb-2" width="64" height="64"
                         onerror="this.src='https://crafatar.com/avatars/steve?size=64&overlay=true'">
                    <h4 class="fw-bold text-white mb-1">{{ $report->reported_name }}</h4>
                    <div class="small text-muted font-monospace mb-3">{{ $report->reported_uuid }}</div>

                    <div class="d-grid gap-2">
                        <a href="{{ route('apexsions-bridge.admin.players.show', $report->reported_uuid) }}" class="btn btn-outline-danger btn-sm">
                            <i class="bi bi-person-bounding-box me-1"></i> Buka Profil Player 360
                        </a>
                        <a href="{{ route('apexsions-bridge.admin.moderation.index') }}?target={{ urlencode($report->reported_name) }}" class="btn btn-danger btn-sm fw-bold">
                            <i class="bi bi-hammer me-1"></i> Berikan Sanksi / Moderasi
                        </a>
                    </div>
                </div>
            </div>

            {{-- Reporter Context Card --}}
            <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px;">
                <div class="card-body p-3 d-flex align-items-center gap-3">
                    <img src="https://crafatar.com/avatars/{{ $report->reporter_uuid }}?size=48&overlay=true" 
                         alt="{{ $report->reporter_name }}" class="rounded shadow" width="48" height="48"
                         onerror="this.src='https://crafatar.com/avatars/steve?size=48&overlay=true'">
                    <div class="flex-grow-1 overflow-hidden">
                        <div class="text-muted small text-uppercase fw-bold">Pelapor</div>
                        <h6 class="fw-bold text-white mb-0 text-truncate">{{ $report->reporter_name }}</h6>
                        <a href="{{ route('apexsions-bridge.admin.players.show', $report->reporter_uuid) }}" class="small text-warning text-decoration-none">
                            Lihat Profil Pelapor &rarr;
                        </a>
                    </div>
                </div>
            </div>

            {{-- Workflow Operations Card --}}
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px;">
                <div class="card-header border-0 py-3 px-4" style="background: #1a1e26; border-radius: 12px 12px 0 0;">
                    <div class="fw-bold text-white">
                        <i class="bi bi-gear-fill me-2 text-warning"></i> Tindakan Pengelolaan
                    </div>
                </div>
                <div class="card-body p-4 d-flex flex-column gap-3">
                    {{-- 1. Atomic Claim Button --}}
                    @if(in_array($report->status, ['OPEN', 'REVIEWING']) || empty($report->assigned_staff_id))
                        <form action="{{ route('apexsions-bridge.admin.reports.claim', $report->id) }}" method="POST">
                            @csrf
                            <button type="submit" class="btn btn-warning w-100 fw-bold py-2 shadow-sm" style="background: linear-gradient(135deg, #d97706, #f59e0b); border: none;">
                                <i class="bi bi-hand-index-thumb me-1"></i> Klaim Penanganan (Claim)
                            </button>
                        </form>
                    @else
                        <div class="p-3 rounded text-center" style="background: #1a1e26;">
                            <div class="small text-muted mb-1">Staf Penanggung Jawab</div>
                            <div class="fw-bold text-info">
                                <i class="bi bi-person-check-fill me-1"></i> {{ $report->assigned_staff_name }}
                            </div>
                            <div class="small text-muted">Diklaim {{ $report->assigned_at ? $report->assigned_at->diffForHumans() : '' }}</div>
                        </div>
                    @endif

                    {{-- 2. Assign to Other Staff --}}
                    <form action="{{ route('apexsions-bridge.admin.reports.assign', $report->id) }}" method="POST">
                        @csrf
                        <label class="text-muted small text-uppercase fw-bold mb-1">Alihkan ke Staf Lain</label>
                        <div class="input-group input-group-sm">
                            <select name="staff_id" class="form-select border-0" style="background: #101317; color: #f8fafc;" required>
                                <option value="">Pilih Staf...</option>
                                @foreach($allStaff as $staff)
                                    <option value="{{ $staff->id }}" {{ $report->assigned_staff_id == $staff->id ? 'selected' : '' }}>
                                        {{ $staff->name }}
                                    </option>
                                @endforeach
                            </select>
                            <button type="submit" class="btn btn-outline-secondary">Tugaskan</button>
                        </div>
                    </form>

                    <hr class="border-secondary border-opacity-25 my-1">

                    {{-- 3. Update Status / Resolution Form --}}
                    <form action="{{ route('apexsions-bridge.admin.reports.status', $report->id) }}" method="POST">
                        @csrf
                        <label class="text-muted small text-uppercase fw-bold mb-1">Perbarui Status</label>
                        <select name="status" class="form-select form-select-sm border-0 mb-2" style="background: #101317; color: #f8fafc;" required>
                            <option value="OPEN" {{ $report->status === 'OPEN' ? 'selected' : '' }}>OPEN</option>
                            <option value="CLAIMED" {{ $report->status === 'CLAIMED' ? 'selected' : '' }}>CLAIMED</option>
                            <option value="INVESTIGATING" {{ $report->status === 'INVESTIGATING' ? 'selected' : '' }}>INVESTIGATING</option>
                            <option value="RESOLVED" {{ $report->status === 'RESOLVED' ? 'selected' : '' }}>RESOLVED (Selesai)</option>
                            <option value="DISMISSED" {{ $report->status === 'DISMISSED' ? 'selected' : '' }}>DISMISSED (Ditolak)</option>
                        </select>

                        <label class="text-muted small text-uppercase fw-bold mb-1">Keputusan / Alasan Resolusi</label>
                        <textarea name="resolution" rows="2" class="form-control form-control-sm border-0 mb-2" 
                                  placeholder="Tuliskan keputusan atau alasan jika menandai Selesai/Tolak..." 
                                  style="background: #101317; color: #f8fafc;">{{ $report->resolution }}</textarea>

                        <button type="submit" class="btn btn-outline-warning btn-sm w-100 fw-bold">
                            Simpan Perubahan Status
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

</div>
@endsection
