-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 03, 2026 at 04:16 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `grocery_connect_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin_notifications`
--

CREATE TABLE `admin_notifications` (
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `message` varchar(255) NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `product_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin_notifications`
--

INSERT INTO `admin_notifications` (`id`, `shop_id`, `order_id`, `title`, `message`, `is_read`, `created_at`, `product_id`) VALUES
(9, 1, 59, 'New Order Received!', 'Order #GRC-0059 has been placed. Total: ₹1,200.00', 1, '2025-12-30 04:50:56', NULL),
(10, 1, NULL, '⚠️ Out of Stock!', 'rice bag is now OUT OF STOCK. Please restock immediately.', 1, '2025-12-30 04:50:56', 2),
(11, 1, 60, 'New Order Received!', 'Order #GRC-0060 has been placed. Total: ₹160.00', 1, '2025-12-30 04:51:23', NULL),
(12, 1, 61, 'New Order Received!', 'Order #GRC-0061 has been placed. Total: ₹180.00', 1, '2025-12-30 04:59:24', NULL),
(13, 5, 62, 'New Order Received!', 'Order #GRC-0062 has been placed. Total: ₹10.00', 1, '2025-12-30 05:24:16', NULL),
(14, 6, 63, 'New Order Received!', 'Order #GRC-0063 has been placed. Total: ₹10.00', 0, '2026-01-02 04:53:03', NULL),
(15, 6, 64, 'New Order Received!', 'Order #GRC-0064 has been placed. Total: ₹10.00', 1, '2026-01-02 04:53:29', NULL),
(16, 6, 65, 'New Order Received!', 'Order #GRC-0065 has been placed. Total: ₹10.00', 1, '2026-01-02 05:01:46', NULL),
(17, 6, 66, 'New Order Received!', 'Order #GRC-0066 has been placed. Total: ₹10.00', 0, '2026-01-02 05:20:37', NULL),
(18, 6, 67, 'New Order Received!', 'Order #GRC-0067 has been placed. Total: ₹10.00', 0, '2026-01-02 05:21:16', NULL),
(19, 6, 68, 'New Order Received!', 'Order #GRC-0068 has been placed. Total: ₹10.00', 0, '2026-01-02 05:22:28', NULL),
(20, 6, 69, 'New Order Received!', 'Order #GRC-0069 has been placed. Total: ₹10.00', 0, '2026-01-02 05:34:00', NULL),
(21, 6, 70, 'New Order Received!', 'Order #GRC-0070 has been placed. Total: ₹10.00', 0, '2026-01-02 05:34:21', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `ai_requests`
--

CREATE TABLE `ai_requests` (
  `request_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `query` varchar(255) NOT NULL,
  `response` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `fcm_tokens`
--

CREATE TABLE `fcm_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `user_type` enum('user','admin') NOT NULL,
  `fcm_token` varchar(255) NOT NULL,
  `device_type` varchar(50) DEFAULT 'android',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fcm_tokens`
--

INSERT INTO `fcm_tokens` (`id`, `user_id`, `shop_id`, `user_type`, `fcm_token`, `device_type`, `created_at`, `updated_at`) VALUES
(1, 11, 6, 'admin', 'dlfdjDIISLqjT51_3DQqJx:APA91bEM1FB-i9hrWmzrfV0to48XLkrN8UJNr5cx8cJDKI7d_HUc5jDifcbsoeMZYn9DhhrDFkwXb5AUvPEVrk216Tpi0nH1ERf2139InTvtug0BMhFCce4', 'android', '2026-01-02 05:00:02', '2026-01-02 05:33:49'),
(2, 3, NULL, 'user', 'eDL3VZAhSZmGEW4XNPjgBL:APA91bGR3xjR1GYJLzeQ6agVdyO_66ni0xMVRX_AnutWGVocTtwLNVmmArr6WcDe6Iz7YATR7_3gSVr6TfThb5kEkievyEz-Uq4dyMUHOScTyCa8mXx_cYI', 'android', '2026-01-02 05:00:40', '2026-01-02 05:33:45');

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
  `feedback_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `rating` int(11) DEFAULT NULL CHECK (`rating` between 1 and 5),
  `comments` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `feedback`
--

INSERT INTO `feedback` (`feedback_id`, `order_id`, `user_id`, `shop_id`, `rating`, `comments`, `created_at`) VALUES
(2, 1, 1, 1, 3, NULL, '2025-12-26 10:35:59'),
(3, 2, 1, 1, 3, NULL, '2025-12-26 10:45:31'),
(4, 33, 3, 1, 5, 'good service', '2025-12-27 07:38:24'),
(5, 34, 3, 1, 4, NULL, '2025-12-27 08:42:42'),
(6, 31, 3, 1, 1, NULL, '2025-12-28 14:44:36'),
(7, 60, 3, 1, 4, 'Good', '2025-12-30 04:52:21');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `message` varchar(255) NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `order_id`, `title`, `message`, `is_read`, `created_at`) VALUES
(30, 3, 64, 'Order Being Packed', 'Your order #64 is now being packed.', 0, '2026-01-02 04:54:00'),
(31, 3, 64, 'Order Ready!', 'Your order #64 is ready for pickup/delivery.', 0, '2026-01-02 04:54:08'),
(32, 3, 65, 'Order Being Packed', 'Your order #65 is now being packed.', 0, '2026-01-02 05:02:18'),
(33, 3, 65, 'Order Ready!', 'Your order #65 is ready for pickup/delivery.', 0, '2026-01-02 05:02:32'),
(34, 3, 65, 'Out for Delivery', 'Your order #65 is out for delivery.', 0, '2026-01-02 05:06:15'),
(35, 3, 65, 'Order Cancelled', 'Your order #65 has been cancelled.', 0, '2026-01-02 05:06:34'),
(36, 3, 63, 'Order Being Packed', 'Your order #63 is now being packed.', 0, '2026-01-02 05:19:24'),
(37, 3, 63, 'Order Ready!', 'Your order #63 is ready for pickup/delivery.', 0, '2026-01-02 05:19:39');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `order_status` enum('PLACED','PACKING','READY','DELIVERED','CANCELLED') DEFAULT 'PLACED',
  `order_type` enum('PICKUP','DELIVERY') NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `delivered_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `user_id`, `shop_id`, `order_status`, `order_type`, `total_amount`, `created_at`, `delivered_at`) VALUES
(1, 1, 1, 'DELIVERED', 'PICKUP', 2450.00, '2025-12-23 13:39:28', NULL),
(2, 1, 1, 'DELIVERED', 'PICKUP', 2450.00, '2025-12-23 13:43:30', NULL),
(3, 3, 2, 'PLACED', 'PICKUP', 1110.00, '2025-12-25 12:05:29', NULL),
(4, 3, 2, 'PLACED', 'DELIVERY', 1365.00, '2025-12-25 12:07:31', NULL),
(5, 1, 2, 'PLACED', 'DELIVERY', 1970.00, '2025-12-25 15:36:04', NULL),
(6, 1, 1, 'PLACED', 'PICKUP', 1560.00, '2025-12-25 15:36:52', NULL),
(7, 3, 1, 'PLACED', 'PICKUP', 1400.00, '2025-12-25 15:55:41', NULL),
(8, 3, 1, '', 'DELIVERY', 2690.00, '2025-12-25 16:02:58', NULL),
(9, 3, 1, 'PLACED', 'PICKUP', 3720.00, '2025-12-25 16:03:46', NULL),
(10, 3, 1, 'DELIVERED', 'DELIVERY', 2730.00, '2025-12-26 03:13:27', '2025-12-27 04:52:59'),
(11, 3, 1, 'DELIVERED', 'PICKUP', 120.00, '2025-12-26 03:26:05', NULL),
(12, 3, 1, 'PLACED', 'DELIVERY', 1040.00, '2025-12-26 03:29:23', NULL),
(13, 3, 2, 'PLACED', 'DELIVERY', 1610.00, '2025-12-26 03:32:33', NULL),
(14, 3, 1, 'DELIVERED', 'DELIVERY', 120.00, '2025-12-26 03:46:43', NULL),
(15, 3, 1, 'DELIVERED', 'PICKUP', 3700.00, '2025-12-26 03:51:19', NULL),
(16, 3, 1, 'DELIVERED', 'DELIVERY', 3600.00, '2025-12-26 03:55:13', NULL),
(17, 3, 1, 'DELIVERED', 'PICKUP', 1250.00, '2025-12-26 04:00:21', NULL),
(18, 3, 1, 'DELIVERED', 'PICKUP', 80.00, '2025-12-26 04:00:56', NULL),
(19, 3, 1, 'CANCELLED', 'PICKUP', 80.00, '2025-12-26 04:08:00', NULL),
(20, 3, 2, 'PLACED', 'PICKUP', 780.00, '2025-12-26 04:08:12', NULL),
(21, 3, 1, 'CANCELLED', 'DELIVERY', 2500.00, '2025-12-26 04:10:30', NULL),
(22, 1, 1, 'CANCELLED', 'DELIVERY', 40.00, '2025-12-26 04:27:52', NULL),
(23, 1, 1, 'CANCELLED', 'DELIVERY', 1600.00, '2025-12-26 10:17:07', NULL),
(26, 3, 1, 'CANCELLED', 'PICKUP', 1440.00, '2025-12-26 11:30:56', NULL),
(27, 3, 2, 'CANCELLED', 'PICKUP', 570.00, '2025-12-27 03:18:12', NULL),
(28, 3, 1, 'CANCELLED', 'DELIVERY', 120.00, '2025-12-27 04:18:28', NULL),
(29, 3, 1, 'DELIVERED', 'DELIVERY', 960.00, '2025-12-27 04:37:07', NULL),
(30, 3, 1, 'DELIVERED', 'PICKUP', 180.00, '2025-12-27 04:52:22', '2025-12-27 04:52:41'),
(31, 3, 1, 'DELIVERED', 'DELIVERY', 255.00, '2025-12-27 07:02:48', '2025-12-27 10:04:32'),
(32, 3, 1, 'CANCELLED', 'DELIVERY', 240.00, '2025-12-27 07:29:13', NULL),
(33, 3, 1, 'DELIVERED', 'DELIVERY', 230.00, '2025-12-27 07:37:41', '2025-12-27 07:38:02'),
(34, 3, 1, 'DELIVERED', 'PICKUP', 2400.00, '2025-12-27 08:41:36', '2025-12-27 08:42:09'),
(35, 3, 1, 'DELIVERED', 'PICKUP', 80.00, '2025-12-27 08:51:17', '2025-12-27 10:04:24'),
(36, 3, 1, 'DELIVERED', 'PICKUP', 80.00, '2025-12-27 09:00:41', '2025-12-27 09:35:53'),
(37, 3, 1, 'DELIVERED', 'PICKUP', 140.00, '2025-12-27 09:03:12', '2025-12-27 10:00:51'),
(38, 3, 5, 'DELIVERED', 'DELIVERY', 10.00, '2025-12-27 09:22:30', '2025-12-27 09:44:41'),
(39, 3, 5, 'DELIVERED', 'DELIVERY', 10.00, '2025-12-27 09:44:59', '2025-12-27 09:47:37'),
(40, 3, 5, 'DELIVERED', 'DELIVERY', 10.00, '2025-12-27 09:48:12', '2025-12-27 09:48:59'),
(41, 3, 5, 'DELIVERED', 'PICKUP', 10.00, '2025-12-27 09:48:25', '2025-12-27 09:48:49'),
(42, 3, 5, 'DELIVERED', 'PICKUP', 10.00, '2025-12-27 09:49:43', '2025-12-27 09:50:12'),
(43, 3, 5, 'CANCELLED', 'DELIVERY', 10.00, '2025-12-27 09:55:04', NULL),
(44, 3, 1, 'CANCELLED', 'PICKUP', 80.00, '2025-12-27 10:47:09', NULL),
(45, 3, 1, 'DELIVERED', 'PICKUP', 50.00, '2025-12-28 10:55:31', '2025-12-28 10:56:08'),
(46, 3, 5, 'DELIVERED', 'DELIVERY', 25.00, '2025-12-28 11:01:21', '2025-12-28 11:02:08'),
(47, 3, 5, 'DELIVERED', 'PICKUP', 20.00, '2025-12-28 11:01:35', '2025-12-28 11:01:59'),
(48, 3, 5, 'CANCELLED', 'DELIVERY', 20.00, '2025-12-28 11:09:19', NULL),
(49, 10, 1, 'PLACED', 'DELIVERY', 1250.00, '2025-12-28 14:38:28', NULL),
(50, 3, 1, '', 'DELIVERY', 410.00, '2025-12-29 03:17:39', NULL),
(51, 3, 5, 'PLACED', 'DELIVERY', 10.00, '2025-12-29 03:17:56', NULL),
(52, 3, 1, '', 'DELIVERY', 40.00, '2025-12-29 03:57:58', NULL),
(53, 3, 1, '', 'DELIVERY', 40.00, '2025-12-29 04:47:15', NULL),
(54, 3, 1, 'DELIVERED', 'DELIVERY', 50.00, '2025-12-29 05:03:41', '2025-12-30 04:30:22'),
(55, 3, 1, 'DELIVERED', 'PICKUP', 180.00, '2025-12-29 05:04:18', '2025-12-30 04:30:17'),
(56, 3, 1, 'DELIVERED', 'DELIVERY', 5760.00, '2025-12-29 05:15:11', '2025-12-29 05:18:25'),
(57, 3, 1, 'DELIVERED', 'PICKUP', 6240.00, '2025-12-29 05:16:35', '2025-12-29 05:18:21'),
(58, 3, 1, 'CANCELLED', 'DELIVERY', 180.00, '2025-12-30 04:29:12', NULL),
(59, 3, 1, 'PLACED', 'DELIVERY', 1200.00, '2025-12-30 04:50:56', NULL),
(60, 3, 1, 'DELIVERED', 'PICKUP', 160.00, '2025-12-30 04:51:23', '2025-12-30 04:51:54'),
(61, 3, 1, 'DELIVERED', 'DELIVERY', 180.00, '2025-12-30 04:59:24', '2025-12-30 05:00:06'),
(62, 3, 5, 'PLACED', 'DELIVERY', 10.00, '2025-12-30 05:24:16', NULL),
(63, 3, 6, 'READY', 'DELIVERY', 10.00, '2026-01-02 04:53:03', NULL),
(64, 3, 6, 'READY', 'PICKUP', 10.00, '2026-01-02 04:53:29', NULL),
(65, 3, 6, 'CANCELLED', 'DELIVERY', 10.00, '2026-01-02 05:01:46', NULL),
(66, 3, 6, 'PLACED', 'PICKUP', 10.00, '2026-01-02 05:20:37', NULL),
(67, 3, 6, 'PLACED', 'PICKUP', 10.00, '2026-01-02 05:21:15', NULL),
(68, 3, 6, 'PLACED', 'DELIVERY', 10.00, '2026-01-02 05:22:28', NULL),
(69, 3, 6, 'PLACED', 'PICKUP', 10.00, '2026-01-02 05:34:00', NULL),
(70, 3, 6, 'PLACED', 'DELIVERY', 10.00, '2026-01-02 05:34:21', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `price`) VALUES
(1, 1, 1, 2, 1200.00),
(2, 1, 2, 1, 50.00),
(3, 2, 1, 2, 1200.00),
(4, 2, 2, 1, 50.00),
(5, 3, 16, 2, 110.00),
(6, 3, 19, 3, 20.00),
(7, 3, 13, 1, 750.00),
(8, 3, 20, 2, 40.00),
(9, 4, 13, 1, 750.00),
(10, 4, 16, 1, 110.00),
(11, 4, 15, 1, 190.00),
(12, 4, 17, 3, 65.00),
(13, 4, 20, 2, 40.00),
(14, 4, 19, 2, 20.00),
(15, 5, 13, 2, 750.00),
(16, 5, 16, 2, 110.00),
(17, 5, 17, 2, 65.00),
(18, 5, 20, 3, 40.00),
(19, 6, 1, 3, 40.00),
(20, 6, 4, 3, 480.00),
(21, 7, 1, 2, 40.00),
(22, 7, 4, 2, 480.00),
(23, 7, 5, 2, 180.00),
(24, 8, 3, 1, 1250.00),
(25, 8, 4, 3, 480.00),
(26, 9, 1, 3, 40.00),
(27, 9, 2, 3, 1200.00),
(28, 10, 1, 1, 40.00),
(29, 10, 3, 1, 1250.00),
(30, 10, 4, 3, 480.00),
(31, 11, 1, 3, 40.00),
(32, 12, 1, 2, 40.00),
(33, 12, 4, 2, 480.00),
(34, 13, 13, 2, 750.00),
(35, 13, 16, 1, 110.00),
(36, 14, 1, 3, 40.00),
(37, 15, 2, 1, 1200.00),
(38, 15, 3, 2, 1250.00),
(39, 16, 2, 3, 1200.00),
(40, 17, 3, 1, 1250.00),
(41, 18, 1, 2, 40.00),
(42, 19, 1, 2, 40.00),
(43, 20, 14, 3, 260.00),
(44, 21, 3, 2, 1250.00),
(45, 22, 1, 1, 40.00),
(46, 23, 4, 3, 480.00),
(47, 23, 1, 4, 40.00),
(48, 26, 4, 3, 480.00),
(49, 27, 15, 3, 190.00),
(50, 28, 1, 3, 40.00),
(51, 29, 4, 2, 480.00),
(52, 30, 5, 1, 180.00),
(53, 31, 5, 1, 180.00),
(54, 31, 7, 1, 50.00),
(55, 31, 8, 1, 25.00),
(56, 32, 1, 6, 40.00),
(57, 33, 5, 1, 180.00),
(58, 33, 7, 1, 50.00),
(59, 34, 2, 2, 1200.00),
(60, 35, 1, 2, 40.00),
(61, 36, 1, 2, 40.00),
(62, 37, 10, 1, 140.00),
(69, 44, 1, 2, 40.00),
(70, 45, 7, 1, 50.00),
(74, 48, 26, 2, 10.00),
(75, 49, 3, 1, 1250.00),
(76, 50, 5, 2, 180.00),
(77, 50, 8, 2, 25.00),
(78, 51, 26, 1, 10.00),
(79, 52, 1, 1, 40.00),
(80, 53, 1, 1, 40.00),
(81, 54, 7, 1, 50.00),
(82, 55, 5, 1, 180.00),
(83, 56, 4, 12, 480.00),
(84, 57, 4, 13, 480.00),
(85, 58, 5, 1, 180.00),
(86, 59, 2, 1, 1200.00),
(87, 60, 6, 1, 160.00),
(88, 61, 5, 1, 180.00),
(89, 62, 26, 1, 10.00),
(90, 63, 27, 1, 10.00),
(91, 64, 27, 1, 10.00),
(92, 65, 27, 1, 10.00),
(93, 66, 27, 1, 10.00),
(94, 67, 27, 1, 10.00),
(95, 68, 27, 1, 10.00),
(96, 69, 27, 1, 10.00),
(97, 70, 27, 1, 10.00);

-- --------------------------------------------------------

--
-- Table structure for table `password_reset_otp`
--

CREATE TABLE `password_reset_otp` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `otp` varchar(6) NOT NULL,
  `expiry` datetime NOT NULL,
  `used` tinyint(4) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `product_image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `shop_id`, `product_name`, `category`, `price`, `stock_quantity`, `created_at`, `product_image`) VALUES
(1, 1, 'Diary Milk', 'Chocolate', 40.00, 25, '2025-12-23 13:23:10', NULL),
(2, 1, 'rice bag', 'rice', 1200.00, 20, '2025-12-23 13:28:19', NULL),
(3, 1, 'Rice Bag 25kg', 'Rice', 1250.00, 14, '2025-12-25 10:43:03', NULL),
(4, 1, 'Wheat Flour 10kg', 'Flour', 480.00, 15, '2025-12-25 10:43:03', NULL),
(5, 1, 'Sunflower Oil 1L', 'Oil', 180.00, 31, '2025-12-25 10:43:03', 'uploads/products/product_1766816685_4466.jpeg'),
(6, 1, 'Toor Dal 1kg', 'Pulses', 160.00, 24, '2025-12-25 10:43:03', NULL),
(7, 1, 'Sugar 1kg', 'Essentials', 50.00, 56, '2025-12-25 10:43:03', 'uploads/products/product_1766815580_9531.jpeg'),
(8, 1, 'Salt 1kg', 'Essentials', 25.00, 77, '2025-12-25 10:43:03', NULL),
(10, 1, 'Tea Powder 250g', 'Beverages', 140.00, 34, '2025-12-25 10:43:03', NULL),
(11, 1, 'Tata Coffee 100g', 'Beverages', 120.00, 30, '2025-12-25 10:43:03', NULL),
(12, 1, 'Bath Soap Pack', 'Personal Care', 95.00, 45, '2025-12-25 10:43:03', NULL),
(13, 2, 'Basmati Rice 5kg', 'Rice', 750.00, 19, '2025-12-25 10:43:22', NULL),
(14, 2, 'Aashirvaad Atta 5kg', 'Flour', 260.00, 37, '2025-12-25 10:43:22', NULL),
(15, 2, 'Fortune Oil 1L', 'Oil', 190.00, 49, '2025-12-25 10:43:22', NULL),
(16, 2, 'Chana Dal 1kg', 'Pulses', 110.00, 29, '2025-12-25 10:43:22', NULL),
(17, 2, 'Brown Sugar 1kg', 'Essentials', 65.00, 15, '2025-12-25 10:43:22', NULL),
(18, 2, 'Himalayan Salt 1kg', 'Essentials', 35.00, 25, '2025-12-25 10:43:22', NULL),
(19, 2, 'Lays Chips', 'Snacks', 20.00, 65, '2025-12-25 10:43:22', NULL),
(20, 2, 'Dairy Milk Chocolate', 'Snacks', 40.00, 53, '2025-12-25 10:43:22', NULL),
(21, 2, 'Boost Health Drink 500g', 'Beverages', 230.00, 15, '2025-12-25 10:43:22', NULL),
(22, 2, 'Shampoo 340ml', 'Personal Care', 210.00, 30, '2025-12-25 10:43:22', NULL),
(26, 5, 'Jim jam', 'biscuit', 10.00, 13, '2025-12-28 11:08:57', NULL),
(27, 6, 'Oreo', 'Biscuits', 10.00, 13, '2026-01-02 04:52:52', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `recipes`
--

CREATE TABLE `recipes` (
  `recipe_id` int(11) NOT NULL,
  `dish_name` varchar(100) NOT NULL,
  `ingredients` text NOT NULL,
  `steps` text NOT NULL,
  `category` varchar(50) NOT NULL,
  `keywords` varchar(255) DEFAULT NULL COMMENT 'Additional keywords for better matching',
  `prep_time` varchar(20) DEFAULT '30 mins',
  `difficulty` varchar(20) DEFAULT 'Easy',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipes`
--

INSERT INTO `recipes` (`recipe_id`, `dish_name`, `ingredients`, `steps`, `category`, `keywords`, `prep_time`, `difficulty`, `created_at`) VALUES
(1, 'Chicken Biryani', 'Basmati rice, Chicken, Onions, Tomatoes, Yogurt, Ginger-garlic paste, Biryani masala, Saffron, Mint leaves, Coriander leaves, Green chilies, Oil, Salt', '1. Marinate chicken with yogurt and spices for 1 hour|2. Soak basmati rice for 30 minutes|3. Fry onions until golden brown|4. Add marinated chicken and cook|5. Layer rice and chicken alternately|6. Add saffron milk and dum cook for 25 minutes|7. Garnish with fried onions and serve', 'Indian', 'biryani, rice, chicken, hyderabadi, dum', '1 hour', 'Medium', '2025-12-31 03:40:18'),
(2, 'Paneer Butter Masala', 'Paneer, Tomatoes, Onions, Cashews, Butter, Cream, Ginger-garlic paste, Kashmiri red chili, Garam masala, Kasuri methi, Salt, Sugar', '1. Blend tomatoes, onions, and cashews into smooth paste|2. Heat butter and saute ginger-garlic paste|3. Add the blended paste and cook for 10 minutes|4. Add spices, cream, and kasuri methi|5. Add paneer cubes and simmer for 5 minutes|6. Garnish with cream and serve with naan', 'Indian', 'paneer, butter, masala, curry, vegetarian, gravy', '40 mins', 'Easy', '2025-12-31 03:40:18'),
(3, 'Dal Tadka', 'Toor dal, Onions, Tomatoes, Garlic, Cumin seeds, Mustard seeds, Red chilies, Turmeric, Coriander powder, Ghee, Coriander leaves, Salt', '1. Wash and pressure cook dal with turmeric|2. Mash the cooked dal|3. Heat ghee and add cumin, mustard seeds|4. Add garlic, onions, and fry until golden|5. Add tomatoes and spices, cook until soft|6. Pour tadka over dal and mix|7. Garnish with coriander and serve with rice', 'Indian', 'dal, lentils, tadka, vegetarian, healthy, protein', '30 mins', 'Easy', '2025-12-31 03:40:18'),
(4, 'Spaghetti Pasta', 'Spaghetti, Tomato sauce, Garlic, Olive oil, Onions, Basil, Oregano, Parmesan cheese, Salt, Black pepper, Chili flakes', '1. Boil spaghetti in salted water until al dente|2. Heat olive oil and saute garlic|3. Add onions and cook until translucent|4. Add tomato sauce, oregano, and basil|5. Simmer for 10 minutes|6. Toss cooked pasta with sauce|7. Top with parmesan and serve', 'Italian', 'pasta, spaghetti, italian, tomato, noodles', '25 mins', 'Easy', '2025-12-31 03:40:18'),
(5, 'Margherita Pizza', 'Pizza dough, Tomato sauce, Mozzarella cheese, Fresh basil, Olive oil, Garlic, Salt, Oregano', '1. Preheat oven to 250°C|2. Roll out pizza dough into circle|3. Spread tomato sauce evenly|4. Add mozzarella cheese|5. Drizzle olive oil and add oregano|6. Bake for 12-15 minutes until crust is golden|7. Top with fresh basil and serve', 'Italian', 'pizza, cheese, italian, baked, margherita', '30 mins', 'Easy', '2025-12-31 03:40:18'),
(6, 'Vegetable Fried Rice', 'Cooked rice, Mixed vegetables, Eggs, Soy sauce, Garlic, Ginger, Green onions, Sesame oil, Salt, Pepper, Oil', '1. Heat oil in wok on high heat|2. Scramble eggs and set aside|3. Stir-fry garlic, ginger, and vegetables|4. Add cold cooked rice|5. Add soy sauce and toss well|6. Add scrambled eggs back|7. Garnish with green onions and serve', 'Chinese', 'rice, fried rice, chinese, vegetables, quick', '20 mins', 'Easy', '2025-12-31 03:40:18'),
(7, 'Manchurian', 'Cabbage, Carrots, Capsicum, Corn flour, All-purpose flour, Soy sauce, Vinegar, Chili sauce, Garlic, Ginger, Green onions, Oil, Salt', '1. Grate vegetables and mix with flours|2. Shape into balls and deep fry until golden|3. For sauce: heat oil, add garlic-ginger|4. Add sauces and stir|5. Add fried balls to sauce|6. Toss well and garnish with green onions|7. Serve hot as starter or with rice', 'Chinese', 'manchurian, indo-chinese, starter, vegetables, crispy', '35 mins', 'Medium', '2025-12-31 03:40:18'),
(8, 'Grilled Cheese Sandwich', 'Bread slices, Cheese slices, Butter, Optional: tomatoes, onions, jalapenos', '1. Butter one side of each bread slice|2. Place cheese between bread slices|3. Add optional vegetables|4. Heat pan on medium|5. Grill sandwich until golden on both sides|6. Cut diagonally and serve hot', 'Continental', 'sandwich, cheese, grilled, quick, breakfast, snack', '10 mins', 'Easy', '2025-12-31 03:40:18'),
(9, 'Caesar Salad', 'Romaine lettuce, Croutons, Parmesan cheese, Caesar dressing, Olive oil, Lemon juice, Garlic, Black pepper', '1. Wash and chop romaine lettuce|2. Prepare caesar dressing with garlic, lemon, olive oil|3. Toss lettuce with dressing|4. Add croutons|5. Top with shaved parmesan|6. Season with black pepper|7. Serve immediately', 'Continental', 'salad, caesar, healthy, lettuce, light, diet', '15 mins', 'Easy', '2025-12-31 03:40:18'),
(10, 'Chocolate Cake', 'All-purpose flour, Cocoa powder, Sugar, Eggs, Butter, Milk, Baking powder, Vanilla extract, Chocolate frosting', '1. Preheat oven to 180°C|2. Mix flour, cocoa, baking powder|3. Beat butter and sugar until fluffy|4. Add eggs and vanilla|5. Alternately add dry ingredients and milk|6. Pour into greased pan and bake 30 mins|7. Cool and frost with chocolate frosting', 'Dessert', 'cake, chocolate, sweet, baking, birthday, dessert', '1 hour', 'Medium', '2025-12-31 03:40:18'),
(11, 'Pancakes', 'All-purpose flour, Milk, Eggs, Sugar, Butter, Baking powder, Salt, Maple syrup, Fresh fruits', '1. Mix flour, baking powder, sugar, salt|2. Whisk milk, eggs, melted butter|3. Combine wet and dry ingredients|4. Heat pan and grease lightly|5. Pour batter and cook until bubbles form|6. Flip and cook other side|7. Stack and serve with maple syrup and fruits', 'Dessert', 'pancake, breakfast, sweet, fluffy, american', '20 mins', 'Easy', '2025-12-31 03:40:18'),
(12, 'Masala Dosa', 'Dosa batter, Potatoes, Onions, Mustard seeds, Curry leaves, Turmeric, Green chilies, Oil, Salt, Coconut chutney, Sambar', '1. Boil and mash potatoes|2. Heat oil, add mustard seeds and curry leaves|3. Add onions, chilies, turmeric|4. Add mashed potatoes and mix well|5. Spread dosa batter on hot tawa|6. Drizzle oil and cook until crispy|7. Place potato filling and fold|8. Serve with chutney and sambar', 'South Indian', 'dosa, masala, south indian, breakfast, crispy, vegetarian', '30 mins', 'Medium', '2025-12-31 03:40:18'),
(13, 'Idli Sambar', 'Idli batter, Toor dal, Mixed vegetables, Tamarind, Sambar powder, Mustard seeds, Curry leaves, Asafoetida, Oil, Salt, Coconut chutney', '1. Steam idli batter in idli molds for 12 mins|2. Cook dal until soft|3. Cook vegetables separately|4. Add tamarind water and sambar powder to dal|5. Add vegetables and simmer|6. Prepare tempering with mustard, curry leaves|7. Serve idlis hot with sambar and chutney', 'South Indian', 'idli, sambar, south indian, breakfast, healthy, steamed', '35 mins', 'Easy', '2025-12-31 03:40:18');

-- --------------------------------------------------------

--
-- Table structure for table `saved_items`
--

CREATE TABLE `saved_items` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `saved_items`
--

INSERT INTO `saved_items` (`id`, `user_id`, `product_id`, `shop_id`, `created_at`) VALUES
(8, 3, 16, 2, '2025-12-30 04:41:02'),
(10, 3, 5, 1, '2025-12-30 04:41:41');

-- --------------------------------------------------------

--
-- Table structure for table `shops`
--

CREATE TABLE `shops` (
  `shop_id` int(11) NOT NULL,
  `admin_id` int(11) NOT NULL,
  `shop_name` varchar(100) NOT NULL,
  `category` enum('GROCERY','SUPERMART') NOT NULL,
  `shop_address` text NOT NULL,
  `landmark` varchar(100) DEFAULT NULL,
  `city` varchar(50) NOT NULL,
  `pincode` varchar(10) NOT NULL,
  `shop_phone` varchar(15) NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `opening_time` time NOT NULL,
  `closing_time` time NOT NULL,
  `delivery_available` tinyint(1) DEFAULT 0,
  `delivery_radius` decimal(5,2) NOT NULL DEFAULT 5.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_online` tinyint(1) DEFAULT 1,
  `shop_image` varchar(255) DEFAULT NULL,
  `notifications_enabled` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shops`
--

INSERT INTO `shops` (`shop_id`, `admin_id`, `shop_name`, `category`, `shop_address`, `landmark`, `city`, `pincode`, `shop_phone`, `latitude`, `longitude`, `opening_time`, `closing_time`, `delivery_available`, `delivery_radius`, `created_at`, `is_online`, `shop_image`, `notifications_enabled`) VALUES
(1, 2, 'kirana store', 'GROCERY', 'chembarambakkam', 'spartan school', 'chennai', '602105', '', NULL, NULL, '00:00:08', '00:00:09', 1, 5.00, '2025-12-23 12:42:29', 1, 'uploads/shops/shop_1_1767067637.jpeg', 1),
(2, 5, 'Siddu grocery mart', 'GROCERY', 'chembarabakkam', NULL, 'chennai', '600123', '8985545407', NULL, NULL, '09:00:00', '08:00:00', 1, 10.00, '2025-12-25 06:06:59', 1, NULL, 1),
(3, 7, 'D mart', 'GROCERY', 'saveetha', NULL, 'chennai', '602105', '9515555557', NULL, NULL, '09:00:00', '10:00:00', 1, 10.00, '2025-12-26 04:19:13', 0, NULL, 1),
(4, 8, 'Sanju Mart', 'SUPERMART', 'Popcity', NULL, 'chennai', '602123', '7780750883', NULL, NULL, '09:00:00', '09:30:00', 1, 15.00, '2025-12-26 10:55:24', 1, NULL, 1),
(5, 9, 'Vishal Mart', 'SUPERMART', 'Pop city', NULL, 'chennai', '602105', '9121084028', NULL, NULL, '09:00:00', '10:00:00', 1, 10.00, '2025-12-27 09:20:53', 1, NULL, 1),
(6, 11, 'kick buttoski', 'SUPERMART', 'Near Tata', NULL, 'chennai', '602105', '9193913441', NULL, NULL, '09:00:00', '10:00:00', 1, 10.00, '2026-01-02 04:52:13', 1, NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `notifications_enabled` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `full_name`, `email`, `phone`, `password_hash`, `role`, `created_at`, `notifications_enabled`) VALUES
(1, 'Siddartha Reddy', 'sid@gmail.com', '9133333337', 'siddu123', 'USER', '2025-12-23 12:07:05', 1),
(2, 'Chenna Kesava Reddy', 'kes@gmail.com', '8374203019', 'kes123', 'ADMIN', '2025-12-23 12:33:10', 1),
(3, 'prem', 'tpkr4446@gmail.com', '8985545407', 'welcome', 'USER', '2025-12-24 16:09:02', 1),
(4, 'Timmareddy Prem Kumar Reddy', 'premkumart1087.sse@saveetha.com', '8985545407', 'welcome', 'USER', '2025-12-25 05:20:56', 1),
(5, 'Timmareddy Prem Kumar Reddy', 'chinnakondujanani@gmail.com', '8985545407', 'welcome', 'ADMIN', '2025-12-25 06:06:59', 1),
(6, 'charan', 'charan@gmail.com', '9515555557', 'welcome', 'USER', '2025-12-26 04:17:31', 1),
(7, 'siva charan', 'siva@gmail.com', '9515555557', 'welcome', 'ADMIN', '2025-12-26 04:19:13', 1),
(8, 'Sanjay Reddy', 'sanju@gmail.com', '7780450883', 'welcome', 'ADMIN', '2025-12-26 10:55:24', 1),
(9, 'Harish Naidu', 'harish@gmail.com', '9121084028', 'welcome', 'ADMIN', '2025-12-27 09:20:53', 1),
(10, 'Siva Siddartha Reddy', 'siddarthreddy.5377@gmail.com', '9133333337', 'welcome123', 'USER', '2025-12-28 12:51:23', 1),
(11, 'Chandu', 'venkatachandu6666@gmail.com', '9391344155', 'welcome', 'ADMIN', '2026-01-02 04:52:13', 1);

-- --------------------------------------------------------

--
-- Table structure for table `user_addresses`
--

CREATE TABLE `user_addresses` (
  `address_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `label` varchar(50) DEFAULT 'Home',
  `address_line` text NOT NULL,
  `landmark` varchar(100) DEFAULT NULL,
  `city` varchar(50) NOT NULL,
  `pincode` varchar(10) NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `address_type` enum('HOME','WORK','OTHER') DEFAULT 'HOME',
  `is_default` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_addresses`
--

INSERT INTO `user_addresses` (`address_id`, `user_id`, `label`, `address_line`, `landmark`, `city`, `pincode`, `latitude`, `longitude`, `address_type`, `is_default`, `created_at`) VALUES
(5, 1, 'Home', 'lv villas', 'spartan school', 'chennai', '602105', 13.08270000, 80.27070000, 'HOME', 1, '2025-12-25 07:38:37'),
(6, 3, 'Home', 'lv villas', 'spartan school', 'chennai', '602105', NULL, NULL, 'HOME', 1, '2025-12-25 09:46:10'),
(9, 10, 'Near Rama Temple', 'Tadipatri', NULL, 'chennai', '515411', NULL, NULL, 'HOME', 1, '2025-12-28 14:38:13');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin_notifications`
--
ALTER TABLE `admin_notifications`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `ai_requests`
--
ALTER TABLE `ai_requests`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `fk_ai_requests_user` (`user_id`);

--
-- Indexes for table `fcm_tokens`
--
ALTER TABLE `fcm_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_token` (`fcm_token`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`feedback_id`),
  ADD KEY `fk_feedback_order` (`order_id`),
  ADD KEY `fk_feedback_user` (`user_id`),
  ADD KEY `fk_feedback_shop` (`shop_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `fk_orders_user` (`user_id`),
  ADD KEY `fk_orders_shop` (`shop_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `fk_order_items_order` (`order_id`),
  ADD KEY `fk_order_items_product` (`product_id`);

--
-- Indexes for table `password_reset_otp`
--
ALTER TABLE `password_reset_otp`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `fk_products_shop` (`shop_id`);

--
-- Indexes for table `recipes`
--
ALTER TABLE `recipes`
  ADD PRIMARY KEY (`recipe_id`),
  ADD KEY `idx_dish_name` (`dish_name`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_keywords` (`keywords`);

--
-- Indexes for table `saved_items`
--
ALTER TABLE `saved_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_save` (`user_id`,`product_id`);

--
-- Indexes for table `shops`
--
ALTER TABLE `shops`
  ADD PRIMARY KEY (`shop_id`),
  ADD KEY `fk_shops_admin` (`admin_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `user_addresses`
--
ALTER TABLE `user_addresses`
  ADD PRIMARY KEY (`address_id`),
  ADD KEY `fk_user_addresses_user` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin_notifications`
--
ALTER TABLE `admin_notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `ai_requests`
--
ALTER TABLE `ai_requests`
  MODIFY `request_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `fcm_tokens`
--
ALTER TABLE `fcm_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `feedback_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=71;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=98;

--
-- AUTO_INCREMENT for table `password_reset_otp`
--
ALTER TABLE `password_reset_otp`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `recipes`
--
ALTER TABLE `recipes`
  MODIFY `recipe_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `saved_items`
--
ALTER TABLE `saved_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `shops`
--
ALTER TABLE `shops`
  MODIFY `shop_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `user_addresses`
--
ALTER TABLE `user_addresses`
  MODIFY `address_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ai_requests`
--
ALTER TABLE `ai_requests`
  ADD CONSTRAINT `fk_ai_requests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `fk_feedback_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  ADD CONSTRAINT `fk_feedback_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`shop_id`),
  ADD CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`shop_id`),
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_products_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`shop_id`) ON DELETE CASCADE;

--
-- Constraints for table `shops`
--
ALTER TABLE `shops`
  ADD CONSTRAINT `fk_shops_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `user_addresses`
--
ALTER TABLE `user_addresses`
  ADD CONSTRAINT `fk_user_addresses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
