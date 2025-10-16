<!DOCTYPE html>
<html>
<head>
    <title>LeakyPay API</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 {
            color: #d32f2f;
        }
        .warning {
            background: #fff3cd;
            border: 1px solid #ffc107;
            padding: 15px;
            border-radius: 4px;
            margin: 20px 0;
        }
        .endpoint {
            background: #e3f2fd;
            padding: 10px;
            margin: 10px 0;
            border-left: 4px solid #2196f3;
        }
        code {
            background: #f5f5f5;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>⚠️ LeakyPay API</h1>

        <div class="warning">
            <strong>WARNING:</strong> This API contains intentional security vulnerabilities
            for educational purposes. DO NOT deploy this in any production environment!
        </div>

        <h2>API Status</h2>
        <p>✅ Server is running</p>
        <p>📅 Time: <?php echo date('Y-m-d H:i:s'); ?></p>
        <p>🖥️ Server: <?php echo $_SERVER['SERVER_SOFTWARE']; ?></p>

        <h2>Available Endpoints</h2>

        <div class="endpoint">
            <strong>POST /api/register.php</strong><br>
            Register a new user<br>
            Body: <code>{"username":"user", "password":"pass"}</code>
        </div>

        <div class="endpoint">
            <strong>GET /api/login.php</strong><br>
            Login (VULN: Credentials in GET params)<br>
            Params: <code>?username=user&password=pass</code>
        </div>

        <div class="endpoint">
            <strong>GET /api/balance.php</strong><br>
            Check account balance<br>
            Params: <code>?username=user</code>
        </div>

        <div class="endpoint">
            <strong>POST /api/transfer.php</strong><br>
            Transfer money between accounts<br>
            Body: <code>{"from":"user1", "to":"user2", "amount":100}</code>
        </div>

        <h2>Test Accounts</h2>
        <ul>
            <li>Username: <code>alice</code> | Password: <code>password123</code> | Balance: $5000</li>
            <li>Username: <code>bob</code> | Password: <code>qwerty</code> | Balance: $3000</li>
            <li>Username: <code>test</code> | Password: <code>test</code> | Balance: $1000</li>
            <li>Username: <code>admin</code> | Password: <code>admin</code> | Balance: $10000</li>
        </ul>

        <h2>Known Vulnerabilities</h2>
        <ul>
            <li>SQL Injection in all endpoints</li>
            <li>No authentication/session management</li>
            <li>Plaintext password storage</li>
            <li>Credentials transmitted in GET parameters</li>
            <li>No CSRF protection</li>
            <li>Information disclosure in error messages</li>
            <li>No rate limiting</li>
            <li>Exposed database credentials in source code</li>
        </ul>

        <h2>Database Info</h2>
        <?php
        require_once 'config.php';
        $conn = @getConnection();
        if ($conn) {
            echo "<p>✅ Database connection successful</p>";
            echo "<p>Database: <code>" . DB_NAME . "</code></p>";

            // VULN: Exposing database statistics
            $result = $conn->query("SELECT COUNT(*) as count FROM users");
            if ($result) {
                $row = $result->fetch_assoc();
                echo "<p>Total users: " . $row['count'] . "</p>";
            }
            $conn->close();
        } else {
            echo "<p>❌ Database connection failed. Make sure to import database.sql</p>";
        }
        ?>

        <p><small>This is part of the LeakyPay educational project.</small></p>
    </div>
</body>
</html>
