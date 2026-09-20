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
        Schema::create('apexsions_bounties', function (Blueprint $table) {
            $table->id();
            $table->uuid('target_uuid')->unique();
            $table->string('target_name', 64)->index();
            $table->double('total_amount')->default(0);
            $table->integer('contributor_count')->default(0);
            $table->json('top_contributors')->nullable();
            $table->timestamp('last_synced_at')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_bounties');
    }
};
