<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php';
header('Content-Type: application/json');

// Only POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Use POST.']);
    exit;
}

$username = trim($_POST['username'] ?? '');
$password = trim($_POST['password'] ?? '');

if (empty($username) || empty($password)) {
    echo json_encode(['error' => 'All fields are required.']);
    exit;
}

// Find user
$stmt = $mysqli->prepare("SELECT id, username, email, password, confirmed FROM Users WHERE username = ?");
$stmt->bind_param('s', $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['error' => 'Invalid username or password.']);
    exit;
}

$user = $result->fetch_assoc();
$stmt->close();

// Verify password
if (!password_verify($password, $user['password'])) {
    echo json_encode(['error' => 'Invalid username or password.']);
    exit;
}

if ((int)$user['confirmed'] === 0) {
    echo json_encode(['error' => 'Account not confirmed.']);
    exit;
}

// Generate session token
$token = bin2hex(random_bytes(32));
$expires = date('Y-m-d H:i:s', time() + 60 * 60 * 24 * 7); // 7 days

$stmt = $mysqli->prepare("INSERT INTO Sessions (user_id, token, expires) VALUES (?, ?, ?)
                          ON DUPLICATE KEY UPDATE token = VALUES(token), expires = VALUES(expires)");
$stmt->bind_param('iss', $user['id'], $token, $expires);
$stmt->execute();
$stmt->close();

echo json_encode([
    'success' => true,
    'token' => $token,
    'player_id'=> (int)$user['id'],
    'user' => [
        'id' => (int)$user['id'],
        'username' => $user['username'],
        'email' => $user['email']
    ]
]);

$mysqli->close();
?>

