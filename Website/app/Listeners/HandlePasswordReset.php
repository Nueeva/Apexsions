<?php

namespace Azuriom\Listeners;

use Azuriom\Notifications\PasswordChanged;
use Illuminate\Auth\Events\PasswordReset;

class HandlePasswordReset
{
    /**
     * Handle the event.
     */
    public function handle(PasswordReset $event): void
    {
        $event->user->forceFill([
            'access_token' => null,
            'password_changed_at' => now(),
        ])->save();

        $event->user->notify(new PasswordChanged());
    }
}
