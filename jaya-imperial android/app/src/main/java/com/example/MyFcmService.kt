package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.NotificationEntity
import com.example.ui.PropertyViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Log incoming message
        Log.d("FCM", "From: ${remoteMessage.from}")

        // 2. Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }

        // 3. Handle notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Jaya Imperial Park", it.body ?: "")
        }

        // If message contains data but no notification object
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Pembaruan Stok"
            val message = remoteMessage.data["message"] ?: ""
            sendNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // TODO: Send token to your Laravel server database
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Sistem",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
