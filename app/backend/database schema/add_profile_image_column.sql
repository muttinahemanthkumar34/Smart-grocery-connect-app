-- Add profile_image column to users table
-- Run this query in your MySQL database

ALTER TABLE users ADD COLUMN profile_image VARCHAR(255) DEFAULT NULL;

-- This column will store the path to the user's profile picture
-- Example: 'uploads/profiles/user_12_1704527200.jpeg'
