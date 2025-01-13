package com.example.happypet

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

    class BackgroundService : Service() {

        private val job = Job()
        private val scope = CoroutineScope(Dispatchers.IO + job)

        override fun onCreate() {
            super.onCreate()
            Log.d("BackgroundService", "Background service created")
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            Log.d("BackgroundService", "Background service started")

            // Perform background task
            scope.launch {
                performCleanupTask()
            }

            // Stop the service after the task is complete
            return START_NOT_STICKY
        }

        private suspend fun performCleanupTask() {
            Log.d("BackgroundService", "Performing cleanup task...")

            // Simulate a cleanup operation (e.g., deleting old mood data)
            delay(3000) // Simulate a 3-second task
            Log.d("BackgroundService", "Cleanup task completed")
        }

        override fun onDestroy() {
            super.onDestroy()
            job.cancel() // Cancel the coroutine when the service is destroyed
            Log.d("BackgroundService", "Background service destroyed")
        }

        override fun onBind(intent: Intent?): IBinder? = null
    }