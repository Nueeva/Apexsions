@extends('admin.layouts.admin')

@section('title', 'Custom Plugin Control & Capabilities — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <a href="{{ route('apexsions-bridge.admin.server.index') }}" class="btn btn-outline-secondary btn-sm" title="Kembali ke Server Operations">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <h2 class="h3 fw-bold text-white mb-0">
                    <i class="bi bi-cpu-fill text-warning me-2"></i>Custom Plugin Control & Capability System
                </h2>
            </div>
            <p class="text-white-50 small mb-0">Registrasi, pengawasan status kesehatan, deklarasi kapabilitas, dan gateway kontrol aman untuk seluruh plugin kustom Apexsions.</p>
        </div>
    </div>

    <!-- Feedback Alerts -->
    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif
    @if(session('error'))
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ session('error') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif

    <!-- Metrics Stat Cards -->
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Total Custom Plugins</span>
                        <h3 class="h2 fw-bold text-white mb-0 mt-1">{{ $stats['total'] }}</h3>
                    </div>
                    <div class="p-3 bg-secondary bg-opacity-10 rounded-circle text-warning fs-3">
                        <i class="bi bi-puzzle-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Kondisi Sehat (Healthy)</span>
                        <h3 class="h2 fw-bold text-success mb-0 mt-1">{{ $stats['healthy'] }}</h3>
                    </div>
                    <div class="p-3 bg-success bg-opacity-10 rounded-circle text-success fs-3">
                        <i class="bi bi-heart-pulse-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Terintegrasi Web (Web Ready)</span>
                        <h3 class="h2 fw-bold text-primary mb-0 mt-1">{{ $stats['web_ready'] }}</h3>
                    </div>
                    <div class="p-3 bg-primary bg-opacity-10 rounded-circle text-primary fs-3">
                        <i class="bi bi-globe2"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Kapabilitas Terdaftar</span>
                        <h3 class="h2 fw-bold text-warning mb-0 mt-1">{{ $stats['total_capabilities'] }}</h3>
                    </div>
                    <div class="p-3 bg-warning bg-opacity-10 rounded-circle text-warning fs-3">
                        <i class="bi bi-lightning-charge-fill"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Filter Bar -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body py-3">
            <form method="GET" action="{{ route('apexsions-bridge.admin.plugins.index') }}" class="row g-3 align-items-center">
                <div class="col-md-3">
                    <label class="form-label text-white-50 small mb-1">Domain / Tipe Plugin</label>
                    <select name="type" class="form-select form-select-sm bg-black border-secondary text-white" onchange="this.form.submit()">
                        <option value="">Semua Domain Tipe</option>
                        <option value="CORE" {{ ($filters['type'] ?? '') === 'CORE' ? 'selected' : '' }}>Core Infrastructure</option>
                        <option value="ECONOMY" {{ ($filters['type'] ?? '') === 'ECONOMY' ? 'selected' : '' }}>Economy & Monetary</option>
                        <option value="CHAT" {{ ($filters['type'] ?? '') === 'CHAT' ? 'selected' : '' }}>Chat & Community</option>
                        <option value="GAMEPLAY" {{ ($filters['type'] ?? '') === 'GAMEPLAY' ? 'selected' : '' }}>Gameplay & Quests</option>
                        <option value="COMBAT" {{ ($filters['type'] ?? '') === 'COMBAT' ? 'selected' : '' }}>Combat & Magic</option>
                        <option value="COSMETIC" {{ ($filters['type'] ?? '') === 'COSMETIC' ? 'selected' : '' }}>Cosmetic & Media</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label text-white-50 small mb-1">Status Operasional</label>
                    <select name="status" class="form-select form-select-sm bg-black border-secondary text-white" onchange="this.form.submit()">
                        <option value="">Semua Status</option>
                        <option value="ENABLED" {{ ($filters['status'] ?? '') === 'ENABLED' ? 'selected' : '' }}>Aktif (Enabled)</option>
                        <option value="DISABLED" {{ ($filters['status'] ?? '') === 'DISABLED' ? 'selected' : '' }}>Nonaktif (Disabled)</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label text-white-50 small mb-1">Tingkat Integrasi Web</label>
                    <select name="integration" class="form-select form-select-sm bg-black border-secondary text-white" onchange="this.form.submit()">
                        <option value="">Semua Integrasi</option>
                        <option value="WEB_READY" {{ ($filters['integration'] ?? '') === 'WEB_READY' ? 'selected' : '' }}>Web Ready</option>
                        <option value="PARTIAL" {{ ($filters['integration'] ?? '') === 'PARTIAL' ? 'selected' : '' }}>Partial Integration</option>
                        <option value="MINECRAFT_ONLY" {{ ($filters['integration'] ?? '') === 'MINECRAFT_ONLY' ? 'selected' : '' }}>Minecraft Only</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex align-items-end gap-2">
                    <a href="{{ route('apexsions-bridge.admin.plugins.index') }}" class="btn btn-outline-secondary btn-sm flex-fill">
                        <i class="bi bi-arrow-counterclockwise me-1"></i>Reset Filter
                    </a>
                </div>
            </form>
        </div>
    </div>

    <!-- Plugins Catalog Grid -->
    <div class="row g-4">
        @forelse($plugins as $plugin)
            <div class="col-xl-6">
                <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100 d-flex flex-column justify-content-between">
                    <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-box-seam-fill text-warning fs-5"></i>
                            <div>
                                <h5 class="card-title text-white h6 mb-0 fw-bold">
                                    <a href="{{ route('apexsions-bridge.admin.plugins.show', $plugin->plugin_id) }}" class="text-white text-decoration-none hover-warning">
                                        {{ $plugin->name }}
                                    </a>
                                </h5>
                                <span class="text-white-50" style="font-size: 0.72rem;">ID: <code>{{ $plugin->plugin_id }}</code> | v{{ $plugin->version }}</span>
                            </div>
                        </div>
                        <div class="d-flex gap-1 align-items-center">
                            <span class="badge {{ $plugin->type_badge }} font-monospace small">
                                {{ $plugin->type }}
                            </span>
                            <span class="badge {{ $plugin->health_badge }} font-monospace small">
                                {{ $plugin->health_status }}
                            </span>
                            <span class="badge {{ $plugin->integration_badge }} font-monospace small">
                                {{ $plugin->integration_status }}
                            </span>
                        </div>
                    </div>

                    <div class="card-body p-3">
                        <p class="text-white-50 small mb-3">{{ $plugin->description }}</p>

                        <div class="mb-3">
                            <div class="text-white-50 small fw-bold text-uppercase mb-2" style="font-size: 0.75rem;">
                                Kapabilitas Terverifikasi ({{ $plugin->capabilities->count() }}):
                            </div>
                            <div class="d-flex flex-wrap gap-1">
                                @forelse($plugin->capabilities->take(6) as $cap)
                                    <span class="badge bg-black border border-secondary border-opacity-25 text-light" style="font-size: 0.72rem;">
                                        <span class="text-warning me-1">[{{ $cap->type }}]</span>{{ $cap->name }}
                                    </span>
                                @empty
                                    <span class="text-muted small">Belum ada kapabilitas terdaftar.</span>
                                @endforelse
                                @if($plugin->capabilities->count() > 6)
                                    <span class="badge bg-secondary bg-opacity-25 text-white-50 small">
                                        +{{ $plugin->capabilities->count() - 6 }} lainnya
                                    </span>
                                @endif
                            </div>
                        </div>

                        @if(!empty($plugin->dependencies))
                            <div class="small text-white-50">
                                <span class="text-muted">Dependensi:</span>
                                @foreach($plugin->dependencies as $dep)
                                    <span class="badge bg-secondary bg-opacity-10 text-white-50 me-1" style="font-size: 0.7rem;">{{ $dep }}</span>
                                @endforeach
                            </div>
                        @endif
                    </div>

                    <div class="card-footer bg-black bg-opacity-20 border-top border-secondary border-opacity-25 py-2 px-3 d-flex justify-content-between align-items-center">
                        <span class="text-white-50 small" style="font-size: 0.75rem;">
                            Heartbeat: <strong class="text-light">{{ $plugin->last_heartbeat_at ? $plugin->last_heartbeat_at->diffForHumans() : 'Belum pernah' }}</strong>
                        </span>
                        <a href="{{ route('apexsions-bridge.admin.plugins.show', $plugin->plugin_id) }}" class="btn btn-outline-warning btn-sm py-1 px-3">
                            <i class="bi bi-sliders me-1"></i>Detail & Kapabilitas
                        </a>
                    </div>
                </div>
            </div>
        @empty
            <div class="col-12">
                <div class="card bg-dark border-secondary border-opacity-25 text-center py-5">
                    <i class="bi bi-inbox text-secondary fs-1 mb-2"></i>
                    <h5 class="text-white">Tidak ada plugin ditemukan</h5>
                    <p class="text-white-50 small mb-0">Tidak ada custom plugin yang sesuai dengan kriteria filter yang dipilih.</p>
                </div>
            </div>
        @endforelse
    </div>
</div>
@endsection
