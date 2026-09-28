<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

require 'validate_key.php';
require 'db.php';
header('Content-Type: application/json');

$mode = $_POST['mode'] ?? $_GET['mode'] ?? null;

if (!$mode) {
    echo json_encode(['error' => 'Missing mode parameter']);
    exit;
}

// ===================================================
// MODE: CREATE / UPDATE (your original logic preserved)
// ===================================================
if ($mode === 'create') {
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

    // Step 1: Lookup proper item name
    $stmt = $mysqli->prepare("SELECT Item FROM CurrentItems WHERE ItemLower = ?");
    $stmt->bind_param('s', $itemLower);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(['error' => 'Item not found in CurrentItems']);
        exit;
    }

    $row = $result->fetch_assoc();
    $properItemName = $row['Item'];

    // Step 2: Check if vendor already has this item listed
    $stmt = $mysqli->prepare("SELECT id, quantity, price FROM Listings WHERE vendor_id = ? AND item_name = ?");
    $stmt->bind_param('is', $user_id, $properItemName);
    $stmt->execute();
    $existingResult = $stmt->get_result();

    if ($existingResult->num_rows > 0) {
        // Update existing listing
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
        // Insert new listing
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
    exit;
}

// ===================================================
// MODE: GET LISTINGS (works via browser or POST)
// ===================================================
if ($mode === 'get') {
    $vendorId = $_GET['vendor_id'] ?? $_POST['vendor_id'] ?? null;
    $item     = $_GET['item'] ?? $_POST['item'] ?? null;
    $limit    = intval($_GET['limit'] ?? $_POST['limit'] ?? 100);

    $query = "SELECT id, vendor_id, item_name, quantity, price, on_hold, created_at FROM Listings WHERE 1=1";
    $params = [];
    $types = '';

    if ($vendorId) {
        $query .= " AND vendor_id = ?";
        $params[] = $vendorId;
        $types .= 'i';
    }

    if ($item) {
        $query .= " AND item_name LIKE ?";
        $params[] = '%' . $item . '%';
        $types .= 's';
    }

    $query .= " ORDER BY created_at DESC LIMIT ?";
    $params[] = $limit;
    $types .= 'i';

    $stmt = $mysqli->prepare($query);
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }
    $stmt->execute();
    $result = $stmt->get_result();

    $listings = [];
    while ($row = $result->fetch_assoc()) {
        $listings[] = $row;
    }

    echo json_encode(['success' => true, 'count' => count($listings), 'listings' => $listings]);
    exit;
}

// ===================================================
// MODE: DELETE
// ===================================================
if ($mode === 'delete') {
    $listingId = $_POST['listing_id'] ?? null;

    if (!$listingId) {
        echo json_encode(['error' => 'Missing listing_id']);
        exit;
    }

    $stmt = $mysqli->prepare("DELETE FROM Listings WHERE id = ? AND vendor_id = ?");
    $stmt->bind_param('ii', $listingId, $user_id);

    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'deleted_id' => $listingId]);
    } else {
        echo json_encode(['error' => 'Failed to delete listing: ' . $stmt->error]);
    }
    exit;
}

// ===================================================
// MODE: UPDATE
// ===================================================
if ($mode === 'update') {
    $listingId = $_POST['listing_id'] ?? null;
    $price     = $_POST['price'] ?? null;
    $quantity  = $_POST['quantity'] ?? null;

    if (!$listingId || (!$price && !$quantity)) {
        echo json_encode(['error' => 'Missing parameters']);
        exit;
    }

    $fields = [];
    $params = [];
    $types = '';

    if ($price !== null) {
        $fields[] = "price = ?";
        $params[] = floatval($price);
        $types .= 'd';
    }

    if ($quantity !== null) {
        $fields[] = "quantity = ?";
        $params[] = intval($quantity);
        $types .= 'i';
    }

    $params[] = $listingId;
    $params[] = $user_id;
    $types .= 'ii';

    $query = "UPDATE Listings SET " . implode(', ', $fields) . " WHERE id = ? AND vendor_id = ?";
    $stmt = $mysqli->prepare($query);
    $stmt->bind_param($types, ...$params);

    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'updated_id' => $listingId]);
    } else {
        echo json_encode(['error' => 'Failed to update listing: ' . $stmt->error]);
    }
    exit;
}

// ===================================================
// INVALID MODE
// ===================================================
echo json_encode(['error' => 'Invalid mode']);
exit;
?>
