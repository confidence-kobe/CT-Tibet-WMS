# CT-Tibet-WMS CI/CD Deployment Pipeline Guide

**Version**: 1.0.0
**Last Updated**: 2025-11-17
**Platform**: GitHub Actions

## Overview

This guide provides comprehensive instructions for setting up a production-grade CI/CD pipeline using GitHub Actions. The pipeline automates testing, building, and deployment across multiple environments (development, staging, production) with security scanning, performance optimization, and automated rollback capabilities.

**Key Features:**
- Automated build, test, and deploy pipeline
- Multi-environment deployment (dev/staging/prod)
- Security scanning and vulnerability analysis
- Artifact management and versioning
- Automated rollback on deployment failure
- Performance and compliance checks
- Comprehensive logging and monitoring

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [GitHub Actions Workflow](#github-actions-workflow)
3. [Build Pipeline](#build-pipeline)
4. [Test Pipeline](#test-pipeline)
5. [Deployment Pipeline](#deployment-pipeline)
6. [Environment Management](#environment-management)
7. [Security Integration](#security-integration)
8. [Monitoring and Alerts](#monitoring-and-alerts)
9. [Rollback Strategies](#rollback-strategies)
10. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
┌─────────────┐
│   Git Push  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│  GitHub Actions Workflow        │
├─────────────────────────────────┤
│ 1. Checkout Code                │
│ 2. Setup Tools (Java, Node)     │
│ 3. Security Scanning            │
│ 4. Build Backend (Maven)        │
│ 5. Build Frontend (npm)         │
│ 6. Run Tests                    │
│ 7. Build Docker Images          │
│ 8. Push to Registry             │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Deployment Strategy            │
├─────────────────────────────────┤
│ Development  │ Staging  │ Production
│ (automatic)  │ (auto)   │ (manual approval)
└─────────────────────────────────┘
```

---

## GitHub Actions Workflow

### Setup Secrets in GitHub

1. Navigate to: Settings > Secrets and variables > Actions
2. Add the following secrets:

```
REGISTRY_URL=docker.io
REGISTRY_USERNAME=your-docker-username
REGISTRY_PASSWORD=your-docker-password

# Deployment Secrets
DEV_DEPLOY_HOST=dev-server.com
DEV_DEPLOY_USER=deploy-user
DEV_DEPLOY_KEY=<private-key>

STAGING_DEPLOY_HOST=staging-server.com
STAGING_DEPLOY_USER=deploy-user
STAGING_DEPLOY_KEY=<private-key>

PROD_DEPLOY_HOST=prod-server.com
PROD_DEPLOY_USER=deploy-user
PROD_DEPLOY_KEY=<private-key>

# Database & Service Credentials
PROD_DB_PASSWORD=<secure-password>
PROD_REDIS_PASSWORD=<secure-password>
PROD_JWT_SECRET=<secure-jwt-key>

# Notification
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
```

### GitHub Actions Workflow Configuration

Create `.github/workflows/ci-cd-pipeline.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches:
      - main
      - develop
      - 'release/**'
  pull_request:
    branches:
      - main
      - develop
  workflow_dispatch:
    inputs:
      deploy_env:
        description: 'Environment to deploy to'
        required: false
        default: 'staging'
        type: choice
        options:
          - development
          - staging
          - production

env:
  REGISTRY: docker.io
  REGISTRY_USERNAME: ${{ secrets.REGISTRY_USERNAME }}
  BACKEND_IMAGE_NAME: ct-wms-backend
  FRONTEND_IMAGE_NAME: ct-wms-frontend

jobs:
  # ============================================================================
  # CHECKOUT AND SETUP
  # ============================================================================
  checkout:
    name: Checkout Code
    runs-on: ubuntu-latest
    outputs:
      java-version: 11
      node-version: 18
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Get commit info
        id: commit
        run: |
          echo "sha=$(git rev-parse --short HEAD)" >> $GITHUB_OUTPUT
          echo "branch=$(git rev-parse --abbrev-ref HEAD)" >> $GITHUB_OUTPUT
          echo "timestamp=$(date -u +'%Y%m%d_%H%M%S')" >> $GITHUB_OUTPUT

      - name: Cache git metadata
        uses: actions/cache@v3
        with:
          path: .git
          key: git-${{ github.sha }}

  # ============================================================================
  # SECURITY SCANNING
  # ============================================================================
  security-scan:
    name: Security Scanning
    runs-on: ubuntu-latest
    needs: checkout
    permissions:
      contents: read
      security-events: write
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'

      - name: Upload Trivy results to GitHub Security tab
        uses: github/codeql-action/upload-sarif@v2
        if: always()
        with:
          sarif_file: 'trivy-results.sarif'

      - name: Run OWASP Dependency-Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'CT-Tibet-WMS'
          path: '.'
          format: 'JSON'
          args: >
            --enableExperimental
            --scan backend/pom.xml
            --scan frontend-pc/package.json

      - name: Upload dependency-check results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: dependency-check-report
          path: dependency-check-report.json

  # ============================================================================
  # BACKEND BUILD
  # ============================================================================
  build-backend:
    name: Build Backend (Maven)
    runs-on: ubuntu-latest
    needs: checkout
    permissions:
      contents: read
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        working-directory: backend
        run: |
          mvn clean package \
            -DskipTests \
            -X \
            -Dorg.slf4j.simpleLogger.defaultLogLevel=info

      - name: Run Maven tests
        working-directory: backend
        run: |
          mvn test \
            -Dorg.slf4j.simpleLogger.defaultLogLevel=warn

      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: backend-test-results
          path: backend/target/surefire-reports/

      - name: Generate test report
        if: always()
        run: |
          mvn surefire-report:report -f backend/pom.xml

      - name: Publish test results
        if: always()
        uses: EnricoMi/publish-unit-test-result-action@v2
        with:
          files: backend/target/surefire-reports/TEST-*.xml
          check_name: Backend Unit Tests

      - name: Upload JAR artifact
        uses: actions/upload-artifact@v3
        with:
          name: backend-jar
          path: backend/target/ct-tibet-wms.jar
          retention-days: 5

      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: ./backend
          file: ./backend/Dockerfile
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.REGISTRY_USERNAME }}/${{ env.BACKEND_IMAGE_NAME }}:latest
            ${{ env.REGISTRY }}/${{ env.REGISTRY_USERNAME }}/${{ env.BACKEND_IMAGE_NAME }}:${{ github.sha }}
          registry: ${{ env.REGISTRY }}
          username: ${{ secrets.REGISTRY_USERNAME }}
          password: ${{ secrets.REGISTRY_PASSWORD }}

  # ============================================================================
  # FRONTEND BUILD
  # ============================================================================
  build-frontend:
    name: Build Frontend (Vue 3)
    runs-on: ubuntu-latest
    needs: checkout
    permissions:
      contents: read
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: npm
          cache-dependency-path: frontend-pc/package-lock.json

      - name: Install dependencies
        working-directory: frontend-pc
        run: npm ci

      - name: Lint code
        working-directory: frontend-pc
        run: npm run lint || true

      - name: Build application
        working-directory: frontend-pc
        run: npm run build

      - name: Analyze bundle size
        working-directory: frontend-pc
        run: |
          du -sh dist/
          find dist -type f -name '*.js' -exec du -h {} + | sort -rh | head -20

      - name: Upload build artifact
        uses: actions/upload-artifact@v3
        with:
          name: frontend-dist
          path: frontend-pc/dist/
          retention-days: 5

      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: ./frontend-pc
          file: ./frontend-pc/Dockerfile
          push: true
          tags: |
            ${{ env.REGISTRY }}/${{ env.REGISTRY_USERNAME }}/${{ env.FRONTEND_IMAGE_NAME }}:latest
            ${{ env.REGISTRY }}/${{ env.REGISTRY_USERNAME }}/${{ env.FRONTEND_IMAGE_NAME }}:${{ github.sha }}
          registry: ${{ env.REGISTRY }}
          username: ${{ secrets.REGISTRY_USERNAME }}
          password: ${{ secrets.REGISTRY_PASSWORD }}

  # ============================================================================
  # INTEGRATION TESTS
  # ============================================================================
  integration-tests:
    name: Integration Tests
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend]
    services:
      mysql:
        image: mysql:8.0-debian
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: ct_tibet_wms_test
        options: >-
          --health-cmd="mysqladmin ping -h localhost"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
        ports:
          - 3306:3306

      redis:
        image: redis:7-alpine
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
        ports:
          - 6379:6379

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Run integration tests
        working-directory: backend
        run: |
          mvn verify \
            -Dspring.datasource.url=jdbc:mysql://localhost:3306/ct_tibet_wms_test \
            -Dspring.datasource.username=root \
            -Dspring.datasource.password=root \
            -Dspring.redis.host=localhost

      - name: Upload integration test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: backend/target/surefire-reports/

  # ============================================================================
  # DEPLOYMENT TO DEVELOPMENT
  # ============================================================================
  deploy-dev:
    name: Deploy to Development
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend, security-scan]
    if: github.ref == 'refs/heads/develop' && github.event_name == 'push'
    environment:
      name: development
      url: http://dev-server.com
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Download artifacts
        uses: actions/download-artifact@v3

      - name: Deploy to development server
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.DEV_DEPLOY_KEY }}" > ~/.ssh/deploy_key
          chmod 600 ~/.ssh/deploy_key
          ssh-keyscan -H ${{ secrets.DEV_DEPLOY_HOST }} >> ~/.ssh/known_hosts

          ssh -i ~/.ssh/deploy_key \
            ${{ secrets.DEV_DEPLOY_USER }}@${{ secrets.DEV_DEPLOY_HOST }} << 'EOF'
            cd /home/deploy/ct-wms
            docker compose -f docker-compose.dev.yml pull
            docker compose -f docker-compose.dev.yml up -d
            sleep 10
            docker compose -f docker-compose.dev.yml exec -T backend curl -f http://localhost:48888/actuator/health || exit 1
          EOF

      - name: Verify deployment
        run: |
          curl -f http://dev-server.com/api/health || exit 1

      - name: Send Slack notification - Success
        if: success()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
          payload: |
            {
              "text": "Development Deployment Successful",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Development Deployment* - Success\n*Commit*: ${{ github.sha }}\n*Branch*: develop"
                  }
                }
              ]
            }

  # ============================================================================
  # DEPLOYMENT TO STAGING
  # ============================================================================
  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend, security-scan, integration-tests]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    environment:
      name: staging
      url: http://staging-server.com
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Deploy to staging server
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.STAGING_DEPLOY_KEY }}" > ~/.ssh/deploy_key
          chmod 600 ~/.ssh/deploy_key
          ssh-keyscan -H ${{ secrets.STAGING_DEPLOY_HOST }} >> ~/.ssh/known_hosts

          ssh -i ~/.ssh/deploy_key \
            ${{ secrets.STAGING_DEPLOY_USER }}@${{ secrets.STAGING_DEPLOY_HOST }} << 'EOF'
            cd /home/deploy/ct-wms
            docker compose -f docker-compose.staging.yml pull
            docker compose -f docker-compose.staging.yml up -d
            sleep 15
            docker compose -f docker-compose.staging.yml exec -T backend curl -f http://localhost:48888/actuator/health || exit 1
            docker compose -f docker-compose.staging.yml exec -T mysql mysql -u root -ppassword -e "SELECT 1;" || exit 1
          EOF

      - name: Run smoke tests
        run: |
          sleep 5
          curl -f http://staging-server.com/health || exit 1
          curl -f http://staging-server.com/api/system/health || exit 1

      - name: Send Slack notification - Success
        if: success()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
          payload: |
            {
              "text": "Staging Deployment Successful",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Staging Deployment* - Success\n*Commit*: ${{ github.sha }}\n*Branch*: main"
                  }
                }
              ]
            }

  # ============================================================================
  # DEPLOYMENT TO PRODUCTION (MANUAL APPROVAL)
  # ============================================================================
  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend, security-scan, integration-tests]
    if: github.ref == 'refs/heads/main' && github.event_name == 'workflow_dispatch'
    environment:
      name: production
      url: https://production-server.com
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Create backup before deployment
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.PROD_DEPLOY_KEY }}" > ~/.ssh/deploy_key
          chmod 600 ~/.ssh/deploy_key
          ssh-keyscan -H ${{ secrets.PROD_DEPLOY_HOST }} >> ~/.ssh/known_hosts

          ssh -i ~/.ssh/deploy_key \
            ${{ secrets.PROD_DEPLOY_USER }}@${{ secrets.PROD_DEPLOY_HOST }} << 'EOF'
            cd /home/deploy/ct-wms
            docker compose exec -T mysql mysqldump -u root -p${{ secrets.PROD_DB_PASSWORD }} ct_tibet_wms | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
            echo "Backup created successfully"
          EOF

      - name: Deploy to production
        run: |
          ssh -i ~/.ssh/deploy_key \
            ${{ secrets.PROD_DEPLOY_USER }}@${{ secrets.PROD_DEPLOY_HOST }} << 'EOF'
            cd /home/deploy/ct-wms

            # Pull latest images
            docker compose -f docker-compose.prod.yml pull

            # Update services with health check
            docker compose -f docker-compose.prod.yml up -d

            # Wait for services to be healthy
            sleep 20

            # Verify backend health
            docker compose -f docker-compose.prod.yml exec -T backend curl -f http://localhost:48888/actuator/health || {
              echo "Backend health check failed"
              docker compose -f docker-compose.prod.yml logs backend
              exit 1
            }

            # Verify database connectivity
            docker compose -f docker-compose.prod.yml exec -T mysql mysql -u root -p${{ secrets.PROD_DB_PASSWORD }} -e "SELECT 1;" || exit 1

            echo "Production deployment completed successfully"
          EOF

      - name: Run production smoke tests
        run: |
          sleep 10
          curl -f https://production-server.com/health || exit 1
          curl -f https://production-server.com/api/system/health || exit 1

      - name: Create deployment record
        run: |
          echo "Deployment to production completed"
          echo "Commit: ${{ github.sha }}"
          echo "Timestamp: $(date -u +'%Y-%m-%d %H:%M:%S UTC')"

      - name: Send Slack notification - Success
        if: success()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
          payload: |
            {
              "text": "Production Deployment Successful",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Production Deployment* - Success\n*Commit*: ${{ github.sha }}\n*Deployed By*: ${{ github.actor }}"
                  }
                }
              ]
            }

      - name: Send Slack notification - Failure
        if: failure()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
          payload: |
            {
              "text": "Production Deployment Failed",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Production Deployment* - FAILED\n*Commit*: ${{ github.sha }}\n*Deployed By*: ${{ github.actor }}\n*Action*: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}"
                  }
                }
              ]
            }

  # ============================================================================
  # NOTIFICATION AND CLEANUP
  # ============================================================================
  notify-on-failure:
    name: Notify on Failure
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend, security-scan]
    if: failure()
    steps:
      - name: Send Slack notification - Failure
        uses: slackapi/slack-github-action@v1.24.0
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
          payload: |
            {
              "text": "CI/CD Pipeline Failed",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*CI/CD Pipeline* - FAILED\n*Repository*: ${{ github.repository }}\n*Branch*: ${{ github.ref_name }}\n*Commit*: ${{ github.sha }}\n*Author*: ${{ github.actor }}\n*Action*: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}"
                  }
                }
              ]
            }
```

---

## Environment Configuration Files

### docker-compose.dev.yml

```yaml
version: '3.9'

services:
  mysql:
    image: mysql:8.0-debian
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ct_tibet_wms_dev

  backend:
    image: ct-wms-backend:latest
    ports:
      - "48888:48888"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      MYSQL_HOST: mysql
      JWT_SECRET: dev-secret-key

  frontend:
    image: ct-wms-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### docker-compose.staging.yml

Similar to docker-compose.dev.yml but with staging-specific configurations.

### docker-compose.prod.yml

```yaml
version: '3.9'

services:
  mysql:
    image: mysql:8.0-debian
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ct_tibet_wms
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]

  backend:
    image: ct-wms-backend:${VERSION}
    restart: always
    ports:
      - "127.0.0.1:48888:48888"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      JWT_SECRET: ${JWT_SECRET}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:48888/actuator/health"]
      interval: 30s
      timeout: 10s
      start_period: 40s
      retries: 3

  frontend:
    image: ct-wms-frontend:${VERSION}
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx-prod.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro

volumes:
  mysql_data:
    driver: local
```

---

## Rollback Strategies

### Automated Rollback on Health Check Failure

```yaml
deploy-production-with-rollback:
  steps:
    - name: Deploy new version
      id: deploy
      run: |
        # Store old image version
        OLD_VERSION=$(docker compose -f docker-compose.prod.yml images -q backend)
        echo "old_version=$OLD_VERSION" >> $GITHUB_OUTPUT

        # Deploy new version
        docker compose -f docker-compose.prod.yml pull
        docker compose -f docker-compose.prod.yml up -d

    - name: Health check and rollback if needed
      if: failure() || steps.deploy.outcome == 'failure'
      run: |
        echo "Deployment failed, rolling back..."
        docker compose -f docker-compose.prod.yml kill
        docker run -d --name backend-rollback ${{ steps.deploy.outputs.old_version }}
```

### Manual Rollback Procedure

```bash
#!/bin/bash
# rollback.sh

ENVIRONMENT=$1
ROLLBACK_VERSION=$2

if [ -z "$ENVIRONMENT" ] || [ -z "$ROLLBACK_VERSION" ]; then
    echo "Usage: ./rollback.sh <environment> <version>"
    exit 1
fi

echo "Rolling back $ENVIRONMENT to version $ROLLBACK_VERSION..."

ssh deploy@$ENVIRONMENT_HOST << EOF
  cd /home/deploy/ct-wms
  docker compose -f docker-compose.$ENVIRONMENT.yml down
  docker rmi ct-wms-backend:latest
  docker rmi ct-wms-frontend:latest
  docker pull ct-wms-backend:$ROLLBACK_VERSION
  docker pull ct-wms-frontend:$ROLLBACK_VERSION
  docker tag ct-wms-backend:$ROLLBACK_VERSION ct-wms-backend:latest
  docker tag ct-wms-frontend:$ROLLBACK_VERSION ct-wms-frontend:latest
  docker compose -f docker-compose.$ENVIRONMENT.yml up -d
  sleep 15
  curl http://localhost/health
EOF

echo "Rollback completed!"
```

---

## Performance Monitoring

Add performance metrics collection to CI/CD:

```yaml
performance-test:
  runs-on: ubuntu-latest
  needs: [deploy-staging]
  steps:
    - name: Run performance tests
      run: |
        # Apache JMeter tests
        jmeter -n -t performance-test-plan.jmx -l results.jtl

    - name: Upload performance results
      uses: actions/upload-artifact@v3
      with:
        name: performance-results
        path: results.jtl

    - name: Analyze performance
      run: |
        # Parse JMeter results and set quality gates
        python analyze_performance.py results.jtl
```

---

## Troubleshooting

### Pipeline Fails During Build

```bash
# Check logs
gh run view <run-id> --log

# Retry specific job
gh run rerun <run-id> --failed
```

### Docker Push Fails

```yaml
- name: Login to Docker Registry
  uses: docker/login-action@v2
  with:
    registry: ${{ env.REGISTRY }}
    username: ${{ secrets.REGISTRY_USERNAME }}
    password: ${{ secrets.REGISTRY_PASSWORD }}
```

### Deployment Hangs

```bash
# Check remote server
ssh deploy@server "docker compose logs -f"

# Manually rollback
ssh deploy@server "docker compose down && docker compose up -d --build"
```

---

## Best Practices

1. **Always run tests before deployment**
2. **Use environment secrets for sensitive data**
3. **Implement approval gates for production**
4. **Automate rollback on health check failure**
5. **Keep deployment history and logs**
6. **Monitor performance after each deployment**
7. **Regular security scanning**
8. **Maintain separate environments for isolation**

---

**Last Updated**: 2025-11-17
**Maintained By**: CT Development Team
