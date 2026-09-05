@extends('layouts.app')

@section('title', trans('wiki::messages.title') . ' | Apexsions')

@section('content')
    @include('wiki::partials._header', ['title' => 'Ensiklopedia Apexsions'])

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
                                    {{ $category->pages->count() }} Artikel
                                </span>
                            </div>

                            <h3 class="card-title mb-2" style="font-family: 'Cinzel', Georgia, serif; font-size: 1.3rem; color: #ffffff;">
                                {{ $category->name }}
                            </h3>

                            <p class="text-muted small mb-4 flex-grow-1">
                                Pelajari seluk-beluk {{ strtolower($category->name) }}, aturan wilayah, dan panduan mekanik server Apexsions.
                            </p>

                            <div class="d-flex align-items-center text-warning fw-bold small">
                                <span>Buka Panduan</span>
                                <i class="bi bi-arrow-right ms-2"></i>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
        @endforeach
    </div>
@endsection
