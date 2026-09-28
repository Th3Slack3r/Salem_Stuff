<?php
require 'validate_key.php';
require 'db.php';
header('Content-Type: application/json');

$contract_id = isset($_GET['contract_id']) ? intval($_GET['contract_id']) : 0;
$from_user_id = isset($_GET['from_user_id']) ? intval($_GET['from_user_id']) : 0;
$to_user_id = isset($_GET['to_user_id']) ? intval($_GET['to_user_id']) : 0;

// If the plugin calls with GET, this returns an array (possibly empty) of feedback left by from_user on this contract
if (!$contract_id || !$from_user_id) {
    echo json_encode([]);
    exit;
}

$stmt = $mysqli->prepare("SELECT id, contract_id, from_user_id, to_user_id, rating, comment, created_at FROM Feedback WHERE contract_id = ? AND from_user_id = ?");
$stmt->bind_param('ii', $contract_id, $from_user_id);
$stmt->execute();
$res = $stmt->get_result();
$rows = [];
while ($r = $res->fetch_assoc()) {
    $rows[] = $r;
}
$stmt->close();

echo json_encode($rows);
