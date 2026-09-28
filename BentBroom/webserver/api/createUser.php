<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php'; // $mysqli connection

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed. Use POST.']);
    exit;
}

// Get POST data
$username = trim($_POST['username'] ?? '');
$password = trim($_POST['password'] ?? '');
$email    = trim($_POST['email'] ?? '');
$hs       = trim($_POST['hs'] ?? '');
$user_key = trim($_POST['key'] ?? ''); // use the derived key from plugin

if (empty($username) || empty($password) || empty($email) || empty($hs) || empty($user_key)) {
    echo json_encode(['error' => 'All fields are required.']);
    exit;
}

// Check if username or email already exists
$check_stmt = $mysqli->prepare("SELECT id FROM Users WHERE username = ? OR email = ?");
$check_stmt->bind_param('ss', $username, $email);
$check_stmt->execute();
if ($check_stmt->get_result()->num_rows > 0) {
    echo json_encode(['error' => 'Username or email already in use.']);
    $check_stmt->close();
    exit;
}
$check_stmt->close();

// Hash the password
$password_hash = password_hash($password, PASSWORD_DEFAULT);

// Default values
$is_vendor  = 0;
$confirmed  = 0;
$reputation = 0;

// Insert user
$insert_stmt = $mysqli->prepare("
    INSERT INTO Users (username, email, is_vendor, confirmed, reputation, password, `key`, hs)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
");
$insert_stmt->bind_param(
    'ssiiisss',
    $username,
    $email,
    $is_vendor,
    $confirmed,
    $reputation,
    $password_hash,
    $user_key,
    $hs
);

if ($insert_stmt->execute()) {
    $player_id = $mysqli->insert_id;

    // Save key to JSON file server-side (optional)
    $folder = __DIR__ . '/plugindata';
    if (!is_dir($folder)) mkdir($folder, 0755, true);
    $keyFile = $folder . '/userkey.json';
    file_put_contents($keyFile, json_encode(['key' => $user_key]));

    echo json_encode([
        'success' => true,
        'player_id' => (int)$player_id,
        'key' => $user_key
    ]);
} else {
    echo json_encode(['error' => 'Failed to create user.', 'details' => $insert_stmt->error]);
}

$insert_stmt->close();
$mysqli->close();
?>

