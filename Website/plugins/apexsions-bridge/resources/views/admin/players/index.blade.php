@extends('admin.layouts.admin')

@section('title', 'Player Management')

@section('content')
<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h2 class="h3 mb-0 fw-bold" style="font-family: 'Cinzel', serif; letter-spacing: 1px; color: #F1D58A;">
            <i class="bi bi-people-fill me-2 text-warning"></i> PLAYER MANAGEMENT
        </h2>
        <small class="text-muted">Direktori seluruh warga peradaban Apexsions yang telah tersinkronisasi.</small>
    </div>
    <span class="badge bg-dark border border-secondary px-3 py-2 text-warning" style="letter-spacing: 1px;">
        TOTAL WARGA: {{ number_format($totalCount) }}
    </span>
</div>

<!-- Filter Bar -->
<div class="card mb-4">
    <div class="card-body p-3">
        <form method="GET" action="{{ route('apexsions-bridge.admin.players.index') }}" class="row g-2 align-items-center">
            <div class="col-md-4">
                <div class="input-group input-group-sm">
                    <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                    <input type="text" name="q" class="form-control form-control-sm" placeholder="Cari username atau UUID pemain..." value="{{ $search }}">
                </div>
            </div>

            <div class="col-md-2">
                <select name="rank" class="form-select form-select-sm">
                    <option value="all">-- Semua Rank --</option>
                    @foreach($availableRanks as $rk)
                        <option value="{{ $rk }}" @selected(strtolower($selectedRank) === strtolower($rk))>{{ ucfirst($rk) }}</option>
                    @endforeach
                </select>
            </div>

            <div class="col-md-2">
                <select name="kingdom" class="form-select form-select-sm">
                    <option value="all">-- Semua Kerajaan / Faksi --</option>
                    @foreach($availableKingdoms as $kd)
                        <option value="{{ $kd }}" @selected(strtoupper($selectedKingdom) === strtoupper($kd))>
                            @if($kd === 'AETHERION')
                                ✦ Aetherion (The Conclave)
                            @elseif($kd === 'NONE')
                                Tanpa Kerajaan (Mortal)
                            @else
                                {{ $kd }}
                            @endif
                        </option>
                    @endforeach
                </select>
            </div>

            <div class="col-md-2">
                <select name="status" class="form-select form-select-sm">
                    <option value="all">-- Semua Status --</option>
                    <option value="online" @selected($selectedStatus === 'online')>Online Sekarang</option>
                    <option value="offline" @selected($selectedStatus === 'offline')>Offline</option>
                </select>
            </div>

            <div class="col-md-2 d-flex gap-1">
                <button type="submit" class="btn btn-sm btn-primary w-100" title="Terapkan Filter">
                    <i class="bi bi-funnel me-1"></i> Filter
                </button>
                @if($search || ($selectedRank && $selectedRank !== 'all') || ($selectedKingdom && $selectedKingdom !== 'all') || ($selectedStatus && $selectedStatus !== 'all'))
                    <a href="{{ route('apexsions-bridge.admin.players.index') }}" class="btn btn-sm btn-outline-secondary" title="Reset Filter">
                        <i class="bi bi-x-lg"></i>
                    </a>
                @endif
            </div>
        </form>
    </div>
</div>

<!-- Player Directory Table -->
<div class="card shadow-sm mb-4">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="font-size: 0.88rem;">
                <thead style="background: rgba(201, 164, 92, 0.08); border-bottom: 1px solid rgba(201, 164, 92, 0.2);">
                    <tr class="text-uppercase text-muted" style="font-size: 0.75rem; letter-spacing: 1px;">
                        <th class="ps-3 py-3">Warga</th>
                        <th>Rank</th>
                        <th>Kerajaan</th>
                        <th>Level Karakter</th>
                        <th>Saldo Finansial</th>
                        <th>Status Live</th>
                        <th>Terakhir Terlihat</th>
                        <th class="text-end pe-3">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($players as $player)
                        @php
                            $isOnline = in_array(strtolower($player->minecraft_uuid), $onlineUuids, true)
                                || in_array(strtolower($player->minecraft_username), $onlineNames, true);
                        @endphp
                        <tr style="border-bottom: 1px solid rgba(255, 255, 255, 0.05);">
                            <td class="ps-3">
                                <div class="d-flex align-items-center gap-2">
                                    <img src="https://mc-heads.net/avatar/{{ urlencode($player->minecraft_username) }}/32" alt="{{ $player->minecraft_username }}" class="rounded shadow-sm" width="32" height="32" onerror="this.src='{{ asset('assets/themes/apexsions/img/favicon.ico') }}'">
                                    <div>
                                        <a href="{{ route('apexsions-bridge.admin.players.show', $player->minecraft_uuid) }}" class="fw-bold text-decoration-none text-white d-block">
                                            {{ $player->minecraft_username }}
                                        </a>
                                        <small class="text-muted d-block" style="font-size: 0.72rem;">
                                            <span class="badge {{ $player->isBedrock() ? 'bg-info text-dark fw-bold' : 'bg-dark border border-secondary text-muted' }}" style="font-size: 0.65rem;">
                                                {{ $player->edition }}
                                            </span>
                                            <code class="text-muted">{{ Str::limit($player->minecraft_uuid, 14, '...') }}</code>
                                        </small>
                                    </div>
                                </div>
                            </td>
                            <td>
                                @php
                                    $rankColors = [
                                        'ancestor' => 'bg-warning text-dark',
                                        'architect' => 'bg-info text-dark',
                                        'overseer' => 'bg-primary',
                                        'warden' => 'bg-primary',
                                        'herald' => 'bg-success',
                                        'sions' => 'bg-danger',
                                        'emperor' => 'bg-danger',
                                        'sovereign' => 'bg-dark border border-warning text-warning',
                                        'archon' => 'bg-secondary',
                                        'ascendant' => 'bg-secondary',
                                        'wanderer' => 'bg-secondary text-light',
                                    ];
                                    $rBadge = $rankColors[strtolower($player->rank)] ?? 'bg-secondary';
                                @endphp
                                <span class="badge {{ $rBadge }} px-2 py-1" style="letter-spacing: 0.5px;">
                                    {{ ucfirst($player->rank) }}
                                </span>
                            </td>
                            <td>
                                @php
                                    $staffRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald'];
                                    $isStaff = in_array(strtolower($player->rank), $staffRanks, true);
                                    $kCode = strtoupper($player->kingdom ?? 'NONE');

                                    if ($isStaff || $kCode === 'AETHERION') {
                                        $kBadge = 'bg-info text-dark fw-bold border border-info';
                                        $kName = '✦ Aetherion (Conclave)';
                                    } else {
                                        $kColors = [
                                            'ZENITHAR' => 'bg-warning text-dark fw-bold',
                                            'SOLTERRA' => 'bg-danger text-white fw-bold',
                                            'SYLVAMOOR' => 'bg-success text-white fw-bold',
                                        ];
                                        $kBadge = $kColors[$kCode] ?? 'bg-secondary text-light';
                                        $kName = $player->kingdom_display ?: ($kCode === 'NONE' ? 'Belum Memilih' : ucfirst(strtolower($player->kingdom)));
                                    }
                                @endphp
                                <span class="badge {{ $kBadge }} px-2 py-1" style="letter-spacing: 0.5px;">
                                    @if(!$isStaff && $kCode !== 'NONE' && $kCode !== 'AETHERION') ⚜ @endif {{ $kName }}
                                </span>
                            </td>
                            <td>
                                <span class="fw-bold text-warning">Lv. {{ $player->level }}</span>
                                <small class="text-muted d-block" style="font-size: 0.72rem;">
                                    {{ number_format($player->xp) }} XP
                                </small>
                            </td>
                            <td>
                                <div style="font-size: 0.82rem;">
                                    <span class="text-success fw-bold d-block">Rp {{ number_format($player->balance_rupiah, 0, ',', '.') }}</span>
                                    <span class="text-info fw-bold">{{ number_format($player->balance_diamond, 0, ',', '.') }} 💎</span>
                                </div>
                            </td>
                            <td>
                                @if($isOnline)
                                    <span class="badge bg-success px-2 py-1">
                                        <i class="bi bi-circle-fill me-1" style="font-size: 0.5rem;"></i> ONLINE
                                    </span>
                                @else
                                    <span class="badge bg-secondary text-muted px-2 py-1">
                                        OFFLINE
                                    </span>
                                @endif
                            </td>
                            <td>
                                <small class="text-muted">
                                    {{ $player->last_seen_at ? $player->last_seen_at->diffForHumans() : 'Belum pernah' }}
                                </small>
                            </td>
                            <td class="text-end pe-3">
                                <a href="{{ route('apexsions-bridge.admin.players.show', $player->minecraft_uuid) }}" class="btn btn-sm btn-outline-warning" title="Buka Profil Lengkap">
                                    <i class="bi bi-person-badge me-1"></i> Detail
                                </a>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="8" class="p-0">
                                <div class="apx-empty-state">
                                    <div class="apx-empty-icon">
                                        <i class="bi bi-person-x"></i>
                                    </div>
                                    <h5 class="apx-empty-title">Tidak Ditemukan Warga yang Cocok</h5>
                                    <p class="apx-empty-desc">
                                        Tidak ada data pemain yang cocok dengan filter atau kata kunci pencarian Anda. Silakan ubah filter atau reset untuk melihat semua warga.
                                    </p>
                                    <a href="{{ route('apexsions-bridge.admin.players.index') }}" class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Semua Filter
                                    </a>
                                </div>
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
    @if($players->hasPages())
        <div class="card-footer py-3">
            {{ $players->links() }}
        </div>
    @endif
</div>
@endsection
