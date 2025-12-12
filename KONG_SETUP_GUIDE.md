# 🚀 Kong API Gateway - Sentinel Implementation Guide

**Created**: December 12, 2025  
**Version**: 1.0  
**Status**: Ready for Deployment

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [API Routes](#api-routes)
6. [Authentication](#authentication)
7. [Rate Limiting](#rate-limiting)
8. [Monitoring](#monitoring)
9. [Troubleshooting](#troubleshooting)
10. [Production Deployment](#production-deployment)

---

## 🎯 Overview

Kong is an open-source API Gateway that sits in front of the Sentinel Backend for Frontend (BFF) service. It provides:

- **Request Routing**: Routes all frontend requests to the appropriate backend services
- **Authentication**: Validates JWT tokens from the Auth Service
- **Rate Limiting**: Prevents API abuse with configurable rate limits
- **CORS**: Handles cross-origin requests from web/mobile clients
- **Logging**: Tracks all API requests for monitoring and debugging
- **Load Balancing**: Distributes load across multiple instances

### Key Benefits

- ✅ Single entry point for all frontend requests
- ✅ Centralized authentication and authorization
- ✅ Protection against DDoS and brute force attacks
- ✅ Request/response transformation
- ✅ API versioning support
- ✅ Request tracing and debugging

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────┐
│                     Frontend (Web/Mobile)                 │
└────────────────────────┬─────────────────────────────────┘
                         │
                    HTTPS/HTTP
                         │
┌────────────────────────▼─────────────────────────────────┐
│                   Kong API Gateway                        │
│  (Port 8000 - Proxy, Port 8001 - Admin API)              │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │             Plugins & Middleware                   │  │
│  │  - JWT Authentication                             │  │
│  │  - Rate Limiting (1000 req/min per consumer)      │  │
│  │  - CORS                                           │  │
│  │  - Request Logging                                │  │
│  │  - Request/Response Transformation                │  │
│  └────────────────────────────────────────────────────┘  │
│                         │                                 │
└─────────────────────────┼─────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
    ┌───────────┐  ┌───────────┐  ┌────────────┐
    │    BFF    │  │   Cache   │  │ Monitoring │
    │ (Port     │  │(MongoDB)  │  │  Services  │
    │  8080)    │  │           │  │            │
    └───────────┘  └───────────┘  └────────────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
 PostgreSQL   Services
 (Metadata)  (Orchestrator,
             Tenant, etc)
```

---

## ⚡ Quick Start

### Prerequisites

- Docker & Docker Compose
- curl or Postman
- Valid JWT token (from Auth Service)

### 1. Start Kong

```bash
# Navigate to Sentinel project
cd /home/samup/Música/sentinel

# Start Kong and its database
docker-compose -f kong-docker-compose.yml up -d

# Verify Kong is running
docker-compose -f kong-docker-compose.yml ps

# Expected output:
# NAME                 STATUS
# sentinel-kong        Up (healthy)
# kong-database        Up (healthy)
```

### 2. Configure Kong

```bash
# Run the configuration script
./kong-setup.sh

# Expected output:
# ✅ Kong Configuration Complete
# 📊 Configured Services: bff-service
# 🛣️  Configured Routes: (all BFF routes)
```

### 3. Test the Gateway

```bash
# Get a valid JWT token from Auth Service
TOKEN=$(curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.token')

# Test a route through Kong
curl http://localhost:8000/api/bff/dashboard \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: your-tenant-id"

# Expected response: 200 OK with dashboard data
```

---

## ⚙️ Configuration

### Kong Services and Routes

Kong is configured with the following services and routes:

| Service | Upstream URL | Routes | Purpose |
|---------|--------------|--------|---------|
| bff-service | http://host.docker.internal:8080 | /api/bff/* | Backend for Frontend |

### Configured Routes

| Route Name | Path Pattern | Service | Purpose |
|-----------|--------------|---------|---------|
| bff-dashboard | /api/bff/dashboard | bff-service | Dashboard data aggregation |
| bff-scans | /api/bff/scans* | bff-service | Scan management |
| bff-projects | /api/bff/projects* | bff-service | Project management |
| bff-analytics | /api/bff/analytics* | bff-service | Analytics and insights |
| bff-users | /api/bff/users* | bff-service | User profiles and preferences |
| bff-notifications | /api/bff/notifications* | bff-service | Notification management |

### Enabled Plugins

#### 1. **JWT Authentication**

Validates incoming requests have a valid JWT token.

```bash
curl http://localhost:8001/services/bff-service/plugins \
  -H "Content-Type: application/json"

# Configuration:
# - key_claim_name: "iss"
# - secret_is_base64: false
# - cookie_names: ["jwt"]
```

**How to Use:**

```bash
# Include token in Authorization header
curl http://localhost:8000/api/bff/dashboard \
  -H "Authorization: Bearer eyJhbGc..."

# Or as query parameter
curl http://localhost:8000/api/bff/dashboard?token=eyJhbGc...
```

#### 2. **Rate Limiting**

Limits API requests to prevent abuse.

```
Configuration:
- Limit: 1000 requests per minute per consumer
- Policy: local (in-memory, suitable for single Kong instance)
- Fault Tolerant: true (allows requests if limit can't be enforced)
```

**Response Headers:**

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1702390800
```

#### 3. **CORS**

Allows cross-origin requests from web/mobile clients.

```
Configuration:
- Allowed Origins: * (all origins)
- Allowed Methods: GET, HEAD, PUT, PATCH, POST, DELETE
- Allowed Headers: Standard + custom (X-Tenant-Id, Authorization)
- Credentials: true
- Max Age: 3600 seconds
```

#### 4. **Request Logging**

Adds request tracking headers for monitoring.

```
Added Headers:
- X-Kong-Forwarded-For: Original client IP
- X-Request-ID: Unique request identifier
```

---

## 🔐 Authentication

### JWT Token Validation

Kong validates JWT tokens using the Auth Service's shared secret.

**JWT Secret (shared across all services):**
```
X9q2N8ZCnO3Tj48p1Fk6B2V0x8Teq9gHBV0SX1e2p6U=
```

### Token Generation Flow

```
1. User submits credentials to Auth Service
   POST /api/auth/login
   {
     "email": "user@example.com",
     "password": "password"
   }

2. Auth Service validates and returns JWT token
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "...",
     "expiresIn": 3600
   }

3. Client uses token in subsequent requests
   GET /api/bff/dashboard
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

4. Kong validates token before forwarding to BFF
   - Checks signature
   - Validates expiration
   - Returns 401 if invalid
```

### Handling Invalid Tokens

```
Request:
GET /api/bff/dashboard
Authorization: Bearer invalid-token

Response:
401 Unauthorized
{
  "error": "Invalid token"
}
```

---

## ⏱️ Rate Limiting

### Limits

- **Default**: 1000 requests per minute per consumer
- **By Consumer**: Can be customized per API key/user

### Response Headers

All API responses include rate limit information:

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 998
X-RateLimit-Reset: 1702390800
```

### Exceeding Limits

```
Request (1001st request):
GET /api/bff/dashboard
Authorization: Bearer eyJhbGc...

Response (429 Too Many Requests):
{
  "message": "API rate limit exceeded"
}
```

### Configuring Per-Consumer Limits

```bash
# Create a consumer with custom rate limit
curl -X POST http://localhost:8001/consumers \
  -d "username=premium-user"

curl -X POST http://localhost:8001/consumers/premium-user/acls \
  -d "group=premium"

# Create rate-limiting plugin for premium group
curl -X POST http://localhost:8001/services/bff-service/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "rate-limiting",
    "config": {
      "minute": 10000
    },
    "tags": ["premium"]
  }'
```

---

## 📊 Monitoring

### Kong Admin API

Access Kong's administrative endpoints at `http://localhost:8001`

```bash
# View all services
curl http://localhost:8001/services

# View all routes
curl http://localhost:8001/routes

# View all plugins
curl http://localhost:8001/plugins

# View metrics
curl http://localhost:8001/status
```

### Kong Manager GUI

Access the web-based Kong Manager at `http://localhost:8002`

Features:
- Visual service and route management
- Plugin configuration
- Consumer management
- Analytics and metrics

### Request Logging

All requests are logged with the following information:

```
[request_id: abc-123]
[timestamp: 2025-12-12T17:05:00Z]
[consumer: user-1]
[path: /api/bff/dashboard]
[method: GET]
[status: 200]
[latency: 45ms]
[response_size: 2048]
```

### Metrics Endpoint

```bash
curl http://localhost:8001/status

{
  "server": {
    "connections_active": 5,
    "connections_accepted": 1250,
    "connections_handled": 1250,
    "connections_reading": 1,
    "connections_waiting": 2,
    "connections_writing": 2
  },
  "database": {
    "reachable": true
  }
}
```

---

## 🐛 Troubleshooting

### Issue: Kong won't start

**Symptom**: Container exits or health check fails

**Solutions**:

```bash
# Check logs
docker-compose -f kong-docker-compose.yml logs kong

# Check database connectivity
docker-compose -f kong-docker-compose.yml logs kong-db

# Verify database is ready
docker exec kong-database pg_isready -U kong -d kong

# Restart Kong
docker-compose -f kong-docker-compose.yml restart kong
```

### Issue: Routes return 502 Bad Gateway

**Symptom**: `Error communicating with upstream`

**Solutions**:

```bash
# 1. Verify BFF service is running
curl http://localhost:8080/api/bff/dashboard

# 2. Check Kong routes configuration
curl http://localhost:8001/routes

# 3. Update upstream service URL if needed
curl -X PATCH http://localhost:8001/services/bff-service \
  -d "url=http://host.docker.internal:8080"

# 4. On Linux, use the actual host IP instead of host.docker.internal
# Find your host IP: hostname -I
curl -X PATCH http://localhost:8001/services/bff-service \
  -d "url=http://192.168.1.100:8080"
```

### Issue: JWT validation fails with 401

**Symptom**: `Invalid token` error even with valid JWT

**Solutions**:

```bash
# 1. Verify JWT plugin is enabled
curl http://localhost:8001/services/bff-service/plugins | jq

# 2. Check token expiration
# Decode JWT: https://jwt.io/
# Verify "exp" claim is in the future

# 3. Verify shared secret matches
# Auth Service JWT Secret: X9q2N8ZCnO3Tj48p1Fk6B2V0x8Teq9gHBV0SX1e2p6U=

# 4. Test without JWT plugin
curl -X DELETE http://localhost:8001/services/bff-service/plugins/{plugin-id}

# 5. Regenerate token from Auth Service
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

### Issue: CORS errors in browser

**Symptom**: `No 'Access-Control-Allow-Origin' header`

**Solutions**:

```bash
# 1. Verify CORS plugin is enabled
curl http://localhost:8001/services/bff-service/plugins | grep cors

# 2. Check CORS configuration
curl http://localhost:8001/services/bff-service/plugins/{cors-plugin-id}

# 3. Update CORS config if needed
curl -X PATCH http://localhost:8001/services/bff-service/plugins/{cors-plugin-id} \
  -H "Content-Type: application/json" \
  -d '{
    "config": {
      "origins": ["http://localhost:3000", "https://app.sentinel.com"],
      "methods": ["GET", "HEAD", "PUT", "PATCH", "POST", "DELETE", "OPTIONS"],
      "credentials": true
    }
  }'
```

### Issue: Rate limit not enforced

**Symptom**: Can make unlimited requests

**Solutions**:

```bash
# 1. Verify rate-limiting plugin is enabled
curl http://localhost:8001/services/bff-service/plugins | grep rate-limiting

# 2. Check plugin configuration
curl http://localhost:8001/services/bff-service/plugins/{ratelimit-plugin-id}

# 3. Enable rate limiting if missing
./kong-setup.sh

# 4. Monitor rate limit headers
curl -v http://localhost:8000/api/bff/dashboard \
  -H "Authorization: Bearer $TOKEN" | grep X-RateLimit
```

---

## 🚀 Production Deployment

### Environment Variables

Update `kong-docker-compose.yml` for production:

```yaml
environment:
  KONG_DATABASE: postgres
  KONG_PG_HOST: prod-kong-db.internal
  KONG_PG_USER: kong
  KONG_PG_PASSWORD: ${KONG_DB_PASSWORD}  # Use secrets management
  KONG_PROXY_ACCESS_LOG: /var/log/kong/access.log
  KONG_PROXY_ERROR_LOG: /var/log/kong/error.log
  KONG_ADMIN_GUI_URL: https://kong-admin.sentinel.com
  KONG_ADMIN_LISTEN: 0.0.0.0:8001 ssl
  KONG_PROXY_LISTEN: 0.0.0.0:8000 ssl, 0.0.0.0:8000
```

### SSL/TLS Configuration

```bash
# 1. Add SSL certificates to Kong
curl -X POST http://localhost:8001/certificates \
  -H "Content-Type: application/json" \
  -d '{
    "cert": "-----BEGIN CERTIFICATE-----...",
    "key": "-----BEGIN PRIVATE KEY-----...",
    "tags": ["production"]
  }'

# 2. Create SNI (Server Name Indication) entry
curl -X POST http://localhost:8001/snis \
  -d "certificate_id=cert-id" \
  -d "name=api.sentinel.com"
```

### Database Backup

```bash
# Backup Kong database
docker exec kong-database pg_dump -U kong kong > kong_backup.sql

# Restore Kong database
docker exec -i kong-database psql -U kong kong < kong_backup.sql
```

### Monitoring and Alerting

```bash
# Add Prometheus plugin for metrics
curl -X POST http://localhost:8001/services/bff-service/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "prometheus",
    "config": {}
  }'

# Metrics available at http://localhost:8001/metrics
```

### Load Balancing

Kong supports load balancing across multiple upstream servers:

```bash
# Create upstream
curl -X POST http://localhost:8001/upstreams \
  -d "name=bff-cluster"

# Add targets
curl -X POST http://localhost:8001/upstreams/bff-cluster/targets \
  -d "target=bff-1.internal:8080" \
  -d "weight=100"

curl -X POST http://localhost:8001/upstreams/bff-cluster/targets \
  -d "target=bff-2.internal:8080" \
  -d "weight=100"

# Update service to use upstream
curl -X PATCH http://localhost:8001/services/bff-service \
  -d "host=bff-cluster"
```

---

## 📞 Support

For issues or questions:

1. Check logs: `docker-compose -f kong-docker-compose.yml logs kong`
2. Review Kong documentation: https://docs.konghq.com/
3. Run troubleshooting tests: `./kong-setup.sh`
4. Contact the development team

---

**Last Updated**: December 12, 2025  
**Version**: 1.0  
**Status**: Production Ready
