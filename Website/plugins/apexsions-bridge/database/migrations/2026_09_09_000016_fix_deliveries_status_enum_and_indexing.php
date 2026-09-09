<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        if (Schema::hasTable('deliveries')) {
            // Alter deliveries status column to VARCHAR(32) so PROCESSING, DELIVERED, FAILED, PENDING, QUEUED work safely without strict truncation errors
            try {
                DB::statement("ALTER TABLE deliveries MODIFY status VARCHAR(32) NOT NULL DEFAULT 'PENDING'");
            } catch (\Throwable $e) {
                // Fallback for non-MySQL or platforms where MODIFY statement syntax differs
                Schema::table('deliveries', function (Blueprint $table) {
                    $table->string('status', 32)->default('PENDING')->change();
                });
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        if (Schema::hasTable('deliveries')) {
            try {
                DB::statement("ALTER TABLE deliveries MODIFY status ENUM('PENDING', 'QUEUED', 'DELIVERED', 'FAILED') NOT NULL DEFAULT 'PENDING'");
            } catch (\Throwable $e) {
                // Fallback
            }
        }
    }
};
