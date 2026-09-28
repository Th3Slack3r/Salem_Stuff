<?php
require 'db.php';
header('Content-Type: application/json');

$contract_id  = isset($_POST['contract_id']) ? intval($_POST['contract_id']) : 0;
$from_user_id = isset($_POST['from_user_id']) ? intval($_POST['from_user_id']) : 0;
$to_user_id   = isset($_POST['to_user_id']) ? intval($_POST['to_user_id']) : 0;
$rating       = isset($_POST['rating']) ? intval($_POST['rating']) : 0;
$comment      = isset($_POST['comment']) ? trim($_POST['comment']) : '';
$buyer_id      = isset($_POST['buyer']) ? intval($_POST['buyer']) : 0;

if (!$contract_id || !$from_user_id || !$to_user_id || $rating < 1 || $rating > 5) {
    echo json_encode(['error' => 'Missing or invalid parameters']);
    exit;
}

// Prevent duplicate feedback
$check = $mysqli->prepare("SELECT id FROM Feedback WHERE contract_id = ? AND from_user_id = ?");
$check->bind_param('ii', $contract_id, $from_user_id);
$check->execute();
$checkRes = $check->get_result();
if ($checkRes->num_rows > 0) {
    echo json_encode(['error' => 'Feedback already left']);
    exit;
}
$check->close();

// Insert feedback
$ins = $mysqli->prepare("INSERT INTO Feedback (contract_id, from_user_id, to_user_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
$ins->bind_param('iiiis', $contract_id, $from_user_id, $to_user_id, $rating, $comment);
if (!$ins->execute()) {
    echo json_encode(['error' => 'DB insert failed']);
    exit;
}
$ins->close();

if ($from_user_id == $buyer_id) {
    $upd = $mysqli->prepare("UPDATE Contracts SET buyer_feedback = 1 WHERE id = ?");
} else {
    $upd = $mysqli->prepare("UPDATE Contracts SET vendor_feedback = 1 WHERE id = ?");
}

$upd->bind_param('i', $contract_id);
if (!$upd->execute()) {
    echo json_encode(['error' => 'Failed to update contract feedback']);
    exit;
}
$upd->close();

echo json_encode(['success' => true, 'message' => 'Feedback recorded']);
?>





