package com.vulnerable.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class DashboardActivity : AppCompatActivity() {
    private lateinit var welcomeText: TextView
    private lateinit var balanceText: TextView
    private lateinit var accountNumberInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var transferButton: Button
    private lateinit var logoutButton: Button

    private var currentUsername: String = ""
    private var currentBalance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        welcomeText = findViewById(R.id.welcomeText)
        balanceText = findViewById(R.id.balanceText)
        accountNumberInput = findViewById(R.id.accountNumberInput)
        amountInput = findViewById(R.id.amountInput)
        transferButton = findViewById(R.id.transferButton)
        logoutButton = findViewById(R.id.logoutButton)

        // VULN M2: Reading insecurely stored credentials
        val (username, password) = Utils.getCredentials(this)
        currentUsername = username

        if (username.isEmpty()) {
            navigateToLogin()
            return
        }

        welcomeText.text = "Welcome, $username"

        // VULN M2: Logging sensitive data
        Utils.logSensitiveData("DashboardActivity", "User $username logged in with password: $password")

        loadBalance()

        transferButton.setOnClickListener {
            val toAccount = accountNumberInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()

            if (toAccount.isEmpty() || amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // VULN M4: No transaction verification, no 2FA
            performTransfer(toAccount, amount)
        }

        logoutButton.setOnClickListener {
            Utils.logout(this)
            navigateToLogin()
        }
    }

    private fun loadBalance() {
        // VULN M3: Insecure Communication
        ApiClient.getBalance(currentUsername) { success, balance ->
            runOnUiThread {
                if (success && balance != null) {
                    currentBalance = balance
                    balanceText.text = "Balance: $${"%.2f".format(balance)}"
                } else {
                    Toast.makeText(this, "Failed to load balance", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performTransfer(toAccount: String, amount: Double) {
        if (amount > currentBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show()
            return
        }

        // VULN M4: Client-side validation only, can be bypassed
        // VULN M3: Insecure Communication
        ApiClient.transfer(currentUsername, toAccount, amount) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Transfer successful!", Toast.LENGTH_SHORT).show()
                    accountNumberInput.text?.clear()
                    amountInput.text?.clear()
                    loadBalance()
                } else {
                    Toast.makeText(this, message ?: "Transfer failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // VULN M1: Improper Platform Usage - Not properly handling onBackPressed
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // VULN: Should require re-authentication, but just exits instead
        super.onBackPressed()
        finishAffinity()
    }
}
