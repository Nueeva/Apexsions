@extends('layouts.app')

@section('title', $category->name . ' — Webstore Resmi')
@section('description', 'Jelajahi paket ' . $category->name . ' di Webstore Resmi Apexsions. Pembelian aman, aktivasi otomatis instan di dalam server Minecraft (Java & Bedrock).')

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
        <h1 class="mb-2">{{ $category->name }}</h1>
        <p data-i18n="shop_hero_sub">Tingkatkan kasta, kedaulatan, dan supremasi peradabanmu di realm Apexsions dengan mandat resmi, kit berkala, serta perolehan sumber daya terpercaya.</p>
    </div>

    <!-- Category Switcher Pills -->
    @if(isset($categories) && count($categories) > 1)
        <div class="apx-store-nav-bar" role="tablist" aria-label="Pilih Kategori Webstore">
            @foreach($categories as $navCat)
                <a href="{{ route('shop.categories.show', $navCat) }}" class="apx-store-pill @if($navCat->is($category)) active @endif">
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

            <div class="row g-4">
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

                        if (str_contains($packageName, 'sions')) {
                            $cardModifierClass = 'apx-pkg-sions';
                            $defaultImage = theme_asset('img/package-sions.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-sions.png');
                            $badgeText = $isPermanent ? 'PERMANEN • APEX SIONS' : ($isTrial90 ? 'TRIAL 90 HARI • SIONS' : 'TRIAL 30 HARI • SIONS');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'emperor')) {
                            $cardModifierClass = 'apx-pkg-emperor';
                            $defaultImage = theme_asset('img/package-emperor.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-emperor.png');
                            $badgeText = $isPermanent ? 'PERMANEN • EMPEROR' : ($isTrial90 ? 'TRIAL 90 HARI • EMPEROR' : 'TRIAL 30 HARI • EMPEROR');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'sovereign')) {
                            $cardModifierClass = 'apx-pkg-sovereign';
                            $defaultImage = theme_asset('img/package-sovereign.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-sovereign.png');
                            $badgeText = $isPermanent ? 'PERMANEN • SOVEREIGN' : ($isTrial90 ? 'TRIAL 90 HARI • SOVEREIGN' : 'TRIAL 30 HARI • SOVEREIGN');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'archon')) {
                            $cardModifierClass = 'apx-pkg-archon';
                            $defaultImage = theme_asset('img/package-archon.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-archon.png');
                            $badgeText = $isPermanent ? 'PERMANEN • ARCHON' : ($isTrial90 ? 'TRIAL 90 HARI • ARCHON' : 'TRIAL 30 HARI • ARCHON');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'ascendant')) {
                            $cardModifierClass = 'apx-pkg-ascendant';
                            $defaultImage = theme_asset('img/package-ascendant.jpg');
                            $rankCrest = theme_asset('img/ranks/rank-ascendant.png');
                            $badgeText = $isPermanent ? 'PERMANEN • ASCENDANT' : ($isTrial90 ? 'TRIAL 90 HARI • ASCENDANT' : 'TRIAL 30 HARI • ASCENDANT');
                            $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
                        } elseif (str_contains($packageName, 'exsio pass')) {
                            $cardModifierClass = 'apx-pkg-exsio-pass';
                            $defaultImage = theme_asset('img/package-exsio-pass.jpg');
                            $fallbackIcon = 'bi bi-award-fill';
                            $badgeText = 'ULTIMATE PASS • INCLUDES SIO';
                            $badgeClass = 'bg-primary text-white border border-info';
                        } elseif (str_contains($packageName, 'sio pass') || str_contains($packageName, 'pass')) {
                            $cardModifierClass = 'apx-pkg-sio-pass';
                            $defaultImage = theme_asset('img/package-sio-pass.jpg');
                            $fallbackIcon = 'bi bi-trophy-fill';
                            $badgeText = 'SEASON PASS • JALUR EMAS';
                            $badgeClass = 'bg-warning text-dark border border-warning';
                        } elseif (str_contains($packageName, 'booster')) {
                            $fallbackIcon = 'bi bi-lightning-charge-fill';
                            $badgeText = 'BOOSTER 72 JAM';
                            $badgeClass = 'bg-warning text-dark';
                        } elseif (str_contains($packageName, 'koin') || str_contains($packageName, 'coin')) {
                            $fallbackIcon = 'bi bi-coin';
                            $badgeText = 'APEX COINS';
                            $badgeClass = 'bg-info text-dark';
                        } else {
                            $badgeText = 'PAKET RESMI';
                        }

                        $rankKey = null;
                        if (str_contains($packageName, 'sions')) $rankKey = 'sions';
                        elseif (str_contains($packageName, 'emperor')) $rankKey = 'emperor';
                        elseif (str_contains($packageName, 'sovereign')) $rankKey = 'sovereign';
                        elseif (str_contains($packageName, 'archon')) $rankKey = 'archon';
                        elseif (str_contains($packageName, 'ascendant')) $rankKey = 'ascendant';

                        $isUpgradeAvailable = false;
                        $isAlreadyOwned = false;
                        $upgradeCalculation = null;
                        $upgradeWaUrl = null;

                        if ($rankKey && $linkedAccount) {
                            $userWeight = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::getRankWeight($accountRank);
                            $targetWeight = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::getRankWeight($rankKey);

                            if ($isAccountPerm && $userWeight >= $targetWeight && $isPermanent) {
                                $isAlreadyOwned = true;
                            } elseif ($isAccountPerm && $isPermanent && $targetWeight > $userWeight) {
                                $upgradeCalculation = \Azuriom\Plugin\ApexsionsBridge\Services\RankService::calculateUpgradePrice($linkedAccount, $rankKey);
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

                        $primaryAdmin = $founderAdmins[0] ?? ['name' => 'Rifqi', 'number' => '6281212994597'];
                        $primaryCleanNum = preg_replace('/[^0-9]/', '', $primaryAdmin['number']);
                        if ($isUpgradeAvailable && $upgradeWaUrl) {
                            $primaryWaUrl = $upgradeWaUrl;
                        } else {
                            $primaryWaUrl = 'https://wa.me/' . $primaryCleanNum . '?text=' . rawurlencode($discountInfo['whatsapp_message']);
                        }
                    @endphp

                    <div class="col-md-6 col-xl-4">
                        <div class="apx-package-card h-100 d-flex flex-column {{ $cardModifierClass }}">
                            @if($isAlreadyOwned)
                                <span class="apx-package-badge bg-success text-white border border-success">
                                    <i class="bi bi-check-circle-fill me-1"></i> SUDAH DIMILIKI
                                </span>
                            @elseif($isUpgradeAvailable)
                                <span class="apx-package-badge bg-purple text-white border border-info" style="background: linear-gradient(135deg, #8E2DE2, #4A00E0);">
                                    <i class="bi bi-arrow-up-circle-fill me-1"></i> UPGRADE DARI {{ strtoupper($accountRank) }}
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

                            <div class="apx-package-image-wrap position-relative">
                                @if($rankCrest)
                                    <img class="apx-rank-badge-overlay" src="{{ $rankCrest }}" alt="Rank Crest" loading="lazy">
                                @endif

                                @if($package->hasImage())
                                    <img class="apx-package-image" src="{{ $package->imageUrl() }}" alt="{{ $package->name }}" loading="lazy">
                                @elseif($defaultImage)
                                    <img class="apx-package-image" src="{{ $defaultImage }}" alt="{{ $package->name }}" style="max-height: 140px; width: 100%; object-fit: cover;" loading="lazy">
                                @else
                                    <div class="d-inline-flex align-items-center justify-content-center w-100" style="height: 140px; background: rgba(245, 158, 11, 0.08); color: var(--apx-gold); font-size: 2.5rem;">
                                        <i class="{{ $fallbackIcon }}"></i>
                                    </div>
                                @endif
                            </div>

                            <div class="apx-package-body d-flex flex-column flex-grow-1">
                                <h3 class="apx-package-title">{{ $package->name }}</h3>

                                <div class="apx-package-price-wrap mb-2">
                                    @if($isAlreadyOwned)
                                        <span class="apx-package-price text-success fs-5">
                                            <i class="bi bi-patch-check-fill me-1"></i> Aktif di Akun
                                        </span>
                                    @elseif($isUpgradeAvailable)
                                        <span class="apx-package-price-del">Rp {{ number_format($upgradeCalculation['target_rank_price'], 0, ',', '.') }}</span>
                                        <span class="apx-package-price text-warning" style="color: #f1c40f !important;">Rp {{ number_format($upgradeCalculation['upgrade_price'], 0, ',', '.') }}</span>
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
                                            <i class="bi bi-arrow-up-circle-fill"></i> Hemat Rp {{ number_format($upgradeCalculation['current_rank_price'], 0, ',', '.') }} (Harga Upgrade Selisih)
                                        </span>
                                    </div>
                                @elseif($discountInfo['has_discount'])
                                    <div class="mb-2">
                                        <span class="apx-discount-chip">
                                            <i class="bi bi-tag-fill"></i> Hemat Rp {{ number_format($discountInfo['savings'], 0, ',', '.') }} (Diskon {{ $discountInfo['discount_percent'] }}% Rank {{ $discountInfo['eligible_rank'] }})
                                        </span>
                                    </div>
                                @endif

                                @if($discountInfo['is_free_current_season'])
                                    <div class="alert alert-info py-1 px-2 small mb-2 d-flex align-items-center gap-2" style="font-size: 0.76rem; background: rgba(6, 182, 212, 0.15); border-color: rgba(6, 182, 212, 0.35); color: #67e8f9;">
                                        <i class="bi bi-gift-fill text-warning fs-6"></i>
                                        <span><strong>Rank {{ $discountInfo['eligible_rank'] }}:</strong> Gratis aktif untuk Season berjalan!</span>
                                    </div>
                                @endif

                                @if($package->short_description)
                                    <p class="text-muted small mb-3 flex-grow-1" style="line-height: 1.6;">
                                        {{ $package->short_description }}
                                    </p>
                                @endif

                                <!-- Key Highlights Verified per Rank / Product -->
                                <ul class="apx-package-perks mb-3">
                                    @if(str_contains($packageName, 'sions'))
                                        <li><i class="bi bi-house-door-fill text-warning"></i><span class="text-light">10 Homes • 20 Slot Lelang • 15 Custom Enchants</span></li>
                                        <li><i class="bi bi-clock-history text-warning"></i><span class="text-light">RTP Cooldown: <strong>50 Detik</strong> (Paling Cepat)</span></li>
                                        <li><i class="bi bi-graph-up-arrow text-warning"></i><span class="text-light">Bonus Jual: <strong>+17%</strong> • Bonus EXP: <strong>+20%</strong> • Bank: <strong>3.0x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-warning"></i><span class="text-light">/craft, /anvil, /smithing, /repair, /feed (3m), /hat, /ec</span></li>
                                        @if($isPermanent)
                                            <li class="pt-1 border-top border-warning border-opacity-25"><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 300.000 Server Money (1x Klaim)</span></li>
                                            <li><i class="bi bi-palette-fill text-warning"></i><span class="text-light">/nick GUI: <strong>Bebas Semua Warna &amp; Gradien</strong></span></li>
                                            <li><i class="bi bi-shield-check text-warning"></i><span class="text-light">Kit Sions + Seluruh Kit Kasta Bawah</span></li>
                                            <li><i class="bi bi-trophy-fill text-warning"></i><span class="text-light">Season Ini Free Sio+Exsio • 15% Diskon Season Depan</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-info-circle me-1"></i>Masa aktif benefit berlaku selama {{ $isTrial90 ? '90' : '30' }} hari.</li>
                                        @endif
                                    @elseif(str_contains($packageName, 'emperor'))
                                        <li><i class="bi bi-house-door-fill text-danger"></i><span class="text-light">7 Homes • 14 Slot Lelang • 11 Custom Enchants</span></li>
                                        <li><i class="bi bi-clock-history text-danger"></i><span class="text-light">RTP Cooldown: <strong>1 Menit 10 Detik</strong></span></li>
                                        <li><i class="bi bi-graph-up-arrow text-danger"></i><span class="text-light">Bonus Jual: <strong>+12%</strong> • Bonus EXP: <strong>+14%</strong> • Bank: <strong>2.0x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-danger"></i><span class="text-light">/craft, /anvil, /smithing, /repair, /feed (5m), /hat, /ec</span></li>
                                        @if($isPermanent)
                                            <li class="pt-1 border-top border-danger border-opacity-25"><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 180.000 Server Money (1x Klaim)</span></li>
                                            <li><i class="bi bi-palette-fill text-danger"></i><span class="text-light">/nick GUI: Pilihan Warna Solid (Tanpa Gradien)</span></li>
                                            <li><i class="bi bi-shield-check text-danger"></i><span class="text-light">Kit Emperor + Seluruh Kit Kasta Bawah</span></li>
                                            <li><i class="bi bi-trophy-fill text-danger"></i><span class="text-light">Season Ini Free Sio Pass • 10% Diskon Season Depan</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-info-circle me-1"></i>Masa aktif benefit berlaku selama {{ $isTrial90 ? '90' : '30' }} hari.</li>
                                        @endif
                                    @elseif(str_contains($packageName, 'sovereign'))
                                        <li><i class="bi bi-house-door-fill text-primary"></i><span class="text-light">5 Homes • 10 Slot Lelang • 8 Custom Enchants</span></li>
                                        <li><i class="bi bi-clock-history text-primary"></i><span class="text-light">RTP Cooldown: <strong>1 Menit 35 Detik</strong></span></li>
                                        <li><i class="bi bi-graph-up-arrow text-primary"></i><span class="text-light">Bonus Jual: <strong>+8%</strong> • Bonus EXP: <strong>+10%</strong> • Bank: <strong>1.5x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-primary"></i><span class="text-light">/craft, /anvil, /smithing, /enderchest Portabel</span></li>
                                        @if($isPermanent)
                                            <li class="pt-1 border-top border-primary border-opacity-25"><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 120.000 Server Money (1x Klaim)</span></li>
                                            <li><i class="bi bi-person-badge-fill text-primary"></i><span class="text-light">/nick GUI: Ganti Nickname (Tanpa Warna)</span></li>
                                            <li><i class="bi bi-shield-check text-primary"></i><span class="text-light">Kit Sovereign + Seluruh Kit Kasta Bawah</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-info-circle me-1"></i>Masa aktif benefit berlaku selama {{ $isTrial90 ? '90' : '30' }} hari.</li>
                                        @endif
                                    @elseif(str_contains($packageName, 'archon'))
                                        <li><i class="bi bi-house-door-fill text-info"></i><span class="text-light">4 Homes • 7 Slot Lelang • 6 Custom Enchants</span></li>
                                        <li><i class="bi bi-clock-history text-info"></i><span class="text-light">RTP Cooldown: <strong>2 Menit 00 Detik</strong></span></li>
                                        <li><i class="bi bi-graph-up-arrow text-info"></i><span class="text-light">Bonus Jual: <strong>+5%</strong> • Bonus EXP: <strong>+8%</strong> • Bank: <strong>1.2x</strong></span></li>
                                        <li><i class="bi bi-terminal-fill text-info"></i><span class="text-light">/craft &amp; /enderchest Portabel</span></li>
                                        @if($isPermanent)
                                            <li class="pt-1 border-top border-info border-opacity-25"><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 80.000 Server Money (1x Klaim)</span></li>
                                            <li><i class="bi bi-shield-check text-info"></i><span class="text-light">Kit Archon + Akses Kit Ascendant</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-info-circle me-1"></i>Masa aktif benefit berlaku selama {{ $isTrial90 ? '90' : '30' }} hari.</li>
                                        @endif
                                    @elseif(str_contains($packageName, 'ascendant'))
                                        <li><i class="bi bi-house-door-fill text-success"></i><span class="text-light">3 Homes • 4 Slot Lelang • 5 Custom Enchants</span></li>
                                        <li><i class="bi bi-clock-history text-success"></i><span class="text-light">RTP Cooldown: <strong>2 Menit 15 Detik</strong></span></li>
                                        <li><i class="bi bi-graph-up-arrow text-success"></i><span class="text-light">Bonus Jual: <strong>+3%</strong> • Bonus EXP: <strong>+5%</strong></span></li>
                                        @if($isPermanent)
                                            <li class="pt-1 border-top border-success border-opacity-25"><i class="bi bi-currency-dollar text-warning"></i><span class="text-warning fw-bold">Bonus Tunai: Rp 50.000 Server Money (1x Klaim)</span></li>
                                            <li><i class="bi bi-shield-check text-success"></i><span class="text-light">Kit Ascendant (/kits &amp; /kit ascendant)</span></li>
                                        @else
                                            <li class="text-info small"><i class="bi bi-info-circle me-1"></i>Masa aktif benefit berlaku selama {{ $isTrial90 ? '90' : '30' }} hari.</li>
                                        @endif
                                    @elseif(str_contains($packageName, 'exsio pass'))
                                        <li><i class="bi bi-check2-circle text-info"></i><span class="text-light fw-bold">Otomatis Membuka Sio Pass Penuh (100 Level)</span></li>
                                        <li><i class="bi bi-lightning-charge-fill text-warning"></i><span class="text-light">Langsung Melompat +20 Level Battlepass Awal</span></li>
                                        <li><i class="bi bi-stars text-warning"></i><span class="text-light">Kosmetik Mitos: Sayap, Aura Partikel, Gelar Chat</span></li>
                                        <li><i class="bi bi-cash text-success"></i><span class="text-light">Bonus Tunai Rp 50.000 Saldo In-game &amp; Crate Keys</span></li>
                                        <li><i class="bi bi-percent text-info"></i><span class="text-light">Diskon 15% untuk Pemegang Rank Sions Permanen</span></li>
                                    @elseif(str_contains($packageName, 'sio pass'))
                                        <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Buka Seluruh 100 Tier Jalur Hadiah Emas Musiman</span></li>
                                        <li><i class="bi bi-trophy-fill text-warning"></i><span class="text-light">Akses Quests Harian, Mingguan &amp; EXP Shop</span></li>
                                        <li><i class="bi bi-gift-fill text-warning"></i><span class="text-light">Kosmetik Eksklusif Musiman, Title, dan Partikel</span></li>
                                        <li><i class="bi bi-percent text-info"></i><span class="text-light">Diskon 10% (Emperor) &amp; 15% (Sions) Pemegang Rank Permanen</span></li>
                                    @else
                                        <li><i class="bi bi-check2-circle text-warning"></i><span class="text-light">Aktivasi Otomatis Langsung ke Akun Minecraft</span></li>
                                        <li><i class="bi bi-shield-check text-warning"></i><span class="text-light">Dukungan Transaksi Terverifikasi &amp; Aman</span></li>
                                    @endif
                                </ul>

                                <!-- Founder Direct Order Quick Selector -->
                                <div class="mb-3 pt-2 border-top border-secondary border-opacity-15">
                                    <div class="small text-muted mb-2 d-flex align-items-center justify-content-between" style="font-size: 0.75rem;">
                                        <span><i class="bi bi-whatsapp text-success me-1"></i> <span data-i18n="shop_choose_founder">Pilih Founder:</span></span>
                                        <span class="text-dim" data-i18n="shop_direct_order">Pesan Langsung</span>
                                    </div>
                                    <div class="d-flex flex-wrap gap-1">
                                        @foreach($founderAdmins as $adm)
                                            @php
                                                $admNum = preg_replace('/[^0-9]/', '', $adm['number']);
                                                if ($isUpgradeAvailable && $upgradeCalculation) {
                                                    $template = setting('apexsions.whatsapp.template_upgrade', 'Min, aku mau upgrade rank dari {rank lama} ke {rank baru} yang harganya Rp.{harga upgrade}');
                                                    $formattedPrice = number_format($upgradeCalculation['upgrade_price'], 0, ',', '.');
                                                    $upgradeMsg = str_replace(['{rank lama}', '{rank baru}', '{harga upgrade}'], [ucfirst($accountRank), ucfirst($rankKey), $formattedPrice], $template);
                                                    $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($upgradeMsg);
                                                } else {
                                                    $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($discountInfo['whatsapp_message']);
                                                }
                                            @endphp
                                            <a href="{{ $admUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-success py-1 px-2 d-flex align-items-center gap-1" style="font-size: 0.75rem;" title="Pesan paket ini via Founder {{ $adm['name'] }}">
                                                 <i class="bi bi-whatsapp"></i> {{ $adm['name'] }}
                                            </a>
                                        @endforeach
                                    </div>
                                </div>

                                <div class="apx-package-footer mt-auto d-flex flex-column gap-2">
                                    @if($isAlreadyOwned)
                                        <button class="btn btn-outline-success w-100 py-2 disabled" disabled>
                                            <i class="bi bi-check2-circle me-1"></i> Rank Sudah Dimiliki
                                        </button>
                                    @elseif($isUpgradeAvailable)
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-warning fw-bold text-dark w-100 py-2 shadow-sm">
                                            <i class="bi bi-arrow-up-circle-fill me-1"></i> Upgrade ke {{ ucfirst($rankKey) }} via WA
                                        </a>
                                    @else
                                        <a href="{{ $primaryWaUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa w-100 py-2">
                                            <i class="bi bi-whatsapp me-1"></i> <span data-i18n="shop_btn_wa">Pesan Cepat via WhatsApp</span>
                                        </a>
                                    @endif
                                    <a href="#" class="btn btn-apx-outline w-100 py-1 small" data-package-url="{{ route('shop.packages.show', $package) }}">
                                        <i class="bi bi-info-circle me-1"></i> <span data-i18n="shop_btn_details">Rincian &amp; Benefit Lengkap</span>
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
