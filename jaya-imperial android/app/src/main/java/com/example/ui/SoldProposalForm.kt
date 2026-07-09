package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.HousingUnit
import com.example.data.SoldProposal
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SoldProposalFormDialog(
    unit: HousingUnit,
    existingProposal: SoldProposal? = null,
    isReadOnly: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (SoldProposal, List<String>) -> Unit
) {
    var photoUri by remember { mutableStateOf(existingProposal?.photoUri) }

    var namaLengkap by remember { mutableStateOf(existingProposal?.namaLengkap ?: "") }
    var alamatKtp by remember { mutableStateOf(existingProposal?.alamatKtp ?: "") }
    var alamatSurat by remember { mutableStateOf(existingProposal?.alamatSurat ?: "") }
    var alamatSuratSda by remember { mutableStateOf(existingProposal?.alamatSurat == "SDA (Sesuai Data Alamat KTP)" || existingProposal?.alamatSurat?.isBlank() == true) }
    var noTelpRumah by remember { mutableStateOf(existingProposal?.noTelpRumah ?: "") }
    var noTelpSeluler by remember { mutableStateOf(existingProposal?.noTelpSeluler ?: "") }
    var noKtpConsumer by remember { mutableStateOf(existingProposal?.noKtpConsumer ?: "") }
    var noNpwp by remember { mutableStateOf(existingProposal?.noNpwp ?: "") }
    var noKk by remember { mutableStateOf(existingProposal?.noKk ?: "") }

    var tujuanPembelian by remember { mutableStateOf(existingProposal?.tujuanPembelian ?: "Tempat Tinggal") }
    var sumberDana by remember { mutableStateOf(existingProposal?.sumberDana ?: "Gaji / Upah") }
    var sistemPembayaran by remember { mutableStateOf(existingProposal?.sistemPembayaran ?: "Tunai") }
    var kprPersen by remember { mutableStateOf(existingProposal?.kprPersen ?: "") }

    var hargaJualString by remember { mutableStateOf(if (existingProposal != null && existingProposal.hargaJual > 0) existingProposal.hargaJual.toLong().toString() else unit.price.toLong().toString()) }
    var plafondKprString by remember { mutableStateOf(if (existingProposal != null && existingProposal.plafondKpr > 0) existingProposal.plafondKpr.toLong().toString() else "") }
    var tandaJadiString by remember { mutableStateOf(if (existingProposal != null && existingProposal.tandaJadi > 0) existingProposal.tandaJadi.toLong().toString() else "") }
    var tandaJadiDate by remember { mutableStateOf(existingProposal?.tandaJadiDate ?: "") }

    var uMukaString by remember { mutableStateOf(if (existingProposal != null && existingProposal.uMuka > 0) existingProposal.uMuka.toLong().toString() else "") }
    var uMukaBln by remember { mutableStateOf(existingProposal?.uMukaBln ?: "") }
    var uMukaPertamaString by remember { mutableStateOf(if (existingProposal != null && existingProposal.uMukaPertama > 0) existingProposal.uMukaPertama.toLong().toString() else "") }
    var uMukaPertamaDate by remember { mutableStateOf(existingProposal?.uMukaPertamaDate ?: "") }
    var angsuranPertamaText by remember { mutableStateOf(existingProposal?.angsuranPertamaText ?: "") }
    var email by remember { mutableStateOf(existingProposal?.email ?: "") }
    var sumber by remember { mutableStateOf(existingProposal?.sumber ?: "Media Sosial") }

    val initialGimmicks = remember {
        val list = mutableStateListOf<String>()
        // Assuming we might have gimmicks in proposal or we just start with empty ones
        list.addAll(listOf("", "", "", ""))
        list
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) photoUri = uri.toString()
    }

    val isPhotoUploaded = photoUri != null
    val isFormValid = namaLengkap.isNotBlank() && isPhotoUploaded

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
                                Text(if (isReadOnly) "Detail Pengajuan Sold" else "Form Pengajuan Sold", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    // --- STEP 1: UPLOAD FOTO FORM ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isPhotoUploaded) TersediaGreen else NavyPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "LANGKAH 1: UNGGAH FOTO FORM TANDA JADI *",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isPhotoUploaded) TersediaGreen else NavyPrimary,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(if (isPhotoUploaded) 200.dp else 120.dp)
                                    .background(NavyPrimary.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isReadOnly) { launcher.launch("image/*") }
                                    .border(1.dp, if (isPhotoUploaded) TersediaGreen else NavyPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri == null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(32.dp))
                                        Text("Pilih Foto", fontSize = 10.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (!isReadOnly) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                .clickable { photoUri = null }
                                                .padding(4.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            if (isPhotoUploaded) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TersediaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Foto Form Terupload", fontSize = 11.sp, color = TersediaGreen, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Wajib mengunggah foto fisik form sebelum mengisi data.", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }

                    // --- STEP 2: ISI DATA (Hanya aktif jika foto sudah ada) ---
                    if (isPhotoUploaded || isReadOnly) {
                        Text(
                            "LANGKAH 2: LENGKAPI DATA ADMINISTRASI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NavyPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // --- SECTION 1: DATA KONSUMEN ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("1. DATA KONSUMEN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                Divider(color = BorderLight)

                                FormTextField("1. Nama Lengkap (Sesuai KTP) *", namaLengkap, isReadOnly) { namaLengkap = it }
                                FormTextField("2. Alamat KTP", alamatKtp, isReadOnly, minLines = 2) { alamatKtp = it }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = alamatSuratSda, onCheckedChange = { if (!isReadOnly) alamatSuratSda = it }, enabled = !isReadOnly)
                                    Text("3. Alamat Surat: SDA (Sesuai KTP)", fontSize = 12.sp, color = NavyDark)
                                }
                                if (!alamatSuratSda) {
                                    FormTextField("Alamat Surat (Detail)", alamatSurat, isReadOnly, minLines = 2) { alamatSurat = it }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FormTextField("4. Telp Rumah", noTelpRumah, isReadOnly, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Phone) { noTelpRumah = it }
                                    FormTextField("5. Telp Seluler *", noTelpSeluler, isReadOnly, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Phone) { noTelpSeluler = it }
                                }

                                FormTextField("6. No. KTP Konsumen *", noKtpConsumer, isReadOnly, keyboardType = KeyboardType.Number) { noKtpConsumer = it }
                                FormTextField("7. No. NPWP", noNpwp, isReadOnly) { noNpwp = it }
                                FormTextField("8. No. KK", noKk, isReadOnly) { noKk = it }
                                FormTextField("20. E-mail", email, isReadOnly, keyboardType = KeyboardType.Email) { email = it }
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

                                DetailRowLabel("9. Type / Blok", "${unit.typeName} - ${unit.block}")
                                DetailRowLabel("10. Cluster", unit.clusterName)

                                Text("11. Tujuan Pembelian", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    listOf("Investasi", "Tempat Tinggal", "Lain-lain").forEach { item ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = tujuanPembelian == item, onClick = { if(!isReadOnly) tujuanPembelian = item }, enabled = !isReadOnly)
                                            Text(item, fontSize = 11.sp, color = NavyDark)
                                        }
                                    }
                                }

                                Text("12. Sumber Dana", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
                                Column {
                                    listOf("Gaji / Upah", "Pemberian Orang Tua", "Warisan", "Hasil/Laba Usaha").forEach { item ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = sumberDana == item, onClick = { if(!isReadOnly) sumberDana = item }, enabled = !isReadOnly)
                                            Text(item, fontSize = 11.sp, color = NavyDark)
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

                                Text("13. Sistem Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    listOf("Tunai", "KPR").forEach { item ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = sistemPembayaran == item, onClick = { if(!isReadOnly) sistemPembayaran = item }, enabled = !isReadOnly)
                                            Text(item, fontSize = 12.sp, color = NavyDark)
                                        }
                                    }
                                }

                                if (sistemPembayaran == "KPR") {
                                    FormTextField("KPR _ x _ %", kprPersen, isReadOnly) { kprPersen = it }
                                }

                                FormTextField("14. Harga Jual Inc. PPN", hargaJualString, isReadOnly, keyboardType = KeyboardType.Number) { hargaJualString = it }

                                if (sistemPembayaran == "KPR") {
                                    FormTextField("15. Rencana Plafond KPR", plafondKprString, isReadOnly, keyboardType = KeyboardType.Number) { plafondKprString = it }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FormTextField("16. Tanda Jadi (Rp)", tandaJadiString, isReadOnly, modifier = Modifier.weight(1.2f), keyboardType = KeyboardType.Number) { tandaJadiString = it }
                                    FormTextField("Tgl Tanda Jadi", tandaJadiDate, isReadOnly, modifier = Modifier.weight(1f)) { tandaJadiDate = it }
                                }

                                if (sistemPembayaran == "KPR") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FormTextField("17. U. Muka (Rp)", uMukaString, isReadOnly, modifier = Modifier.weight(1.2f), keyboardType = KeyboardType.Number) { uMukaString = it }
                                        FormTextField("Jml Bln", uMukaBln, isReadOnly, modifier = Modifier.weight(0.8f), keyboardType = KeyboardType.Number) { uMukaBln = it }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FormTextField("18. U. Muka I (Rp)", uMukaPertamaString, isReadOnly, modifier = Modifier.weight(1.2f), keyboardType = KeyboardType.Number) { uMukaPertamaString = it }
                                        FormTextField("Tgl Pembayaran", uMukaPertamaDate, isReadOnly, modifier = Modifier.weight(1f)) { uMukaPertamaDate = it }
                                    }
                                } else {
                                    FormTextField("19. Angsuran Pertama (Cash)", angsuranPertamaText, isReadOnly) { angsuranPertamaText = it }
                                }

                                FormTextField("Sumber Info (Media Sosial/dll)", sumber, isReadOnly) { sumber = it }
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

                                initialGimmicks.forEachIndexed { index, gimmickText ->
                                    FormTextField("Gimmick ${index + 1}", gimmickText, isReadOnly) { initialGimmicks[index] = it }
                                }

                                if (!isReadOnly) {
                                    TextButton(
                                        onClick = { initialGimmicks.add("") },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("TAMBAH GIMMICK", color = NavyPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!isReadOnly) {
                            Button(
                                onClick = {
                                    if (isFormValid) {
                                        val proposal = SoldProposal(
                                            unitId = unit.id,
                                            photoUri = photoUri,
                                            namaLengkap = namaLengkap,
                                            alamatKtp = alamatKtp,
                                            alamatSurat = if (alamatSuratSda) "SDA (Sesuai Data Alamat KTP)" else alamatSurat,
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
                                        onSubmit(proposal, initialGimmicks.filter { it.isNotBlank() })
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TersediaGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = isFormValid
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AJUKAN PENJUALAN (SOLD)", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Info placeholder when photo is not yet uploaded
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Form Terkunci",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                                Text(
                                    "Unggah foto form tanda jadi di Langkah 1\nuntuk membuka data administrasi.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    isReadOnly: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = NavyPrimary) },
        modifier = modifier.fillMaxWidth(),
        readOnly = isReadOnly,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NavyDark,
            unfocusedTextColor = NavyDark,
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = NavyPrimary,
            unfocusedLabelColor = Color.Gray,
            disabledTextColor = NavyDark,
            disabledBorderColor = Color.LightGray
        )
    )
}

@Composable
fun DetailRowLabel(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, color = ContentSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
    }
}
