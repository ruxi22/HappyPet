package com.example.happypet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.Service
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PetReminderService : Service() {

    private val CHANNEL_ID = "PetReminderChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()

        // Create notification channel for API 26 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pet Reminder Notifications"
            val descriptionText = "Channel for pet reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pet Reminder")
            .setContentText("Don't forget to update your pet's profile!")
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure the correct icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Handle the service's ongoing task
        return START_STICKY // Restart the service if it's killed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optionally, you can stop the service here
    }
}
