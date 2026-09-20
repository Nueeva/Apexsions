@extends('layouts.app')

@section('title', 'Bounty Oversight')

@section('content')
<div class="container-fluid py-4">
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h1 class="h3 text-white mb-1"><i class="bi bi-crosshair text-gold me-2"></i>Bounty Oversight</h1>
            <p class="text-muted small mb-0">Pantau hadiah kepala aktif dan kirim perintah pembersihan ke server.</p>
        </div>
        <div class="text-end">
            <div class="text-gold fw-bold">Rp {{ number_format($totalPool, 0, ',', '.') }}</div>
            @if($lastSyncedAt)
                <div class="text-muted small">Sinkron terakhir {{ $lastSyncedAt->diffForHumans() }}</div>
            @endif
        </div>
    </div>

    @if(session('success'))
        <div class="alert alert-success">{{ session('success') }}</div>
    @endif

    <div class="card bg-black bg-opacity-25 border border-secondary border-opacity-25">
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Target</th>
                        <th>Total Hadiah</th>
                        <th>Kontributor</th>
                        <th>Disinkron</th>
                        <th class="text-end">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($bounties as $index => $bounty)
                        <tr>
                            <td class="font-monospace text-gold">{{ $index + 1 }}</td>
                            <td>
                                <div class="text-white fw-bold">{{ $bounty->target_name }}</div>
                                <div class="text-muted font-monospace" style="font-size: 0.7rem;">{{ $bounty->target_uuid }}</div>
                            </td>
                            <td class="text-gold fw-bold">Rp {{ number_format($bounty->total_amount, 0, ',', '.') }}</td>
                            <td class="text-white">{{ $bounty->contributor_count }}</td>
                            <td class="text-muted small">{{ $bounty->last_synced_at?->diffForHumans() ?? '—' }}</td>
                            <td class="text-end">
                                <form action="{{ route('apexsions-bridge.admin.bounties.clear') }}" method="POST" class="d-inline"
                                      onsubmit="return confirm('Bersihkan bounty untuk {{ $bounty->target_name }}? Seluruh kontribusi akan dikembalikan ke pemasang.');">
                                    @csrf
                                    <input type="hidden" name="target_name" value="{{ $bounty->target_name }}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                        <i class="bi bi-x-circle me-1"></i>Bersihkan
                                    </button>
                                </form>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center text-muted py-4">Belum ada bounty aktif.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>
@endsection