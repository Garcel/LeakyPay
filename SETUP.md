# LeakyPay Setup Guide

Step-by-step instructions to get LeakyPay (the intentionally vulnerable mobile banking app) running.

## Prerequisites Installation

### Windows

1. **Install Java JDK 11+**
   - Download from: https://adoptium.net/
   - Add to PATH

2. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install Android SDK API 34
   - Create an emulator (Pixel 5 API 34 recommended)

3. **Install PHP 7.4+**
   - Download from: https://windows.php.net/download/
   - Add to PATH
   - Enable mysqli extension in php.ini

4. **Install MySQL 5.7+**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Or use XAMPP: https://www.apachefriends.org/

### macOS

```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install prerequisites
brew install openjdk@11
brew install php
brew install mysql

# Install Android Studio manually from:
# https://developer.android.com/studio
```

### Linux (Ubuntu/Debian)

```bash
# Update package list
sudo apt update

# Install Java
sudo apt install openjdk-11-jdk

# Install PHP and MySQL
sudo apt install php php-mysqli mysql-server

# Install Android Studio from:
# https://developer.android.com/studio
```

## Backend Setup

### Step 1: Start MySQL

**Windows (XAMPP):**
- Start XAMPP Control Panel
- Start MySQL service

**Windows (Standalone MySQL):**
```cmd
net start MySQL80
```

**macOS/Linux:**
```bash
mysql.server start
# Or
sudo systemctl start mysql
```

### Step 2: Import Database

```bash
# Navigate to project
cd "LeakyPay/web"

# Import database (will prompt for password)
mysql -u root -p < database.sql

# If no password set:
mysql -u root < database.sql
```

**Verify Import:**
```sql
mysql -u root -p
USE vulnerable_bank;
SHOW TABLES;
SELECT username, password FROM users;
exit;
```

You should see 5 users: alice, bob, charlie, admin, test

### Step 3: Start PHP Server

```bash
cd web
php -S localhost:80

# If port 80 is in use, try 8000:
php -S localhost:8000
```

**Verify Backend:**
- Open browser: http://localhost
- You should see "LeakyPay API" page
- Test endpoint: http://localhost/api/login.php?username=test&password=test
- Should return JSON with success: true

### Step 4: Configure Backend URL (If Using Port 8000)

If you used port 8000, update the Android app:

Edit `android/app/src/main/java/com/vulnerable/app/ApiClient.kt`:
```kotlin
private const val PORT = "8000"  // Change from "80" to "8000"
```

## Android App Setup

### Step 1: Update SDK Path

Edit `android/local.properties`:
```properties
# Windows
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# macOS
sdk.dir=/Users/YourUsername/Library/Android/sdk

# Linux
sdk.dir=/home/YourUsername/Android/Sdk
```

Or let Android Studio configure this automatically.

### Step 2: Open Project in Android Studio

1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to `LeakyPay/android`
4. Click "OK"
5. Wait for Gradle sync to complete

**If Gradle Sync Fails:**
- File → Invalidate Caches / Restart
- Tools → SDK Manager → Install Android SDK 34
- Rebuild project

### Step 3: Create Android Emulator

1. Tools → Device Manager
2. Create Virtual Device
3. Choose: Pixel 5
4. System Image: API 34 (Android 14)
5. Finish
6. Start the emulator

### Step 4: Configure Backend URL for Physical Device

**If using a physical Android device:**

1. Find your computer's local IP address:

   **Windows:**
   ```cmd
   ipconfig
   # Look for IPv4 Address (e.g., 192.168.1.100)
   ```

   **macOS/Linux:**
   ```bash
   ifconfig
   # Look for inet address (e.g., 192.168.1.100)
   ```

2. Update `ApiClient.kt` (line 14):
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100"  // Your IP
   ```

3. Connect device and computer to same WiFi network

**For Emulator:**
- Default `http://10.0.2.2` works (maps to host's localhost)

### Compilation Fixes

The Android app has been configured to compile successfully with these fixes:

1. **OkHttp 3.12 API Compatibility**: Updated ApiClient.kt to use correct OkHttp 3.x syntax
   - `MediaType.parse()` instead of `.toMediaType()`
   - `RequestBody.create(mediaType, json)` instead of `.toRequestBody()`
   - `response.body()?.string()` instead of `.body?.string()`

2. **Missing Launcher Icons**: Removed icon references from AndroidManifest.xml (app uses Android default icon)

3. **Deprecation Warnings**: Added `@Suppress("DEPRECATION")` for intentionally vulnerable deprecated methods

### Step 5: Build and Install

**Option A: Using Android Studio**
1. Select your device/emulator
2. Click Run (green play button)
3. App will install and launch

**Option B: Command Line**
```bash
cd android

# Build
./gradlew assembleDebug
# or on Windows:
gradlew.bat assembleDebug

# Install
./gradlew installDebug

# Launch
adb shell am start -n com.vulnerable.app/.MainActivity
```

## Verification & Testing

### Test Backend API

```bash
# Test login
curl "http://localhost/api/login.php?username=test&password=test"

# Expected response:
# {"success":true,"message":"Login successful",...}

# Test SQL injection
curl "http://localhost/api/login.php?username=admin'%20OR%20'1'='1&password=x"

# Test balance
curl "http://localhost/api/balance.php?username=alice"
```

### Test Android App

1. Launch app
2. Login with test credentials:
   - Username: `test`
   - Password: `test`
3. Should see dashboard with balance: $1000.00
4. Try transfer to another account (e.g., `alice`)

### Test Vulnerabilities

```bash
# M1: Launch secret activity
adb shell am start -a com.vulnerable.app.SECRET_ACTION

# M2: View stored credentials
adb shell
run-as com.vulnerable.app
cat shared_prefs/user_prefs.xml
exit

# View logs
adb logcat | grep -E "(MainActivity|ApiClient|password)"
```

## Troubleshooting

### Backend Issues

**"Connection failed" in browser:**
- Check MySQL is running: `mysql -u root -p`
- Verify database imported: `USE vulnerable_bank; SHOW TABLES;`
- Check PHP extensions: `php -m | grep mysqli`

**Port 80 already in use:**
- Use different port: `php -S localhost:8000`
- Update Android app PORT constant

**API returns HTML instead of JSON:**
- Check you're accessing `/api/*.php` not just `/`
- Verify PHP is processing files (not just serving as text)

### Android Issues

**Gradle sync failed:**
- Check Java version: `java -version` (should be 11+)
- Update Gradle: Edit `gradle/wrapper/gradle-wrapper.properties`
- Install Android SDK 34 via SDK Manager

**App can't connect to backend:**
- Emulator: Verify using `http://10.0.2.2:80`
- Physical device: Check WiFi, firewall, correct IP
- Test backend in browser first
- Check ApiClient.kt BASE_URL and PORT

**"CLEARTEXT communication not permitted":**
- This is expected to be allowed (it's a vulnerability)
- Verify AndroidManifest.xml has `usesCleartextTraffic="true"`

**App crashes on launch:**
- Check logcat: `adb logcat *:E`
- Verify all dependencies installed
- Clean and rebuild: Build → Clean Project → Rebuild

### Database Issues

**"Access denied for user":**
```bash
# Reset MySQL root password
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
```

**"Unknown database":**
- Re-import: `mysql -u root -p < database.sql`
- Or create manually:
  ```sql
  CREATE DATABASE vulnerable_bank;
  USE vulnerable_bank;
  SOURCE database.sql;
  ```

## Development Setup

### Enable ADB Debugging

**Physical Device:**
1. Settings → About Phone
2. Tap "Build Number" 7 times
3. Settings → Developer Options
4. Enable "USB Debugging"
5. Connect via USB
6. Verify: `adb devices`

### Install Burp Suite (Optional - for MITM testing)

1. Download: https://portswigger.net/burp/communitydownload
2. Configure Android proxy:
   - Settings → WiFi → Long press network → Modify
   - Proxy: Manual
   - Host: Your computer's IP
   - Port: 8080
3. Install Burp CA certificate on device
4. Intercept traffic in Burp

## Next Steps

Once everything is running:

1. **Read the documentation:**
   - `README.md` - Project overview
   - `VULNERABILITIES.md` - Complete vulnerability list
   - `android/README.md` - Android-specific details
   - `web/README.md` - Backend API details

2. **Try the exploits:**
   - Follow examples in VULNERABILITIES.md
   - Practice SQL injection
   - Test exported activity
   - Intercept HTTP traffic

3. **Learn from the code:**
   - Every vulnerability has `// VULN:` comments
   - Understand why each is dangerous
   - Think about proper fixes

## Support

For issues with this setup:
1. Check TROUBLESHOOTING section above
2. Review error messages carefully
3. Verify all prerequisites installed
4. Ensure backend and Android app can communicate

Remember: This is an educational tool with intentional vulnerabilities. Never deploy to production!
