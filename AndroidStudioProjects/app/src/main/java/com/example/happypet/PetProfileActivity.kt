package com.example.happypet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happypet.model.Pet
import com.example.happypet.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        // Retrieve the animal type from the intent
        val animalType = intent.getStringExtra("animalType") ?: "Unknown"

        // Map of animal type to drawable resources
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

        // Display the animal type and image
        findViewById<TextView>(R.id.animal_type).text = getString(R.string.animal_type_label, animalType)
        findViewById<ImageView>(R.id.profile_picture).setImageResource(
            petImageMap[animalType.lowercase()] ?: R.drawable.ic_dog
        )

        val petNameField = findViewById<EditText>(R.id.pet_name)
        val petAgeField = findViewById<EditText>(R.id.pet_age)
        val petBreedSpeciesField = findViewById<EditText>(R.id.pet_breed_species)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Adjust breed/species hint based on animal type
        when (animalType.lowercase()) {
            "dog", "cat", "reptile" -> petBreedSpeciesField.hint = "Breed"
            "fish", "bird" -> petBreedSpeciesField.hint = "Species"
            else -> petBreedSpeciesField.hint = "Type"
        }

        // Save button click listener
        saveButton.setOnClickListener {
            val petName = petNameField.text.toString()
            val petAge = petAgeField.text.toString().toIntOrNull() ?: 0
            val petBreedSpecies = petBreedSpeciesField.text.toString()

            if (petName.isBlank() || petAge <= 0 || petBreedSpecies.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pet = Pet(
                name = petName,
                age = petAge,
                breed = petBreedSpecies,
                animalType = animalType,
                image = animalType.lowercase()
            )

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@PetProfileActivity)
                withContext(Dispatchers.IO) {
                    val petId = db.petDao().insertPet(pet)

                    // Update user's petId in the database
                    val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    val userEmail = sharedPref.getString("userEmail", null)
                    if (!userEmail.isNullOrEmpty()) {
                        val user = db.userDao().getUserByEmail(userEmail)
                        user?.let {
                            it.petId = petId.toInt()
                            db.userDao().update(it)
                        }
                    }
                }
                Toast.makeText(this@PetProfileActivity, "Pet saved successfully!", Toast.LENGTH_SHORT).show()

                // Redirect to StatusPageActivity after saving the pet
                val intent = Intent(this@PetProfileActivity, StatusPageActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
