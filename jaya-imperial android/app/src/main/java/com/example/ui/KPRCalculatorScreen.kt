package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KPRCalculatorScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val propertyPrice by viewModel.propertyPrice.collectAsState()
    val dpPercent by viewModel.dpPercent.collectAsState()
    val interestRate by viewModel.interestRate.collectAsState()
    val termYears by viewModel.termYears.collectAsState()
    val monthlyInstallment by viewModel.monthlyInstallment.collectAsState()

    var priceText by remember { mutableStateOf(propertyPrice.toLong().toString()) }
    var interestText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.1f", interestRate)) }

    val computedDpValue = propertyPrice * (dpPercent / 100.0)
    val loanAmount = propertyPrice - computedDpValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Simulasi KPR",
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
            Text(
                "Kalkulator Simulasi KPR Mandiri",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 1. Property Price Input
                    Text(
                        "Harga Properti (Rp)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentPrimary
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.setPropertyPrice(parsed)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        prefix = { Text("Rp ", color = ContentLight) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Text(
                        text = "Setara: ${formatRupiahShort(propertyPrice)}",
                        fontSize = 11.sp,
                        color = ContentSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Property Price Slider helper
                    Text(
                        "Gunakan Slider untuk Menyesuaikan",
                        fontSize = 11.sp,
                        color = ContentLight
                    )
                    Slider(
                        value = propertyPrice.toFloat(),
                        onValueChange = {
                            val adjusted = (it / 10_000_000).toInt() * 10_000_000.0 // lock steps to 10M rupiah
                            viewModel.setPropertyPrice(adjusted)
                            priceText = adjusted.toLong().toString()
                        },
                        valueRange = 100_000_000f..10_000_000_000f,
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = NavyPrimary,
                            inactiveTrackColor = BorderLight
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = BorderLight, modifier = Modifier.padding(bottom = 12.dp))

                    // 2. Down Payment (DP) Percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Uang Muka (DP) (%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                        Text(
                            "${dpPercent.toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Slider(
                        value = dpPercent.toFloat(),
                        onValueChange = { viewModel.setDpPercent(it.toDouble()) },
                        valueRange = 5f..90f,
                        steps = 16, // custom steps for 5% splits
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = NavyPrimary,
                            inactiveTrackColor = BorderLight
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Jumlah DP: ${formatRupiahLong(computedDpValue)}",
                        fontSize = 12.sp,
                        color = TersediaGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = BorderLight, modifier = Modifier.padding(bottom = 12.dp))

                    // 3. Suku Bunga per Tahun
                    Text(
                        "Suku Bunga (% per tahun)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentPrimary
                    )
                    OutlinedTextField(
                        value = interestText,
                        onValueChange = {
                            interestText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed in 0.0..50.0) {
                                viewModel.setInterestRate(parsed)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        suffix = { Text("% / tahun", color = ContentLight) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Slider(
                        value = interestRate.toFloat(),
                        onValueChange = {
                            viewModel.setInterestRate(it.toDouble())
                            interestText = String.format(java.util.Locale.US, "%.1f", it)
                        },
                        valueRange = 1f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = NavyPrimary,
                            inactiveTrackColor = BorderLight
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(color = BorderLight, modifier = Modifier.padding(bottom = 12.dp))

                    // 4. Jangka Waktu (Tahun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Jangka Waktu (Tenor)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentPrimary
                        )
                        Text(
                            "${termYears.toInt()} Tahun",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Slider(
                        value = termYears.toFloat(),
                        onValueChange = { viewModel.setTermYears(it.toDouble()) },
                        valueRange = 1f..35f,
                        steps = 34,
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = NavyPrimary,
                            inactiveTrackColor = BorderLight
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Loan Principal Information Row
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDark.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pokok Pinjaman KPR Anda: ${formatRupiahLong(loanAmount)}",
                        fontSize = 12.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dynamic Installment Result Card matching mockup colors (Soft Cream / Warm Card)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)), // Creamy yellow Card container
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_kpr_card"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTIMASI ANGSURAN BULANAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        letterSpacing = 1.0.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatRupiahLong(monthlyInstallment),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = NavyPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "/ bulan",
                        fontSize = 12.sp,
                        color = ContentSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Angsuran flat berbasis suku bunga simulasi. Syarat & ketentuan bank pemberi KPR berlaku.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
