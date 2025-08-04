# Production Deployment Guide - AWS EC2 with RDS

## Prerequisites

1. **AWS RDS MySQL Instance**
   - Create RDS MySQL instance
   - Note the endpoint, username, and password
   - Configure security group to allow connections from EC2

2. **AWS EC2 Instance**
   - Amazon Linux 2 or Ubuntu
   - t3.micro or larger
   - Security group allowing HTTP (80), HTTPS (443), and SSH (22)

## Deployment Steps

### 1. Prepare EC2 Instance

```bash
# Connect to EC2
ssh -i your-key.pem ec2-user@your-ec2-ip

# Run setup script
curl -O https://raw.githubusercontent.com/your-repo/deploy/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh

# Logout and login again to apply Docker group changes
exit
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 2. Deploy Application

```bash
# Clone repository
git clone https://github.com/your-username/TaskManagementServer.git
cd TaskManagementServer

# Configure environment
cp .env.prod.example .env.prod
nano .env.prod  # Update with your RDS details

# Deploy
chmod +x deploy/deploy.sh
./deploy/deploy.sh
```

### 3. Set Up Auto-Restart (Optional)

```bash
# Create systemd service
chmod +x deploy/systemd-service.sh
./deploy/systemd-service.sh
```

## Environment Variables Configuration

Update `.env.prod` with your actual values:

```bash
# RDS Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint.region.rds.amazonaws.com:3306/taskmanager
SPRING_DATASOURCE_USERNAME=your_rds_username
SPRING_DATASOURCE_PASSWORD=your_rds_password

# JWT Secret (generate a secure 256-bit key)
JWT_SECRET=your-super-secure-jwt-secret-key

# CORS Origins (your domain)
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

## Security Considerations

### RDS Security Group
- Allow inbound MySQL (3306) from EC2 security group
- No public access

### EC2 Security Group
- SSH (22): Your IP only
- HTTP (80): 0.0.0.0/0
- HTTPS (443): 0.0.0.0/0
- Custom (8080): 0.0.0.0/0 (temporary for testing)

### Environment Variables Security
For production secrets, consider using AWS Systems Manager Parameter Store:

```bash
# Store secrets in Parameter Store
aws ssm put-parameter --name "/taskmanager/rds-password" --value "your-password" --type "SecureString"
aws ssm put-parameter --name "/taskmanager/jwt-secret" --value "your-jwt-secret" --type "SecureString"

# Retrieve in application startup script
export SPRING_DATASOURCE_PASSWORD=$(aws ssm get-parameter --name "/taskmanager/rds-password" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET=$(aws ssm get-parameter --name "/taskmanager/jwt-secret" --with-decryption --query "Parameter.Value" --output text)
```

## Monitoring and Maintenance

### Application Logs
```bash
# View application logs
docker-compose -f docker-compose.prod.yml logs -f app

# View system service logs
journalctl -u taskmanager -f
```

### Health Checks
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check container status
docker ps
docker stats
```

### Updates
```bash
# Pull latest changes
git pull origin main

# Redeploy
./deploy/deploy.sh
```

## Load Balancer Setup (Optional)

For production with multiple instances, consider using:
- Application Load Balancer (ALB)
- Auto Scaling Group
- Target Group health checks

## SSL/TLS Setup

### Using Nginx Reverse Proxy
```bash
# Install Nginx
sudo yum install nginx -y

# Configure SSL with Let's Encrypt
sudo yum install certbot python3-certbot-nginx -y
sudo certbot --nginx -d yourdomain.com
```

### Nginx Configuration
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Troubleshooting

### Common Issues
1. **Connection refused**: Check security groups and RDS endpoint
2. **Health check failed**: Check application logs and RDS connectivity
3. **Out of memory**: Increase EC2 instance size or optimize JVM settings

### Debug Commands
```bash
# Test RDS connectivity
mysql -h your-rds-endpoint -u username -p

# Check Docker logs
docker logs taskmanager-prod

# Check system resources
free -h
df -h
```