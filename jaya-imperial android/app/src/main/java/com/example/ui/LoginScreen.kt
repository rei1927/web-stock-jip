package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.GoldAccent
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

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
            // Real estate company logo badge
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
                text = "Aplikasi Management & KPR",
                fontSize = 12.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
            )

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
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
                    errorMessage = null
                },
                label = { Text("Email Pengguna") },
                placeholder = { Text("e.g. admin@example.com") },
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
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Kata Sandi") },
                placeholder = { Text("Masukkan password Anda") },
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
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        errorMessage = "Email tidak boleh kosong!"
                    } else {
                        isLoggingIn = true
                        viewModel.login(username, password) { success ->
                            isLoggingIn = false
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Gagal login. Periksa email/password Anda."
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

            // Collapsible / Elegant profiles grid for instant testing
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AKSES PORTAL (TESTING)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        letterSpacing = 1.0.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val testProfiles = listOf(
                        Triple("siska@example.com", "Sales", Icons.Default.Engineering),
                        Triple("rudi@example.com", "Sales Manager", Icons.Default.SupervisedUserCircle),
                        Triple("admin@example.com", "Admin", Icons.Default.AdminPanelSettings),
                        Triple("reza@example.com", "Super Admin", Icons.Default.Security)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        testProfiles.forEach { (usr, roleLabel, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                                    .clickable {
                                        username = usr
                                        password = "password"
                                        errorMessage = null
                                        if (!isLoggingIn) {
                                            isLoggingIn = true
                                            viewModel.login(usr, "password") { success ->
                                                isLoggingIn = false
                                                if (success) {
                                                    onLoginSuccess()
                                                } else {
                                                    errorMessage = "Gagal login. Periksa email/password Anda."
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = usr.lowercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "Role: $roleLabel • Pass: password",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
