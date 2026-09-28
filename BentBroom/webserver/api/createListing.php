<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

require 'validate_key.php';
require 'db.php';
header('Content-Type: application/json');

// --- Path to cache file ---
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

// --- Discord embed function (2-line description, no timestamp) ---
function sendDiscordEmbed($webhookUrl, $vendorName, $itemName, $quantity, $price, $isNew = true, $imageUrl = null) {
    $title = ($isNew ? "New Listing - " : "Updated Listing - ") . $itemName;

    // Description: quantity/price and vendor
    $description = "x{$quantity} @ {$price} Silver\nListed by {$vendorName}";

    $embed = [
        "title" => $title,
        "description" => $description,
        "color" => $isNew ? 3066993 : 16753920 // green for new, orange for update
    ];

    if ($imageUrl) {
        $embed["thumbnail"] = ["url" => $imageUrl]; // image on left
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

// --- Wiki image fetcher with caching ---
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

// --- Discord webhook URL ---
$discordWebhook = "https://discord.com/api/webhooks/1438290922260660246/hrRaODg9scCSooZhuTCWXa6DiHg5iaSTuvCcZpOZ0GAt4CdMw8O8NBv_lMfi7PFR3OMw";

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

// --- Lookup proper item name ---
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

// --- Get vendor name ---
$stmt = $mysqli->prepare("SELECT username FROM Users WHERE id = ?");
$stmt->bind_param('i', $user_id);
$stmt->execute();
$userResult = $stmt->get_result();
$vendorName = ($userResult->num_rows > 0) ? $userResult->fetch_assoc()['username'] : "Unknown Vendor";

// --- Get item image ---
$imageUrl = getWikiItemImage($properItemName);

// --- Update CurrentItems.Image if image found ---
if ($imageUrl) {
    $updateImage = $mysqli->prepare("UPDATE CurrentItems SET Image = ?, UpdatedAt = NOW() WHERE Item = ?");
    $updateImage->bind_param('ss', $imageUrl, $properItemName);
    $updateImage->execute();
}

// --- Check existing listing ---
$stmt = $mysqli->prepare("SELECT id, quantity, price FROM Listings WHERE vendor_id = ? AND item_name = ?");
$stmt->bind_param('is', $user_id, $properItemName);
$stmt->execute();
$existingResult = $stmt->get_result();

if ($existingResult->num_rows > 0) {
    $row = $existingResult->fetch_assoc();
    $listing_id = $row['id'];
    $newQuantity = $row['quantity'] + $quantity;

    $update = $mysqli->prepare("UPDATE Listings SET quantity = ?, price = ? WHERE id = ?");
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
        sendDiscordEmbed($discordWebhook, $vendorName, $properItemName, $newQuantity, $price, false, $imageUrl);
    } else {
        echo json_encode(['error' => 'Failed to update listing: ' . $update->error]);
    }
} else {
    $insert = $mysqli->prepare("INSERT INTO Listings (vendor_id, item_name, quantity, price, on_hold, created_at) VALUES (?, ?, ?, ?, 0, NOW())");
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
        sendDiscordEmbed($discordWebhook, $vendorName, $properItemName, $quantity, $price, true, $imageUrl);
    } else {
        echo json_encode(['error' => 'Failed to create listing: ' . $insert->error]);
    }
}
?>
