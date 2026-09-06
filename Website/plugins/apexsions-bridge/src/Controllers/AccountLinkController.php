<?php

namespace Azuriom\Plugin\ApexsionsBridge\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;

class AccountLinkController extends Controller
{
    /**
     * Show player's linked Minecraft accounts.
     */
    public function index()
    {
        $accounts = MinecraftAccount::where('user_id', auth()->id())->get();

        return view('apexsions-bridge::link', [
            'accounts' => $accounts,
        ]);
    }

    /**
     * Generate a 6-digit linking PIN for the player to enter in-game.
     */
    public function generatePin(Request $request)
    {
        $validated = $request->validate([
            'username' => ['required', 'string', 'min:3', 'max:32', 'regex:/^[a-zA-Z0-9_.*]+$/'],
            'edition' => ['required', 'in:JAVA,BEDROCK'],
        ]);

        $username = trim($validated['username']);
        $edition = $validated['edition'];

        // Bedrock players often have '.' or '*' prefix from Floodgate
        $authMode = ($edition === 'BEDROCK') ? 'BEDROCK_FLOODGATE' : 'JAVA_ONLINE';

        // Check if username is already verified by another user
        $alreadyVerified = MinecraftAccount::where('minecraft_username', $username)
            ->whereNotNull('verified_at')
            ->where('user_id', '!=', auth()->id())
            ->exists();

        if ($alreadyVerified) {
            return back()->with('error', 'Username Minecraft ini telah ditautkan dan diverifikasi oleh akun lain.');
        }

        // Enforce max 1 Java and 1 Bedrock account per web user
        $existingEdition = MinecraftAccount::where('user_id', auth()->id())
            ->where('edition', $edition)
            ->whereNotNull('verified_at')
            ->first();

        if ($existingEdition && strcasecmp($existingEdition->minecraft_username, $username) !== 0) {
            return back()->with('error', "Anda sudah memiliki akun {$edition} yang terverifikasi ({$existingEdition->minecraft_username}). Lepaskan akun tersebut jika ingin mengganti akun.");
        }

        // Generate 6-digit secure PIN with 5-minute validity window
        $code = str_pad((string) random_int(100000, 999999), 6, '0', STR_PAD_LEFT);

        MinecraftAccount::updateOrCreate(
            [
                'user_id' => auth()->id(),
                'minecraft_username' => $username,
            ],
            [
                'edition' => $edition,
                'auth_mode' => $authMode,
                'verification_code' => $code,
                'verification_expires_at' => Carbon::now()->addMinutes(5),
            ]
        );

        return back()->with('success_pin', [
            'code' => $code,
            'username' => $username,
            'expires_at' => Carbon::now()->addMinutes(5)->toDateTimeString(),
        ]);
    }

    /**
     * Unlink a linked account.
     */
    public function unlink(MinecraftAccount $account)
    {
        if ($account->user_id !== auth()->id()) {
            abort(403);
        }

        $account->delete();

        return back()->with('success', 'Akun Minecraft berhasil dilepas.');
    }
}
