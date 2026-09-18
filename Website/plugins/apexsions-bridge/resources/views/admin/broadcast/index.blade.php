@extends('admin.layouts.admin')

@section('title', 'Live Broadcast & Lockdown Hub — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-broadcast-pin text-warning me-2"></i>Live Server Broadcast & Lockdown Hub
            </h2>
            <p class="text-white-50 small mb-0">
                Siarkan pengumuman real-time langsung ke layar pemain (Chat, Screen Title, Actionbar) dan saklar darurat Server Lockdown.
            </p>
        </div>
        <div>
            @if($maintenance && $maintenance->is_enabled)
                <form action="{{ route('apexsions-bridge.admin.broadcast.toggle-lockdown') }}" method="POST" class="d-inline" onsubmit="return confirm('Apakah Anda yakin ingin menonaktifkan status Server Lockdown?');">
                    @csrf
                    <input type="hidden" name="enable" value="0">
                    <button type="submit" class="btn btn-outline-success btn-sm shadow-sm fw-bold">
                        <i class="bi bi-shield-check me-1"></i>Cabut Mode Lockdown
                    </button>
                </form>
            @else
                <button type="button" class="btn btn-danger btn-sm shadow-sm fw-bold" data-bs-toggle="modal" data-bs-target="#lockdownModal">
                    <i class="bi bi-shield-slash-fill me-1"></i>Aktifkan Server Lockdown (Darurat)
                </button>
            @endif
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

    <!-- Emergency Lockdown Alert if active -->
    @if($maintenance && $maintenance->is_enabled)
        <div class="alert alert-danger bg-danger bg-opacity-25 border-danger text-white p-3 mb-4 shadow">
            <div class="d-flex align-items-center gap-3">
                <div class="spinner-grow text-danger" role="status"></div>
                <div>
                    <span class="badge bg-danger text-uppercase px-2 py-1 mb-1">⚠ SERVER LOCKDOWN AKTIF ⚠</span>
                    <h5 class="fw-bold mb-1">Server sedang berada di bawah Protokol Darurat / Pemeliharaan</h5>
                    <p class="small text-white-50 mb-0">Alasan: {{ $maintenance->reason }} | Diaktifkan oleh: {{ $maintenance->enabled_by }}</p>
                </div>
            </div>
        </div>
    @endif

    <div class="row g-4">
        <!-- Live Dispatch Form -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
                    <h5 class="card-title h6 fw-bold text-white mb-0">
                        <i class="bi bi-megaphone-fill text-warning me-2"></i>Kirim Siaran Instan
                    </h5>
                </div>
                <div class="card-body">
                    <form action="{{ route('apexsions-bridge.admin.broadcast.send') }}" method="POST">
                        @csrf
                        <div class="mb-3">
                            <label class="form-label small text-white-50">Tipe Siaran Layar</label>
                            <select id="broadcastTypeSelect" name="broadcast_type" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                                <option value="CHAT">Chat Announcement (Pesan Obrolan Emas Terpusat)</option>
                                <option value="TITLE">Big Screen Title (Judul Raksasa di Tengah Layar)</option>
                                <option value="ACTIONBAR">Actionbar Message (Pesan Halus di Atas Hotbar)</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label small text-white-50">Target Penerima</label>
                            <select name="target_audience" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                                <option value="GLOBAL">Seluruh Pemain Online (@a)</option>
                                <option value="SOLTERRA">Warga Kerajaan Solterra</option>
                                <option value="ZENITHAR">Warga Kerajaan Zenithar</option>
                                <option value="SYLVAMOOR">Warga Kerajaan Sylvamoor</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label small text-white-50">Efek Suara (Sound Effect)</label>
                            <select name="sound_effect" class="form-select bg-secondary bg-opacity-25 border-secondary text-white">
                                <option value="DING">Experience Orb (Ding Nyaring)</option>
                                <option value="LEVELUP">Challenge Complete (Jingle Peringatan)</option>
                                <option value="BELL">Church Bell (Lonceng Kerajaan)</option>
                                <option value="DRAGON">Ender Dragon (Geraman Perang)</option>
                                <option value="NONE">Tanpa Suara</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label small text-white-50">Isi Pesan Siaran</label>
                            <textarea name="message" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" rows="3" placeholder="Ketik pesan pengumuman di sini..." required></textarea>
                        </div>
                        <div class="mb-4" id="subtitleField" style="display: none;">
                            <label class="form-label small text-white-50">Subtitle Pendukung (Khusus Tipe Title)</label>
                            <input type="text" name="subtitle" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Pesan kecil di bawah judul">
                        </div>
                        <button type="submit" class="btn btn-warning w-100 fw-bold text-dark shadow-sm">
                            <i class="bi bi-send-fill me-1"></i>Siarkan Sekarang ke Server
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <!-- Recent Broadcasts Logs -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
                    <h5 class="card-title h6 fw-bold text-white mb-0">
                        <i class="bi bi-clock-history text-warning me-2"></i>Riwayat Siaran Terkini
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead class="table-secondary bg-opacity-10 text-uppercase small">
                                <tr>
                                    <th class="ps-3">Waktu</th>
                                    <th>Aktor</th>
                                    <th>Jenis</th>
                                    <th class="pe-3 text-end">Pesan</th>
                                </tr>
                            </thead>
                            <tbody>
                                @forelse($recentBroadcasts as $bCast)
                                    <tr>
                                        <td class="ps-3 small text-white-50">{{ $bCast->created_at->format('H:i:s') }}</td>
                                        <td><span class="badge bg-secondary">{{ $bCast->actor_name }}</span></td>
                                        <td><span class="badge bg-info text-dark">{{ $bCast->action }}</span></td>
                                        <td class="pe-3 text-end small text-white">{{ Str::limit($bCast->new_value, 35) }}</td>
                                    </tr>
                                @empty
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-white-50">Belum ada siaran tercatat hari ini.</td>
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

<!-- Modal Lockdown -->
<div class="modal fade" id="lockdownModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-danger text-white">
            <form action="{{ route('apexsions-bridge.admin.broadcast.toggle-lockdown') }}" method="POST">
                @csrf
                <input type="hidden" name="enable" value="1">
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-danger"><i class="bi bi-shield-slash-fill me-2"></i>Konfirmasi Server Lockdown</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="alert alert-danger bg-danger bg-opacity-10 border-danger small mb-3">
                        <i class="bi bi-exclamation-octagon me-1"></i>
                        Mode Lockdown akan mengunci server hanya untuk Staf (Authority), melarang login warga biasa, dan menghentikan seluruh transaksi pasar untuk perlindungan integritas data.
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Alasan Lockdown</label>
                        <input type="text" name="reason" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Contoh: Investigasi Duplikasi Item / Patching Darurat" required>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger btn-sm fw-bold">Aktifkan Protokol Lockdown</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const typeSelect = document.getElementById('broadcastTypeSelect');
    const subField = document.getElementById('subtitleField');
    if (typeSelect && subField) {
        typeSelect.addEventListener('change', function () {
            if (this.value === 'TITLE') {
                subField.style.display = 'block';
            } else {
                subField.style.display = 'none';
            }
        });
    }
});
</script>
@endsection
