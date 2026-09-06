<div class="modal-dialog modal-lg modal-dialog-centered" role="document">
    <div class="modal-content" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border); box-shadow: var(--apx-shadow-elevated); border-radius: var(--apx-radius-lg); overflow: hidden;">
        <div class="modal-header" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-gold-border-subtle); padding: 1.25rem 1.5rem;">
            <div class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center justify-content-center" style="width: 42px; height: 42px; border-radius: 10px; background: rgba(245, 158, 11, 0.15); color: var(--apx-gold); font-size: 1.4rem; border: 1px solid var(--apx-gold-border);">
                    <i class="bi bi-shield-shaded"></i>
                </div>
                <div>
                    <h3 class="modal-title mb-0" id="itemModalLabel" style="font-family: 'Cinzel', Georgia, serif; color: #ffffff; font-size: 1.35rem;">{{ $package->name }}</h3>
                    <div class="small text-muted" style="font-size: 0.78rem;">Webstore Resmi Apexsions &bull; Transaksi Langsung &amp; Aman</div>
                </div>
            </div>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>

        <div class="modal-body p-4" style="color: var(--apx-text-sub); line-height: 1.7;">
            <div class="p-3 mb-4 rounded-3" style="background: var(--apx-bg-surface-raised); border: 1px solid var(--apx-border);">
                {!! \Illuminate\Support\Str::markdown($package->description) !!}
            </div>

            @php
                $userIgn = (auth()->check() ? auth()->user()->name : null) ?? 'Username_Minecraft_Kamu';
                $userEmail = auth()->check() ? auth()->user()->email : '-';
                $priceFormatted = shop_format_amount($package->getPrice());
                $categoryName = $package->category ? $package->category->name : 'Paket Server';

                $waBaseText = "Halo Admin Apexsions! Saya ingin memesan paket dari Webstore resmi:\n\n"
                    . "👑 Paket: " . $package->name . "\n"
                    . "💰 Harga: " . $priceFormatted . "\n"
                    . "📂 Kategori: " . $categoryName . "\n"
                    . "🎮 Akun Minecraft (IGN): " . $userIgn . "\n"
                    . "📧 Email Akun: " . $userEmail . "\n\n"
                    . "Mohon nomor rekening/QRIS dan instruksi aktivasi peradaban. Terima kasih!";

                $modalAdmins = config('services.whatsapp.admins', [
                    ['name' => 'Rifqi', 'number' => '6281212994597', 'role' => 'Founder'],
                    ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
                    ['name' => 'Favian', 'number' => '6287729112281', 'role' => 'Founder'],
                ]);
            @endphp

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
                    Pilih salah satu Founder untuk memulai chat WhatsApp dengan data pesanan Anda yang telah terisi secara otomatis:
                </p>
                <div class="row g-2">
                    @foreach($modalAdmins as $adm)
                        @php
                            $admNum = preg_replace('/[^0-9]/', '', $adm['number']);
                            $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($waBaseText);
                        @endphp
                        <div class="col-md-4 col-12">
                            <a href="{{ $admUrl }}" target="_blank" rel="noopener noreferrer" class="apx-founder-contact-card" title="Chat dengan Founder {{ $adm['name'] }}">
                                <div class="d-flex align-items-center gap-2">
                                    <i class="bi bi-whatsapp text-success fs-5"></i>
                                    <div>
                                        <div class="fw-bold text-white small">{{ $adm['name'] }}</div>
                                        <div class="text-dim" style="font-size: 0.68rem;">Founder</div>
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
                @if($package->isDiscounted())
                    <span class="text-muted text-decoration-line-through small">{{ shop_format_amount($package->getOriginalPrice()) }}</span>
                @endif
                <span class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif;">
                    {{ shop_format_amount($package->getPrice()) }}
                </span>
            </div>

            <div class="d-flex align-items-center gap-2">
                <button type="button" class="btn btn-apx-outline" data-bs-dismiss="modal">Tutup</button>
                @php
                    $primaryModalAdmin = $modalAdmins[0] ?? ['name' => 'Rifqi', 'number' => '6281212994597'];
                    $primaryModalNum = preg_replace('/[^0-9]/', '', $primaryModalAdmin['number']);
                    $primaryModalUrl = 'https://wa.me/' . $primaryModalNum . '?text=' . rawurlencode($waBaseText);
                @endphp
                <a href="{{ $primaryModalUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa">
                    <i class="bi bi-whatsapp me-1"></i> Pesan Cepat Sekarang
                </a>
            </div>
        </div>
    </div>
</div>
