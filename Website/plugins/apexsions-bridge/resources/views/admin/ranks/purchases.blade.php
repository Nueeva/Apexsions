@extends('admin.layouts.admin')

@section('title', 'Riwayat Pembelian & Upgrade Rank')

@section('content')
<div class="mb-4">
    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <a href="{{ route('apexsions-bridge.admin.ranks.index') }}" class="btn btn-sm btn-outline-secondary">
                        <i class="bi bi-arrow-left me-1"></i> Kembali
                    </a>
                    <span class="badge bg-warning text-dark fw-bold px-2 py-1">TRANSACTION AUDIT</span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
                        📜 RIWAYAT PEMBELIAN & UPGRADE RANK
                    </h3>
                </div>
                <p class="text-muted small mb-0">
                    Audit lengkap seluruh transaksi rank pemain, status masa aktif trial, histori upgrade rank permanen, harga yang dibayar, serta sinkronisasi ke server Minecraft.
                </p>
            </div>
            <div class="d-flex gap-2">
                <a href="{{ route('apexsions-bridge.admin.ranks.settings') }}" class="btn btn-outline-info fw-bold px-3">
                    <i class="bi bi-gear-fill me-1"></i> Pengaturan
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Filter Bar -->
<div class="card p-3 mb-4">
    <form action="{{ route('apexsions-bridge.admin.ranks.purchases') }}" method="GET" class="row g-2 align-items-center">
        <div class="col-md-3">
            <input type="text" name="search" class="form-control" placeholder="Cari nama pemain / UUID..." value="{{ request('search') }}">
        </div>
        <div class="col-md-2">
            <select name="rank" class="form-select">
                <option value="">-- Semua Rank --</option>
                @foreach($ranks as $k => $r)
                    <option value="{{ $k }}" {{ request('rank') === $k ? 'selected' : '' }}>{{ $r['display_name'] }}</option>
                @endforeach
            </select>
        </div>
        <div class="col-md-2">
            <select name="rank_type" class="form-select">
                <option value="">-- Semua Tipe --</option>
                <option value="PERMANENT" {{ request('rank_type') === 'PERMANENT' ? 'selected' : '' }}>Permanen</option>
                <option value="TRIAL" {{ request('rank_type') === 'TRIAL' ? 'selected' : '' }}>Trial</option>
            </select>
        </div>
        <div class="col-md-2">
            <select name="is_upgrade" class="form-select">
                <option value="">-- Semua Transaksi --</option>
                <option value="1" {{ request('is_upgrade') === '1' ? 'selected' : '' }}>Khusus Upgrade</option>
                <option value="0" {{ request('is_upgrade') === '0' ? 'selected' : '' }}>Beli Baru / Admin</option>
            </select>
        </div>
        <div class="col-md-3 d-flex gap-2">
            <button type="submit" class="btn btn-primary w-100"><i class="bi bi-filter me-1"></i> Filter</button>
            @if(request()->anyFilled(['search', 'rank', 'rank_type', 'is_upgrade']))
                <a href="{{ route('apexsions-bridge.admin.ranks.purchases') }}" class="btn btn-outline-secondary"><i class="bi bi-x-circle"></i></a>
            @endif
        </div>
    </form>
</div>

<!-- Tabel Transaksi -->
<div class="card overflow-hidden">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th>ID</th>
                    <th>Pemain</th>
                    <th>Rank & Tipe</th>
                    <th>Detail Transaksi</th>
                    <th>Harga & Pembayaran</th>
                    <th>Periode Aktif</th>
                    <th>Status & Sync</th>
                </tr>
            </thead>
            <tbody>
                @forelse($purchases as $p)
                <tr>
                    <td class="text-muted small">#{{ $p->id }}</td>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <img src="https://mc-heads.net/avatar/{{ $p->minecraft_uuid }}/28" class="rounded" alt="" style="width: 28px; height: 28px;">
                            <div>
                                <div class="fw-bold text-white">{{ $p->minecraft_username }}</div>
                                <code class="text-muted" style="font-size: 0.7rem;">{{ substr($p->minecraft_uuid, 0, 18) }}...</code>
                            </div>
                        </div>
                    </td>
                    <td>
                        @php
                            $rankMeta = $ranks[strtolower($p->rank)] ?? null;
                            $color = $rankMeta['color'] ?? '#ffd700';
                        @endphp
                        <span class="badge" style="background-color: {{ $color }}; color: #000; font-weight: bold;">
                            {{ $rankMeta['display_name'] ?? ucfirst($p->rank) }}
                        </span>
                        <div class="mt-1">
                            @if($p->is_upgrade)
                                <span class="badge bg-purple text-white" style="background-color: #8E2DE2; font-size: 0.68rem;">
                                    <i class="bi bi-arrow-up-circle me-1"></i>UPGRADE
                                </span>
                            @elseif($p->rank_type === 'PERMANENT')
                                <span class="badge bg-success text-white" style="font-size: 0.68rem;">PERMANEN</span>
                            @else
                                <span class="badge bg-info text-dark" style="font-size: 0.68rem;">TRIAL ({{ $p->duration_days ?? 30 }}h)</span>
                            @endif
                        </div>
                    </td>
                    <td>
                        @if($p->is_upgrade && $p->previous_rank)
                            <div class="small">
                                <span class="text-muted">{{ ucfirst($p->previous_rank) }}</span>
                                <i class="bi bi-arrow-right mx-1 text-warning"></i>
                                <span class="fw-bold text-warning">{{ ucfirst($p->rank) }}</span>
                            </div>
                        @else
                            <div class="small text-muted">{{ $p->notes ?? 'Pembelian Normal' }}</div>
                        @endif
                        <small class="text-muted d-block" style="font-size: 0.7rem;">Sumber: {{ $p->source }}</small>
                    </td>
                    <td>
                        <div class="fw-bold text-success">Rp {{ number_format($p->price_paid, 0, ',', '.') }}</div>
                        @if($p->normal_price && $p->normal_price > $p->price_paid)
                            <div class="text-muted text-decoration-line-through small" style="font-size: 0.72rem;">
                                Rp {{ number_format($p->normal_price, 0, ',', '.') }}
                            </div>
                        @endif
                        <small class="badge bg-dark text-muted" style="font-size: 0.65rem;">{{ $p->payment_method }}</small>
                    </td>
                    <td class="small">
                        <div>{{ $p->started_at ? $p->started_at->format('d M Y H:i') : '-' }}</div>
                        @if($p->rank_type === 'TRIAL' && $p->expires_at)
                            <div class="text-{{ $p->expires_at->isPast() ? 'danger' : 'info' }}" style="font-size: 0.75rem;">
                                Exp: {{ $p->expires_at->format('d M Y H:i') }}
                                @if($p->expires_at->isPast()) (Expired) @endif
                            </div>
                        @else
                            <div class="text-muted" style="font-size: 0.75rem;">Tidak Pernah Expire</div>
                        @endif
                    </td>
                    <td>
                        @if($p->status === 'ACTIVE')
                            <span class="badge bg-success">ACTIVE</span>
                        @elseif($p->status === 'EXPIRED')
                            <span class="badge bg-secondary">EXPIRED</span>
                        @else
                            <span class="badge bg-warning text-dark">{{ $p->status }}</span>
                        @endif

                        <div class="mt-1">
                            @if($p->sync_status === 'DELIVERED')
                                <span class="badge bg-dark text-success" style="font-size: 0.65rem;"><i class="bi bi-check2 me-1"></i>SYNCED</span>
                            @elseif($p->sync_status === 'PENDING')
                                <span class="badge bg-dark text-warning" style="font-size: 0.65rem;"><i class="bi bi-clock me-1"></i>PENDING</span>
                            @else
                                <span class="badge bg-dark text-danger" style="font-size: 0.65rem;">{{ $p->sync_status }}</span>
                            @endif
                        </div>
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="7" class="text-center py-4 text-muted">
                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                        Belum ada riwayat transaksi rank yang tercatat.
                    </td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>

    @if($purchases->hasPages())
    <div class="card-footer bg-transparent border-top">
        {{ $purchases->links() }}
    </div>
    @endif
</div>
@endsection
