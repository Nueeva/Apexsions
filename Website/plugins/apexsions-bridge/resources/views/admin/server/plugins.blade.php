@extends('admin.layouts.admin')

@section('title', 'Plugin Status Monitoring — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <a href="{{ route('apexsions-bridge.admin.server.index') }}" class="btn btn-outline-secondary btn-sm">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <h2 class="h3 fw-bold text-white mb-0">
                    <i class="bi bi-puzzle-fill text-warning me-2"></i>Plugin Status Monitoring
                </h2>
            </div>
            <p class="text-white-50 small mb-0">Status operasional, versi runtime, dan matriks kapabilitas 6 modul plugin custom ekosistem Apexsions.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
            <span class="badge {{ $serverStatus['badge_class'] }} px-3 py-2 font-monospace">
                {{ $serverStatus['label'] }}
            </span>
        </div>
    </div>

    <!-- Alert Info (Monitoring Only) -->
    <div class="alert alert-secondary bg-dark border-secondary border-opacity-25 text-white-50 small mb-4 d-flex align-items-center">
        <i class="bi bi-info-circle text-warning fs-5 me-3"></i>
        <div>
            <strong class="text-white">Portal Pengawasan Modul (Read-Only Monitoring):</strong>
            Halaman ini hanya menampilkan status kesehatan dan kapabilitas fungsional yang terverifikasi pada runtime server. Tidak tersedia pengunggahan atau penghapusan file JAR demi integritas dan keamanan server.
        </div>
    </div>

    <!-- Plugins Grid -->
    <div class="row g-4">
        @foreach($plugins as $plugin)
            <div class="col-lg-6">
                <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                    <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-box-seam-fill text-warning fs-5"></i>
                            <div>
                                <h5 class="card-title text-white h6 mb-0 fw-bold">{{ $plugin['name'] }}</h5>
                                <span class="text-white-50" style="font-size: 0.72rem;">Versi: {{ $plugin['version'] }}</span>
                            </div>
                        </div>
                        <div class="d-flex gap-2">
                            <span class="badge {{ $plugin['badge_class'] }} font-monospace">
                                {{ $plugin['status'] }}
                            </span>
                            <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace">
                                {{ $plugin['integration'] }}
                            </span>
                        </div>
                    </div>
                    <div class="card-body p-3 d-flex flex-column justify-content-between">
                        <p class="text-white-50 small mb-3">{{ $plugin['description'] }}</p>

                        <div>
                            <div class="text-white-50 small fw-bold text-uppercase mb-2" style="font-size: 0.75rem;">
                                Kapabilitas Terverifikasi:
                            </div>
                            <ul class="list-unstyled mb-0 d-flex flex-column gap-1">
                                @foreach($plugin['capabilities'] as $cap)
                                    <li class="small text-white-50 d-flex align-items-center">
                                        <i class="bi bi-check-circle-fill text-success me-2" style="font-size: 0.8rem;"></i>
                                        <span class="text-light">{{ $cap }}</span>
                                    </li>
                                @endforeach
                            </ul>
                        </div>
                    </div>
                    <div class="card-footer bg-black bg-opacity-10 border-top border-secondary border-opacity-10 py-2 d-flex justify-content-between align-items-center" style="font-size: 0.75rem;">
                        <span class="text-muted">Target Runtime: <strong class="text-white-50">Minecraft 26.2 (Paper API)</strong></span>
                        <span class="text-muted">Java: <strong class="text-white-50">21 LTS</strong></span>
                    </div>
                </div>
            </div>
        @endforeach
    </div>
</div>
@endsection
