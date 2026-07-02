package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Retrieve repository from JayaImperialApp context structure
    private val viewModel: PropertyViewModel by viewModels {
        PropertyViewModelFactory((application as JayaImperialApp).repository)
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                viewModel.setFcmToken(token ?: "Gagal mendapatkan token")
            } else {
                Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                viewModel.setFcmToken("Gagal: ${task.exception?.message}")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge display compliance
        enableEdgeToEdge()

        fetchFcmToken()
        askNotificationPermission()

        setContent {
                MyApplicationTheme {
                    val navController = rememberNavController()
                    val currentUser by viewModel.currentUser.collectAsState()

                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. LOGIN SCREEN SCREEN
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. MAIN DASHBOARD SCREEN
                        composable("dashboard") {
                            // Use LaunchedEffect to handle navigation side-effects
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            }

                            // Only render Dashboard if user is present to prevent "flickering" or null access
                            currentUser?.let {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToSearch = {
                                        viewModel.syncData() // Trigger Sync
                                        navController.navigate("stock_list")
                                    },
                                    onNavigateToCalculator = { navController.navigate("calculator") },
                                    onNavigateToReport = {
                                        viewModel.syncData() // Trigger Sync for reports context
                                        navController.navigate("report")
                                    },
                                    onNavigateToUserManagement = { navController.navigate("user_management") },
                                    onNavigateToReviewPenjualan = {
                                        viewModel.syncData() // Trigger Sync
                                        navController.navigate("review_penjualan")
                                    },
                                    onNavigateToGimmick = {
                                        // Gimmick requests are Flow-based, but syncData handles Units which are often context
                                        viewModel.syncData()
                                        navController.navigate("gimmick_management")
                                    },
                                    onNavigateToAttendance = { navController.navigate("attendance") },
                                    onNavigateToNotifications = { navController.navigate("notifications") },
                                    onNavigateToSalesRecap = { navController.navigate("sales_recap") },
                                    onLogout = {
                                        viewModel.logout()
                                        // The LaunchedEffect above will handle the navigation to "login"
                                    }
                                )
                            }
                        }

                        // 3. HOUSING LISTINGS STOCK SCREEN
                        composable("stock_list") {
                            StockListScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 4. MORTGAGE SIMULATION CALCULATOR SCREEN
                        composable("calculator") {
                            KPRCalculatorScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 5. MONTHLY SALES TURNOVER REPORT EXECUTIVE CHART SCREEN
                        composable("report") {
                            OmzetReportScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. USER ORGANIZATIONAL MANAGEMENT SCREEN
                        composable("user_management") {
                            UserManagementScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 7. REVIEW & APPROVE PENJUALAN
                        composable("review_penjualan") {
                            ReviewPenjualanScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 8. GIMMICK MANAGEMENT
                        composable("gimmick_management") {
                            GimmickManagementScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 9. ATTENDANCE
                        composable("attendance") {
                            AttendanceScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 10. NOTIFICATIONS
                        composable("notifications") {
                            NotificationScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 11. SALES RECAP
                        composable("sales_recap") {
                            SalesRecapScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
        }
    }
}
