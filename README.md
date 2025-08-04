# 🚀 Task Management Server

A professional Spring Boot REST API for task management with JWT authentication, containerized with Docker.

## 📋 Features

- **JWT Authentication** with access and refresh tokens
- **Email-based login** with strong password validation
- **Task CRUD operations** with status management
- **Rate limiting** and security features
- **Docker containerization** with MySQL database
- **Production-ready** configuration

## 🐳 Docker Setup

### Prerequisites
- Docker Desktop
- Docker Compose

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/GhassanAbuKhaled/TaskManagementServer.git
   cd TaskManagementServer
   ```

2. **Configure environment variables**
   ```bash
   # Copy and edit the .env file
   cp .env.example .env
   # Edit .env with your preferred values
   ```

3. **Run with Docker Compose**
   ```bash
   # Build and start all services
   docker-compose up --build

   # Run in background
   docker-compose up -d --build
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health

### 🛠️ Development Mode

For development with hot reload and debugging:

```bash
# Use development override
docker-compose -f docker-compose.yml -f docker-compose.override.yml up --build

# Debug port available at 5005
```

### 🏭 Production Deployment

1. **Set production environment variables**
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export JWT_SECRET=your-super-secure-jwt-secret
   export MYSQL_ROOT_PASSWORD=secure-root-password
   # ... other production values
   ```

2. **Run production containers**
   ```bash
   docker-compose -f docker-compose.yml up -d --build
   ```

## 📊 Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | Database connection URL | - | ✅ |
| `SPRING_DATASOURCE_USERNAME` | Database username | - | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - | ✅ |
| `JWT_SECRET` | JWT signing secret | - | ✅ |
| `SERVER_PORT` | Application port | 8080 | ❌ |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | - | ✅ |
| `SPRING_PROFILES_ACTIVE` | Spring profile | dev | ❌ |

## 🔧 Docker Commands

```bash
# Build only
docker-compose build

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Remove volumes (⚠️ deletes data)
docker-compose down -v

# Scale application (multiple instances)
docker-compose up --scale app=3
```

## 📋 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token
- `GET /api/auth/profile` - User profile

### Tasks
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Delete task

### Health & Monitoring
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │
│   (Port 3000)   │◄──►│   (Port 8080)   │
└─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │     MySQL       │
                       │   (Port 3306)   │
                       └─────────────────┘
```

## 🔒 Security Features

- Environment-based configuration
- Non-root Docker user
- Health checks and monitoring
- Input sanitization
- Rate limiting
- JWT token security

## 📝 License

MIT License