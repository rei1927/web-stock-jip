package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.allNotifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi Sistem", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllNotifications() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Bersihkan", tint = Color.White)
                        }
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
            if (notifications.isNotEmpty()) {
                Button(
                    onClick = { viewModel.markAllNotificationsAsRead() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tandai Semua Sudah Dibaca", fontWeight = FontWeight.Bold)
                }
            }

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada notifikasi.",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notif ->
                        NotificationItem(notif)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: NotificationEntity) {
    val isBroadcast = notif.title.contains("BROADCAST")
    val isHold = notif.title.contains("HOLD")
    val isSold = notif.title.contains("SOLD")

    val icon = when {
        isBroadcast -> Icons.Default.Campaign
        isHold -> Icons.Default.PauseCircle
        isSold -> Icons.Default.CheckCircle
        else -> Icons.Default.Notifications
    }

    val color = when {
        isBroadcast -> GoldAccent
        isHold -> GoldAccent
        isSold -> TersediaGreen
        else -> NavyPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (notif.isRead) BorderLight else color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (notif.isRead) NavyDark else color
                    )
                    Text(
                        text = formatTimeAgo(notif.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notif.message,
                    fontSize = 12.sp,
                    color = ContentPrimary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
