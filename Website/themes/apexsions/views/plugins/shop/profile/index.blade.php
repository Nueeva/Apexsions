@extends('layouts.app')

@section('title', trans('shop::messages.profile.payments') . ' | Webstore Apexsions')

@section('content')
    <div class="apx-store-hero">
        <div class="apx-section-kicker mb-2" data-i18n-html="shop_hist_kicker">
            <i class="bi bi-receipt"></i> RIWAYAT TRANSAKSI RESMI
        </div>
        <h1 class="mb-2">{{ trans('shop::messages.profile.payments') }}</h1>
        <p data-i18n="shop_hist_sub">Arsip catatan transaksi, perolehan kasta donatur, dan langganan resmi peradaban akun Anda.</p>
    </div>

    <!-- Payments Card -->
    <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
        <div class="card-header py-3 px-4 d-flex align-items-center justify-content-between" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-border);">
            <h5 class="mb-0 text-white" style="font-family: 'Cinzel', Georgia, serif;">
                <i class="bi bi-clock-history text-warning me-2"></i> {{ trans('shop::messages.profile.payments') }}
            </h5>
            <a href="{{ route('shop.home') }}" class="btn btn-apx-outline btn-sm">
                <i class="bi bi-shop me-1"></i> <span data-i18n="shop_btn_back">Kembali ke Toko</span>
            </a>
        </div>
        <div class="card-body p-4">
            <div class="table-responsive apx-shop-table">
                <table class="table">
                    <thead>
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col" data-i18n="shop_th_price">{{ trans('shop::messages.fields.price') }}</th>
                        <th scope="col" data-i18n="shop_th_type">{{ trans('messages.fields.type') }}</th>
                        <th scope="col" data-i18n="shop_th_status">{{ trans('messages.fields.status') }}</th>
                        <th scope="col" data-i18n="shop_th_payment_id">{{ trans('shop::messages.fields.payment_id') }}</th>
                        <th scope="col" data-i18n="shop_th_date">{{ trans('messages.fields.date') }}</th>
                    </tr>
                    </thead>
                    <tbody>

                    @forelse($payments as $payment)
                        <tr>
                            <th scope="row" class="text-dim">#{{ $payment->id }}</th>
                            <td><span class="text-warning fw-bold font-monospace">{{ $payment->formatPrice() }}</span></td>
                            <td><span class="badge bg-secondary bg-opacity-50 text-light">{{ $payment->getTypeName() }}</span></td>
                            <td>
                                <span class="badge bg-{{ $payment->statusColor() }}">
                                    {{ trans('shop::admin.payments.status.'.$payment->status) }}
                                </span>
                            </td>
                            <td><code class="text-dim font-monospace" style="font-size: 0.78rem;">{{ $payment->transaction_id ?? trans('messages.unknown') }}</code></td>
                            <td><span class="small text-muted">{{ format_date($payment->created_at, true) }}</span></td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center py-4 text-muted">
                                <i class="bi bi-inbox fs-4 d-block mb-1 text-dim"></i>
                                <span data-i18n="shop_hist_empty">Belum ada riwayat transaksi tercatat untuk akun Anda.</span>
                            </td>
                        </tr>
                    @endforelse

                    </tbody>
                </table>
            </div>
        </div>
    </div>

    @if(! $purchases->isEmpty())
        <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
            <div class="card-header py-3 px-4" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-border);">
                <h5 class="mb-0 text-white" style="font-family: 'Cinzel', Georgia, serif;">
                    <i class="bi bi-bag-check text-warning me-2"></i> <span data-i18n="shop_purchases_title">{{ trans('shop::messages.profile.purchases') }}</span>
                </h5>
            </div>
            <div class="card-body p-4">
                <div class="table-responsive apx-shop-table">
                    <table class="table">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">{{ trans('shop::messages.fields.price') }}</th>
                            <th scope="col">{{ trans('messages.fields.date') }}</th>
                        </tr>
                        </thead>
                        <tbody>

                        @foreach($purchases as $purchase)
                            <tr>
                                <th scope="row" class="text-dim">#{{ $purchase->id }}</th>
                                <td><span class="text-warning fw-bold font-monospace">{{ $purchase->formatPrice() }}</span></td>
                                <td><span class="small text-muted">{{ format_date($purchase->created_at, true) }}</span></td>
                            </tr>
                        @endforeach

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    @endif

    @if(! $subscriptions->isEmpty())
        <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
            <div class="card-header py-3 px-4" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-border);">
                <h5 class="mb-0 text-white" style="font-family: 'Cinzel', Georgia, serif;">
                    <i class="bi bi-arrow-repeat text-warning me-2"></i> <span data-i18n="shop_subscriptions_title">{{ trans('shop::messages.profile.subscriptions') }}</span>
                </h5>
            </div>
            <div class="card-body p-4">
                <div class="table-responsive apx-shop-table">
                    <table class="table">
                        <thead>
                        <tr>
                            <th scope="col">#</th>
                            <th scope="col">{{ trans('shop::messages.fields.price') }}</th>
                            @if(! use_site_money())
                                <th scope="col">{{ trans('messages.fields.type') }}</th>
                            @endif
                            <th scope="col">{{ trans('shop::messages.fields.package') }}</th>
                            <th scope="col">{{ trans('messages.fields.status') }}</th>
                            <th scope="col">{{ trans('shop::messages.fields.subscription_id') }}</th>
                            <th scope="col">{{ trans('messages.fields.date') }}</th>
                            <th scope="col">{{ trans('shop::messages.fields.renewal_date') }}</th>
                            <th scope="col" class="text-end">{{ trans('messages.fields.action') }}</th>
                        </tr>
                        </thead>
                        <tbody>

                        @foreach($subscriptions as $subscription)
                            <tr>
                                <th scope="row" class="text-dim">#{{ $subscription->id }}</th>
                                <td><span class="text-warning fw-bold font-monospace">{{ $subscription->formatPrice() }}</span></td>
                                @if(! use_site_money())
                                    <td><span class="badge bg-secondary bg-opacity-50 text-light">{{ $subscription->getTypeName() }}</span></td>
                                @endif
                                <td class="text-white fw-bold">{{ $subscription->package?->name ?? trans('messages.unknown') }}</td>
                                <td>
                                    <span class="badge bg-{{ $subscription->statusColor() }}">
                                        {{ trans('shop::admin.subscriptions.status.'.$subscription->status) }}
                                    </span>
                                </td>
                                <td><code class="text-dim font-monospace" style="font-size: 0.78rem;">{{ $subscription->subscription_id ?? trans('messages.unknown') }}</code></td>
                                <td><span class="small text-muted">{{ format_date($subscription->created_at) }}</span></td>
                                <td><span class="small text-muted">{{ $subscription->ends_at !== null ? format_date($subscription->ends_at) : '-' }}</span></td>
                                <td class="text-end">
                                    @if($subscription->isActive() && ! $subscription->isCanceled())
                                        <form action="{{ route('shop.subscriptions.destroy', $subscription) }}" method="POST" class="d-inline-block">
                                            @method('DELETE')
                                            @csrf

                                            <button type="submit" class="btn btn-outline-danger btn-sm">
                                                <i class="bi bi-x-circle me-1"></i> {{ trans('messages.actions.cancel') }}
                                            </button>
                                        </form>
                                    @endif
                                </td>
                            </tr>
                        @endforeach

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    @endif
@endsection
