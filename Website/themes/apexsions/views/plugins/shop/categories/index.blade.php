@extends('layouts.app')

@section('title', 'Webstore Resmi Peradaban — Apexsions')
@section('description', 'Pusat perbekalan resmi realm Apexsions. Tingkatkan supremasi kedaulatan dengan kasta donatur, seasonal battlepass, booster peradaban, dan pundi koin resmi.')

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
                    // Collect featured packages: Sions, Season Pass, and a booster or coin pack
                    $featuredPackages = collect();
                    if (isset($categories)) {
                        foreach ($categories as $cat) {
                            foreach ($cat->packages as $pkg) {
                                $pName = strtolower($pkg->name);
                                if (str_contains($pName, 'sions') || str_contains($pName, 'peradaban kuno') || str_contains($pName, 'xp booster') || str_contains($pName, '1.200')) {
                                    $pkg->setRelation('category', $cat);
                                    $featuredPackages->push($pkg);
                                }
                            }
                        }
                    }
                    // Take top 3
                    $featuredPackages = $featuredPackages->take(3);
                @endphp

                <div class="row g-4">
                    @forelse($featuredPackages as $package)
                        @php
                            $packageName = strtolower($package->name);
                            $pkgCategory = $package->category;
                            $defaultImage = null;
                            $fallbackIcon = 'bi bi-gem';
                            $cardModifierClass = '';

                            if (str_contains($packageName, 'sions')) {
                                $cardModifierClass = 'apx-pkg-sions';
                                $defaultImage = theme_asset('img/logo.jpg') . '?v=' . (@filemtime(public_path('assets/themes/apexsions/img/logo.jpg')) ?: '2');
                            } elseif (str_contains($packageName, 'pass')) {
                                $cardModifierClass = 'apx-pkg-pass';
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
                                . "📂 Kategori: " . ($pkgCategory ? $pkgCategory->name : 'Webstore') . "\n"
                                . "🎮 Akun Minecraft (IGN): " . $userIgn . "\n"
                                . "📧 Email Akun: " . $userEmail . "\n\n"
                                . "Mohon nomor rekening/QRIS dan instruksi aktivasi peradaban. Terima kasih!";

                            $primaryAdmin = $founderAdmins[0] ?? ['name' => 'Rifqi', 'number' => '6281212994597'];
                            $primaryCleanNum = preg_replace('/[^0-9]/', '', $primaryAdmin['number']);
                            $primaryWaUrl = 'https://wa.me/' . $primaryCleanNum . '?text=' . rawurlencode($waBaseText);
                        @endphp

                        <div class="col-md-6 col-xl-4">
                            <div class="apx-package-card h-100 d-flex flex-column {{ $cardModifierClass }}">
                                @if(str_contains($packageName, 'sions'))
                                    <span class="apx-package-badge" style="background: rgba(234, 179, 8, 0.25); color: #fde047; border: 1px solid #fde047;">
                                        <i class="bi bi-star-fill me-1"></i> APEX TIER
                                    </span>
                                @elseif(str_contains($packageName, 'pass'))
                                    <span class="apx-package-badge" style="background: rgba(168, 85, 247, 0.25); color: #d8b4fe; border: 1px solid #a855f7;">
                                        <i class="bi bi-trophy-fill me-1"></i> SEASON PASS
                                    </span>
                                @elseif(str_contains($packageName, 'booster'))
                                    <span class="apx-package-badge" style="background: rgba(245, 158, 11, 0.25); color: #fde68a; border: 1px solid #f59e0b;">
                                        <i class="bi bi-lightning-fill me-1"></i> <span data-i18n="shop_badge_3days">3 HARI AKTIF</span>
                                    </span>
                                @else
                                    <span class="apx-package-badge">
                                        <i class="bi bi-patch-check-fill text-warning me-1"></i> <span data-i18n="shop_badge_perm">PERMANEN</span>
                                    </span>
                                @endif

                                @if($package->hasImage())
                                    <div class="apx-package-image-wrap">
                                        <img class="apx-package-image" src="{{ $package->imageUrl() }}" alt="{{ $package->name }}" loading="lazy">
                                    </div>
                                @elseif($defaultImage)
                                    <div class="apx-package-image-wrap p-2">
                                        <img class="apx-package-image rounded" src="{{ $defaultImage }}" alt="{{ $package->name }}" style="max-height: 140px; width: 100%; object-fit: cover;" loading="lazy">
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

                                    <ul class="apx-package-perks">
                                        @if(str_contains($packageName, 'sions'))
                                            <li><i class="bi bi-crown text-warning"></i><span class="text-light" data-i18n="shop_sions_p1">Prefix Mahkota ✦ SIONS ✦</span></li>
                                            <li><i class="bi bi-shield-check text-warning"></i><span class="text-light" data-i18n="shop_sions_p2">Seluruh Kit + Kit Sions Eksklusif</span></li>
                                            <li><i class="bi bi-broadcast text-warning"></i><span class="text-light" data-i18n="shop_sions_p3">Pesan Broadcast Masuk Server Megah</span></li>
                                            <li><i class="bi bi-geo-alt text-warning"></i><span class="text-light" data-i18n="shop_sions_p4">+15 Batas Klaim Wilayah Kerajaan</span></li>
                                        @elseif(str_contains($packageName, 'peradaban kuno'))
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_prempass_p1">Buka Jalur Emas 100 Level Hadiah</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_prempass_p2">Akses Quests Harian &amp; Mingguan</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_prempass_p3">+25% Pengganda Perolehan EXP Pass</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_prempass_p4">Diskon Toko Berputar /abp shop</span></li>
                                        @elseif(str_contains($packageName, 'booster'))
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Pengganda Pengalaman 2x Lipat</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Durasi Penuh 72 Jam Real-Time</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Berlaku untuk Leveling &amp; Pass</span></li>
                                        @else
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_fallback_p1">Aktivasi Otomatis via Akun Minecraft</span></li>
                                            <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light" data-i18n="shop_fallback_p2">Dukungan Transaksi Aman &amp; Terverifikasi</span></li>
                                        @endif
                                    </ul>

                                    <div class="apx-package-footer mt-auto d-flex flex-column gap-2">
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2">
                                            <i class="bi bi-whatsapp me-1"></i> <span data-i18n="shop_btn_wa">Pesan Cepat via WhatsApp</span>
                                        </a>
                                        <a href="#" class="btn btn-apx-outline w-100 py-1 small" data-package-url="{{ route('shop.packages.show', $package) }}">
                                            <i class="bi bi-info-circle me-1"></i> <span data-i18n="shop_btn_details">Rincian &amp; Benefit Lengkap</span>
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
