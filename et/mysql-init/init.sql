CREATE DATABASE IF NOT EXISTS authservice;
CREATE DATABASE IF NOT EXISTS userservice;
GRANT ALL PRIVILEGES ON authservice.* TO 'test'@'%';
GRANT ALL PRIVILEGES ON userservice.* TO 'test'@'%';
FLUSH PRIVILEGES;