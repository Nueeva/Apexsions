@extends('admin.layouts.admin')

@section('title', 'BattlePass Season Desk — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-ticket-perforated-fill text-warning me-2"></i>BattlePass Season & Pass Operations
            </h2>
            <p class="text-white-50 small mb-0">
                Pusat administrasi 200 Level BattlePass, rotasi misi season, dan pembagian tiket Sio & Exsio Pass.
            </p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            <button type="button" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="modal" data-bs-target="#givePassModal">
                <i class="bi bi-gift-fill me-1"></i>Beri Pass Pemain
            </button>
            <button type="button" class="btn btn-outline-info btn-sm shadow-sm" data-bs-toggle="modal" data-bs-target="#adjustBpModal">
                <i class="bi bi-sliders me-1"></i>Atur Level / XP
            </button>
            <form action="{{ route('apexsions-bridge.admin.battlepass.reload') }}" method="POST" class="d-inline">
                @csrf
                <button type="submit" class="btn btn-outline-secondary btn-sm shadow-sm" title="Reload konfigurasi BattlePass di game server">
                    <i class="bi bi-arrow-repeat me-1"></i>Reload BP Engine
                </button>
            </form>
        </div>
    </div>

    @if(session('success'))
        <div class="alert alert-success bg-success bg-opacity-25 border-success text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2 text-success"></i>{{ session('success') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    @if(session('error'))
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2 text-danger"></i>{{ session('error') }}
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <!-- Season Banner -->
    <div class="card border-warning border-opacity-50 bg-warning bg-opacity-10 mb-4 shadow-sm">
        <div class="card-body p-4 d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
                <span class="badge bg-warning text-dark text-uppercase px-2 py-1 mb-2 fw-bold">ACTIVE SEASON</span>
                <h3 class="h4 fw-bold text-white mb-1">{{ $season['name'] }}</h3>
                <p class="text-white-50 small mb-0">
                    Periode: <span class="text-white">{{ $season['start_date'] }}</span> s/d <span class="text-white">{{ $season['end_date'] }}</span> | 
                    Progres: <span class="text-warning fw-bold">Week {{ $season['current_week'] }}</span> (Bulan {{ $season['current_month'] }}) | 
                    Maksimum Level: <span class="text-info fw-bold">{{ $season['max_level'] }}</span>
                </p>
            </div>
            <div class="text-end">
                <div class="h2 fw-bold text-warning mb-0">{{ $season['days_remaining'] }}</div>
                <small class="text-white-50 text-uppercase fw-semibold">Hari Tersisa</small>
            </div>
        </div>
    </div>

    <!-- Pass Tier Metrics -->
    <div class="row g-3 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Peserta Aktif</span>
                        <i class="bi bi-people-fill text-info fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-info mb-1">{{ number_format($activeParticipants) }} Pemain</div>
                    <small class="text-white-50">Dari total {{ number_format($totalPlayers) }} akun terdaftar</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Citizen Pass (Free)</span>
                        <i class="bi bi-book-half text-secondary fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-white mb-1">{{ number_format($freePassCount) }} Warga</div>
                    <small class="text-white-50">Jalur hadiah dasar gratis</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-warning border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Sio Pass (Premium)</span>
                        <i class="bi bi-award-fill text-warning fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-warning mb-1">{{ number_format($sioPassCount) }} Pemilik</div>
                    <small class="text-white-50">Membuka reward ganda & bonus XP</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-danger border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Exsio Pass (Ultimate)</span>
                        <i class="bi bi-gem text-danger fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-danger mb-1">{{ number_format($exsioPassCount) }} Pemilik</div>
                    <small class="text-white-50">Membuka seluruh hadiah eksklusif</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Leaderboard & Recent Logs -->
    <div class="row g-4">
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
                    <h5 class="card-title h6 fw-bold text-white mb-0">
                        <i class="bi bi-trophy-fill text-warning me-2"></i>Puncak Progresi Pemain (Top 10 BP)
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead class="table-secondary bg-opacity-10 text-uppercase small">
                                <tr>
                                    <th class="ps-3">Pemain</th>
                                    <th>Pass Aktif</th>
                                    <th>Level</th>
                                    <th class="pe-3 text-end">XP</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($topPlayers as $player)
                                    <tr>
                                        <td class="ps-3 fw-semibold text-white">
                                            <a href="{{ route('apexsions-bridge.admin.players.show', $player->minecraft_uuid) }}" class="text-decoration-none text-white">
                                                {{ $player->minecraft_username }}
                                            </a>
                                        </td>
                                        <td>
                                            @if(str_contains(strtoupper($player->battlepass_pass_name ?? ''), 'EXSIO'))
                                                <span class="badge bg-danger">EXSIO</span>
                                            @elseif(str_contains(strtoupper($player->battlepass_pass_name ?? ''), 'SIO'))
                                                <span class="badge bg-warning text-dark">SIO</span>
                                            @else
                                                <span class="badge bg-secondary">Citizen</span>
                                            @endif
                                        </td>
                                        <td><span class="fw-bold text-warning">Lv. {{ $player->battlepass_tier ?? 0 }}</span></td>
                                        <td class="pe-3 text-end text-white-50 small">{{ number_format($player->battlepass_xp ?? 0) }} XP</td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-white-50">Belum ada progresi BattlePass tercatat.</td>
                                    </tr>
                                @endforelse
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
                    <h5 class="card-title h6 fw-bold text-white mb-0">
                        <i class="bi bi-journal-text text-warning me-2"></i>Log Aktivitas BattlePass Terkini
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead class="table-secondary bg-opacity-10 text-uppercase small">
                                <tr>
                                    <th class="ps-3">Waktu</th>
                                    <th>Aksi</th>
                                    <th>Pemain</th>
                                    <th class="pe-3 text-end">Rincian</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($recentLogs as $log)
                                    <tr>
                                        <td class="ps-3 small text-white-50">{{ $log->created_at->format('d M H:i') }}</td>
                                        <td><span class="badge bg-warning text-dark">{{ $log->action }}</span></td>
                                        <td class="fw-semibold text-white">{{ $log->target_name }}</td>
                                        <td class="pe-3 text-end small text-white-50">{{ $log->new_value ?? $log->reason }}</td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-white-50">Belum ada log BattlePass.</td>
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

<!-- Modal Give Pass -->
<div class="modal fade" id="givePassModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-warning text-white">
            <form action="{{ route('apexsions-bridge.admin.battlepass.give-pass') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-warning"><i class="bi bi-gift-fill me-2"></i>Beri Tiket Pass Pemain</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Username Pemain</label>
                        <input type="text" name="player_username" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Contoh: NuevaID" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Tier Pass</label>
                        <select name="pass_type" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="sio">Sio Pass (Tingkat Premium)</option>
                            <option value="exsio">Exsio Pass (Tingkat Tertinggi / Ultimate)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Alasan Pemberian</label>
                        <input type="text" name="reason" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Reward Event / Pembelian Toko">
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Kirim Pass</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Adjust BP -->
<div class="modal fade" id="adjustBpModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary text-white">
            <form action="{{ route('apexsions-bridge.admin.battlepass.adjust-progress') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-white"><i class="bi bi-sliders text-info me-2"></i>Atur Level / XP BattlePass</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Username Pemain</label>
                        <input type="text" name="player_username" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Aksi</label>
                        <select name="action" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="setlevel">Atur Level Langsung (/abp setlevel)</option>
                            <option value="addxp">Tambah XP Progresi (/abp addxp)</option>
                            <option value="reset">Reset Total Progresi (/abp reset)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Nilai (Level 1-200 atau Jumlah XP)</label>
                        <input type="number" name="value" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" value="10" min="0" required>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-info btn-sm fw-bold">Eksekusi Perubahan</button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
