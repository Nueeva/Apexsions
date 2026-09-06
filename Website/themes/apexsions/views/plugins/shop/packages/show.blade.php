<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border); box-shadow: var(--apx-shadow-elevated);">
        <div class="modal-header" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-gold-border-subtle);">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-shield-shaded text-warning fs-4"></i>
                <h3 class="modal-title mb-0" id="itemModalLabel" style="font-family: 'Cinzel', Georgia, serif; color: #ffffff;">{{ $package->name }}</h3>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-4" style="color: var(--apx-text-sub); line-height: 1.7;">
            {!! \Illuminate\Support\Str::markdown($package->description) !!}

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

                $admins = config('services.whatsapp.admins', [
                    ['name' => 'Rifqi', 'number' => '6285883161047', 'role' => 'Founder'],
                    ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
                    ['name' => 'Favian', 'number' => '6285883161047', 'role' => 'Founder'],
                ]);
            @endphp

            <!-- WhatsApp Direct Order Notice in Modal -->
            <div class="p-3 mt-4 rounded-3" style="background: linear-gradient(135deg, rgba(34, 197, 94, 0.09) 0%, rgba(15, 23, 42, 0.95) 100%); border: 1px solid rgba(34, 197, 94, 0.35);">
                <div class="d-flex align-items-center gap-2 mb-2 text-white fw-bold">
                    <i class="bi bi-whatsapp text-success fs-5"></i>
                    <span>Pesan Langsung via WhatsApp Founder:</span>
                </div>
                <p class="text-muted small mb-3" style="line-height: 1.5;">
                    Gateway Midtrans sedang dalam proses pengajuan. Pilih salah satu Founder untuk memulai chat WhatsApp dengan data pesanan Anda yang otomatis terisi:
                </p>
                <div class="d-flex flex-wrap gap-2">
                    @foreach($admins as $adm)
                        @php
                            $admNum = preg_replace('/[^0-9]/', '', $adm['number']);
                            $admUrl = 'https://wa.me/' . $admNum . '?text=' . rawurlencode($waBaseText);
                        @endphp
                        <a href="{{ $admUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa px-3 py-2 flex-grow-1" style="font-size: 0.85rem;">
                            <i class="bi bi-whatsapp"></i>
                            <span>Hubungi {{ $adm['name'] }} (Founder)</span>
                        </a>
                    @endforeach
                </div>
            </div>
        </div>
        <div class="modal-footer d-flex justify-content-between align-items-center" style="background: var(--apx-bg-surface-raised); border-top: 1px solid var(--apx-border);">
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
                    $primaryAdmin = $admins[0] ?? ['name' => 'Rifqi', 'number' => '6285883161047'];
                    $primaryNum = preg_replace('/[^0-9]/', '', $primaryAdmin['number']);
                    $primaryUrl = 'https://wa.me/' . $primaryNum . '?text=' . rawurlencode($waBaseText);
                @endphp
                <a href="{{ $primaryUrl }}" target="_blank" rel="noopener noreferrer" class="btn btn-apx-wa">
                    <i class="bi bi-whatsapp me-1"></i> Pesan Cepat Sekarang
                </a>
            </div>
        </div>
    </div>
</div>
