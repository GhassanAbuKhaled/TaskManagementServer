CREATE DATABASE IF NOT EXISTS taskmanager;
CREATE USER IF NOT EXISTS 'taskmanager'@'%' IDENTIFIED BY 'taskmanager123';
GRANT ALL PRIVILEGES ON taskmanager.* TO 'taskmanager'@'%';
FLUSH PRIVILEGES;
USE taskmanager;