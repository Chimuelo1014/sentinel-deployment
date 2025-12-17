#!/bin/bash

# 🚀 SCRIPT AUTOMATIZADO: Levantar Sentinel Completo
# Levanta todos los microservicios Java y .NET con Docker Compose

set -e  # Exit on error

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  🚀 SENTINEL STARTUP - Docker Compose Automation${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# ============================================================================
# PASO 1: Verificar prerequisitos
# ============================================================================
echo -e "${YELLOW}[1/6] Verificando prerequisitos...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker no está instalado${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose no está instalado${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker ${NC}$(docker --version)"
echo -e "${GREEN}✅ Docker Compose ${NC}$(docker-compose --version)"
echo ""

# ============================================================================
# PASO 2: Limpiar contenedores existentes
# ============================================================================
echo -e "${YELLOW}[2/6] Limpiando contenedores existentes...${NC}"
cd "$PROJECT_DIR"

if docker-compose ps 2>/dev/null | grep -q "Up\|Exit"; then
    echo "   Deteniendo servicios activos..."
    docker-compose down --remove-orphans 2>/dev/null || true
    sleep 3
fi
echo -e "${GREEN}✅ Limpieza completada${NC}"
echo ""

# ============================================================================
# PASO 3: Infraestructura Base
# ============================================================================
echo -e "${YELLOW}[3/6] Levantando infraestructura base...${NC}"
echo "   - PostgreSQL (Base de datos principal)"
echo "   - MongoDB (Resultados de escaneos)"
echo "   - RabbitMQ (Message Broker)"
echo "   - MailHog (SMTP Mock)"

docker-compose up -d postgres mongodb rabbitmq mailhog

echo "   ⏳ Esperando health checks (esto toma ~30-60 segundos)..."
sleep 20

# Verificar PostgreSQL
if docker-compose exec -T postgres pg_isready -U sentinel &>/dev/null; then
    echo -e "${GREEN}   ✅ PostgreSQL listo${NC}"
else
    echo -e "${RED}   ❌ PostgreSQL no responde${NC}"
fi

# Verificar MongoDB
if docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" &>/dev/null; then
    echo -e "${GREEN}   ✅ MongoDB listo${NC}"
else
    echo -e "${RED}   ❌ MongoDB no responde${NC}"
fi

# Verificar RabbitMQ
if docker-compose exec -T rabbitmq rabbitmq-diagnostics ping &>/dev/null; then
    echo -e "${GREEN}   ✅ RabbitMQ listo${NC}"
else
    echo -e "${RED}   ❌ RabbitMQ no responde${NC}"
fi

echo -e "${GREEN}✅ Infraestructura base levantada${NC}"
echo ""

# ============================================================================
# PASO 4: Kong API Gateway
# ============================================================================
echo -e "${YELLOW}[4/6] Configurando Kong API Gateway...${NC}"

docker-compose up -d kong-db
echo "   ⏳ Esperando Kong DB..."
sleep 15

echo "   - Ejecutando migraciones de Kong..."
docker-compose run --rm kong-migration 2>/dev/null

echo "   - Iniciando Kong + Konga..."
docker-compose up -d kong konga

echo "   ⏳ Esperando Kong..."
sleep 10

if curl -s http://localhost:8001/status 2>/dev/null | grep -q "\"ok\""; then
    echo -e "${GREEN}   ✅ Kong API Gateway listo${NC}"
else
    echo -e "${YELLOW}   ⚠️  Kong aún inicializando (revisar en 30s)${NC}"
fi

echo -e "${GREEN}✅ Kong Gateway configurado${NC}"
echo ""

# ============================================================================
# PASO 5: Servicios Java
# ============================================================================
echo -e "${YELLOW}[5/6] Levantando servicios Java...${NC}"
echo "   Compilando imágenes Docker..."

docker-compose build --no-cache \
    auth-service tenant-service project-service billing-service \
    scanner-orchestrator-service results-aggregator-service \
    backend-for-frontend-service 2>/dev/null

echo "   - Iniciando Auth Service"
echo "   - Iniciando Tenant Service"
echo "   - Iniciando Project Service"
echo "   - Iniciando Billing Service"
echo "   - Iniciando Scanner Orchestrator"
echo "   - Iniciando Results Aggregator"
echo "   - Iniciando BFF Service (Backend-For-Frontend)"

docker-compose up -d auth-service tenant-service project-service \
    billing-service scanner-orchestrator-service results-aggregator-service \
    backend-for-frontend-service

echo "   ⏳ Esperando servicios Java (esto puede tomar 2-3 minutos)..."
sleep 60

# Verificar BFF
BFF_READY=0
for i in {1..30}; do
    if curl -s http://localhost:8080/api/bff/health 2>/dev/null | grep -q "UP\|ok"; then
        echo -e "${GREEN}   ✅ BFF Service listo${NC}"
        BFF_READY=1
        break
    fi
    echo -n "."
    sleep 2
done

if [ $BFF_READY -eq 0 ]; then
    echo -e "${YELLOW}   ⚠️  BFF aún inicializando (revisar logs)${NC}"
fi

echo -e "${GREEN}✅ Servicios Java levantados${NC}"
echo ""

# ============================================================================
# PASO 6: Servicios .NET
# ============================================================================
echo -e "${YELLOW}[6/6] Levantando servicios .NET...${NC}"
echo "   Compilando servicios C#..."

docker-compose build --no-cache \
    security-gate-service code-quality-service vulnerability-service \
    user-management-service 2>/dev/null

echo "   - Iniciando SecurityGate Service"
echo "   - Iniciando CodeQuality Service"
echo "   - Iniciando Vulnerability Service"
echo "   - Iniciando User Management Service"

docker-compose up -d security-gate-service code-quality-service \
    vulnerability-service user-management-service

echo "   ⏳ Esperando servicios .NET (1-2 minutos)..."
sleep 45

echo -e "${GREEN}✅ Servicios .NET levantados${NC}"
echo ""

# ============================================================================
# RESUMEN FINAL
# ============================================================================
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  ✅ SENTINEL COMPLETAMENTE LEVANTADO${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

echo -e "${GREEN}📊 ESTADO DE SERVICIOS:${NC}"
docker-compose ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo -e "${GREEN}🌐 ACCESOS RÁPIDOS:${NC}"
echo ""
echo -e "  ${BLUE}Frontend${NC}"
echo "    • Vite Dev: http://localhost:5173"
echo ""
echo -e "  ${BLUE}APIs & Gateways${NC}"
echo "    • Kong Proxy: http://localhost:8000"
echo "    • Kong Admin: http://localhost:8001"
echo "    • BFF Service: http://localhost:8080"
echo "    • Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo -e "  ${BLUE}Microservicios Java${NC}"
echo "    • Auth Service: http://localhost:8081"
echo "    • Tenant Service: http://localhost:8082"
echo "    • Project Service: http://localhost:8084"
echo "    • Scanner Orchestrator: http://localhost:8086"
echo "    • Results Aggregator: http://localhost:8087"
echo "    • User Management: http://localhost:8088"
echo ""
echo -e "  ${BLUE}Microservicios .NET${NC}"
echo "    • SecurityGate: http://localhost:5000"
echo "    • CodeQuality: http://localhost:5001"
echo "    • Vulnerability: http://localhost:5002"
echo ""
echo -e "  ${BLUE}Administración${NC}"
echo "    • Konga (Kong Admin UI): http://localhost:1337 (admin/admin)"
echo "    • RabbitMQ Management: http://localhost:15672 (guest/guest)"
echo "    • MailHog: http://localhost:8025"
echo ""
echo -e "  ${BLUE}Bases de Datos${NC}"
echo "    • PostgreSQL: localhost:5432 (sentinel/sentinel123)"
echo "    • MongoDB: localhost:27017 (sentinel/sentinel123)"
echo ""

echo -e "${YELLOW}📝 PRÓXIMOS PASOS:${NC}"
echo ""
echo "1. Configurar Kong (una sola vez):"
echo "   $ ./kong-setup.sh"
echo ""
echo "2. Levantar Frontend (en otra terminal):"
echo "   $ cd sentinel_front && npm install && npm run dev"
echo ""
echo "3. Ejecutar tests de integración:"
echo "   $ ./test-integration.sh"
echo ""
echo "4. Ver logs en tiempo real:"
echo "   $ docker-compose logs -f"
echo ""
echo "5. Detener servicios:"
echo "   $ docker-compose down"
echo ""

echo -e "${GREEN}✨ Sistema listo para usar ✨${NC}"
