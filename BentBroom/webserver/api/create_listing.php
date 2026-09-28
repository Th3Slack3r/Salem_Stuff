<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

require 'validate_key.php';
require 'db.php';
header('Content-Type: application/json');

// --- Get POST data ---
$price    = $_POST['price'] ?? null;
$item     = $_POST['item'] ?? null;
$quantity = $_POST['quantity'] ?? null;

if (!$price || !$item || !$quantity) {
    echo json_encode(['error' => 'Missing parameters']);
    exit;
}

$price    = floatval($price);
$quantity = intval($quantity);
$item     = trim($item);
$itemLower = strtolower($item);

// --- Step 1: Lookup proper item name from CurrentItems ---
$stmt = $mysqli->prepare("SELECT Item FROM CurrentItems WHERE ItemLower = ?");
$stmt->bind_param('s', $itemLower);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['error' => 'Item not found in CurrentItems']);
    exit;
}

$row = $result->fetch_assoc();
$properItemName = $row['Item']; // proper-cased name from CurrentItems

// --- Step 2: Check if vendor already has this item listed ---
$stmt = $mysqli->prepare("SELECT id, quantity, price FROM Listings WHERE vendor_id = ? AND item_name = ?");
$stmt->bind_param('is', $user_id, $properItemName);
$stmt->execute();
$existingResult = $stmt->get_result();

if ($existingResult->num_rows > 0) {
    // --- Update existing listing ---
    $row = $existingResult->fetch_assoc();
    $listing_id = $row['id'];
    $newQuantity = $row['quantity'] + $quantity;

    $update = $mysqli->prepare("
        UPDATE Listings
        SET quantity = ?, price = ?
        WHERE id = ?
    ");
    $update->bind_param('idi', $newQuantity, $price, $listing_id);

    if ($update->execute()) {
        echo json_encode([
            'success' => true,
            'listing_id' => $listing_id,
            'updated' => true,
            'item' => $properItemName,
            'new_quantity' => $newQuantity,
            'price' => $price
        ]);
    } else {
        echo json_encode(['error' => 'Failed to update listing: ' . $update->error]);
    }

} else {
    // --- Insert new listing ---
    $insert = $mysqli->prepare("
        INSERT INTO Listings (vendor_id, item_name, quantity, price, on_hold, created_at)
        VALUES (?, ?, ?, ?, 0, NOW())
    ");
    $insert->bind_param('isid', $user_id, $properItemName, $quantity, $price);

    if ($insert->execute()) {
        $listing_id = $mysqli->insert_id;
        echo json_encode([
            'success' => true,
            'listing_id' => $listing_id,
            'created' => true,
            'item' => $properItemName,
            'quantity' => $quantity,
            'price' => $price
        ]);
    } else {
        echo json_encode(['error' => 'Failed to create listing: ' . $insert->error]);
    }
}
?>
