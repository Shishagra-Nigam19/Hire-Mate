#!/bin/bash
# ===================================================================
# HireMate Production Zero-Downtime Automated Deployment Script
# ===================================================================

set -e

echo "=================================================="
echo " Starting Automated Deployment for HireMate Platform"
echo "=================================================="

# Ensure script is run from project root
cd "$(dirname "$0")/.."

# Check if production env file exists
if [ ! -f .env ]; then
    echo "ERROR: Production .env file not found! Copy .env.example to .env and configure secrets."
    exit 1
fi

# Export environment variables
set -a
source .env
set +a

# Pull latest container images from registry
echo "--> Pulling latest container images..."
docker-compose -f docker-compose.prod.yml pull

# Re-create database & backend containers with minimal delay
echo "--> Deploying updated container stack..."
docker-compose -f docker-compose.prod.yml up -d --remove-orphans

# Health Check Verification
echo "--> Waiting for application health check..."
MAX_ATTEMPTS=20
ATTEMPT=1

until [ $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health) -eq 200 ] || [ $ATTEMPT -eq $MAX_ATTEMPTS ]; do
    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Waiting for backend service to become healthy..."
    sleep 3
    ATTEMPT=$((ATTEMPT + 1))
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "ERROR: Health check failed after 60 seconds!"
    echo "Triggering automated rollback procedure..."
    ./scripts/rollback.sh
    exit 1
fi

echo "=================================================="
echo " DEPLOYMENT SUCCESSFUL! HireMate is live & healthy."
echo "=================================================="
