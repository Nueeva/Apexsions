@extends('layouts.app')

@section('title', 'Beranda')

@section('content')
<!-- Panoramic Hero Section: Viewport Adaptive (Fits 100% Player Screen at Normal Zoom) -->
<section class="apx-hero-panoramic">
    <div class="apx-hero-panoramic-bg" style="background-image: linear-gradient(180deg, rgba(6, 8, 13, 0.75) 0%, rgba(9, 12, 19, 0.2) 40%, rgba(6, 8, 13, 0.85) 85%, #07090e 100%), url('{{ theme_asset('img/hero-panoramic.png') }}');"></div>

    <div class="container position-relative d-flex flex-column justify-content-between h-100 py-2" style="z-index: 2; flex: 1;">
        <!-- Top Kicker & Action Verbs Row -->
        <div class="d-flex justify-content-between align-items-center pt-2 mb-2">
            <div class="d-flex align-items-center gap-3">
                <span class="apx-kicker"><i class="bi bi-shield-shaded me-1"></i> THE PEAK CIVILIZATIONS</span>
                <span class="apx-kicker-line"></span>
            </div>
            <div class="apx-hero-verbs d-none d-md-flex flex-column text-end">
                <span>PLAY</span>
                <span>BUILD</span>
                <span>TRADE</span>
                <span>RULE</span>
            </div>
        </div>

        <!-- Main Headline & Subtitle -->
        <div class="row my-auto py-2">
            <div class="col-xl-8 col-lg-9">
                <h1 class="apx-hero-headline">
                    Build. Conquer. Belong.<br>
                    <span class="text-gradient-gold">Apexsions.</span>
                </h1>

                <p class="apx-hero-subtext">
                    Rasakan sensasi membangun kerajaan berdaulat, ekonomi atomik yang digerakkan oleh pemain, dan progres peradaban yang kompetitif di atas Minecraft 1.21.4.
                </p>

                <!-- Action CTAs -->
                <div class="apx-hero-ctas d-flex align-items-center gap-3 flex-wrap">
                    <button type="button" class="btn btn-apx-play" data-apx-copy="apexsions.my.id" aria-label="Mulai Bermain dan Salin IP">
                        <i class="bi bi-play-fill me-1" style="font-size: 1.15rem;"></i> Mulai Bermain <i class="bi bi-chevron-right ms-2 small"></i>
                    </button>

                    @if(plugins()->isEnabled('shop'))
                        <a href="{{ route('shop.home') }}" class="btn btn-apx-webstore">
                            <i class="bi bi-bag me-2"></i> Webstore <i class="bi bi-chevron-right ms-2 small"></i>
                        </a>
                    @endif

                    @if(plugins()->isEnabled('wiki'))
                        <a href="{{ route('wiki.index') }}" class="btn btn-apx-webstore">
                            <i class="bi bi-journal-text me-2"></i> Panduan Wiki <i class="bi bi-chevron-right ms-2 small"></i>
                        </a>
                    @endif
                </div>
            </div>
        </div>

        <!-- Horizontal Dark Glass Server Status Bar (Anchored Cleanly to Bottom of First Viewport) -->
        <div class="apx-status-panoramic mt-auto">
            <!-- 1. Server Online Status -->
            <div class="apx-status-cell">
                <span class="apx-pulse-dot" id="apxLiveDot"></span>
                <div>
                    <div class="apx-status-label text-success fw-bold" id="apxLiveBadge">SERVER ONLINE</div>
                    <div class="apx-status-sub" id="apxLiveSub">Java &amp; Bedrock Siap</div>
                </div>
            </div>

            <div class="apx-status-divider d-none d-md-block"></div>

            <!-- 2. Server IP Address (Click to copy) -->
            <div class="apx-status-cell apx-copyable" data-apx-copy="apexsions.my.id" role="button" tabindex="0" title="Klik untuk menyalin IP Java">
                <i class="bi bi-globe2 apx-cell-icon"></i>
                <div>
                    <div class="apx-status-value">APEXSIONS.MY.ID</div>
                    <div class="apx-status-sub">Alamat Server <span class="badge-copy"><i class="bi bi-clipboard"></i></span></div>
                </div>
            </div>

            <div class="apx-status-divider d-none d-md-block"></div>

            <!-- 3. Bedrock Port (Click to copy) -->
            <div class="apx-status-cell apx-copyable" data-apx-copy="19132" role="button" tabindex="0" title="Klik untuk menyalin Port Bedrock">
                <i class="bi bi-controller apx-cell-icon"></i>
                <div>
                    <div class="apx-status-value">19132</div>
                    <div class="apx-status-sub">Bedrock Port <span class="badge-copy"><i class="bi bi-clipboard"></i></span></div>
                </div>
            </div>

            <div class="apx-status-divider d-none d-lg-block"></div>

            <!-- 4. Minecraft Version -->
            <div class="apx-status-cell d-none d-lg-flex">
                <i class="bi bi-box-seam apx-cell-icon"></i>
                <div>
                    <div class="apx-status-value" id="apxVersion">1.21.4</div>
                    <div class="apx-status-sub">Versi Minecraft</div>
                </div>
            </div>

            <div class="apx-status-divider d-none d-sm-block"></div>

            <!-- 5. Online Players (Live Bridge Integration) -->
            <div class="apx-status-cell">
                <i class="bi bi-people apx-cell-icon"></i>
                <div>
                    <div class="apx-status-value"><span id="apxOnlinePlayers">0</span> / <span id="apxMaxPlayers">500</span></div>
                    <div class="apx-status-sub">Pemain Online</div>
                </div>
            </div>

            <div class="apx-status-divider d-none d-xl-block"></div>

            <!-- 6. Tagline Pillar -->
            <div class="apx-status-cell apx-status-motto d-none d-xl-flex">
                <div class="text-end">
                    <div class="apx-status-label text-gold fw-bold">A GREATER TOMORROW</div>
                    <div class="apx-status-sub">BUILT TOGETHER</div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Section: A WORLD BUILT BY PLAYERS (2-Column Asymmetric Showcase) -->
<section class="apx-section apx-world-section position-relative overflow-hidden" id="features">
    <div class="apx-watermark-bg" aria-hidden="true">APEXSIONS</div>

    <div class="container position-relative" style="z-index: 2;">
        <!-- Header / Intro -->
        <div class="apx-section-intro mb-5">
            <div class="d-flex align-items-center gap-3 mb-2">
                <span class="apx-kicker">MENGAPA APEXSIONS</span>
                <span class="apx-kicker-line"></span>
            </div>
            <h2 class="apx-world-title mb-3">
                A WORLD BUILT <span class="text-gradient-gold">BY PLAYERS</span>
            </h2>
            <p class="apx-world-lead text-muted" style="max-width: 680px; font-size: 1.05rem; line-height: 1.7;">
                Apexsions adalah lebih dari sekadar server Minecraft. Ini adalah dunia yang dibentuk oleh pemain: tempat kerajaan lahir, ekonomi bergerak, dan peradaban berkembang.
            </p>
        </div>

        <!-- 2-Column Showcase Row -->
        <div class="row g-4 align-items-stretch mb-5">
            <!-- Left Column: 3 Sleek Feature Cards -->
            <div class="col-lg-6 d-flex flex-column justify-content-between gap-3">
                <!-- Feature 1: Kerajaan -->
                <div class="apx-player-card">
                    <div class="apx-player-icon">
                        <i class="bi bi-shield-shaded"></i>
                    </div>
                    <div class="apx-player-content">
                        <h4 class="apx-player-heading">Kerajaan</h4>
                        <p class="apx-player-desc">
                            Dirikan kerajaan, bentuk aliansi, perluas wilayah, dan tinggalkan warisan dalam sejarah peradaban Apexsions.
                        </p>
                        <a href="#ranks" class="apx-player-link">
                            JELAJAHI <i class="bi bi-arrow-right ms-1"></i>
                        </a>
                    </div>
                </div>

                <!-- Feature 2: Ekonomi -->
                <div class="apx-player-card">
                    <div class="apx-player-icon">
                        <i class="bi bi-coin"></i>
                    </div>
                    <div class="apx-player-content">
                        <h4 class="apx-player-heading">Ekonomi</h4>
                        <p class="apx-player-desc">
                            Sistem ekonomi pemain yang dinamis, bebas, dan saling terhubung di seluruh kerajaan dengan proteksi transaksi atomic.
                        </p>
                        @if(plugins()->isEnabled('shop'))
                            <a href="{{ route('shop.home') }}" class="apx-player-link">
                                PELAJARI <i class="bi bi-arrow-right ms-1"></i>
                            </a>
                        @else
                            <a href="#ranks" class="apx-player-link">
                                PELAJARI <i class="bi bi-arrow-right ms-1"></i>
                            </a>
                        @endif
                    </div>
                </div>

                <!-- Feature 3: Peradaban -->
                <div class="apx-player-card">
                    <div class="apx-player-icon">
                        <i class="bi bi-bank"></i>
                    </div>
                    <div class="apx-player-content">
                        <h4 class="apx-player-heading">Peradaban</h4>
                        <p class="apx-player-desc">
                            Bangun infrastruktur megah, kembangkan kota, dan dorong kemajuan bersama komunitas petualang yang suportif.
                        </p>
                        <a href="#getting-started" class="apx-player-link apx-link-gold">
                            MULAI SEKARANG <i class="bi bi-arrow-right ms-1"></i>
                        </a>
                    </div>
                </div>
            </div>

            <!-- Right Column: Photographic Realm Showcase Card -->
            <div class="col-lg-6">
                <div class="apx-showcase-box">
                    <div class="apx-showcase-media">
                        <img src="{{ theme_asset('img/realm-showcase.jpg') }}" alt="Apexsions Realm Scenery" class="apx-showcase-img">
                        <div class="apx-showcase-overlay"></div>
                        <div class="apx-showcase-tag">SAME PLAYERS / NEW HORIZONS</div>
                    </div>
                    <div class="apx-showcase-footer">
                        <div>
                            <h4 class="apx-showcase-title">DUNIA TANPA BATAS</h4>
                            <p class="apx-showcase-sub">Dari kota <strong class="text-white">megah</strong> hingga alam liar yang belum terjamah.</p>
                        </div>
                        <div class="apx-showcase-nav">
                            <button type="button" class="btn-showcase-arrow" aria-label="Sebelumnya" title="Foto Sebelumnya">
                                <i class="bi bi-chevron-left"></i>
                            </button>
                            <button type="button" class="btn-showcase-arrow" aria-label="Berikutnya" title="Foto Berikutnya">
                                <i class="bi bi-chevron-right"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section Footnote Accents -->
        <div class="d-flex justify-content-between align-items-center pt-3 border-top border-secondary border-opacity-15 text-uppercase small flex-wrap gap-2" style="letter-spacing: 0.16em; color: var(--apx-text-dim);">
            <div>PEOPLE BUILD WORLDS &bull; WORLDS BUILD PEOPLE</div>
            <div class="d-flex align-items-center gap-2">
                <span>THE PEAK CIVILIZATIONS</span>
                <span class="apx-kicker-line" style="width: 45px;"></span>
            </div>
        </div>
    </div>
</section>

<!-- Official Rank Hierarchy Showcase (Pure Visual Artwork & Clean Showcase) -->
<section class="apx-section py-5" id="ranks" style="background: #06080d; border-top: 1px solid var(--apx-gold-border-subtle);">
    <div class="container">
        <!-- Section Header -->
        <div class="text-center mb-4">
            <div class="d-inline-flex align-items-center gap-2 mb-2">
                <span class="apx-kicker"><i class="bi bi-shield-shaded me-1"></i> STRUKTUR KEKUASAAN</span>
            </div>
            <h2 class="apx-world-title mb-2">
                HIERARKI <span class="text-gradient-gold">KASTA RESMI</span>
            </h2>
            <p class="text-muted mx-auto" style="max-width: 620px; font-size: 0.98rem;">
                Sembilan tingkatan peradaban berdaulat di Apexsions, dari warga perintis hingga sang leluhur pendiri kerajaan.
            </p>
        </div>

        <!-- 9 Ranks 3D Visual Artwork Showcase (Self-Contained Masterpiece from User) -->
        <div class="apx-rank-showcase-wrap mx-auto">
            <img src="{{ theme_asset('img/rank-hierarchy-showcase.jpg') }}" alt="Hierarki Kasta Apexsions - 9 Tingkat Kasta Resmi" class="apx-rank-showcase-img">
        </div>

        <!-- Action Call to Action -->
        <div class="text-center mt-4">
            <div class="d-flex justify-content-center align-items-center gap-3 flex-wrap">
                @if(plugins()->isEnabled('shop'))
                    <a href="{{ route('shop.home') }}" class="btn btn-apx-play px-4 py-2">
                        <i class="bi bi-crown me-2"></i> Jelajahi Kasta di Webstore <i class="bi bi-chevron-right ms-1 small"></i>
                    </a>
                @endif
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.index') }}" class="btn btn-apx-webstore px-4 py-2">
                        <i class="bi bi-journal-text me-2"></i> Panduan Lengkap Wiki <i class="bi bi-chevron-right ms-1 small"></i>
                    </a>
                @endif
            </div>
            <div class="mt-3">
                <span class="apx-trust-badge">
                    <i class="bi bi-shield-check text-warning me-1"></i> Seluruh hak istimewa &amp; donasi terintegrasi otomatis ke Minecraft 1.21.4
                </span>
            </div>
        </div>
    </div>
</section>

<!-- Getting Started Walkthrough -->
<section class="apx-section py-5" id="getting-started" style="background: rgba(10, 14, 23, 0.7); border-top: 1px solid var(--apx-gold-border-subtle);">
    <div class="container py-3">
        <div class="apx-section-header">
            <span class="apx-section-tag"><i class="bi bi-lightning-charge-fill me-1"></i> Langkah Mudah</span>
            <h2 class="apx-section-title">Cara Bergabung ke <span class="text-gradient-gold">Server</span></h2>
            <p class="text-muted mx-auto" style="max-width: 600px; font-size: 0.98rem;">Hanya membutuhkan 3 langkah sederhana untuk memulai petualangan peradaban Anda.</p>
        </div>

        <div class="row g-4 text-center">
            <div class="col-md-4">
                <div class="apx-feature-card">
                    <div class="apx-card-icon-wrap apx-icon-gold mx-auto">
                        <i class="bi bi-box-seam"></i>
                    </div>
                    <h4 class="fw-bold mb-2">1. Pasang Client</h4>
                    <p class="text-muted small mb-0">
                        Gunakan Minecraft versi <strong>1.21.4</strong> (tersedia untuk Java Edition &amp; Bedrock Edition).
                    </p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="apx-feature-card">
                    <div class="apx-card-icon-wrap apx-icon-emerald mx-auto">
                        <i class="bi bi-controller"></i>
                    </div>
                    <h4 class="fw-bold mb-2">2. Masukkan Alamat IP</h4>
                    <p class="text-muted small mb-0">
                        Ketik <code>apexsions.my.id</code> pada daftar Multiplayer. Port Bedrock: <code>19132</code>.
                    </p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="apx-feature-card">
                    <div class="apx-card-icon-wrap apx-icon-blue mx-auto">
                        <i class="bi bi-person-badge"></i>
                    </div>
                    <h4 class="fw-bold mb-2">3. Tautkan Akun Web</h4>
                    <p class="text-muted small mb-0">
                        Registrasi akun web untuk mengklaim starter reward dan kemudahan transaksi di Webstore.
                    </p>
                </div>
            </div>
        </div>
    </div>
</section>
@endsection
