@extends('layouts.app')

@section('title', 'Karakter: ' . $account->minecraft_username)

@php
    $rankStyles = [
        'ancestor' => ['name' => '👑 ANCESTOR', 'badge' => 'background: linear-gradient(135deg, #8B0000, #FF0000); color: #fff; box-shadow: 0 0 15px rgba(255,0,0,0.4);'],
        'architect' => ['name' => '📐 ARCHITECT', 'badge' => 'background: linear-gradient(135deg, #8E2DE2, #4A00E0); color: #fff; box-shadow: 0 0 15px rgba(142,45,226,0.4);'],
        'overseer' => ['name' => '👁 OVERSEER', 'badge' => 'background: linear-gradient(135deg, #FFD700, #f39c12); color: #1a1a1a; box-shadow: 0 0 15px rgba(243,156,18,0.4); font-weight: bold;'],
        'warden' => ['name' => '🛡 WARDEN', 'badge' => 'background: linear-gradient(135deg, #1e3c72, #2a5298); color: #fff; box-shadow: 0 0 15px rgba(42,82,152,0.4);'],
        'herald' => ['name' => '📜 HERALD', 'badge' => 'background: linear-gradient(135deg, #f857a6, #ff5858); color: #fff; box-shadow: 0 0 15px rgba(248,87,166,0.4);'],
        'sions' => ['name' => '✦ SIONS', 'badge' => 'background: linear-gradient(135deg, #00FFFF, #FFD700); color: #000; box-shadow: 0 0 15px rgba(0,255,255,0.4); font-weight: bold;'],
        'emperor' => ['name' => '⚔ EMPEROR', 'badge' => 'background: linear-gradient(135deg, #e52d27, #b31217); color: #fff;'],
        'sovereign' => ['name' => '⚜ SOVEREIGN', 'badge' => 'background: linear-gradient(135deg, #f39c12, #f1c40f); color: #000; font-weight: bold;'],
        'archon' => ['name' => '💎 ARCHON', 'badge' => 'background: linear-gradient(135deg, #00c6ff, #0072ff); color: #fff;'],
        'ascendant' => ['name' => '☘ ASCENDANT', 'badge' => 'background: linear-gradient(135deg, #11998e, #38ef7d); color: #000; font-weight: bold;'],
        'wanderer' => ['name' => 'Wanderer', 'badge' => 'background: #2c3e50; color: #dfe6e9;'],
    ];

    $currentRankKey = strtolower($account->rank ?? 'wanderer');
    $currentRankInfo = $rankStyles[$currentRankKey] ?? $rankStyles['wanderer'];

    $kingdomColors = [
        'ZENITHAR' => ['color' => '#f39c12', 'icon' => 'bi-sun', 'name' => 'Zenithar'],
        'SOLTERRA' => ['color' => '#e74c3c', 'icon' => 'bi-fire', 'name' => 'Solterra'],
        'SYLVAMOOR' => ['color' => '#2ecc71', 'icon' => 'bi-tree', 'name' => 'Sylvamoor'],
        'NONE' => ['color' => '#7f8c8d', 'icon' => 'bi-compass', 'name' => 'Belum Memilih'],
    ];
    $currentKingdomKey = strtoupper($account->kingdom ?? 'NONE');
    $currentKingdom = $kingdomColors[$currentKingdomKey] ?? $kingdomColors['NONE'];

    $xpPercent = 0;
    if ($account->required_xp > 0) {
        $xpPercent = min(100, max(0, round(($account->xp / $account->required_xp) * 100)));
    }
@endphp

@section('content')
<div class="container py-5">
    <div class="mb-4">
        <a href="{{ url('/leaderboard') }}" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> <span data-i18n="profile_pub_back">Kembali ke Papan Peringkat</span>
        </a>
    </div>

    <!-- Character Card Hero -->
    <div class="card bg-dark border-gold shadow-lg overflow-hidden" style="background: radial-gradient(circle at top right, rgba(243, 156, 18, 0.15), rgba(18, 22, 34, 0.98) 75%) !important;">
        <div class="card-body p-4 p-md-5">
            <div class="row align-items-center g-4">
                <!-- 3D Body Column -->
                <div class="col-md-4 text-center">
                    <div class="position-relative d-inline-block">
                        <div class="position-absolute top-50 start-50 translate-middle w-100 h-100 rounded-circle" style="background: radial-gradient(circle, rgba(243,156,18,0.25) 0%, transparent 70%); filter: blur(25px); z-index: 0;"></div>
                        <img src="https://mc-heads.net/body/{{ $account->minecraft_uuid ?: $account->minecraft_username }}/right" 
                             alt="{{ $account->minecraft_username }}" 
                             class="img-fluid position-relative" 
                             style="max-height: 280px; filter: drop-shadow(0 12px 20px rgba(0,0,0,0.7)); z-index: 1;">
                    </div>
                </div>

                <!-- Info Column -->
                <div class="col-md-8">
                    <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                        <h1 class="display-6 fw-bold text-white font-cinzel mb-0">{{ $account->minecraft_username }}</h1>
                        <span class="badge bg-secondary">{{ $account->edition }}</span>
                    </div>

                    <div class="mb-3 d-flex flex-wrap gap-2 align-items-center">
                        <span class="badge px-3 py-2 text-uppercase" style="{{ $currentRankInfo['badge'] }}">
                            {{ $currentRankInfo['name'] }}
                        </span>
                        <span class="badge px-3 py-2" style="background: {{ $currentKingdom['color'] }}20; color: {{ $currentKingdom['color'] }}; border: 1px solid {{ $currentKingdom['color'] }}40;">
                            <i class="bi {{ $currentKingdom['icon'] }} me-1"></i> <span data-i18n="profile_pub_kingdom_prefix">Kerajaan</span> <span data-i18n="kingdom_{{ strtolower($currentKingdomKey) }}_name">{{ $currentKingdom['name'] }}</span>
                        </span>
                        @if($account->active_title)
                            <span class="badge bg-warning text-dark fw-bold px-3 py-2">
                                <i class="bi bi-award-fill me-1"></i> {{ strip_tags($account->active_title) }}
                            </span>
                        @endif
                    </div>

                    <!-- Core Civilization Level & XP Bar -->
                    <div class="mb-3 p-2 px-3 rounded bg-black bg-opacity-30 border border-secondary border-opacity-25">
                        <div class="d-flex justify-content-between align-items-center text-sm mb-1">
                            <span class="fw-bold text-gold">
                                <i class="bi bi-shield-shaded text-warning me-1"></i>
                                <span data-i18n="profile_core_level_label">Level Peradaban</span> {{ $account->level }}
                            </span>
                            <span class="text-muted small">
                                <span data-i18n="profile_core_xp_label">Exp Karakter</span>: {{ number_format($account->xp) }} / {{ number_format($account->required_xp) }} XP ({{ $xpPercent }}%)
                            </span>
                        </div>
                        <div class="progress" style="height: 8px; background-color: rgba(255,255,255,0.1); border-radius: 4px;">
                            <div class="progress-bar bg-warning progress-bar-striped progress-bar-animated" role="progressbar" style="width: {{ $xpPercent }}%;" aria-valuenow="{{ $xpPercent }}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                    </div>

                    <!-- Economy Stats Grid (Rupiah & Diamond) -->
                    <div class="row g-2 mb-3">
                        <div class="col-sm-6">
                            <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between h-100">
                                <span class="text-secondary small"><i class="bi bi-cash-coin text-success me-1"></i> <span data-i18n="profile_balance_rp">Saldo Rupiah:</span></span>
                                <span class="fw-bold text-white small">Rp {{ number_format($account->balance_rupiah, 0, ',', '.') }}</span>
                            </div>
                        </div>
                        <div class="col-sm-6">
                            <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between h-100">
                                <span class="text-secondary small"><i class="bi bi-gem text-info me-1"></i> <span data-i18n="profile_balance_dia">Diamond:</span></span>
                                <span class="fw-bold text-white small">💎 {{ number_format($account->balance_diamond, 0, ',', '.') }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Detailed Meta Info Grid -->
                    <div class="row g-3 border-top border-secondary border-opacity-25 pt-3 text-secondary small">
                        <div class="col-sm-6">
                            <span class="d-block text-muted" data-i18n="profile_pub_tier_title">Gelar Tingkat:</span>
                            <span class="text-white fw-bold">
                                @if($account->level_title)
                                    {{ strip_tags($account->level_title) }}
                                @else
                                    <span data-i18n="profile_pub_default_title">Pengelana Awal</span>
                                @endif
                            </span>
                        </div>
                        <div class="col-sm-6">
                            <span class="d-block text-muted" data-i18n="profile_pub_last_seen">Terakhir Aktif:</span>
                            <span class="text-white fw-bold">{{ $account->last_seen_at ? $account->last_seen_at->diffForHumans() : 'Baru Saja' }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="d-block text-muted" data-i18n="profile_pub_uuid">Minecraft UUID:</span>
                            <span class="text-white font-monospace small">{{ $account->minecraft_uuid ?: '-' }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="d-block text-muted" data-i18n="profile_pub_status">Status Akun:</span>
                            @if($account->verified_at)
                                <span class="text-success fw-bold"><i class="bi bi-patch-check-fill me-1"></i> <span data-i18n="profile_pub_verified">Terverifikasi Resmi</span></span>
                            @else
                                <span class="text-warning fw-bold"><i class="bi bi-shield me-1"></i> <span data-i18n="profile_pub_unlinked">Karakter In-Game</span></span>
                                <small class="d-block text-muted" style="font-size: 0.72rem;" data-i18n="profile_pub_claim_hint">Ketik /link di Minecraft untuk klaim web</small>
                            @endif
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
