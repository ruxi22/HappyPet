package com.example.happypet

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happypet.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class StatusPageActivity : AppCompatActivity() {

    companion object {
        val petImageMap = mapOf(
            "dog" to R.drawable.ic_dog,
            "cat" to R.drawable.ic_cat,
            "rabbit" to R.drawable.ic_rabbit,
            "hamster" to R.drawable.ic_hamster,
            "turtle" to R.drawable.ic_turtle,
            "fish" to R.drawable.ic_fish,
            "bird" to R.drawable.ic_bird,
            "reptile" to R.drawable.ic_reptile
        )
    }

    private var boundService: BoundPetService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as BoundPetService.LocalBinder
            boundService = localBinder.getService()
            isBound = true
            Log.d("StatusPageActivity", "Bound to service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
            isBound = false
            Log.d("StatusPageActivity", "Unbound from service")
        }
    }

    private val petHealthReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val petHealthStatus = intent.getStringExtra("PET_HEALTH_STATUS") ?: "Unknown"
            Toast.makeText(context, "Pet Health: $petHealthStatus", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_page)

        val fetchCatFactButton = findViewById<Button>(R.id.fetch_cat_fact_button)
        val catFactTextView = findViewById<TextView>(R.id.cat_fact_text)

        fetchCatFactButton.setOnClickListener {
            RetrofitInstance.api.getRandomCatFact().enqueue(object : Callback<CatFactResponse> {
                override fun onResponse(call: Call<CatFactResponse>, response: Response<CatFactResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val catFact = response.body()!!.fact
                        catFactTextView.text = catFact
                    } else {
                        catFactTextView.text = "Failed to fetch cat fact!"
                    }
                }

                override fun onFailure(call: Call<CatFactResponse>, t: Throwable) {
                    catFactTextView.text = "Error: ${t.message}"
                }
            })
        }

        findViewById<Button>(R.id.instagram_button).setOnClickListener {
            val cursor = contentResolver.query(
                Uri.parse("content://com.example.happypet.provider/instagram_link"),
                null,
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val instagramLink = it.getString(0)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramLink))
                    startActivity(intent)
                }
            }
        }

        // Bind to the BoundPetService
        Intent(this, BoundPetService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

        // Register the broadcast receiver
        val filter = IntentFilter("com.example.happypet.PET_HEALTH_UPDATE")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(petHealthReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(petHealthReceiver, filter)
        }
        val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", null)

        if (userEmail.isNullOrEmpty()) {
            // Redirect to LoginActivity if no email is found
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val db = AppDatabase.getDatabase(this)
            lifecycleScope.launch {
                try {
                    val user = withContext(Dispatchers.IO) {
                        db.userDao().getUserByEmail(userEmail)
                    }

                    if (user != null) {
                        val petId = user.petId

                        if (petId != null) {
                            val pet = withContext(Dispatchers.IO) {
                                db.petDao().getPetById(petId)
                            }

                            if (pet != null) {
                                // Display pet info
                                findViewById<TextView>(R.id.pet_name).text = "Pet Name: ${pet.name}"
                                findViewById<TextView>(R.id.pet_age).text = "Age: ${pet.age}"
                                findViewById<TextView>(R.id.pet_breed_species).text =
                                    "Breed/Species: ${pet.breed}"

                                // Set pet image
                                val petImageRes = petImageMap[pet.animalType.lowercase()] ?: R.drawable.ic_dog
                                findViewById<ImageView>(R.id.profile_picture).setImageResource(petImageRes)
                            } else {
                                // Redirect to PetProfileActivity if pet is not found
                                Toast.makeText(
                                    this@StatusPageActivity,
                                    "Pet info not found. Please add a pet.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@StatusPageActivity, PetProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // Redirect to PetProfileActivity if petId is null
                            Toast.makeText(
                                this@StatusPageActivity,
                                "No pet associated with this user.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@StatusPageActivity, PetProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@StatusPageActivity, "User not found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@StatusPageActivity,
                        "An error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val scheduleButton = findViewById<Button>(R.id.schedule_button)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }

        val checkPetHealthButton = findViewById<Button>(R.id.check_pet_health_button)
        checkPetHealthButton.setOnClickListener {
            if (isBound) {
                val healthStatus = boundService?.getPetHealthStatus() ?: "Unknown status"

                // Create an intent with the health status and set the package
                val intent = Intent("com.example.happypet.PET_HEALTH_UPDATE").apply {
                    putExtra("PET_HEALTH_STATUS", healthStatus)
                    setPackage(packageName) // Restrict the broadcast to this app
                }

                // Send the broadcast
                sendBroadcast(intent)
            } else {
                Toast.makeText(this, "Service not bound. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }


        // Log out button click listener
        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.remove("userEmail")
            editor.apply()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(petHealthReceiver)
    }

}
