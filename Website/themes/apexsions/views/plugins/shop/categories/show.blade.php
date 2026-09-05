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
    <!-- Store Header Banner -->
    <div class="apx-store-hero">
        <div class="d-inline-flex align-items-center gap-2 px-3 py-1 mb-3 rounded-pill" style="background: rgba(245, 158, 11, 0.12); border: 1px solid var(--apx-gold-border); color: var(--apx-gold-light); font-size: 0.85rem; font-weight: 700;">
            <i class="bi bi-shield-check"></i> TRANSAKSI RESMI &amp; OTOMATIS
        </div>
        <h1>{{ $category->name }}</h1>
        <p>Tingkatkan kasta dan kejayaan peradabanmu di realm Apexsions dengan benefit eksklusif dan kasta permanen.</p>
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
                        if (str_contains($packageName, 'ascendant')) {
                            $defaultImage = theme_asset('img/package-ascendant.jpg');
                        } elseif (str_contains($packageName, 'archon')) {
                            $defaultImage = theme_asset('img/package-archon.jpg');
                        } elseif (str_contains($packageName, 'sovereign')) {
                            $defaultImage = theme_asset('img/package-sovereign.jpg');
                        }
                    @endphp

                    <div class="col-md-6 col-xl-4">
                        <div class="apx-package-card">
                            <span class="apx-package-badge">
                                <i class="bi bi-patch-check-fill text-warning me-1"></i> PERMANENT
                            </span>

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
                                        <i class="bi bi-gem"></i>
                                    </div>
                                </div>
                            @endif

                            <div class="apx-package-body">
                                <h3 class="apx-package-title">{{ $package->name }}</h3>

                                <div class="apx-package-price-wrap">
                                    @if($package->isDiscounted())
                                        <span class="apx-package-price-del">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                                    @endif
                                    <span class="apx-package-price">{{ shop_format_amount($package->getPrice()) }}</span>
                                </div>

                                <!-- Standardized High-Tier Perks List -->
                                <ul class="list-unstyled small mb-4 text-muted" style="line-height: 1.8;">
                                    <li class="d-flex align-items-center gap-2">
                                        <i class="bi bi-check2-circle text-warning"></i>
                                        <span class="text-light">Prefix Kasta Eksklusif Chat &amp; Tab</span>
                                    </li>
                                    <li class="d-flex align-items-center gap-2">
                                        <i class="bi bi-check2-circle text-warning"></i>
                                        <span class="text-light">Batas Klaim Wilayah &amp; Region Ekstra</span>
                                    </li>
                                    <li class="d-flex align-items-center gap-2">
                                        <i class="bi bi-check2-circle text-warning"></i>
                                        <span class="text-light">Slot Prioritas Masuk Saat Penuh</span>
                                    </li>
                                    <li class="d-flex align-items-center gap-2">
                                        <i class="bi bi-check2-circle text-warning"></i>
                                        <span class="text-light">Perintah Kosmetik &amp; Aura Kilau</span>
                                    </li>
                                </ul>

                                <div class="apx-package-footer">
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
