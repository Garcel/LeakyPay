# LeakyPay Backend API

This is an intentionally vulnerable PHP/MySQL API backend for the LeakyPay educational project.

## ⚠️ Security Warning

This application contains **INTENTIONAL SECURITY VULNERABILITIES** for educational purposes only. **NEVER** deploy this in any production environment or publicly accessible server.

## Setup Instructions

### Prerequisites
- PHP 7.4 or higher
- MySQL 5.7 or higher
- Apache/Nginx web server (or PHP built-in server for testing)

### Installation

1. **Import the Database**
   ```bash
   mysql -u root -p < database.sql
   ```

2. **Configure Database Connection**
   - Edit `config.php` if your MySQL credentials differ from defaults
   - Default: user=`root`, password=``, database=`vulnerable_bank`

3. **Start the Server**

   **Option A: PHP Built-in Server (Development)**
   ```bash
   cd web
   php -S localhost:80
   ```

   **Option B: Apache/Nginx**
   - Copy the `web` folder to your web server document root
   - Ensure PHP is enabled
   - Access via `http://localhost`

4. **Test the API**
   - Visit `http://localhost` in your browser
   - You should see the API documentation page
   - Test an endpoint: `http://localhost/api/login.php?username=test&password=test`

## API Endpoints

### POST /api/register.php
Register a new user account.
```json
{
  "username": "newuser",
  "password": "password123"
}
```

### GET /api/login.php
Login with credentials (VULN: GET parameters).
```
?username=test&password=test
```

### GET /api/balance.php
Get account balance.
```
?username=test
```

### POST /api/transfer.php
Transfer money between accounts.
```json
{
  "from": "alice",
  "to": "bob",
  "amount": 100.50
}
```

## Test Accounts

| Username | Password     | Balance  |
|----------|--------------|----------|
| alice    | password123  | $5,000   |
| bob      | qwerty       | $3,000   |
| charlie  | 12345        | $1,500   |
| admin    | admin        | $10,000  |
| test     | test         | $1,000   |

## Documented Vulnerabilities

### SQL Injection
All endpoints are vulnerable to SQL injection. Examples:
```
login.php?username=admin' OR '1'='1&password=anything
balance.php?username=alice' UNION SELECT password FROM users--
```

### Insecure Authentication
- Passwords stored in plaintext
- No session management
- Credentials in GET parameters
- No rate limiting on login attempts

### Information Disclosure
- Detailed error messages expose database structure
- Debug information included in responses
- Database credentials hardcoded in config.php

### No Access Control
- Any user can check any other user's balance
- No validation that authenticated user matches transfer sender
- No CSRF tokens

### Additional Issues
- No input validation
- No prepared statements
- Overly permissive CORS
- Race conditions in transfers
- No security headers

## For Android App Integration

The Android app expects the API to be available at:
- **Emulator**: `http://10.0.2.2:80` (localhost on host machine)
- **Physical Device**: Use your computer's local IP address

Update `ApiClient.kt` in the Android app if using a different host/port.
