# LeakyPay Vulnerability Catalog

Complete list of intentional security vulnerabilities in the LeakyPay educational project.

## OWASP Mobile Top 10 2021 (M1-M5)

### M1: Improper Platform Usage

#### 1. Exported Activity Without Authentication
- **File**: `android/app/src/main/AndroidManifest.xml:41-48`
- **Description**: SecretActivity is exported and can be launched by any app
- **Impact**: Unauthorized access to admin functionality
- **Exploitation**:
  ```bash
  adb shell am start -a com.vulnerable.app.SECRET_ACTION
  ```
- **CVSS**: High (7.5)

#### 2. Improper Back Button Handling
- **File**: `android/app/src/main/java/com/vulnerable/app/DashboardActivity.kt:87`
- **Description**: No re-authentication required when returning to app
- **Impact**: Session persistence without timeout
- **Exploitation**: Background the app, return without re-login

---

### M2: Insecure Data Storage

#### 3. Plaintext Credentials in SharedPreferences
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:12-24`
- **Description**: Username and password stored in plaintext
- **Impact**: Credentials accessible via backup, root, or ADB
- **Exploitation**:
  ```bash
  adb shell cat /data/data/com.vulnerable.app/shared_prefs/user_prefs.xml
  ```
- **CVSS**: Critical (9.1)

#### 4. Sensitive Data in External Storage
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:35-42`
- **Description**: Writing session tokens to external storage
- **Impact**: Any app with storage permission can read
- **Exploitation**:
  ```bash
  adb shell cat /sdcard/Android/data/com.vulnerable.app/files/sensitive_data.txt
  ```
- **CVSS**: High (7.8)

#### 5. Full Backup Enabled for Sensitive Data
- **File**: `android/app/src/main/res/xml/backup_rules.xml`
- **Description**: SharedPreferences and databases backed up to cloud
- **Impact**: Sensitive data exposed in Android backups
- **Exploitation**: Extract from Android backup file
- **CVSS**: High (7.2)

#### 6. Logging Sensitive Information
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:74-77`
- **Description**: Passwords and tokens logged to Logcat
- **Impact**: Credentials visible in system logs
- **Exploitation**:
  ```bash
  adb logcat | grep -E "(password|token)"
  ```
- **CVSS**: High (7.5)

---

### M3: Insecure Communication

#### 7. Cleartext HTTP Traffic Allowed
- **File**: `android/app/src/main/AndroidManifest.xml:18`
- **Description**: usesCleartextTraffic="true" allows HTTP
- **Impact**: MITM attacks, credential interception
- **Exploitation**: Use Burp Suite or mitmproxy
- **CVSS**: Critical (9.3)

#### 8. Disabled SSL Certificate Validation
- **File**: `android/app/src/main/java/com/vulnerable/app/ApiClient.kt:21-33`
- **Description**: Custom TrustManager accepts all certificates
- **Impact**: MITM attacks even with HTTPS
- **Exploitation**: Self-signed certificate accepted
- **CVSS**: Critical (9.8)

#### 9. Credentials in GET Parameters
- **File**: `android/app/src/main/java/com/vulnerable/app/ApiClient.kt:39-44`
- **Description**: Username/password sent in URL
- **Impact**: Credentials logged in server logs, browser history
- **Exploitation**: Check server access logs
- **CVSS**: High (8.2)

#### 10. No Certificate Pinning
- **File**: `android/app/src/main/java/com/vulnerable/app/ApiClient.kt`
- **Description**: No certificate pinning implemented
- **Impact**: MITM with CA-signed certificate
- **Exploitation**: Issue valid certificate for domain
- **CVSS**: Medium (6.5)

---

### M4: Insecure Authentication

#### 11. No Input Validation on Credentials
- **File**: `android/app/src/main/java/com/vulnerable/app/MainActivity.kt:50`
- **Description**: Raw user input sent to API
- **Impact**: SQL injection, XSS, command injection
- **Exploitation**: `username=admin'OR'1'='1`
- **CVSS**: Critical (9.8)

#### 12. No Password Strength Requirements
- **File**: `android/app/src/main/java/com/vulnerable/app/MainActivity.kt:70`
- **Description**: Accepts any password including "1", "a"
- **Impact**: Weak passwords allowed
- **Exploitation**: Brute force attack
- **CVSS**: Medium (5.3)

#### 13. Client-Side Only Validation
- **File**: `android/app/src/main/java/com/vulnerable/app/DashboardActivity.kt:55-60`
- **Description**: Transaction amount validated only on client
- **Impact**: Can send negative or excessive amounts
- **Exploitation**: Modify request with proxy
- **CVSS**: High (8.1)

#### 14. No Session Management
- **File**: `android/app/src/main/java/com/vulnerable/app/ApiClient.kt`
- **Description**: No session tokens, relies on username only
- **Impact**: Session hijacking, replay attacks
- **Exploitation**: Reuse captured requests
- **CVSS**: High (7.5)

---

### M5: Insufficient Cryptography

#### 15. Hardcoded Encryption Key
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:44`
- **Description**: DES key "12345678" hardcoded in source
- **Impact**: All encrypted data easily decrypted
- **Exploitation**: Decompile APK, extract key
- **CVSS**: Critical (9.1)

#### 16. Deprecated DES Algorithm
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:48`
- **Description**: Using DES instead of AES
- **Impact**: Weak 56-bit encryption
- **Exploitation**: DES can be brute-forced
- **CVSS**: High (7.5)

#### 17. Insecure ECB Mode
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:49`
- **Description**: Using ECB mode which shows patterns
- **Impact**: Pattern recognition in encrypted data
- **Exploitation**: Statistical analysis
- **CVSS**: Medium (6.2)

#### 18. Weak Random Number Generation
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:68-72`
- **Description**: Using java.util.Random with time seed
- **Impact**: Predictable session tokens
- **Exploitation**: Predict next token value
- **CVSS**: High (7.8)

#### 19. Fallback to Plaintext on Error
- **File**: `android/app/src/main/java/com/vulnerable/app/Utils.kt:53`
- **Description**: Returns plaintext if encryption fails
- **Impact**: Sensitive data exposed
- **Exploitation**: Trigger encryption error
- **CVSS**: Medium (6.5)

---

## Web/Backend Vulnerabilities

### SQL Injection

#### 20. Login SQL Injection
- **File**: `web/api/login.php:18`
- **Description**: Direct string concatenation in SQL
- **Impact**: Authentication bypass, data extraction
- **Exploitation**:
  ```sql
  username=admin' OR '1'='1'--&password=x
  ```
- **CVSS**: Critical (10.0)

#### 21. Balance SQL Injection
- **File**: `web/api/balance.php:16`
- **Description**: No prepared statements for user lookup
- **Impact**: Access any user's balance, dump database
- **Exploitation**:
  ```sql
  username=alice' UNION SELECT password FROM users--
  ```
- **CVSS**: Critical (9.8)

#### 22. Transfer SQL Injection
- **File**: `web/api/transfer.php:18-20`
- **Description**: Multiple injection points in transfer logic
- **Impact**: Arbitrary SQL execution
- **Exploitation**:
  ```sql
  from=alice' OR '1'='1&to=bob&amount=0.01
  ```
- **CVSS**: Critical (10.0)

#### 23. Registration SQL Injection
- **File**: `web/api/register.php:22-24`
- **Description**: User-controlled data in INSERT statement
- **Impact**: Database manipulation
- **Exploitation**:
  ```sql
  username=admin')--&password=x
  ```
- **CVSS**: Critical (9.9)

---

### Authentication & Authorization

#### 24. Plaintext Password Storage
- **File**: `web/database.sql:22`
- **Description**: Passwords stored without hashing
- **Impact**: Database compromise reveals all passwords
- **Exploitation**: Read database
- **CVSS**: Critical (9.8)

#### 25. No Authentication Required
- **File**: `web/api/balance.php`, `web/api/transfer.php`
- **Description**: Endpoints accessible without login
- **Impact**: Anyone can perform actions
- **Exploitation**: Direct API calls
- **CVSS**: Critical (10.0)

#### 26. No Authorization Checks
- **File**: `web/api/transfer.php:18`
- **Description**: Can transfer from any account
- **Impact**: Steal money from any user
- **Exploitation**:
  ```json
  {"from":"alice","to":"attacker","amount":9999}
  ```
- **CVSS**: Critical (10.0)

#### 27. Username Enumeration
- **File**: `web/api/login.php:38-45`
- **Description**: Different errors for invalid username vs password
- **Impact**: Enumerate valid usernames
- **Exploitation**: Brute force usernames
- **CVSS**: Medium (5.3)

---

### Information Disclosure

#### 28. Detailed Error Messages
- **File**: `web/api/login.php:28-32`
- **Description**: SQL errors exposed to client
- **Impact**: Database schema disclosure
- **Exploitation**: Trigger SQL error, read response
- **CVSS**: Medium (6.5)

#### 29. Debug Information in Responses
- **File**: `web/api/login.php:23-27`
- **Description**: Query and server info in JSON response
- **Impact**: Internal structure exposed
- **Exploitation**: Make API call, read debug field
- **CVSS**: Medium (5.8)

#### 30. Hardcoded Credentials Exposed
- **File**: `web/config.php:3-6`
- **Description**: Database credentials in source code
- **Impact**: Database access if source disclosed
- **Exploitation**: Read source file
- **CVSS**: Critical (9.6)

#### 31. Database Connection Details in Errors
- **File**: `web/config.php:16-21`
- **Description**: Host, user revealed in connection error
- **Impact**: Reconnaissance for attackers
- **Exploitation**: Cause connection failure
- **CVSS**: Low (4.2)

---

### Other Web Vulnerabilities

#### 32. No CSRF Protection
- **File**: `web/api/transfer.php`
- **Description**: No CSRF tokens on state-changing operations
- **Impact**: Forced transactions via XSS/phishing
- **Exploitation**: Create malicious HTML form
- **CVSS**: High (8.1)

#### 33. Overly Permissive CORS
- **File**: `web/config.php:11`
- **Description**: Access-Control-Allow-Origin: *
- **Impact**: Any website can make requests
- **Exploitation**: XSS from any domain
- **CVSS**: Medium (6.5)

#### 34. No Rate Limiting
- **File**: All `web/api/*.php` files
- **Description**: No throttling on requests
- **Impact**: Brute force attacks, DoS
- **Exploitation**: Automated attack scripts
- **CVSS**: High (7.5)

#### 35. Race Condition in Transfers
- **File**: `web/api/transfer.php:37-40`
- **Description**: No transaction locking
- **Impact**: Double-spend vulnerability
- **Exploitation**: Concurrent transfer requests
- **CVSS**: High (7.8)

#### 36. Directory Listing Enabled
- **File**: `web/.htaccess:4`
- **Description**: Options +Indexes allows browsing
- **Impact**: Discover hidden files
- **Exploitation**: Browse http://localhost/api/
- **CVSS**: Medium (5.3)

---

## Summary Statistics

- **Total Vulnerabilities**: 36
- **Critical (9.0-10.0)**: 13
- **High (7.0-8.9)**: 14
- **Medium (4.0-6.9)**: 9
- **Low (0.1-3.9)**: 0

### By Category
- **M1 - Improper Platform Usage**: 2
- **M2 - Insecure Data Storage**: 4
- **M3 - Insecure Communication**: 4
- **M4 - Insecure Authentication**: 4
- **M5 - Insufficient Cryptography**: 5
- **SQL Injection**: 4
- **Authentication/Authorization**: 4
- **Information Disclosure**: 4
- **Other Web**: 5

---

## Exploitation Priority

### Highest Impact (Try First)
1. SQL Injection in login (bypass auth)
2. No authorization on transfers (steal money)
3. Plaintext credentials in storage
4. HTTP traffic interception

### Educational Value (Best for Learning)
1. Exported activity (M1 demonstration)
2. Weak cryptography with hardcoded keys
3. SQL injection variations
4. Client-side validation bypass

---

## Remediation Guide

This section shows how to fix each vulnerability (for learning purposes):

### Fix M1: Improper Platform Usage
```xml
<!-- Remove android:exported="true" -->
<activity android:name=".SecretActivity" android:exported="false" />
```

### Fix M2: Insecure Data Storage
```kotlin
// Use Android Keystore
val keyStore = KeyStore.getInstance("AndroidKeyStore")
// Encrypt before storing in SharedPreferences
// Never log sensitive data
```

### Fix M3: Insecure Communication
```xml
<!-- Disable cleartext -->
<application android:usesCleartextTraffic="false">
<!-- Implement certificate pinning -->
```

### Fix M4: Insecure Authentication
```kotlin
// Validate input
// Implement proper session management with JWT
// Use secure password policies
```

### Fix M5: Insufficient Cryptography
```kotlin
// Use AES-256-GCM
// Generate keys properly with KeyGenerator
// Use SecureRandom for tokens
```

### Fix SQL Injection
```php
// Use prepared statements
$stmt = $conn->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
$stmt->bind_param("ss", $username, $password);
```

### Fix Authentication
```php
// Hash passwords
$hashed = password_hash($password, PASSWORD_ARGON2ID);
// Verify
password_verify($input, $hashed);
```

---

*This document is maintained as part of the LeakyPay educational project.*
