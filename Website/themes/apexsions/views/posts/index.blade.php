@extends('layouts.app')

@section('title', 'Warta & Artikel Peradaban | Apexsions')
@section('description', 'Kumpulan artikel resmi, pembaruan sistem, panduan berkala, dan warta peristiwa peradaban Minecraft Apexsions.')

@section('content')
<div class="apx-posts-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page" data-i18n="posts_breadcrumb">Warta &amp; Artikel</li>
            </ol>
        </nav>

        <!-- Page Header -->
        <div class="apx-section-header text-center mb-5">
            <div class="apx-section-kicker mb-2" data-i18n="posts_header_kicker">WARTA &amp; DOKUMEN RESMI</div>
            <h1 class="apx-section-title display-5 mb-3" data-i18n="posts_header_title">Warta &amp; Artikel Peradaban</h1>
            <p class="text-muted mx-auto" style="max-width: 720px; font-size: 1.05rem; line-height: 1.8;" data-i18n="posts_header_desc">
                Ikuti seluruh rilis fitur terbaru, catatan pembaruan server, berita perang kerajaan, serta artikel komunitas resmi Apexsions.
            </p>

            <!-- Search Form -->
            <div class="col-md-6 col-lg-5 mx-auto mt-4">
                <form action="{{ route('posts.index') }}" method="GET" role="search">
                    <div class="input-group">
                        <input type="search" class="form-control text-white" name="q" value="{{ $search ?? '' }}" placeholder="Cari warta atau artikel..." data-i18n-placeholder="posts_search_placeholder" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                        <button type="submit" class="btn btn-apx-gold">
                            <i class="bi bi-search"></i>
                        </button>
                    </div>
                </form>
            </div>
        </div>

        @if($posts->isEmpty())
            <div class="card p-5 text-center my-5 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                <div class="mb-3">
                    <i class="bi bi-newspaper text-gold" style="font-size: 3rem;"></i>
                </div>
                <h3 class="h4 text-white mb-2 font-cinzel" data-i18n="posts_empty_title">Belum Ada Warta Diterbitkan</h3>
                <p class="text-muted small mb-4 mx-auto" style="max-width: 480px;" data-i18n="posts_empty_desc">
                    Seluruh dokumentasi dan panduan mekanik dapat Anda pelajari secara mendalam melalui portal Ensiklopedia Wiki resmi.
                </p>
                <div>
                    <a href="{{ route('wiki.index') }}" class="btn btn-apx-gold btn-sm px-4 py-2">
                        <i class="bi bi-journal-text me-1"></i> <span data-i18n="nav_wiki">Buka Ensiklopedia Wiki</span>
                    </a>
                </div>
            </div>
        @else
            <div class="row g-4 justify-content-center">
                @foreach($posts as $post)
                    <div class="col-lg-6">
                        <div class="card h-100 p-4 rounded d-flex flex-column" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border); transition: transform 0.2s ease, border-color 0.2s ease;">
                            @if($post->hasImage())
                                <div class="mb-3 overflow-hidden rounded" style="max-height: 240px;">
                                    <img src="{{ $post->imageUrl() }}" class="w-100 h-100 object-fit-cover" alt="{{ $post->title }}">
                                </div>
                            @endif

                            <div class="d-flex align-items-center gap-2 text-muted small mb-2">
                                <i class="bi bi-calendar3 text-gold"></i>
                                <span>{{ format_date($post->published_at) }}</span>
                                <span>•</span>
                                <i class="bi bi-person text-info"></i>
                                <span>{{ $post->author->name }}</span>
                            </div>

                            <h2 class="h5 text-white mb-3 font-cinzel">
                                <a href="{{ route('posts.show', $post) }}" class="text-white text-decoration-none">
                                    {{ $post->title }}
                                </a>
                            </h2>

                            <p class="text-muted small mb-4 flex-grow-1" style="line-height: 1.7;">
                                {{ Str::limit(strip_tags($post->content), 180) }}
                            </p>

                            <div class="d-flex align-items-center justify-content-between pt-3 border-top border-secondary border-opacity-15 mt-auto">
                                <a href="{{ route('posts.show', $post) }}" class="btn btn-apx-outline btn-sm">
                                    <span data-i18n="posts_read_more">Baca Artikel</span> <i class="bi bi-arrow-right ms-1"></i>
                                </a>

                                <span class="text-dim small">
                                    <i class="bi bi-heart me-1 text-danger"></i> {{ $post->likes->count() }}
                                </span>
                            </div>
                        </div>
                    </div>
                @endforeach
            </div>

            <div class="d-flex justify-content-center mt-5">
                {{ $posts->links() }}
            </div>
        @endif
    </div>
</div>
@endsection
