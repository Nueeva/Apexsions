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

    $kingdomColors = [
        'ZENITHAR' => ['color' => '#f39c12', 'icon' => 'bi-sun', 'name' => 'Zenithar'],
        'SOLTERRA' => ['color' => '#e74c3c', 'icon' => 'bi-fire', 'name' => 'Solterra'],
        'SYLVAMOOR' => ['color' => '#2ecc71', 'icon' => 'bi-tree', 'name' => 'Sylvamoor'],
        'NONE' => ['color' => '#7f8c8d', 'icon' => 'bi-compass', 'name' => 'Belum Memilih'],
    ];
    $currentKingdomKey = strtoupper($linkedAccount->kingdom ?? 'NONE');
    $currentKingdom = $kingdomColors[$currentKingdomKey] ?? $kingdomColors['NONE'];

    $xpPercent = 0;
    if ($linkedAccount && $linkedAccount->required_xp > 0) {
        $xpPercent = min(100, max(0, round(($linkedAccount->xp / $linkedAccount->required_xp) * 100)));
    }
@endphp

@section('content')
<div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
        <div>
            <h1 class="h2 mb-1 text-gold"><i class="bi bi-person-badge"></i> Profil Pengguna</h1>
            <p class="text-secondary mb-0">Kelola informasi akun website dan karakter in-game Apexsions Anda.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ url('/leaderboard') }}" class="btn btn-outline-warning">
                <i class="bi bi-trophy"></i> Papan Peringkat
            </a>
            @if($linkedAccount)
                <a href="{{ url('/player/' . $linkedAccount->minecraft_username) }}" class="btn btn-outline-info">
                    <i class="bi bi-eye"></i> Lihat Profil Publik
                </a>
            @endif
        </div>
    </div>

    @if(session('success'))
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    @if(session('error'))
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ session('error') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

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
                            <span class="text-secondary small">Kerajaan Faksi:</span>
                            <span class="fw-bold ms-1" style="color: {{ $currentKingdom['color'] }};">
                                <i class="bi {{ $currentKingdom['icon'] }}"></i> {{ $currentKingdom['name'] }}
                            </span>
                            @if($linkedAccount->level_title)
                                <span class="text-muted ms-2">• {{ strip_tags($linkedAccount->level_title) }}</span>
                            @endif
                        </div>

                        <!-- Level & XP Bar -->
                        <div class="mb-3">
                            <div class="d-flex justify-content-between text-sm mb-1">
                                <span class="fw-bold text-gold"><i class="bi bi-lightning-charge-fill"></i> Level {{ $linkedAccount->level }}</span>
                                <span class="text-muted">{{ number_format($linkedAccount->xp) }} / {{ number_format($linkedAccount->required_xp) }} XP ({{ $xpPercent }}%)</span>
                            </div>
                            <div class="progress" style="height: 10px; background-color: rgba(255,255,255,0.1); border-radius: 5px;">
                                <div class="progress-bar bg-warning progress-bar-striped progress-bar-animated" role="progressbar" style="width: {{ $xpPercent }}%;" aria-valuenow="{{ $xpPercent }}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                        </div>

                        <!-- Economy Stats Grid -->
                        <div class="row g-2 pt-2">
                            <div class="col-sm-6">
                                <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between">
                                    <span class="text-secondary small"><i class="bi bi-cash-coin text-success me-1"></i> Saldo Rupiah:</span>
                                    <span class="fw-bold text-white">Rp {{ number_format($linkedAccount->balance_rupiah, 0, ',', '.') }}</span>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-2 px-3 rounded bg-black bg-opacity-40 border border-secondary border-opacity-25 d-flex align-items-center justify-content-between">
                                    <span class="text-secondary small"><i class="bi bi-gem text-info me-1"></i> Diamond:</span>
                                    <span class="fw-bold text-white">💎 {{ number_format($linkedAccount->balance_diamond, 0, ',', '.') }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Action Buttons Column -->
                    <div class="col-lg-3 text-center text-lg-end">
                        <div class="d-grid gap-2">
                            <form action="{{ url('/profile/minecraft/claim-reward') }}" method="POST">
                                @csrf
                                <button type="submit" class="btn btn-warning w-100 fw-bold shadow-sm">
                                    <i class="bi bi-gift-fill me-1"></i> Klaim Hadiah Harian
                                </button>
                            </form>

                            <button type="button" class="btn btn-outline-light w-100" data-bs-toggle="modal" data-bs-target="#resetPasswordModal">
                                <i class="bi bi-key-fill me-1"></i> Reset Password In-Game
                            </button>

                            <form action="{{ url('/profile/minecraft/unlink') }}" method="POST" onsubmit="return confirm('Apakah Anda yakin ingin memutuskan tautan akun Minecraft ini?');">
                                @csrf
                                <button type="submit" class="btn btn-outline-danger btn-sm w-100">
                                    <i class="bi bi-link-45deg"></i> Putuskan Tautan (Unlink)
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
                    <h3 class="h3 fw-bold text-white font-cinzel">Hubungkan Karakter Minecraft Anda</h3>
                    <p class="text-secondary max-w-600 mx-auto mb-4">
                        Kaitkan akun website Anda dengan server Minecraft Apexsions untuk menampilkan skin 3D, sinkronisasi rank & level otomatis, mengklaim hadiah harian, dan mengelola profil in-game secara langsung.
                    </p>
                    <a href="{{ url('/link') }}" class="btn btn-warning btn-lg px-4 fw-bold shadow">
                        <i class="bi bi-link-45deg me-2"></i> Tautkan Akun Minecraft Sekarang
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
                            <span class="text-muted"><i class="bi bi-calendar3 me-1"></i> Terdaftar:</span>
                            <span class="text-white">{{ format_date($user->created_at, true) }}</span>
                        </li>
                        <li class="mb-2 d-flex justify-content-between">
                            <span class="text-muted"><i class="bi bi-wallet2 me-1"></i> Web Points:</span>
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
                                <i class="bi bi-shield-lock me-1"></i> {{ $user->hasTwoFactorAuth() ? 'Kelola 2FA' : 'Aktifkan 2FA' }}
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
                                    <i class="bi bi-envelope-at me-2 text-warning"></i> Ganti Alamat Email
                                </h3>
                                <form action="{{ route('profile.email') }}" method="POST">
                                    @csrf
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="emailInput">Email Baru</label>
                                        <input type="email" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('email') is-invalid @enderror" id="emailInput" name="email" value="{{ old('email', $user->email) }}" required>
                                        @error('email')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="emailConfirmPassInput">Password Saat Ini</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('email_confirm_pass') is-invalid @enderror" id="emailConfirmPassInput" name="email_confirm_pass" required>
                                        @error('email_confirm_pass')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                                        <i class="bi bi-check-lg"></i> Perbarui Email
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
                                    <i class="bi bi-lock me-2 text-warning"></i> Ganti Password Web
                                </h3>
                                <form action="{{ route('profile.password') }}" method="POST">
                                    @csrf
                                    <div class="mb-2">
                                        <label class="form-label text-muted small" for="passwordConfirmPassInput">Password Saat Ini</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('password_confirm_pass') is-invalid @enderror" id="passwordConfirmPassInput" name="password_confirm_pass" required>
                                        @error('password_confirm_pass')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-2">
                                        <label class="form-label text-muted small" for="passwordInput">Password Baru</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary @error('password') is-invalid @enderror" id="passwordInput" name="password" required>
                                        @error('password')
                                            <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                        @enderror
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label text-muted small" for="confirmPasswordInput">Konfirmasi Password Baru</label>
                                        <input type="password" class="form-control bg-black bg-opacity-30 text-white border-secondary" id="confirmPasswordInput" name="password_confirmation" required>
                                    </div>
                                    <button type="submit" class="btn btn-warning w-100 fw-bold">
                                        <i class="bi bi-check-lg"></i> Perbarui Password Web
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
                    <i class="bi bi-shield-lock-fill me-2"></i> Reset Password In-Game (AuthMe)
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="{{ url('/profile/minecraft/reset-password') }}" method="POST">
                @csrf
                <div class="modal-body">
                    <div class="alert alert-info py-2 small" role="alert">
                        <i class="bi bi-info-circle me-1"></i> Perintah reset akan dikirimkan ke server Minecraft untuk akun <strong>{{ $linkedAccount->minecraft_username }}</strong>.
                    </div>
                    <div class="mb-3">
                        <label for="newIngamePassword" class="form-label small text-muted">Password In-Game Baru (Minimal 6 karakter)</label>
                        <input type="password" class="form-control bg-black text-white border-secondary" id="newIngamePassword" name="new_password" minlength="6" required placeholder="Ketik password baru...">
                    </div>
                    <div class="mb-3">
                        <label for="confirmIngamePassword" class="form-label small text-muted">Konfirmasi Password Baru</label>
                        <input type="password" class="form-control bg-black text-white border-secondary" id="confirmIngamePassword" name="new_password_confirmation" minlength="6" required placeholder="Ulangi password baru...">
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">
                        <i class="bi bi-check2-circle me-1"></i> Konfirmasi & Ganti Password
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endif
@endsection
