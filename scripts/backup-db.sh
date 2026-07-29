#!/bin/bash
# ===================================================================
# HireMate Database Automated Backup & AWS S3 Archival Script
# ===================================================================

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/tmp/hiremate-db-backups"
BACKUP_FILE="${BACKUP_DIR}/hiremate_db_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "--> Dumping production PostgreSQL database..."
docker exec hiremate-postgres-prod pg_dump -U ${POSTGRES_USER:-postgres} ${POSTGRES_DB:-hiremate_db} | gzip > "$BACKUP_FILE"

echo "--> Backup file created: $BACKUP_FILE"

# Upload to AWS S3 Backup Bucket if AWS CLI is installed
if command -v aws &> /dev/null; then
    echo "--> Uploading DB backup to AWS S3..."
    aws s3 cp "$BACKUP_FILE" "s3://${AWS_S3_BUCKET:-hiremate-resumes-prod}/db-backups/hiremate_db_${TIMESTAMP}.sql.gz"
    echo "--> S3 upload complete."
fi

# Clean up local temporary file older than 7 days
find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +7 -delete

echo "--> Database backup completed successfully."
