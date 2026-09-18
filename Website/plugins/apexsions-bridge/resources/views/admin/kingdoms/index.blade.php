@extends('admin.layouts.admin')

@section('title', 'Kingdoms & Territory War — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <h2 class="h3 fw-bold text-white mb-1">
                <i class="bi bi-shield-shaded text-warning me-2"></i>Kingdoms & Territory War Desk
            </h2>
            <p class="text-white-50 small mb-0">
                Pusat komando kedaulatan 3 Kerajaan (Solterra, Zenithar, Sylvamoor), pengangkatan Raja, kas perbendaharaan, dan deklarasi perang.
            </p>
        </div>
        <div class="d-flex gap-2 flex-wrap">
            @if($activeWar)
                <form action="{{ route('apexsions-bridge.admin.kingdoms.stop-war') }}" method="POST" onsubmit="return confirm('Apakah Anda yakin ingin menghentikan perang yang sedang berlangsung?');">
                    @csrf
                    <button type="submit" class="btn btn-danger btn-sm shadow-sm fw-bold">
                        <i class="bi bi-stop-circle-fill me-1"></i>Hentikan Perang (Gencatan Senjata)
                    </button>
                </form>
            @else
                <button type="button" class="btn btn-warning btn-sm shadow-sm fw-bold text-dark" data-bs-toggle="modal" data-bs-target="#startWarModal">
                    <i class="bi bi-swords me-1"></i>Deklarasikan Perang Kerajaan
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

    <!-- Active War Banner -->
    @if($activeWar)
        <div class="card border-danger bg-danger bg-opacity-10 mb-4 shadow">
            <div class="card-body p-3 d-flex justify-content-between align-items-center flex-wrap gap-3">
                <div class="d-flex align-items-center gap-3">
                    <div class="spinner-grow text-danger" role="status"></div>
                    <div>
                        <span class="badge bg-danger text-uppercase px-2 py-1 mb-1">⚔ PERANG SEDANG BERKECAMUK ⚔</span>
                        <h4 class="h5 fw-bold text-white mb-0">
                            {{ $activeWar['kingdom_1'] }} vs {{ $activeWar['kingdom_2'] }}
                        </h4>
                        <small class="text-white-50">Dideklarasikan oleh: {{ $activeWar['initiated_by'] ?? 'Admin' }} | Durasi: {{ $activeWar['duration_minutes'] ?? 30 }} Menit</small>
                    </div>
                </div>
                <div class="text-end">
                    <small class="text-danger fw-bold d-block">Batas Waktu Berakhir</small>
                    <span class="text-white fw-bold">{{ \Carbon\Carbon::parse($activeWar['expires_at'])->diffForHumans() }}</span>
                </div>
            </div>
        </div>
    @endif

    <!-- Metric Overview Cards -->
    <div class="row g-3 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-warning border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Solterra (Surya & Tambang)</span>
                        <i class="bi bi-sun-fill text-warning fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-warning mb-1">{{ number_format($popSolterra) }} Warga</div>
                    <small class="text-white-50">Kas: Rp {{ number_format($treasuries['SOLTERRA']->balance ?? 0, 0, ',', '.') }}</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-info border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Zenithar (Kubah Kemegahan)</span>
                        <i class="bi bi-gem text-info fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-info mb-1">{{ number_format($popZenithar) }} Warga</div>
                    <small class="text-white-50">Kas: Rp {{ number_format($treasuries['ZENITHAR']->balance ?? 0, 0, ',', '.') }}</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-success border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">Sylvamoor (Hutan Keramat)</span>
                        <i class="bi bi-tree-fill text-success fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-success mb-1">{{ number_format($popSylvamoor) }} Warga</div>
                    <small class="text-white-50">Kas: Rp {{ number_format($treasuries['SYLVAMOOR']->balance ?? 0, 0, ',', '.') }}</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-white-50 small fw-medium text-uppercase">The Conclave & Pengembara</span>
                        <i class="bi bi-people-fill text-secondary fs-5"></i>
                    </div>
                    <div class="h4 fw-bold text-white mb-1">{{ number_format($popAetherion) }} Staf / {{ number_format($popNone) }} Wanderer</div>
                    <small class="text-white-50">Total Populasi: {{ number_format($totalCitizens) }} Jiwa</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Kingdom Cards & Monarchy Details -->
    <div class="row g-4 mb-4">
        <!-- SOLTERRA -->
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center bg-black bg-opacity-25">
                    <span class="fw-bold text-warning"><i class="bi bi-shield-fill text-warning me-2"></i>SOLTERRA</span>
                    <span class="badge bg-warning text-dark">Sun Forged</span>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <small class="text-white-50 d-block mb-1">Penguasa / Raja:</small>
                        @if(!empty($monarchs['SOLTERRA']))
                            <div class="d-flex justify-content-between align-items-center bg-secondary bg-opacity-10 p-2 rounded">
                                <span class="fw-bold text-white"><i class="bi bi-crown text-warning me-1"></i>{{ $monarchs['SOLTERRA'] }}</span>
                                <form action="{{ route('apexsions-bridge.admin.kingdoms.unset-king') }}" method="POST" class="d-inline">
                                    @csrf
                                    <input type="hidden" name="kingdom" value="SOLTERRA">
                                    <button type="submit" class="btn btn-outline-danger btn-xs py-0 px-2" title="Copot Raja">Copot</button>
                                </form>
                            </div>
                        @else
                            <div class="text-muted fst-italic small mb-2">Takhta Sedang Kosong</div>
                            <button type="button" class="btn btn-sm btn-outline-warning w-100" data-bs-toggle="modal" data-bs-target="#setKingModal" data-kingdom="SOLTERRA">
                                <i class="bi bi-award me-1"></i>Tunjuk Raja Solterra
                            </button>
                        @endif
                    </div>
                    <div class="border-top border-secondary border-opacity-25 pt-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-white-50 small">Kas Perbendaharaan:</span>
                            <span class="fw-bold text-warning">Rp {{ number_format($treasuries['SOLTERRA']->balance ?? 0, 0, ',', '.') }}</span>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline-secondary w-100" data-bs-toggle="modal" data-bs-target="#treasuryModal" data-kingdom="solterra" data-name="Solterra">
                            <i class="bi bi-cash-stack me-1"></i>Kelola Perbendaharaan
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- ZENITHAR -->
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center bg-black bg-opacity-25">
                    <span class="fw-bold text-info"><i class="bi bi-shield-fill text-info me-2"></i>ZENITHAR</span>
                    <span class="badge bg-info text-dark">Golden Spire</span>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <small class="text-white-50 d-block mb-1">Penguasa / Raja:</small>
                        @if(!empty($monarchs['ZENITHAR']))
                            <div class="d-flex justify-content-between align-items-center bg-secondary bg-opacity-10 p-2 rounded">
                                <span class="fw-bold text-white"><i class="bi bi-crown text-warning me-1"></i>{{ $monarchs['ZENITHAR'] }}</span>
                                <form action="{{ route('apexsions-bridge.admin.kingdoms.unset-king') }}" method="POST" class="d-inline">
                                    @csrf
                                    <input type="hidden" name="kingdom" value="ZENITHAR">
                                    <button type="submit" class="btn btn-outline-danger btn-xs py-0 px-2" title="Copot Raja">Copot</button>
                                </form>
                            </div>
                        @else
                            <div class="text-muted fst-italic small mb-2">Takhta Sedang Kosong</div>
                            <button type="button" class="btn btn-sm btn-outline-info w-100" data-bs-toggle="modal" data-bs-target="#setKingModal" data-kingdom="ZENITHAR">
                                <i class="bi bi-award me-1"></i>Tunjuk Raja Zenithar
                            </button>
                        @endif
                    </div>
                    <div class="border-top border-secondary border-opacity-25 pt-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-white-50 small">Kas Perbendaharaan:</span>
                            <span class="fw-bold text-info">Rp {{ number_format($treasuries['ZENITHAR']->balance ?? 0, 0, ',', '.') }}</span>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline-secondary w-100" data-bs-toggle="modal" data-bs-target="#treasuryModal" data-kingdom="zenithar" data-name="Zenithar">
                            <i class="bi bi-cash-stack me-1"></i>Kelola Perbendaharaan
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- SYLVAMOOR -->
        <div class="col-md-4">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center bg-black bg-opacity-25">
                    <span class="fw-bold text-success"><i class="bi bi-shield-fill text-success me-2"></i>SYLVAMOOR</span>
                    <span class="badge bg-success text-white">Emerald Canopy</span>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <small class="text-white-50 d-block mb-1">Penguasa / Raja:</small>
                        @if(!empty($monarchs['SYLVAMOOR']))
                            <div class="d-flex justify-content-between align-items-center bg-secondary bg-opacity-10 p-2 rounded">
                                <span class="fw-bold text-white"><i class="bi bi-crown text-warning me-1"></i>{{ $monarchs['SYLVAMOOR'] }}</span>
                                <form action="{{ route('apexsions-bridge.admin.kingdoms.unset-king') }}" method="POST" class="d-inline">
                                    @csrf
                                    <input type="hidden" name="kingdom" value="SYLVAMOOR">
                                    <button type="submit" class="btn btn-outline-danger btn-xs py-0 px-2" title="Copot Raja">Copot</button>
                                </form>
                            </div>
                        @else
                            <div class="text-muted fst-italic small mb-2">Takhta Sedang Kosong</div>
                            <button type="button" class="btn btn-sm btn-outline-success w-100" data-bs-toggle="modal" data-bs-target="#setKingModal" data-kingdom="SYLVAMOOR">
                                <i class="bi bi-award me-1"></i>Tunjuk Raja Sylvamoor
                            </button>
                        @endif
                    </div>
                    <div class="border-top border-secondary border-opacity-25 pt-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-white-50 small">Kas Perbendaharaan:</span>
                            <span class="fw-bold text-success">Rp {{ number_format($treasuries['SYLVAMOOR']->balance ?? 0, 0, ',', '.') }}</span>
                        </div>
                        <button type="button" class="btn btn-sm btn-outline-secondary w-100" data-bs-toggle="modal" data-bs-target="#treasuryModal" data-kingdom="sylvamoor" data-name="Sylvamoor">
                            <i class="bi bi-cash-stack me-1"></i>Kelola Perbendaharaan
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Audit Logs for Kingdoms & War -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header border-secondary border-opacity-25 py-3 bg-black bg-opacity-25">
            <h5 class="card-title h6 fw-bold text-white mb-0">
                <i class="bi bi-journal-text text-warning me-2"></i>Log Aktivitas Kedaulatan & Perang Terkini
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-dark table-hover align-middle mb-0">
                    <thead class="table-secondary bg-opacity-10 text-uppercase small">
                        <tr>
                            <th class="ps-3">Waktu</th>
                            <th>Aktor</th>
                            <th>Aksi</th>
                            <th>Target</th>
                            <th>Rincian / Alasan</th>
                            <th class="pe-3 text-end">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($recentLogs as $log)
                            <tr>
                                <td class="ps-3 small text-white-50">{{ $log->created_at->format('d M H:i:s') }}</td>
                                <td><span class="badge bg-secondary">{{ $log->actor_name ?? 'System' }}</span></td>
                                <td><span class="badge bg-warning text-dark">{{ $log->action }}</span></td>
                                <td class="fw-semibold text-white">{{ $log->target_name ?? $log->target_id }}</td>
                                <td class="small text-white-50">{{ $log->reason }}</td>
                                <td class="pe-3 text-end">
                                    <span class="badge bg-success bg-opacity-25 text-success">{{ $log->status }}</span>
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="6" class="text-center py-4 text-white-50">Belum ada aktivitas kedaulatan tercatat.</td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal Start War -->
<div class="modal fade" id="startWarModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-danger text-white">
            <form action="{{ route('apexsions-bridge.admin.kingdoms.start-war') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-danger"><i class="bi bi-swords me-2"></i>Deklarasi Perang Kerajaan</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Kerajaan Penantang (Kerajaan 1)</label>
                        <select name="kingdom_1" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="SOLTERRA">SOLTERRA</option>
                            <option value="ZENITHAR">ZENITHAR</option>
                            <option value="SYLVAMOOR">SYLVAMOOR</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Kerajaan Bertahan (Kerajaan 2)</label>
                        <select name="kingdom_2" class="form-select bg-secondary bg-opacity-25 border-secondary text-white" required>
                            <option value="ZENITHAR">ZENITHAR</option>
                            <option value="SOLTERRA">SOLTERRA</option>
                            <option value="SYLVAMOOR">SYLVAMOOR</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Durasi Perang (Menit)</label>
                        <input type="number" name="duration" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" value="30" min="5" max="120" required>
                        <small class="text-white-50">Rentang yang diizinkan: 5 - 120 menit.</small>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Alasan Deklarasi (Lore / Event)</label>
                        <input type="text" name="reason" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Contoh: Sengketa Wilayah Perbatasan Tambang">
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-danger btn-sm fw-bold">Nyalakan Api Peperangan!</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Set King -->
<div class="modal fade" id="setKingModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-warning text-white">
            <form action="{{ route('apexsions-bridge.admin.kingdoms.set-king') }}" method="POST">
                @csrf
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-warning"><i class="bi bi-crown me-2"></i>Nobatkan Raja Kerajaan</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Kerajaan Target</label>
                        <input type="text" id="modalKingdomTarget" name="kingdom" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" readonly>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Username Pemain Minecraft</label>
                        <input type="text" name="player_username" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Contoh: NuevaID" required>
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Nobatkan Penguasa</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Treasury -->
<div class="modal fade" id="treasuryModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content bg-dark border-secondary text-white">
            <form action="{{ route('apexsions-bridge.admin.kingdoms.adjust-treasury') }}" method="POST">
                @csrf
                <input type="hidden" id="modalTreasuryKey" name="kingdom_key" value="">
                <div class="modal-header border-secondary border-opacity-25">
                    <h5 class="modal-title fw-bold text-white"><i class="bi bi-cash-stack text-warning me-2"></i>Kelola Kas Kerajaan</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Aksi Perbendaharaan</label>
                        <select name="action" class="form-select bg-secondary bg-opacity-25 border-secondary text-white">
                            <option value="add">Tambah Saldo (+)</option>
                            <option value="subtract">Kurangi Saldo (-)</option>
                            <option value="set">Atur Nominal Persis (=)</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Jumlah Nominal (Rupiah)</label>
                        <input type="number" name="amount" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="500000" min="0" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-white-50">Alasan</label>
                        <input type="text" name="reason" class="form-control bg-secondary bg-opacity-25 border-secondary text-white" placeholder="Suntikan dana ekspansi kerajaan">
                    </div>
                </div>
                <div class="modal-footer border-secondary border-opacity-25">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Batal</button>
                    <button type="submit" class="btn btn-warning btn-sm fw-bold text-dark">Simpan Perubahan</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const setKingModal = document.getElementById('setKingModal');
    if (setKingModal) {
        setKingModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const kingdom = button.getAttribute('data-kingdom');
            document.getElementById('modalKingdomTarget').value = kingdom;
        });
    }

    const treasuryModal = document.getElementById('treasuryModal');
    if (treasuryModal) {
        treasuryModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const kingdomKey = button.getAttribute('data-kingdom');
            document.getElementById('modalTreasuryKey').value = kingdomKey;
        });
    }
});
</script>
@endsection
