#!/bin/bash

set -e

# Check dependencies
if ! command -v aws &> /dev/null; then
  echo "ERROR: aws CLI is not installed"
  exit 1
fi
if ! command -v jq &> /dev/null; then
  echo "ERROR: jq is not installed"
  exit 1
fi

echo "🚀 Starting deployment..."

# Load secrets from AWS Secrets Manager
echo "🔐 Loading secrets..."
SECRET_JSON=$(aws secretsmanager get-secret-value \
    --secret-id prod/taskmanager/app-config \
    --query SecretString \
    --output text) || {
  echo "ERROR: Failed to get secrets from AWS"
  exit 1
}

# Export environment variables
export_if_exists() {
  local key="$1"
  local value=$(echo "$SECRET_JSON" | jq -r --arg k "$key" '.[$k] // empty')
  if [ -n "$value" ]; then
    export "$key"="$value"
  fi
}

export_if_exists SPRING_PROFILES_ACTIVE
export_if_exists SPRING_DATASOURCE_URL
export_if_exists SPRING_DATASOURCE_USERNAME
export_if_exists SPRING_DATASOURCE_PASSWORD
export_if_exists SPRING_JPA_HIBERNATE_DDL_AUTO
export_if_exists SPRING_JPA_SHOW_SQL
export_if_exists JWT_SECRET
export_if_exists JWT_EXPIRATION
export_if_exists JWT_REFRESH_EXPIRATION
export_if_exists CORS_ALLOWED_ORIGINS
export_if_exists SERVER_PORT
export_if_exists LOG_LEVEL
export_if_exists HIBERNATE_BATCH_SIZE
export_if_exists MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE
export_if_exists MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS

echo "✅ Secrets loaded"

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down || true

# Pull latest image
echo "📥 Pulling latest image..."
docker-compose -f docker-compose.prod.yml pull

# Start application
echo "🚀 Starting application..."
docker-compose -f docker-compose.prod.yml up -d

# Wait and check health
echo "⏳ Waiting for application..."
sleep 30

if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Deployment successful!"
else
    echo "❌ Health check failed"
    docker-compose -f docker-compose.prod.yml logs app
    exit 1
fi