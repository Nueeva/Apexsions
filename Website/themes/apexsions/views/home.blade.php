@extends('layouts.app')

@section('title', 'Selamat Datang di Apexsions')

@section('content')
<!-- Hero Section -->
<section class="apx-hero">
    <div class="container">
        <div class="apx-hero-badge">
            <span class="apx-status-indicator"></span>
            <span id="apxPlayerCount" data-server="apexsions.my.id">Memuat Status Server...</span>
        </div>

        <h1 class="apx-hero-title">
            JELAJAHI DUNIA <br>
            <span class="apx-hero-gradient-text">APEXSIONS</span>
        </h1>
        <div class="apx-hero-tagline text-uppercase fw-bold text-info mb-3" style="letter-spacing: 2px;">
            The Peak Civilizations
        </div>

        <p class="apx-hero-desc">
            Rasakan sensasi petualangan epik dengan sistem Kingdom War, ekonomi multi-currency atomic, 
            dan progres level yang mendalam. Mendukung penuh Java Edition dan Bedrock Edition.
        </p>

        <!-- Server IP Copy Widget -->
        <div class="mb-5">
            <div class="apx-ip-widget" id="apxIpWidget" data-ip="apexsions.my.id">
                <div class="d-flex align-items-center gap-2">
                    <span class="apx-status-indicator"></span>
                    <span class="apx-ip-address">apexsions.my.id</span>
                </div>
                <button type="button" class="btn btn-apx-primary btn-sm px-3">
                    <i class="bi bi-clipboard me-1"></i> Salin IP
                </button>
            </div>
            <div class="mt-2 text-muted small">Port Bedrock: <code>19132</code> (Default)</div>
        </div>

        <!-- Action CTAs -->
        <div class="d-flex justify-content-center gap-3 flex-wrap">
            @if(plugins()->isEnabled('shop'))
                <a href="{{ route('shop.home') }}" class="btn btn-apx-primary px-4 py-3 fs-6">
                    <i class="bi bi-bag-check-fill me-2"></i> Kunjungi Webstore
                </a>
            @endif
            @if(plugins()->isEnabled('wiki'))
                <a href="{{ route('wiki.home') }}" class="btn btn-apx-secondary px-4 py-3 fs-6">
                    <i class="bi bi-book-half me-2"></i> Baca Wiki & Command
                </a>
            @endif
        </div>
    </div>
</section>

<!-- Feature Showcase Section -->
<section class="py-5">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="fw-bold fs-1">Fitur Unggulan Ekosistem</h2>
            <p class="text-muted">Dibangun khusus dengan plugin suite modular berkinerja tinggi</p>
        </div>

        <div class="row g-4">
            <!-- Kingdom & Region -->
            <div class="col-md-6 col-lg-3">
                <div class="apx-card">
                    <div class="apx-card-icon">
                        <i class="bi bi-shield-shaded"></i>
                    </div>
                    <h4 class="fw-bold mb-2">Kingdom War</h4>
                    <p class="text-muted small mb-0">
                        Bentuk aliansi kerajaan, kuasai teritorial region strategis, dan hadapi pertempuran war antar-faksi yang terorganisir.
                    </p>
                </div>
            </div>

            <!-- Economy & Trade -->
            <div class="col-md-6 col-lg-3">
                <div class="apx-card">
                    <div class="apx-card-icon">
                        <i class="bi bi-cash-coin"></i>
                    </div>
                    <h4 class="fw-bold mb-2">Atomic Economy</h4>
                    <p class="text-muted small mb-0">
                        Transaksi aman bebas duplikasi dengan Auction House, Barter, Escrow, dan pasar dinamis yang responsif terhadap tren server.
                    </p>
                </div>
            </div>

            <!-- Progression & Ranks -->
            <div class="col-md-6 col-lg-3">
                <div class="apx-card">
                    <div class="apx-card-icon">
                        <i class="bi bi-stars"></i>
                    </div>
                    <h4 class="fw-bold mb-2">Rank & Progres</h4>
                    <p class="text-muted small mb-0">
                        Hierarki rank resmi dari Wanderer hingga Ancestor dengan animasi eksklusif, XP booster, dan reward level otomatis.
                    </p>
                </div>
            </div>

            <!-- Crossplay Identity -->
            <div class="col-md-6 col-lg-3">
                <div class="apx-card">
                    <div class="apx-card-icon">
                        <i class="bi bi-phone-flip"></i>
                    </div>
                    <h4 class="fw-bold mb-2">Cross-Platform</h4>
                    <p class="text-muted small mb-0">
                        Integrasi mulus Java & Bedrock (Floodgate) dengan akun web terpadu untuk pengiriman reward otomatis tanpa hambatan.
                    </p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- News & Posts Section -->
@if(isset($posts) && $posts->isNotEmpty())
<section class="py-5 border-top border-secondary border-opacity-25">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h3 class="fw-bold mb-0">Berita & Pembaruan Server</h3>
                <p class="text-muted small mb-0">Informasi update patch dan pengumuman terbaru</p>
            </div>
        </div>

        <div class="row g-4">
            @foreach($posts->take(3) as $post)
                <div class="col-md-4">
                    <div class="apx-card">
                        @if($post->hasImage())
                            <img src="{{ $post->imageUrl() }}" alt="{{ $post->title }}" class="img-fluid rounded mb-3" style="height: 180px; width: 100%; object-fit: cover;">
                        @endif
                        <span class="badge bg-primary mb-2">{{ $post->published_at->format('d M Y') }}</span>
                        <h5 class="fw-bold mb-2">{{ $post->title }}</h5>
                        <p class="text-muted small mb-3">{{ Str::limit(strip_tags($post->content), 100) }}</p>
                        <a href="{{ route('posts.show', $post) }}" class="btn btn-sm btn-outline-light">Baca Selengkapnya</a>
                    </div>
                </div>
            @endforeach
        </div>
    </div>
</section>
@endif
@endsection
