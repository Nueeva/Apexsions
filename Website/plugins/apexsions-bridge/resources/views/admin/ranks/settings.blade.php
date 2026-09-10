@extends('admin.layouts.admin')

@section('title', 'Pengaturan Webstore & WhatsApp')

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
                    <span class="badge bg-success fw-bold px-2 py-1">INTEGRATION</span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
                        ⚙ PENGATURAN WHATSAPP & BATTLEPASS
                    </h3>
                </div>
                <p class="text-muted small mb-0">
                    Konfigurasi nomor WhatsApp admin/founder penerima order, template pesan dinamis (Rank, Diskon BattlePass, dan Upgrade Rank), serta parameter global BattlePass.
                </p>
            </div>
            <div>
                <a href="{{ route('apexsions-bridge.admin.ranks.purchases') }}" class="btn btn-outline-warning fw-bold px-3">
                    <i class="bi bi-receipt me-1"></i> Riwayat Transaksi
                </a>
            </div>
        </div>
    </div>
</div>

<form action="{{ route('apexsions-bridge.admin.ranks.settings.update') }}" method="POST">
    @csrf

    <div class="row g-4">
        <!-- Pengaturan WhatsApp Order -->
        <div class="col-lg-6">
            <div class="card p-4 h-100">
                <h5 class="fw-bold mb-3 text-success">
                    <i class="bi bi-whatsapp me-2"></i>Pengaturan WhatsApp Order Flow
                </h5>

                <div class="mb-3">
                    <label class="form-label small fw-bold">Nomor WhatsApp Admin (Pisahkan koma jika lebih dari satu)</label>
                    <input type="text" name="whatsapp_numbers" class="form-control font-monospace" value="{{ old('whatsapp_numbers', $whatsapp_numbers) }}" required placeholder="6281212994597, 6285883161047">
                    <small class="text-muted" style="font-size: 0.75rem;">Format internasional tanpa tanda plus atau spasi (contoh: 6281212994597).</small>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold">Template Pesan Produk Standar</label>
                    <textarea name="template_standard" class="form-control" rows="2" required>{{ old('template_standard', $template_standard) }}</textarea>
                    <small class="text-muted" style="font-size: 0.75rem;">Placeholder tersedia: <code>{nama produk}</code>, <code>{harga produk}</code></small>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold">Template Pesan BattlePass (Dengan Diskon Rank)</label>
                    <textarea name="template_battlepass_discount" class="form-control" rows="3" required>{{ old('template_battlepass_discount', $template_battlepass_discount) }}</textarea>
                    <small class="text-muted" style="font-size: 0.75rem;">Placeholder tersedia: <code>{nama pass}</code>, <code>{harga produk}</code>, <code>{rank}</code></small>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-bold">Template Pesan Upgrade Rank</label>
                    <textarea name="template_upgrade" class="form-control" rows="3" required>{{ old('template_upgrade', $template_upgrade) }}</textarea>
                    <small class="text-muted" style="font-size: 0.75rem;">Placeholder tersedia: <code>{rank lama}</code>, <code>{rank baru}</code>, <code>{harga upgrade}</code></small>
                </div>
            </div>
        </div>

        <!-- Pengaturan Global BattlePass -->
        <div class="col-lg-6">
            <div class="card p-4 h-100">
                <h5 class="fw-bold mb-3 text-warning">
                    <i class="bi bi-trophy-fill me-2"></i>Konfigurasi Global BattlePass
                </h5>

                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Season Aktif</label>
                        <input type="text" name="battlepass_season" class="form-control fw-bold" value="{{ old('battlepass_season', $battlepass_season) }}" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Harga Sio Pass</label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="sio_price" class="form-control" value="{{ old('sio_price', $sio_price) }}" min="0" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Harga Exsio Pass</label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="exsio_price" class="form-control" value="{{ old('exsio_price', $exsio_price) }}" min="0" required>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Diskon Pemilik Emperor Permanent (%)</label>
                        <div class="input-group">
                            <input type="number" name="emperor_discount" class="form-control" value="{{ old('emperor_discount', $emperor_discount) }}" min="0" max="100" required>
                            <span class="input-group-text">%</span>
                        </div>
                        <small class="text-muted" style="font-size: 0.75rem;">Berlaku untuk pembelian Sio Pass season berikutnya.</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Diskon Pemilik Sions Permanent (%)</label>
                        <div class="input-group">
                            <input type="number" name="sions_discount" class="form-control" value="{{ old('sions_discount', $sions_discount) }}" min="0" max="100" required>
                            <span class="input-group-text">%</span>
                        </div>
                        <small class="text-muted" style="font-size: 0.75rem;">Berlaku untuk pembelian Sio Pass dan Exsio Pass season berikutnya.</small>
                    </div>

                    <div class="col-12 mt-4 pt-3 border-top">
                        <div class="alert alert-dark border-secondary">
                            <h6 class="fw-bold mb-1"><i class="bi bi-shield-check text-info me-2"></i>Keamanan Perhitungan Harga</h6>
                            <p class="small mb-0 text-muted">
                                Seluruh kalkulasi harga diskon BattlePass dihitung 100% pada layer server PHP melalui <code>BattlepassDiscountService</code>. Nilai dari frontend dan parameter URL tidak dipercaya oleh backend.
                            </p>
                        </div>
                    </div>
                </div>

                <div class="mt-auto pt-4 text-end">
                    <button type="submit" class="btn btn-warning fw-bold px-4 shadow-sm">
                        <i class="bi bi-check2-circle me-1"></i> Simpan Pengaturan
                    </button>
                </div>
            </div>
        </div>
    </div>
</form>
@endsection
