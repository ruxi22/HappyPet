package com.example.happypet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happypet.model.AppDatabase
import com.example.happypet.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val emailInput = findViewById<EditText>(R.id.email_input)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val createAccountButton = findViewById<Button>(R.id.create_account_button)

        createAccountButton.setOnClickListener {
            val email = emailInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isBlank() || username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@SignUpActivity)
                val newUser = User(email = email, username = username, password = password)
                withContext(Dispatchers.IO) {
                    db.userDao().insert(newUser)
                }
                Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                // Save email to SharedPreferences
                val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("userEmail", email)
                    apply()
                }

                // Redirect to MainActivity for animal selection
                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
