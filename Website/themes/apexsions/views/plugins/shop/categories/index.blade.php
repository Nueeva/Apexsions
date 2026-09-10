@extends('layouts.app')

@section('title', 'Webstore Apexsions | Rank, Diamond & BattlePass Minecraft')
@section('description', 'Beli Rank, Diamond, BattlePass, dan berbagai item premium untuk mendukung perjalananmu di server Minecraft Apexsions.')

@push('scripts')
    <script>
        (function() {
            function initIndexModals() {
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
            }

            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', initIndexModals);
            } else {
                initIndexModals();
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
        <h1 class="mb-2" data-i18n="shop_index_title">Webstore Resmi Apexsions</h1>
        <p data-i18n="shop_index_sub">Selamat datang di pusat logistik peradaban tertinggi. Dukung kelangsungan server sembari memperkuat supremasi karaktermu dengan kasta donatur, seasonal battlepass, booster, dan pundi koin.</p>
    </div>

    <!-- Category Switcher Pills -->
    <div class="apx-store-nav-bar" role="tablist" aria-label="Pilih Kategori Webstore">
        <a href="{{ route('shop.home') }}" class="apx-store-pill active">
            <i class="bi bi-house-door-fill"></i>
            <span data-i18n="shop_nav_home">Beranda Toko</span>
        </a>
        @if(isset($categories))
            @foreach($categories as $navCat)
                <a href="{{ route('shop.categories.show', $navCat) }}" class="apx-store-pill">
                    <i class="{{ $navCat->icon ?? 'bi bi-tag-fill' }}"></i>
                    <span>{{ $navCat->name }}</span>
                    @if($navCat->packages_count ?? false)
                        <span class="badge bg-secondary bg-opacity-25 ms-1" style="font-size: 0.65rem;">{{ $navCat->packages_count }}</span>
                    @endif
                </a>
            @endforeach
        @endif
    </div>

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

        <!-- Main Home Showcase Area -->
        <div class="col-lg-9">
            <!-- Grand Welcome Hub Card -->
            <div class="card mb-4 overflow-hidden position-relative" style="background: radial-gradient(circle at 85% 20%, rgba(245, 158, 11, 0.15), rgba(15, 21, 35, 0.98) 70%); border: 1px solid var(--apx-gold-border); border-radius: var(--apx-radius-lg); box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
                <div class="card-body p-4 p-md-5">
                    <div class="row align-items-center g-4">
                        <div class="col-lg-8">
                            <span class="badge mb-3 px-3 py-1 font-monospace" style="background: rgba(245, 158, 11, 0.18); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.75rem; letter-spacing: 0.1em;" data-i18n="shop_hub_badge">
                                <i class="bi bi-stars text-warning me-1"></i> LOGISTIK REALM • APEXSIONS
                            </span>
                            <h2 class="font-cinzel fw-bold text-white mb-3" style="font-size: 1.85rem;" data-i18n="shop_hub_title">
                                Pusat Perbekalan &amp; Kehormatan Peradaban
                            </h2>
                            <p class="text-light text-opacity-75 mb-4" style="line-height: 1.7; font-size: 0.95rem;" data-i18n="shop_hub_desc">
                                Pilih kategori di bawah atau jelajahi paket unggulan. Setiap kontribusi langsung dialokasikan untuk pemeliharaan server berkecepatan tinggi dan otomatis aktif ke akun Minecraft kamu.
                            </p>
                            <div class="d-flex flex-wrap gap-2">
                                @if(isset($categories) && $categories->isNotEmpty())
                                    <a href="{{ route('shop.categories.show', $categories->first()) }}" class="btn btn-apx-gold px-4 py-2">
                                        <i class="bi bi-compass me-1"></i> <span data-i18n="shop_btn_explore">Mulai Jelajahi Katalog</span>
                                    </a>
                                @endif
                                <a href="https://wa.me/6281212994597?text={{ rawurlencode('Halo Founder Apexsions, saya butuh panduan untuk pembelian webstore.') }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-outline px-3 py-2">
                                    <i class="bi bi-chat-dots me-1"></i> <span data-i18n="shop_btn_guide">Bimbingan Transaksi</span>
                                </a>
                            </div>
                        </div>
                        <div class="col-lg-4 text-center d-none d-lg-block">
                            <div class="position-relative d-inline-block">
                                <div class="position-absolute top-50 start-50 translate-middle w-100 h-100 rounded-circle" style="background: radial-gradient(circle, rgba(245, 158, 11, 0.3) 0%, transparent 70%); filter: blur(25px); z-index: 0;"></div>
                                <img src="{{ theme_asset('img/logo.jpg') . '?v=' . (@filemtime(public_path('assets/themes/apexsions/img/logo.jpg')) ?: '2') }}" 
                                     alt="Apexsions Logo" 
                                     class="img-fluid rounded-circle position-relative" 
                                     style="max-width: 170px; border: 2px solid var(--apx-gold); box-shadow: 0 0 25px rgba(245, 158, 11, 0.4); z-index: 1;">
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 3 Main Category Showcase Cards -->
            <div class="row g-3 mb-5">
                @if(isset($categories))
                    @foreach($categories as $cat)
                        @php
                            $catSlug = $cat->slug;
                            $catIcon = $cat->icon ?? 'bi bi-tag-fill';
                            $badgeText = 'KATEGORI RESMI';
                            $btnI18n = 'shop_cat_btn_view';
                            $cardAccentColor = 'var(--apx-gold)';

                            if (str_contains($catSlug, 'rank') || str_contains(strtolower($cat->name), 'rank')) {
                                $catIcon = 'bi bi-award-fill';
                                $badgeText = '5 TINGKATAN KASTA';
                                $cardAccentColor = '#f59e0b';
                            } elseif (str_contains($catSlug, 'battlepass') || str_contains(strtolower($cat->name), 'battlepass')) {
                                $catIcon = 'bi bi-trophy-fill';
                                $badgeText = 'SEASON PASS AKTIF';
                                $cardAccentColor = '#a855f7';
                            } elseif (str_contains($catSlug, 'coin') || str_contains($catSlug, 'booster') || str_contains(strtolower($cat->name), 'koin')) {
                                $catIcon = 'bi bi-gem';
                                $badgeText = 'BOOSTER & KOIN';
                                $cardAccentColor = '#06b6d4';
                            }
                        @endphp
                        <div class="col-md-4">
                            <div class="card h-100 p-3 p-xl-4 d-flex flex-column" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md); transition: transform 0.25s ease, border-color 0.25s ease;">
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <div class="d-flex align-items-center justify-content-center" style="width: 48px; height: 48px; border-radius: 12px; background: rgba(245, 158, 11, 0.1); border: 1px solid {{ $cardAccentColor }}; color: {{ $cardAccentColor }}; font-size: 1.4rem;">
                                        <i class="{{ $catIcon }}"></i>
                                    </div>
                                    <span class="badge px-2 py-1 font-monospace" style="background: rgba(255,255,255,0.06); color: #cbd5e1; border: 1px solid rgba(255,255,255,0.12); font-size: 0.65rem;">
                                        {{ $badgeText }}
                                    </span>
                                </div>
                                <h4 class="font-cinzel text-white fw-bold mb-2" style="font-size: 1.15rem;">{{ $cat->name }}</h4>
                                <p class="text-muted small mb-4 flex-grow-1" style="line-height: 1.6; font-size: 0.85rem;">
                                    {{ Str::limit(strip_tags($cat->description), 110) }}
                                </p>
                                <a href="{{ route('shop.categories.show', $cat) }}" class="btn btn-apx-outline w-100 py-2 d-flex align-items-center justify-content-center gap-2">
                                    <span>Buka Katalog</span>
                                    <i class="bi bi-arrow-right"></i>
                                </a>
                            </div>
                        </div>
                    @endforeach
                @endif
            </div>

            <!-- Featured Packages Grid -->
            <div class="mb-5">
                <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
                    <div>
                        <h3 class="font-cinzel text-white fw-bold mb-1" style="font-size: 1.4rem;">
                            <i class="bi bi-fire text-warning me-2"></i><span data-i18n="shop_featured_title">Paket Paling Populer &amp; Direkomendasikan</span>
                        </h3>
                        <p class="text-muted small mb-0" data-i18n="shop_featured_sub">Pilihan terfavorit para kaisar dan penguasa peradaban Apexsions.</p>
                    </div>
                </div>

                @php
                    $linkedAccount = auth()->check() ? \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', auth()->id())->first() : null;

                    // Collect featured packages from Webstore Manager settings or fallback
                    $featuredPackages = collect();
                    if (isset($categories)) {
                        foreach ($categories as $cat) {
                            foreach ($cat->packages as $pkg) {
                                if (setting('apexsions.webstore.pkg_' . $pkg->id . '.is_featured', '0') === '1') {
                                    $pkg->setRelation('category', $cat);
                                    $featuredPackages->push($pkg);
                                }
                            }
                        }
                    }

                    // Fallback to default featured packages if none explicitly chosen
                    if ($featuredPackages->isEmpty() && isset($categories)) {
                        foreach ($categories as $cat) {
                            foreach ($cat->packages as $pkg) {
                                $pName = strtolower($pkg->name);
                                if ((str_contains($pName, 'sions') && str_contains($pName, 'permanen')) || str_contains($pName, 'exsio pass') || str_contains($pName, 'xp booster')) {
                                    $pkg->setRelation('category', $cat);
                                    $featuredPackages->push($pkg);
                                }
                            }
                        }
                    }
                    $featuredPackages = $featuredPackages->take(6);
                @endphp

                <div class="row g-4">
                    @forelse($featuredPackages as $package)
                        @php
                            $packageName = strtolower($package->name);
                            $pkgCategory = $package->category;
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

                            if (str_contains($packageName, 'sions')) {
                                $cardModifierClass = 'apx-pkg-sions';
                                $defaultImage = theme_asset('img/package-sions.jpg');
                                $rankCrest = theme_asset('img/ranks/rank-sions.png');
                                $badgeText = 'PERMANEN';
                                $badgeClass = 'apx-badge-perm';
                            } elseif (str_contains($packageName, 'exsio pass')) {
                                $cardModifierClass = 'apx-pkg-exsio-pass';
                                $defaultImage = theme_asset('img/package-exsio-pass.jpg');
                                $fallbackIcon = 'bi bi-award-fill';
                                $badgeText = 'ULTIMATE PASS';
                                $badgeClass = 'bg-primary text-white border border-info';
                            } elseif (str_contains($packageName, 'pass')) {
                                $cardModifierClass = 'apx-pkg-sio-pass';
                                $defaultImage = theme_asset('img/package-sio-pass.jpg');
                                $fallbackIcon = 'bi bi-trophy-fill';
                                $badgeText = 'SEASON PASS';
                                $badgeClass = 'bg-warning text-dark border border-warning';
                            } elseif (str_contains($packageName, 'booster')) {
                                $fallbackIcon = 'bi bi-lightning-charge-fill';
                                $badgeText = 'BOOSTER 72J';
                                $badgeClass = 'bg-warning text-dark';
                            }

                            // Custom badge from Webstore Manager
                            $adminCustomBadge = setting('apexsions.webstore.pkg_' . $package->id . '.badge');
                            if (!empty($adminCustomBadge)) {
                                $badgeText = $adminCustomBadge;
                                $badgeClass = 'bg-warning text-dark fw-bold border border-warning shadow-sm';
                            }

                            $discountInfo = \Azuriom\Plugin\ApexsionsBridge\Services\BattlepassDiscountService::calculateDiscount(
                                $linkedAccount,
                                $package,
                                (float) $package->getPrice()
                            );

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
                                    [$package->name, 'Rp ' . number_format($waEffectivePrice, 0, ',', '.'), $linkedAccount->player_name ?? 'Player'],
                                    $customWaTemplate
                                );
                            } else {
                                $waMessage = $discountInfo['whatsapp_message'];
                            }
                            $primaryWaUrl = 'https://wa.me/' . $primaryCleanNum . '?text=' . rawurlencode($waMessage);
                        @endphp

                        <div class="col-md-6 col-xl-4">
                            <div class="apx-package-card h-100 d-flex flex-column {{ $cardModifierClass }}">
                                <div class="apx-package-image-wrap position-relative">
                                    <div class="apx-package-top-bar d-flex justify-content-between align-items-center w-100">
                                        <div class="d-flex align-items-center gap-2">
                                            @if($rankCrest)
                                                <img class="apx-rank-crest-icon" src="{{ $rankCrest }}" alt="Crest" loading="lazy">
                                            @endif
                                            @if(str_contains($packageName, 'sions'))
                                                <span class="apx-rank-tier-tag">SIONS</span>
                                            @endif
                                        </div>

                                        <div>
                                            @if($badgeText)
                                                <span class="apx-package-badge {{ $badgeClass }}">
                                                    @if($isPermanent)
                                                        <i class="bi bi-patch-check-fill me-1"></i>
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
                                        <img class="apx-package-image" src="{{ $effectiveImage }}" alt="{{ $package->name }}" loading="lazy" onerror="this.onerror=null; @if($defaultImage) this.src='{{ $defaultImage }}'; @else this.style.display='none'; @endif">
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
                                        @if($discountInfo['has_discount'])
                                            <span class="apx-package-price-del">Rp {{ number_format($discountInfo['original_price'], 0, ',', '.') }}</span>
                                            <span class="apx-package-price text-success">Rp {{ number_format($discountInfo['discounted_price'], 0, ',', '.') }}</span>
                                        @elseif($package->isDiscounted())
                                            <span class="apx-package-price-del">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                                            <span class="apx-package-price">{{ shop_format_amount($package->getPrice()) }}</span>
                                        @else
                                            <span class="apx-package-price">Rp {{ number_format($package->getPrice(), 0, ',', '.') }}</span>
                                        @endif
                                    </div>

                                    @if($discountInfo['has_discount'])
                                        <div class="mb-2">
                                            <span class="apx-discount-chip">
                                                <i class="bi bi-tag-fill"></i> Hemat Rp {{ number_format($discountInfo['savings'], 0, ',', '.') }} (Diskon {{ $discountInfo['discount_percent'] }}% Rank {{ $discountInfo['eligible_rank'] }})
                                            </span>
                                        </div>
                                    @endif

                                    @if($package->short_description)
                                        <p class="text-muted small mb-3 flex-grow-1" style="line-height: 1.6;">
                                            {{ $package->short_description }}
                                        </p>
                                    @endif

                                    <ul class="apx-package-perks mb-3">
                                        @if(str_contains($packageName, 'sions'))
                                            <li><i class="bi bi-house-door-fill text-warning"></i><span class="text-light">10 Homes • 20 Lelang • 15 Custom Enchants</span></li>
                                            <li><i class="bi bi-clock-history text-warning"></i><span class="text-light">RTP 50s • Jual +17% • EXP +20% • Bank 3.0x</span></li>
                                            <li><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 300.000 Server Money</span></li>
                                            <li><i class="bi bi-palette-fill text-warning"></i><span class="text-light">/nick: Bebas Semua Warna &amp; Gradien</span></li>
                                        @elseif(str_contains($packageName, 'exsio pass'))
                                            <li><i class="bi bi-check2-circle text-info"></i><span class="text-light fw-bold">Otomatis Membuka Sio Pass Penuh (100 Level)</span></li>
                                            <li><i class="bi bi-lightning-charge-fill text-warning"></i><span class="text-light">Instan Skip +20 Level Awal Battlepass</span></li>
                                            <li><i class="bi bi-cash text-success"></i><span class="text-light">Bonus Tunai Rp 50.000 Saldo In-game</span></li>
                                            <li><i class="bi bi-percent text-info"></i><span class="text-light">Diskon 15% untuk Pemegang Rank Sions</span></li>
                                        @elseif(str_contains($packageName, 'booster'))
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Pengganda Pengalaman 2x Lipat</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Durasi Penuh 72 Jam Real-Time</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Berlaku untuk Leveling &amp; Pass</span></li>
                                        @else
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Aktivasi Otomatis via Akun Minecraft</span></li>
                                            <li><i class="bi bi-shield-check text-warning"></i><span class="text-light">Dukungan Transaksi Aman &amp; Terverifikasi</span></li>
                                        @endif
                                    </ul>

                                    <div class="apx-package-footer mt-auto d-flex flex-column gap-2">
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2">
                                            <i class="bi bi-whatsapp me-1"></i> <span data-i18n="shop_btn_wa">Pesan Cepat via WhatsApp</span>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    @empty
                        <div class="col-12">
                            <div class="alert alert-secondary text-center py-4">
                                <i class="bi bi-info-circle fs-3 d-block mb-2 text-warning"></i>
                                <div>Belum ada paket unggulan yang ditampilkan. Silakan jelajahi kategori di atas.</div>
                            </div>
                        </div>
                    @endforelse
                </div>
            </div>

            <!-- 4 Service Guarantees Grid -->
            <div class="card mb-5" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-lg);">
                <div class="card-body p-4">
                    <h4 class="font-cinzel text-white fw-bold mb-4 text-center" style="font-size: 1.2rem;">
                        <i class="bi bi-shield-lock text-warning me-2"></i><span data-i18n="shop_guarantee_title">Jaminan &amp; Transparansi Layanan Webstore</span>
                    </h4>
                    <div class="row g-4 text-center text-md-start">
                        <div class="col-md-3">
                            <div class="d-flex flex-column align-items-center align-items-md-start">
                                <div class="mb-2 text-warning fs-3"><i class="bi bi-lightning-charge-fill"></i></div>
                                <h6 class="text-white fw-bold mb-1" data-i18n="shop_g1_title">Aktivasi Instan</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_g1_desc">Sinkronisasi real-time via WebBridge in-game tanpa jeda.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="d-flex flex-column align-items-center align-items-md-start">
                                <div class="mb-2 text-success fs-3"><i class="bi bi-qr-code-scan"></i></div>
                                <h6 class="text-white fw-bold mb-1" data-i18n="shop_g2_title">Pembayaran Fleksibel</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_g2_desc">Mendukung QRIS, GoPay, OVO, DANA, ShopeePay, &amp; Bank Transfer.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="d-flex flex-column align-items-center align-items-md-start">
                                <div class="mb-2 text-info fs-3"><i class="bi bi-fingerprint"></i></div>
                                <h6 class="text-white fw-bold mb-1" data-i18n="shop_g3_title">Terikat UUID Pemain</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_g3_desc">Perk dan donasi tersimpan aman pada identitas unik akun Minecraft Anda.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="d-flex flex-column align-items-center align-items-md-start">
                                <div class="mb-2 text-warning fs-3"><i class="bi bi-headset"></i></div>
                                <h6 class="text-white fw-bold mb-1" data-i18n="shop_g4_title">Bimbingan Founder Langsung</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_g4_desc">Konsultasi dan bantuan transaksi langsung ditangani oleh Rifqi &amp; Friell.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 4 Steps Purchasing Guide -->
            <div class="card mb-4" style="background: radial-gradient(circle at 10% 50%, rgba(245, 158, 11, 0.08), rgba(15, 21, 35, 0.95) 75%); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-lg);">
                <div class="card-body p-4 p-md-5">
                    <h4 class="font-cinzel text-white fw-bold mb-4 text-center" style="font-size: 1.25rem;">
                        <i class="bi bi-list-check text-warning me-2"></i><span data-i18n="shop_steps_title">4 Langkah Mudah Berbelanja di Webstore Apexsions</span>
                    </h4>
                    <div class="row g-4">
                        <div class="col-md-3">
                            <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(255,255,255,0.06);">
                                <div class="badge bg-warning text-dark fw-bold mb-2">01</div>
                                <h6 class="text-white fw-bold mb-2" data-i18n="shop_step1_title">1. Pilih Paket</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_step1_desc">Pilih kasta rank, battlepass, atau booster yang Anda perlukan dari katalog.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(255,255,255,0.06);">
                                <div class="badge bg-warning text-dark fw-bold mb-2">02</div>
                                <h6 class="text-white fw-bold mb-2" data-i18n="shop_step2_title">2. Konfirmasi Akun</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_step2_desc">Pastikan Username Minecraft (IGN) telah sesuai dan terdaftar di server.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(255,255,255,0.06);">
                                <div class="badge bg-warning text-dark fw-bold mb-2">03</div>
                                <h6 class="text-white fw-bold mb-2" data-i18n="shop_step3_title">3. Chat Founder via WA</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_step3_desc">Dapatkan nomor rekening/QRIS resmi dari Founder Rifqi atau Friell.</p>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="p-3 rounded h-100" style="background: rgba(0,0,0,0.25); border: 1px solid rgba(255,255,255,0.06);">
                                <div class="badge bg-warning text-dark fw-bold mb-2">04</div>
                                <h6 class="text-white fw-bold mb-2" data-i18n="shop_step4_title">4. Nikmati Keuntungan</h6>
                                <p class="text-muted small mb-0" style="line-height: 1.5;" data-i18n="shop_step4_desc">Login ke Minecraft, perk langsung aktif dan siap digunakan!</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Package Detail / Purchase Modal -->
    <div class="modal fade" id="itemModal" tabindex="-1" role="dialog" aria-labelledby="itemModalLabel" aria-hidden="true"></div>
@endsection
