package com.example.happypet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCustomPetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_pet)

        val petTypeInput = findViewById<EditText>(R.id.custom_pet_type)
        val petNameInput = findViewById<EditText>(R.id.custom_pet_name)
        val saveButton = findViewById<Button>(R.id.save_custom_pet_button)

        saveButton.setOnClickListener {
            val petType = petTypeInput.text.toString()
            val petName = petNameInput.text.toString()

            if (petType.isBlank() || petName.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the custom pet (you can use SharedPreferences or a database)
            val sharedPreferences = getSharedPreferences("CustomPets", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("custom_pet_type", petType)
            editor.putString("custom_pet_name", petName)
            editor.apply()

            Toast.makeText(this, "Custom pet saved!", Toast.LENGTH_SHORT).show()

            // Navigate back to the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
