@extends('layouts.app')

@section('title', 'Bilik Suara & Dukungan Realm (Vote)')

@section('description', 'Dukung kedaulatan server Minecraft Apexsions dengan memberikan suara di platform voting resmi. Dapatkan 3x Vote Keys dan Rp 1.000 saldo peradaban secara otomatis setiap suara sah.')

@section('content')
@php
    $activeUsername = $activeUsername ?? (auth()->check() ? (auth()->user()->name ?? '') : '');
    $linkedAccount = $linkedAccount ?? (auth()->check() ? \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', auth()->id())->first() : null);
    $cooldowns = $cooldowns ?? [];
    $personalHistory = $personalHistory ?? collect();
    $recentVotes = $recentVotes ?? collect();
    $voterStats = $voterStats ?? ['total' => 0, 'this_month' => 0, 'this_week' => 0, 'today' => 0, 'streak' => 0, 'last_voted_at' => null];
    $serverTotalVotes = $serverTotalVotes ?? 0;
    $serverVotesToday = $serverVotesToday ?? 0;
    $serverVotesMonth = $serverVotesMonth ?? 0;
@endphp
<div class="apx-vote-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb Navigation -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page" data-i18n="vote_breadcrumb">Bilik Suara (Vote)</li>
            </ol>
        </nav>

        <!-- Page Header Hero -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="vote_kicker">DUKUNGAN REALM &amp; AUTO-REWARD RESMI</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="vote_title">Suarakan Kedaulatan Apexsions</h1>
            <p class="text-muted mx-auto" style="max-width: 780px; font-size: 1.05rem; line-height: 1.8;" data-i18n="vote_desc">
                Setiap suara sah yang Anda berikan mengumandangkan kemakmuran peradaban Apexsions ke kancah dunia. Cukup klik tombol platform di bawah dan berikan suara Anda di situs tersebut. Sistem akan mendeteksi suara Anda secara otomatis dan menghadiahkan <strong class="text-warning">3x Vote Keys</strong> serta <strong class="text-success">Rp 1.000</strong> langsung ke inventaris Anda!
            </p>
            <div class="d-inline-flex align-items-center gap-2 px-3 py-2 rounded-pill mt-2" style="background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.3);">
                <span class="badge bg-warning text-dark fw-bold">⚡ AUTO REWARD</span>
                <span class="text-white small fw-bold">Tidak perlu lagi verifikasi manual atau kembali untuk klik claim!</span>
            </div>
        </div>

        <!-- Player Identity & Statistics Dashboard -->
        <div class="apx-player-identity-card p-3 p-md-4 mb-5 rounded" style="background: linear-gradient(135deg, rgba(24, 27, 36, 0.95) 0%, rgba(17, 19, 25, 0.95) 100%); border: 1px solid var(--apx-gold-border); box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
            <div class="row align-items-center g-3 mb-3">
                <div class="col-md-auto text-center text-md-start">
                    <img id="voterAvatar" src="https://mc-heads.net/avatar/{{ $activeUsername ?: 'steve' }}/56" class="rounded shadow-sm border border-secondary" width="56" height="56" alt="Avatar">
                </div>
                <div class="col-md">
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <span class="badge bg-gold-subtle text-gold border border-gold-subtle text-uppercase" style="font-size: 0.68rem; letter-spacing: 0.08em;" data-i18n="vote_recipient">Target Penerima Imbalan</span>
                        @if($linkedAccount)
                            <span class="badge bg-success text-white" style="font-size: 0.68rem;"><i class="bi bi-shield-check me-1"></i> Akun Tertaut Resmi</span>
                        @endif
                    </div>
                    <div class="input-group input-group-sm" style="max-width: 420px;">
                        <span class="input-group-text bg-dark border-secondary text-gold"><i class="bi bi-person-fill"></i></span>
                        <input type="text" id="voterUsername" class="form-control bg-dark border-secondary text-white fw-bold" placeholder="Masukkan Username Minecraft Anda..." value="{{ $activeUsername ?: '' }}" maxlength="32">
                        <button type="button" class="btn btn-apx-gold btn-sm px-3" onclick="updateVoterIdentity()">
                            <i class="bi bi-arrow-clockwise me-1"></i> Perbarui Profil
                        </button>
                    </div>
                    <small class="text-muted d-block mt-1" style="font-size: 0.75rem;">
                        *Pastikan username ini dimasukkan persis sama saat voting di situs luar. Baik Anda sedang online maupun offline, hadiah tetap aman!
                    </small>
                </div>
                <div class="col-md-auto text-center text-md-end">
                    <div class="d-inline-block text-start p-2 px-3 rounded" style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08);">
                        <div class="small fw-bold text-gold text-uppercase" style="font-size: 0.7rem;">Imbalan Pasti per Suara:</div>
                        <div class="text-white small"><i class="bi bi-gift-fill text-warning me-1"></i> <strong>3x</strong> Vote Crate Keys</div>
                        <div class="text-white small"><i class="bi bi-coin text-success me-1"></i> <strong>Rp 1.000</strong> Saldo Uang Realm</div>
                    </div>
                </div>
            </div>

            <!-- Player Personal Metric Badges -->
            <div class="row g-2 pt-2 border-top border-secondary border-opacity-25 text-center">
                <div class="col-6 col-md">
                    <div class="p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="text-muted d-block small" style="font-size: 0.72rem;">Total Suara Kamu</span>
                        <strong class="text-gold fs-5">{{ number_format($voterStats['total']) }}</strong>
                    </div>
                </div>
                <div class="col-6 col-md">
                    <div class="p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="text-muted d-block small" style="font-size: 0.72rem;">Bulan Ini</span>
                        <strong class="text-white fs-5">{{ number_format($voterStats['this_month']) }}</strong>
                    </div>
                </div>
                <div class="col-6 col-md">
                    <div class="p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="text-muted d-block small" style="font-size: 0.72rem;">Minggu Ini</span>
                        <strong class="text-info fs-5">{{ number_format($voterStats['this_week']) }}</strong>
                    </div>
                </div>
                <div class="col-6 col-md">
                    <div class="p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="text-muted d-block small" style="font-size: 0.72rem;">Hari Ini</span>
                        <strong class="text-success fs-5">{{ number_format($voterStats['today']) }}</strong>
                    </div>
                </div>
                <div class="col-12 col-md">
                    <div class="p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="text-muted d-block small" style="font-size: 0.72rem;">Streak Harian</span>
                        <strong class="text-warning fs-5">🔥 {{ $voterStats['streak'] }} Hari</strong>
                    </div>
                </div>
            </div>
        </div>

        <!-- Voting Platforms Grid (Direct CTA, No Manual Verification) -->
        <div class="row g-4 mb-5 justify-content-center">
            @php
                $siteMeta = [
                    'minecraft-mp' => [
                        'sub' => 'Daftar Server Teratas Dunia',
                        'icon' => 'bi-trophy-fill',
                        'icon_color' => 'text-gold',
                        'desc' => 'Dukung peradaban Apexsions di daftar server Minecraft paling bergengsi. Hadiah otomatis diproses secara real-time.',
                    ],
                    'topg' => [
                        'sub' => 'Peringkat Server Komunitas',
                        'icon' => 'bi-globe-americas',
                        'icon_color' => 'text-blue',
                        'desc' => 'Pilihan voting dengan siklus reset lebih cepat (12 jam). Berikan suara dua kali sehari untuk memaksimalkan kunci peti dan saldo.',
                    ],
                    'planetminecraft' => [
                        'sub' => 'Komunitas Kreatif Global',
                        'icon' => 'bi-stars',
                        'icon_color' => 'text-purple',
                        'desc' => 'Sentra kreasi arsitektur dan skin Minecraft terbesar. Perkuat pengaruh peradaban Apexsions di antara para builder dunia.',
                    ],
                ];
            @endphp

            @forelse($sites as $index => $site)
            @php
                $meta = $siteMeta[$site->slug] ?? [
                    'sub' => 'Platform Voting Sah',
                    'icon' => 'bi-patch-check-fill',
                    'icon_color' => 'text-gold',
                    'desc' => 'Berikan suara kedaulatan untuk server Apexsions dan klaim imbalan sah Anda.',
                ];
                $cdData = $cooldowns[$site->slug] ?? ['ready' => true, 'human_time' => null];
                $isReady = $cdData['ready'];
                $num = str_pad($index + 1, 2, '0', STR_PAD_LEFT);
            @endphp
            <div class="{{ count($sites) === 1 ? 'col-lg-6 col-md-8' : (count($sites) === 2 ? 'col-lg-6' : 'col-lg-4 col-md-6') }}">
                <div class="apx-vote-card h-100 d-flex flex-column" id="card-{{ $site->slug }}" style="background: linear-gradient(135deg, rgba(20, 24, 33, 0.95) 0%, rgba(14, 16, 22, 0.95) 100%); border: 1px solid var(--apx-gold-border); border-radius: 12px; padding: 1.5rem;">
                    <div class="apx-vote-card-header d-flex align-items-center justify-content-between mb-3">
                        <span class="apx-vote-number font-monospace text-gold fw-bold" style="font-size: 1.25rem;">{{ $num }}</span>
                        @if($isReady)
                            <span class="badge bg-success"><i class="bi bi-check-circle me-1"></i> Siap Diberikan</span>
                        @else
                            <span class="badge bg-secondary text-white"><i class="bi bi-clock-history me-1"></i> Estimasi Cooldown: {{ $cdData['human_time'] }}</span>
                        @endif
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="apx-vote-icon-box {{ $meta['icon_color'] }} p-2 rounded" style="background: rgba(255, 255, 255, 0.04); border: 1px solid rgba(255, 255, 255, 0.08);">
                            <i class="bi {{ $meta['icon'] }} fs-3"></i>
                        </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">{{ $site->name }}</h2>
                            <span class="text-dim small">{{ $meta['sub'] }}</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-4 flex-grow-1">
                        {{ $meta['desc'] }}
                    </p>
                    <div class="apx-vote-perks mb-4 p-3 rounded" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-gold-border-subtle);">
                        <div class="text-gold small fw-bold mb-2 text-uppercase" style="letter-spacing: 0.08em; font-size: 0.72rem;">Imbalan Sah per Suara:</div>
                        <ul class="list-unstyled mb-0 small text-muted">
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> <strong class="text-warning">3x Kunci Peti Pusaka (Vote Keys)</strong></li>
                            <li><i class="bi bi-check2 text-gold me-2"></i> <strong class="text-success">Rp 1.000 Saldo Uang Peradaban</strong></li>
                        </ul>
                    </div>
                    <!-- Single Direct CTA: Vote Now -->
                    <div>
                        <a href="{{ $site->vote_url }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold w-100 py-2 fw-bold d-flex align-items-center justify-content-center gap-2 shadow-sm" onclick="trackVoteClick('{{ $site->slug }}')">
                            <span>Beri Suara di {{ $site->name }}</span>
                            <i class="bi bi-box-arrow-up-right"></i>
                        </a>
                        <span class="text-muted d-block text-center mt-2" style="font-size: 0.72rem;">
                            <i class="bi bi-shield-check text-success me-1"></i> Hadiah otomatis masuk setelah vote selesai.
                        </span>
                    </div>
                </div>
            </div>
            @empty
            <div class="col-12 text-center py-5">
                <div class="alert alert-warning d-inline-block">
                    <i class="bi bi-exclamation-triangle me-2"></i> Bilik suara saat ini sedang dalam sinkronisasi berkala. Silakan coba beberapa saat lagi.
                </div>
            </div>
            @endforelse
        </div>

        <!-- 2 Simple Steps Guide -->
        <div class="apx-vote-claim-guide p-4 mb-5 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border-subtle);">
            <div class="text-center mb-4">
                <span class="apx-section-kicker mb-1" data-i18n="vote_guide_kicker">ALUR PENGALAMAN VOTING</span>
                <h2 class="h4 text-white font-cinzel mb-1" data-i18n="vote_guide_title">Semudah 2 Langkah Tanpa Ribet</h2>
                <p class="text-muted small mx-auto mb-0" style="max-width: 650px;">
                    Anda tidak perlu bolak-balik menekan tombol verifikasi manual. Server mendeteksi dan mengirimkan hadiah langsung ke dalam game.
                </p>
            </div>
            <div class="row g-3">
                <div class="col-md-6">
                    <div class="p-3 rounded h-100" style="background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace fw-bold">1</span>
                            <h3 class="h6 text-white mb-0">Klik Tombol &amp; Beri Suara</h3>
                        </div>
                        <p class="text-muted small mb-0">
                            Pilih platform di atas, klik tombol emas, masukkan username Minecraft Anda persis sama di situs voting, dan selesaikan vote.
                        </p>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="p-3 rounded h-100" style="background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-success text-white font-monospace fw-bold">2</span>
                            <h3 class="h6 text-white mb-0">Hadiah Masuk Otomatis</h3>
                        </div>
                        <p class="text-muted small mb-0">
                            Selesai! Hadiah <strong>3x Vote Crate Keys</strong> dan <strong>Rp 1.000</strong> langsung diproses ke akun Anda di in-game server.
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- User Vote History & Live Community Feed -->
        <div class="row g-4 mb-5">
            <!-- Left: Personal History -->
            <div class="col-lg-6">
                <div class="p-4 rounded h-100" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border);">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <h3 class="h5 text-white font-cinzel mb-0">
                            <i class="bi bi-journal-bookmark me-2 text-gold"></i> Riwayat Suara Saya
                        </h3>
                        <span class="badge bg-dark border border-secondary text-warning" id="historyUsernameBadge">
                            {{ $activeUsername ?: 'Belum Mengisi Username' }}
                        </span>
                    </div>
                    <p class="text-muted small mb-3">
                        Daftar suara dan status pengiriman hadiah ke akun Anda:
                    </p>
                    <div class="table-responsive">
                        <table class="table table-sm table-dark align-middle mb-0" style="font-size: 0.8rem;">
                            <thead>
                                <tr class="text-muted text-uppercase">
                                    <th>Platform</th>
                                    <th>Waktu</th>
                                    <th>Imbalan</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody id="personalHistoryTable">
                                @forelse($personalHistory as $vote)
                                <tr>
                                    <td><span class="badge bg-dark border text-warning">{{ $vote->site ? $vote->site->name : $vote->site_slug }}</span></td>
                                    <td>{{ $vote->voted_at->format('d M H:i') }}</td>
                                    <td><span class="text-warning">3 Keys</span> + <span class="text-success">Rp 1k</span></td>
                                    <td>
                                        @if($vote->reward_status === 'REWARDED')
                                            <span class="badge bg-success text-white"><i class="bi bi-check-circle me-1"></i> DITERIMA</span>
                                        @elseif($vote->reward_status === 'FAILED')
                                            <span class="badge bg-danger text-white"><i class="bi bi-x-circle me-1"></i> GAGAL</span>
                                        @elseif($vote->reward_status === 'PARTIAL')
                                            <span class="badge bg-warning text-dark"><i class="bi bi-exclamation-circle me-1"></i> SEBAGIAN</span>
                                        @else
                                            <span class="badge bg-info text-dark"><i class="bi bi-hourglass-split me-1"></i> MEMPROSES</span>
                                        @endif
                                    </td>
                                </tr>
                                @empty
                                <tr>
                                    <td colspan="4" class="text-center py-3 text-muted">
                                        Belum ada riwayat suara sah yang tercatat untuk akun ini.
                                    </td>
                                </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Right: Live Community Recent Votes -->
            <div class="col-lg-6">
                <div class="p-4 rounded h-100" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border);">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <h3 class="h5 text-white font-cinzel mb-0">
                            <i class="bi bi-broadcast me-2 text-info"></i> Suara Warga Realm Terbaru
                        </h3>
                        <span class="badge bg-success"><span class="apx-pulse-dot me-1"></span> Live Feed</span>
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3 p-2 rounded" style="background: rgba(0,0,0,0.25);">
                        <span class="small text-muted">Total Suara Server: <strong class="text-gold">{{ number_format($serverTotalVotes) }}</strong></span>
                        <span class="small text-muted">&bull;</span>
                        <span class="small text-muted">Hari Ini: <strong class="text-success">{{ number_format($serverVotesToday) }}</strong></span>
                        <span class="small text-muted">&bull;</span>
                        <span class="small text-muted">Bulan Ini: <strong class="text-info">{{ number_format($serverVotesMonth) }}</strong></span>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-sm table-dark align-middle mb-0" style="font-size: 0.8rem;">
                            <thead>
                                <tr class="text-muted text-uppercase">
                                    <th>Warga</th>
                                    <th>Platform</th>
                                    <th>Waktu</th>
                                    <th>Imbalan</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($recentVotes as $v)
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <img src="https://mc-heads.net/avatar/{{ $v->player_username }}/20" width="20" height="20" class="rounded" alt="">
                                            <span class="fw-bold text-white">{{ $v->player_username }}</span>
                                        </div>
                                    </td>
                                    <td><span class="badge bg-dark border text-info">{{ $v->site ? $v->site->name : $v->site_slug }}</span></td>
                                    <td>{{ $v->voted_at->diffForHumans() }}</td>
                                    <td><span class="badge bg-success text-white">3 Keys + Rp 1k</span></td>
                                </tr>
                                @empty
                                <tr>
                                    <td colspan="4" class="text-center py-3 text-muted">
                                        Belum ada catatan suara baru hari ini. Jadilah yang pertama!
                                    </td>
                                </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Troubleshoot & Check Vote Status (Secondary Assistance Accordion) -->
        <div class="accordion mb-5" id="troubleshootAccordion">
            <div class="accordion-item bg-dark border-secondary">
                <h2 class="accordion-header" id="headingTroubleshoot">
                    <button class="accordion-button collapsed bg-dark text-muted fw-bold" type="button" data-bs-toggle="collapse" data-bs-target="#collapseTroubleshoot" aria-expanded="false" aria-controls="collapseTroubleshoot">
                        <i class="bi bi-question-circle text-gold me-2"></i> Mengalami Kendala atau Delay dari Platform? (Cek Status Suara)
                    </button>
                </h2>
                <div id="collapseTroubleshoot" class="accordion-collapse collapse" aria-labelledby="headingTroubleshoot" data-bs-parent="#troubleshootAccordion">
                    <div class="accordion-body text-muted small p-4">
                        <p class="mb-3">
                            Platform eksternal seperti Minecraft-MP terkadang membutuhkan waktu 1-3 menit untuk merilis data suara ke server. Sistem kami melakukan polling berkala secara otomatis. Jika Anda ingin melakukan pengecekan instan, klik tombol di bawah:
                        </p>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <button type="button" class="btn btn-outline-warning btn-sm" id="btnCheckStatus" onclick="checkVoteStatus()">
                                <i class="bi bi-search me-1"></i> Cek Status Suara Saya Sekarang
                            </button>
                            <span id="statusCheckFeedback" class="small ms-2"></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>

<script>
// LocalStorage caching for voter username
document.addEventListener('DOMContentLoaded', () => {
    const savedUser = localStorage.getItem('apx_voter_username');
    const input = document.getElementById('voterUsername');
    if (!input.value && savedUser) {
        input.value = savedUser;
        updateVoterAvatar(savedUser);
    }
});

function updateVoterIdentity() {
    const input = document.getElementById('voterUsername');
    const username = input.value.trim();
    if (!username) {
        alert('Silakan masukkan username Minecraft Anda terlebih dahulu.');
        return;
    }
    localStorage.setItem('apx_voter_username', username);
    updateVoterAvatar(username);
    const badge = document.getElementById('historyUsernameBadge');
    if (badge) badge.textContent = username;
    // Reload page with parameter to refresh cooldowns & statistics
    window.location.href = `{{ route('vote') }}?username=${encodeURIComponent(username)}`;
}

function updateVoterAvatar(username) {
    const avatar = document.getElementById('voterAvatar');
    if (avatar && username) {
        avatar.src = `https://mc-heads.net/avatar/${encodeURIComponent(username)}/56`;
    }
}

function trackVoteClick(siteSlug) {
    console.log(`[Vote] Player opened platform ${siteSlug}`);
}

async function checkVoteStatus() {
    const input = document.getElementById('voterUsername');
    const username = input.value.trim();
    if (!username) {
        alert('Silakan masukkan username Minecraft Anda di bagian atas halaman.');
        input.focus();
        return;
    }

    const btn = document.getElementById('btnCheckStatus');
    const feedback = document.getElementById('statusCheckFeedback');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Memeriksa...`;
    feedback.innerHTML = `<span class="text-info">Menghubungi platform...</span>`;

    try {
        const response = await fetch('{{ route("vote.check-status") }}', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': '{{ csrf_token() }}',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username: username, site_slug: 'minecraft-mp' })
        });

        const data = await response.json();
        if (response.ok) {
            feedback.innerHTML = `<span class="text-success"><i class="bi bi-check-circle me-1"></i> ${data.message}</span>`;
            if (data.status === 'REWARDED') {
                setTimeout(() => { window.location.reload(); }, 2000);
            }
        } else {
            feedback.innerHTML = `<span class="text-warning"><i class="bi bi-info-circle me-1"></i> ${data.message || 'Status belum tersedia.'}</span>`;
        }
    } catch (err) {
        feedback.innerHTML = `<span class="text-danger"><i class="bi bi-x-circle me-1"></i> Gagal menghubungi server. Silakan coba lagi.</span>`;
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
</script>
@endsection
