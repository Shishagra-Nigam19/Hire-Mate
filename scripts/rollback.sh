#!/bin/bash
# ===================================================================
# HireMate Automated Rollback Script
# ===================================================================

set -e

echo "=================================================="
echo " EMERGENCY ROLLBACK INITIATED FOR HIREMATE PLATFORM"
echo "=================================================="

cd "$(dirname "$0")/.."

# Check if previous image tag is saved
if [ -z "$PREVIOUS_DOCKER_TAG" ]; then
    PREVIOUS_DOCKER_TAG="latest"
fi

echo "--> Rolling back to previous stable container tag: $PREVIOUS_DOCKER_TAG..."

# Update container tag to previous image
export DOCKER_IMAGE_TAG=$PREVIOUS_DOCKER_TAG

# Restart container stack
docker-compose -f docker-compose.prod.yml up -d --no-deps backend

echo "--> Verifying health of rolled-back container..."
sleep 10

if [ $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health) -eq 200 ]; then
    echo "SUCCESS: Rollback complete. Application restored to stable state."
else
    echo "CRITICAL WARNING: Rollback health check failed. Human intervention required immediately."
    exit 2
fi
