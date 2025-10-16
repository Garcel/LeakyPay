-- Vulnerable Banking Application Database Schema
-- This database contains intentional vulnerabilities for educational purposes

CREATE DATABASE IF NOT EXISTS vulnerable_bank;
USE vulnerable_bank;

-- Drop existing tables
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- Users table with plaintext passwords (VULN: Insecure storage)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- VULN: Storing plaintext passwords
    balance DECIMAL(10, 2) DEFAULT 1000.00,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username)
);

-- Transactions table
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    from_user VARCHAR(50) NOT NULL,
    to_user VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'completed',
    INDEX idx_from_user (from_user),
    INDEX idx_to_user (to_user)
);

-- Insert test users with plaintext passwords (VULN)
INSERT INTO users (username, password, balance, email, phone) VALUES
('alice', 'password123', 5000.00, 'alice@email.com', '555-0001'),
('bob', 'qwerty', 3000.00, 'bob@email.com', '555-0002'),
('charlie', '12345', 1500.00, 'charlie@email.com', '555-0003'),
('admin', 'admin', 10000.00, 'admin@bank.com', '555-9999'),
('test', 'test', 1000.00, 'test@email.com', '555-0000');

-- Insert some test transactions
INSERT INTO transactions (from_user, to_user, amount) VALUES
('alice', 'bob', 100.00),
('bob', 'charlie', 50.00),
('charlie', 'alice', 25.00);

-- Create a view that exposes sensitive data (VULN)
CREATE OR REPLACE VIEW user_credentials AS
SELECT username, password, email, phone, balance
FROM users;

-- Grant all privileges (VULN: Excessive permissions)
-- In production, this would be a security risk

-- Display created data
SELECT 'Users created:' AS message;
SELECT id, username, password, balance FROM users;

SELECT 'Transactions created:' AS message;
SELECT * FROM transactions;

-- VULN: Exposed admin credentials
SELECT 'ADMIN CREDENTIALS (INSECURE!):' AS WARNING;
SELECT username, password FROM users WHERE username = 'admin';
