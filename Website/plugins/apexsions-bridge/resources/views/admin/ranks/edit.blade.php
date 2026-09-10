@extends('admin.layouts.admin')

@section('title', 'Edit Rank - ' . $config->display_name)

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
                    <span class="badge" style="background-color: {{ $config->color }}; color: #000; font-weight: bold;">
                        {{ $config->badge ?? $config->display_name }}
                    </span>
                    <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1px;">
                        EDIT KONFIGURASI RANK: {{ strtoupper($config->display_name) }}
                    </h3>
                </div>
                <p class="text-muted small mb-0">
                    Atur harga normal, harga upgrade, benefit Minecraft (Homes, Auctions, Enchants, RTP, Sell Bonus, XP Bonus, Nickname, Kits), dan status webstore.
                </p>
            </div>
            <div>
                <span class="badge bg-secondary px-3 py-2">Rank Key: <code>{{ $config->rank_key }}</code></span>
            </div>
        </div>
    </div>
</div>

<form action="{{ route('apexsions-bridge.admin.ranks.update', $config->rank_key) }}" method="POST">
    @csrf
    @method('PUT')

    <div class="row g-4">
        <!-- Kolom Kiri: Metadata & Pricing -->
        <div class="col-lg-6">
            <!-- Informasi Umum -->
            <div class="card p-4 mb-4">
                <h5 class="fw-bold mb-3 text-warning"><i class="bi bi-info-circle me-2"></i>Informasi Umum & Visual</h5>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Nama Tampilan <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['display_name'] ?? '-' }}</span></label>
                        <input type="text" name="display_name" class="form-control" value="{{ old('display_name', $config->display_name ?? ($defaults['display_name'] ?? '')) }}" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Badge Text <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['badge'] ?? '-' }}</span></label>
                        <input type="text" name="badge" class="form-control" value="{{ old('badge', $config->badge ?? ($defaults['badge'] ?? '')) }}">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Prefix In-Game <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['prefix'] ?? '-' }}</span></label>
                        <input type="text" name="prefix" class="form-control" value="{{ old('prefix', $config->prefix ?? ($defaults['prefix'] ?? '')) }}">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Warna Aksen (Hex) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['color'] ?? '#ffd700' }}</span></label>
                        <div class="input-group">
                            <input type="color" class="form-control form-control-color" value="{{ old('color', $config->color ?? ($defaults['color'] ?? '#ffd700')) }}" onchange="document.getElementById('hexColor').value = this.value">
                            <input type="text" name="color" id="hexColor" class="form-control" value="{{ old('color', $config->color ?? ($defaults['color'] ?? '#ffd700')) }}" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Tier <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['tier'] ?? 'Tier II' }}</span></label>
                        <input type="text" name="tier" class="form-control" value="{{ old('tier', $config->tier ?? ($defaults['tier'] ?? 'Tier II')) }}" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Bobot Hierarki (Weight) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['weight'] ?? 10 }}</span></label>
                        <input type="number" name="weight" class="form-control" value="{{ old('weight', $config->weight ?? ($defaults['weight'] ?? 10)) }}" min="0" max="100" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Urutan Tampilan <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['order_index'] ?? 0 }}</span></label>
                        <input type="number" name="order_index" class="form-control" value="{{ old('order_index', $config->order_index ?? ($defaults['order_index'] ?? 0)) }}" min="0" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label small fw-bold">Banner / Image Path <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['banner_image'] ?? 'assets/themes/apexsions/img/package-ascendant.jpg' }}</span></label>
                        <input type="text" name="banner_image" class="form-control" value="{{ old('banner_image', $config->banner_image ?? ($defaults['banner_image'] ?? 'assets/themes/apexsions/img/package-ascendant.jpg')) }}" placeholder="assets/themes/apexsions/img/package-ascendant.jpg">
                    </div>
                    <div class="col-12">
                        <label class="form-label small fw-bold">Deskripsi Rank</label>
                        <textarea name="description" class="form-control" rows="3">{{ old('description', $config->description ?? ($defaults['description'] ?? '')) }}</textarea>
                    </div>
                    <div class="col-md-6">
                        <div class="form-check form-switch mt-2">
                            <input class="form-check-input" type="checkbox" name="is_active" id="isActiveSwitch" value="1" {{ old('is_active', $config->is_active ?? ($defaults['is_active'] ?? true)) ? 'checked' : '' }}>
                            <label class="form-check-label fw-bold" for="isActiveSwitch">Rank Aktif</label>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-check form-switch mt-2">
                            <input class="form-check-input" type="checkbox" name="is_buyable" id="isBuyableSwitch" value="1" {{ old('is_buyable', $config->is_buyable ?? ($defaults['is_buyable'] ?? false)) ? 'checked' : '' }}>
                            <label class="form-check-label fw-bold" for="isBuyableSwitch">Dapat Dibeli di Webstore</label>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Pricing & Rewards -->
            <div class="card p-4">
                <h5 class="fw-bold mb-3 text-warning"><i class="bi bi-tag-fill me-2"></i>Harga Webstore & Hadiah Uang</h5>

                <div class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Harga Trial (30 Hari) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: Rp {{ number_format($defaults['price_trial_30'] ?? 0, 0, ',', '.') }}</span></label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="price_trial_30" class="form-control" value="{{ old('price_trial_30', $config->price_trial_30 ?? ($defaults['price_trial_30'] ?? 0)) }}" min="0" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Harga Trial (90 Hari) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: Rp {{ number_format($defaults['price_trial_90'] ?? 0, 0, ',', '.') }}</span></label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="price_trial_90" class="form-control" value="{{ old('price_trial_90', $config->price_trial_90 ?? ($defaults['price_trial_90'] ?? 0)) }}" min="0" required>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Harga Permanen <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: Rp {{ number_format($defaults['price_permanent'] ?? 0, 0, ',', '.') }}</span></label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="price_permanent" class="form-control" value="{{ old('price_permanent', $config->price_permanent ?? ($defaults['price_permanent'] ?? 0)) }}" min="0" required>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Custom Upgrade Price Override (Opsional)</label>
                        <div class="input-group">
                            <span class="input-group-text">Rp</span>
                            <input type="number" name="price_upgrade_override" class="form-control" value="{{ old('price_upgrade_override', $config->price_upgrade_override ?? ($defaults['price_upgrade_override'] ?? '')) }}" min="0" placeholder="Kosongkan untuk selisih otomatis">
                        </div>
                        <small class="text-muted" style="font-size: 0.75rem;">Jika dikosongkan, harga upgrade dihitung dari selisih harga permanen.</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Diskon Promo (%) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['discount_percent'] ?? 0 }}%</span></label>
                        <div class="input-group">
                            <input type="number" name="discount_percent" class="form-control" value="{{ old('discount_percent', $config->discount_percent ?? ($defaults['discount_percent'] ?? 0)) }}" min="0" max="100">
                            <span class="input-group-text">%</span>
                        </div>
                    </div>

                    <div class="col-12">
                        <label class="form-label small fw-bold text-success">Bonus Saldo Uang Server (Permanen Satu Kali) <span class="badge bg-dark text-success border border-success border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: Rp {{ number_format($defaults['money_reward_permanent'] ?? 0, 0, ',', '.') }}</span></label>
                        <div class="input-group">
                            <span class="input-group-text text-success fw-bold">Rp</span>
                            <input type="number" name="money_reward_permanent" class="form-control fw-bold text-success" value="{{ old('money_reward_permanent', $config->money_reward_permanent ?? ($defaults['money_reward_permanent'] ?? 0)) }}" min="0" required>
                        </div>
                        <small class="text-muted" style="font-size: 0.75rem;">Hanya diberikan satu kali saat pembelian permanen pertama atau upgrade. Tidak dapat diduplikasi.</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- Kolom Kanan: Benefit Minecraft -->
        <div class="col-lg-6">
            <div class="card p-4 h-100">
                <h5 class="fw-bold mb-3 text-warning"><i class="bi bi-shield-shaded me-2"></i>Benefit & Batasan Minecraft</h5>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Maksimal Home (/sethome) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_max_homes'] ?? 2 }}</span></label>
                        <input type="number" name="benefit_max_homes" class="form-control" value="{{ old('benefit_max_homes', $config->benefit_max_homes ?? ($defaults['benefit_max_homes'] ?? 2)) }}" min="1" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Maksimal Listing Lelang (/lelang) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_max_auctions'] ?? 3 }}</span></label>
                        <input type="number" name="benefit_max_auctions" class="form-control" value="{{ old('benefit_max_auctions', $config->benefit_max_auctions ?? ($defaults['benefit_max_auctions'] ?? 3)) }}" min="1" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Batas Custom Enchant per Item <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_max_enchants'] ?? 4 }}</span></label>
                        <input type="number" name="benefit_max_enchants" class="form-control" value="{{ old('benefit_max_enchants', $config->benefit_max_enchants ?? ($defaults['benefit_max_enchants'] ?? 4)) }}" min="1" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Cooldown RTP (Detik) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_rtp_cooldown'] ?? 150 }} dtk</span></label>
                        <div class="input-group">
                            <input type="number" name="benefit_rtp_cooldown" class="form-control" value="{{ old('benefit_rtp_cooldown', $config->benefit_rtp_cooldown ?? ($defaults['benefit_rtp_cooldown'] ?? 150)) }}" min="5" required>
                            <span class="input-group-text">dtk</span>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Bonus Jual Shop (%) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: +{{ $defaults['benefit_shop_sell_bonus'] ?? 0 }}%</span></label>
                        <div class="input-group">
                            <input type="number" step="0.1" name="benefit_shop_sell_bonus" class="form-control" value="{{ old('benefit_shop_sell_bonus', $config->benefit_shop_sell_bonus ?? ($defaults['benefit_shop_sell_bonus'] ?? 0)) }}" min="0" required>
                            <span class="input-group-text">%</span>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Bonus XP Level (%) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: +{{ $defaults['benefit_xp_bonus'] ?? 0 }}%</span></label>
                        <div class="input-group">
                            <input type="number" step="0.1" name="benefit_xp_bonus" class="form-control" value="{{ old('benefit_xp_bonus', $config->benefit_xp_bonus ?? ($defaults['benefit_xp_bonus'] ?? 0)) }}" min="0" required>
                            <span class="input-group-text">%</span>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label small fw-bold">Multiplier Deposito Bank <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_bank_multiplier'] ?? 1.0 }}x</span></label>
                        <div class="input-group">
                            <input type="number" step="0.1" name="benefit_bank_multiplier" class="form-control" value="{{ old('benefit_bank_multiplier', $config->benefit_bank_multiplier ?? ($defaults['benefit_bank_multiplier'] ?? 1.0)) }}" min="1.0" required>
                            <span class="input-group-text">x</span>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Feed Cooldown (Detik / Opsional) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ isset($defaults['benefit_feed_cooldown']) ? $defaults['benefit_feed_cooldown'] . ' dtk' : '-' }}</span></label>
                        <input type="number" name="benefit_feed_cooldown" class="form-control" value="{{ old('benefit_feed_cooldown', $config->benefit_feed_cooldown ?? ($defaults['benefit_feed_cooldown'] ?? '')) }}" placeholder="{{ isset($defaults['benefit_feed_cooldown']) ? 'Default: ' . $defaults['benefit_feed_cooldown'] . ' dtk' : 'Contoh: 300 (5 menit)' }}">
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Hak Nickname (/nick) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_nick_permission'] ?? 'none' }}</span></label>
                        @php
                            $selectedNick = old('benefit_nick_permission', $config->benefit_nick_permission ?? ($defaults['benefit_nick_permission'] ?? 'none'));
                        @endphp
                        <select name="benefit_nick_permission" class="form-select">
                            <option value="none" {{ $selectedNick === 'none' ? 'selected' : '' }}>Tidak Ada / Default</option>
                            <option value="no_color" {{ $selectedNick === 'no_color' ? 'selected' : '' }}>Akses /nick (Tanpa Edit Warna)</option>
                            <option value="solid_color" {{ $selectedNick === 'solid_color' ? 'selected' : '' }}>Warna Solid Saja (Tanpa Gradient)</option>
                            <option value="all_colors" {{ $selectedNick === 'all_colors' ? 'selected' : '' }}>Seluruh Warna & Gradient Bebas</option>
                        </select>
                    </div>

                    <div class="col-12">
                        <label class="form-label small fw-bold">Perintah Khusus (Pisahkan dengan baris baru atau koma)
                            @if(!empty($defaults['benefit_commands']))
                                <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ implode(', ', $defaults['benefit_commands']) }}</span>
                            @endif
                        </label>
                        @php
                            $cmdsVal = old('benefit_commands_raw');
                            if (is_null($cmdsVal)) {
                                if (!empty($config->benefit_commands)) {
                                    $cmdsVal = is_array($config->benefit_commands) ? implode(', ', $config->benefit_commands) : $config->benefit_commands;
                                } elseif (!empty($defaults['benefit_commands'])) {
                                    $cmdsVal = implode(', ', $defaults['benefit_commands']);
                                }
                            }
                        @endphp
                        <textarea name="benefit_commands_raw" class="form-control" rows="2" placeholder="/craft, /anvil, /smithing, /repair, /feed, /hat, /enderchest">{{ $cmdsVal }}</textarea>
                    </div>

                    <div class="col-12">
                        <label class="form-label small fw-bold">Daftar Kit yang Terbuka
                            @if(!empty($defaults['benefit_kits']))
                                <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ implode(', ', $defaults['benefit_kits']) }}</span>
                            @endif
                        </label>
                        @php
                            $kitsVal = old('benefit_kits_raw');
                            if (is_null($kitsVal)) {
                                if (!empty($config->benefit_kits)) {
                                    $kitsVal = is_array($config->benefit_kits) ? implode(', ', $config->benefit_kits) : $config->benefit_kits;
                                } elseif (!empty($defaults['benefit_kits'])) {
                                    $kitsVal = implode(', ', $defaults['benefit_kits']);
                                }
                            }
                        @endphp
                        <textarea name="benefit_kits_raw" class="form-control" rows="2" placeholder="Ascendant Kit, Archon Kit, Sovereign Kit">{{ $kitsVal }}</textarea>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">BattlePass Season Ini Gratis <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ !empty($defaults['benefit_battlepass_unlock']) ? strtoupper($defaults['benefit_battlepass_unlock']) : 'Tidak Ada' }}</span></label>
                        @php
                            $bpUnlockVal = old('benefit_battlepass_unlock', $config->benefit_battlepass_unlock ?? ($defaults['benefit_battlepass_unlock'] ?? ''));
                        @endphp
                        <select name="benefit_battlepass_unlock" class="form-select">
                            <option value="" {{ empty($bpUnlockVal) ? 'selected' : '' }}>Tidak Ada</option>
                            <option value="sio" {{ $bpUnlockVal === 'sio' ? 'selected' : '' }}>Sio Pass (Emperor)</option>
                            <option value="exsio" {{ $bpUnlockVal === 'exsio' ? 'selected' : '' }}>Sio Pass + Exsio Pass (Sions / Ancestor)</option>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label small fw-bold">Diskon BattlePass Season Depan (%) <span class="badge bg-dark text-warning border border-warning border-opacity-25 ms-1 fw-normal" style="font-size: 0.72rem;">Default: {{ $defaults['benefit_battlepass_discount'] ?? 0 }}%</span></label>
                        <div class="input-group">
                            <input type="number" name="benefit_battlepass_discount" class="form-control" value="{{ old('benefit_battlepass_discount', $config->benefit_battlepass_discount ?? ($defaults['benefit_battlepass_discount'] ?? 0)) }}" min="0" max="100">
                            <span class="input-group-text">%</span>
                        </div>
                    </div>
                </div>

                <div class="mt-4 pt-3 border-top d-flex justify-content-end gap-2">
                    <a href="{{ route('apexsions-bridge.admin.ranks.index') }}" class="btn btn-secondary px-4">Batal</a>
                    <button type="submit" class="btn btn-warning fw-bold px-4">
                        <i class="bi bi-save me-1"></i> Simpan Perubahan
                    </button>
                </div>
            </div>
        </div>
    </div>
</form>
@endsection
