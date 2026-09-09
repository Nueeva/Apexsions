@extends('admin.layouts.admin')

@section('title', 'Server Operations — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-hdd-network-fill text-warning me-2"></i>Server Operations & Safe Control
            </h2>
            <p class="text-white-50 small mb-0">Pusat pemantauan kesehatan Paper API, eksekusi tindakan server terproteksi, dan manajemen mode pemeliharaan.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.server.actions') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-journal-text me-1"></i>Server Action History
            </a>
            <a href="{{ route('apexsions-bridge.admin.server.plugins') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-puzzle-fill me-1"></i>Plugin Status
            </a>
            <a href="{{ route('apexsions-bridge.admin.server.metrics') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-graph-up me-1"></i>Historical Metrics
            </a>
            <button type="button" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="modal" data-bs-target="#quickActionModal">
                <i class="bi bi-play-circle-fill me-1"></i>Safe Action Desk
            </button>
        </div>
    </div>

    <!-- Active Server Status & Maintenance Banner -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-body p-4">
            <div class="row align-items-center g-3">
                <div class="col-lg-8">
                    <div class="d-flex align-items-center gap-3 mb-2 flex-wrap">
                        <span class="badge {{ $serverStatus['badge_class'] }} px-3 py-2 fs-6 shadow-sm fw-bold font-monospace">
                            {{ $serverStatus['label'] }}
                        </span>
                        @if($maintenance->isEnabled())
                            <span class="badge bg-danger bg-opacity-25 text-danger border border-danger border-opacity-50 px-2 py-1">
                                <i class="bi bi-shield-lock-fill me-1"></i>Non-Staf Diblokir Masuk
                            </span>
                        @endif
                        <span class="text-white-50 small">
                            <i class="bi bi-clock-history me-1"></i>Pembaruan: <span class="text-white fw-medium">{{ $serverStatus['last_updated'] }}</span>
                        </span>
                    </div>
                    <p class="text-white-50 mb-0" style="font-size: 0.95rem;">
                        {{ $serverStatus['description'] }}
                    </p>
                    <div class="mt-2 text-muted font-monospace" style="font-size: 0.75rem;">
                        Sumber Data: <span class="text-warning text-opacity-75">{{ $serverStatus['source'] }}</span>
                    </div>
                </div>
                <div class="col-lg-4 text-lg-end d-flex gap-2 justify-content-lg-end align-items-center flex-wrap">
                    @if($maintenance->isEnabled())
                        <form action="{{ route('apexsions-bridge.admin.server.maintenance.toggle') }}" method="POST" class="d-inline" onsubmit="return confirm('Apakah Anda yakin ingin menonaktifkan Maintenance Mode dan membuka kembali akses server untuk pemain?');">
                            @csrf
                            <input type="hidden" name="is_enabled" value="0">
                            <button type="submit" class="btn btn-success btn-sm px-3 py-2 fw-bold shadow-sm">
                                <i class="bi bi-unlock-fill me-1"></i>Buka Server (Matikan Maintenance)
                            </button>
                        </form>
                    @endif
                    <button type="button" class="btn {{ $maintenance->isEnabled() ? 'btn-outline-danger' : 'btn-outline-warning' }} btn-sm px-3 py-2 fw-medium shadow-sm" data-bs-toggle="modal" data-bs-target="#maintenanceModal">
                        <i class="bi bi-tools me-1"></i>{{ $maintenance->isEnabled() ? 'Konfigurasi Pemeliharaan' : 'Aktifkan Mode Pemeliharaan' }}
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Active Alerts Banner (If Any) -->
    @if($activeAlerts->isNotEmpty())
        <div class="mb-4">
            <h5 class="h6 fw-bold text-white mb-2 d-flex align-items-center">
                <i class="bi bi-exclamation-triangle-fill text-danger me-2"></i>Peringatan Kesehatan Server Aktif ({{ $activeAlerts->count() }})
            </h5>
            <div class="d-flex flex-column gap-2">
                @foreach($activeAlerts as $alert)
                    <div class="alert alert-{{ $alert->severity === 'CRITICAL' ? 'danger' : 'warning' }} bg-opacity-10 border-{{ $alert->severity === 'CRITICAL' ? 'danger' : 'warning' }} border-opacity-25 mb-0 d-flex justify-content-between align-items-center flex-wrap gap-2 p-3">
                        <div>
                            <span class="badge bg-{{ $alert->severity === 'CRITICAL' ? 'danger' : 'warning' }} text-{{ $alert->severity === 'CRITICAL' ? 'white' : 'dark' }} me-2 font-monospace">
                                {{ $alert->type }}
                            </span>
                            <strong class="text-white">{{ $alert->message }}</strong>
                            <span class="text-white-50 ms-2 small">({{ $alert->created_at->diffForHumans() }})</span>
                            @if($alert->acknowledged_by)
                                <span class="badge bg-secondary bg-opacity-50 text-white-50 ms-2">Dikonfirmasi oleh {{ $alert->acknowledged_by }}</span>
                            @endif
                        </div>
                        <div class="d-flex gap-2">
                            @if($alert->status === 'ACTIVE')
                                <form action="{{ route('apexsions-bridge.admin.server.alerts.acknowledge', $alert->id) }}" method="POST">
                                    @csrf
                                    <button type="submit" class="btn btn-sm btn-outline-warning py-0 px-2" style="font-size: 0.75rem;">Konfirmasi</button>
                                </form>
                            @endif
                            <form action="{{ route('apexsions-bridge.admin.server.alerts.resolve', $alert->id) }}" method="POST">
                                @csrf
                                <button type="submit" class="btn btn-sm btn-outline-success py-0 px-2" style="font-size: 0.75rem;">Selesaikan</button>
                            </form>
                        </div>
                    </div>
                @endforeach
            </div>
        </div>
    @endif

    <!-- Health Metrics Grid -->
    <div class="row g-3 mb-4">
        <!-- TPS Metric -->
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium">TPS (TICKS PER SECOND)</span>
                        <span class="badge {{ $metrics['tps']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['tps']['status'] === 'WARNING' ? 'bg-warning text-dark' : 'bg-danger') }} font-monospace" style="font-size: 0.65rem;">
                            {{ $metrics['tps']['status'] }}
                        </span>
                    </div>
                    <div class="d-flex align-items-baseline gap-2 mb-2">
                        <h3 class="h3 fw-bold text-white mb-0">{{ $metrics['tps']['value'] }}</h3>
                        <span class="text-white-50 small">/ 20.0 Target</span>
                    </div>
                    <div class="progress bg-secondary bg-opacity-25 mb-2" style="height: 6px;">
                        <div class="progress-bar {{ $metrics['tps']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['tps']['status'] === 'WARNING' ? 'bg-warning' : 'bg-danger') }}" style="width: {{ $metrics['tps']['stability_pct'] }}%;"></div>
                    </div>
                    <div class="text-muted font-monospace" style="font-size: 0.68rem;">
                        Sumber: {{ $metrics['tps']['source'] }}
                    </div>
                </div>
            </div>
        </div>

        <!-- MSPT Metric -->
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium">MSPT (TICK DURATION)</span>
                        <span class="badge {{ $metrics['mspt']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['mspt']['status'] === 'WARNING' ? 'bg-warning text-dark' : 'bg-danger') }} font-monospace" style="font-size: 0.65rem;">
                            {{ $metrics['mspt']['status'] }}
                        </span>
                    </div>
                    <div class="d-flex align-items-baseline gap-2 mb-2">
                        <h3 class="h3 fw-bold text-white mb-0">{{ $metrics['mspt']['value'] }}</h3>
                        <span class="text-white-50 small">&lt; 50.0 ms Ideal</span>
                    </div>
                    <div class="progress bg-secondary bg-opacity-25 mb-2" style="height: 6px;">
                        <div class="progress-bar {{ $metrics['mspt']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['mspt']['status'] === 'WARNING' ? 'bg-warning' : 'bg-danger') }}" style="width: {{ min(100, round(($metrics['mspt']['raw'] / 50.0) * 100)) }}%;"></div>
                    </div>
                    <div class="text-muted font-monospace" style="font-size: 0.68rem;">
                        Sumber: {{ $metrics['mspt']['source'] }}
                    </div>
                </div>
            </div>
        </div>

        <!-- JVM Memory Metric -->
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium">ALOKASI RAM JVM</span>
                        <span class="badge {{ $metrics['memory']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['memory']['status'] === 'WARNING' ? 'bg-warning text-dark' : 'bg-danger') }} font-monospace" style="font-size: 0.65rem;">
                            {{ $metrics['memory']['percentage'] }}%
                        </span>
                    </div>
                    <div class="d-flex align-items-baseline gap-2 mb-2">
                        <h3 class="h3 fw-bold text-white mb-0">{{ $metrics['memory']['value'] }}</h3>
                        <span class="text-white-50 small">/ {{ number_format($metrics['memory']['max_mb']) }} MB</span>
                    </div>
                    <div class="progress bg-secondary bg-opacity-25 mb-2" style="height: 6px;">
                        <div class="progress-bar {{ $metrics['memory']['status'] === 'HEALTHY' ? 'bg-success' : ($metrics['memory']['status'] === 'WARNING' ? 'bg-warning' : 'bg-danger') }}" style="width: {{ $metrics['memory']['percentage'] }}%;"></div>
                    </div>
                    <div class="text-muted font-monospace" style="font-size: 0.68rem;">
                        Sisa: {{ number_format($metrics['memory']['free_mb']) }} MB
                    </div>
                </div>
            </div>
        </div>

        <!-- Online Players Metric -->
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium">PEMAIN AKTIF</span>
                        <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25 font-monospace" style="font-size: 0.65rem;">
                            LIVE
                        </span>
                    </div>
                    <div class="d-flex align-items-baseline gap-2 mb-2">
                        <h3 class="h3 fw-bold text-warning mb-0">{{ $metrics['players']['count'] }}</h3>
                        <span class="text-white-50 small">/ {{ $metrics['players']['max'] }} Slot</span>
                    </div>
                    <div class="progress bg-secondary bg-opacity-25 mb-2" style="height: 6px;">
                        <div class="progress-bar bg-warning" style="width: {{ $metrics['players']['percentage'] }}%;"></div>
                    </div>
                    <div class="text-muted font-monospace" style="font-size: 0.68rem;">
                        Uptime: {{ $metrics['uptime']['formatted'] }}
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <!-- World & Host Infrastructure Info -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-cpu-fill text-warning me-2"></i>Infrastruktur Server & Dunia
                    </h5>
                    <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace">
                        Real-time Telemetry
                    </span>
                </div>
                <div class="card-body p-3">
                    <div class="row g-3">
                        <div class="col-6">
                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="text-white-50 small mb-1">Beban CPU Host</div>
                                <div class="h5 fw-bold text-white mb-0">{{ $metrics['cpu']['value'] }}</div>
                                <div class="text-muted font-monospace mt-1" style="font-size: 0.68rem;">{{ $metrics['cpu']['source'] }}</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="text-white-50 small mb-1">Penyimpanan Disk VPS</div>
                                <div class="h5 fw-bold text-white mb-0">{{ $metrics['disk']['value'] }}</div>
                                <div class="text-muted font-monospace mt-1" style="font-size: 0.68rem;">{{ $metrics['disk']['source'] }}</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="text-white-50 small mb-1">Loaded Chunks</div>
                                <div class="h5 fw-bold text-white mb-0">{{ number_format($metrics['world']['chunks']) }}</div>
                                <div class="text-muted font-monospace mt-1" style="font-size: 0.68rem;">Aktif di seluruh dunia</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="text-white-50 small mb-1">Entitas Aktif</div>
                                <div class="h5 fw-bold text-white mb-0">{{ number_format($metrics['world']['entities']) }}</div>
                                <div class="text-muted font-monospace mt-1" style="font-size: 0.68rem;">Monster, Hewan, Item</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- WebBridge Health Status -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-broadcast text-warning me-2"></i>Status WebBridge & Antrean Delivery
                    </h5>
                    <span class="badge {{ $bridgeHealth['badge_class'] }} font-monospace">
                        {{ $bridgeHealth['status'] }}
                    </span>
                </div>
                <div class="card-body p-3">
                    <p class="text-white-50 small mb-3">{{ $bridgeHealth['message'] }}</p>
                    <div class="row g-2 text-center">
                        <div class="col-3">
                            <div class="p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="h5 fw-bold text-warning mb-0">{{ $bridgeHealth['pending_count'] }}</div>
                                <div class="text-white-50" style="font-size: 0.7rem;">Pending</div>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="h5 fw-bold text-info mb-0">{{ $bridgeHealth['processing_count'] }}</div>
                                <div class="text-white-50" style="font-size: 0.7rem;">Processing</div>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="h5 fw-bold text-success mb-0">{{ $bridgeHealth['delivered_today'] }}</div>
                                <div class="text-white-50" style="font-size: 0.7rem;">Terkirim Hari Ini</div>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-10">
                                <div class="h5 fw-bold text-danger mb-0">{{ $bridgeHealth['failed_today'] }}</div>
                                <div class="text-white-50" style="font-size: 0.7rem;">Gagal Hari Ini</div>
                            </div>
                        </div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top border-secondary border-opacity-10 text-muted" style="font-size: 0.75rem;">
                        <span>Polling Terakhir: <span class="text-white-50">{{ $bridgeHealth['last_sync'] }}</span></span>
                        <span>Antrean Tertua: <span class="text-white-50">{{ $bridgeHealth['oldest_pending_age_seconds'] }} detik</span></span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Server Actions Log -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
            <h5 class="card-title text-white h6 mb-0 fw-bold">
                <i class="bi bi-clock-history text-warning me-2"></i>Tindakan Administratif Server Terbaru
            </h5>
            <a href="{{ route('apexsions-bridge.admin.server.actions') }}" class="btn btn-outline-warning btn-sm py-0 px-2" style="font-size: 0.75rem;">
                Lihat Seluruh Log
            </a>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                        <th class="ps-3">ACTION ID</th>
                        <th>TINDAKAN</th>
                        <th>STAF</th>
                        <th>ALASAN</th>
                        <th>STATUS</th>
                        <th class="text-end pe-3">WAKTU</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($recentActions as $act)
                        <tr>
                            <td class="ps-3 font-monospace small text-warning">
                                {{ substr($act->action_id ?? $act->metadata['action_id'] ?? 'N/A', 0, 8) }}...
                            </td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-50 text-white font-monospace">
                                    {{ $act->action }}
                                </span>
                            </td>
                            <td class="text-white small">{{ $act->actor_name }}</td>
                            <td class="text-white-50 small">{{ Str::limit($act->reason ?? 'Tidak ada alasan', 40) }}</td>
                            <td>
                                <span class="badge {{ $act->status === 'SUCCESS' ? 'bg-success' : ($act->status === 'PENDING' ? 'bg-warning text-dark' : 'bg-danger') }}">
                                    {{ $act->status }}
                                </span>
                            </td>
                            <td class="text-end pe-3 text-white-50 small">{{ $act->created_at->diffForHumans() }}</td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center py-4 text-white-50 small">
                                <i class="bi bi-inbox text-muted fs-4 d-block mb-1"></i>
                                Belum ada tindakan administratif server yang tercatat.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Safe Server Action Modal -->
<div class="modal fade" id="quickActionModal" tabindex="-1" aria-labelledby="quickActionModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary border-opacity-50 text-white">
            <form action="{{ route('apexsions-bridge.admin.server.actions.execute') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25 bg-black bg-opacity-25">
                    <h5 class="modal-title h6 fw-bold text-warning" id="quickActionModalLabel">
                        <i class="bi bi-play-circle-fill me-2"></i>Eksekusi Tindakan Server Terproteksi
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="alert alert-info bg-opacity-10 border-info border-opacity-25 small mb-3">
                        <i class="bi bi-shield-check me-1"></i>Setiap tindakan dieksekusi melalui template perintah tervalidasi dengan pencatatan audit log otomatis.
                    </div>

                    <div class="mb-3">
                        <label for="actionTypeSelect" class="form-label text-white-50 small fw-bold">PILIH TINDAKAN</label>
                        <select class="form-select bg-black text-white border-secondary border-opacity-50" id="actionTypeSelect" name="action_type" required onchange="updateActionForm(this.value)">
                            <option value="BROADCAST">Broadcast Pengumuman (SAFE)</option>
                            <option value="SAVE_WORLD">Save World Chunks (SENSITIVE)</option>
                            <option value="CLEAR_ITEMS">Pembersihan Sampah Entitas / Item (SENSITIVE)</option>
                            <option value="RELOAD_CONFIG">Reload Konfigurasi ApexsionsCore (SENSITIVE)</option>
                            <option value="CHAT_MUTE">Toggle Global Chat Mute (SENSITIVE)</option>
                            <option value="CHAT_CLEAR">Bersihkan Riwayat Chat Buffer (SENSITIVE)</option>
                            <option value="AH_CLEAR">Bersihkan Lelang Expired / AH Clear (SENSITIVE)</option>
                            <option value="TOGGLE_WAR">Toggle Status Perang Kerajaan (SENSITIVE)</option>
                        </select>
                    </div>

                    <div class="mb-3" id="broadcastMessageField">
                        <label for="broadcastMessage" class="form-label text-white-50 small fw-bold">PESAN BROADCAST</label>
                        <input type="text" class="form-control bg-black text-white border-secondary border-opacity-50" id="broadcastMessage" name="message" placeholder="Tulis pengumuman resmi server...">
                    </div>

                    <div class="mb-3">
                        <label for="actionReason" class="form-label text-white-50 small fw-bold">ALASAN TINDAKAN (WAJIB UNTUK TINDAKAN SENSITIF)</label>
                        <textarea class="form-control bg-black text-white border-secondary border-opacity-50" id="actionReason" name="reason" rows="2" placeholder="Jelaskan alasan operasional untuk catatan audit log..."></textarea>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25 bg-black bg-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">
                        <i class="bi bi-send-fill me-1"></i>Eksekusi Tindakan
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Maintenance Mode Modal -->
<div class="modal fade" id="maintenanceModal" tabindex="-1" aria-labelledby="maintenanceModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary border-opacity-50 text-white">
            <form action="{{ route('apexsions-bridge.admin.server.maintenance.toggle') }}" method="POST">
                @csrf
                <input type="hidden" name="is_enabled" value="0">
                <input type="hidden" name="allow_staff" value="0">
                <div class="modal-header border-secondary border-opacity-25 bg-black bg-opacity-25">
                    <h5 class="modal-title h6 fw-bold text-warning" id="maintenanceModalLabel">
                        <i class="bi bi-tools me-2"></i>Konfigurasi Mode Pemeliharaan (Maintenance)
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="form-check form-switch mb-3 p-3 rounded border border-secondary border-opacity-25 bg-black bg-opacity-25">
                        <input class="form-check-input ms-0 me-3" type="checkbox" role="switch" id="maintenanceSwitch" name="is_enabled" value="1" {{ $maintenance->isEnabled() ? 'checked' : '' }}>
                        <label class="form-check-label fw-bold text-white fs-6" for="maintenanceSwitch">
                            Aktifkan Maintenance Mode
                        </label>
                        <div class="text-white-50 small mt-1">Hilangkan centang tombol switch ini untuk menonaktifkan pemeliharaan dan membuka server untuk umum.</div>
                    </div>

                    <div class="mb-3">
                        <label for="maintenanceMessage" class="form-label text-white-50 small fw-bold">PESAN KICK TAMPILAN PEMAIN</label>
                        <input type="text" class="form-control bg-black text-white border-secondary border-opacity-50" id="maintenanceMessage" name="message" value="{{ $maintenance->message }}" placeholder="Server sedang dalam pemeliharaan berkala. Silakan kembali beberapa saat lagi.">
                    </div>

                    <div class="mb-3">
                        <label for="maintenanceReason" class="form-label text-white-50 small fw-bold">ALASAN PEMELIHARAAN (WAJIB JIKA DIAKTIFKAN)</label>
                        <input type="text" class="form-control bg-black text-white border-secondary border-opacity-50" id="maintenanceReason" name="reason" value="{{ $maintenance->reason }}" placeholder="Contoh: Migrasi database server / update plugin">
                    </div>

                    <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" id="allowStaffCheck" name="allow_staff" value="1" {{ $maintenance->allow_staff ? 'checked' : '' }}>
                        <label class="form-check-label text-white-50 small" for="allowStaffCheck">
                            Izinkan staf dengan permission <code class="text-warning">apexsions.maintenance.bypass</code> tetap masuk
                        </label>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25 bg-black bg-opacity-25 justify-content-between">
                    <div>
                        @if($maintenance->isEnabled())
                            <button type="submit" name="is_enabled" value="0" class="btn btn-outline-success btn-sm fw-bold">
                                <i class="bi bi-unlock-fill me-1"></i>Matikan Sekarang
                            </button>
                        @endif
                    </div>
                    <div class="d-flex gap-2">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                        <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">
                            Simpan Perubahan
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function updateActionForm(action) {
    const msgField = document.getElementById('broadcastMessageField');
    if (action === 'BROADCAST') {
        msgField.style.display = 'block';
    } else {
        msgField.style.display = 'none';
    }
}
</script>
@endsection
