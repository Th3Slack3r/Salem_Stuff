<?php
$host     = '2.56.246.128';
$port     = '3306';
$user     = 'u14751_gGrr4LahO7';
$password = 'yl6q7+GcCY6+PWILdf^W0vsF';
$dbname   = 's14751_BentBroom';

// Create connection
$mysqli = new mysqli($host, $user, $password, $dbname, (int)$port);

// Check connection
if ($mysqli->connect_errno) {
    header('Content-Type: application/json');
    echo json_encode([
        'error' => 'Database connection failed',
        'details' => $mysqli->connect_error
    ]);
    exit;
}

// Optional: set charset
$mysqli->set_charset('utf8mb4');
?>
