@extends('layouts.app')

@section('title', trans('messages.profile.title'))

@php
    $linkedAccount = \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', $user->id)
        ->whereNotNull('verified_at')
        ->first();

    $rankStyles = [
        'ancestor' => ['name' => '👑 ANCESTOR', 'badge' => 'background: linear-gradient(135deg, #8B0000, #FF0000); color: #fff; box-shadow: 0 0 15px rgba(255,0,0,0.4);'],
        'architect' => ['name' => '📐 ARCHITECT', 'badge' => 'background: linear-gradient(135deg, #8E2DE2, #4A00E0); color: #fff; box-shadow: 0 0 15px rgba(142,45,226,0.4);'],
        'overseer' => ['name' => '👁 OVERSEER', 'badge' => 'background: linear-gradient(135deg, #FFD700, #f39c12); color: #1a1a1a; box-shadow: 0 0 15px rgba(243,156,18,0.4); font-weight: bold;'],
        'warden' => ['name' => '🛡 WARDEN', 'badge' => 'background: linear-gradient(135deg, #1e3c72, #2a5298); color: #fff; box-shadow: 0 0 15px rgba(42,82,152,0.4);'],
        'herald' => ['name' => '📜 HERALD', 'badge' => 'background: linear-gradient(135deg, #f857a6, #ff5858); color: #fff; box-shadow: 0 0 15px rgba(248,87,166,0.4);'],
        'sions' => ['name' => '✦ SIONS', 'badge' => 'background: linear-gradient(135deg, #00FFFF, #FFD700); color: #000; box-shadow: 0 0 15px rgba(0,255,255,0.4); font-weight: bold;'],
        'emperor' => ['name' => '⚔ EMPEROR', 'badge' => 'background: linear-gradient(135deg, #e52d27, #b31217); color: #fff; box-shadow: 0 0 12px rgba(229,45,39,0.3);'],
        'sovereign' => ['name' => '⚜ SOVEREIGN', 'badge' => 'background: linear-gradient(135deg, #f39c12, #f1c40f); color: #000; box-shadow: 0 0 12px rgba(243,156,18,0.3); font-weight: bold;'],
        'archon' => ['name' => '💎 ARCHON', 'badge' => 'background: linear-gradient(135deg, #00c6ff, #0072ff); color: #fff; box-shadow: 0 0 12px rgba(0,198,255,0.3);'],
        'ascendant' => ['name' => '☘ ASCENDANT', 'badge' => 'background: linear-gradient(135deg, #11998e, #38ef7d); color: #000; box-shadow: 0 0 12px rgba(56,239,125,0.3); font-weight: bold;'],
        'wanderer' => ['name' => 'Wanderer', 'badge' => 'background: #2c3e50; color: #dfe6e9; border: 1px solid #4b6584;'],
    ];

    $currentRankKey = strtolower($linkedAccount->rank ?? 'wanderer');
    $currentRankInfo = $rankStyles[$currentRankKey] ?? $rankStyles['wanderer'];

    $staffRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald'];
    $isStaff = in_array($currentRankKey, $staffRanks, true);

    $kingdomColors = [
        'ZENITHAR' => ['color' => '#f39c12', 'icon' => 'bi-sun', 'name' => 'Zenithar'],
        'SOLTERRA' => ['color' => '#e74c3c', 'icon' => 'bi-fire', 'name' => 'Solterra'],
        'SYLVAMOOR' => ['color' => '#2ecc71', 'icon' => 'bi-tree', 'name' => 'Sylvamoor'],
        'AETHERION' => ['color' => '#00f2fe', 'icon' => 'bi-stars', 'name' => 'Aetherion (The Conclave)'],
        'NONE' => ['color' => '#7f8c8d', 'icon' => 'bi-compass', 'name' => 'Belum Memilih'],
    ];
    $currentKingdomKey = strtoupper($linkedAccount->kingdom ?? 'NONE');
    if ($isStaff && ($currentKingdomKey === 'NONE' || empty($currentKingdomKey))) {
        $currentKingdomKey = 'AETHERION';
    }
    $currentKingdom = $kingdomColors[$currentKingdomKey] ?? $kingdomColors['NONE'];

    $xpPercent = 0;
    if ($linkedAccount && $linkedAccount->required_xp > 0) {
        $xpPercent = min(100, max(0, round(($linkedAccount->xp / $linkedAccount->required_xp) * 100)));
    }

    $bpXpPercent = 0;
    if ($linkedAccount && ($linkedAccount->battlepass_required_xp ?? 100) > 0) {
        $bpXpPercent = min(100, max(0, round((($linkedAccount->battlepass_xp ?? 0) / $linkedAccount->battlepass_required_xp) * 100)));
    }

    // Daily reward cooldown calculation
    $rewardCacheKey = $linkedAccount ? 'daily_web_reward_' . $linkedAccount->id : null;
    $claimedAt = null;
    if ($linkedAccount) {
        $claimedAt = $linkedAccount->last_daily_reward_at;
        if (!$claimedAt && $rewardCacheKey && cache()->has($rewardCacheKey)) {
            $claimedAtStr = cache()->get($rewardCacheKey);
            $claimedAt = $claimedAtStr ? \Carbon\Carbon::parse($claimedAtStr) : null;
        }
    }
    $nextClaimAt = $claimedAt ? $claimedAt->copy()->addDay() : null;
    $canClaimReward = !$nextClaimAt || $nextClaimAt->isPast();
    $secondsRemaining = (!$canClaimReward && $nextClaimAt) ? (int) max(0, \Carbon\Carbon::now()->diffInSeconds($nextClaimAt, false)) : 0;
    $hoursRemaining = (int) floor($secondsRemaining / 3600);
    $minutesRemaining = (int) floor(($secondsRemaining % 3600) / 60);
    $secsRemaining = (int) ($secondsRemaining % 60);
    $nextClaimFormatted = $nextClaimAt ? $nextClaimAt->timezone('Asia/Jakarta')->format('H:i') : '';
@endphp

@section('content')
<div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
        <div>
            <h1 class="h2 mb-1 text-gold"><i class="bi bi-person-badge"></i> <span data-i18n="profile_title">Profil Pengguna</span></h1>
            <p class="text-secondary mb-0" data-i18n="profile_sub">Kelola informasi akun website dan karakter in-game Apexsions Anda.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ url('/profile') }}" class="btn btn-outline-warning" title="Segarkan data in-game">
                <i class="bi bi-arrow-clockwise"></i> <span data-i18n="profile_btn_refresh">Segarkan</span>
            </a>
            <a href="{{ url('/leaderboard') }}" class="btn btn-outline-warning">
                <i class="bi bi-trophy"></i> <span data-i18n="profile_btn_leaderboard">Papan Peringkat</span>
            </a>
            @if($linkedAccount)
                <a href="{{ url('/player/' . ($linkedAccount->minecraft_uuid ?: $linkedAccount->minecraft_username)) }}" class="btn btn-outline-info">
                    <i class="bi bi-eye"></i> <span data-i18n="profile_btn_public">Lihat Profil Publik</span>
                </a>
            @endif
        </div>
    </div>

    <!-- ==================== APEXSIONS IN-GAME CHARACTER HERO CARD ==================== -->
    <div class="card bg-dark border-gold mb-4 overflow-hidden shadow-lg position-relative" style="background: radial-gradient(circle at top right, rgba(243, 156, 18, 0.12), rgba(18, 22, 34, 0.95) 70%) !important;">
        <div class="card-body p-4 p-md-5">
            @if($linkedAccount)
                <div class="row align-items-center g-4">
                    <!-- 3D Skin Body Column -->
                    <div class="col-lg-3 col-md-4 text-center">
                        <div class="position-relative d-inline-block">
                            <div class="position-absolute top-50 start-50 translate-middle w-100 h-100 rounded-circle" style="background: radial-gradient(circle, rgba(243,156,18,0.2) 0%, transparent 70%); filter: blur(20px); z-index: 0;"></div>
                            <img src="https://mc-heads.net/body/{{ $linkedAccount->minecraft_uuid ?: $linkedAccount->minecraft_username }}/right" 
                                 alt="{{ $linkedAccount->minecraft_username }}" 
                                 class="img-fluid position-relative" 
                                 style="max-height: 230px; filter: drop-shadow(0 10px 15px rgba(0,0,0,0.6)); z-index: 1;">
                        </div>
                        <div class="mt-3">
                            <span class="badge px-3 py-1 text-uppercase" style="{{ $currentRankInfo['badge'] }}">
                                {{ $currentRankInfo['name'] }}
                            </span>
                        </div>
                    </div>

                    <!-- Character Stats Column -->
                    <div class="col-lg-6 col-md-8">
                        <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                            <h2 class="h1 fw-bold text-white mb-0 font-cinzel">{{ $linkedAccount->minecraft_username }}</h2>
                            <span class="badge bg-secondary text-uppercase">{{ $linkedAccount->edition }}</span>
                            @if($linkedAccount->active_title)
                                <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-award"></i> {{ strip_tags($linkedAccount->active_title) }}</span>
                            @endif
                        </div>

                        <!-- Kingdom Pledged -->
                        <div class="mb-3">
                            <span class="text-secondary small" data-i18n="profile_kingdom_label">Kerajaan Faksi:</span>
                            <span class="fw-bold ms-1" style="color: {{ $currentKingdom['color'] }};">
                                <i class="bi {{ $currentKingdom['icon'] }}"></i>
                                @if($currentKingdomKey === 'AETHERION')
                                    <span data-i18n="kingdom_aetherion_name">Aetherion (The Conclave)</span>
                                @elseif($currentKingdomKey === 'NONE')
                                    <span data-i18n="profile_kingdom_none">Belum Memilih</span>
                                @else
                                    <span data-i18n="kingdom_{{ strtolower($currentKingdomKey) }}_name">{{ $currentKingdom['name'] }}</span>
                                @endif
                            </span>
                            @if($linkedAccount->level_title)
                                <span class="text-muted ms-2">• {{ strip_tags($linkedAccount->level_title) }}</span>
                            @endif
                        </div>

                        <!-- Core Civilization Level & XP Bar -->
                        <div class="mb-3 p-2 px-3 rounded bg-black bg-opacity-30 border border-secondary border-opacity-25">
                            <div class="d-flex justify-content-between align-items-center text-sm mb-1">
                                <span class="fw-bold text-gold">
                                    <i class="bi bi-shield-shaded text-warning me-1"></i>
                                    <span data-i18n="profile_core_level_label">Level Peradaban</span> {{ $linkedAccount->level }}
                                </span>
                                <span class="text-muted small">
                                    <span data-i18n="profile_core_xp_label">Exp Karakter</span>: {{ number_format($linkedAccount->xp) }} / {{ number_format($linkedAccount->required_xp) }} XP ({{ $xpPercent }}%)
                                </span>
                            </div>
                            <div class="progress" style="height: 8px; background-color: rgba(255,255,255,0.1); border-radius: 4px;">
                                <div class="progress-bar bg-warning progress-bar-striped progress-bar-animated" role="progressbar" style="width: {{ $xpPercent }}%;" aria-valuenow="{{ $xpPercent }}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                        </div>

                        <!-- Economy Stats Grid (Official Dual Currencies: Rupiah & Diamond) -->
                        <div class="row g-2 pt-2 mb-3">
                            <div class="col-sm-6">
                                <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between h-100">
                                    <span class="text-secondary small"><i class="bi bi-cash-coin text-success me-1"></i> <span data-i18n="profile_balance_rp">Saldo Rupiah:</span></span>
                                    <span class="fw-bold text-white small">Rp {{ number_format($linkedAccount->balance_rupiah, 0, ',', '.') }}</span>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between h-100">
                                    <span class="text-secondary small"><i class="bi bi-gem text-info me-1"></i> <span data-i18n="profile_balance_dia">Saldo Diamond:</span></span>
                                    <span class="fw-bold text-info small">💎 {{ number_format($linkedAccount->balance_diamond, 0, ',', '.') }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- BattlePass Season Progress Card -->
                        <div class="p-3 rounded bg-black bg-opacity-40 border border-gold border-opacity-40 mb-2">
                            <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
                                <div class="d-flex align-items-center gap-2">
                                    <span class="text-gold fw-bold small"><i class="bi bi-trophy-fill text-warning me-1"></i> <span data-i18n="profile_bp_title">BattlePass: Musim Peradaban</span></span>
                                    <span class="badge px-2 py-1 font-monospace fw-bold" style="background: rgba(245, 158, 11, 0.25); color: #fde047; border: 1px solid #f59e0b; font-size: 0.85rem;"><span data-i18n="profile_bp_tier">Tier</span> {{ $linkedAccount->battlepass_tier ?? 1 }}</span>
                                </div>
                                <div>
                                    @php
                                        $rawPassName = $linkedAccount->battlepass_pass_name;
                                        if (!$rawPassName) {
                                            $rawPassName = $linkedAccount->battlepass_has_premium ? 'PREMIUM PASS' : 'CITIZEN PASS';
                                        }
                                        $upperPass = strtoupper(trim($rawPassName));
                                    @endphp
                                    @if(str_contains($upperPass, 'EXSIO'))
                                        <span class="badge px-2 py-1 font-monospace fw-bold" style="background: linear-gradient(135deg, #d946ef, #8b5cf6, #06b6d4); color: #fff; text-shadow: 0 0 6px rgba(0,0,0,0.6); box-shadow: 0 0 12px rgba(217, 70, 239, 0.5); border: 1px solid rgba(255, 255, 255, 0.4);">
                                            <i class="bi bi-stars me-1 text-warning"></i> <span data-i18n="profile_bp_exsio">{{ $upperPass }}</span>
                                        </span>
                                    @elseif(str_contains($upperPass, 'SIO') || str_contains($upperPass, 'PREMIUM'))
                                        <span class="badge px-2 py-1 font-monospace fw-bold" style="background: linear-gradient(135deg, #FFD700, #f39c12); color: #000; box-shadow: 0 0 10px rgba(243,156,18,0.5); border: 1px solid #ffeaa7;">
                                            <i class="bi bi-star-fill me-1 text-dark"></i> <span data-i18n="profile_bp_sio">{{ $upperPass }}</span>
                                        </span>
                                    @else
                                        <span class="badge px-2 py-1 font-monospace" style="background: rgba(255,255,255,0.08); color: #cbd5e1; border: 1px solid rgba(255,255,255,0.2);">
                                            <i class="bi bi-shield me-1 text-secondary"></i> <span data-i18n="profile_bp_citizen">{{ $upperPass }}</span>
                                        </span>
                                    @endif
                                </div>
                            </div>
                            <div class="d-flex justify-content-between text-sm mb-1">
                                <span class="text-secondary small"><span data-i18n="profile_bp_xp">Progress Tier Pass</span>:</span>
                                <span class="text-white small fw-bold">{{ number_format($linkedAccount->battlepass_xp ?? 0) }} / {{ number_format($linkedAccount->battlepass_required_xp ?? 100) }} XP ({{ $bpXpPercent }}%)</span>
                            </div>
                            <div class="progress" style="height: 8px; background-color: rgba(255,255,255,0.08); border-radius: 4px;">
                                <div class="progress-bar bg-info progress-bar-striped progress-bar-animated" role="progressbar" style="width: {{ $bpXpPercent }}%;" aria-valuenow="{{ $bpXpPercent }}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                            <div class="mt-2 pt-1 text-end">
                                <small class="text-muted" style="font-size: 0.72rem;">
                                    <i class="bi bi-info-circle me-1"></i><span data-i18n="profile_bp_sync_tip">Ketik /sync atau /bp in-game untuk update seketika ke web.</span>
                                </small>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Action Buttons Column -->
                    <div class="col-lg-3 text-center text-lg-end">
                        <div class="d-grid gap-2">
                            @if($canClaimReward)
                                <div class="card bg-black bg-opacity-50 border border-warning border-opacity-40 p-3 rounded text-center mb-1 shadow-sm">
                                    <div class="d-flex align-items-center justify-content-between mb-2">
                                        <span class="badge bg-warning text-dark fw-bold">
                                            <i class="bi bi-stars"></i> <span data-i18n="profile_reward_daily">Bonus Harian</span>
                                        </span>
                                        <span class="badge bg-success bg-opacity-25 text-success border border-success border-opacity-25">
                                            <i class="bi bi-check-circle"></i> <span data-i18n="profile_reward_ready">Siap Diklaim</span>
                                        </span>
                                    </div>
                                    <div class="small text-secondary mb-2" style="font-size: 0.78rem;" data-i18n-html="profile_reward_ready_sub">
                                        Dapatkan <strong>+Rp 5.000</strong> & <strong>+25 EXP</strong> in-game setiap 24 jam!
                                    </div>
                                    <form action="{{ url('/profile/minecraft/claim-reward') }}" method="POST">
                                        @csrf
                                        <button type="submit" class="btn btn-warning w-100 fw-bold shadow-sm py-2">
                                            <i class="bi bi-gift-fill me-1"></i> <span data-i18n="profile_reward_btn_claim">Klaim Hadiah Harian</span>
                                        </button>
                                    </form>
                                </div>
                            @else
                                <div class="card bg-black bg-opacity-50 border border-secondary border-opacity-25 p-3 rounded text-center mb-1 shadow-sm">
                                    <div class="d-flex align-items-center justify-content-between mb-2">
                                        <span class="badge bg-secondary bg-opacity-50 text-light">
                                            <i class="bi bi-gift"></i> <span data-i18n="profile_reward_daily">Hadiah Harian</span>
                                        </span>
                                        <span class="badge bg-success bg-opacity-25 text-success border border-success border-opacity-25">
                                            <i class="bi bi-check2-circle"></i> <span data-i18n="profile_reward_claimed">Sudah Diklaim</span>
                                        </span>
                                    </div>
                                    <div class="small text-muted mb-1" style="font-size: 0.78rem;" data-i18n="profile_reward_next_available">
                                        Klaim berikutnya tersedia dalam:
                                    </div>
                                    <div class="font-monospace fw-bold text-warning fs-5 my-1" id="dailyRewardCountdown" data-seconds="{{ $secondsRemaining }}">
                                        {{ sprintf('%02d:%02d:%02d', $hoursRemaining, $minutesRemaining, $secsRemaining) }}
                                    </div>
                                    <div class="small text-secondary mb-2" style="font-size: 0.75rem;">
                                        <i class="bi bi-clock-history me-1"></i> <span data-i18n="profile_reward_tomorrow_prefix">Tersedia besok pukul</span> <strong>{{ $nextClaimFormatted }} WIB</strong>
                                    </div>
                                    <button type="button" class="btn btn-secondary w-100 btn-sm disabled" disabled style="opacity: 0.65; cursor: not-allowed;">
                                        <i class="bi bi-hourglass-split me-1"></i> <span data-i18n="profile_reward_btn_cooldown">Sedang Cooldown</span>
                                    </button>
                                </div>
                            @endif

                            <button type="button" class="btn btn-outline-light w-100 mt-1" data-bs-toggle="modal" data-bs-target="#resetPasswordModal">
                                <i class="bi bi-key-fill me-1"></i> <span data-i18n="profile_btn_reset_pass">Reset Password In-Game</span>
                            </button>

                            <form action="{{ url('/profile/minecraft/unlink') }}" method="POST" onsubmit="return confirm(document.documentElement.lang === 'en' ? 'Are you sure you want to unlink this Minecraft account?' : 'Apakah Anda yakin ingin memutuskan tautan akun Minecraft ini?');">
                                @csrf
                                <button type="submit" class="btn btn-outline-danger btn-sm w-100">
                                    <i class="bi bi-link-45deg"></i> <span data-i18n="profile_btn_unlink">Putuskan Tautan (Unlink)</span>
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            @else
                <!-- Unlinked State Banner -->
                <div class="text-center py-4">
                    <div class="mb-3">
                        <i class="bi bi-controller text-warning" style="font-size: 3.5rem;"></i>
                    </div>
                    <h3 class="h3 fw-bold text-white font-cinzel" data-i18n="profile_unlink_title">Hubungkan Karakter Minecraft Anda</h3>
                    <p class="text-secondary max-w-600 mx-auto mb-4" data-i18n="profile_unlink_desc">
                        Kaitkan akun website Anda dengan server Minecraft Apexsions untuk menampilkan skin 3D, sinkronisasi rank & level otomatis, mengklaim hadiah harian, dan mengelola profil in-game secara langsung.
                    </p>
                    <a href="{{ url('/link') }}" class="btn btn-warning btn-lg px-4 fw-bold shadow">
                        <i class="bi bi-link-45deg me-2"></i> <span data-i18n="profile_unlink_btn">Tautkan Akun Minecraft Sekarang</span>
                    </a>
                </div>
            @endif
        </div>
    </div>

    <!-- ==================== AZURIOM WEBSITE ACCOUNT SETTINGS ==================== -->
    <div class="row g-4">
        <!-- Profile Details Card -->
        <div class="col-lg-4">
            <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow-sm">
                <div class="card-body text-center p-4">
                    <img src="{{ $user->getAvatar(130) }}" class="rounded-circle mb-3 img-fluid border border-warning border-2 p-1" alt="{{ $user->name }}">
                    <h3 class="h4 fw-bold text-white mb-1">{{ $user->name }}</h3>
                    <div class="mb-3">
                        <span class="badge" style="{{ $user->role->getBadgeStyle() }}; font-size: 0.9rem;">
                            @if($user->role->icon) <i class="{{ $user->role->icon }}"></i> @endif
                            {{ $user->role->name }}
                        </span>
                    </div>

                    <ul class="list-unstyled text-start small border-top border-secondary border-opacity-25 pt-3 mb-4">
                        <li class="mb-2 d-flex justify-content-between">
                            <span class="text-muted"><i class="bi bi-calendar3 me-1"></i> <span data-i18n="profile_lbl_registered">Terdaftar:</span></span>
                            <span class="text-white">{{ format_date($user->created_at, true) }}</span>
                        </li>
                        <li class="mb-2 d-flex justify-content-between">
                            <span class="text-muted"><i class="bi bi-wallet2 me-1"></i> <span data-i18n="profile_lbl_points">Web Points:</span></span>
                            <span class="text-gold fw-bold">{{ format_money($user->money) }}</span>
                        </li>
                        <li class="mb-2 d-flex justify-content-between">
                            <span class="text-muted"><i class="bi bi-shield-check me-1"></i> 2FA:</span>
                            <span class="{{ $user->hasTwoFactorAuth() ? 'text-success' : 'text-danger' }}">
                                {{ trans_bool($user->hasTwoFactorAuth()) }}
                            </span>
                        </li>
                    </ul>

                    @if(! oauth_login())
                        <div class="d-grid gap-2">
                            <a class="btn btn-outline-warning btn-sm" href="{{ route('profile.2fa.index') }}">
                                <i class="bi bi-shield-lock me-1"></i> <span data-i18n="{{ $user->hasTwoFactorAuth() ? 'profile_btn_manage_2fa' : 'profile_btn_enable_2fa' }}">{{ $user->hasTwoFactorAuth() ? 'Kelola 2FA' : 'Aktifkan 2FA' }}</span>
                            </a>
                        </div>
                    @endif
                </div>
            </div>
        </div>

        <!-- Change Password & Email Forms -->
        <div class="col-lg-8">
            <div class="row g-4">
                @if(! oauth_login())
                    <!-- Change Email Address -->
                    <div class="col-md-6">
                        <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow-sm">
                            <div class="card-body p-4">
                                <h3 class="h5 fw-bold text-white mb-3">
                                    <i class="bi bi-envelope-at me-2 text-warning"></i> <span data-i18n="profile_title_change_email">Ganti Alamat Email</span>
                                </h3>
                                <form action="{{ route('profile.email') }}" method="POST">
                                    @csrf
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="emailInput" data-i18n="profile_lbl_new_email">Email Baru</label>
                                        <input type="email" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('email') is-invalid @enderror" id="emailInput" name="email" value="{{ old('email', $user->email) }}" required>
                                        @error('email')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="emailConfirmPassInput" data-i18n="profile_lbl_curr_pass">Password Saat Ini</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('email_confirm_pass') is-invalid @enderror" id="emailConfirmPassInput" name="email_confirm_pass" required>
                                        @error('email_confirm_pass')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                                        <i class="bi bi-check-lg"></i> <span data-i18n="profile_btn_update_email">Perbarui Email</span>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- Change Website Password -->
                    <div class="col-md-6">
                        <div class="card bg-dark border-secondary border-opacity-25 h-100 shadow-sm">
                            <div class="card-body p-4">
                                <h3 class="h5 fw-bold text-white mb-3">
                                    <i class="bi bi-lock me-2 text-warning"></i> <span data-i18n="profile_title_change_pass">Ganti Password Web</span>
                                </h3>
                                <form action="{{ route('profile.password') }}" method="POST">
                                    @csrf
                                    <div class="mb-2">
                                        <label class="form-label text-muted small" for="passwordConfirmPassInput" data-i18n="profile_lbl_curr_pass">Password Saat Ini</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('password_confirm_pass') is-invalid @enderror" id="passwordConfirmPassInput" name="password_confirm_pass" required>
                                        @error('password_confirm_pass')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-2">
                                        <label class="form-label text-muted small" for="passwordInput" data-i18n="profile_lbl_new_pass">Password Baru</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('password') is-invalid @enderror" id="passwordInput" name="password" required>
                                        @error('password')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="confirmPasswordInput" data-i18n="profile_lbl_confirm_pass">Konfirmasi Password Baru</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary" id="confirmPasswordInput" name="password_confirmation" required>
                                    </div>
                                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                                        <i class="bi bi-check-lg"></i> <span data-i18n="profile_btn_update_pass">Perbarui Password Web</span>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                @endif
            </div>
        </div>
    </div>
</div>

<!-- ==================== MODAL: RESET IN-GAME AUTHMERELOADED PASSWORD ==================== -->
@if($linkedAccount)
<div class="modal fade" id="resetPasswordModal" tabindex="-1" aria-labelledby="resetPasswordModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-gold text-white">
            <div class="modal-header border-secondary">
                <h5 class="modal-title font-cinzel text-gold" id="resetPasswordModalLabel">
                    <i class="bi bi-shield-lock-fill me-2"></i> <span data-i18n="profile_modal_reset_title">Reset Password In-Game (AuthMe)</span>
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="{{ url('/profile/minecraft/reset-password') }}" method="POST">
                @csrf
                <div class="modal-body">
                    <div class="alert alert-info py-2 small" role="alert">
                        <i class="bi bi-info-circle me-1"></i> <span data-i18n="profile_modal_reset_alert">Perintah reset akan dikirimkan ke server Minecraft untuk akun</span> <strong>{{ $linkedAccount->minecraft_username }}</strong>.
                    </div>
                    <div class="mb-3">
                        <label for="newIngamePassword" class="form-label small text-muted" data-i18n="profile_modal_lbl_new_pass">Password In-Game Baru (Minimal 6 karakter)</label>
                        <input type="password" class="form-control bg-black text-white border-secondary" id="newIngamePassword" name="new_password" minlength="6" required placeholder="Ketik password baru..." data-i18n-placeholder="profile_modal_ph_new_pass">
                    </div>
                    <div class="mb-3">
                        <label for="confirmIngamePassword" class="form-label small text-muted" data-i18n="profile_modal_lbl_confirm_pass">Konfirmasi Password Baru</label>
                        <input type="password" class="form-control bg-black text-white border-secondary" id="confirmIngamePassword" name="new_password_confirmation" minlength="6" required placeholder="Ulangi password baru..." data-i18n-placeholder="profile_modal_ph_confirm_pass">
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal" data-i18n="profile_modal_btn_cancel">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">
                        <i class="bi bi-check2-circle me-1"></i> <span data-i18n="profile_modal_btn_submit">Konfirmasi & Ganti Password</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endif
@endsection

@push('scripts')
<script>
document.addEventListener('DOMContentLoaded', function() {
    const timerElem = document.getElementById('dailyRewardCountdown');
    if (!timerElem) return;

    let remainingSeconds = parseInt(timerElem.getAttribute('data-seconds'), 10) || 0;

    function formatTime(totalSec) {
        if (totalSec <= 0) return '00:00:00';
        const hours = Math.floor(totalSec / 3600);
        const minutes = Math.floor((totalSec % 3600) / 60);
        const seconds = totalSec % 60;
        return [
            String(hours).padStart(2, '0'),
            String(minutes).padStart(2, '0'),
            String(seconds).padStart(2, '0')
        ].join(':');
    }

    if (remainingSeconds > 0) {
        const interval = setInterval(function() {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                clearInterval(interval);
                timerElem.textContent = '00:00:00';
                setTimeout(function() {
                    window.location.reload();
                }, 1000);
            } else {
                timerElem.textContent = formatTime(remainingSeconds);
            }
        }, 1000);
    }
});
</script>
@endpush

