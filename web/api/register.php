<?php
require_once '../config.php';

// VULN: No input validation or sanitization
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['username']) || !isset($data['password'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Missing credentials'
    ]);
    exit;
}

$username = $data['username'];
$password = $data['password'];

// VULN: No password strength requirements
// VULN: Storing passwords in plaintext
// Should use password_hash() but intentionally vulnerable

$conn = getConnection();

// Check if user exists (also vulnerable to SQL injection)
$checkQuery = "SELECT * FROM users WHERE username = '$username'";
$result = $conn->query($checkQuery);

if ($result->num_rows > 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Username already exists'
    ]);
    exit;
}

// VULN: SQL Injection - No prepared statements
$insertQuery = "INSERT INTO users (username, password, balance) VALUES ('$username', '$password', 1000.00)";

if ($conn->query($insertQuery) === TRUE) {
    echo json_encode([
        'success' => true,
        'message' => 'Registration successful',
        'debug' => [
            'query' => $insertQuery,  // VULN: Exposing query structure
            'new_user_id' => $conn->insert_id
        ]
    ]);
} else {
    // VULN: Exposing database errors
    echo json_encode([
        'success' => false,
        'message' => 'Registration failed: ' . $conn->error,
        'query' => $insertQuery
    ]);
}

$conn->close();
?>
