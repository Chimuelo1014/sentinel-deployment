# 🐳 Sentinel Project - Docker Deployment Guide

## Quick Start

Start all services with one command:

```bash
chmod +x docker-*.sh
./docker-start.sh
```

Stop all services:

```bash
./docker-stop.sh
```

## System Requirements

- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Memory**: 8GB recommended
- **Disk Space**: 20GB for images and volumes

## Architecture Overview

The Sentinel project uses Docker Compose to orchestrate 16 services across 3 layers:

### Infrastructure Services
- **PostgreSQL 15**: Primary database for all Java services
- **MongoDB**: Document database for results aggregation
- **RabbitMQ 3**: Message broker for service communication
- **Kong API Gateway**: API routing and management
- **Konga**: Kong Administration UI

### Java Microservices (7 services)
| Service | Port | Purpose |
|---------|------|---------|
| Backend for Frontend | 8080 | API Gateway & Frontend proxy |
| Auth Service | 8081 | Authentication & Authorization |
| Tenant Service | 8082 | Tenant management |
| Project Service | 8083 | Project management |
| Scanner Orchestrator | 8086 | Scan orchestration & scheduling |
| Results Aggregator | 8087 | Results aggregation & reporting |
| User Management | 8088 | User management service |

### C# Microservices (3 services)
| Service | Port | Purpose |
|---------|------|---------|
| Security Gate | 5001 | Security scanning & policy enforcement |
| Code Quality | 5002 | Code quality analysis |
| Vulnerability | 5003 | Vulnerability scanning |

## Configuration

### Database Credentials

**PostgreSQL:**
```
Host: localhost or postgres (internal)
Port: 5432
Username: sentinel
Password: sentinel123
Database: sentinel_db
```

**MongoDB:**
```
Host: localhost or mongodb (internal)
Port: 27017
Username: sentinel
Password: sentinel123
Database: sentinel_db
```

**RabbitMQ:**
```
Host: localhost or rabbitmq (internal)
Port: 5672
Management UI: http://localhost:15672
Username: sentinel
Password: sentinel123
```

## Service Endpoints

### API Gateway
- **Kong Proxy**: http://localhost:8000
- **Kong Admin API**: http://localhost:8001
- **Konga UI**: http://localhost:1337

### Services
- Auth Service: http://localhost:8081
- BFF Service: http://localhost:8080
- Tenant Service: http://localhost:8082
- Project Service: http://localhost:8083
- Scanner Orchestrator: http://localhost:8086
- Results Aggregator: http://localhost:8087
- User Management: http://localhost:8088
- Security Gate: http://localhost:5001
- Code Quality: http://localhost:5002
- Vulnerability: http://localhost:5003

## Useful Docker Commands

### View Service Status
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
./docker-logs.sh auth-service -f

# Last 100 lines
docker-compose logs --tail=100 auth-service
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart auth-service
```

### Execute Commands in Container
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U sentinel -d sentinel_db

# Connect to MongoDB
docker-compose exec mongodb mongosh -u sentinel -p

# View RabbitMQ stats
docker-compose exec rabbitmq rabbitmq-diagnostics status
```

### Clean Up
```bash
# Stop all services and remove containers
docker-compose down

# Stop and remove all volumes (WARNING: Deletes data)
docker-compose down -v

# Remove unused images
docker image prune

# Remove all unused Docker objects
docker system prune -a
```

## Troubleshooting

### Services Not Starting

Check logs:
```bash
./docker-logs.sh [service-name] -f
```

Common issues:
- **Port already in use**: Change port mapping in docker-compose.yml
- **Out of memory**: Increase Docker memory allocation
- **Network issues**: Ensure sentinel-network exists: `docker network ls`

### Database Connection Issues

Verify PostgreSQL is running:
```bash
docker-compose exec postgres pg_isready -U sentinel
```

Verify MongoDB is running:
```bash
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
```

### Service Health Check

View service health status:
```bash
docker-compose ps

# Look for "healthy" status
```

### Rebuild Services

If services don't start after code changes:
```bash
docker-compose down
docker-compose up -d --build
```

### Clear Everything and Start Fresh

```bash
./docker-stop.sh -v
docker system prune -a
./docker-start.sh
```

## Performance Optimization

### Increase Docker Memory
For macOS/Windows (Docker Desktop):
- Open Docker Desktop Preferences
- Go to Resources
- Increase Memory to 8GB+

For Linux:
- Edit `/etc/docker/daemon.json`
- Add: `"memory": "8g"`

### View Resource Usage
```bash
docker stats
```

## Development Workflow

### Make Changes to Java Service
```bash
# Edit code
# Rebuild and restart
docker-compose up -d --build auth-service

# View logs
./docker-logs.sh auth-service -f
```

### View Database
```bash
# PostgreSQL
docker-compose exec postgres psql -U sentinel -d sentinel_db

# MongoDB
docker-compose exec mongodb mongosh -u sentinel -p
```

## Security Notes

⚠️ **Default Credentials are for Development Only!**

For production, change:
- PostgreSQL password
- MongoDB password
- RabbitMQ password
- Kong settings

Edit in `docker-compose.yml` before deploying.

## Service Dependencies

Startup order is automatically managed:
1. PostgreSQL & Kong Database
2. Kong migrations
3. RabbitMQ
4. All other services (depend on infrastructure being healthy)

## Network Communication

All services communicate via `sentinel-network` bridge:
- Internal hostname: service name (e.g., `postgres`, `rabbitmq`)
- External access: localhost + port number

Example connection strings inside containers:
```
PostgreSQL: jdbc:postgresql://postgres:5432/sentinel_db
MongoDB: mongodb://sentinel:sentinel123@mongodb:27017/sentinel_db
RabbitMQ: amqp://sentinel:sentinel123@rabbitmq:5672
```

## Monitoring

### Real-time Monitoring
```bash
# CPU, Memory, Network stats
docker stats

# Service health
docker-compose ps
```

### Log Analysis
```bash
# Search logs for errors
docker-compose logs | grep ERROR

# Follow specific service
docker-compose logs -f auth-service
```

## Next Steps

1. ✅ Start services: `./docker-start.sh`
2. 📊 Access Konga UI: http://localhost:1337
3. 🔐 Configure Kong routes for microservices
4. 🧪 Test service endpoints
5. 📈 Monitor logs and health status

## Support

For issues with services, check:
- Service logs: `./docker-logs.sh [service-name] -f`
- Docker daemon logs
- Service health: `docker-compose ps`
- Network connectivity: `docker network inspect sentinel-network`
