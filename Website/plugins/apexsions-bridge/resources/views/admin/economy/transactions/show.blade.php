@extends('admin.layouts.admin')

@section('title', 'Detail Transaksi #' . substr($transaction->transaction_id, 0, 8) . ' — Apexsions')

@section('content')
<div class="container-fluid px-4 py-3">
    <!-- Breadcrumb & Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1 small">
                    <li class="breadcrumb-item"><a href="{{ route('apexsions-bridge.admin.economy.index') }}" class="text-warning text-decoration-none">Economy</a></li>
                    <li class="breadcrumb-item"><a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" class="text-warning text-decoration-none">Transactions</a></li>
                    <li class="breadcrumb-item active text-white-50" aria-current="page">#{{ substr($transaction->transaction_id, 0, 8) }}</li>
                </ol>
            </nav>
            <h2 class="h4 fw-bold text-white mb-0 font-monospace">
                <i class="bi bi-receipt text-warning me-2"></i>Transaksi {{ $transaction->transaction_id }}
            </h2>
        </div>
        <div>
            <a href="{{ route('apexsions-bridge.admin.economy.transactions.index') }}" class="btn btn-outline-secondary btn-sm shadow-sm">
                <i class="bi bi-arrow-left me-1"></i>Kembali
            </a>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <!-- Financial Information -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3 d-flex justify-content-between align-items-center">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-cash-coin text-warning me-2"></i>Informasi Keuangan
                    </h5>
                    @if($transaction->status === 'COMPLETED')
                        <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">COMPLETED</span>
                    @elseif($transaction->status === 'PENDING')
                        <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25">PENDING</span>
                    @else
                        <span class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25">{{ $transaction->status }}</span>
                    @endif
                </div>
                <div class="card-body p-4">
                    <div class="row g-3 mb-4">
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Tipe Mutasi</span>
                            <span class="badge bg-secondary bg-opacity-50 text-white font-monospace">{{ $transaction->type }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Mata Uang</span>
                            <span class="text-uppercase fw-bold text-white font-monospace">{{ $transaction->currency }}</span>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Jumlah Kotor (Gross)</span>
                            <h4 class="fw-bold text-warning mb-0">
                                {{ $transaction->currency === 'diamond' ? number_format($transaction->amount, 0) . ' 💎' : 'Rp ' . number_format($transaction->amount, 0, ',', '.') }}
                            </h4>
                        </div>
                        <div class="col-sm-6">
                            <span class="text-white-50 small d-block mb-1">Potongan Pajak Kerajaan</span>
                            <h5 class="fw-bold text-white-50 mb-0 font-monospace">
                                {{ $transaction->tax_amount > 0 ? 'Rp ' . number_format($transaction->tax_amount, 0, ',', '.') : 'Rp 0' }}
                            </h5>
                        </div>
                        <div class="col-12">
                            <span class="text-white-50 small d-block mb-1">Jumlah Bersih Diterima (Net)</span>
                            <h4 class="fw-bold text-success mb-0">
                                {{ $transaction->currency === 'diamond' ? number_format($transaction->net_amount, 0) . ' 💎' : 'Rp ' . number_format($transaction->net_amount, 0, ',', '.') }}
                            </h4>
                        </div>
                    </div>

                    @if($transaction->reason)
                        <div class="p-3 bg-black bg-opacity-25 rounded border border-secondary border-opacity-25">
                            <span class="text-white-50 small d-block fw-bold mb-1">Alasan / Catatan Transaksi:</span>
                            <p class="text-white small mb-0">{{ $transaction->reason }}</p>
                        </div>
                    @endif
                </div>
            </div>
        </div>

        <!-- Parties Context -->
        <div class="col-lg-6">
            <div class="card bg-dark border-secondary border-opacity-25 shadow-sm h-100">
                <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
                    <h5 class="card-title text-white h6 mb-0 fw-bold">
                        <i class="bi bi-people text-warning me-2"></i>Pihak Terlibat
                    </h5>
                </div>
                <div class="card-body p-4">
                    <!-- Sender -->
                    <div class="p-3 bg-black bg-opacity-25 rounded border border-secondary border-opacity-25 mb-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="badge bg-secondary bg-opacity-50 text-white-50 mb-1">SENDER (PENGIRIM)</span>
                                @if($transaction->sender_name)
                                    <h5 class="h6 text-white mb-1 fw-bold">{{ $transaction->sender_name }}</h5>
                                    <code class="text-white-50 small d-block">{{ $transaction->sender_uuid ?? '-' }}</code>
                                @else
                                    <h5 class="h6 text-warning mb-0 fw-bold">SYSTEM</h5>
                                @endif
                            </div>
                            @if($transaction->sender_name)
                                <a href="{{ route('apexsions-bridge.admin.players.show', $transaction->sender_name) }}" class="btn btn-outline-warning btn-sm">
                                    <i class="bi bi-person-badge me-1"></i>Profil 360°
                                </a>
                            @endif
                        </div>
                    </div>

                    <!-- Receiver -->
                    <div class="p-3 bg-black bg-opacity-25 rounded border border-secondary border-opacity-25">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="badge bg-secondary bg-opacity-50 text-white-50 mb-1">RECEIVER (PENERIMA)</span>
                                @if($transaction->receiver_name)
                                    <h5 class="h6 text-white mb-1 fw-bold">{{ $transaction->receiver_name }}</h5>
                                    <code class="text-white-50 small d-block">{{ $transaction->receiver_uuid ?? '-' }}</code>
                                @else
                                    <h5 class="h6 text-warning mb-0 fw-bold">SYSTEM</h5>
                                @endif
                            </div>
                            @if($transaction->receiver_name)
                                <a href="{{ route('apexsions-bridge.admin.players.show', $transaction->receiver_name) }}" class="btn btn-outline-success btn-sm">
                                    <i class="bi bi-person-badge me-1"></i>Profil 360°
                                </a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Trace & Audit Information -->
    <div class="card bg-dark border-secondary border-opacity-25 shadow-sm">
        <div class="card-header bg-black bg-opacity-25 border-bottom border-secondary border-opacity-25 py-3">
            <h5 class="card-title text-white h6 mb-0 fw-bold">
                <i class="bi bi-diagram-3 text-warning me-2"></i>Jejak Alur Transaksi & Audit Trail
            </h5>
        </div>
        <div class="card-body p-4">
            <div class="row g-4">
                <div class="col-md-4 border-end border-secondary border-opacity-25">
                    <div class="d-flex gap-3">
                        <div class="text-warning fs-3">
                            <i class="bi bi-1-circle-fill"></i>
                        </div>
                        <div>
                            <span class="text-white fw-bold d-block">Inisiasi Transaksi</span>
                            <span class="text-white-50 small d-block font-monospace">{{ $transaction->created_at->format('Y-m-d H:i:s') }}</span>
                            <span class="badge bg-secondary bg-opacity-25 text-white-50 mt-1">Source: {{ $transaction->source }}</span>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 border-end border-secondary border-opacity-25">
                    <div class="d-flex gap-3">
                        <div class="text-warning fs-3">
                            <i class="bi bi-2-circle-fill"></i>
                        </div>
                        <div>
                            <span class="text-white fw-bold d-block">Identifikasi Aksi & Bridge</span>
                            <span class="text-white-50 small d-block font-monospace">Action ID: {{ $transaction->action_id ?? 'None (In-Game Direct)' }}</span>
                            @if($transaction->action_id)
                                <span class="badge bg-success bg-opacity-25 text-success mt-1">Idempotent</span>
                            @endif
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="d-flex gap-3">
                        <div class="text-success fs-3">
                            <i class="bi bi-3-circle-fill"></i>
                        </div>
                        <div>
                            <span class="text-white fw-bold d-block">Status Penyelesaian</span>
                            <span class="text-success small d-block fw-bold font-monospace">{{ $transaction->status }}</span>
                            <span class="text-white-50 small d-block">Ledger updated successfully</span>
                        </div>
                    </div>
                </div>
            </div>

            @if($transaction->auditLog)
                <div class="mt-4 pt-3 border-top border-secondary border-opacity-25">
                    <h6 class="text-warning small fw-bold text-uppercase mb-2">Terkait Unified Audit Log:</h6>
                    <div class="bg-black bg-opacity-25 p-3 rounded border border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                        <div>
                            <span class="text-white fw-medium">Aksi: <code>{{ $transaction->auditLog->action }}</code></span>
                            <span class="text-white-50 small ms-3">Staf: <strong>{{ $transaction->auditLog->actor_name }}</strong></span>
                        </div>
                        <a href="{{ route('apexsions-bridge.admin.audit-logs.show', $transaction->auditLog->id) }}" class="btn btn-outline-secondary btn-sm py-0 px-2" style="font-size: 0.75rem;">
                            Buka Audit #{{ $transaction->auditLog->id }}
                        </a>
                    </div>
                </div>
            @endif
        </div>
    </div>
</div>
@endsection
