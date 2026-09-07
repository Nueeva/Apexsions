@extends('layouts.app')

@section('title', 'Papan Peringkat Realm — Leaderboard')

@php
    $rankStyles = [
        'ancestor' => ['name' => '👑 ANCESTOR', 'badge' => 'background: linear-gradient(135deg, #8B0000, #FF0000); color: #fff;'],
        'architect' => ['name' => '📐 ARCHITECT', 'badge' => 'background: linear-gradient(135deg, #8E2DE2, #4A00E0); color: #fff;'],
        'overseer' => ['name' => '👁 OVERSEER', 'badge' => 'background: linear-gradient(135deg, #FFD700, #f39c12); color: #1a1a1a; font-weight: bold;'],
        'warden' => ['name' => '🛡 WARDEN', 'badge' => 'background: linear-gradient(135deg, #1e3c72, #2a5298); color: #fff;'],
        'herald' => ['name' => '📜 HERALD', 'badge' => 'background: linear-gradient(135deg, #f857a6, #ff5858); color: #fff;'],
        'sions' => ['name' => '✦ SIONS', 'badge' => 'background: linear-gradient(135deg, #00FFFF, #FFD700); color: #000; font-weight: bold;'],
        'emperor' => ['name' => '⚔ EMPEROR', 'badge' => 'background: linear-gradient(135deg, #e52d27, #b31217); color: #fff;'],
        'sovereign' => ['name' => '⚜ SOVEREIGN', 'badge' => 'background: linear-gradient(135deg, #f39c12, #f1c40f); color: #000; font-weight: bold;'],
        'archon' => ['name' => '💎 ARCHON', 'badge' => 'background: linear-gradient(135deg, #00c6ff, #0072ff); color: #fff;'],
        'ascendant' => ['name' => '☘ ASCENDANT', 'badge' => 'background: linear-gradient(135deg, #11998e, #38ef7d); color: #000; font-weight: bold;'],
        'wanderer' => ['name' => 'Wanderer', 'badge' => 'background: #2c3e50; color: #dfe6e9;'],
    ];

    $kingdomColors = [
        'ZENITHAR' => ['color' => '#f39c12', 'icon' => 'bi-sun', 'name' => 'Zenithar'],
        'SOLTERRA' => ['color' => '#e74c3c', 'icon' => 'bi-fire', 'name' => 'Solterra'],
        'SYLVAMOOR' => ['color' => '#2ecc71', 'icon' => 'bi-tree', 'name' => 'Sylvamoor'],
        'NONE' => ['color' => '#7f8c8d', 'icon' => 'bi-compass', 'name' => 'Tanpa Kerajaan'],
    ];
@endphp

@section('content')
<div class="container py-5">
    <!-- Header Title -->
    <div class="text-center mb-5">
        <div class="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-warning bg-opacity-10 border border-warning border-opacity-25 text-warning small mb-3">
            <i class="bi bi-trophy-fill"></i> <span data-i18n="leaderboard_kicker">DEWAN KEHORMATAN APEXSIONS</span>
        </div>
        <h1 class="display-5 fw-bold font-cinzel text-gold mb-2" data-i18n="leaderboard_title">Papan Peringkat Peradaban</h1>
        <p class="text-secondary max-w-600 mx-auto" data-i18n="leaderboard_desc">
            Catatan kejayaan pengembara terhebat, kekayaan konglomerat kerajaan, dan dominasi faksi Tiga Kerajaan di seluruh realm Apexsions.
        </p>
    </div>

    <!-- ==================== THREE KINGDOMS DOMINANCE ==================== -->
    <div class="row g-4 mb-5">
        @foreach($kingdoms as $kKey => $kData)
            <div class="col-md-4">
                <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow overflow-hidden position-relative" style="background: radial-gradient(circle at top, {{ $kData['color'] }}15, rgba(18,22,34,0.98) 75%) !important;">
                    <div class="card-body p-4 text-center">
                        <div class="mb-3">
                            <span class="d-inline-flex p-3 rounded-circle shadow" style="background: {{ $kData['color'] }}20; color: {{ $kData['color'] }}; font-size: 2rem;">
                                <i class="bi {{ $kData['name'] === 'Zenithar' ? 'bi-sun-fill' : ($kData['name'] === 'Solterra' ? 'bi-fire' : 'bi-tree-fill') }}"></i>
                            </span>
                        </div>
                        <h3 class="h4 fw-bold text-white font-cinzel mb-1" data-i18n="kingdom_{{ strtolower($kKey) }}_name">{{ $kData['name'] }}</h3>
                        <p class="text-muted small mb-3">{{ $kData['tagline'] }}</p>

                        <div class="row g-2 border-top border-secondary border-opacity-25 pt-3">
                            <div class="col-6">
                                <span class="text-muted small d-block" data-i18n="leaderboard_population">Populasi:</span>
                                <span class="h5 fw-bold text-white mb-0">{{ number_format($kData['count']) }}</span> <small class="text-muted" data-i18n="leaderboard_citizens">Warga</small>
                            </div>
                            <div class="col-6">
                                <span class="text-muted small d-block" data-i18n="leaderboard_power_level">Kekuatan Level:</span>
                                <span class="h5 fw-bold" style="color: {{ $kData['color'] }};">{{ number_format($kData['total_levels']) }}</span> <small class="text-muted" data-i18n="leaderboard_lv">Lv</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        @endforeach
    </div>

    <!-- ==================== TOP LEVEL & TOP ECONOMY TABLES ==================== -->
    <div class="row g-4">
        <!-- Top Level Progression -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow">
                <div class="card-header bg-black bg-opacity-30 border-secondary border-opacity-25 p-3 d-flex align-items-center justify-content-between">
                    <h2 class="h5 fw-bold text-gold font-cinzel mb-0">
                        <i class="bi bi-star-fill text-warning me-2"></i> <span data-i18n="leaderboard_top_level_title">Top 10 Level &amp; Pengalaman (EXP)</span>
                    </h2>
                    <span class="badge bg-warning bg-opacity-20 text-warning" data-i18n="leaderboard_badge_progression">Progresi</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead class="text-secondary small text-uppercase bg-black bg-opacity-40">
                                <tr>
                                    <th class="ps-3 py-3" style="width: 60px;">#</th>
                                    <th data-i18n="leaderboard_th_player">Pemain</th>
                                    <th data-i18n="leaderboard_th_rank">Rank</th>
                                    <th class="text-end pe-3" data-i18n="leaderboard_th_level_xp">Level &amp; XP</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($topLevels as $idx => $p)
                                    @php
                                        $rKey = strtolower($p->rank ?? 'wanderer');
                                        $rInfo = $rankStyles[$rKey] ?? $rankStyles['wanderer'];
                                    @endphp
                                    <tr>
                                        <td class="ps-3 fw-bold">
                                            @if($idx === 0) <span class="text-warning"><i class="bi bi-award-fill"></i> 1</span>
                                            @elseif($idx === 1) <span class="text-secondary"><i class="bi bi-award-fill"></i> 2</span>
                                            @elseif($idx === 2) <span class="text-danger"><i class="bi bi-award-fill"></i> 3</span>
                                            @else <span class="text-muted">{{ $idx + 1 }}</span>
                                            @endif
                                        </td>
                                        <td>
                                            <a href="{{ url('/player/' . ($p->minecraft_uuid ?: $p->id)) }}" class="text-decoration-none text-white d-flex align-items-center gap-2">
                                                <img src="https://mc-heads.net/avatar/{{ $p->minecraft_uuid ?: $p->minecraft_username }}/32" alt="{{ $p->minecraft_username }}" class="rounded" width="32" height="32">
                                                <div>
                                                    <span class="fw-bold">{{ $p->minecraft_username }}</span>
                                                    @if($p->active_title)
                                                        <span class="d-block small text-warning opacity-75">{{ strip_tags($p->active_title) }}</span>
                                                    @endif
                                                </div>
                                            </a>
                                        </td>
                                        <td>
                                            <span class="badge" style="{{ $rInfo['badge'] }}">
                                                {{ $rInfo['name'] }}
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <span class="fw-bold text-gold">Lv. {{ $p->level }}</span>
                                            <span class="d-block small text-muted">{{ number_format($p->xp) }} XP</span>
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-muted" data-i18n="leaderboard_empty_players">Belum ada data pemain terverifikasi.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Top Economy Balance -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow">
                <div class="card-header bg-black bg-opacity-30 border-secondary border-opacity-25 p-3 d-flex align-items-center justify-content-between">
                    <h2 class="h5 fw-bold text-gold font-cinzel mb-0">
                        <i class="bi bi-cash-coin text-success me-2"></i> <span data-i18n="leaderboard_top_balance_title">Top 10 Konglomerat Realm (Saldo)</span>
                    </h2>
                    <span class="badge bg-success bg-opacity-20 text-success" data-i18n="leaderboard_badge_economy">Ekonomi</span>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead class="text-secondary small text-uppercase bg-black bg-opacity-40">
                                <tr>
                                    <th class="ps-3 py-3" style="width: 60px;">#</th>
                                    <th data-i18n="leaderboard_th_player">Pemain</th>
                                    <th data-i18n="leaderboard_th_kingdom">Kerajaan</th>
                                    <th class="text-end pe-3" data-i18n="leaderboard_th_wealth">Kekayaan</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($topBalances as $idx => $p)
                                    @php
                                        $kKey = strtoupper($p->kingdom ?? 'NONE');
                                        $kInfo = $kingdomColors[$kKey] ?? $kingdomColors['NONE'];
                                    @endphp
                                    <tr>
                                        <td class="ps-3 fw-bold">
                                            @if($idx === 0) <span class="text-warning"><i class="bi bi-cash-coin"></i> 1</span>
                                            @elseif($idx === 1) <span class="text-secondary"><i class="bi bi-cash-coin"></i> 2</span>
                                            @elseif($idx === 2) <span class="text-danger"><i class="bi bi-cash-coin"></i> 3</span>
                                            @else <span class="text-muted">{{ $idx + 1 }}</span>
                                            @endif
                                        </td>
                                        <td>
                                            <a href="{{ url('/player/' . ($p->minecraft_uuid ?: $p->id)) }}" class="text-decoration-none text-white d-flex align-items-center gap-2">
                                                <img src="https://mc-heads.net/avatar/{{ $p->minecraft_uuid ?: $p->minecraft_username }}/32" alt="{{ $p->minecraft_username }}" class="rounded" width="32" height="32">
                                                <span class="fw-bold">{{ $p->minecraft_username }}</span>
                                            </a>
                                        </td>
                                        <td>
                                            <span class="badge" style="background: {{ $kInfo['color'] }}20; color: {{ $kInfo['color'] }}; border: 1px solid {{ $kInfo['color'] }}40;">
                                                <i class="bi {{ $kInfo['icon'] }}"></i> <span data-i18n="kingdom_{{ strtolower($kKey) }}_name">{{ $kInfo['name'] }}</span>
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <span class="fw-bold text-success">Rp {{ number_format($p->balance_rupiah, 0, ',', '.') }}</span>
                                            <span class="d-block small text-info">💎 {{ number_format($p->balance_diamond, 0, ',', '.') }}</span>
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-muted" data-i18n="leaderboard_empty_economy">Belum ada data kekayaan pemain.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
