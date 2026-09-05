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
                            $defaultImage = theme_asset('img/logo.jpg');
                        } elseif (str_contains($packageName, 'pass')) {
                            $fallbackIcon = 'bi bi-trophy-fill';
                        } elseif (str_contains($packageName, 'booster')) {
                            $fallbackIcon = 'bi bi-lightning-charge-fill';
                        }
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
                                <ul class="list-unstyled small mb-4 text-muted" style="line-height: 1.8;">
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

                                <div class="apx-package-footer mt-auto">
                                    <a href="#" class="btn btn-apx-gold w-100 py-2" data-package-url="{{ route('shop.packages.show', $package) }}">
                                        <i class="bi bi-bag-check-fill me-1"></i> {{ trans('shop::messages.buy') }}
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
