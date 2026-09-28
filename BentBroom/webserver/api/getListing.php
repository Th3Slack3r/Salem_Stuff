<?php
require 'db.php'; // contains $mysqli connection

header('Content-Type: application/json');

$item = $_GET['item'] ?? '';
$vendor_id = $_GET['vendor_id'] ?? '';

if (empty($item) && empty($vendor_id)) {
    echo json_encode(['error' => 'Item or Vendor ID parameter missing']);
    exit;
}

// Base SQL
$sql = "
    SELECT 
        l.id, 
        u.username AS vendor, 
        l.price,
        (l.quantity - l.on_hold) AS quantity,
        l.item_name,
        l.on_hold
    FROM 
        Listings l
    JOIN 
        Users u ON l.vendor_id = u.id
    WHERE 
        (l.quantity - l.on_hold) > 0
";

// Bind params dynamically
$params = [];
$types = '';
if (!empty($item)) {
    $sql .= " AND l.item_name = ?";
    $params[] = $item;
    $types .= 's';
} elseif (!empty($vendor_id)) {
    $sql .= " AND l.vendor_id = ?";
    $params[] = $vendor_id;
    $types .= 'i';
}

$sql .= " ORDER BY l.price ASC";

$stmt = $mysqli->prepare($sql);
if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}
$stmt->execute();
$result = $stmt->get_result();

$listings = [];
while ($row = $result->fetch_assoc()) {
    $listings[] = $row;
}

echo json_encode($listings);
$stmt->close();
$mysqli->close();
?>
