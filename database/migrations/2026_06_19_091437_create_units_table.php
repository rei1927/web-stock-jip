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
        Schema::create('units', function (Blueprint $table) {
            $table->id();
            $table->foreignId('cluster_id')->constrained('clusters')->cascadeOnDelete();
            $table->string('name');
            $table->string('block');
            $table->decimal('selling_price', 15, 2);
            $table->json('unit_details')->nullable();
            $table->enum('status', ['available', 'hold', 'request_booking', 'booked', 'sold'])->default('available');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('units');
    }
};
