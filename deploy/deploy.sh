#!/bin/bash

# Deployment Script for Task Manager Application
# Run this script on EC2 after setup-ec2.sh

set -e

echo "🚀 Deploying Task Manager Application..."

# Check if .env.prod exists
if [ ! -f .env.prod ]; then
    echo "❌ Error: .env.prod file not found!"
    echo "Please create .env.prod file with your RDS details"
    exit 1
fi

# Validate required environment variables
echo "🔍 Validating environment variables..."
source .env.prod

required_vars=("SPRING_DATASOURCE_URL" "SPRING_DATASOURCE_USERNAME" "SPRING_DATASOURCE_PASSWORD" "JWT_SECRET")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Error: $var is not set in .env.prod"
        exit 1
    fi
done

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose -f docker-compose.prod.yml down || true

# Build and start the application
echo "🏗️ Building and starting application..."
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 30

# Check application health
echo "🏥 Checking application health..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is healthy and running!"
    echo "🌐 Application URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
else
    echo "❌ Application health check failed"
    echo "📋 Checking logs..."
    docker-compose -f docker-compose.prod.yml logs app
    exit 1
fi

echo "🎉 Deployment completed successfully!"