<?php
// VULN: Hardcoded database credentials in source code
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'vulnerable_bank');

// VULN: Exposing error messages in production
error_reporting(E_ALL);
ini_set('display_errors', 1);

// VULN: Overly permissive CORS policy
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

// Create database connection
function getConnection() {
    // VULN: Using mysqli without proper error handling
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

    if ($conn->connect_error) {
        // VULN: Exposing database connection details in error message
        die(json_encode([
            'success' => false,
            'message' => 'Connection failed: ' . $conn->connect_error,
            'host' => DB_HOST,
            'user' => DB_USER
        ]));
    }

    return $conn;
}

// VULN: No security headers
// Missing: X-Frame-Options, X-Content-Type-Options, etc.
?>
