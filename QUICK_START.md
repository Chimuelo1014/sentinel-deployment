# ⚡ QUICK START GUIDE - Sentinel Backend Implementation

**Quick Access to Everything You Need**

---

## 🚀 Start Services (5 minutes)

### 1. Start Dependencies (Docker)
```bash
cd ~/Música/sentinel

# Start RabbitMQ (if not running)
docker-compose -f docker-compose.yml up -d rabbitmq

# Start Kong Gateway
docker-compose -f kong-docker-compose.yml up -d

# Verify Kong is ready
docker-compose -f kong-docker-compose.yml ps
```

### 2. Configure Kong
```bash
# Run configuration script (one-time)
./kong-setup.sh

# Expected: ✅ Kong Configuration Complete
```

### 3. Start BFF Service
```bash
cd backend-for-frontend-service

# Compile
mvn clean compile

# Run
mvn spring-boot:run

# Or use your IDE (Run → BackendForFrontendServiceApplication)
```

### 4. Verify Everything Works
```bash
# Run integration tests
./test-integration.sh

# Expected: 9 test categories, mostly PASS
```

---

## 📍 Access Points (Bookmarks These!)

| Service | URL | Purpose |
|---------|-----|---------|
| **Kong Proxy** | http://localhost:8000 | Frontend API entry point |
| **Kong Admin** | http://localhost:8001 | Admin API management |
| **Kong Manager** | http://localhost:8002 | Web-based GUI |
| **BFF Service** | http://localhost:8080 | Backend for Frontend |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API documentation |
| **OpenAPI Spec** | http://localhost:8080/v3/api-docs | Machine-readable API |

---

## 🧪 Test API Endpoints (10 seconds each)

### Get JWT Token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@sentinel.com",
    "password": "Admin@123"
  }' | jq '.token'

# Save token to environment variable
export TOKEN="eyJhbGc..."
export TENANT_ID="your-tenant-id"
```

### Test Dashboard (Through Kong Gateway)
```bash
curl http://localhost:8000/api/bff/dashboard \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .
```

### Test Direct BFF (No Gateway)
```bash
curl http://localhost:8080/api/bff/dashboard \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .
```

### Test Other Endpoints
```bash
# List projects
curl http://localhost:8000/api/bff/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .

# List scans
curl http://localhost:8000/api/bff/scans \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .

# Get analytics
curl http://localhost:8000/api/bff/analytics/vulnerabilities \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .

# Get user profile
curl http://localhost:8000/api/bff/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" | jq .
```

---

## 🐛 Troubleshooting (Quick Fixes)

### Kong won't start
```bash
# Check logs
docker-compose -f kong-docker-compose.yml logs kong

# Reset and restart
docker-compose -f kong-docker-compose.yml down -v
docker-compose -f kong-docker-compose.yml up -d
./kong-setup.sh
```

### BFF service fails to start
```bash
# Check compilation errors
cd backend-for-frontend-service
mvn clean compile

# Check logs in IDE
# Look for database connection issues
```

### JWT validation fails
```bash
# Verify token is valid (check expiration on jwt.io)
# Get new token
curl -X POST http://localhost:8081/api/auth/login ...

# Verify Auth Service is running
curl http://localhost:8081/health
```

### Database connection issues
```bash
# Check PostgreSQL
psql -U postgres -h localhost -l

# Check MongoDB
mongo localhost:27017

# Verify databases exist
psql -U postgres -h localhost -c "SELECT datname FROM pg_database WHERE datname LIKE 'sentinel%';"
```

---

## 📊 Key Files & Locations

```
~/Música/sentinel/
├── backend-for-frontend-service/          # BFF Service
│   ├── src/main/java/com/sentinel/
│   │   ├── controller/                    # 6 Controllers
│   │   ├── service/                       # 2 Interfaces + 2 Implementations
│   │   ├── client/                        # 5 Feign Clients
│   │   ├── dto/                           # 4 DTOs
│   │   ├── exception/                     # GlobalExceptionHandler
│   │   └── config/                        # OpenApiConfig
│   └── src/main/resources/
│       └── application.properties          # Full configuration
│
├── kong-docker-compose.yml                # Kong + DB setup
├── kong-setup.sh                          # Kong configuration
├── test-integration.sh                    # Integration tests
│
├── KONG_SETUP_GUIDE.md                    # Kong reference (700+ lines)
├── IMPLEMENTACION_COMPLETADA.md           # Summary document
├── RUTAS_API_FRONTEND.md                  # API specifications
└── README_INTEGRACION.md                  # Integration guide
```

---

## ✅ What's Implemented

### ✅ Backend for Frontend (BFF)
- 6 Controllers (Dashboard, Scan, Project, Analytics, User, Notification)
- 20+ endpoints
- Service layer with Feign integration
- DTOs for data transfer
- OpenAPI/Swagger documentation
- Global exception handling
- JWT authentication support
- Multi-tenant support

### ✅ Kong API Gateway
- Service configuration (bff-service)
- 6 routes configured
- JWT authentication plugin
- Rate limiting (1000 req/min)
- CORS enabled
- Request logging
- Health checks

### ✅ Database Configuration
- PostgreSQL: sentinel_bff database
- MongoDB: sentinel_bff_cache database
- JPA/Hibernate auto-schema creation
- Connection pooling configured

### ✅ Testing & Documentation
- Integration test suite (9 categories)
- API documentation (Swagger)
- Kong operation guide (700+ lines)
- Implementation summary (2000+ lines)
- Quick start guide (this file)

---

## 📈 Next Steps

1. **Run Tests**: `./test-integration.sh`
2. **Review Swagger**: http://localhost:8080/swagger-ui.html
3. **Check Kong**: http://localhost:8002 (Manager GUI)
4. **Load Testing**: Use Apache JMeter or similar
5. **Deploy**: Follow production checklist in IMPLEMENTACION_COMPLETADA.md

---

## 🎯 Common Tasks

### Update BFF Configuration
```bash
cd backend-for-frontend-service/src/main/resources
# Edit application.properties
# Restart service
```

### Add New Kong Route
```bash
curl -X POST http://localhost:8001/services/bff-service/routes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "route-name",
    "paths": ["/api/bff/path"],
    "strip_path": false
  }'
```

### Enable New Plugin
```bash
curl -X POST http://localhost:8001/services/bff-service/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "plugin-name",
    "config": {...}
  }'
```

### View Kong Status
```bash
curl http://localhost:8001/status | jq .
```

### View BFF Metrics
```bash
curl http://localhost:8080/actuator/health | jq .
```

---

## 🆘 Getting Help

1. **Kong Issues**: See `KONG_SETUP_GUIDE.md` troubleshooting section
2. **BFF Issues**: Check `IMPLEMENTACION_COMPLETADA.md` 
3. **API Issues**: Check `RUTAS_API_FRONTEND.md`
4. **Integration Issues**: Run `./test-integration.sh`
5. **Database Issues**: Review `REVISION_COMPLETA_PROYECTO.md`

---

## 📞 Support Contacts

- **BFF Service**: Check logs → IDE console
- **Kong Issues**: Admin API → http://localhost:8001
- **Database Issues**: Check connection strings in application.properties
- **Integration**: Run test script for diagnostics

---

**Last Updated**: December 12, 2025  
**Version**: 1.0  
**Status**: Ready for Use
