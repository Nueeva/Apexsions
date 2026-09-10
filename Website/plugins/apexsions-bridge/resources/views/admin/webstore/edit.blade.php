@extends('admin.layouts.admin')

@section('title', 'Edit Paket Webstore — ' . $package->name)

@section('content')
<div class="mb-4">
    <!-- Header Hero -->
    <div class="card p-4" style="background: linear-gradient(135deg, #181b24 0%, #111319 100%); border: 1px solid rgba(201, 164, 92, 0.35); box-shadow: 0 10px 30px rgba(0,0,0,0.85);">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
                <div class="d-flex align-items-center gap-2 mb-1">
                    <a href="{{ route('apexsions-bridge.admin.webstore.index') }}" class="btn btn-sm btn-outline-secondary text-light">
                        <i class="bi bi-arrow-left me-1"></i> Kembali ke Daftar
                    </a>
                    <span class="badge bg-warning text-dark fw-bold px-2 py-1 font-monospace">PAKET #{{ $package->id }}</span>
                </div>
                <h3 class="mb-0 fw-bold text-white" style="font-family: 'Cinzel', serif; letter-spacing: 1.5px;">
                    🛠️ KELOLA: {{ $package->name }}
                </h3>
                <p class="text-muted small mb-0 mt-1">
                    Kategori: <strong class="text-warning">{{ $package->category->name ?? 'Uncategorized' }}</strong> &bull; File Gambar Saat Ini: <code class="text-info">{{ $package->image ?: 'None (Fallback Ikon)' }}</code>
                </p>
            </div>
            <div class="d-flex flex-wrap align-items-center gap-2">
                @if($package->category)
                    <a href="{{ route('shop.categories.show', $package->category) }}" target="_blank" class="btn btn-outline-warning fw-bold px-3 shadow-sm">
                        <i class="bi bi-box-arrow-up-right me-1"></i> Lihat di Webstore
                    </a>
                @endif
            </div>
        </div>
    </div>
</div>

@if(isset($errors) && $errors->any())
    <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
        <h6 class="fw-bold mb-2"><i class="bi bi-exclamation-triangle-fill me-1"></i> Terdapat kesalahan pada formulir:</h6>
        <ul class="mb-0 ps-3">
            @foreach($errors->all() as $error)
                <li>{{ $error }}</li>
            @endforeach
        </ul>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
@endif

<form action="{{ route('apexsions-bridge.admin.webstore.update', $package->id) }}" method="POST" enctype="multipart/form-data">
    @csrf
    @method('PUT')

    <div class="row g-4">
        <!-- Main Configuration Tabs (Left Column) -->
        <div class="col-lg-8">
            <!-- Nav Tabs -->
            <ul class="nav nav-tabs nav-fill mb-3" id="webstoreEditTabs" role="tablist" style="border-bottom: 2px solid rgba(255,255,255,0.1);">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active fw-bold text-white py-3" id="media-tab" data-bs-toggle="tab" data-bs-target="#media-tab-pane" type="button" role="tab" aria-controls="media-tab-pane" aria-selected="true">
                        <i class="bi bi-image-fill me-1 text-warning"></i> 1. Banner &amp; Visual
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link fw-bold text-white py-3" id="info-tab" data-bs-toggle="tab" data-bs-target="#info-tab-pane" type="button" role="tab" aria-controls="info-tab-pane" aria-selected="false">
                        <i class="bi bi-info-circle-fill me-1 text-info"></i> 2. Info &amp; Tag Promo
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link fw-bold text-white py-3" id="pricing-tab" data-bs-toggle="tab" data-bs-target="#pricing-tab-pane" type="button" role="tab" aria-controls="pricing-tab-pane" aria-selected="false">
                        <i class="bi bi-cash-coin me-1 text-success"></i> 3. Harga &amp; WhatsApp
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link fw-bold text-white py-3" id="commands-tab" data-bs-toggle="tab" data-bs-target="#commands-tab-pane" type="button" role="tab" aria-controls="commands-tab-pane" aria-selected="false">
                        <i class="bi bi-terminal-fill me-1 text-danger"></i> 4. Commands In-Game
                    </button>
                </li>
            </ul>

            <div class="tab-content" id="webstoreEditTabsContent">
                <!-- TAB 1: MEDIA & BANNER VISUAL -->
                <div class="tab-pane fade show active" id="media-tab-pane" role="tabpanel" aria-labelledby="media-tab">
                    <div class="card p-4 shadow-sm" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
                        <h5 class="fw-bold text-white mb-3 d-flex align-items-center gap-2">
                            <i class="bi bi-aspect-ratio text-warning"></i> Pengaturan Banner Produk (Rasio 16:9)
                        </h5>
                        <p class="text-muted small mb-4">
                            Banner produk ini ditampilkan di kartu katalog webstore, modal detail pembelian, dan halaman kategori. Format optimal: <strong>16:9 (1280x720 atau 1920x1080)</strong>, format JPG/PNG/WebP maks 3MB.
                        </p>

                        <!-- Live Banner Preview Container -->
                        <div class="mb-4">
                            <label class="form-label text-muted small fw-bold text-uppercase">Live Banner Preview</label>
                            <div class="position-relative overflow-hidden shadow" style="width: 100%; max-width: 580px; aspect-ratio: 16/9; border-radius: 8px; border: 2px solid rgba(201, 164, 92, 0.4); background: #0b0d13;">
                                @php
                                    $currentImgUrl = $package->hasImage() ? $package->imageUrl() : null;
                                @endphp
                                <img id="liveBannerPreview" src="{{ $currentImgUrl ?: asset('assets/themes/apexsions/img/' . $package->image) }}" alt="Preview" style="width: 100%; height: 100%; object-fit: cover; {{ empty($package->image) ? 'display: none;' : '' }}" onerror="this.onerror=null; this.src='{{ asset('assets/themes/apexsions/img/' . $package->image) }}';">
                                <div id="noBannerFallback" class="w-100 h-100 d-flex flex-column align-items-center justify-content-center text-muted p-3 text-center" style="{{ !empty($package->image) ? 'display: none !important;' : '' }}">
                                    <i class="bi bi-card-image fs-1 text-secondary mb-2"></i>
                                    <span class="small">Belum ada banner terpasang. Pilih dari galeri preset di bawah atau upload banner baru.</span>
                                </div>
                            </div>
                        </div>

                        <!-- Option A: Upload Custom Banner File -->
                        <div class="mb-4 p-3 rounded" style="background: rgba(255,255,255,0.03); border: 1px dashed rgba(255,255,255,0.2);">
                            <label for="image_file" class="form-label fw-bold text-white d-flex align-items-center gap-2">
                                <i class="bi bi-cloud-arrow-up-fill text-info"></i> Opsi A: Upload Banner File dari Komputer
                            </label>
                            <input class="form-control bg-dark border-secondary text-white" type="file" id="image_file" name="image_file" accept="image/png,image/jpeg,image/webp" onchange="previewUploadedImage(this)">
                            <small class="text-muted d-block mt-1">Mengunggah file baru akan otomatis menyimpannya ke <code class="text-warning">storage/app/public/packages/</code> dan menggantikan banner saat ini.</small>
                        </div>

                        <!-- Option B: Select from Preset Official Server Images -->
                        <div class="mb-4">
                            <label class="form-label fw-bold text-white d-flex align-items-center justify-content-between">
                                <span><i class="bi bi-collection-play-fill text-warning me-1"></i> Opsi B: Pilih dari Galeri Preset Resmi Server</span>
                                <small class="text-muted fw-normal">Klik salah satu untuk memilih instan</small>
                            </label>
                            
                            <!-- Preset Hidden Input -->
                            <input type="hidden" name="image_preset" id="selectedPresetInput" value="{{ old('image_preset', $package->image) }}">

                            <div class="row g-2" style="max-height: 380px; overflow-y: auto; padding-right: 5px;">
                                @foreach($presets as $filename => $preset)
                                    @php
                                        $isSelected = ($package->image === $filename);
                                    @endphp
                                    <div class="col-sm-6 col-md-4">
                                        <div class="card h-100 preset-card p-2 text-center cursor-pointer position-relative {{ $isSelected ? 'border-warning shadow' : 'border-secondary' }}" 
                                             style="background: #111319; border: 2px solid {{ $isSelected ? '#ffd700' : 'rgba(255,255,255,0.1)' }}; border-radius: 6px; cursor: pointer; transition: all 0.2s ease;" 
                                             onclick="selectPreset('{{ $filename }}', '{{ $preset['preview'] }}', this)">
                                            <div class="position-relative overflow-hidden mb-2" style="aspect-ratio: 16/9; border-radius: 4px; background: #000;">
                                                <img src="{{ $preset['preview'] }}" alt="{{ $filename }}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.onerror=null; this.src='{{ asset('assets/themes/apexsions/img/' . $filename) }}';">
                                                @if($isSelected)
                                                    <div class="position-absolute top-0 end-0 bg-warning text-dark px-1 font-monospace small fw-bold" style="border-bottom-left-radius: 4px;">
                                                        <i class="bi bi-check-circle-fill"></i> DIPILIH
                                                    </div>
                                                @endif
                                            </div>
                                            <div class="text-truncate small fw-bold text-white" title="{{ $preset['label'] }}" style="font-size: 0.76rem;">
                                                {{ $preset['label'] }}
                                            </div>
                                            <code class="text-muted small" style="font-size: 0.68rem;">{{ $filename }}</code>
                                        </div>
                                    </div>
                                @endforeach
                            </div>
                        </div>

                        <!-- Option C: Remove Image Checkbox -->
                        <div class="form-check form-switch pt-2 border-top border-secondary">
                            <input class="form-check-input" type="checkbox" role="switch" id="remove_image" name="remove_image" value="1" onchange="toggleRemoveImage(this)">
                            <label class="form-check-label text-danger small fw-bold" for="remove_image">
                                Hapus gambar banner (gunakan fallback ikon tema saja)
                            </label>
                        </div>
                    </div>
                </div>

                <!-- TAB 2: INFORMASI & PROMO TAG -->
                <div class="tab-pane fade" id="info-tab-pane" role="tabpanel" aria-labelledby="info-tab">
                    <div class="card p-4 shadow-sm" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
                        <h5 class="fw-bold text-white mb-3 d-flex align-items-center gap-2">
                            <i class="bi bi-file-earmark-text-fill text-info"></i> Detail Produk &amp; Tampilan Visual
                        </h5>

                        <div class="row g-3 mb-3">
                            <div class="col-md-8">
                                <label for="name" class="form-label text-white fw-bold">Nama Paket Produk <span class="text-danger">*</span></label>
                                <input type="text" class="form-control bg-dark border-secondary text-white" id="name" name="name" value="{{ old('name', $package->name) }}" required>
                            </div>
                            <div class="col-md-4">
                                <label for="category_id" class="form-label text-white fw-bold">Kategori Webstore <span class="text-danger">*</span></label>
                                <select class="form-select bg-dark border-secondary text-white" id="category_id" name="category_id" required>
                                    @foreach($categories as $cat)
                                        <option value="{{ $cat->id }}" @selected(old('category_id', $package->category_id) == $cat->id)>{{ $cat->name }}</option>
                                    @endforeach
                                </select>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="custom_badge" class="form-label text-white fw-bold">
                                    Tag Promo Kustom (Badge Pita)
                                </label>
                                <div class="input-group">
                                    <span class="input-group-text bg-dark border-secondary text-warning"><i class="bi bi-tag"></i></span>
                                    <input type="text" class="form-control bg-dark border-secondary text-white" id="custom_badge" name="custom_badge" value="{{ old('custom_badge', $customBadge) }}" placeholder="Contoh: HOT DEAL, BEST VALUE, POPULER, PERMANEN">
                                </div>
                                <small class="text-muted">Pita emas mencolok di pojok kanan atas kartu katalog.</small>
                            </div>
                            <div class="col-md-3">
                                <label for="position" class="form-label text-white fw-bold">Urutan Tampilan</label>
                                <input type="number" class="form-control bg-dark border-secondary text-white" id="position" name="position" value="{{ old('position', $package->position) }}" min="0" required>
                                <small class="text-muted">Angka lebih kecil tampil lebih dulu.</small>
                            </div>
                            <div class="col-md-3">
                                <label for="user_limit" class="form-label text-white fw-bold">Limit / Akun</label>
                                <input type="number" class="form-control bg-dark border-secondary text-white" id="user_limit" name="user_limit" value="{{ old('user_limit', $package->user_limit) }}" min="0" placeholder="Kosong = Bebas">
                                <small class="text-muted">Maksimal beli per akun player.</small>
                            </div>
                        </div>

                        <div class="mb-3">
                            <div class="form-check form-switch p-3 rounded" style="background: rgba(234, 179, 8, 0.08); border: 1px solid rgba(234, 179, 8, 0.2);">
                                <input class="form-check-input ms-0 me-2" type="checkbox" role="switch" id="is_featured" name="is_featured" value="1" @checked(old('is_featured', $isFeatured))>
                                <label class="form-check-label text-white fw-bold" for="is_featured">
                                    <i class="bi bi-star-fill text-warning me-1"></i> Jadikan Produk Unggulan di Beranda Toko (Featured Package)
                                </label>
                                <small class="text-muted d-block mt-1">Paket ini akan disorot di etalase beranda webstore untuk menarik perhatian pemain baru.</small>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="short_description" class="form-label text-white fw-bold">Deskripsi Ringkas (1-2 Kalimat)</label>
                            <input type="text" class="form-control bg-dark border-secondary text-white" id="short_description" name="short_description" value="{{ old('short_description', $package->short_description) }}" placeholder="Penjelasan singkat benefit di kartu katalog...">
                        </div>

                        <div class="mb-0">
                            <label for="description" class="form-label text-white fw-bold">Deskripsi Lengkap &amp; Rincian Hak Istimewa (Markdown / HTML)</label>
                            <textarea class="form-control bg-dark border-secondary text-white font-monospace" id="description" name="description" rows="7" placeholder="Rincian lengkap fasilitas, command, bonus...">{!! old('description', $package->description) !!}</textarea>
                            <small class="text-muted">Mendukung format Markdown atau HTML untuk bullet points di modal rincian paket.</small>
                        </div>
                    </div>
                </div>

                <!-- TAB 3: HARGA & WHATSAPP ROUTING -->
                <div class="tab-pane fade" id="pricing-tab-pane" role="tabpanel" aria-labelledby="pricing-tab">
                    <div class="card p-4 shadow-sm" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
                        <h5 class="fw-bold text-white mb-3 d-flex align-items-center gap-2">
                            <i class="bi bi-currency-dollar text-success"></i> Pengaturan Harga &amp; Routing WhatsApp
                        </h5>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="price" class="form-label text-white fw-bold">Harga Normal Satuan (IDR) <span class="text-danger">*</span></label>
                                <div class="input-group">
                                    <span class="input-group-text bg-dark border-secondary text-warning fw-bold">Rp</span>
                                    <input type="number" class="form-control bg-dark border-secondary text-white font-monospace fs-5" id="price" name="price" value="{{ old('price', $package->price) }}" min="0" step="500" required>
                                </div>
                                <small class="text-muted">Harga dasar dalam Rupiah sebelum diskon kasta.</small>
                            </div>
                            <div class="col-md-6">
                                <label for="discount_percent" class="form-label text-white fw-bold">Diskon Promo Khusus (%)</label>
                                <div class="input-group">
                                    <input type="number" class="form-control bg-dark border-secondary text-white font-monospace" id="discount_percent" name="discount_percent" value="{{ old('discount_percent', $discountPercent) }}" min="0" max="100" placeholder="0">
                                    <span class="input-group-text bg-dark border-secondary text-light">%</span>
                                </div>
                                <small class="text-muted">Opsional: memberikan harga coret promo langsung pada paket ini.</small>
                            </div>
                        </div>

                        <div class="mb-4 p-3 rounded" style="background: rgba(34, 197, 94, 0.08); border: 1px solid rgba(34, 197, 94, 0.25);">
                            <h6 class="fw-bold text-success mb-2 d-flex align-items-center gap-2">
                                <i class="bi bi-whatsapp"></i> WhatsApp Direct Order Routing
                            </h6>
                            <p class="text-muted small mb-3">
                                Karena checkout payment gateway otomatis dialihkan ke WhatsApp, tentukan Founder/Admin mana yang menerima chat saat tombol "Pesan via WhatsApp" diklik pada paket ini.
                            </p>

                            <div class="mb-3">
                                <label for="wa_admin" class="form-label text-white fw-bold">Penerima WhatsApp Utama</label>
                                <select class="form-select bg-dark border-secondary text-white" id="wa_admin" name="wa_admin">
                                    @foreach($waAdminOptions as $num => $label)
                                        <option value="{{ $num }}" @selected(old('wa_admin', $waAdmin) === $num)>{{ $label }}</option>
                                    @endforeach
                                </select>
                            </div>

                            <div class="mb-0">
                                <label for="wa_template" class="form-label text-white fw-bold">Template Pesan WhatsApp Kustom (Opsional)</label>
                                <textarea class="form-control bg-dark border-secondary text-white" id="wa_template" name="wa_template" rows="3" placeholder="Biarkan kosong untuk menggunakan template otomatis peradaban Apexsions...">{{ old('wa_template', $waTemplate) }}</textarea>
                                <small class="text-muted">Jika diisi, pesan ini akan digunakan saat membuka aplikasi WhatsApp pembeli.</small>
                            </div>
                        </div>

                        <div class="form-check form-switch p-3 rounded" style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.1);">
                            <input class="form-check-input ms-0 me-2" type="checkbox" role="switch" id="is_enabled" name="is_enabled" value="1" @checked(old('is_enabled', $package->is_enabled))>
                            <label class="form-check-label text-white fw-bold" for="is_enabled">
                                <i class="bi bi-check-circle-fill text-success me-1"></i> Paket Aktif &amp; Ditampilkan di Webstore
                            </label>
                            <small class="text-muted d-block mt-1">Jika dinonaktifkan, paket tidak akan dapat dilihat atau dibeli oleh pemain umum.</small>
                        </div>
                    </div>
                </div>

                <!-- TAB 4: IN-GAME CONSOLE COMMANDS -->
                <div class="tab-pane fade" id="commands-tab-pane" role="tabpanel" aria-labelledby="commands-tab">
                    <div class="card p-4 shadow-sm" style="background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(255,255,255,0.08);">
                        <h5 class="fw-bold text-white mb-3 d-flex align-items-center gap-2">
                            <i class="bi bi-terminal-fill text-danger"></i> Perintah Konsol In-Game Minecraft
                        </h5>
                        <p class="text-muted small mb-3">
                            Daftar baris perintah yang otomatis dieksekusi server Minecraft saat paket ini diaktifkan atau dikirimkan ke pemain. Tulis <strong>satu baris perintah per baris</strong>. Gunakan placeholder <code class="text-warning">{player}</code> untuk nama akun pemain.
                        </p>

                        <div class="mb-3">
                            <label for="commands_raw" class="form-label text-white fw-bold">Baris Perintah Konsol</label>
                            <textarea class="form-control bg-dark border-secondary text-white font-monospace" id="commands_raw" name="commands_raw" rows="8" placeholder="lp user {player} parent addtemp ascendant 30d&#10;eco give {player} 50000&#10;title {player} title {&quot;text&quot;:&quot;Pangkat Aktif!&quot;}">{{ old('commands_raw', $commandsRaw) }}</textarea>
                        </div>

                        <div class="alert alert-dark border-secondary text-light small mb-0">
                            <div class="fw-bold text-warning mb-1"><i class="bi bi-lightbulb-fill me-1"></i> Contoh Perintah Bawaan Apexsions:</div>
                            <ul class="mb-0 ps-3">
                                <li><code>lp user {player} parent addtemp ascendant 30d</code> — Beri rank LuckPerms 30 hari</li>
                                <li><code>lp user {player} parent set sions</code> — Beri rank LuckPerms permanen</li>
                                <li><code>eco give {player} 100000</code> — Beri uang bonus ekonomi</li>
                                <li><code>broadcast &amp;6[Apexsions]&amp;e {player} baru saja mengaktifkan paket {package}!</code> — Pengumuman realm</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Sidebar Summary & Save Card (Right Column) -->
        <div class="col-lg-4">
            <div class="card p-4 shadow-sm sticky-top" style="top: 20px; background: rgba(18, 20, 29, 0.95); border: 1px solid rgba(201, 164, 92, 0.35);">
                <h5 class="fw-bold text-white mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-floppy-fill text-warning"></i> Simpan Perubahan
                </h5>

                <ul class="list-group list-group-flush bg-transparent mb-4">
                    <li class="list-group-item bg-transparent text-muted px-0 d-flex justify-content-between">
                        <span>ID Paket:</span>
                        <strong class="text-white font-monospace">#{{ $package->id }}</strong>
                    </li>
                    <li class="list-group-item bg-transparent text-muted px-0 d-flex justify-content-between">
                        <span>Kategori:</span>
                        <strong class="text-warning">{{ $package->category->name ?? '-' }}</strong>
                    </li>
                    <li class="list-group-item bg-transparent text-muted px-0 d-flex justify-content-between">
                        <span>Harga:</span>
                        <strong class="text-success font-monospace">Rp {{ number_format($package->price, 0, ',', '.') }}</strong>
                    </li>
                    <li class="list-group-item bg-transparent text-muted px-0 d-flex justify-content-between">
                        <span>Status Saat Ini:</span>
                        @if($package->is_enabled)
                            <span class="badge bg-success">Aktif</span>
                        @else
                            <span class="badge bg-danger">Nonaktif</span>
                        @endif
                    </li>
                </ul>

                <button type="submit" class="btn btn-warning fw-bold py-2 w-100 shadow mb-2" style="font-size: 1rem;">
                    <i class="bi bi-check2-circle me-1"></i> Simpan Semua Perubahan
                </button>
                <a href="{{ route('apexsions-bridge.admin.webstore.index') }}" class="btn btn-outline-secondary w-100">
                    Batal
                </a>
            </div>
        </div>
    </div>
</form>

<script>
function previewUploadedImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('liveBannerPreview');
            const fallback = document.getElementById('noBannerFallback');
            preview.src = e.target.result;
            preview.style.display = 'block';
            fallback.style.display = 'none';
            // Clear preset input since file upload takes precedence
            document.getElementById('selectedPresetInput').value = '';
            // Reset remove image toggle
            document.getElementById('remove_image').checked = false;
            // Clear visual selection from preset cards
            document.querySelectorAll('.preset-card').forEach(c => {
                c.style.borderColor = 'rgba(255,255,255,0.1)';
            });
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function selectPreset(filename, previewUrl, cardElem) {
    document.getElementById('selectedPresetInput').value = filename;
    const preview = document.getElementById('liveBannerPreview');
    const fallback = document.getElementById('noBannerFallback');
    preview.src = previewUrl;
    preview.style.display = 'block';
    fallback.style.display = 'none';
    
    // Clear file input
    document.getElementById('image_file').value = '';
    document.getElementById('remove_image').checked = false;

    // Highlight selected card
    document.querySelectorAll('.preset-card').forEach(c => {
        c.style.borderColor = 'rgba(255,255,255,0.1)';
        const check = c.querySelector('.position-absolute');
        if (check) check.remove();
    });
    cardElem.style.borderColor = '#ffd700';
    const badge = document.createElement('div');
    badge.className = 'position-absolute top-0 end-0 bg-warning text-dark px-1 font-monospace small fw-bold';
    badge.style.borderBottomLeftRadius = '4px';
    badge.innerHTML = '<i class="bi bi-check-circle-fill"></i> DIPILIH';
    cardElem.querySelector('.position-relative').appendChild(badge);
}

function toggleRemoveImage(checkbox) {
    const preview = document.getElementById('liveBannerPreview');
    const fallback = document.getElementById('noBannerFallback');
    if (checkbox.checked) {
        preview.style.display = 'none';
        fallback.style.display = 'flex';
        document.getElementById('selectedPresetInput').value = '';
        document.getElementById('image_file').value = '';
        document.querySelectorAll('.preset-card').forEach(c => {
            c.style.borderColor = 'rgba(255,255,255,0.1)';
        });
    }
}
</script>
@endsection
