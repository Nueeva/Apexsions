<?php

namespace Azuriom\Policies;

use Azuriom\Models\Post;
use Azuriom\Models\User;
use Illuminate\Auth\Access\HandlesAuthorization;

class LikePolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the user can like the post.
     */
    public function create(User $user, Post $post): bool
    {
        return $user->can('view', $post);
    }
}
