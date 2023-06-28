# create databases
CREATE DATABASE IF NOT EXISTS `ejemplo-db`;

# create root user and grant rights
CREATE USER 'development'@'localhost' IDENTIFIED BY 'devpassword';
CREATE USER 'development'@'%' IDENTIFIED BY 'devpassword';
GRANT ALL ON *.* TO 'development'@'localhost';
GRANT ALL ON *.* TO 'development'@'%';
FLUSH PRIVILEGES;