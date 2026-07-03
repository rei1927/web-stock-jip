package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AttendanceEntity
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsState()
    val myAttendance by viewModel.myAttendance.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val role = currentUser?.role ?: ""
    val isAdmin = role.contains("Admin")
    val attendanceList = if (isAdmin) allAttendance else myAttendance

    // Admin Filter States
    var selectedManager by remember { mutableStateOf<String?>(null) }
    var selectedSalesperson by remember { mutableStateOf<String?>(null) }

    // Calendar States
    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    // Selected Day Detail State
    var selectedDayDetail by remember { mutableStateOf<List<AttendanceEntity>?>(null) }

    val filteredAttendanceList = remember(attendanceList, selectedManager, selectedSalesperson) {
        attendanceList.filter { log ->
            val user = allUsers.find { it.username == log.username }
            val managerMatch = selectedManager == null || user?.managerName == selectedManager || user?.username == selectedManager
            val salesMatch = selectedSalesperson == null || log.username == selectedSalesperson
            managerMatch && salesMatch
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showCamera by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var locationInfo by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var addressText by remember { mutableStateOf("Mencari lokasi...") }
    var isProcessing by remember { mutableStateOf(false) }

    // Logic for restricting attendance type
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val hasClockedInToday = remember(myAttendance) {
        myAttendance.any { it.type == "Masuk" && it.timestamp >= todayStart }
    }
    val hasClockedOutToday = remember(myAttendance) {
        myAttendance.any { it.type == "Keluar" && it.timestamp >= todayStart }
    }

    var attendanceType by remember(hasClockedInToday) {
        mutableStateOf(if (hasClockedInToday) "Keluar" else "Masuk")
    }

    // Clock state
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAdmin) "Dashboard Monitoring Absensi" else "Absensi Karyawan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (!isAdmin && !permissionsState.allPermissionsGranted) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Izin Diperlukan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Aplikasi memerlukan izin Kamera dan Lokasi untuk melakukan absensi.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Berikan Izin")
                }
            }
        } else if (!isAdmin && showCamera) {
            CameraCaptureView(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    showCamera = false
                    fetchLocation(context) { lat, lon, addr ->
                        locationInfo = Pair(lat, lon)
                        addressText = addr
                    }
                },
                onClose = { showCamera = false }
            )
        } else if (isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyDark)
                    .padding(innerPadding)
            ) {
                AdminAttendanceFilters(
                    allUsers = allUsers,
                    selectedManager = selectedManager,
                    onManagerChange = {
                        selectedManager = it
                        selectedSalesperson = null
                    },
                    selectedSalesperson = selectedSalesperson,
                    onSalespersonChange = { selectedSalesperson = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MonitoringCalendar(
                    calendar = currentMonthCalendar,
                    attendanceLogs = filteredAttendanceList,
                    onMonthChange = { currentMonthCalendar = it },
                    onDayClick = { dayAttendance ->
                        selectedDayDetail = dayAttendance
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftBackground)
                    .padding(innerPadding)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentDate, fontSize = 12.sp, color = ContentSecondary, fontWeight = FontWeight.Medium)
                        Text(currentTime, fontSize = 32.sp, fontWeight = FontWeight.Black, color = NavyPrimary)

                        Spacer(modifier = Modifier.height(20.dp))

                        if (hasClockedOutToday) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TersediaGreen
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Absensi Hari Ini Selesai", fontWeight = FontWeight.Black, fontSize = 18.sp, color = NavyDark)
                                Text("Terima kasih, Anda telah melakukan absen masuk dan keluar.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        } else if (capturedImageUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (attendanceType == "Masuk") TersediaGreenBg else TerjualRedBg, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (attendanceType == "Masuk") "SILAKAN LAKUKAN ABSEN MASUK" else "SILAKAN LAKUKAN ABSEN KELUAR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attendanceType == "Masuk") TersediaGreen else TerjualRed
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showCamera = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (attendanceType == "Masuk") TersediaGreen else TerjualRed
                                )
                            ) {
                                Icon(if (attendanceType == "Masuk") Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AMBIL SELFIE & ABSEN ${attendanceType.uppercase()}", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                capturedImageUri?.let { uri ->
                                    val bitmap = BitmapFactory.decodeFile(uri.path)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Captured",
                                            modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, if (attendanceType == "Masuk") TersediaGreen else TerjualRed, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text("Lokasi Terdeteksi:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(addressText, fontSize = 12.sp, color = NavyDark, maxLines = 2)
                                    Text(
                                        "Tipe: $attendanceType",
                                        fontSize = 11.sp,
                                        color = if (attendanceType == "Masuk") TersediaGreen else TerjualRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { capturedImageUri = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ulangi")
                                }
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        viewModel.submitAttendance(
                                            locationInfo?.first ?: 0.0,
                                            locationInfo?.second ?: 0.0,
                                            capturedImageUri.toString(),
                                            addressText,
                                            attendanceType
                                        )
                                        capturedImageUri = null
                                        isProcessing = false
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    enabled = !isProcessing && locationInfo != null
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                    } else {
                                        Text("KIRIM ABSENSI", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "RIWAYAT ABSENSI SAYA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(start = 20.dp, bottom = 12.dp, top = 8.dp)
                )

                if (attendanceList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada data absensi.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(attendanceList) { item ->
                            AttendanceLogCard(item, false)
                        }
                    }
                }
            }
        }
    }

    if (selectedDayDetail != null) {
        AttendanceDetailDialog(
            attendanceLogs = selectedDayDetail!!,
            onDismiss = { selectedDayDetail = null }
        )
    }
}

@Composable
fun AdminAttendanceFilters(
    allUsers: List<com.example.data.User>,
    selectedManager: String?,
    onManagerChange: (String?) -> Unit,
    selectedSalesperson: String?,
    onSalespersonChange: (String?) -> Unit
) {
    val managers = allUsers.filter { it.role == "Sales Manager" }
    val salespersons = if (selectedManager != null) {
        allUsers.filter { it.role == "Sales" && it.managerName == selectedManager }
    } else {
        allUsers.filter { it.role == "Sales" }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdown(
                label = "Manager",
                options = managers.map { it.name to it.username },
                selectedOption = selectedManager,
                onOptionSelected = onManagerChange,
                modifier = Modifier.weight(1f)
            )
            FilterDropdown(
                label = "Sales",
                options = salespersons.map { it.name to it.username },
                selectedOption = selectedSalesperson,
                onOptionSelected = onSalespersonChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.second == selectedOption }?.first ?: "Semua"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = NavyPrimary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NavyDark,
                unfocusedTextColor = NavyDark,
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = NavyPrimary,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Semua $label") },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                }
            )
            options.forEach { (name, username) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onOptionSelected(username)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MonitoringCalendar(
    calendar: Calendar,
    attendanceLogs: List<AttendanceEntity>,
    onMonthChange: (Calendar) -> Unit,
    onDayClick: (List<AttendanceEntity>) -> Unit
) {
    val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calendar.time)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val days = mutableListOf<Calendar?>()
    repeat(firstDayOfWeek - 1) { days.add(null) }
    for (i in 1..daysInMonth) {
        val dayCal = calendar.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, i)
        days.add(dayCal)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                onMonthChange(newCal)
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = Color.White)
            }
            Text(monthName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            IconButton(onClick = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                onMonthChange(newCal)
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val weekDays = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(400.dp),
            userScrollEnabled = false
        ) {
            items(days) { dayCalendar ->
                if (dayCalendar != null) {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCalendar.time)
                    val dayAttendance = attendanceLogs.filter {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp)) == dateStr
                    }

                    CalendarDayItem(
                        day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        attendance = dayAttendance,
                        onClick = { if (dayAttendance.isNotEmpty()) onDayClick(dayAttendance) }
                    )
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: Int,
    attendance: List<AttendanceEntity>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(enabled = attendance.isNotEmpty(), onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (attendance.isNotEmpty()) {
            val firstLog = attendance.first()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(firstLog.photoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.dp, GoldAccent, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$day", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        } else {
            Text(text = "$day", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@Composable
fun AttendanceDetailDialog(
    attendanceLogs: List<AttendanceEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detail Absensi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NavyDark)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))

                val logMasuk = attendanceLogs.find { it.type == "Masuk" }
                val logKeluar = attendanceLogs.find { it.type == "Keluar" }

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (logMasuk != null) {
                        AttendanceDetailItem(title = "ABSEN MASUK", log = logMasuk)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (logKeluar != null) {
                        AttendanceDetailItem(title = "ABSEN KELUAR", log = logKeluar)
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailItem(title: String, log: AttendanceEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftBackground, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (title.contains("MASUK")) TersediaGreen else TerjualRed
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = log.photoUri,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyDark
                )
                Text(log.address, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(10.dp), tint = NavyPrimary)
                    Text("${log.latitude}, ${log.longitude}", fontSize = 10.sp, color = NavyPrimary)
                }
            }
        }
    }
}

@Composable
fun AttendanceLogCard(log: AttendanceEntity, showName: Boolean) {
    val isMasuk = log.type == "Masuk"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(
                    if (isMasuk) TersediaGreen.copy(alpha = 0.1f) else TerjualRed.copy(alpha = 0.1f),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMasuk) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = if (isMasuk) TersediaGreen else TerjualRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (showName) {
                    Text(log.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                }
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ContentPrimary
                )
                Text(log.address, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isMasuk) "MASUK" else "KELUAR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isMasuk) TersediaGreen else TerjualRed
                )
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(context: Context, onResult: (Double, Double, String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Alamat tidak ditemukan"
                    onResult(location.latitude, location.longitude, address)
                } catch (e: Exception) {
                    onResult(location.latitude, location.longitude, "Koordinat: ${location.latitude}, ${location.longitude}")
                }
            } else {
                onResult(0.0, 0.0, "Gagal mendapatkan lokasi")
            }
        }
}

@Composable
fun CameraCaptureView(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val cameraSelector = remember { CameraSelector.DEFAULT_FRONT_CAMERA }

    var capturedUri by remember { mutableStateOf<Uri?>(null) }

    if (capturedUri == null) {
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraX", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Posisikan wajah Anda di tengah layar", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

                Button(
                    onClick = {
                        val file = File(context.cacheDir, "attendance_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    capturedUri = Uri.fromFile(file)
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraX", "Capture failed", exception)
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(4.dp, Color.LightGray)
                ) {
                }
            }
        }
    } else {
        // --- PHOTO PREVIEW & CONFIRMATION ---
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val bitmap = remember(capturedUri) { BitmapFactory.decodeFile(capturedUri!!.path) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gunakan foto ini untuk absensi?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                OutlinedButton(
                    onClick = { capturedUri = null },
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("ULANGI", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onImageCaptured(capturedUri!!) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("GUNAKAN FOTO", fontWeight = FontWeight.Bold, color = Color.White)
                }
                }
            }
        }
    }
}
