<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php'; // Your DB connection ($mysqli)
header('Content-Type: application/json');

// Get player_id from GET parameters
$player_id = $_GET['player_id'] ?? null;
if (!$player_id) {
    echo json_encode(['error' => 'Missing player_id parameter']);
    exit;
}
$player_id = (int)$player_id;

// SQL query to get contracts
$stmt = $mysqli->prepare("
    SELECT 
        id, 
        buyer_id, 
        vendor_id, 
        item_name, 
        quantity, 
        price,
        status,
        buyer_confirmed,
        vendor_confirmed,
        buyer_feedback,
        vendor_feedback
    FROM Contracts
    WHERE buyer_id = ? OR vendor_id = ?
    ORDER BY id DESC
");
$stmt->bind_param('ii', $player_id, $player_id);
$stmt->execute();
$result = $stmt->get_result();

$contracts = [];
while ($row = $result->fetch_assoc()) {
    // Determine if feedback is left
    $feedbackLeft = 0;
    if (($row['vendor_id'] == $player_id && (int)$row['vendor_feedback'] == 1) ||
        ($row['buyer_id']  == $player_id && (int)$row['buyer_feedback'] == 1)) {
        $feedbackLeft = 1;
    }

    // Determine if player has confirmed
    $playerConfirmed = 0;
    if (($row['buyer_id'] == $player_id && (int)$row['buyer_confirmed'] == 1) ||
        ($row['vendor_id'] == $player_id && (int)$row['vendor_confirmed'] == 1)) {
        $playerConfirmed = 1;
    }

    $contracts[] = [
        'id'                => (int)$row['id'],
        'buyer_id'          => (int)$row['buyer_id'],
        'vendor_id'         => (int)$row['vendor_id'],
        'item_name'         => $row['item_name'],
        'quantity'          => (int)$row['quantity'],
        'price'             => (int)$row['price'],
        'status'            => $row['status'],
        'feedback_left'     => $feedbackLeft,
        'player_confirmed'  => $playerConfirmed
    ];
}

echo json_encode($contracts);

$mysqli->close();
?>
