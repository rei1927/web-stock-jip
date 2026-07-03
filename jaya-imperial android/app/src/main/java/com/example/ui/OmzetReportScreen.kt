package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SalesLog
import com.example.data.User
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmzetReportScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val rawSalesLogs by viewModel.salesLogs.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val role = currentUser?.role ?: "Sales"
    val username = currentUser?.username ?: ""

    // Manager / Admin sub-filtering state
    var selectedSalespersonFilter by remember { mutableStateOf<String?> (null) }
    var selectedManagerFilter by remember { mutableStateOf<String?> (null) }

    // Derive reporting logs based on selections and authorizations
    val reportingLogs = remember(rawSalesLogs, currentUser, selectedSalespersonFilter, selectedManagerFilter, startDate, endDate) {
        var logs = rawSalesLogs.filter { it.timestamp in startDate..endDate }

        when (role) {
            "Sales" -> {
                logs = logs.filter { it.soldBy == username }
            }
            "Sales Manager" -> {
                val teamMembers = allUsers.filter { it.managerName == username }.map { it.username }
                logs = logs.filter { it.managerName == username || teamMembers.contains(it.soldBy) || it.soldBy == username }

                if (selectedSalespersonFilter != null) {
                    logs = logs.filter { it.soldBy == selectedSalespersonFilter }
                }
            }
            else -> {
                if (selectedManagerFilter != null) {
                    logs = logs.filter { it.managerName == selectedManagerFilter }
                }
                if (selectedSalespersonFilter != null) {
                    logs = logs.filter { it.soldBy == selectedSalespersonFilter }
                }
            }
        }
        logs
    }

    // Recalculate indicators dynamically
    val chartMonthlySales = remember(reportingLogs) {
        val list = MutableList(12) { 0.0 }
        reportingLogs.forEach { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            val month = cal.get(Calendar.MONTH)
            if (month in 0..11) {
                list[month] += log.salePrice
            }
        }
        list.toList()
    }

    val totalOmzet = remember(chartMonthlySales) { chartMonthlySales.sum() }
    val soldCount = remember(reportingLogs) { reportingLogs.size }
    val averageUnitPrice = remember(totalOmzet, soldCount) { if (soldCount > 0) totalOmzet / soldCount else 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Laporan & Grafik Omzet",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Dashboard",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title & Info
            Text(
                text = when (role) {
                    "Sales" -> "Analisis Omzet Pribadi"
                    "Sales Manager" -> "Analisis Omzet Tim"
                    else -> "Analisis Omzet Developer"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = NavyDark
            )
            Text(
                text = "Periode: ${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- DATE RANGE SELECTOR ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("PILIH RENTANG PERIODE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SoftBackground, RoundedCornerShape(8.dp))
                                .clickable { showStartDatePicker = true }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Mulai", fontSize = 9.sp, color = Color.Gray)
                                Text(dateFormatter.format(Date(startDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SoftBackground, RoundedCornerShape(8.dp))
                                .clickable { showEndDatePicker = true }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Sampai", fontSize = 9.sp, color = Color.Gray)
                                Text(dateFormatter.format(Date(endDate)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            }
                        }
                    }
                }
            }

            // --- CUSTOM CONTROLS BASED ON ROLES ---
            if (role == "Sales Manager") {
                val teamList = allUsers.filter { it.managerName == username }
                if (teamList.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "FILTER PERSONEL TIM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedSalespersonFilter == null,
                                    onClick = { selectedSalespersonFilter = null },
                                    label = { Text("Semua Tim", fontSize = 10.sp) }
                                )
                                teamList.forEach { member ->
                                    FilterChip(
                                        selected = selectedSalespersonFilter == member.username,
                                        onClick = { selectedSalespersonFilter = member.username },
                                        label = { Text(member.name.substringBefore(" "), fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (role == "Admin" || role == "Super Admin") {
                // Admin can filter by manager team OR specific sales
                val managers = allUsers.filter { it.role == "Sales Manager" }
                val salespersons = allUsers.filter { it.role == "Sales" }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                "FILTER TIM MANAGER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedManagerFilter == null,
                                    onClick = { selectedManagerFilter = null },
                                    label = { Text("Semua Tim", fontSize = 10.sp) }
                                )
                                managers.forEach { mgr ->
                                    FilterChip(
                                        selected = selectedManagerFilter == mgr.username,
                                        onClick = { selectedManagerFilter = mgr.username },
                                        label = { Text(mgr.name.substringBefore(" "), fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        if (salespersons.isNotEmpty()) {
                            Column {
                                Text(
                                    "FILTER PERSONAL SALES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedSalespersonFilter == null,
                                        onClick = { selectedSalespersonFilter = null },
                                        label = { Text("Semua Sales", fontSize = 10.sp) }
                                    )
                                    val filteredAgents = if (selectedManagerFilter != null) {
                                        salespersons.filter { it.managerName == selectedManagerFilter }
                                    } else salespersons

                                    filteredAgents.take(4).forEach { agent ->
                                        FilterChip(
                                            selected = selectedSalespersonFilter == agent.username,
                                            onClick = { selectedSalespersonFilter = agent.username },
                                            label = { Text(agent.name.substringBefore(" "), fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main Bar Canvas Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Grafik Pendapatan Omzet (Rupiah)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        CanvasAndTrendBarChart(monthlyData = chartMonthlySales)
                    }

                    // Month Labels Row
                    val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt", "Sep", "Okt", "Nov", "Des")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        months.forEach { month ->
                            Text(
                                text = month,
                                fontSize = 10.sp,
                                color = ContentLight,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(22.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Consolidated Key Results Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "AKUMULASI OMZET DIPILIH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.0.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatRupiahShort(totalOmzet),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyPrimary
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = BorderLight)

                    // Specs Grid Row (Unit Terjual | Rata-rata Harga Unit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Unit Sah Terjual",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$soldCount Unit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentPrimary
                            )
                        }

                        // Vertical separator line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(BorderLight)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Rata-rata Harga Unit",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = formatRupiahShort(averageUnitPrice),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }

            // --- LEADERBOARD & INDIVIDUAL METRICS ---
            if (role == "Sales Manager" || role == "Admin" || role == "Super Admin") {
                Text(
                    text = if (role == "Sales Manager") "LEADERBOARD TIM MANAGER (RUDI)" else "LEADERBOARD SUPERVISOR TIM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 10.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (role == "Sales Manager") {
                            // Compare Siska and Ani
                            val teamLogs = rawSalesLogs.filter { it.year == selectedYear }
                            val siskaLogs = teamLogs.filter { it.soldBy == "siska" }
                            val aniLogs = teamLogs.filter { it.soldBy == "ani" }

                            LeaderRow(name = "Siska Lestari (Sales)", unitCount = siskaLogs.size, omzetVal = siskaLogs.sumOf { it.salePrice })
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)
                            LeaderRow(name = "Ani Wijaya (Sales)", unitCount = aniLogs.size, omzetVal = aniLogs.sumOf { it.salePrice })
                        } else {
                            // Admin / Super Admin compares Rudi's group vs Hendra's group
                            val teamLogs = rawSalesLogs.filter { it.year == selectedYear }
                            val rudiTeamLogs = teamLogs.filter { it.managerName == "rudi" }
                            val hendraTeamLogs = teamLogs.filter { it.managerName == "hendra" }

                            LeaderRow(name = "Kelompok Tim Rudi (SM)", unitCount = rudiTeamLogs.size, omzetVal = rudiTeamLogs.sumOf { it.salePrice })
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)
                            LeaderRow(name = "Kelompok Tim Hendra (SM)", unitCount = hendraTeamLogs.size, omzetVal = hendraTeamLogs.sumOf { it.salePrice })
                        }
                    }
                }
            }

            // Informational Notice Board
            Card(
                colors = CardDefaults.cardColors(containerColor = TersediaGreenBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TersediaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data grafik omzet tersinkronisasi otomatis dengan database SQLite lokal.",
                        fontSize = 11.sp,
                        color = TersediaGreen,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Period Selector launcher
            Button(
                onClick = { showYearSelector = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_select_period")
            ) {
                Text("PILIH TAHUN PRESTASI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Period Selector
    if (showYearSelector) {
        AlertDialog(
            onDismissRequest = { showYearSelector = false },
            title = { Text("Pilih Periode Tahun", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih tahun operasional untuk melihat volume dan riwayat omzet properti:", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    listOf(2026, 2025, 2024).forEach { year ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (year == selectedYear) NavyPrimary.copy(alpha = 0.1f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                            .fillMaxWidth()
                                .clickable {
                                    viewModel.setSelectedYear(year)
                                    showYearSelector = false
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Periode Tahun $year",
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (year == selectedYear) NavyPrimary else ContentPrimary
                                )
                                if (year == selectedYear) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Aktif",
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun LeaderRow(name: String, unitCount: Int, omzetVal: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NavyPrimary.copy(alpha = 0.08f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ContentPrimary)
                Text("Total Penjualan: $unitCount Unit", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(formatRupiahShort(omzetVal), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
            Text("Omzet Sah", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CanvasAndTrendBarChart(monthlyData: List<Double>) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxVal = monthlyData.maxOrNull() ?: 1.0
        val scaleMax = if (maxVal <= 0) 1.0 else maxVal * 1.15 // 15% padding on top of maximum for safety

        val width = size.width
        val height = size.height

        val barCount = monthlyData.size
        val spacing = width / (barCount * 1.5f + 0.5f) // proportionate spacing
        val barWidth = spacing * 0.9f

        // Draw horizontal subtle grid lanes
        val lanesCount = 4
        for (i in 0..lanesCount) {
            val laneY = height - (height / lanesCount) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, laneY),
                end = Offset(width, laneY),
                strokeWidth = 1.dp.toPx()
            )
        }

        val peaks = mutableListOf<Offset>()

        // 1. Draw Bar items and map peak coordinates for trend computation
        for (idx in monthlyData.indices) {
            val saleValue = monthlyData[idx]
            val barHeight = (saleValue / scaleMax) * height

            val leftX = spacing * 0.5f + idx * (barWidth + spacing)
            val topY = (height - barHeight).toFloat()

            // Draw beautiful vertical rounded bar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(NavyPrimary, NavyDark)
                ),
                topLeft = Offset(leftX, topY),
                size = Size(barWidth, barHeight.toFloat())
            )

            // Track points for drawing the peak golden trend connection curve
            val centerX = leftX + barWidth / 2f
            peaks.add(Offset(centerX, topY))
        }

        // 2. Draw continuous golden trend growth line connecting points
        if (peaks.isNotEmpty()) {
            val trendPath = Path()
            trendPath.moveTo(peaks[0].x, peaks[0].y)
            for (i in 1 until peaks.size) {
                // Connect with simple elegant segment lines (or curve bezier vectors)
                trendPath.lineTo(peaks[i].x, peaks[i].y)
            }

            drawPath(
                path = trendPath,
                color = GoldAccent,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Highlight nodes at peaks with small golden dots
            for (point in peaks) {
                drawCircle(
                    color = GoldAccent,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
