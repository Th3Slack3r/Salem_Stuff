<?php
require 'db.php';
header('Content-Type: application/json');

$token = trim($_POST['token'] ?? '');

if (empty($token)) {
    echo json_encode(['error' => 'Token required.']);
    exit;
}

$stmt = $mysqli->prepare("
    SELECT Users.id, Users.username, Users.email
    FROM Sessions
    JOIN Users ON Sessions.user_id = Users.id
    WHERE Sessions.token = ? AND Sessions.expires > NOW()
");
$stmt->bind_param('s', $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['error' => 'Invalid or expired token.']);
    exit;
}

$user = $result->fetch_assoc();

echo json_encode([
    'success' => true,
    'user' => $user
]);

$stmt->close();
$mysqli->close();
?>

