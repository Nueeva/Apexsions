@component('mail::layout')
{{-- Header --}}
@slot('header')
@component('mail::header', ['url' => config('app.url')])
@php
    $logoUrl = site_logo();
    $isLocal = str_contains($logoUrl, '127.0.0.1') || str_contains($logoUrl, 'localhost');
@endphp
@if(!$isLocal && filter_var($logoUrl, FILTER_VALIDATE_URL))
<img src="{{ $logoUrl }}" class="logo" alt="{{ site_name() }}" style="width: 60px; height: 60px; border-radius: 12px; border: 1px solid rgba(245, 158, 11, 0.6); margin-bottom: 8px; object-fit: cover;">
@else
<table cellpadding="0" cellspacing="0" border="0" align="center" style="margin: 0 auto 10px auto;">
    <tr>
        <td align="center" style="width: 54px; height: 54px; background: #18191f; border: 2px solid #f59e0b; border-radius: 14px; box-shadow: 0 0 16px rgba(245, 158, 11, 0.35); text-align: center; vertical-align: middle;">
            <span style="font-size: 26px; line-height: 54px; display: inline-block;">👑</span>
        </td>
    </tr>
</table>
@endif
<span style="font-size: 22px; font-weight: 800; color: #ffffff; letter-spacing: 0.08em; display: inline-block;">APEXSIONS</span>
@endcomponent
@endslot

{{-- Body --}}
{{ $slot }}

{{-- Subcopy --}}
@isset($subcopy)
@slot('subcopy')
@component('mail::subcopy')
{{ $subcopy }}
@endcomponent
@endslot
@endisset

{{-- Footer --}}
@slot('footer')
@component('mail::footer')
@lang('mail.copyright', [
    'year' => date('Y'),
    'name' => site_name(),
])
@endcomponent
@endslot
@endcomponent
