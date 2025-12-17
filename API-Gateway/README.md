# 🔐 Sentinel API Gateway - Kong Configuration

This folder contains the Kong API Gateway configuration and setup scripts for the Sentinel project.

## Quick Start

```bash
# Start Kong standalone
cd API-Gateway
docker-compose up -d

# Configure routes (after Kong is healthy)
./configure-kong.sh
```

## Architecture

```
Frontend (Web/Mobile)
       ↓
   Kong Gateway (Port 8000)
       ↓
   ┌───────┴───────┐
   ↓               ↓
BFF (8080)    Other Services
   ↓
Microservices
```

## Files

| File | Description |
|------|-------------|
| `kong.yml` | Declarative Kong configuration |
| `docker-compose.yml` | Kong standalone docker-compose |
| `configure-kong.sh` | Script to configure Kong routes |

## Services & Routes

| Service | Port | Route |
|---------|------|-------|
| BFF | 8080 | `/api/bff/*` |
| Auth | 8081 | `/api/auth/*` |
| Tenant | 8082 | `/api/tenants/*` |
| Project | 8083 | `/api/projects/*` |
| Billing | 8084 | `/api/billing/*` |
| Orchestrator | 8086 | `/api/scans/*` |
| Results | 8087 | `/api/results/*` |
| User Mgmt | 8088 | `/api/users/*` |
| Security Gate | 5001 | `/api/security-gate/*` |
| Code Quality | 5002 | `/api/code-quality/*` |
| Vulnerability | 5003 | `/api/vulnerabilities/*` |

## Access Points

- **Kong Proxy**: http://localhost:8000
- **Kong Admin**: http://localhost:8001
- **Kong Manager**: http://localhost:8002
- **Konga UI**: http://localhost:1337

## Testing

```bash
# Health check
curl http://localhost:8001/status

# Test BFF via Kong
curl http://localhost:8000/api/bff/dashboard \
  -H "Authorization: Bearer <token>"
```

## Plugins Enabled

- **CORS**: Cross-origin requests
- **Rate Limiting**: 1000 req/min
- **Request ID**: Tracking header
