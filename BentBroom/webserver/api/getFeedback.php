<?php
require 'validate_key.php';
require 'db.php';
header('Content-Type: application/json');
$to_user_id = isset($_GET['to_user_id']) ? intval($_GET['to_user_id']) : 0;

$stmt = $mysqli->prepare("SELECT id, contract_id, from_user_id, to_user_id, rating, comment, created_at FROM Feedback WHERE to_user_id = ?");


