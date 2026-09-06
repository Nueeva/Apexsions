@extends('layouts.app')

@section('title', trans('shop::messages.cart.title') . ' | Webstore Apexsions')

@push('styles')
    <style>
        .cart-items thead th {
            width: 40%;
        }
        .cart-items tbody td {
            width: 15%;
        }
    </style>
@endpush

@section('content')
    <div class="apx-store-hero">
        <div class="apx-section-kicker mb-2">
            <i class="bi bi-cart3"></i> KERANJANG BELANJA PERADABAN
        </div>
        <h1 class="mb-2">{{ trans('shop::messages.cart.title') }}</h1>
        <p>Periksa rincian pesanan paket kedaulatan Anda sebelum melanjutkan ke proses pembayaran atau aktivasi via WhatsApp.</p>
    </div>

    <!-- WhatsApp Direct Order Banner on Cart -->
    <div class="card mb-4" style="background: linear-gradient(135deg, rgba(34, 197, 94, 0.08) 0%, rgba(15, 23, 42, 0.95) 100%); border: 1px solid rgba(34, 197, 94, 0.35); border-radius: var(--apx-radius-md);">
        <div class="card-body p-3 p-md-4 d-flex align-items-center justify-content-between flex-wrap gap-3">
            <div class="d-flex align-items-center gap-3">
                <div class="d-flex align-items-center justify-content-center flex-shrink-0" style="width: 46px; height: 46px; border-radius: 50%; background: rgba(34, 197, 94, 0.2); color: #4ade80; font-size: 1.5rem; border: 1px solid rgba(34, 197, 94, 0.4);">
                    <i class="bi bi-whatsapp"></i>
                </div>
                <div>
                    <div class="fw-bold text-white mb-1" style="font-size: 1rem;">
                        <i class="bi bi-patch-check-fill text-success me-1"></i> Mau Aktivasi Instan Tanpa Antre?
                    </div>
                    <div class="text-muted small" style="line-height: 1.5;">
                        Anda dapat langsung konfirmasi dan pesan melalui WhatsApp ke 3 Founder resmi:
                        <strong class="text-white">Rifqi</strong>, <strong class="text-white">Friell</strong>, atau <strong class="text-white">Favian</strong>.
                    </div>
                </div>
            </div>
            <div class="d-flex align-items-center gap-2 flex-wrap">
                @php
                    $cartAdmins = config('services.whatsapp.admins', [
                        ['name' => 'Rifqi', 'number' => '6281212994597', 'role' => 'Founder'],
                        ['name' => 'Friell', 'number' => '6285883161047', 'role' => 'Founder'],
                        ['name' => 'Favian', 'number' => '6287729112281', 'role' => 'Founder'],
                    ]);
                @endphp
                @foreach($cartAdmins as $adm)
                    @php
                        $cleanPhone = preg_replace('/[^0-9]/', '', $adm['number']);
                    @endphp
                    <a href="https://wa.me/{{ $cleanPhone }}?text={{ rawurlencode('Halo Admin ' . $adm['name'] . ', saya ingin konfirmasi pesanan keranjang Webstore Apexsions.') }}" target="_blank" rel="noopener noreferrer" class="apx-shop-founder-pill">
                        <i class="bi bi-whatsapp text-success"></i>
                        <span>WA {{ $adm['name'] }}</span>
                    </a>
                @endforeach
            </div>
        </div>
    </div>

    <div class="card mb-4" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border-subtle); border-radius: var(--apx-radius-md);">
        <div class="card-body p-4">
            @if(! $cart->isEmpty())
                <form action="{{ route('shop.cart.update') }}" method="POST">
                    @csrf

                    <div class="table-responsive apx-shop-table mb-3">
                        <table class="table cart-items">
                            <thead>
                            <tr>
                                <th scope="col">{{ trans('messages.fields.name') }}</th>
                                <th scope="col">{{ trans('shop::messages.fields.price') }}</th>
                                <th scope="col">{{ trans('shop::messages.fields.total') }}</th>
                                <th scope="col">{{ trans('shop::messages.fields.quantity') }}</th>
                                <th scope="col" class="text-end">{{ trans('messages.fields.action') }}</th>
                            </tr>
                            </thead>
                            <tbody>

                            @foreach($cart->content() as $cartItem)
                                <tr>
                                    <th scope="row" class="text-white fw-bold">{{ $cartItem->name() }}</th>
                                    <td><span class="text-warning font-monospace">{{ shop_format_amount($cartItem->price()) }}</span></td>
                                    <td><span class="text-warning fw-bold font-monospace">{{ shop_format_amount($cartItem->total()) }}</span></td>
                                    <td>
                                        <input type="number" min="0" max="{{ $cartItem->maxQuantity() }}" size="5" class="form-control form-control-sm d-inline-block bg-dark text-white border-secondary"
                                               name="quantities[{{ $cartItem->itemId }}]" value="{{ $cartItem->quantity }}" aria-label="{{ trans('shop::messages.fields.quantity') }}"
                                               style="max-width: 80px;"
                                               required @if(!$cartItem->hasQuantity()) readonly @endif>
                                    </td>
                                    <td class="text-end">
                                        <a href="{{ route('shop.cart.remove', $cartItem->id) }}" class="btn btn-sm btn-outline-danger" title="{{ trans('messages.actions.delete') }}">
                                            <i class="bi bi-trash"></i>
                                        </a>
                                    </td>
                                </tr>
                            @endforeach

                            </tbody>
                        </table>
                    </div>

                    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                        <button type="submit" class="btn btn-apx-outline btn-sm">
                            <i class="bi bi-arrow-repeat me-1"></i> {{ trans('messages.actions.update') }}
                        </button>
                    </div>
                </form>

                <form method="POST" action="{{ route('shop.cart.clear') }}" class="text-end mb-4">
                    @csrf
                    <button type="submit" class="btn btn-outline-danger btn-sm">
                        <i class="bi bi-trash me-1"></i> {{ trans('shop::messages.cart.clear') }}
                    </button>
                </form>
            @else
                <div class="alert alert-warning d-flex align-items-center gap-2" role="alert">
                    <i class="bi bi-exclamation-triangle-fill fs-5"></i>
                    <div>{{ trans('shop::messages.cart.empty') }}</div>
                </div>
            @endif

            <div class="row g-4 my-3">
                <div class="col-md-5">
                    <div class="p-3 rounded-3" style="background: var(--apx-bg-surface-raised); border: 1px solid var(--apx-border);">
                        <h6 class="text-white mb-2"><i class="bi bi-ticket-perforated text-warning me-1"></i> {{ trans('shop::messages.coupons.add') }}</h6>

                        <form action="{{ route('shop.cart.coupons.add') }}" method="POST">
                            @csrf

                            <div class="input-group @error('coupon') has-validation @enderror">
                                <input type="text" class="form-control bg-dark text-white border-secondary @error('coupon') is-invalid @enderror" id="coupon" name="coupon"
                                       value="{{ old('coupon') }}" placeholder="{{ trans('shop::messages.fields.code') }}" required>

                                <button type="submit" class="btn btn-apx-gold">
                                    <i class="bi bi-plus-lg"></i> {{ trans('messages.actions.add') }}
                                </button>

                                @error('coupon')
                                <span class="invalid-feedback" role="alert"><strong>{{ $message }}</strong></span>
                                @enderror
                            </div>
                        </form>
                    </div>
                </div>

                @if(! $cart->coupons()->isEmpty())
                    <div class="col-md-7">
                        <div class="p-3 rounded-3" style="background: var(--apx-bg-surface-raised); border: 1px solid var(--apx-border);">
                            <h6 class="text-white mb-2">{{ trans('shop::messages.coupons.title') }}</h6>

                            <table class="table table-dark table-sm coupons mb-0">
                                <thead>
                                <tr>
                                    <th scope="col">{{ trans('messages.fields.name') }}</th>
                                    <th scope="col">{{ trans('shop::messages.fields.discount') }}</th>
                                    <th scope="col" class="text-end">{{ trans('messages.fields.action') }}</th>
                                </tr>
                                </thead>
                                <tbody>

                                @foreach($cart->coupons() as $coupon)
                                    <tr>
                                        <th scope="row">{{ $coupon->code }}</th>
                                        <td class="text-success font-monospace">{{ $coupon->is_fixed ? shop_format_amount($coupon->discount) : $coupon->discount.' %' }}</td>
                                        <td class="text-end">
                                            <form action="{{ route('shop.cart.coupons.remove', $coupon) }}" method="POST" class="d-inline-block">
                                                @csrf

                                                <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-2" title="{{ trans('messages.actions.delete') }}">
                                                    <i class="bi bi-x-lg"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                @endforeach

                                </tbody>
                            </table>
                        </div>
                    </div>
                @endif
            </div>

            <!-- Total Price Summary -->
            <div class="p-3 mb-4 rounded-3 d-flex justify-content-between align-items-center flex-wrap gap-2" style="background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(20, 28, 46, 0.95) 100%); border: 1px solid var(--apx-gold-border);">
                <span class="text-muted small">TOTAL PEMBAYARAN:</span>
                <span class="fs-4 fw-bold text-warning font-monospace" style="font-family: 'Cinzel', Georgia, serif;">
                    {{ trans('shop::messages.cart.total', ['total' => shop_format_amount($cart->total())]) }}
                </span>
            </div>

            <form @if(! use_site_money()) action="{{ route('shop.payments.payment') }}" @endif>
                @if(! use_site_money())
                    <div class="d-flex justify-content-end mb-3">
                        @include('shop::cart._terms', ['terms' => $terms])
                    </div>
                @endif

                <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                    <a href="{{ route('shop.home') }}" class="btn btn-apx-outline">
                        <i class="bi bi-arrow-left me-1"></i> {{ trans('shop::messages.cart.back') }}
                    </a>

                    @if(use_site_money())
                        <button type="button" class="btn btn-apx-gold" data-bs-toggle="modal" data-bs-target="#confirmBuyModal">
                            {{ trans('shop::messages.buy') }}
                        </button>
                    @else
                        @if($emailRequired)
                            <div class="me-2">
                                <input type="email" class="form-control bg-dark text-white border-secondary @error('email') is-invalid @enderror" id="email" name="email"
                                       value="{{ old('email') }}" placeholder="{{ trans('auth.email') }}" required>
                            </div>
                        @endif

                        <button type="submit" class="btn btn-apx-gold">
                            <i class="bi bi-cart-check me-1"></i> {{ trans('shop::messages.cart.checkout') }}
                        </button>
                    @endif
                </div>
            </form>
        </div>
    </div>

    @if(use_site_money())
        <div class="modal fade" id="confirmBuyModal" tabindex="-1" role="dialog" aria-labelledby="confirmBuyLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content" style="background: var(--apx-bg-surface); border: 1px solid var(--apx-gold-border); border-radius: var(--apx-radius-lg);">
                    <div class="modal-header" style="background: var(--apx-bg-surface-raised); border-bottom: 1px solid var(--apx-border);">
                        <h4 class="modal-title text-white" id="confirmBuyLabel" style="font-family: 'Cinzel', Georgia, serif;">
                            {{ trans('shop::messages.cart.confirm.title') }}
                        </h4>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4 text-white">
                        {{ trans('shop::messages.cart.confirm.price', ['price' => shop_format_amount($cart->payableTotal())]) }}
                    </div>

                    <form class="modal-footer" method="POST" action="{{ route('shop.cart.payment') }}" style="background: var(--apx-bg-surface-raised); border-top: 1px solid var(--apx-border);">
                        @csrf

                        @include('shop::cart._terms', ['terms' => $terms])

                        <div class="ms-auto d-flex gap-2">
                            <button class="btn btn-apx-outline" type="button" data-bs-dismiss="modal">
                                {{ trans('messages.actions.cancel') }}
                            </button>

                            <button class="btn btn-apx-gold" type="submit">
                                {{ trans('shop::messages.cart.pay') }}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    @endif
@endsection
