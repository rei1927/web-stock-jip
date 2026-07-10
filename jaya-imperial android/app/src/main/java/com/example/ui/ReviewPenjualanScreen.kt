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
import com.example.ui.SoldProposalFormDialog

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
        val uId = approvingUnit!!.id
        val proposalState = produceState<SoldProposal?>(initialValue = null, uId) {
            value = viewModel.getSoldProposalForUnit(uId)
        }
        val proposal = proposalState.value

        // Manager can still approve even if proposal is partially empty (only has photo)
        SoldProposalFormDialog(
            unit = approvingUnit!!,
            existingProposal = proposal ?: SoldProposal(unitId = uId),
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
            SoldProposalFormDialog(
                unit = unit,
                existingProposal = proposal,
                isReadOnly = true,
                onDismiss = { viewingProposalForUnitId = null },
                onSubmit = { _, _ -> }
            )
        } else if (unit != null) {
            AlertDialog(
                onDismissRequest = { viewingProposalForUnitId = null },
                title = { Text("Memuat Berkas...", color = NavyDark) },
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
                        Text("Batal", color = NavyPrimary)
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
