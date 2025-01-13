package com.example.happypet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.happypet.model.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signUpButton = findViewById<Button>(R.id.sign_up_button)
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(this@LoginActivity)
                    val user = withContext(Dispatchers.IO) {
                        db.userDao().getUserByCredentials(email, password)
                    }

                    if (user != null) {
                        // Save email in SharedPreferences
                        val sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("userEmail", email)
                            apply()
                        }

                        /// Redirect directly to StatusPageActivity
                        val intent = Intent(this@LoginActivity, StatusPageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error during login: ${e.localizedMessage}", e)
                    Toast.makeText(this@LoginActivity, "An error occurred during login", Toast.LENGTH_SHORT).show()
                }
            }



        }


        signUpButton.setOnClickListener {
            Log.d("LoginActivity", "Sign up button clicked.")
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
