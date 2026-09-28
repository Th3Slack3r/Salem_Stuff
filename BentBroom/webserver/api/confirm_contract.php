<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php'; // contains $mysqli connection
header('Content-Type: application/json');

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed.']);
    exit;
}

$contract_id = $_POST['contract_id'] ?? null;
$player_id = $_POST['player_id'] ?? null;
$role = $_POST['role'] ?? null;

if (empty($contract_id) || empty($player_id) || empty($role)) {
    echo json_encode(['error' => 'Missing required parameters (contract_id, player_id, role).']);
    exit;
}

$contract_id = (int)$contract_id;
$player_id = (int)$player_id;

$column_to_update = '';
if ($role === 'buyer') {
    $column_to_update = 'buyer_confirmed';
} elseif ($role === 'seller') {
    $column_to_update = 'vendor_confirmed';
} else {
    echo json_encode(['error' => 'Invalid role specified.']);
    exit;
}

// 1. Check if the contract is active and if the player owns it
$check_stmt = $mysqli->prepare("
    SELECT status 
    FROM Contracts 
    WHERE id = ? AND (buyer_id = ? OR vendor_id = ?) AND status = 'active'
");
$check_stmt->bind_param('iii', $contract_id, $player_id, $player_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows === 0) {
    echo json_encode(['error' => 'Contract not found, not owned by player, or not currently active.']);
    $check_stmt->close();
    exit;
}
$check_stmt->close();

// 2. Update the specific confirmation column to 1
$update_sql = "UPDATE Contracts SET $column_to_update = 1 WHERE id = ?";
$update_stmt = $mysqli->prepare($update_sql);
$update_stmt->bind_param('i', $contract_id);

if (!$update_stmt->execute()) {
    echo json_encode(['error' => 'Database update failed: ' . $mysqli->error]);
    $update_stmt->close();
    $mysqli->close();
    exit;
}
$update_stmt->close();

// 3. Check for Dual Confirmation and Update Status
$message = "Contract #$contract_id confirmed as $role.";
$status_check_sql = "
    SELECT buyer_confirmed, vendor_confirmed 
    FROM Contracts 
    WHERE id = ?
";
$status_check_stmt = $mysqli->prepare($status_check_sql);
$status_check_stmt->bind_param('i', $contract_id);
$status_check_stmt->execute();
$status_result = $status_check_stmt->get_result();
$contract_data = $status_result->fetch_assoc();
$status_check_stmt->close();

if ($contract_data && $contract_data['buyer_confirmed'] == 1 && $contract_data['vendor_confirmed'] == 1) {
    // Both confirmed, change status to 'complete'
    $complete_stmt = $mysqli->prepare("UPDATE Contracts SET status = 'completed' WHERE id = ?");
    $complete_stmt->bind_param('i', $contract_id);
    
    if ($complete_stmt->execute()) {
        $message .= " Contract status changed to 'completed'.";
    } else {
        // If status update fails, log error but don't fail the whole confirmation
        error_log("Failed to set contract #$contract_id status to completed: " . $mysqli->error);
        $message .= " WARNING: Failed to finalize contract status.";
    }
    $complete_stmt->close();
}

// Final Success Response
echo json_encode(['success' => true, 'message' => $message]);

$mysqli->close();
?>