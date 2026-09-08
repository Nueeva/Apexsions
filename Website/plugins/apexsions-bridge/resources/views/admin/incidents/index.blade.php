@extends('admin.layouts.admin')

@section('title', 'Daftar Insiden & Investigasi — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <a href="{{ route('apexsions-bridge.admin.intelligence.index') }}" class="btn btn-outline-secondary btn-sm" title="Kembali ke Intelligence Dashboard">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <h2 class="h3 fw-bold text-white mb-0">
                    <i class="bi bi-exclamation-octagon-fill text-warning me-2"></i>Incident Management Registry
                </h2>
            </div>
            <p class="text-white-50 small mb-0">Manajemen berkas investigasi, eskalasi anomali, penugasan staf, dan penelusuran insiden realm Apexsions.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.intelligence.index') }}" class="btn btn-outline-warning btn-sm">
                <i class="bi bi-shield-check me-1"></i>Intelligence Dashboard
            </a>
        </div>
    </div>

    <!-- Status Stats Badges Bar -->
    <div class="row g-2 mb-4">
        <div class="col-auto">
            <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-sm {{ $selectedStatus === 'all' ? 'btn-primary' : 'btn-dark border-secondary' }}">
                Semua <span class="badge bg-light text-dark ms-1">{{ $counts['total'] }}</span>
            </a>
        </div>
        <div class="col-auto">
            <a href="{{ route('apexsions-bridge.admin.incidents.index', ['status' => 'ACTIVE']) }}" class="btn btn-sm {{ $selectedStatus === 'ACTIVE' ? 'btn-warning text-dark fw-bold' : 'btn-dark border-secondary' }}">
                Aktif <span class="badge bg-warning text-dark ms-1">{{ $counts['active'] }}</span>
            </a>
        </div>
        <div class="col-auto">
            <a href="{{ route('apexsions-bridge.admin.incidents.index', ['severity' => 'CRITICAL']) }}" class="btn btn-sm {{ $selectedSeverity === 'CRITICAL' ? 'btn-danger fw-bold' : 'btn-dark border-secondary' }}">
                Kritis <span class="badge bg-danger ms-1">{{ $counts['critical'] }}</span>
            </a>
        </div>
        <div class="col-auto">
            <a href="{{ route('apexsions-bridge.admin.incidents.index', ['status' => 'RESOLVED']) }}" class="btn btn-sm {{ $selectedStatus === 'RESOLVED' ? 'btn-success fw-bold' : 'btn-dark border-secondary' }}">
                Terselesaikan <span class="badge bg-success ms-1">{{ $counts['resolved'] }}</span>
            </a>
        </div>
    </div>

    <!-- Filter Form -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-3">
            <form method="GET" action="{{ route('apexsions-bridge.admin.incidents.index') }}" class="row g-2 align-items-center">
                <div class="col-md-4">
                    <div class="input-group input-group-sm">
                        <span class="input-group-text bg-dark border-secondary text-white-50"><i class="bi bi-search"></i></span>
                        <input type="text" name="q" class="form-control bg-dark border-secondary text-white" placeholder="Cari ID, judul, entitas, staf..." value="{{ $search }}">
                    </div>
                </div>
                <div class="col-md-2">
                    <select name="status" class="form-select form-select-sm bg-dark border-secondary text-white">
                        <option value="all" {{ $selectedStatus === 'all' ? 'selected' : '' }}>Semua Status</option>
                        <option value="ACTIVE" {{ $selectedStatus === 'ACTIVE' ? 'selected' : '' }}>Aktif (Open/Investigating/Mitigated)</option>
                        <option value="OPEN" {{ $selectedStatus === 'OPEN' ? 'selected' : '' }}>OPEN</option>
                        <option value="INVESTIGATING" {{ $selectedStatus === 'INVESTIGATING' ? 'selected' : '' }}>INVESTIGATING</option>
                        <option value="MITIGATED" {{ $selectedStatus === 'MITIGATED' ? 'selected' : '' }}>MITIGATED</option>
                        <option value="RESOLVED" {{ $selectedStatus === 'RESOLVED' ? 'selected' : '' }}>RESOLVED</option>
                        <option value="CLOSED" {{ $selectedStatus === 'CLOSED' ? 'selected' : '' }}>CLOSED</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <select name="severity" class="form-select form-select-sm bg-dark border-secondary text-white">
                        <option value="all" {{ $selectedSeverity === 'all' ? 'selected' : '' }}>Semua Tingkat</option>
                        <option value="CRITICAL" {{ $selectedSeverity === 'CRITICAL' ? 'selected' : '' }}>CRITICAL</option>
                        <option value="HIGH" {{ $selectedSeverity === 'HIGH' ? 'selected' : '' }}>HIGH</option>
                        <option value="MEDIUM" {{ $selectedSeverity === 'MEDIUM' ? 'selected' : '' }}>MEDIUM</option>
                        <option value="LOW" {{ $selectedSeverity === 'LOW' ? 'selected' : '' }}>LOW</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <select name="type" class="form-select form-select-sm bg-dark border-secondary text-white">
                        <option value="all" {{ $selectedType === 'all' ? 'selected' : '' }}>Semua Tipe</option>
                        <option value="ECONOMY" {{ $selectedType === 'ECONOMY' ? 'selected' : '' }}>ECONOMY</option>
                        <option value="PLAYER" {{ $selectedType === 'PLAYER' ? 'selected' : '' }}>PLAYER</option>
                        <option value="MODERATION" {{ $selectedType === 'MODERATION' ? 'selected' : '' }}>MODERATION</option>
                        <option value="SERVER" {{ $selectedType === 'SERVER' ? 'selected' : '' }}>SERVER</option>
                        <option value="PLUGIN" {{ $selectedType === 'PLUGIN' ? 'selected' : '' }}>PLUGIN</option>
                        <option value="BRIDGE" {{ $selectedType === 'BRIDGE' ? 'selected' : '' }}>BRIDGE</option>
                        <option value="SECURITY" {{ $selectedType === 'SECURITY' ? 'selected' : '' }}>SECURITY</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex gap-1">
                    <button type="submit" class="btn btn-sm btn-primary w-100"><i class="bi bi-filter me-1"></i>Filter</button>
                    <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-x-circle"></i></a>
                </div>
            </form>
        </div>
    </div>

    <!-- Incidents Table Card -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-dark table-hover align-middle mb-0">
                    <thead>
                        <tr class="text-white-50 small text-uppercase">
                            <th>ID Insiden</th>
                            <th>Judul & Tipe</th>
                            <th>Keparahan</th>
                            <th>Status</th>
                            <th>Entitas Terkait</th>
                            <th>Frekuensi</th>
                            <th>Terakhir Muncul</th>
                            <th>Penugasan</th>
                            <th class="text-end">Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($incidents as $inc)
                            <tr>
                                <td>
                                    <a href="{{ route('apexsions-bridge.admin.incidents.show', $inc->incident_id) }}" class="text-info fw-bold text-decoration-none">
                                        {{ $inc->incident_id }}
                                    </a>
                                </td>
                                <td>
                                    <div class="fw-semibold text-white">{{ $inc->title }}</div>
                                    <span class="badge bg-secondary text-uppercase" style="font-size: 0.7rem;">{{ $inc->type }}</span>
                                </td>
                                <td>
                                    <span class="badge {{ $inc->severity_badge }}">{{ $inc->severity }}</span>
                                </td>
                                <td>
                                    <span class="badge {{ $inc->status_badge }}">{{ $inc->status }}</span>
                                </td>
                                <td>
                                    <div class="text-white small fw-bold">{{ $inc->root_entity_name }}</div>
                                    <div class="text-muted" style="font-size: 0.75rem;">{{ $inc->root_entity_type }}: {{ $inc->root_entity_id }}</div>
                                </td>
                                <td>
                                    <span class="badge bg-secondary bg-opacity-50 text-white">{{ $inc->occurrence_count }}x</span>
                                </td>
                                <td class="text-white-50 small" style="white-space: nowrap;">
                                    {{ $inc->last_occurred_at ? $inc->last_occurred_at->format('d M H:i') : $inc->detected_at->format('d M H:i') }}
                                    <div class="text-muted" style="font-size: 0.7rem;">{{ ($inc->last_occurred_at ?: $inc->detected_at)->diffForHumans() }}</div>
                                </td>
                                <td>
                                    @if($inc->assigned_to)
                                        <span class="badge bg-info bg-opacity-25 text-info border border-info border-opacity-25">
                                            <i class="bi bi-person-fill me-1"></i>{{ $inc->assigned_to }}
                                        </span>
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
                                <td colspan="9" class="text-center text-white-50 py-5">
                                    <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary"></i>
                                    Tidak ada catatan insiden yang sesuai dengan kriteria filter.
                                </td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
        @if($incidents->hasPages())
            <div class="card-footer bg-transparent border-secondary border-opacity-25 d-flex justify-content-end">
                {{ $incidents->links() }}
            </div>
        @endif
    </div>
</div>
@endsection
