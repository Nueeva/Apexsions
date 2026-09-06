@extends('layouts.app')

@section('title', 'Bilik Suara & Dukungan Realm (Vote)')

@section('description', 'Dukung kedaulatan server Minecraft Apexsions dengan memberikan suara di platform voting resmi. Dapatkan Vote Keys, Battlepass XP, dan hadiah uang peradaban.')

@section('content')
<div class="apx-vote-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb Navigation -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page">Bilik Suara (Vote)</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2">DUKUNGAN REALM &amp; BILIK SUARA</div>
            <h1 class="apx-section-title display-5 mb-3">Suarakan Kedaulatan Apexsions</h1>
            <p class="text-muted mx-auto" style="max-width: 760px; font-size: 1.05rem; line-height: 1.8;">
                Setiap suara yang Anda berikan memperluas jangkauan peradaban Apexsions ke seluruh penjuru dunia. Sebagai wujud terima kasih, para tetua kerajaan menganugerahi hadiah pusaka, kunci peti langka, dan akselerasi progres untuk setiap suara sah.
            </p>
        </div>

        <!-- 3 Voting Platforms Grid -->
        <div class="row g-4 mb-5">
            <!-- Platform 1: Minecraft-MP -->
            <div class="col-lg-4 col-md-6">
                <div class="apx-vote-card h-100 d-flex flex-column">
                    <div class="apx-vote-card-header d-flex align-items-center justify-content-between mb-3">
                        <span class="apx-vote-number font-monospace">01</span>
                        <span class="badge apx-badge-cooldown"><i class="bi bi-clock-history me-1"></i> Cooldown 24 Jam</span>
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="apx-vote-icon-box text-gold">
                            <i class="bi bi-trophy-fill fs-3"></i>
                        </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">Minecraft-MP</h2>
                            <span class="text-dim small">Daftar Server Teratas</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-4 flex-grow-1">
                        Dukung peradaban di daftar server Minecraft paling bergengsi. Suara Anda menaikkan peringkat realm di mata komunitas global.
                    </p>
                    <div class="apx-vote-perks mb-4 p-3 rounded" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-gold-border-subtle);">
                        <div class="text-gold small fw-bold mb-2 text-uppercase" style="letter-spacing: 0.08em; font-size: 0.72rem;">Imbalan Langsung:</div>
                        <ul class="list-unstyled mb-0 small text-muted">
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> 1x Kunci Peti Pusaka (Vote Key)</li>
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> +250 Battlepass XP</li>
                            <li><i class="bi bi-check2 text-gold me-2"></i> Rp 10.000 Saldo Peradaban</li>
                        </ul>
                    </div>
                    <a href="https://minecraft-mp.com" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold w-100 py-2">
                        Beri Suara Sekarang <i class="bi bi-box-arrow-up-right ms-2 small"></i>
                    </a>
                </div>
            </div>

            <!-- Platform 2: TopG -->
            <div class="col-lg-4 col-md-6">
                <div class="apx-vote-card h-100 d-flex flex-column">
                    <div class="apx-vote-card-header d-flex align-items-center justify-content-between mb-3">
                        <span class="apx-vote-number font-monospace">02</span>
                        <span class="badge apx-badge-cooldown"><i class="bi bi-clock-history me-1"></i> Cooldown 12 Jam</span>
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="apx-vote-icon-box text-blue">
                            <i class="bi bi-globe-americas fs-3"></i>
                        </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">TopG Global</h2>
                            <span class="text-dim small">Peringkat Server Dunia</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-4 flex-grow-1">
                        Pilihan voting dengan jeda reset lebih cepat (12 jam). Berikan suara dua kali sehari untuk memaksimalkan perolehan kunci peti.
                    </p>
                    <div class="apx-vote-perks mb-4 p-3 rounded" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-gold-border-subtle);">
                        <div class="text-gold small fw-bold mb-2 text-uppercase" style="letter-spacing: 0.08em; font-size: 0.72rem;">Imbalan Langsung:</div>
                        <ul class="list-unstyled mb-0 small text-muted">
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> 1x Kunci Peti Pusaka (Vote Key)</li>
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> +200 Battlepass XP</li>
                            <li><i class="bi bi-check2 text-gold me-2"></i> 3x Diamond Murni Kerajaan</li>
                        </ul>
                    </div>
                    <a href="https://topg.org/minecraft-servers" target="_blank" rel="noopener noreferrer" class="btn btn-apx-outline w-100 py-2">
                        Beri Suara Sekarang <i class="bi bi-box-arrow-up-right ms-2 small"></i>
                    </a>
                </div>
            </div>

            <!-- Platform 3: PlanetMinecraft -->
            <div class="col-lg-4 col-md-6">
                <div class="apx-vote-card h-100 d-flex flex-column">
                    <div class="apx-vote-card-header d-flex align-items-center justify-content-between mb-3">
                        <span class="apx-vote-number font-monospace">03</span>
                        <span class="badge apx-badge-cooldown"><i class="bi bi-clock-history me-1"></i> Cooldown 24 Jam</span>
                    </div>
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="apx-vote-icon-box text-purple">
                            <i class="bi bi-stars fs-3"></i>
                        </div>
                        <div>
                            <h2 class="h5 text-white mb-0 font-cinzel">PlanetMinecraft</h2>
                            <span class="text-dim small">Komunitas Kreatif Global</span>
                        </div>
                    </div>
                    <p class="text-muted small mb-4 flex-grow-1">
                        Sentra kreasi arsitektur dan skin Minecraft terbesar. Perkuat reputasi peradaban Apexsions di kancah internasional.
                    </p>
                    <div class="apx-vote-perks mb-4 p-3 rounded" style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--apx-gold-border-subtle);">
                        <div class="text-gold small fw-bold mb-2 text-uppercase" style="letter-spacing: 0.08em; font-size: 0.72rem;">Imbalan Langsung:</div>
                        <ul class="list-unstyled mb-0 small text-muted">
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> 1x Kunci Peti Pusaka (Vote Key)</li>
                            <li class="mb-1"><i class="bi bi-check2 text-gold me-2"></i> +300 Battlepass XP</li>
                            <li><i class="bi bi-check2 text-gold me-2"></i> 1x Botol Penempaan Mistis</li>
                        </ul>
                    </div>
                    <a href="https://www.planetminecraft.com" target="_blank" rel="noopener noreferrer" class="btn btn-apx-outline w-100 py-2">
                        Beri Suara Sekarang <i class="bi bi-box-arrow-up-right ms-2 small"></i>
                    </a>
                </div>
            </div>
        </div>

        <!-- Claim Tutorial (3 Monolith Stepper) -->
        <div class="apx-vote-claim-guide p-4 p-lg-5 mb-5 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border-subtle);">
            <div class="text-center mb-4">
                <span class="apx-section-kicker mb-2">TATA CARA KLAIM HADIAH</span>
                <h2 class="h3 text-white font-cinzel">Tiga Langkah Mengklaim Hadiah di Dalam Game</h2>
                <p class="text-muted small mx-auto" style="max-width: 600px;">
                    Sistem WebBridge Apexsions menyinkronkan data pemilih secara otomatis setiap detik.
                </p>
            </div>
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">1</span>
                            <span class="text-gold small fw-bold text-uppercase">Nama Akun Akurat</span>
                        </div>
                        <h3 class="h6 text-white mb-2">Masukkan Username Minecraft</h3>
                        <p class="text-muted small mb-0">
                            Saat membuka halaman voting salah satu platform di atas, masukkan username Minecraft Anda secara persis (case-sensitive) tanpa spasi tambahan.
                        </p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">2</span>
                            <span class="text-gold small fw-bold text-uppercase">Kirimkan Suara</span>
                        </div>
                        <h3 class="h6 text-white mb-2">Selesaikan Verifikasi Suara</h3>
                        <p class="text-muted small mb-0">
                            Selesaikan captcha pada situs voting dan tekan tombol kirim (Vote). Situs akan mengonfirmasi bahwa suara Anda telah tercatat sah.
                        </p>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="apx-claim-step h-100 p-3 rounded" style="background: rgba(255, 255, 255, 0.015); border: 1px solid rgba(255, 255, 255, 0.05);">
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <span class="badge rounded-pill bg-warning text-dark font-monospace">3</span>
                            <span class="text-gold small fw-bold text-uppercase">Klaim Otomatis</span>
                        </div>
                        <h3 class="h6 text-white mb-2">Pemberian Hadiah Instan</h3>
                        <p class="text-muted small mb-0">
                            Jika sedang online di server, hadiah langsung masuk ke tas Anda. Jika sedang offline, ketik perintah <code class="text-gold">/claim</code> atau <code class="text-gold">/vote</code> saat login.
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Monthly Leaderboard & Prestigious Voter Rewards -->
        <div class="row g-4 align-items-center">
            <div class="col-lg-6">
                <div class="p-4 rounded h-100" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-gold-border);">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="badge bg-gold-subtle text-gold border border-gold-subtle text-uppercase small" style="letter-spacing: 0.1em;">
                            <i class="bi bi-crown-fill me-1"></i> PAPAN KEHORMATAN BULANAN
                        </span>
                        <span class="text-dim font-monospace small">RESET SETIAP TGL 1</span>
                    </div>
                    <h2 class="h4 text-white font-cinzel mb-3">Penghargaan Voter Terbanyak</h2>
                    <p class="text-muted small mb-4">
                        Warga yang paling setia mengumandangkan nama peradaban Apexsions setiap bulan akan dianugerahi gelar kehormatan dan paket pusaka eksklusif oleh dewan kerajaan:
                    </p>
                    <div class="vstack gap-3">
                        <div class="d-flex align-items-center justify-content-between p-2 rounded" style="background: rgba(245, 158, 11, 0.08); border-left: 3px solid var(--apx-gold);">
                            <div class="d-flex align-items-center gap-3">
                                <span class="fs-4 text-gold"><i class="bi bi-trophy-fill"></i></span>
                                <div>
                                    <div class="text-white small fw-bold">JUARA I &bull; MAHKOTA KEDAULATAN</div>
                                    <div class="text-muted" style="font-size: 0.78rem;">Gelar <strong>[✦ TOP VOTER ✦]</strong> (30 Hari) + 3x Golden Keys + Rp 100.000</div>
                                </div>
                            </div>
                            <span class="badge bg-warning text-dark font-monospace">TOP 1</span>
                        </div>
                        <div class="d-flex align-items-center justify-content-between p-2 rounded" style="background: rgba(255, 255, 255, 0.02); border-left: 3px solid #94a3b8;">
                            <div class="d-flex align-items-center gap-3">
                                <span class="fs-4 text-secondary"><i class="bi bi-award-fill"></i></span>
                                <div>
                                    <div class="text-white small fw-bold">JUARA II &bull; KESATRIA UTUSAN</div>
                                    <div class="text-muted" style="font-size: 0.78rem;">2x Golden Keys + Rp 50.000 Uang Realm + 500 XP Pass</div>
                                </div>
                            </div>
                            <span class="badge bg-secondary text-white font-monospace">TOP 2</span>
                        </div>
                        <div class="d-flex align-items-center justify-content-between p-2 rounded" style="background: rgba(255, 255, 255, 0.02); border-left: 3px solid #b45309;">
                            <div class="d-flex align-items-center gap-3">
                                <span class="fs-4 text-warning"><i class="bi bi-shield-fill-check"></i></span>
                                <div>
                                    <div class="text-white small fw-bold">JUARA III &bull; PEJUANG SUARA</div>
                                    <div class="text-muted" style="font-size: 0.78rem;">1x Golden Key + Rp 25.000 Uang Realm + 250 XP Pass</div>
                                </div>
                            </div>
                            <span class="badge bg-dark border border-secondary text-white font-monospace">TOP 3</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Server Telemetry Callout & Community Link -->
            <div class="col-lg-6">
                <div class="p-4 rounded h-100 text-center text-lg-start d-flex flex-column justify-content-between" style="background: linear-gradient(135deg, rgba(13, 18, 30, 0.95) 0%, rgba(20, 29, 47, 0.85) 100%); border: 1px solid var(--apx-gold-border-subtle);">
                    <div>
                        <div class="apx-section-kicker mb-2">GERBANG REALM SIAP MENYAMBUT</div>
                        <h2 class="h4 text-white font-cinzel mb-2">Masuk &amp; Rasakan Kemakmurannya</h2>
                        <p class="text-muted small mb-4" style="line-height: 1.8;">
                            Setelah memberikan suara, sambungkan klien Anda ke dunia Apexsions untuk segera membuka peti keberuntungan di pelataran spawn utama.
                        </p>
                        <div class="p-3 rounded mb-4 d-inline-flex align-items-center gap-3 apx-copyable" data-apx-copy="apexsions.my.id:32348" role="button" tabindex="0" title="Klik untuk menyalin IP" aria-label="Salin Alamat Server Java">
                            <span class="apx-pulse-dot" style="width: 8px; height: 8px;"></span>
                            <span class="font-monospace text-white small">apexsions.my.id:32348</span>
                            <i class="bi bi-clipboard text-gold small ms-2"></i>
                        </div>
                    </div>
                    <div class="d-flex align-items-center justify-content-center justify-content-lg-start gap-3 flex-wrap">
                        <a href="{{ route('home') }}" class="btn btn-apx-outline btn-sm py-2 px-3">
                            <i class="bi bi-house me-1"></i> Kembali ke Beranda
                        </a>
                        <a href="https://discord.gg/apexsions" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold btn-sm py-2 px-3">
                            <i class="bi bi-discord me-1"></i> Gabung Discord Komunitas
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
