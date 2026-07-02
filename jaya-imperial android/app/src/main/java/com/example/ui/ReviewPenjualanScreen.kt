package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPenjualanScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val allUnits by viewModel.allUnits.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var viewingProposalForUnitId by remember { mutableStateOf<Int?>(null) }
    var approvingUnit by remember { mutableStateOf<HousingUnit?>(null) }

    // Filter units that have status "Pending Sold"
    val pendingUnits = remember(allUnits) {
        allUnits.filter { it.status == "Pending Sold" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Review & Approve Penjualan",
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
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OTORISASI APPROVAL PENJUALAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            letterSpacing = 1.0.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Periksa foto form penjualan yang diunggah sales, lalu klik Approve untuk melengkapi data administrasi dan meresmikan penjualan.",
                        fontSize = 12.sp,
                        color = ContentSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Pending Listings
            if (pendingUnits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Semua Bersih!",
                            color = ContentPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tidak ada pengajuan penjualan (Pending Sold) dari sales yang perlu di-review atau di-approve saat ini.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
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
                    items(pendingUnits) { unit ->
                        PendingReviewCard(
                            unit = unit,
                            onApprove = { approvingUnit = unit },
                            onReject = { viewModel.releaseHoldOrRejectSold(unit) },
                            onViewProposal = { id -> viewingProposalForUnitId = id }
                        )
                    }
                }
            }
        }
    }

    if (approvingUnit != null) {
        SoldProposalFormDialogInternal(
            unit = approvingUnit!!,
            onDismiss = { approvingUnit = null },
            onSubmit = { finalProposal: SoldProposal, selectedGimmicks: List<String> ->
                viewModel.approveSoldUnit(approvingUnit!!, finalProposal, selectedGimmicks)
                approvingUnit = null
            }
        )
    }

    if (viewingProposalForUnitId != null) {
        val uId = viewingProposalForUnitId!!
        val unit = pendingUnits.find { it.id == uId }
        val proposalState = produceState<SoldProposal?>(initialValue = null, uId) {
            value = viewModel.getSoldProposalForUnit(uId)
        }
        val proposal = proposalState.value

        if (proposal != null && unit != null) {
            SoldProposalDetailDialog(
                proposal = proposal,
                unit = unit,
                onDismiss = { viewingProposalForUnitId = null }
            )
        } else if (unit != null) {
            AlertDialog(
                onDismissRequest = { viewingProposalForUnitId = null },
                title = { Text("Memuat Berkas...") },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewingProposalForUnitId = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun PendingReviewCard(
    unit: HousingUnit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onViewProposal: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pending_review_card_${unit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Project Location Block and Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MapsHomeWork,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${unit.clusterName} ${unit.block}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyDark
                    )
                }

                Box(
                    modifier = Modifier
                        .background(NavyPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PENDING REVIEW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spec line
            Text(
                text = "Tipe properti: ${unit.typeName} (${unit.buildingArea} m² LB / ${unit.landArea} m² LT)",
                fontSize = 12.sp,
                color = ContentSecondary
            )

            // Specifications Icons
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

            // Specs Notes if any
            if (unit.notes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(SoftBackground, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Catatan Unit: ${unit.notes}",
                        fontSize = 11.sp,
                        color = ContentSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            // Price Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Harga Penjualan", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = formatRupiahLong(unit.price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

            // MANDATORY HIGHLIGHT: Sales person who requested/submitted this sale
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(NavyPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Diajukan Oleh:",
                        fontSize = 10.sp,
                        color = ContentLight,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${unit.actionUserLabel ?: "Nama Sales"} (@${unit.actionByUser ?: "sales"})",
                        fontSize = 13.sp,
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onViewProposal(unit.id) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary.copy(alpha = 0.08f), contentColor = NavyPrimary),
                border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("btn_view_berkas_${unit.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lihat Form Administrasi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: REJECT vs APPROVE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerjualRed),
                    border = BorderStroke(1.dp, TerjualRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_reject_sales_${unit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tolak / Kembalikan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                        .testTag("btn_approve_sales_${unit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "APPROVE PENJUALAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoldProposalDetailDialog(
    proposal: SoldProposal,
    unit: HousingUnit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SoftBackground
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Foto Form Penjualan (Review)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Unit: ${unit.clusterName} - ${unit.block}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Berikut adalah foto dokumen yang dikirim oleh Sales:",
                        fontSize = 13.sp,
                        color = ContentSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display Photo Placeholder (since we don't have actual file system image loading implemented here yet)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = NavyPrimary.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Foto Form Penjualan", fontWeight = FontWeight.Bold, color = NavyDark)
                                Text(proposal.photoUri ?: "No URI", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("TUTUP PREVIEW")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SoldProposalFormDialogInternal(
    unit: HousingUnit,
    onDismiss: () -> Unit,
    onSubmit: (SoldProposal, List<String>) -> Unit
) {
    var namaLengkap by remember { mutableStateOf("") }
    var alamatKtp by remember { mutableStateOf("") }
    var alamatSurat by remember { mutableStateOf("") }
    var alamatSuratSda by remember { mutableStateOf(true) }
    var noTelpRumah by remember { mutableStateOf("") }
    var noTelpSeluler by remember { mutableStateOf("") }
    var noKtpConsumer by remember { mutableStateOf("") }
    var noNpwp by remember { mutableStateOf("") }
    var noKk by remember { mutableStateOf("") }

    var tujuanPembelian by remember { mutableStateOf("Tempat Tinggal") }
    var sumberDana by remember { mutableStateOf("Gaji / Upah") }
    var sistemPembayaran by remember { mutableStateOf("Tunai") }
    var kprPersen by remember { mutableStateOf("") } // 13. KPR _ x _ %

    var hargaJualString by remember { mutableStateOf(unit.price.toLong().toString()) }
    var plafondKprString by remember { mutableStateOf("") }
    var tandaJadiString by remember { mutableStateOf("") }
    var tandaJadiDate by remember { mutableStateOf("") }

    var uMukaString by remember { mutableStateOf("") }
    var uMukaBln by remember { mutableStateOf("") }
    var uMukaPertamaString by remember { mutableStateOf("") }
    var uMukaPertamaDate by remember { mutableStateOf("") }
    var angsuranPertamaText by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sumber by remember { mutableStateOf("Media Sosial") }

    // Gimmick state: Initial 4 empty strings
    val gimmicks = remember { mutableStateListOf("", "", "", "") }

    val isValid = namaLengkap.isNotBlank() &&
            noKtpConsumer.length >= 16 &&
            noTelpSeluler.isNotBlank()

    val finalAlamatSurat = if (alamatSuratSda) "SDA (Sesuai Data Alamat KTP)" else alamatSurat

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SoftBackground
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Lengkapi Form & Approve", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Unit: ${unit.clusterName} - ${unit.block}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Batal", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Silakan lengkapi 20 Kolom data administrasi sesuai Formulir Tanda Jadi sebelum menyetujui penjualan.",
                                fontSize = 11.sp,
                                color = NavyDark,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // --- SECTION 1: DATA KONSUMEN ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. DATA KONSUMEN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            Divider(color = BorderLight)

                            OutlinedTextField(value = namaLengkap, onValueChange = { namaLengkap = it }, label = { Text("1. Nama Lengkap (Sesuai KTP) *") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = alamatKtp, onValueChange = { alamatKtp = it }, label = { Text("2. Alamat KTP *") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = alamatSuratSda, onCheckedChange = { alamatSuratSda = it })
                                Text("3. Alamat Surat: SDA (Sesuai Data Alamat)", fontSize = 12.sp)
                            }
                            if (!alamatSuratSda) {
                                OutlinedTextField(value = alamatSurat, onValueChange = { alamatSurat = it }, label = { Text("Alamat Surat (Detail)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = noTelpRumah, onValueChange = { noTelpRumah = it }, label = { Text("4. Telp Rumah") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                                OutlinedTextField(value = noTelpSeluler, onValueChange = { noTelpSeluler = it }, label = { Text("5. Telp Seluler *") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                            }

                            OutlinedTextField(value = noKtpConsumer, onValueChange = { noKtpConsumer = it }, label = { Text("6. No. KTP Konsumen *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = noNpwp, onValueChange = { noNpwp = it }, label = { Text("7. No. NPWP") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = noKk, onValueChange = { noKk = it }, label = { Text("8. No. KK") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("20. E-mail") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                        }
                    }

                    // --- SECTION 2: DETAIL PROPERTI ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("2. DETAIL UNIT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            Divider(color = BorderLight)

                            DetailRowRecap("9. Type / Blok", "${unit.typeName} - ${unit.block}")
                            DetailRowRecap("10. Cluster", unit.clusterName)

                            Text("11. Tujuan Pembelian", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                listOf("Investasi", "Tempat Tinggal", "Lain-lain").forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = tujuanPembelian == item, onClick = { tujuanPembelian = item })
                                        Text(item, fontSize = 11.sp)
                                    }
                                }
                            }

                            Text("12. Sumber Dana", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Column {
                                listOf("Gaji / Upah", "Pemberian Orang Tua", "Warisan", "Hasil/Laba Usaha").forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = sumberDana == item, onClick = { sumberDana = item })
                                        Text(item, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // --- SECTION 3: SKEMA PEMBAYARAN ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("3. PEMBAYARAN & BIAYA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            Divider(color = BorderLight)

                            Text("13. Sistem Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                listOf("Tunai", "KPR").forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = sistemPembayaran == item, onClick = { sistemPembayaran = item })
                                        Text(item, fontSize = 12.sp)
                                    }
                                }
                            }
                            if (sistemPembayaran == "KPR") {
                                OutlinedTextField(value = kprPersen, onValueChange = { kprPersen = it }, label = { Text("KPR _ x _ %") }, modifier = Modifier.fillMaxWidth())
                            }

                            OutlinedTextField(value = hargaJualString, onValueChange = { hargaJualString = it }, label = { Text("14. Harga Jual Inc. PPN *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                            if (sistemPembayaran == "KPR") {
                                OutlinedTextField(value = plafondKprString, onValueChange = { plafondKprString = it }, label = { Text("15. Rencana Plafond KPR") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = tandaJadiString, onValueChange = { tandaJadiString = it }, label = { Text("16. Tanda Jadi (Rp)") }, modifier = Modifier.weight(1.2f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                OutlinedTextField(value = tandaJadiDate, onValueChange = { tandaJadiDate = it }, label = { Text("Tgl Tanda Jadi") }, modifier = Modifier.weight(1f))
                            }

                            if (sistemPembayaran == "KPR") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = uMukaString, onValueChange = { uMukaString = it }, label = { Text("17. U. Muka (Rp)") }, modifier = Modifier.weight(1.2f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    OutlinedTextField(value = uMukaBln, onValueChange = { uMukaBln = it }, label = { Text("Jml Bln") }, modifier = Modifier.weight(0.8f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = uMukaPertamaString, onValueChange = { uMukaPertamaString = it }, label = { Text("18. U. Muka I (Rp)") }, modifier = Modifier.weight(1.2f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    OutlinedTextField(value = uMukaPertamaDate, onValueChange = { uMukaPertamaDate = it }, label = { Text("Tgl Pembayaran") }, modifier = Modifier.weight(1f))
                                }
                            } else {
                                OutlinedTextField(value = angsuranPertamaText, onValueChange = { angsuranPertamaText = it }, label = { Text("19. Angsuran Pertama (Cash)") }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    // --- SECTION 4: GIMMICK / HADIAH ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("4. GIMMICK / HADIAH", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                            Divider(color = BorderLight)

                            gimmicks.forEachIndexed { index, gimmickText ->
                                OutlinedTextField(
                                    value = gimmickText,
                                    onValueChange = { gimmicks[index] = it },
                                    label = { Text("Gimmick ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Masukkan rincian gimmick...") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            TextButton(
                                onClick = { gimmicks.add("") },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("TAMBAH GIMMICK")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isValid) {
                                val proposal = SoldProposal(
                                    unitId = unit.id,
                                    namaLengkap = namaLengkap,
                                    alamatKtp = alamatKtp,
                                    alamatSurat = finalAlamatSurat,
                                    noTelpRumah = noTelpRumah,
                                    noTelpSeluler = noTelpSeluler,
                                    noKtpConsumer = noKtpConsumer,
                                    noNpwp = noNpwp,
                                    noKk = noKk,
                                    typeBlok = "${unit.typeName} - ${unit.block}",
                                    cluster = unit.clusterName,
                                    tujuanPembelian = tujuanPembelian,
                                    sumberDana = sumberDana,
                                    sistemPembayaran = sistemPembayaran,
                                    kprPersen = kprPersen,
                                    hargaJual = hargaJualString.toDoubleOrNull() ?: unit.price,
                                    plafondKpr = plafondKprString.toDoubleOrNull() ?: 0.0,
                                    tandaJadi = tandaJadiString.toDoubleOrNull() ?: 0.0,
                                    tandaJadiDate = tandaJadiDate,
                                    uMuka = uMukaString.toDoubleOrNull() ?: 0.0,
                                    uMukaBln = uMukaBln,
                                    uMukaPertama = uMukaPertamaString.toDoubleOrNull() ?: 0.0,
                                    uMukaPertamaDate = uMukaPertamaDate,
                                    angsuranPertamaText = angsuranPertamaText,
                                    email = email,
                                    sumber = sumber
                                )
                                onSubmit(proposal, gimmicks.filter { it.isNotBlank() })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = isValid
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APPROVE & KONFIRMASI SOLD", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRowRecap(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, color = ContentSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, color = ContentSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
    }
}
