<?php
// feedback.php
header('Content-Type: application/json');

$host = 'localhost';
$db   = 'plugin_db';
$user = 'db_user';
$pass = 'db_pass';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (Exception $e) {
    echo json_encode(['success'=>false,'error'=>'DB connection failed']);
    exit;
}

if(!isset($_GET['contract_id'], $_GET['from_user_id'], $_GET['to_user_id'], $_GET['rating'])){
    echo json_encode(['success'=>false,'error'=>'Missing parameters']);
    exit;
}

$contract_id = intval($_GET['contract_id']);
$from_user_id = intval($_GET['from_user_id']);
$to_user_id = intval($_GET['to_user_id']);
$rating = intval($_GET['rating']);
$comment = isset($_GET['comment']) ? $_GET['comment'] : '';

// Check contract exists
$stmt = $pdo->prepare("SELECT * FROM contracts WHERE id=?");
$stmt->execute([$contract_id]);
$contract = $stmt->fetch();

if(!$contract){
    echo json_encode(['success'=>false,'error'=>'Contract not found']);
    exit;
}

// Insert feedback
$stmt = $pdo->prepare("INSERT INTO feedback (contract_id, from_user_id, to_user_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, NOW())");
$stmt->execute([$contract_id, $from_user_id, $to_user_id, $rating, $comment]);

echo json_encode(['success'=>true]);
