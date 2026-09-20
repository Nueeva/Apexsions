@extends('layouts.app')

@section('title', 'Apexsions - Minecraft Survival & Kingdom Server Indonesia')
@section('description', 'Bergabunglah di Apexsions, server Minecraft Indonesia dengan sistem Survival, Kingdom, BattlePass, Economy, Auction, Custom Enchants, Crates, dan dunia yang terus berkembang.')

@section('content')
<!-- Panoramic Hero Section: Viewport Adaptive (Fits 100% Player Screen at Normal Zoom) -->
<section class="apx-hero-panoramic">
    <div class="apx-hero-panoramic-bg"></div>

    <div class="container position-relative d-flex flex-column justify-content-center h-100 py-4" style="z-index: 2; flex: 1;">
        <!-- Top Sovereign Identity Mark & Hierarchy Axis -->
        <div class="mb-2">
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

        <!-- Main Civilization Statement & Narrative -->
        <div class="row py-2">
            <div class="col-xl-7 col-lg-8">
                <h1 class="apx-hero-headline" data-i18n="hero_headline">
                    Peradaban Berdaulat yang Dibangun di Atas Tatanan Hierarki.
                </h1>

                <p class="apx-hero-subtext" data-i18n="hero_subtext">
                    Sebelas kasta sosial, tiga wilayah kerajaan, dan satu dunia yang dibentuk seutuhnya oleh sejarah warganya.
                </p>

                <!-- Action CTAs: Two Clear Primary Paths -->
                <div class="apx-hero-action-group d-flex align-items-center gap-2 mb-3 flex-wrap">
                    <a href="#civilizations" class="btn btn-apx-sovereign apx-sheen-periodic">
                        <span data-i18n="hero_btn_explore">JELAJAHI PERADABAN</span> <i class="bi bi-arrow-down ms-1"></i>
                    </a>
                    <a href="#getting-started" class="btn btn-apx-outline">
                        <i class="bi bi-compass me-1"></i><span data-i18n="hero_btn_guide">PANDUAN MASUK</span>
                    </a>
                </div>

                <!-- Unified Sovereign Server Gateway Card (Zero Duplicate Clutter) -->
                <div class="apx-gateway-card">
                    <!-- Status & Online Players Counter -->
                    <div class="apx-gateway-header d-flex align-items-center justify-content-between flex-wrap gap-2 pb-2 mb-2 border-bottom border-secondary border-opacity-20">
                        <div class="d-flex align-items-center gap-2">
                            <span class="apx-pulse-dot apx-beacon-live" id="apxLiveDot" aria-hidden="true"></span>
                            <span class="apx-gateway-status text-emerald fw-bold small" id="apxLiveBadge" data-i18n="hero_status_online">SERVER ONLINE</span>
                            <span class="text-secondary opacity-30">/</span>
                            <span class="apx-gateway-players text-sub small" id="apxPlayerCountContainer">
                                <span id="apxPlayerStatusText" data-i18n="hero_status_ready">Gerbang Terbuka &bull; Siap Menjelajah</span>
                                <span id="apxPlayerNumbers" class="d-none">
                                    <span class="text-gold fw-bold" id="apxOnlinePlayers">0</span> / <span id="apxMaxPlayers">200</span> <span data-i18n="hero_citizens">Warga</span>
                                </span>
                            </span>
                        </div>
                        <div class="apx-gateway-version small text-muted font-monospace">
                            v<span id="apxVersion">26.2</span>
                        </div>
                    </div>

                    <!-- Clean Tactile Copy Chips for Java & Bedrock -->
                    <div class="apx-gateway-chips d-flex align-items-center gap-2 flex-wrap">
                        <!-- Java Chip -->
                        <div class="apx-gateway-chip apx-copyable flex-grow-1" data-apx-copy="apexsions.my.id:32348" role="button" tabindex="0" title="Klik untuk menyalin IP Java" aria-label="Salin Alamat IP Server Java">
                            <span class="apx-chip-pill apx-pill-java">JAVA</span>
                            <span class="apx-chip-text font-monospace">apexsions.my.id:32348</span>
                            <i class="bi bi-clipboard apx-chip-copy-icon ms-auto"></i>
                        </div>

                        <!-- Bedrock Chip -->
                        <div class="apx-gateway-chip apx-copyable flex-grow-1" data-apx-copy="IP: apexsions.my.id | Port: 32348" role="button" tabindex="0" title="Klik untuk menyalin IP dan Port Bedrock" aria-label="Salin IP dan Port Server Bedrock">
                            <span class="apx-chip-pill apx-pill-bedrock">BEDROCK</span>
                            <span class="apx-chip-text font-monospace">apexsions.my.id <span class="text-gold opacity-60">&bull;</span> 32348</span>
                            <i class="bi bi-clipboard apx-chip-copy-icon ms-auto"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Section: A WORLD BUILT BY PLAYERS (2-Column Asymmetric Showcase) -->
<section class="apx-section apx-world-section position-relative overflow-hidden" id="civilizations">
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
                Satu peradaban mahaluas, <strong>Kekaisaran Sions</strong>, runtuh setelah pembukaan portal ke <strong>Dimensi Kegelapan Umbra</strong>. <em>Eksodus Akbar</em> melahirkan tiga wilayah berdaulat yang mandiri dengan keunggulan dan tantangan uniknya masing-masing.
            </p>
        </div>

        <!-- Interactive Sovereign Altar (Tabbed Kingdom Realm Switcher) -->
        <div class="apx-altar-wrapper mb-4">
            <!-- Altar Tab Navigation -->
            <div class="apx-altar-tabs-nav mb-3" role="tablist" aria-label="Pilihan Kerajaan Berdaulat">
                <button type="button" class="apx-altar-tab-btn active" data-altar-target="zenithar" role="tab" aria-selected="true">
                    <i class="bi bi-compass text-gold me-2"></i><span class="fw-bold">Zenithar</span>
                    <span class="apx-altar-tab-badge ms-2" data-i18n="zenithar_badge">TIMUR</span>
                </button>
                <button type="button" class="apx-altar-tab-btn" data-altar-target="solterra" role="tab" aria-selected="false">
                    <i class="bi bi-fire text-danger me-2"></i><span class="fw-bold">Solterra</span>
                    <span class="apx-altar-tab-badge ms-2" data-i18n="solterra_badge">SELATAN</span>
                </button>
                <button type="button" class="apx-altar-tab-btn" data-altar-target="sylvamoor" role="tab" aria-selected="false">
                    <i class="bi bi-tree text-info me-2"></i><span class="fw-bold">Sylvamoor</span>
                    <span class="apx-altar-tab-badge ms-2" data-i18n="sylvamoor_badge">BARAT</span>
                </button>
            </div>

            <!-- Altar Stage Panels -->
            <div class="apx-altar-stage">
                <!-- Panel 1: Zenithar -->
                <div class="apx-altar-panel active apx-kingdom-zenithar" data-altar-panel="zenithar" role="tabpanel">
                    <div class="row align-items-center g-4">
                        <div class="col-lg-7">
                            <div class="apx-province-header mb-2">
                                <span class="apx-province-type"><i class="bi bi-compass me-1"></i> <span data-i18n="zenithar_type">TERITORI TIMUR &bull; DINASTI KERAJAAN</span></span>
                                <h3 class="apx-province-name">Zenithar</h3>
                            </div>
                            <p class="apx-province-desc mb-3" data-i18n="zenithar_desc">
                                Dipimpin oleh keluarga dinasti dan pengawal elit Kekaisaran Sions yang mengungsi ke timur menuju puncak cakrawala. Menjunjung kemurnian tata krama istana, kavaleri suci, dan kubah menara langit.
                            </p>
                            <!-- Visual Scannable Spec Chips -->
                            <div class="d-flex flex-wrap gap-2 mb-3">
                                <span class="apx-spec-chip"><i class="bi bi-bank text-gold"></i> Puncak Cakrawala</span>
                                <span class="apx-spec-chip"><i class="bi bi-gem text-gold"></i> Solarium Spire</span>
                                <span class="apx-spec-chip"><i class="bi bi-coin text-gold"></i> Tambang Emas Murni</span>
                                <span class="apx-spec-chip"><i class="bi bi-building text-gold"></i> Arsitektur Megah</span>
                            </div>
                            <!-- Buffs & Debuffs Summary -->
                            <div class="apx-altar-effects p-3 mb-3">
                                <div class="mb-2">
                                    <span class="apx-buff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-plus me-1"></i> Buff:</span>
                                    <span class="small text-muted" data-i18n-html="zenithar_buffs">
                                        <span class="text-success fw-semibold">+8% Speed</span>, <span class="text-success fw-semibold">+15% Luck</span>, <span class="text-success fw-semibold">[Royal Discipline] +6% Damage</span>, <span class="text-success fw-semibold">[Royal Aegis] 20% Reduksi Damage</span>, <span class="text-success fw-semibold">Diskon 30% Pajak Lelang &amp; Bunga Bank +25%</span>.
                                    </span>
                                </div>
                                <div>
                                    <span class="apx-debuff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-minus me-1"></i> Debuff:</span>
                                    <span class="small text-muted" data-i18n-html="zenithar_debuffs">
                                        <span class="apx-debuff-text fw-semibold">+15% Rentan Racun &amp; Wither</span>, <span class="apx-debuff-text fw-semibold">+12% Cepat Lapar</span>, <span class="apx-debuff-text fw-semibold">-10% Mining Speed</span>.
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-5 text-lg-end">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="btn btn-apx-gold px-4 py-2">
                                    <i class="bi bi-journal-bookmark me-2"></i><span data-i18n="zenithar_link">Buka Arsip Doktrin Zenithar</span>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>

                <!-- Panel 2: Solterra -->
                <div class="apx-altar-panel apx-kingdom-solterra" data-altar-panel="solterra" role="tabpanel">
                    <div class="row align-items-center g-4">
                        <div class="col-lg-7">
                            <div class="apx-province-header mb-2">
                                <span class="apx-province-type"><i class="bi bi-compass me-1"></i> <span data-i18n="solterra_type">TERITORI SELATAN &bull; MAGICIAN &amp; VETERAN</span></span>
                                <h3 class="apx-province-name">Solterra</h3>
                            </div>
                            <p class="apx-province-desc mb-3" data-i18n="solterra_desc">
                                Dibentuk oleh para pesulap tempur agung dan prajurit garis depan terkuat bekas legiun Sions di selatan. Memadukan sihir api elemen dengan penempaan senjata berat brutal di atas cadas vulkanik.
                            </p>
                            <!-- Visual Scannable Spec Chips -->
                            <div class="d-flex flex-wrap gap-2 mb-3">
                                <span class="apx-spec-chip"><i class="bi bi-fire text-danger"></i> Cadas Vulkanik</span>
                                <span class="apx-spec-chip"><i class="bi bi-cone-striped text-danger"></i> Kawah Lahar</span>
                                <span class="apx-spec-chip"><i class="bi bi-shield-shaded text-danger"></i> Ignis Bastion</span>
                                <span class="apx-spec-chip"><i class="bi bi-hammer text-danger"></i> Penempaan Berat</span>
                            </div>
                            <!-- Buffs & Debuffs Summary -->
                            <div class="apx-altar-effects p-3 mb-3">
                                <div class="mb-2">
                                    <span class="apx-buff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-plus me-1"></i> Buff:</span>
                                    <span class="small text-muted" data-i18n-html="solterra_buffs">
                                        <span class="text-success fw-semibold">[Battle Momentum] +15% Damage</span>, <span class="text-success fw-semibold">+10% Critical</span>, <span class="text-success fw-semibold">+10% Mining Speed</span>, <span class="text-success fw-semibold">Rasio Jual Ore Tinggi (30%)</span>.
                                    </span>
                                </div>
                                <div>
                                    <span class="apx-debuff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-minus me-1"></i> Debuff:</span>
                                    <span class="small text-muted" data-i18n-html="solterra_debuffs">
                                        <span class="apx-debuff-text fw-semibold">-2 HP Maksimal (9 Hati)</span>, <span class="apx-debuff-text fw-semibold">+8% Damage Masuk</span>, <span class="apx-debuff-text fw-semibold">+7% Cepat Lapar</span>.
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-5 text-lg-end">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="btn btn-apx-gold px-4 py-2">
                                    <i class="bi bi-journal-bookmark me-2"></i><span data-i18n="solterra_link">Buka Arsip Doktrin Solterra</span>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>

                <!-- Panel 3: Sylvamoor -->
                <div class="apx-altar-panel apx-kingdom-sylvamoor" data-altar-panel="sylvamoor" role="tabpanel">
                    <div class="row align-items-center g-4">
                        <div class="col-lg-7">
                            <div class="apx-province-header mb-2">
                                <span class="apx-province-type"><i class="bi bi-compass me-1"></i> <span data-i18n="sylvamoor_type">TERITORI BARAT &bull; PEKERJA &amp; PEJUANG RIMBA</span></span>
                                <h3 class="apx-province-name">Sylvamoor</h3>
                            </div>
                            <p class="apx-province-desc mb-3" data-i18n="sylvamoor_desc">
                                Dibangun oleh kaum pekerja, pembangun, petani lumbung, dan pejuang rimba yang mengungsi ke belantara rimba barat. Mengisolasi diri dan hidup selaras menjaga kelestarian Pohon Dunia.
                            </p>
                            <!-- Visual Scannable Spec Chips -->
                            <div class="d-flex flex-wrap gap-2 mb-3">
                                <span class="apx-spec-chip"><i class="bi bi-tree text-info"></i> Rimba Kanopi Purba</span>
                                <span class="apx-spec-chip"><i class="bi bi-water text-info"></i> Samudra Kristal</span>
                                <span class="apx-spec-chip"><i class="bi bi-flower1 text-info"></i> Eldergrove</span>
                                <span class="apx-spec-chip"><i class="bi bi-basket text-info"></i> Lumbung Pangan</span>
                            </div>
                            <!-- Buffs & Debuffs Summary -->
                            <div class="apx-altar-effects p-3 mb-3">
                                <div class="mb-2">
                                    <span class="apx-buff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-plus me-1"></i> Buff:</span>
                                    <span class="small text-muted" data-i18n-html="sylvamoor_buffs">
                                        <span class="text-success fw-semibold">[Nature's Blessing] +2 HP (11 Hati)</span>, <span class="text-success fw-semibold">+12% Luck</span>, <span class="text-success fw-semibold">+7% Extra Drops</span>, <span class="text-success fw-semibold">Defense Rimba (+15%)</span>, <span class="text-success fw-semibold">Kelembapan Lahan Abadi</span>.
                                    </span>
                                </div>
                                <div>
                                    <span class="apx-debuff-label fw-bold text-uppercase small me-2"><i class="bi bi-shield-minus me-1"></i> Debuff:</span>
                                    <span class="small text-muted" data-i18n-html="sylvamoor_debuffs">
                                        <span class="apx-debuff-text fw-semibold">Mabuk Ketinggian Y &gt; 110</span>, <span class="apx-debuff-text fw-semibold">+15% Damage Api</span>, <span class="apx-debuff-text fw-semibold">-10% PvP &amp; Mining</span>.
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-5 text-lg-end">
                            @if(plugins()->isEnabled('wiki'))
                                <a href="{{ route('wiki.show', 'tiga-kerajaan') }}" class="btn btn-apx-gold px-4 py-2">
                                    <i class="bi bi-journal-bookmark me-2"></i><span data-i18n="sylvamoor_link">Buka Arsip Doktrin Sylvamoor</span>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section Footnote Accents -->
        <div class="d-flex justify-content-between align-items-center mt-5 pt-4 border-top border-secondary border-opacity-15 text-uppercase small flex-wrap gap-2" style="letter-spacing: 0.16em; color: var(--apx-text-dim);">
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

<!-- Section: Explore Apexsions World (Tactical Cartography & Sovereign Atlas) -->
<section class="apx-section apx-map-section position-relative overflow-hidden py-5 py-lg-6" id="server-map">
    <div class="container position-relative" style="z-index: 2;">
        <div class="apx-map-monolith p-4 p-md-5">
            <div class="row align-items-center g-4 g-lg-5">
                <!-- Left Column: Cartographic Intel & Sovereign Navigation -->
                <div class="col-lg-7">
                    <!-- Clean Telemetry Badges (Anti-slop: zero raw debug slash box) -->
                    <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
                        <span class="apx-section-kicker mb-0" data-i18n="map_kicker">KARTOGRAFI REALM</span>
                        <span class="apx-badge-chiseled apx-badge-chiseled-success">
                            <span class="apx-pulse-dot-sm apx-beacon-live me-1"></span>
                            <span data-i18n="{{ $serverMapOnline ? 'map_status_online' : 'map_status_standby' }}">
                                {{ $serverMapOnline ? 'ATLAS ONLINE' : 'ATLAS STANDBY' }}
                            </span>
                        </span>
                        <span class="apx-badge-chiseled">
                            <i class="bi bi-layers-half text-gold me-1"></i> BLUEMAP 3D
                        </span>
                        <span class="apx-badge-chiseled text-gold">
                            <i class="bi bi-geo-alt text-gold me-1"></i> <span data-i18n="map_telemetry_center">PUSAT TIGA KERAJAAN</span>
                        </span>
                    </div>

                    <h2 class="apx-world-title mb-3" data-i18n="map_title">
                        Eksplorasi Bentang Alam Apexsions
                    </h2>

                    <p class="apx-world-lead text-sub mb-4" data-i18n="map_lead">
                        Jelajahi peradaban Apexsions secara langsung melalui visualisasi 3D interaktif beresolusi tinggi. Pantau benteng kerajaan, persebaran warga, serta batas wilayah kedaulatan dari sudut pandang bebas tanpa batas.
                    </p>

                    <!-- Cartographic Capabilities (Architectural Pillars, anti-slop) -->
                    <div class="row g-3 mb-4 apx-carto-capabilities">
                        <div class="col-md-4 col-sm-6">
                            <div class="apx-carto-spec-card h-100">
                                <div class="d-flex align-items-center gap-2 mb-1.5">
                                    <i class="bi bi-compass text-gold"></i>
                                    <span class="apx-spec-title fw-semibold text-main" data-i18n="map_feat_persp_title">Perspektif 3D Bebas</span>
                                </div>
                                <div class="apx-spec-desc text-dim" data-i18n="map_feat_persp_desc">Rotasi orbital 360°, sudut isometrik, dan inspeksi topografi kontur dunia.</div>
                            </div>
                        </div>
                        <div class="col-md-4 col-sm-6">
                            <div class="apx-carto-spec-card h-100">
                                <div class="d-flex align-items-center gap-2 mb-1.5">
                                    <i class="bi bi-geo-alt text-gold"></i>
                                    <span class="apx-spec-title fw-semibold text-main" data-i18n="map_feat_track_title">Pelacakan Warga</span>
                                </div>
                                <div class="apx-spec-desc text-dim" data-i18n="map_feat_track_desc">Pantau koordinat pergerakan pemain dan pemukiman secara real-time.</div>
                            </div>
                        </div>
                        <div class="col-md-4 col-sm-12">
                            <div class="apx-carto-spec-card h-100">
                                <div class="d-flex align-items-center gap-2 mb-1.5">
                                    <i class="bi bi-shield text-gold"></i>
                                    <span class="apx-spec-title fw-semibold text-main" data-i18n="map_feat_bounds_title">Teritorial 3 Kerajaan</span>
                                </div>
                                <div class="apx-spec-desc text-dim" data-i18n="map_feat_bounds_desc">Visualisasi batas kedaulatan Zenithar, Solterra, dan Sylvamoor.</div>
                            </div>
                        </div>
                    </div>

                    <!-- Actions Bar -->
                    <div class="d-flex flex-wrap align-items-center gap-3">
                        <a href="{{ $serverMapUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold px-4 py-2.5 fw-bold d-inline-flex align-items-center gap-2">
                            <i class="bi bi-compass"></i>
                            <span data-i18n="map_btn_open">Buka Atlas Satelit 3D</span>
                            <i class="bi bi-box-arrow-up-right small ms-1"></i>
                        </a>
                        <span class="text-dim small font-monospace d-inline-flex align-items-center gap-2">
                            <i class="bi bi-globe2 text-gold opacity-75"></i>
                            <span data-i18n="map_tab_note">Satelit BlueMap Resolusi Penuh</span>
                        </span>
                    </div>
                </div>

                <!-- Right Column: Tactical Cartography Viewport Frame -->
                <div class="col-lg-5">
                    <a href="{{ $serverMapUrl }}" target="_blank" rel="noopener noreferrer" class="text-decoration-none d-block apx-carto-link">
                        <div class="apx-carto-viewport position-relative overflow-hidden">
                            <!-- Background Cartography Media Layer -->
                            <div class="apx-carto-media-layer">
                                <img src="{{ theme_asset('img/realm-showcase.jpg') }}" alt="Apexsions Realm Cartography Viewport" class="apx-carto-media-img" loading="lazy" decoding="async" width="800" height="450">
                                <div class="apx-carto-vignette"></div>
                                <div class="apx-carto-scanlines"></div>
                            </div>

                            <!-- Imperial Chiseled Corner Reticles -->
                            <span class="apx-carto-bracket apx-bracket-tl"></span>
                            <span class="apx-carto-bracket apx-bracket-tr"></span>
                            <span class="apx-carto-bracket apx-bracket-bl"></span>
                            <span class="apx-carto-bracket apx-bracket-br"></span>

                            <!-- HUD Header: Top Telemetry -->
                            <div class="apx-carto-hud-top d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center gap-2">
                                    <span class="apx-hud-indicator {{ $serverMapOnline ? 'is-live' : 'is-idle' }}"></span>
                                    <span class="apx-hud-title font-monospace" data-i18n="map_viewport_tag">REALM OBSERVER // LIVE</span>
                                </div>
                                <span class="apx-hud-coords font-monospace" data-i18n="map_hud_scope">CAKUPAN REALM AKTIF</span>
                            </div>

                            <!-- HUD Center: Tactical Reticle & Territory Scope -->
                            <div class="apx-carto-hud-center text-center">
                                <div class="apx-carto-reticle mx-auto mb-2">
                                    <i class="bi bi-crosshair2"></i>
                                </div>
                                <h4 class="font-cinzel apx-carto-realm-title mb-1.5" data-i18n="map_viewport_title">
                                    Atlas Tiga Kerajaan
                                </h4>
                                <div class="apx-carto-kingdoms d-flex justify-content-center align-items-center gap-2 font-monospace">
                                    <span class="apx-kingdom-tag tag-zenithar">ZENITHAR</span>
                                    <span class="apx-tag-separator">•</span>
                                    <span class="apx-kingdom-tag tag-solterra">SOLTERRA</span>
                                    <span class="apx-tag-separator">•</span>
                                    <span class="apx-kingdom-tag tag-sylvamoor">SYLVAMOOR</span>
                                </div>
                            </div>

                            <!-- HUD Footer: Interactive Action Strip -->
                            <div class="apx-carto-hud-bottom d-flex justify-content-between align-items-center">
                                <span class="apx-hud-lens font-monospace">FOV 3D &bull; 60 FPS</span>
                                <span class="apx-hud-action-cta d-inline-flex align-items-center gap-1.5 font-monospace">
                                    <span data-i18n="map_viewport_prompt">JELAJAHI PETA LAYAR PENUH</span>
                                    <i class="bi bi-arrow-up-right small"></i>
                                </span>
                            </div>
                        </div>
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>
@endif

<!-- Official Rank Hierarchy Showcase (Pure Visual Artwork & Clean Showcase) -->
<section class="apx-section apx-ranks-section py-5" id="ranks">
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

        <!-- Compact Tabbed Caste Matrix Navigation -->
        <div class="apx-caste-matrix-nav mb-4" role="tablist" aria-label="Navigasi Hierarki Kasta">
            <button type="button" class="apx-caste-matrix-tab active" data-caste-target="authority" role="tab" aria-selected="true">
                <i class="bi bi-crown text-gold me-2"></i><span data-i18n="caste_tab_authority">Tahta &amp; Otoritas (3)</span>
            </button>
            <button type="button" class="apx-caste-matrix-tab" data-caste-target="nobles" role="tab" aria-selected="false">
                <i class="bi bi-star-fill text-gold me-2"></i><span data-i18n="caste_tab_nobles">Ordo Bangsawan (5)</span>
            </button>
            <button type="button" class="apx-caste-matrix-tab" data-caste-target="staff" role="tab" aria-selected="false">
                <i class="bi bi-shield-shaded text-blue me-2"></i><span data-i18n="caste_tab_staff">Administrasi &amp; Staf (2)</span>
            </button>
            <button type="button" class="apx-caste-matrix-tab" data-caste-target="foundation" role="tab" aria-selected="false">
                <i class="bi bi-compass text-dim me-2"></i><span data-i18n="caste_tab_foundation">Fondasi Warga (1)</span>
            </button>
        </div>

        <!-- Compact Tabbed Caste Matrix Panels -->
        <div class="apx-caste-matrix-stage">

            <!-- Panel 1: Tahta & Otoritas (Ancestor, Architect, Overseer) -->
            <div class="apx-caste-matrix-panel active" data-caste-panel="authority" role="tabpanel">
                <div class="row g-4 align-items-stretch">
                    <!-- 1. The Ancestor (Tier V - Weight 100) -->
                    <div class="col-lg-4 col-md-6">
                        <div class="apx-caste-mini-card apx-card-apex h-100 d-flex flex-column justify-content-between">
                            <div>
                                <img src="{{ theme_asset('img/ranks/rank-ancestor.png') }}" alt="Banner Resmi Kasta The Ancestor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="apx-caste-tier-badge text-gold" data-i18n="caste_ancestor_badge">TAHTA TERTINGGI</span>
                                    <span class="apx-weight-pill gold">WEIGHT 100</span>
                                </div>
                                <h3 class="apx-caste-name mb-1">The Ancestor</h3>
                                <div class="apx-caste-prefix text-gold mb-2"><i class="bi bi-crown"></i> ✦ ANCESTOR ✦</div>
                                <p class="apx-caste-desc small mb-3" data-i18n="caste_ancestor_desc">
                                    Sang leluhur agung dan pendiri peradaban. Pemegang mandat tertinggi atas hukum, arsitektur, dan kedaulatan seluruh realm Apexsions.
                                </p>
                                <ul class="apx-caste-perks small mb-3">
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p1">Mandat Tertinggi Dekrit Kerajaan</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p2">Kedaulatan Mutlak Seluruh Realm</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_ancestor_p3">Mahkota Segel Leluhur Abadi</span></li>
                                </ul>
                            </div>
                            <div>
                                @if(plugins()->isEnabled('wiki'))
                                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm w-100">
                                        <span data-i18n="caste_btn_mandate">Pelajari Mandat</span> <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>

                    <!-- 2. Architect (Tier IV - Weight 95) -->
                    <div class="col-lg-4 col-md-6">
                        <div class="apx-caste-mini-card h-100 d-flex flex-column justify-content-between">
                            <div>
                                <img src="{{ theme_asset('img/ranks/rank-architect.png') }}" alt="Banner Resmi Kasta Architect" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="apx-caste-tier-badge text-purple" data-i18n="caste_authority_badge">DEWAN OTORITAS</span>
                                    <span class="apx-weight-pill">WEIGHT 95</span>
                                </div>
                                <h3 class="apx-caste-name mb-1">Architect</h3>
                                <div class="apx-caste-prefix text-purple mb-2"><i class="bi bi-compass-fill"></i> ARCHITECT</div>
                                <p class="apx-caste-desc small mb-3" data-i18n="caste_architect_desc">
                                    Perancang tata ruang dan pembangun peradaban. Mengatur cetak biru arsitektur realm, struktur kota, dan batas kedaulatan wilayah.
                                </p>
                                <ul class="apx-caste-perks small mb-3">
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p1">Otoritas Cetak Biru Pembangunan</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p2">Pengawasan Tata Ruang Realm</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_architect_p3">Akses Konsol &amp; Inspeksi Properti</span></li>
                                </ul>
                            </div>
                            <div>
                                @if(plugins()->isEnabled('wiki'))
                                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm w-100">
                                        <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>

                    <!-- 3. Overseer (Tier IV - Weight 95) -->
                    <div class="col-lg-4 col-md-6">
                        <div class="apx-caste-mini-card h-100 d-flex flex-column justify-content-between">
                            <div>
                                <img src="{{ theme_asset('img/ranks/rank-overseer.png') }}" alt="Banner Resmi Kasta Overseer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="81">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="apx-caste-tier-badge text-gold" data-i18n="caste_authority_badge">DEWAN OTORITAS</span>
                                    <span class="apx-weight-pill">WEIGHT 95</span>
                                </div>
                                <h3 class="apx-caste-name mb-1">Overseer</h3>
                                <div class="apx-caste-prefix text-gold mb-2"><i class="bi bi-eye-fill"></i> OVERSEER</div>
                                <p class="apx-caste-desc small mb-3" data-i18n="caste_overseer_desc">
                                    Mata pengawas kedaulatan dan kestabilan dunia. Memantau integritas transaksi ekonomi, kepatuhan peradaban, dan audit peradilan.
                                </p>
                                <ul class="apx-caste-perks small mb-3">
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p1">Audit Transaksi &amp; Keadilan Realm</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p2">Pemantauan Pasar &amp; Kas Kerajaan</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_overseer_p3">Akses Meja Investigasi &amp; Log Realm</span></li>
                                </ul>
                            </div>
                            <div>
                                @if(plugins()->isEnabled('wiki'))
                                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm w-100">
                                        <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Panel 2: Ordo Bangsawan (Sions, Emperor, Sovereign, Archon, Ascendant) -->
            <div class="apx-caste-matrix-panel" data-caste-panel="nobles" role="tabpanel">
                <div class="apx-tier-ascension-grid">
                    <!-- 4. Sions (Weight 70) -->
                    <div class="apx-caste-mini-card d-flex flex-column justify-content-between">
                        <div>
                            <img src="{{ theme_asset('img/ranks/rank-sions.png') }}" alt="Banner Resmi Kasta Sions" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="132">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="apx-caste-tier-badge text-gold" data-i18n="caste_sions_badge">APEX NOBLE</span>
                                <span class="apx-weight-pill gold">WEIGHT 70</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Sions</h3>
                            <div class="apx-caste-prefix text-gold mb-2"><i class="bi bi-star-fill"></i> ✦ SIONS ✦</div>
                            <p class="apx-caste-desc small mb-2" data-i18n="caste_sions_desc">Bangsawan puncak peradaban. Pilar kemakmuran tertinggi realm dengan keistimewaan absolut.</p>
                            <ul class="apx-caste-perks small mb-3">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p1">+15 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p2">Kit Sions Eksklusif</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sions_p3">Siaran Kedatangan Megah</span></li>
                            </ul>
                        </div>
                        <div>
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-gold btn-sm w-100">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 5. Emperor (Weight 60) -->
                    <div class="apx-caste-mini-card d-flex flex-column justify-content-between">
                        <div>
                            <img src="{{ theme_asset('img/ranks/rank-emperor.png') }}" alt="Banner Resmi Kasta Emperor" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_emperor_badge">DONATUR TIER 4</span>
                                <span class="apx-weight-pill">WEIGHT 60</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Emperor</h3>
                            <div class="apx-caste-prefix mb-2"><i class="bi bi-gem"></i> EMPEROR</div>
                            <p class="apx-caste-desc small mb-2" data-i18n="caste_emperor_desc">Bangsawan penakluk berwibawa tinggi. Penguasa langit dengan hak terbang di wilayah klaim.</p>
                            <ul class="apx-caste-perks small mb-3">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p1">Hak Terbang /fly di Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p2">+10 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_emperor_p3">Kit Bulanan Gear Lengkap</span></li>
                            </ul>
                        </div>
                        <div>
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline btn-sm w-100">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 6. Sovereign (Weight 50) -->
                    <div class="apx-caste-mini-card d-flex flex-column justify-content-between">
                        <div>
                            <img src="{{ theme_asset('img/ranks/rank-sovereign.png') }}" alt="Banner Resmi Kasta Sovereign" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="86">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_sovereign_badge">DONATUR TIER 3</span>
                                <span class="apx-weight-pill">WEIGHT 50</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Sovereign</h3>
                            <div class="apx-caste-prefix mb-2"><i class="bi bi-feather"></i> SOVEREIGN</div>
                            <p class="apx-caste-desc small mb-2" data-i18n="caste_sovereign_desc">Tuan tanah emas peradaban. Menguasai jalur niaga bebas tarif dagang lintas kerajaan.</p>
                            <ul class="apx-caste-perks small mb-3">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p1">Bebas Tarif Dagang Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p2">+7 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_sovereign_p3">Kit Sovereign 14 Harian</span></li>
                            </ul>
                        </div>
                        <div>
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline btn-sm w-100">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 7. Archon (Weight 40) -->
                    <div class="apx-caste-mini-card d-flex flex-column justify-content-between">
                        <div>
                            <img src="{{ theme_asset('img/ranks/rank-archon.png') }}" alt="Banner Resmi Kasta Archon" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="87">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_archon_badge">DONATUR TIER 2</span>
                                <span class="apx-weight-pill">WEIGHT 40</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Archon</h3>
                            <div class="apx-caste-prefix mb-2"><i class="bi bi-lightning-charge"></i> ARCHON</div>
                            <p class="apx-caste-desc small mb-2" data-i18n="caste_archon_desc">Kaum perajin kristal dan cendekiawan realm. Menikmati utilitas workbench portabel di mana saja.</p>
                            <ul class="apx-caste-perks small mb-3">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p1">Akses /ec &amp; /anvil Portabel</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p2">+4 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_archon_p3">Kit Mingguan &amp; Kosmetik</span></li>
                            </ul>
                        </div>
                        <div>
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline btn-sm w-100">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- 8. Ascendant (Weight 30) -->
                    <div class="apx-caste-mini-card d-flex flex-column justify-content-between">
                        <div>
                            <img src="{{ theme_asset('img/ranks/rank-ascendant.png') }}" alt="Banner Resmi Kasta Ascendant" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_ascendant_badge">DONATUR TIER 1</span>
                                <span class="apx-weight-pill">WEIGHT 30</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Ascendant</h3>
                            <div class="apx-caste-prefix mb-2"><i class="bi bi-flower1"></i> ASCENDANT</div>
                            <p class="apx-caste-desc small mb-2" data-i18n="caste_ascendant_desc">Warga terhormat yang membuktikan dedikasinya. Prioritas antrean masuk dan perbekalan harian.</p>
                            <ul class="apx-caste-perks small mb-3">
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p1">Bypass Antrean Masuk Server</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p2">+2 Batas Klaim Wilayah</span></li>
                                <li><i class="bi bi-check2"></i> <span data-i18n="caste_ascendant_p3">Kit Ascendant Harian</span></li>
                            </ul>
                        </div>
                        <div>
                            @if(plugins()->isEnabled('shop'))
                                <a href="{{ route('shop.categories.show', 'rank-donatur') }}" class="btn btn-apx-outline btn-sm w-100">
                                    <span data-i18n="caste_btn_webstore_small">Webstore</span> <i class="bi bi-chevron-right ms-1"></i>
                                </a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>

            <!-- Panel 3: Administrasi & Staf (Warden, Herald) -->
            <div class="apx-caste-matrix-panel" data-caste-panel="staff" role="tabpanel">
                <div class="row g-4 align-items-stretch">
                    <!-- 9. Warden (Weight 90) -->
                    <div class="col-md-6">
                        <div class="apx-caste-mini-card h-100 d-flex flex-column justify-content-between">
                            <div>
                                <img src="{{ theme_asset('img/ranks/rank-warden.png') }}" alt="Banner Resmi Kasta Warden" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="85">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="apx-caste-tier-badge text-blue" data-i18n="caste_staff_badge">ADMINISTRASI &amp; STAF</span>
                                    <span class="apx-weight-pill">WEIGHT 90</span>
                                </div>
                                <h3 class="apx-caste-name mb-1">Warden</h3>
                                <div class="apx-caste-prefix text-blue mb-2"><i class="bi bi-shield-shaded"></i> WARDEN</div>
                                <p class="apx-caste-desc small mb-3" data-i18n="caste_warden_desc">
                                    Penjaga gerbang utama dan kepala staf administrasi peradaban di bawah naungan Dewan Otoritas. Mengawasi kestabilan wilayah, tribunal keadilan, dan ketertiban hukum dunia.
                                </p>
                                <ul class="apx-caste-perks small mb-3">
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p1">Otoritas Tribunal &amp; Keamanan Realm</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p2">Pengawasan Integritas Transaksi &amp; War</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_warden_p3">Hak Inspeksi Wilayah Berdaulat</span></li>
                                </ul>
                            </div>
                            <div>
                                @if(plugins()->isEnabled('wiki'))
                                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm w-100">
                                        <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>

                    <!-- 10. Herald (Weight 80) -->
                    <div class="col-md-6">
                        <div class="apx-caste-mini-card h-100 d-flex flex-column justify-content-between">
                            <div>
                                <img src="{{ theme_asset('img/ranks/rank-herald.png') }}" alt="Banner Resmi Kasta Herald" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="84">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="apx-caste-tier-badge text-pink" data-i18n="caste_staff_badge">ADMINISTRASI &amp; STAF</span>
                                    <span class="apx-weight-pill">WEIGHT 80</span>
                                </div>
                                <h3 class="apx-caste-name mb-1">Herald</h3>
                                <div class="apx-caste-prefix text-pink mb-2"><i class="bi bi-chat-quote"></i> HERALD</div>
                                <p class="apx-caste-desc small mb-3" data-i18n="caste_herald_desc">
                                    Utusan resmi, pembawa maklumat peradaban, dan moderator realm. Menjembatani suara warga dengan dewan penguasa dan menjaga etika publik.
                                </p>
                                <ul class="apx-caste-perks small mb-3">
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p1">Moderasi &amp; Diplomasi Publik</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p2">Penegakan Etika Peradaban</span></li>
                                    <li><i class="bi bi-check2"></i> <span data-i18n="caste_herald_p3">Akses Meja Laporan Warga</span></li>
                                </ul>
                            </div>
                            <div>
                                @if(plugins()->isEnabled('wiki'))
                                    <a href="{{ route('wiki.show', 'hierarki-kasta') }}" class="btn btn-apx-outline btn-sm w-100">
                                        <span data-i18n="caste_btn_role">Pelajari Peran</span> <i class="bi bi-chevron-right ms-1"></i>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Panel 4: Fondasi Warga (Wanderer) -->
            <div class="apx-caste-matrix-panel" data-caste-panel="foundation" role="tabpanel">
                <div class="apx-caste-mini-card apx-card-foundation">
                    <img src="{{ theme_asset('img/ranks/rank-wanderer.png') }}" alt="Banner Resmi Kasta Wanderer" class="apx-caste-banner" loading="lazy" decoding="async" width="952" height="88">
                    <div class="row align-items-center gy-3">
                        <div class="col-lg-4 col-md-5">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="apx-caste-tier-badge" data-i18n="caste_wanderer_badge">WARGA PERINTIS</span>
                                <span class="apx-weight-pill">WEIGHT 10</span>
                            </div>
                            <h3 class="apx-caste-name mb-1">Wanderer</h3>
                            <div class="apx-caste-prefix mb-1"><i class="bi bi-compass"></i> WANDERER</div>
                            <span class="text-dim small" data-i18n="caste_wanderer_sub">Pijakan Awal Seluruh Warga Baru</span>
                        </div>
                        <div class="col-lg-5 col-md-7">
                            <p class="apx-caste-desc mb-2" data-i18n="caste_wanderer_desc">
                                Fondasi dan jiwa peradaban Apexsions. Setiap legenda dimulai dari warga baru yang berani menancapkan pijakan pertama di alam liar.
                            </p>
                            <ul class="apx-caste-perks mb-0 small">
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
<section class="apx-section apx-onboarding-section py-5" id="getting-started">
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
                    <button type="button" class="btn btn-apx-outline apx-btn-copy w-100" data-apx-copy="apexsions.my.id:32348" aria-label="Salin Alamat Server Java">
                        <i class="bi bi-laptop me-1"></i> <span data-i18n="step2_copy_java">Salin Java &bull; apexsions.my.id:32348</span>
                    </button>
                    <button type="button" class="btn btn-apx-outline apx-btn-copy w-100" data-apx-copy="IP: apexsions.my.id | Port: 32348" aria-label="Salin IP dan Port Bedrock">
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
                            <a href="{{ route('apexsions-bridge.link.index') }}" class="btn btn-apx-outline apx-btn-copy w-100">
                                <i class="bi bi-controller me-1"></i> <span data-i18n="step3_btn_link">Buka Portal Tautkan</span>
                            </a>
                        @else
                            <span class="text-dim small" data-i18n="step3_registered">Akun Anda Terdaftar</span>
                        @endif
                    @else
                        <a href="{{ route('register') }}" class="btn btn-apx-outline apx-btn-copy w-100">
                            <i class="bi bi-person-plus me-1"></i> <span data-i18n="step3_btn_register">Daftar Akun Peradaban</span>
                        </a>
                    @endauth
                </div>
            </div>
        </div>

        <!-- Callout Banner: Panduan 15 Menit Pertama Warga Baru -->
        <div class="apx-quick-guide-banner mt-4 p-4 text-center position-relative overflow-hidden">
            <div class="row align-items-center">
                <div class="col-lg-8 text-lg-start mb-3 mb-lg-0">
                    <span class="badge mb-2 apx-guide-callout-badge" data-i18n="guide_callout_badge">
                        <i class="bi bi-compass-fill me-1"></i> PANDUAN KILAT WARGA BARU
                    </span>
                    <h3 class="mb-1 apx-guide-callout-title" data-i18n="guide_callout_title">
                        Bingung Harus Melakukan Apa Setelah Mendarat di Spawn?
                    </h3>
                    <p class="apx-guide-callout-desc small mb-0" data-i18n="guide_callout_desc">
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

<!-- Sticky Horizon Quick-Dock (Floating Page Navigation) -->
<nav class="apx-horizon-dock" id="apxHorizonDock" aria-label="Navigasi Cepat Halaman">
    <div class="apx-dock-container">
        <a href="#civilizations" class="apx-dock-item" data-dock-target="civilizations" title="Kerajaan">
            <i class="bi bi-shield-shaded apx-dock-icon"></i>
            <span class="apx-dock-label" data-i18n="dock_civ">Kerajaan</span>
        </a>
        @if(\Azuriom\Plugin\ApexsionsBridge\Services\ServerMapService::isMapEnabled())
        <a href="#server-map" class="apx-dock-item" data-dock-target="server-map" title="Peta 3D">
            <i class="bi bi-map apx-dock-icon"></i>
            <span class="apx-dock-label" data-i18n="dock_map">Peta 3D</span>
        </a>
        @endif
        <a href="#ranks" class="apx-dock-item" data-dock-target="ranks" title="Kasta">
            <i class="bi bi-crown apx-dock-icon"></i>
            <span class="apx-dock-label" data-i18n="dock_ranks">Kasta</span>
        </a>
        <a href="#getting-started" class="apx-dock-item" data-dock-target="getting-started" title="Cara Masuk">
            <i class="bi bi-play-circle-fill apx-dock-icon"></i>
            <span class="apx-dock-label" data-i18n="dock_join">Cara Masuk</span>
        </a>
    </div>
</nav>
@endsection
