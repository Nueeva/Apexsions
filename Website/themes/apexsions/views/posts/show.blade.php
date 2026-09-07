@extends('layouts.app')

@section('title', $post->title . ' | Apexsions')
@section('description', $post->description ?? Str::limit(strip_tags($post->content), 150))
@section('type', 'article')

@push('meta')
<meta property="og:article:author:username" content="{{ $post->author->name }}">
<meta property="og:article:published_time" content="{{ $post->published_at->toIso8601String() }}">
<meta property="og:article:modified_time" content="{{ $post->updated_at->toIso8601String() }}">
@endpush

@section('content')
<div class="apx-post-detail-page py-5">
    <div class="container py-4">
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb apx-breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="{{ route('home') }}" class="text-gold text-decoration-none" data-i18n="nav_home">Beranda</a></li>
                <li class="breadcrumb-item"><a href="{{ route('posts.index') }}" class="text-gold text-decoration-none" data-i18n="posts_breadcrumb">Warta &amp; Artikel</a></li>
                <li class="breadcrumb-item active text-white" aria-current="page">{{ Str::limit($post->title, 40) }}</li>
            </ol>
        </nav>

        <div class="row justify-content-center">
            <div class="col-lg-9">
                <article class="p-4 p-lg-5 rounded mb-5" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                    <div class="d-flex align-items-center gap-2 text-muted small mb-3">
                        <i class="bi bi-calendar3 text-gold"></i>
                        <span>{{ format_date($post->published_at) }}</span>
                        <span>•</span>
                        <i class="bi bi-person text-info"></i>
                        <span>{{ $post->author->name }}</span>
                        <span>•</span>
                        <i class="bi bi-clock"></i>
                        <span>~{{ max(1, ceil(str_word_count(strip_tags($post->content)) / 180)) }} <span data-i18n="wiki_min_read">menit baca</span></span>
                    </div>

                    <h1 class="display-6 text-white mb-4 font-cinzel">{{ $post->title }}</h1>

                    @if($post->hasImage())
                        <div class="mb-4 rounded overflow-hidden" style="max-height: 420px;">
                            <img class="img-fluid w-100 object-fit-cover rounded" src="{{ $post->imageUrl() }}" alt="{{ $post->title }}">
                        </div>
                    @endif

                    <div class="apx-wiki-body text-white mb-5" style="line-height: 1.8;">
                        {!! $post->content !!}
                    </div>

                    <hr class="border-secondary border-opacity-20 my-4">

                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                        <button type="button" class="btn btn-outline-danger @if($post->isLiked()) active @endif btn-sm px-3" @guest disabled @endguest data-like-url="{{ route('posts.like', $post) }}">
                            <i class="bi bi-heart @if($post->isLiked()) d-none @endif" data-liked="true"></i>
                            <i class="bi bi-heart-fill @if(! $post->isLiked()) d-none @endif" data-liked="false"></i>
                            <span class="ms-1"><span class="likes-count">{{ $post->likes->count() }}</span> <span data-i18n="posts_likes">Menyukai</span></span>
                            <span class="d-none spinner-border spinner-border-sm load-spinner ms-1" role="status"></span>
                        </button>

                        <a href="{{ route('posts.index') }}" class="btn btn-apx-outline btn-sm">
                            <i class="bi bi-arrow-left me-1"></i> <span data-i18n="posts_back">Kembali ke Daftar Artikel</span>
                        </a>
                    </div>
                </article>

                <!-- Comments Section -->
                <section id="comments" class="mb-5">
                    <h3 class="h5 text-white mb-4 font-cinzel">
                        <i class="bi bi-chat-dots-fill text-gold me-2"></i> <span data-i18n="posts_comments_title">Komentar &amp; Diskusi Warga</span> ({{ $post->comments->count() }})
                    </h3>

                    @foreach($post->comments as $comment)
                        <div class="card p-3 mb-3 rounded" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                            <div class="d-flex gap-3">
                                <img class="rounded" src="{{ $comment->author->getAvatar() }}" alt="{{ $comment->author->name }}" width="48" height="48">
                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <div class="text-white small fw-bold">{{ $comment->author->name }}</div>
                                        <div class="text-muted" style="font-size: 0.75rem;">{{ format_date($comment->created_at, true) }}</div>
                                    </div>
                                    <div class="text-muted small" style="line-height: 1.6;">
                                        {{ $comment->parseContent() }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    @endforeach

                    @can('create', [\Azuriom\Models\Comment::class, $post])
                        <div class="card p-4 rounded mt-4" style="background: var(--apx-bg-deep); border: 1px solid var(--apx-border);">
                            <h4 class="h6 text-white mb-3 font-cinzel" data-i18n="posts_leave_comment">Tulis Komentar</h4>
                            <form action="{{ route('posts.comments.store', $post) }}" method="POST">
                                @csrf
                                <div class="mb-3">
                                    <textarea class="form-control text-white @error('content') is-invalid @enderror" id="content" name="content" rows="3" placeholder="Sampaikan pandangan Anda dengan sopan..." data-i18n-placeholder="posts_comment_placeholder" required style="background: var(--apx-bg-surface); border: 1px solid var(--apx-border);"></textarea>
                                </div>
                                <button type="submit" class="btn btn-apx-gold btn-sm px-4">
                                    <i class="bi bi-send me-1"></i> <span data-i18n="posts_send_comment">Kirim Komentar</span>
                                </button>
                            </form>
                        </div>
                    @endcan

                    @guest
                        <div class="alert text-center p-3 rounded" style="background: rgba(212, 163, 89, 0.1); border: 1px solid var(--apx-gold-border); color: #e2e8f0; font-size: 0.88rem;">
                            <i class="bi bi-info-circle text-gold me-1"></i> <span data-i18n-html="posts_guest_comment">Silakan <a href="{{ route('login') }}" class="text-gold fw-bold text-decoration-none">Masuk ke Akun</a> untuk berpartisipasi dalam diskusi.</span>
                        </div>
                    @endguest
                </section>
            </div>
        </div>
    </div>
</div>
@endsection
