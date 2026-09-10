@extends('admin.layouts.admin')

@section('title', 'Vote Management')

@section('content')
<div class="mb-4">
    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="badge bg-warning text-dark fw-bold px-2 py-1">VOTING INTEGRATION</span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1.5px;">
                        🗳 VOTE MANAGEMENT &amp; AUTO-REWARD LEDGER
                    </h3>
                </div>
                <p class="text-muted small mb-0" style="max-width: 750px;">
                    Pusat pemantauan suara dukungan server Apexsions di kancah dunia. Suara diproses otomatis melalui background poller &amp; webhook, memberikan imbalan <strong class="text-warning">3x Vote Keys</strong> dan <strong class="text-success">Rp 1.000</strong> secara atomic melalui Minecraft Deliveries Bridge.
                </p>
            </div>
            <div class="d-flex align-items-center gap-2">
                <form action="{{ route('apexsions-bridge.admin.votes.poll') }}" method="POST" class="d-inline">
                    @csrf
                    <button type="submit" class="btn btn-warning fw-bold px-3 shadow-sm" title="Jalankan polling otomatis untuk memeriksa suara baru di Minecraft-MP/TopG">
                        <i class="bi bi-arrow-repeat me-1"></i> Sinkronisasi Suara Sekarang
                    </button>
                </form>
                <a href="{{ route('vote') }}" target="_blank" class="btn btn-outline-warning fw-bold px-3 shadow-sm">
                    <i class="bi bi-box-arrow-up-right me-1"></i> Buka Bilik Suara Web
                </a>
            </div>
        </div>
    </div>
</div>

<!-- KPI Cards -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Total Suara Sah</span>
                <i class="bi bi-check2-circle text-warning fs-5"></i>
            </div>
            <h3 class="fw-bold mb-0">{{ number_format($totalVotes) }}</h3>
            <small class="text-muted">Sepanjang masa</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Suara Hari Ini</span>
                <i class="bi bi-calendar-check text-info fs-5"></i>
            </div>
            <h3 class="fw-bold text-info mb-0">{{ number_format($votesToday) }}</h3>
            <small class="text-muted">24 Jam terakhir</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Suara Minggu Ini</span>
                <i class="bi bi-calendar-range text-primary fs-5"></i>
            </div>
            <h3 class="fw-bold text-primary mb-0">{{ number_format($votesThisWeek) }}</h3>
            <small class="text-muted">7 Hari terakhir</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Suara Bulan Ini</span>
                <i class="bi bi-calendar-month text-gold fs-5"></i>
            </div>
            <h3 class="fw-bold text-warning mb-0">{{ number_format($votesThisMonth) }}</h3>
            <small class="text-muted">Bulan berjalan</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Pemilih Unik</span>
                <i class="bi bi-people-fill text-info fs-5"></i>
            </div>
            <h3 class="fw-bold text-info mb-0">{{ number_format($uniqueVoters) }}</h3>
            <small class="text-muted">Pemain berbeda</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Imbalan Terkirim</span>
                <i class="bi bi-gift-fill text-success fs-5"></i>
            </div>
            <h3 class="fw-bold text-success mb-0">{{ number_format($rewardsDelivered) }}</h3>
            <small class="text-muted">3 Keys + Rp 1.000 sukses</small>
        </div>
    </div>
    <div class="col-sm-6 col-xl">
        <div class="card p-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Imbalan Gagal / Pending</span>
                <i class="bi bi-exclamation-triangle-fill text-danger fs-5"></i>
            </div>
            <h3 class="fw-bold text-danger mb-0">{{ number_format($failedRewards) }}</h3>
            <small class="text-muted">Membutuhkan perhatian</small>
        </div>
    </div>
</div>

<!-- Platforms Management & Settings Card -->
<div class="card mb-4">
    <div class="card-header d-flex justify-content-between align-items-center bg-dark text-white border-secondary">
        <h5 class="mb-0 fw-bold">
            <i class="bi bi-hdd-network me-2 text-warning"></i> Platform Voting Terdaftar
        </h5>
        <span class="badge bg-warning text-dark">{{ $sites->count() }} Platform</span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="text-muted text-uppercase small">
                        <th>Platform</th>
                        <th>Slug</th>
                        <th>URL Voting</th>
                        <th>Server ID</th>
                        <th>API Key Platform</th>
                        <th>Cooldown</th>
                        <th>Status</th>
                        <th class="text-end">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @foreach($sites as $site)
                    <tr>
                        <td>
                            <strong class="text-white">{{ $site->name }}</strong>
                        </td>
                        <td><code class="text-warning">{{ $site->slug }}</code></td>
                        <td>
                            <a href="{{ $site->vote_url }}" target="_blank" class="text-truncate d-inline-block text-info text-decoration-none" style="max-width: 260px;">
                                {{ $site->vote_url }} <i class="bi bi-box-arrow-up-right small"></i>
                            </a>
                        </td>
                        <td>
                            <span class="font-monospace small text-muted">{{ $site->server_id ?: '-' }}</span>
                        </td>
                        <td>
                            @if($site->api_key)
                                <span class="badge bg-success"><i class="bi bi-key-fill me-1"></i> Terpasang</span>
                            @else
                                <span class="badge bg-secondary"><i class="bi bi-shield-x me-1"></i> Manual / Webhook</span>
                            @endif
                        </td>
                        <td>{{ $site->cooldown_hours }} Jam</td>
                        <td>
                            @if($site->is_active)
                                <span class="badge bg-success">AKTIF</span>
                            @else
                                <span class="badge bg-danger">NONAKTIF</span>
                            @endif
                        </td>
                        <td class="text-end">
                            <div class="btn-group btn-group-sm">
                                <button type="button" class="btn btn-outline-warning" data-bs-toggle="modal" data-bs-target="#editSiteModal{{ $site->id }}">
                                    <i class="bi bi-pencil-square"></i> Konfigurasi
                                </button>
                                <form action="{{ route('apexsions-bridge.admin.votes.sites.toggle', $site->id) }}" method="POST" class="d-inline">
                                    @csrf
                                    <button type="submit" class="btn btn-outline-{{ $site->is_active ? 'danger' : 'success' }}">
                                        {{ $site->is_active ? 'Nonaktifkan' : 'Aktifkan' }}
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>

                    <!-- Edit Modal for each site -->
                    <div class="modal fade" id="editSiteModal{{ $site->id }}" tabindex="-1" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered">
                            <div class="modal-content bg-dark border-secondary text-white">
                                <form action="{{ route('apexsions-bridge.admin.votes.sites.update', $site->id) }}" method="POST">
                                    @csrf
                                    <div class="modal-header border-secondary">
                                        <h5 class="modal-title font-cinzel text-warning">Konfigurasi {{ $site->name }}</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="mb-3">
                                            <label class="form-label small text-muted">Nama Tampilan Platform</label>
                                            <input type="text" name="name" class="form-control bg-dark text-white border-secondary" value="{{ $site->name }}" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small text-muted">Tautan Resmi Voting (URL)</label>
                                            <input type="url" name="vote_url" class="form-control bg-dark text-white border-secondary" value="{{ $site->vote_url }}" required>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small text-muted">Server ID (di situs platform)</label>
                                            <input type="text" name="server_id" class="form-control bg-dark text-white border-secondary" value="{{ $site->server_id }}">
                                            <small class="text-muted">Misal: <code>338274</code> untuk Minecraft-MP.</small>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small text-muted">API Key Platform (Untuk Verifikasi/Polling API)</label>
                                            <input type="text" name="api_key" class="form-control bg-dark text-white border-secondary" value="{{ $site->api_key }}" placeholder="Masukkan API Key dari dashboard platform...">
                                            <small class="text-muted">Digunakan untuk auto-poll vote dan claim API.</small>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label small text-muted">Cooldown Suara (Jam)</label>
                                            <input type="number" name="cooldown_hours" class="form-control bg-dark text-white border-secondary" value="{{ $site->cooldown_hours }}" min="1" max="168" required>
                                        </div>
                                    </div>
                                    <div class="modal-footer border-secondary">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                                        <button type="submit" class="btn btn-warning fw-bold">Simpan Pengaturan</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    @endforeach
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Transactions Ledger with Filters -->
<div class="card">
    <div class="card-header bg-dark text-white border-secondary">
        <div class="row align-items-center g-3">
            <div class="col-md">
                <h5 class="mb-0 fw-bold">
                    <i class="bi bi-journal-text me-2 text-warning"></i> Buku Besar Transaksi Suara (Vote Ledger)
                </h5>
            </div>
            <div class="col-md-auto">
                <!-- Search & Filters -->
                <form action="{{ route('apexsions-bridge.admin.votes.index') }}" method="GET" class="d-flex flex-wrap gap-2">
                    <input type="text" name="player" class="form-control form-control-sm bg-dark text-white border-secondary" placeholder="Cari pemain..." value="{{ request('player') }}" style="width: 140px;">
                    <select name="site" class="form-select form-select-sm bg-dark text-white border-secondary" style="width: 130px;">
                        <option value="">Semua Situs</option>
                        @foreach($sites as $s)
                            <option value="{{ $s->slug }}" {{ request('site') === $s->slug ? 'selected' : '' }}>{{ $s->name }}</option>
                        @endforeach
                    </select>
                    <select name="status" class="form-select form-select-sm bg-dark text-white border-secondary" style="width: 130px;">
                        <option value="">Semua Status</option>
                        <option value="REWARDED" {{ request('status') === 'REWARDED' ? 'selected' : '' }}>REWARDED</option>
                        <option value="PENDING" {{ request('status') === 'PENDING' ? 'selected' : '' }}>PENDING</option>
                        <option value="PROCESSING" {{ request('status') === 'PROCESSING' ? 'selected' : '' }}>PROCESSING</option>
                        <option value="PARTIAL" {{ request('status') === 'PARTIAL' ? 'selected' : '' }}>PARTIAL</option>
                        <option value="FAILED" {{ request('status') === 'FAILED' ? 'selected' : '' }}>FAILED</option>
                    </select>
                    <input type="date" name="date" class="form-control form-control-sm bg-dark text-white border-secondary" value="{{ request('date') }}" style="width: 130px;">
                    <button type="submit" class="btn btn-sm btn-warning fw-bold"><i class="bi bi-search"></i> Filter</button>
                    @if(request()->hasAny(['player', 'site', 'status', 'date']))
                        <a href="{{ route('apexsions-bridge.admin.votes.index') }}" class="btn btn-sm btn-secondary"><i class="bi bi-x"></i> Reset</a>
                    @endif
                </form>
            </div>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-dark table-hover mb-0 align-middle">
                <thead>
                    <tr class="text-muted text-uppercase small">
                        <th>Pemain</th>
                        <th>Platform</th>
                        <th>Waktu Suara</th>
                        <th>Imbalan</th>
                        <th>Status Imbalan</th>
                        <th>Delivery IDs</th>
                        <th class="text-end">Aksi</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($transactions as $tx)
                    <tr>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <img src="https://mc-heads.net/avatar/{{ $tx->player_username }}/24" class="rounded" width="24" height="24">
                                <div>
                                    <div class="fw-bold text-white">{{ $tx->player_username }}</div>
                                    <div class="text-muted font-monospace small" style="font-size: 0.68rem;">{{ $tx->player_uuid ?: 'Belum ditautkan' }}</div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span class="badge bg-dark border text-warning">{{ $tx->site ? $tx->site->name : $tx->site_slug }}</span>
                        </td>
                        <td>
                            <span class="small text-white">{{ $tx->voted_at->format('d M Y H:i') }}</span>
                            <div class="text-muted small" style="font-size: 0.7rem;">{{ $tx->voted_at->diffForHumans() }}</div>
                        </td>
                        <td>
                            <span class="badge bg-warning text-dark fw-bold me-1">3x Keys</span>
                            <span class="badge bg-success text-white fw-bold">Rp 1.000</span>
                        </td>
                        <td>
                            @if($tx->reward_status === 'REWARDED')
                                <span class="badge bg-success text-white"><i class="bi bi-check-circle me-1"></i> REWARDED</span>
                            @elseif($tx->reward_status === 'FAILED')
                                <span class="badge bg-danger text-white"><i class="bi bi-x-circle me-1"></i> FAILED</span>
                            @elseif($tx->reward_status === 'PARTIAL')
                                <span class="badge bg-warning text-dark"><i class="bi bi-exclamation-circle me-1"></i> PARTIAL</span>
                            @else
                                <span class="badge bg-info text-dark"><i class="bi bi-hourglass-split me-1"></i> {{ $tx->reward_status }}</span>
                            @endif
                        </td>
                        <td>
                            <div class="font-monospace small" style="font-size: 0.72rem;">
                                Key: #{{ $tx->keys_delivery_id ?: '-' }} &bull; Money: #{{ $tx->money_delivery_id ?: '-' }}
                            </div>
                        </td>
                        <td class="text-end">
                            <div class="btn-group btn-group-sm">
                                <button type="button" class="btn btn-outline-secondary" onclick="viewVoteDetail({{ json_encode($tx) }})">
                                    <i class="bi bi-eye"></i> Detail
                                </button>
                                @if($tx->reward_status !== 'REWARDED')
                                <button type="button" class="btn btn-outline-danger dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="bi bi-arrow-repeat"></i> Retry
                                </button>
                                <ul class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow">
                                    <li>
                                        <form action="{{ route('apexsions-bridge.admin.votes.retry', $tx->id) }}" method="POST" onsubmit="return confirm('Kirim ulang seluruh imbalan yang belum masuk untuk {{ $tx->player_username }}?');">
                                            @csrf
                                            <button type="submit" class="dropdown-item text-warning">
                                                <i class="bi bi-check-all me-2"></i> Retry Semua Hadiah
                                            </button>
                                        </form>
                                    </li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li>
                                        <form action="{{ route('apexsions-bridge.admin.votes.retry.key', $tx->id) }}" method="POST" onsubmit="return confirm('Kirim ulang 3x Vote Keys untuk {{ $tx->player_username }}?');">
                                            @csrf
                                            <button type="submit" class="dropdown-item text-white">
                                                <i class="bi bi-key me-2"></i> Retry Kunci Saja (3 Keys)
                                            </button>
                                        </form>
                                    </li>
                                    <li>
                                        <form action="{{ route('apexsions-bridge.admin.votes.retry.money', $tx->id) }}" method="POST" onsubmit="return confirm('Kirim ulang Rp 1.000 untuk {{ $tx->player_username }}?');">
                                            @csrf
                                            <button type="submit" class="dropdown-item text-success">
                                                <i class="bi bi-coin me-2"></i> Retry Uang Saja (Rp 1.000)
                                            </button>
                                        </form>
                                    </li>
                                </ul>
                                @endif
                            </div>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <i class="bi bi-inbox fs-3 d-block mb-2"></i>
                            Belum ada data transaksi suara yang sesuai dengan filter pencarian.
                        </td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
    @if($transactions->hasPages())
    <div class="card-footer bg-dark border-secondary">
        {{ $transactions->links() }}
    </div>
    @endif
</div>

<!-- Transaction Inspection Modal -->
<div class="modal fade" id="voteDetailModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary text-white">
            <div class="modal-header border-secondary">
                <h5 class="modal-title font-cinzel text-warning"><i class="bi bi-receipt me-2"></i> Detail Transaksi Suara</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" id="voteModalBody">
                <!-- Populated dynamically via JavaScript -->
            </div>
            <div class="modal-footer border-secondary">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Tutup</button>
            </div>
        </div>
    </div>
</div>

<script>
function viewVoteDetail(tx) {
    const body = document.getElementById('voteModalBody');
    body.innerHTML = `
        <div class="d-flex align-items-center gap-3 mb-3 p-3 rounded" style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08);">
            <img src="https://mc-heads.net/avatar/${tx.player_username}/48" class="rounded" width="48" height="48">
            <div>
                <h5 class="mb-0 fw-bold text-white">${tx.player_username}</h5>
                <div class="text-muted font-monospace small">${tx.player_uuid || 'UUID belum ditautkan'}</div>
            </div>
        </div>
        <table class="table table-sm table-dark text-muted small mb-0">
            <tr><td>Vote UUID:</td><td class="font-monospace text-white">${tx.vote_uuid}</td></tr>
            <tr><td>External Vote ID:</td><td class="font-monospace text-info">${tx.external_vote_id || '-'}</td></tr>
            <tr><td>Platform:</td><td class="text-warning">${tx.site ? tx.site.name : tx.site_slug}</td></tr>
            <tr><td>Waktu Suara:</td><td class="text-white">${new Date(tx.voted_at).toLocaleString('id-ID')}</td></tr>
            <tr><td>Diverifikasi Pada:</td><td class="text-white">${tx.verified_at ? new Date(tx.verified_at).toLocaleString('id-ID') : '-'}</td></tr>
            <tr><td>Status Suara:</td><td><span class="badge bg-info">${tx.vote_status}</span></td></tr>
            <tr><td>Sumber/External Status:</td><td><span class="badge bg-secondary">${tx.external_status || 'MANUAL'}</span></td></tr>
            <tr><td>Status Imbalan:</td><td><span class="badge ${tx.reward_status === 'REWARDED' ? 'bg-success' : 'bg-danger'}">${tx.reward_status}</span></td></tr>
            <tr><td>Kunci Peti:</td><td class="text-warning fw-bold">${tx.keys_amount}x Vote Keys</td></tr>
            <tr><td>Saldo Uang:</td><td class="text-success fw-bold">Rp ${Number(tx.money_amount).toLocaleString('id-ID')}</td></tr>
            <tr><td>Delivery Key ID:</td><td class="font-monospace">#${tx.keys_delivery_id || '-'}</td></tr>
            <tr><td>Delivery Money ID:</td><td class="font-monospace">#${tx.money_delivery_id || '-'}</td></tr>
            <tr><td>Jumlah Retry:</td><td>${tx.retry_count || 0}</td></tr>
            <tr><td>Idempotency Hash:</td><td class="font-monospace small text-truncate" style="max-width: 200px;">${tx.idempotency_hash || '-'}</td></tr>
            ${tx.failure_reason ? `<tr><td class="text-danger">Penyebab Gagal:</td><td class="text-danger">${tx.failure_reason}</td></tr>` : ''}
        </table>
    `;
    new bootstrap.Modal(document.getElementById('voteDetailModal')).show();
}
</script>
@endsection
