package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.HousingUnit
import com.example.ui.theme.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRecapScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()

    var selectedUnitDetail by remember { mutableStateOf<HousingUnit?>(null) }
    var unitForSoldForm by remember { mutableStateOf<HousingUnit?>(null) }

    // Filter unit yang berinteraksi dengan sales saat ini (Hold/Pending/Sold)
    val mySales = remember(allUnits, currentUser) {
        val username = currentUser?.username ?: ""
        allUnits.filter { it.actionByUser == username }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Rekap Penjualan Saya", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                },
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
        Column(
            modifier = Modifier.fillMaxSize().background(SoftBackground).padding(innerPadding)
        ) {
            // Ringkasan Statistik
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItemRecap("HOLD", mySales.count { it.status == "Hold" || it.status == "Pending Sold" }, GoldAccent)
                    StatItemRecap("PENDING", mySales.count { it.status == "Pending Sold" }, NavyPrimary)
                    StatItemRecap("SOLD", mySales.count { it.status == "Terjual" }, TersediaGreen)
                }
            }

            if (mySales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada data transaksi.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mySales) { unit ->
                        SalesRecapItemCard(
                            unit = unit,
                            onClick = { selectedUnitDetail = unit }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedUnitDetail != null) {
        UnitDetailDialogRecap(
            unit = selectedUnitDetail!!,
            onDismiss = { selectedUnitDetail = null },
            onAjukanSold = {
                unitForSoldForm = selectedUnitDetail
                selectedUnitDetail = null
            }
        )
    }

    // Sold Form Dialog
    if (unitForSoldForm != null) {
        SoldPhotoSubmissionDialogRecap(
            unit = unitForSoldForm!!,
            onDismiss = { unitForSoldForm = null },
            onSubmit = { photoUri ->
                viewModel.submitSoldPhoto(unitForSoldForm!!, photoUri)
                unitForSoldForm = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailDialogRecap(
    unit: HousingUnit,
    onDismiss: () -> Unit,
    onAjukanSold: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SoftBackground
        ) {
            Column {
                // Custom Top Bar for Dialog
                Surface(
                    color = NavyDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                        }
                        Text(
                            "Detail Properti",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = unit.clusterName, fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                                    Text(text = unit.block, fontSize = 22.sp, fontWeight = FontWeight.Black, color = NavyDark)
                                }
                                Surface(
                                    color = when(unit.status) {
                                        "Hold" -> GoldAccent.copy(alpha = 0.1f)
                                        "Pending Sold" -> NavyPrimary.copy(alpha = 0.1f)
                                        "Terjual" -> TerjualRed.copy(alpha = 0.1f)
                                        else -> TersediaGreen.copy(alpha = 0.1f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (unit.status == "Pending Sold") "HOLD (PENGAJUAN)" else unit.status.uppercase(),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when(unit.status) {
                                            "Hold" -> GoldAccent
                                            "Pending Sold" -> NavyPrimary
                                            "Terjual" -> TerjualRed
                                            else -> TersediaGreen
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Harga Unit", fontSize = 11.sp, color = Color.Gray)
                            Text(text = formatRupiahRecap(unit.price), fontSize = 24.sp, fontWeight = FontWeight.Black, color = GoldAccent)

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = BorderLight)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Tipe", fontSize = 11.sp, color = Color.Gray)
                                    Text(unit.typeName, fontWeight = FontWeight.Bold, color = NavyDark)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Luas LB/LT", fontSize = 11.sp, color = Color.Gray)
                                    Text("${unit.buildingArea}/${unit.landArea} m²", fontWeight = FontWeight.Bold, color = NavyDark)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Kamar", fontSize = 11.sp, color = Color.Gray)
                                    Text("${unit.bedrooms} KT / ${unit.bathrooms} KM", fontWeight = FontWeight.Bold, color = NavyDark)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (unit.status == "Hold") {
                        Button(
                            onClick = onAjukanSold,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ajukan Sold (Upload Form UTJ)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoldPhotoSubmissionDialogRecap(
    unit: HousingUnit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unggah Form UTJ", fontWeight = FontWeight.Bold, color = NavyDark) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Silakan pilih foto Formulir UTJ dari galeri untuk unit ${unit.clusterName} - ${unit.block}.",
                    fontSize = 12.sp,
                    color = ContentSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(NavyPrimary.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp))
                        .clickable { launcher.launch("image/*") }
                        .border(1.dp, NavyPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
                            Text("Buka Galeri", fontSize = 10.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                if (selectedImageUri != null) {
                    Text("Foto dipilih", fontSize = 11.sp, color = TersediaGreen, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedImageUri.toString()) },
                enabled = selectedImageUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Kirim Pengajuan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = NavyPrimary) }
        }
    )
}

@Composable
fun StatItemRecap(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun SalesRecapItemCard(
    unit: HousingUnit,
    onClick: () -> Unit
) {
    val statusColor = when (unit.status) {
        "Hold" -> GoldAccent
        "Pending Sold" -> NavyPrimary
        "Terjual" -> TersediaGreen
        else -> Color.Gray
    }

    val statusLabel = when (unit.status) {
        "Hold" -> "HOLD"
        "Pending Sold" -> "HOLD (PENGAJUAN)"
        "Terjual" -> "SOLD"
        else -> unit.status.uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when(unit.status) {
                        "Hold" -> Icons.Default.PauseCircle
                        "Pending Sold" -> Icons.Default.PendingActions
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${unit.clusterName} - ${unit.block}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                Text(text = unit.typeName, fontSize = 12.sp, color = Color.Gray)
                Text(text = formatRupiahRecap(unit.price), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
            }
            Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                Text(text = statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = statusColor)
            }
        }
    }
}

fun formatRupiahRecap(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").substringBefore(",")
}
