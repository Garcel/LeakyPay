package com.vulnerable.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Utils {
    // VULN M2: Insecure Data Storage - Storing sensitive data in SharedPreferences
    fun saveCredentials(context: Context, username: String, password: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", username)
            putString("password", password) // VULN: Storing password in plaintext
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    fun getCredentials(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        return Pair(username, password)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // VULN M2: Insecure Data Storage - Writing sensitive data to external storage
    fun saveToExternalStorage(context: Context, data: String) {
        try {
            val file = File(context.getExternalFilesDir(null), "sensitive_data.txt")
            file.writeText(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // VULN M5: Insufficient Cryptography - Using weak encryption (hardcoded key, DES)
    private const val SECRET_KEY = "12345678" // VULN: Hardcoded encryption key

    fun encryptData(data: String): String {
        return try {
            // VULN: Using DES algorithm which is weak and deprecated
            val key = SecretKeySpec(SECRET_KEY.toByteArray(), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            data // VULN: Returns plaintext on error
        }
    }

    fun decryptData(encryptedData: String): String {
        return try {
            val key = SecretKeySpec(SECRET_KEY.toByteArray(), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decrypted = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            String(decrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedData // Returns encrypted data on error
        }
    }

    // VULN M5: Insufficient Cryptography - Weak random number generation
    fun generateSessionToken(): String {
        // VULN: Using predictable random number generation
        val random = java.util.Random(System.currentTimeMillis())
        return random.nextInt(999999).toString().padStart(6, '0')
    }

    // VULN M2: Insecure Data Storage - Logging sensitive information
    fun logSensitiveData(tag: String, message: String) {
        android.util.Log.d(tag, message) // VULN: Logging sensitive data
    }
}
