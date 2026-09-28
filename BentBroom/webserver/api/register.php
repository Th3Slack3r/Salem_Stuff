<?php
require 'db.php'; // Contains the $mysqli connection object

header('Content-Type: application/json');

// --- 1. Input Validation and Sanitization ---
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(array('error' => 'Method not allowed.'));
    exit;
}

// Check for database connection failure defined in db.php
if (!isset($mysqli) || $mysqli->connect_error) {
    http_response_code(500);
    echo json_encode(array('error' => 'Database connection failed. Check db.php.'));
    exit;
}

// Get and sanitize input
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';
$email    = $_POST['email'] ?? '';
$hs       = $_POST['hs'] ?? '';

if (empty($username) || empty($password) || empty($email) || empty($hs)) {
    http_response_code(400);
    echo json_encode(array('error' => 'Missing one or more required fields.'));
    exit;
}

// --- 2. Account Creation Logic (using $mysqli) ---
try {
    // a. Check if username already exists
    $stmt = $mysqli->prepare("SELECT COUNT(*) FROM users WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->bind_result($count);
    $stmt->fetch();
    $stmt->close();
    
    if ($count > 0) {
        http_response_code(409);
        echo json_encode(array('error' => 'Username already exists.'));
        exit;
    }

    // b. Hash the password
    $password_hash = password_hash($password, PASSWORD_DEFAULT);
    
    // c. Generate a unique API Key (Token)
    $api_key = bin2hex(random_bytes(32));

    // d. Insert the new user into the database
    $sql = "INSERT INTO users (username, password_hash, email, homeserver, api_key) VALUES (?, ?, ?, ?, ?)";
    $stmt = $mysqli->prepare($sql);
    $stmt->bind_param("sssss", $username, $password_hash, $email, $hs, $api_key);
    
    if ($stmt->execute()) {
        // e. Return success with the API key
        http_response_code(201);
        echo json_encode(array('success' => true, 'key' => $api_key));
    } else {
        http_response_code(500);
        echo json_encode(array('error' => 'Failed to insert user: ' . $stmt->error));
    }

    $stmt->close();

} catch (Exception $e) {
    error_log("Registration Error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(array('error' => 'A critical server error occurred.'));
} finally {
    // Close the connection established in db.php
    $mysqli->close(); 
}
?>