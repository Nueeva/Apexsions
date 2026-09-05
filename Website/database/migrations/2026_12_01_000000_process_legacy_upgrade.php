<?php

use Illuminate\Database\Migrations\Migration;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        if (file_exists($postUpgrade = base_path('post_upgrade.php'))) {
            include $postUpgrade;
            @unlink($postUpgrade);
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        //
    }
};
