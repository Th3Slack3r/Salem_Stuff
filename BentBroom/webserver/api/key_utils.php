<?php
// key_utils.php

// Function to generate a secure, unique key based on username and email
// Using SHA-256 hash as requested.
function generate_user_key($username, $email) {
    // Combine credentials with a server-side salt (important for security)
    $server_salt = "YOUR_STRONG_RANDOM_SECRET_SALT_HERE_12345"; 
    $data = $username . $email . $server_salt . time(); // Adding time() ensures uniqueness if called rapidly

    // Use SHA-256 for a fixed-length cryptographic hash
    return hash('sha256', $data);
}
?>