@extends('admin.layouts.admin')

@section('title', $plugin->name . ' — Custom Plugin Control')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <a href="{{ route('apexsions-bridge.admin.plugins.index') }}" class="btn btn-outline-secondary btn-sm" title="Kembali ke Daftar Plugin">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <h2 class="h3 fw-bold text-white mb-0">
                    <i class="bi bi-box-seam-fill text-warning me-2"></i>{{ $plugin->name }}
                </h2>
                <span class="badge {{ $plugin->type_badge }} font-monospace ms-2">{{ $plugin->type }}</span>
                <span class="badge {{ $plugin->health_badge }} font-monospace">{{ $plugin->health_status }}</span>
                <span class="badge {{ $plugin->integration_badge }} font-monospace">{{ $plugin->integration_status }}</span>
            </div>
            <p class="text-white-50 small mb-0">{{ $plugin->description }}</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.server.actions') }}" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-clock-history me-1"></i>Audit History
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
    @if(session('error'))
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ session('error') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif

    <!-- Information & Diagnostics Cards -->
    <div class="row g-4 mb-4">
        <!-- Architecture Metadata -->
        <div class="col-lg-7">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-info-circle-fill text-warning me-2"></i>Spesifikasi & Informasi Plugin
                    </h5>
                </div>
                <div class="card-body p-3">
                    <div class="row g-3">
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block">Identifikasi Plugin (ID):</span>
                            <code class="text-warning">{{ $plugin->plugin_id }}</code>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block">Versi Release:</span>
                            <strong class="text-white">v{{ $plugin->version }}</strong>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block">Target Runtime / Platform:</span>
                            <span class="text-light">Minecraft 26.2 (Paper API)</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block">Java LTS Baseline:</span>
                            <span class="text-light">Java 21 LTS</span>
                        </div>
                        <div class="col-12">
                            <span class="text-white-50 small d-block">Main Class Entrypoint:</span>
                            <code class="text-light">{{ $plugin->metadata['main_class'] ?? 'N/A' }}</code>
                        </div>
                        <div class="col-12">
                            <span class="text-white-50 small d-block">Service Locator / SPI Provider:</span>
                            <code class="text-light">{{ $plugin->metadata['api_provider'] ?? 'In-Plugin SPI' }}</code>
                        </div>
                        @if(!empty($plugin->dependencies))
                            <div class="col-12">
                                <span class="text-white-50 small d-block mb-1">Dependensi Terhubung:</span>
                                @foreach($plugin->dependencies as $dep)
                                    <span class="badge bg-secondary bg-opacity-25 text-light me-1">{{ $dep }}</span>
                                @endforeach
                            </div>
                        @endif
                    </div>
                </div>
            </div>
        </div>

        <!-- Health & Status Engine -->
        <div class="col-lg-5">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-heart-pulse-fill text-danger me-2"></i>Status Kesehatan & Telemetri
                    </h5>
                </div>
                <div class="card-body p-3 d-flex flex-column justify-content-between">
                    <div>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="text-white-50 small">Status Operasional:</span>
                            <span class="badge {{ $plugin->status === 'ENABLED' ? 'bg-success' : 'bg-danger' }} font-monospace">
                                {{ $plugin->status }}
                            </span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="text-white-50 small">Evaluasi Kesehatan:</span>
                            <span class="badge {{ $plugin->health_badge }} font-monospace">
                                {{ $plugin->health_status }}
                            </span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="text-white-50 small">Koneksi WebBridge:</span>
                            <span class="badge {{ $plugin->integration_badge }} font-monospace">
                                {{ $plugin->integration_status }}
                            </span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="text-white-50 small">Heartbeat Terakhir:</span>
                            <span class="text-light small">
                                {{ $plugin->last_heartbeat_at ? $plugin->last_heartbeat_at->diffForHumans() : 'Belum pernah' }}
                            </span>
                        </div>
                    </div>

                    <div class="alert alert-secondary bg-black bg-opacity-25 border-secondary border-opacity-25 text-white-50 small mb-0">
                        <i class="bi bi-shield-check text-success me-1"></i>
                        Semua instruksi diproses lewat <strong>PluginActionGateway</strong> dengan verifikasi izin, idempoten, dan pencatatan audit trail otomatis.
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Capabilities Matrix Navigation -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
            <h5 class="card-title text-white h6 mb-0 fw-bold">
                <i class="bi bi-lightning-charge-fill text-warning me-2"></i>Matriks Kapabilitas Terdaftar ({{ $plugin->capabilities->count() }})
            </h5>
        </div>
        <div class="card-body p-3">
            <ul class="nav nav-pills mb-3" id="capabilitiesTabs" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active btn-sm" id="action-tab" data-bs-toggle="pill" data-bs-target="#tab-action" type="button" role="tab">
                        <i class="bi bi-play-circle-fill me-1 text-danger"></i>ACTION ({{ $groupedCapabilities['ACTION']->count() }})
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link btn-sm" id="write-tab" data-bs-toggle="pill" data-bs-target="#tab-write" type="button" role="tab">
                        <i class="bi bi-pencil-square me-1 text-warning"></i>WRITE ({{ $groupedCapabilities['WRITE']->count() }})
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link btn-sm" id="read-tab" data-bs-toggle="pill" data-bs-target="#tab-read" type="button" role="tab">
                        <i class="bi bi-eye-fill me-1 text-info"></i>READ ({{ $groupedCapabilities['READ']->count() }})
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link btn-sm" id="event-tab" data-bs-toggle="pill" data-bs-target="#tab-event" type="button" role="tab">
                        <i class="bi bi-broadcast me-1 text-primary"></i>EVENT ({{ $groupedCapabilities['EVENT']->count() }})
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link btn-sm" id="metric-tab" data-bs-toggle="pill" data-bs-target="#tab-metric" type="button" role="tab">
                        <i class="bi bi-bar-chart-fill me-1 text-success"></i>METRIC ({{ $groupedCapabilities['METRIC']->count() }})
                    </button>
                </li>
            </ul>

            <div class="tab-content" id="capabilitiesTabContent">
                <!-- Tab: ACTION -->
                <div class="tab-pane fade show active" id="tab-action" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small border-secondary">
                                    <th>KAPABILITAS</th>
                                    <th>KODE IDENTIFIER</th>
                                    <th>AKSES</th>
                                    <th>STATUS</th>
                                    <th>SYARAT AUDIT</th>
                                    <th class="text-end">AKSI GATEWAY</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($groupedCapabilities['ACTION'] as $cap)
                                    <tr>
                                        <td>
                                            <strong class="text-white d-block">{{ $cap->name }}</strong>
                                            <span class="text-white-50 small">{{ $cap->description }}</span>
                                        </td>
                                        <td><code>{{ $cap->capability_id }}</code></td>
                                        <td><span class="badge {{ $cap->access_badge }} font-monospace">{{ $cap->access }}</span></td>
                                        <td><span class="badge bg-success font-monospace">{{ $cap->status }}</span></td>
                                        <td>
                                            @if($cap->requires_reason)
                                                <span class="badge bg-warning text-dark small" title="Wajib mengisi alasan administratif">Alasan Wajib</span>
                                            @else
                                                <span class="badge bg-secondary bg-opacity-25 text-white-50 small">Standar</span>
                                            @endif
                                            @if($cap->requires_confirmation)
                                                <span class="badge bg-danger text-white small" title="Memerlukan konfirmasi ganda">Konfirmasi</span>
                                            @endif
                                        </td>
                                        <td class="text-end">
                                            @if(isset($supportedActions[$cap->capability_id]))
                                                <button type="button" 
                                                        class="btn btn-outline-warning btn-sm"
                                                        data-bs-toggle="modal" 
                                                        data-bs-target="#actionModal_{{ Str::slug($cap->capability_id) }}">
                                                    <i class="bi bi-gear-fill me-1"></i>Eksekusi
                                                </button>

                                                <!-- Action Confirmation Modal -->
                                                <div class="modal fade text-start" id="actionModal_{{ Str::slug($cap->capability_id) }}" tabindex="-1">
                                                    <div class="modal-dialog modal-dialog-centered">
                                                        <div class="modal-content bg-dark border-secondary">
                                                            <form method="POST" action="{{ route('apexsions-bridge.admin.plugins.action', $plugin->plugin_id) }}">
                                                                @csrf
                                                                <input type="hidden" name="capability_id" value="{{ $cap->capability_id }}">
                                                                <div class="modal-header border-secondary">
                                                                    <h5 class="modal-title text-white">
                                                                        <i class="bi bi-shield-exclamation text-warning me-2"></i>Konfirmasi Aksi: {{ $cap->name }}
                                                                    </h5>
                                                                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                                                </div>
                                                                <div class="modal-body">
                                                                    <div class="alert alert-warning bg-warning bg-opacity-10 border-warning text-warning small mb-3">
                                                                        <i class="bi bi-exclamation-triangle-fill me-1"></i>
                                                                        Aksi ini akan dieksekusi secara aman melalui WebBridge Delivery Queue dengan identifikasi unik (UUID Action ID).
                                                                    </div>

                                                                    <p class="text-white-50 small mb-3">
                                                                        Plugin Target: <strong class="text-white">{{ $plugin->name }}</strong><br>
                                                                        Identifier: <code>{{ $cap->capability_id }}</code>
                                                                    </p>

                                                                    @if($cap->capability_id === 'core.broadcast')
                                                                        <div class="mb-3">
                                                                            <label class="form-label text-white small">Pesan Pengumuman</label>
                                                                            <input type="text" name="params[message]" class="form-control bg-black border-secondary text-white" placeholder="Masukkan pesan pengumuman..." required>
                                                                        </div>
                                                                    @endif

                                                                    @if($cap->capability_id === 'core.maintenance.manage')
                                                                        <div class="mb-3">
                                                                            <label class="form-label text-white small">Mode Pemeliharaan</label>
                                                                            <select name="params[enable]" class="form-select bg-black border-secondary text-white">
                                                                                <option value="1">Aktifkan Maintenance (Kunci Server)</option>
                                                                                <option value="0">Nonaktifkan Maintenance (Buka Server)</option>
                                                                            </select>
                                                                        </div>
                                                                    @endif

                                                                    <div class="mb-3">
                                                                        <label class="form-label text-white small">
                                                                            Alasan Tindakan Administratif
                                                                            @if($cap->requires_reason) <span class="text-danger">*</span> @endif
                                                                        </label>
                                                                        <textarea name="reason" 
                                                                                  class="form-control bg-black border-secondary text-white" 
                                                                                  rows="2" 
                                                                                  placeholder="Tuliskan justifikasi operasional..."
                                                                                  {{ $cap->requires_reason ? 'required' : '' }}></textarea>
                                                                    </div>
                                                                </div>
                                                                <div class="modal-footer border-secondary">
                                                                    <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                                                                    <button type="submit" class="btn btn-warning btn-sm">
                                                                        <i class="bi bi-check-circle-fill me-1"></i>Konfirmasi & Masukkan Antrean
                                                                    </button>
                                                                </div>
                                                            </form>
                                                        </div>
                                                    </div>
                                                </div>
                                            @else
                                                <span class="badge bg-secondary bg-opacity-25 text-white-50 small">Internal / Bridge Managed</span>
                                            @endif
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="6" class="text-center text-white-50 py-3">Tidak ada kapabilitas ACTION pada plugin ini.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Tab: WRITE -->
                <div class="tab-pane fade" id="tab-write" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small border-secondary">
                                    <th>KAPABILITAS</th>
                                    <th>KODE IDENTIFIER</th>
                                    <th>AKSES</th>
                                    <th>STATUS</th>
                                    <th>SYARAT AUDIT</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($groupedCapabilities['WRITE'] as $cap)
                                    <tr>
                                        <td>
                                            <strong class="text-white d-block">{{ $cap->name }}</strong>
                                            <span class="text-white-50 small">{{ $cap->description }}</span>
                                        </td>
                                        <td><code>{{ $cap->capability_id }}</code></td>
                                        <td><span class="badge {{ $cap->access_badge }} font-monospace">{{ $cap->access }}</span></td>
                                        <td><span class="badge bg-success font-monospace">{{ $cap->status }}</span></td>
                                        <td>
                                            @if($cap->requires_reason)
                                                <span class="badge bg-warning text-dark small">Alasan Wajib</span>
                                            @else
                                                <span class="badge bg-secondary bg-opacity-25 text-white-50 small">Standar</span>
                                            @endif
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="5" class="text-center text-white-50 py-3">Tidak ada kapabilitas WRITE pada plugin ini.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Tab: READ -->
                <div class="tab-pane fade" id="tab-read" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small border-secondary">
                                    <th>KAPABILITAS</th>
                                    <th>KODE IDENTIFIER</th>
                                    <th>AKSES</th>
                                    <th>STATUS</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($groupedCapabilities['READ'] as $cap)
                                    <tr>
                                        <td>
                                            <strong class="text-white d-block">{{ $cap->name }}</strong>
                                            <span class="text-white-50 small">{{ $cap->description }}</span>
                                        </td>
                                        <td><code>{{ $cap->capability_id }}</code></td>
                                        <td><span class="badge {{ $cap->access_badge }} font-monospace">{{ $cap->access }}</span></td>
                                        <td><span class="badge bg-success font-monospace">{{ $cap->status }}</span></td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center text-white-50 py-3">Tidak ada kapabilitas READ pada plugin ini.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Tab: EVENT -->
                <div class="tab-pane fade" id="tab-event" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small border-secondary">
                                    <th>EVENT DISPATCHER</th>
                                    <th>KODE IDENTIFIER</th>
                                    <th>AKSES</th>
                                    <th>STATUS</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($groupedCapabilities['EVENT'] as $cap)
                                    <tr>
                                        <td>
                                            <strong class="text-white d-block">{{ $cap->name }}</strong>
                                            <span class="text-white-50 small">{{ $cap->description }}</span>
                                        </td>
                                        <td><code>{{ $cap->capability_id }}</code></td>
                                        <td><span class="badge {{ $cap->access_badge }} font-monospace">{{ $cap->access }}</span></td>
                                        <td><span class="badge bg-primary font-monospace">{{ $cap->status }}</span></td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center text-white-50 py-3">Tidak ada kapabilitas EVENT pada plugin ini.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Tab: METRIC -->
                <div class="tab-pane fade" id="tab-metric" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small border-secondary">
                                    <th>METRIK TELEMETRI</th>
                                    <th>KODE IDENTIFIER</th>
                                    <th>AKSES</th>
                                    <th>STATUS</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($groupedCapabilities['METRIC'] as $cap)
                                    <tr>
                                        <td>
                                            <strong class="text-white d-block">{{ $cap->name }}</strong>
                                            <span class="text-white-50 small">{{ $cap->description }}</span>
                                        </td>
                                        <td><code>{{ $cap->capability_id }}</code></td>
                                        <td><span class="badge {{ $cap->access_badge }} font-monospace">{{ $cap->access }}</span></td>
                                        <td><span class="badge bg-success font-monospace">{{ $cap->status }}</span></td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center text-white-50 py-3">Tidak ada kapabilitas METRIC pada plugin ini.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Health History Log -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
            <h5 class="card-title text-white h6 mb-0 fw-bold">
                <i class="bi bi-clock-history text-warning me-2"></i>Riwayat Status & Evaluasi Kesehatan
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-dark table-hover align-middle mb-0">
                    <thead>
                        <tr class="text-white-50 small border-secondary">
                            <th class="ps-3">TIMESTAMP</th>
                            <th>STATUS</th>
                            <th>HEALTH</th>
                            <th>KATEGORI</th>
                            <th>RINGKASAN DIAGNOSTIK</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($healthHistory as $record)
                            <tr>
                                <td class="ps-3 text-white-50 font-monospace small">
                                    {{ $record->created_at->format('Y-m-d H:i:s') }}
                                </td>
                                <td><span class="badge bg-secondary font-monospace small">{{ $record->status }}</span></td>
                                <td><span class="badge {{ $record->health_badge }} font-monospace small">{{ $record->health_status }}</span></td>
                                <td>
                                    <span class="badge bg-dark border border-secondary text-white-50 small">
                                        {{ $record->error_category ?: 'NONE' }}
                                    </span>
                                </td>
                                <td class="text-light small">{{ $record->error_summary ?: 'Operasional stabil dan normal.' }}</td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="5" class="text-center text-white-50 py-4">
                                    <i class="bi bi-check2-circle text-success fs-4 d-block mb-1"></i>
                                    Belum ada catatan anomali kesehatan atau pergantian status runtime.
                                </td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
@endsection
