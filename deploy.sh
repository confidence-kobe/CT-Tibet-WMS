#!/bin/bash

# CT-Tibet-WMS Production Deployment Script
# Description: Complete one-click deployment with backup and rollback support
# Usage: ./deploy.sh [start|stop|restart|upgrade|rollback|backup|status|logs]

set -e

# ============================================================================
# CONFIGURATION
# ============================================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="ct-wms-prod"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.production"
BACKUP_DIR="${SCRIPT_DIR}/backups"
BACKUP_RETENTION_DAYS=30
LOG_FILE="${SCRIPT_DIR}/deploy.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================================
# LOGGING FUNCTIONS
# ============================================================================
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "${LOG_FILE}"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "${LOG_FILE}"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "${LOG_FILE}"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "${LOG_FILE}"
    exit 1
}

# ============================================================================
# PREREQUISITE CHECKS
# ============================================================================
check_prerequisites() {
    log "Checking prerequisites..."

    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
    fi

    # Check if Docker Compose is installed
    if ! command -v docker compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
    fi

    # Check if .env.production exists
    if [ ! -f "${SCRIPT_DIR}/${ENV_FILE}" ]; then
        error "Environment file ${ENV_FILE} not found. Please create it first."
    fi

    # Check if docker-compose.prod.yml exists
    if [ ! -f "${SCRIPT_DIR}/${COMPOSE_FILE}" ]; then
        error "Docker Compose file ${COMPOSE_FILE} not found."
    fi

    success "All prerequisites met."
}

# ============================================================================
# ENVIRONMENT VALIDATION
# ============================================================================
validate_environment() {
    log "Validating environment variables..."

    # Load environment variables
    set -a
    source "${SCRIPT_DIR}/${ENV_FILE}"
    set +a

    # Check critical variables
    if [ -z "${MYSQL_ROOT_PASSWORD}" ] || [ "${MYSQL_ROOT_PASSWORD}" = "change-me-to-secure-password-32-chars-minimum!" ]; then
        error "MYSQL_ROOT_PASSWORD must be changed in ${ENV_FILE}"
    fi

    if [ -z "${MYSQL_PASSWORD}" ] || [ "${MYSQL_PASSWORD}" = "change-me-to-secure-mysql-password!" ]; then
        error "MYSQL_PASSWORD must be changed in ${ENV_FILE}"
    fi

    if [ -z "${JWT_SECRET}" ] || [ "${JWT_SECRET}" = "change-me-to-a-strong-jwt-secret-key-minimum-32-chars-for-HS256!" ]; then
        error "JWT_SECRET must be changed in ${ENV_FILE}"
    fi

    success "Environment variables validated."
}

# ============================================================================
# BACKUP FUNCTIONS
# ============================================================================
create_backup() {
    log "Creating backup..."

    # Create backup directory if it doesn't exist
    mkdir -p "${BACKUP_DIR}"

    local BACKUP_NAME="backup-$(date +'%Y%m%d-%H%M%S')"
    local BACKUP_PATH="${BACKUP_DIR}/${BACKUP_NAME}"
    mkdir -p "${BACKUP_PATH}"

    # Backup MySQL database
    log "Backing up MySQL database..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T mysql mysqldump \
        -u root -p"${MYSQL_ROOT_PASSWORD}" \
        "${MYSQL_DATABASE}" > "${BACKUP_PATH}/database.sql" 2>/dev/null || warning "MySQL backup failed - container may not be running"

    # Backup Redis data
    log "Backing up Redis data..."
    if docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" ps redis | grep -q running; then
        docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T redis redis-cli -a "${REDIS_PASSWORD}" BGSAVE 2>/dev/null || true
        sleep 2
        docker cp ct-wms-redis-prod:/data/dump.rdb "${BACKUP_PATH}/redis-dump.rdb" 2>/dev/null || warning "Redis backup failed"
    fi

    # Backup uploaded files
    log "Backing up file uploads..."
    if [ -d "${SCRIPT_DIR}/uploads" ]; then
        tar -czf "${BACKUP_PATH}/uploads.tar.gz" -C "${SCRIPT_DIR}" uploads 2>/dev/null || warning "File upload backup failed"
    fi

    # Backup configuration files
    log "Backing up configuration files..."
    cp "${SCRIPT_DIR}/${ENV_FILE}" "${BACKUP_PATH}/.env.production.bak"
    cp "${SCRIPT_DIR}/docker-compose.prod.yml" "${BACKUP_PATH}/docker-compose.prod.yml.bak"

    success "Backup completed: ${BACKUP_PATH}"

    # Clean old backups
    cleanup_old_backups

    echo "${BACKUP_PATH}"
}

cleanup_old_backups() {
    log "Cleaning up old backups (older than ${BACKUP_RETENTION_DAYS} days)..."
    find "${BACKUP_DIR}" -maxdepth 1 -type d -name "backup-*" -mtime +${BACKUP_RETENTION_DAYS} -exec rm -rf {} \; || true
}

restore_backup() {
    local BACKUP_PATH="$1"

    if [ ! -d "${BACKUP_PATH}" ]; then
        error "Backup directory not found: ${BACKUP_PATH}"
    fi

    log "Restoring from backup: ${BACKUP_PATH}"

    # Restore MySQL database
    if [ -f "${BACKUP_PATH}/database.sql" ]; then
        log "Restoring MySQL database..."
        docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T mysql mysql \
            -u root -p"${MYSQL_ROOT_PASSWORD}" \
            "${MYSQL_DATABASE}" < "${BACKUP_PATH}/database.sql" 2>/dev/null || warning "MySQL restore failed"
    fi

    # Restore Redis data
    if [ -f "${BACKUP_PATH}/redis-dump.rdb" ]; then
        log "Restoring Redis data..."
        docker cp "${BACKUP_PATH}/redis-dump.rdb" ct-wms-redis-prod:/data/dump.rdb 2>/dev/null || warning "Redis restore failed"
    fi

    # Restore uploaded files
    if [ -f "${BACKUP_PATH}/uploads.tar.gz" ]; then
        log "Restoring file uploads..."
        tar -xzf "${BACKUP_PATH}/uploads.tar.gz" -C "${SCRIPT_DIR}" 2>/dev/null || warning "File upload restore failed"
    fi

    success "Backup restoration completed."
}

# ============================================================================
# DEPLOYMENT FUNCTIONS
# ============================================================================
start_services() {
    log "Starting CT-Tibet-WMS services..."

    # Create necessary directories
    mkdir -p "${SCRIPT_DIR}/data/"{mysql,redis,rabbitmq,nginx/cache}
    mkdir -p "${SCRIPT_DIR}/logs/"{backend,mysql,nginx,rabbitmq,redis}
    mkdir -p "${SCRIPT_DIR}/uploads"

    # Set proper permissions
    chmod 755 "${SCRIPT_DIR}/data" "${SCRIPT_DIR}/logs" "${SCRIPT_DIR}/uploads"

    # Start services
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" up -d

    log "Waiting for services to be healthy..."
    local MAX_ATTEMPTS=30
    local ATTEMPT=0

    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        # Check if all services are healthy
        local UNHEALTHY=$(docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" ps | grep -c "unhealthy" || true)

        if [ "$UNHEALTHY" -eq 0 ]; then
            success "All services started and healthy."
            return 0
        fi

        ATTEMPT=$((ATTEMPT + 1))
        log "Waiting for services to be healthy... (${ATTEMPT}/${MAX_ATTEMPTS})"
        sleep 2
    done

    error "Services failed to start within timeout period."
}

stop_services() {
    log "Stopping CT-Tibet-WMS services..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" down

    success "Services stopped."
}

restart_services() {
    log "Restarting CT-Tibet-WMS services..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" restart

    success "Services restarted."
}

upgrade_deployment() {
    log "Upgrading deployment..."

    # Create backup before upgrade
    local BACKUP_PATH=$(create_backup)

    log "Pulling latest images..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" pull

    log "Rebuilding services..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" build

    log "Starting upgraded services..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" up -d

    success "Upgrade completed. Backup saved at: ${BACKUP_PATH}"
}

rollback_deployment() {
    # List available backups
    log "Available backups:"
    ls -1d "${BACKUP_DIR}"/backup-* 2>/dev/null | tail -5 | nl

    read -p "Enter backup number to restore: " BACKUP_NUM
    local BACKUP_PATH=$(ls -1d "${BACKUP_DIR}"/backup-* 2>/dev/null | tail -5 | sed -n "${BACKUP_NUM}p")

    if [ -z "${BACKUP_PATH}" ]; then
        error "Invalid backup selection."
    fi

    log "Stopping services for rollback..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" down

    restore_backup "${BACKUP_PATH}"

    log "Restarting services..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" up -d

    success "Rollback completed from: ${BACKUP_PATH}"
}

# ============================================================================
# MONITORING FUNCTIONS
# ============================================================================
check_status() {
    log "Checking service status..."
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" ps
}

view_logs() {
    local SERVICE="$1"
    local LINES="${2:-100}"

    if [ -z "${SERVICE}" ]; then
        docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" logs -f --tail="${LINES}"
    else
        docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" --project-name "${PROJECT_NAME}" logs -f --tail="${LINES}" "${SERVICE}"
    fi
}

# ============================================================================
# HEALTH CHECK FUNCTION
# ============================================================================
health_check() {
    log "Running health checks..."

    # Check backend
    if curl -f http://localhost:48888/actuator/health &> /dev/null; then
        success "Backend service: HEALTHY"
    else
        error "Backend service: UNHEALTHY"
    fi

    # Check frontend
    if curl -f http://localhost/health &> /dev/null; then
        success "Frontend service: HEALTHY"
    else
        error "Frontend service: UNHEALTHY"
    fi

    # Check database
    if docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T mysql mysqladmin -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" ping &> /dev/null; then
        success "MySQL database: HEALTHY"
    else
        error "MySQL database: UNHEALTHY"
    fi

    # Check Redis
    if docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T redis redis-cli -a "${REDIS_PASSWORD}" ping &> /dev/null; then
        success "Redis cache: HEALTHY"
    else
        error "Redis cache: UNHEALTHY"
    fi

    # Check RabbitMQ
    if docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" exec -T rabbitmq rabbitmq-diagnostics ping &> /dev/null; then
        success "RabbitMQ queue: HEALTHY"
    else
        error "RabbitMQ queue: UNHEALTHY"
    fi
}

# ============================================================================
# MAIN COMMAND HANDLER
# ============================================================================
main() {
    local COMMAND="${1:-status}"

    # Ensure log directory exists
    mkdir -p "$(dirname "${LOG_FILE}")"

    case "${COMMAND}" in
        start)
            check_prerequisites
            validate_environment
            start_services
            health_check
            ;;
        stop)
            stop_services
            ;;
        restart)
            check_prerequisites
            validate_environment
            restart_services
            health_check
            ;;
        upgrade)
            check_prerequisites
            validate_environment
            upgrade_deployment
            health_check
            ;;
        rollback)
            check_prerequisites
            validate_environment
            rollback_deployment
            health_check
            ;;
        backup)
            set -a
            source "${SCRIPT_DIR}/${ENV_FILE}"
            set +a
            create_backup
            ;;
        status)
            check_prerequisites
            check_status
            ;;
        logs)
            check_prerequisites
            view_logs "$2" "$3"
            ;;
        health)
            check_prerequisites
            health_check
            ;;
        *)
            echo "Usage: $0 {start|stop|restart|upgrade|rollback|backup|status|logs|health}"
            echo ""
            echo "Commands:"
            echo "  start      - Start all services"
            echo "  stop       - Stop all services"
            echo "  restart    - Restart all services"
            echo "  upgrade    - Upgrade deployment with latest images"
            echo "  rollback   - Rollback to previous backup"
            echo "  backup     - Create manual backup"
            echo "  status     - Check service status"
            echo "  logs       - View service logs (logs [service] [lines])"
            echo "  health     - Run health checks"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
