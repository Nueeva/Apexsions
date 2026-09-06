@extends('layouts.app')

@section('title', trans('wiki::messages.title') . ' | Apexsions')

@section('content')
    @include('wiki::partials._header', ['title' => 'Ensiklopedia & Fandom Apexsions'])

    <!-- Fandom Portal Banner & Quick Stats -->
    <div class="card mb-4" style="background: linear-gradient(135deg, rgba(20, 28, 46, 0.95) 0%, rgba(10, 14, 24, 0.98) 100%); border: 1px solid var(--apx-gold-border); border-radius: var(--apx-radius-lg); box-shadow: var(--apx-shadow-elevated);">
        <div class="card-body p-4">
            <div class="row align-items-center gy-3">
                <div class="col-lg-8">
                    <div class="d-flex align-items-center gap-2 mb-2">
                        <span class="badge" style="background: rgba(245, 158, 11, 0.2); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.75rem; letter-spacing: 0.08em;">
                            <i class="bi bi-book-half me-1"></i> ARSIP RESMI REALM
                        </span>
                        <span class="text-dim small">• Versi Runtime Minecraft 26.2</span>
                    </div>
                    <h2 class="h4 mb-2" style="font-family: 'Cinzel', Georgia, serif; color: #ffffff;">
                        Pusat Pengetahuan &amp; Sejarah Peradaban Puncak
                    </h2>
                    <p class="text-muted small mb-3" style="line-height: 1.6;">
                        Selamat datang di ensiklopedia resmi Apexsions. Temukan dokumentasi lengkap tentang 3 Kerajaan berdaulat, 11 hierarki kasta resmi, 28 Custom Enchants beserta efek set bonus armor, panduan pasar dinamis, serta peta jalan dari pengelana menjadi penguasa wilayah.
                    </p>
                    <div class="d-flex flex-wrap gap-2">
                        <span class="text-muted small me-1 align-self-center">Topik Populer:</span>
                        <a href="{{ route('wiki.search', ['q' => 'pemula']) }}" class="badge text-decoration-none py-2 px-3" style="background: rgba(255, 255, 255, 0.06); color: var(--apx-gold-light); border: 1px solid rgba(255, 255, 255, 0.1);">#PanduanPemula</a>
                        <a href="{{ route('wiki.search', ['q' => 'kerajaan']) }}" class="badge text-decoration-none py-2 px-3" style="background: rgba(255, 255, 255, 0.06); color: var(--apx-gold-light); border: 1px solid rgba(255, 255, 255, 0.1);">#TigaKerajaan</a>
                        <a href="{{ route('wiki.search', ['q' => 'kasta']) }}" class="badge text-decoration-none py-2 px-3" style="background: rgba(255, 255, 255, 0.06); color: var(--apx-gold-light); border: 1px solid rgba(255, 255, 255, 0.1);">#11KastaResmi</a>
                        <a href="{{ route('wiki.search', ['q' => 'enchants']) }}" class="badge text-decoration-none py-2 px-3" style="background: rgba(255, 255, 255, 0.06); color: var(--apx-gold-light); border: 1px solid rgba(255, 255, 255, 0.1);">#CustomEnchants</a>
                        <a href="{{ route('wiki.search', ['q' => 'lelang']) }}" class="badge text-decoration-none py-2 px-3" style="background: rgba(255, 255, 255, 0.06); color: var(--apx-gold-light); border: 1px solid rgba(255, 255, 255, 0.1);">#PasarLelang</a>
                    </div>
                </div>
                <div class="col-lg-4 text-lg-end">
                    <div class="row g-2 text-center">
                        <div class="col-6">
                            <div class="p-3 rounded-3" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                                <div class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif;">6</div>
                                <div class="text-dim" style="font-size: 0.75rem; text-transform: uppercase;">Kategori Utama</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded-3" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                                <div class="fs-4 fw-bold text-white" style="font-family: 'Cinzel', Georgia, serif;">21+</div>
                                <div class="text-dim" style="font-size: 0.75rem; text-transform: uppercase;">Artikel Terinci</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded-3" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                                <div class="fs-4 fw-bold text-warning" style="font-family: 'Cinzel', Georgia, serif;">11</div>
                                <div class="text-dim" style="font-size: 0.75rem; text-transform: uppercase;">Kasta Resmi</div>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 rounded-3" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                                <div class="fs-4 fw-bold text-info" style="font-family: 'Cinzel', Georgia, serif;">28</div>
                                <div class="text-dim" style="font-size: 0.75rem; text-transform: uppercase;">Custom Enchants</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4" id="wiki">
        @foreach($categories as $category)
            <div class="col-md-6 col-lg-4">
                <a href="{{ route('wiki.show', $category) }}" class="text-decoration-none">
                    <div class="card h-100 apx-feature-card" style="border: 1px solid var(--apx-gold-border-subtle); transition: var(--apx-transition);">
                        <div class="card-body p-4 d-flex flex-column">
                            <div class="d-flex align-items-center justify-content-between mb-3">
                                <div class="apx-card-icon-wrap apx-icon-gold mb-0">
                                    <i class="{{ $category->icon ?? 'bi bi-journal-text' }}"></i>
                                </div>
                                <span class="badge" style="background: rgba(245, 158, 11, 0.15); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.75rem;">
                                    {{ $category->pages->count() }} Dokumen
                                </span>
                            </div>

                            <h3 class="card-title mb-2" style="font-family: 'Cinzel', Georgia, serif; font-size: 1.25rem; color: #ffffff;">
                                {{ $category->name }}
                            </h3>

                            <p class="text-muted small mb-3 flex-grow-1" style="line-height: 1.6;">
                                Pelajari seluk-beluk {{ strtolower($category->name) }}, aturan wilayah, dan panduan mekanik server Apexsions.
                            </p>

                            <!-- Mini Article List Preview -->
                            <div class="mb-3 pt-2 border-top border-secondary border-opacity-15">
                                <ul class="list-unstyled mb-0" style="font-size: 0.8rem; line-height: 1.7;">
                                    @foreach($category->pages->take(3) as $pg)
                                        <li class="text-truncate text-dim">
                                            <i class="bi bi-file-text text-warning me-1"></i> {{ $pg->title }}
                                        </li>
                                    @endforeach
                                    @if($category->pages->count() > 3)
                                        <li class="text-muted fst-italic" style="font-size: 0.75rem;">
                                            +{{ $category->pages->count() - 3 }} artikel lainnya...
                                        </li>
                                    @endif
                                </ul>
                            </div>

                            <div class="d-flex align-items-center justify-content-between text-warning fw-bold small mt-auto pt-2">
                                <span>Buka Ensiklopedia</span>
                                <i class="bi bi-arrow-right"></i>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
        @endforeach
    </div>
@endsection
