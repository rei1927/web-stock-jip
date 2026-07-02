package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.NotificationEntity
import com.example.ui.theme.*
import androidx.compose.ui.window.Dialog

@Composable
fun DashboardScreen(
    viewModel: PropertyViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToReviewPenjualan: () -> Unit,
    onNavigateToGimmick: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSalesRecap: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()

    var showBroadcastDialog by remember { mutableStateOf(false) }
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val userInitial = currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "U"
    val role = currentUser?.role?.trim() ?: "Sales"
    val isAdminOrSuperAdmin = role.equals("Admin", ignoreCase = true) || role.equals("Super Admin", ignoreCase = true)
    val isManagerOrAbove = role.equals("Sales Manager", ignoreCase = true) || isAdminOrSuperAdmin

    // Compute live analytics from Room Database
    val totalStock = allUnits.size
    val availableCount = allUnits.count { it.status == "Tersedia" }
    val holdCount = allUnits.count { it.status == "Hold" }
    val soldCount = allUnits.count { it.status == "Terjual" }

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    // Snackbars for new notifications (simulated Push)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(notifications.size) {
        if (notifications.isNotEmpty()) {
            val latest = notifications.first()
            // Only show snackbar if it happened in the last 10 seconds (to avoid spamming on start)
            if (System.currentTimeMillis() - latest.timestamp < 10000) {
                snackbarHostState.showSnackbar(
                    message = "${latest.title}: ${latest.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = Color(0xFFF3F4F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dashboard active tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { }
                            .padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(TersediaGreenBg, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Domain,
                                contentDescription = "Dashboard",
                                tint = NavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dashboard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                    }

                    // Stock tab shortcut
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigateToSearch() }
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Stock",
                            tint = ContentSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ContentSecondary
                        )
                    }

                    // Sales report tab shortcut
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigateToReport() }
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Laporan",
                            tint = ContentSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Omzet",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ContentSecondary
                        )
                    }

                    // Attendance tab shortcut
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigateToAttendance() }
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Absensi",
                            tint = ContentSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isAdminOrSuperAdmin) "Monitoring" else "Absen",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentSecondary
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header element
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NavyPrimary, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Domain,
                            contentDescription = "Real Estate",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Jaya Imperial",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            lineHeight = 18.sp
                        )
                        if (isSyncing) {
                            Text(
                                text = "MENSINKRONISASI DATA...",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                letterSpacing = 1.0.sp
                            )
                        } else if (syncError != null) {
                            Text(
                                text = "OFFLINE - GAGAL SYNC",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Red,
                                letterSpacing = 1.0.sp
                            )
                        } else {
                            Text(
                                text = "OFFLINE MANAGEMENT SYSTEM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentSecondary,
                                letterSpacing = 1.0.sp
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAdminOrSuperAdmin) {
                        IconButton(
                            onClick = { showBroadcastDialog = true },
                            modifier = Modifier.size(40.dp).background(GoldAccent.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Broadcast", tint = NavyPrimary)
                        }
                    }

                    Box(modifier = Modifier.size(40.dp)) {
                        IconButton(
                            onClick = { onNavigateToNotifications() },
                            modifier = Modifier.fillMaxSize().testTag("bell_notification")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = NavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(TerjualRed, shape = CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(TersediaGreenBg, shape = CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }
                }
            }

            // Expanded Live Database statistics including holds!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "IKHTISAR STOK UNIT PROYEK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "TOTAL UNIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "$totalStock", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ContentPrimary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderLight))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "TERSEDIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "$availableCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TersediaGreen)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderLight))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "HOLD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "$holdCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderLight))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "SOLD OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "$soldCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TerjualRed)
                            }
                        }
                    }
                }
            }

            // Services list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Greeting Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selamat Datang,",
                            fontSize = 11.sp,
                            color = ContentSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.name ?: "Pengguna",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ContentPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(NavyPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = role.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = onLogout,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar",
                            tint = TerjualRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log Out",
                            fontSize = 11.sp,
                            color = TerjualRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title Section
                Text(
                    text = "MENU UTAMA SISTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                // Menu items
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuCard(
                        title = "DAFTAR STOK UNIT & TRANSKASI",
                        subtitle = "Lihat status ketersediaan unit, " + when (role) {
                            "Sales" -> "lakukan hold, dan ajukan klaim penjualan sold."
                            "Sales Manager" -> "hold unit, dan approve pengajuan sold dari sales."
                            else -> "kelola stok unit properti secara lengkap."
                        },
                        icon = Icons.Default.HomeWork,
                        iconColor = NavyPrimary,
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("menu_cari_stok")
                    )

                    MenuCard(
                        title = "LAPORAN OMZET BULANAN",
                        subtitle = when (role) {
                            "Sales" -> "Lihat performa laporan omzet grafik dan pencapaian komparasi pribadi."
                            "Sales Manager" -> "Pantau performa omzet individu tim sales dan target kelompok."
                            "Admin" -> "Analisis visual omzet penjualan developer dan tim sales manager."
                            else -> "Akses penuh visualisasi grafik performa omzet agensi & cabang."
                        },
                        icon = Icons.Default.TrendingUp,
                        iconColor = GoldAccent,
                        onClick = onNavigateToReport,
                        modifier = Modifier.testTag("menu_laporan_omzet")
                    )

                    MenuCard(
                        title = "SIMULASI KPR INTERAKTIF",
                        subtitle = "Bantu nasabah memproyeksikan angsuran bulanan berbasis rasio bunga.",
                        icon = Icons.Default.Calculate,
                        iconColor = NavyPrimary,
                        onClick = onNavigateToCalculator,
                        modifier = Modifier.testTag("menu_kpr_kalkulator")
                    )

                    if (role.equals("Sales", ignoreCase = true)) {
                        MenuCard(
                            title = "REKAP PENJUALAN SAYA",
                            subtitle = "Pantau status unit yang Anda Hold, sedang diajukan (Pending), maupun yang sudah Sold.",
                            icon = Icons.Default.Assignment,
                            iconColor = NavyPrimary,
                            onClick = onNavigateToSalesRecap,
                            modifier = Modifier.testTag("menu_sales_recap")
                        )
                    }

                    if (isManagerOrAbove) {
                        MenuCard(
                            title = "REVIEW & APPROVE PENJUALAN",
                            subtitle = "Periksa detail unit properti dan berikan persetujuan klaim penjualan sales.",
                            icon = Icons.Default.RateReview,
                            iconColor = GoldAccent,
                            onClick = onNavigateToReviewPenjualan,
                            modifier = Modifier.testTag("menu_review_penjualan")
                        )

                        MenuCard(
                            title = "PENGAJUAN GIMMICK HADIAH",
                            subtitle = if (role == "Sales Manager") "Ajukan hadiah tambahan untuk unit terjual tim Anda."
                                       else "Review dan setujui pengajuan gimmick dari Sales Manager.",
                            icon = Icons.Default.CardGiftcard,
                            iconColor = Color(0xFFE91E63),
                            onClick = onNavigateToGimmick,
                            modifier = Modifier.testTag("menu_gimmick_management")
                        )
                    }

                    if (isAdminOrSuperAdmin) {
                        MenuCard(
                            title = "KELOLA AKUN & PENGGUNA TIM",
                            subtitle = "Tambah akun sales/executive, hapus akun, dan sesuaikan penugasan manajer.",
                            icon = Icons.Default.GroupAdd,
                            iconColor = Color(0xFF2E7D32),
                            onClick = onNavigateToUserManagement,
                            modifier = Modifier.testTag("menu_user_management")
                        )
                    }
                }
            }

            // Informational Notice Board
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Integrasi Sinc Proyek-Web Ready", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                        Text(text = "Seluruh manipulasi data lokal (DB SQLite) siap dihubungkan dengan API web panel internal CRUD jika infrastruktur web hosting telah daring.", fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                    }
                }
            }
            if (showBroadcastDialog) {
                BroadcastDialog(
                    onDismiss = { showBroadcastDialog = false },
                    onSend = { title, message ->
                        viewModel.broadcastNotification(title, message)
                        showBroadcastDialog = false
                    }
                )
            }

            // FCM Token Info (Admin Only)
            if (isAdminOrSuperAdmin) {
                val token by viewModel.fcmToken.collectAsState()
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("DEBUG: FCM TOKEN (FOR LARAVEL)", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SelectionContainer {
                            Text(token, color = Color.White, fontSize = 9.sp, lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(iconColor.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = ContentSecondary, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun BroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Broadcast Notifikasi", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NavyPrimary)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Notifikasi") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Isi Pesan") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSend(title, message) },
                        enabled = title.isNotBlank() && message.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim")
                    }
                }
            }
        }
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 0) return "Baru saja"
    val diffSecs = diff / 1000
    if (diffSecs < 60) return "Baru saja"
    val diffMins = diffSecs / 60
    if (diffMins < 60) return "$diffMins menit lalu"
    val diffHours = diffMins / 60
    if (diffHours < 24) return "$diffHours jam lalu"
    val diffDays = diffHours / 24
    return "$diffDays hari lalu"
}
