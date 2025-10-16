package com.vulnerable.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if already logged in
        if (Utils.isLoggedIn(this)) {
            navigateToDashboard()
            return
        }

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // VULN M4: Insecure Authentication - No input validation
            performLogin(username, password)
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // VULN M4: No password strength requirements
            performRegister(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        // VULN M3: Insecure Communication over HTTP
        ApiClient.login(username, password) { success, message ->
            runOnUiThread {
                if (success) {
                    // VULN M2: Insecure Data Storage - Saving plaintext credentials
                    Utils.saveCredentials(this, username, password)

                    // VULN M2: Logging sensitive information
                    Utils.logSensitiveData("MainActivity", "Login successful for user: $username with password: $password")

                    // VULN M5: Weak encryption of session token
                    val token = Utils.generateSessionToken()
                    val encryptedToken = Utils.encryptData(token)
                    Utils.saveToExternalStorage(this, "Session token: $encryptedToken")

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performRegister(username: String, password: String) {
        // VULN M3: Insecure Communication
        ApiClient.register(username, password) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
