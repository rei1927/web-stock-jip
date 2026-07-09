<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    public function index(Request $request)
    {
        return response()->json(User::all());
    }

    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['Kredensial tidak valid.'],
            ]);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'Login berhasil',
            'access_token' => $token,
            'token_type' => 'Bearer',
            'user' => $user
        ]);
    }

    public function register(Request $request)
    {
        // Hanya super admin yang boleh menambahkan user dari aplikasi
        if ($request->user()->role !== 'super_admin') {
            return response()->json(['message' => 'Unauthorized. Only Super Admin can add users.'], 403);
        }

        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:4',
            'role' => 'required|string',
        ]);

        $roleMap = [
            'Super Admin' => 'super_admin',
            'Admin' => 'admin',
            'Sales' => 'sales',
            'Sales Manager' => 'sales_manager',
            'Manager' => 'sales_manager',
        ];
        $role = $request->role;
        $dbRole = $roleMap[$role] ?? strtolower($role);

        $newUser = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role' => $dbRole,
        ]);

        return response()->json([
            'message' => 'User berhasil didaftarkan ke server',
            'user' => $newUser
        ], 201);
    }

    public function updateUser(Request $request)
    {
        // Hanya super admin yang boleh mengubah user dari aplikasi
        if ($request->user()->role !== 'super_admin') {
            return response()->json(['message' => 'Unauthorized. Only Super Admin can update users.'], 403);
        }

        $request->validate([
            'email' => 'required|email|exists:users,email',
            'role' => 'required|string',
        ]);

        $roleMap = [
            'Super Admin' => 'super_admin',
            'Admin' => 'admin',
            'Sales' => 'sales',
            'Sales Manager' => 'sales_manager',
            'Manager' => 'sales_manager',
        ];
        $role = $request->role;
        $dbRole = $roleMap[$role] ?? strtolower($role);

        $user = User::where('email', $request->email)->first();
        $user->role = $dbRole;
        $user->save();

        return response()->json([
            'message' => 'Role user berhasil diupdate',
            'user' => $user
        ]);
    }

    public function profile(Request $request)
    {
        return response()->json([
            'user' => $request->user()
        ]);
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'message' => 'Berhasil logout'
        ]);
    }
}
