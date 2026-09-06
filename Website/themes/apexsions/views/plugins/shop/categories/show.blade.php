@extends('layouts.app')

@section('title', $category->name . ' | Webstore Apexsions')

@push('footer-scripts')
    <script>
        document.querySelectorAll('[data-package-url]').forEach(function (el) {
            el.addEventListener('click', function (ev) {
                ev.preventDefault();

                axios.get(el.dataset['packageUrl']).then(function (response) {
                    const itemModal = document.getElementById('itemModal');
                    itemModal.innerHTML = response.data;
                    new bootstrap.Modal(itemModal).show();
                }).catch(function (error) {
                    if (typeof createAlert === 'function') {
                        createAlert('danger', error, true);
                    } else {
                        alert(error);
                    }
                });
            });
        });
    </script>
@endpush

@section('content')
    <div class="apx-store-hero">
        <div class="apx-section-kicker mb-3">
            <i class="bi bi-shield-check"></i> TRANSAKSI RESMI PERADABAN
        </div>
        <h1>{{ $category->name }}</h1>
        <p>Tingkatkan kasta dan kedaulatan peradabanmu di realm Apexsions dengan mandat kasta dan perolehan sumber daya resmi.</p>
    </div>

    <!-- WhatsApp Direct Order Notice -->
    <div class="card mb-4" style="background: linear-gradient(135deg, rgba(34, 197, 94, 0.08) 0%, rgba(15, 23, 42, 0.9) 100%); border: 1px solid rgba(34, 197, 94, 0.35); border-radius: var(--apx-radius-md);">
        <div class="card-body p-3 p-md-4 d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center justify-content-center flex-shrink-0" style="width: 46px; height: 46px; border-radius: 50%; background: rgba(34, 197, 94, 0.2); color: #4ade80; font-size: 1.5rem; border: 1px solid rgba(34, 197, 94, 0.4);">
                    <i class="bi bi-whatsapp"></i>
                </div>
                <div>
                    <div class="fw-bold text-white mb-1" style="font-size: 1rem;">
                        <i class="bi bi-patch-check-fill text-success me-1"></i> Pemesanan Langsung via WhatsApp Founder
                    </div>
                    <div class="text-muted small" style="line-height: 1.5;">
                        Gerbang otomatis Midtrans sedang disiapkan. Transaksi saat ini diproses manual &amp; aman langsung oleh 3 Founder:
                        <strong class="text-white">Rifqi</strong>, <strong class="text-white">Friell</strong>, dan <strong class="text-white">Favian</strong>.
                    </div>
                </div>
            </div>
            <div class="d-flex align-items-center gap-2 flex-wrap">
                @foreach(config('services.whatsapp.admins', []) as $adm)
                    @php
                        $cleanPhone = preg_replace('/[^0-9]/', '', $adm['number']);
                    @endphp
                    <a href="https://wa.me/{{ $cleanPhone }}?text={{ rawurlencode('Halo Admin ' . $adm['name'] . ', saya ingin konsultasi seputar Webstore Apexsions.') }}" target="_blank" rel="noopener noreferrer" class="apx-shop-founder-pill">
                        <i class="bi bi-whatsapp text-success"></i>
                        <span>WA {{ $adm['name'] }}</span>
                    </a>
                @endforeach
            </div>
        </div>
    </div>

    <div class="row" id="shop">
        <!-- Sidebar Navigation & User Info -->
        <div class="col-lg-3 apx-shop-sidebar">
            @include('shop::categories._sidebar')
        </div>

        <!-- Package Grid Area -->
        <div class="col-lg-9">
            @if($category->description)
                <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
                    <div class="card-body">
                        {!! $category->description !!}
                    </div>
                </div>
            @endif

            <div class="row g-4">
                @forelse($category->packages as $package)
                    @php
                        $packageName = strtolower($package->name);
                        $defaultImage = null;
                        $fallbackIcon = 'bi bi-gem';
                        if (str_contains($packageName, 'ascendant')) {
                            $defaultImage = theme_asset('img/package-ascendant.jpg');
                        } elseif (str_contains($packageName, 'archon')) {
                            $defaultImage = theme_asset('img/package-archon.jpg');
                        } elseif (str_contains($packageName, 'sovereign')) {
                            $defaultImage = theme_asset('img/package-sovereign.jpg');
                        } elseif (str_contains($packageName, 'emperor')) {
                            $defaultImage = theme_asset('img/hero-warrior.jpg');
                        } elseif (str_contains($packageName, 'sions')) {
                            $defaultImage = theme_asset('img/logo.jpg') . '?v=' . (@filemtime(public_path('assets/themes/apexsions/img/logo.jpg')) ?: '2');
                        } elseif (str_contains($packageName, 'pass')) {
                            $fallbackIcon = 'bi bi-trophy-fill';
                        } elseif (str_contains($packageName, 'booster')) {
                            $fallbackIcon = 'bi bi-lightning-charge-fill';
                        }

                        $userIgn = (auth()->check() ? auth()->user()->name : null) ?? 'Username_Minecraft_Kamu';
                        $userEmail = auth()->check() ? auth()->user()->email : '-';
                        $priceFormatted = shop_format_amount($package->getPrice());

                        $waBaseText = "Halo Admin Apexsions! Saya ingin memesan paket dari Webstore resmi:\n\n"
                            . "👑 Paket: " . $package->name . "\n"
                            . "💰 Harga: " . $priceFormatted . "\n"
                            . "📂 Kategori: " . $category->name . "\n"
                            . "🎮 Akun Minecraft (IGN): " . $userIgn . "\n"
                            . "📧 Email Akun: " . $userEmail . "\n\n"
                            . "Mohon nomor rekening/QRIS dan instruksi aktivasi peradaban. Terima kasih!";

                        $admins = config('services.whatsapp.admins', [
                            ['name' => 'Rifqi', 'number' => '6285883161047', 'role' => 'Founder'],
                            ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
                            ['name' => 'Favian', 'number' => '6285883161047', 'role' => 'Founder'],
                        ]);
                        $primaryAdmin = $admins[0] ?? ['name' => 'Admin', 'number' => '6285883161047'];
                        $primaryCleanNum = preg_replace('/[^0-9]/', '', $primaryAdmin['number']);
                        $primaryWaUrl = 'https://wa.me/' . $primaryCleanNum . '?text=' . rawurlencode($waBaseText);
                    @endphp

                    <div class="col-md-6 col-xl-4">
                        <div class="apx-package-card h-100 d-flex flex-column">
                            @if(str_contains($packageName, 'sions'))
                                <span class="apx-package-badge" style="background: rgba(234, 179, 8, 0.25); color: #fde047; border: 1px solid #fde047;">
                                    <i class="bi bi-star-fill me-1"></i> APEX TIER
                                </span>
                            @elseif(str_contains($packageName, 'pass'))
                                <span class="apx-package-badge" style="background: rgba(59, 130, 246, 0.2); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.4);">
                                    <i class="bi bi-trophy-fill me-1"></i> SEASON PASS
                                </span>
                            @elseif(str_contains($packageName, 'booster'))
                                <span class="apx-package-badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4);">
                                    <i class="bi bi-lightning-fill me-1"></i> 3 HARI AKTIF
                                </span>
                            @else
                                <span class="apx-package-badge">
                                    <i class="bi bi-patch-check-fill text-warning me-1"></i> PERMANEN
                                </span>
                            @endif

                            @if($package->hasImage())
                                <div class="apx-package-image-wrap">
                                    <img class="apx-package-image" src="{{ $package->imageUrl() }}" alt="{{ $package->name }}">
                                </div>
                            @elseif($defaultImage)
                                <div class="apx-package-image-wrap p-2">
                                    <img class="apx-package-image rounded" src="{{ $defaultImage }}" alt="{{ $package->name }}" style="max-height: 140px; width: 100%; object-fit: cover;">
                                </div>
                            @else
                                <div class="apx-package-image-wrap">
                                    <div class="d-inline-flex align-items-center justify-content-center" style="width: 72px; height: 72px; border-radius: 12px; background: rgba(245, 158, 11, 0.12); border: 1px solid var(--apx-gold-border); color: var(--apx-gold); font-size: 2rem;">
                                        <i class="{{ $fallbackIcon }}"></i>
                                    </div>
                                </div>
                            @endif

                            <div class="apx-package-body d-flex flex-column flex-grow-1">
                                <h3 class="apx-package-title">{{ $package->name }}</h3>

                                <div class="apx-package-price-wrap">
                                    @if($package->isDiscounted())
                                        <span class="apx-package-price-del">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                                    @endif
                                    <span class="apx-package-price">{{ shop_format_amount($package->getPrice()) }}</span>
                                </div>

                                @if($package->short_description)
                                    <p class="text-muted small mb-3 flex-grow-1" style="line-height: 1.6;">
                                        {{ $package->short_description }}
                                    </p>
                                @endif

                                <!-- Key Highlights -->
                                <ul class="list-unstyled small mb-3 text-muted" style="line-height: 1.8;">
                                    @if(str_contains($packageName, 'sions'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Prefix Mahkota ✦ SIONS ✦</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Seluruh Kit + Kit Sions Eksklusif</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Pesan Broadcast Masuk Server Megah</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+15 Batas Klaim Wilayah Kerajaan</span></li>
                                    @elseif(str_contains($packageName, 'emperor'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Prefix Kaisar [⚔ EMPEROR]</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Kit Bulanan Gear Set Bonus Lengkap</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Hak Terbang /fly di Ibukota &amp; Claim</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+10 Batas Klaim Wilayah Kerajaan</span></li>
                                    @elseif(str_contains($packageName, 'sovereign'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Prefix Emas [⚜ SOVEREIGN]</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Kit Sovereign 14 Harian &amp; Sayap Partikel</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Bebas Tarif Dagang Lintas Kerajaan</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+7 Batas Klaim Wilayah Kerajaan</span></li>
                                    @elseif(str_contains($packageName, 'archon'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Prefix Cyan [💎 ARCHON]</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Kit Mingguan &amp; Kosmetik Glow Kristal</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Akses /ec, /anvil, /smithing Portable</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+4 Batas Klaim Wilayah Kerajaan</span></li>
                                    @elseif(str_contains($packageName, 'ascendant'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Prefix Zamrud [☘ ASCENDANT]</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Kit Ascendant Harian (/kit ascendant)</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Bypass Antrean Saat Server Penuh</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+2 Batas Klaim Wilayah Kerajaan</span></li>
                                    @elseif(str_contains($packageName, 'vip pass'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Akses Penuh 100 Level Jalur Hadiah</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Instan Skip Langsung 20 Level Awal</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Kosmetik Jubah &amp; Gelar Chat Eksklusif</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Bonus Rp 50.000 + 5x Magic Dust</span></li>
                                    @elseif(str_contains($packageName, 'premium pass'))
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Buka Jalur Emas 100 Level Hadiah</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Akses Quests Harian &amp; Mingguan</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">+25% Pengganda Perolehan EXP Pass</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Diskon Toko Berputar /abp shop</span></li>
                                    @else
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Aktivasi Otomatis via Akun Minecraft</span></li>
                                        <li class="d-flex align-items-center gap-2"><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Dukungan Transaksi Aman &amp; Terverifikasi</span></li>
                                    @endif
                                </ul>

                                <!-- Founder Selection Pills -->
                                <div class="mb-3 pt-2 border-top border-secondary border-opacity-15">
                                    <div class="small text-muted mb-2 d-flex align-items-center justify-content-between" style="font-size: 0.76rem;">
                                        <span><i class="bi bi-whatsapp text-success me-1"></i> Pilih Founder:</span>
                                        <span class="text-dim">Pesan Langsung</span>
                                    </div>
                                    <div class="d-flex flex-wrap gap-1">
                                        @foreach($admins as $adm)
                                            @php
                                                $admNum = preg_replace('/[^0-9]/', '', $adm['number']);
                                                $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($waBaseText);
                                            @endphp
                                            <a href="{{ $admUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-success py-1 px-2 d-flex align-items-center gap-1" style="font-size: 0.75rem;" title="Pesan paket ini via Founder {{ $adm['name'] }}">
                                                <i class="bi bi-whatsapp"></i> {{ $adm['name'] }}
                                            </a>
                                        @endforeach
                                    </div>
                                </div>

                                <div class="apx-package-footer mt-auto d-flex flex-column gap-2">
                                    <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2">
                                        <i class="bi bi-whatsapp me-1"></i> Pesan via WhatsApp
                                    </a>
                                    <a href="#" class="btn btn-apx-outline w-100 py-1 small" data-package-url="{{ route('shop.packages.show', $package) }}">
                                        <i class="bi bi-info-circle me-1"></i> Rincian &amp; Benefit
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                @empty
                    <div class="col-12">
                        <div class="alert alert-warning d-flex align-items-center gap-2" role="alert">
                            <i class="bi bi-exclamation-triangle-fill fs-5"></i>
                            <div>{{ trans('shop::messages.categories.empty') }}</div>
                        </div>
                    </div>
                @endforelse
            </div>
        </div>
    </div>

    <!-- Package Detail / Purchase Modal -->
    <div class="modal fade" id="itemModal" tabindex="-1" role="dialog" aria-labelledby="itemModalLabel" aria-hidden="true"></div>
@endsection
