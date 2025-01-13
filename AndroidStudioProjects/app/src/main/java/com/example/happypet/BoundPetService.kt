package com.example.happypet

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BoundPetService : Service() {

    // Binder given to clients
    private val binder = LocalBinder()

    fun getPetHealthStatus(): String {
        return "Healthy and Happy!"
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("BoundPetService", "Service bound")
        return binder
    }

    // Inner class for clients to access
    inner class LocalBinder : Binder() {
        fun getService(): BoundPetService = this@BoundPetService
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("BoundPetService", "Service unbound")
        return super.onUnbind(intent)
    }
}
