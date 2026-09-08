@extends('admin.layouts.admin')

@section('title', 'Moderation Center — Apexsions Admin')

@section('content')
<div class="container-fluid px-4 py-3" style="background: #0d0f12; min-height: 100vh; color: #e2e8f0; font-family: 'Outfit', sans-serif;">

    {{-- Breadcrumb & Title --}}
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-2 border-bottom border-secondary border-opacity-25">
        <div>
            <div class="text-warning text-uppercase small fw-bold tracking-wider" style="letter-spacing: 1.5px;">
                <i class="bi bi-shield-shaded me-1"></i> Operations & Security
            </div>
            <h2 class="fw-bold mb-0 text-white" style="letter-spacing: -0.5px;">Moderation Center</h2>
            <p class="text-muted small mb-0">Pusat penegakan kedisiplinan, manajemen sanksi aktif, dan riwayat tindakan moderasi server.</p>
        </div>
        <div class="d-flex gap-2">
            <button type="button" class="btn btn-warning btn-sm fw-bold shadow-sm" data-bs-toggle="modal" data-bs-target="#newPunishmentModal" style="background: linear-gradient(135deg, #d97706, #f59e0b); border: none;">
                <i class="bi bi-hammer me-1"></i> Terbitkan Sanksi Baru
            </button>
            <a href="{{ route('apexsions-bridge.admin.reports.index') }}" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-flag-fill me-1"></i> Reports Center
            </a>
        </div>
    </div>

    {{-- Alert Messages --}}
    @if(session('success'))
        <div class="alert alert-success alert-dismissible fade show bg-success bg-opacity-10 border-success border-opacity-25 text-success mb-4" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> {{ session('success') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif
    @if(session('error'))
        <div class="alert alert-danger alert-dismissible fade show bg-danger bg-opacity-10 border-danger border-opacity-25 text-danger mb-4" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ session('error') }}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    {{-- Statistics Overview --}}
    <div class="row g-3 mb-4">
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #ef4444 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Active Bans</div>
                    <div class="fs-3 fw-bold text-danger">{{ number_format($activeBans) }}</div>
                    <small class="text-muted">Pemain dilarang masuk</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #f59e0b !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Active Mutes</div>
                    <div class="fs-3 fw-bold text-warning">{{ number_format($activeMutes) }}</div>
                    <small class="text-muted">Chat dibungkam</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #38bdf8 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Warnings (30 Hari)</div>
                    <div class="fs-3 fw-bold text-info">{{ number_format($totalWarns30d) }}</div>
                    <small class="text-muted">Teguran resmi staf</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #a855f7 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Open Reports</div>
                    <div class="fs-3 fw-bold text-purple" style="color: #c084fc;">{{ number_format($openReports) }}</div>
                    <small class="text-muted">Laporan perlu ditindak</small>
                </div>
            </div>
        </div>
        <div class="col-md">
            <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; border-left: 4px solid #10b981 !important;">
                <div class="card-body p-3">
                    <div class="text-muted small text-uppercase fw-bold">Aktivitas Staf (24h)</div>
                    <div class="fs-3 fw-bold text-success">{{ number_format($recentActions24h) }}</div>
                    <small class="text-muted">Tindakan baru</small>
                </div>
            </div>
        </div>
    </div>

    {{-- Section: Active Punishments --}}
    <div class="card border-0 shadow-sm mb-4" style="background: #14171d; border-radius: 12px; overflow: hidden;">
        <div class="card-header border-0 py-3 px-4 d-flex justify-content-between align-items-center" style="background: #1a1e26;">
            <div>
                <h5 class="fw-bold text-white mb-0">
                    <i class="bi bi-shield-fill-exclamation me-2 text-danger"></i> Daftar Sanksi Sedang Aktif
                </h5>
                <small class="text-muted">Hukuman ban atau mute yang saat ini masih berlaku pada pemain.</small>
            </div>
            <div class="d-flex gap-2">
                <a href="{{ route('apexsions-bridge.admin.moderation.index', ['type' => 'all']) }}" 
                   class="btn btn-xs btn-sm {{ $selectedType === 'all' ? 'btn-warning' : 'btn-outline-secondary' }}">Semua</a>
                <a href="{{ route('apexsions-bridge.admin.moderation.index', ['type' => 'BAN']) }}" 
                   class="btn btn-xs btn-sm {{ $selectedType === 'BAN' ? 'btn-danger' : 'btn-outline-secondary' }}">Ban</a>
                <a href="{{ route('apexsions-bridge.admin.moderation.index', ['type' => 'MUTE']) }}" 
                   class="btn btn-xs btn-sm {{ $selectedType === 'MUTE' ? 'btn-warning' : 'btn-outline-secondary' }}">Mute</a>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="color: #cbd5e1; border-color: rgba(255,255,255,0.05);">
                <thead style="background: #101317; color: #94a3b8; font-size: 0.75rem; text-transform: uppercase;">
                    <tr>
                        <th class="ps-4">Pemain</th>
                        <th>Tipe</th>
                        <th>Alasan Hukuman</th>
                        <th>Diterbitkan Oleh</th>
                        <th>Kedaluwarsa</th>
                        <th>Waktu Kejadian</th>
                        <th class="text-end pe-4">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($activePunishments as $p)
                        <tr>
                            <td class="ps-4">
                                <div class="d-flex align-items-center gap-2">
                                    <img src="https://crafatar.com/avatars/{{ $p->player_uuid }}?size=24&overlay=true" 
                                         alt="{{ $p->player_name }}" class="rounded" width="24" height="24"
                                         onerror="this.src='https://crafatar.com/avatars/steve?size=24&overlay=true'">
                                    <a href="{{ route('apexsions-bridge.admin.players.show', $p->player_uuid) }}" class="text-decoration-none text-white fw-bold">
                                        {{ $p->player_name }}
                                    </a>
                                </div>
                            </td>
                            <td>
                                @php
                                    $bColor = match($p->type) {
                                        'BAN' => 'danger',
                                        'MUTE' => 'warning',
                                        'KICK' => 'orange',
                                        default => 'info'
                                    };
                                @endphp
                                <span class="badge bg-{{ $bColor }} bg-opacity-20 text-{{ $bColor }} border border-{{ $bColor }} border-opacity-25">
                                    {{ $p->type }}
                                </span>
                            </td>
                            <td style="max-width: 250px;" class="text-truncate text-white" title="{{ $p->reason }}">
                                {{ $p->reason }}
                            </td>
                            <td>
                                <span class="text-info small fw-medium">
                                    <i class="bi bi-person-badge me-1"></i>{{ $p->staff_name }}
                                </span>
                            </td>
                            <td>
                                @if($p->expires_at)
                                    <span class="text-warning small" title="{{ $p->expires_at->toIso8601String() }}">
                                        {{ $p->expires_at->diffForHumans() }}
                                    </span>
                                @else
                                    <span class="badge bg-danger bg-opacity-25 text-danger border border-danger border-opacity-25">PERMANEN</span>
                                @endif
                            </td>
                            <td>
                                <span class="small text-muted">{{ $p->created_at->diffForHumans() }}</span>
                            </td>
                            <td class="text-end pe-4">
                                <form action="{{ route('apexsions-bridge.admin.moderation.pardon', $p->id) }}" method="POST" class="d-inline"
                                      onsubmit="return confirm('Apakah Anda yakin ingin mencabut sanksi {{ $p->type }} untuk {{ $p->player_name }}?');">
                                    @csrf
                                    <button type="submit" class="btn btn-sm btn-outline-success py-1 px-2" title="Cabut Sanksi (Pardon)">
                                        <i class="bi bi-unlock-fill me-1"></i> Cabut Sanksi
                                    </button>
                                </form>
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" class="text-center py-4 text-muted">
                                <i class="bi bi-check2-all fs-2 d-block mb-1 text-success opacity-50"></i>
                                Tidak ada sanksi aktif pada kategori ini saat ini.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        @if($activePunishments->hasPages())
            <div class="card-footer border-0 p-3" style="background: #101317;">
                {{ $activePunishments->links() }}
            </div>
        @endif
    </div>

    {{-- Section: Recent Moderation Log --}}
    <div class="card border-0 shadow-sm" style="background: #14171d; border-radius: 12px; overflow: hidden;">
        <div class="card-header border-0 py-3 px-4" style="background: #1a1e26;">
            <h5 class="fw-bold text-white mb-0">
                <i class="bi bi-clock-history me-2 text-info"></i> Riwayat Aktivitas Moderasi Terbaru
            </h5>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" style="color: #cbd5e1; border-color: rgba(255,255,255,0.05);">
                <thead style="background: #101317; color: #94a3b8; font-size: 0.75rem; text-transform: uppercase;">
                    <tr>
                        <th class="ps-4">ID</th>
                        <th>Pemain Target</th>
                        <th>Tindakan</th>
                        <th>Alasan</th>
                        <th>Staf Pelaksana</th>
                        <th>Status Sanksi</th>
                        <th class="text-end pe-4">Waktu</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($recentPunishments as $rp)
                        <tr>
                            <td class="ps-4 font-monospace small text-muted">#{{ $rp->id }}</td>
                            <td>
                                <a href="{{ route('apexsions-bridge.admin.players.show', $rp->player_uuid) }}" class="text-decoration-none text-light fw-medium">
                                    {{ $rp->player_name }}
                                </a>
                            </td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-25 text-light">{{ $rp->type }}</span>
                            </td>
                            <td style="max-width: 280px;" class="text-truncate text-white" title="{{ $rp->reason }}">
                                {{ $rp->reason }}
                            </td>
                            <td>
                                <span class="small text-muted">{{ $rp->staff_name }}</span>
                            </td>
                            <td>
                                @if($rp->status === 'ACTIVE')
                                    <span class="badge bg-danger bg-opacity-15 text-danger">AKTIF</span>
                                @elseif($rp->status === 'PARDONED')
                                    <span class="badge bg-success bg-opacity-15 text-success" title="{{ $rp->pardon_reason }}">DICABUT</span>
                                @else
                                    <span class="badge bg-secondary bg-opacity-15 text-muted">KEDALUWARSA</span>
                                @endif
                            </td>
                            <td class="text-end pe-4 small text-muted">
                                {{ $rp->created_at->diffForHumans() }}
                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="7" class="text-center py-4 text-muted">Belum ada riwayat moderasi.</td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>

</div>

{{-- Modal: Issue New Punishment --}}
<div class="modal fade" id="newPunishmentModal" tabindex="-1" aria-labelledby="newPunishmentModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg" style="background: #14171d; color: #f8fafc; border-radius: 12px;">
            <div class="modal-header border-bottom border-secondary border-opacity-25 py-3 px-4" style="background: #1a1e26;">
                <h5 class="modal-title fw-bold text-white" id="newPunishmentModalLabel">
                    <i class="bi bi-hammer me-2 text-warning"></i> Terbitkan Sanksi Moderasi
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="{{ route('apexsions-bridge.admin.moderation.store') }}" method="POST">
                @csrf
                <div class="modal-body p-4 d-flex flex-column gap-3">
                    <div>
                        <label class="form-label small text-muted text-uppercase fw-bold">Pemain Target (Username / UUID)</label>
                        <input type="text" name="player" class="form-control border-0" 
                               value="{{ request('target', '') }}"
                               placeholder="Contoh: Steve atau 069a79f4-44e9-4726-a5be-fca90e38aaf5" 
                               style="background: #101317; color: #f8fafc;" required>
                    </div>

                    <div>
                        <label class="form-label small text-muted text-uppercase fw-bold">Tipe Sanksi</label>
                        <select name="type" class="form-select border-0" style="background: #101317; color: #f8fafc;" required id="punishmentTypeSelect">
                            <option value="WARN">WARN — Peringatan Resmi</option>
                            <option value="MUTE">MUTE — Bungkam Akses Chat</option>
                            <option value="KICK">KICK — Keluarkan dari Server</option>
                            <option value="BAN">BAN — Larangan Masuk Server</option>
                        </select>
                    </div>

                    <div id="durationWrapper">
                        <label class="form-label small text-muted text-uppercase fw-bold" id="durationLabel">Durasi (Menit untuk Mute / Jam untuk Ban, kosongkan jika Permanen)</label>
                        <input type="number" name="duration" class="form-control border-0" 
                               placeholder="Contoh: 10 (menit) atau 24 (jam)" 
                               style="background: #101317; color: #f8fafc;" min="1">
                    </div>

                    <div>
                        <label class="form-label small text-muted text-uppercase fw-bold">Alasan Penindakan (Wajib)</label>
                        <textarea name="reason" rows="3" class="form-control border-0" 
                                  placeholder="Jelaskan alasan pelanggaran aturan secara objektif..." 
                                  style="background: #101317; color: #f8fafc;" required></textarea>
                    </div>
                </div>
                <div class="modal-footer border-top border-secondary border-opacity-25 p-3">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger btn-sm fw-bold">
                        <i class="bi bi-shield-fill-check me-1"></i> Eksekusi Sanksi
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
@endsection
