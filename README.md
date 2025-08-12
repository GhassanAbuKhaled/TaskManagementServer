# 🚀 Task Management Server

A professional Spring Boot REST API for task management with JWT authentication, containerized with Docker and deployed on AWS.

## 📋 Table of Contents

- [Features](#-features)
- [Quick Start](#-quick-start)
- [Development Setup](#-development-setup)
- [Production Deployment](#-production-deployment)
- [API Documentation](#-api-documentation)
- [Environment Variables](#-environment-variables)
- [Docker Best Practices](#-docker-best-practices)
- [Security](#-security)
- [Troubleshooting](#-troubleshooting)

## 🎯 Features

- **JWT Authentication** with access and refresh tokens
- **Email-based login** with strong password validation
- **Task CRUD operations** with status management
- **Rate limiting** and security features
- **Docker containerization** with MySQL database
- **AWS deployment** with Secrets Manager integration
- **Production-ready** configuration with health checks

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Docker Compose
- AWS CLI (for production)

### Local Development
```bash
# Clone repository
git clone https://github.com/GhassanAbuKhaled/TaskManagementServer.git
cd TaskManagementServer

# Setup development environment
cp .env.example .env

# Start development stack
docker-compose -f docker-compose.dev.yml up --build

# Access application
curl http://localhost:8080/actuator/health
```

## 🛠️ Development Setup

### Development Environment
```bash
# Start with hot reload and debugging
docker-compose -f docker-compose.dev.yml up --build

# Debug port available at 5005
# MySQL available at localhost:3306
```

### Development Features
- **Hot Reload**: Automatic code reloading
- **Debug Port**: IDE debugging on port 5005
- **Local Database**: MySQL container with test data
- **Development Logging**: Detailed logs and SQL queries

### IDE Debug Configuration
1. Set breakpoints in your IDE
2. Create remote debug configuration:
   - Host: `localhost`
   - Port: `5005`
3. Start debugging session

### Development Commands
```bash
# View logs
docker-compose -f docker-compose.dev.yml logs -f app

# Reset database (removes all data)
docker-compose -f docker-compose.dev.yml down -v

# Scale application
docker-compose -f docker-compose.dev.yml up --scale app=2
```

## 🏭 Production Deployment

### Deployment Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Local Build   │───▶│   Docker Hub    │───▶│   EC2 Server    │
│                 │    │                 │    │                 │
│ docker build    │    │ Image Registry  │    │ deploy.sh       │
│ docker push     │    │                 │    │ AWS Secrets     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                      │
                                                      ▼
                                             ┌─────────────────┐
                                             │   Application   │
                                             │   + Database    │
                                             └─────────────────┘
```

### Step 1: Local Build & Push
```bash
# Build application image
docker build -t ghassanabukhaled/taskmanager:latest .

# Test locally (optional)
docker run -p 8080:8080 ghassanabukhaled/taskmanager:latest

# Login to Docker Hub
docker login

# Push to registry
docker push ghassanabukhaled/taskmanager:latest
```

### Step 2: AWS Secrets Setup
Create secrets in AWS Secrets Manager:

```json
{
  "SPRING_PROFILES_ACTIVE": "prod",
  "SPRING_DATASOURCE_URL": "jdbc:mysql://your-rds-endpoint:3306/taskmanager",
  "SPRING_DATASOURCE_USERNAME": "admin",
  "SPRING_DATASOURCE_PASSWORD": "secure-password",
  "JWT_SECRET": "your-256-bit-secret-key",
  "JWT_EXPIRATION": "86400000",
  "JWT_REFRESH_EXPIRATION": "604800000",
  "CORS_ALLOWED_ORIGINS": "https://yourdomain.com",
  "SERVER_PORT": "8080",
  "LOG_LEVEL": "INFO"
}
```

```bash
# Create secret
aws secretsmanager create-secret \
  --name "prod/taskmanager/app-config" \
  --description "Task Manager production configuration"

# Update secret value
aws secretsmanager put-secret-value \
  --secret-id "prod/taskmanager/app-config" \
  --secret-string file://secrets.json
```

### Step 3: Server Deployment
On your EC2 server:

```bash
# Set AWS region
export AWS_REGION=us-east-1

# Run automated deployment
./deploy/deploy.sh
```

The deployment script automatically:
- Retrieves secrets from AWS Secrets Manager
- Pulls latest image from Docker Hub
- Starts application with Docker Compose
- Performs health checks

### Manual Production Commands
```bash
# Manual deployment
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f app

# Health check
curl http://localhost:8080/actuator/health
```

## 📋 API Documentation

### Authentication Endpoints
```bash
# User registration
POST /api/auth/register
Content-Type: application/json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

# User login
POST /api/auth/login
Content-Type: application/json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

# Refresh token
POST /api/auth/refresh
Authorization: Bearer <refresh_token>

# Get user profile
GET /api/auth/profile
Authorization: Bearer <access_token>
```

### Task Management Endpoints
```bash
# Get all tasks
GET /api/tasks
Authorization: Bearer <access_token>

# Create task
POST /api/tasks
Authorization: Bearer <access_token>
Content-Type: application/json
{
  "title": "Complete project",
  "description": "Finish the task management system",
  "priority": "HIGH",
  "dueDate": "2024-12-31T23:59:59"
}

# Update task
PUT /api/tasks/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
{
  "title": "Updated task title",
  "description": "Updated description",
  "status": "IN_PROGRESS"
}

# Update task status
PATCH /api/tasks/{id}/status
Authorization: Bearer <access_token>
Content-Type: application/json
{
  "status": "COMPLETED"
}

# Delete task
DELETE /api/tasks/{id}
Authorization: Bearer <access_token>
```

### Health & Monitoring
```bash
# Application health
GET /actuator/health

# Application info
GET /actuator/info
```

## 📊 Environment Variables

### Required Variables
| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:mysql://localhost:3306/taskmanager` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `secure-password` |
| `JWT_SECRET` | JWT signing secret (256-bit) | `base64-encoded-secret` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `https://yourdomain.com` |

### Optional Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |
| `SERVER_PORT` | Application port | `8080` |
| `JWT_EXPIRATION` | Access token expiration (ms) | `86400000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Environment Variable Flow

#### Development
```
.env.example → .env (local) → docker-compose.dev.yml → Container
```

#### Production
```
AWS Secrets Manager → deploy.sh → Environment Variables → docker-compose.prod.yml → Container
```

## 🐳 Docker Best Practices

### Development Configuration
```yaml
# docker-compose.dev.yml
services:
  app:
    build: .
    ports:
      - "8080:8080"
      - "5005:5005"  # Debug port
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    env_file:
      - .env
    volumes:
      - ./target/classes:/app/target/classes  # Hot reload
    depends_on:
      - mysql
    
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: devpassword123
      MYSQL_DATABASE: taskmanager
    ports:
      - "3306:3306"
    volumes:
      - mysql_dev_data:/var/lib/mysql
```

### Production Configuration
```yaml
# docker-compose.prod.yml
services:
  app:
    image: ghassanabukhaled/taskmanager:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - JWT_SECRET=${JWT_SECRET}
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

## 🔒 Security

### Security Features
- **JWT Authentication** with access and refresh tokens
- **Password Encryption** using BCrypt
- **Rate Limiting** to prevent abuse
- **CORS Configuration** for cross-origin requests
- **Input Validation** and sanitization
- **Security Headers** (HSTS, Frame Options)
- **Non-root Container** execution
- **Secret Management** via AWS Secrets Manager

### Security Checklist

#### Development Security
- [ ] `.env` files in `.gitignore`
- [ ] Use `.env.example` as template
- [ ] Separate dev/prod configurations
- [ ] Enable SSL even in development

#### Production Security
- [ ] Use external secret management (AWS Secrets Manager)
- [ ] Enable SSL/TLS for all connections
- [ ] Use specific image tags, not `latest`
- [ ] Run containers as non-root user
- [ ] Implement health checks and monitoring
- [ ] Set resource limits
- [ ] Use managed databases with encryption

#### Common Security Mistakes to Avoid
- ❌ Committing `.env` files with real secrets
- ❌ Using weak default passwords
- ❌ Exposing debug ports in production
- ❌ Running containers as root
- ❌ Using `latest` tags in production
- ❌ Storing secrets in Docker images

## 🔧 Troubleshooting

### Common Issues

#### Build Failures
```bash
# Check Docker build
docker build -t ghassanabukhaled/taskmanager:latest .

# Check Maven dependencies
./mvnw dependency:tree

# Clean build
./mvnw clean package -DskipTests
```

#### Database Connection Issues
```bash
# Check database connectivity
docker exec -it mysql-container mysql -u root -p

# Verify environment variables
docker exec -it app-container env | grep SPRING

# Check application logs
docker-compose logs -f app
```

#### Secrets Management Issues
```bash
# Test AWS credentials
aws sts get-caller-identity

# Check secret exists
aws secretsmanager describe-secret --secret-id prod/taskmanager/app-config

# Retrieve secret value
aws secretsmanager get-secret-value --secret-id prod/taskmanager/app-config
```

#### Health Check Failures
```bash
# Test health endpoint
curl -f http://localhost:8080/actuator/health

# Check application startup
docker-compose logs -f app

# Verify port binding
docker-compose ps
```

### Rollback Process
```bash
# Stop current deployment
docker-compose -f docker-compose.prod.yml down

# Deploy previous version
docker pull ghassanabukhaled/taskmanager:previous-tag
docker-compose -f docker-compose.prod.yml up -d
```

### Monitoring Commands
```bash
# System resources
docker stats

# Container logs
docker-compose logs -f app

# Health status
watch -n 5 'curl -s http://localhost:8080/actuator/health'

# Database connections
docker exec -it mysql-container mysql -u root -p -e "SHOW PROCESSLIST;"
```

## 📚 Additional Resources

### Spring Boot Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### Docker Documentation
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Multi-stage Builds](https://docs.docker.com/develop/dev-best-practices/)

### AWS Documentation
- [AWS Secrets Manager](https://docs.aws.amazon.com/secretsmanager/)
- [Amazon RDS](https://docs.aws.amazon.com/rds/)
- [Amazon EC2](https://docs.aws.amazon.com/ec2/)

## 📝 License

MIT License - see LICENSE file for details.

---

## Quick Commands Reference

```bash
# Development
cp .env.example .env && docker-compose -f docker-compose.dev.yml up --build

# Build and Push
docker build -t ghassanabukhaled/taskmanager:latest . && docker push ghassanabukhaled/taskmanager:latest

# Production Deploy
export AWS_REGION=us-east-1 && ./deploy/deploy.sh

# Health Check
curl http://localhost:8080/actuator/health
```