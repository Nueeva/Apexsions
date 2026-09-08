@extends('admin.layouts.admin')

@section('title', 'Historical Metrics — Apexsions')

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
                    <i class="bi bi-graph-up text-warning me-2"></i>Historical Server Metrics
                </h2>
            </div>
            <p class="text-white-50 small mb-0">Riwayat snapshot telemetri performa TPS, MSPT, alokasi memori JVM, dan fluktuasi pemain.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
            <div class="btn-group btn-group-sm">
                @foreach([6 => '6 Jam', 12 => '12 Jam', 24 => '24 Jam', 48 => '48 Jam'] as $val => $label)
                    <a href="{{ route('apexsions-bridge.admin.server.metrics', ['hours' => $val]) }}" class="btn {{ $selectedHours === $val ? 'btn-warning text-dark fw-bold' : 'btn-outline-secondary text-white' }}">
                        {{ $label }}
                    </a>
                @endforeach
            </div>
        </div>
    </div>

    <!-- Summary Snapshot Cards -->
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="text-white-50 small mb-1">TPS TERAKHIR</div>
                <div class="h4 fw-bold text-white mb-0">{{ $currentMetrics['tps']['value'] }}</div>
                <div class="text-muted font-monospace mt-1" style="font-size: 0.7rem;">Target: 20.0</div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="text-white-50 small mb-1">MSPT TERAKHIR</div>
                <div class="h4 fw-bold text-white mb-0">{{ $currentMetrics['mspt']['value'] }}</div>
                <div class="text-muted font-monospace mt-1" style="font-size: 0.7rem;">Batas: &lt; 50.0 ms</div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="text-white-50 small mb-1">ALOKASI RAM JVM</div>
                <div class="h4 fw-bold text-white mb-0">{{ $currentMetrics['memory']['value'] }}</div>
                <div class="text-muted font-monospace mt-1" style="font-size: 0.7rem;">{{ $currentMetrics['memory']['percentage'] }}% terpakai</div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm p-3">
                <div class="text-white-50 small mb-1">PEMAIN TERAKHIR</div>
                <div class="h4 fw-bold text-warning mb-0">{{ $currentMetrics['players']['count'] }}</div>
                <div class="text-muted font-monospace mt-1" style="font-size: 0.7rem;">Dari {{ $currentMetrics['players']['max'] }} Slot</div>
            </div>
        </div>
    </div>

    <!-- Historical Snapshots Table -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
            <h5 class="card-title text-white h6 mb-0 fw-bold">
                <i class="bi bi-clock-history text-warning me-2"></i>Log Snapshot Telemetri ({{ $snapshots->count() }} Entri)
            </h5>
            <span class="badge bg-secondary bg-opacity-25 text-white-50 font-monospace">
                Rentang Waktu: {{ $selectedHours }} Jam Terakhir
            </span>
        </div>
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="text-white-50 small border-bottom border-secondary border-opacity-25">
                        <th class="ps-3">WAKTU RECORD</th>
                        <th>STATUS SERVER</th>
                        <th>TPS</th>
                        <th>MSPT</th>
                        <th>RAM JVM</th>
                        <th>PEMAIN ONLINE</th>
                        <th>CHUNKS</th>
                        <th class="text-end pe-3">ENTITAS</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($snapshots as $snap)
                        <tr>
                            <td class="ps-3 text-white-50 small">
                                {{ $snap->created_at->format('d M Y, H:i:s') }}
                                <span class="text-muted ms-1" style="font-size: 0.7rem;">({{ $snap->created_at->diffForHumans() }})</span>
                            </td>
                            <td>
                                <span class="badge {{ $snap->server_status === 'ONLINE' ? 'bg-success' : ($snap->server_status === 'DEGRADED' ? 'bg-warning text-dark' : 'bg-danger') }} font-monospace" style="font-size: 0.68rem;">
                                    {{ $snap->server_status }}
                                </span>
                            </td>
                            <td class="fw-bold {{ $snap->tps >= 19.5 ? 'text-success' : ($snap->tps >= 17.0 ? 'text-warning' : 'text-danger') }}">
                                {{ number_format($snap->tps, 1) }}
                            </td>
                            <td class="small {{ $snap->mspt < 40.0 ? 'text-white' : ($snap->mspt < 50.0 ? 'text-warning' : 'text-danger') }}">
                                {{ number_format($snap->mspt, 1) }} ms
                            </td>
                            <td class="small text-white-50">
                                {{ number_format($snap->ram_used_mb) }} / {{ number_format($snap->ram_max_mb) }} MB
                            </td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-50 text-white font-monospace">
                                    {{ $snap->online_players }} / {{ $snap->max_players }}
                                </span>
                            </td>
                            <td class="small text-white-50">{{ number_format($snap->loaded_chunks) }}</td>
                            <td class="text-end pe-3 small text-white-50">{{ number_format($snap->entities) }}</td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="8" class="text-center py-5 text-white-50">
                                <i class="bi bi-inbox text-muted fs-2 d-block mb-2"></i>
                                Belum ada riwayat snapshot telemetri dalam {{ $selectedHours }} jam terakhir.
                                <div class="text-muted small mt-1">Snapshot akan dicatat otomatis setiap 60 detik saat heartbeat Minecraft aktif.</div>
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>
@endsection
