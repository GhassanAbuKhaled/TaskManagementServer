#!/bin/bash

set -euo pipefail  # Stricter error handling

# Validate environment
if [[ "${AWS_REGION:-}" == "" ]]; then
  echo "ERROR: AWS_REGION not set"
  exit 1
fi

# Check dependencies
for cmd in aws jq docker docker-compose curl; do
  if ! command -v "$cmd" &> /dev/null; then
    echo "ERROR: $cmd is not installed"
    exit 1
  fi
done

echo "🚀 Starting deployment..."

# Load secrets from AWS Secrets Manager
echo "🔐 Loading secrets..."
SECRET_JSON=$(aws secretsmanager get-secret-value \
    --secret-id prod/taskmanager/app-config \
    --query SecretString \
    --output text 2>/dev/null) || {
  echo "ERROR: Failed to get secrets from AWS Secrets Manager"
  echo "Check AWS credentials and secret existence"
  exit 1
}

# Validate JSON
if ! echo "$SECRET_JSON" | jq empty 2>/dev/null; then
  echo "ERROR: Invalid JSON in secret"
  exit 1
fi

# Export environment variables with validation
export_if_exists() {
  local key="$1"
  local value=$(echo "$SECRET_JSON" | jq -r --arg k "$key" '.[$k] // empty')
  if [[ -n "$value" && "$value" != "null" ]]; then
    export "$key"="$value"
    echo "  ✓ $key"
  else
    echo "  ⚠ $key (not found)"
  fi
}

echo "Loading environment variables:"

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
export_if_exists TASKFLOW_SENDGRID_API_KEY

# Validate critical secrets
for required in SPRING_DATASOURCE_URL SPRING_DATASOURCE_PASSWORD JWT_SECRET; do
  if [[ -z "${!required:-}" ]]; then
    echo "ERROR: Required secret $required not found"
    exit 1
  fi
done

echo "✅ Secrets loaded and validated"

# Cleanup function
cleanup() {
  echo "🧹 Cleaning up..."
  unset SECRET_JSON
}
trap cleanup EXIT

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down --timeout 30 || true

# Pull latest image
echo "📥 Pulling latest image..."
docker-compose -f docker-compose.prod.yml pull

# Start application
echo "🚀 Starting application..."
docker-compose -f docker-compose.prod.yml up -d

# Enhanced health check with retries
echo "⏳ Waiting for application..."
for i in {1..12}; do
  sleep 10
  if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Deployment successful!"
    exit 0
  fi
  echo "  Attempt $i/12..."
done

echo "❌ Health check failed after 2 minutes"
docker-compose -f docker-compose.prod.yml logs --tail=50 app
exit 1