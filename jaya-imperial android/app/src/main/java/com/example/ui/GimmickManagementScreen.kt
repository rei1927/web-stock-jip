package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GimmickRequest
import com.example.data.HousingUnit
import com.example.data.User
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GimmickManagementScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allRequests by viewModel.allGimmickRequests.collectAsState()
    val myRequests by viewModel.myGimmickRequests.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val role = currentUser?.role ?: "Sales"
    val isManager = role == "Sales Manager"
    val isAdmin = role == "Admin" || role == "Super Admin"

    val displayRequests = if (isAdmin) allRequests else myRequests

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengajuan Gimmick Hadiah", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        floatingActionButton = {
            if (isManager) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Gimmick")
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
            // Header Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isManager) "DAFTAR PENGAJUAN GIMMICK SAYA" else "REVIEW PENGAJUAN GIMMICK TIM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isManager) "Kelola hadiah tambahan untuk konsumen yang berhasil membeli unit melalui tim sales Anda."
                               else "Periksa dan berikan persetujuan untuk pengajuan gimmick hadiah tambahan dari para Sales Manager.",
                        fontSize = 12.sp,
                        color = ContentSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            if (displayRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada pengajuan gimmick.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayRequests) { request ->
                        GimmickRequestCard(
                            request = request,
                            isAdmin = isAdmin,
                            onApprove = { viewModel.approveGimmickRequest(request) },
                            onReject = { viewModel.rejectGimmickRequest(request) },
                            onDelete = { viewModel.deleteGimmickRequest(request) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGimmickDialog(
            soldUnits = allUnits.filter { it.status == "Terjual" },
            salesList = allUsers.filter { it.role == "Sales" },
            onDismiss = { showAddDialog = false },
            onSubmit = { unit, salesperson, details ->
                viewModel.submitGimmickRequest(unit, salesperson, details)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GimmickRequestCard(
    request: GimmickRequest,
    isAdmin: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (request.status) {
        "Approved" -> TersediaGreen
        "Rejected" -> TerjualRed
        else -> GoldAccent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Unit: ${request.block}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                    Text(text = request.clusterName, fontSize = 12.sp, color = Color.Gray)
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Sales: ${request.salespersonName}", fontSize = 12.sp, color = ContentPrimary)
            }

            if (isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Oleh: ${request.requestedByName}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftBackground, shape = RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "GIMMICK TAMBAHAN:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = request.gimmickDetails, fontSize = 13.sp, color = NavyDark, lineHeight = 18.sp)
                }
            }

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            Text(
                text = "Diajukan pada: ${sdf.format(Date(request.timestamp))}",
                fontSize = 10.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 12.dp)
            )

            if (isAdmin && request.status == "Pending") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerjualRed),
                        border = BorderStroke(1.dp, TerjualRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("TOLAK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SETUJUI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (request.status != "Pending" || !isAdmin) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGimmickDialog(
    soldUnits: List<HousingUnit>,
    salesList: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (HousingUnit, User, String) -> Unit
) {
    var unitSearchText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf<HousingUnit?>(null) }

    var salesSearchText by remember { mutableStateOf("") }
    var selectedSalesperson by remember { mutableStateOf<User?>(null) }

    var gimmickList by remember { mutableStateOf(listOf("", "", "", "")) }

    var unitExpanded by remember { mutableStateOf(false) }
    var salesExpanded by remember { mutableStateOf(false) }

    val filteredUnits = remember(unitSearchText, soldUnits) {
        soldUnits.filter {
            it.clusterName.contains(unitSearchText, ignoreCase = true) ||
            it.block.contains(unitSearchText, ignoreCase = true)
        }
    }

    val filteredSales = remember(salesSearchText, salesList) {
        salesList.filter { it.name.contains(salesSearchText, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengajuan Gimmick Hadiah", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Select Unit (Searchable)
                Column {
                    Text("Pilih Blok Terjual (Ketik Cluster/Blok) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = unitSearchText,
                            onValueChange = {
                                unitSearchText = it
                                unitExpanded = true
                                if (selectedUnit?.let { u -> "${u.clusterName} - ${u.block}" } != it) {
                                    selectedUnit = null
                                }
                            },
                            placeholder = { Text("Cari Cluster atau Blok...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NavyDark,
                                unfocusedTextColor = NavyDark,
                                focusedBorderColor = if (selectedUnit != null) TersediaGreen else NavyPrimary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = NavyPrimary,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            if (filteredUnits.isEmpty()) {
                                DropdownMenuItem(text = { Text("Tidak ditemukan") }, onClick = { })
                            }
                            filteredUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(unit.block, fontWeight = FontWeight.Bold)
                                            Text(unit.clusterName, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedUnit = unit
                                        unitSearchText = "${unit.clusterName} - ${unit.block}"
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Select Sales (Searchable)
                Column {
                    Text("Pilih Sales Pelaksana (Ketik Nama) *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    ExposedDropdownMenuBox(
                        expanded = salesExpanded,
                        onExpandedChange = { salesExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = salesSearchText,
                            onValueChange = {
                                salesSearchText = it
                                salesExpanded = true
                                if (selectedSalesperson?.name != it) {
                                    selectedSalesperson = null
                                }
                            },
                            placeholder = { Text("Cari Nama Sales...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = salesExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NavyDark,
                                unfocusedTextColor = NavyDark,
                                focusedBorderColor = if (selectedSalesperson != null) TersediaGreen else NavyPrimary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = NavyPrimary,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = salesExpanded,
                            onDismissRequest = { salesExpanded = false }
                        ) {
                            if (filteredSales.isEmpty()) {
                                DropdownMenuItem(text = { Text("Tidak ditemukan") }, onClick = { })
                            }
                            filteredSales.forEach { sales ->
                                DropdownMenuItem(
                                    text = { Text(sales.name) },
                                    onClick = {
                                        selectedSalesperson = sales
                                        salesSearchText = sales.name
                                        salesExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Gimmick Details
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Rincian Gimmick Hadiah *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)

                    gimmickList.forEachIndexed { index, gimmick ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gimmick,
                                onValueChange = { newValue ->
                                    val newList = gimmickList.toMutableList()
                                    newList[index] = newValue
                                    gimmickList = newList
                                },
                                placeholder = { Text("Gimmick #${index + 1} (e.g. AC 1/2 PK)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            if (gimmickList.size > 1) {
                                IconButton(
                                    onClick = {
                                        val newList = gimmickList.toMutableList()
                                        newList.removeAt(index)
                                        gimmickList = newList
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Hapus", tint = TerjualRed)
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { gimmickList = gimmickList + "" },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Gimmick Lain", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            val finalGimmickString = gimmickList.filter { it.isNotBlank() }.joinToString("\n• ", prefix = "• ")
            Button(
                onClick = {
                    if (selectedUnit != null && selectedSalesperson != null && finalGimmickString.length > 2) {
                        onSubmit(selectedUnit!!, selectedSalesperson!!, finalGimmickString)
                    }
                },
                enabled = selectedUnit != null && selectedSalesperson != null && gimmickList.any { it.isNotBlank() },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Kirim Pengajuan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

