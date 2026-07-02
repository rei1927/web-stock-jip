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
        Schema::create('transactions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('unit_id')->constrained('units')->cascadeOnDelete();
            $table->foreignId('sales_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('customer_id')->nullable()->constrained('customers')->nullOnDelete();
            $table->foreignId('admin_id')->nullable()->constrained('users')->nullOnDelete();
            $table->enum('status', ['hold', 'request_booking', 'approved', 'rejected']);
            $table->text('notes')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('transactions');
    }
};
