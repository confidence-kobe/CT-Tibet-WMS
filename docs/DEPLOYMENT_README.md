# CT-Tibet-WMS Production Deployment Documentation

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Project**: CT-Tibet-WMS (西藏电信仓库管理系统)
**Status**: Ready for Production Deployment

## Quick Start

Choose your deployment method:

1. **Docker Deployment** (Recommended for most environments)
   - Fastest setup
   - Production-ready multi-container orchestration
   - See: [DEPLOYMENT_DOCKER.md](./DEPLOYMENT_DOCKER.md)

2. **Manual Deployment** (Traditional server setup)
   - Full control over each component
   - Direct service management
   - See: [DEPLOYMENT_MANUAL.md](./DEPLOYMENT_MANUAL.md)

3. **CI/CD Pipeline** (Continuous Deployment)
   - Automated build and deploy
   - GitHub Actions integration
   - See: [DEPLOYMENT_CICD.md](./DEPLOYMENT_CICD.md)

4. **Pre-Deployment Verification**
   - Comprehensive checklist
   - Production readiness
   - See: [DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md)

---

## Documentation Structure

### 1. DEPLOYMENT_DOCKER.md
**Target**: Docker and Docker Compose deployment
**Contents**:
- Prerequisites and installation
- Dockerfile for backend (Spring Boot)
- Dockerfile for frontend (Vue 3 + Nginx)
- Docker Compose configuration
- Environment variables for all stages
- Container management procedures
- Security hardening
- Troubleshooting guide

**Best For**:
- Cloud deployments (AWS, Azure, GCP)
- Kubernetes-ready architecture
- Microservices environments
- Development to production consistency

**Time to Deploy**: 15-30 minutes (after prerequisites)

---

### 2. DEPLOYMENT_MANUAL.md
**Target**: Direct server deployment without containers
**Contents**:
- System requirements and setup
- Database installation and configuration
- Backend deployment with Maven
- Frontend deployment with Nginx
- Service management with systemd
- Log management and rotation
- Backup and recovery procedures
- Performance tuning
- Troubleshooting guide

**Best For**:
- On-premises servers
- Legacy infrastructure
- Organizations without Docker experience
- Custom infrastructure requirements

**Time to Deploy**: 45-90 minutes (depending on system setup)

---

### 3. DEPLOYMENT_CICD.md
**Target**: GitHub Actions CI/CD pipeline
**Contents**:
- CI/CD architecture overview
- Complete GitHub Actions workflow
- Build pipeline (backend and frontend)
- Test pipeline with coverage
- Multi-environment deployment (dev/staging/prod)
- Security scanning integration
- Artifact management
- Rollback strategies
- Performance monitoring

**Best For**:
- GitHub-based projects
- Teams practicing continuous deployment
- Automated testing and deployment
- Audit trail and compliance requirements

**Key Features**:
- Automated on every push to main/develop
- Security scanning before deployment
- Manual approval for production
- Automated rollback on failure
- Slack notifications

**Time to Setup**: 30-45 minutes (secrets and runners)

---

### 4. DEPLOYMENT_CHECKLIST.md
**Target**: Pre-deployment verification and sign-off
**Contents**:
- Infrastructure verification
- Software prerequisites
- Access and permissions
- Database configuration
- Backend deployment verification
- Frontend deployment verification
- Security hardening checklist
- Monitoring and logging setup
- Performance optimization
- Go-live procedures
- Post-deployment validation
- Team sign-off section

**Best For**:
- Final pre-deployment review
- Enterprise deployments
- Change management processes
- Compliance and audit documentation

**Purpose**: Print this document and use it as your deployment checklist. Verify each item before going live.

---

## Application Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (Vue 3)                    │
│            Port: 80/443 (HTTP/HTTPS)                    │
│   - Single Page Application with Element Plus UI       │
│   - 29 pages implemented                               │
│   - Responsive design                                  │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             │
┌──────────────────────┐             │
│   Nginx Reverse      │             │
│   Proxy & Cache      │             │
│ (Static files serve) │             │
└──────────┬───────────┘             │
           │                         │
        ┌──┴──────────────────────────┴──┐
        │                               │
        ▼                               ▼
    /api routes                    /static routes
        │                               │
        ▼                               ▼
┌───────────────────────────────────────────┐
│     Backend (Spring Boot 2.7.18)          │
│       Port: 48888                        │
│   - REST APIs                            │
│   - Business logic                       │
│   - User & role management               │
│   - Inbound/Outbound operations          │
└───────────────┬───────────────────────────┘
                │
    ┌───────────┼───────────┬──────────────┐
    │           │           │              │
    ▼           ▼           ▼              ▼
┌─────────┐ ┌──────────┐ ┌──────┐ ┌─────────────┐
│ MySQL   │ │ Redis    │ │Rabbit│ │ File System │
│Database │ │Cache     │ │ MQ   │ │ (Uploads)   │
│ Port:   │ │ Port:    │ │ Port:│ │ /data/wms/  │
│ 3306    │ │ 6379     │ │5672  │ │             │
└─────────┘ └──────────┘ └──────┘ └─────────────┘
```

---

## System Requirements Summary

### Minimum Production Setup
- **CPU**: 4 cores
- **RAM**: 8GB
- **Storage**: 100GB SSD
- **Network**: 1Gbps, static IP

### Recommended Production Setup
- **CPU**: 8+ cores
- **RAM**: 16GB+
- **Storage**: 200GB+ SSD with 50% reserved for data/backups
- **Network**: 1Gbps, redundant connections

### Software Versions
| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 11+ | Backend JVM |
| Maven | 3.6+ | Build tool |
| Node.js | 16+ | Frontend build |
| Nginx | 1.20+ | Web server & reverse proxy |
| MySQL | 8.0+ | Database |
| Redis | 6.0+ | Cache & session |
| RabbitMQ | 3.8+ | Message queue |
| Docker | 20.10+ | Container runtime (optional) |

---

## Deployment Decision Tree

```
┌─ Are you using Docker/Kubernetes?
│  ├─ YES → Use DEPLOYMENT_DOCKER.md
│  └─ NO ──┐
│          └─ Do you have CI/CD pipeline?
│             ├─ YES (GitHub) → Use DEPLOYMENT_CICD.md
│             └─ NO → Use DEPLOYMENT_MANUAL.md
│
└─ Always use DEPLOYMENT_CHECKLIST.md before going live
```

---

## Key Configuration Parameters

### Backend Application
```yaml
Server Port: 48888
Database: MySQL 8.0+
Database Name: ct_tibet_wms
Database User: wms_user
Cache: Redis (6379)
Message Queue: RabbitMQ (5672)
Uploads Path: /data/wms/uploads
Logs Path: /var/log/wms/
Max Upload Size: 10MB
Session Timeout: 2 hours (configurable)
```

### Frontend Application
```
Frontend Port: 80 (HTTP) / 443 (HTTPS)
Build Output: dist/
Serves: Static files + SPA
Proxies API: /api → http://backend:48888/api
Cache Policy: 30 days for versioned assets
Compression: gzip enabled
```

### Database
```
Character Set: utf8mb4
Collation: utf8mb4_unicode_ci
Connection Pool: 10-30 connections
Slow Query Log: Enabled (2s threshold)
Binary Logging: Enabled (for backups)
Backup Schedule: Daily at 2:00 AM
Retention: 30 days
```

---

## Security Highlights

### Network Security
- [ ] Firewall restricts port access
- [ ] Database only accessible internally
- [ ] Redis/RabbitMQ only on localhost
- [ ] SSH key-based authentication only
- [ ] DDoS protection configured

### Application Security
- [ ] HTTPS/TLS configured
- [ ] JWT authentication
- [ ] RBAC (Role-Based Access Control)
- [ ] Input validation on all APIs
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS protection (HTML escaping)
- [ ] CSRF token validation

### Secrets Management
- [ ] All secrets externalized from code
- [ ] Strong password policies (16+ characters)
- [ ] Secrets stored securely (vault/secrets manager)
- [ ] Regular secret rotation
- [ ] Audit logging for secret access

### Compliance
- [ ] Data encryption in transit (HTTPS)
- [ ] Secure database backups
- [ ] Access audit logging
- [ ] Compliance with security standards

---

## High-Level Deployment Procedures

### Docker Deployment (Recommended)

```bash
# 1. Prepare environment
cd H:/java/CT-Tibet-WMS
cp .env.production .env
# Edit .env with your credentials

# 2. Build images
docker compose build

# 3. Start services
docker compose up -d

# 4. Verify
docker compose ps
curl http://localhost:48888/actuator/health
curl http://localhost/health
```

**Total time**: 15-30 minutes

---

### Manual Deployment

```bash
# 1. Install dependencies (Java, Node, Nginx, MySQL, Redis, RabbitMQ)

# 2. Configure MySQL
sudo mysql < database_schema.sql

# 3. Build and deploy backend
cd backend
mvn clean package
sudo cp target/ct-tibet-wms.jar /opt/wms/backend/
sudo systemctl start wms-backend

# 4. Build and deploy frontend
cd ../frontend-pc
npm ci && npm run build
sudo cp -r dist/* /var/www/ct-tibet-wms/
sudo systemctl start nginx

# 5. Verify
curl http://localhost:48888/actuator/health
curl http://localhost/health
```

**Total time**: 45-90 minutes

---

### CI/CD Pipeline Deployment

```bash
# 1. Setup GitHub Actions secrets
Settings > Secrets and Variables > Actions
Add: REGISTRY_USERNAME, REGISTRY_PASSWORD, DEPLOY_KEYS, etc.

# 2. Push to main branch
git push origin main

# 3. GitHub Actions automatically:
# - Runs tests
# - Scans for vulnerabilities
# - Builds Docker images
# - Deploys to staging
# - Waits for manual approval

# 4. Approve production deployment
# - GitHub UI shows "Review deployments"
# - Click "Review deployments" > "Approve and deploy"

# 5. Automatic deployment to production
```

**Total time**: 5-15 minutes (automated)

---

## Verification Checklist

After deployment, verify:

```bash
# Backend Health
curl http://localhost:48888/actuator/health
Response should include: {"status":"UP"}

# Frontend Accessibility
curl http://localhost/
Response should be HTML (200 status)

# Database
mysql -u wms_user -p ct_tibet_wms -e "SELECT COUNT(*) FROM tb_user;"
Should show: | COUNT(*) |
             |    1     |  (or more)

# Redis
redis-cli -a password ping
Response should be: PONG

# RabbitMQ
docker/direct access to management UI at http://localhost:15672

# Logs
tail -f /var/log/wms/wms.log
Should show: "Starting application" or "Application started"
```

---

## Monitoring and Maintenance

### Daily Tasks
- [ ] Check application health: `curl http://localhost:48888/actuator/health`
- [ ] Review error logs: `tail -f /var/log/wms/error.log`
- [ ] Monitor disk space: `df -h`
- [ ] Monitor memory: `free -h`

### Weekly Tasks
- [ ] Verify backups completed successfully
- [ ] Check security updates available
- [ ] Review slow query log
- [ ] Analyze application metrics

### Monthly Tasks
- [ ] Full system backup test (restore on staging)
- [ ] Security patch updates
- [ ] Database maintenance (ANALYZE, OPTIMIZE)
- [ ] Capacity planning review
- [ ] Performance optimization

### Quarterly Tasks
- [ ] Security audit
- [ ] Disaster recovery drill
- [ ] Load testing
- [ ] Documentation review and update

---

## Troubleshooting Quick Links

| Issue | Check | Solution |
|-------|-------|----------|
| Backend won't start | Port 48888 | Change port or kill process using it |
| Database connection error | MySQL running | `sudo systemctl start mysql` |
| Frontend blank page | Nginx logs | `tail -f /var/log/nginx/error.log` |
| Out of disk space | `df -h` | Archive old logs, clean up Docker images |
| High memory usage | `top` | Increase JVM heap or scale horizontally |
| Slow queries | MySQL slow log | Add indexes, optimize queries |

For detailed troubleshooting, see the respective deployment guide.

---

## Support and Contact

### Getting Help

1. **Check Logs First**
   - Backend: `/var/log/wms/wms.log`
   - Frontend: `/var/log/nginx/ct-tibet-wms-error.log`
   - System: `journalctl -u wms-backend.service`

2. **Review Documentation**
   - Deployment guide for your method
   - DEPLOYMENT_CHECKLIST.md for verification
   - DEPLOYMENT_CICD.md for pipeline issues

3. **Common Issues**
   - See "Troubleshooting" section in each guide
   - Check "FAQ" section if available

4. **Escalation**
   - Contact your system administrator
   - Contact your database administrator
   - Contact the development team lead

---

## Documentation Maintenance

This documentation is maintained and updated regularly:

- **Version**: 1.0.0
- **Last Updated**: 2025-11-17
- **Next Review**: 2025-12-17
- **Maintained By**: CT Development Team

### Contributing to Documentation

Found an issue or have improvements?
1. Update the relevant document
2. Create a pull request
3. Get review from team lead
4. Merge and update version number

---

## File Structure

```
docs/
├── DEPLOYMENT_README.md          (This file)
├── DEPLOYMENT_DOCKER.md          (Docker deployment guide)
├── DEPLOYMENT_MANUAL.md          (Manual deployment guide)
├── DEPLOYMENT_CICD.md            (CI/CD pipeline guide)
├── DEPLOYMENT_CHECKLIST.md       (Pre-deployment checklist)
├── ...other documentation files

Root Directory:
├── backend/                       (Backend source code)
│   ├── Dockerfile               (Backend container image)
│   ├── pom.xml                 (Maven configuration)
│   ├── src/
│   └── target/                 (Build artifacts)
│
├── frontend-pc/                 (Frontend source code)
│   ├── Dockerfile               (Frontend container image)
│   ├── package.json             (npm configuration)
│   ├── vite.config.js          (Vite build config)
│   ├── src/
│   └── dist/                   (Build artifacts)
│
├── nginx/                       (Nginx configuration)
│   ├── nginx.conf
│   └── nginx-site.conf
│
├── docker-compose.yml           (Multi-container orchestration)
├── .env.production              (Environment variables)
├── .env.staging
├── .env.development
│
└── .github/
    └── workflows/
        └── ci-cd-pipeline.yml   (GitHub Actions workflow)
```

---

## Key Success Factors

1. **Infrastructure First**: Ensure server meets minimum requirements
2. **Security**: Change all default passwords and secrets
3. **Backups**: Configure and test backup procedures before going live
4. **Monitoring**: Set up monitoring and alerting from day one
5. **Documentation**: Keep deployment documentation up to date
6. **Testing**: Always test deployment on staging first
7. **Team Preparation**: Brief team on deployment procedure
8. **Runbook**: Create and maintain operational runbook
9. **On-call**: Designate on-call support for first week
10. **Lessons Learned**: Document issues and improvements after deployment

---

## Next Steps

1. **Choose your deployment method** (Docker recommended for most)
2. **Review the appropriate deployment guide** in detail
3. **Complete the Pre-Deployment Checklist**
4. **Perform deployment on staging environment** first
5. **Get team sign-off** from DEPLOYMENT_CHECKLIST.md
6. **Schedule production deployment** window
7. **Execute deployment** following the chosen guide
8. **Verify using provided checklists**
9. **Monitor closely** for first 24-48 hours
10. **Document lessons learned** for future deployments

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Vue 3 Guide](https://vuejs.org/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/mysql-refe

nce-8.0/en/)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

**Document Version**: 1.0.0
**Last Updated**: 2025-11-17 14:30 UTC
**Status**: Production Ready
**Maintained By**: CT Development Team

---

For questions or updates to this documentation, please contact the Development Team.

**Safe deployment!**
