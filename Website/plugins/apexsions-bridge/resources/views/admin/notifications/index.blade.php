@extends('admin.layouts.admin')

@section('title', 'Admin Notifications Desk — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-0">
                <i class="bi bi-bell-fill text-warning me-2"></i>Admin Notifications Desk
            </h2>
            <p class="text-white-50 small mb-0">Pusat orkestrasi notifikasi sistem, peringatan dini terdeduplikasi, dan konfirmasi pengakuan insiden.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.automation.index') }}" class="btn btn-outline-info btn-sm">
                <i class="bi bi-gear-wide-connected me-1"></i>Safe Automation
            </a>
            <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-outline-warning btn-sm">
                <i class="bi bi-exclamation-octagon-fill me-1"></i>Incident Registry
            </a>
        </div>
    </div>

    <!-- Quick Stats Bar -->
    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-white-50 small fw-bold text-uppercase">Belum Diakui</div>
                        <div class="fs-4 fw-bold text-warning">{{ number_format($stats['unacknowledged']) }}</div>
                    </div>
                    <div class="bg-warning bg-opacity-10 text-warning rounded p-2">
                        <i class="bi bi-bell-fill fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-white-50 small fw-bold text-uppercase">Kritis Terbuka</div>
                        <div class="fs-4 fw-bold text-danger">{{ number_format($stats['critical']) }}</div>
                    </div>
                    <div class="bg-danger bg-opacity-10 text-danger rounded p-2">
                        <i class="bi bi-shield-slash-fill fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-white-50 small fw-bold text-uppercase">Tingkat Tinggi</div>
                        <div class="fs-4 fw-bold text-warning">{{ number_format($stats['high']) }}</div>
                    </div>
                    <div class="bg-warning bg-opacity-10 text-warning rounded p-2">
                        <i class="bi bi-exclamation-triangle-fill fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-white-50 small fw-bold text-uppercase">Total Terdaftar</div>
                        <div class="fs-4 fw-bold text-white">{{ number_format($stats['total']) }}</div>
                    </div>
                    <div class="bg-primary bg-opacity-10 text-primary rounded p-2">
                        <i class="bi bi-archive-fill fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Filters -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-3">
            <form method="GET" action="{{ route('apexsions-bridge.admin.notifications.index') }}" class="row g-2 align-items-center">
                <div class="col-md-4">
                    <div class="input-group input-group-sm">
                        <span class="input-group-text bg-dark border-secondary text-white-50"><i class="bi bi-search"></i></span>
                        <input type="text" name="q" class="form-control bg-dark border-secondary text-white" placeholder="Cari notifikasi, entitas, ID..." value="{{ $search }}">
                    </div>
                </div>
                <div class="col-md-3">
                    <select name="status" class="form-select form-select-sm bg-dark border-secondary text-white">
                        <option value="all" {{ $currentStatus === 'all' ? 'selected' : '' }}>Semua Status</option>
                        <option value="UNACKNOWLEDGED" {{ $currentStatus === 'UNACKNOWLEDGED' ? 'selected' : '' }}>Belum Diakui (Unacknowledged)</option>
                        <option value="ACKNOWLEDGED" {{ $currentStatus === 'ACKNOWLEDGED' ? 'selected' : '' }}>Sudah Diakui (Acknowledged)</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <select name="severity" class="form-select form-select-sm bg-dark border-secondary text-white">
                        <option value="all" {{ $currentSeverity === 'all' ? 'selected' : '' }}>Semua Keparahan</option>
                        <option value="CRITICAL" {{ $currentSeverity === 'CRITICAL' ? 'selected' : '' }}>CRITICAL</option>
                        <option value="HIGH" {{ $currentSeverity === 'HIGH' ? 'selected' : '' }}>HIGH</option>
                        <option value="MEDIUM" {{ $currentSeverity === 'MEDIUM' ? 'selected' : '' }}>MEDIUM</option>
                        <option value="LOW" {{ $currentSeverity === 'LOW' ? 'selected' : '' }}>LOW</option>
                        <option value="INFO" {{ $currentSeverity === 'INFO' ? 'selected' : '' }}>INFO</option>
                    </select>
                </div>
                <div class="col-md-2 d-flex gap-1">
                    <button type="submit" class="btn btn-primary btn-sm w-100">Filter</button>
                    <a href="{{ route('apexsions-bridge.admin.notifications.index') }}" class="btn btn-outline-secondary btn-sm" title="Reset"><i class="bi bi-arrow-counterclockwise"></i></a>
                </div>
            </form>
        </div>
    </div>

    <!-- Table -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="table-responsive">
            <table class="table table-dark table-hover align-middle mb-0">
                <thead class="table-dark text-white-50 small text-uppercase">
                    <tr>
                        <th style="width: 100px;">Keparahan</th>
                        <th>Notifikasi & Pesan</th>
                        <th>Sumber & Tipe</th>
                        <th>Entitas Terkait</th>
                        <th>Frekuensi</th>
                        <th>Status</th>
                        <th>Waktu</th>
                        <th class="text-end">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($alertNotifications as $notif)
                        <tr>
                            <td>
                                @if($notif->severity === 'CRITICAL')
                                    <span class="badge bg-danger">CRITICAL</span>
                                @elseif($notif->severity === 'HIGH')
                                    <span class="badge bg-warning text-dark fw-bold">HIGH</span>
                                @elseif($notif->severity === 'MEDIUM')
                                    <span class="badge bg-info text-dark">MEDIUM</span>
                                @else
                                    <span class="badge bg-secondary">{{ $notif->severity }}</span>
                                @endif
                            </td>
                            <td>
                                <div class="fw-bold text-white">{{ $notif->title }}</div>
                                <div class="small text-white-50 text-truncate" style="max-width: 380px;">{{ $notif->message }}</div>
                                @if($notif->incident_id)
                                    <a href="{{ route('apexsions-bridge.admin.incidents.show', $notif->incident_id) }}" class="badge bg-dark border border-warning text-warning text-decoration-none mt-1">
                                        <i class="bi bi-link-45deg"></i> {{ $notif->incident_id }}
                                    </a>
                                @endif
                            </td>
                            <td>
                                <span class="badge bg-dark border border-secondary">{{ $notif->source }}</span>
                                <div class="small text-white-50 mt-1">{{ $notif->type }}</div>
                            </td>
                            <td>
                                <span class="badge bg-dark border border-secondary">{{ $notif->entity_type }}</span>
                                <div class="small font-monospace text-white-50">{{ Str::limit($notif->entity_id, 18) }}</div>
                            </td>
                            <td>
                                @if($notif->occurrence_count > 1)
                                    <span class="badge bg-danger bg-opacity-20 text-danger border border-danger border-opacity-50">
                                        {{ $notif->occurrence_count }}x terjaring
                                    </span>
                                @else
                                    <span class="text-white-50 small">1x</span>
                                @endif
                            </td>
                            <td>
                                @if($notif->status === 'UNACKNOWLEDGED')
                                    <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-clock me-1"></i>Menunggu Respon</span>
                                @else
                                    <span class="badge bg-success"><i class="bi bi-check2-circle me-1"></i>Diakui</span>
                                    <div class="small text-white-50 mt-1">Oleh: {{ $notif->acknowledged_by }}</div>
                                @endif
                            </td>
                            <td>
                                <div class="small text-white">{{ $notif->last_occurred_at ? $notif->last_occurred_at->diffForHumans() : '-' }}</div>
                                <div class="text-white-50" style="font-size: 0.75rem;">{{ $notif->last_occurred_at ? $notif->last_occurred_at->format('d/m/Y H:i') : '' }}</div>
                            </td>
                            <td class="text-end">
                                <div class="btn-group btn-group-sm">
                                    <a href="{{ route('apexsions-bridge.admin.notifications.show', $notif->notification_id) }}" class="btn btn-outline-light btn-sm" title="Rincian Dossier">
                                        <i class="bi bi-eye"></i>
                                    </a>
                                    @if($notif->status === 'UNACKNOWLEDGED')
                                        <form method="POST" action="{{ route('apexsions-bridge.admin.notifications.acknowledge', $notif->notification_id) }}" class="d-inline">
                                            @csrf
                                            <button type="submit" class="btn btn-success btn-sm" title="Acknowledge Notifikasi">
                                                <i class="bi bi-check2"></i>
                                            </button>
                                        </form>
                                    @endif
                                </div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="8" class="text-center py-5 text-white-50">
                                <i class="bi bi-bell-slash fs-1 d-block mb-2"></i>
                                Tidak ada notifikasi yang sesuai dengan kriteria filter saat ini.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        @if(isset($alertNotifications) && method_exists($alertNotifications, 'hasPages') && $alertNotifications->hasPages())
            <div class="card-footer bg-dark border-secondary border-opacity-25 p-3">
                {{ $alertNotifications->links() }}
            </div>
        @endif
    </div>
</div>
@endsection
