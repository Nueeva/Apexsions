@extends('admin.layouts.admin')

@section('title', 'Investigasi Insiden ' . $incident->incident_id . ' — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-outline-secondary btn-sm" title="Kembali ke Daftar Insiden">
                    <i class="bi bi-arrow-left"></i>
                </a>
                <h2 class="h3 fw-bold text-white mb-0">
                    <i class="bi bi-file-earmark-medical text-warning me-2"></i>Berkas Investigasi: {{ $incident->incident_id }}
                </h2>
            </div>
            <p class="text-white-50 small mb-0">{{ $incident->title }}</p>
        </div>
        <div class="d-flex gap-2">
            <span class="badge {{ $incident->severity_badge }} fs-6 px-3 py-2 align-self-center">{{ $incident->severity }}</span>
            <span class="badge {{ $incident->status_badge }} fs-6 px-3 py-2 align-self-center">{{ $incident->status }}</span>
        </div>
    </div>

    <!-- Feedback Alerts -->
    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif
    @if($errors->any())
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            @foreach($errors->all() as $error)
                <div>{{ $error }}</div>
            @endforeach
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert"></button>
        </div>
    @endif

    <div class="row g-4 mb-4">
        <!-- Incident Overview & Controls -->
        <div class="col-xl-8">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-transparent border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-info-circle text-info me-2"></i>Ringkasan Insiden & Root Entity
                    </h5>
                    <span class="badge bg-secondary text-uppercase">{{ $incident->type }}</span>
                </div>
                <div class="card-body">
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <div class="text-white-50 small text-uppercase">Entitas Utama (Root Entity)</div>
                            <div class="text-white fw-bold fs-5">{{ $incident->root_entity_name }}</div>
                            <div class="text-muted small">{{ $incident->root_entity_type }} &bull; {{ $incident->root_entity_id }}</div>
                            @if(strtoupper($incident->root_entity_type) === 'PLAYER' && !empty($incident->root_entity_id) && $incident->root_entity_id !== 'SYSTEM')
                                <a href="{{ route('apexsions-bridge.admin.players.show', $incident->root_entity_id) }}" class="btn btn-sm btn-outline-info mt-2">
                                    <i class="bi bi-person-badge me-1"></i>Buka Profil Pemain
                                </a>
                            @endif
                        </div>
                        <div class="col-md-6">
                            <div class="text-white-50 small text-uppercase">Siklus Deteksi</div>
                            <div class="text-white small mt-1"><strong>Terdeteksi:</strong> {{ $incident->detected_at->format('d M Y H:i:s') }} ({{ $incident->detected_at->diffForHumans() }})</div>
                            <div class="text-white small"><strong>Terakhir Muncul:</strong> {{ $incident->last_occurred_at ? $incident->last_occurred_at->format('d M Y H:i:s') : '-' }}</div>
                            <div class="text-white small"><strong>Frekuensi:</strong> <span class="badge bg-secondary">{{ $incident->occurrence_count }} kali</span></div>
                            <div class="text-white small"><strong>Status Selesai:</strong> {{ $incident->resolved_at ? $incident->resolved_at->format('d M Y H:i:s') : 'Belum selesai' }}</div>
                        </div>
                    </div>

                    @if(!empty($incident->correlation_summary))
                        <div class="p-3 bg-secondary bg-opacity-10 border border-secondary border-opacity-25 rounded text-white small mb-3">
                            <strong class="text-warning"><i class="bi bi-lightbulb-fill me-1"></i>Ringkasan Deteksi Rule:</strong>
                            <div class="mt-1 text-white-50">{{ $incident->correlation_summary }}</div>
                        </div>
                    @endif

                    @if(!empty($incident->metadata))
                        <div class="text-white-50 small mb-1">Metadata Pemicu:</div>
                        <pre class="bg-black bg-opacity-50 text-success p-2 rounded small mb-0" style="max-height: 120px; overflow-y: auto;">{{ json_encode($incident->metadata, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) }}</pre>
                    @endif
                </div>
            </div>

            <!-- Investigation Timeline -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-transparent border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-clock-history text-warning me-2"></i>Investigation Timeline (Kronologi Terpadu)
                    </h5>
                    <span class="badge bg-secondary">{{ count($timeline) }} entri</span>
                </div>
                <div class="card-body p-3">
                    <div class="timeline">
                        @forelse($timeline as $item)
                            <div class="d-flex align-items-start gap-3 mb-3 pb-3 border-bottom border-secondary border-opacity-25">
                                <div class="text-center" style="min-width: 80px;">
                                    <div class="text-white small fw-bold">{{ $item['timestamp']->format('H:i:s') }}</div>
                                    <div class="text-muted" style="font-size: 0.7rem;">{{ $item['timestamp']->format('d M') }}</div>
                                </div>
                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <div class="text-white fw-semibold small">
                                            <span class="{{ $item['badge_class'] }} me-1">{{ $item['type'] }}</span>
                                            {{ $item['title'] }}
                                        </div>
                                        <span class="badge bg-dark border border-secondary text-white-50" style="font-size: 0.7rem;">
                                            {{ $item['source'] }}
                                        </span>
                                    </div>
                                    <div class="text-white-50 small">
                                        Aktor: <span class="text-white">{{ $item['actor'] }}</span>
                                        @if(!empty($item['target']))
                                            &bull; Target: <span class="text-white">{{ $item['target'] }}</span>
                                        @endif
                                    </div>
                                    @if(!empty($item['details']))
                                        <div class="mt-1 p-2 bg-black bg-opacity-25 rounded small text-white-50 font-monospace" style="font-size: 0.75rem;">
                                            @if($item['type'] === 'NOTE')
                                                <div class="text-white">{{ $item['details']['content'] }}</div>
                                            @elseif($item['type'] === 'AUDIT')
                                                <div>Perubahan: [{{ $item['details']['old'] ?: 'None' }}] → [{{ $item['details']['new'] }}]</div>
                                                @if(!empty($item['details']['reason']))
                                                    <div class="text-muted">Alasan: {{ $item['details']['reason'] }}</div>
                                                @endif
                                            @else
                                                {{ json_encode($item['details'], JSON_UNESCAPED_SLASHES) }}
                                            @endif
                                        </div>
                                    @endif
                                </div>
                            </div>
                        @empty
                            <div class="text-center text-white-50 py-4">
                                Belum ada timeline kejadian yang terekam untuk insiden ini.
                            </div>
                        @endforelse
                    </div>
                </div>
            </div>

            <!-- Correlated Events Analysis -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-diagram-3 text-info me-2"></i>Event Correlation Analysis
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-white-50 small text-uppercase">
                                    <th>Tingkat Korelasi</th>
                                    <th>Event & Waktu</th>
                                    <th>Entitas / Aktor</th>
                                    <th>Justifikasi Korelasi</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($correlatedEvents as $corr)
                                    @php
                                        $conf = $corr['confidence'];
                                        $badgeStyle = match($conf) {
                                            'STRONG' => 'bg-success text-white',
                                            'MEDIUM' => 'bg-warning text-dark',
                                            'WEAK' => 'bg-secondary text-white',
                                            default => 'bg-info',
                                        };
                                    @endphp
                                    <tr>
                                        <td>
                                            <span class="badge {{ $badgeStyle }}">{{ $conf }}</span>
                                        </td>
                                        <td>
                                            <div class="text-white small fw-bold">{{ $corr['event']->event_type }}</div>
                                            <div class="text-muted" style="font-size: 0.75rem;">{{ $corr['event']->occurred_at->format('d M H:i:s') }}</div>
                                        </td>
                                        <td>
                                            <div class="text-white small">{{ $corr['event']->actor_name ?: $corr['event']->actor_id }}</div>
                                            <div class="text-muted" style="font-size: 0.75rem;">{{ $corr['event']->entity_id }}</div>
                                        </td>
                                        <td class="small">
                                            <span class="{{ $conf === 'WEAK' ? 'text-warning' : 'text-white-50' }}">
                                                {{ $corr['reason'] }}
                                            </span>
                                        </td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center text-white-50 py-3">
                                            Tidak ditemukan event korelasi sekunder di luar kejadian utama.
                                        </td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Sidebar Actions & Staff Notes -->
        <div class="col-xl-4">
            <!-- Lifecycle Status Transition -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-sliders text-warning me-2"></i>Transisi Status Insiden
                    </h5>
                </div>
                <div class="card-body">
                    <form method="POST" action="{{ route('apexsions-bridge.admin.incidents.status', $incident->incident_id) }}">
                        @csrf
                        <div class="mb-3">
                            <label class="form-label text-white-50 small">Pilih Status Baru</label>
                            <select name="status" class="form-select bg-dark border-secondary text-white">
                                <option value="OPEN" {{ $incident->status === 'OPEN' ? 'selected' : '' }}>OPEN (Baru terdeteksi)</option>
                                <option value="INVESTIGATING" {{ $incident->status === 'INVESTIGATING' ? 'selected' : '' }}>INVESTIGATING (Sedang diinvestigasi)</option>
                                <option value="MITIGATED" {{ $incident->status === 'MITIGATED' ? 'selected' : '' }}>MITIGATED (Tindakan awal diterapkan)</option>
                                <option value="RESOLVED" {{ $incident->status === 'RESOLVED' ? 'selected' : '' }}>RESOLVED (Masalah tuntas)</option>
                                <option value="CLOSED" {{ $incident->status === 'CLOSED' ? 'selected' : '' }}>CLOSED (Ditutup)</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label text-white-50 small">Catatan Alasan Transisi (Opsional)</label>
                            <textarea name="reason" rows="2" class="form-control bg-dark border-secondary text-white small" placeholder="Contoh: Sudah diperiksa transaksi sah dan diselesaikan..."></textarea>
                        </div>
                        <button type="submit" class="btn btn-warning btn-sm w-100 fw-bold">
                            <i class="bi bi-arrow-repeat me-1"></i>Perbarui Status Insiden
                        </button>
                    </form>
                </div>
            </div>

            <!-- Staff Assignment -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-transparent border-secondary border-opacity-25">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-person-check text-info me-2"></i>Penugasan Investigator
                    </h5>
                </div>
                <div class="card-body">
                    <form method="POST" action="{{ route('apexsions-bridge.admin.incidents.assign', $incident->incident_id) }}">
                        @csrf
                        <div class="mb-3">
                            <label class="form-label text-white-50 small">Nama Staf / Investigator</label>
                            <input type="text" name="staff_name" class="form-control bg-dark border-secondary text-white" value="{{ $incident->assigned_to }}" placeholder="Nama Staf (Kosongkan untuk unassign)">
                        </div>
                        <div class="mb-3">
                            <label class="form-label text-white-50 small">Catatan Penugasan</label>
                            <input type="text" name="reason" class="form-control bg-dark border-secondary text-white small" placeholder="Alasan penugasan...">
                        </div>
                        <button type="submit" class="btn btn-info btn-sm w-100 fw-bold text-white">
                            <i class="bi bi-save me-1"></i>Simpan Penugasan
                        </button>
                    </form>
                </div>
            </div>

            <!-- Internal Staff Notes -->
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
                <div class="card-header bg-transparent border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white mb-0">
                        <i class="bi bi-journal-text text-success me-2"></i>Catatan Investigasi Internal
                    </h5>
                    <span class="badge bg-secondary">{{ $incident->notes->count() }}</span>
                </div>
                <div class="card-body">
                    <!-- Form Tambah Note -->
                    <form method="POST" action="{{ route('apexsions-bridge.admin.incidents.notes.store', $incident->incident_id) }}" class="mb-4">
                        @csrf
                        <div class="mb-2">
                            <textarea name="content" rows="3" class="form-control bg-dark border-secondary text-white small" placeholder="Tulis catatan investigasi, temuan barang bukti, atau hasil konfirmasi..." required></textarea>
                        </div>
                        <button type="submit" class="btn btn-success btn-sm w-100">
                            <i class="bi bi-plus-circle me-1"></i>Tambah Catatan Investigasi
                        </button>
                    </form>

                    <!-- List Notes -->
                    <div class="notes-list" style="max-height: 400px; overflow-y: auto;">
                        @forelse($incident->notes as $note)
                            <div class="p-3 bg-secondary bg-opacity-10 border border-secondary border-opacity-25 rounded mb-2">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <span class="text-info fw-bold small"><i class="bi bi-person-fill me-1"></i>{{ $note->staff_name }}</span>
                                    <span class="text-muted" style="font-size: 0.75rem;">{{ $note->created_at->diffForHumans() }}</span>
                                </div>
                                <div class="text-white small">{{ $note->content }}</div>
                            </div>
                        @empty
                            <div class="text-center text-white-50 py-3">
                                Belum ada catatan investigasi staf.
                            </div>
                        @endforelse
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
