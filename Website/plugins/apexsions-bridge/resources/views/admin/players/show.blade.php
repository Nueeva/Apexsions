@extends('admin.layouts.admin')

@section('title', 'Player 360: ' . $account->minecraft_username)

@section('content')
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
                <a href="{{ route('player.show', $account->minecraft_uuid) }}" target="_blank" class="btn btn-sm btn-outline-warning">
                    <i class="bi bi-box-arrow-up-right me-1"></i> Lihat Profil Publik
                </a>
            </div>
        </div>
    </div>
</div>

<div class="row g-4">
    <!-- Main Content Tabs -->
    <div class="col-lg-8">
        <div class="card shadow-sm" style="background: rgba(18, 20, 26, 0.95); border: 1px solid rgba(201, 164, 92, 0.22);">
            <div class="card-header bg-transparent border-bottom border-secondary p-0">
                <ul class="nav nav-tabs nav-fill border-0" id="playerTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active py-3 text-uppercase fw-bold" id="overview-tab" data-bs-toggle="tab" data-bs-target="#overview" type="button" role="tab" style="letter-spacing: 1px; font-size: 0.8rem;">
                            <i class="bi bi-person-badge me-1"></i> Overview
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
                        <div class="row g-3">
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Rank In-Game</small>
                                    <h5 class="fw-bold text-warning mb-0">{{ ucfirst($account->rank) }}</h5>
                                    <small class="text-muted">Display: {{ $account->rank_display ?: ucfirst($account->rank) }}</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Progresi Karakter</small>
                                    <h5 class="fw-bold text-white mb-0">Level {{ $account->level }}</h5>
                                    <small class="text-muted">{{ number_format($account->xp) }} / {{ number_format($account->required_xp) }} XP</small>
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
                    </div>

                    <!-- 2. ECONOMY TAB -->
                    <div class="tab-pane fade" id="economy" role="tabpanel">
                        <div class="row g-3">
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Saldo Rupiah (Rp)</small>
                                        <i class="bi bi-cash-stack text-success fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-success mb-0">Rp {{ number_format($account->balance_rupiah, 0, ',', '.') }}</h4>
                                    <small class="text-muted">Mata uang utama ekonomi kerajaan</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Saldo Diamond (💎)</small>
                                        <i class="bi bi-gem text-info fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-info mb-0">{{ number_format($account->balance_diamond, 0, ',', '.') }} 💎</h4>
                                    <small class="text-muted">Mata uang komoditas tambang langka</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <small class="text-muted text-uppercase fw-bold" style="font-size: 0.72rem;">Apex Coins / BP Points</small>
                                        <i class="bi bi-stars text-warning fs-5"></i>
                                    </div>
                                    <h4 class="fw-bold text-warning mb-0">{{ number_format($account->apex_coins) }}</h4>
                                    <small class="text-muted">Poin penukaran toko musim</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Hadiah Harian Web</small>
                                    <div class="text-light">
                                        {{ $account->last_daily_reward_at ? $account->last_daily_reward_at->diffForHumans() : 'Belum pernah klaim' }}
                                    </div>
                                    <small class="text-muted">Klaim reward gratis web profile</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 3. KINGDOM TAB -->
                    <div class="tab-pane fade" id="kingdom" role="tabpanel">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Afiliasi Peradaban Kerajaan</small>
                                    <h4 class="fw-bold mb-1 text-warning">
                                        <i class="bi bi-flag-fill me-2"></i>
                                        {{ $account->kingdom_display ?: ($account->kingdom === 'NONE' ? 'Belum Memilih Kerajaan' : $account->kingdom) }}
                                    </h4>
                                    <small class="text-muted">Kode Regional: <code>{{ $account->kingdom }}</code></small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Gelar Level Karakter</small>
                                    <div class="fw-bold text-white">{{ $account->level_title ?: 'Citizen' }}</div>
                                    <small class="text-muted">Berdasarkan pencapaian level</small>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-1" style="font-size: 0.72rem;">Gelar Aktif (Title Equip)</small>
                                    <div class="fw-bold text-warning">{{ $account->active_title ?: 'Tidak Ada' }}</div>
                                    <small class="text-muted">Ditampilkan di atas kepala & chat</small>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="p-3 rounded bg-dark border border-secondary">
                                    <small class="text-muted text-uppercase fw-bold d-block mb-2" style="font-size: 0.72rem;">Daftar Gelar yang Dimiliki (Unlocked Titles)</small>
                                    <div class="d-flex flex-wrap gap-1">
                                        @forelse($account->unlocked_titles ?? [] as $title)
                                            <span class="badge bg-secondary border border-secondary">{{ $title }}</span>
                                        @empty
                                            <span class="text-muted small">Belum ada gelar khusus yang terbuka.</span>
                                        @endforelse
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 4. MODERATION TAB (ACTIVE & HISTORICAL SANCTIONS) -->
                    <div class="tab-pane fade" id="moderation" role="tabpanel">
                        {{-- Quick Action Header --}}
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h6 class="text-white fw-bold mb-0">
                                <i class="bi bi-shield-shaded me-2 text-warning"></i> Sanksi & Penegakan Kedisiplinan
                            </h6>
                            <a href="{{ route('apexsions-bridge.admin.moderation.index') }}?target={{ urlencode($account->minecraft_username) }}" class="btn btn-warning btn-sm fw-bold">
                                <i class="bi bi-hammer me-1"></i> Terbitkan Sanksi untuk Pemain Ini
                            </a>
                        </div>

                        {{-- Punishments Table --}}
                        <div class="card border-0 mb-4" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3">
                                <span class="small fw-bold text-muted text-uppercase">Riwayat Sanksi ({{ $punishments->count() }})</span>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th>Tipe</th>
                                            <th>Alasan</th>
                                            <th>Staf</th>
                                            <th>Status</th>
                                            <th>Kedaluwarsa</th>
                                            <th>Waktu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($punishments as $punish)
                                            <tr>
                                                <td>
                                                    @php
                                                        $pColor = match($punish->type) {
                                                            'BAN' => 'danger',
                                                            'MUTE' => 'warning',
                                                            'KICK' => 'orange',
                                                            default => 'info'
                                                        };
                                                    @endphp
                                                    <span class="badge bg-{{ $pColor }} bg-opacity-25 text-{{ $pColor }} border border-{{ $pColor }} border-opacity-25">
                                                        {{ $punish->type }}
                                                    </span>
                                                </td>
                                                <td class="text-white">{{ $punish->reason }}</td>
                                                <td><span class="text-info">{{ $punish->staff_name }}</span></td>
                                                <td>
                                                    @if($punish->status === 'ACTIVE')
                                                        <span class="badge bg-danger bg-opacity-20 text-danger">AKTIF</span>
                                                    @elseif($punish->status === 'PARDONED')
                                                        <span class="badge bg-success bg-opacity-20 text-success">DICABUT</span>
                                                    @else
                                                        <span class="badge bg-secondary bg-opacity-20 text-muted">KEDALUWARSA</span>
                                                    @endif
                                                </td>
                                                <td>
                                                    @if($punish->expires_at)
                                                        {{ $punish->expires_at->diffForHumans() }}
                                                    @else
                                                        <span class="text-muted">Permanen</span>
                                                    @endif
                                                </td>
                                                <td class="text-muted">{{ $punish->created_at->diffForHumans() }}</td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="6" class="text-center py-3 text-muted">
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

                        {{-- Reports Created By This Player --}}
                        <div class="card border-0" style="background: #14171d; border-radius: 8px;">
                            <div class="card-header bg-transparent border-bottom border-secondary border-opacity-25 py-2 px-3">
                                <span class="small fw-bold text-muted text-uppercase">Laporan yang Diajukan oleh Pemain Ini ({{ $reportsCreated->count() }})</span>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                    <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                        <tr>
                                            <th>Tiket</th>
                                            <th>Terlapor</th>
                                            <th>Alasan</th>
                                            <th>Status</th>
                                            <th>Waktu</th>
                                            <th class="text-end">Aksi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        @forelse($reportsCreated as $repCreated)
                                            <tr>
                                                <td class="fw-bold font-monospace text-warning">#{{ $repCreated->id }}</td>
                                                <td>{{ $repCreated->reported_name }}</td>
                                                <td class="text-white">{{ $repCreated->reason }}</td>
                                                <td>
                                                    <span class="badge bg-secondary bg-opacity-25 text-light">{{ $repCreated->status }}</span>
                                                </td>
                                                <td class="text-muted">{{ $repCreated->created_at->diffForHumans() }}</td>
                                                <td class="text-end">
                                                    <a href="{{ route('apexsions-bridge.admin.reports.show', $repCreated->id) }}" class="btn btn-xs btn-outline-warning py-0 px-2">
                                                        Lihat Tiket
                                                    </a>
                                                </td>
                                            </tr>
                                        @empty
                                            <tr>
                                                <td colspan="6" class="text-center py-3 text-muted">
                                                    Pemain belum pernah mengajukan laporan.
                                                </td>
                                            </tr>
                                        @endforelse
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- 5. AUDIT TAB -->
                    <div class="tab-pane fade" id="audit" role="tabpanel">
                        <div class="table-responsive">
                            <table class="table table-sm table-hover text-light mb-0" style="font-size: 0.82rem;">
                                <thead class="border-bottom border-secondary text-muted text-uppercase" style="font-size: 0.7rem;">
                                    <tr>
                                        <th>ID</th>
                                        <th>Actor</th>
                                        <th>Action</th>
                                        <th>Reason</th>
                                        <th>Source</th>
                                        <th>Timestamp</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @forelse($auditLogs as $alog)
                                        <tr>
                                            <td class="text-muted">#{{ $alog->id }}</td>
                                            <td class="fw-bold text-warning">{{ $alog->actor_name }}</td>
                                            <td><span class="badge bg-dark border border-secondary">{{ $alog->action }}</span></td>
                                            <td class="text-muted">{{ Str::limit($alog->reason, 35) }}</td>
                                            <td><span class="badge bg-info text-dark">{{ $alog->source }}</span></td>
                                            <td class="text-muted">{{ $alog->created_at->diffForHumans() }}</td>
                                        </tr>
                                    @empty
                                        <tr>
                                            <td colspan="6" class="text-center py-4 text-muted">
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

    <!-- Sidebar Actions -->
    <div class="col-lg-4">
        <div class="card shadow-sm mb-4" style="background: rgba(18, 20, 26, 0.95); border: 1px solid rgba(201, 164, 92, 0.22);">
            <div class="card-header bg-transparent border-bottom border-secondary py-3">
                <h6 class="mb-0 fw-bold text-uppercase text-warning" style="letter-spacing: 1px; font-size: 0.82rem;">
                    <i class="bi bi-shield-lock me-2"></i> Safe Admin Actions
                </h6>
            </div>
            <div class="card-body p-3">
                <p class="text-muted small mb-3" style="font-size: 0.78rem;">
                    Aksi administratif ini dilindungi pengecekan izin, token idempotency, dan dicatat otomatis ke Unified Audit Log.
                </p>

                <!-- Action 1: Send In-Game Tellraw Alert -->
                <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST" class="mb-3">
                    @csrf
                    <input type="hidden" name="action_type" value="DISPATCH_ALERT">
                    <label class="form-label small fw-bold text-light mb-1">Kirim Pesan Resmi Langsung</label>
                    <input type="text" name="message" class="form-control form-control-sm bg-dark text-light border-secondary mb-2" placeholder="Tulis pesan tellraw..." required maxlength="250">
                    <input type="text" name="reason" class="form-control form-control-sm bg-dark text-light border-secondary mb-2" placeholder="Alasan audit (wajib)..." required maxlength="250">
                    <button type="submit" class="btn btn-sm btn-primary w-100" @if(!$isOnline) title="Pemain sedang offline" @endif>
                        <i class="bi bi-chat-dots me-1"></i> Kirim Pesan Resmi
                    </button>
                </form>

                <hr class="border-secondary my-3">

                <!-- Action 2: Trigger Force Sync -->
                <form action="{{ route('apexsions-bridge.admin.players.action', $account->minecraft_uuid) }}" method="POST">
                    @csrf
                    <input type="hidden" name="action_type" value="TRIGGER_SYNC">
                    <input type="hidden" name="reason" value="Manual admin triggered player stat refresh">
                    <button type="submit" class="btn btn-sm btn-outline-warning w-100">
                        <i class="bi bi-arrow-repeat me-1"></i> Jadwalkan Sinkronisasi Ulang
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
@endsection
