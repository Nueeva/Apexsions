@extends('admin.layouts.admin')

@section('title', 'Bounty Oversight — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-crosshair text-warning me-2"></i>Bounty Oversight Desk
            </h2>
            <p class="text-white-50 small mb-0">
                Pusat pengawasan buronan aktif seantero realm Apexsions, agregasi perbendaharaan hadiah kepala, dan intervensi pembersihan buronan.
            </p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <a href="{{ route('apexsions-bridge.bounties') }}" target="_blank" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-box-arrow-up-right me-1"></i>Buka Papan Buronan Publik
            </a>
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

    <!-- Metrics Cards -->
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-4">
            <div class="card p-3 h-100 bg-black bg-opacity-25 border border-secondary border-opacity-25">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem; letter-spacing: 0.5px;">Total Buronan Aktif</span>
                    <i class="bi bi-person-fill-exclamation text-warning fs-4"></i>
                </div>
                <h3 class="fw-bold mb-0 text-white font-monospace">{{ number_format($bounties->count()) }}</h3>
                <small class="text-muted">Target yang memiliki buronan aktif di game</small>
            </div>
        </div>
        <div class="col-sm-6 col-xl-4">
            <div class="card p-3 h-100 bg-black bg-opacity-25 border border-secondary border-opacity-25">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem; letter-spacing: 0.5px;">Total Perbendaharaan Pool</span>
                    <i class="bi bi-cash-stack text-warning fs-4"></i>
                </div>
                <h3 class="fw-bold mb-0 text-warning font-monospace">Rp {{ number_format($totalPool, 0, ',', '.') }}</h3>
                <small class="text-muted">Akumulasi uang hadiah yang dipasang pemain</small>
            </div>
        </div>
        <div class="col-sm-6 col-xl-4">
            <div class="card p-3 h-100 bg-black bg-opacity-25 border border-secondary border-opacity-25">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem; letter-spacing: 0.5px;">Sinkronisasi Game Server</span>
                    <i class="bi bi-arrow-repeat text-info fs-4"></i>
                </div>
                <h3 class="fw-bold mb-0 text-white" style="font-size: 1.25rem;">
                    @if($lastSyncedAt)
                        {{ $lastSyncedAt->diffForHumans() }}
                    @else
                        <span class="text-muted">Belum ada sinkron</span>
                    @endif
                </h3>
                <small class="text-muted">
                    @if($lastSyncedAt)
                        Pembaruan otomatis tiap penempatan/pembunuhan
                    @else
                        Menunggu push snapshot dari ApexsionsCore
                    @endif
                </small>
            </div>
        </div>
    </div>

    <!-- Active Bounties Table -->
    <div class="card bg-black bg-opacity-25 border border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center py-3">
            <h5 class="card-title mb-0 text-white fw-bold">
                <i class="bi bi-list-stars text-warning me-2"></i>Daftar Kepala Buronan (Top Bounties)
            </h5>
            <span class="badge bg-dark border border-secondary text-warning">
                Maks. 200 Target
            </span>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="text-muted text-uppercase small" style="font-size: 0.75rem; letter-spacing: 0.5px;">
                        <th style="width: 60px;">#</th>
                        <th>Target Buronan</th>
                        <th>Total Hadiah</th>
                        <th>Kontributor</th>
                        <th>Terakhir Disinkronkan</th>
                        <th class="text-end" style="width: 140px;">Tindakan</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($bounties as $index => $bounty)
                        <tr>
                            <td class="font-monospace text-warning fw-bold">{{ $index + 1 }}</td>
                            <td>
                                @php
                                    $isBedrock = str_starts_with($bounty->target_uuid, '00000000-0000-0000-');
                                    $avatarUrl = $isBedrock 
                                        ? 'https://mc-heads.net/avatar/MHF_Steve/36' 
                                        : 'https://mc-heads.net/avatar/' . $bounty->target_uuid . '/36';
                                @endphp
                                <div class="d-flex align-items-center gap-2">
                                    <img src="{{ $avatarUrl }}" alt="{{ $bounty->target_name }}" class="rounded border border-secondary" width="36" height="36">
                                    <div>
                                        <div class="text-white fw-bold d-flex align-items-center gap-1">
                                            {{ $bounty->target_name }}
                                            @if($isBedrock)
                                                <span class="badge bg-info text-dark font-monospace" style="font-size: 0.65rem;">BEDROCK</span>
                                            @endif
                                        </div>
                                        <div class="text-muted font-monospace" style="font-size: 0.7rem;">{{ $bounty->target_uuid }}</div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="text-warning fw-bold font-monospace fs-6">
                                    Rp {{ number_format($bounty->total_amount, 0, ',', '.') }}
                                </span>
                            </td>
                            <td>
                                <div class="text-white fw-semibold">
                                    <i class="bi bi-people me-1 text-muted"></i>{{ $bounty->contributor_count }} pemburu
                                </div>
                                @if(!empty($bounty->top_contributors))
                                    <div class="text-muted small" style="font-size: 0.75rem;">
                                        Top: {{ implode(', ', array_slice($bounty->top_contributors, 0, 3)) }}
                                    </div>
                                @endif
                            </td>
                            <td class="text-muted small">
                                {{ $bounty->last_synced_at?->diffForHumans() ?? '—' }}
                            </td>
                            <td class="text-end">
                                <form action="{{ route('apexsions-bridge.admin.bounties.clear') }}" method="POST" class="d-inline"
                                      onsubmit="return confirm('Peringatan Admin: Apakah Anda yakin ingin membatalkan/membersihkan seluruh bounty target {{ $bounty->target_name }}? Perintah akan dikirim ke game server secara atomic.');">
                                    @csrf
                                    <input type="hidden" name="target_name" value="{{ $bounty->target_name }}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger shadow-sm">
                                        <i class="bi bi-x-circle me-1"></i>Bersihkan
                                    </button>
                                </form>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center text-muted py-5">
                                <i class="bi bi-shield-check text-success fs-1 d-block mb-2"></i>
                                <span class="fw-semibold text-white">Tidak Ada Buronan Aktif</span>
                                <p class="small text-muted mb-0">Semua target buronan telah dibersihkan atau belum ada pemain yang memasang bounty in-game.</p>
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>
@endsection