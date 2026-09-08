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
        // 1. Centralized Reports Table
        Schema::create('apexsions_reports', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->unsignedBigInteger('in_game_report_id')->nullable()->index();
            $table->string('reporter_uuid', 64)->index();
            $table->string('reporter_name', 64)->index();
            $table->string('reported_uuid', 64)->index();
            $table->string('reported_name', 64)->index();
            $table->string('reason', 255);
            $table->text('description')->nullable();
            $table->string('server', 64)->default('apexsions-survival');
            $table->string('world', 64)->default('world');
            $table->string('status', 32)->default('OPEN')->index(); // OPEN, CLAIMED, INVESTIGATING, RESOLVED, DISMISSED
            $table->string('priority', 16)->default('MEDIUM')->index(); // LOW, MEDIUM, HIGH, CRITICAL
            $table->unsignedBigInteger('assigned_staff_id')->nullable()->index();
            $table->string('assigned_staff_name', 64)->nullable();
            $table->timestamp('assigned_at')->nullable();
            $table->text('resolution')->nullable();
            $table->timestamp('resolved_at')->nullable();
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['status', 'priority']);
            $table->index('created_at');
        });

        // 2. Staff Notes on Reports
        Schema::create('apexsions_report_notes', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->unsignedBigInteger('report_id')->index();
            $table->unsignedBigInteger('author_id')->nullable()->index();
            $table->string('author_name', 64);
            $table->text('note');
            $table->softDeletes();
            $table->timestamps();

            $table->foreign('report_id')->references('id')->on('apexsions_reports')->cascadeOnDelete();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_report_notes');
        Schema::dropIfExists('apexsions_reports');
    }
};
