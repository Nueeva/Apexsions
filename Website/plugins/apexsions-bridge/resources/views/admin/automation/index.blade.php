@extends('admin.layouts.admin')

@section('title', 'Safe Automation Orchestration — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-0">
                <i class="bi bi-gear-wide-connected text-info me-2"></i>Safe Automation Orchestration
            </h2>
            <p class="text-white-50 small mb-0">Manajemen kebijakan otomasi aman, approval gate terpadu, dan riwayat eksekusi terisolasi tanpa celah aksi destruktif.</p>
        </div>
        <div class="d-flex gap-2">
            <a href="{{ route('apexsions-bridge.admin.notifications.index') }}" class="btn btn-outline-warning btn-sm">
                <i class="bi bi-bell-fill me-1"></i>Notifications Desk
            </a>
            <a href="{{ route('apexsions-bridge.admin.incidents.index') }}" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-exclamation-octagon-fill me-1"></i>Incident Registry
            </a>
        </div>
    </div>

    <!-- Alert Banner on Approval Gate -->
    @if($pendingApprovals->count() > 0)
        <div class="alert alert-warning bg-warning bg-opacity-10 border-warning border-opacity-50 text-warning d-flex align-items-center gap-3 mb-4 p-3 rounded">
            <i class="bi bi-shield-lock-fill fs-2"></i>
            <div>
                <div class="fw-bold fs-6">Terdapat {{ $pendingApprovals->count() }} Usulan Aksi Otomasi Menunggu Persetujuan Manusia</div>
                <div class="small text-white-50">Sesuai kebijakan keamanan Safe Automation, tindakan sensitif tidak akan dieksekusi sebelum disetujui staf administrator.</div>
            </div>
        </div>

        <!-- Pending Approval Queue -->
        <div class="card bg-dark border-warning border-opacity-50 shadow-sm mb-4">
            <div class="card-header bg-dark border-warning border-opacity-25 py-3">
                <h5 class="card-title text-warning mb-0"><i class="bi bi-hourglass-split me-2"></i>Antrean Persetujuan (Approval Gate)</h5>
            </div>
            <div class="table-responsive">
                <table class="table table-dark table-hover align-middle mb-0">
                    <thead class="table-dark text-white-50 small text-uppercase">
                        <tr>
                            <th>Execution ID</th>
                            <th>Kebijakan</th>
                            <th>Aksi Yang Diusulkan</th>
                            <th>Waktu Usulan</th>
                            <th class="text-end">Tindakan Otorisasi</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($pendingApprovals as $exec)
                            <tr>
                                <td class="font-monospace text-white">{{ $exec->execution_id }}</td>
                                <td>
                                    <div class="fw-bold text-white">{{ $exec->policy->name ?? $exec->policy_id }}</div>
                                    <div class="small text-white-50">{{ $exec->policy_id }}</div>
                                </td>
                                <td>
                                    <span class="badge bg-info text-dark">{{ $exec->action_type }}</span>
                                </td>
                                <td>
                                    <div class="small text-white">{{ $exec->created_at->diffForHumans() }}</div>
                                    <div class="text-white-50" style="font-size: 0.75rem;">{{ $exec->created_at->format('d/m/Y H:i') }}</div>
                                </td>
                                <td class="text-end">
                                    <div class="d-inline-flex gap-2">
                                        <form method="POST" action="{{ route('apexsions-bridge.admin.automation.executions.approve', $exec->execution_id) }}">
                                            @csrf
                                            <button type="submit" class="btn btn-success btn-sm fw-bold">
                                                <i class="bi bi-check2-circle me-1"></i>Setujui & Jalankan
                                            </button>
                                        </form>

                                        <!-- Reject Form trigger modal -->
                                        <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#rejectModal{{ $exec->id }}">
                                            <i class="bi bi-x-circle me-1"></i>Tolak
                                        </button>

                                        <div class="modal fade" id="rejectModal{{ $exec->id }}" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content bg-dark text-white border-secondary">
                                                    <form method="POST" action="{{ route('apexsions-bridge.admin.automation.executions.reject', $exec->execution_id) }}">
                                                        @csrf
                                                        <div class="modal-header border-secondary">
                                                            <h5 class="modal-title">Tolak Usulan Aksi {{ $exec->execution_id }}</h5>
                                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <div class="modal-body text-start">
                                                            <label class="form-label small text-white-50">Alasan Penolakan (Wajib)</label>
                                                            <input type="text" name="reason" class="form-control bg-dark border-secondary text-white" placeholder="Contoh: Kondisi server sudah stabil manual" required>
                                                        </div>
                                                        <div class="modal-footer border-secondary">
                                                            <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                                                            <button type="submit" class="btn btn-danger btn-sm">Konfirmasi Tolak</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>
    @endif

    <!-- Automation Policies Matrix -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
        <div class="card-header bg-dark border-secondary border-opacity-25 py-3">
            <h5 class="card-title text-white mb-0"><i class="bi bi-sliders me-2 text-primary"></i>Daftar Kebijakan Otomasi Aktif</h5>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover align-middle mb-0">
                <thead class="table-dark text-white-50 small text-uppercase">
                    <tr>
                        <th>Kebijakan</th>
                        <th>Pemicu (Trigger)</th>
                        <th>Aksi Whitelist</th>
                        <th>Keparahan</th>
                        <th>Cooldown</th>
                        <th>Approval Gate</th>
                        <th>Status</th>
                        <th class="text-end">Kontrol</th>
                    </tr>
                </thead>
                <tbody>
                    @foreach($policies as $pol)
                        <tr>
                            <td>
                                <div class="fw-bold text-white">{{ $pol->name }}</div>
                                <div class="small font-monospace text-white-50">{{ $pol->policy_id }}</div>
                            </td>
                            <td>
                                <span class="badge bg-dark border border-secondary">{{ $pol->trigger_type }}</span>
                            </td>
                            <td>
                                <span class="badge bg-primary bg-opacity-20 text-primary border border-primary border-opacity-25">
                                    {{ $pol->action_type }}
                                </span>
                            </td>
                            <td>
                                @if($pol->severity === 'CRITICAL')
                                    <span class="badge bg-danger">CRITICAL</span>
                                @elseif($pol->severity === 'HIGH')
                                    <span class="badge bg-warning text-dark fw-bold">HIGH</span>
                                @else
                                    <span class="badge bg-info text-dark">{{ $pol->severity }}</span>
                                @endif
                            </td>
                            <td class="text-white-50 small">{{ $pol->cooldown_minutes }} m</td>
                            <td>
                                @if($pol->approval_required)
                                    <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-lock-fill me-1"></i>Wajib Staf</span>
                                @else
                                    <span class="badge bg-success bg-opacity-20 text-success border border-success border-opacity-25">Otomatis Langsung</span>
                                @endif
                            </td>
                            <td>
                                @if($pol->is_enabled)
                                    <span class="badge bg-success">AKTIF</span>
                                @else
                                    <span class="badge bg-secondary">NONAKTIF</span>
                                @endif
                            </td>
                            <td class="text-end">
                                <form method="POST" action="{{ route('apexsions-bridge.admin.automation.toggle', $pol->policy_id) }}">
                                    @csrf
                                    @if($pol->is_enabled)
                                        <button type="submit" class="btn btn-outline-warning btn-sm" title="Nonaktifkan Kebijakan">
                                            <i class="bi bi-pause-fill"></i>
                                        </button>
                                    @else
                                        <button type="submit" class="btn btn-outline-success btn-sm" title="Aktifkan Kebijakan">
                                            <i class="bi bi-play-fill"></i>
                                        </button>
                                    @endif
                                </form>
                            </td>
                        </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
    </div>

    <!-- Recent Executions Ledger -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-dark border-secondary border-opacity-25 py-3">
            <h5 class="card-title text-white mb-0"><i class="bi bi-journal-check me-2 text-info"></i>Riwayat Eksekusi Otomasi Terkini</h5>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover align-middle mb-0">
                <thead class="table-dark text-white-50 small text-uppercase">
                    <tr>
                        <th>Execution ID</th>
                        <th>Kebijakan</th>
                        <th>Aksi</th>
                        <th>Status</th>
                        <th>Ringkasan Hasil</th>
                        <th>Waktu</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($recentExecutions as $exec)
                        <tr>
                            <td class="font-monospace text-white small">{{ $exec->execution_id }}</td>
                            <td>
                                <div class="text-white small">{{ $exec->policy->name ?? $exec->policy_id }}</div>
                            </td>
                            <td>
                                <span class="badge bg-dark border border-secondary">{{ $exec->action_type }}</span>
                            </td>
                            <td>
                                @if($exec->status === 'EXECUTED')
                                    <span class="badge bg-success"><i class="bi bi-check2 me-1"></i>EXECUTED</span>
                                @elseif($exec->status === 'PENDING_APPROVAL')
                                    <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-hourglass-split me-1"></i>PENDING</span>
                                @elseif($exec->status === 'REJECTED')
                                    <span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>REJECTED</span>
                                @elseif($exec->status === 'SUPPRESSED')
                                    <span class="badge bg-secondary"><i class="bi bi-slash-circle me-1"></i>SUPPRESSED</span>
                                @else
                                    <span class="badge bg-info">{{ $exec->status }}</span>
                                @endif
                            </td>
                            <td>
                                <div class="small text-white-50 text-truncate" style="max-width: 320px;">
                                    {{ $exec->result_summary ?: ($exec->rejection_reason ?: '-') }}
                                </div>
                            </td>
                            <td class="text-white-50 small">
                                {{ $exec->created_at->diffForHumans() }}
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center py-4 text-white-50">
                                Belum ada riwayat eksekusi otomasi yang tercatat.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>
@endsection
