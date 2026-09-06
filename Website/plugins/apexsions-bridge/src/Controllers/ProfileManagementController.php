<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Carbon\Carbon;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class ProfileManagementController extends Controller
{
    /**
     * Get verified minecraft account of the currently authenticated user.
     */
    protected function getLinkedAccount(): ?MinecraftAccount
    {
        $user = Auth::user();
        if (!$user) {
            return null;
        }

        return MinecraftAccount::where('user_id', $user->id)
            ->whereNotNull('verified_at')
            ->first();
    }

    /**
     * Request in-game AuthMe password reset via safe delivery queue.
     */
    public function resetPassword(Request $request): RedirectResponse
    {
        $account = $this->getLinkedAccount();
        if (!$account) {
            return back()->with('error', 'Anda harus menautkan akun Minecraft terlebih dahulu.');
        }

        $validated = $request->validate([
            'new_password' => ['required', 'string', 'min:6', 'max:64', 'confirmed'],
        ]);

        $newPassword = $validated['new_password'];

        // Enqueue AuthMe password change command
        Delivery::create([
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => 'authme changepassword ' . $account->minecraft_username . ' ' . $newPassword,
            'status' => 'PENDING',
        ]);

        return back()->with('success', 'Permintaan ganti password in-game berhasil dikirim! Server akan memperbarui password Anda dalam beberapa saat.');
    }

    /**
     * Change active in-game title from web.
     */
    public function changeTitle(Request $request): RedirectResponse
    {
        $account = $this->getLinkedAccount();
        if (!$account) {
            return back()->with('error', 'Akun Minecraft belum terhubung.');
        }

        $validated = $request->validate([
            'title_id' => ['required', 'string', 'max:64'],
        ]);

        $titleId = strtolower(trim($validated['title_id']));
        $cmd = ($titleId === 'none' || $titleId === 'unequip')
            ? 'titles unequip ' . $account->minecraft_username
            : 'titles equip ' . $account->minecraft_username . ' ' . $titleId;

        Delivery::create([
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        return back()->with('success', 'Gelar in-game sedang diperbarui oleh server.');
    }

    /**
     * Unlink Minecraft account from the website account.
     */
    public function unlink(Request $request): RedirectResponse
    {
        $account = $this->getLinkedAccount();
        if (!$account) {
            return back()->with('error', 'Tidak ada akun Minecraft yang ditautkan.');
        }

        $account->delete();

        return back()->with('success', 'Akun Minecraft berhasil diputuskan tautannya. Anda dapat menautkan akun baru kapan saja.');
    }

    /**
     * Claim daily web bonus reward delivered directly in-game.
     */
    public function claimReward(Request $request): RedirectResponse
    {
        $account = $this->getLinkedAccount();
        if (!$account) {
            return back()->with('error', 'Tautkan akun Minecraft Anda terlebih dahulu untuk mengklaim hadiah!');
        }

        // Daily cooldown check (24 hours)
        $cacheKey = 'daily_web_reward_' . $account->id;
        if (cache()->has($cacheKey)) {
            return back()->with('error', 'Anda sudah mengklaim hadiah harian hari ini. Silakan coba lagi besok!');
        }

        // Enqueue in-game rewards (+5,000 Rupiah & +100 EXP)
        Delivery::create([
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => 'eco give ' . $account->minecraft_username . ' 5000',
            'status' => 'PENDING',
        ]);

        Delivery::create([
            'player_uuid' => $account->minecraft_uuid,
            'player_username' => $account->minecraft_username,
            'command' => 'ac addxp ' . $account->minecraft_username . ' 25',
            'status' => 'PENDING',
        ]);

        // Set cooldown for 24 hours
        cache()->put($cacheKey, Carbon::now()->toIso8601String(), 86400);

        return back()->with('success', '🎉 Berhasil mengklaim Hadiah Harian Web (+Rp 5.000 & +25 EXP)! Hadiah akan langsung masuk ke karakter Anda.');
    }
}
