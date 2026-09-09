@extends('admin.layouts.admin')

@section('title', 'Unified Audit Logs')

@section('content')
<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h2 class="h3 mb-0 fw-bold" style="font-family: 'Cinzel', serif; letter-spacing: 1px; color: #F1D58A;">
            <i class="bi bi-journal-text me-2 text-warning"></i> UNIFIED AUDIT LOGS
        </h2>
        <small class="text-muted">Pencatatan jejak audit administratif terpadu lintas Web, Game Server, dan API.</small>
    </div>
    <span class="badge bg-dark border border-secondary px-3 py-2 text-warning" style="letter-spacing: 1px;">
        TOTAL RECORD: {{ $logs->total() }}
    </span>
</div>

<!-- Filter Bar -->
<div class="card mb-4">
    <div class="card-body p-3">
        <form method="GET" action="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="row g-2 align-items-center">
            <div class="col-md-3">
                <div class="input-group input-group-sm">
                    <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                    <input type="text" name="search" class="form-control form-control-sm" placeholder="Cari actor, target, aksi, alasan..." value="{{ $search }}">
                </div>
            </div>

            <div class="col-md-2">
                <select name="action" class="form-select form-select-sm">
                    <option value="all">-- Semua Aksi --</option>
                    @foreach($availableActions as $act)
                        <option value="{{ $act }}" @selected($selectedAction === $act)>{{ $act }}</option>
                    @endforeach
                </select>
            </div>

            <div class="col-md-2">
                <select name="source" class="form-select form-select-sm">
                    <option value="all">-- Semua Sumber --</option>
                    @foreach($availableSources as $src)
                        <option value="{{ $src }}" @selected($selectedSource === $src)>{{ $src }}</option>
                    @endforeach
                </select>
            </div>

            <div class="col-md-2">
                <select name="status" class="form-select form-select-sm">
                    <option value="all">-- Semua Status --</option>
                    @foreach($availableStatuses as $st)
                        <option value="{{ $st }}" @selected($selectedStatus === $st)>{{ $st }}</option>
                    @endforeach
                </select>
            </div>

            <div class="col-md-2">
                <input type="date" name="from" class="form-control form-control-sm" title="Dari Tanggal" value="{{ $selectedFrom }}">
            </div>

            <div class="col-md-1 d-flex gap-1">
                <button type="submit" class="btn btn-sm btn-primary w-100" title="Terapkan Filter">
                    <i class="bi bi-funnel"></i>
                </button>
                @if($search || ($selectedAction && $selectedAction !== 'all') || ($selectedSource && $selectedSource !== 'all') || ($selectedStatus && $selectedStatus !== 'all') || $selectedFrom)
                    <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-sm btn-outline-secondary" title="Reset Filter">
                        <i class="bi bi-x-lg"></i>
                    </a>
                @endif
            </div>
        </form>
    </div>
</div>

<!-- Logs Table -->
<div class="card shadow-sm mb-4">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="font-size: 0.88rem;">
                <thead style="background: rgba(201, 164, 92, 0.08); border-bottom: 1px solid rgba(201, 164, 92, 0.2);">
                    <tr class="text-uppercase text-muted" style="font-size: 0.75rem; letter-spacing: 1px;">
                        <th class="ps-3 py-3" style="width: 70px;">ID</th>
                        <th>Actor</th>
                        <th>Action</th>
                        <th>Target</th>
                        <th>Source</th>
                        <th>Status</th>
                        <th>Timestamp</th>
                        <th class="text-end pe-3">Detail</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($logs as $log)
                        <tr style="border-bottom: 1px solid rgba(255, 255, 255, 0.05);">
                            <td class="ps-3 text-muted fw-bold">#{{ $log->id }}</td>
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <div class="avatar-sm rounded p-1 bg-dark border border-secondary text-center" style="width: 28px; height: 28px;">
                                        <i class="bi bi-person text-warning" style="font-size: 0.8rem;"></i>
                                    </div>
                                    <div>
                                        <span class="fw-bold d-block text-white" style="font-size: 0.85rem;">{{ $log->actor_name }}</span>
                                        <small class="text-muted" style="font-size: 0.72rem;">{{ $log->actor_type }}</small>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="badge" style="background: rgba(201, 164, 92, 0.15); color: #F1D58A; border: 1px solid rgba(201, 164, 92, 0.3);">
                                    {{ $log->action }}
                                </span>
                            </td>
                            <td>
                                @if($log->target_name)
                                    <div>
                                        <span class="text-white fw-bold">{{ $log->target_name }}</span>
                                        <small class="d-block text-muted" style="font-size: 0.72rem;">{{ $log->target_type ?? 'ENTITY' }}</small>
                                    </div>
                                @else
                                    <span class="text-muted small">-</span>
                                @endif
                            </td>
                            <td>
                                @php
                                    $sourceBadge = match($log->source) {
                                        'INGAME' => 'bg-success',
                                        'WEB' => 'bg-info text-dark',
                                        'API' => 'bg-primary',
                                        default => 'bg-secondary'
                                    };
                                @endphp
                                <span class="badge {{ $sourceBadge }}" style="font-size: 0.7rem; letter-spacing: 0.5px;">
                                    {{ $log->source }}
                                </span>
                            </td>
                            <td>
                                @php
                                    $statusBadge = match($log->status) {
                                        'SUCCESS' => 'text-success',
                                        'PENDING' => 'text-warning',
                                        'FAILED' => 'text-danger',
                                        default => 'text-muted'
                                    };
                                @endphp
                                <span class="fw-bold {{ $statusBadge }}" style="font-size: 0.8rem;">
                                    <i class="bi bi-circle-fill me-1" style="font-size: 0.5rem;"></i>{{ $log->status }}
                                </span>
                            </td>
                            <td>
                                <span class="text-light" title="{{ $log->created_at->format('d M Y H:i:s') }}">
                                    {{ $log->created_at->diffForHumans() }}
                                </span>
                                <small class="d-block text-muted" style="font-size: 0.72rem;">{{ $log->created_at->format('H:i:s') }}</small>
                            </td>
                            <td class="text-end pe-3">
                                <button type="button" class="btn btn-sm btn-outline-warning btn-view-audit" data-audit-id="{{ $log->id }}" title="Lihat Detail Audit">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="8" class="p-0">
                                <div class="apx-empty-state">
                                    <div class="apx-empty-icon">
                                        <i class="bi bi-journal-x"></i>
                                    </div>
                                    <h5 class="apx-empty-title">Belum Ada Catatan Audit Log</h5>
                                    <p class="apx-empty-desc">
                                        Tidak ditemukan catatan aktivitas atau audit log yang sesuai dengan filter pencarian Anda.
                                    </p>
                                    <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Filter
                                    </a>
                                </div>
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
    @if($logs->hasPages())
        <div class="card-footer py-3">
            {{ $logs->links() }}
        </div>
    @endif
</div>

<!-- Audit Detail Modal -->
<div class="modal fade" id="auditDetailModal" tabindex="-1" aria-labelledby="auditDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content text-light" style="background: #151821; border: 1px solid rgba(201, 164, 92, 0.4); box-shadow: 0 15px 40px rgba(0,0,0,0.9);">
            <div class="modal-header" style="border-bottom: 1px solid rgba(201, 164, 92, 0.25); background: rgba(201, 164, 92, 0.08);">
                <h5 class="modal-title fw-bold" id="auditDetailModalLabel" style="font-family: 'Cinzel', serif; color: #F1D58A;">
                    <i class="bi bi-shield-check me-2 text-warning"></i> AUDIT EVENT INSPECTION
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body p-4" id="auditModalBody">
                <div class="text-center py-4">
                    <div class="spinner-border text-warning" role="status">
                        <span class="visually-hidden">Memuat...</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer" style="border-top: 1px solid rgba(201, 164, 92, 0.15);">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>
@endsection

@push('footer-scripts')
<script>
document.addEventListener('DOMContentLoaded', function() {
    const modalEl = document.getElementById('auditDetailModal');
    const modal = new bootstrap.Modal(modalEl);
    const bodyEl = document.getElementById('auditModalBody');

    document.querySelectorAll('.btn-view-audit').forEach(btn => {
        btn.addEventListener('click', async function() {
            const id = this.getAttribute('data-audit-id');
            modal.show();
            bodyEl.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-warning" role="status"></div></div>';

            try {
                const res = await fetch(`{{ url('admin/audit-logs') }}/${id}`, {
                    headers: { 'Accept': 'application/json' }
                });
                if (!res.ok) throw new Error('Status HTTP ' + res.status);
                const json = await res.json();
                const log = json.data;

                bodyEl.innerHTML = `
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="p-3 rounded bg-dark border border-secondary h-100">
                                <small class="text-uppercase text-muted fw-bold d-block mb-1" style="font-size: 0.7rem; letter-spacing: 1px;">WHO (Actor)</small>
                                <h6 class="mb-0 fw-bold text-warning">${log.actor_name}</h6>
                                <small class="text-muted">Type: ${log.actor_type} ${log.actor_id ? `(ID: ${log.actor_id})` : ''}</small>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="p-3 rounded bg-dark border border-secondary h-100">
                                <small class="text-uppercase text-muted fw-bold d-block mb-1" style="font-size: 0.7rem; letter-spacing: 1px;">WHOM (Target)</small>
                                <h6 class="mb-0 fw-bold text-white">${log.target_name || '-'}</h6>
                                <small class="text-muted">Type: ${log.target_type || 'NONE'} ${log.target_id ? `(UUID: ${log.target_id})` : ''}</small>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="p-3 rounded bg-dark border border-secondary h-100">
                                <small class="text-uppercase text-muted fw-bold d-block mb-1" style="font-size: 0.7rem; letter-spacing: 1px;">WHAT (Action & Result)</small>
                                <span class="badge bg-warning text-dark me-2">${log.action}</span>
                                <span class="badge ${log.status === 'SUCCESS' ? 'bg-success' : (log.status === 'PENDING' ? 'bg-warning' : 'bg-danger')}">${log.status}</span>
                                <small class="d-block mt-2 text-muted">Reason: <span class="text-light">${log.reason || 'Tidak ada alasan'}</span></small>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="p-3 rounded bg-dark border border-secondary h-100">
                                <small class="text-uppercase text-muted fw-bold d-block mb-1" style="font-size: 0.7rem; letter-spacing: 1px;">WHEN & FROM WHERE</small>
                                <div class="text-light">${log.created_at}</div>
                                <small class="text-muted d-block">Platform Source: <strong class="text-info">${log.source}</strong></small>
                            </div>
                        </div>

                        ${log.old_value || log.new_value ? `
                        <div class="col-12">
                            <div class="p-3 rounded bg-dark border border-secondary">
                                <small class="text-uppercase text-muted fw-bold d-block mb-2" style="font-size: 0.7rem; letter-spacing: 1px;">VALUE TRANSITION</small>
                                <div class="row">
                                    <div class="col-6">
                                        <span class="small text-danger fw-bold">OLD VALUE:</span>
                                        <pre class="bg-black p-2 rounded text-muted mt-1 small" style="max-height: 120px; overflow-y: auto;">${log.old_value || '(null)'}</pre>
                                    </div>
                                    <div class="col-6">
                                        <span class="small text-success fw-bold">NEW VALUE:</span>
                                        <pre class="bg-black p-2 rounded text-light mt-1 small" style="max-height: 120px; overflow-y: auto;">${log.new_value || '(null)'}</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                        ` : ''}

                        ${log.metadata ? `
                        <div class="col-12">
                            <div class="p-3 rounded bg-dark border border-secondary">
                                <small class="text-uppercase text-muted fw-bold d-block mb-2" style="font-size: 0.7rem; letter-spacing: 1px;">METADATA PAYLOAD</small>
                                <pre class="bg-black p-2 rounded text-warning mb-0 small" style="max-height: 150px; overflow-y: auto;">${JSON.stringify(log.metadata, null, 2)}</pre>
                            </div>
                        </div>
                        ` : ''}
                    </div>
                `;
            } catch (err) {
                bodyEl.innerHTML = `<div class="alert alert-danger mb-0">Gagal mengambil detail audit: ${err.message}</div>`;
            }
        });
    });
});
</script>
@endpush
