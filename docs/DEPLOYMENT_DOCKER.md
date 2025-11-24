# CT-Tibet-WMS Docker Deployment Guide

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Environment**: Production

## Overview

This guide provides comprehensive instructions for deploying CT-Tibet-WMS using Docker and Docker Compose. It includes containerized deployment for the entire stack: Spring Boot backend, Vue 3 frontend, MySQL database, Redis cache, and RabbitMQ message queue.

**Key Features:**
- Production-ready multi-container deployment
- Health checks and auto-restart policies
- Volume management for data persistence
- Environment-based configuration
- Security best practices (non-root users, secret management)
- Resource limits and optimization

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Backend Dockerfile](#backend-dockerfile)
4. [Frontend Dockerfile](#frontend-dockerfile)
5. [Docker Compose Configuration](#docker-compose-configuration)
6. [Environment Variables](#environment-variables)
7. [Building and Deployment](#building-and-deployment)
8. [Container Management](#container-management)
9. [Troubleshooting](#troubleshooting)
10. [Security Hardening](#security-hardening)

---

## Prerequisites

### Required Software
- **Docker**: Version 20.10 or later
- **Docker Compose**: Version 1.29 or later
- **Git**: For cloning the repository
- **Node.js**: 18+ (for local frontend builds if needed)
- **Java 11**: (for local backend builds if needed)
- **Maven**: 3.6+ (for building backend)

### System Requirements
- **CPU**: Minimum 2 cores, 4 cores recommended for production
- **RAM**: Minimum 4GB, 8GB+ recommended for production
- **Storage**: 20GB free space for containers and data
- **Network**: Docker network driver support, port availability (443, 80, 48888, 3306, 6379, 5672)

### Docker Installation

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker compose version
```

**macOS:**
```bash
# Using Homebrew
brew install docker docker-compose
# Or download Docker Desktop from https://www.docker.com/products/docker-desktop

docker --version
docker compose version
```

**Windows:**
- Download Docker Desktop from https://www.docker.com/products/docker-desktop
- Install and ensure WSL 2 backend is enabled
- Verify: `docker --version` and `docker compose version`

---

## Project Structure

```
CT-Tibet-WMS/
├── backend/
│   ├── pom.xml
│   ├── src/
│   ├── Dockerfile
│   └── .dockerignore
├── frontend-pc/
│   ├── package.json
│   ├── vite.config.js
│   ├── Dockerfile
│   └── .dockerignore
├── docker-compose.yml
├── .env.production
├── .env.staging
├── nginx/
│   └── nginx.conf
└── docs/
    └── DEPLOYMENT_DOCKER.md
```

---

## Backend Dockerfile

Create `H:\java\CT-Tibet-WMS\backend\Dockerfile`:

```dockerfile
# Stage 1: Build
FROM maven:3.8.1-openjdk-11 AS builder

LABEL maintainer="CT Development Team"

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -X

# Stage 2: Runtime
FROM openjdk:11-jre-slim

LABEL maintainer="CT Development Team" \
      version="1.0.0" \
      description="CT Tibet WMS Backend Service"

# Install utilities
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN useradd -m -u 1001 appuser

WORKDIR /app

# Copy application JAR from builder
COPY --from=builder /build/target/ct-tibet-wms.jar /app/
COPY --chown=appuser:appuser --from=builder /build/target/ct-tibet-wms.jar /app/

# Switch to non-root user
USER appuser

# Port exposure
EXPOSE 48888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:48888/actuator/health || exit 1

# Environment variables (can be overridden)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockDiagnosticVMOptions -XX:G1SummarizeRSetStatsPeriod=1 -Xms512m -Xmx2g"
ENV SERVER_PORT=48888
ENV SPRING_PROFILES_ACTIVE=prod

# Run application
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar ct-tibet-wms.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE} --server.port=${SERVER_PORT}"]
```

Create `H:\java\CT-Tibet-WMS\backend\.dockerignore`:

```
.git
.gitignore
.idea
.vscode
target/
*.class
*.jar
*.log
*.md
README.md
docs/
node_modules/
.DS_Store
*.swp
*.swo
*~
```

---

## Frontend Dockerfile

Create `H:\java\CT-Tibet-WMS\frontend-pc\Dockerfile`:

```dockerfile
# Stage 1: Build
FROM node:18-alpine AS builder

LABEL maintainer="CT Development Team"

WORKDIR /build

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --prefer-offline --no-audit

# Copy source code
COPY . .

# Build application
RUN npm run build

# Stage 2: Runtime (Nginx)
FROM nginx:1.25-alpine

LABEL maintainer="CT Development Team" \
      version="1.0.0" \
      description="CT Tibet WMS Frontend Service"

# Create non-root user for Nginx
RUN addgroup -g 101 -S nginx && \
    adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -c "nginx user" -G nginx nginx

WORKDIR /app

# Copy built application
COPY --from=builder /build/dist /usr/share/nginx/html

# Copy Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx-site.conf /etc/nginx/conf.d/default.conf

# Create necessary directories with proper permissions
RUN mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/log/nginx && \
    chmod -R 755 /usr/share/nginx/html

# Port exposure
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

# Run as non-root user
USER nginx

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
```

Create `H:\java\CT-Tibet-WMS\frontend-pc\.dockerignore`:

```
.git
.gitignore
.idea
.vscode
node_modules/
dist/
coverage/
*.log
*.md
README.md
.env.local
.env.*.local
*.swp
*.swo
*~
.DS_Store
docs/
```

---

## Nginx Configuration

Create `H:\java\CT-Tibet-WMS\nginx\nginx.conf`:

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 50M;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript
               application/json application/javascript application/xml+rss;
    gzip_disable "msie6";

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    include /etc/nginx/conf.d/*.conf;
}
```

Create `H:\java\CT-Tibet-WMS\nginx\nginx-site.conf`:

```nginx
upstream backend {
    server backend:48888;
}

server {
    listen 80;
    listen [::]:80;
    server_name _;

    # Redirect HTTP to HTTPS in production (comment out for development)
    # return 301 https://$host$request_uri;

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_buffering off;
    }

    # Static files - cache aggressively
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # Frontend routes - SPA fallback
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
        expires 1h;
        add_header Cache-Control "public, must-revalidate";
    }

    # Deny access to sensitive files
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }
}
```

---

## Docker Compose Configuration

Create `H:\java\CT-Tibet-WMS\docker-compose.yml`:

```yaml
version: '3.9'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0-debian
    container_name: ct-wms-mysql
    restart: unless-stopped
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-ct_tibet_wms}
      MYSQL_USER: ${MYSQL_USER:-wms_user}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      TZ: Asia/Shanghai
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u${MYSQL_USER}", "-p${MYSQL_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    ulimits:
      nofile:
        soft: 65535
        hard: 65535
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-storage-engine=InnoDB
      --max_connections=1000
      --max_allowed_packet=256M
      --innodb_buffer_pool_size=1G
      --slow_query_log=1
      --slow_query_log_file=/var/log/mysql/slow.log
      --long_query_time=2

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: ct-wms-redis
    restart: unless-stopped
    ports:
      - "${REDIS_PORT:-6379}:6379"
    environment:
      TZ: Asia/Shanghai
    volumes:
      - redis_data:/data
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-redis-password} --maxmemory 512mb --maxmemory-policy allkeys-lru
    sysctls:
      - net.core.somaxconn=512

  # RabbitMQ Message Queue (Optional)
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: ct-wms-rabbitmq
    restart: unless-stopped
    ports:
      - "${RABBITMQ_PORT:-5672}:5672"
      - "${RABBITMQ_MANAGEMENT_PORT:-15672}:15672"
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-guest}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD:-guest}
      RABBITMQ_DEFAULT_VHOST: ${RABBITMQ_VHOST:-/wms}
      TZ: Asia/Shanghai
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Backend Service
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    image: ct-wms-backend:1.0.0
    container_name: ct-wms-backend
    restart: unless-stopped
    ports:
      - "${BACKEND_PORT:-48888}:48888"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SERVER_PORT: 48888
      MYSQL_HOST: mysql
      MYSQL_PORT: 3306
      MYSQL_DATABASE: ${MYSQL_DATABASE:-ct_tibet_wms}
      MYSQL_USER: ${MYSQL_USER:-wms_user}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USER: ${RABBITMQ_USER:-guest}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-guest}
      RABBITMQ_VHOST: ${RABBITMQ_VHOST:-/wms}
      JWT_SECRET: ${JWT_SECRET}
      FILE_UPLOAD_PATH: /data/wms/uploads
      TZ: Asia/Shanghai
      JAVA_OPTS: "-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xms512m -Xmx2g"
    volumes:
      - ./backend/logs:/app/logs
      - ./uploads:/data/wms/uploads
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:48888/actuator/health"]
      interval: 30s
      timeout: 10s
      start_period: 40s
      retries: 3
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Frontend Service
  frontend:
    build:
      context: ./frontend-pc
      dockerfile: Dockerfile
    image: ct-wms-frontend:1.0.0
    container_name: ct-wms-frontend
    restart: unless-stopped
    ports:
      - "${FRONTEND_PORT:-80}:80"
    depends_on:
      - backend
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/nginx-site.conf:/etc/nginx/conf.d/default.conf:ro
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 5s
      start_period: 10s
      retries: 3
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  mysql_data:
    driver: local
  redis_data:
    driver: local
  rabbitmq_data:
    driver: local

networks:
  wms-network:
    driver: bridge
    driver_opts:
      com.docker.network.driver.mtu: 1500
```

---

## Environment Variables

### Production Environment (`.env.production`)

```env
# Application Environment
APP_ENV=production
COMPOSE_PROJECT_NAME=ct-wms-prod

# MySQL Configuration
MYSQL_ROOT_PASSWORD=your-secure-root-password-here
MYSQL_DATABASE=ct_tibet_wms
MYSQL_USER=wms_user
MYSQL_PASSWORD=your-secure-mysql-password-here
MYSQL_PORT=3306

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your-secure-redis-password-here

# RabbitMQ Configuration
RABBITMQ_USER=wms_admin
RABBITMQ_PASSWORD=your-secure-rabbitmq-password-here
RABBITMQ_VHOST=/wms
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672

# Backend Configuration
BACKEND_PORT=48888
SERVER_PORT=48888
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your-super-secret-jwt-key-change-in-production-minimum-32-chars

# Frontend Configuration
FRONTEND_PORT=80

# File Upload
FILE_UPLOAD_PATH=/data/wms/uploads
```

### Staging Environment (`.env.staging`)

```env
# Application Environment
APP_ENV=staging
COMPOSE_PROJECT_NAME=ct-wms-staging

# MySQL Configuration
MYSQL_ROOT_PASSWORD=staging-mysql-password
MYSQL_DATABASE=ct_tibet_wms_staging
MYSQL_USER=wms_user_staging
MYSQL_PASSWORD=staging-mysql-user-password
MYSQL_PORT=3307

# Redis Configuration
REDIS_PASSWORD=staging-redis-password

# RabbitMQ Configuration
RABBITMQ_USER=staging_admin
RABBITMQ_PASSWORD=staging-rabbitmq-password
RABBITMQ_VHOST=/wms-staging
RABBITMQ_PORT=5673

# Backend Configuration
BACKEND_PORT=48889
JWT_SECRET=staging-jwt-secret-key-change-before-production

# Frontend Configuration
FRONTEND_PORT=8080
```

### Development Environment (`.env.development`)

```env
# Application Environment
APP_ENV=development
COMPOSE_PROJECT_NAME=ct-wms-dev

# MySQL Configuration
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=ct_tibet_wms_dev
MYSQL_USER=dev_user
MYSQL_PASSWORD=dev_password
MYSQL_PORT=3308

# Redis Configuration
REDIS_PASSWORD=dev_redis_password

# RabbitMQ Configuration
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/wms-dev

# Backend Configuration
BACKEND_PORT=48890
JWT_SECRET=dev-jwt-secret-key

# Frontend Configuration
FRONTEND_PORT=4444
```

---

## Building and Deployment

### Step 1: Prepare Environment Files

```bash
cd H:/java/CT-Tibet-WMS

# For production
cp .env.production .env

# For staging
# cp .env.staging .env

# For development
# cp .env.development .env

# Edit .env with your actual credentials
# IMPORTANT: Change all passwords and secrets!
```

### Step 2: Create SQL Initialization Script

Create `H:\java\CT-Tibet-WMS\sql\init.sql`:

```sql
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ct_tibet_wms
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ct_tibet_wms;

-- Import your schema here
-- This should be the complete database schema from your application

-- Create tables (copy from docs/数据库设计文档.md)
-- ...

-- Create default admin user (password should be hashed in production)
-- INSERT INTO tb_user VALUES (...);
```

### Step 3: Build Docker Images

```bash
# Build all services
docker compose build

# Build specific service
docker compose build backend
docker compose build frontend

# Build with no cache
docker compose build --no-cache

# View built images
docker image ls | grep ct-wms
```

### Step 4: Start Services

```bash
# Start all services in background
docker compose up -d

# Start and view logs
docker compose up

# Start specific services
docker compose up -d mysql redis rabbitmq
docker compose up -d backend frontend

# Wait for services to be healthy
docker compose ps
```

### Step 5: Verify Deployment

```bash
# Check container status
docker compose ps

# View service logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql

# Test backend health
curl http://localhost:48888/actuator/health

# Test frontend
curl http://localhost/health

# Test database connectivity
docker compose exec mysql mysqladmin -u${MYSQL_USER} -p${MYSQL_PASSWORD} ping

# Test Redis
docker compose exec redis redis-cli ping

# Test RabbitMQ
docker compose exec rabbitmq rabbitmq-diagnostics ping
```

---

## Container Management

### Common Operations

```bash
# View running containers
docker compose ps

# View container logs
docker compose logs backend
docker compose logs -f backend --tail=100

# Execute command in container
docker compose exec backend bash
docker compose exec mysql mysql -u root -p${MYSQL_ROOT_PASSWORD}

# Restart services
docker compose restart
docker compose restart backend
docker compose restart frontend

# Stop services
docker compose stop
docker compose stop backend frontend

# Start services
docker compose start
docker compose start backend frontend

# Remove containers (keeps volumes)
docker compose down

# Remove containers and volumes
docker compose down -v

# Update service and restart
docker compose pull
docker compose up -d
```

### Backup and Restore

```bash
# Backup MySQL database
docker compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ct_tibet_wms > backup.sql

# Restore MySQL database
docker compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} ct_tibet_wms < backup.sql

# Backup Redis data
docker compose exec redis redis-cli --rdb /data/dump.rdb
docker cp ct-wms-redis:/data/dump.rdb ./redis-backup.rdb

# Backup volume data
docker run --rm -v ct-wms-mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-data-backup.tar.gz -C /data .
docker run --rm -v ct-wms-redis_data:/data -v $(pwd):/backup alpine tar czf /backup/redis-data-backup.tar.gz -C /data .
```

---

## Troubleshooting

### Container Won't Start

```bash
# Check logs
docker compose logs backend
docker compose logs mysql

# Verify image exists
docker image ls

# Check resource constraints
docker stats

# Inspect container
docker inspect ct-wms-backend
```

### Database Connection Issues

```bash
# Verify MySQL is running and healthy
docker compose ps mysql

# Test database connection
docker compose exec backend bash
mysql -h mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} -D ct_tibet_wms

# Check MySQL logs
docker compose logs mysql

# Verify network
docker network ls
docker network inspect ct-tibet-wms_wms-network
```

### Application Startup Issues

```bash
# Check if port is in use
netstat -tuln | grep 48888

# View application logs
docker compose logs -f backend --tail=500

# Check memory usage
docker stats ct-wms-backend

# Increase memory limit in docker-compose.yml if needed
```

### Frontend Not Loading

```bash
# Check Nginx configuration
docker compose exec frontend nginx -t

# View Nginx logs
docker compose logs frontend

# Test API connectivity
docker compose exec frontend curl http://backend:48888/api/health

# Verify static files
docker compose exec frontend ls -la /usr/share/nginx/html/
```

### Redis/RabbitMQ Issues

```bash
# Test Redis connection
docker compose exec redis redis-cli -a ${REDIS_PASSWORD} ping

# Test RabbitMQ connection
docker compose exec rabbitmq rabbitmq-diagnostics ping

# View RabbitMQ management UI
# http://localhost:15672 (default: guest/guest)

# Check message queue stats
docker compose exec rabbitmq rabbitmq-diagnostics memory_breakdown
```

---

## Security Hardening

### 1. Change Default Credentials

```bash
# Update .env file with strong passwords
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 32)
MYSQL_PASSWORD=$(openssl rand -base64 32)
REDIS_PASSWORD=$(openssl rand -base64 32)
RABBITMQ_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 48)
```

### 2. Enable HTTPS

Update `nginx-site.conf`:

```nginx
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # ... rest of configuration
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

### 3. Limit Network Access

```yaml
# In docker-compose.yml, restrict port exposure
services:
  backend:
    ports:
      - "127.0.0.1:48888:48888"  # Only accessible locally
  mysql:
    ports:
      - "127.0.0.1:3306:3306"     # Only accessible from Docker network
```

### 4. Set Resource Limits

```yaml
# In docker-compose.yml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 2G
    reservations:
      cpus: '1'
      memory: 1G
```

### 5. Enable Logging and Monitoring

```yaml
# In docker-compose.yml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

---

## Production Checklist

- [ ] All default passwords changed and stored securely
- [ ] HTTPS configured and certificates valid
- [ ] Database backups automated and tested
- [ ] Redis persistence enabled
- [ ] RabbitMQ users configured with strong passwords
- [ ] Health checks passing for all services
- [ ] Resource limits set appropriately
- [ ] Logging configured and monitored
- [ ] Firewall rules configured
- [ ] Volume backups scheduled
- [ ] Regular security updates applied
- [ ] SSL certificates auto-renewal configured
- [ ] Monitoring and alerting set up
- [ ] Disaster recovery plan documented
- [ ] Performance baselines established

---

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Nginx Configuration](https://nginx.org/en/docs/)
- [MySQL Docker Best Practices](https://dev.mysql.com/doc/)

---

**Last Updated**: 2025-11-17
**Maintained By**: CT Development Team
