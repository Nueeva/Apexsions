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
        Schema::create('minecraft_accounts', function (Blueprint $table) {
            $table->id();
            $table->unsignedInteger('user_id')->index();
            $table->string('edition', 16)->default('JAVA'); // JAVA, BEDROCK
            $table->string('auth_mode', 32)->default('JAVA_ONLINE'); // JAVA_ONLINE, JAVA_OFFLINE, BEDROCK_FLOODGATE
            $table->string('minecraft_uuid', 36)->nullable()->index();
            $table->string('minecraft_username', 32)->index();
            $table->string('floodgate_uuid', 36)->nullable();
            $table->string('verification_code', 10)->nullable()->index();
            $table->timestamp('verification_expires_at')->nullable();
            $table->timestamp('verified_at')->nullable();
            $table->timestamp('last_seen_at')->nullable();
            $table->timestamps();

            $table->foreign('user_id')->references('id')->on('users')->cascadeOnDelete();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('minecraft_accounts');
    }
};
