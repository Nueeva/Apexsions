@extends('admin.layouts.admin')

@section('title', 'Webstore Manager')

@section('content')
<div class="mb-4">
    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="badge bg-warning text-dark fw-bold px-2 py-1 font-monospace">SHOP ENGINE</span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1.5px;">
                        🛍️ WEBSTORE PRODUCT & BANNER MANAGER
                    </h3>
                </div>
                <p class="text-muted small mb-0" style="max-width: 800px;">
                    Pusat kendali visual, banner produk (16:9), preset grafis resmi, tag promo, harga, routing WhatsApp Founder, dan baris perintah konsol in-game Minecraft untuk seluruh paket webstore Apexsions.
                </p>
            </div>
            <div class="d-flex flex-wrap align-items-center gap-2">
                <a href="{{ route('shop.home') }}" target="_blank" class="btn btn-outline-warning fw-bold px-3 shadow-sm">
                    <i class="bi bi-box-arrow-up-right me-1"></i> Buka Webstore Live
                </a>
                <a href="{{ route('apexsions-bridge.admin.ranks.index') }}" class="btn btn-outline-light fw-bold px-3 shadow-sm">
                    <i class="bi bi-trophy-fill me-1"></i> Rank Hierarchy
                </a>
            </div>
        </div>
    </div>
</div>

@if(session('success'))
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2 mb-4" role="alert">
        <i class="bi bi-check-circle-fill fs-5"></i>
        <div>{{ session('success') }}</div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
@endif

<!-- KPI Cards -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100" style="background: rgba(18, 20, 29, 0.85); border: 1px solid rgba(255,255,255,0.08);">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Total Produk</span>
                <i class="bi bi-boxes text-warning fs-5"></i>
            </div>
            <h3 class="fw-bold text-white mb-0">{{ $totalPackages }}</h3>
            <small class="text-muted">Paket di semua kategori</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100" style="background: rgba(18, 20, 29, 0.85); border: 1px solid rgba(255,255,255,0.08);">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Produk Aktif</span>
                <i class="bi bi-patch-check-fill text-success fs-5"></i>
            </div>
            <h3 class="fw-bold text-success mb-0">{{ $activePackages }}</h3>
            <small class="text-muted">Dapat dibeli di webstore</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100" style="background: rgba(18, 20, 29, 0.85); border: 1px solid rgba(255,255,255,0.08);">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Kategori Webstore</span>
                <i class="bi bi-tags-fill text-info fs-5"></i>
            </div>
            <h3 class="fw-bold text-info mb-0">{{ $totalCategories }}</h3>
            <small class="text-muted">Rank, Battlepass, Booster & Koin</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card p-3 h-100" style="background: rgba(18, 20, 29, 0.85); border: 1px solid rgba(255,255,255,0.08);">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Produk Unggulan</span>
                <i class="bi bi-star-fill text-warning fs-5"></i>
            </div>
            <h3 class="fw-bold text-warning mb-0">{{ $featuredCount }}</h3>
            <small class="text-muted">Ditandai Featured di Home</small>
        </div>
    </div>
</div>

<!-- Filter & Search Bar -->
<div class="card mb-4" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
    <div class="card-body p-3">
        <form method="GET" action="{{ route('apexsions-bridge.admin.webstore.index') }}" class="row g-2 align-items-center">
            <div class="col-md-4">
                <div class="input-group">
                    <span class="input-group-text bg-dark border-secondary text-muted"><i class="bi bi-search"></i></span>
                    <input type="text" name="q" class="form-control bg-dark border-secondary text-white" placeholder="Cari nama atau deskripsi paket..." value="{{ $search }}">
                </div>
            </div>
            <div class="col-md-3">
                <select name="category" class="form-select bg-dark border-secondary text-white">
                    <option value="all">Semua Kategori</option>
                    @foreach($categories as $cat)
                        <option value="{{ $cat->id }}" @selected($selectedCategory == $cat->id)>{{ $cat->name }}</option>
                    @endforeach
                </select>
            </div>
            <div class="col-md-3">
                <select name="status" class="form-select bg-dark border-secondary text-white">
                    <option value="all" @selected($selectedStatus === 'all')>Semua Status</option>
                    <option value="enabled" @selected($selectedStatus === 'enabled')>Hanya Aktif</option>
                    <option value="disabled" @selected($selectedStatus === 'disabled')>Hanya Nonaktif</option>
                </select>
            </div>
            <div class="col-md-2 d-flex gap-2">
                <button type="submit" class="btn btn-warning fw-bold w-100">
                    <i class="bi bi-funnel-fill me-1"></i> Filter
                </button>
                @if(!empty($search) || $selectedCategory !== 'all' || $selectedStatus !== 'all')
                    <a href="{{ route('apexsions-bridge.admin.webstore.index') }}" class="btn btn-outline-secondary" title="Reset Filter">
                        <i class="bi bi-x-lg"></i>
                    </a>
                @endif
            </div>
        </form>
    </div>
</div>

<!-- Packages Table -->
<div class="card shadow-sm" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
    <div class="card-header bg-dark bg-opacity-50 py-3 border-bottom border-secondary d-flex justify-content-between align-items-center">
        <h5 class="mb-0 fw-bold text-white d-flex align-items-center gap-2">
            <i class="bi bi-grid-3x3-gap-fill text-warning"></i>
            Daftar Paket Webstore ({{ $packages->total() }})
        </h5>
        <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25 px-2 py-1">
            Klik "Edit" untuk atur Banner, Gambar &amp; Info
        </span>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0 text-white" style="border-color: rgba(255,255,255,0.06);">
            <thead class="table-dark" style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.8px;">
                <tr>
                    <th class="ps-3" style="width: 50px;">ID</th>
                    <th style="width: 140px;">Banner / Visual</th>
                    <th>Nama Paket &amp; Kategori</th>
                    <th style="width: 130px;">Harga Satuan</th>
                    <th style="width: 140px;">Tag Promo</th>
                    <th style="width: 110px;">Commands</th>
                    <th style="width: 90px;" class="text-center">Unggulan</th>
                    <th style="width: 90px;" class="text-center">Status</th>
                    <th class="text-end pe-3" style="width: 120px;">Aksi</th>
                </tr>
            </thead>
            <tbody>
                @forelse($packages as $pkg)
                    @php
                        $customBadge = setting('apexsions.webstore.pkg_' . $pkg->id . '.badge', '');
                        $isFeatured = setting('apexsions.webstore.pkg_' . $pkg->id . '.is_featured', '0') === '1';
                        $effectiveImg = $pkg->hasImage() ? $pkg->imageUrl() : null;
                        $cmdCount = is_array($pkg->commands) ? count($pkg->commands) : 0;
                    @endphp
                    <tr>
                        <td class="ps-3 text-muted font-monospace small">#{{ $pkg->id }}</td>
                        <td>
                            @if($effectiveImg)
                                <div class="position-relative" style="width: 120px; height: 60px; border-radius: 6px; overflow: hidden; border: 1px solid rgba(255,255,255,0.15); background: #000;">
                                    <img src="{{ $effectiveImg }}" alt="{{ $pkg->name }}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.onerror=null; this.src='{{ asset('assets/themes/apexsions/img/' . $pkg->image) }}';">
                                </div>
                            @else
                                <div class="d-flex align-items-center justify-content-center bg-dark text-muted border border-secondary" style="width: 120px; height: 60px; border-radius: 6px; font-size: 0.75rem;">
                                    <i class="bi bi-image me-1"></i> Tanpa Banner
                                </div>
                            @endif
                        </td>
                        <td>
                            <div class="fw-bold text-white fs-6">{{ $pkg->name }}</div>
                            <div class="d-flex align-items-center gap-2 mt-1">
                                <span class="badge bg-secondary bg-opacity-50 text-light border border-secondary" style="font-size: 0.72rem;">
                                    <i class="bi bi-tag-fill me-1 text-warning"></i> {{ $pkg->category->name ?? 'Uncategorized' }}
                                </span>
                                @if(!empty($pkg->short_description))
                                    <span class="text-muted small text-truncate" style="max-width: 320px;" title="{{ $pkg->short_description }}">
                                        {{ $pkg->short_description }}
                                    </span>
                                @endif
                            </div>
                        </td>
                        <td>
                            <div class="fw-bold text-warning font-monospace">
                                Rp {{ number_format($pkg->price, 0, ',', '.') }}
                            </div>
                            @if($pkg->user_limit)
                                <small class="text-info" style="font-size: 0.7rem;">Limit: {{ $pkg->user_limit }}x/akun</small>
                            @endif
                        </td>
                        <td>
                            @if(!empty($customBadge))
                                <span class="badge bg-warning text-dark fw-bold px-2 py-1 shadow-sm">
                                    <i class="bi bi-fire me-1"></i> {{ $customBadge }}
                                </span>
                            @else
                                <span class="text-muted small">- Standar -</span>
                            @endif
                        </td>
                        <td>
                            <span class="badge bg-dark border border-secondary text-info font-monospace" style="font-size: 0.75rem;">
                                <i class="bi bi-terminal me-1"></i> {{ $cmdCount }} Perintah
                            </span>
                        </td>
                        <td class="text-center">
                            <form action="{{ route('apexsions-bridge.admin.webstore.toggle-featured', $pkg->id) }}" method="POST">
                                @csrf
                                <button type="submit" class="btn btn-sm p-1 border-0" title="{{ $isFeatured ? 'Hapus dari Unggulan' : 'Jadikan Unggulan' }}">
                                    @if($isFeatured)
                                        <i class="bi bi-star-fill text-warning fs-5"></i>
                                    @else
                                        <i class="bi bi-star text-muted fs-5"></i>
                                    @endif
                                </button>
                            </form>
                        </td>
                        <td class="text-center">
                            <form action="{{ route('apexsions-bridge.admin.webstore.toggle-status', $pkg->id) }}" method="POST">
                                @csrf
                                <button type="submit" class="btn btn-sm border-0 p-0" title="Klik untuk ubah status aktif">
                                    @if($pkg->is_enabled)
                                        <span class="badge bg-success bg-opacity-25 text-success border border-success px-2 py-1">
                                            <i class="bi bi-check-circle-fill me-1"></i> Aktif
                                        </span>
                                    @else
                                        <span class="badge bg-danger bg-opacity-25 text-danger border border-danger px-2 py-1">
                                            <i class="bi bi-slash-circle me-1"></i> Nonaktif
                                        </span>
                                    @endif
                                </button>
                            </form>
                        </td>
                        <td class="text-end pe-3">
                            <a href="{{ route('apexsions-bridge.admin.webstore.edit', $pkg->id) }}" class="btn btn-sm btn-outline-warning fw-bold px-2">
                                <i class="bi bi-pencil-square me-1"></i> Kelola
                            </a>
                        </td>
                    </tr>
                @empty
                    <tr>
                        <td colspan="9" class="text-center py-5 text-muted">
                            <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                            Tidak ada paket webstore yang ditemukan dengan filter ini.
                        </td>
                    </tr>
                @endforelse
            </tbody>
        </table>
    </div>
    @if($packages->hasPages())
        <div class="card-footer bg-dark bg-opacity-50 py-3 border-top border-secondary">
            {{ $packages->links() }}
        </div>
    @endif
</div>
@endsection
