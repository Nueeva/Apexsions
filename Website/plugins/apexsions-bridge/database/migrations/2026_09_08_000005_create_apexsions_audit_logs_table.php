<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('apexsions_audit_logs', function (Blueprint $table) {
            $table->id();
            $table->string('actor_type', 32)->default('USER');
            $table->string('actor_id', 64)->nullable()->index();
            $table->string('actor_name', 64)->default('System');
            $table->string('action', 64)->index();
            $table->string('target_type', 32)->nullable()->index();
            $table->string('target_id', 64)->nullable()->index();
            $table->string('target_name', 64)->nullable();
            $table->text('old_value')->nullable();
            $table->text('new_value')->nullable();
            $table->string('reason', 255)->nullable();
            $table->string('source', 16)->default('WEB')->index(); // WEB, API, INGAME, SYSTEM
            $table->string('status', 16)->default('SUCCESS')->index(); // PENDING, SUCCESS, FAILED
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_audit_logs');
    }
};
