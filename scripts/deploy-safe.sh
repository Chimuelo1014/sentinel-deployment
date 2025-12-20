#!/bin/bash
# Script de Despliegue SEGURO para Sentinel
# Preserva volúmenes críticos de datos

cd /home/samuel/sentinel-deployment

echo "==================================="
echo "🔍 PASO 1: Verificar volúmenes críticos"
echo "==================================="

# Lista de volúmenes a PRESERVAR
CRITICAL_VOLUMES=(
    "sentinel-deployment_n8n_data"
    "sentinel-deployment_postgres_data"
    "sentinel-deployment_mongodb_data"
    "sentinel-deployment_rabbitmq_data"
    "sentinel-deployment_kong_data"
)

echo "Volúmenes críticos que NO se eliminarán:"
for vol in "${CRITICAL_VOLUMES[@]}"; do
    if docker volume inspect "$vol" &>/dev/null; then
        echo "  ✅ $vol (existe)"
    else
        echo "  ⚠️  $vol (no existe, se creará)"
    fi
done
echo ""

echo "==================================="
echo "🛑 PASO 2: Parar contenedores"
echo "==================================="
docker-compose down 2>/dev/null || true
echo ""

echo "==================================="
echo "🧹 PASO 3: Limpiar (SIN eliminar volúmenes críticos)"
echo "==================================="

# Limpiar redes
docker network prune -f

# Limpiar SOLO volúmenes huérfanos (no los nombrados)
echo "Limpiando volúmenes huérfanos..."
docker volume ls -q -f dangling=true | xargs -r docker volume rm 2>/dev/null || true

# Limpiar imágenes sin usar
docker image prune -a -f

echo ""

echo "==================================="
echo "🔄 PASO 4: Actualizar código"
echo "==================================="
git fetch origin main
git reset --hard origin/main
git pull origin main
echo ""

echo "==================================="
echo "🔨 PASO 5: Build (~5-7 minutos)"
echo "==================================="
docker-compose -f docker-compose.yml --env-file .env.production build --no-cache
echo ""

echo "==================================="
echo "🚀 PASO 6: Iniciar servicios"
echo "==================================="
docker-compose -f docker-compose.yml --env-file .env.production up -d
echo ""

echo "==================================="
echo "✅ PASO 7: Verificar estado"
echo "==================================="
echo "Esperando 10 segundos para que los servicios inicien..."
sleep 10

echo ""
echo "Estado de contenedores:"
docker-compose ps

echo ""
echo "Volúmenes preservados:"
docker volume ls | grep sentinel-deployment

echo ""
echo "==================================="
echo "🎉 Despliegue completado"
echo "==================================="
echo "Accede a:"
echo "  - Frontend: https://sentinel.crudzaso.com"
echo "  - N8N: http://tu-vps-ip:5678"
echo "  - Kong Admin: http://tu-vps-ip:8001"
echo ""
