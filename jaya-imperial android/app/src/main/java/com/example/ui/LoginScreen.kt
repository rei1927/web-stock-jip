package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun LoginScreen(
    viewModel: PropertyViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Ambil pesan error langsung dari ViewModel
    val serverErrorMessage by viewModel.syncError.collectAsState()

    // Gunakan error lokal jika ada, jika tidak gunakan error dari server
    var localErrorMessage by remember { mutableStateOf<String?>(null) }
    val displayErrorMessage = localErrorMessage ?: serverErrorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyPrimary, Color(0xFF1E6C93))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .background(Color.White.copy(alpha = 0.96f), shape = RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_jip),
                contentDescription = "Jaya Imperial Park Logo",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "JAYA IMPERIAL PARK",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                letterSpacing = 1.0.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "SALES MANAGEMENT SYSTEM",
                fontSize = 12.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
            )

            if (displayErrorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = displayErrorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    localErrorMessage = null
                },
                label = { Text("Email Pengguna", color = NavyPrimary) },
                placeholder = { Text("e.g. admin@example.com", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NavyDark,
                    unfocusedTextColor = NavyDark,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = NavyPrimary,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    localErrorMessage = null
                },
                label = { Text("Kata Sandi", color = NavyPrimary) },
                placeholder = { Text("Masukkan password Anda", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NavyDark,
                    unfocusedTextColor = NavyDark,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = NavyPrimary,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        localErrorMessage = "Email tidak boleh kosong!"
                    } else {
                        isLoggingIn = true
                        viewModel.login(username, password) { success ->
                            isLoggingIn = false
                            if (success) {
                                onLoginSuccess()
                            }
                        }
                    }
                },
                enabled = !isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "L O G I N",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
