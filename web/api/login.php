<?php
require_once '../config.php';

// VULN: Accepting credentials via GET parameters (exposed in logs/history)
if (isset($_GET['username']) && isset($_GET['password'])) {
    $username = $_GET['username'];
    $password = $_GET['password'];
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Missing credentials'
    ]);
    exit;
}

$conn = getConnection();

// VULN: SQL Injection - Direct concatenation of user input
// VULN: No prepared statements
$query = "SELECT * FROM users WHERE username = '$username' AND password = '$password'";

// VULN: Logging the full query including credentials
error_log("Login query: $query");

$result = $conn->query($query);

if ($result === false) {
    // VULN: Exposing SQL error details to client
    echo json_encode([
        'success' => false,
        'message' => 'Query error: ' . $conn->error,
        'query' => $query
    ]);
    exit;
}

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();

    // VULN: Returning sensitive information
    echo json_encode([
        'success' => true,
        'message' => 'Login successful',
        'user' => $user,  // VULN: Exposing password hash and other sensitive data
        'debug' => [
            'query' => $query,
            'server' => $_SERVER['SERVER_SOFTWARE']
        ]
    ]);
} else {
    // VULN: Information disclosure - revealing whether username exists
    $checkUser = "SELECT username FROM users WHERE username = '$username'";
    $userCheck = $conn->query($checkUser);

    if ($userCheck->num_rows > 0) {
        $message = "Invalid password for user '$username'";
    } else {
        $message = "Username '$username' not found";
    }

    echo json_encode([
        'success' => false,
        'message' => $message
    ]);
}

$conn->close();
?>
