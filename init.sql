CREATE
DATABASE IF NOT EXISTS `keycloak`;
GRANT ALL PRIVILEGES ON `keycloak`.* TO 'admin'@'%';
FLUSH
PRIVILEGES;

CREATE
DATABASE IF NOT EXISTS `receipt-analyzer-service`;
GRANT ALL PRIVILEGES ON `receipt-analyzer-service`.* TO 'admin'@'%';
FLUSH
PRIVILEGES;