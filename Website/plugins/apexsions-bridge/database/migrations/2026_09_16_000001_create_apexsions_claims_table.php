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
        Schema::create('apexsions_claims', function (Blueprint $table) {
            $table->id();
            $table->uuid('claim_id')->unique();
            $table->uuid('owner_uuid')->index();
            $table->string('owner_name', 64)->index();
            $table->string('world', 128)->index();
            $table->integer('chunk_x');
            $table->integer('chunk_z');
            $table->integer('trusted_count')->default(0);
            $table->timestamp('in_game_created_at')->nullable();
            $table->timestamps();

            $table->unique(['world', 'chunk_x', 'chunk_z'], 'uq_web_claim_chunk');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_claims');
    }
};
