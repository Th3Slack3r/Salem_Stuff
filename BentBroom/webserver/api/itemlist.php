<?php
//require 'db.php'; // contains $mysqli connection
// 1. Database Connection Details
$host     = '2.56.246.128';
$port     = '3306';
$user     = 'u14751_gGrr4LahO7';
$password = 'yl6q7+GcCY6+PWILdf^W0vsF';
$dbname   = 's14751_BentBroom';
$table    = 'CurrentItems';

// Set the content type header to application/json
header('Content-Type: application/json');

// 2. Establish Connection
// Using the full host:port format for the connection string might be needed for specific setups
$mysqli = new mysqli("$host", $user, $password, $dbname, $port);

// Check connection
if ($mysqli->connect_errno) {
    // If connection fails, output an error message as JSON
    http_response_code(500); // Set HTTP response code to 500 (Internal Server Error)
    echo json_encode(['error' => 'Database connection failed: ' . $mysqli->connect_error]);
    exit();
}

// 3. Prepare and Execute Query
// We are selecting only the 'Item' column
$sql = "SELECT Item FROM $table";

// Prepare the statement
if (!$stmt = $mysqli->prepare($sql)) {
    http_response_code(500);
    echo json_encode(['error' => 'SQL prepare failed: ' . $mysqli->error]);
    $mysqli->close();
    exit();
}

// Execute the statement
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(['error' => 'SQL execution failed: ' . $stmt->error]);
    $stmt->close();
    $mysqli->close();
    exit();
}

// Get the result set
$result = $stmt->get_result();

// 4. Fetch Data and Build Array
$items = [];
if ($result->num_rows > 0) {
    // Loop through each row of the result set
    while ($row = $result->fetch_assoc()) {
        // Add the value of the 'Item' column to the $items array
        // We use $row['Item'] to get the specific string value
        $items[] = $row['Item'];
    }
}

// 5. Output JSON
// The json_encode function will automatically convert the $items array
// into the requested simple JSON array format: ["Item1", "Item2", ...]
echo json_encode($items, JSON_PRETTY_PRINT); // JSON_PRETTY_PRINT is optional for readable output

// 6. Close Connection
$stmt->close();
$mysqli->close();

?>