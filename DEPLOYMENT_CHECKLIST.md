# CT-Tibet-WMS Production Deployment Checklist

**Project**: CT-Tibet-WMS (西藏电信仓库管理系统)
**Date**: 2025-11-24
**Version**: 1.0.0
**Status**: Ready for Production Deployment

---

## Pre-Deployment Phase

### System Requirements

- [ ] Linux server or VM (Ubuntu 20.04 LTS or later recommended)
- [ ] Minimum 4 CPU cores available
- [ ] Minimum 8 GB RAM available
- [ ] Minimum 50 GB free disk space
- [ ] Root or sudo access on the server

### Software Prerequisites

- [ ] Docker 20.10 or later installed and running
- [ ] Docker Compose 2.0 or later installed
- [ ] Git installed for version control
- [ ] curl installed for API testing
- [ ] mysql-client or similar for database testing

### Network Requirements

- [ ] Port 80 (HTTP) available and not in use
- [ ] Port 443 (HTTPS) available and not in use
- [ ] Port 3306 (MySQL) not exposed to public internet
- [ ] Port 6379 (Redis) not exposed to public internet
- [ ] Port 5672 (RabbitMQ) not exposed to public internet
- [ ] Port 48888 (Backend API) accessible only to frontend or internal network
- [ ] Port 15672 (RabbitMQ Management) protected behind authentication

### Security Preparations

- [ ] SSL/TLS certificates obtained (self-signed or from CA)
- [ ] Firewall rules planned and documented
- [ ] SSH key authentication configured
- [ ] OS security patches applied
- [ ] Unnecessary services disabled on host
- [ ] Regular security scanning tool ready

---

## Configuration Phase

### File Verification

- [ ] `docker-compose.prod.yml` - Docker Compose configuration
- [ ] `.env.production` - Environment variables file
- [ ] `nginx/nginx.conf` - Main Nginx configuration
- [ ] `nginx/nginx-site.conf` - Site-specific configuration
- [ ] `nginx/gzip.conf` - Compression configuration
- [ ] `mysql/my.cnf` - MySQL configuration
- [ ] `redis/redis.conf` - Redis configuration
- [ ] `rabbitmq/rabbitmq.conf` - RabbitMQ configuration
- [ ] `sql/init-database.sql` - Database initialization script
- [ ] `backend/src/main/resources/application-prod.yml` - Spring Boot config
- [ ] `frontend-pc/.env.production` - Frontend environment config
- [ ] `deploy.sh` - Deployment automation script

### Environment Configuration

#### Critical Security Changes (MUST COMPLETE BEFORE DEPLOYMENT)

In `.env.production`, update all default values:

- [ ] **MYSQL_ROOT_PASSWORD**
  - [ ] Generated with: `openssl rand -base64 32`
  - [ ] Changed from default value
  - [ ] Stored securely (not in version control)
  - [ ] Minimum 32 characters
  - [ ] Contains uppercase, lowercase, numbers, special characters

- [ ] **MYSQL_PASSWORD**
  - [ ] Generated with: `openssl rand -base64 32`
  - [ ] Different from root password
  - [ ] Changed from default value
  - [ ] Stored securely

- [ ] **REDIS_PASSWORD**
  - [ ] Generated with: `openssl rand -base64 32`
  - [ ] Changed from default value
  - [ ] Minimum 16 characters
  - [ ] Stored securely

- [ ] **RABBITMQ_PASSWORD**
  - [ ] Generated with: `openssl rand -base64 32`
  - [ ] Changed from default value
  - [ ] Stored securely

- [ ] **JWT_SECRET**
  - [ ] Generated with: `openssl rand -base64 48`
  - [ ] Minimum 32 characters (for HS256)
  - [ ] High entropy random value
  - [ ] Never shared or hardcoded elsewhere
  - [ ] Stored securely

#### Optional Configuration

- [ ] VITE_API_BASE_URL in `frontend-pc/.env.production`
  - [ ] Set to correct domain/IP for production
  - [ ] Verify HTTPS if applicable

- [ ] Nginx SSL certificates (if using HTTPS)
  - [ ] Place in `nginx/ssl/cert.pem` and `nginx/ssl/key.pem`
  - [ ] Verify certificate validity: `openssl x509 -in cert.pem -text -noout`
  - [ ] Uncomment HTTPS sections in `nginx/nginx-site.conf`

### Database Initialization

- [ ] Review `sql/init-database.sql` for correctness
- [ ] Verify default admin user credentials
  - [ ] Username: `admin`
  - [ ] Password: `admin123` (to be changed immediately after first login)
- [ ] Review default roles and permissions
- [ ] Confirm all table definitions are appropriate

---

## Infrastructure Setup Phase

### Directory Structure Creation

- [ ] Create `data/` directory structure
  ```bash
  mkdir -p data/{mysql,redis,rabbitmq,nginx/cache}
  chmod 755 data
  ```

- [ ] Create `logs/` directory structure
  ```bash
  mkdir -p logs/{backend,mysql,nginx,rabbitmq,redis}
  chmod 755 logs
  ```

- [ ] Create `uploads/` directory
  ```bash
  mkdir -p uploads
  chmod 755 uploads
  ```

- [ ] Create `backups/` directory
  ```bash
  mkdir -p backups
  chmod 755 backups
  ```

- [ ] Create `ssl/` directory (for SSL certificates)
  ```bash
  mkdir -p nginx/ssl
  chmod 700 nginx/ssl
  ```

### File Permissions

- [ ] Set executable permission on deploy.sh
  ```bash
  chmod +x deploy.sh
  ```

- [ ] Verify file ownership (should be deploy user, not root)
  ```bash
  ls -la deploy.sh
  ```

### Docker Verification

- [ ] Test Docker daemon
  ```bash
  docker --version
  docker info
  ```

- [ ] Test Docker Compose
  ```bash
  docker compose version
  ```

- [ ] Verify Docker can pull images
  ```bash
  docker pull mysql:8.0.35
  ```

---

## Pre-Deployment Validation Phase

### Configuration Validation

- [ ] Validate `.env.production` file
  ```bash
  grep "change-me" .env.production || echo "All defaults changed"
  ```

- [ ] Validate Docker Compose file
  ```bash
  docker compose -f docker-compose.prod.yml config > /dev/null
  ```

- [ ] Check Nginx configuration syntax
  ```bash
  docker run --rm -v ${PWD}/nginx:/etc/nginx nginx:latest nginx -t
  ```

- [ ] Verify MySQL configuration
  - [ ] Review `mysql/my.cnf` for production suitability
  - [ ] Verify memory settings appropriate for server

- [ ] Verify Redis configuration
  - [ ] Review `redis/redis.conf` for production suitability
  - [ ] Confirm persistence settings

- [ ] Verify RabbitMQ configuration
  - [ ] Review `rabbitmq/rabbitmq.conf` for production suitability
  - [ ] Confirm virtual host configuration

### Network Validation

- [ ] Check required ports are available
  ```bash
  netstat -tuln | grep -E ":80|:443|:3306|:6379|:5672|:48888"
  ```

- [ ] Verify firewall is configured (or plan configuration)
  ```bash
  sudo ufw status
  ```

- [ ] Test network connectivity to Docker daemon
  ```bash
  docker version
  ```

---

## Deployment Phase

### Initial Deployment

- [ ] Run deployment script (first time)
  ```bash
  chmod +x deploy.sh
  ./deploy.sh start
  ```

- [ ] Monitor deployment progress
  ```bash
  ./deploy.sh logs
  ```

- [ ] Wait for all services to be healthy (5-10 minutes typical)
  ```bash
  ./deploy.sh health
  ```

### Service Verification

After deployment completes, verify each service:

#### MySQL Database

- [ ] Check MySQL container is running
  ```bash
  docker ps | grep mysql
  ```

- [ ] Verify database was created
  ```bash
  docker compose exec mysql mysql -u root -p<PASSWORD> -e "SHOW DATABASES;"
  ```

- [ ] Verify tables were created
  ```bash
  docker compose exec mysql mysql -u wms_user -p<PASSWORD> ct_tibet_wms -e "SHOW TABLES;"
  ```

- [ ] Verify default data
  ```bash
  docker compose exec mysql mysql -u wms_user -p<PASSWORD> ct_tibet_wms -e "SELECT COUNT(*) FROM tb_user;"
  ```

#### Redis Cache

- [ ] Check Redis container is running
  ```bash
  docker ps | grep redis
  ```

- [ ] Test Redis connectivity
  ```bash
  docker compose exec redis redis-cli -a <PASSWORD> ping
  ```

- [ ] Verify persistence is enabled
  ```bash
  docker compose exec redis redis-cli -a <PASSWORD> INFO persistence
  ```

#### RabbitMQ Message Queue

- [ ] Check RabbitMQ container is running
  ```bash
  docker ps | grep rabbitmq
  ```

- [ ] Test RabbitMQ connectivity
  ```bash
  docker compose exec rabbitmq rabbitmq-diagnostics ping
  ```

- [ ] Access RabbitMQ Management UI
  ```
  http://localhost:15672
  Username: wms_admin
  Password: <RABBITMQ_PASSWORD>
  ```

#### Backend Service

- [ ] Check backend container is running
  ```bash
  docker ps | grep backend
  ```

- [ ] Test backend health endpoint
  ```bash
  curl http://localhost:48888/actuator/health
  ```

- [ ] Check backend logs for errors
  ```bash
  docker logs ct-wms-backend-prod | grep -i error
  ```

- [ ] Test API endpoint
  ```bash
  curl http://localhost:48888/api/auth/public/version
  ```

#### Frontend Service

- [ ] Check frontend container is running
  ```bash
  docker ps | grep frontend
  ```

- [ ] Test frontend health endpoint
  ```bash
  curl http://localhost/health
  ```

- [ ] Test frontend accessibility
  ```bash
  curl -I http://localhost/
  ```

- [ ] Check Nginx logs for errors
  ```bash
  docker logs ct-wms-frontend-prod 2>&1 | grep -i error
  ```

---

## Application Verification Phase

### Frontend Application

- [ ] Access application in browser
  - [ ] URL: `http://localhost/`
  - [ ] Page loads without errors
  - [ ] CSS and images display correctly

- [ ] Test login functionality
  - [ ] Username: `admin`
  - [ ] Password: `admin123`
  - [ ] Login successful

- [ ] Test core features
  - [ ] Dashboard loads
  - [ ] Navigation menu visible
  - [ ] API requests respond correctly

### Backend API

- [ ] Access API documentation
  - [ ] URL: `http://localhost:48888/swagger-ui.html`
  - [ ] All endpoints documented
  - [ ] Try executing sample requests

- [ ] Test authentication
  - [ ] Login endpoint functional
  - [ ] JWT token generated and valid
  - [ ] Token expiration working

- [ ] Test database operations
  - [ ] Read operations functional
  - [ ] Create operations functional
  - [ ] Update operations functional
  - [ ] Delete operations functional

### File Upload Functionality

- [ ] Test file upload
  - [ ] Upload small file
  - [ ] Verify file saved in `uploads/` directory
  - [ ] Download file
  - [ ] Verify file integrity

### Session and Cache

- [ ] Test Redis caching
  - [ ] Login and verify token cached
  - [ ] Cache TTL working correctly
  - [ ] Logout clears session

- [ ] Test session persistence
  - [ ] Restart backend service
  - [ ] Session data preserved (if applicable)

---

## Post-Deployment Phase

### Backup and Recovery

- [ ] Create initial backup
  ```bash
  ./deploy.sh backup
  ```

- [ ] Verify backup created
  ```bash
  ls -la backups/backup-*/
  ```

- [ ] Test backup restoration
  ```bash
  ./deploy.sh rollback
  # Select first backup when prompted
  ```

- [ ] Verify restored data
  - [ ] Database restored
  - [ ] Files restored
  - [ ] Services functional after restore

### Monitoring Setup

- [ ] Configure log aggregation (if applicable)
  - [ ] Set up centralized logging
  - [ ] Configure log retention
  - [ ] Test log delivery

- [ ] Configure monitoring (if applicable)
  - [ ] Set up performance monitoring
  - [ ] Configure alerting
  - [ ] Test alert notifications

- [ ] Configure backup automation
  - [ ] Schedule daily backups
  - [ ] Configure backup retention
  - [ ] Monitor backup completion

### Documentation

- [ ] Document access procedures
  - [ ] Admin console URLs
  - [ ] Database access methods
  - [ ] Redis/RabbitMQ access

- [ ] Document password management
  - [ ] Store credentials securely
  - [ ] Document password expiration policy
  - [ ] Document password reset procedures

- [ ] Document troubleshooting procedures
  - [ ] Common issues and solutions
  - [ ] Contact information for support
  - [ ] Escalation procedures

### User Administration

- [ ] Change default admin password
  - [ ] Login as admin
  - [ ] Change password to secure value
  - [ ] Verify new password works

- [ ] Create additional admin users (if needed)
  - [ ] Create user accounts
  - [ ] Assign appropriate roles
  - [ ] Verify access levels

- [ ] Configure user permissions
  - [ ] Review role-based access
  - [ ] Verify permissions enforcement
  - [ ] Test permission restrictions

---

## Security Hardening Phase

### Network Security

- [ ] Configure firewall rules
  ```bash
  sudo ufw allow 80/tcp
  sudo ufw allow 443/tcp
  sudo ufw allow 22/tcp
  sudo ufw default deny incoming
  sudo ufw enable
  ```

- [ ] Restrict database access
  - [ ] MySQL only accessible from Docker network
  - [ ] Redis only accessible from Docker network
  - [ ] RabbitMQ only accessible from Docker network

- [ ] Restrict backend API access
  - [ ] Only frontend can access backend API
  - [ ] No public access to internal endpoints

### HTTPS Configuration (if applicable)

- [ ] Install SSL certificates
  - [ ] Copy certificate to `nginx/ssl/cert.pem`
  - [ ] Copy key to `nginx/ssl/key.pem`
  - [ ] Verify permissions (chmod 400)

- [ ] Enable HTTPS in Nginx
  - [ ] Uncomment HTTPS server blocks in `nginx/nginx-site.conf`
  - [ ] Verify configuration: `docker compose exec frontend nginx -t`

- [ ] Configure HTTPS redirect
  - [ ] HTTP redirects to HTTPS
  - [ ] HSTS header configured
  - [ ] Test with browser

- [ ] Set up certificate renewal
  - [ ] Configure auto-renewal (if using Let's Encrypt)
  - [ ] Monitor certificate expiration
  - [ ] Test renewal process

### Authentication Security

- [ ] Enforce password policies
  - [ ] Minimum length: 12 characters
  - [ ] Complexity requirements: uppercase, lowercase, numbers, special chars
  - [ ] Password history: prevent reuse

- [ ] Enable MFA (if supported)
  - [ ] Configure two-factor authentication
  - [ ] Enable for admin accounts
  - [ ] Document MFA procedures

- [ ] Disable default accounts
  - [ ] Disable or delete default `admin` account (if creating new one)
  - [ ] Change all default credentials

### Data Security

- [ ] Enable database encryption (if available)
  - [ ] Configure TLS for database connections
  - [ ] Verify encryption is active

- [ ] Configure backup encryption
  - [ ] Backup files encrypted
  - [ ] Keys stored securely
  - [ ] Test backup decryption

- [ ] Set up data retention policies
  - [ ] Define how long data is kept
  - [ ] Implement automatic cleanup
  - [ ] Document retention requirements

### Access Logging

- [ ] Enable comprehensive logging
  - [ ] Application logs: enabled
  - [ ] Database logs: enabled
  - [ ] Access logs: enabled
  - [ ] System logs: enabled

- [ ] Configure log retention
  - [ ] Logs kept for minimum 30 days
  - [ ] Automatic rotation configured
  - [ ] Disk space monitored

- [ ] Enable audit logging
  - [ ] User actions logged
  - [ ] Data modifications logged
  - [ ] Access attempts logged

---

## Operational Readiness Phase

### Team Training

- [ ] Operations team trained
  - [ ] Deployment procedures understood
  - [ ] Troubleshooting procedures understood
  - [ ] Backup/recovery procedures understood

- [ ] Development team trained
  - [ ] Deployment process understood
  - [ ] Environment-specific configurations understood
  - [ ] Logging and monitoring understood

### Runbooks and Documentation

- [ ] Create operational runbooks
  - [ ] Start/stop procedures
  - [ ] Scaling procedures
  - [ ] Update procedures
  - [ ] Rollback procedures

- [ ] Create troubleshooting guides
  - [ ] Common issues and solutions
  - [ ] Log interpretation
  - [ ] Performance optimization

- [ ] Create emergency procedures
  - [ ] Disaster recovery procedures
  - [ ] Incident response procedures
  - [ ] Escalation procedures

### Incident Management

- [ ] Define incident severity levels
  - [ ] Critical (service down)
  - [ ] Major (degraded service)
  - [ ] Minor (non-critical issues)

- [ ] Create incident response plan
  - [ ] On-call rotation
  - [ ] Escalation procedures
  - [ ] Communication plan

- [ ] Define SLO/SLA
  - [ ] Uptime targets
  - [ ] Response time targets
  - [ ] Recovery time targets

### Change Management

- [ ] Define change control procedures
  - [ ] Change request process
  - [ ] Approval process
  - [ ] Testing requirements
  - [ ] Rollback criteria

- [ ] Create change log
  - [ ] Document all changes
  - [ ] Track deployment history
  - [ ] Maintain audit trail

---

## Go-Live Checklist

### Final Verification

- [ ] All systems operational and tested
- [ ] All services passing health checks
- [ ] Database backup created and verified
- [ ] Disaster recovery tested
- [ ] Security hardening complete
- [ ] Documentation complete
- [ ] Team trained and ready
- [ ] Monitoring and alerting configured
- [ ] Incident response procedures defined
- [ ] Change management procedures defined

### Go-Live Approval

- [ ] Stakeholder sign-off obtained
- [ ] All requirements verified met
- [ ] Risk assessment completed
- [ ] Contingency plans documented
- [ ] Go/No-go decision made

### Go-Live Execution

- [ ] Announce maintenance window (if needed)
- [ ] Start deployment
- [ ] Monitor deployment progress
- [ ] Verify services operational
- [ ] Announce service availability
- [ ] Document deployment completion

### Post Go-Live Monitoring (24-48 hours)

- [ ] Monitor system performance
- [ ] Check error logs for issues
- [ ] Verify backup completion
- [ ] Monitor user activity
- [ ] Respond to user issues
- [ ] Document any problems encountered

---

## Maintenance Schedule

### Daily Tasks

- [ ] Check service logs for errors
- [ ] Monitor disk space usage
- [ ] Verify backup completion
- [ ] Check system resource usage

### Weekly Tasks

- [ ] Review performance metrics
- [ ] Check for Docker image updates
- [ ] Verify database integrity
- [ ] Review access logs
- [ ] Test backup restoration

### Monthly Tasks

- [ ] Update Docker images
- [ ] Review and optimize resource usage
- [ ] Check certificate expiration
- [ ] Rotate security credentials
- [ ] Review and update documentation

### Quarterly Tasks

- [ ] Security audit
- [ ] Capacity planning
- [ ] Disaster recovery drill
- [ ] Performance baseline update

### Annual Tasks

- [ ] Major version updates
- [ ] Architecture review
- [ ] Security assessment
- [ ] Cost optimization review

---

## Rollback Procedures

If issues occur after deployment:

### Quick Rollback

```bash
./deploy.sh rollback
# Select previous backup from list
```

### Manual Rollback

```bash
# Stop current services
./deploy.sh stop

# Restore from backup
# See PRODUCTION_DEPLOYMENT_GUIDE.md for detailed steps

# Restart services
./deploy.sh start
```

---

## Sign-Off

### Deployment Manager

- [ ] Name: ____________________
- [ ] Date: ____________________
- [ ] Signature: ____________________

### Technical Lead

- [ ] Name: ____________________
- [ ] Date: ____________________
- [ ] Signature: ____________________

### Operations Manager

- [ ] Name: ____________________
- [ ] Date: ____________________
- [ ] Signature: ____________________

### Project Manager

- [ ] Name: ____________________
- [ ] Date: ____________________
- [ ] Signature: ____________________

---

## Notes and Issues

```
_______________________________________________________________

_______________________________________________________________

_______________________________________________________________

_______________________________________________________________

_______________________________________________________________
```

---

**Document Version**: 1.0.0
**Created**: 2025-11-24
**Last Updated**: 2025-11-24
**Status**: Ready for Use
**Maintained By**: CT Development Team
**Next Review Date**: 2026-01-24
