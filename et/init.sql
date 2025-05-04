-- Drop existing users to avoid conflicts
DROP USER IF EXISTS 'test'@'%';
DROP USER IF EXISTS 'test'@'172.19.0.1';

-- Create users explicitly
CREATE USER 'test'@'%' IDENTIFIED BY 'password';
CREATE USER 'test'@'172.19.0.1' IDENTIFIED BY 'password';

-- Create databases
CREATE DATABASE IF NOT EXISTS authservice;
CREATE DATABASE IF NOT EXISTS userservice;

-- Grant privileges
GRANT ALL PRIVILEGES ON authservice.* TO 'test'@'%';
GRANT ALL PRIVILEGES ON userservice.* TO 'test'@'%';
GRANT ALL PRIVILEGES ON authservice.* TO 'test'@'172.19.0.1';
GRANT ALL PRIVILEGES ON userservice.* TO 'test'@'172.19.0.1';

FLUSH PRIVILEGES;