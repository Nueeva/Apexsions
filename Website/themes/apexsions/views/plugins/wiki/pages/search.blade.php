@extends('layouts.app')

@section('title', trans('wiki::messages.search.results') . ' | Apexsions')

@section('content')
    @include('wiki::partials._header', ['title' => trans('wiki::messages.search.results'), 'search' => request('q')])

    <div id="wiki" class="apx-wiki-search-results">
        <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
            <div class="text-muted small">
                Hasil pencarian untuk: <strong class="text-warning">"{{ request('q') }}"</strong>
                @if(isset($pages))
                    ({{ $pages->total() }} artikel ditemukan)
                @endif
            </div>
            <a href="{{ route('wiki.index') }}" class="btn btn-apx-outline btn-sm">
                <i class="bi bi-arrow-left me-1"></i> Kembali ke Indeks Wiki
            </a>
        </div>

        @forelse($pages as $page)
            @can('view', $page->category)
                <div class="card mb-3 apx-wiki-search-card" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md); transition: var(--apx-transition);">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="badge" style="background: rgba(245, 158, 11, 0.12); color: var(--apx-gold-light); border: 1px solid var(--apx-gold-border); font-size: 0.75rem;">
                                <i class="{{ $page->category->icon ?? 'bi bi-folder2' }} me-1"></i>
                                {{ $page->category->name }}
                            </span>
                        </div>

                        <h3 class="card-title mb-2" style="font-family: 'Cinzel', Georgia, serif; font-size: 1.25rem;">
                            <a href="{{ route('wiki.pages.show', [$page->category, $page]) }}" class="text-white text-decoration-none hover-gold">
                                {{ $page->title }}
                            </a>
                        </h3>

                        <p class="text-muted small mb-3" style="line-height: 1.7;">
                            {{ Str::limit(strip_tags($page->content), 240) }}
                        </p>

                        <a href="{{ route('wiki.pages.show', [$page->category, $page]) }}" class="d-inline-flex align-items-center text-warning fw-bold small text-decoration-none">
                            <span>Buka Panduan Lengkap</span>
                            <i class="bi bi-arrow-right ms-2"></i>
                        </a>
                    </div>
                </div>
            @endcan
        @empty
            <div class="card p-5 text-center" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
                <div class="apx-card-icon-wrap apx-icon-gold mx-auto mb-3">
                    <i class="bi bi-search"></i>
                </div>
                <h4 class="text-white mb-2" style="font-family: 'Cinzel', Georgia, serif;">Tidak Ada Hasil Ditemukan</h4>
                <p class="text-muted small mb-4">
                    Tidak ditemukan artikel ensiklopedia yang cocok dengan kata kunci <strong>"{{ request('q') }}"</strong>. Coba gunakan kata kunci umum seperti <em>"kerajaan"</em>, <em>"enchant"</em>, <em>"perintah"</em>, atau <em>"level"</em>.
                </p>
                <div>
                    <a href="{{ route('wiki.index') }}" class="btn btn-apx-gold btn-sm">
                        <i class="bi bi-journal-bookmark me-1"></i> Jelajahi Seluruh Kategori
                    </a>
                </div>
            </div>
        @endforelse

        <div class="mt-4">
            {{ $pages->withQueryString()->links() }}
        </div>
    </div>
@endsection
