<!-- User Auth & Cart Widget -->
@if($shopUser !== null)
    <div class="apx-user-widget">
        <img src="{{ $shopUser->getAvatar(48) }}" class="apx-user-avatar" alt="{{ $shopUser->name }}">
        <div class="flex-grow-1 overflow-hidden">
            <div class="apx-user-name text-truncate">{{ $shopUser->name }}</div>
            @if(use_site_money())
                <div class="small text-warning">
                    <i class="bi bi-wallet2 me-1"></i> {{ format_money($shopUser->money) }}
                </div>
            @endif
        </div>
    </div>

    <div class="d-grid gap-2 mb-4">
        @if(use_site_money())
            <a href="{{ route('shop.offers.select') }}" class="btn btn-apx-outline">
                <i class="bi bi-credit-card"></i> {{ trans('shop::messages.cart.credit') }}
            </a>
        @endif

        <a href="{{ route('shop.cart.index') }}" class="btn btn-apx-gold">
            <i class="bi bi-cart3"></i> {{ trans('shop::messages.cart.title') }}
        </a>

        @if($userHasPayments)
            <a href="{{ route('shop.profile') }}" class="btn btn-apx-outline">
                <i class="bi bi-receipt"></i> {{ trans('shop::messages.profile.payments') }}
            </a>
        @endif

        @guest
            <form action="{{ route('shop.logout') }}" method="POST" class="text-center">
                @csrf
                <button type="submit" class="btn btn-secondary w-100 btn-sm">
                    <i class="bi bi-box-arrow-right"></i> {{ trans('auth.logout') }}
                </button>
            </form>
        @endguest
    </div>
@else
    <div class="card mb-4 text-center p-3" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
        <div class="mb-2 text-warning fs-3">
            <i class="bi bi-person-badge"></i>
        </div>
        <h5 class="mb-1" style="font-family: 'Cinzel', Georgia, serif; color: #ffffff;">Akun Pemain</h5>
        <p class="small text-muted mb-3">Masuk untuk melihat keranjang dan klaim perk otomatis ke dalam in-game.</p>
        <a href="{{ route('shop.login') }}" class="btn btn-apx-gold">
            <i class="bi bi-box-arrow-in-right me-1"></i> {{ trans('auth.login') }}
        </a>
    </div>
@endif

<!-- Categories Navigation List -->
<div class="list-group mb-4">
    @if($displayHome)
        <a href="{{ route('shop.home') }}" class="list-group-item @if($category === null) active @endif">
            <i class="bi bi-house-door me-2"></i> {{ trans('messages.home') }}
        </a>
    @endif

    @foreach($categories as $subCategory)
        <a href="{{ route('shop.categories.show', $subCategory) }}" class="list-group-item @if($subCategory->is($category)) active @endif">
            <i class="{{ $subCategory->icon ?? 'bi bi-tag-fill' }} me-2"></i>
            {{ $subCategory->name }}
        </a>

        @foreach($subCategory->categories as $cat)
            <a href="{{ route('shop.categories.show', $cat) }}" class="list-group-item ps-4 @if($cat->is($category)) active @endif">
                <i class="{{ $cat->icon ?? 'bi bi-chevron-right' }} me-2"></i>
                {{ $cat->name }}
            </a>
        @endforeach
    @endforeach
</div>

<!-- Monthly Server Goal Widget -->
@if($goal >= 0)
    <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
        <div class="card-header">
            <i class="bi bi-graph-up text-warning me-1"></i> {{ trans('shop::messages.goal.title') }}
        </div>
        <div class="card-body">
            <div class="progress mb-2" style="height: 10px; background: var(--apx-bg-deep);">
                <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" aria-valuenow="{{ $goal }}" aria-valuemin="0" aria-valuemax="100" style="width: {{ min($goal, 100) }}%; background: linear-gradient(90deg, #d97706, #fbbf24);"></div>
            </div>

            <p class="card-text text-center small text-muted mb-0">
                {{ trans_choice('shop::messages.goal.progress', $goal) }}
            </p>
        </div>
    </div>
@endif

<!-- Top Donator Widget -->
@if($topCustomer !== null)
    <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
        <div class="card-header">
            <i class="bi bi-trophy-fill text-warning me-1"></i> {{ trans('shop::messages.top.title') }}
        </div>
        <div class="card-body d-flex align-items-center gap-3">
            <img class="rounded border border-warning" src="{{ $topCustomer->user->getAvatar(52) }}" alt="{{ $topCustomer->user->name }}" width="52">
            <div>
                <h5 class="mb-0" style="color: #ffffff; font-family: 'Cinzel', Georgia, serif;">{{ $topCustomer->user->name }}</h5>
                @if($displaySidebarAmount)
                    <div class="small text-warning fw-bold">{{ $topCustomer->formatPrice() }}</div>
                @endif
                <div class="small text-muted">Pelindung Kerajaan</div>
            </div>
        </div>
    </div>
@endif

<!-- Recent Payments Widget -->
@if($recentPayments !== null)
    <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle);">
        <div class="card-header">
            <i class="bi bi-clock-history text-warning me-1"></i> {{ trans('shop::messages.recent.title') }}
        </div>
        <div class="list-group list-group-flush">
            @forelse($recentPayments as $payment)
                <div class="list-group-item d-flex align-items-center gap-2 px-3 py-2">
                    <img src="{{ $payment->user->getAvatar(36) }}" class="rounded" alt="{{ $payment->user->name }}" width="32">
                    <div class="flex-grow-1 overflow-hidden">
                        <div class="text-truncate fw-bold small text-white">{{ $payment->user->name }}</div>
                        <div class="small text-muted" style="font-size: 0.75rem;">
                            {{ format_date($payment->created_at) }}
                        </div>
                    </div>
                    @if($displaySidebarAmount)
                        <div class="small text-warning fw-bold">{{ $payment->formatPrice() }}</div>
                    @endif
                </div>
            @empty
                <div class="list-group-item text-muted small text-center py-3">
                    {{ trans('shop::messages.recent.empty') }}
                </div>
            @endforelse
        </div>
    </div>
@endif
