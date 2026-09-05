@extends('layouts.app')

@section('title', 'Beranda')

@section('content')
<!-- Panoramic Hero Section: Viewport Adaptive (Fits 100% Player Screen at Normal Zoom) -->
<section class="apx-hero-panoramic">
    <div class="apx-hero-panoramic-bg" style="background-image: linear-gradient(90deg, rgba(5, 7, 10, 0.96) 0%, rgba(5, 7, 10, 0.85) 35%, rgba(5, 7, 10, 0.3) 70%, rgba(5, 7, 10, 0.5) 100%), linear-gradient(180deg, rgba(5, 7, 10, 0.45) 0%, transparent 40%, rgba(5, 7, 10, 0.95) 100%), url('{{ theme_asset('img/hero-panoramic.png') }}');"></div>

    <div class="container position-relative d-flex flex-column justify-content-between h-100 py-3" style="z-index: 2; flex: 1;">
        <!-- Top Sovereign Identity Mark & Hierarchy Axis -->
        <div class="pt-2 mb-2">
            <div class="apx-hero-brand-mark mb-2">
                <span>APEXSIONS &bull; THE PEAK CIVILIZATIONS</span>
            </div>
            <div class="apx-hero-hierarchy-axis" aria-label="Tingkatan Hierarki Peradaban" role="list">
                <span class="apx-axis-step" role="listitem">FONDASI</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step" role="listitem">KENAIKAN</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step" role="listitem">OTORITAS</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step apx-axis-apex" role="listitem">PUNCAK</span>
            </div>
        </div>

        <!-- Main Civilization Statement & Narrative (Asymmetric Left-Aligned Column) -->
        <div class="row my-auto py-3">
            <div class="col-xl-6 col-lg-7">
                <h1 class="apx-hero-headline">
                    Peradaban Berdaulat yang Dibangun di Atas Tatanan Hierarki.
                </h1>

                <p class="apx-hero-subtext">
                    Sebelas kasta sosial, tiga wilayah kerajaan, dan satu dunia yang dibentuk seutuhnya oleh sejarah warganya.
                </p>

                <!-- Primary Sovereign Action & Separate Technical Infrastructure -->
                <div class="apx-hero-action-group d-flex align-items-center gap-3 flex-wrap">
                    <a href="#getting-started" class="btn btn-apx-sovereign">
                        MASUK KE PERADABAN <i class="bi bi-arrow-right ms-2"></i>
                    </a>
                    <div class="apx-hero-ip-block apx-copyable" data-apx-copy="apexsions.my.id" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                        <span class="apx-ip-label">SERVER IP</span>
                        <span class="apx-ip-address font-monospace">apexsions.my.id</span>
                        <i class="bi bi-clipboard apx-ip-icon ms-1"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- Restrained Architectural Infrastructure Metadata (Demoted Telemetry) -->
        <div class="apx-hero-infrastructure">
            <!-- 1. Live Signal & Player Count -->
            <div class="apx-infra-item">
                <span class="apx-pulse-dot" id="apxLiveDot" aria-hidden="true"></span>
                <span class="apx-infra-status" id="apxLiveBadge">SERVER ONLINE</span>
                <span class="apx-infra-divider">/</span>
                <span class="apx-infra-val"><span id="apxOnlinePlayers">0</span> / <span id="apxMaxPlayers">500</span> Warga</span>
            </div>

            <!-- 2. Java Server IP (Click to copy) -->
            <div class="apx-infra-item apx-copyable" data-apx-copy="apexsions.my.id" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                <span class="apx-infra-label">JAVA</span>
                <span class="apx-infra-val font-monospace">apexsions.my.id</span>
            </div>

            <!-- 3. Bedrock Port & Version (Click to copy) -->
            <div class="apx-infra-item apx-copyable" data-apx-copy="19132" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin Port Bedrock" aria-label="Salin Port Server Bedrock">
                <span class="apx-infra-label">BEDROCK</span>
                <span class="apx-infra-val font-monospace">19132</span>
                <span class="apx-infra-meta">&bull; v<span id="apxVersion">1.21.4</span></span>
            </div>
        </div>
    </div>
</section>

<!-- Section: A WORLD BUILT BY PLAYERS (2-Column Asymmetric Showcase) -->
<section class="apx-section apx-world-section position-relative overflow-hidden" id="features">

    <div class="container position-relative" style="z-index: 2;">
        <!-- Header / Intro -->
        <div class="apx-section-intro mb-5">
            <div class="d-flex align-items-center gap-3 mb-2">
                <span class="apx-section-kicker">TATANAN WILAYAH</span>
            </div>
            <h2 class="apx-world-title mb-3">
                Tiga Kerajaan Berdaulat dalam Satu Realm
            </h2>
            <p class="apx-world-lead text-muted" style="max-width: 680px; font-size: 1.05rem; line-height: 1.7;">
                Apexsions terbagi ke dalam tiga kedaulatan kerajaan otonom dengan spesialisasi wilayah masing-masing, dipersatukan oleh sistem ekonomi pasar terbuka dan perebutan supremasi pada Kingdom War.
            </p>
        </div>

        <!-- The Three Sovereign Territories Triptych -->
        <div class="apx-kingdoms-triptych mb-4">
            <!-- 1. Zenithar -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type">TERITORI UTARA</span>
                    <h3 class="apx-province-name">Zenithar</h3>
                </div>
                <p class="apx-province-desc">
                    Wilayah dataran tinggi dan jurang cadas. Sentra penambangan mineral langka, obsidian, dan benteng pertambangan peradaban.
                </p>
                <ul class="apx-province-specs">
                    <li><i class="bi bi-geo-alt text-dim"></i> Dataran Tinggi &amp; Puncak Tebing</li>
                    <li><i class="bi bi-gem text-dim"></i> Sentra Mineral &amp; Metalurgi</li>
                </ul>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        Arsip Zenithar <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>

            <!-- 2. Solterra -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type">TERITORI SELATAN</span>
                    <h3 class="apx-province-name">Solterra</h3>
                </div>
                <p class="apx-province-desc">
                    Wilayah batuan vulkanik dan kawah lahar. Pusat komando militer, penempaan senjata berat, dan arena supremasi Kingdom War.
                </p>
                <ul class="apx-province-specs">
                    <li><i class="bi bi-geo-alt text-dim"></i> Cadas Vulkanik &amp; Gurun Api</li>
                    <li><i class="bi bi-shield-shaded text-dim"></i> Komando Militer &amp; Tempur</li>
                </ul>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        Arsip Solterra <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>

            <!-- 3. Sylvamoor -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type">TERITORI TIMUR</span>
                    <h3 class="apx-province-name">Sylvamoor</h3>
                </div>
                <p class="apx-province-desc">
                    Wilayah lembah subur, hutan kanopi purba, dan jalur sungai perdagangan. Lumbung agrikultur dan pasokan pangan seluruh realm.
                </p>
                <ul class="apx-province-specs">
                    <li><i class="bi bi-geo-alt text-dim"></i> Lembah Hijau &amp; Kanopi Hutan</li>
                    <li><i class="bi bi-flower1 text-dim"></i> Lumbung Agrikultur &amp; Niaga</li>
                </ul>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        Arsip Sylvamoor <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>
        </div>

        <!-- 2-Column Showcase Row: Civil Systems & Territorial Archive -->
        <div class="row g-4 align-items-stretch mb-5">
            <!-- Left Column: Civil Systems Ledger -->
            <div class="col-lg-6">
                <div class="apx-monolith-ledger h-100 d-flex flex-column justify-content-between">
                    <!-- Row 1: Perjanjian Kedaulatan -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title">Perjanjian Kedaulatan &amp; Kingdom War</h4>
                            <span class="text-dim small font-monospace">DEKRIT REALM</span>
                        </div>
                        <p class="apx-ledger-desc">
                            Setiap warga bebas memilih baiat kepada satu kerajaan. Pertahankan perbatasan teritori, bangun benteng pertahanan, dan rebut supremasi pada Kingdom War mingguan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link">
                                Dekrit Kedaulatan Kerajaan <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @endif
                    </div>

                    <!-- Row 2: Ekonomi -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title">Ekonomi Pasar Terbuka &amp; Escrow</h4>
                            <span class="text-dim small font-monospace">DUAL-CURRENCY</span>
                        </div>
                        <p class="apx-ledger-desc">
                            Sistem transaksi ganda Rupiah (Rp) dan Diamond 💎. Kuasai pasar dinamis berbasis supply-demand (/shop), lelang aman Escrow, dan pertukaran barter langsung lintas kerajaan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'ekonomi-perdagangan') }}" class="apx-ledger-link">
                                Mekanisme Pasar &amp; Escrow <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @elseif(plugins()->isEnabled('shop'))
                            <a href="{{ route('shop.home') }}" class="apx-ledger-link">
                                Kunjungi Toko Peradaban <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @endif
                    </div>

                    <!-- Row 3: Enchants & Kits -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title">Sistem Penempaan &amp; 182 Enchants</h4>
                            <span class="text-dim small font-monospace">7 TIER KEKUATAN</span>
                        </div>
                        <p class="apx-ledger-desc">
                            Tujuh tingkatan custom enchants melalui Alchemist dan Tinkerer, dipadukan dengan set bonus armor legendaris yang seimbang dan teruji untuk pertempuran kedaulatan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'enchants-dan-kits') }}" class="apx-ledger-link">
                                Ensiklopedia Penempaan <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @endif
                    </div>
                </div>
            </div>

            <!-- Right Column: Photographic Realm Showcase Card -->
            <div class="col-lg-6">
                <div class="apx-showcase-box">
                    <div class="apx-showcase-media">
                        <img src="{{ theme_asset('img/realm-showcase.jpg') }}" alt="Panorama Bentang Alam Wilayah Kekaisaran Apexsions" class="apx-showcase-img" loading="lazy" decoding="async" width="800" height="450">
                        <div class="apx-showcase-overlay"></div>
                        <div class="apx-showcase-tag">ARSIP WILAYAH REALM</div>
                    </div>
                    <div class="apx-showcase-footer">
                        <div>
                            <h4 class="apx-showcase-title">BENTANG ALAM PERADABAN</h4>
                            <p class="apx-showcase-sub">Dari benteng ibukota yang kokoh hingga hamparan alam liar yang menanti untuk ditaklukkan.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section Footnote Accents -->
        <div class="d-flex justify-content-between align-items-center pt-3 border-top border-secondary border-opacity-15 text-uppercase small flex-wrap gap-2" style="letter-spacing: 0.16em; color: var(--apx-text-dim);">
            <div>CIVILIZATIONS RISE BY DECREE &bull; FORGED BY CITIZENS</div>
            <div>THE PEAK CIVILIZATIONS</div>
        </div>
    </div>
</section>

<!-- Official Rank Hierarchy Showcase (Pure Visual Artwork & Clean Showcase) -->
<section class="apx-section py-5" id="ranks" style="background: var(--apx-bg-deep); border-top: 1px solid var(--apx-gold-border-subtle);">
    <div class="container">
        <!-- Section Header -->
        <div class="text-center mb-5">
            <div class="apx-section-kicker mb-2">TATANAN SOSIAL</div>
            <h2 class="apx-world-title mb-2">
                Tatanan Sebelas Kasta Sosial
            </h2>
            <p class="text-muted mx-auto" style="max-width: 680px; font-size: 1rem; line-height: 1.7;">
                Dari pijakan awal warga perintis hingga tahta tertinggi sang leluhur pendiri kerajaan, setiap kasta memikul bobot kedaulatan, hak wilayah, dan kehormatan yang terukur.
            </p>
        </div>

        <!-- Interactive Strata Filter Navigation -->
        <div class="apx-caste-nav" role="tablist" aria-label="Filter Hierarki Kasta">
            <button type="button" class="apx-caste-filter-btn active" data-rank-filter="all" role="tab" aria-selected="true">SEMUA KASTA (11)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="authority" role="tab" aria-selected="false">DEWAN OTORITAS (5)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="nobles" role="tab" aria-selected="false">ORDO BANGSAWAN (5)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="pioneers" role="tab" aria-selected="false">WARGA PERINTIS (1)</button>
        </div>

        <!-- The Architectural Social Ladder -->
        <div class="apx-social-ladder">

            <!-- TIER IV: PUNCAK KEDAULATAN (THE APEX) -->
            <div class="apx-ladder-tier apx-tier-apex-wrapper" data-rank-category="authority">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman">TINGKAT IV</span>
                    <span class="apx-tier-name">PUNCAK KEDAULATAN &bull; THE APEX</span>
                    <span class="apx-weight-pill gold">WEIGHT 100</span>
                </div>
                <div class="apx-caste-card apx-card-apex" data-rank-category="authority">
                    <img src="{{ theme_asset('img/ranks/rank-ancestor.png') }}" alt="Banner Resmi Kasta The Ancestor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                    <div class="row align-items-center gy-3">
                        <div class="col-lg-4 col-md-5">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="apx-caste-tier-badge text-gold">DEWAN OTORITAS</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">The Ancestor</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-crown"></i> ✦ ANCESTOR ✦</div>
                            <span class="text-dim small">Pendiri Peradaban &amp; Tahta Tertinggi</span>
                        </div>
                        <div class="col-lg-5 col-md-7">
                            <p class="apx-caste-desc mb-2">
                                Sang leluhur agung dan pendiri peradaban. Pemegang mandat tertinggi atas hukum, arsitektur, dan kedaulatan seluruh realm Apexsions.
                            </p>
                            <ul class="apx-caste-perks mb-0">
                                <li><i class="bi bi-check2"></i> Mandat Tertinggi Dekrit Kerajaan</li>
                                <li><i class="bi bi-check2"></i> Kedaulatan Mutlak Seluruh Realm</li>
                                <li><i class="bi bi-check2"></i> Mahkota Segel Leluhur Abadi</li>
                            </ul>
                        </div>
                        <div class="col-lg-3 text-lg-end">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm">
                                    Pelajari Mandat <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-gold small font-monospace">TAHTA TERTINGGI</span>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER III: DEWAN OTORITAS (COUNCIL OF AUTHORITY) -->
            <div class="apx-ladder-tier apx-tier-authority-wrapper" data-rank-category="authority">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman">TINGKAT III</span>
                    <span class="apx-tier-name">DEWAN OTORITAS &bull; AUTHORITY</span>
                    <span class="apx-weight-pill">WEIGHT 95 &bull; 92 &bull; 90 &bull; 80</span>
                </div>
                <div class="apx-tier-authority-grid">
                    <!-- 2. Architect -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-architect.png') }}" alt="Banner Resmi Kasta Architect" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-purple">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 95</span>
                            </div>
                            <h3 class="apx-caste-name">Architect</h3>
                            <div class="apx-caste-prefix text-purple"><i class="bi bi-compass-fill"></i> 📐 ARCHITECT</div>
                            <p class="apx-caste-desc">
                                Perancang tata ruang dan pembangun peradaban. Mengatur cetak biru arsitektur realm, struktur kota, dan batas kedaulatan wilayah.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Otoritas Cetak Biru &amp; Pembangunan Realm</li>
                                <li><i class="bi bi-check2"></i> Hak Pengawasan Tata Ruang &amp; Konstruksi</li>
                                <li><i class="bi bi-check2"></i> Akses Konsol Kreatif &amp; Inspeksi Properti</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    Pelajari Peran <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;">ARSITEK REALM</span>
                            @endif
                        </div>
                    </div>

                    <!-- 3. Overseer -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-overseer.png') }}" alt="Banner Resmi Kasta Overseer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="81">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-gold">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 92</span>
                            </div>
                            <h3 class="apx-caste-name">Overseer</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-eye-fill"></i> 👁 OVERSEER</div>
                            <p class="apx-caste-desc">
                                Mata pengawas kedaulatan dan kestabilan dunia. Memantau integritas transaksi ekonomi, kepatuhan peradaban, dan keseimbangan realm.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Audit Kedaulatan, Transaksi &amp; Keadilan</li>
                                <li><i class="bi bi-check2"></i> Pemantauan Dinamika Pasar &amp; Kas Kerajaan</li>
                                <li><i class="bi bi-check2"></i> Akses Meja Investigasi &amp; Log Peradaban</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    Pelajari Peran <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;">PENGAWAS REALM</span>
                            @endif
                        </div>
                    </div>

                    <!-- 4. Warden -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-warden.png') }}" alt="Banner Resmi Kasta Warden" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-blue">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 90</span>
                            </div>
                            <h3 class="apx-caste-name">Warden</h3>
                            <div class="apx-caste-prefix text-blue"><i class="bi bi-shield-shaded"></i> ⚔ WARDEN</div>
                            <p class="apx-caste-desc">
                                Penjaga gerbang utama dan kepala staf peradaban. Mengawasi kestabilan wilayah, tribunal keadilan, dan ketertiban hukum dunia.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Otoritas Tribunal &amp; Keamanan Realm</li>
                                <li><i class="bi bi-check2"></i> Pengawasan Integritas Transaksi &amp; War</li>
                                <li><i class="bi bi-check2"></i> Hak Inspeksi Wilayah Berdaulat</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    Pelajari Peran <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;">KEPALA PENGAWAS</span>
                            @endif
                        </div>
                    </div>

                    <!-- 5. Herald -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-herald.png') }}" alt="Banner Resmi Kasta Herald" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-pink">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 80</span>
                            </div>
                            <h3 class="apx-caste-name">Herald</h3>
                            <div class="apx-caste-prefix text-pink"><i class="bi bi-chat-quote"></i> 📜 HERALD</div>
                            <p class="apx-caste-desc">
                                Utusan resmi dan pembawa maklumat peradaban. Menjembatani suara warga dengan dewan penguasa dan menjaga diplomasi publik.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Moderasi &amp; Diplomasi Publik</li>
                                <li><i class="bi bi-check2"></i> Penegakan Etika Peradaban</li>
                                <li><i class="bi bi-check2"></i> Akses Meja Laporan Warga</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    Pelajari Peran <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;">UTUSAN RESMI</span>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER II: ORDO BANGSAWAN (THE NOBLE ASCENSION) -->
            <div class="apx-ladder-tier apx-tier-nobles-wrapper" data-rank-category="nobles">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman">TINGKAT II</span>
                    <span class="apx-tier-name">ORDO BANGSAWAN &bull; THE ASCENSION</span>
                    <span class="apx-weight-pill">WEIGHT 70 &bull; 60 &bull; 50 &bull; 40 &bull; 30</span>
                </div>
                <div class="apx-tier-ascension-grid">
                    <!-- 6. Sions -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-sions.png') }}" alt="Banner Resmi Kasta Sions" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="132">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-gold">APEX NOBLE</span>
                                <span class="apx-caste-weight">WEIGHT 70</span>
                            </div>
                            <h3 class="apx-caste-name">Sions</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-star-fill"></i> ✦ SIONS ✦</div>
                            <p class="apx-caste-desc">
                                Kasta bangsawan puncak peradaban. Pilar kemakmuran tertinggi realm dengan keistimewaan absolut.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> +15 Batas Klaim Wilayah</li>
                                <li><i class="bi bi-check2"></i> Kit Sions Eksklusif</li>
                                <li><i class="bi bi-check2"></i> Siaran Kedatangan Megah</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    Webstore <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 7. Emperor -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-emperor.png') }}" alt="Banner Resmi Kasta Emperor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge">DONATUR TIER 4</span>
                                <span class="apx-caste-weight">WEIGHT 60</span>
                            </div>
                            <h3 class="apx-caste-name">Emperor</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-gem"></i> [⚔ EMPEROR]</div>
                            <p class="apx-caste-desc">
                                Bangsawan penakluk berwibawa tinggi. Penguasa langit dengan hak terbang di wilayah klaim.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Hak Terbang /fly di Wilayah</li>
                                <li><i class="bi bi-check2"></i> +10 Batas Klaim Wilayah</li>
                                <li><i class="bi bi-check2"></i> Kit Bulanan Gear Lengkap</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    Webstore <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 8. Sovereign -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-sovereign.png') }}" alt="Banner Resmi Kasta Sovereign" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge">DONATUR TIER 3</span>
                                <span class="apx-caste-weight">WEIGHT 50</span>
                            </div>
                            <h3 class="apx-caste-name">Sovereign</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-feather"></i> [⚜ SOVEREIGN]</div>
                            <p class="apx-caste-desc">
                                Tuan tanah emas peradaban. Menguasai jalur niaga bebas tarif dagang lintas kerajaan.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Bebas Tarif Dagang Wilayah</li>
                                <li><i class="bi bi-check2"></i> +7 Batas Klaim Wilayah</li>
                                <li><i class="bi bi-check2"></i> Kit Sovereign 14 Harian</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    Webstore <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 9. Archon -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-archon.png') }}" alt="Banner Resmi Kasta Archon" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="87">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge">DONATUR TIER 2</span>
                                <span class="apx-caste-weight">WEIGHT 40</span>
                            </div>
                            <h3 class="apx-caste-name">Archon</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-lightning-charge"></i> [💎 ARCHON]</div>
                            <p class="apx-caste-desc">
                                Kaum perajin kristal dan cendekiawan realm. Menikmati utilitas workbench portabel di mana saja.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Akses /ec, /anvil Portabel</li>
                                <li><i class="bi bi-check2"></i> +4 Batas Klaim Wilayah</li>
                                <li><i class="bi bi-check2"></i> Kit Mingguan &amp; Kosmetik</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    Webstore <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 10. Ascendant -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-ascendant.png') }}" alt="Banner Resmi Kasta Ascendant" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge">DONATUR TIER 1</span>
                                <span class="apx-caste-weight">WEIGHT 30</span>
                            </div>
                            <h3 class="apx-caste-name">Ascendant</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-flower1"></i> [☘ ASCENDANT]</div>
                            <p class="apx-caste-desc">
                                Warga terhormat yang membuktikan dedikasinya. Prioritas antrean masuk dan perbekalan harian.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> Bypass Antrean Server</li>
                                <li><i class="bi bi-check2"></i> +2 Batas Klaim Wilayah</li>
                                <li><i class="bi bi-check2"></i> Kit Ascendant Harian</li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    Webstore <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER I: FONDASI PERADABAN (THE FOUNDATION) -->
            <div class="apx-ladder-tier apx-tier-foundation-wrapper" data-rank-category="pioneers">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman">TINGKAT I</span>
                    <span class="apx-tier-name">FONDASI PERADABAN &bull; THE FOUNDATION</span>
                    <span class="apx-weight-pill">WEIGHT 10</span>
                </div>
                <div class="apx-caste-card apx-card-foundation" data-rank-category="pioneers">
                    <img src="{{ theme_asset('img/ranks/rank-wanderer.png') }}" alt="Banner Resmi Kasta Wanderer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="88">
                    <div class="row align-items-center gy-3">
                        <div class="col-lg-4 col-md-5">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="apx-caste-tier-badge">WARGA PERINTIS</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Wanderer</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-compass"></i> WANDERER</div>
                            <span class="text-dim small">Pijakan Awal Seluruh Warga Baru</span>
                        </div>
                        <div class="col-lg-5 col-md-7">
                            <p class="apx-caste-desc mb-2">
                                Fondasi dan jiwa peradaban Apexsions. Setiap legenda dimulai dari warga baru yang berani menancapkan pijakan pertama di alam liar.
                            </p>
                            <ul class="apx-caste-perks mb-0">
                                <li><i class="bi bi-check2"></i> Akses Penuh ke Tiga Kerajaan</li>
                                <li><i class="bi bi-check2"></i> Partisipasi Pasar &amp; Lelang Escrow</li>
                                <li><i class="bi bi-check2"></i> Batas Wilayah Awal &amp; Progresi Bebas</li>
                            </ul>
                        </div>
                        <div class="col-lg-3 text-lg-end">
                            <a href="#getting-started" class="btn btn-apx-outline btn-sm">
                                Cara Bergabung <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- Section Action Footer -->
        <div class="text-center mt-5">
            <div class="d-flex justify-content-center align-items-center gap-3 flex-wrap">
                @if(plugins()->isEnabled('shop'))
                    <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline px-4 py-2">
                        <i class="bi bi-crown me-2"></i> Jelajahi Kasta di Webstore
                    </a>
                @endif
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline px-4 py-2">
                        <i class="bi bi-journal-text me-2"></i> Panduan Lengkap Kasta
                    </a>
                @endif
            </div>
        </div>
    </div>
</section>

<!-- Connected Rite of Passage (Onboarding Stepper Section) -->
<section class="apx-section py-5" id="getting-started" style="background: var(--apx-bg-base); border-top: 1px solid var(--apx-border);">
    <div class="container py-3">
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2">GERBANG INISIASI</div>
            <h2 class="apx-section-title">Tata Cara Masuk ke Peradaban</h2>
            <p class="text-muted mx-auto" style="max-width: 620px; font-size: 1rem; line-height: 1.7;">
                Tiga langkah sederhana untuk menghubungkan klien Minecraft dan mencatatkan namamu dalam sejarah warga berdaulat.
            </p>
        </div>

        <!-- Connected 3-Step Monolith Stepper -->
        <div class="apx-stepper-grid">
            <!-- Step 1: Pasang Klien -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3">TAHAP PERTAMA</div>
                <h4 class="apx-step-title">Klien Minecraft 1.21.4</h4>
                <p class="apx-step-desc">
                    Gunakan Minecraft versi resmi atau launcher pilihanmu pada versi <strong>1.21.4</strong>. Mendukung penuh koneksi <strong>Java Edition &amp; Bedrock Edition</strong>.
                </p>
                <div class="apx-step-action mt-auto">
                    <span class="text-dim small">Java &amp; Bedrock Crossplay</span>
                </div>
            </div>

            <!-- Step 2: Tembus Gerbang (Salin IP) -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3">TAHAP KEDUA</div>
                <h4 class="apx-step-title">Alamat Server &amp; Port</h4>
                <p class="apx-step-desc">
                    Buka menu Multiplayer dan masukkan alamat server <code>apexsions.my.id</code>. Untuk pemain Bedrock, hubungkan melalui Port <code>19132</code>.
                </p>
                <div class="apx-step-action mt-auto">
                    <button type="button" class="btn btn-apx-outline btn-sm w-100 py-2" data-apx-copy="apexsions.my.id" aria-label="Salin Alamat Server">
                        <i class="bi bi-clipboard me-1"></i> Salin IP &bull; apexsions.my.id
                    </button>
                </div>
            </div>

            <!-- Step 3: Ikrar Peradaban -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3">TAHAP KETIGA</div>
                <h4 class="apx-step-title">Autentikasi Akun (/link)</h4>
                <p class="apx-step-desc">
                    Setelah berada di lobi server, ketik perintah <code>/link</code> untuk menerima kode autentikasi rahasia guna menautkan akun dengan portal web.
                </p>
                <div class="apx-step-action mt-auto">
                    @auth
                        @if(plugins()->isEnabled('apexsions-bridge'))
                            <a href="{{ route('apexsions-bridge.link.index') }}" class="btn btn-apx-outline btn-sm w-100 py-2">
                                <i class="bi bi-controller me-1"></i> Buka Portal Tautkan
                            </a>
                        @else
                            <span class="text-dim small">Akun Anda Terdaftar</span>
                        @endif
                    @else
                        <a href="{{ route('register') }}" class="btn btn-apx-outline btn-sm w-100 py-2">
                            <i class="bi bi-person-plus me-1"></i> Daftar Akun Peradaban
                        </a>
                    @endauth
                </div>
            </div>
        </div>
    </div>
</section>
@endsection
