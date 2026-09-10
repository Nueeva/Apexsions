@extends('layouts.app')

@section('title', 'Apexsions - Minecraft Survival & Kingdom Server Indonesia')
@section('description', 'Bergabunglah di Apexsions, server Minecraft Indonesia dengan sistem Survival, Kingdom, BattlePass, Economy, Auction, Custom Enchants, Crates, dan dunia yang terus berkembang.')

@section('content')
<!-- Panoramic Hero Section: Viewport Adaptive (Fits 100% Player Screen at Normal Zoom) -->
<section class="apx-hero-panoramic">
    <div class="apx-hero-panoramic-bg" style="background-image: linear-gradient(90deg, rgba(5, 7, 10, 0.96) 0%, rgba(5, 7, 10, 0.85) 35%, rgba(5, 7, 10, 0.3) 70%, rgba(5, 7, 10, 0.5) 100%), linear-gradient(180deg, rgba(5, 7, 10, 0.45) 0%, transparent 40%, rgba(5, 7, 10, 0.95) 100%), url('{{ theme_asset('img/hero-panoramic.png') }}');"></div>

    <div class="container position-relative d-flex flex-column justify-content-between h-100 py-3" style="z-index: 2; flex: 1;">
        <!-- Top Sovereign Identity Mark & Hierarchy Axis -->
        <div class="pt-2 mb-2">
            <div class="apx-hero-brand-mark mb-2">
                <span data-i18n="hero_brand_mark">APEXSIONS &bull; THE PEAK CIVILIZATIONS</span>
            </div>
            <div class="apx-hero-hierarchy-axis" aria-label="Tingkatan Hierarki Peradaban" role="list">
                <span class="apx-axis-step" role="listitem" data-i18n="hero_axis_foundation">FONDASI</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step" role="listitem" data-i18n="hero_axis_nobles">BANGSAWAN</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step" role="listitem" data-i18n="hero_axis_admin">ADMINISTRASI</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step" role="listitem" data-i18n="hero_axis_authority">OTORITAS</span>
                <span class="apx-axis-arrow" aria-hidden="true">→</span>
                <span class="apx-axis-step apx-axis-apex" role="listitem" data-i18n="hero_axis_apex">PUNCAK</span>
            </div>
        </div>

        <!-- Main Civilization Statement & Narrative (Asymmetric Left-Aligned Column) -->
        <div class="row my-auto py-3">
            <div class="col-xl-6 col-lg-7">
                <h1 class="apx-hero-headline" data-i18n="hero_headline">
                    Peradaban Berdaulat yang Dibangun di Atas Tatanan Hierarki.
                </h1>

                <p class="apx-hero-subtext" data-i18n="hero_subtext">
                    Sebelas kasta sosial, tiga wilayah kerajaan, dan satu dunia yang dibentuk seutuhnya oleh sejarah warganya.
                </p>

                <!-- Primary Sovereign Action & Separate Technical Infrastructure -->
                <div class="apx-hero-action-group d-flex align-items-center gap-3 flex-wrap">
                    <a href="#features" class="btn btn-apx-sovereign">
                        <span data-i18n="hero_btn_explore">JELAJAHI PERADABAN</span> <i class="bi bi-arrow-down ms-2"></i>
                    </a>
                    <div class="apx-hero-ip-block apx-copyable" data-apx-copy="apexsions.my.id:32348" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                        <span class="apx-ip-label" data-i18n="hero_ip_label">SERVER IP</span>
                        <span class="apx-ip-address font-monospace">apexsions.my.id:32348</span>
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
                <span class="apx-infra-status" id="apxLiveBadge" data-i18n="hero_status_online">SERVER ONLINE</span>
                <span class="apx-infra-divider">/</span>
                <span class="apx-infra-val" id="apxPlayerCountContainer"><span id="apxPlayerStatusText" data-i18n="hero_status_ready">Gerbang Terbuka &bull; Siap Menjelajah</span><span id="apxPlayerNumbers" class="d-none"><span id="apxOnlinePlayers">0</span> / <span id="apxMaxPlayers">200</span> <span data-i18n="hero_citizens">Warga</span></span></span>
            </div>

            <!-- 2. Java Server IP (Click to copy) -->
            <div class="apx-infra-item apx-copyable" data-apx-copy="apexsions.my.id:32348" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                <span class="apx-infra-label">JAVA</span>
                <span class="apx-infra-val font-monospace">apexsions.my.id:32348</span>
            </div>

            <!-- 3. Bedrock IP, Port & Version (Click to copy) -->
            <div class="apx-infra-item apx-copyable" data-apx-copy="IP: apexsions.my.id | Port: 32348" role="button" tabindex="0" title="Klik atau tekan Enter untuk menyalin IP dan Port Bedrock" aria-label="Salin IP dan Port Server Bedrock">
                <span class="apx-infra-label">BEDROCK</span>
                <span class="apx-infra-val font-monospace">IP: apexsions.my.id &bull; Port: 32348</span>
                <span class="apx-infra-meta">&bull; v<span id="apxVersion">26.2</span></span>
            </div>
        </div>
    </div>
</section>

<!-- Section: A WORLD BUILT BY PLAYERS (2-Column Asymmetric Showcase) -->
<section class="apx-section apx-world-section position-relative overflow-hidden" id="civilizations">
    <span id="features" style="position: absolute; top: -80px;"></span>

    <div class="container position-relative" style="z-index: 2;">
        <!-- Header / Intro -->
        <div class="apx-section-intro mb-5">
            <div class="d-flex align-items-center gap-3 mb-2">
                <span class="apx-section-kicker" data-i18n="world_kicker">LORE &amp; TATANAN WILAYAH</span>
            </div>
            <h2 class="apx-world-title mb-3" data-i18n="world_title">
                Runtuhnya Kekaisaran Sions &amp; Eksodus Tiga Kerajaan
            </h2>
            <p class="apx-world-lead text-muted" style="max-width: 820px; font-size: 1.05rem; line-height: 1.75;" data-i18n-html="world_lead">
                Dahulu kala, satu peradaban tunggal yang mahaluas—<strong>Kekaisaran Sions</strong>—menaungi seluruh penjuru realm dalam kemakmuran tanpa pernah menyentuh ilmu hitam. Namun kehancuran tak terelakkan tiba ketika ambisi pemimpinnya memaksakan penggunaan energi terlarang dari <strong>Dimensi Kegelapan (Dark Dimension)</strong> demi melipatgandakan kekuatan pasukan. Kekuatan gelap yang tak terkendali meremukkan ibukota agung kekaisaran dan memicu <em>Eksodus Akbar</em> ke tiga penjuru mata angin: dinasti kerajaan melarikan diri ke timur (<strong>Zenithar</strong>), kaum pekerja dan prajurit bertahan hidup ke rimba barat (<strong>Sylvamoor</strong>), serta para pesulap agung dan prajurit tempur terkuat hijrah ke cadas selatan (<strong>Solterra</strong>).
            </p>
        </div>

        <!-- The Three Sovereign Territories Triptych -->
        <div class="apx-kingdoms-triptych mb-4">
            <!-- 1. Zenithar -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type" style="color: #f6e58d;"><i class="bi bi-compass me-1"></i> <span data-i18n="zenithar_type">TERITORI TIMUR &bull; DINASTI KERAJAAN</span></span>
                    <h3 class="apx-province-name" style="color: #ffeaa7;">Zenithar</h3>
                </div>
                <p class="apx-province-desc" data-i18n="zenithar_desc">
                    Dipimpin oleh keluarga dinasti dan pengawal elit Kekaisaran Sions yang berhasil mempertahankan diri dari malapetaka, mengungsi ke timur menuju puncak cakrawala (Zenith). Menjunjung tinggi kemurnian tata krama istana, kavaleri suci, dan kubah menara langit.
                </p>
                <div class="mb-3">
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: var(--apx-gold-light);"><i class="bi bi-geo-alt me-1"></i> <span data-i18n="label_characteristics">Karakteristik Wilayah</span></div>
                    <ul class="apx-province-specs mb-2">
                        <li><i class="bi bi-bank text-dim"></i> <span data-i18n="zenithar_spec1">Puncak Cakrawala &amp; Solarium Spire Citadel</span></li>
                        <li><i class="bi bi-gem text-dim"></i> <span data-i18n="zenithar_spec2">Penambangan Emas Murni &amp; Arsitektur Megah</span></li>
                    </ul>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #2ecc71;"><i class="bi bi-shield-plus me-1"></i> <span data-i18n="label_buffs">Buff Kondisi Sejarah</span></div>
                    <div class="small text-muted mb-2" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="zenithar_buffs">
                        <span class="text-success fw-semibold">+5% Speed</span>, <span class="text-success fw-semibold">+7% Luck</span>, <span class="text-success fw-semibold">+6% All Damage &amp; Defense</span>, <span class="text-success fw-semibold">-5% Reduksi Serangan Kritis Musuh</span>.
                    </div>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #e74c3c;"><i class="bi bi-shield-minus me-1"></i> <span data-i18n="label_debuffs">Debuff Kondisi Fisik</span></div>
                    <div class="small text-muted" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="zenithar_debuffs">
                        <span class="text-danger fw-semibold">+7% Kerentanan Racun</span>, makanan memulihkan hunger lebih lambat (-1 point) karena terbiasa dengan santapan istana steril.
                    </div>
                </div>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        <span data-i18n="zenithar_link">Arsip Zenithar</span> <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>

            <!-- 2. Solterra -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type" style="color: #ff7675;"><i class="bi bi-compass me-1"></i> <span data-i18n="solterra_type">TERITORI SELATAN &bull; MAGICIAN &amp; VETERAN</span></span>
                    <h3 class="apx-province-name" style="color: #ff7675;">Solterra</h3>
                </div>
                <p class="apx-province-desc" data-i18n="solterra_desc">
                    Dibentuk oleh para pesulap tempur agung (arcanists) serta prajurit garis depan terkuat bekas legiun Sions yang bermigrasi ke wilayah selatan. Memadukan kedahsyatan sihir elemen api dengan kekuatan fisik brutal tanpa ampun untuk menaklukkan alam yang mematikan.
                </p>
                <div class="mb-3">
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #e17055;"><i class="bi bi-geo-alt me-1"></i> <span data-i18n="label_characteristics">Karakteristik Wilayah</span></div>
                    <ul class="apx-province-specs mb-2">
                        <li><i class="bi bi-fire text-dim"></i> <span data-i18n="solterra_spec1">Cadas Vulkanik, Kawah Lahar &amp; Ignis Bastion</span></li>
                        <li><i class="bi bi-shield-shaded text-dim"></i> <span data-i18n="solterra_spec2">Penempaan Senjata Berat &amp; Nilai Jual Ore Tinggi</span></li>
                    </ul>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #2ecc71;"><i class="bi bi-shield-plus me-1"></i> <span data-i18n="label_buffs">Buff Kondisi Sejarah</span></div>
                    <div class="small text-muted mb-2" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="solterra_buffs">
                        <span class="text-success fw-semibold">+15% Total Damage</span>, <span class="text-success fw-semibold">+10% Critical Damage</span>, <span class="text-success fw-semibold">+10% Mining Speed</span>, <span class="text-success fw-semibold">Rasio Jual Ore Tinggi (65%)</span>.
                    </div>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #e74c3c;"><i class="bi bi-shield-minus me-1"></i> <span data-i18n="label_debuffs">Debuff Kondisi Fisik</span></div>
                    <div class="small text-muted" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="solterra_debuffs">
                        <span class="text-danger fw-semibold">-2 HP Maksimal (9 Hati)</span>, <span class="text-danger fw-semibold">+8% Damage Masuk</span>, <span class="text-danger fw-semibold">+7% Cepat Lapar</span>, lahan pertanian cepat kering di tanah cadas.
                    </div>
                </div>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        <span data-i18n="solterra_link">Arsip Solterra</span> <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>

            <!-- 3. Sylvamoor -->
            <div class="apx-kingdom-province">
                <div class="apx-province-header">
                    <span class="apx-province-type" style="color: #74b9ff;"><i class="bi bi-compass me-1"></i> <span data-i18n="sylvamoor_type">TERITORI BARAT &bull; PEKERJA &amp; PEJUANG RIMBA</span></span>
                    <h3 class="apx-province-name" style="color: #74b9ff;">Sylvamoor</h3>
                </div>
                <p class="apx-province-desc" data-i18n="sylvamoor_desc">
                    Dibangun oleh kaum pekerja, pembangun, petani lumbung, serta prajurit garda rakyat (pengguna sihir alam dasar dan prajurit non-sihir) yang mengungsi ke belantara rimba barat. Mengisolasi diri dari ambisi kekuasaan dan hidup selaras menjaga kelestarian Pohon Dunia.
                </p>
                <div class="mb-3">
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #0984e3;"><i class="bi bi-geo-alt me-1"></i> <span data-i18n="label_characteristics">Karakteristik Wilayah</span></div>
                    <ul class="apx-province-specs mb-2">
                        <li><i class="bi bi-tree text-dim"></i> <span data-i18n="sylvamoor_spec1">Rimba Kanopi Purba, Samudra Kristal &amp; Eldergrove</span></li>
                        <li><i class="bi bi-flower1 text-dim"></i> <span data-i18n="sylvamoor_spec2">Lumbung Agrikultur, Alkemis Herbal &amp; Pangan</span></li>
                    </ul>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #2ecc71;"><i class="bi bi-shield-plus me-1"></i> <span data-i18n="label_buffs">Buff Kondisi Sejarah</span></div>
                    <div class="small text-muted mb-2" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="sylvamoor_buffs">
                        <span class="text-success fw-semibold">+2 HP Maksimal (11 Hati)</span>, <span class="text-success fw-semibold">+12% Luck</span>, <span class="text-success fw-semibold">+7% Extra Mob Drop</span>, <span class="text-success fw-semibold">Defense Tinggi (~12.6%)</span>, <span class="text-success fw-semibold">Kelembapan Lahan Abadi</span>.
                    </div>
                    <div class="small fw-bold text-uppercase mb-1" style="font-size: 0.72rem; letter-spacing: 0.1em; color: #e74c3c;"><i class="bi bi-shield-minus me-1"></i> <span data-i18n="label_debuffs">Debuff Kondisi Fisik</span></div>
                    <div class="small text-muted" style="font-size: 0.8rem; line-height: 1.5;" data-i18n-html="sylvamoor_debuffs">
                        <span class="text-danger fw-semibold">Mabuk Ketinggian di Y &gt; 110</span> (Hunger/Weakness), <span class="text-danger fw-semibold">+15% Damage Terbakar Api</span>, <span class="text-danger fw-semibold">-10% Serangan PvP &amp; Mining</span>.
                    </div>
                </div>
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link mt-auto">
                        <span data-i18n="sylvamoor_link">Arsip Sylvamoor</span> <i class="bi bi-chevron-right ms-1"></i>
                    </a>
                @endif
            </div>
        </div>

        <!-- 2-Column Showcase Row: Civil Systems & Territorial Archive -->
        <div class="row g-4 align-items-stretch mb-5" id="gameplay">
            <!-- Left Column: Civil Systems Ledger -->
            <div class="col-lg-6">
                <div class="apx-monolith-ledger h-100 d-flex flex-column justify-content-between">
                    <!-- Row 1: Perjanjian Kedaulatan -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title" data-i18n="ledger_title_war">Perjanjian Kedaulatan &amp; Kingdom War</h4>
                            <span class="text-dim small font-monospace" data-i18n="ledger_badge_war">DEKRIT REALM</span>
                        </div>
                        <p class="apx-ledger-desc" data-i18n="ledger_desc_war">
                            Setiap warga bebas memilih baiat kepada satu kerajaan. Pertahankan perbatasan teritori, bangun benteng pertahanan, dan rebut supremasi pada Kingdom War mingguan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="apx-ledger-link">
                                <span data-i18n="ledger_link_war">Dekrit Kedaulatan Kerajaan</span> <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @endif
                    </div>

                    <!-- Row 2: Ekonomi -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title" data-i18n="ledger_title_econ">Ekonomi Pasar Terbuka &amp; Escrow</h4>
                            <span class="text-dim small font-monospace" data-i18n="ledger_badge_econ">DUAL-CURRENCY</span>
                        </div>
                        <p class="apx-ledger-desc" data-i18n="ledger_desc_econ">
                            Sistem transaksi ganda Rupiah (Rp) dan Diamond 💎. Kuasai pasar dinamis berbasis supply-demand (/shop), lelang aman Escrow, dan pertukaran barter langsung lintas kerajaan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'ekonomi-perdagangan') }}" class="apx-ledger-link">
                                <span data-i18n="ledger_link_econ">Mekanisme Pasar &amp; Escrow</span> <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @elseif(plugins()->isEnabled('shop'))
                            <a href="{{ route('shop.home') }}" class="apx-ledger-link">
                                <span data-i18n="ledger_link_econ_shop">Kunjungi Toko Peradaban</span> <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        @endif
                    </div>

                    <!-- Row 3: Enchants & Kits -->
                    <div class="apx-ledger-row">
                        <div class="d-flex align-items-baseline justify-content-between mb-1">
                            <h4 class="apx-ledger-title" data-i18n="ledger_title_ench">Sistem Penempaan &amp; 182 Enchants</h4>
                            <span class="text-dim small font-monospace" data-i18n="ledger_badge_ench">7 TIER KEKUATAN</span>
                        </div>
                        <p class="apx-ledger-desc" data-i18n="ledger_desc_ench">
                            Tujuh tingkatan custom enchants melalui Alchemist dan Tinkerer, dipadukan dengan set bonus armor legendaris yang seimbang dan teruji untuk pertempuran kedaulatan.
                        </p>
                        @if(plugins()->isEnabled('wiki'))
                            <a href="{{ route('wiki.show', 'enchants-dan-kits') }}" class="apx-ledger-link">
                                <span data-i18n="ledger_link_ench">Ensiklopedia Penempaan</span> <i class="bi bi-chevron-right ms-1"></i>
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
                        <div class="apx-showcase-tag" data-i18n="realm_tag">ARSIP WILAYAH REALM</div>
                    </div>
                    <div class="apx-showcase-footer">
                        <div>
                            <h4 class="apx-showcase-title" data-i18n="realm_title">BENTANG ALAM PERADABAN</h4>
                            <p class="apx-showcase-sub" data-i18n="realm_sub">Dari benteng ibukota yang kokoh hingga hamparan alam liar yang menanti untuk ditaklukkan.</p>
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

@if(\Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService::isMapEnabled())
@php
    $serverMapUrl = \Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService::getMapUrl();
    $serverMapOnline = \Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService::isMapOnline();
@endphp

<!-- Section: Explore Apexsions World (Live Interactive Server Map) -->
<section class="apx-section apx-map-section position-relative overflow-hidden py-5" id="server-map" style="background: linear-gradient(180deg, var(--apx-bg-surface) 0%, var(--apx-bg-deep) 100%); border-top: 1px solid var(--apx-gold-border-subtle);">
    <div class="container position-relative" style="z-index: 2;">
        <div class="p-4 p-md-5 rounded-4 border border-warning border-opacity-25" style="background: radial-gradient(circle at 10% 20%, rgba(15, 23, 42, 0.95), rgba(9, 12, 19, 0.98)); box-shadow: 0 16px 40px rgba(0,0,0,0.6); border-radius: var(--apx-radius-lg);">
            <div class="row align-items-center g-4">
                <div class="col-lg-7">
                    <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
                        <span class="apx-section-kicker" data-i18n="map_kicker">PETA REAL-TIME SERVER</span>
                        @if($serverMapOnline)
                            <span class="badge bg-success bg-opacity-20 text-success border border-success border-opacity-30 px-2 py-1 small d-inline-flex align-items-center gap-1">
                                <span class="spinner-grow spinner-grow-sm text-success" style="width: 0.45rem; height: 0.45rem;" role="status"></span>
                                <span>🟢 Map Online</span>
                            </span>
                        @else
                            <span class="badge bg-secondary bg-opacity-20 text-muted border border-secondary border-opacity-30 px-2 py-1 small">
                                ⚪ Map Standby
                            </span>
                        @endif
                        <span class="badge bg-black bg-opacity-40 text-white-50 border border-secondary border-opacity-20 px-2 py-1 small font-monospace">
                            Port :32076
                        </span>
                    </div>

                    <h2 class="apx-world-title mb-3" data-i18n="map_title">
                        Explore Apexsions World
                    </h2>

                    <p class="apx-world-lead text-muted mb-4" style="max-width: 680px; font-size: 1.05rem; line-height: 1.75;" data-i18n="map_lead">
                        Jelajahi dunia Apexsions secara langsung melalui peta interaktif dan temukan kerajaan, wilayah, serta berbagai lokasi penting di server.
                    </p>

                    <div class="d-flex flex-wrap gap-3">
                        <a href="{{ $serverMapUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold px-4 py-3 fw-bold d-inline-flex align-items-center gap-2 apx-btn-glow">
                            <i class="bi bi-map-fill"></i>
                            <span data-i18n="map_btn_open">🗺 Buka Server Map</span>
                            <i class="bi bi-box-arrow-up-right small ms-1"></i>
                        </a>
                        <a href="{{ url('/server-map') }}" class="btn btn-apx-outline px-4 py-3 d-inline-flex align-items-center gap-2">
                            <i class="bi bi-info-circle"></i>
                            <span data-i18n="map_btn_guide">Panduan &amp; Sektor Kerajaan</span>
                        </a>
                    </div>
                </div>

                <div class="col-lg-5">
                    <div class="p-4 rounded-3 border border-secondary border-opacity-20 bg-black bg-opacity-40 text-center position-relative overflow-hidden">
                        <div class="mb-3 text-warning" style="font-size: 3rem; filter: drop-shadow(0 0 16px rgba(245, 158, 11, 0.4));">
                            <i class="bi bi-compass"></i>
                        </div>
                        <h4 class="font-cinzel text-white mb-2" data-i18n="map_card_title">Visualisasi 3D Interaktif</h4>
                        <p class="text-muted small mb-3" style="line-height: 1.6;" data-i18n="map_card_desc">
                            Pantau pergerakan warga, bangunan megah kerajaan, dan benteng pertahanan dari sudut pandang 3D real-time beresolusi tinggi.
                        </p>
                        <div class="d-flex justify-content-center gap-2 small font-monospace text-warning">
                            <span>Zenithar</span> &bull; <span>Solterra</span> &bull; <span>Sylvamoor</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
@endif

<!-- Official Rank Hierarchy Showcase (Pure Visual Artwork & Clean Showcase) -->
<section class="apx-section py-5" id="ranks" style="background: var(--apx-bg-deep); border-top: 1px solid var(--apx-gold-border-subtle);">
    <div class="container">
        <!-- Section Header -->
        <div class="text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="caste_kicker">TATANAN SOSIAL</div>
            <h2 class="apx-world-title mb-2" data-i18n="caste_title">
                Tatanan Sebelas Kasta Sosial
            </h2>
            <p class="text-muted mx-auto" style="max-width: 680px; font-size: 1rem; line-height: 1.7;" data-i18n="caste_lead">
                Dari pijakan awal warga perintis hingga tahta tertinggi sang leluhur pendiri kerajaan, setiap kasta memikul bobot kedaulatan, hak wilayah, dan kehormatan yang terukur.
            </p>
        </div>

        <!-- Interactive Strata Filter Navigation -->
        <div class="apx-caste-nav" role="tablist" aria-label="Filter Hierarki Kasta">
            <button type="button" class="apx-caste-filter-btn active" data-rank-filter="all" role="tab" aria-selected="true" data-i18n="caste_filter_all">SEMUA KASTA (11)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="apex" role="tab" aria-selected="false" data-i18n="caste_filter_apex">PUNCAK LELUHUR (1)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="authority" role="tab" aria-selected="false" data-i18n="caste_filter_authority">DEWAN OTORITAS (2)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="staff" role="tab" aria-selected="false" data-i18n="caste_filter_staff">ADMINISTRASI &amp; STAF (2)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="nobles" role="tab" aria-selected="false" data-i18n="caste_filter_nobles">ORDO BANGSAWAN (5)</button>
            <button type="button" class="apx-caste-filter-btn" data-rank-filter="pioneers" role="tab" aria-selected="false" data-i18n="caste_filter_pioneers">WARGA PERINTIS (1)</button>
        </div>

        <!-- The Architectural Social Ladder -->
        <div class="apx-social-ladder">

            <!-- TIER V: PUNCAK KEDAULATAN (THE APEX) -->
            <div class="apx-ladder-tier apx-tier-apex-wrapper" data-rank-category="apex">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman" data-i18n="caste_t5_roman">TINGKAT V</span>
                    <span class="apx-tier-name" data-i18n-html="caste_t5_name">PUNCAK KEDAULATAN &bull; THE APEX</span>
                    <span class="apx-weight-pill gold">WEIGHT 100</span>
                </div>
                <div class="apx-caste-card apx-card-apex" data-rank-category="apex">
                    <img src="{{ theme_asset('img/ranks/rank-ancestor.png') }}" alt="Banner Resmi Kasta The Ancestor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                    <div class="row align-items-center gy-3">
                        <div class="col-lg-4 col-md-5">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="apx-caste-tier-badge text-gold" data-i18n="caste_ancestor_badge">TAHTA TERTINGGI</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">The Ancestor</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-crown"></i> ✦ ANCESTOR ✦</div>
                            <span class="text-dim small" data-i18n="caste_ancestor_sub">Pendiri Peradaban &amp; Tahta Tertinggi</span>
                        </div>
                        <div class="col-lg-5 col-md-7">
                            <p class="apx-caste-desc mb-2" data-i18n="caste_ancestor_desc">
                                Sang leluhur agung dan pendiri peradaban. Pemegang mandat tertinggi atas hukum, arsitektur, dan kedaulatan seluruh realm Apexsions.
                            </p>
                            <ul class="apx-caste-perks mb-0">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p1">Mandat Tertinggi Dekrit Kerajaan</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p2">Kedaulatan Mutlak Seluruh Realm</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p3">Mahkota Segel Leluhur Abadi</span></li>
                            </ul>
                        </div>
                        <div class="col-lg-3 text-lg-end">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm">
                                    <span data-i18n="caste_btn_mandate">Pelajari Mandat</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-gold small font-monospace" data-i18n="caste_ancestor_badge">TAHTA TERTINGGI</span>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER IV: DEWAN OTORITAS (COUNCIL OF HIGH AUTHORITY) -->
            <div class="apx-ladder-tier apx-tier-authority-wrapper" data-rank-category="authority">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman" data-i18n="caste_t4_roman">TINGKAT IV</span>
                    <span class="apx-tier-name" data-i18n-html="caste_t4_name">DEWAN OTORITAS &bull; HIGH AUTHORITY</span>
                    <span class="apx-weight-pill" data-i18n="caste_t4_weight">WEIGHT 95 (KEDUDUKAN SETARA)</span>
                </div>
                <div class="apx-tier-authority-grid">
                    <!-- 2. Architect -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-architect.png') }}" alt="Banner Resmi Kasta Architect" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-purple" data-i18n="caste_authority_badge">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 95</span>
                            </div>
                            <h3 class="apx-caste-name">Architect</h3>
                            <div class="apx-caste-prefix text-purple"><i class="bi bi-compass-fill"></i> ARCHITECT</div>
                            <p class="apx-caste-desc" data-i18n="caste_architect_desc">
                                Perancang tata ruang dan pembangun peradaban. Mengatur cetak biru arsitektur realm, struktur kota, dan batas kedaulatan wilayah secara setara di Dewan Otoritas.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p1">Otoritas Cetak Biru &amp; Pembangunan Realm</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p2">Hak Pengawasan Tata Ruang &amp; Konstruksi</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p3">Akses Konsol Kreatif &amp; Inspeksi Properti</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;" data-i18n="caste_role_architect">ARSITEK REALM</span>
                            @endif
                        </div>
                    </div>

                    <!-- 3. Overseer -->
                    <div class="apx-caste-card" data-rank-category="authority">
                        <img src="{{ theme_asset('img/ranks/rank-overseer.png') }}" alt="Banner Resmi Kasta Overseer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="81">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-gold" data-i18n="caste_authority_badge">DEWAN OTORITAS</span>
                                <span class="apx-caste-weight">WEIGHT 95</span>
                            </div>
                            <h3 class="apx-caste-name">Overseer</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-eye-fill"></i> OVERSEER</div>
                            <p class="apx-caste-desc" data-i18n="caste_overseer_desc">
                                Mata pengawas kedaulatan dan kestabilan dunia. Memantau integritas transaksi ekonomi, kepatuhan peradaban, dan audit peradilan secara setara di Dewan Otoritas.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p1">Audit Kedaulatan, Transaksi &amp; Keadilan</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p2">Pemantauan Dinamika Pasar &amp; Kas Kerajaan</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p3">Akses Meja Investigasi &amp; Log Peradaban</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;" data-i18n="caste_role_overseer">PENGAWAS REALM</span>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER III: ADMINISTRASI & PENEGAK HUKUM (STAFF & LAW ENFORCEMENT) -->
            <div class="apx-ladder-tier apx-tier-staff-wrapper" data-rank-category="staff">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman" data-i18n="caste_t3_roman">TINGKAT III</span>
                    <span class="apx-tier-name" data-i18n-html="caste_t3_name">ADMINISTRASI &amp; PENEGAK HUKUM &bull; STAFF</span>
                    <span class="apx-weight-pill">WEIGHT 90 &bull; 80</span>
                </div>
                <div class="apx-tier-staff-grid">
                    <!-- 4. Warden -->
                    <div class="apx-caste-card" data-rank-category="staff">
                        <img src="{{ theme_asset('img/ranks/rank-warden.png') }}" alt="Banner Resmi Kasta Warden" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-blue" data-i18n="caste_staff_badge">ADMINISTRASI &amp; STAF</span>
                                <span class="apx-caste-weight">WEIGHT 90</span>
                            </div>
                            <h3 class="apx-caste-name">Warden</h3>
                            <div class="apx-caste-prefix text-blue"><i class="bi bi-shield-shaded"></i> WARDEN</div>
                            <p class="apx-caste-desc" data-i18n="caste_warden_desc">
                                Penjaga gerbang utama dan kepala staf administrasi peradaban di bawah naungan Dewan Otoritas. Mengawasi kestabilan wilayah, tribunal keadilan, dan ketertiban hukum dunia.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p1">Otoritas Tribunal &amp; Keamanan Realm</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p2">Pengawasan Integritas Transaksi &amp; War</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p3">Hak Inspeksi Wilayah Berdaulat</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;" data-i18n="caste_role_warden">KEPALA PENGAWAS</span>
                            @endif
                        </div>
                    </div>

                    <!-- 5. Herald -->
                    <div class="apx-caste-card" data-rank-category="staff">
                        <img src="{{ theme_asset('img/ranks/rank-herald.png') }}" alt="Banner Resmi Kasta Herald" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-pink" data-i18n="caste_staff_badge">ADMINISTRASI &amp; STAF</span>
                                <span class="apx-caste-weight">WEIGHT 80</span>
                            </div>
                            <h3 class="apx-caste-name">Herald</h3>
                            <div class="apx-caste-prefix text-pink"><i class="bi bi-chat-quote"></i> HERALD</div>
                            <p class="apx-caste-desc" data-i18n="caste_herald_desc">
                                Utusan resmi, pembawa maklumat peradaban, dan moderator realm. Menjembatani suara warga dengan dewan penguasa dan menjaga etika publik.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p1">Moderasi &amp; Diplomasi Publik</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p2">Penegakan Etika Peradaban</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p3">Akses Meja Laporan Warga</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @else
                                <span class="text-dim small text-uppercase fw-bold" style="letter-spacing: 0.1em;" data-i18n="caste_role_herald">UTUSAN RESMI</span>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER II: ORDO BANGSAWAN (THE NOBLE ASCENSION) -->
            <div class="apx-ladder-tier apx-tier-nobles-wrapper" data-rank-category="nobles">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman" data-i18n="caste_t2_roman">TINGKAT II</span>
                    <span class="apx-tier-name" data-i18n-html="caste_t2_name">ORDO BANGSAWAN &bull; THE ASCENSION</span>
                    <span class="apx-weight-pill">WEIGHT 70 &bull; 60 &bull; 50 &bull; 40 &bull; 30</span>
                </div>
                <div class="apx-tier-ascension-grid">
                    <!-- 6. Sions -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-sions.png') }}" alt="Banner Resmi Kasta Sions" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="132">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge text-gold" data-i18n="caste_sions_badge">APEX NOBLE</span>
                                <span class="apx-caste-weight">WEIGHT 70</span>
                            </div>
                            <h3 class="apx-caste-name">Sions</h3>
                            <div class="apx-caste-prefix text-gold"><i class="bi bi-star-fill"></i> ✦ SIONS ✦</div>
                            <p class="apx-caste-desc" data-i18n="caste_sions_desc">
                                Kasta bangsawan puncak peradaban. Pilar kemakmuran tertinggi realm dengan keistimewaan absolut.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p1">+15 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p2">Kit Sions Eksklusif</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p3">Siaran Kedatangan Megah</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 7. Emperor -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-emperor.png') }}" alt="Banner Resmi Kasta Emperor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge" data-i18n="caste_emperor_badge">DONATUR TIER 4</span>
                                <span class="apx-caste-weight">WEIGHT 60</span>
                            </div>
                            <h3 class="apx-caste-name">Emperor</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-gem"></i> EMPEROR</div>
                            <p class="apx-caste-desc" data-i18n="caste_emperor_desc">
                                Bangsawan penakluk berwibawa tinggi. Penguasa langit dengan hak terbang di wilayah klaim.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p1">Hak Terbang /fly di Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p2">+10 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p3">Kit Bulanan Gear Lengkap</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 8. Sovereign -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-sovereign.png') }}" alt="Banner Resmi Kasta Sovereign" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge" data-i18n="caste_sovereign_badge">DONATUR TIER 3</span>
                                <span class="apx-caste-weight">WEIGHT 50</span>
                            </div>
                            <h3 class="apx-caste-name">Sovereign</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-feather"></i> SOVEREIGN</div>
                            <p class="apx-caste-desc" data-i18n="caste_sovereign_desc">
                                Tuan tanah emas peradaban. Menguasai jalur niaga bebas tarif dagang lintas kerajaan.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p1">Bebas Tarif Dagang Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p2">+7 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p3">Kit Sovereign 14 Harian</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 9. Archon -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-archon.png') }}" alt="Banner Resmi Kasta Archon" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="87">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge" data-i18n="caste_archon_badge">DONATUR TIER 2</span>
                                <span class="apx-caste-weight">WEIGHT 40</span>
                            </div>
                            <h3 class="apx-caste-name">Archon</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-lightning-charge"></i> ARCHON</div>
                            <p class="apx-caste-desc" data-i18n="caste_archon_desc">
                                Kaum perajin kristal dan cendekiawan realm. Menikmati utilitas workbench portabel di mana saja.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p1">Akses /ec, /anvil Portabel</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p2">+4 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p3">Kit Mingguan &amp; Kosmetik</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 10. Ascendant -->
                    <div class="apx-caste-card" data-rank-category="nobles">
                        <img src="{{ theme_asset('img/ranks/rank-ascendant.png') }}" alt="Banner Resmi Kasta Ascendant" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                        <div>
                            <div class="apx-caste-header">
                                <span class="apx-caste-tier-badge" data-i18n="caste_ascendant_badge">DONATUR TIER 1</span>
                                <span class="apx-caste-weight">WEIGHT 30</span>
                            </div>
                            <h3 class="apx-caste-name">Ascendant</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-flower1"></i> ASCENDANT</div>
                            <p class="apx-caste-desc" data-i18n="caste_ascendant_desc">
                                Warga terhormat yang membuktikan dedikasinya. Prioritas antrean masuk dan perbekalan harian.
                            </p>
                            <ul class="apx-caste-perks">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p1">Bypass Antrean Server</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p2">+2 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p3">Kit Ascendant Harian</span></li>
                            </ul>
                        </div>
                        <div class="apx-caste-action">
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="apx-player-link">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- TIER I: FONDASI PERADABAN (THE FOUNDATION) -->
            <div class="apx-ladder-tier apx-tier-foundation-wrapper" data-rank-category="pioneers">
                <div class="apx-tier-label-bar">
                    <span class="apx-tier-roman" data-i18n="caste_t1_roman">TINGKAT I</span>
                    <span class="apx-tier-name" data-i18n-html="caste_t1_name">FONDASI PERADABAN &bull; THE FOUNDATION</span>
                    <span class="apx-weight-pill">WEIGHT 10</span>
                </div>
                <div class="apx-caste-card apx-card-foundation" data-rank-category="pioneers">
                    <img src="{{ theme_asset('img/ranks/rank-wanderer.png') }}" alt="Banner Resmi Kasta Wanderer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="88">
                    <div class="row align-items-center gy-3">
                        <div class="col-lg-4 col-md-5">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_wanderer_badge">WARGA PERINTIS</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Wanderer</h3>
                            <div class="apx-caste-prefix"><i class="bi bi-compass"></i> WANDERER</div>
                            <span class="text-dim small" data-i18n="caste_wanderer_sub">Pijakan Awal Seluruh Warga Baru</span>
                        </div>
                        <div class="col-lg-5 col-md-7">
                            <p class="apx-caste-desc mb-2" data-i18n="caste_wanderer_desc">
                                Fondasi dan jiwa peradaban Apexsions. Setiap legenda dimulai dari warga baru yang berani menancapkan pijakan pertama di alam liar.
                            </p>
                            <ul class="apx-caste-perks mb-0">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_wanderer_p1">Akses Penuh ke Tiga Kerajaan</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_wanderer_p2">Partisipasi Pasar &amp; Lelang Escrow</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_wanderer_p3">Batas Wilayah Awal &amp; Progresi Bebas</span></li>
                            </ul>
                        </div>
                        <div class="col-lg-3 text-lg-end">
                            <a href="#getting-started" class="btn btn-apx-outline btn-sm">
                                <span data-i18n="caste_btn_join">Cara Bergabung</span> <i class="bi bi-chevron-right ms-1"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- Section Action Footer -->
        <div class="text-center mt-5">
            <div class="d-flex justify-content-center align-items-center gap-3 flex-wrap">
                @if(plugins()->isEnabled('wiki'))
                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-gold px-4 py-2">
                        <i class="bi bi-journal-text me-2"></i> <span data-i18n="caste_btn_full_guide">Panduan Lengkap Kasta</span>
                    </a>
                @endif
                @if(plugins()->isEnabled('shop'))
                    <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline px-4 py-2">
                        <i class="bi bi-crown me-2"></i> <span data-i18n="caste_btn_webstore">Jelajahi Kasta di Webstore</span>
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
            <div class="apx-section-kicker mb-2" data-i18n="step_kicker">GERBANG INISIASI</div>
            <h2 class="apx-section-title" data-i18n="step_title">Tata Cara Masuk ke Peradaban</h2>
            <p class="text-muted mx-auto" style="max-width: 620px; font-size: 1rem; line-height: 1.7;" data-i18n="step_lead">
                Tiga langkah sederhana untuk menghubungkan klien Minecraft dan mencatatkan namamu dalam sejarah warga berdaulat.
            </p>
        </div>

        <!-- Connected 3-Step Monolith Stepper -->
        <div class="apx-stepper-grid">
            <!-- Step 1: Pasang Klien -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3" data-i18n="step1_phase">TAHAP PERTAMA</div>
                <h3 class="apx-step-title" data-i18n="step1_title">Klien Minecraft 26.2</h3>
                <p class="apx-step-desc" data-i18n-html="step1_desc">
                    Gunakan Minecraft versi resmi atau launcher pilihanmu pada versi <strong>26.2</strong>. Mendukung penuh koneksi <strong>Java Edition &amp; Bedrock Edition</strong>.
                </p>
                <div class="apx-step-action mt-auto">
                    <span class="text-dim small" data-i18n="step1_meta">Java &amp; Bedrock Crossplay</span>
                </div>
            </div>

            <!-- Step 2: Tembus Gerbang (Salin IP) -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3" data-i18n="step2_phase">TAHAP KEDUA</div>
                <h3 class="apx-step-title" data-i18n="step2_title">Alamat Server &amp; Port</h3>
                <p class="apx-step-desc" data-i18n-html="step2_desc">
                    Buka menu Multiplayer dan masukkan alamat server <code>apexsions.my.id:32348</code>. Untuk pemain Bedrock, masukkan IP <code>apexsions.my.id</code> dengan Port <code>32348</code>.
                </p>
                <div class="apx-step-action mt-auto d-flex flex-column gap-2">
                    <button type="button" class="btn btn-apx-outline btn-sm w-100 py-2" data-apx-copy="apexsions.my.id:32348" aria-label="Salin Alamat Server Java">
                        <i class="bi bi-laptop me-1"></i> <span data-i18n="step2_copy_java">Salin Java &bull; apexsions.my.id:32348</span>
                    </button>
                    <button type="button" class="btn btn-apx-outline btn-sm w-100 py-2" data-apx-copy="IP: apexsions.my.id | Port: 32348" aria-label="Salin IP dan Port Bedrock">
                        <i class="bi bi-phone me-1"></i> <span data-i18n="step2_copy_bedrock">Salin Bedrock &bull; IP: apexsions.my.id | Port: 32348</span>
                    </button>
                </div>
            </div>

            <!-- Step 3: Ikrar Peradaban -->
            <div class="apx-step-monolith">
                <div class="apx-step-phase-label mb-3" data-i18n="step3_phase">TAHAP KETIGA</div>
                <h3 class="apx-step-title" data-i18n="step3_title">Autentikasi Akun (/link)</h3>
                <p class="apx-step-desc" data-i18n-html="step3_desc">
                    Setelah berada di lobi server, ketik perintah <code>/link</code> untuk menerima kode autentikasi rahasia guna menautkan akun dengan portal web.
                </p>
                <div class="apx-step-action mt-auto">
                    @auth
                        @if(plugins()->isEnabled('apexsions-bridge'))
                            <a href="{{ route('apexsions-bridge.link.index') }}" class="btn btn-apx-outline btn-sm w-100 py-2">
                                <i class="bi bi-controller me-1"></i> <span data-i18n="step3_btn_link">Buka Portal Tautkan</span>
                            </a>
                        @else
                            <span class="text-dim small" data-i18n="step3_registered">Akun Anda Terdaftar</span>
                        @endif
                    @else
                        <a href="{{ route('register') }}" class="btn btn-apx-outline btn-sm w-100 py-2">
                            <i class="bi bi-person-plus me-1"></i> <span data-i18n="step3_btn_register">Daftar Akun Peradaban</span>
                        </a>
                    @endauth
                </div>
            </div>
        </div>

        <!-- Callout Banner: Panduan 15 Menit Pertama Warga Baru -->
        <div class="mt-4 p-4 rounded text-center position-relative overflow-hidden" style="background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(13, 18, 30, 0.85) 100%); border: 1px solid var(--apx-gold-border); border-radius: var(--apx-radius-md);">
            <div class="row align-items-center">
                <div class="col-lg-8 text-lg-start mb-3 mb-lg-0">
                    <span class="badge mb-2" style="background: rgba(245, 158, 11, 0.2); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.72rem; letter-spacing: 0.08em;" data-i18n="guide_callout_badge">
                        <i class="bi bi-compass-fill me-1"></i> PANDUAN KILAT WARGA BARU
                    </span>
                    <h3 class="mb-1 text-white" style="font-family: 'Cinzel', Georgia, serif; font-size: 1.35rem;" data-i18n="guide_callout_title">
                        Bingung Harus Melakukan Apa Setelah Mendarat di Spawn?
                    </h3>
                    <p class="text-muted small mb-0" data-i18n="guide_callout_desc">
                        Pelajari peta jalan 15 menit dari mengambil starter kit, memilih kerajaan, menandai rumah, hingga menghasilkan pundi Rupiah pertama.
                    </p>
                </div>
                <div class="col-lg-4 text-lg-end">
                    @if(plugins()->isEnabled('wiki'))
                        <a href="{{ route('wiki.pages.show', ['panduan-pemula', 'panduan-15-menit-pertama']) }}" class="btn btn-apx-gold">
                            <i class="bi bi-book-half me-1"></i> <span data-i18n="guide_callout_btn">Buka Panduan 15 Menit</span>
                        </a>
                    @endif
                </div>
            </div>
        </div>
    </div>
</section>
@endsection
