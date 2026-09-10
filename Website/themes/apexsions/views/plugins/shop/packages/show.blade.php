@php
    $linkedAccount = auth()->check() ? \Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount::where('user_id', auth()->id())->first() : null;
    $accountRank = $linkedAccount ? strtolower(trim($linkedAccount->rank ?? 'wanderer')) : 'wanderer';
    $isAccountPerm = $linkedAccount ? $linkedAccount->isPermanentRank() : false;

    $packageName = strtolower($package->name);
    $defaultImage = null;
    $rankCrest = null;
    $badgeText = null;
    $badgeClass = '';

    $isTrial30 = str_contains($packageName, '30 hari') || str_contains($packageName, 'trial 30');
    $isTrial90 = str_contains($packageName, '90 hari') || str_contains($packageName, 'trial 90');
    $isTrial = $isTrial30 || $isTrial90 || str_contains($packageName, 'trial');
    $isPermanent = str_contains($packageName, 'permanen');

    if (str_contains($packageName, 'sions')) {
        $defaultImage = theme_asset('img/package-sions.jpg');
        $rankCrest = theme_asset('img/ranks/rank-sions.png');
        $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90 HARI' : 'TRIAL 30 HARI');
        $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
    } elseif (str_contains($packageName, 'emperor')) {
        $defaultImage = theme_asset('img/package-emperor.jpg');
        $rankCrest = theme_asset('img/ranks/rank-emperor.png');
        $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90 HARI' : 'TRIAL 30 HARI');
        $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
    } elseif (str_contains($packageName, 'sovereign')) {
        $defaultImage = theme_asset('img/package-sovereign.jpg');
        $rankCrest = theme_asset('img/ranks/rank-sovereign.png');
        $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90 HARI' : 'TRIAL 30 HARI');
        $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
    } elseif (str_contains($packageName, 'archon')) {
        $defaultImage = theme_asset('img/package-archon.jpg');
        $rankCrest = theme_asset('img/ranks/rank-archon.png');
        $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90 HARI' : 'TRIAL 30 HARI');
        $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
    } elseif (str_contains($packageName, 'ascendant')) {
        $defaultImage = theme_asset('img/package-ascendant.jpg');
        $rankCrest = theme_asset('img/ranks/rank-ascendant.png');
        $badgeText = $isPermanent ? 'PERMANEN' : ($isTrial90 ? 'TRIAL 90 HARI' : 'TRIAL 30 HARI');
        $badgeClass = $isPermanent ? 'apx-badge-perm' : 'apx-badge-trial';
    } elseif (str_contains($packageName, 'exsio pass')) {
        $defaultImage = theme_asset('img/package-exsio-pass.jpg');
        $badgeText = 'ULTIMATE PASS';
        $badgeClass = 'bg-primary text-white border border-info';
    } elseif (str_contains($packageName, 'sio pass') || str_contains($packageName, 'pass')) {
        $defaultImage = theme_asset('img/package-sio-pass.jpg');
        $badgeText = 'SEASON PASS';
        $badgeClass = 'bg-warning text-dark border border-warning';
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

    $modalAdmins = config('services.whatsapp.admins', [
        ['name' => 'Rifqi', 'number' => '6281212994597', 'role' => 'Founder'],
        ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
    ]);

    $primaryModalAdmin = $modalAdmins[0] ?? ['name' => 'Rifqi', 'number' => '6281212994597'];
    $primaryModalNum = preg_replace('/[^0-9]/', '', $primaryModalAdmin['number']);
    if ($isUpgradeAvailable && $upgradeWaUrl) {
        $primaryModalUrl = $upgradeWaUrl;
    } else {
        $primaryModalUrl = 'https://wa.me/' . $primaryModalNum . '?text=' . rawurlencode($discountInfo['whatsapp_message']);
    }
@endphp

<div class="modal-dialog modal-lg modal-dialog-centered" role="document">
    <div class="modal-content" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border); box-shadow: var(--apx-shadow-elevated); border-radius: var(--apx-radius-lg); overflow: hidden;">
        <div class="modal-header" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-gold-border-subtle); padding: 1.25rem 1.5rem;">
            <div class="d-flex align-items-center gap-3">
                @if($rankCrest)
                    <img src="{{ $rankCrest }}" alt="Crest" style="width: 44px; height: 44px; filter: drop-shadow(0 2px 8px rgba(0,0,0,0.6));">
                @else
                    <div class="d-flex align-items-center justify-content-center" style="width: 42px; height: 42px; border-radius: 10px; background: rgba(245, 158, 11, 0.15); color: var(--apx-gold); font-size: 1.4rem; border: 1px solid var(--apx-gold-border);">
                        <i class="bi bi-shield-shaded"></i>
                    </div>
                @endif
                <div>
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <h3 class="modal-title mb-0" id="itemModalLabel" style="font-family: 'Cinzel', Georgia, serif; color: #ffffff; font-size: 1.35rem;">{{ $package->name }}</h3>
                        @if($badgeText)
                            <span class="badge {{ $badgeClass }}" style="font-size: 0.68rem;">{{ $badgeText }}</span>
                        @endif
                    </div>
                    <div class="small text-muted" style="font-size: 0.78rem;">Webstore Resmi Realm Apexsions &bull; Transaksi Langsung &amp; Terverifikasi</div>
                </div>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body p-4" style="color: var(--apx-text-sub); line-height: 1.7;">
            @if($package->hasImage() || $defaultImage)
                <div class="mb-4 text-center rounded-3 overflow-hidden" style="max-height: 220px; border: 1px solid rgba(255,255,255,0.08); background: #0b0f19;">
                    <img src="{{ $package->hasImage() ? $package->imageUrl() : $defaultImage }}" alt="{{ $package->name }}" style="width: 100%; height: 220px; object-fit: cover;">
                </div>
            @endif

            @if($discountInfo['has_discount'])
                <div class="alert alert-success d-flex align-items-center justify-content-between flex-wrap gap-2 py-2 px-3 mb-3" style="background: rgba(34, 197, 94, 0.15); border: 1px solid rgba(34, 197, 94, 0.4); border-radius: 8px;">
                    <div class="d-flex align-items-center gap-2">
                        <i class="bi bi-tag-fill text-success fs-5"></i>
                        <div>
                            <strong class="text-white">Diskon Khusus Pemegang Rank {{ $discountInfo['eligible_rank'] }} Aktif!</strong>
                            <div class="small text-muted">Harga dipotong otomatis sebesar {{ $discountInfo['discount_percent'] }}% berdasarkan akun Minecraft terhubung.</div>
                        </div>
                    </div>
                    <span class="badge bg-success text-dark fw-bold px-2 py-1">Hemat Rp {{ number_format($discountInfo['savings'], 0, ',', '.') }}</span>
                </div>
            @endif

            @if($discountInfo['is_free_current_season'])
                <div class="alert alert-info py-2 px-3 mb-3 d-flex align-items-center gap-2" style="background: rgba(6, 182, 212, 0.15); border: 1px solid rgba(6, 182, 212, 0.4); border-radius: 8px; color: #67e8f9;">
                    <i class="bi bi-gift-fill text-warning fs-5"></i>
                    <div>
                        <strong>Hak Istimewa Musiman Aktif:</strong> Akun Minecraft kamu berhak mengklaim BattlePass musim ini secara <strong>GRATIS</strong> melalui Admin atau sistem in-game.
                    </div>
                </div>
            @endif

            <div class="p-3 mb-4 rounded-3" style="background: var(--apx-bg-surface-raised); border: 1px solid var(--apx-border);">
                {!! \Illuminate\Support\Str::markdown($package->description) !!}
            </div>

            <!-- WhatsApp Direct Order Notice in Modal -->
            <div class="p-3 mt-4 rounded-3" style="background: linear-gradient(135deg, rgba(34, 197, 94, 0.09) 0%, rgba(15, 23, 42, 0.95) 100%); border: 1px solid rgba(34, 197, 94, 0.35);">
                <div class="d-flex align-items-center justify-content-between mb-2 flex-wrap gap-2">
                    <div class="d-flex align-items-center gap-2 text-white fw-bold">
                        <i class="bi bi-whatsapp text-success fs-5"></i>
                        <span>Pesan Langsung via WhatsApp Founder:</span>
                    </div>
                    <span class="badge bg-success bg-opacity-25 text-success font-monospace" style="font-size: 0.65rem;">VERIFIED</span>
                </div>
                <p class="text-muted small mb-3" style="line-height: 1.5;">
                    Pilih salah satu Founder untuk memulai chat WhatsApp dengan pesan pemesanan yang otomatis memuat nama produk, nominal akurat, dan status kasta akun:
                </p>
                <div class="row g-2">
                    @foreach($modalAdmins as $adm)
                        @php
                            $admNum = preg_replace('/[^0-9]/', '', $adm['number']);
                            $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($discountInfo['whatsapp_message']);
                        @endphp
                        <div class="col-md-6 col-12">
                            <a href="{{ $admUrl }}" target="_blank" rel="noopener noreferrer" class="apx-founder-contact-card" title="Chat dengan Founder {{ $adm['name'] }}">
                                <div class="d-flex align-items-center gap-2">
                                    <i class="bi bi-whatsapp text-success fs-5"></i>
                                    <div>
                                        <div class="fw-bold text-white small">{{ $adm['name'] }}</div>
                                        <div class="text-dim" style="font-size: 0.68rem;">Founder Apexsions</div>
                                    </div>
                                </div>
                                <i class="bi bi-arrow-up-right-square text-success small"></i>
                            </a>
                        </div>
                    @endforeach
                </div>
            </div>
        </div>

        <div class="modal-footer d-flex justify-content-between align-items-center" style="background: var(--apx-bg-surface-raised); border-top: 1px solid var(--apx-border); padding: 1.25rem 1.5rem;">
            <div class="d-flex align-items-baseline gap-2">
                @if($isAlreadyOwned)
                    <span class="fs-5 fw-bold text-success"><i class="bi bi-patch-check-fill me-1"></i> Rank Sudah Dimiliki</span>
                @elseif($isUpgradeAvailable)
                    <span class="text-muted text-decoration-line-through small">Rp {{ number_format($upgradeCalculation['target_rank_price'], 0, ',', '.') }}</span>
                    <span class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif; color: #f1c40f !important;">
                        Rp {{ number_format($upgradeCalculation['upgrade_price'], 0, ',', '.') }}
                    </span>
                    <span class="badge bg-purple text-white ms-1" style="background: linear-gradient(135deg, #8E2DE2, #4A00E0); font-size: 0.65rem;">UPGRADE</span>
                @elseif($discountInfo['has_discount'])
                    <span class="text-muted text-decoration-line-through small">Rp {{ number_format($discountInfo['original_price'], 0, ',', '.') }}</span>
                    <span class="fs-4 fw-bold text-success" style="font-family: 'Cinzel', Georgia, serif;">
                        Rp {{ number_format($discountInfo['discounted_price'], 0, ',', '.') }}
                    </span>
                @elseif($package->isDiscounted())
                    <span class="text-muted text-decoration-line-through small">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                    <span class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif;">
                        {{ shop_format_amount($package->getPrice()) }}
                    </span>
                @else
                    <span class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif;">
                        Rp {{ number_format($package->getPrice(), 0, ',', '.') }}
                    </span>
                @endif
            </div>

            <div class="d-flex align-items-center gap-2">
                <button type="button" class="btn btn-apx-outline" data-bs-dismiss="modal">Tutup</button>
                @if($isAlreadyOwned)
                    <button class="btn btn-outline-success disabled" disabled>
                        <i class="bi bi-check2-circle me-1"></i> Sudah Aktif
                    </button>
                @elseif($isUpgradeAvailable)
                    <a href="{{ $primaryModalUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-warning fw-bold text-dark shadow-sm">
                        <i class="bi bi-arrow-up-circle-fill me-1"></i> Upgrade ke {{ ucfirst($rankKey) }} via WA
                    </a>
                @else
                    <a href="{{ $primaryModalUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa">
                        <i class="bi bi-whatsapp me-1"></i> <span>Pesan Cepat via WhatsApp</span>
                    </a>
                @endif
            </div>
        </div>
    </div>
</div>
