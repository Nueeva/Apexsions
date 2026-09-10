@extends('layouts.app')

@section('title', 'Server Map Apexsions | Live Minecraft World Map')
@section('description', 'Jelajahi dunia Apexsions melalui peta Minecraft interaktif dan temukan berbagai wilayah serta kerajaan di server.')

@section('content')
<div class="container py-4 py-md-5">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb apx-breadcrumb mb-0">
            <li class="breadcrumb-item"><a href="{{ route('home') }}"><i class="bi bi-house-door me-1"></i> Beranda</a></li>
            <li class="breadcrumb-item active" aria-current="page">Server Map</li>
        </ol>
    </nav>

    <!-- Header Hero Banner -->
    <div class="card p-4 p-md-5 mb-4 border border-warning border-opacity-25 position-relative overflow-hidden" style="background: radial-gradient(circle at 10% 20%, rgba(20, 24, 33, 0.98), rgba(9, 12, 19, 0.99)); box-shadow: 0 16px 40px rgba(0,0,0,0.6); border-radius: var(--apx-radius-lg);">
        <div class="position-absolute top-0 end-0 p-4 d-none d-lg-block opacity-10" style="font-size: 15rem; transform: translate(25%, -25%); pointer-events: none;">
            <i class="bi bi-map text-warning"></i>
        </div>

        <div class="row align-items-center position-relative" style="z-index: 2;">
            <div class="col-lg-8">
                <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
                    <span class="badge bg-warning bg-opacity-20 text-warning border border-warning border-opacity-30 px-3 py-1 font-monospace">
                        <i class="bi bi-compass-fill me-1"></i> LIVE INTERACTIVE 3D
                    </span>
                    @if($isOnline)
                        <span class="badge bg-success bg-opacity-25 text-success border border-success border-opacity-40 px-3 py-1 d-flex align-items-center gap-1">
                            <span class="spinner-grow spinner-grow-sm text-success" style="width: 0.55rem; height: 0.55rem;" role="status"></span>
                            <span>🟢 Map Online</span>
                        </span>
                    @else
                        <span class="badge bg-secondary bg-opacity-25 text-muted border border-secondary border-opacity-30 px-3 py-1">
                            ⚪ Map Offline / Standby
                        </span>
                    @endif
                    <span class="badge bg-black bg-opacity-50 text-white-50 border border-secondary border-opacity-20 px-2 py-1 small font-monospace">
                        Port :32076
                    </span>
                </div>

                <h1 class="font-cinzel text-white mb-3" style="font-size: clamp(2rem, 4vw, 2.8rem); letter-spacing: 0.04em;">
                    Server Map Apexsions
                </h1>
                <p class="text-muted lead mb-4" style="font-size: 1.05rem; line-height: 1.75; max-width: 720px;">
                    Jelajahi bentang alam dunia Apexsions secara langsung melalui visualisasi interaktif. Temukan batas kedaulatan tiga kerajaan agung (Zenithar, Solterra, Sylvamoor), pusat spawn peradaban, benteng pertahanan, dan lokasi strategis secara real-time dari browser Anda.
                </p>

                <div class="d-flex flex-wrap gap-3">
                    <a href="{{ $mapUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-gold px-4 py-3 fw-bold d-inline-flex align-items-center gap-2 apx-btn-glow shadow">
                        <i class="bi bi-map-fill"></i>
                        <span>Buka Server Map (Tab Baru)</span>
                        <i class="bi bi-box-arrow-up-right small ms-1"></i>
                    </a>
                    <button type="button" class="btn btn-apx-outline px-4 py-3 d-inline-flex align-items-center gap-2" onclick="document.getElementById('mapLiveContainer').classList.toggle('d-none'); this.scrollIntoView({behavior: 'smooth'});">
                        <i class="bi bi-display"></i>
                        <span>Tampilkan Layar Langsung</span>
                    </button>
                </div>
            </div>

            <div class="col-lg-4 text-center mt-4 mt-lg-0">
                <div class="p-3 rounded-4 border border-secondary border-opacity-20 bg-black bg-opacity-40 d-inline-block shadow-sm">
                    <div class="text-warning mb-2" style="font-size: 2.5rem;">
                        <i class="bi bi-pin-map-fill"></i>
                    </div>
                    <h6 class="text-white font-cinzel mb-1">Koordinat Pusat</h6>
                    <p class="text-muted small font-monospace mb-2">X: 0 | Y: 64 | Z: 0</p>
                    <span class="badge bg-warning bg-opacity-15 text-warning small px-2 py-1">Dimensi: Overworld &amp; Nether</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Toggleable Live Iframe Section -->
    <div id="mapLiveContainer" class="card mb-4 border border-warning border-opacity-30 overflow-hidden shadow-lg d-none" style="border-radius: var(--apx-radius-lg); background: #000;">
        <div class="card-header bg-dark bg-opacity-50 border-bottom border-secondary border-opacity-20 p-3 d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-broadcast text-warning"></i>
                <span class="text-white small fw-bold font-cinzel">Pratinjau Langsung Server Map 3D</span>
            </div>
            <div class="d-flex align-items-center gap-2">
                <a href="{{ $mapUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-apx-outline py-1 px-2" title="Buka Fullscreen">
                    <i class="bi bi-fullscreen"></i>
                </a>
                <button type="button" class="btn btn-sm btn-outline-secondary py-1 px-2" onclick="document.getElementById('mapLiveContainer').classList.add('d-none');" title="Tutup">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
        </div>
        <div class="ratio ratio-16x9" style="min-height: 540px;">
            <iframe src="{{ $mapUrl }}" title="Apexsions Live World Map" loading="lazy" allowfullscreen style="border: 0; width: 100%; height: 100%;"></iframe>
        </div>
    </div>

    <!-- Realm Landmarks & Kingdom Guide Grid -->
    <div class="row g-4 mb-4">
        <!-- 1. Zenithar Landmark -->
        <div class="col-md-4">
            <div class="card h-100 p-4 border border-secondary border-opacity-20 position-relative" style="background: var(--apx-bg-surface); border-radius: var(--apx-radius-md);">
                <div class="d-flex align-items-center gap-2 mb-2" style="color: #f6e58d;">
                    <i class="bi bi-compass fs-5"></i>
                    <span class="small fw-bold font-monospace text-uppercase">Teritori Timur</span>
                </div>
                <h4 class="font-cinzel text-white mb-2">Kerajaan Zenithar</h4>
                <p class="text-muted small mb-3" style="line-height: 1.6;">
                    Puncak cakrawala istana Solarium Spire Citadel. Tempat tinggal kaum bangsawan dan kesatria penjaga ketertiban langit Apexsions.
                </p>
                <div class="mt-auto pt-2 border-top border-secondary border-opacity-15 small font-monospace text-warning">
                    <i class="bi bi-geo-alt me-1"></i> Sektor: Timur (+X, +Z)
                </div>
            </div>
        </div>

        <!-- 2. Solterra Landmark -->
        <div class="col-md-4">
            <div class="card h-100 p-4 border border-secondary border-opacity-20 position-relative" style="background: var(--apx-bg-surface); border-radius: var(--apx-radius-md);">
                <div class="d-flex align-items-center gap-2 mb-2" style="color: #ff7675;">
                    <i class="bi bi-fire fs-5"></i>
                    <span class="small fw-bold font-monospace text-uppercase">Teritori Selatan</span>
                </div>
                <h4 class="font-cinzel text-white mb-2">Kerajaan Solterra</h4>
                <p class="text-muted small mb-3" style="line-height: 1.6;">
                    Kawah vulkanik dan Ignis Bastion. Markas para pesulap api tempur dan penempa senjata berat terkuat dengan rasio jual mineral tinggi.
                </p>
                <div class="mt-auto pt-2 border-top border-secondary border-opacity-15 small font-monospace text-warning">
                    <i class="bi bi-geo-alt me-1"></i> Sektor: Selatan (+X, -Z)
                </div>
            </div>
        </div>

        <!-- 3. Sylvamoor Landmark -->
        <div class="col-md-4">
            <div class="card h-100 p-4 border border-secondary border-opacity-20 position-relative" style="background: var(--apx-bg-surface); border-radius: var(--apx-radius-md);">
                <div class="d-flex align-items-center gap-2 mb-2" style="color: #55efc4;">
                    <i class="bi bi-tree fs-5"></i>
                    <span class="small fw-bold font-monospace text-uppercase">Teritori Barat</span>
                </div>
                <h4 class="font-cinzel text-white mb-2">Kerajaan Sylvamoor</h4>
                <p class="text-muted small mb-3" style="line-height: 1.6;">
                    Hutan purba dan kanopi raksasa Aethelgard. Teritori kaum pejuang alam liar, petani tangguh, dan pemburu terhebat peradaban.
                </p>
                <div class="mt-auto pt-2 border-top border-secondary border-opacity-15 small font-monospace text-warning">
                    <i class="bi bi-geo-alt me-1"></i> Sektor: Barat (-X, +Z)
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
