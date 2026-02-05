#!/bin/bash
# Deploy script for Azure VM - djaj-bladi-backend
# Usage: ./deploy.sh [--init]
set -e

DEPLOY_DIR="/opt/djaj-bladi"
APP_DIR="$DEPLOY_DIR/app"
DATA_DIR="$DEPLOY_DIR/data"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# Check if .env exists
check_env() {
    if [ ! -f "$APP_DIR/.env" ]; then
        error ".env file not found! Copy .env.example to .env and configure it."
    fi
    
    # Validate required vars
    source "$APP_DIR/.env"
    [ -z "$POSTGRES_PASSWORD" ] && error "POSTGRES_PASSWORD not set in .env"
    [ -z "$JWT_SECRET" ] && error "JWT_SECRET not set in .env"
    [ "$JWT_SECRET" == "CHANGE_ME_GENERATE_SECURE_SECRET_256_BITS" ] && error "JWT_SECRET not changed from default!"
}

# Initial setup (run once)
init() {
    log "🔧 Initial setup for Azure VM..."
    
    # Create directories
    sudo mkdir -p "$DATA_DIR/postgres"
    sudo mkdir -p "$DATA_DIR/redis"
    sudo mkdir -p "$DEPLOY_DIR/backups"
    sudo chown -R $USER:$USER "$DEPLOY_DIR"
    
    log "✅ Directories created at $DEPLOY_DIR"
    log "📝 Next steps:"
    echo "   1. cd $APP_DIR"
    echo "   2. cp .env.example .env"
    echo "   3. Edit .env with your secrets"
    echo "   4. Run: ./deploy.sh"
}

# Main deploy
deploy() {
    log " Deploying djaj-bladi to Azure VM..."
    
    cd "$APP_DIR"
    check_env
    
    # Pull latest code
    log " Pulling latest code..."
    git pull origin main
    
    # Stop existing containers (keeps data)
    log "Stopping existing containers..."
    docker compose -f compose.prod.yaml down || true
    
    # Build and start
    log "Building and starting containers..."
    docker compose -f compose.prod.yaml up -d --build
    
    # Wait for health
    log "Waiting for services to be healthy..."
    sleep 10
    
    # Show status
    log " Container status:"
    docker compose -f compose.prod.yaml ps
    
    # Show logs
    log " Recent logs:"
    docker compose -f compose.prod.yaml logs --tail=20
    
    # Get public IP
    PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || echo "localhost")
    
    log " Deployment complete!"
    echo ""
    echo " API URL: http://$PUBLIC_IP:8080"
    echo " Health: http://$PUBLIC_IP:8080/actuator/health"
    echo ""
    echo " Useful commands:"
    echo "   Logs:    docker compose -f compose.prod.yaml logs -f"
    echo "   Status:  docker compose -f compose.prod.yaml ps"
    echo "   Restart: docker compose -f compose.prod.yaml restart"
    echo "   Stop:    docker compose -f compose.prod.yaml down"
}

# Backup database
backup() {
    log " Creating PostgreSQL backup..."
    BACKUP_FILE="$DEPLOY_DIR/backups/postgres_$(date +%Y%m%d_%H%M%S).sql"
    docker exec djaj-bladi-postgres pg_dump -U postgres djaj_bladi > "$BACKUP_FILE"
    log " Backup saved: $BACKUP_FILE"
}

# Parse arguments
case "${1:-}" in
    --init|-i)
        init
        ;;
    --backup|-b)
        backup
        ;;
    *)
        deploy
        ;;
esac
