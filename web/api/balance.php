<?php
require_once '../config.php';

// VULN: No authentication token validation
// VULN: Anyone can check any user's balance
if (!isset($_GET['username'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Username required'
    ]);
    exit;
}

$username = $_GET['username'];

$conn = getConnection();

// VULN: SQL Injection vulnerability
$query = "SELECT balance FROM users WHERE username = '$username'";

$result = $conn->query($query);

if ($result === false) {
    echo json_encode([
        'success' => false,
        'message' => 'Query error: ' . $conn->error,
        'query' => $query  // VULN: Exposing query
    ]);
    exit;
}

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        'success' => true,
        'balance' => floatval($row['balance']),
        'username' => $username
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'User not found'
    ]);
}

$conn->close();
?>
