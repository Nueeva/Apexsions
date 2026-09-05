<?php

namespace Azuriom\Policies;

use Azuriom\Models\Comment;
use Azuriom\Models\Post;
use Azuriom\Models\User;
use Illuminate\Auth\Access\HandlesAuthorization;

class CommentPolicy
{
    use HandlesAuthorization;

    /**
     * Determine whether the user can comment on the post.
     */
    public function create(User $user, ?Post $post = null): bool
    {
        // Keep post optional for simplifying authorization check in views.
        return ($post === null || $user->can('view', $post))
            && $user->can('comments.create');
    }

    /**
     * Determine whether the user can delete the comment.
     */
    public function delete(User $user, Comment $comment): bool
    {
        return $user->is($comment->author) || $user->can('comments.delete.other');
    }
}
