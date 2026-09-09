@extends('layouts.app')

@section('title', 'Bilik Suara & Dukungan Realm (Vote)')

@section('description', 'Dukung kedaulatan server Minecraft Apexsions dengan memberikan suara di platform voting resmi. Dapatkan 3x Vote Keys dan Rp 1.000 saldo peradaban setiap suara sah.')

@section('content')
@php
    $activeUsername = $activeUsername ?? (auth()->check() ? (auth()->user()->name ?? '') : '');
    $linkedAccount = $linkedAccount ?? (auth()->check() ? \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', auth()->id())->first() : null);
    $cooldowns = $cooldowns ?? [];
    $personalHistory = $personalHistory ?? collect();
    $recentVotes = $recentVotes ?? collect();
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

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="vote_kicker">DUKUNGAN REALM &amp; BILIK SUARA RESMI</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="vote_title">Suarakan Kedaulatan Apexsions</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;" data-i18n="vote_desc">
                Setiap suara sah yang Anda berikan mengumandangkan kemakmuran peradaban Apexsions ke kancah dunia. Sebagai wujud terima kasih, para tetua menganugerahi imbalan pusaka sah secara instan ke dalam inventaris Anda.
            </p>
        </div>

        <!-- Player Identity Bar -->
        <div class="apx-player-identity-card p-3 p-md-4 mb-5 rounded" style="background: linear-gradient(135deg, rgba(24, 27, 36, 0.95) 0%, rgba(17, 19, 25, 0.95) 100%); border: 1px solid var(--apx-gold-border); box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
            <div class="row align-items-center g-3">
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
                    <div class="input-group input-group-sm" style="max-width: 380px;">
                        <span class="input-group-text bg-dark border-secondary text-gold"><i class="bi bi-person-fill"></i></span>
                        <input type="text" id="voterUsername" class="form-control bg-dark border-secondary text-white fw-bold" placeholder="Masukkan Username Minecraft Anda..." value="{{ $activeUsername ?: '' }}" maxlength="32">
                        <button type="button" class="btn btn-apx-gold btn-sm px-3" onclick="updateVoterIdentity()">
                            <i class="bi bi-save me-1"></i> Simpan
                        </button>
                    </div>
                    <small class="text-muted d-block mt-1" style="font-size: 0.75rem;">
                        *Imbalan 3x Vote Keys &amp; Rp 1.000 akan otomatis dikirimkan ke username ini saat verifikasi berhasil.
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
        </div>

        <!-- Voting Platforms Grid -->
        <div class="row g-4 mb-5 justify-content-center">
            @php
                $siteMeta = [
                    'minecraft-mp' => [
                        'sub' => 'Daftar Server Teratas Dunia',
                        'icon' => 'bi-trophy-fill',
                        'icon_color' => 'text-gold',
                        'desc' => 'Dukung peradaban Apexsions di daftar server Minecraft paling bergengsi. Suara Anda menaikkan kedaulatan realm di panggung internasional.',
                    ],
                    'topg' => [
                        'sub' => 'Peringkat Server Komunitas',
                        'icon' => 'bi-globe-americas',
                        'icon_color' => 'text-blue',
                        'desc' => 'Pilihan voting dengan siklus reset lebih cepat (12 jam). Berikan suara dua kali sehari untuk memaksimalkan perolehan kunci peti dan saldo.',
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
                <div class="apx-vote-card h-100 d-flex flex-column" id="card-{{ $site->slug }}">
                    <div class="apx-vote-card-header d-flex align-items-center justify-content-between mb-3">
                        <span class="apx-vote-number font-monospace">{{ $num }}</span>
                        @if($isReady)
                            <span class="badge bg-success"><i class="bi bi-check-circle me-1"></i> Suara Tersedia</span>
                        @else
                            <span class="badge apx-badge-cooldown"><i class="bi bi-clock-history me-1"></i> Cooldown: {{ $cdData['human_time'] }}</span>
                        @endif
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="apx-vote-icon-box {{ $meta['icon_color'] }}">
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
                    <div class="vstack gap-2">
                        <a href="{{ $site->vote_url }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-outline w-100 py-2" onclick="trackVoteClick('{{ $site->slug }}')">
                            <span>1. Buka Situs &amp; Beri Suara</span> <i class="bi bi-box-arrow-up-right ms-1 small"></i>
                        </a>
                        <button type="button" class="btn btn-apx-gold w-100 py-2 fw-bold" id="btn-claim-{{ $site->slug }}" onclick="claimVote('{{ $site->slug }}', '{{ $site->name }}')">
                            <i class="bi bi-patch-check-fill me-1"></i> 2. Verifikasi &amp; Klaim Hadiah
                        </button>
                    </div>
                </div>
            </div>
            @empty
            <div class="col-12 text-center py-5">
                <div class="alert alert-warning d-inline-block">
                    <i class="bi bi-exclamation-triangle me-2"></i> Bilik suara saat ini sedang dalam pemeliharaan berkala. Silakan coba beberapa saat lagi.
                </div>
            </div>
            @endforelse
        </div>

        <!-- User Vote History & Community Feats -->
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
                        Daftar suara terverifikasi dan imbalan yang berhasil dikirimkan ke akun Anda:
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
                                    <td>{{ $vote->voted_at->format('d/m H:i') }}</td>
                                    <td><span class="text-warning">3 Keys</span> + <span class="text-success">Rp 1k</span></td>
                                    <td><span class="badge bg-success">TERKIRIM</span></td>
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
                    <p class="text-muted small mb-3">
                        Warga yang baru saja memberikan suara kedaulatan dan menerima imbalan:
                    </p>
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

        <!-- 3 Claim Guide Steps -->
        <div class="apx-vote-claim-guide p-4 p-lg-5 mb-5 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border-subtle);">
            <div class="text-center mb-4">
                <span class="apx-section-kicker mb-2" data-i18n="vote_guide_kicker">TATA CARA KLAIM HADIAH RESMI</span>
                <h2 class="h3 text-white font-cinzel" data-i18n="vote_guide_title">Tiga Langkah Mengklaim Hadiah Kedaulatan</h2>
                <p class="text-muted small mx-auto" style="max-width: 600px;">
                    Sistem Deliveries Bridge Apexsions menyinkronkan data imbalan secara real-time ke dalam server Minecraft.
                </p>
            </div>
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">1</span>
                            <span class="text-gold small fw-bold text-uppercase">Beri Suara di Platform</span>
                        </div>
                        <h3 class="h6 text-white mb-2">Pilih Situs &amp; Kirim Vote</h3>
                        <p class="text-muted small mb-0">
                            Klik tombol <em>"Buka Situs &amp; Beri Suara"</em> di atas. Masukkan username Minecraft Anda persis sama di situs tersebut dan selesaikan captcha.
                        </p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">2</span>
                            <span class="text-gold small fw-bold text-uppercase">Tekan Verifikasi</span>
                        </div>
                        <h3 class="h6 text-white mb-2">Klaim Melalui Bilik Suara</h3>
                        <p class="text-muted small mb-0">
                            Kembali ke tab ini lalu tekan tombol <em>"Verifikasi &amp; Klaim Hadiah"</em>. Sistem akan memeriksa catatan suara sah Anda secara otomatis.
                        </p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">3</span>
                            <span class="text-gold small fw-bold text-uppercase">Imbalan Diterima</span>
                        </div>
                        <h3 class="h6 text-white mb-2">3 Vote Keys &amp; Rp 1.000</h3>
                        <p class="text-muted small mb-0">
                            Server Minecraft langsung mengeksekusi penambahan 3 Kunci Peti Pusaka dan Rp 1.000 saldo uang. Anda juga dapat memeriksa saldo melalui perintah <code class="text-gold">/balance</code> atau <code class="text-gold">/crates</code> di dalam game.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Toast Result -->
<div class="modal fade" id="claimResultModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary text-white">
            <div class="modal-header border-secondary" id="claimModalHeader">
                <h5 class="modal-title font-cinzel text-warning" id="claimModalTitle">Status Klaim Suara</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body text-center py-4" id="claimModalBody">
                <!-- Dynamic Content -->
            </div>
            <div class="modal-footer border-secondary">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Tutup</button>
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
    document.getElementById('historyUsernameBadge').textContent = username;
    // Reload page with parameter to refresh cooldowns
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

async function claimVote(siteSlug, siteName) {
    const input = document.getElementById('voterUsername');
    const username = input.value.trim();
    if (!username) {
        alert('Silakan masukkan username Minecraft Anda terlebih dahulu di bagian atas halaman.');
        input.focus();
        return;
    }

    const btn = document.getElementById(`btn-claim-${siteSlug}`);
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Memverifikasi...`;

    try {
        const response = await fetch(`/vote/verify/${siteSlug}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': '{{ csrf_token() }}',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username: username })
        });

        const data = await response.json();
        const modal = new bootstrap.Modal(document.getElementById('claimResultModal'));
        const modalTitle = document.getElementById('claimModalTitle');
        const modalBody = document.getElementById('claimModalBody');

        if (response.ok && data.success) {
            modalTitle.innerHTML = `<i class="bi bi-check-circle-fill text-success me-2"></i> Klaim Berhasil!`;
            modalBody.innerHTML = `
                <div class="mb-3">
                    <i class="bi bi-gift-fill text-warning display-4"></i>
                </div>
                <h4 class="text-white font-cinzel mb-2">Terima Kasih, ${username}!</h4>
                <p class="text-muted small mb-3">${data.message}</p>
                <div class="p-3 rounded mx-auto" style="max-width: 320px; background: rgba(255,255,255,0.03); border: 1px solid var(--apx-gold-border);">
                    <div class="text-warning fw-bold mb-1">🎁 3x Kunci Peti Pusaka (Vote Keys)</div>
                    <div class="text-success fw-bold">💰 Rp 1.000 Saldo Uang Realm</div>
                </div>
                <small class="text-dim d-block mt-3 font-monospace">Vote UUID: ${data.vote_uuid}</small>
            `;
            modal.show();
            setTimeout(() => { window.location.reload(); }, 3500);
        } else {
            modalTitle.innerHTML = `<i class="bi bi-exclamation-circle-fill text-danger me-2"></i> ${data.duplicate ? 'Sudah Diklaim' : 'Belum Terdeteksi'}`;
            modalBody.innerHTML = `
                <div class="mb-3">
                    <i class="bi ${data.duplicate ? 'bi-clock-history text-warning' : 'bi-x-octagon text-danger'} display-4"></i>
                </div>
                <h5 class="text-white mb-2">${data.duplicate ? 'Cooldown Aktif' : 'Verifikasi Gagal'}</h5>
                <p class="text-muted small">${data.message || 'Terjadi kesalahan saat memverifikasi suara Anda.'}</p>
            `;
            modal.show();
        }
    } catch (err) {
        console.error(err);
        alert('Gagal menghubungi server verifikasi. Silakan periksa koneksi internet Anda.');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
</script>
@endsection
