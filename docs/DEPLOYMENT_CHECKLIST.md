# CT-Tibet-WMS Production Deployment Checklist

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Target Environment**: Production

## Pre-Deployment Phase

### Infrastructure Verification

- [ ] **Server Hardware**
  - [ ] CPU cores: Minimum 4, actual: ____
  - [ ] RAM: Minimum 8GB, actual: ____
  - [ ] Storage: Minimum 100GB, actual: ____
  - [ ] Network connectivity: _____ Mbps
  - [ ] Data center/cloud region confirmed: _______

- [ ] **Network Configuration**
  - [ ] Public IP/DNS configured: _______
  - [ ] Firewall rules applied
    - [ ] Port 22 (SSH) restricted to admin IPs
    - [ ] Port 80 (HTTP) open to world
    - [ ] Port 443 (HTTPS) open to world
    - [ ] Port 3306 (MySQL) restricted to localhost
    - [ ] Port 6379 (Redis) restricted to localhost
    - [ ] Port 5672 (RabbitMQ) restricted to localhost
  - [ ] Load balancer configured (if applicable)
  - [ ] VPN/Bastion host setup (if applicable)
  - [ ] NAT/Port forwarding configured (if applicable)

- [ ] **Domain Configuration**
  - [ ] DNS A record points to server IP
  - [ ] DNS TTL set appropriately (300-3600 seconds)
  - [ ] DNS propagation verified
  - [ ] Wildcard subdomain configured (if needed)

### Software Prerequisites

- [ ] **Base Operating System**
  - [ ] OS: Linux (Ubuntu 20.04+ / CentOS 8+) or Windows Server 2019+
  - [ ] System updates applied: `apt update && apt upgrade`
  - [ ] Timezone set to Asia/Shanghai: `timedatectl set-timezone Asia/Shanghai`
  - [ ] NTP configured and synced
  - [ ] SELinux/AppArmor reviewed (if applicable)

- [ ] **Required Software Installed**
  - [ ] Java 11+: Version ______, location ______
  - [ ] Maven 3.6+: Version ______, location ______
  - [ ] Node.js 16+: Version ______, location ______
  - [ ] Nginx 1.20+: Version ______, location ______
  - [ ] MySQL 8.0+: Version ______, location ______
  - [ ] Redis 6.0+: Version ______, location ______
  - [ ] RabbitMQ 3.8+: Version ______, location ______
  - [ ] Docker 20.10+ (if Docker deployment): Version ______
  - [ ] Docker Compose 1.29+ (if Docker deployment): Version ______

### Access and Permissions

- [ ] **User Accounts**
  - [ ] Non-root application user created: `wmsapp`
  - [ ] SSH key pair generated and secured
  - [ ] SSH public key deployed to server
  - [ ] Sudo privileges configured appropriately
  - [ ] Password expiration policy: _____ days

- [ ] **Directory Permissions**
  - [ ] `/opt/wms/` created with 755 permissions
  - [ ] `/var/log/wms/` created with 755 permissions
  - [ ] `/data/wms/uploads/` created with 755 permissions
  - [ ] `/etc/wms/` created with 700 permissions
  - [ ] All directories owned by `wmsapp:wmsapp`

### Backup Infrastructure

- [ ] **Backup Storage**
  - [ ] Backup location configured: _______
  - [ ] Backup capacity verified: _____ GB
  - [ ] Backup retention policy defined: _____ days
  - [ ] Backup encryption enabled
  - [ ] Backup path accessible and writable

- [ ] **Backup Testing**
  - [ ] Backup script tested successfully
  - [ ] Restore procedure documented and tested
  - [ ] Recovery time objective (RTO): _____ minutes
  - [ ] Recovery point objective (RPO): _____ minutes

---

## Database Deployment Phase

### MySQL Installation and Configuration

- [ ] **MySQL Service**
  - [ ] MySQL installed and started
  - [ ] MySQL service enabled for auto-start
  - [ ] MySQL running on port 3306
  - [ ] MySQL version confirmed: ______
  - [ ] `mysql_secure_installation` completed

- [ ] **Database and Users**
  - [ ] Database `ct_tibet_wms` created
  - [ ] Application user `wms_user` created
  - [ ] User password set to strong value (minimum 16 characters)
  - [ ] User password stored securely (password manager, vault)
  - [ ] Remote access disabled (only localhost)
  - [ ] Minimal privileges granted (only needed databases)

- [ ] **Database Schema**
  - [ ] Database schema imported successfully
  - [ ] All tables created: Count = _____
  - [ ] Primary keys verified
  - [ ] Foreign keys verified
  - [ ] Indexes created
  - [ ] Initial data populated (admin user, roles, etc.)

- [ ] **MySQL Configuration**
  - [ ] Character set: `utf8mb4`
  - [ ] Collation: `utf8mb4_unicode_ci`
  - [ ] Connection limit: 1000+
  - [ ] InnoDB buffer pool: _____ GB
  - [ ] Query cache disabled
  - [ ] Slow query log enabled
  - [ ] Binary logging enabled (for replication/backups)
  - [ ] Max allowed packet: 256M

- [ ] **Performance Tuning**
  - [ ] Connection pool optimized
  - [ ] Indexes analyzed: `ANALYZE TABLE ...`
  - [ ] Tables optimized: `OPTIMIZE TABLE ...`
  - [ ] Query execution plans reviewed for slow queries
  - [ ] Lock monitoring configured

- [ ] **High Availability Setup (if applicable)**
  - [ ] Replication configured
  - [ ] Master-slave verified synced
  - [ ] Failover tested
  - [ ] Monitoring configured

### MySQL Backup Setup

- [ ] **Automated Backups**
  - [ ] Backup script created and tested
  - [ ] Cron job configured: `0 2 * * * /opt/wms/scripts/backup_database.sh`
  - [ ] Backup location verified
  - [ ] Backup compression enabled (gzip)
  - [ ] Backup retention policy enforced

- [ ] **Backup Verification**
  - [ ] Backup created successfully
  - [ ] Backup size is reasonable: _____ MB
  - [ ] Backup can be restored (test on staging)
  - [ ] Backup encryption enabled (if required)

---

## Backend Deployment Phase

### Java and Maven Setup

- [ ] **Java Environment**
  - [ ] Java 11 installed: Version ______
  - [ ] JAVA_HOME environment variable set
  - [ ] Java PATH configured
  - [ ] Java version verified: `java -version`

- [ ] **Maven Build**
  - [ ] Maven 3.6+ installed
  - [ ] Maven settings.xml configured (if needed)
  - [ ] Local repository cache warmed up
  - [ ] Build tested successfully
  - [ ] Build artifacts verified: `ct-tibet-wms.jar` present and correct size

### Backend Application Deployment

- [ ] **Application Files**
  - [ ] JAR file copied to `/opt/wms/backend/`
  - [ ] JAR file ownership: `wmsapp:wmsapp`
  - [ ] JAR file permissions: 755
  - [ ] File size verified: _____ MB
  - [ ] File checksum verified (optional)

- [ ] **Application Configuration**
  - [ ] Configuration file created: `/etc/wms/application-prod.yml`
  - [ ] Database connection string configured
  - [ ] Database user/password configured securely
  - [ ] Redis host/port/password configured
  - [ ] RabbitMQ host/port/credentials configured
  - [ ] JWT secret configured (strong, randomly generated)
  - [ ] File upload path configured: `/data/wms/uploads`
  - [ ] All passwords and secrets externalized (not in JAR)

- [ ] **Startup Scripts**
  - [ ] Start script created: `/opt/wms/backend/start.sh`
  - [ ] Stop script created: `/opt/wms/backend/stop.sh`
  - [ ] Scripts are executable: `chmod +x`
  - [ ] Scripts tested and working
  - [ ] Graceful shutdown implemented
  - [ ] Health check implemented

- [ ] **JVM Configuration**
  - [ ] Heap size configured: `-Xms512m -Xmx2g`
  - [ ] Garbage collector: G1GC configured
  - [ ] GC logging enabled
  - [ ] Memory monitoring configured
  - [ ] Heap dump on OOM enabled

- [ ] **Systemd Service**
  - [ ] Service file created: `/etc/systemd/system/wms-backend.service`
  - [ ] Service enabled: `systemctl enable wms-backend.service`
  - [ ] Service started: `systemctl start wms-backend.service`
  - [ ] Service status verified: `systemctl status wms-backend.service`
  - [ ] Service auto-restart configured
  - [ ] Dependencies set correctly (After=mysql redis-server rabbitmq)

### Backend Health Verification

- [ ] **Application Startup**
  - [ ] Application starts without errors
  - [ ] Startup time: _____ seconds
  - [ ] No exceptions in logs
  - [ ] All Spring beans loaded
  - [ ] Database connectivity verified
  - [ ] Redis connectivity verified
  - [ ] RabbitMQ connectivity verified

- [ ] **Health Endpoints**
  - [ ] `/actuator/health` responds with UP status
  - [ ] `/actuator/info` returns application info
  - [ ] `/actuator/metrics` accessible
  - [ ] Health check includes database status
  - [ ] Health check includes Redis status

- [ ] **API Verification**
  - [ ] `/api/system/health` endpoint works
  - [ ] Authentication endpoint works
  - [ ] Token generation successful
  - [ ] Sample API calls successful
  - [ ] CORS configured if needed

---

## Frontend Deployment Phase

### Node.js and npm Setup

- [ ] **Node.js Environment**
  - [ ] Node.js 16+ installed: Version ______
  - [ ] npm installed and updated
  - [ ] npm cache cleared
  - [ ] Node PATH configured

### Frontend Build and Deployment

- [ ] **Frontend Build**
  - [ ] Dependencies installed: `npm ci`
  - [ ] Build completed: `npm run build`
  - [ ] Build artifacts in `dist/` directory
  - [ ] Build size verified: _____ MB
  - [ ] Source maps disabled for production

- [ ] **Frontend Files Deployment**
  - [ ] `dist/` contents copied to `/var/www/ct-tibet-wms/`
  - [ ] File ownership: `www-data:www-data`
  - [ ] Directory permissions: 755, file permissions: 644
  - [ ] All required files present (index.html, CSS, JS, assets)
  - [ ] No sensitive files exposed

- [ ] **Nginx Configuration**
  - [ ] Nginx configuration file created
  - [ ] Gzip compression enabled
  - [ ] Caching headers configured
  - [ ] Security headers configured
  - [ ] API proxy configured to backend
  - [ ] SPA fallback to index.html configured
  - [ ] Static file caching: 30 days for versioned files

- [ ] **Nginx Service**
  - [ ] Nginx configuration tested: `nginx -t`
  - [ ] Nginx service started: `systemctl start nginx`
  - [ ] Nginx service enabled: `systemctl enable nginx`
  - [ ] Nginx listening on port 80
  - [ ] Nginx status verified: `systemctl status nginx`

- [ ] **Frontend Health Verification**
  - [ ] Frontend accessible at http://server-ip/
  - [ ] Home page loads successfully
  - [ ] Static resources load (CSS, JS, images)
  - [ ] No 404 errors in browser console
  - [ ] No CORS errors
  - [ ] API communication working
  - [ ] Responsive design verified on multiple device sizes

---

## Security Configuration Phase

### SSL/TLS Certificate

- [ ] **HTTPS Setup**
  - [ ] SSL certificate obtained
  - [ ] Certificate type: Self-signed / CA-signed (circle one)
  - [ ] Certificate validity: From _____ to _____
  - [ ] Certificate path: `/etc/nginx/ssl/cert.pem`
  - [ ] Private key path: `/etc/nginx/ssl/key.pem`
  - [ ] Private key permissions: 400 (read-only by root)
  - [ ] Certificate chain included (if applicable)

- [ ] **Certificate Renewal (if CA-signed)**
  - [ ] Certificate renewal process documented
  - [ ] Auto-renewal configured (if supported)
  - [ ] Reminder 30 days before expiry: _____ (email/alert configured)
  - [ ] Staging certificate tested before production

- [ ] **HTTPS Configuration**
  - [ ] TLS 1.2+ enforced
  - [ ] TLS 1.0 and 1.1 disabled
  - [ ] Strong cipher suites configured
  - [ ] SSL session caching enabled
  - [ ] HTTP to HTTPS redirect configured (301)
  - [ ] HSTS header configured: `max-age=31536000`

### Firewall and Access Control

- [ ] **Firewall Rules**
  - [ ] UFW enabled (if using)
  - [ ] SSH (22): Restricted to admin IPs only
  - [ ] HTTP (80): Open to world
  - [ ] HTTPS (443): Open to world
  - [ ] MySQL (3306): Locked to localhost only
  - [ ] Redis (6379): Locked to localhost only
  - [ ] RabbitMQ (5672): Locked to localhost only
  - [ ] Firewall status: `sudo ufw status`

- [ ] **Network Security**
  - [ ] Private ports not exposed to internet
  - [ ] Database not accessible from public network
  - [ ] Admin panels restricted by IP (if applicable)
  - [ ] Rate limiting configured on Nginx
  - [ ] DDoS protection configured (if applicable)

### Authentication and Secrets Management

- [ ] **Credentials Security**
  - [ ] All passwords changed from defaults
  - [ ] All passwords stored securely (password manager/vault)
  - [ ] API credentials/tokens rotated
  - [ ] No credentials in source code or config files
  - [ ] Environment variables used for sensitive data
  - [ ] Secrets vault configured (if applicable)

- [ ] **JWT Configuration**
  - [ ] JWT secret randomly generated (minimum 32 characters)
  - [ ] JWT secret changed from default
  - [ ] JWT expiration set: 2 hours
  - [ ] Refresh token expiration set: 7 days
  - [ ] JWT validation tested

- [ ] **Database Credentials**
  - [ ] Root password strong and changed
  - [ ] Application user password strong
  - [ ] All credentials externalized (not in code)
  - [ ] Database credentials in environment variables only

### Application Security

- [ ] **Security Headers**
  - [ ] X-Frame-Options: SAMEORIGIN
  - [ ] X-Content-Type-Options: nosniff
  - [ ] X-XSS-Protection: 1; mode=block
  - [ ] Referrer-Policy: strict-origin-when-cross-origin
  - [ ] Content-Security-Policy configured
  - [ ] CORS headers configured (restrictive)

- [ ] **Input Validation**
  - [ ] All inputs validated on backend
  - [ ] SQL injection prevention (parameterized queries)
  - [ ] XSS prevention (HTML escaping)
  - [ ] CSRF tokens implemented
  - [ ] File upload validation (type, size)

- [ ] **API Security**
  - [ ] API authentication required
  - [ ] API authorization checks in place
  - [ ] Rate limiting configured
  - [ ] API versioning implemented (optional but recommended)
  - [ ] Sensitive data not logged
  - [ ] API endpoints not exposing internal errors

### Security Scanning and Testing

- [ ] **Vulnerability Scanning**
  - [ ] Dependency scanning completed (OWASP Dependency-Check)
  - [ ] Container image scanned (Trivy)
  - [ ] No critical vulnerabilities
  - [ ] High severity vulnerabilities documented and planned
  - [ ] Regular scanning scheduled (weekly minimum)

- [ ] **Penetration Testing (Optional)**
  - [ ] Penetration test scheduled/completed
  - [ ] Test results documented
  - [ ] Vulnerabilities remediated
  - [ ] Remediation verified

---

## Redis Cache Configuration

- [ ] **Redis Installation**
  - [ ] Redis installed: Version ______
  - [ ] Redis service started and enabled
  - [ ] Redis port: 6379
  - [ ] Redis password configured (strong)

- [ ] **Redis Configuration**
  - [ ] Persistence enabled: AOF or RDB
  - [ ] Memory limit configured: _____ MB
  - [ ] Memory eviction policy: `allkeys-lru`
  - [ ] Only localhost connections allowed
  - [ ] Slow log configured

- [ ] **Redis Testing**
  - [ ] Redis connectivity verified: `redis-cli ping`
  - [ ] Authentication working: `redis-cli -a password ping`
  - [ ] Data persistence tested
  - [ ] Backup tested

---

## RabbitMQ Message Queue Configuration

- [ ] **RabbitMQ Installation**
  - [ ] RabbitMQ installed: Version ______
  - [ ] RabbitMQ service started and enabled
  - [ ] RabbitMQ port: 5672
  - [ ] Management UI port: 15672 (optional, secured)

- [ ] **RabbitMQ Configuration**
  - [ ] Admin user created: `wms_admin`
  - [ ] Admin password set (strong)
  - [ ] Virtual host created: `/wms`
  - [ ] Queues configured for notifications
  - [ ] Exchanges configured
  - [ ] Bindings configured

- [ ] **RabbitMQ Testing**
  - [ ] Connection verified
  - [ ] Test message published and consumed
  - [ ] Message persistence tested
  - [ ] Backup tested

---

## Monitoring and Logging Setup

### Log Configuration

- [ ] **Application Logging**
  - [ ] Log directory: `/var/log/wms/`
  - [ ] Log files: all.log, error.log, business.log
  - [ ] Log rotation configured
  - [ ] Log retention: _____ days
  - [ ] Log level: INFO (or WARN for production)

- [ ] **Nginx Logging**
  - [ ] Access log: `/var/log/nginx/ct-tibet-wms-access.log`
  - [ ] Error log: `/var/log/nginx/ct-tibet-wms-error.log`
  - [ ] Log rotation configured
  - [ ] Log format configured

- [ ] **System Logging**
  - [ ] Systemd logs: `journalctl -u wms-backend.service`
  - [ ] MySQL logs: `/var/log/mysql/error.log`
  - [ ] Log aggregation configured (if applicable)

### Monitoring Setup

- [ ] **Application Monitoring**
  - [ ] Application health endpoint monitored
  - [ ] Response time baseline established: _____ ms average
  - [ ] Error rate baseline established: _____ % target
  - [ ] CPU usage monitored: _____ % threshold
  - [ ] Memory usage monitored: _____ % threshold

- [ ] **Infrastructure Monitoring**
  - [ ] Disk space monitored: Alert at 80%
  - [ ] Memory usage monitored: Alert at 90%
  - [ ] CPU usage monitored: Alert at 80%
  - [ ] Network usage monitored
  - [ ] Service availability monitored

- [ ] **Database Monitoring**
  - [ ] Connection count monitored
  - [ ] Slow query log monitored
  - [ ] Disk space monitored
  - [ ] Replication lag monitored (if applicable)

### Alerting Configuration

- [ ] **Alert Channels**
  - [ ] Email configured: _______
  - [ ] Slack configured (if applicable)
  - [ ] PagerDuty configured (if applicable)
  - [ ] SMS alerts configured (if applicable)

- [ ] **Alert Rules**
  - [ ] Service down: Alert immediately
  - [ ] Health check failure: Alert immediately
  - [ ] Disk space critical (>90%): Alert immediately
  - [ ] Memory high (>80%): Alert after 5 minutes
  - [ ] Error rate spike: Alert if >5% increase
  - [ ] Response time slow (>2s): Alert after 5 minutes

---

## Performance Optimization

### Response Time

- [ ] **Baseline Metrics**
  - [ ] API response time average: _____ ms
  - [ ] API response time P95: _____ ms
  - [ ] API response time P99: _____ ms
  - [ ] Page load time: _____ ms

- [ ] **Optimization**
  - [ ] Database queries optimized
  - [ ] Database indexes verified
  - [ ] Caching configured
  - [ ] CDN configured (if applicable)
  - [ ] Compression enabled

### Load Testing

- [ ] **Load Test Conducted**
  - [ ] Test date: _______
  - [ ] Test tool: _____ (JMeter, Load Runner, etc.)
  - [ ] Concurrent users tested: _____
  - [ ] Peak load response time: _____ ms
  - [ ] Error rate at peak: _____ %
  - [ ] Bottleneck identified: _______

- [ ] **Test Results**
  - [ ] System handles expected load
  - [ ] Graceful degradation configured
  - [ ] Scaling plan documented
  - [ ] Load balancing configured (if applicable)

---

## Data and Configuration

### Data Migration

- [ ] **Master Data**
  - [ ] User accounts migrated/created
  - [ ] Roles and permissions configured
  - [ ] Departments configured
  - [ ] Materials/products master data loaded
  - [ ] Warehouses configured

- [ ] **Historical Data (if applicable)**
  - [ ] Previous transaction data migrated (if needed)
  - [ ] Data validation completed
  - [ ] Data integrity verified
  - [ ] Archive strategy configured

### Configuration Files

- [ ] **Environment Configuration**
  - [ ] `.env` file created and secured
  - [ ] All environment variables documented
  - [ ] Configuration file backups: _____ location
  - [ ] Configuration version controlled (git)
  - [ ] Sensitive data not in git (use .env)

- [ ] **Application Configuration**
  - [ ] Database connection pool tuned
  - [ ] File upload path configured
  - [ ] Temp directory configured
  - [ ] Cache settings configured
  - [ ] Session timeout configured

---

## Deployment Readiness

### Code Readiness

- [ ] **Code Quality**
  - [ ] Unit tests: 100% of code coverage requirement met
  - [ ] Integration tests passed
  - [ ] Code review completed
  - [ ] No critical code issues
  - [ ] No security warnings

- [ ] **Dependencies**
  - [ ] All dependencies locked (pom.xml, package-lock.json)
  - [ ] Dependency licenses reviewed (if required)
  - [ ] No vulnerable dependencies
  - [ ] No deprecated dependencies

- [ ] **Documentation**
  - [ ] README.md updated
  - [ ] API documentation current
  - [ ] Deployment procedure documented
  - [ ] Troubleshooting guide prepared
  - [ ] Change log documented

### Deployment Plan

- [ ] **Deployment Procedure**
  - [ ] Step-by-step deployment procedure documented
  - [ ] Rollback procedure documented and tested
  - [ ] Estimated downtime: _____ minutes (or zero-downtime deployment configured)
  - [ ] Deployment window scheduled: _______
  - [ ] Team members assigned roles
  - [ ] Communication plan prepared

- [ ] **Deployment Team**
  - [ ] Team lead: _______
  - [ ] Backend engineer: _______
  - [ ] Frontend engineer: _______
  - [ ] Database administrator: _______
  - [ ] System administrator: _______
  - [ ] Backup contact: _______

- [ ] **Deployment Communication**
  - [ ] Stakeholders notified of deployment schedule
  - [ ] Status update channel configured (Slack, email)
  - [ ] Post-deployment testing plan defined
  - [ ] Rollback decision criteria defined

---

## Go-Live Phase

### Pre-Deployment (T-24 Hours)

- [ ] **Final Verification**
  - [ ] All checklist items completed
  - [ ] Database backup verified
  - [ ] Rollback scripts tested
  - [ ] Team briefing completed
  - [ ] Monitoring systems verified
  - [ ] Alert channels tested

- [ ] **Staging Validation**
  - [ ] Staging environment mirrors production
  - [ ] Full regression testing completed
  - [ ] Performance testing completed
  - [ ] Security scanning completed

### Deployment Day (T-0)

- [ ] **Pre-Deployment (T-30 min)**
  - [ ] Final database backup
  - [ ] Team standup completed
  - [ ] Communication channel open
  - [ ] Monitoring dashboard prepared
  - [ ] Rollback plan briefed

- [ ] **Deployment Execution**
  - [ ] Deployment started at scheduled time
  - [ ] Services brought up in correct order
  - [ ] Health checks passed
  - [ ] Database connectivity verified
  - [ ] All services reporting healthy

- [ ] **Post-Deployment (T+30 min)**
  - [ ] Frontend accessible and functional
  - [ ] API endpoints responding
  - [ ] Database operations working
  - [ ] Cache working
  - [ ] Message queue working
  - [ ] File uploads working
  - [ ] Authentication working
  - [ ] No critical errors in logs

- [ ] **Smoke Testing**
  - [ ] Homepage loads
  - [ ] Login functionality works
  - [ ] Main workflows tested (inbound, outbound, approvals)
  - [ ] Database queries responsive
  - [ ] Search functionality working
  - [ ] Report generation working

### Post-Deployment (T+2 Hours)

- [ ] **Stability Monitoring**
  - [ ] Error rate stable
  - [ ] Response times acceptable
  - [ ] No memory leaks
  - [ ] No database connection issues
  - [ ] No authentication issues

- [ ] **User Validation**
  - [ ] Sample users can log in
  - [ ] Business processes working
  - [ ] No user reports of issues
  - [ ] Performance acceptable

- [ ] **Sign-Off**
  - [ ] Project manager approval
  - [ ] Business owner approval
  - [ ] System owner sign-off
  - [ ] Deployment team debriefing completed

---

## Post-Deployment Phase

### Day 1-7 Monitoring

- [ ] **Ongoing Monitoring**
  - [ ] Daily health check reviews
  - [ ] Daily error log reviews
  - [ ] Daily performance metric reviews
  - [ ] User feedback tracked
  - [ ] Support tickets monitored

- [ ] **Issue Resolution**
  - [ ] Any issues documented
  - [ ] Issues prioritized and assigned
  - [ ] Hot fixes applied as needed
  - [ ] Root cause analysis performed

### Week 2-4 Stabilization

- [ ] **Performance Analysis**
  - [ ] Actual usage patterns analyzed
  - [ ] Performance baselines adjusted as needed
  - [ ] Optimization opportunities identified
  - [ ] Database growth analyzed

- [ ] **Refinements**
  - [ ] Minor bugs fixed
  - [ ] Performance optimizations applied
  - [ ] Configuration adjustments made
  - [ ] Documentation updated

### Month 1+ Operations

- [ ] **Operational Excellence**
  - [ ] Runbook finalized based on real experience
  - [ ] Alert thresholds fine-tuned
  - [ ] Backup/restore procedures validated
  - [ ] Capacity planning for growth
  - [ ] Regular security updates scheduled

- [ ] **Continuous Improvement**
  - [ ] Lessons learned documented
  - [ ] Process improvements identified
  - [ ] Training materials developed
  - [ ] Handoff to operations team completed

---

## Sign-Off

**Deployment Approved By:**

- [ ] **Infrastructure Team**
  - Name: _________________
  - Date: _________________
  - Signature: _____________

- [ ] **Development Lead**
  - Name: _________________
  - Date: _________________
  - Signature: _____________

- [ ] **Project Manager**
  - Name: _________________
  - Date: _________________
  - Signature: _____________

- [ ] **Business Owner/Stakeholder**
  - Name: _________________
  - Date: _________________
  - Signature: _____________

---

## Contact Information

| Role | Name | Phone | Email |
|------|------|-------|-------|
| Infrastructure Lead | | | |
| Backend Lead | | | |
| Frontend Lead | | | |
| Database Administrator | | | |
| On-Call Support | | | |

---

## Appendix: Quick Reference

### Critical Paths

**Start Services:**
```bash
sudo systemctl start mysql
sudo systemctl start redis-server
sudo systemctl start rabbitmq-server
sudo systemctl start nginx
sudo systemctl start wms-backend.service
```

**Health Check:**
```bash
curl http://localhost:48888/actuator/health
curl http://localhost/health
mysql -u wms_user -p ct_tibet_wms -e "SELECT 1;"
redis-cli ping
```

**Emergency Rollback:**
```bash
# Stop services
sudo systemctl stop wms-backend.service
# Restore backup
mysql -u root -p ct_tibet_wms < backup.sql
# Restart
sudo systemctl start wms-backend.service
```

### Important Passwords/Secrets Locations

- Database: `/etc/wms/application-prod.yml` or `.env`
- Redis: `.env` file
- JWT Secret: `.env` file
- SSL Certificate: `/etc/nginx/ssl/`

**All sensitive information should be managed by a secrets management system.**

---

**Last Updated**: 2025-11-17
**Maintained By**: CT Development Team
**Next Review Date**: _______

---

**Print this checklist and retain a signed copy in your records.**
