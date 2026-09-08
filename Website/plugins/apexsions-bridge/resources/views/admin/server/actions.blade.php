@extends('admin.layouts.admin')

@section('title', 'Server Action History — Apexsions')

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
                    <i class="bi bi-journal-text text-warning me-2"></i>Server Action History
                </h2>
            </div>
            <p class="text-white-50 small mb-0">Rekam jejak audit seluruh tindakan administratif server terproteksi.</p>
        </div>
        <div>
            <a href="{{ route('apexsions-bridge.admin.audit-logs.index') }}" class="btn btn-outline-warning btn-sm shadow-sm">
                <i class="bi bi-shield-check me-1"></i>Unified Audit Logs
            </a>
        </div>
    </div>

    <!-- Actions Table Card -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
            <span class="text-white-50 small">Menampilkan <strong>{{ $actions->total() }}</strong> rekam jejak tindakan server</span>
            <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace">Kategori: SERVER</span>
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
                        <th>SUMBER</th>
                        <th class="text-end pe-3">WAKTU EKSEKUSI</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($actions as $act)
                        <tr>
                            <td class="ps-3 font-monospace small text-warning">
                                {{ $act->action_id ?? $act->metadata['action_id'] ?? 'N/A' }}
                            </td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-50 text-white font-monospace">
                                    {{ $act->action }}
                                </span>
                            </td>
                            <td class="text-white small fw-bold">
                                {{ $act->actor_name }}
                            </td>
                            <td class="text-white-50 small">
                                {{ $act->reason ?? 'Tidak ada alasan' }}
                            </td>
                            <td>
                                <span class="badge {{ $act->status === 'SUCCESS' ? 'bg-success' : ($act->status === 'PENDING' ? 'bg-warning text-dark' : 'bg-danger') }}">
                                    {{ $act->status }}
                                </span>
                            </td>
                            <td>
                                <span class="badge bg-dark border border-secondary border-opacity-50 text-white-50 font-monospace" style="font-size: 0.68rem;">
                                    {{ $act->source }}
                                </span>
                            </td>
                            <td class="text-end pe-3 text-white-50 small">
                                {{ $act->created_at->format('d M Y, H:i:s') }}
                                <div class="text-muted" style="font-size: 0.7rem;">{{ $act->created_at->diffForHumans() }}</div>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" class="text-center py-5 text-white-50">
                                <i class="bi bi-inbox text-muted fs-2 d-block mb-2"></i>
                                Belum ada tindakan administratif server yang tercatat dalam sistem audit.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        @if($actions->hasPages())
            <div class="card-footer bg-black bg-opacity-25 border-top border-secondary border-opacity-25 py-3">
                {{ $actions->links() }}
            </div>
        @endif
    </div>
</div>
@endsection
