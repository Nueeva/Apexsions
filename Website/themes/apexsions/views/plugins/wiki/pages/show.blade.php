@extends('layouts.app')

@section('title', $page->title . ' | ' . $page->category->name . ' | Apexsions')

@section('content')
    @include('wiki::partials._header', ['title' => $page->category->name])

    <div class="row g-4" id="wiki">
        <!-- Sidebar Navigation -->
        <div class="col-lg-3 apx-wiki-sidebar">
            <div class="mb-3">
                @if($page->category->parent !== null)
                    <a href="{{ route('wiki.show', $page->category->parent) }}" class="btn btn-apx-outline btn-sm w-100 mb-3">
                        <i class="bi bi-chevron-left me-1"></i> Kembali ke Kategori
                    </a>
                @else
                    <a href="{{ route('wiki.index') }}" class="btn btn-apx-outline btn-sm w-100 mb-3">
                        <i class="bi bi-chevron-left me-1"></i> Semua Kategori Wiki
                    </a>
                @endif
            </div>

            @if(! $page->category->categories->isEmpty())
                <div class="list-group mb-3" role="tablist">
                    @foreach($page->category->categories as $subCategory)
                        @can('view', $subCategory)
                            <a href="{{ route('wiki.show', [$subCategory]) }}" class="list-group-item">
                                <i class="{{ $subCategory->icon ?? 'bi bi-folder2' }} text-warning me-2"></i> {{ $subCategory->name }}
                            </a>
                        @endcan
                    @endforeach
                </div>
            @endif

            <div class="card mb-3" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
                <div class="card-header py-2 px-3 small">
                    <i class="bi bi-list-nested me-1 text-warning"></i> DAFTAR ARTIKEL
                </div>
                <div class="list-group list-group-flush" role="tablist">
                    @foreach($page->category->pages as $catPage)
                        <a href="{{ route('wiki.pages.show', [$page->category, $catPage]) }}" class="list-group-item @if($page->is($catPage)) active @endif"
                           title="{{ $catPage->title }}"
                           onclick="selectWikiPage(this)"
                           data-bs-toggle="tab" data-bs-target="#page-{{ $catPage->id }}" role="tab"
                           aria-controls="page-{{ $catPage->id }}" aria-selected="{{ $page->is($catPage) ? 'true' : 'false' }}">
                            <i class="bi bi-file-earmark-text me-2 text-warning"></i>
                            {{ $catPage->title }}
                        </a>
                    @endforeach
                </div>
            </div>
        </div>

        <!-- Article Content Area -->
        <div class="col-lg-9 tab-content">
            @foreach($page->category->pages as $catPage)
                <div class="tab-pane fade @if($page->is($catPage)) show active @endif" id="page-{{ $catPage->id }}" role="tabpanel">
                    <article class="apx-wiki-article-card">
                        <!-- Breadcrumb navigation inside article -->
                        <div class="d-flex align-items-center gap-2 text-muted small mb-3">
                            <a href="{{ route('wiki.index') }}" class="text-muted">Wiki</a>
                            <span>/</span>
                            <span class="text-warning">{{ $page->category->name }}</span>
                            <span>/</span>
                            <span class="text-white">{{ $catPage->title }}</span>
                        </div>

                        <h1 class="mb-4" style="font-family: 'Cinzel', Georgia, serif; font-size: 2rem; color: #ffffff; border-bottom: 2px solid var(--apx-gold-border-subtle); padding-bottom: 0.75rem;">
                            {{ $catPage->title }}
                        </h1>

                        <div class="apx-wiki-body">
                            {!! $catPage->content !!}
                        </div>
                    </article>
                </div>
            @endforeach
        </div>
    </div>
@endsection

@push('scripts')
    <script>
        let currentTitle = '{{ $page->title }}';

        function selectWikiPage(element, replaceState = false) {
            const tab = bootstrap.Tab.getOrCreateInstance(element);
            tab.show();

            if (replaceState) {
                window.history.replaceState({}, '', element.href);
            } else {
                window.history.pushState({}, '', element.href);
            }

            document.title = document.title.replace(currentTitle, element.title);
            currentTitle = element.title;
        }

        window.onpopstate = function(e) {
            const target = document.querySelector('[href="' + e.target.location.href + '"]');

            if (target) {
                selectWikiPage(target, true);
            }
        };
    </script>
@endpush
