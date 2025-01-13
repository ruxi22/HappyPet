package com.example.happypet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PetHealthReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Get the pet health status from the intent
        val petHealthStatus = intent.getStringExtra("PET_HEALTH_STATUS") ?: "Unknown status"

        // Display the status in a toast
        Toast.makeText(context, "Pet Health: $petHealthStatus", Toast.LENGTH_SHORT).show()
    }
}
