package com.vulnerable.app

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object ApiClient {
    // VULN M3: Insecure Communication - Using HTTP instead of HTTPS
    // Change this to your local server IP address
    private const val BASE_URL = "http://10.0.2.2" // Android emulator localhost
    private const val PORT = "80"

    private val gson = Gson()
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    // VULN M3: Insecure Communication - Disabling SSL certificate validation
    private val client: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // VULN: Accepting all hostnames
            .build()
    }

    // VULN M4: Insecure Authentication - Weak authentication mechanism
    fun login(username: String, password: String, callback: (Boolean, String?) -> Unit) {
        // VULN M3: Sending credentials over HTTP
        val url = "$BASE_URL:$PORT/api/login.php"

        // VULN M4: Credentials in URL parameters (should be in POST body)
        val requestUrl = "$url?username=$username&password=$password"

        Utils.logSensitiveData("ApiClient", "Login request: $requestUrl") // VULN M2: Logging credentials

        val request = Request.Builder()
            .url(requestUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                try {
                    val jsonResponse = gson.fromJson(body, Map::class.java)
                    if (jsonResponse["success"] == true) {
                        callback(true, jsonResponse["message"] as String?)
                    } else {
                        callback(false, jsonResponse["message"] as String?)
                    }
                } catch (e: Exception) {
                    callback(false, "Parse error: ${e.message}")
                }
            }
        })
    }

    fun register(username: String, password: String, callback: (Boolean, String?) -> Unit) {
        val url = "$BASE_URL:$PORT/api/register.php"

        // VULN M3: Insecure Communication - Sending data over HTTP
        val json = """{"username":"$username","password":"$password"}"""
        val body = RequestBody.create(JSON, json)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                try {
                    val jsonResponse = gson.fromJson(responseBody, Map::class.java)
                    callback(
                        jsonResponse["success"] == true,
                        jsonResponse["message"] as String?
                    )
                } catch (e: Exception) {
                    callback(false, "Parse error: ${e.message}")
                }
            }
        })
    }

    fun getBalance(username: String, callback: (Boolean, Double?) -> Unit) {
        val url = "$BASE_URL:$PORT/api/balance.php?username=$username"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                try {
                    val jsonResponse = gson.fromJson(body, Map::class.java)
                    if (jsonResponse["success"] == true) {
                        val balance = (jsonResponse["balance"] as? Double) ?: 0.0
                        callback(true, balance)
                    } else {
                        callback(false, null)
                    }
                } catch (e: Exception) {
                    callback(false, null)
                }
            }
        })
    }

    fun transfer(from: String, to: String, amount: Double, callback: (Boolean, String?) -> Unit) {
        // VULN M4: No session token validation, relying on client-side username
        val url = "$BASE_URL:$PORT/api/transfer.php"
        val json = """{"from":"$from","to":"$to","amount":$amount}"""
        val body = RequestBody.create(JSON, json)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                try {
                    val jsonResponse = gson.fromJson(responseBody, Map::class.java)
                    callback(
                        jsonResponse["success"] == true,
                        jsonResponse["message"] as String?
                    )
                } catch (e: Exception) {
                    callback(false, "Parse error: ${e.message}")
                }
            }
        })
    }
}
