@extends('layouts.app')

@section('title', 'Chronicles of Apexsions — Live Feed')

@section('content')
<div class="container py-5">
    <div class="text-center mb-5">
        <div class="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill bg-danger bg-opacity-10 border border-danger border-opacity-25 text-danger small mb-3">
            <i class="bi bi-broadcast"></i> <span data-i18n="chronicles_kicker">KRONIK PERADABAN</span>
        </div>
        <h1 class="display-5 fw-bold font-cinzel text-gold mb-2" data-i18n="chronicles_title">Chronicles of Apexsions</h1>
        <p class="text-secondary mx-auto" style="max-width: 640px;" data-i18n="chronicles_desc">
            Catatan peristiwa terkini dari seluruh realm: pertempuran, pemburuan bounty, dan momen besar peradaban.
        </p>
    </div>

    <div class="card bg-black bg-opacity-25 border border-secondary border-opacity-25">
        <div class="card-body p-0">
            @forelse($events as $event)
                @php
                    $message = $event->metadata['message'] ?? null;
                    $icon = match ($event->event_type) {
                        'PLAYER_KILL' => '⚔',
                        'BOUNTY_CLAIMED' => '☠',
                        'KINGDOM_WAR' => '🏰',
                        'BOSS_KILL' => '',
                        'WONDER_COMPLETED' => '🏛',
                        default => '✦',
                    };
                @endphp
                <div class="d-flex align-items-start gap-3 px-4 py-3 border-bottom border-secondary border-opacity-10">
                    <div class="fs-4">{{ $icon }}</div>
                    <div class="flex-grow-1">
                        <div class="text-white">{{ $message ?: $event->event_type }}</div>
                        <div class="text-muted small">
                            {{ $event->occurred_at?->diffForHumans() ?? 'baru saja' }}
                        </div>
                    </div>
                    <span class="badge {{ $event->severity_badge }}">{{ $event->event_type }}</span>
                </div>
            @empty
                <div class="text-center text-muted py-5" data-i18n="chronicles_empty">
                    <i class="bi bi-journal-text fs-1 d-block mb-2"></i>
                    Belum ada peristiwa tercatat. Kronik akan terisi seiring aksi pemain di realm.
                </div>
            @endforelse
        </div>
    </div>

    <div class="text-center mt-4">
        <a href="{{ route('apexsions-bridge.leaderboard') }}" class="btn btn-outline-warning btn-sm">
            <i class="bi bi-trophy me-1"></i> <span data-i18n="chronicles_to_leaderboard">Lihat Papan Peringkat</span>
        </a>
    </div>
</div>
@endsection