@extends('admin.layouts.admin')

@section('title', 'Rank: ' . $rank['display_name'])

@section('content')
<div class="mb-4">
    <a href="{{ route('apexsions-bridge.admin.ranks.index') }}" class="btn btn-sm btn-outline-secondary mb-3">
        <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Rank
    </a>

    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid {{ $rank['color'] }}; box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div class="d-flex align-items-center gap-3">
                <div class="p-3 rounded-circle" style="background: rgba(0,0,0,0.5); border: 2px solid {{ $rank['color'] }};">
                    <span class="fs-2" style="color: {{ $rank['color'] }};">✦</span>
                </div>
                <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <span class="badge bg-dark border border-secondary text-white">{{ $rank['tier'] }}</span>
                        <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
                            {{ $rank['display_name'] }}
                        </h3>
                        <span class="badge px-2 py-1 font-monospace fw-bold" style="background: rgba(0,0,0,0.6); border: 1px solid {{ $rank['color'] }}; color: {{ $rank['color'] }};">
                            {{ $rank['badge'] }}
                        </span>
                    </div>
                    <p class="text-muted small mb-0">
                        {{ $rank['description'] }}
                    </p>
                </div>
            </div>

            <div class="d-flex flex-wrap align-items-center gap-3">
                <div class="text-end">
                    <span class="small text-muted d-block">Bobot (Weight)</span>
                    <span class="badge bg-dark border border-secondary text-warning fs-6 px-3 py-1">{{ $rank['weight'] }}</span>
                </div>
                <div class="text-end">
                    <span class="small text-muted d-block">Total Pemegang</span>
                    <span class="badge bg-dark border border-secondary text-light fs-6 px-3 py-1">{{ number_format($players->total()) }} Pemain</span>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Player Search & Filter Bar -->
<div class="card shadow-sm mb-4" style="background: rgba(18, 20, 26, 0.95); border: 1px solid rgba(201, 164, 92, 0.22);">
    <div class="card-body p-3">
        <form action="{{ route('apexsions-bridge.admin.ranks.show', $rank['key']) }}" method="GET" class="row g-2 align-items-center">
            <div class="col-md-8 col-lg-9">
                <div class="input-group input-group-sm">
                    <span class="input-group-text bg-dark text-muted border-secondary"><i class="bi bi-search"></i></span>
                    <input type="text" name="q" value="{{ $search }}" class="form-control bg-dark text-light border-secondary" placeholder="Cari username atau UUID pemegang rank ini...">
                </div>
            </div>
            <div class="col-md-4 col-lg-3 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary w-100">
                    <i class="bi bi-filter me-1"></i> Cari
                </button>
                @if($search)
                    <a href="{{ route('apexsions-bridge.admin.ranks.show', $rank['key']) }}" class="btn btn-sm btn-outline-secondary">
                        Reset
                    </a>
                @endif
            </div>
        </form>
    </div>
</div>

<!-- Players Holding This Rank Table -->
<div class="card shadow-sm mb-4" style="background: rgba(18, 20, 26, 0.95); border: 1px solid rgba(201, 164, 92, 0.22);">
    <div class="card-header bg-transparent border-bottom border-secondary py-3 px-4 d-flex justify-content-between align-items-center">
        <h6 class="mb-0 fw-bold text-white text-uppercase" style="letter-spacing: 1px; font-size: 0.85rem;">
            <i class="bi bi-people-fill text-warning me-2"></i> Daftar Warga dengan Rank {{ $rank['display_name'] }}
        </h6>
        <span class="small text-muted">Menampilkan {{ $players->firstItem() ?? 0 }} - {{ $players->lastItem() ?? 0 }} dari {{ $players->total() }}</span>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 text-light" style="font-size: 0.86rem;">
            <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.72rem; background: rgba(0,0,0,0.3);">
                <tr>
                    <th class="ps-4">Pemain</th>
                    <th>Level & XP</th>
                    <th>Saldo Rupiah</th>
                    <th>Saldo Diamond</th>
                    <th>Kerajaan</th>
                    <th>Terakhir Dilihat</th>
                    <th class="text-end pe-4">Aksi</th>
                </tr>
            </thead>
            <tbody>
                @forelse($players as $p)
                    <tr>
                        <td class="ps-4">
                            <div class="d-flex align-items-center gap-2">
                                <img src="https://mc-heads.net/avatar/{{ urlencode($p->minecraft_username) }}/32" alt="{{ $p->minecraft_username }}" class="rounded border border-secondary" width="32" height="32" onerror="this.src='{{ asset('assets/themes/apexsions/img/favicon.ico') }}'">
                                <div>
                                    <a href="{{ route('apexsions-bridge.admin.players.show', $p->minecraft_uuid) }}" class="text-white fw-bold text-decoration-none hover-warning">
                                        {{ $p->minecraft_username }}
                                    </a>
                                    <small class="text-muted d-block font-monospace" style="font-size: 0.72rem;">{{ Str::limit($p->minecraft_uuid, 16) }}</small>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span class="badge bg-dark border border-secondary text-warning fw-bold">Lv. {{ $p->level }}</span>
                            <small class="text-muted d-block mt-1">{{ number_format($p->xp) }} XP</small>
                        </td>
                        <td>
                            <span class="text-success fw-bold">Rp {{ number_format($p->balance_rupiah, 0, ',', '.') }}</span>
                        </td>
                        <td>
                            <span class="text-info fw-bold">{{ number_format($p->balance_diamond, 0, ',', '.') }} 💎</span>
                        </td>
                        <td>
                            @if($p->kingdom && strtoupper($p->kingdom) !== 'NONE')
                                <span class="badge bg-dark border border-secondary text-light">{{ $p->kingdom_display ?: $p->kingdom }}</span>
                            @else
                                <span class="badge bg-secondary bg-opacity-25 text-muted">Belum Memilih</span>
                            @endif
                        </td>
                        <td>
                            <span class="text-muted small">{{ $p->last_seen_at ? $p->last_seen_at->diffForHumans() : 'Belum pernah' }}</span>
                        </td>
                        <td class="text-end pe-4">
                            <a href="{{ route('apexsions-bridge.admin.players.show', $p->minecraft_uuid) }}" class="btn btn-sm btn-outline-warning">
                                <i class="bi bi-person-lines-fill me-1"></i> Kelola Pemain
                            </a>
                        </td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            Tidak ada pemain yang memegang rank ini.
                        </td>
                    </tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($players->hasPages())
        <div class="card-footer bg-transparent border-top border-secondary py-3 px-4">
            {{ $players->links() }}
        </div>
    @endif
</div>
@endsection
