@extends('admin.layouts.admin')

@section('title', 'Intelligence & Incident Operations — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-0">
                <i class="bi bi-shield-check text-warning me-2"></i>Intelligence & Incident Operations Desk
            </h2>
            <p class="text-white-50 small mb-0">Deteksi anomali berbasis rule deterministik, korelasi kejadian, dan lembar investigasi terpadu untuk Apexsions.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-warning btn-sm fw-bold">
                <i class="bi bi-exclamation-octagon-fill me-1"></i>Daftar Seluruh Insiden
            </a>
            <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-clock-history me-1"></i>Audit Logs
            </a>
        </div>
    </div>

    <!-- Feedback Alerts -->
    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif

    <!-- Metrics Stat Cards -->
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-danger border-opacity-50 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-danger small fw-bold text-uppercase">Insiden Kritis Terbuka</span>
                        <h3 class="h2 fw-bold text-white mb-0 mt-1">{{ $criticalCount }}</h3>
                    </div>
                    <div class="p-3 bg-danger bg-opacity-10 rounded-circle text-danger fs-3">
                        <i class="bi bi-radioactive"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-warning border-opacity-50 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-warning small fw-bold text-uppercase">Total Insiden Aktif</span>
                        <h3 class="h2 fw-bold text-white mb-0 mt-1">{{ $openIncidentsCount }}</h3>
                    </div>
                    <div class="p-3 bg-warning bg-opacity-10 rounded-circle text-warning fs-3">
                        <i class="bi bi-exclamation-triangle-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Normalisasi Event (24 Jam)</span>
                        <h3 class="h2 fw-bold text-info mb-0 mt-1">{{ number_format($eventsPast24h) }}</h3>
                    </div>
                    <div class="p-3 bg-info bg-opacity-10 rounded-circle text-info fs-3">
                        <i class="bi bi-activity"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="text-white-50 small fw-bold text-uppercase">Insiden Terselesaikan</span>
                        <h3 class="h2 fw-bold text-success mb-0 mt-1">{{ $resolvedCount }}</h3>
                    </div>
                    <div class="p-3 bg-success bg-opacity-10 rounded-circle text-success fs-3">
                        <i class="bi bi-check-all"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <!-- Prioritized Active Incidents Table -->
        <div class="col-xl-8">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-transparent border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-bell-fill text-warning me-2"></i>Insiden Aktif Membutuhkan Investigasi
                    </h5>
                    <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-outline-secondary btn-sm">Lihat Semua</a>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small text-uppercase">
                                    <th>ID Insiden</th>
                                    <th>Judul & Tipe</th>
                                    <th>Keparahan</th>
                                    <th>Status</th>
                                    <th>Frekuensi</th>
                                    <th>Penugasan</th>
                                    <th class="text-end">Aksi</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($activeIncidents as $inc)
                                    <tr>
                                        <td>
                                            <a href="{{ route('apexsions-bridge.admin.incidents.show', $inc->incident_id) }}" class="text-info fw-bold text-decoration-none">
                                                {{ $inc->incident_id }}
                                            </a>
                                        </td>
                                        <td>
                                            <div class="fw-semibold text-white">{{ $inc->title }}</div>
                                            <span class="badge bg-secondary text-uppercase" style="font-size: 0.7rem;">{{ $inc->type }}</span>
                                            <span class="text-white-50 small ms-1">{{ $inc->root_entity_name }}</span>
                                        </td>
                                        <td>
                                            <span class="badge {{ $inc->severity_badge }}">{{ $inc->severity }}</span>
                                        </td>
                                        <td>
                                            <span class="badge {{ $inc->status_badge }}">{{ $inc->status }}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary bg-opacity-50 text-white">{{ $inc->occurrence_count }}x</span>
                                        </td>
                                        <td>
                                            @if($inc->assigned_to)
                                                <span class="text-white small"><i class="bi bi-person-fill text-info me-1"></i>{{ $inc->assigned_to }}</span>
                                            @else
                                                <span class="text-muted small">Belum Ditugaskan</span>
                                            @endif
                                        </td>
                                        <td class="text-end">
                                            <a href="{{ route('apexsions-bridge.admin.incidents.show', $inc->incident_id) }}" class="btn btn-sm btn-primary">
                                                <i class="bi bi-search me-1"></i>Investigasi
                                            </a>
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="7" class="text-center text-white-50 py-4">
                                            <i class="bi bi-shield-check text-success fs-2 d-block mb-2"></i>
                                            Tidak ada insiden aktif yang terbuka. Seluruh komponen terpantau stabil.
                                        </td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Health & Server Alert Status -->
        <div class="col-xl-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-exclamation-diamond text-danger me-2"></i>Peringatan Server Aktif
                    </h5>
                </div>
                <div class="card-body">
                    @forelse($activeAlerts as $alert)
                        <div class="d-flex align-items-start gap-2 mb-3 pb-2 border-bottom border-secondary border-opacity-25">
                            <span class="badge bg-danger mt-1">{{ $alert->severity }}</span>
                            <div>
                                <div class="text-white small fw-bold">{{ $alert->type }}</div>
                                <div class="text-white-50 small">{{ $alert->message }}</div>
                                <div class="text-muted" style="font-size: 0.75rem;">{{ $alert->created_at->diffForHumans() }}</div>
                            </div>
                        </div>
                    @empty
                        <div class="text-center text-white-50 py-3">
                            <i class="bi bi-check-circle text-success fs-3 d-block mb-1"></i>
                            Tidak ada sinyal peringatan server aktif.
                        </div>
                    @endforelse
                </div>
            </div>

            <!-- Degraded Plugins -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-cpu text-info me-2"></i>Status Modul Plugin
                    </h5>
                </div>
                <div class="card-body">
                    @forelse($degradedPlugins as $plg)
                        <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom border-secondary border-opacity-25">
                            <div>
                                <span class="text-white small fw-bold">{{ $plg->plugin_name }}</span>
                                <div class="text-muted" style="font-size: 0.75rem;">{{ $plg->plugin_id }}</div>
                            </div>
                            <span class="badge bg-danger">{{ $plg->health_status }}</span>
                        </div>
                    @empty
                        <div class="text-center text-white-50 py-3">
                            <i class="bi bi-heart-pulse text-success fs-3 d-block mb-1"></i>
                            Seluruh plugin custom Apexsions berjalan normal (Healthy).
                        </div>
                    @endforelse
                </div>
            </div>
        </div>
    </div>

    <!-- Active Rules & Recent Normalized Events -->
    <div class="row g-4">
        <!-- Explicit Intelligence Rules -->
        <div class="col-xl-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-gear-wide-connected text-warning me-2"></i>Explicit Rule Engine (Non-AI Anomaly Detection)
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small text-uppercase">
                                    <th>Rule ID</th>
                                    <th>Kategori</th>
                                    <th>Tingkat</th>
                                    <th>Cooldown</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($rules as $rule)
                                    <tr>
                                        <td>
                                            <div class="fw-bold text-white small">{{ $rule->name }}</div>
                                            <div class="text-white-50" style="font-size: 0.75rem;">{{ $rule->rule_id }}</div>
                                            <div class="text-muted" style="font-size: 0.75rem;">{{ $rule->description }}</div>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary">{{ $rule->category }}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-warning text-dark">{{ $rule->severity }}</span>
                                        </td>
                                        <td class="text-white-50 small">
                                            {{ $rule->cooldown_minutes }} m
                                        </td>
                                        <td>
                                            <span class="badge {{ $rule->is_enabled ? 'bg-success' : 'bg-danger' }}">
                                                {{ $rule->is_enabled ? 'AKTIF' : 'NONAKTIF' }}
                                            </span>
                                        </td>
                                    </tr>
                                @endforeach
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Normalized Events -->
        <div class="col-xl-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-clock-history text-info me-2"></i>Aliran Event Ternormalisasi Terkini
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small text-uppercase">
                                    <th>Waktu</th>
                                    <th>Event Type</th>
                                    <th>Source</th>
                                    <th>Entitas / Aktor</th>
                                    <th>Tingkat</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($recentEvents as $evt)
                                    <tr>
                                        <td class="text-white-50 small" style="white-space: nowrap;">
                                            {{ $evt->occurred_at->format('H:i:s') }}
                                            <div class="text-muted" style="font-size: 0.7rem;">{{ $evt->occurred_at->diffForHumans() }}</div>
                                        </td>
                                        <td>
                                            <span class="fw-semibold text-white small">{{ $evt->event_type }}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary text-uppercase" style="font-size: 0.7rem;">{{ $evt->source }}</span>
                                        </td>
                                        <td>
                                            <div class="text-white small">{{ $evt->actor_name ?: ($evt->actor_id ?: 'System') }}</div>
                                            @if($evt->target_name || $evt->target_id)
                                                <div class="text-white-50" style="font-size: 0.75rem;">→ {{ $evt->target_name ?: $evt->target_id }}</div>
                                            @endif
                                        </td>
                                        <td>
                                            <span class="badge bg-{{ strtolower($evt->severity) === 'critical' ? 'danger' : (strtolower($evt->severity) === 'high' ? 'warning text-dark' : 'info') }}" style="font-size: 0.7rem;">
                                                {{ $evt->severity }}
                                            </span>
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="5" class="text-center text-white-50 py-4">
                                            Belum ada log event yang terekam.
                                        </td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
