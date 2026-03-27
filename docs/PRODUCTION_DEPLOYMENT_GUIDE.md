# CT-Tibet-WMS Production Deployment Guide

**Version**: 1.0.0
**Last Updated**: 2025-11-24
**Environment**: Production
**Status**: Ready for Deployment

## Table of Contents

1. [Overview](#overview)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Environment Setup](#environment-setup)
4. [Configuration Files](#configuration-files)
5. [Deployment Instructions](#deployment-instructions)
6. [Health Checks](#health-checks)
7. [Monitoring and Maintenance](#monitoring-and-maintenance)
8. [Backup and Disaster Recovery](#backup-and-disaster-recovery)
9. [Troubleshooting](#troubleshooting)
10. [Security Hardening](#security-hardening)

---

## Overview

### Project Architecture

The CT-Tibet-WMS production deployment uses Docker Compose to orchestrate a complete stack:

- **Frontend**: Vue 3 SPA served by Nginx (port 80/443)
- **Backend**: Spring Boot REST API (port 48888)
- **Database**: MySQL 8.0 (port 3306)
- **Cache**: Redis 7.2 (port 6379)
- **Message Queue**: RabbitMQ 3.12 (port 5672)

### Deployment Strategy

- **Infrastructure**: Docker Compose with persistent volumes
- **High Availability**: Health checks and auto-restart
- **Persistence**: MySQL, Redis, RabbitMQ with data persistence
- **Scalability**: Resource limits and connection pooling optimized
- **Security**: Non-root containers, password-protected services, SSL-ready

---

## Pre-Deployment Checklist

### System Requirements

- [ ] Linux Server or VM (Ubuntu 20.04+ recommended)
- [ ] Docker 20.10+ installed
- [ ] Docker Compose 2.0+ installed
- [ ] 4+ CPU cores
- [ ] 8GB+ RAM
- [ ] 50GB+ free disk space
- [ ] Network access to required services

### Prerequisites

- [ ] Source code cloned from repository
- [ ] All required configuration files present
- [ ] Docker and Docker Compose tested and working
- [ ] Network ports available (80, 443, 3306, 6379, 5672, 48888)
- [ ] Firewall rules configured
- [ ] SSL certificates prepared (optional but recommended)

### Credentials and Secrets

- [ ] Generate strong passwords for:
  - MySQL root password
  - MySQL user password
  - Redis password
  - RabbitMQ password
  - JWT secret key
- [ ] Store credentials securely (not in version control)
- [ ] Create `.env.production` with actual values

---

## Environment Setup

### 1. Generate Strong Credentials

```bash
# Generate random passwords and secrets
openssl rand -base64 32  # MySQL root password
openssl rand -base64 32  # MySQL user password
openssl rand -base64 32  # Redis password
openssl rand -base64 32  # RabbitMQ password
openssl rand -base64 48  # JWT secret (minimum 32 chars for HS256)
```

### 2. Create Environment File

Copy the production environment template:

```bash
cd /path/to/CT-Tibet-WMS
cp .env.production.template .env.production
```

Edit `.env.production` with your actual credentials:

```bash
nano .env.production
```

### 3. Create Data Directories

```bash
mkdir -p data/{mysql,redis,rabbitmq,nginx/cache}
mkdir -p logs/{backend,mysql,nginx,rabbitmq,redis}
mkdir -p uploads
chmod 755 data logs uploads
```

### 4. Prepare Configuration Files

Ensure all configuration files exist:

```bash
# Check required files
ls -la docker-compose.prod.yml
ls -la nginx/nginx.conf
ls -la nginx/nginx-site.conf
ls -la nginx/gzip.conf
ls -la mysql/my.cnf
ls -la rabbitmq/rabbitmq.conf
ls -la redis/redis.conf
ls -la sql/init-database.sql
ls -la backend/src/main/resources/application-prod.yml
ls -la frontend-pc/.env.production
```

---

## Configuration Files

### Docker Compose Configuration

**File**: `docker-compose.prod.yml`

Key features:
- All services configured for production
- Health checks for automatic failure detection
- Resource limits and reservations
- Volume persistence with named volumes
- Custom bridge network for service communication
- JSON file logging with rotation

**Services**:
1. `mysql` - Database server (port 3306)
2. `redis` - Cache server (port 6379)
3. `rabbitmq` - Message queue (ports 5672, 15672)
4. `backend` - Spring Boot API (port 48888)
5. `frontend` - Nginx SPA (port 80/443)

### Backend Configuration

**File**: `backend/src/main/resources/application-prod.yml`

Key configurations:
- HikariCP connection pool (20 max connections)
- Redis Lettuce client pool (8 max active)
- RabbitMQ publisher confirms and consumer retries
- JWT token expiration (2 hours)
- Graceful shutdown timeout (30 seconds)

### Frontend Configuration

**File**: `frontend-pc/.env.production`

Key settings:
- API base URL: `http://localhost:48888/api`
- Token expiration: 7200000 ms (2 hours)
- Max file size: 10MB
- Performance optimizations enabled

### Nginx Configuration

**File**: `nginx/nginx-site.conf`

Features:
- Reverse proxy to backend API
- Static file caching (30 days)
- SPA routing fallback to index.html
- Security headers (X-Frame-Options, X-Content-Type-Options, etc.)
- CORS headers configuration
- Rate limiting zones (10 req/s for API, 50 req/s general)
- HTTPS support (commented out, uncomment when SSL ready)

### Database Initialization

**File**: `sql/init-database.sql`

Contains:
- Database and table creation
- Default roles (SYSTEM_ADMIN, DEPT_ADMIN, WAREHOUSE_ADMIN, USER)
- Default department and admin user
- Indexes for performance optimization
- Views for common queries

---

## Deployment Instructions

### Step 1: Prepare the Server

```bash
# Connect to your production server
ssh user@production-server

# Navigate to project directory
cd /path/to/CT-Tibet-WMS

# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Verify Docker and Docker Compose
docker --version
docker compose version
```

### Step 2: Make Deploy Script Executable

```bash
chmod +x deploy.sh
```

### Step 3: Validate Configuration

```bash
# Check environment file
cat .env.production | grep -v "^#" | grep -v "^$"

# Verify Docker Compose file
docker compose -f docker-compose.prod.yml config
```

### Step 4: Initial Deployment

```bash
# Start deployment
./deploy.sh start

# Wait for services to start (5-10 minutes first time)
# Monitor logs:
./deploy.sh logs

# Check specific service logs:
./deploy.sh logs backend
./deploy.sh logs mysql
./deploy.sh logs frontend
```

### Step 5: Verify Deployment

```bash
# Check service status
./deploy.sh status

# Run health checks
./deploy.sh health

# Check logs for errors
./deploy.sh logs | grep -i error
```

### Step 6: Test Application

```bash
# Test frontend accessibility
curl -I http://localhost/

# Test backend API
curl http://localhost:48888/actuator/health

# Test database connectivity
docker compose -f docker-compose.prod.yml --env-file .env.production exec mysql mysql -u wms_user -p -e "SELECT 1;"

# Test Redis connectivity
docker compose -f docker-compose.prod.yml --env-file .env.production exec redis redis-cli -a <REDIS_PASSWORD> ping

# Test RabbitMQ connectivity
docker compose -f docker-compose.prod.yml --env-file .env.production exec rabbitmq rabbitmq-diagnostics ping
```

---

## Health Checks

### Manual Health Verification

```bash
# Check all services health status
docker compose -f docker-compose.prod.yml ps

# Frontend health
curl http://localhost/health

# Backend health
curl http://localhost:48888/actuator/health

# Database health
docker compose -f docker-compose.prod.yml exec mysql mysqladmin -u wms_user -p ping

# Redis health
docker compose -f docker-compose.prod.yml exec redis redis-cli ping

# RabbitMQ health
docker compose -f docker-compose.prod.yml exec rabbitmq rabbitmq-diagnostics ping
```

### Automated Health Checks

Health checks are configured in `docker-compose.prod.yml`:

- **MySQL**: healthcheck every 10s, 5 retries
- **Redis**: healthcheck every 10s, 5 retries
- **RabbitMQ**: healthcheck every 10s, 5 retries
- **Backend**: healthcheck every 30s with 60s startup grace period
- **Frontend**: healthcheck every 30s with 10s startup grace period

Unhealthy containers are automatically restarted.

---

## Monitoring and Maintenance

### Log Management

```bash
# View real-time logs
./deploy.sh logs

# View backend logs
./deploy.sh logs backend --tail=100

# View frontend logs
./deploy.sh logs frontend --tail=50

# View database logs
./deploy.sh logs mysql

# Save logs to file
./deploy.sh logs > deployment_logs.txt 2>&1
```

### Performance Monitoring

```bash
# View container resource usage
docker stats

# View detailed container information
docker inspect ct-wms-backend-prod

# Monitor specific metrics
docker compose -f docker-compose.prod.yml ps
```

### Regular Maintenance Tasks

#### Daily
- [ ] Check service logs for errors
- [ ] Monitor disk space usage
- [ ] Verify backup completion

#### Weekly
- [ ] Review performance metrics
- [ ] Check for Docker image updates
- [ ] Verify database integrity
- [ ] Test backup restoration

#### Monthly
- [ ] Update Docker images
- [ ] Review and optimize resource usage
- [ ] Check certificate expiration (if using HTTPS)
- [ ] Rotate security credentials

---

## Backup and Disaster Recovery

### Automatic Backups

The `deploy.sh` script automatically creates backups before upgrades:

```bash
# Create manual backup
./deploy.sh backup

# Backups are stored in: ./backups/backup-YYYYMMDD-HHMMSS/
```

### Backup Contents

Each backup includes:
- MySQL database dump
- Redis data snapshot
- Uploaded files archive
- Configuration files

### Restore from Backup

```bash
# List available backups
ls -1d ./backups/backup-*

# Restore from specific backup
./deploy.sh rollback
# Select backup number when prompted
```

### Backup Retention

- Old backups are automatically cleaned up after 30 days
- Modify `BACKUP_RETENTION_DAYS` in `deploy.sh` to change

### Manual Database Backup

```bash
# Backup MySQL
docker compose -f docker-compose.prod.yml exec mysql mysqldump \
  -u root -p<MYSQL_ROOT_PASSWORD> \
  ct_tibet_wms > ct_tibet_wms_backup.sql

# Restore MySQL
docker compose -f docker-compose.prod.yml exec mysql mysql \
  -u root -p<MYSQL_ROOT_PASSWORD> \
  ct_tibet_wms < ct_tibet_wms_backup.sql
```

---

## Troubleshooting

### Services Won't Start

```bash
# Check Docker daemon status
sudo systemctl status docker

# View container logs
./deploy.sh logs

# Check resource constraints
docker stats

# Restart Docker
sudo systemctl restart docker
```

### Database Connection Errors

```bash
# Test database connectivity
docker compose -f docker-compose.prod.yml exec mysql mysql -u wms_user -p -e "SELECT 1;"

# Check MySQL logs
./deploy.sh logs mysql

# Verify network connectivity
docker compose -f docker-compose.prod.yml exec backend ping mysql
```

### Backend Service Failing

```bash
# Check backend logs
./deploy.sh logs backend --tail=200

# Check Java memory usage
docker stats ct-wms-backend-prod

# Increase memory if needed (edit docker-compose.prod.yml)
# Change: memory: 2.5G to higher value
```

### Frontend Not Loading

```bash
# Test frontend container
curl -I http://localhost/

# Check Nginx configuration
docker compose -f docker-compose.prod.yml exec frontend nginx -t

# View Nginx error logs
./deploy.sh logs frontend

# Test backend connectivity from frontend
docker compose -f docker-compose.prod.yml exec frontend curl http://backend:48888/api/health
```

### Port Already in Use

```bash
# Find process using port
lsof -i :80
lsof -i :3306
lsof -i :6379
lsof -i :48888

# Kill process (if necessary)
kill -9 <PID>

# Or change ports in .env.production
```

### Disk Space Issues

```bash
# Check disk usage
df -h

# Clean up old Docker images
docker image prune -a

# Clean up Docker volumes
docker volume prune

# Check container sizes
docker system df
```

---

## Security Hardening

### 1. Enable HTTPS

Update `nginx/nginx-site.conf`:

```nginx
# Uncomment HTTPS server block
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    return 301 https://$server_name$request_uri;
}
```

### 2. Firewall Configuration

```bash
# Allow only necessary ports
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp

# Deny all other inbound traffic
sudo ufw default deny incoming
sudo ufw enable
```

### 3. Database Security

```bash
# Change default admin password
docker compose -f docker-compose.prod.yml exec mysql mysql -u root -p
# ALTER USER 'wms_user'@'%' IDENTIFIED BY 'new_strong_password';

# Restrict database access to Docker network only
# In docker-compose.prod.yml, change mysql ports to:
# ports:
#   - "127.0.0.1:3306:3306"  # Only accessible locally
```

### 4. Credential Management

- Use Docker secrets (for Docker Swarm)
- Use environment variables from secure sources
- Never commit `.env.production` to version control
- Rotate credentials periodically

### 5. Container Security

- All containers run as non-root users
- Read-only file systems where possible
- Resource limits enforced
- Health checks enabled

### 6. API Security

- JWT authentication with 2-hour expiration
- CORS headers configured
- Rate limiting (10 req/s for API)
- Input validation required

---

## Deployment Checklist

### Before Going Live

- [ ] All configuration files created and validated
- [ ] Strong passwords generated for all services
- [ ] SSL certificates obtained (if using HTTPS)
- [ ] Firewall rules configured
- [ ] Backup system tested and working
- [ ] Health checks verified
- [ ] Database initialized with test data
- [ ] Frontend and backend API tested
- [ ] Performance baseline established
- [ ] Monitoring and alerting configured
- [ ] Incident response plan documented
- [ ] Admin user password changed from default

### Production Sign-Off

- [ ] System meets all performance requirements
- [ ] All security requirements implemented
- [ ] Documentation complete
- [ ] Team training completed
- [ ] Backup and recovery procedures tested
- [ ] Monitoring and alerting functional

---

## Common Commands

```bash
# Start services
./deploy.sh start

# Stop services
./deploy.sh stop

# Restart services
./deploy.sh restart

# Check status
./deploy.sh status

# View logs
./deploy.sh logs

# Create backup
./deploy.sh backup

# Upgrade deployment
./deploy.sh upgrade

# Rollback to previous version
./deploy.sh rollback

# Run health checks
./deploy.sh health

# Execute command in backend container
docker compose -f docker-compose.prod.yml exec backend bash

# Execute database query
docker compose -f docker-compose.prod.yml exec mysql mysql -u wms_user -p ct_tibet_wms -e "SELECT * FROM tb_user LIMIT 5;"
```

---

## Support and Documentation

- **API Documentation**: http://localhost:48888/swagger-ui.html (when running)
- **RabbitMQ Management**: http://localhost:15672 (username: wms_admin)
- **MySQL Client**: `mysql -h localhost -u wms_user -p ct_tibet_wms`
- **Redis CLI**: `redis-cli -a <password> -n 0`

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-24 | Initial production deployment guide |

---

**Last Updated**: 2025-11-24
**Maintained By**: CT Development Team
**Contact**: support@ct-wms.com
