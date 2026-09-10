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
        if (!Schema::hasTable('apexsions_rank_configs')) {
            Schema::create('apexsions_rank_configs', function (Blueprint $table) {
                $table->id();
                $table->string('rank_key', 32)->unique();
                $table->string('display_name', 64);
                $table->string('badge', 64)->nullable();
                $table->string('prefix', 64)->nullable();
                $table->string('color', 16)->default('#ffd700');
                $table->string('tier', 32)->default('Tier II');
                $table->integer('weight')->default(10);
                $table->integer('order_index')->default(0);
                $table->boolean('is_active')->default(true);
                $table->boolean('is_buyable')->default(true);
                $table->string('banner_image', 255)->nullable();
                $table->string('card_image', 255)->nullable();
                $table->text('description')->nullable();

                // Pricing
                $table->decimal('price_trial_30', 12, 2)->default(0.00);
                $table->decimal('price_trial_90', 12, 2)->default(0.00);
                $table->decimal('price_permanent', 12, 2)->default(0.00);
                $table->decimal('price_upgrade_override', 12, 2)->nullable();
                $table->decimal('discount_percent', 5, 2)->default(0.00);
                $table->decimal('money_reward_permanent', 12, 2)->default(0.00);

                // Benefit Limits
                $table->integer('benefit_max_homes')->default(2);
                $table->integer('benefit_max_auctions')->default(3);
                $table->integer('benefit_max_enchants')->default(4);
                $table->integer('benefit_rtp_cooldown')->default(150); // seconds
                $table->decimal('benefit_shop_sell_bonus', 5, 2)->default(0.00); // percentage, e.g. 3.00 for +3%
                $table->decimal('benefit_xp_bonus', 5, 2)->default(0.00); // percentage, e.g. 5.00 for +5%
                $table->decimal('benefit_bank_multiplier', 4, 2)->default(1.00); // multiplier, e.g. 1.20, 1.50
                $table->integer('benefit_feed_cooldown')->nullable(); // seconds

                // Permissions and Unlocks
                $table->json('benefit_commands')->nullable();
                $table->json('benefit_kits')->nullable();
                $table->string('benefit_nick_permission', 32)->default('none'); // 'none', 'no_color', 'solid_color', 'all_colors'
                $table->string('benefit_battlepass_unlock', 32)->nullable(); // 'sio', 'exsio'
                $table->decimal('benefit_battlepass_discount', 5, 2)->default(0.00); // percentage e.g. 10.00, 15.00

                $table->timestamps();
            });
        }

        if (Schema::hasTable('apexsions_rank_purchases')) {
            Schema::table('apexsions_rank_purchases', function (Blueprint $table) {
                if (!Schema::hasColumn('apexsions_rank_purchases', 'is_upgrade')) {
                    $table->boolean('is_upgrade')->default(false)->after('rank_type');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'previous_rank')) {
                    $table->string('previous_rank', 32)->nullable()->after('is_upgrade');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'normal_price')) {
                    $table->decimal('normal_price', 12, 2)->nullable()->after('price_paid');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'discount_amount')) {
                    $table->decimal('discount_amount', 12, 2)->default(0.00)->after('normal_price');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'payment_method')) {
                    $table->string('payment_method', 32)->default('WHATSAPP_MANUAL')->after('source');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'sync_status')) {
                    $table->string('sync_status', 32)->default('DELIVERED')->after('status');
                }
                if (!Schema::hasColumn('apexsions_rank_purchases', 'delivery_id')) {
                    $table->unsignedBigInteger('delivery_id')->nullable()->after('sync_status');
                }
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_rank_configs');

        if (Schema::hasTable('apexsions_rank_purchases')) {
            Schema::table('apexsions_rank_purchases', function (Blueprint $table) {
                $columns = ['is_upgrade', 'previous_rank', 'normal_price', 'discount_amount', 'payment_method', 'sync_status', 'delivery_id'];
                foreach ($columns as $column) {
                    if (Schema::hasColumn('apexsions_rank_purchases', $column)) {
                        $table->dropColumn($column);
                    }
                }
            });
        }
    }
};
