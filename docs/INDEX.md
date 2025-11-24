# CT-Tibet-WMS Documentation Index

Complete documentation for the CT-Tibet-WMS (西藏电信仓库管理系统) warehouse management system.

## Deployment Documentation (NEW - PRIORITY)

### Getting Started
- **[DEPLOYMENT_README.md](DEPLOYMENT_README.md)** - Start here! Master guide with decision tree

### Deployment Guides (Choose One)

1. **[DEPLOYMENT_DOCKER.md](DEPLOYMENT_DOCKER.md)** - Docker Compose (15-30 min)
   - For: Cloud deployments, modern infrastructure
   - Includes: Dockerfiles, docker-compose.yml, Nginx config

2. **[DEPLOYMENT_MANUAL.md](DEPLOYMENT_MANUAL.md)** - Traditional deployment (45-90 min)
   - For: On-premises servers, legacy infrastructure
   - Includes: Installation guides, systemd, performance tuning

3. **[DEPLOYMENT_CICD.md](DEPLOYMENT_CICD.md)** - GitHub Actions CI/CD (5-15 min deploy)
   - For: Continuous deployment, automated testing
   - Includes: Complete workflow, security scanning, rollback

### Pre-Deployment Verification
- **[DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)** - Complete verification (200+ items)
  - Infrastructure checklist
  - Security hardening
  - Team sign-off forms

### Reference
- **[DEPLOYMENT_FILES_MANIFEST.md](DEPLOYMENT_FILES_MANIFEST.md)** - Documentation overview
- **[../DEPLOYMENT_SUMMARY.txt](../DEPLOYMENT_SUMMARY.txt)** - High-level summary

---

## Project Documentation (Existing)

### Requirements & Planning
- **[PRD-产品需求文档.md](PRD-产品需求文档.md)** - Product requirements
- **[需求分析.md](需求分析.md)** - Requirements analysis

### Design & Architecture
- **[项目架构文档.md](项目架构文档.md)** - System architecture
- **[数据库设计文档.md](数据库设计文档.md)** - Database schema
- **[原型设计文档.md](原型设计文档.md)** - UI/UX prototypes

### Development
- **[开发实施指导手册.md](开发实施指导手册.md)** - Development guide
- **[API接口文档.md](API接口文档.md)** - API documentation

### API Reference
- **[API_REFERENCE.md](API_REFERENCE.md)** - Complete API reference
- **[API_QUICK_START.md](API_QUICK_START.md)** - API quick start
- **[API_TEST_GUIDE.md](API_TEST_GUIDE.md)** - API testing

---

## Quick Navigation

| Need | Read |
|------|------|
| Deploy the application | [DEPLOYMENT_README.md](DEPLOYMENT_README.md) |
| Use Docker | [DEPLOYMENT_DOCKER.md](DEPLOYMENT_DOCKER.md) |
| Manual deployment | [DEPLOYMENT_MANUAL.md](DEPLOYMENT_MANUAL.md) |
| Set up CI/CD | [DEPLOYMENT_CICD.md](DEPLOYMENT_CICD.md) |
| Verify deployment | [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) |
| API documentation | [API_REFERENCE.md](API_REFERENCE.md) |
| Architecture | [项目架构文档.md](项目架构文档.md) |
| Database schema | [数据库设计文档.md](数据库设计文档.md) |

---

## Documentation Statistics

**Deployment Guides**: 5,779+ lines
**Deployment Files**: 6 comprehensive guides
**Total Documentation**: 150+ pages
**Coverage**: 3 deployment methods, 3 environments, 7+ components

---

## Getting Help

1. For deployment: Start with [DEPLOYMENT_README.md](DEPLOYMENT_README.md)
2. For APIs: Start with [API_QUICK_START.md](API_QUICK_START.md)
3. For architecture: See [项目架构文档.md](项目架构文档.md)
4. For troubleshooting: Check deployment guide's troubleshooting section

---

**Status**: Production Ready
**Last Updated**: 2025-11-17
**Version**: 1.0.0
