<?php
require __DIR__.'/vendor/autoload.php';
$app = require_once __DIR__.'/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Http\Kernel::class);

// Login as super admin
$user = \App\Models\User::where('email', 'reizarachmattullah@gmail.com')->first();
auth()->login($user);

$response = $kernel->handle(
    $request = Illuminate\Http\Request::create('/users', 'GET')
);

if ($response->getStatusCode() === 500) {
    echo "500 Error: " . $response->exception->getMessage() . "\n" . $response->exception->getTraceAsString();
} else {
    echo "Status: " . $response->getStatusCode() . "\n";
}
