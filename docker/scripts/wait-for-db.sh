#!/bin/bash

# Wait for database readiness script
# This script ensures the database is ready before starting the application

set -e

# Configuration
DB_HOST=${DB_HOST:-postgres}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-eventpulse}
DB_USER=${DB_USER:-eventpulse}
DB_PASSWORD=${DB_PASSWORD:-eventpulse}
MAX_ATTEMPTS=${MAX_ATTEMPTS:-30}
RETRY_DELAY=${RETRY_DELAY:-5}

echo "Waiting for PostgreSQL database to be ready..."
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Max attempts: $MAX_ATTEMPTS"
echo "Retry delay: ${RETRY_DELAY}s"

# Function to check database connection
check_db() {
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" > /dev/null 2>&1
}

# Wait for database to be ready
attempt=1
while ! check_db; do
    if [ $attempt -gt $MAX_ATTEMPTS ]; then
        echo "ERROR: Database not ready after $MAX_ATTEMPTS attempts"
        echo "Please check your database configuration and try again"
        exit 1
    fi
    
    echo "Attempt $attempt/$MAX_ATTEMPTS: Database not ready, waiting ${RETRY_DELAY}s..."
    sleep $RETRY_DELAY
    attempt=$((attempt + 1))
done

echo "✅ Database is ready!"
echo "Starting EventPulse application..."

# Execute the main command
exec "$@"

