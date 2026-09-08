@extends('admin.layouts.admin')

@section('title', 'Notifikasi Dossier ' . $notification->notification_id . ' — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div class="d-flex align-items-center gap-2">
            <a href="{{ route('apexsions-bridge.admin.notifications.index') }}" class="btn btn-outline-secondary btn-sm" title="Kembali">
                <i class="bi bi-arrow-left"></i>
            </a>
            <div>
                <h2 class="h4 fw-bold text-white mb-0">
                    <span class="font-monospace text-warning me-2">{{ $notification->notification_id }}</span>
                    @if($notification->severity === 'CRITICAL')
                        <span class="badge bg-danger">CRITICAL</span>
                    @elseif($notification->severity === 'HIGH')
                        <span class="badge bg-warning text-dark fw-bold">HIGH</span>
                    @elseif($notification->severity === 'MEDIUM')
                        <span class="badge bg-info text-dark">MEDIUM</span>
                    @else
                        <span class="badge bg-secondary">{{ $notification->severity }}</span>
                    @endif
                </h2>
                <div class="text-white-50 small mt-1">Dibuat {{ $notification->created_at->diffForHumans() }} ({{ $notification->created_at->format('d M Y H:i:s') }})</div>
            </div>
        </div>
        <div class="d-flex gap-2">
            @if($notification->status === 'UNACKNOWLEDGED')
                <form method="POST" action="{{ route('apexsions-bridge.admin.notifications.acknowledge', $notification->notification_id) }}">
                    @csrf
                    <button type="submit" class="btn btn-success btn-sm">
                        <i class="bi bi-check2-circle me-1"></i>Acknowledge (Tandai Diakui)
                    </button>
                </form>
            @else
                <span class="badge bg-success py-2 px-3 fs-6">
                    <i class="bi bi-check2-all me-1"></i>Diakui oleh {{ $notification->acknowledged_by }} ({{ $notification->acknowledged_at?->diffForHumans() }})
                </span>
            @endif
        </div>
    </div>

    <div class="row g-4">
        <!-- Left: Notification Dossier -->
        <div class="col-lg-7">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-dark border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white mb-0"><i class="bi bi-info-circle me-2 text-info"></i>Rincian Notifikasi</h5>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="text-white-50 small text-uppercase fw-bold d-block">Judul Alert</label>
                        <div class="fs-5 text-white fw-bold">{{ $notification->title }}</div>
                    </div>
                    <div class="mb-3">
                        <label class="text-white-50 small text-uppercase fw-bold d-block">Isi Pesan</label>
                        <div class="p-3 bg-black bg-opacity-40 rounded border border-secondary border-opacity-25 text-white">
                            {{ $notification->message }}
                        </div>
                    </div>
                    <div class="row g-3 mb-3">
                        <div class="col-sm-6">
                            <label class="text-white-50 small text-uppercase fw-bold d-block">Tipe Alert</label>
                            <span class="badge bg-secondary">{{ $notification->type }}</span>
                        </div>
                        <div class="col-sm-6">
                            <label class="text-white-50 small text-uppercase fw-bold d-block">Sumber Kejadian</label>
                            <span class="badge bg-dark border border-secondary text-white">{{ $notification->source }}</span>
                        </div>
                        <div class="col-sm-6">
                            <label class="text-white-50 small text-uppercase fw-bold d-block">Entitas Terkait</label>
                            <div class="text-white font-monospace">{{ $notification->entity_type }}: {{ $notification->entity_id }}</div>
                        </div>
                        <div class="col-sm-6">
                            <label class="text-white-50 small text-uppercase fw-bold d-block">Frekuensi Kejadian Terjaring</label>
                            <span class="badge bg-danger bg-opacity-20 text-danger border border-danger border-opacity-50">
                                {{ $notification->occurrence_count }}x (Terakhir: {{ $notification->last_occurred_at?->diffForHumans() }})
                            </span>
                        </div>
                    </div>

                    @if($notification->incident_id)
                        <div class="p-3 bg-warning bg-opacity-10 border border-warning border-opacity-25 rounded mb-3">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <div class="small fw-bold text-warning text-uppercase">Tertaut Berkas Insiden</div>
                                    <div class="text-white font-monospace">{{ $notification->incident_id }}</div>
                                </div>
                                <a href="{{ route('apexsions-bridge.admin.incidents.show', $notification->incident_id) }}" class="btn btn-warning btn-sm fw-bold">
                                    <i class="bi bi-box-arrow-up-right me-1"></i>Buka Investigasi
                                </a>
                            </div>
                        </div>
                    @endif

                    @if(!empty($notification->metadata))
                        <div class="mt-4">
                            <label class="text-white-50 small text-uppercase fw-bold d-block mb-1">Metadata Terstruktur</label>
                            <pre class="bg-black bg-opacity-40 p-3 rounded border border-secondary border-opacity-25 text-white font-monospace small mb-0">{{ json_encode($notification->metadata, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) }}</pre>
                        </div>
                    @endif
                </div>
            </div>
        </div>

        <!-- Right: Deliveries Status -->
        <div class="col-lg-5">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm mb-4">
                <div class="card-header bg-dark border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white mb-0"><i class="bi bi-send-fill me-2 text-primary"></i>Status Pengiriman Kanal</h5>
                </div>
                <div class="card-body p-0">
                    <ul class="list-group list-group-flush bg-transparent">
                        @forelse($notification->deliveries as $deliv)
                            <li class="list-group-item bg-dark border-secondary border-opacity-25 text-white p-3">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <div>
                                        <div class="fw-bold">
                                            @if($deliv->channel === 'IN_APP')
                                                <i class="bi bi-window-sidebar text-info me-1"></i>In-App Dashboard
                                            @elseif($deliv->channel === 'DISCORD_WEBHOOK')
                                                <i class="bi bi-discord text-primary me-1"></i>Discord Webhook
                                            @else
                                                <i class="bi bi-broadcast me-1"></i>{{ $deliv->channel }}
                                            @endif
                                        </div>
                                        <div class="small font-monospace text-white-50">{{ $deliv->delivery_id }}</div>
                                    </div>
                                    <div>
                                        @if($deliv->status === 'DELIVERED')
                                            <span class="badge bg-success"><i class="bi bi-check2 me-1"></i>DELIVERED</span>
                                        @elseif($deliv->status === 'SUPPRESSED')
                                            <span class="badge bg-secondary"><i class="bi bi-slash-circle me-1"></i>SUPPRESSED</span>
                                        @elseif($deliv->status === 'RETRYING')
                                            <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-arrow-repeat me-1"></i>RETRYING ({{ $deliv->attempt_count }}/{{ $deliv->max_attempts }})</span>
                                        @elseif($deliv->status === 'FAILED')
                                            <span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>FAILED</span>
                                        @else
                                            <span class="badge bg-info">{{ $deliv->status }}</span>
                                        @endif
                                    </div>
                                </div>
                                <div class="small text-white-50">
                                    Percobaan: {{ $deliv->attempt_count }}x | Terakhir: {{ $deliv->last_attempt_at ? $deliv->last_attempt_at->diffForHumans() : '-' }}
                                </div>
                                @if($deliv->error_summary)
                                    <div class="alert alert-danger bg-danger bg-opacity-10 border-danger border-opacity-25 text-danger small py-2 px-3 mt-2 mb-0">
                                        <i class="bi bi-exclamation-triangle me-1"></i>{{ $deliv->error_summary }}
                                    </div>
                                @endif
                            </li>
                        @empty
                            <li class="list-group-item bg-dark border-secondary border-opacity-25 text-white-50 text-center py-4">
                                Tidak ada log pengiriman terpisah.
                            </li>
                        @endforelse
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
