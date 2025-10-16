package com.vulnerable.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// VULN M1: Improper Platform Usage - This activity is exported without proper authorization
class SecretActivity : AppCompatActivity() {
    private lateinit var secretDataText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret)

        secretDataText = findViewById(R.id.secretDataText)

        // VULN M1: This activity can be launched via deeplink without authentication
        // Anyone can access this by sending an intent with action "com.vulnerable.app.SECRET_ACTION"

        // VULN M2: Reading insecurely stored data
        val (username, password) = Utils.getCredentials(this)

        // Display sensitive information that should be protected
        val secretData = """
            CONFIDENTIAL INFORMATION
            ========================

            Stored Username: $username
            Stored Password: $password

            API Base URL: ${getApiUrl()}
            Database Connection: Direct SQL access enabled

            Admin Panel Access: Unrestricted
            Debug Mode: Enabled

            This activity should NOT be exported!
            Any app can launch this with an Intent.
        """.trimIndent()

        secretDataText.text = secretData

        // VULN M2: Logging all sensitive data
        Utils.logSensitiveData("SecretActivity", secretData)
    }

    private fun getApiUrl(): String {
        // VULN M2: Hardcoded API credentials in code
        return "http://10.0.2.2:80/api (admin:admin123)"
    }
}
