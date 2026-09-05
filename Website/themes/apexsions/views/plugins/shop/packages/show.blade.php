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

            <div>
                @if($shopUser !== null)
                    @if($package->isSubscription())
                        @if($package->isUserSubscribed($shopUser))
                            <a href="{{ route('shop.profile') }}" class="btn btn-apx-outline">
                                {{ trans('shop::messages.actions.manage') }}
                            </a>
                        @else
                            <form action="{{ route('shop.subscriptions.select', $package) }}" method="POST" class="d-inline">
                                @csrf
                                <button type="submit" class="btn btn-apx-gold">
                                    {{ trans('shop::messages.actions.subscribe') }}
                                </button>
                            </form>
                        @endif
                    @elseif($package->isInCart())
                        <form action="{{ route('shop.cart.remove', $package) }}" method="POST" class="d-inline">
                            @csrf
                            <button type="submit" class="btn btn-danger">
                                <i class="bi bi-cart-x me-1"></i> {{ trans('messages.actions.remove') }}
                            </button>
                        </form>
                    @elseif($package->global_limit === 0)
                        <span class="text-muted small"><i class="bi bi-x-circle me-1"></i> {{ trans('shop::messages.packages.unavailable') }}</span>
                    @elseif($package->getMaxQuantity() < 1)
                        <span class="text-muted small"><i class="bi bi-x-circle me-1"></i> {{ trans('shop::messages.packages.limit') }}</span>
                    @elseif(! $package->hasBoughtRequirements())
                        <span class="text-muted small"><i class="bi bi-x-circle me-1"></i> {{ trans('shop::messages.packages.requirements') }}</span>
                    @else
                        <form action="{{ route('shop.packages.buy', $package) }}" method="POST" class="d-inline-flex align-items-center gap-2">
                            @csrf

                            @if($package->custom_price)
                                <label for="price" class="form-label mb-0 small">{{ trans('shop::messages.fields.price') }}:</label>
                                <input type="number" step="0.01" min="{{ $package->getPrice() }}" size="5" class="form-control form-control-sm" style="width: 100px;" name="price" id="price" value="{{ $package->getPrice() }}">
                            @endif

                            @if($package->has_quantity)
                                <label for="quantity" class="form-label mb-0 small">{{ trans('shop::messages.fields.quantity') }}:</label>
                                <input type="number" min="1" max="{{ $package->getMaxQuantity() }}" size="5" class="form-control form-control-sm" style="width: 80px;" name="quantity" id="quantity" value="1" required>
                            @endif

                            <button type="submit" class="btn btn-apx-gold">
                                <i class="bi bi-cart-plus me-1"></i> {{ trans('shop::messages.buy') }}
                            </button>
                        </form>
                    @endif
                @else
                    <a href="{{ route('shop.login') }}" class="btn btn-apx-gold">
                        <i class="bi bi-box-arrow-in-right me-1"></i> {{ trans('auth.login') }}
                    </a>
                @endif
            </div>
        </div>
    </div>
</div>
