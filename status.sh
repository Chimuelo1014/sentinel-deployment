#!/bin/bash

# 📊 Script para verificar estado de todos los servicios y realizar diagnostics

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  📊 SENTINEL STATUS CHECK${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""

# ============================================================================
# ESTADO DE CONTENEDORES
# ============================================================================
echo -e "${YELLOW}🐳 ESTADO DE CONTENEDORES:${NC}"
echo ""
docker-compose ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "sentinel|NAMES"
echo ""

# ============================================================================
# VERIFICAR CONECTIVIDAD
# ============================================================================
echo -e "${YELLOW}🔗 VERIFICANDO CONECTIVIDAD:${NC}"
echo ""

check_service() {
    local name=$1
    local port=$2
    local endpoint=$3
    
    if [ -z "$endpoint" ]; then
        endpoint="/"
    fi
    
    if curl -s -m 2 "http://localhost:$port$endpoint" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅${NC} $name (http://localhost:$port$endpoint)"
    else
        echo -e "  ${RED}❌${NC} $name (http://localhost:$port$endpoint)"
    fi
}

check_service "Kong Proxy" 8000
check_service "Kong Admin" 8001
check_service "Konga" 1337
check_service "BFF Service" 8080 "/swagger-ui.html"
check_service "Auth Service" 8081
check_service "SecurityGate" 5000
check_service "CodeQuality" 5001
check_service "Vulnerability" 5002
check_service "RabbitMQ Management" 15672
check_service "MailHog" 8025

echo ""

# ============================================================================
# VERIFICAR BASES DE DATOS
# ============================================================================
echo -e "${YELLOW}💾 ESTADO DE BASES DE DATOS:${NC}"
echo ""

# PostgreSQL
if docker-compose exec -T postgres pg_isready -U sentinel &>/dev/null; then
    echo -e "  ${GREEN}✅${NC} PostgreSQL (localhost:5432)"
else
    echo -e "  ${RED}❌${NC} PostgreSQL (localhost:5432)"
fi

# MongoDB
if docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" &>/dev/null 2>&1; then
    echo -e "  ${GREEN}✅${NC} MongoDB (localhost:27017)"
else
    echo -e "  ${RED}❌${NC} MongoDB (localhost:27017)"
fi

# RabbitMQ
if docker-compose exec -T rabbitmq rabbitmq-diagnostics ping &>/dev/null 2>&1; then
    echo -e "  ${GREEN}✅${NC} RabbitMQ (localhost:5672)"
else
    echo -e "  ${RED}❌${NC} RabbitMQ (localhost:5672)"
fi

echo ""

# ============================================================================
# INFORMACIÓN DE RABBITMQ
# ============================================================================
echo -e "${YELLOW}📨 RABBITMQ EXCHANGES & QUEUES:${NC}"
echo ""

if docker-compose exec -T rabbitmq rabbitmqctl list_exchanges 2>/dev/null > /tmp/exchanges.txt; then
    echo "  Exchanges configurados:"
    grep "sentinel" /tmp/exchanges.txt | awk '{print "    • " $1}' || echo "    (Ninguno)"
fi

echo ""

if docker-compose exec -T rabbitmq rabbitmqctl list_queues 2>/dev/null > /tmp/queues.txt; then
    echo "  Queues configuradas:"
    grep "scan" /tmp/queues.txt | awk '{print "    • " $1 " (mensajes: " $2 ")"}' || echo "    (Ninguno)"
fi

echo ""

# ============================================================================
# ÚLTIMOS ERRORES EN LOGS
# ============================================================================
echo -e "${YELLOW}🔍 ÚLTIMOS ERRORES EN LOGS:${NC}"
echo ""

SERVICES=("backend-for-frontend-service" "security-gate-service" "code-quality-service" "auth-service")

for service in "${SERVICES[@]}"; do
    ERRORS=$(docker-compose logs "$service" 2>/dev/null | grep -i "error\|exception" | tail -3 | wc -l)
    if [ "$ERRORS" -gt 0 ]; then
        echo -e "  ${YELLOW}⚠️  $service:${NC}"
        docker-compose logs "$service" 2>/dev/null | grep -i "error\|exception" | tail -1 | sed 's/^/     /'
    fi
done

echo ""

# ============================================================================
# RESUMEN
# ============================================================================
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✨ Diagnóstico completado${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo "Para ver logs en tiempo real, ejecuta:"
echo "  $ docker-compose logs -f [SERVICE_NAME]"
echo ""
echo "Para parar todos los servicios:"
echo "  $ docker-compose down"
echo ""
