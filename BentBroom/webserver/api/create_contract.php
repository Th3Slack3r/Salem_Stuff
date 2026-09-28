<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php';
header('Content-Type: application/json');

// Get parameters from POST or GET
$listing_id = $_POST['listing_id'] ?? $_GET['listing_id'] ?? null;
$buyer_id   = $_POST['buyer_id']   ?? $_GET['buyer_id'] ?? null;
$quantity   = $_POST['quantity']   ?? $_GET['quantity'] ?? null;

// Validate required parameters
if (!$listing_id || !$buyer_id || !$quantity) {
    echo json_encode(['error' => 'Missing parameters']);
    exit;
}

// Convert to integers
$listing_id = (int)$listing_id;
$buyer_id   = (int)$buyer_id;
$quantity   = (int)$quantity;

// Fetch listing details
$stmt = $mysqli->prepare("SELECT vendor_id, item_name, price, quantity, on_hold FROM Listings WHERE id = ?");
$stmt->bind_param('i', $listing_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $available = $row['quantity'] - $row['on_hold'];
    if ($available < $quantity) {
        echo json_encode(['error' => 'Not enough stock available']);
        exit;
    }

    // Create contract
    $insert = $mysqli->prepare("
        INSERT INTO Contracts (listing_id, buyer_id, vendor_id, item_name, quantity, price, status)
        VALUES (?, ?, ?, ?, ?, ?, 'active')
    ");
    $insert->bind_param(
        'iiisii',
        $listing_id,
        $buyer_id,
        $row['vendor_id'],
        $row['item_name'],
        $quantity,
        $row['price']
    );
    $insert->execute();

    // Put items on hold
    $update = $mysqli->prepare("UPDATE Listings SET on_hold = on_hold + ? WHERE id = ?");
    $update->bind_param('ii', $quantity, $listing_id);
    $update->execute();

    echo json_encode([
        'success' => true,
        'contract_id' => $mysqli->insert_id,
        'listing_id'  => $listing_id,
        'buyer_id'    => $buyer_id,
        'quantity'    => $quantity
    ]);

} else {
    echo json_encode(['error' => 'Listing not found']);
}
?>
