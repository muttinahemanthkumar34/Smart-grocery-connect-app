<?php
/**
 * FCM Push Notification Helper - V1 API
 * 
 * Uses Firebase Cloud Messaging HTTP v1 API with Service Account authentication.
 * 
 * SETUP:
 * 1. Download service account JSON from Firebase Console -> Project Settings -> Service accounts
 * 2. Rename to 'firebase-service-account.json' and place in same folder as this file
 */

class FCMNotification {
    
    // Path to your service account JSON file
    private static $SERVICE_ACCOUNT_FILE = __DIR__ . '/firebase-service-account.json';
    
    // FCM V1 API endpoint (project ID will be inserted)
    private static $FCM_V1_URL = 'https://fcm.googleapis.com/v1/projects/{PROJECT_ID}/messages:send';
    
    // Cache for access token
    private static $accessToken = null;
    private static $tokenExpiry = 0;
    
    /**
     * Get access token using service account
     */
    private static function getAccessToken() {
        // Return cached token if still valid
        if (self::$accessToken && time() < self::$tokenExpiry - 60) {
            return self::$accessToken;
        }
        
        // Read service account file
        if (!file_exists(self::$SERVICE_ACCOUNT_FILE)) {
            error_log("FCM Error: Service account file not found: " . self::$SERVICE_ACCOUNT_FILE);
            return null;
        }
        
        $serviceAccount = json_decode(file_get_contents(self::$SERVICE_ACCOUNT_FILE), true);
        if (!$serviceAccount) {
            error_log("FCM Error: Invalid service account JSON");
            return null;
        }
        
        // Create JWT
        $now = time();
        $header = [
            'alg' => 'RS256',
            'typ' => 'JWT'
        ];
        
        $claims = [
            'iss' => $serviceAccount['client_email'],
            'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
            'aud' => 'https://oauth2.googleapis.com/token',
            'iat' => $now,
            'exp' => $now + 3600
        ];
        
        $headerEncoded = self::base64UrlEncode(json_encode($header));
        $claimsEncoded = self::base64UrlEncode(json_encode($claims));
        
        // Sign with private key
        $signature = '';
        openssl_sign(
            $headerEncoded . '.' . $claimsEncoded,
            $signature,
            $serviceAccount['private_key'],
            'SHA256'
        );
        
        $jwt = $headerEncoded . '.' . $claimsEncoded . '.' . self::base64UrlEncode($signature);
        
        // Exchange JWT for access token
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        
        $response = curl_exec($ch);
        curl_close($ch);
        
        $data = json_decode($response, true);
        if (isset($data['access_token'])) {
            self::$accessToken = $data['access_token'];
            self::$tokenExpiry = $now + $data['expires_in'];
            return self::$accessToken;
        }
        
        error_log("FCM Error: Failed to get access token: " . $response);
        return null;
    }
    
    /**
     * Base64 URL encode
     */
    private static function base64UrlEncode($data) {
        return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
    }
    
    /**
     * Get project ID from service account
     */
    private static function getProjectId() {
        if (!file_exists(self::$SERVICE_ACCOUNT_FILE)) {
            return null;
        }
        $serviceAccount = json_decode(file_get_contents(self::$SERVICE_ACCOUNT_FILE), true);
        return $serviceAccount['project_id'] ?? null;
    }
    
    /**
     * Send notification to a single device
     */
    public static function sendToDevice($fcm_token, $title, $message, $data = []) {
        return self::sendOrderNotification($fcm_token, $title, $message, 0);
    }
    
    /**
     * Send order notification
     */
    public static function sendOrderNotification($fcm_token, $title, $message, $order_id) {
        $accessToken = self::getAccessToken();
        if (!$accessToken) {
            return ['success' => false, 'error' => 'Failed to get access token'];
        }
        
        $projectId = self::getProjectId();
        if (!$projectId) {
            return ['success' => false, 'error' => 'Failed to get project ID'];
        }
        
        $url = str_replace('{PROJECT_ID}', $projectId, self::$FCM_V1_URL);
        
        // Build the message payload
        $payload = [
            'message' => [
                'token' => $fcm_token,
                'data' => [
                    'title' => $title,
                    'message' => $message,
                    'type' => 'order',
                    'order_id' => strval($order_id)
                ],
                'android' => [
                    'priority' => 'high'
                ]
            ]
        ];
        
        $headers = [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ];
        
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        curl_close($ch);
        
        if ($error) {
            error_log("FCM Error: " . $error);
            return ['success' => false, 'error' => $error];
        }
        
        error_log("FCM Response (HTTP $httpCode): " . $response);
        
        $responseData = json_decode($response, true);
        if ($httpCode == 200) {
            return ['success' => true, 'response' => $responseData];
        } else {
            return ['success' => false, 'error' => $responseData['error']['message'] ?? 'Unknown error'];
        }
    }
    
    /**
     * Get FCM tokens for a user
     */
    public static function getUserTokens($conn, $user_id) {
        $tokens = [];
        error_log("getUserTokens called with user_id: " . var_export($user_id, true));
        $stmt = $conn->prepare("SELECT fcm_token FROM fcm_tokens WHERE user_id = ? AND user_type = 'user'");
        $stmt->execute([$user_id]);
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $tokens[] = $row['fcm_token'];
            error_log("Found user token: " . substr($row['fcm_token'], 0, 20) . "...");
        }
        if (empty($tokens)) {
            error_log("No tokens found for user_id: $user_id");
        }
        return $tokens;
    }
    
    /**
     * Get FCM tokens for a shop/admin
     */
    public static function getShopTokens($conn, $shop_id) {
        $tokens = [];
        $stmt = $conn->prepare("SELECT fcm_token FROM fcm_tokens WHERE shop_id = ? AND user_type = 'admin'");
        $stmt->execute([$shop_id]);
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $tokens[] = $row['fcm_token'];
        }
        return $tokens;
    }
}
?>
