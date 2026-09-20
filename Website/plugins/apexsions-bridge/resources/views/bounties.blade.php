@extends('layouts.app')

@section('title', 'Papan Buronan — Bounties')

@section('content')
<div class="container py-5">
    <div class="text-center mb-5">
        <div class="apx-badge-chiseled apx-badge-chiseled-danger mb-3">
            <i class="bi bi-crosshair"></i> <span data-i18n="bounty_kicker">HADIAH KEPALA REALM</span>
        </div>
        <h1 class="display-5 fw-bold font-cinzel text-gold mb-2" data-i18n="bounty_title">Papan Buronan</h1>
        <p class="text-secondary mx-auto" style="max-width: 640px;" data-i18n="bounty_desc">
            Daftar pengembara yang sedang diburu. Kalahkan target dan klaim seluruh hadiah yang terkumpul.
        </p>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-md-6">
            <div class="p-3 rounded-3 border border-secondary border-opacity-25 h-100" style="background: rgba(0,0,0,0.25);">
                <small class="text-muted d-block text-uppercase" style="font-size: 0.7rem; letter-spacing: 0.08em;" data-i18n="bounty_total_pool">Total Hadiah Beredar</small>
                <strong class="text-gold fs-4">Rp {{ number_format($totalPool, 0, ',', '.') }}</strong>
            </div>
        </div>
        <div class="col-md-6">
            <div class="p-3 rounded-3 border border-secondary border-opacity-25 h-100" style="background: rgba(0,0,0,0.25);">
                <small class="text-muted d-block text-uppercase" style="font-size: 0.7rem; letter-spacing: 0.08em;" data-i18n="bounty_active_targets">Target Aktif</small>
                <strong class="text-white fs-4">{{ $bounties->count() }}</strong>
                @if($lastSyncedAt)
                    <div class="text-muted small mt-1" data-i18n="bounty_synced">Terakhir disinkron {{ $lastSyncedAt->diffForHumans() }}</div>
                @endif
            </div>
        </div>
    </div>

    <div class="card bg-black bg-opacity-25 border border-secondary border-opacity-25">
        <div class="card-body p-0">
            @forelse($bounties as $index => $bounty)
                <div class="d-flex align-items-center gap-3 px-4 py-3 border-bottom border-secondary border-opacity-10">
                    <div class="font-monospace text-gold fw-bold" style="min-width: 2.5rem;">
                        #{{ $index + 1 }}
                    </div>
                    @php
                        $isBedrockTarget = str_starts_with($bounty->target_uuid, '00000000-0000-0000-');
                    @endphp
                    <img src="{{ $isBedrockTarget ? 'https://mc-heads.net/avatar/MHF_Steve/36' : 'https://mc-heads.net/avatar/'.$bounty->target_uuid.'/36' }}"
                         alt="{{ $bounty->target_name }}" class="rounded border border-secondary" width="36" height="36">
                    <div class="flex-grow-1">
                        <div class="text-white fw-bold">{{ $bounty->target_name }}</div>
                        <div class="text-muted small">
                            {{ $bounty->contributor_count }} {{ $bounty->contributor_count === 1 ? 'pemburu' : 'pemburu' }} menaruh hadiah
                        </div>
                    </div>
                    <div class="text-end">
                        <div class="text-gold fw-bold">Rp {{ number_format($bounty->total_amount, 0, ',', '.') }}</div>
                    </div>
                </div>
            @empty
                <div class="text-center text-muted py-5" data-i18n="bounty_empty">
                    <i class="bi bi-crosshair fs-1 d-block mb-2"></i>
                    Belum ada bounty aktif. Pasang hadiah dengan <code>/bounty add &lt;pemain&gt; &lt;jumlah&gt;</code> di dalam game.
                </div>
            @endforelse
        </div>
    </div>

    <div class="text-center mt-4">
        <a href="{{ route('apexsions-bridge.feed') }}" class="btn btn-outline-warning btn-sm">
            <i class="bi bi-broadcast me-1"></i> <span data-i18n="bounty_to_feed">Lihat Kronik Peradaban</span>
        </a>
    </div>
</div>
@endsection