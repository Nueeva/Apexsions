<div class="apx-wiki-header">
    <div class="apx-wiki-title-area">
        <div class="apx-wiki-badge">
            <i class="bi bi-journal-bookmark-fill me-1"></i> ENSIKLOPEDIA &amp; PANDUAN
        </div>
        <h1 class="mb-0">{{ $title }}</h1>
    </div>

    <div class="apx-wiki-search-form">
        <form action="{{ route('wiki.search') }}" method="GET" role="search">
            <label class="visually-hidden" for="searchInput">
                {{ trans('messages.actions.search') }}
            </label>

            <div class="input-group">
                <input type="search" class="form-control text-white" id="searchInput" name="q" value="{{ $search ?? '' }}" placeholder="Cari topik atau perintah..." required>
                <button type="submit" class="btn btn-apx-gold">
                    <i class="bi bi-search"></i>
                </button>
            </div>
        </form>
    </div>
</div>
