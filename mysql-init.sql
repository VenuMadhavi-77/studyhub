ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
CREATE DATABASE IF NOT EXISTS studyhub_db;
CREATE USER IF NOT EXISTS 'studyhub_user'@'%' IDENTIFIED BY 'studyhub_pass_secure';
CREATE USER IF NOT EXISTS 'studyhub_user'@'localhost' IDENTIFIED BY 'studyhub_pass_secure';
GRANT ALL PRIVILEGES ON studyhub_db.* TO 'studyhub_user'@'%';
GRANT ALL PRIVILEGES ON studyhub_db.* TO 'studyhub_user'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
