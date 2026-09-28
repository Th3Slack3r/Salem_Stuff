<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php';        // Database connection ($mysqli)
require 'key_utils.php'; // generate_user_key()
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit;
}

$action   = $_POST['action'] ?? '';
$username = trim($_POST['username'] ?? '');
$password = trim($_POST['password'] ?? '');
$email    = trim($_POST['email'] ?? '');

// --- Helper to send success with key ---
function send_success($mysqli, $key) {
    $stmt = $mysqli->prepare("SELECT id, user_key FROM Users WHERE user_key = ?");
    $stmt->bind_param('s', $key);
    $stmt->execute();
    $result = $stmt->get_result();
    $user = $result->fetch_assoc();
    $stmt->close();

    if ($user) {
        echo json_encode([
            'success'   => true,
            'player_id' => (int)$user['id'],
            'key'       => $user['user_key']
        ]);
    } else {
        echo json_encode(['error' => 'Verification failed after update.']);
    }
    exit;
}

// --- LOGIN ---
if ($action === 'login') {
    if (empty($username) || empty($password)) {
        echo json_encode(['error' => 'Username and password are required']);
        exit;
    }

    $stmt = $mysqli->prepare("SELECT password_hash, user_key FROM Users WHERE username = ?");
    $stmt->bind_param('s', $username);
    $stmt->execute();
    $result = $stmt->get_result();
    $user = $result->fetch_assoc();
    $stmt->close();

    if ($user && password_verify($password, $user['password_hash'])) {
        send_success($mysqli, $user['user_key']);
    } else {
        echo json_encode(['error' => 'Invalid username or password']);
    }

// --- REGISTER / SETUP ---
} elseif ($action === 'setup') {
    if (empty($username) || empty($password) || empty($email)) {
        echo json_encode(['error' => 'Username, password, and email are required']);
        exit;
    }

    // Check existing username/email
    $check_stmt = $mysqli->prepare("SELECT id FROM Users WHERE username = ? OR email = ?");
    $check_stmt->bind_param('ss', $username, $email);
    $check_stmt->execute();
    if ($check_stmt->get_result()->num_rows > 0) {
        echo json_encode(['error' => 'Username or email already in use']);
        $check_stmt->close();
        exit;
    }
    $check_stmt->close();

    // Generate key and hash password
    $new_key = generate_user_key($username, $email);
    $password_hash = password_hash($password, PASSWORD_DEFAULT);

    // Insert new user
    $insert_stmt = $mysqli->prepare("
        INSERT INTO Users (username, password_hash, email, user_key)
        VALUES (?, ?, ?, ?)
    ");
    $insert_stmt->bind_param('ssss', $username, $password_hash, $email, $new_key);

    if ($insert_stmt->execute()) {
        send_success($mysqli, $new_key);
    } else {
        echo json_encode(['error' => 'Failed to create user: ' . $mysqli->error]);
    }
    $insert_stmt->close();

} else {
    echo json_encode(['error' => 'Invalid action specified']);
}

$mysqli->close();
?>
