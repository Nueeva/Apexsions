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
        Schema::create('deliveries', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('order_item_id')->nullable()->index();
            $table->unsignedBigInteger('server_id')->nullable()->index();
            $table->string('player_uuid', 36)->index();
            $table->string('player_username', 32)->index();
            $table->text('command');
            $table->enum('status', ['PENDING', 'QUEUED', 'DELIVERED', 'FAILED'])->default('PENDING')->index();
            $table->timestamp('executed_at')->nullable();
            $table->text('error_message')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('deliveries');
    }
};
