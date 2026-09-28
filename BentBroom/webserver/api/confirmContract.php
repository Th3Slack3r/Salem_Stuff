<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php'; // contains $mysqli connection
header('Content-Type: application/json');

// --- Path to wiki cache and embed functions ---
define('WIKI_CACHE_FILE', __DIR__ . '/wiki_cache.json');

// --- Load wiki cache ---
function loadWikiCache() {
    if (file_exists(WIKI_CACHE_FILE)) {
        $data = file_get_contents(WIKI_CACHE_FILE);
        return json_decode($data, true) ?: [];
    }
    return [];
}

// --- Save wiki cache ---
function saveWikiCache($cache) {
    file_put_contents(WIKI_CACHE_FILE, json_encode($cache));
}

// --- Fetch wiki image for item ---
function getWikiItemImage($itemName) {
    static $cache = null;
    if ($cache === null) $cache = loadWikiCache();
    if (isset($cache[$itemName])) return $cache[$itemName];

    $smallWords = ["a","an","the","and","but","or","for","nor","on","at","to","from","by","with","in","of","is","are"];
    $words = explode(" ", strtolower($itemName));
    $formattedWords = [];
    foreach ($words as $i => $word) {
        $formattedWords[] = ($i === 0 || !in_array($word, $smallWords)) ? ucfirst($word) : $word;
    }
    $wikiName = implode("_", $formattedWords);

    $urls = [
        "https://salemthegame.wiki/wiki/{$wikiName}",
        "https://salemthegame.wiki/page/{$wikiName}"
    ];

    $html = null;
    foreach ($urls as $url) {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_USERAGENT, 'SalemItemBot/1.0');
        curl_setopt($ch, CURLOPT_TIMEOUT, 10);
        $resp = curl_exec($ch);
        curl_close($ch);
        if ($resp && strpos($resp, 'Salem') !== false) {
            $html = $resp;
            break;
        }
    }

    if (!$html) {
        $cache[$itemName] = null;
        saveWikiCache($cache);
        return null;
    }

    $doc = new DOMDocument();
    @$doc->loadHTML($html);
    $xpath = new DOMXPath($doc);

    $queries = [
        '//table[contains(@class,"infobox")]//img',
        '//div[@id="mw-content-text"]//img'
    ];

    $imageUrl = null;
    foreach ($queries as $q) {
        $node = $xpath->query($q)->item(0);
        if ($node) {
            $srcset = $node->getAttribute('srcset');
            if ($srcset) {
                $parts = explode(',', $srcset);
                $last = trim(end($parts));
                $src = explode(' ', $last)[0];
            } else {
                $src = $node->getAttribute('src');
            }

            if ($src) {
                if (strpos($src, '//') === 0) $src = 'https:' . $src;
                elseif (!preg_match('/^https?:\/\//', $src)) $src = 'https://salemthegame.wiki' . $src;
                $imageUrl = $src;
                break;
            }
        }
    }

    $cache[$itemName] = $imageUrl;
    saveWikiCache($cache);
    return $imageUrl;
}

// --- Send Discord embed ---
function sendDiscordEmbed($webhookUrl, $vendorName, $itemName, $quantity, $price, $isNew = true, $imageUrl = null) {
    $title = ($isNew ? "Contract Complete - " : "Updated Listing - ") . $itemName;
    $description = "x{$quantity} @ {$price} Silver\nListed by {$vendorName}";

    $embed = [
        "title" => $title,
        "description" => $description,
        "color" => $isNew ? 3066993 : 16753920
    ];

    if ($imageUrl) {
        $embed["thumbnail"] = ["url" => $imageUrl];
    }

    $data = ["username" => "Marketplace Bot", "embeds" => [$embed]];

    $ch = curl_init($webhookUrl);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_exec($ch);
    curl_close($ch);
}

// --- Discord webhook URL ---
$discordWebhook = "https://discord.com/api/webhooks/1438290922260660246/hrRaODg9scCSooZhuTCWXa6DiHg5iaSTuvCcZpOZ0GAt4CdMw8O8NBv_lMfi7PFR3OMw";

// --- Validate POST ---
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

// --- Determine column to update ---
$column_to_update = '';
if ($role === 'buyer') {
    $column_to_update = 'buyer_confirmed';
} elseif ($role === 'seller') {
    $column_to_update = 'vendor_confirmed';
} else {
    echo json_encode(['error' => 'Invalid role specified.']);
    exit;
}

// --- Check contract exists and is active ---
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

// --- Update confirmation ---
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

// --- Check for completion ---
$message = "Contract #$contract_id confirmed as $role.";

$status_check_sql = "SELECT buyer_confirmed, vendor_confirmed FROM Contracts WHERE id = ?";
$status_check_stmt = $mysqli->prepare($status_check_sql);
$status_check_stmt->bind_param('i', $contract_id);
$status_check_stmt->execute();
$status_result = $status_check_stmt->get_result();
$contract_data = $status_result->fetch_assoc();
$status_check_stmt->close();

if ($contract_data && $contract_data['buyer_confirmed'] == 1 && $contract_data['vendor_confirmed'] == 1) {
    // Mark contract as completed
    $complete_stmt = $mysqli->prepare("UPDATE Contracts SET status = 'completed' WHERE id = ?");
    $complete_stmt->bind_param('i', $contract_id);

    if ($complete_stmt->execute()) {
        $message .= " Contract status changed to 'completed'.";

        // --- Fetch contract details ---
        $details_stmt = $mysqli->prepare("
            SELECT c.item_name, c.quantity, c.price, u_vendor.username AS vendor_name, u_buyer.username AS buyer_name
            FROM Contracts c
            JOIN Users u_vendor ON c.vendor_id = u_vendor.id
            JOIN Users u_buyer ON c.buyer_id = u_buyer.id
            WHERE c.id = ?
        ");
        $details_stmt->bind_param('i', $contract_id);
        $details_stmt->execute();
        $details_result = $details_stmt->get_result();
        $contract_info = $details_result->fetch_assoc();
        $details_stmt->close();

        if ($contract_info) {
            $itemName = $contract_info['item_name'];
            $quantity = $contract_info['quantity'];
            $price = $contract_info['price'];
            $vendorName = $contract_info['vendor_name'];

            // --- Fetch wiki image ---
            $imageUrl = getWikiItemImage($itemName);

            // --- Update CurrentItems table ---
            if ($imageUrl) {
                $updateImage = $mysqli->prepare("UPDATE CurrentItems SET Image = ?, UpdatedAt = NOW() WHERE Item = ?");
                $updateImage->bind_param('ss', $imageUrl, $itemName);
                $updateImage->execute();
            }

            // --- Send Discord embed ---
            sendDiscordEmbed($discordWebhook, $vendorName, $itemName, $quantity, $price, true, $imageUrl);
        }
    } else {
        error_log("Failed to set contract #$contract_id status to completed: " . $mysqli->error);
        $message .= " WARNING: Failed to finalize contract status.";
    }
    $complete_stmt->close();
}

// --- Final response ---
echo json_encode(['success' => true, 'message' => $message]);
$mysqli->close();
?>
