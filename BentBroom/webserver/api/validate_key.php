<?php
require 'db.php';
header('Content-Type: application/json');

$token = $_POST['token'] ?? $_GET['token'] ?? '';

if (empty($token)) {
    echo json_encode(['error' => 'Missing API key']);
    exit;
}

// Check if key exists in the Users table
$stmt = $mysqli->prepare("SELECT user_id FROM Sessions WHERE `token` = ?");
$stmt->bind_param('s', $token);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    echo json_encode(['error' => 'Invalid API key']);
    exit;
}

// Make $user_id available to including scripts
$user_id = (int)$user['user_id'];
?>
