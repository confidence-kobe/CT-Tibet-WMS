# CT-Tibet-WMS Manual Deployment Guide

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Environment**: Production

## Overview

This guide provides step-by-step instructions for deploying CT-Tibet-WMS on a physical or virtual server without Docker. It covers installation of all required components, configuration, and deployment procedures.

**Suitable For:**
- On-premises server deployments
- Systems without Docker support
- Custom infrastructure requirements
- Legacy system integration

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Database Setup](#database-setup)
4. [Backend Deployment](#backend-deployment)
5. [Frontend Deployment](#frontend-deployment)
6. [Service Management](#service-management)
7. [Log Management](#log-management)
8. [Backup and Recovery](#backup-and-recovery)
9. [Troubleshooting](#troubleshooting)
10. [Performance Tuning](#performance-tuning)

---

## System Requirements

### Operating System

**Linux (Recommended for Production)**
- Ubuntu 20.04 LTS or later
- CentOS 8 or later
- Red Hat Enterprise Linux 8 or later
- Debian 10 or later

**Windows Server (if necessary)**
- Windows Server 2019 or later

### Hardware Requirements

**Minimum (Development/Testing)**
- CPU: 2 cores
- RAM: 4GB
- Storage: 50GB
- Network: 100Mbps

**Recommended (Production)**
- CPU: 4+ cores
- RAM: 8-16GB
- Storage: 100GB+ (SSD)
- Network: 1Gbps
- Dedicated drives for data and logs

### Software Requirements

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 11+ | Backend runtime |
| Maven | 3.6+ | Backend build tool |
| Node.js | 16+ | Frontend build tool |
| Nginx | 1.20+ | Frontend web server & reverse proxy |
| MySQL | 8.0+ | Database |
| Redis | 6.0+ | Cache & session store |
| RabbitMQ | 3.8+ | Message queue |

---

## Pre-Deployment Checklist

### System Preparation

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Set timezone to Asia/Shanghai
sudo timedatectl set-timezone Asia/Shanghai

# Verify timezone
timedatectl

# Check disk space
df -h

# Check memory
free -h

# Check CPU cores
nproc
```

### Create Application User

```bash
# Create dedicated user for application
sudo useradd -m -s /bin/bash -d /home/wmsapp wmsapp

# Add sudo privileges (optional)
sudo usermod -aG sudo wmsapp

# Switch to application user
su - wmsapp
```

### Open Required Ports

```bash
# Linux (UFW)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 3306/tcp  # MySQL
sudo ufw allow 6379/tcp  # Redis
sudo ufw allow 5672/tcp  # RabbitMQ
sudo ufw enable

# Verify ports
sudo ufw status

# Linux (iptables)
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 3306 -j ACCEPT
```

---

## Database Setup

### Install MySQL Server

**Linux (Ubuntu/Debian):**

```bash
# Update package list
sudo apt update

# Install MySQL Server
sudo apt install -y mysql-server mysql-client

# Secure MySQL installation
sudo mysql_secure_installation

# Start MySQL service
sudo systemctl start mysql
sudo systemctl enable mysql

# Verify installation
sudo systemctl status mysql
mysql --version
```

**Linux (CentOS/RHEL):**

```bash
# Install MySQL Server
sudo yum install -y mysql-server

# Start service
sudo systemctl start mysqld
sudo systemctl enable mysqld

# Secure installation
sudo mysql_secure_installation

# Verify
sudo systemctl status mysqld
```

### Configure MySQL

```bash
# Connect to MySQL
sudo mysql -u root -p

# Create application user and database
CREATE DATABASE IF NOT EXISTS ct_tibet_wms
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER 'wms_user'@'localhost' IDENTIFIED BY 'your-secure-password-here';
GRANT ALL PRIVILEGES ON ct_tibet_wms.* TO 'wms_user'@'localhost';

-- For remote access (use with caution)
CREATE USER 'wms_user'@'%' IDENTIFIED BY 'your-secure-password-here';
GRANT ALL PRIVILEGES ON ct_tibet_wms.* TO 'wms_user'@'%';

FLUSH PRIVILEGES;
EXIT;
```

### Import Database Schema

```bash
# Export schema from your backup or documentation
# This should be the complete database structure

# Import schema
mysql -u wms_user -p ct_tibet_wms < database_schema.sql

# Verify tables created
mysql -u wms_user -p ct_tibet_wms -e "SHOW TABLES;"
```

### Optimize MySQL Configuration

Edit `/etc/mysql/mysql.conf.d/mysqld.cnf`:

```ini
[mysqld]
# Basic Configuration
bind-address = 127.0.0.1
port = 3306
socket = /var/run/mysqld/mysqld.sock

# Character Set
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
init_connect = 'SET NAMES utf8mb4'

# Connection Limits
max_connections = 1000
max_allowed_packet = 256M
connect_timeout = 10
interactive_timeout = 28800
wait_timeout = 28800

# Performance Settings
skip-external-locking
key_buffer_size = 256M
thread_cache_size = 128
sort_buffer_size = 2M
bulk_insert_buffer_size = 16M
tmp_table_size = 32M
max_heap_table_size = 32M

# InnoDB Settings
default_storage_engine = InnoDB
innodb_buffer_pool_size = 2G
innodb_file_per_table = 1
innodb_flush_log_at_trx_commit = 2
innodb_log_file_size = 512M
innodb_log_buffer_size = 16M

# Query Optimization
query_cache_type = 0
query_cache_size = 0

# Slow Query Log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# Binary Logging (for replication)
log_bin = /var/log/mysql/mysql-bin.log
binlog_format = ROW
server_id = 1

# Logging
log_error = /var/log/mysql/error.log
log_warnings = 2

[mysqldump]
quick
quote-names
max_allowed_packet = 256M

[mysqld_safe]
log_error = /var/log/mysql/error.log
pid-file = /var/run/mysqld/mysqld.pid
socket = /var/run/mysqld/mysqld.sock
```

Restart MySQL:

```bash
sudo systemctl restart mysql
```

---

## Backend Deployment

### Install Java Development Kit

**Linux (Ubuntu/Debian):**

```bash
# Install Java 11
sudo apt install -y openjdk-11-jdk

# Verify installation
java -version
javac -version

# Set JAVA_HOME
echo 'export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Verify JAVA_HOME
echo $JAVA_HOME
```

**Linux (CentOS/RHEL):**

```bash
# Install Java 11
sudo yum install -y java-11-openjdk java-11-openjdk-devel

# Verify and set JAVA_HOME
java -version
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-$(uname -m)/
```

### Install Maven

```bash
# Download Maven
cd /opt
sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 maven

# Set environment variables
echo 'export M2_HOME="/opt/maven"' | sudo tee -a /etc/profile.d/maven.sh
echo 'export PATH="${M2_HOME}/bin:${PATH}"' | sudo tee -a /etc/profile.d/maven.sh
source /etc/profile.d/maven.sh

# Verify
mvn -version
```

### Build Backend Application

```bash
# Navigate to backend directory
cd /home/wmsapp/ct-tibet-wms/backend

# Build with Maven
mvn clean package -DskipTests -X

# Build output
# Target: backend/target/ct-tibet-wms.jar (40-50 MB)

# Verify build
ls -lah target/ct-tibet-wms.jar
```

### Create Application Directory

```bash
# Create directory structure
sudo mkdir -p /opt/wms/backend
sudo mkdir -p /var/log/wms
sudo mkdir -p /data/wms/uploads
sudo mkdir -p /etc/wms

# Set permissions
sudo chown -R wmsapp:wmsapp /opt/wms
sudo chown -R wmsapp:wmsapp /var/log/wms
sudo chown -R wmsapp:wmsapp /data/wms
sudo chown -R wmsapp:wmsapp /etc/wms

# Copy JAR file
cp backend/target/ct-tibet-wms.jar /opt/wms/backend/
```

### Configure Backend Application

Create `/etc/wms/application-prod.yml`:

```yaml
spring:
  application:
    name: ct-tibet-wms
  profiles:
    active: prod

  # Database Configuration
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/ct_tibet_wms?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: wms_user
    password: your-secure-password-here
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: WmsHikariPool
      auto-commit: true
      leak-detection-threshold: 60000

  # Redis Configuration
  redis:
    host: 127.0.0.1
    port: 6379
    password: your-redis-password-here
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
      shutdown-timeout: 100

  # RabbitMQ Configuration
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: wms_admin
    password: your-rabbitmq-password-here
    virtual-host: /wms
    publisher-confirm-type: correlated
    publisher-returns: true
    connection-timeout: 10000
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        retry:
          enabled: true
          initial-interval: 1000
          max-interval: 10000
          multiplier: 1.0
          max-attempts: 3

  # Jackson Configuration
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null

  # File Upload
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB

# MyBatis-Plus Configuration
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.ct.wms.**.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    default-fetch-size: 100
    default-statement-timeout: 30
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
    banner: false

# JWT Configuration
jwt:
  secret: your-super-secret-jwt-key-minimum-32-characters-change-in-production
  expiration: 7200
  refresh-expiration: 604800
  issuer: ct-tibet-wms

# File Upload Configuration
file:
  upload-path: /data/wms/uploads
  max-size: 10485760
  allowed-extensions: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx

# Server Configuration
server:
  port: 48888
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024
  tomcat:
    uri-encoding: UTF-8
    threads:
      max: 200
      min-spare: 10
    accept-count: 100
    max-http-post-size: 52428800

# Management Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# Knife4j Configuration
knife4j:
  enable: true
  production: true
```

### Create Application Startup Script

Create `/opt/wms/backend/start.sh`:

```bash
#!/bin/bash

APP_JAR="/opt/wms/backend/ct-tibet-wms.jar"
APP_LOG_DIR="/var/log/wms"
APP_PID_FILE="/var/run/wms.pid"
CONFIG_FILE="/etc/wms/application-prod.yml"

# Java JVM parameters
JAVA_OPTS="-XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=1 \
  -Xms512m \
  -Xmx2g \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${APP_LOG_DIR}/heap.dump \
  -Dlog.path=${APP_LOG_DIR}"

# Check if JAR file exists
if [ ! -f "$APP_JAR" ]; then
    echo "Error: JAR file not found at $APP_JAR"
    exit 1
fi

# Check if application is already running
if [ -f "$APP_PID_FILE" ]; then
    OLD_PID=$(cat "$APP_PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "Application is already running with PID $OLD_PID"
        exit 1
    fi
fi

# Create log directory if not exists
mkdir -p "$APP_LOG_DIR"

# Start application
echo "Starting CT-Tibet-WMS application..."
nohup java $JAVA_OPTS \
    -jar "$APP_JAR" \
    --spring.config.location="file:${CONFIG_FILE}" \
    --spring.profiles.active=prod \
    > "$APP_LOG_DIR/wms.log" 2>&1 &

APP_PID=$!
echo $APP_PID > "$APP_PID_FILE"

# Wait for application to start
sleep 5

# Check if application is running
if kill -0 $APP_PID 2>/dev/null; then
    echo "Application started successfully with PID $APP_PID"
    exit 0
else
    echo "Failed to start application"
    exit 1
fi
```

Make script executable:

```bash
chmod +x /opt/wms/backend/start.sh
```

### Create Stop Script

Create `/opt/wms/backend/stop.sh`:

```bash
#!/bin/bash

APP_PID_FILE="/var/run/wms.pid"
APP_LOG_DIR="/var/log/wms"

if [ -f "$APP_PID_FILE" ]; then
    PID=$(cat "$APP_PID_FILE")

    if kill -0 "$PID" 2>/dev/null; then
        echo "Stopping application with PID $PID..."
        kill -TERM "$PID"

        # Wait for graceful shutdown
        for i in {1..30}; do
            if kill -0 "$PID" 2>/dev/null; then
                sleep 1
            else
                echo "Application stopped gracefully"
                rm -f "$APP_PID_FILE"
                exit 0
            fi
        done

        # Force kill if still running
        echo "Force killing application..."
        kill -9 "$PID"
        rm -f "$APP_PID_FILE"
    else
        echo "Application is not running"
        rm -f "$APP_PID_FILE"
    fi
else
    echo "PID file not found"
    exit 1
fi
```

Make script executable:

```bash
chmod +x /opt/wms/backend/stop.sh
```

---

## Frontend Deployment

### Install Node.js and npm

**Linux (Ubuntu/Debian):**

```bash
# Install Node.js LTS
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node --version
npm --version

# Update npm
npm install -g npm@latest
```

**Linux (CentOS/RHEL):**

```bash
# Install Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# Verify
node --version
npm --version
```

### Build Frontend Application

```bash
# Navigate to frontend directory
cd /home/wmsapp/ct-tibet-wms/frontend-pc

# Install dependencies
npm ci

# Build for production
npm run build

# Verify build output
ls -lah dist/
du -sh dist/
```

### Install Nginx

**Linux (Ubuntu/Debian):**

```bash
# Install Nginx
sudo apt install -y nginx

# Start service
sudo systemctl start nginx
sudo systemctl enable nginx

# Verify
sudo systemctl status nginx
nginx -v
```

**Linux (CentOS/RHEL):**

```bash
# Install Nginx
sudo yum install -y nginx

# Start service
sudo systemctl start nginx
sudo systemctl enable nginx

# Verify
sudo systemctl status nginx
```

### Configure Nginx

Create `/etc/nginx/sites-available/ct-tibet-wms`:

```nginx
upstream backend {
    server 127.0.0.1:48888;
    keepalive 32;
}

# Rate limiting zones
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/s;
limit_req_zone $binary_remote_addr zone=general_limit:10m rate=50r/s;

server {
    listen 80 default_server;
    listen [::]:80 default_server;

    server_name _;

    # Redirect HTTP to HTTPS (uncomment for production with SSL)
    # return 301 https://$host$request_uri;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';" always;

    # Client upload size
    client_max_body_size 50M;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1000;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript
               application/json application/javascript application/xml+rss;

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # API Proxy
    location /api/ {
        limit_req zone=api_limit burst=200 nodelay;

        proxy_pass http://backend/api/;
        proxy_http_version 1.1;

        # Headers
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $server_name;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_buffering off;

        # Cache settings
        proxy_cache_bypass $http_pragma $http_authorization;
    }

    # Static files - Cache aggressively
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /var/www/ct-tibet-wms;
        expires 30d;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # SPA - fallback to index.html
    location / {
        limit_req zone=general_limit burst=50 nodelay;

        root /var/www/ct-tibet-wms;
        try_files $uri $uri/ /index.html;

        expires 1h;
        add_header Cache-Control "public, must-revalidate";
    }

    # Deny access to hidden files/directories
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }

    # Deny access to source files
    location ~ \.(env|git|svn)$ {
        deny all;
        access_log off;
        log_not_found off;
    }

    # Logging
    access_log /var/log/nginx/ct-tibet-wms-access.log combined;
    error_log /var/log/nginx/ct-tibet-wms-error.log warn;
}

# HTTPS Configuration (uncomment for production)
# server {
#     listen 443 ssl http2;
#     listen [::]:443 ssl http2;
#
#     server_name your-domain.com;
#
#     ssl_certificate /etc/nginx/ssl/cert.pem;
#     ssl_certificate_key /etc/nginx/ssl/key.pem;
#     ssl_protocols TLSv1.2 TLSv1.3;
#     ssl_ciphers HIGH:!aNULL:!MD5;
#     ssl_prefer_server_ciphers on;
#     ssl_session_cache shared:SSL:10m;
#     ssl_session_timeout 10m;
#
#     # ... rest of configuration from above
# }
```

Enable the site:

```bash
# Create symlink
sudo ln -s /etc/nginx/sites-available/ct-tibet-wms /etc/nginx/sites-enabled/

# Remove default site
sudo rm /etc/nginx/sites-enabled/default

# Test Nginx configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

### Deploy Frontend Files

```bash
# Create directory
sudo mkdir -p /var/www/ct-tibet-wms

# Copy build output
sudo cp -r frontend-pc/dist/* /var/www/ct-tibet-wms/

# Set permissions
sudo chown -R www-data:www-data /var/www/ct-tibet-wms
sudo chmod -R 755 /var/www/ct-tibet-wms

# Verify
ls -la /var/www/ct-tibet-wms/
```

---

## Service Management

### Create Systemd Service for Backend

Create `/etc/systemd/system/wms-backend.service`:

```ini
[Unit]
Description=CT-Tibet-WMS Backend Service
After=network-online.target mysql.service redis-server.service
Wants=network-online.target

[Service]
Type=forking
User=wmsapp
Group=wmsapp
WorkingDirectory=/opt/wms/backend

# Environment
Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
Environment="SPRING_PROFILES_ACTIVE=prod"

# Start command
ExecStart=/opt/wms/backend/start.sh
ExecStop=/opt/wms/backend/stop.sh

# Restart policy
Restart=always
RestartSec=10
StartLimitInterval=60s
StartLimitBurst=5

# Security
PrivateTmp=true
NoNewPrivileges=true

# Logging
StandardOutput=append:/var/log/wms/wms-backend.log
StandardError=append:/var/log/wms/wms-backend.log

[Install]
WantedBy=multi-user.target
```

Register and start service:

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service (auto-start on boot)
sudo systemctl enable wms-backend.service

# Start service
sudo systemctl start wms-backend.service

# Check status
sudo systemctl status wms-backend.service

# View service logs
sudo journalctl -u wms-backend.service -f

# Tail application logs
sudo tail -f /var/log/wms/wms.log
```

### Create Systemd Service for Nginx

Nginx already has a default systemd service:

```bash
# Enable Nginx
sudo systemctl enable nginx

# Start/restart Nginx
sudo systemctl start nginx
sudo systemctl restart nginx

# Check status
sudo systemctl status nginx
```

### Service Commands

```bash
# Start/stop/restart services
sudo systemctl start wms-backend.service
sudo systemctl stop wms-backend.service
sudo systemctl restart wms-backend.service

# Check service status
sudo systemctl status wms-backend.service

# View detailed logs
sudo journalctl -u wms-backend.service -n 100
sudo journalctl -u wms-backend.service --since "2 hours ago"

# Reload systemd configuration
sudo systemctl daemon-reload
```

---

## Log Management

### Logback Configuration for Backend

Update backend `logback-spring.xml` for production:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="LOG_HOME" source="log.path" defaultValue="/var/log/wms"/>

    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!-- File Appender - All Logs -->
    <appender name="FILE_ALL" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/all.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/all.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!-- File Appender - Error Logs Only -->
    <appender name="FILE_ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/error.log</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/error.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n%rEx</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!-- File Appender - Business Logs -->
    <appender name="FILE_BUSINESS" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/business.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/business.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!-- Logger Configuration -->
    <logger name="com.ct.wms" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE_ALL"/>
        <appender-ref ref="FILE_BUSINESS"/>
    </logger>

    <logger name="org.springframework" level="WARN"/>
    <logger name="org.mybatis" level="WARN"/>
    <logger name="com.zaxxer.hikari" level="INFO"/>
    <logger name="com.alibaba.druid" level="WARN"/>

    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE_ALL"/>
        <appender-ref ref="FILE_ERROR"/>
    </root>

    <!-- Production Profile -->
    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="FILE_ALL"/>
            <appender-ref ref="FILE_ERROR"/>
        </root>
    </springProfile>
</configuration>
```

### Nginx Log Rotation

Create `/etc/logrotate.d/ct-tibet-wms`:

```
/var/log/nginx/ct-tibet-wms-*.log {
    daily
    rotate 14
    compress
    delaycompress
    notifempty
    create 640 www-data adm
    sharedscripts
    postrotate
        if [ -f /var/run/nginx.pid ]; then
            kill -USR1 `cat /var/run/nginx.pid`
        fi
    endscript
}

/var/log/wms/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 640 wmsapp wmsapp
    missingok
}
```

### View Logs

```bash
# Backend logs
tail -f /var/log/wms/wms.log
tail -f /var/log/wms/error.log

# Nginx logs
tail -f /var/log/nginx/ct-tibet-wms-access.log
tail -f /var/log/nginx/ct-tibet-wms-error.log

# Search logs
grep "ERROR" /var/log/wms/error.log | tail -50
grep "exception" /var/log/wms/wms.log -i | tail -50

# Analyze logs with time range
awk '$4 >= "[17/Nov/2025:10:00:00" && $4 <= "[17/Nov/2025:11:00:00"' /var/log/nginx/ct-tibet-wms-access.log
```

---

## Backup and Recovery

### Automated Database Backup

Create `/opt/wms/scripts/backup_database.sh`:

```bash
#!/bin/bash

BACKUP_DIR="/var/backups/wms/database"
MYSQL_USER="wms_user"
MYSQL_PASSWORD="your-password-here"
MYSQL_DB="ct_tibet_wms"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/db_$TIMESTAMP.sql.gz"

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Backup database
mysqldump -u $MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DB | gzip > "$BACKUP_FILE"

# Keep only last 30 days of backups
find "$BACKUP_DIR" -name "db_*.sql.gz" -mtime +30 -delete

# Log backup
echo "Backup completed: $BACKUP_FILE ($(du -h "$BACKUP_FILE" | cut -f1))" >> /var/log/wms/backup.log

# Verify backup
if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
    echo "Backup verification: SUCCESS" >> /var/log/wms/backup.log
    exit 0
else
    echo "Backup verification: FAILED" >> /var/log/wms/backup.log
    exit 1
fi
```

Make executable and schedule:

```bash
chmod +x /opt/wms/scripts/backup_database.sh

# Add to crontab (daily at 2:00 AM)
crontab -e

# Add line:
# 0 2 * * * /opt/wms/scripts/backup_database.sh
```

### Restore Database Backup

```bash
# List available backups
ls -lah /var/backups/wms/database/

# Restore from backup
gunzip < /var/backups/wms/database/db_20251117_020000.sql.gz | mysql -u wms_user -p ct_tibet_wms

# Verify restoration
mysql -u wms_user -p ct_tibet_wms -e "SELECT COUNT(*) FROM tb_user;"
```

### File Upload Backup

```bash
# Backup uploads
tar czf /var/backups/wms/uploads_$(date +%Y%m%d_%H%M%S).tar.gz /data/wms/uploads/

# Restore uploads
tar xzf /var/backups/wms/uploads_20251117_020000.tar.gz -C /
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check logs
tail -f /var/log/wms/wms.log

# Check if port is in use
sudo lsof -i :48888
sudo netstat -tulnp | grep 48888

# Check Java process
ps aux | grep java
ps aux | grep ct-tibet-wms

# Verify configuration file
cat /etc/wms/application-prod.yml

# Check file permissions
ls -la /opt/wms/backend/
ls -la /var/log/wms/

# Check database connectivity
mysql -h 127.0.0.1 -u wms_user -p ct_tibet_wms -e "SELECT 1;"
```

### Database Connection Issues

```bash
# Check MySQL is running
sudo systemctl status mysql

# Test connection
mysql -h 127.0.0.1 -u wms_user -p -e "SELECT 1;"

# Check user privileges
mysql -u root -p -e "SHOW GRANTS FOR 'wms_user'@'localhost';"

# View MySQL error log
tail -f /var/log/mysql/error.log

# Monitor slow queries
tail -f /var/log/mysql/slow.log
```

### Nginx Issues

```bash
# Test configuration
sudo nginx -t

# Check if Nginx is running
sudo systemctl status nginx

# Check Nginx logs
tail -f /var/log/nginx/ct-tibet-wms-error.log
tail -f /var/log/nginx/ct-tibet-wms-access.log

# Check port binding
sudo netstat -tulnp | grep nginx

# Monitor Nginx processes
ps aux | grep nginx
```

### Memory Issues

```bash
# Check memory usage
free -h
watch -n 1 free -h

# Check Java process memory
ps aux | grep java
jps -l

# View memory statistics
vmstat 1

# Check disk space
df -h

# Monitor top processes
top -b -n 1 | head -20
```

---

## Performance Tuning

### Database Optimization

```sql
-- Check table sizes
SELECT
    TABLE_NAME,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS Size_MB
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
ORDER BY SIZE_MB DESC;

-- Analyze tables
ANALYZE TABLE tb_user, tb_material, tb_inbound, tb_outbound, tb_apply;

-- Optimize tables
OPTIMIZE TABLE tb_user, tb_material, tb_inbound, tb_outbound, tb_apply;

-- Check index efficiency
SELECT OBJECT_SCHEMA, OBJECT_NAME, COUNT_READ, COUNT_WRITE, COUNT_DELETE, COUNT_UPDATE, COUNT_INSERT
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'ct_tibet_wms'
ORDER BY COUNT_READ DESC;
```

### Java JVM Tuning

Optimize in `/opt/wms/backend/start.sh`:

```bash
JAVA_OPTS="-XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=1 \
  -Xms2g \
  -Xmx4g \
  -XX:+ParallelRefProcEnabled \
  -XX:+AlwaysPreTouch \
  -XX:+UnlockExperimentalVMOptions \
  -XX:G1NewCollectionHeuristicWeight=35 \
  -XX:G1ReservePercent=20 \
  -XX:InitiatingHeapOccupancyPercent=20 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=${APP_LOG_DIR}/heap.dump"
```

### Connection Pool Optimization

Adjust in `/etc/wms/application-prod.yml`:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  redis:
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 4
```

### Enable Query Caching

```yaml
# Cache frequently accessed data
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000
```

---

**Last Updated**: 2025-11-17
**Maintained By**: CT Development Team
