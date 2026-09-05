@props(['url'])
<tr>
<td class="header">
<a href="{{ $url }}" style="display: inline-block; text-decoration: none;">
@if (trim($slot) === 'Laravel')
<span style="font-size: 22px; font-weight: 800; color: #ffffff; letter-spacing: 0.08em;">APEXSIONS</span>
@else
{!! $slot !!}
@endif
<br>
<span style="font-size: 10px; letter-spacing: 0.2em; color: #d97706; text-transform: uppercase; font-weight: 600;">THE PEAK CIVILIZATIONS</span>
</a>
</td>
</tr>
