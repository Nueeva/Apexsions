@extends('admin.layouts.admin')

@section('title', 'Kingdom Market & Shop Admin — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1" data-i18n="admin_market_title">
                <i class="bi bi-shop-window text-warning me-2"></i>Kingdom Market & Shop Admin
            </h2>
            <p class="text-white-50 small mb-0" data-i18n="admin_market_subtitle">
                Kendali terpusat perbankan komoditas, tarif pajak 3 kerajaan, kurva saturasi pasokan, dan katalog item in-game.
            </p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <form action="{{ route('apexsions-bridge.admin.market.sync-now') }}" method="POST" class="d-inline">
                @csrf
                <button type="submit" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="tooltip" title="Kirim instruksi reload ke Minecraft Server via WebBridge">
                    <i class="bi bi-arrow-repeat me-1"></i><span data-i18n="admin_market_sync_btn">Sinkronkan ke Game Server</span>
                </button>
            </form>
            <button type="button" class="btn btn-outline-danger btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#resetDefaultsModal">
                <i class="bi bi-arrow-counterclockwise me-1"></i><span data-i18n="admin_market_reset_btn">Reset ke Standar Seimbang</span>
            </button>
        </div>
    </div>

    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2 text-success"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    @if(session('error'))
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2 text-danger"></i>{{ session('error') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <!-- Metric Overview Cards -->
    <div class="row g-3 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase" data-i18n="admin_market_total_items">Total Komoditas</span>
                        <i class="bi bi-box-seam text-warning"></i>
                    </div>
                    <h3 class="h4 fw-bold text-white mb-0">{{ $totalItems }} <span class="fs-6 text-white-50 fw-normal">Item</span></h3>
                    <span class="badge bg-secondary bg-opacity-25 text-warning font-monospace mt-2" style="font-size: 0.72rem;">
                        6 Kategori Pasar
                    </span>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase" data-i18n="admin_market_avg_ratio">Rata-Rata Rasio Jual</span>
                        <i class="bi bi-percent text-info"></i>
                    </div>
                    <h3 class="h4 fw-bold text-info mb-0">{{ $avgSellRatio }}% <span class="fs-6 text-white-50 fw-normal">dari Beli</span></h3>
                    <span class="badge bg-info bg-opacity-10 text-info font-monospace mt-2" style="font-size: 0.72rem;">
                        Baseline Standar: 20%
                    </span>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase" data-i18n="admin_market_taxes">Tarif Pajak Kerajaan</span>
                        <i class="bi bi-bank2 text-warning"></i>
                    </div>
                    <div class="d-flex gap-2 align-items-center mt-1">
                        <span class="badge bg-warning bg-opacity-25 text-warning font-monospace">ZEN {{ $settings['kingdoms']['ZENITHAR']['tax_percent'] }}%</span>
                        <span class="badge bg-danger bg-opacity-25 text-danger font-monospace">SOL {{ $settings['kingdoms']['SOLTERRA']['tax_percent'] }}%</span>
                        <span class="badge bg-success bg-opacity-25 text-success font-monospace">SYL {{ $settings['kingdoms']['SYLVAMOOR']['tax_percent'] }}%</span>
                    </div>
                    <span class="text-white-50 small d-block mt-2 font-monospace" style="font-size: 0.7rem;">
                        Masuk ke Kas Kerajaan
                    </span>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase" data-i18n="admin_market_bridge_status">WebBridge Status</span>
                        <i class="bi bi-hdd-network text-success"></i>
                    </div>
                    <h3 class="h5 fw-bold mb-0">
                        <span class="badge {{ $serverStatus['badge_class'] ?? 'bg-success' }}">
                            {{ $serverStatus['status'] ?? 'CONNECTED' }}
                        </span>
                    </h3>
                    <span class="text-white-50 small d-block mt-2 font-monospace" style="font-size: 0.7rem;">
                        Endpoint: /api/apexsions-bridge/shop/config
                    </span>
                </div>
            </div>
        </div>
    </div>

    <!-- Navigation Tabs -->
    @php
        $currentTab = request()->input('tab', 'kingdoms');
    @endphp
    <ul class="nav nav-tabs border-secondary border-opacity-25 mb-4" id="marketTabs" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link {{ $currentTab === 'kingdoms' ? 'active text-warning fw-bold' : 'text-white-50' }}" id="kingdoms-tab" data-bs-toggle="tab" data-bs-target="#tab-kingdoms" type="button" role="tab">
                <i class="bi bi-shield-shaded me-1"></i><span data-i18n="tab_kingdoms">Tiga Kerajaan &amp; Pajak</span>
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link {{ $currentTab === 'dynamics' ? 'active text-warning fw-bold' : 'text-white-50' }}" id="dynamics-tab" data-bs-toggle="tab" data-bs-target="#tab-dynamics" type="button" role="tab">
                <i class="bi bi-sliders me-1"></i><span data-i18n="tab_dynamics">Pasar Dinamis &amp; Cuaca</span>
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link {{ $currentTab === 'catalog' ? 'active text-warning fw-bold' : 'text-white-50' }}" id="catalog-tab" data-bs-toggle="tab" data-bs-target="#tab-catalog" type="button" role="tab">
                <i class="bi bi-grid-3x3-gap me-1"></i><span data-i18n="tab_catalog">Katalog Item &amp; Harga</span>
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link {{ $currentTab === 'sync' ? 'active text-warning fw-bold' : 'text-white-50' }}" id="sync-tab" data-bs-toggle="tab" data-bs-target="#tab-sync" type="button" role="tab">
                <i class="bi bi-cloud-arrow-up me-1"></i><span data-i18n="tab_sync">Sinkronisasi &amp; API</span>
            </button>
        </li>
    </ul>

    <!-- Tab Contents -->
    <div class="tab-content" id="marketTabsContent">

        <!-- ======================================================== -->
        <!-- TAB 1: KINGDOM MARKETS -->
        <!-- ======================================================== -->
        <div class="tab-pane fade {{ $currentTab === 'kingdoms' ? 'show active' : '' }}" id="tab-kingdoms" role="tabpanel">
            <form action="{{ route('apexsions-bridge.admin.market.kingdoms.update') }}" method="POST">
                @csrf
                <div class="row g-4 mb-4">
                    <!-- Solterra -->
                    <div class="col-lg-4">
                        <div class="card bg-dark border-danger border-opacity-50 shadow-sm h-100">
                            <div class="card-header bg-danger bg-opacity-10 border-bottom border-danger border-opacity-25 py-3">
                                <h5 class="card-title text-danger h6 mb-0 fw-bold">
                                    <i class="bi bi-fire me-2"></i>SOLTERRA (Magisi &amp; Prajurit)
                                </h5>
                                <small class="text-white-50">Cadas Vulkanik, Ignis Bastion, Spesialis Pertambangan</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pajak Kerajaan (%):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="0" max="100" class="form-control bg-dark text-white border-secondary" name="kingdoms[SOLTERRA][tax_percent]" value="{{ $settings['kingdoms']['SOLTERRA']['tax_percent'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">%</span>
                                    </div>
                                    <small class="text-white-50">Tarif pajak dipotong otomatis saat transaksi toko.</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Beli Ore (Ores Buy Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[SOLTERRA][ores_buy_multiplier]" value="{{ $settings['kingdoms']['SOLTERRA']['ores_buy_multiplier'] }}" required>
                                    <small class="text-white-50">Standar: 1.15 (Ore bernilai tinggi dan stabil di Solterra).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Rasio Jual Ore (Ores Sell Ratio):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.01" min="0.01" max="1.0" class="form-control bg-dark text-white border-secondary" name="kingdoms[SOLTERRA][ores_sell_ratio]" value="{{ $settings['kingdoms']['SOLTERRA']['ores_sell_ratio'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">x Beli</span>
                                    </div>
                                    <small class="text-white-50">Standar: 0.30 (30% dari harga beli, unggul dibanding 20%).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Makanan &amp; Tani (Buy Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[SOLTERRA][food_buy_multiplier]" value="{{ $settings['kingdoms']['SOLTERRA']['food_buy_multiplier'] ?? 0.90 }}" required>
                                    <small class="text-white-50">Standar: 0.90 (Makanan murah 10% di tanah cadas).</small>
                                </div>
                                <div class="form-check form-switch mt-3">
                                    <input class="form-check-input" type="checkbox" name="kingdoms[SOLTERRA][ores_stability]" value="1" id="solterraStability" {{ !empty($settings['kingdoms']['SOLTERRA']['ores_stability']) ? 'checked' : '' }}>
                                    <label class="form-check-label text-white small fw-bold" for="solterraStability">
                                        Stabilitas Harga Ore (Kebal Saturasi)
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Zenithar -->
                    <div class="col-lg-4">
                        <div class="card bg-dark border-warning border-opacity-50 shadow-sm h-100">
                            <div class="card-header bg-warning bg-opacity-10 border-bottom border-warning border-opacity-25 py-3">
                                <h5 class="card-title text-warning h6 mb-0 fw-bold">
                                    <i class="bi bi-gem me-2"></i>ZENITHAR (Dinasti &amp; Langit)
                                </h5>
                                <small class="text-white-50">Puncak Cakrawala, Solarium Spire, Sentra Perbankan &amp; Kemewahan</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pajak Kerajaan (%):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="0" max="100" class="form-control bg-dark text-white border-secondary" name="kingdoms[ZENITHAR][tax_percent]" value="{{ $settings['kingdoms']['ZENITHAR']['tax_percent'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">%</span>
                                    </div>
                                    <small class="text-white-50">Standar: 18.0% (Sistem perbankan kuat memotong pajak wajar).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Diskon Blok Kemewahan &amp; Marmer (Blocks Buy Multiplier):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.05" min="0.1" max="5.0" class="form-control bg-dark text-white border-secondary" name="kingdoms[ZENITHAR][blocks_buy_multiplier]" value="{{ $settings['kingdoms']['ZENITHAR']['blocks_buy_multiplier'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">x Beli</span>
                                    </div>
                                    <small class="text-white-50">Standar: 0.85 (Diskon 15% blok bangunan &amp; marmer istana).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Volatilitas Pasar (Volatility Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[ZENITHAR][volatility_multiplier]" value="{{ $settings['kingdoms']['ZENITHAR']['volatility_multiplier'] ?? 1.05 }}" required>
                                    <small class="text-white-50">Standar: 1.05 (Ketahanan pasar istana terhadap fluktuasi liar).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Pewarna (Dyes Buy Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[ZENITHAR][dyes_buy_multiplier]" value="{{ $settings['kingdoms']['ZENITHAR']['dyes_buy_multiplier'] ?? 0.90 }}" required>
                                    <small class="text-white-50">Standar: 0.90 (Diskon 10% sutra dan pewarna istana).</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Sylvamoor -->
                    <div class="col-lg-4">
                        <div class="card bg-dark border-success border-opacity-50 shadow-sm h-100">
                            <div class="card-header bg-success bg-opacity-10 border-bottom border-success border-opacity-25 py-3">
                                <h5 class="card-title text-success h6 mb-0 fw-bold">
                                    <i class="bi bi-tree me-2"></i>SYLVAMOOR (Pekerja &amp; Lumbung Rimba)
                                </div>
                            <div class="card-body p-3">
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pajak Kerajaan (%):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.5" min="0" max="100" class="form-control bg-dark text-white border-secondary" name="kingdoms[SYLVAMOOR][tax_percent]" value="{{ $settings['kingdoms']['SYLVAMOOR']['tax_percent'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">%</span>
                                    </div>
                                    <small class="text-white-50">Standar: 15.0% (Pajak paling ramah rakyat pekerja rimba).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Hasil Tani (Farming Buy Multiplier):</label>
                                    <div class="input-group input-group-sm">
                                        <input type="number" step="0.05" min="0.1" max="5.0" class="form-control bg-dark text-white border-secondary" name="kingdoms[SYLVAMOOR][farming_buy_multiplier]" value="{{ $settings['kingdoms']['SYLVAMOOR']['farming_buy_multiplier'] }}" required>
                                        <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">x Beli</span>
                                    </div>
                                    <small class="text-white-50">Standar: 0.90 (Hasil tani terjangkau murah 10%).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Pewarna &amp; Tanaman Liar (Dyes Buy Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[SYLVAMOOR][dyes_buy_multiplier]" value="{{ $settings['kingdoms']['SYLVAMOOR']['dyes_buy_multiplier'] }}" required>
                                    <small class="text-white-50">Standar: 0.85 (Diskon 15% tanaman liar dan pewarna alam).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Pengali Drop Monster (Mob Drops Buy Multiplier):</label>
                                    <input type="number" step="0.05" min="0.1" max="5.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="kingdoms[SYLVAMOOR][mob_drops_buy_multiplier]" value="{{ $settings['kingdoms']['SYLVAMOOR']['mob_drops_buy_multiplier'] ?? 0.90 }}" required>
                                    <small class="text-white-50">Standar: 0.90 (Penjaga rimba terbiasa berburu monster).</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="text-end">
                    <button type="submit" class="btn btn-warning shadow-sm fw-bold text-dark px-4">
                        <i class="bi bi-save me-1"></i>Simpan Parameter Kerajaan &amp; Sinkronkan
                    </button>
                </div>
            </form>
        </div>

        <!-- ======================================================== -->
        <!-- TAB 2: DYNAMIC MARKET & WEATHER -->
        <!-- ======================================================== -->
        <div class="tab-pane fade {{ $currentTab === 'dynamics' ? 'show active' : '' }}" id="tab-dynamics" role="tabpanel">
            <form action="{{ route('apexsions-bridge.admin.market.dynamics.update') }}" method="POST">
                @csrf
                <div class="row g-4 mb-4">
                    <!-- Saturation Curve -->
                    <div class="col-lg-6">
                        <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                            <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                                <h5 class="card-title text-white h6 mb-0 fw-bold">
                                    <i class="bi bi-graph-down text-warning me-2"></i>Saturasi Pasokan (Supply Market Engine)
                                </h5>
                                <small class="text-white-50">Mencegah player menimbun kekayaan instan saat menjual komoditas massal.</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="form-check form-switch mb-3">
                                    <input class="form-check-input" type="checkbox" name="supply_market[enabled]" value="1" id="supplyEnabled" {{ !empty($settings['supply_market']['enabled']) ? 'checked' : '' }}>
                                    <label class="form-check-label text-white small fw-bold" for="supplyEnabled">
                                        Aktifkan Sistem Penurunan Harga Pasokan Jenuh
                                    </label>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Sensitivitas Kurva Logaritmik:</label>
                                    <input type="number" step="0.005" min="0.001" max="0.5" class="form-control form-control-sm bg-dark text-white border-secondary" name="supply_market[sensitivity]" value="{{ $settings['supply_market']['sensitivity'] }}" required>
                                    <small class="text-white-50">Standar: 0.025 (Semakin kecil, penurunan harga berlangsung semakin halus).</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-white small fw-bold">Ambang Batas Volume Dasar (Butir Item):</label>
                                    <input type="number" step="64" min="64" max="100000" class="form-control form-control-sm bg-dark text-white border-secondary" name="supply_market[base_volume_threshold]" value="{{ $settings['supply_market']['base_volume_threshold'] }}" required>
                                    <small class="text-white-50">Standar: 2304 butir (setara 36 stack atau 1 inventori penuh pemain).</small>
                                </div>
                                <div class="row g-2 mb-3">
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Batas Penurunan Maks:</label>
                                        <div class="input-group input-group-sm">
                                            <input type="number" step="0.01" min="0.01" max="0.8" class="form-control bg-dark text-white border-secondary" name="supply_market[max_saturation_drop]" value="{{ $settings['supply_market']['max_saturation_drop'] }}" required>
                                            <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">%</span>
                                        </div>
                                        <small class="text-white-50">0.10 = maks turun 10%.</small>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Pengali Jual Minimum:</label>
                                        <input type="number" step="0.05" min="0.1" max="1.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="supply_market[min_sell_multiplier]" value="{{ $settings['supply_market']['min_sell_multiplier'] }}" required>
                                        <small class="text-white-50">0.90 = harga minimal 90%.</small>
                                    </div>
                                </div>
                                <div class="row g-2">
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Interval Pemulihan (Menit):</label>
                                        <input type="number" min="1" max="120" class="form-control form-control-sm bg-dark text-white border-secondary" name="supply_market[recovery_interval_minutes]" value="{{ $settings['supply_market']['recovery_interval_minutes'] }}" required>
                                        <small class="text-white-50">Standar: 10 menit.</small>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">% Pemulihan / Interval:</label>
                                        <div class="input-group input-group-sm">
                                            <input type="number" step="1" min="1" max="100" class="form-control bg-dark text-white border-secondary" name="supply_market[recovery_percent_per_interval]" value="{{ $settings['supply_market']['recovery_percent_per_interval'] }}" required>
                                            <span class="input-group-text bg-secondary bg-opacity-25 text-white border-secondary">%</span>
                                        </div>
                                        <small class="text-white-50">Standar: 25.0%.</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Price Clamping & Weather -->
                    <div class="col-lg-6">
                        <!-- Clamping -->
                        <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                            <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                                <h5 class="card-title text-white h6 mb-0 fw-bold">
                                    <i class="bi bi-shield-lock text-info me-2"></i>Batas Perlindungan Harga (Price Clamping)
                                </h5>
                                <small class="text-white-50">Mengunci fluktuasi harga agar tidak pernah melambung atau anjlok drastis.</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="row g-2 mb-2">
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Min Buy Ratio:</label>
                                        <input type="number" step="0.05" min="0.1" max="1.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="clamping[min_buy_ratio]" value="{{ $settings['clamping']['min_buy_ratio'] }}" required>
                                        <small class="text-white-50">Standar: 0.85 (Minimal 85%).</small>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Max Buy Ratio:</label>
                                        <input type="number" step="0.05" min="1.0" max="3.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="clamping[max_buy_ratio]" value="{{ $settings['clamping']['max_buy_ratio'] }}" required>
                                        <small class="text-white-50">Standar: 1.20 (Maksimal 120%).</small>
                                    </div>
                                </div>
                                <div class="row g-2">
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Min Sell Ratio:</label>
                                        <input type="number" step="0.05" min="0.1" max="1.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="clamping[min_sell_ratio]" value="{{ $settings['clamping']['min_sell_ratio'] }}" required>
                                        <small class="text-white-50">Standar: 0.85 (Minimal 85%).</small>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Max Sell Ratio:</label>
                                        <input type="number" step="0.05" min="1.0" max="3.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="clamping[max_sell_ratio]" value="{{ $settings['clamping']['max_sell_ratio'] }}" required>
                                        <small class="text-white-50">Standar: 1.20 (Maksimal 120%).</small>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Weather Dynamics -->
                        <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
                            <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                                <h5 class="card-title text-white h6 mb-0 fw-bold">
                                    <i class="bi bi-cloud-sun text-warning me-2"></i>Dinamika Cuaca Realm (Weather Pricing)
                                </h5>
                                <small class="text-white-50">Mengubah harga komoditas secara realistis saat cuaca hujan atau badai petir.</small>
                            </div>
                            <div class="card-body p-3">
                                <div class="form-check form-switch mb-3">
                                    <input class="form-check-input" type="checkbox" name="weather[enabled]" value="1" id="weatherEnabled" {{ !empty($settings['weather']['enabled']) ? 'checked' : '' }}>
                                    <label class="form-check-label text-white small fw-bold" for="weatherEnabled">
                                        Aktifkan Pengaruh Cuaca Terhadap Harga Jual
                                    </label>
                                </div>
                                <div class="row g-2">
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Cerah: Pengali Tani:</label>
                                        <input type="number" step="0.05" min="0.1" max="3.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="weather[clear][farming_sell_multiplier]" value="{{ $settings['weather']['clear']['farming_sell_multiplier'] }}" required>
                                        <small class="text-white-50">Standar: 1.10 (+10% panen cerah).</small>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label text-white small fw-bold">Badai: Pengali Drop Monster:</label>
                                        <input type="number" step="0.05" min="0.1" max="3.0" class="form-control form-control-sm bg-dark text-white border-secondary" name="weather[thunder][mob_sell_multiplier]" value="{{ $settings['weather']['thunder']['mob_sell_multiplier'] }}" required>
                                        <small class="text-white-50">Standar: 1.15 (+15% monster ganas).</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="text-end">
                    <button type="submit" class="btn btn-warning shadow-sm fw-bold text-dark px-4">
                        <i class="bi bi-save me-1"></i>Simpan Dinamika Pasar &amp; Sinkronkan
                    </button>
                </div>
            </form>
        </div>

        <!-- ======================================================== -->
        <!-- TAB 3: ITEM CATALOG & PRICE EDITOR -->
        <!-- ======================================================== -->
        <div class="tab-pane fade {{ $currentTab === 'catalog' ? 'show active' : '' }}" id="tab-catalog" role="tabpanel">
            <!-- Category Tabs / Pills -->
            <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div class="btn-group btn-group-sm" role="group">
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'ores']) }}" class="btn {{ $activeCategory === 'ores' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-diamond-fill me-1"></i>Pertambangan (Ores)
                    </a>
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'farming']) }}" class="btn {{ $activeCategory === 'farming' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-flower1 me-1"></i>Pertanian (Farming)
                    </a>
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'food']) }}" class="btn {{ $activeCategory === 'food' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-cup-straw me-1"></i>Makanan (Food)
                    </a>
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'mob_drops']) }}" class="btn {{ $activeCategory === 'mob_drops' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-droplet-fill me-1"></i>Drop Monster
                    </a>
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'blocks']) }}" class="btn {{ $activeCategory === 'blocks' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-bricks me-1"></i>Blok &amp; Kayu
                    </a>
                    <a href="{{ route('apexsions-bridge.admin.market.index', ['tab' => 'catalog', 'category' => 'dyes']) }}" class="btn {{ $activeCategory === 'dyes' ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white-50' }}">
                        <i class="bi bi-palette-fill me-1"></i>Pewarna (Dyes)
                    </a>
                </div>

                <!-- Batch Action Modal Trigger -->
                <button type="button" class="btn btn-outline-warning btn-sm" data-bs-toggle="modal" data-bs-target="#batchModal">
                    <i class="bi bi-magic me-1"></i>Penyesuaian Massal (Batch)
                </button>
            </div>

            <!-- Items Table -->
            @php
                $categoryItems = $categories[$activeCategory] ?? [];
            @endphp
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover mb-0 align-middle">
                            <thead>
                                <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                                    <th class="ps-3" style="width: 25%;">Komoditas</th>
                                    <th style="width: 15%;">Material In-Game</th>
                                    <th style="width: 15%;">Harga Beli (Buy)</th>
                                    <th style="width: 15%;">Harga Jual (Sell)</th>
                                    <th style="width: 15%;">Rasio Jual</th>
                                    <th style="width: 8%;" class="text-center">Tersedia</th>
                                    <th class="pe-3 text-end" style="width: 7%;">Aksi</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($categoryItems as $itemId => $item)
                                    <tr>
                                        <td class="ps-3 fw-bold text-white">
                                            <span class="text-warning">{{ $item['display_name'] }}</span>
                                            <span class="d-block text-white-50 font-monospace" style="font-size: 0.72rem;">{{ $itemId }}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary bg-opacity-25 text-white font-monospace">{{ $item['material'] }}</span>
                                        </td>
                                        <td class="text-white fw-bold font-monospace">
                                            Rp {{ number_format($item['buy_price'], 2, ',', '.') }}
                                        </td>
                                        <td class="text-warning fw-bold font-monospace">
                                            Rp {{ number_format($item['sell_price'], 2, ',', '.') }}
                                        </td>
                                        <td>
                                            <span class="badge {{ $item['sell_ratio'] > 25 ? 'bg-success' : 'bg-secondary' }} bg-opacity-25 text-white font-monospace">
                                                {{ $item['sell_ratio'] }}%
                                            </span>
                                        </td>
                                        <td class="text-center">
                                            @if($item['buy_enabled'])
                                                <i class="bi bi-check-circle-fill text-success fs-5" title="Tersedia di Toko"></i>
                                            @else
                                                <i class="bi bi-x-circle-fill text-danger fs-5" title="Dinonaktifkan"></i>
                                            @endif
                                        </td>
                                        <td class="pe-3 text-end">
                                            <button type="button" class="btn btn-outline-warning btn-sm" data-bs-toggle="modal" data-bs-target="#editItemModal_{{ $itemId }}">
                                                <i class="bi bi-pencil-square"></i>
                                            </button>
                                        </td>
                                    </tr>

                                    <!-- Edit Single Item Modal -->
                                    <div class="modal fade" id="editItemModal_{{ $itemId }}" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content bg-dark border-secondary">
                                                <form action="{{ route('apexsions-bridge.admin.market.items.update') }}" method="POST">
                                                    @csrf
                                                    <input type="hidden" name="item_id" value="{{ $itemId }}">
                                                    <div class="modal-header border-secondary border-opacity-25">
                                                        <h5 class="modal-title text-white h6 fw-bold">
                                                            <i class="bi bi-pencil-square text-warning me-2"></i>Edit Komoditas: {{ $item['display_name'] }}
                                                        </h5>
                                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label class="form-label text-white small fw-bold">Nama Tampilan (Display Name):</label>
                                                            <input type="text" class="form-control bg-dark text-white border-secondary" name="display_name" value="{{ $item['display_name'] }}" required>
                                                        </div>
                                                        <div class="row g-2 mb-3">
                                                            <div class="col-6">
                                                                <label class="form-label text-white small fw-bold">Harga Beli (Buy Price Rp):</label>
                                                                <input type="number" step="0.1" min="0.1" class="form-control bg-dark text-white border-secondary font-monospace" name="buy_price" value="{{ $item['buy_price'] }}" required>
                                                            </div>
                                                            <div class="col-6">
                                                                <label class="form-label text-white small fw-bold">Harga Jual (Sell Price Rp):</label>
                                                                <input type="number" step="0.05" min="0.05" class="form-control bg-dark text-white border-secondary font-monospace" name="sell_price" value="{{ $item['sell_price'] }}" required>
                                                            </div>
                                                        </div>
                                                        <div class="form-check form-switch mt-3">
                                                            <input class="form-check-input" type="checkbox" name="buy_enabled" value="1" id="buyCheck_{{ $itemId }}" {{ $item['buy_enabled'] ? 'checked' : '' }}>
                                                            <label class="form-check-label text-white small fw-bold" for="buyCheck_{{ $itemId }}">
                                                                Izinkan Pembelian di Toko In-Game
                                                            </label>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer border-secondary border-opacity-25">
                                                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                                                        <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Simpan Perubahan</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                @empty
                                    <tr>
                                        <td colspan="7" class="p-4 text-center text-white-50">
                                            Tidak ada komoditas dalam kategori ini.
                                        </td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- ======================================================== -->
        <!-- TAB 4: SYNC & API STATUS -->
        <!-- ======================================================== -->
        <div class="tab-pane fade {{ $currentTab === 'sync' ? 'show active' : '' }}" id="tab-sync" role="tabpanel">
            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                            <h5 class="card-title text-white h6 mb-0 fw-bold">
                                <i class="bi bi-hdd-network text-warning me-2"></i>Arsitektur Sinkronisasi Jembatan (WebBridge)
                            </h5>
                        </div>
                        <div class="card-body p-3">
                            <p class="text-white-50 small">
                                Perubahan harga atau parameter pasar yang Anda simpan di panel web ini secara otomatis:
                            </p>
                            <ol class="text-white-50 small ps-3 mb-3">
                                <li class="mb-1"><strong class="text-white">Disimpan di Database Azuriom:</strong> Tersimpan secara permanen dalam sistem setting web platform.</li>
                                <li class="mb-1"><strong class="text-white">Terekam di Audit Log:</strong> Setiap mutasi nilai dicatat lengkap dengan nama admin, IP address, nilai lama, dan nilai baru.</li>
                                <li class="mb-1"><strong class="text-white">Di-push via Delivery Queue:</strong> Perintah <code class="text-warning">shop reload</code> dikirim ke server Minecraft secara asinkron (lease timeout 60s).</li>
                                <li class="mb-1"><strong class="text-white">Tersedia via REST API:</strong> Daemon plugin in-game dapat mem-fetch konfigurasi JSON lengkap via endpoint resmi.</li>
                            </ol>
                            <div class="p-3 bg-black bg-opacity-50 rounded border border-secondary border-opacity-25 font-monospace text-warning small">
                                GET http://web.apexsions.my.id/api/apexsions-bridge/shop/config<br>
                                Header: X-Apexsions-Key: apexsions_bridge_key_live_2026
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-6">
                    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                            <h5 class="card-title text-white h6 mb-0 fw-bold">
                                <i class="bi bi-terminal text-info me-2"></i>Panduan Perintah Konsol &amp; In-Game
                            </h5>
                        </div>
                        <div class="card-body p-3">
                            <div class="table-responsive">
                                <table class="table table-dark table-sm table-borderless mb-0 align-middle">
                                    <tbody>
                                        <tr class="border-bottom border-secondary border-opacity-10">
                                            <td><code class="text-warning">/shopadmin</code></td>
                                            <td class="text-white-50 small">Membuka GUI selektor 3 pasar kerajaan secara in-game.</td>
                                        </tr>
                                        <tr class="border-bottom border-secondary border-opacity-10">
                                            <td><code class="text-warning">/shopadmin view &lt;kingdom&gt;</code></td>
                                            <td class="text-white-50 small">Pratinjau katalog pasar kerajaan (zenithar, solterra, sylvamoor).</td>
                                        </tr>
                                        <tr class="border-bottom border-secondary border-opacity-10">
                                            <td><code class="text-warning">/shopadmin reload</code></td>
                                            <td class="text-white-50 small">Memuat ulang konfigurasi toko dari disk/cache.</td>
                                        </tr>
                                        <tr>
                                            <td><code class="text-warning">/shopadmin sync</code></td>
                                            <td class="text-white-50 small">Menarik konfigurasi terbaru langsung dari API WebBridge.</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>

<!-- Batch Update Modal -->
<div class="modal fade" id="batchModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary">
            <form action="{{ route('apexsions-bridge.admin.market.items.batch') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title text-white h6 fw-bold">
                        <i class="bi bi-magic text-warning me-2"></i>Penyesuaian Harga Massal (Batch Update)
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label text-white small fw-bold">Target Kategori:</label>
                        <select class="form-select bg-dark text-white border-secondary" name="category" required>
                            <option value="all">Semua Kategori (Seluruh Katalog)</option>
                            <option value="ores" {{ $activeCategory === 'ores' ? 'selected' : '' }}>Pertambangan (Ores)</option>
                            <option value="farming" {{ $activeCategory === 'farming' ? 'selected' : '' }}>Pertanian (Farming)</option>
                            <option value="food" {{ $activeCategory === 'food' ? 'selected' : '' }}>Makanan (Food)</option>
                            <option value="mob_drops" {{ $activeCategory === 'mob_drops' ? 'selected' : '' }}>Drop Monster</option>
                            <option value="blocks" {{ $activeCategory === 'blocks' ? 'selected' : '' }}>Blok &amp; Kayu</option>
                            <option value="dyes" {{ $activeCategory === 'dyes' ? 'selected' : '' }}>Pewarna (Dyes)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-white small fw-bold">Jenis Penyesuaian:</label>
                        <select class="form-select bg-dark text-white border-secondary" name="type" required>
                            <option value="ratio">Tetapkan Rasio Jual terhadap Beli (misal 0.20 = 20%)</option>
                            <option value="percent">Ubah Harga Beli &amp; Jual secara Persentase (+/- %)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label text-white small fw-bold">Nilai Penyesuaian:</label>
                        <input type="number" step="0.01" class="form-control bg-dark text-white border-secondary font-monospace" name="value" placeholder="Contoh: 0.25 untuk rasio 25%, atau 10 untuk +10%" required>
                        <small class="text-white-50">Untuk rasio: masukkan angka 0.01 s/d 1.0 (misal 0.20 = 20%). Untuk persentase: masukkan angka seperti 10 (+10%) atau -15 (-15%).</small>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Terapkan Massal &amp; Sinkronkan</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Reset Defaults Confirmation Modal -->
<div class="modal fade" id="resetDefaultsModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-danger">
            <form action="{{ route('apexsions-bridge.admin.market.reset-defaults') }}" method="POST">
                @csrf
                <div class="modal-header border-danger border-opacity-25">
                    <h5 class="modal-title text-danger h6 fw-bold">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>Konfirmasi Reset Baseline Pasar
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body text-white">
                    <p class="small mb-2">
                        Tindakan ini akan mengembalikan seluruh parameter ketiga kerajaan, kurva saturasi pasokan, batas clamping, serta harga beli dan jual seluruh komoditas ke standar dasar seimbang (*balanced default*).
                    </p>
                    <p class="small text-danger fw-bold mb-0">
                        Perubahan kustom yang telah Anda buat akan ditimpa!
                    </p>
                </div>
                <div class="modal-footer border-danger border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger btn-sm fw-bold">Ya, Reset ke Standar Seimbang</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
