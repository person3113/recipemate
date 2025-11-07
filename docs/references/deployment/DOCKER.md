# RecipeMate Docker Setup

This directory contains Docker configuration for running RecipeMate in containerized environments.

## Prerequisites

- Docker Engine 20.10+ 
- Docker Compose 2.0+
- Minimum 4GB RAM available for Docker

## Quick Start

### 1. Environment Setup

#### Step 1.1: Create .env file

Copy the example environment file:

```bash
# Linux/Mac
cp .env.example .env

# Windows (Command Prompt)
copy .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env
```

#### Step 1.2: Configure environment variables

Open `.env` file and configure the following:

**Database Configuration:**
```bash
DB_USERNAME=recipemate          # PostgreSQL username
DB_PASSWORD=recipemate2024!secure  # CHANGE THIS for production!
```

**Redis Configuration:**
```bash
REDIS_PASSWORD=redis2024!secure    # CHANGE THIS for production!
```

**Food Safety Korea API Key:**
```bash
# Get your API key from: https://www.foodsafetykorea.go.kr/api/
# 1. Visit the website and sign up (free)
# 2. Go to "공공데이터활용 신청" -> "오픈API 신청"
# 3. Choose "COOKRCP01" (조리식품의 레시피 DB조회 서비스)
# 4. Copy your API key
FOOD_SAFETY_API_KEY=your_actual_api_key_here
```

**Application Profile:**
```bash
SPRING_PROFILES_ACTIVE=prod     # Use 'prod' for Docker deployment
```

#### Step 1.3: Security Best Practices

**For Development:**
- Use simple passwords like `recipemate2024!dev`
- Keep credentials in `.env` file (already in .gitignore)

**For Production:**
- Use strong passwords (16+ characters, mixed case, numbers, symbols)
- Example: `Xk9#mL2$pQ7@nR5^wT8!vY3`
- Rotate passwords every 90 days
- Never commit `.env` to version control
- Consider using secrets manager:
  - AWS Secrets Manager
  - Azure Key Vault
  - HashiCorp Vault
  - Kubernetes Secrets

**Password Generation Example:**
```bash
# Generate secure random password (Linux/Mac)
openssl rand -base64 24

# Generate secure random password (Windows PowerShell)
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 20 | % {[char]$_})
```

### 2. Build and Run

Start all services:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL 16 (port 5432)
- Redis 7 (port 6379)
- RecipeMate Spring Boot Application (port 8080)
- Nginx Reverse Proxy (port 80)

### 3. Verify Services

Check if all services are running:

```bash
docker-compose ps
```

Check application logs:

```bash
docker-compose logs -f app
```

Access the application:
- Via Nginx: http://localhost
- Direct access: http://localhost:8080

### 4. Stop Services

Stop all services:

```bash
docker-compose down
```

Stop and remove volumes (WARNING: deletes all data):

```bash
docker-compose down -v
```

## Architecture

```
┌─────────┐
│ Browser │
└────┬────┘
     │
     ▼
┌─────────────┐
│   Nginx     │ (Port 80)
│   Proxy     │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Spring Boot    │ (Port 8080)
│  Application    │
└────┬───────┬────┘
     │       │
     ▼       ▼
┌──────┐  ┌───────┐
│ Postgres │ Redis │
│  DB      │ Cache │
└──────┘  └───────┘
```

## Services Configuration

### PostgreSQL
- **Image**: postgres:16-alpine
- **Port**: 5432
- **Database**: recipemate
- **Volume**: postgres_data
- **Health Check**: pg_isready

### Redis
- **Image**: redis:7-alpine
- **Port**: 6379
- **Memory Limit**: 256MB
- **Eviction Policy**: allkeys-lru
- **Volume**: redis_data

### Spring Boot Application
- **Build**: Multi-stage Dockerfile (Gradle + JRE)
- **Base Image**: eclipse-temurin:21-jre-alpine
- **Port**: 8080
- **Profile**: prod
- **Health Check**: /actuator/health
- **Volume**: app_uploads (for file storage)

### Nginx
- **Image**: nginx:1.25-alpine
- **Ports**: 80 (HTTP), 443 (HTTPS - ready for SSL)
- **Features**: 
  - Reverse proxy to Spring Boot
  - Gzip compression
  - Static resource caching
  - Security headers

## Development vs Production

### Development Mode
Use the default Spring Boot dev profile with H2 database:

```bash
./gradlew bootRun
```

### Production Mode (Docker)
Uses PostgreSQL, Redis, and optimized JVM settings:

```bash
docker-compose up -d
```

## Common Operations

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f nginx
```

### Rebuild Application
```bash
# Rebuild and restart app service
docker-compose up -d --build app
```

### Database Access
```bash
# Connect to PostgreSQL (interactive)
docker exec -it recipemate-postgres psql -U recipemate -d recipemate

# Execute SQL file
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < schema.sql

# Backup database
docker exec recipemate-postgres pg_dump -U recipemate recipemate > backup.sql

# Restore database
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < backup.sql
```

### Redis Access
```bash
# Connect to Redis CLI (use your actual password)
docker exec -it recipemate-redis redis-cli -a redis2024!secure

# Check Redis connection
docker exec recipemate-redis redis-cli -a redis2024!secure ping

# Monitor Redis commands
docker exec -it recipemate-redis redis-cli -a redis2024!secure monitor

# Clear all cache
docker exec recipemate-redis redis-cli -a redis2024!secure FLUSHALL
```

### Scale Services
```bash
# Run multiple app instances (requires load balancer config)
docker-compose up -d --scale app=3
```

## Volumes

Persistent data is stored in Docker volumes:

- `postgres_data`: Database files
- `redis_data`: Redis persistence
- `app_uploads`: Uploaded files (images, etc.)
- `nginx_logs`: Nginx access and error logs

### Backup Volumes
```bash
# Backup PostgreSQL
docker exec recipemate-postgres pg_dump -U recipemate recipemate > backup.sql

# Restore PostgreSQL
docker exec -i recipemate-postgres psql -U recipemate recipemate < backup.sql
```

## Performance Tuning

### JVM Options
Edit `docker-compose.yml` to adjust JVM settings:

```yaml
environment:
  JAVA_OPTS: >-
    -XX:+UseContainerSupport
    -XX:MaxRAMPercentage=75.0
    -XX:+UseG1GC
```

### PostgreSQL Tuning
Edit `docker-compose.yml` to add PostgreSQL parameters:

```yaml
command: >
  -c shared_buffers=256MB
  -c max_connections=200
  -c effective_cache_size=1GB
```

## SSL/HTTPS Configuration

To enable HTTPS:

1. Obtain SSL certificates (Let's Encrypt recommended)
2. Place certificates in `nginx/ssl/` directory
3. Uncomment HTTPS server block in `nginx/conf.d/recipemate.conf`
4. Update `docker-compose.yml` to mount SSL directory
5. Restart Nginx: `docker-compose restart nginx`

## Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs app

# Common issues:
# - Database not ready: Wait for health check
# - Port already in use: Stop conflicting services
# - Environment variables: Check .env file
```

### Database connection errors
```bash
# Verify PostgreSQL is healthy
docker-compose ps postgres

# Check network connectivity
docker-compose exec app ping postgres
```

### Out of memory errors
```bash
# Increase Docker memory limit
# Docker Desktop: Settings > Resources > Memory

# Or reduce JVM heap
# Edit JAVA_OPTS in docker-compose.yml
```

### Redis connection refused
```bash
# Check Redis is running
docker-compose ps redis

# Verify password in .env matches application config
```

## Monitoring

### Health Checks
- Application: http://localhost:8080/actuator/health
- Nginx: http://localhost/health

### Resource Usage
```bash
# Monitor resource usage
docker stats

# Specific service
docker stats recipemate-app
```

## Security Best Practices

1. **Never commit `.env` file** to version control
2. **Use strong passwords** for database and Redis
3. **Keep images updated**: `docker-compose pull`
4. **Enable SSL/HTTPS** in production
5. **Restrict network access** using firewall rules
6. **Regular backups** of database volumes
7. **Monitor logs** for suspicious activity

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Build and push Docker image
  run: |
    docker build -t recipemate:${{ github.sha }} .
    docker tag recipemate:${{ github.sha }} recipemate:latest
```

### Docker Hub
```bash
# Tag and push
docker tag recipemate-app:latest username/recipemate:latest
docker push username/recipemate:latest
```

