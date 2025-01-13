package com.example.happypet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happypet.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_selection)

        startBackgroundService()

        // Check if the user already has a pet; if yes, redirect to StatusPageActivity
        lifecycleScope.launch {
            val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val userEmail = sharedPref.getString("userEmail", null)


            if (userEmail != null) {
                val db = AppDatabase.getDatabase(this@MainActivity)
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserByEmail(userEmail)
                }

                if (user?.petId != null) {
                    val intent = Intent(this@MainActivity, StatusPageActivity::class.java)
                    startActivity(intent)
                    finish() // Close MainActivity
                    return@launch
                }
            }
        }

        // Start the foreground service
        val serviceIntent = Intent(this, PetReminderService::class.java)
        startService(serviceIntent)

        // Set click listeners for animal icons
        setAnimalClickListener(R.id.icon_dog, "dog")
        setAnimalClickListener(R.id.icon_cat, "cat")
        setAnimalClickListener(R.id.icon_bird, "bird")
        setAnimalClickListener(R.id.icon_fish, "fish")
        setAnimalClickListener(R.id.icon_rabbit, "rabbit")
        setAnimalClickListener(R.id.icon_hamster, "hamster")
        setAnimalClickListener(R.id.icon_reptile, "reptile")
        setAnimalClickListener(R.id.icon_turtle, "turtle")

        // Listener for the "Add Custom Pet" button
        findViewById<Button>(R.id.add_custom_pet_button).setOnClickListener {
            val intent = Intent(this, PetProfileActivity::class.java)
            intent.putExtra("animalType", "Custom")
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the background service when the activity is destroyed
        stopBackgroundService()
    }

    private fun setAnimalClickListener(iconId: Int, animalType: String) {
        findViewById<ImageView>(iconId).setOnClickListener {
            val intent = Intent(this, PetProfileActivity::class.java)
            intent.putExtra("animalType", animalType)
            startActivity(intent)
        }
    }

    private fun startBackgroundService() {
        val intent = Intent(this, BackgroundService::class.java)
        startService(intent)
    }
    private fun stopBackgroundService() {
        val intent = Intent(this, BackgroundService::class.java)
        stopService(intent)
        }
}
