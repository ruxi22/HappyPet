package com.example.happypet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "Reminder!"

        // Debugging log to verify the receiver is triggered
        println("NotificationReceiver triggered with message: $message at ${System.currentTimeMillis()}")

        // Check notification permission (for Android 13+)
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(context, "HappyPetChannel")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
                .setContentTitle("HappyPet Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val manager = NotificationManagerCompat.from(context)
            manager.notify(System.currentTimeMillis().toInt(), notification)
        } else {
            println("NotificationReceiver: Permission not granted for notifications.")
        }
    }
}
