@extends('admin.layouts.admin')

@section('title', 'Player 360: ' . $account->minecraft_username)

@section('content')
@php
    $staffRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald'];
    $isStaff = in_array(strtolower($account->rank), $staffRanks, true) || ($currentRankMeta['weight'] ?? 0) >= 80;
    $kCode = strtoupper($account->kingdom ?? 'NONE');
@endphp
<div class="mb-4">
    <a href="{{ route('apexsions-bridge.admin.players.index') }}" class="btn btn-sm btn-outline-secondary mb-3">
        <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar Warga
    </a>

    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div class="d-flex align-items-center gap-3">
                <img src="https://mc-heads.net/avatar/{{ urlencode($account->minecraft_username) }}/64" alt="{{ $account->minecraft_username }}" class="rounded shadow border border-warning" width="64" height="64" onerror="this.src='{{ asset('assets/themes/apexsions/img/favicon.ico') }}'">
                <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
                            {{ $account->minecraft_username }}
                        </h3>
                        @if($isOnline)
                            <span class="badge bg-success px-2 py-1">
                                <i class="bi bi-circle-fill me-1" style="font-size: 0.5rem;"></i> ONLINE
                                @if($playerPing !== null)
                                    <small class="ms-1 opacity-75">({{ $playerPing }}ms)</small>
                                @endif
                            </span>
                        @else
                            <span class="badge bg-dark border border-secondary text-muted px-2 py-1">
                                OFFLINE
                            </span>
                        @endif

                        @if($currentRankMeta)
                            <span class="badge px-2 py-1 font-monospace fw-bold" style="background: rgba(0,0,0,0.6); border: 1px solid {{ $currentRankMeta['color'] }}; color: {{ $currentRankMeta['color'] }};">
                                {{ $currentRankMeta['badge'] }}
                            </span>
                        @endif
                    </div>
                    <div class="d-flex flex-wrap align-items-center gap-2 text-muted small">
                        <span>UUID: <code class="text-warning">{{ $account->minecraft_uuid }}</code></span>
                        <span>&bull;</span>
                        <span>Edisi: <strong class="text-white">{{ $account->edition }} ({{ $account->auth_mode }})</strong></span>
                        <span>&bull;</span>
                        <span>Terakhir Terlihat: {{ $account->last_seen_at ? $account->last_seen_at->diffForHumans() : 'Belum pernah' }}</span>
                    </div>
                </div>
            </div>

            <div class="d-flex align-items-center gap-2">
                <a href="{{ route('apexsions-bridge.player.show', $account->minecraft_uuid) }}" target="_blank" class="btn btn-sm btn-outline-warning">
                    <i class="bi bi-box-arrow-up-right me-1"></i> Lihat Profil Publik
                </a>
            </div>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- Main Content Tabs -->
    <div class="col-lg-8">
        <div class="card shadow-sm" >
            <div class="card-header bg-transparent border-bottom border-secondary p-0">
                <ul class="nav nav-tabs nav-fill border-0" id="playerTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active py-3 text-uppercase fw-bold" id="overview-tab" data-bs-toggle="tab" data-bs-target="#overview" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-person-badge me-1"></i> Overview
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link py-3 text-uppercase fw-bold" id="rank-tab" data-bs-toggle="tab" data-bs-target="#rank" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-trophy-fill me-1 text-warning"></i> Rank & Privilese
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link py-3 text-uppercase fw-bold" id="economy-tab" data-bs-toggle="tab" data-bs-target="#economy" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-coin me-1"></i> Ekonomi
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link py-3 text-uppercase fw-bold" id="kingdom-tab" data-bs-toggle="tab" data-bs-target="#kingdom" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-shield-shaded me-1"></i> Kerajaan
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link py-3 text-uppercase fw-bold" id="moderation-tab" data-bs-toggle="tab" data-bs-target="#moderation" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-shield-slash me-1"></i> Moderasi
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link py-3 text-uppercase fw-bold" id="audit-tab" data-bs-toggle="tab" data-bs-target="#audit" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-journal-text me-1"></i> Audit Trail
                        </button>
                    </li>
                </ul>
            </div>

            <div class="card-body p-4">
                <div class="tab-content" id="playerTabContent">
                    <!-- 1. OVERVIEW TAB -->
                    <div class="tab-pane fade show active" id="overview" role="tabpanel">
                        <div class="row g-3 mb-4">
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Rank In-Game</small>
                                    <h5 class="fw-bold text-warning mb-0">{{ $currentRankMeta['display_name'] ?? ucfirst($account->rank) }}</h5>
                                    <small class="text-muted">Tier: {{ $currentRankMeta['tier'] ?? 'Tier I' }} &bull; Weight: {{ $currentRankMeta['weight'] ?? 10 }}</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Progresi Karakter</small>
                                    <h5 class="fw-bold text-white mb-0">Level {{ $account->level }}</h5>
                                    <small class="text-muted">{{ number_format($account->xp) }} / {{ number_format($account->required_xp) }} XP ({{ $account->level_title ?: 'Citizen' }})</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Tautan Akun Web</small>
                                    @if($account->user)
                                        <div class="d-flex align-items-center gap-2">
                                            <i class="bi bi-check-circle-fill text-success"></i>
                                            <span class="text-white fw-bold">{{ $account->user->name }}</span>
                                            <small class="text-muted">({{ $account->user->email }})</small>
                                        </div>
                                        <small class="text-muted d-block mt-1">Terverifikasi: {{ $account->verified_at ? $account->verified_at->format('d M Y H:i') : '-' }}</small>
                                    @else
                                        <span class="badge bg-secondary">Belum Tertaut ke Akun Web</span>
                                    @endif
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">BattlePass Musim</small>
                                    <div class="d-flex align-items-center gap-2">
                                        <span class="badge bg-warning text-dark fw-bold">Tier {{ $account->battlepass_tier }}</span>
                                        <span class="text-light small">{{ $account->battlepass_pass_name ?: 'Citizen Pass' }}</span>
                                    </div>
                                    <small class="text-muted d-block mt-1">Status: {{ $account->battlepass_has_premium ? 'Premium Activated' : 'Free Track' }}</small>
                                </div>
                            </div>
                        </div>

                        <!-- Balances Overview Cards -->
                        <div class="row g-3">
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Saldo Rupiah (IDR)</small>
                                    <h4 class="fw-bold text-success mb-0">Rp {{ number_format($account->balance_rupiah, 0, ',', '.') }}</h4>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Saldo Diamond (💎)</small>
                                    <h4 class="fw-bold text-info mb-0">{{ number_format($account->balance_diamond, 0, ',', '.') }} 💎</h4>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 2. RANK & PRIVILEGE TAB (NEW) -->
                    <div class="tab-pane fade" id="rank" role="tabpanel">
                        <div class="card p-3 mb-4" style="background: rgba(0,0,0,0.4); border: 1px solid {{ $currentRankMeta['color'] ?? '#95a5a6' }};">
                            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
                                <div>
                                    <div class="d-flex align-items-center gap-2 mb-1">
                                        <span class="badge bg-dark border border-secondary text-white">{{ $currentRankMeta['tier'] ?? 'Tier I' }}</span>
                                        <h4 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif;">
                                            {{ $currentRankMeta['display_name'] ?? ucfirst($account->rank) }}
                                        </h4>
                                        <span class="badge px-2 py-1 font-monospace fw-bold" style="background: rgba(0,0,0,0.6); border: 1px solid {{ $currentRankMeta['color'] ?? '#95a5a6' }}; color: {{ $currentRankMeta['color'] ?? '#95a5a6' }};">
                                            {{ $currentRankMeta['badge'] ?? $account->rank }}
                                        </span>
                                    </div>
                                    <p class="text-muted small mb-0">{{ $currentRankMeta['description'] ?? 'Rank default warga baru.' }}</p>
                                    <div class="mt-2 text-muted small">
                                        <span>Bobot (Weight): <strong class="text-warning">{{ $currentRankMeta['weight'] ?? 10 }}</strong></span>
                                        <span class="mx-2">&bull;</span>
                                        <span>Prefix In-Game: <code class="text-light">{{ $currentRankMeta['prefix'] ?? '[Wanderer] ' }}</code></span>
                                    </div>
                                </div>
                                <div class="d-flex gap-2">
                                    <button type="button" class="btn btn-warning fw-bold btn-sm" data-bs-toggle="modal" data-bs-target="#changeRankModal">
                                        <i class="bi bi-pencil-square me-1"></i> Ubah Rank
                                    </button>
                                    @if($account->rank !== 'wanderer')
                                        <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#resetRankModal">
                                            <i class="bi bi-arrow-counterclockwise me-1"></i> Reset ke Wanderer
                                        </button>
                                    @endif
                                </div>
                            </div>
                        </div>

                        <!-- Rank Audit History -->
                        <div class="card border-0" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3 d-flex justify-content-between align-items-center">
                                <span class="small fw-bold text-white text-uppercase">
                                    <i class="bi bi-clock-history me-1 text-warning"></i> Riwayat Perubahan Rank Pemain ({{ $rankHistory->count() }})
                                </span>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th class="ps-3">Waktu</th>
                                            <th>Aksi</th>
                                            <th>Rank Lama</th>
                                            <th>Rank Baru</th>
                                            <th>Administrator</th>
                                            <th>Alasan Audit</th>
                                            <th class="pe-3 text-end">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($rankHistory as $rh)
                                            <tr>
                                                <td class="ps-3 text-muted">{{ $rh->created_at->diffForHumans() }}</td>
                                                <td><span class="badge bg-dark border border-secondary">{{ $rh->action }}</span></td>
                                                <td><span class="badge bg-secondary">{{ ucfirst($rh->old_value) }}</span></td>
                                                <td><span class="badge bg-warning text-dark fw-bold">{{ ucfirst($rh->new_value) }}</span></td>
                                                <td class="text-light fw-semibold">{{ $rh->actor_name }}</td>
                                                <td class="text-muted">{{ Str::limit($rh->reason, 30) }}</td>
                                                <td class="pe-3 text-end">
                                                    <span class="badge bg-{{ $rh->status === 'SUCCESS' ? 'success' : 'warning' }} bg-opacity-25 text-{{ $rh->status === 'SUCCESS' ? 'success' : 'warning' }}">
                                                        {{ $rh->status }}
                                                    </span>
                                                </td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="7" class="text-center py-4 text-muted small">
                                                    Belum ada riwayat perubahan rank tercatat untuk pemain ini.
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- 3. ECONOMY TAB -->
                    <div class="tab-pane fade" id="economy" role="tabpanel">
                        <div class="row g-3 mb-4">
                            <div class="col-sm-4">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Saldo Rupiah (Rp)</small>
                                        <i class="bi bi-cash-stack text-success fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-success mb-0">Rp {{ number_format($account->balance_rupiah, 0, ',', '.') }}</h4>
                                    <small class="text-muted">Mata uang utama ekonomi</small>
                                </div>
                            </div>
                            <div class="col-sm-4">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Saldo Diamond (💎)</small>
                                        <i class="bi bi-gem text-info fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-info mb-0">{{ number_format($account->balance_diamond, 0, ',', '.') }} 💎</h4>
                                    <small class="text-muted">Komoditas tambang langka</small>
                                </div>
                            </div>
                            <div class="col-sm-4">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Apex Coins (AC)</small>
                                        <i class="bi bi-stars text-warning fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-warning mb-0">{{ number_format($account->apex_coins) }}</h4>
                                    <small class="text-muted">Poin BattlePass / Musim</small>
                                </div>
                            </div>
                        </div>

                        <!-- Recent Player Transactions -->
                        <div class="card border-0 mb-4" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3 d-flex justify-content-between align-items-center">
                                <span class="small fw-bold text-white text-uppercase">
                                    <i class="bi bi-receipt me-1 text-warning"></i> Mutasi Transaksi Terakhir ({{ $playerTransactions->count() }})
                                </span>
                                <a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}?q={{ urlencode($account->minecraft_username) }}" class="btn btn-link text-warning text-decoration-none p-0 small" style="font-size: 0.75rem;">
                                    Buka Transaction Explorer <i class="bi bi-arrow-right"></i>
                                </a>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th class="ps-3">Waktu</th>
                                            <th>Tipe</th>
                                            <th>Pihak Lawan</th>
                                            <th>Jumlah</th>
                                            <th>Status</th>
                                            <th class="pe-3 text-end">Aksi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($playerTransactions as $tx)
                                            @php
                                                $isSender = ($tx->sender_name === $account->minecraft_username || $tx->sender_uuid === $account->minecraft_uuid);
                                                $counterParty = $isSender ? ($tx->receiver_name ?? 'SYSTEM') : ($tx->sender_name ?? 'SYSTEM');
                                            @endphp
                                            <tr>
                                                <td class="ps-3 font-monospace text-white-50 small">{{ $tx->created_at->format('d/m H:i') }}</td>
                                                <td>
                                                    <span class="badge bg-secondary bg-opacity-50 text-white font-monospace" style="font-size: 0.7rem;">{{ $tx->type }}</span>
                                                </td>
                                                <td>
                                                    <span class="{{ $isSender ? 'text-danger' : 'text-success' }} small">
                                                        <i class="bi {{ $isSender ? 'bi-arrow-up-right' : 'bi-arrow-down-left' }} me-1"></i>
                                                        {{ $counterParty }}
                                                    </span>
                                                </td>
                                                <td class="fw-bold {{ $isSender ? 'text-danger' : 'text-success' }}">
                                                    {{ $isSender ? '-' : '+' }}{{ $tx->currency === 'diamond' ? number_format($tx->amount, 0) . ' 💎' : 'Rp ' . number_format($tx->amount, 0, ',', '.') }}
                                                </td>
                                                <td>
                                                    <span class="badge bg-{{ $tx->status === 'COMPLETED' ? 'success' : 'warning' }} bg-opacity-25 text-{{ $tx->status === 'COMPLETED' ? 'success' : 'warning' }} font-monospace" style="font-size: 0.68rem;">
                                                        {{ $tx->status }}
                                                    </span>
                                                </td>
                                                <td class="pe-3 text-end">
                                                    <a href="{{ route('apexsions-bridge.admin.economy.transactions.show', $tx->id) }}" class="btn btn-outline-secondary btn-sm py-0 px-2" style="font-size: 0.72rem;">
                                                        Trace
                                                    </a>
                                                </td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="6" class="text-center py-4 text-muted small">
                                                    Belum ada catatan mutasi transaksi untuk pemain ini.
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Player Auction Listings -->
                        <div class="card border-0" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3 d-flex justify-content-between align-items-center">
                                <span class="small fw-bold text-white text-uppercase">
                                    <i class="bi bi-shop me-1 text-warning"></i> Aktivitas Lelang Pemain ({{ $playerAuctions->count() }})
                                </span>
                                <a href="{{ route('apexsions-bridge.admin.economy.auctions.index') }}?q={{ urlencode($account->minecraft_username) }}" class="btn btn-link text-warning text-decoration-none p-0 small" style="font-size: 0.75rem;">
                                    Buka Auction Inspector <i class="bi bi-arrow-right"></i>
                                </a>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th class="ps-3">Lot ID</th>
                                            <th>Item</th>
                                            <th>Harga</th>
                                            <th>Status</th>
                                            <th>Waktu Berakhir</th>
                                            <th class="pe-3 text-end">Aksi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($playerAuctions as $auc)
                                            <tr>
                                                <td class="ps-3 font-monospace text-warning">#{{ $auc->id }}</td>
                                                <td><strong class="text-white">{{ $auc->item_name }}</strong> <small class="text-muted">x{{ $auc->item_amount }}</small></td>
                                                <td><span class="text-success fw-bold">Rp {{ number_format($auc->price, 0, ',', '.') }}</span></td>
                                                <td>
                                                    <span class="badge bg-{{ $auc->status === 'ACTIVE' ? 'success' : 'secondary' }} bg-opacity-25 text-{{ $auc->status === 'ACTIVE' ? 'success' : 'light' }}">
                                                        {{ $auc->status }}
                                                    </span>
                                                </td>
                                                <td class="text-muted">{{ $auc->expires_at ? $auc->expires_at->diffForHumans() : '-' }}</td>
                                                <td class="pe-3 text-end">
                                                    <a href="{{ route('apexsions-bridge.admin.economy.auctions.show', $auc->id) }}" class="btn btn-outline-secondary btn-sm py-0 px-2" style="font-size: 0.72rem;">
                                                        Inspeksi
                                                    </a>
                                                </td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="6" class="text-center py-4 text-muted small">
                                                    Tidak ada listing lelang aktif untuk pemain ini.
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- 4. KINGDOM TAB -->
                    <div class="tab-pane fade" id="kingdom" role="tabpanel">
                        @php
                            $staffRanks = ['ancestor', 'architect', 'overseer', 'warden', 'herald'];
                            $isStaff = in_array(strtolower($account->rank), $staffRanks, true) || ($currentRankMeta['weight'] ?? 0) >= 80;
                            $kCode = strtoupper($account->kingdom ?? 'NONE');
                        @endphp

                        @if($isStaff || $kCode === 'AETHERION')
                            <div class="p-4 rounded mb-4" style="background: linear-gradient(135deg, rgba(0, 242, 254, 0.08) 0%, rgba(10, 16, 32, 0.95) 100%); border: 1px solid rgba(0, 242, 254, 0.4); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.5);">
                                <div class="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
                                    <div class="d-flex align-items-center gap-2">
                                        <span class="fs-4 text-info">✦</span>
                                        <div>
                                            <h5 class="fw-bold text-white mb-0" style="font-family: 'Cinzel', serif;">
                                                The Aetherial Conclave (Dimensi Atas)
                                            </h5>
                                            <small class="text-info opacity-75">Mandat Entitas Transenden & Pengawas Realitas Apexsions</small>
                                        </div>
                                    </div>
                                    <span class="badge bg-info text-dark fs-6 px-3 py-2 fw-bold border border-light">
                                        ✦ Aetherion (The Conclave)
                                    </span>
                                </div>

                                <div class="alert alert-dark border-info border-opacity-50 py-3 small mb-3 text-light" style="background: rgba(0,0,0,0.5);">
                                    <div class="d-flex align-items-start gap-2">
                                        <i class="bi bi-info-circle-fill text-info fs-5 mt-n1"></i>
                                        <div>
                                            <strong>Tatanan Lore Apexsions:</strong> Pemain ini menyandang gelar staf Conclave (<strong>{{ ucfirst($account->rank) }}</strong>). Sesuai kanon resmi Apexsions, entitas dimensi atas tidak terikat pada faksi mortal (<strong>Zenithar, Solterra, Sylvamoor</strong>), tidak dapat menduduki takhta Raja fana, dan kebal terhadap pertikaian Kingdom War.
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex align-items-center gap-2">
                                    <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST" class="d-inline">
                                        @csrf
                                        <input type="hidden" name="action_type" value="SET_KINGDOM">
                                        <input type="hidden" name="kingdom" value="AETHERION">
                                        <input type="hidden" name="reason" value="Pemulihan mandat faksi dimensi atas Aetherion">
                                        <button type="submit" class="btn btn-sm btn-outline-info">
                                            <i class="bi bi-stars me-1"></i> Pulihkan Mandat Aetherion
                                        </button>
                                    </form>

                                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#resetKingdomModal">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Status Faksi
                                    </button>
                                </div>
                            </div>
                        @else
                            <div class="p-4 rounded bg-dark border border-secondary mb-4">
                                <h5 class="fw-bold text-white mb-2">Afiliasi Kerajaan</h5>
                                @if($account->kingdom && strtoupper($account->kingdom) !== 'NONE')
                                    <div class="d-flex align-items-center gap-3 mb-3">
                                        <span class="badge bg-warning text-dark fs-6 px-3 py-2 fw-bold">
                                            ⚜ {{ $account->kingdom_display ?: $account->kingdom }}
                                        </span>
                                        <button type="button" class="btn btn-sm btn-outline-warning" data-bs-toggle="modal" data-bs-target="#setKingdomModal">
                                            Pindah Kerajaan
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#resetKingdomModal">
                                            Reset Kerajaan
                                        </button>
                                    </div>
                                @else
                                    <p class="text-muted small mb-3">Warga ini belum menentukan kesetiaan kerajaan (Belum Memilih).</p>
                                    <button type="button" class="btn btn-sm btn-warning fw-bold mb-3" data-bs-toggle="modal" data-bs-target="#setKingdomModal">
                                        Tetapkan Kerajaan
                                    </button>
                                @endif

                                <div class="pt-3 border-top border-secondary d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="fw-bold text-warning d-block"><i class="bi bi-crown me-1"></i> Takhta Raja (Monarch)</span>
                                        <small class="text-muted">Gelar kehormatan tertinggi & hak veto kerajaan.</small>
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-warning" data-bs-toggle="modal" data-bs-target="#monarchModal">
                                        Kelola Takhta Raja
                                    </button>
                                </div>
                            </div>
                        @endif
                    </div>

                    <!-- 5. MODERATION TAB -->
                    <div class="tab-pane fade" id="moderation" role="tabpanel">
                        {{-- Punishments Table --}}
                        <div class="card border-0 mb-4" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3">
                                <span class="small fw-bold text-muted text-uppercase">Catatan Hukuman & Sanksi ({{ $punishments->count() }})</span>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th>Tipe</th>
                                            <th>Alasan</th>
                                            <th>Staf Penindak</th>
                                            <th>Kedaluwarsa</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($punishments as $pun)
                                            <tr>
                                                <td><span class="badge bg-danger">{{ $pun->type }}</span></td>
                                                <td>{{ $pun->reason }}</td>
                                                <td>{{ $pun->staff_name }}</td>
                                                <td>{{ $pun->expires_at ? $pun->expires_at->diffForHumans() : 'Permanen' }}</td>
                                                <td><span class="badge bg-{{ $pun->is_active ? 'warning text-dark' : 'secondary' }}">{{ $pun->is_active ? 'AKTIF' : 'SELESAI' }}</span></td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="5" class="text-center py-3 text-muted">
                                                    Pemain ini memiliki rekam jejak bersih (tidak ada sanksi tercatat).
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {{-- Reports Against This Player --}}
                        <div class="card border-0 mb-4" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3">
                                <span class="small fw-bold text-muted text-uppercase">Laporan Pelanggaran Terhadap Pemain Ini ({{ $reportsAgainst->count() }})</span>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th>Tiket</th>
                                            <th>Pelapor</th>
                                            <th>Alasan</th>
                                            <th>Status</th>
                                            <th>Waktu</th>
                                            <th class="text-end">Aksi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($reportsAgainst as $rep)
                                            <tr>
                                                <td class="fw-bold font-monospace text-warning">#{{ $rep->id }}</td>
                                                <td>{{ $rep->reporter_name }}</td>
                                                <td class="text-white">{{ $rep->reason }}</td>
                                                <td>
                                                    <span class="badge bg-secondary bg-opacity-25 text-light">{{ $rep->status }}</span>
                                                </td>
                                                <td class="text-muted">{{ $rep->created_at->diffForHumans() }}</td>
                                                <td class="text-end">
                                                    <a href="{{ route('apexsions-bridge.admin.reports.show', $rep->id) }}" class="btn btn-xs btn-outline-warning py-0 px-2">
                                                        Lihat Tiket
                                                    </a>
                                                </td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="6" class="text-center py-3 text-muted">
                                                    Tidak ada laporan pelanggaran terhadap pemain ini.
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- 6. AUDIT TAB -->
                    <div class="tab-pane fade" id="audit" role="tabpanel">
                        <div class="table-responsive">
                            <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                    <tr>
                                        <th>ID</th>
                                        <th>Actor</th>
                                        <th>Action</th>
                                        <th>Old Value</th>
                                        <th>New Value</th>
                                        <th>Reason</th>
                                        <th>Timestamp</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @forelse($auditLogs as $alog)
                                        <tr>
                                            <td class="text-muted">#{{ $alog->id }}</td>
                                            <td class="fw-bold text-warning">{{ $alog->actor_name }}</td>
                                            <td><span class="badge bg-dark border border-secondary">{{ $alog->action }}</span></td>
                                            <td><small class="text-muted">{{ Str::limit($alog->old_value, 20) ?: '-' }}</small></td>
                                            <td><small class="text-light fw-bold">{{ Str::limit($alog->new_value, 20) ?: '-' }}</small></td>
                                            <td class="text-muted">{{ Str::limit($alog->reason, 30) }}</td>
                                            <td class="text-muted">{{ $alog->created_at->diffForHumans() }}</td>
                                        </tr>
                                    @empty
                                        <tr>
                                            <td colspan="7" class="text-center py-4 text-muted">
                                                Belum ada catatan audit log administratif untuk pemain ini.
                                            </td>
                                        </tr>
                                    @endforelse
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Sidebar Actions (Expanded & Grouped) -->
    <div class="col-lg-4">
        <div class="card shadow-sm mb-4" >
            <div class="card-header bg-transparent border-bottom border-secondary py-3">
                <h6 class="mb-0 fw-bold text-uppercase text-warning" style="letter-spacing: 1px; font-size: 0.82rem;">
                    <i class="bi bi-shield-lock me-2"></i> Administrative Control Desk
                </h6>
            </div>
            <div class="card-body p-3">
                <p class="text-muted small mb-3" style="font-size: 0.78rem;">
                    Seluruh tindakan administrasi dieksekusi melalui WebBridge Daemon (<code class="text-warning">Delivery Queue</code>), divalidasi status online/offline, dan diaudit otomatis.
                </p>

                <!-- SECTION 1: RANK MANAGEMENT -->
                <div class="mb-3">
                    <span class="small fw-bold text-muted text-uppercase d-block mb-2" style="font-size: 0.72rem;">👑 Manajemen Rank & Privilese</span>
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-sm btn-outline-warning text-start" data-bs-toggle="modal" data-bs-target="#changeRankModal">
                            <i class="bi bi-award-fill me-2 text-warning"></i> Ubah / Berikan Rank
                        </button>
                        @if($account->rank !== 'wanderer')
                            <button type="button" class="btn btn-sm btn-outline-danger text-start" data-bs-toggle="modal" data-bs-target="#resetRankModal">
                                <i class="bi bi-arrow-counterclockwise me-2 text-danger"></i> Reset Rank ke Wanderer
                            </button>
                        @endif
                    </div>
                </div>

                <hr class="border-secondary my-3">

                <!-- SECTION 2: ECONOMY ADJUSTMENTS -->
                <div class="mb-3">
                    <span class="small fw-bold text-muted text-uppercase d-block mb-2" style="font-size: 0.72rem;">💰 Penyesuaian Saldo Multi-Currency</span>
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-sm btn-outline-success text-start" data-bs-toggle="modal" data-bs-target="#adjustRupiahModal">
                            <i class="bi bi-cash-stack me-2 text-success"></i> Atur Saldo Rupiah (IDR)
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-info text-start" data-bs-toggle="modal" data-bs-target="#adjustDiamondModal">
                            <i class="bi bi-gem me-2 text-info"></i> Atur Saldo Diamond (💎)
                        </button>
                    </div>
                </div>

                <hr class="border-secondary my-3">

                <!-- SECTION 3: PROGRESSION & REALM -->
                <div class="mb-3">
                    <span class="small fw-bold text-muted text-uppercase d-block mb-2" style="font-size: 0.72rem;">⭐ Progresi & Kerajaan</span>
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-sm btn-outline-light text-start" data-bs-toggle="modal" data-bs-target="#setLevelModal">
                            <i class="bi bi-bar-chart-steps me-2 text-warning"></i> Set Level Karakter (1 - 100)
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-light text-start" data-bs-toggle="modal" data-bs-target="#addXpModal">
                            <i class="bi bi-lightning-charge me-2 text-warning"></i> Tambah Progression XP
                        </button>
                        @if($isStaff)
                            <button type="button" class="btn btn-sm btn-outline-info text-start" data-bs-toggle="modal" data-bs-target="#setKingdomModal">
                                <i class="bi bi-stars me-2 text-info"></i> Mandat Faksi Conclave (Aetherion)
                            </button>
                        @else
                            <button type="button" class="btn btn-sm btn-outline-light text-start" data-bs-toggle="modal" data-bs-target="#setKingdomModal">
                                <i class="bi bi-shield-shaded me-2 text-warning"></i> Ganti Afiliasi Kerajaan
                            </button>
                        @endif
                        <button type="button" class="btn btn-sm btn-outline-info text-start @if(!$isOnline) disabled @endif" data-bs-toggle="modal" data-bs-target="#setGameModeModal" @if(!$isOnline) title="Pemain sedang offline" @endif>
                            <i class="bi bi-controller me-2 text-info"></i> Ubah GameMode @if(!$isOnline) <small class="badge bg-secondary ms-1">Offline</small> @endif
                        </button>
                        @if($isStaff)
                            <button type="button" class="btn btn-sm btn-outline-secondary text-start disabled" title="Staf Conclave tidak dapat menduduki takhta mortal">
                                <i class="bi bi-crown me-2 text-muted"></i> Takhta Raja Mortal <small class="badge bg-secondary ms-1">Terkunci</small>
                            </button>
                        @else
                            <button type="button" class="btn btn-sm btn-outline-warning text-start" data-bs-toggle="modal" data-bs-target="#monarchModal">
                                <i class="bi bi-crown me-2 text-warning"></i> Kelola Takhta Raja (Monarch)
                            </button>
                        @endif
                    </div>
                </div>

                <hr class="border-secondary my-3">

                <!-- SECTION 4: BATTLEPASS -->
                <div class="mb-3">
                    <span class="small fw-bold text-muted text-uppercase d-block mb-2" style="font-size: 0.72rem;">🎫 Kontrol BattlePass Musim</span>
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-sm btn-outline-light text-start" data-bs-toggle="modal" data-bs-target="#battlepassPassModal">
                            <i class="bi bi-ticket-perforated me-2 text-info"></i> Berikan Sio / Exsio Pass
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-light text-start" data-bs-toggle="modal" data-bs-target="#battlepassTierModal">
                            <i class="bi bi-star me-2 text-info"></i> Atur Tier BattlePass
                        </button>
                    </div>
                </div>

                <hr class="border-secondary my-3">

                <!-- SECTION 5: MODERATION & UTILITIES -->
                <div class="mb-3">
                    <span class="small fw-bold text-muted text-uppercase d-block mb-2" style="font-size: 0.72rem;">🛡 Moderasi & Aksi In-Game</span>
                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-sm btn-outline-danger text-start @if(!$isOnline) disabled @endif" data-bs-toggle="modal" data-bs-target="#kickPlayerModal" @if(!$isOnline) title="Pemain sedang offline" @endif>
                            <i class="bi bi-box-arrow-right me-2 text-danger"></i> Kick Pemain @if(!$isOnline) <small class="badge bg-secondary ms-1">Offline</small> @endif
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-success text-start @if(!$isOnline) disabled @endif" data-bs-toggle="modal" data-bs-target="#healFeedModal" @if(!$isOnline) title="Pemain sedang offline" @endif>
                            <i class="bi bi-heart-pulse me-2 text-success"></i> Heal & Feed @if(!$isOnline) <small class="badge bg-secondary ms-1">Offline</small> @endif
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-primary text-start @if(!$isOnline) disabled @endif" data-bs-toggle="modal" data-bs-target="#dispatchAlertModal" @if(!$isOnline) title="Pemain sedang offline" @endif>
                            <i class="bi bi-chat-dots me-2 text-primary"></i> Kirim Pesan Tellraw @if(!$isOnline) <small class="badge bg-secondary ms-1">Offline</small> @endif
                        </button>
                    </div>
                </div>

                <hr class="border-secondary my-3">

                <!-- Force Sync Button -->
                <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                    @csrf
                    <input type="hidden" name="action_type" value="TRIGGER_SYNC">
                    <input type="hidden" name="reason" value="Manual admin triggered player stat refresh">
                    <button type="submit" class="btn btn-sm btn-outline-secondary w-100">
                        <i class="bi bi-arrow-repeat me-1"></i> Jadwalkan Sinkronisasi Ulang (Sync)
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- ========================================================================= -->
<!-- MODAL DIALOGS FOR SAFE ACTIONS -->
<!-- ========================================================================= -->

<!-- 1. Modal Change Rank -->
<div class="modal fade" id="changeRankModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning" style="font-family: 'Cinzel', serif;">
                    <i class="bi bi-award-fill me-2"></i> Ubah Rank: {{ $account->minecraft_username }}
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="ASSIGN_RANK">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Pilih Rank Baru <span class="text-danger">*</span></label>
                        <select name="rank" class="form-select " required>
                            @foreach($allRanks as $rkKey => $rkData)
                                <option value="{{ $rkKey }}" @if($account->rank === $rkKey) selected disabled @endif>
                                    [{{ $rkData['tier'] }}] {{ $rkData['display_name'] }} (Weight: {{ $rkData['weight'] }})
                                    @if($account->rank === $rkKey) (Aktif Saat Ini) @endif
                                </option>
                            @endforeach
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit Penugasan <span class="text-danger">*</span></label>
                        <textarea name="reason" class="form-control " rows="3" placeholder="Tulis alasan perubahan rank pemain..." required maxlength="250"></textarea>
                    </div>
                    <div class="alert alert-warning py-2 small mb-0">
                        <i class="bi bi-info-circle me-1"></i> Perintah <code class="text-dark">lp user {{ $account->minecraft_username }} parent set &lt;rank&gt;</code> akan dijadwalkan ke server.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Konfirmasi Ubah Rank</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 2. Modal Reset Rank -->
<div class="modal fade" id="resetRankModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(220, 53, 69, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-danger">Reset Rank ke Wanderer</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="RESET_RANK">
                <div class="modal-body">
                    <p class="text-light small">
                        Apakah Anda yakin ingin mereset rank <strong>{{ $account->minecraft_username }}</strong> kembali ke <strong class="text-warning">Wanderer</strong>?
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Reset <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Contoh: Masa donasi berakhir / sanksi demosi..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger fw-bold">Reset Rank</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 3. Modal Adjust Rupiah -->
<div class="modal fade" id="adjustRupiahModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(40, 167, 69, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-success">
                    <i class="bi bi-cash-stack me-2"></i> Atur Saldo Rupiah: {{ $account->minecraft_username }}
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="ADJUST_BALANCE">
                <input type="hidden" name="currency" value="rupiah">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Jenis Penyesuaian <span class="text-danger">*</span></label>
                        <select name="sub_type" class="form-select " required>
                            <option value="give">Tambah Saldo (+ Give)</option>
                            <option value="take">Kurangi Saldo (- Take)</option>
                            <option value="set">Tetapkan Saldo (= Set)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Nominal Angka (IDR) <span class="text-danger">*</span></label>
                        <input type="number" name="amount" class="form-control " placeholder="Contoh: 100000" min="0" step="1000" required>
                        <small class="text-muted">Saldo Rupiah saat ini: Rp {{ number_format($account->balance_rupiah, 0, ',', '.') }}</small>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan penyesuaian saldo..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-success fw-bold">Eksekusi Saldo</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 4. Modal Adjust Diamond -->
<div class="modal fade" id="adjustDiamondModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(0, 198, 255, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-info">
                    <i class="bi bi-gem me-2"></i> Atur Saldo Diamond: {{ $account->minecraft_username }}
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="ADJUST_BALANCE">
                <input type="hidden" name="currency" value="diamond">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Jenis Penyesuaian <span class="text-danger">*</span></label>
                        <select name="sub_type" class="form-select " required>
                            <option value="give">Tambah Saldo Diamond (+ Give)</option>
                            <option value="take">Kurangi Saldo Diamond (- Take)</option>
                            <option value="set">Tetapkan Saldo Diamond (= Set)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Jumlah Diamond (💎) <span class="text-danger">*</span></label>
                        <input type="number" name="amount" class="form-control " placeholder="Contoh: 50" min="0" step="1" required>
                        <small class="text-muted">Saldo Diamond saat ini: {{ number_format($account->balance_diamond, 0, ',', '.') }} 💎</small>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan penyesuaian diamond..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-info fw-bold">Eksekusi Diamond</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 5. Modal Set Level -->
<div class="modal fade" id="setLevelModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning">Set Level Karakter: {{ $account->minecraft_username }}</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="SET_LEVEL">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Level Baru (1 - 100) <span class="text-danger">*</span></label>
                        <input type="number" name="level" class="form-control " min="1" max="100" value="{{ $account->level }}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan penyesuaian level..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Terapkan Level</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 6. Modal Add XP -->
<div class="modal fade" id="addXpModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning">Tambah Progression XP: {{ $account->minecraft_username }}</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="ADD_XP">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Jumlah XP <span class="text-danger">*</span></label>
                        <input type="number" name="xp_amount" class="form-control " min="1" step="100" value="1000" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan pemberian XP..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Tambah XP</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 7. Modal Set Kingdom -->
<div class="modal fade" id="setKingdomModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning">Tetapkan Afiliasi Kerajaan</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="SET_KINGDOM">
                <div class="modal-body">
                    @if($isStaff)
                        <div class="alert alert-warning py-2 small mb-3">
                            <i class="bi bi-exclamation-triangle-fill me-1"></i>
                            <strong>Peringatan Lore Conclave:</strong> Pemain ini berpangkat staf Conclave (<strong>{{ ucfirst($account->rank) }}</strong>). Entitas dimensi atas tidak dapat berafiliasi dengan kerajaan fana. Memilih faksi fana akan ditolak demi integritas kanon semesta.
                        </div>
                    @endif
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Pilih Kerajaan / Mandat <span class="text-danger">*</span></label>
                        <select name="kingdom" class="form-select " required>
                            @if($isStaff)
                                <option value="AETHERION" @if(strtoupper($account->kingdom) === 'AETHERION' || strtoupper($account->kingdom) === 'NONE') selected @endif>✦ Aetherion (The Aetherial Conclave / Dimensi Atas)</option>
                            @endif
                            <option value="ZENITHAR" @if(strtoupper($account->kingdom) === 'ZENITHAR') selected @endif>Zenithar (Kekayaan & Keemasan)</option>
                            <option value="SOLTERRA" @if(strtoupper($account->kingdom) === 'SOLTERRA') selected @endif>Solterra (Api & Pertambangan)</option>
                            <option value="SYLVAMOOR" @if(strtoupper($account->kingdom) === 'SYLVAMOOR') selected @endif>Sylvamoor (Alam & Hutan)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan perpindahan kerajaan..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Pindah Kerajaan</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 8. Modal Reset Kingdom -->
<div class="modal fade" id="resetKingdomModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(220, 53, 69, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-danger">Reset Kerajaan</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="RESET_KINGDOM">
                <div class="modal-body">
                    <p class="text-light small">
                        Afiliasi kerajaan <strong>{{ $account->minecraft_username }}</strong> akan di-reset menjadi <strong>Belum Memilih</strong>. Pemain dapat memilih kembali via command in-game.
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Reset <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan reset kerajaan..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger fw-bold">Reset Kerajaan</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 9. Modal BattlePass Pass -->
<div class="modal fade" id="battlepassPassModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning">Berikan BattlePass Premium</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="BATTLEPASS_PASS">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Tipe BattlePass <span class="text-danger">*</span></label>
                        <select name="pass_type" class="form-select " required>
                            <option value="sio">SIO PASS (Standar Premium)</option>
                            <option value="exsio">EXSIO PASS (VIP Pass Tier)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Contoh: Pembelian Webstore BattlePass..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Berikan Pass</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 10. Modal BattlePass Tier -->
<div class="modal fade" id="battlepassTierModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(201, 164, 92, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning">Atur Tier BattlePass</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="BATTLEPASS_TIER">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Tier Target (1 - 100) <span class="text-danger">*</span></label>
                        <input type="number" name="tier" class="form-control " min="1" max="100" value="{{ $account->battlepass_tier }}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan pengaturan tier..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning fw-bold">Terapkan Tier</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 11. Modal Kick Player -->
<div class="modal fade" id="kickPlayerModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(220, 53, 69, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-danger">Kick Pemain: {{ $account->minecraft_username }}</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="KICK_PLAYER">
                <div class="modal-body">
                    <p class="text-light small">
                        Pemain akan dikeluarkan dari server secara paksa. Aksi ini hanya dapat berjalan apabila pemain sedang online.
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Kick <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Tulis alasan kick..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger fw-bold">Kick Pemain</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 12. Modal Heal & Feed -->
<div class="modal fade" id="healFeedModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(40, 167, 69, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-success">Heal & Feed: {{ $account->minecraft_username }}</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="HEAL_FEED">
                <div class="modal-body">
                    <p class="text-light small">
                        Memulihkan seluruh HP, mengisi indikator lapar (Food level 20), dan menghapus status efek negatif pemain in-game.
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " value="Bantuan admin / pemulihan HP pemain" required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-success fw-bold">Eksekusi Heal</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 13. Modal Dispatch Alert -->
<div class="modal fade" id="dispatchAlertModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="background: #151820; border: 1px solid rgba(0, 123, 255, 0.4); color: #fff;">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-primary">Kirim Pesan Resmi Langsung</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="DISPATCH_ALERT">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Pesan Tellraw <span class="text-danger">*</span></label>
                        <input type="text" name="message" class="form-control " placeholder="Tulis pesan resmi..." required maxlength="250">
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Audit <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control " placeholder="Alasan pengiriman pesan..." required maxlength="250">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-primary fw-bold">Kirim Pesan</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 14. Modal Set GameMode -->
<div class="modal fade" id="setGameModeModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-info">
                    <i class="bi bi-controller me-2"></i> Ubah GameMode: {{ $account->minecraft_username }}
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                @csrf
                <input type="hidden" name="action_type" value="SET_GAMEMODE">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Pilih GameMode Baru <span class="text-danger">*</span></label>
                        <select name="gamemode" class="form-select" required>
                            <option value="SURVIVAL">SURVIVAL (Mode Normal Warga)</option>
                            <option value="CREATIVE">CREATIVE (Mode Kreatif / Pembangunan)</option>
                            <option value="ADVENTURE">ADVENTURE (Mode Petualangan / Terbatas)</option>
                            <option value="SPECTATOR">SPECTATOR (Mode Penonton / Transparan)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Perubahan <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control" placeholder="Tulis alasan audit perubahan gamemode..." required maxlength="250">
                    </div>
                    <div class="alert alert-info py-2 small mb-0">
                        <i class="bi bi-info-circle me-1"></i> Perintah <code>gamemode &lt;mode&gt; {{ $account->minecraft_username }}</code> akan langsung dijalankan ke pemain online.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-info fw-bold text-dark">Simpan GameMode</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 15. Modal Monarch / King Management -->
<div class="modal fade" id="monarchModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header border-secondary">
                <h5 class="modal-title fw-bold text-warning" style="font-family: 'Cinzel', serif;">
                    <i class="bi bi-crown me-2"></i> Penobatan / Pencabutan Takhta Raja
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                @if($isStaff)
                    <div class="alert alert-danger py-2 small mb-3">
                        <i class="bi bi-shield-x me-1"></i>
                        <strong>Mandat Conclave Transenden:</strong> Pemain ini berpangkat staf Conclave (<strong>{{ ucfirst($account->rank) }}</strong>). Entitas dimensi atas dilarang menduduki takhta Raja fana!
                    </div>
                @else
                    <div class="alert alert-warning py-2 small mb-3">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i> Penobatan Raja memberikan gelar kehormatan realm, hak veto wilayah, dan pengumuman siaran resmi ke seluruh pemain di server.
                    </div>
                @endif

                <!-- Form Penobatan -->
                <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST" class="mb-4 pb-3 border-bottom border-secondary">
                    @csrf
                    <input type="hidden" name="action_type" value="APPOINT_KING">
                    <h6 class="fw-bold text-warning mb-2"><i class="bi bi-award me-1"></i> Nobatkan Menjadi Raja</h6>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Kerajaan Takhta <span class="text-danger">*</span></label>
                        <select name="kingdom" class="form-select" required @if($isStaff) disabled @endif>
                            <option value="ZENITHAR" @if(strtoupper($account->kingdom) === 'ZENITHAR') selected @endif>Zenithar (Gelar: Raja Zenithar)</option>
                            <option value="SOLTERRA" @if(strtoupper($account->kingdom) === 'SOLTERRA') selected @endif>Solterra (Gelar: Raja Solterra)</option>
                            <option value="SYLVAMOOR" @if(strtoupper($account->kingdom) === 'SYLVAMOOR') selected @endif>Sylvamoor (Gelar: Raja Sylvamoor)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Penobatan <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control" placeholder="Alasan administratif penobatan raja..." required maxlength="250" @if($isStaff) disabled @endif>
                    </div>
                    <button type="submit" class="btn btn-warning w-100 fw-bold" @if($isStaff) disabled title="Staf Conclave dilarang menjadi Raja mortal" @endif>
                        <i class="bi bi-crown me-1"></i> Nobatkan Sebagai Raja
                    </button>
                </form>

                <!-- Form Pencabutan -->
                <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                    @csrf
                    <input type="hidden" name="action_type" value="REVOKE_KING">
                    <h6 class="fw-bold text-danger mb-2"><i class="bi bi-x-circle me-1"></i> Cabut Status Raja</h6>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Kerajaan Takhta Yang Dicabut <span class="text-danger">*</span></label>
                        <select name="kingdom" class="form-select" required>
                            <option value="ZENITHAR" @if(strtoupper($account->kingdom) === 'ZENITHAR') selected @endif>Zenithar</option>
                            <option value="SOLTERRA" @if(strtoupper($account->kingdom) === 'SOLTERRA') selected @endif>Solterra</option>
                            <option value="SYLVAMOOR" @if(strtoupper($account->kingdom) === 'SYLVAMOOR') selected @endif>Sylvamoor</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-bold">Alasan Pencabutan <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control" placeholder="Alasan pencabutan gelar raja..." required maxlength="250">
                    </div>
                    <button type="submit" class="btn btn-outline-danger w-100 fw-bold">
                        <i class="bi bi-slash-circle me-1"></i> Cabut Gelar Raja (Kembali ke Rakyat)
                    </button>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>
@endsection
