# LeakyPay Android App

This is an intentionally vulnerable Android banking application demonstrating the **OWASP Mobile Top 10 2021** vulnerabilities (M1-M5).

## ⚠️ Security Warning

This application contains **INTENTIONAL SECURITY VULNERABILITIES** for educational purposes only. This is a defensive security training tool. **DO NOT** use this code in production applications.

## OWASP Mobile Top 10 Vulnerabilities Implemented

### M1: Improper Platform Usage
- **SecretActivity.kt**: Exported activity without proper authorization
  - Can be launched via deeplink by any app
  - Access via: `adb shell am start -a com.vulnerable.app.SECRET_ACTION`
- **AndroidManifest.xml**: Improper intent filter configuration

### M2: Insecure Data Storage
- **Utils.kt**:
  - Plaintext credentials in SharedPreferences
  - Sensitive data written to external storage
  - Logging sensitive information (passwords, session tokens)
- **backup_rules.xml**: Full backup including sensitive data allowed
- **data_extraction_rules.xml**: Cloud backup of sensitive data enabled

### M3: Insecure Communication
- **AndroidManifest.xml**:
  - `usesCleartextTraffic="true"` - HTTP allowed
- **ApiClient.kt**:
  - Using HTTP instead of HTTPS
  - Disabled SSL certificate validation
  - Accepting all hostnames
  - Sending credentials in GET parameters
- **build.gradle**: Using outdated OkHttp version

### M4: Insecure Authentication
- **MainActivity.kt**:
  - No input validation on credentials
  - No password strength requirements
  - Storing passwords in plaintext locally
- **ApiClient.kt**:
  - Credentials transmitted in URL parameters (exposed in logs)
  - No session token management
  - Client-side only validation
- **DashboardActivity.kt**:
  - No transaction verification
  - No re-authentication for sensitive operations
  - Client can manipulate transaction parameters

### M5: Insufficient Cryptography
- **Utils.kt**:
  - Hardcoded encryption key (`SECRET_KEY = "12345678"`)
  - Using deprecated DES algorithm
  - Using weak ECB mode
  - Predictable random number generation for session tokens
  - Fallback to plaintext on encryption errors

## Build and Run

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API 34
- Gradle 8.1+

### Setup

1. **Open Project in Android Studio**
   ```bash
   cd android
   # Open this folder in Android Studio
   ```

2. **Configure Backend URL**
   - Open `ApiClient.kt` (line 14)
   - Update `BASE_URL` if needed:
     - Emulator: `http://10.0.2.2` (default, maps to host's localhost)
     - Physical device: Use your computer's local IP address (e.g., `http://192.168.1.100`)

3. **Build and Run**
   - Click "Run" in Android Studio, or:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### Compilation Notes

The app has been configured to compile successfully with the following fixes:
- **OkHttp 3.12 API**: Using correct syntax for MediaType.parse(), RequestBody.create(), and response.body()
- **No custom launcher icons**: App uses Android default icon (to avoid missing resource errors)
- **Deprecated API suppression**: @Suppress annotation added for onBackPressed() method

### Running on Emulator
```bash
# Start emulator
emulator -avd <your_avd_name>

# Install app
./gradlew installDebug

# Run app
adb shell am start -n com.vulnerable.app/.MainActivity
```

### Testing Vulnerabilities

#### Test M1 (Improper Platform Usage)
Launch the secret activity from another app or adb:
```bash
adb shell am start -a com.vulnerable.app.SECRET_ACTION
```

#### Test M2 (Insecure Data Storage)
View stored credentials:
```bash
adb shell
cd /data/data/com.vulnerable.app/shared_prefs
cat user_prefs.xml

# View logs with sensitive data
adb logcat | grep -E "(MainActivity|DashboardActivity|ApiClient)"

# Check external storage
cd /sdcard/Android/data/com.vulnerable.app/files
cat sensitive_data.txt
```

#### Test M3 (Insecure Communication)
Intercept traffic with proxy tools:
- Set up Burp Suite or mitmproxy
- Configure Android proxy settings
- Observe plaintext HTTP traffic with credentials

#### Test M4 (Insecure Authentication)
- Try SQL injection in login: `admin' OR '1'='1`
- No account lockout after failed attempts
- Session persists without timeout

#### Test M5 (Insufficient Cryptography)
Decrypt stored tokens:
```kotlin
// The hardcoded key "12345678" with DES can be easily decrypted
```

## Test Credentials

Use these credentials with the backend API:
- Username: `test` / Password: `test`
- Username: `alice` / Password: `password123`
- Username: `admin` / Password: `admin`

## Project Structure

```
app/src/main/
├── java/com/vulnerable/app/
│   ├── MainActivity.kt          # Login/Register screen
│   ├── DashboardActivity.kt     # Main app screen with balance/transfer
│   ├── SecretActivity.kt        # Exported activity (M1 vuln)
│   ├── ApiClient.kt             # HTTP client with vulnerabilities
│   └── Utils.kt                 # Crypto and storage utilities
├── res/
│   ├── layout/                  # UI layouts
│   ├── values/                  # Strings, colors, themes
│   └── xml/                     # Backup rules
└── AndroidManifest.xml          # App configuration with vulns
```

## Learning Resources

Each vulnerability is marked with `// VULN:` comments explaining:
- What the vulnerability is
- Why it's dangerous
- How it violates OWASP Mobile Top 10

Study these comments to understand mobile security best practices by seeing what NOT to do.
