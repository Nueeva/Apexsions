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
        Schema::create('apexsions_punishments', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->string('action_id', 64)->nullable()->unique();
            $table->string('player_uuid', 64)->index();
            $table->string('player_name', 64)->index();
            $table->string('type', 32)->index(); // WARN, MUTE, KICK, BAN
            $table->text('reason');
            $table->unsignedBigInteger('staff_id')->nullable()->index();
            $table->string('staff_name', 64)->default('System');
            $table->unsignedBigInteger('duration_seconds')->nullable();
            $table->timestamp('expires_at')->nullable()->index();
            $table->string('status', 16)->default('ACTIVE')->index(); // ACTIVE, EXPIRED, PARDONED
            $table->text('pardon_reason')->nullable();
            $table->string('pardoned_by', 64)->nullable();
            $table->timestamp('pardoned_at')->nullable();
            $table->string('source', 16)->default('WEB')->index(); // WEB, INGAME, API, SYSTEM
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['player_uuid', 'status']);
            $table->index(['type', 'status']);
            $table->index('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_punishments');
    }
};
