<?php
require_once '../config.php';

// VULN: No CSRF protection
// VULN: No authentication/session validation
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['from']) || !isset($data['to']) || !isset($data['amount'])) {
    echo json_encode([
        'success' => false,
        'message' => 'Missing parameters'
    ]);
    exit;
}

$from = $data['from'];
$to = $data['to'];
$amount = $data['amount'];

// VULN: No server-side validation of amount
// VULN: Client can manipulate who they're sending from

$conn = getConnection();

// VULN: SQL Injection in all queries
// Check sender balance
$balanceQuery = "SELECT balance FROM users WHERE username = '$from'";
$result = $conn->query($balanceQuery);

if ($result->num_rows == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Sender account not found'
    ]);
    exit;
}

$senderBalance = $result->fetch_assoc()['balance'];

// VULN: Race condition - no transaction locking
if ($senderBalance < $amount) {
    echo json_encode([
        'success' => false,
        'message' => 'Insufficient balance',
        'current_balance' => $senderBalance
    ]);
    exit;
}

// Check recipient exists
$recipientQuery = "SELECT id FROM users WHERE username = '$to'";
$recipientResult = $conn->query($recipientQuery);

if ($recipientResult->num_rows == 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Recipient account not found'
    ]);
    exit;
}

// VULN: Not using database transactions
// VULN: SQL Injection in UPDATE queries
$debitQuery = "UPDATE users SET balance = balance - $amount WHERE username = '$from'";
$creditQuery = "UPDATE users SET balance = balance + $amount WHERE username = '$to'";

$debitResult = $conn->query($debitQuery);
$creditResult = $conn->query($creditQuery);

if ($debitResult && $creditResult) {
    // VULN: Exposing internal query details
    echo json_encode([
        'success' => true,
        'message' => 'Transfer successful',
        'details' => [
            'from' => $from,
            'to' => $to,
            'amount' => $amount,
            'debit_query' => $debitQuery,
            'credit_query' => $creditQuery
        ]
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Transfer failed: ' . $conn->error,
        'debug' => [
            'debit_result' => $debitResult ? 'success' : 'failed',
            'credit_result' => $creditResult ? 'success' : 'failed'
        ]
    ]);
}

$conn->close();
?>
