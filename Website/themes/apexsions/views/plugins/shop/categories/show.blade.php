@extends('layouts.app')

@section('title', $category->name . ' | Webstore Apexsions')
@section('description', 'Beli paket ' . $category->name . ' di Webstore Apexsions. Pembelian aman via WhatsApp, aktivasi otomatis instan di server Minecraft.')

@push('scripts')
    <script>
        (function() {
            function initShopCategoryInteractivity() {
                // 1. Package details modal click handler
                document.querySelectorAll('[data-package-url]').forEach(function (el) {
                    if (el.dataset.boundClick) return;
                    el.dataset.boundClick = 'true';
                    el.addEventListener('click', function (ev) {
                        ev.preventDefault();
                        const url = el.getAttribute('data-package-url');
                        if (!url) return;

                        if (typeof axios !== 'undefined') {
                            axios.get(url).then(function (response) {
                                const itemModal = document.getElementById('itemModal');
                                if (itemModal) {
                                    itemModal.innerHTML = response.data;
                                    if (typeof bootstrap !== 'undefined') {
                                        bootstrap.Modal.getOrCreateInstance(itemModal).show();
                                    }
                                }
                            }).catch(function (error) {
                                if (typeof createAlert === 'function') {
                                    createAlert('danger', error, true);
                                } else {
                                    alert('Gagal memuat rincian paket: ' + error);
                                }
                            });
                        }
                    });
                });

                // 2. Expandable perks toggle handled centrally in app.js

                // 3. Multi-Axis Interactive Filtering Engine
                const rankFilterBtns = document.querySelectorAll('.apx-filter-rank');
                const durationFilterBtns = document.querySelectorAll('.apx-filter-duration');
                const subcatFilterBtns = document.querySelectorAll('.apx-filter-subcat');
                const packageCols = document.querySelectorAll('.apx-package-col');
                const countSpan = document.getElementById('visiblePackageCount');
                const emptyState = document.getElementById('apxFilterEmptyState');

                let activeRank = 'all';
                let activeDuration = 'all';
                let activeSubcat = 'all';

                function applyFilters() {
                    let visibleCount = 0;

                    packageCols.forEach(function (col) {
                        const colRank = col.getAttribute('data-rank') || 'other';
                        const colDuration = col.getAttribute('data-duration') || 'other';
                        const colSubcat = col.getAttribute('data-subcat') || 'other';

                        const matchRank = (activeRank === 'all' || colRank === activeRank);
                        const matchDuration = (activeDuration === 'all' || colDuration === activeDuration);
                        const matchSubcat = (activeSubcat === 'all' || colSubcat === activeSubcat);

                        if (matchRank && matchDuration && matchSubcat) {
                            col.style.display = '';
                            visibleCount++;
                        } else {
                            col.style.display = 'none';
                        }
                    });

                    if (countSpan) {
                        countSpan.textContent = visibleCount;
                    }

                    if (emptyState) {
                        emptyState.style.display = visibleCount === 0 ? '' : 'none';
                    }
                }

                if (rankFilterBtns.length > 0) {
                    rankFilterBtns.forEach(function (btn) {
                        btn.addEventListener('click', function () {
                            rankFilterBtns.forEach(function (b) { b.classList.remove('active'); });
                            this.classList.add('active');
                            activeRank = this.getAttribute('data-rank-filter') || 'all';
                            applyFilters();
                        });
                    });
                }

                if (durationFilterBtns.length > 0) {
                    durationFilterBtns.forEach(function (btn) {
                        btn.addEventListener('click', function () {
                            durationFilterBtns.forEach(function (b) { b.classList.remove('active'); });
                            this.classList.add('active');
                            activeDuration = this.getAttribute('data-duration-filter') || 'all';
                            applyFilters();
                        });
                    });
                }

                if (subcatFilterBtns.length > 0) {
                    subcatFilterBtns.forEach(function (btn) {
                        btn.addEventListener('click', function () {
                            subcatFilterBtns.forEach(function (b) { b.classList.remove('active'); });
                            this.classList.add('active');
                            activeSubcat = this.getAttribute('data-subcat-filter') || 'all';
                            applyFilters();
                        });
                    });
                }

                const resetBtn = document.getElementById('apxResetFilterBtn');
                if (resetBtn) {
                    resetBtn.addEventListener('click', function () {
                        activeRank = 'all';
                        activeDuration = 'all';
                        activeSubcat = 'all';
                        rankFilterBtns.forEach(function (b) { b.classList.toggle('active', b.getAttribute('data-rank-filter') === 'all'); });
                        durationFilterBtns.forEach(function (b) { b.classList.toggle('active', b.getAttribute('data-duration-filter') === 'all'); });
                        subcatFilterBtns.forEach(function (b) { b.classList.toggle('active', b.getAttribute('data-subcat-filter') === 'all'); });
                        applyFilters();
                    });
                }
            }

            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', initShopCategoryInteractivity);
            } else {
                initShopCategoryInteractivity();
            }
        })();
    </script>
@endpush

@section('content')
    <!-- Store Header / Hero -->
    <div class="apx-store-hero">
        <div class="apx-section-kicker mb-2">
            <i class="bi bi-shield-check"></i> <span data-i18n="shop_kicker">TRANSAKSI RESMI PERADABAN</span>
        </div>
        <h1 class="mb-2">{{ $category->name }}</h1>
        <p data-i18n="shop_hero_sub">Tingkatkan kasta, kedaulatan, dan supremasi peradabanmu di realm Apexsions dengan mandat resmi, kit berkala, serta perolehan sumber daya terpercaya.</p>
    </div>

    <!-- Category Switcher Pills -->
    @if(isset($categories) && count($categories) > 0)
        <div class="apx-store-nav-bar" role="tablist" aria-label="Pilih Kategori Webstore">
            <a href="{{ route('shop.home') }}" class="apx-store-pill @if($category === null) active @endif">
                <i class="bi bi-house-door-fill"></i>
                <span data-i18n="shop_nav_home">Beranda Toko</span>
            </a>
            @foreach($categories as $navCat)
                <a href="{{ route('shop.categories.show', $navCat) }}" class="apx-store-pill @if($category && $navCat->is($category)) active @endif">
                    <i class="{{ $navCat->icon ?? 'bi bi-tag-fill' }}"></i>
                    <span>{{ $navCat->name }}</span>
                    @if($navCat->packages_count ?? false)
                        <span class="badge bg-secondary bg-opacity-25 ms-1" style="font-size: 0.65rem;">{{ $navCat->packages_count }}</span>
                    @endif
                </a>
            @endforeach
        </div>
    @endif

    <!-- WhatsApp Direct Order Notice Banner -->
    <div class="card mb-4" style="background: linear-gradient(135deg, rgba(34, 197, 94, 0.08) 0%, rgba(15, 23, 42, 0.95) 100%); border: 1px solid rgba(34, 197, 94, 0.35); border-radius: var(--apx-radius-md);">
        <div class="card-body p-3 p-md-4 d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center justify-content-center flex-shrink-0" style="width: 48px; height: 48px; border-radius: 50%; background: rgba(34, 197, 94, 0.2); color: #4ade80; font-size: 1.6rem; border: 1px solid rgba(34, 197, 94, 0.4);">
                    <i class="bi bi-whatsapp"></i>
                </div>
                <div>
                    <div class="fw-bold text-white mb-1 d-flex align-items-center gap-2" style="font-size: 1.05rem;">
                        <span data-i18n="shop_wa_title">Pemesanan Langsung &amp; Terverifikasi via WhatsApp Founder</span>
                        <span class="badge bg-success bg-opacity-25 text-success font-monospace" style="font-size: 0.65rem; letter-spacing: 0.05em;" data-i18n="shop_wa_badge">AKTIVASI CEPAT</span>
                    </div>
                    <div class="text-muted small" style="line-height: 1.5;" data-i18n-html="shop_wa_desc">
                        Pilih paket yang diinginkan lalu hubungi salah satu dari 2 Founder resmi kami:
                        <strong class="text-white">Rifqi</strong> atau <strong class="text-white">Friell</strong>. Pesanan diproses aman, transparan, dan langsung aktif ke in-game.
                    </div>
                </div>
            </div>
            <div class="d-flex align-items-center gap-2 flex-wrap">
                @php
                    $founderAdmins = config('services.whatsapp.admins', [
                        ['name' => 'Rifqi', 'number' => '6281212994597', 'role' => 'Founder'],
                        ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
                    ]);
                @endphp
                @foreach($founderAdmins as $adm)
                    @php
                        $cleanPhone = preg_replace('/[^0-9]/', '', $adm['number']);
                    @endphp
                    <a href="https://wa.me/{{ $cleanPhone }}?text={{ rawurlencode('Halo Founder ' . $adm['name'] . ', saya ingin konsultasi seputar Webstore Apexsions.') }}" target="_blank" rel="noopener noreferrer" class="apx-shop-founder-pill" title="Konsultasi langsung dengan Founder {{ $adm['name'] }}">
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
                <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
                    <div class="card-body">
                        {!! $category->description !!}
                    </div>
                </div>
            @endif

            @php
                $linkedAccount = auth()->check() ? \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', auth()->id())->first() : null;
                $accountRank = $linkedAccount ? strtolower(trim($linkedAccount->rank ?? 'wanderer')) : 'wanderer';
                $isAccountPerm = $linkedAccount ? $linkedAccount->isPermanentRank() : false;
            @endphp

            @if($linkedAccount && ($accountRank === 'emperor' || $accountRank === 'sions') && $isAccountPerm)
                <div class="alert alert-dark mb-4 d-flex align-items-center justify-content-between flex-wrap gap-2" style="background: linear-gradient(135deg, rgba(234, 179, 8, 0.12) 0%, rgba(15, 23, 42, 0.95) 100%); border: 1px solid rgba(234, 179, 8, 0.4); border-radius: var(--apx-radius-md);">
                    <div class="d-flex align-items-center gap-3">
                        <div class="fs-2 text-warning"><i class="bi bi-award-fill"></i></div>
                        <div>
                            <div class="fw-bold text-white mb-0" style="font-size: 1rem;">
                                Status Kasta Terdeteksi: <span class="text-warning text-uppercase font-cinzel">{{ $accountRank }} (PERMANEN)</span>
                            </div>
                            <div class="text-muted small">
                                Akun Minecraft <strong class="text-white">{{ $linkedAccount->minecraft_username }}</strong> berhak atas diskon BattlePass musiman ({{ $accountRank === 'sions' ? '15%' : '10%' }}) dan klaim pass musim berjalan secara gratis.
                            </div>
                        </div>
                    </div>
                    <span class="badge bg-warning bg-opacity-25 text-warning px-3 py-2 font-monospace">VERIFIED VIP</span>
                </div>
            @endif

            @php
                $isRankCategory = str_contains(strtolower($category->name), 'rank') || str_contains(strtolower($category->slug ?? ''), 'rank');
                $isDiamondCategory = str_contains(strtolower($category->name), 'diamond') || str_contains(strtolower($category->slug ?? ''), 'diamond') || str_contains(strtolower($category->name), 'koin') || str_contains(strtolower($category->name), 'coin') || str_contains(strtolower($category->slug ?? ''), 'coin') || str_contains(strtolower($category->slug ?? ''), 'booster');
                $isBattlepassCategory = str_contains(strtolower($category->name), 'battlepass') || str_contains(strtolower($category->slug ?? ''), 'battlepass') || str_contains(strtolower($category->slug ?? ''), 'pass');
            @endphp

            @if($isRankCategory)
                <!-- Interactive Multi-Axis Rank & Duration Filter Bar -->
                <div class="apx-rank-filter-bar mb-4 p-3 d-flex flex-column gap-3">
                    <!-- Row 1: Filter Kategori Kasta -->
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <span class="text-warning small fw-bold me-1 d-flex align-items-center gap-1">
                                <i class="bi bi-shield-shaded"></i> <span>Kasta:</span>
                            </span>
                            <button type="button" class="apx-filter-btn apx-filter-rank active" data-rank-filter="all">
                                <i class="bi bi-grid-fill"></i> Semua Kasta
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-rank apx-rank-btn-ascendant" data-rank-filter="ascendant">
                                <i class="bi bi-shield-fill text-success"></i> Ascendant
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-rank apx-rank-btn-archon" data-rank-filter="archon">
                                <i class="bi bi-shield-fill text-info"></i> Archon
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-rank apx-rank-btn-sovereign" data-rank-filter="sovereign">
                                <i class="bi bi-shield-fill text-primary"></i> Sovereign
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-rank apx-rank-btn-emperor" data-rank-filter="emperor">
                                <i class="bi bi-shield-fill text-danger"></i> Emperor
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-rank apx-rank-btn-sions" data-rank-filter="sions">
                                <i class="bi bi-award-fill text-warning"></i> ✦ SIONS ✦
                            </button>
                        </div>
                        <div>
                            <a href="#matrix" class="apx-matrix-jump-btn text-warning text-decoration-none small d-flex align-items-center gap-1">
                                <i class="bi bi-table"></i> <span>Matriks Benefit</span>
                            </a>
                        </div>
                    </div>

                    <!-- Divider -->
                    <div style="border-top: 1px solid rgba(255, 255, 255, 0.08);"></div>

                    <!-- Row 2: Filter Durasi & Counter -->
                    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <span class="text-muted small fw-semibold me-1 d-flex align-items-center gap-1">
                                <i class="bi bi-hourglass-split"></i> <span>Durasi:</span>
                            </span>
                            <button type="button" class="apx-filter-btn apx-filter-duration active" data-duration-filter="all">
                                <i class="bi bi-collection-fill"></i> Semua Durasi
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-duration apx-filter-perm" data-duration-filter="permanen">
                                <i class="bi bi-patch-check-fill"></i> Permanen
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-duration" data-duration-filter="90-hari">
                                <i class="bi bi-clock-history"></i> Trial 90 Hari
                            </button>
                            <button type="button" class="apx-filter-btn apx-filter-duration" data-duration-filter="30-hari">
                                <i class="bi bi-calendar-event"></i> Trial 30 Hari
                            </button>
                        </div>
                        <div class="text-muted small" id="packageFilterCounter" style="font-size: 0.8rem;">
                            Menampilkan <span class="text-warning fw-bold font-monospace" id="visiblePackageCount">{{ count($category->packages) }}</span> paket
                        </div>
                    </div>
                </div>
            @elseif($isDiamondCategory)
                <!-- Interactive Subcategory Filter for Diamond & Boosters -->
                <div class="apx-rank-filter-bar mb-4 p-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <span class="text-warning small fw-bold me-1 d-flex align-items-center gap-1">
                            <i class="bi bi-funnel"></i> <span>Kategori:</span>
                        </span>
                        <button type="button" class="apx-filter-btn apx-filter-subcat active" data-subcat-filter="all">
                            <i class="bi bi-grid-fill"></i> Semua Paket
                        </button>
                        <button type="button" class="apx-filter-btn apx-filter-subcat" data-subcat-filter="diamond">
                            <i class="bi bi-gem text-info"></i> 💎 Diamond
                        </button>
                        <button type="button" class="apx-filter-btn apx-filter-subcat" data-subcat-filter="booster">
                            <i class="bi bi-lightning-charge-fill text-warning"></i> Booster Server
                        </button>
                    </div>
                    <div class="text-muted small" id="packageFilterCounter" style="font-size: 0.8rem;">
                        Menampilkan <span class="text-warning fw-bold font-monospace" id="visiblePackageCount">{{ count($category->packages) }}</span> paket
                    </div>
                </div>
            @elseif($isBattlepassCategory)
                <!-- Interactive Subcategory Filter for Battlepass -->
                <div class="apx-rank-filter-bar mb-4 p-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <span class="text-warning small fw-bold me-1 d-flex align-items-center gap-1">
                            <i class="bi bi-trophy"></i> <span>Tipe Pass:</span>
                        </span>
                        <button type="button" class="apx-filter-btn apx-filter-subcat active" data-subcat-filter="all">
                            <i class="bi bi-grid-fill"></i> Semua Pass
                        </button>
                        <button type="button" class="apx-filter-btn apx-filter-subcat" data-subcat-filter="sio">
                            <i class="bi bi-trophy-fill text-warning"></i> Sio Pass (100 Level)
                        </button>
                        <button type="button" class="apx-filter-btn apx-filter-subcat" data-subcat-filter="exsio">
                            <i class="bi bi-stars text-info"></i> Exsio Pass (+20 Level)
                        </button>
                    </div>
                    <div class="text-muted small" id="packageFilterCounter" style="font-size: 0.8rem;">
                        Menampilkan <span class="text-warning fw-bold font-monospace" id="visiblePackageCount">{{ count($category->packages) }}</span> paket
                    </div>
                </div>
            @endif

            <div class="row g-4" id="packagesGrid">
                @forelse($category->packages as $package)
                    @php
                        $packageName = strtolower($package->name);
                        $defaultImage = null;
                        $fallbackIcon = 'bi bi-gem';
                        $cardModifierClass = '';
                        $rankCrest = null;
                        $badgeText = null;
                        $badgeClass = '';

                        $isTrial30 = str_contains($packageName, '30 hari') || str_contains($packageName, 'trial 30');
                        $isTrial90 = str_contains($packageName, '90 hari') || str_contains($packageName, 'trial 90');
                        $isTrial = $isTrial30 || $isTrial90 || str_contains($packageName, 'trial');
                        $isPermanent = str_contains($packageName, 'permanen');

                        $durationTag = 'other';
                        if ($isPermanent) $durationTag = 'permanen';
                        elseif ($isTrial90) $durationTag = '90-hari';
                        elseif ($isTrial30) $durationTag = '30-hari';

                        $subcatTag = 'other';
                        if (str_contains($packageName, 'koin') || str_contains($packageName, 'coin')) {
                            $subcatTag = 'coins';
                        } elseif (str_contains($packageName, 'booster')) {
                            $subcatTag = 'booster';
                        } elseif (str_contains($packageName, 'exsio')) {
                            $subcatTag = 'exsio';
                        } elseif (str_contains($packageName, 'sio pass') || str_contains($packageName, 'pass')) {
                            $subcatTag = 'sio';
                        }

                        $rankKey = null;
                        if (str_contains($packageName, 'sions')) $rankKey = 'sions';
                        elseif (str_contains($packageName, 'emperor')) $rankKey = 'emperor';
                        elseif (str_contains($packageName, 'sovereign')) $rankKey = 'sovereign';
                        elseif (str_contains($packageName, 'archon')) $rankKey = 'archon';
                        elseif (str_contains($packageName, 'ascendant')) $rankKey = 'ascendant';

                        if ($rankKey === 'sions') {
                            $cardModifierClass = 'apx-pkg-sions';
                            $defaultImage = theme_asset('img/package-sions.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-sions.png');
                            $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90H' : 'TRIAL 30H');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif ($rankKey === 'emperor') {
                            $cardModifierClass = 'apx-pkg-emperor';
                            $defaultImage = theme_asset('img/package-emperor.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-emperor.png');
                            $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90H' : 'TRIAL 30H');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif ($rankKey === 'sovereign') {
                            $cardModifierClass = 'apx-pkg-sovereign';
                            $defaultImage = theme_asset('img/package-sovereign.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-sovereign.png');
                            $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90H' : 'TRIAL 30H');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif ($rankKey === 'archon') {
                            $cardModifierClass = 'apx-pkg-archon';
                            $defaultImage = theme_asset('img/package-archon.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-archon.png');
                            $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90H' : 'TRIAL 30H');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif ($rankKey === 'ascendant') {
                            $cardModifierClass = 'apx-pkg-ascendant';
                            $defaultImage = theme_asset('img/package-ascendant.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-ascendant.png');
                            $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90H' : 'TRIAL 30H');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'exsio pass')) {
                            $cardModifierClass = 'apx-pkg-exsio-pass';
                            $defaultImage = theme_asset('img/package-exsio-pass.jpg');
                            $fallbackIcon = 'bi bi-award-fill';
                            $badgeText = 'ULTIMATE PASS';
                            $badgeClass = 'bg-primary text-white border border-info';
                        } elseif (str_contains($packageName, 'sio pass') || str_contains($packageName, 'pass')) {
                            $cardModifierClass = 'apx-pkg-sio-pass';
                            $defaultImage = theme_asset('img/package-sio-pass.jpg');
                            $fallbackIcon = 'bi bi-trophy-fill';
                            $badgeText = 'SEASON PASS';
                            $badgeClass = 'bg-warning text-dark border border-warning';
                        } elseif (str_contains($packageName, 'diamond')) {
                            $cardModifierClass = 'apx-pkg-diamond';
                            $defaultImage = theme_asset('img/package-diamond.jpg');
                            $fallbackIcon = 'bi bi-gem';
                            $badgeText = 'CURRENCY RESMI';
                            $badgeClass = 'bg-info text-dark fw-bold border border-info';
                            $subcatTag = 'diamond';
                        } elseif (str_contains($packageName, 'booster')) {
                            $fallbackIcon = 'bi bi-lightning-charge-fill';
                            $badgeText = 'BOOSTER 72J';
                            $badgeClass = 'bg-warning text-dark';
                            $subcatTag = 'booster';
                        } elseif (str_contains($packageName, 'koin') || str_contains($packageName, 'coin')) {
                            $cardModifierClass = 'apx-pkg-diamond';
                            $fallbackIcon = 'bi bi-gem';
                            $badgeText = 'DIAMOND';
                            $badgeClass = 'bg-info text-dark';
                            $subcatTag = 'diamond';
                        } else {
                            $badgeText = 'PAKET RESMI';
                        }

                        // Override badge with Webstore Manager custom badge if configured
                        $adminCustomBadge = setting('apexsions.webstore.pkg_' . $package->id . '.badge');
                        if (!empty($adminCustomBadge)) {
                            $badgeText = $adminCustomBadge;
                            $badgeClass = 'bg-warning text-dark fw-bold border border-warning shadow-sm';
                        }

                        $isUpgradeAvailable = false;
                        $isAlreadyOwned = false;
                        $upgradeCalculation = null;
                        $upgradeWaUrl = null;

                        if ($linkedAccount && $rankKey && $isPermanent) {
                            $rankHierarchy = ['wanderer' => 10, 'ascendant' => 30, 'archon' => 40, 'sovereign' => 50, 'emperor' => 60, 'sions' => 70];
                            $userRankWeight = $rankHierarchy[$accountRank] ?? 0;
                            $targetRankWeight = $rankHierarchy[$rankKey] ?? 0;

                            if ($isAccountPerm && $userRankWeight >= $targetRankWeight) {
                                $isAlreadyOwned = true;
                            } elseif ($isAccountPerm && $targetRankWeight > $userRankWeight) {
                                $upgradeCalculation = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::calculateUpgradePrice(
                                    $linkedAccount,
                                    $rankKey
                                );
                                if (!empty($upgradeCalculation['eligible'])) {
                                    $isUpgradeAvailable = true;
                                    $upgradeWaUrl = \Azuriom\Plugin\ApexsionsBridge\Services\BattlepassDiscountService::generateUpgradeWhatsAppUrl(
                                        $accountRank,
                                        $rankKey,
                                        $upgradeCalculation['upgrade_price']
                                    );
                                }
                            }
                        }

                        $discountInfo = \Azuriom\Plugin\ApexsionsBridge\Services\BattlepassDiscountService::calculateDiscount(
                            $linkedAccount,
                            $package,
                            (float) $package->getPrice()
                        );

                        // Override with Webstore Manager custom discount if defined
                        $adminDiscount = setting('apexsions.webstore.pkg_' . $package->id . '.discount');
                        if (!empty($adminDiscount) && is_numeric($adminDiscount) && (float)$adminDiscount > 0) {
                            $adminDiscVal = (float) $adminDiscount;
                            $origPrice = (float) $package->getPrice();
                            $discPrice = max(0, $origPrice * (1 - ($adminDiscVal / 100)));
                            $discountInfo['has_discount'] = true;
                            $discountInfo['discount_percent'] = $adminDiscVal;
                            $discountInfo['original_price'] = $origPrice;
                            $discountInfo['discounted_price'] = $discPrice;
                            $discountInfo['savings'] = $origPrice - $discPrice;
                            $discountInfo['eligible_rank'] = 'Promo Spesial';
                        }

                        // WhatsApp Admin Routing Override
                        $customWaAdmin = setting('apexsions.webstore.pkg_' . $package->id . '.wa_admin');
                        if (!empty($customWaAdmin)) {
                            $primaryCleanNum = preg_replace('/[^0-9]/', '', $customWaAdmin);
                        } else {
                            $primaryAdmin = $founderAdmins[0] ?? ['name' => 'Rifqi', 'number' => '6281212994597'];
                            $primaryCleanNum = preg_replace('/[^0-9]/', '', $primaryAdmin['number']);
                        }

                        $customWaTemplate = setting('apexsions.webstore.pkg_' . $package->id . '.wa_template');
                        if (!empty($customWaTemplate)) {
                            $waEffectivePrice = $discountInfo['has_discount'] ? $discountInfo['discounted_price'] : (float)$package->getPrice();
                            $waMessage = str_replace(
                                ['{package}', '{price}', '{player}'],
                                [$package->name, 'Rp ' . number_format($waEffectivePrice, 0, ',', '.'), $linkedAccount->minecraft_username ?? 'Player'],
                                $customWaTemplate
                            );
                        } elseif (str_contains($packageName, 'diamond')) {
                            $effPrice = $discountInfo['has_discount'] ? $discountInfo['discounted_price'] : (float)$package->getPrice();
                            $username = $linkedAccount->minecraft_username ?? (auth()->user()?->name ?? 'Player');
                            $waMessage = "Min, aku mau beli {$package->name} seharga Rp " . number_format($effPrice, 0, ',', '.') . " untuk akun {$username}.";
                        } else {
                            $waMessage = $discountInfo['whatsapp_message'];
                        }

                        if ($isUpgradeAvailable && $upgradeWaUrl) {
                            $primaryWaUrl = $upgradeWaUrl;
                        } else {
                            $primaryWaUrl = 'https://wa.me/' . $primaryCleanNum . '?text=' . rawurlencode($waMessage);
                        }

                        // Core specs for 2x2 micro-grid
                        $specHomes = 2; $specLelang = 3; $specEnchants = 4; $specRtp = '2m 30s';
                        if ($rankKey === 'sions') {
                            $specHomes = 10; $specLelang = 20; $specEnchants = 15; $specRtp = '50s';
                        } elseif ($rankKey === 'emperor') {
                            $specHomes = 7; $specLelang = 14; $specEnchants = 11; $specRtp = '1m 10s';
                        } elseif ($rankKey === 'sovereign') {
                            $specHomes = 5; $specLelang = 10; $specEnchants = 8; $specRtp = '1m 35s';
                        } elseif ($rankKey === 'archon') {
                            $specHomes = 4; $specLelang = 7; $specEnchants = 6; $specRtp = '2m 00s';
                        } elseif ($rankKey === 'ascendant') {
                            $specHomes = 3; $specLelang = 4; $specEnchants = 5; $specRtp = '2m 15s';
                        }
                    @endphp

                    <div class="col-md-6 col-xl-4 apx-package-col" data-duration="{{ $durationTag }}" data-rank="{{ $rankKey ?? 'other' }}" data-subcat="{{ $subcatTag }}">
                        <div class="apx-package-card h-100 d-flex flex-column {{ $cardModifierClass }}">
                            <!-- Zero-Collision Flexbox Header -->
                            <div class="apx-package-image-wrap position-relative">
                                <div class="apx-package-top-bar d-flex justify-content-between align-items-center w-100">
                                    <div class="d-flex align-items-center gap-2">
                                        @if($rankCrest)
                                            <img class="apx-rank-crest-icon" src="{{ $rankCrest }}" alt="Crest" loading="lazy">
                                        @endif
                                        @if($rankKey)
                                            <span class="apx-rank-tier-tag">{{ strtoupper($rankKey) }}</span>
                                        @endif
                                    </div>

                                    <div>
                                        @if($isAlreadyOwned)
                                            <span class="apx-package-badge bg-success text-white border border-success">
                                                <i class="bi bi-check-circle-fill me-1"></i> DIMILIKI
                                            </span>
                                        @elseif($isUpgradeAvailable)
                                            <span class="apx-package-badge apx-badge-upgrade">
                                                <i class="bi bi-arrow-up-circle-fill me-1"></i> UPGRADE
                                            </span>
                                        @elseif($badgeText)
                                            <span class="apx-package-badge {{ $badgeClass }}">
                                                @if($isPermanent)
                                                    <i class="bi bi-patch-check-fill me-1"></i>
                                                @elseif($isTrial)
                                                    <i class="bi bi-clock-history me-1"></i>
                                                @else
                                                    <i class="bi bi-star-fill me-1"></i>
                                                @endif
                                                {{ $badgeText }}
                                            </span>
                                        @endif
                                    </div>
                                </div>

                                @php
                                    $effectiveImage = $package->hasImage() ? $package->imageUrl() : $defaultImage;
                                @endphp

                                @if($effectiveImage)
                                    <img class="apx-package-image" src="{{ $effectiveImage }}" alt="{{ $package->name }} Apexsions Minecraft" loading="lazy" onerror="this.onerror=null; @if($defaultImage) this.src='{{ $defaultImage }}'; @else this.style.display='none'; @endif">
                                @elseif(str_contains($packageName, 'diamond'))
                                    <div class="d-flex flex-column align-items-center justify-content-center w-100 h-100 position-relative overflow-hidden" style="background: radial-gradient(circle at center, rgba(6, 182, 212, 0.28) 0%, rgba(15, 23, 42, 0.95) 80%);">
                                        <div class="position-absolute" style="top: 50%; left: 50%; transform: translate(-50%, -50%); width: 90px; height: 90px; background: rgba(56, 189, 248, 0.25); filter: blur(24px); border-radius: 50%;"></div>
                                        <i class="bi bi-gem text-info position-relative" style="font-size: 3rem; filter: drop-shadow(0 0 16px rgba(56, 189, 248, 0.8));"></i>
                                        <span class="position-relative small fw-bold font-monospace mt-1" style="color: #67e8f9; font-size: 0.72rem; letter-spacing: 0.1em;">PREMIUM CURRENCY</span>
                                    </div>
                                @else
                                    <div class="d-flex align-items-center justify-content-center w-100 h-100" style="background: rgba(245, 158, 11, 0.08); color: var(--apx-gold); font-size: 2.2rem;">
                                        <i class="{{ $fallbackIcon }}"></i>
                                    </div>
                                @endif
                                <div class="apx-package-image-overlay"></div>
                            </div>

                            <div class="apx-package-body d-flex flex-column flex-grow-1">
                                <h3 class="apx-package-title">{{ $package->name }}</h3>

                                <div class="apx-package-price-wrap mb-2">
                                    @if($isAlreadyOwned)
                                        <span class="apx-package-price text-success fs-5">
                                            <i class="bi bi-patch-check-fill me-1"></i> Aktif di Akun
                                        </span>
                                    @elseif($isUpgradeAvailable)
                                        <div class="d-flex flex-column">
                                            <span class="text-muted small text-decoration-line-through" style="font-size: 0.8rem;">Harga Normal Rp {{ number_format($upgradeCalculation['target_rank_price'], 0, ',', '.') }}</span>
                                            <div class="d-flex align-items-baseline gap-1">
                                                <span class="text-warning small fw-bold">Harga Upgrade</span>
                                                <span class="apx-package-price text-warning" style="color: #f1c40f !important;">Rp {{ number_format($upgradeCalculation['upgrade_price'], 0, ',', '.') }}</span>
                                            </div>
                                        </div>
                                    @elseif($discountInfo['has_discount'])
                                        <span class="apx-package-price-del">Rp {{ number_format($discountInfo['original_price'], 0, ',', '.') }}</span>
                                        <span class="apx-package-price text-success">Rp {{ number_format($discountInfo['discounted_price'], 0, ',', '.') }}</span>
                                    @elseif($package->isDiscounted())
                                        <span class="apx-package-price-del">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                                        <span class="apx-package-price">{{ shop_format_amount($package->getPrice()) }}</span>
                                    @else
                                        <span class="apx-package-price">Rp {{ number_format($package->getPrice(), 0, ',', '.') }}</span>
                                    @endif
                                </div>

                                @if($isUpgradeAvailable)
                                    <div class="mb-2">
                                        <span class="apx-discount-chip">
                                            <i class="bi bi-arrow-up-circle-fill"></i> Hemat Rp {{ number_format($upgradeCalculation['current_rank_price'], 0, ',', '.') }} (Selisih Upgrade)
                                        </span>
                                    </div>
                                @elseif($discountInfo['has_discount'])
                                    <div class="mb-2">
                                        <span class="apx-discount-chip">
                                            <i class="bi bi-tag-fill"></i> Hemat Rp {{ number_format($discountInfo['savings'], 0, ',', '.') }} (Diskon {{ $discountInfo['discount_percent'] }}% {{ $discountInfo['eligible_rank'] }})
                                        </span>
                                    </div>
                                @endif

                                @if($discountInfo['is_free_current_season'])
                                    <div class="alert alert-info py-1 px-2 small mb-2 d-flex align-items-center gap-2" style="font-size: 0.74rem; background: rgba(6, 182, 212, 0.15); border-color: rgba(6, 182, 212, 0.35); color: #67e8f9;">
                                        <i class="bi bi-gift-fill text-warning fs-6"></i>
                                        <span><strong>Rank {{ $discountInfo['eligible_rank'] }}:</strong> Gratis aktif untuk Season berjalan!</span>
                                    </div>
                                @endif

                                @if($rankKey)
                                    <!-- 2x2 Core Spec Micro Grid -->
                                    <div class="apx-spec-grid mb-3">
                                        <div class="apx-spec-pill" title="Batas /sethome">
                                            <i class="bi bi-house-door-fill text-warning"></i>
                                            <span><strong>{{ $specHomes }}</strong> Home</span>
                                        </div>
                                        <div class="apx-spec-pill" title="Slot Lelang /ah">
                                            <i class="bi bi-shop text-warning"></i>
                                            <span><strong>{{ $specLelang }}</strong> Lelang</span>
                                        </div>
                                        <div class="apx-spec-pill" title="Batas Custom Enchant">
                                            <i class="bi bi-magic text-warning"></i>
                                            <span><strong>{{ $specEnchants }}</strong> Enchant</span>
                                        </div>
                                        <div class="apx-spec-pill" title="Cooldown Teleport RTP">
                                            <i class="bi bi-compass-fill text-warning"></i>
                                            <span><strong>{{ $specRtp }}</strong> RTP</span>
                                        </div>
                                    </div>
                                @endif

                                <!-- Highlighted Perks -->
                                <ul class="apx-package-perks mb-2 is-collapsible" id="package-perks-{{ $package->id }}">
                                    @if($rankKey === 'sions')
                                        <li><i class="bi bi-graph-up-arrow text-warning"></i><span class="text-light">Jual: <strong>+17%</strong> • EXP: <strong>+20%</strong> • Bank: <strong>3.0x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-warning"></i><span class="text-light">/craft, /anvil, /repair, /feed (3m), /hat, /ec</span></li>
                                        @if($isPermanent)
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 300.000 In-game</span></li>
                                            <li><i class="bi bi-palette-fill text-warning"></i><span class="text-light">/nick: Bebas Semua Warna &amp; Gradien</span></li>
                                            <li><i class="bi bi-trophy-fill text-warning"></i><span class="text-light">Season Ini Free Sio+Exsio • 15% Diskon Depan</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-clock-history me-1"></i>Durasi aktif: <strong>{{ $isTrial90 ? '90' : '30' }} Hari</strong></li>
                                        @endif
                                    @elseif($rankKey === 'emperor')
                                        <li><i class="bi bi-graph-up-arrow text-danger"></i><span class="text-light">Jual: <strong>+12%</strong> • EXP: <strong>+14%</strong> • Bank: <strong>2.0x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-danger"></i><span class="text-light">/craft, /anvil, /repair, /feed (5m), /hat, /ec</span></li>
                                        @if($isPermanent)
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 180.000 In-game</span></li>
                                            <li><i class="bi bi-palette-fill text-danger"></i><span class="text-light">/nick: Warna Solid (Tanpa Gradien)</span></li>
                                            <li><i class="bi bi-trophy-fill text-danger"></i><span class="text-light">Season Ini Free Sio Pass • 10% Diskon Depan</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-clock-history me-1"></i>Durasi aktif: <strong>{{ $isTrial90 ? '90' : '30' }} Hari</strong></li>
                                        @endif
                                    @elseif($rankKey === 'sovereign')
                                        <li><i class="bi bi-graph-up-arrow text-primary"></i><span class="text-light">Jual: <strong>+8%</strong> • EXP: <strong>+10%</strong> • Bank: <strong>1.5x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-primary"></i><span class="text-light">/craft, /anvil, /smithing, /enderchest Portabel</span></li>
                                        @if($isPermanent)
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 120.000 In-game</span></li>
                                            <li><i class="bi bi-person-badge-fill text-primary"></i><span class="text-light">/nick: Ganti Nickname (Tanpa Warna)</span></li>
                                            <li><i class="bi bi-shield-check text-primary"></i><span class="text-light">Kit Sovereign + Seluruh Kit Bawah</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-clock-history me-1"></i>Durasi aktif: <strong>{{ $isTrial90 ? '90' : '30' }} Hari</strong></li>
                                        @endif
                                    @elseif($rankKey === 'archon')
                                        <li><i class="bi bi-graph-up-arrow text-info"></i><span class="text-light">Jual: <strong>+5%</strong> • EXP: <strong>+8%</strong> • Bank: <strong>1.2x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-info"></i><span class="text-light">/craft &amp; /enderchest Portabel</span></li>
                                        @if($isPermanent)
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 80.000 In-game</span></li>
                                            <li><i class="bi bi-shield-check text-info"></i><span class="text-light">Kit Archon + Akses Kit Ascendant</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-clock-history me-1"></i>Durasi aktif: <strong>{{ $isTrial90 ? '90' : '30' }} Hari</strong></li>
                                        @endif
                                    @elseif($rankKey === 'ascendant')
                                        <li><i class="bi bi-graph-up-arrow text-success"></i><span class="text-light">Jual: <strong>+3%</strong> • EXP: <strong>+5%</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-success"></i><span class="text-light">Akses Perintah Portabel Dasar</span></li>
                                        @if($isPermanent)
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 50.000 In-game</span></li>
                                            <li><i class="bi bi-shield-check text-success"></i><span class="text-light">Kit Ascendant (/kits &amp; /kit ascendant)</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-clock-history me-1"></i>Durasi aktif: <strong>{{ $isTrial90 ? '90' : '30' }} Hari</strong></li>
                                        @endif
                                    @elseif(str_contains($packageName, 'exsio pass'))
                                        <li><i class="bi bi-check2-circle text-info"></i><span class="text-light fw-bold">Otomatis Membuka Sio Pass (100 Level)</span></li>
                                        <li><i class="bi bi-lightning-charge-fill text-warning"></i><span class="text-light">Langsung Melompat +20 Level Awal</span></li>
                                        <li><i class="bi bi-stars text-warning"></i><span class="text-light">Kosmetik Mitos: Sayap, Aura &amp; Gelar</span></li>
                                        <li><i class="bi bi-cash text-success"></i><span class="text-light">Bonus Tunai Rp 50.000 Saldo In-game</span></li>
                                    @elseif(str_contains($packageName, 'sio pass'))
                                        <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Buka 100 Tier Jalur Hadiah Emas Musiman</span></li>
                                        <li><i class="bi bi-trophy-fill text-warning"></i><span class="text-light">Akses Quests Harian, Mingguan &amp; EXP Shop</span></li>
                                        <li><i class="bi bi-gift-fill text-warning"></i><span class="text-light">Kosmetik Eksklusif Musiman &amp; Title</span></li>
                                    @elseif(str_contains($packageName, 'diamond'))
                                        <li><i class="bi bi-gem text-info"></i><span class="text-light fw-bold">Mata Uang Premium Resmi Server (💎)</span></li>
                                        <li><i class="bi bi-calculator text-info"></i><span class="text-light">Kurs Resmi: <strong>Rp 375 / 1 Diamond</strong></span></li>
                                        <li><i class="bi bi-lightning-charge-fill text-warning"></i><span class="text-light">Aktivasi Instan ke Saldo In-game via Daemon</span></li>
                                        <li><i class="bi bi-shield-check text-success"></i><span class="text-light">Transaksi Resmi, Terverifikasi &amp; Aman</span></li>
                                        <li><i class="bi bi-shop text-info"></i><span class="text-light">Dapat Dibelanjakan di /ah &amp; Toko Kerajaan</span></li>
                                    @else
                                        <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Aktivasi Otomatis Langsung ke In-game</span></li>
                                        <li><i class="bi bi-shield-check text-warning"></i><span class="text-light">Transaksi Terverifikasi &amp; Aman</span></li>
                                    @endif
                                </ul>

                                @if($rankKey)
                                    <button type="button" class="apx-btn-perks-toggle" data-apx-toggle-perks="package-perks-{{ $package->id }}" aria-expanded="false" aria-controls="package-perks-{{ $package->id }}">
                                        <span class="apx-toggle-perks-text">Lihat Semua Benefit</span> <i class="bi bi-chevron-down ms-1"></i>
                                    </button>
                                @endif

                                <div class="apx-package-footer mt-auto d-flex flex-column gap-2 pt-2">
                                    @if($isAlreadyOwned)
                                        <button class="btn btn-outline-secondary w-100 py-2 disabled" disabled>
                                            <i class="bi bi-check2-circle me-1"></i> Sudah Dimiliki
                                        </button>
                                    @elseif($isUpgradeAvailable)
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-warning fw-bold text-dark w-100 py-2 shadow-sm apx-btn-glow">
                                            <i class="bi bi-arrow-up-circle-fill me-1"></i> Upgrade Rank (Rp {{ number_format($upgradeCalculation['upgrade_price'], 0, ',', '.') }})
                                        </a>
                                    @elseif(str_contains($packageName, 'diamond'))
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2 apx-btn-glow" style="background: linear-gradient(135deg, #0284c7, #06b6d4); border-color: #38bdf8; font-weight: 700;">
                                            <i class="bi bi-gem me-1"></i> <span>Beli Sekarang</span>
                                        </a>
                                    @else
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2 apx-btn-glow">
                                            <i class="bi bi-whatsapp me-1"></i> <span data-i18n="shop_btn_wa">Beli via WhatsApp</span>
                                        </a>
                                    @endif
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

                <!-- Interactive Filter Empty State -->
                <div class="col-12" id="apxFilterEmptyState" style="display: none;">
                    <div class="card p-4 text-center" style="background: var(--apx-bg-surface); border: 1px dashed var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
                        <div class="mb-2 text-warning fs-2">
                            <i class="bi bi-funnel"></i>
                        </div>
                        <h5 class="text-white mb-1 font-cinzel">Tidak Ada Paket yang Sesuai Filter</h5>
                        <p class="text-muted small mb-3">Tidak ditemukan paket yang memenuhi kombinasi kriteria filter yang sedang aktif.</p>
                        <div>
                            <button type="button" class="btn btn-sm btn-apx-outline px-3 py-2" id="apxResetFilterBtn">
                                <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Semua Filter
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Rank Benefit Comparison Matrix Section -->
            @if(str_contains(strtolower($category->name), 'rank') || str_contains(strtolower($category->slug ?? ''), 'rank'))
                <div class="apx-rank-matrix-wrap mt-5" id="matrix">
                    <div class="apx-rank-matrix-header d-flex align-items-center justify-content-between flex-wrap gap-3">
                        <div>
                            <div class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25 px-2 py-1 mb-2 font-monospace" style="font-size: 0.72rem;">
                                <i class="bi bi-table me-1"></i> MATRIKS KEHORMATAN PERADABAN
                            </div>
                            <h3 class="font-cinzel text-white fw-bold mb-1" style="font-size: 1.35rem;">
                                Perbandingan Benefit &amp; Hak Istimewa Kasta Resmi
                            </h3>
                            <p class="text-muted small mb-0">
                                Pahami seluruh perbedaan hak istimewa, limitasi sumber daya, dan perintah portabel dari Warga Biasa hingga kasta puncak SIONS.
                            </p>
                        </div>
                        <a href="#shop" class="btn btn-sm btn-apx-outline">
                            <i class="bi bi-arrow-up-circle me-1"></i> Kembali ke Paket
                        </a>
                    </div>

                    <div class="apx-rank-matrix-scroll-hint d-lg-none">
                        <i class="bi bi-arrow-left-right me-1"></i> Geser tabel ke samping untuk melihat seluruh kasta
                    </div>

                    <div class="apx-rank-matrix-table-scroll">
                        <table class="apx-rank-matrix-table">
                            <thead>
                                <tr>
                                    <th style="min-width: 220px;">FITUR / HAK ISTIMEWA</th>
                                    <th style="min-width: 110px;">WARGA BIASA</th>
                                    <th style="min-width: 125px; color: #6ee7b7;">ASCENDANT</th>
                                    <th style="min-width: 125px; color: #67e8f9;">ARCHON</th>
                                    <th style="min-width: 125px; color: #93c5fd;">SOVEREIGN</th>
                                    <th style="min-width: 125px; color: #fca5a5;">EMPEROR</th>
                                    <th style="min-width: 140px;" class="col-sions">✦ SIONS ✦</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td><i class="bi bi-house-door-fill text-warning me-2"></i>Batas Home (/sethome)</td>
                                    <td>2 Home</td>
                                    <td>3 Home</td>
                                    <td>4 Home</td>
                                    <td>5 Home</td>
                                    <td>7 Home</td>
                                    <td class="col-sions apx-matrix-val-gold">10 Home</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-shop text-warning me-2"></i>Slot Lelang Aktif (/ah /lelang)</td>
                                    <td>3 Listing</td>
                                    <td>4 Listing</td>
                                    <td>7 Listing</td>
                                    <td>10 Listing</td>
                                    <td>14 Listing</td>
                                    <td class="col-sions apx-matrix-val-gold">20 Listing</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-magic text-warning me-2"></i>Maksimal Custom Enchant</td>
                                    <td>4 Enchant</td>
                                    <td>5 Enchant</td>
                                    <td>6 Enchant</td>
                                    <td>8 Enchant</td>
                                    <td>11 Enchant</td>
                                    <td class="col-sions apx-matrix-val-gold">15 Enchant</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-compass-fill text-warning me-2"></i>Cooldown Teleport (/rtp &amp; /tpr)</td>
                                    <td>2m 30s</td>
                                    <td>2m 15s</td>
                                    <td>2m 00s</td>
                                    <td>1m 35s</td>
                                    <td>1m 10s</td>
                                    <td class="col-sions apx-matrix-val-gold">50 Detik</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-graph-up-arrow text-warning me-2"></i>Bonus Harga Jual (/sell)</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="text-success fw-bold">+3%</td>
                                    <td class="text-info fw-bold">+5%</td>
                                    <td class="text-primary fw-bold">+8%</td>
                                    <td class="text-danger fw-bold">+12%</td>
                                    <td class="col-sions apx-matrix-val-gold">+17%</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-stars text-warning me-2"></i>Bonus Perolehan EXP Leveling</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="text-success fw-bold">+5%</td>
                                    <td class="text-info fw-bold">+8%</td>
                                    <td class="text-primary fw-bold">+10%</td>
                                    <td class="text-danger fw-bold">+14%</td>
                                    <td class="col-sions apx-matrix-val-gold">+20%</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-piggy-bank-fill text-warning me-2"></i>Multiplier Bunga Bank (/bank)</td>
                                    <td>1.0x Normal</td>
                                    <td>1.0x Normal</td>
                                    <td class="text-info fw-bold">1.2x Return</td>
                                    <td class="text-primary fw-bold">1.5x Return</td>
                                    <td class="text-danger fw-bold">2.0x Return</td>
                                    <td class="col-sions apx-matrix-val-gold">3.0x Return</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-terminal-fill text-warning me-2"></i>Perintah Portabel</td>
                                    <td>Standar</td>
                                    <td>Standar</td>
                                    <td>/craft, /ec</td>
                                    <td>/craft, /anvil, /smithing, /ec</td>
                                    <td>+ /repair, /feed (5m), /hat</td>
                                    <td class="col-sions apx-matrix-val-gold">Lengkap + /feed (3m)</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-shield-shaded text-warning me-2"></i>Akses Kit Kasta (/kit)</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td>Kit Ascendant</td>
                                    <td>Kit Archon (+Asc)</td>
                                    <td>Kit Sovereign (+Bawah)</td>
                                    <td>Kit Emperor (+Bawah)</td>
                                    <td class="col-sions apx-matrix-val-gold">Kit Sions (Semua Kit)</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-palette-fill text-warning me-2"></i>Kustomisasi Nickname (/nick)</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td>/nick (Tanpa Warna)</td>
                                    <td>/nick (Warna Solid)</td>
                                    <td class="col-sions apx-matrix-val-gold">Bebas Semua Warna &amp; Gradien</td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-trophy-fill text-warning me-2"></i>Privilese BattlePass Musiman</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td>Season Ini Free Sio Pass<br><small class="text-muted">+ 10% Diskon Mendatang</small></td>
                                    <td class="col-sions apx-matrix-val-gold">Season Ini Free Sio &amp; Exsio<br><small class="text-warning">+ 15% Diskon Mendatang</small></td>
                                </tr>
                                <tr>
                                    <td><i class="bi bi-currency-dollar text-warning me-2"></i>Hadiah Uang Tunai Permanen</td>
                                    <td class="apx-matrix-val-cross">-</td>
                                    <td class="text-success fw-bold">Rp 50.000</td>
                                    <td class="text-info fw-bold">Rp 80.000</td>
                                    <td class="text-primary fw-bold">Rp 120.000</td>
                                    <td class="text-danger fw-bold">Rp 180.000</td>
                                    <td class="col-sions apx-matrix-val-gold">Rp 300.000 (Non-Duplikasi)</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            @endif
        </div>
    </div>

    <!-- Package Detail / Purchase Modal -->
    <div class="modal fade" id="itemModal" tabindex="-1" role="dialog" aria-labelledby="itemModalLabel" aria-hidden="true"></div>
@endsection
