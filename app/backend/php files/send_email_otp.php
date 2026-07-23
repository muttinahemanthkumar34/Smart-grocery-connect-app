<?php
// Suppress warnings from being output (they break JSON parsing)
error_reporting(0);
ini_set('display_errors', 0);

// Set timezone to India Standard Time
date_default_timezone_set('Asia/Kolkata');

header("Content-Type: application/json");
require_once("config/db.php");

// PHPMailer - Include it via composer autoload or direct include
// If you installed via composer: require_once("vendor/autoload.php");
// If you downloaded manually, include the files directly:
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

// Try to load PHPMailer from composer
if (file_exists(__DIR__ . '/vendor/autoload.php')) {
    require_once(__DIR__ . '/vendor/autoload.php');
} else {
    // Fallback - Return success with debug OTP if PHPMailer not installed
    $USE_PHPMAILER = false;
}

// ==================== GMAIL CONFIGURATION ====================
$GMAIL_EMAIL = "siddarthareddy67@gmail.com";
$GMAIL_APP_PASSWORD = "vlnxqunrfkebbwhd";  // App password without spaces
// =============================================================
// =============================================================

// Validate required fields
if (empty($_POST['email']) || empty($_POST['role'])) {
    echo json_encode([
        "status" => false,
        "message" => "Email and role are required"
    ]);
    exit;
}

$email = trim($_POST['email']);
$role = strtoupper(trim($_POST['role']));

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid email format"
    ]);
    exit;
}

// Validate role
if ($role !== 'USER' && $role !== 'ADMIN') {
    echo json_encode([
        "status" => false,
        "message" => "Invalid role specified"
    ]);
    exit;
}

try {
    // Check if email exists in users table with the specified role
    $stmt = $conn->prepare(
        "SELECT user_id, full_name, email FROM users WHERE email = ? AND role = ?"
    );
    $stmt->execute([$email, $role]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode([
            "status" => false,
            "message" => "Email not registered as " . $role
        ]);
        exit;
    }

    // Generate 6-digit OTP
    $otp = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
    $expiry = date('Y-m-d H:i:s', strtotime('+10 minutes'));

    // Store OTP in database (create table if not exists)
    $conn->exec("CREATE TABLE IF NOT EXISTS password_reset_otp (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        otp VARCHAR(6) NOT NULL,
        expiry DATETIME NOT NULL,
        used TINYINT DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )");

    // Delete any existing OTPs for this user
    $deleteStmt = $conn->prepare("DELETE FROM password_reset_otp WHERE user_id = ?");
    $deleteStmt->execute([$user['user_id']]);

    // Insert new OTP
    $insertStmt = $conn->prepare(
        "INSERT INTO password_reset_otp (user_id, otp, expiry) VALUES (?, ?, ?)"
    );
    $insertStmt->execute([$user['user_id'], $otp, $expiry]);

    // Email content
    $emailBody = "
    <html>
    <head>
        <style>
            body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }
            .container { max-width: 500px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 10px; }
            .header { text-align: center; color: #10B981; }
            .otp-box { 
                font-size: 36px; 
                font-weight: bold; 
                color: #10B981; 
                letter-spacing: 10px;
                padding: 20px;
                background: #f0fdf4;
                border-radius: 8px;
                text-align: center;
                margin: 25px 0;
                border: 2px dashed #10B981;
            }
            .footer { color: #888; font-size: 12px; text-align: center; margin-top: 30px; }
            .warning { color: #666; font-size: 13px; margin-top: 20px; }
        </style>
    </head>
    <body>
        <div class='container'>
            <h2 class='header'>🔐 Password Reset</h2>
            <p>Hello <strong>" . htmlspecialchars($user['full_name']) . "</strong>,</p>
            <p>You requested to reset your password for GroceryConnect. Use the verification code below:</p>
            <div class='otp-box'>" . $otp . "</div>
            <p>⏰ This code will expire in <strong>10 minutes</strong>.</p>
            <p class='warning'>If you didn't request this, please ignore this email. Your password will remain unchanged.</p>
            <div class='footer'>
                GroceryConnect App &copy; " . date('Y') . "
            </div>
        </div>
    </body>
    </html>
    ";

    $mailSent = false;
    $errorMsg = "";

    // Try sending email with PHPMailer
    if (isset($USE_PHPMAILER) && $USE_PHPMAILER === false) {
        // PHPMailer not installed, skip email
        $errorMsg = "PHPMailer not installed";
    } else if (class_exists('PHPMailer\PHPMailer\PHPMailer')) {
        try {
            $mail = new PHPMailer(true);
            
            // SMTP Configuration
            $mail->isSMTP();
            $mail->Host = 'smtp.gmail.com';
            $mail->SMTPAuth = true;
            $mail->Username = $GMAIL_EMAIL;
            $mail->Password = $GMAIL_APP_PASSWORD;
            $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
            $mail->Port = 587;
            
            // Email settings
            $mail->setFrom($GMAIL_EMAIL, 'GroceryConnect');
            $mail->addAddress($email, $user['full_name']);
            $mail->isHTML(true);
            $mail->Subject = 'GroceryConnect - Password Reset OTP';
            $mail->Body = $emailBody;
            $mail->AltBody = "Your OTP is: " . $otp . " (Valid for 10 minutes)";
            
            $mail->send();
            $mailSent = true;
        } catch (Exception $e) {
            $errorMsg = $mail->ErrorInfo;
        }
    } else {
        $errorMsg = "PHPMailer class not found";
    }

    if ($mailSent) {
        echo json_encode([
            "status" => true,
            "message" => "OTP sent to your email",
            "user_id" => $user['user_id'],
            "email" => $email
        ]);
    } else {
        // Email failed but OTP is in database - return for testing
        echo json_encode([
            "status" => true,
            "message" => "OTP generated (email sending failed: " . $errorMsg . ")",
            "user_id" => $user['user_id'],
            "email" => $email,
            "debug_otp" => $otp  // Remove in production!
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}
