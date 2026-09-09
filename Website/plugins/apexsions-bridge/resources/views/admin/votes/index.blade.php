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
                        🗳 VOTE MANAGEMENT &amp; CIVIC VERIFICATION
                    </h3>
                </div>
                <p class="text-muted small mb-0" style="max-width: 750px;">
                    Pusat pemantauan suara dukungan server Apexsions di kancah dunia. Setiap suara sah diverifikasi secara ketat dan memberikan imbalan otomatis <strong class="text-warning">3x Vote Keys</strong> dan <strong class="text-success">Rp 1.000</strong> secara atomic melalui Minecraft Deliveries Bridge.
                </p>
            </div>
            <div class="d-flex align-items-center gap-2">
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
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Pemilih Unik</span>
                <i class="bi bi-people-fill text-primary fs-5"></i>
            </div>
            <h3 class="fw-bold text-primary mb-0">{{ number_format($uniqueVoters) }}</h3>
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
                <span class="small fw-bold text-muted text-uppercase" style="font-size: 0.72rem;">Imbalan Gagal</span>
                <i class="bi bi-exclamation-triangle-fill text-danger fs-5"></i>
            </div>
            <h3 class="fw-bold text-danger mb-0">{{ number_format($failedRewards) }}</h3>
            <small class="text-muted">Membutuhkan retry</small>
        </div>
    </div>
</div>

<!-- VOTING PLATFORMS INTEGRATION & CONTROL -->
<div class="card mb-4 shadow-sm" style="border: 1px solid rgba(201, 164, 92, 0.25); background: linear-gradient(135deg, rgba(24, 27, 36, 0.95) 0%, rgba(17, 19, 25, 0.95) 100%);">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2 py-3" style="background: rgba(255, 255, 255, 0.02); border-bottom: 1px solid rgba(201, 164, 92, 0.2);">
        <div>
            <h5 class="card-title mb-0 fw-bold font-cinzel text-white">
                <i class="bi bi-sliders me-2 text-warning"></i> INTEGRASI &amp; KONTROL PLATFORM VOTING
            </h5>
            <small class="text-muted">Kelola status aktif/nonaktif, URL voting, Server ID, dan Server API Key setiap platform.</small>
        </div>
        <span class="badge bg-dark border border-secondary text-warning px-3 py-2">
            <i class="bi bi-broadcast me-1"></i> {{ $sites->where('is_active', true)->count() }} dari {{ $sites->count() }} Platform Aktif
        </span>
    </div>
    <div class="card-body p-3">
        <div class="row g-3">
            @foreach($sites as $site)
            <div class="col-lg-4 col-md-6">
                <div class="p-3 rounded h-100 d-flex flex-column justify-content-between" style="background: rgba(0, 0, 0, 0.35); border: 1px solid {{ $site->is_active ? 'rgba(40, 167, 69, 0.45)' : 'rgba(220, 53, 69, 0.35)' }};">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <div>
                                <h6 class="fw-bold mb-0 text-white font-cinzel">{{ $site->name }}</h6>
                                <span class="font-monospace text-muted" style="font-size: 0.72rem;">slug: {{ $site->slug }}</span>
                            </div>
                            @if($site->is_active)
                                <span class="badge bg-success"><i class="bi bi-check-circle-fill me-1"></i> AKTIF</span>
                            @else
                                <span class="badge bg-danger"><i class="bi bi-slash-circle me-1"></i> NONAKTIF</span>
                            @endif
                        </div>

                        <div class="small mb-3">
                            <div class="text-truncate text-muted mb-2" style="font-size: 0.78rem;" title="{{ $site->vote_url }}">
                                <i class="bi bi-link-45deg text-warning me-1"></i>
                                <a href="{{ $site->vote_url }}" target="_blank" class="text-dim text-decoration-none">{{ $site->vote_url }}</a>
                            </div>
                            <div class="d-flex flex-wrap gap-2" style="font-size: 0.72rem;">
                                @if(!empty($site->server_id))
                                    <span class="badge bg-dark border text-info"><i class="bi bi-hash me-1"></i>Server ID: {{ $site->server_id }}</span>
                                @endif
                                @if(!empty($site->api_key))
                                    <span class="badge bg-dark border text-success"><i class="bi bi-key-fill me-1"></i>API Key Aktif</span>
                                @else
                                    <span class="badge bg-dark border text-muted"><i class="bi bi-key me-1"></i>Tanpa API Key</span>
                                @endif
                                <span class="badge bg-dark border text-warning"><i class="bi bi-clock me-1"></i>{{ $site->cooldown_hours }}j Cooldown</span>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex gap-2 pt-2 border-top border-secondary border-opacity-25 mt-2">
                        <!-- Toggle Button (Active <-> Disabled) -->
                        <form method="POST" action="{{ route('apexsions-bridge.admin.votes.sites.toggle', $site->id) }}" class="flex-grow-1">
                            @csrf
                            @if($site->is_active)
                                <button type="submit" class="btn btn-sm btn-outline-danger w-100 fw-bold" onclick="return confirm('Nonaktifkan platform {{ $site->name }}? Pemain tidak akan melihat platform ini di halaman /vote.')">
                                    <i class="bi bi-pause-circle me-1"></i> Nonaktifkan
                                </button>
                            @else
                                <button type="submit" class="btn btn-sm btn-outline-success w-100 fw-bold">
                                    <i class="bi bi-play-circle me-1"></i> Aktifkan
                                </button>
                            @endif
                        </form>

                        <!-- Edit / Configure Settings Button -->
                        <button type="button" class="btn btn-sm btn-outline-warning px-3" data-bs-toggle="modal" data-bs-target="#editSiteModal{{ $site->id }}" title="Konfigurasi Platform">
                            <i class="bi bi-gear-fill"></i>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Modal Edit Platform -->
            <div class="modal fade" id="editSiteModal{{ $site->id }}" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content bg-dark border-secondary text-white">
                        <form method="POST" action="{{ route('apexsions-bridge.admin.votes.sites.update', $site->id) }}">
                            @csrf
                            <input type="hidden" name="has_active_toggle" value="1">
                            <div class="modal-header border-secondary">
                                <h5 class="modal-title font-cinzel text-warning">
                                    <i class="bi bi-gear-wide-connected me-2"></i> Konfigurasi {{ $site->name }}
                                </h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label class="form-label small text-muted">Nama Platform</label>
                                    <input type="text" name="name" class="form-control bg-black border-secondary text-white form-control-sm" value="{{ $site->name }}" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label small text-muted">Vote URL (Halaman Pemilihan Publik)</label>
                                    <input type="url" name="vote_url" class="form-control bg-black border-secondary text-white form-control-sm" value="{{ $site->vote_url }}" required>
                                </div>
                                <div class="row g-2 mb-3">
                                    <div class="col-md-6">
                                        <label class="form-label small text-muted">Server ID (Platform)</label>
                                        <input type="text" name="server_id" class="form-control bg-black border-secondary text-white form-control-sm font-monospace" placeholder="Contoh: 363636" value="{{ $site->server_id }}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label small text-muted">Cooldown (Jam)</label>
                                        <input type="number" name="cooldown_hours" class="form-control bg-black border-secondary text-white form-control-sm" min="1" max="168" value="{{ $site->cooldown_hours }}" required>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label small text-muted">Server API Key (Untuk Validasi Otomatis)</label>
                                    <input type="text" name="api_key" class="form-control bg-black border-secondary text-white form-control-sm font-monospace" placeholder="Masukkan Server API Key..." value="{{ $site->api_key }}">
                                    <small class="text-muted" style="font-size: 0.72rem;">*Kosongkan jika platform tidak menggunakan API Key verifikasi klaim.</small>
                                </div>
                                <div class="form-check form-switch mt-2">
                                    <input class="form-check-input" type="checkbox" name="is_active" id="isActiveSwitch{{ $site->id }}" value="1" {{ $site->is_active ? 'checked' : '' }}>
                                    <label class="form-check-label small" for="isActiveSwitch{{ $site->id }}">
                                        Aktifkan Platform ini di Bilik Suara Publik
                                    </label>
                                </div>
                            </div>
                            <div class="modal-footer border-secondary">
                                <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Batal</button>
                                <button type="submit" class="btn btn-sm btn-warning fw-bold">
                                    <i class="bi bi-save me-1"></i> Simpan Perubahan
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            @endforeach
        </div>
    </div>
</div>

<!-- Filters -->
<div class="card p-3 mb-4">
    <form method="GET" action="{{ route('apexsions-bridge.admin.votes.index') }}" class="row g-2 align-items-center">
        <div class="col-md-3">
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" name="player" class="form-control form-control-sm" placeholder="Cari nama pemain..." value="{{ request('player') }}">
            </div>
        </div>
        <div class="col-md-3">
            <select name="site" class="form-select form-select-sm">
                <option value="">-- Semua Platform --</option>
                @foreach($sites as $site)
                    <option value="{{ $site->slug }}" {{ request('site') === $site->slug ? 'selected' : '' }}>{{ $site->name }}</option>
                @endforeach
            </select>
        </div>
        <div class="col-md-2">
            <select name="status" class="form-select form-select-sm">
                <option value="">-- Semua Status --</option>
                <option value="REWARDED" {{ request('status') === 'REWARDED' ? 'selected' : '' }}>REWARDED (Sukses)</option>
                <option value="PENDING" {{ request('status') === 'PENDING' ? 'selected' : '' }}>PENDING (Diproses)</option>
                <option value="FAILED" {{ request('status') === 'FAILED' ? 'selected' : '' }}>FAILED (Gagal)</option>
            </select>
        </div>
        <div class="col-md-2">
            <input type="date" name="date" class="form-control form-control-sm" value="{{ request('date') }}">
        </div>
        <div class="col-md-2 d-flex gap-1">
            <button type="submit" class="btn btn-sm btn-warning w-100 fw-bold">
                <i class="bi bi-filter"></i> Filter
            </button>
            <a href="{{ route('apexsions-bridge.admin.votes.index') }}" class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-arrow-counterclockwise"></i>
            </a>
        </div>
    </form>
</div>

<!-- Vote Transactions Table -->
<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center py-3">
        <h5 class="card-title mb-0 fw-bold font-cinzel">
            <i class="bi bi-receipt-cutoff me-2 text-warning"></i> BUKU BESAR TRANSAKSI SUARA (VOTE LEDGER)
        </h5>
        <span class="badge bg-secondary">{{ $transactions->total() }} Catatan</span>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark text-uppercase small" style="font-size: 0.75rem; letter-spacing: 0.05em;">
                <tr>
                    <th>Warga Pemilih</th>
                    <th>Platform</th>
                    <th>Waktu Suara</th>
                    <th>Imbalan Sah</th>
                    <th>Status Imbalan</th>
                    <th>Ref Deliveries</th>
                    <th class="text-end">Aksi</th>
                </tr>
            </thead>
            <tbody>
                @forelse($transactions as $tx)
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="https://mc-heads.net/avatar/{{ $tx->player_username }}/28" class="rounded" alt="{{ $tx->player_username }}" width="28" height="28">
                            <div>
                                <span class="fw-bold text-white">{{ $tx->player_username }}</span>
                                @if($tx->player_uuid)
                                    <div class="text-muted font-monospace" style="font-size: 0.68rem;">{{ Str::limit($tx->player_uuid, 14) }}</div>
                                @endif
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
                        @else
                            <span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i> {{ $tx->reward_status }}</span>
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
                            @if($tx->reward_status === 'FAILED')
                            <form action="{{ route('apexsions-bridge.admin.votes.retry', $tx->id) }}" method="POST" class="d-inline" onsubmit="return confirm('Kirim ulang imbalan 3 Keys + Rp 1.000 untuk {{ $tx->player_username }}?');">
                                @csrf
                                <button type="submit" class="btn btn-danger" title="Retry Reward">
                                    <i class="bi bi-arrow-repeat"></i> Retry
                                </button>
                            </form>
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
    @if($transactions->hasPages())
    <div class="card-footer py-2">
        {{ $transactions->links() }}
    </div>
    @endif
</div>

<!-- Vote Detail Modal -->
<div class="modal fade" id="voteDetailModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary text-white">
            <div class="modal-header border-secondary">
                <h5 class="modal-title font-cinzel text-warning"><i class="bi bi-info-circle me-2"></i> Rincian Suara Realm</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" id="voteModalBody">
                <!-- Injected via JavaScript -->
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
            <tr><td>Platform:</td><td class="text-warning">${tx.site ? tx.site.name : tx.site_slug}</td></tr>
            <tr><td>Waktu Suara:</td><td class="text-white">${new Date(tx.voted_at).toLocaleString('id-ID')}</td></tr>
            <tr><td>Status Suara:</td><td><span class="badge bg-info">${tx.vote_status}</span></td></tr>
            <tr><td>Status Imbalan:</td><td><span class="badge ${tx.reward_status === 'REWARDED' ? 'bg-success' : 'bg-danger'}">${tx.reward_status}</span></td></tr>
            <tr><td>Kunci Peti:</td><td class="text-warning fw-bold">${tx.keys_amount}x Vote Keys</td></tr>
            <tr><td>Saldo Uang:</td><td class="text-success fw-bold">Rp ${Number(tx.money_amount).toLocaleString('id-ID')}</td></tr>
            <tr><td>Delivery Key ID:</td><td class="font-monospace">#${tx.keys_delivery_id || '-'}</td></tr>
            <tr><td>Delivery Money ID:</td><td class="font-monospace">#${tx.money_delivery_id || '-'}</td></tr>
            <tr><td>Jumlah Retry:</td><td>${tx.retry_count || 0}</td></tr>
            ${tx.failure_reason ? `<tr><td class="text-danger">Penyebab Gagal:</td><td class="text-danger">${tx.failure_reason}</td></tr>` : ''}
        </table>
    `;
    new bootstrap.Modal(document.getElementById('voteDetailModal')).show();
}
</script>
@endsection
