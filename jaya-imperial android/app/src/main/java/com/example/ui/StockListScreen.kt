package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HousingUnit
import com.example.data.SoldProposal
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val filteredUnits by viewModel.filteredUnits.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.statusFilter.collectAsState()
    val activeClusterFilter by viewModel.clusterFilter.collectAsState()
    val availableClusters by viewModel.availableClusters.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUnitDetail by remember { mutableStateOf<HousingUnit?>(null) }
    var unitForSoldForm by remember { mutableStateOf<HousingUnit?>(null) }
    var unitToEdit by remember { mutableStateOf<HousingUnit?>(null) }

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    val role = currentUser?.role ?: "Sales"
    val isAdmin = role == "Admin" || role == "Super Admin"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Stok Properti & Transaksi",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        if (isSyncing) {
                            Text("Mensinkronisasi data...", fontSize = 10.sp, color = GoldAccent)
                        } else if (syncError != null) {
                            Text("Gagal Sync: Hubungkan ke Wifi Kantor", fontSize = 10.sp, color = Color.Red)
                        }
                    }
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
                actions = {
                    IconButton(onClick = { viewModel.syncData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync dari Web",
                            tint = if (isSyncing) GoldAccent else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("fab_add_unit")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Unit")
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari Klaster, Tipe, Blok...", color = ContentLight) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NavyDark,
                    unfocusedTextColor = NavyDark,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_bar")
            )

            // Status Filtering chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Status:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentSecondary,
                    modifier = Modifier.padding(end = 4.dp)
                )

                FilterChip(
                    selected = activeFilter == FilterStatus.ALL,
                    onClick = { viewModel.setStatusFilter(FilterStatus.ALL) },
                    label = { Text("Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = NavyPrimary
                    )
                )

                FilterChip(
                    selected = activeFilter == FilterStatus.TERSEDIA,
                    onClick = { viewModel.setStatusFilter(FilterStatus.TERSEDIA) },
                    label = { Text("Tersedia") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TersediaGreen, shape = RoundedCornerShape(4.dp))
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TersediaGreenBg,
                        selectedLabelColor = TersediaGreen
                    )
                )

                FilterChip(
                    selected = activeFilter == FilterStatus.HOLD,
                    onClick = { viewModel.setStatusFilter(FilterStatus.HOLD) },
                    label = { Text("Hold") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(GoldAccent, shape = RoundedCornerShape(4.dp))
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent.copy(alpha = 0.15f),
                        selectedLabelColor = GoldAccent
                    )
                )

                FilterChip(
                    selected = activeFilter == FilterStatus.TERJUAL,
                    onClick = { viewModel.setStatusFilter(FilterStatus.TERJUAL) },
                    label = { Text("Terjual") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TerjualRed, shape = RoundedCornerShape(4.dp))
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerjualRedBg,
                        selectedLabelColor = TerjualRed
                    )
                )
            }

            // Cluster filter row
            if (availableClusters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Klaster:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentSecondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    FilterChip(
                        selected = activeClusterFilter == null,
                        onClick = { viewModel.setClusterFilter(null) },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = NavyPrimary
                        )
                    )

                    availableClusters.forEach { clusterName ->
                        FilterChip(
                            selected = activeClusterFilter == clusterName,
                            onClick = { viewModel.setClusterFilter(clusterName) },
                            label = { Text(clusterName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(alpha = 0.15f),
                                selectedLabelColor = GoldAccent
                            )
                        )
                    }
                }
            }

            // Property Listings List
            if (filteredUnits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Unit tidak ditemukan",
                            color = ContentSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Coba ubah kata kunci pencarian atau filter status Anda.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredUnits) { unit ->
                        HousingUnitCard(
                            unit = unit,
                            onLihatDetail = { selectedUnitDetail = unit }
                        )
                    }
                }
            }
        }
    }

    // Modal dialog: Unit Specifications Detail & Interactive workflows
    if (selectedUnitDetail != null) {
        val unit = selectedUnitDetail!!
        val isOwner = unit.actionByUser == currentUser?.username
        val isManager = role.contains("Manager", ignoreCase = true)
        val isManagerOrAdmin = isManager || role.contains("Admin", ignoreCase = true)
        val userCanControlHold = isOwner || isManagerOrAdmin

        // Setup status variables for the modal UI
        val (bgStatusColor, textStatusColor, labelStatus) = when (unit.status) {
            "Hold" -> Triple(GoldAccent.copy(alpha = 0.15f), GoldAccent, "HOLD")
            "Pending Sold" -> Triple(NavyPrimary.copy(alpha = 0.15f), NavyPrimary, "PENDING SOLD")
            "Terjual" -> Triple(TerjualRedBg, TerjualRed, "TERJUAL (SOLD)")
            else -> Triple(TersediaGreenBg, TersediaGreen, "TERSEDIA")
        }

        AlertDialog(
            onDismissRequest = { selectedUnitDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${unit.clusterName} - ${unit.block}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    Box(
                        modifier = Modifier
                            .background(bgStatusColor, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = labelStatus,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textStatusColor
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Price Layout
                    Text(
                        text = "Harga Unit Properti",
                        fontSize = 11.sp,
                        color = ContentSecondary
                    )
                    Text(
                        text = formatRupiahLong(unit.price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )

                    // Owner Label
                    if (unit.actionUserLabel != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Oleh: ${unit.actionUserLabel} (@${unit.actionByUser})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                    // Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecBlock(label = "Tipe Bangunan", value = unit.typeName)
                        SpecBlock(label = "Kamar Tidur", value = "${unit.bedrooms} KT")
                        SpecBlock(label = "Kamar Mandi", value = "${unit.bathrooms} KM")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecBlock(label = "Luas Bangunan", value = "${unit.buildingArea} m²")
                        SpecBlock(label = "Luas Tanah", value = "${unit.landArea} m²")
                        SpecBlock(label = "Bahan Bangunan", value = "Standard M3")
                    }

                    if (unit.notes.isNotBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)
                        Text(
                            text = "Catatan Spesifikasi",
                            fontSize = 11.sp,
                            color = ContentSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = unit.notes,
                            fontSize = 12.sp,
                            color = ContentPrimary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // --- Dynamic Workflow Controls depending on 4 roles ---
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                    Text(
                        text = "Workflow Kontrol Unit",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    when (unit.status) {
                        "Tersedia" -> {
                            // Any Sales / Manager can hold or request sold
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.holdUnit(unit)
                                        selectedUnitDetail = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hold", fontSize = 11.sp, color = NavyDark, fontWeight = FontWeight.Bold)
                                }

                                if (isManagerOrAdmin) {
                                    // Sales Manager or Admin can mark as SOLD directly without proposal form
                                    Button(
                                        onClick = {
                                            viewModel.markAsSoldDirectly(unit)
                                            selectedUnitDetail = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Set Terjual", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Standard Sales uploads a photo
                                    Button(
                                        onClick = {
                                            unitForSoldForm = unit
                                            selectedUnitDetail = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ajukan Sold", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        "Hold" -> {
                            if (userCanControlHold) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.releaseHoldOrRejectSold(unit)
                                            selectedUnitDetail = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Lepas Hold", fontSize = 11.sp, color = Color.White)
                                    }

                                    if (isManagerOrAdmin) {
                                        Button(
                                            onClick = {
                                                viewModel.markAsSoldDirectly(unit)
                                                selectedUnitDetail = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Set Terjual", fontSize = 11.sp, color = Color.White)
                                        }
                                    } else if (isOwner) {
                                        // Owner Sales can Edit or Submit Photo
                                        Button(
                                            onClick = {
                                                unitToEdit = unit
                                                selectedUnitDetail = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Spek", fontSize = 11.sp, color = NavyDark)
                                        }

                                        Button(
                                            onClick = {
                                                unitForSoldForm = unit
                                                selectedUnitDetail = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Ajukan Sold", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                // Locked state notification
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unik sedang di-Hold oleh ${unit.actionUserLabel ?: "sales lain"}. Hubungi mereka atau manajer untuk melakukan pembebasan.",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        "Pending Sold" -> {
                            if (isManagerOrAdmin) {
                                // Manager approval action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.releaseHoldOrRejectSold(unit)
                                            selectedUnitDetail = null
                                        },
                                        modifier = Modifier.weight(1.0f),
                                        colors = ButtonDefaults.buttonColors(containerColor = TerjualRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tolak/Kembalikan", fontSize = 11.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.approveSoldUnit(unit)
                                            selectedUnitDetail = null
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("APPROVE PENJUALAN", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // Pulsing loading label for Sales waiting approval
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val animatedAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "pulseAlpha"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NavyPrimary.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = NavyPrimary.copy(alpha = animatedAlpha),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Menunggu verifikasi approval penjualan dari Sales Manager (Rudi/Hendra)...",
                                        fontSize = 10.sp,
                                        color = NavyPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        "Terjual" -> {
                            if (isAdmin) {
                                // Admin reset option
                                Button(
                                    onClick = {
                                        viewModel.releaseHoldOrRejectSold(unit)
                                        selectedUnitDetail = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Buka Terjual (Atur Tersedia Kembali)", fontSize = 11.sp, color = Color.White)
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TerjualRedBg, shape = RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = TerjualRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SOLD OUT • Unit ini telah lunas terjual dan tidak dapat dimodifikasi lagi.",
                                        fontSize = 11.sp,
                                        color = TerjualRedText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Admin full deletions
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteUnit(unit)
                                selectedUnitDetail = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Unit Dari Database", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedUnitDetail = null }) {
                    Text("Tutup", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal dialog: Add New Asset unit for Admin
    if (showAddDialog) {
        var clusterName by remember { mutableStateOf("Emerald") }
        var block by remember { mutableStateOf("") }
        var typeName by remember { mutableStateOf("Tipe 70/120") }
        var priceInput by remember { mutableStateOf("") }
        var lbInput by remember { mutableStateOf("70") }
        var ltInput by remember { mutableStateOf("120") }
        var ktInput by remember { mutableStateOf("3") }
        var kmInput by remember { mutableStateOf("2") }
        var notesInput by remember { mutableStateOf("") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Unit Baru", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 18.sp) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        if (dialogError != null) {
                            Text(dialogError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        // Cluster select
                        Text("Pilih Cluster", fontSize = 12.sp, color = ContentSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Emerald", "Imperial Garden", "Royal Residence", "Jasmine Spring").forEach { cl ->
                                FilterChip(
                                    selected = clusterName == cl,
                                    onClick = { clusterName = cl },
                                    label = { Text(cl, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = block,
                            onValueChange = { block = it },
                            label = { Text("Nomor Blok") },
                            placeholder = { Text("e.g. Blok B-14") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = typeName,
                            onValueChange = { typeName = it },
                            label = { Text("Tipe Bangunan / Tanah") },
                            placeholder = { Text("e.g. Tipe 70/120") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Harga Properti (Rp)") },
                            placeholder = { Text("e.g. 1500000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = lbInput,
                                onValueChange = { lbInput = it },
                                label = { Text("Luas Bangunan (m²)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ltInput,
                                onValueChange = { ltInput = it },
                                label = { Text("Luas Tanah (m²)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ktInput,
                                onValueChange = { ktInput = it },
                                label = { Text("Kamar Tidur") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = kmInput,
                                onValueChange = { kmInput = it },
                                label = { Text("Kamar Mandi") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Komentar / Catatan Khusus") },
                            placeholder = { Text("e.g. Bonus AC atau Kanopi, Hook, etc.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = priceInput.toDoubleOrNull()
                        val parsedLb = lbInput.toIntOrNull()
                        val parsedLt = ltInput.toIntOrNull()
                        val parsedKt = ktInput.toIntOrNull()
                        val parsedKm = kmInput.toIntOrNull()

                        if (block.isBlank() || parsedPrice == null || parsedPrice <= 0 || parsedLb == null || parsedLt == null) {
                            dialogError = "Silakan lengkapi blok, tipe, dan harga numerik valid!"
                        } else {
                            viewModel.addNewUnit(
                                clusterName = clusterName,
                                block = block,
                                typeName = typeName,
                                price = parsedPrice,
                                buildingArea = parsedLb,
                                landArea = parsedLt,
                                bedrooms = parsedKt ?: 2,
                                bathrooms = parsedKm ?: 1,
                                notes = notesInput
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    if (unitForSoldForm != null) {
        if (role == "Sales") {
            SoldPhotoSubmissionDialog(
                unit = unitForSoldForm!!,
                onDismiss = { unitForSoldForm = null },
                onSubmit = { photoUri ->
                    viewModel.submitSoldPhoto(unitForSoldForm!!, photoUri)
                    unitForSoldForm = null
                }
            )
        } else {
            // Managers/Admins can set sold directly from the main button,
            // so we just clear this state if they reach here.
            unitForSoldForm = null
        }
    }

    if (unitToEdit != null) {
        EditUnitDialog(
            unit = unitToEdit!!,
            onDismiss = { unitToEdit = null },
            onConfirm = { updatedUnit ->
                viewModel.updateUnit(updatedUnit)
                unitToEdit = null
            }
        )
    }
}

@Composable
fun EditUnitDialog(
    unit: HousingUnit,
    onDismiss: () -> Unit,
    onConfirm: (HousingUnit) -> Unit
) {
    var typeName by remember { mutableStateOf(unit.typeName) }
    var priceInput by remember { mutableStateOf(unit.price.toLong().toString()) }
    var lbInput by remember { mutableStateOf(unit.buildingArea.toString()) }
    var ltInput by remember { mutableStateOf(unit.landArea.toString()) }
    var ktInput by remember { mutableStateOf(unit.bedrooms.toString()) }
    var kmInput by remember { mutableStateOf(unit.bathrooms.toString()) }
    var notesInput by remember { mutableStateOf(unit.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Detail Spek Unit", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Unit: ${unit.clusterName} - ${unit.block}", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    OutlinedTextField(value = typeName, onValueChange = { typeName = it }, label = { Text("Tipe Unit") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) priceInput = it },
                        label = { Text("Harga Properti (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = lbInput, onValueChange = { lbInput = it }, label = { Text("LB (m²)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = ltInput, onValueChange = { ltInput = it }, label = { Text("LT (m²)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = ktInput, onValueChange = { ktInput = it }, label = { Text("KT") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = kmInput, onValueChange = { kmInput = it }, label = { Text("KM") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    OutlinedTextField(value = notesInput, onValueChange = { notesInput = it }, label = { Text("Catatan / Bonus") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = unit.copy(
                        typeName = typeName,
                        price = priceInput.toDoubleOrNull() ?: unit.price,
                        buildingArea = lbInput.toIntOrNull() ?: unit.buildingArea,
                        landArea = ltInput.toIntOrNull() ?: unit.landArea,
                        bedrooms = ktInput.toIntOrNull() ?: unit.bedrooms,
                        bathrooms = kmInput.toIntOrNull() ?: unit.bathrooms,
                        notes = notesInput
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun SoldPhotoSubmissionDialog(
    unit: HousingUnit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var photoUploaded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unggah Form Penjualan", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Silakan unggah foto fisik Formulir Penjualan untuk unit ${unit.clusterName} - ${unit.block}.",
                    fontSize = 12.sp,
                    color = ContentSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(NavyPrimary.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp))
                        .clickable { photoUploaded = true } // Simulate upload
                        .border(1.dp, NavyPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUploaded) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
                            Text("Ambil Foto", fontSize = 10.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TersediaGreen, modifier = Modifier.size(48.dp))
                    }
                }

                if (photoUploaded) {
                    Text("Foto berhasil dipilih.", fontSize = 11.sp, color = TersediaGreen, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit("content://simulated/path/to/photo.jpg") },
                enabled = photoUploaded,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Kirim Pengajuan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun HousingUnitCard(
    unit: HousingUnit,
    onLihatDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Label Cluster name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MapsHomeWork,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${unit.clusterName} ${unit.block}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )
                }

                // Inline status badge
                val (bgPillColor, textPillColor, labelStr) = when (unit.status) {
                    "Hold" -> Triple(GoldAccent.copy(alpha = 0.15f), GoldAccent, "Hold")
                    "Pending Sold" -> Triple(NavyPrimary.copy(alpha = 0.15f), NavyPrimary, "Pending Sold")
                    "Terjual" -> Triple(TerjualRedBg, TerjualRed, "Terjual")
                    else -> Triple(TersediaGreenBg, TersediaGreen, "Tersedia")
                }

                Box(
                    modifier = Modifier
                        .background(bgPillColor, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = labelStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPillColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle type
            Text(
                text = "Tipe: ${unit.typeName} (Bangunan ${unit.buildingArea}m² / Tanah ${unit.landArea}m²)",
                fontSize = 12.sp,
                color = ContentSecondary
            )

            // Specifications Icons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bed, contentDescription = null, tint = ContentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${unit.bedrooms} KT", fontSize = 11.sp, color = ContentSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bathtub, contentDescription = null, tint = ContentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${unit.bathrooms} KM", fontSize = 11.sp, color = ContentSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ZoomOutMap, contentDescription = null, tint = ContentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${unit.landArea} m² Tanah", fontSize = 11.sp, color = ContentSecondary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Aproksimasi Harga", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = formatRupiahShort(unit.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                Button(
                    onClick = onLihatDetail,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("LIHAT DETAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SpecBlock(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = ContentLight)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ContentPrimary)
    }
}

// Helpers for formatted Rupiah values
fun formatRupiahLong(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").substringBefore(",")
}

fun formatRupiahShort(amount: Double): String {
    return when {
        amount >= 1_000_000_000.0 -> {
            val bn = amount / 1_000_000_000.0
            String.format(Locale.getDefault(), "Rp %.1f Miliar", bn).replace(",0", "")
        }
        amount >= 1_000_000.0 -> {
            val mn = amount / 1_000_000.0
            String.format(Locale.getDefault(), "Rp %.1f Juta", mn).replace(",0", "")
        }
        else -> {
            formatRupiahLong(amount)
        }
    }
}
