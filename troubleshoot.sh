#!/bin/bash

# 🔧 Script de Troubleshooting y Reparación

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

show_menu() {
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  🔧 SENTINEL TROUBLESHOOTING MENU${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""
    echo "1. Ver status de todos los servicios"
    echo "2. Ver logs en tiempo real"
    echo "3. Reiniciar servicio específico"
    echo "4. Limpiar y rebuildar imágenes"
    echo "5. Resetear bases de datos (DESTRUCTIVO)"
    echo "6. Verificar puerto disponible"
    echo "7. Diagnosticar RabbitMQ"
    echo "8. Diagnosticar Kong"
    echo "9. Ver estadísticas de Docker"
    echo "10. Salir"
    echo ""
}

check_port() {
    local port=$1
    if lsof -i ":$port" &>/dev/null; then
        echo -e "${RED}❌ Puerto $port ESTÁ EN USO${NC}"
        lsof -i ":$port" | tail -1 | awk '{print "   Proceso: " $1 " (PID: " $2 ")"}'
    else
        echo -e "${GREEN}✅ Puerto $port disponible${NC}"
    fi
}

while true; do
    show_menu
    read -p "Selecciona una opción (1-10): " option
    
    case $option in
        1)
            echo ""
            echo -e "${YELLOW}Verificando estado de servicios...${NC}"
            ./status.sh
            ;;
        2)
            echo ""
            echo -e "${YELLOW}Servicios disponibles:${NC}"
            docker-compose config --services | nl
            echo ""
            read -p "Selecciona el nombre del servicio: " service_name
            if [ ! -z "$service_name" ]; then
                docker-compose logs -f "$service_name"
            fi
            ;;
        3)
            echo ""
            echo -e "${YELLOW}Servicios disponibles:${NC}"
            docker-compose config --services | nl
            echo ""
            read -p "Selecciona el nombre del servicio: " service_name
            if [ ! -z "$service_name" ]; then
                echo -e "${YELLOW}Reiniciando $service_name...${NC}"
                docker-compose restart "$service_name"
                echo -e "${GREEN}✅ Hecho${NC}"
            fi
            ;;
        4)
            echo ""
            read -p "¿Estás seguro? Esto elimará todas las imágenes y las reconstruirá. (s/n): " confirm
            if [[ "$confirm" == "s" || "$confirm" == "S" ]]; then
                echo -e "${YELLOW}Limpiando y reconstruyendo...${NC}"
                docker-compose down --remove-orphans
                docker-compose build --no-cache
                echo -e "${GREEN}✅ Hecho${NC}"
            fi
            ;;
        5)
            echo ""
            read -p "¿CONFIRMAR RESET DESTRUCTIVO? (escribir 'SI' para confirmar): " confirm
            if [[ "$confirm" == "SI" ]]; then
                echo -e "${RED}🔴 Eliminando volúmenes...${NC}"
                docker-compose down -v
                echo -e "${GREEN}✅ Volúmenes eliminados. Ejecuta ./startup.sh para reiniciar${NC}"
            else
                echo "Cancelado"
            fi
            ;;
        6)
            echo ""
            echo -e "${YELLOW}Verificando puertos comunes:${NC}"
            for port in 5000 5001 5002 5173 5432 5433 5672 8000 8001 8025 8080 8081 8082 8084 8086 8087 8088 15672 27017 1337; do
                check_port $port
            done
            ;;
        7)
            echo ""
            echo -e "${YELLOW}Diagnosticando RabbitMQ...${NC}"
            echo ""
            
            echo "1. Estado de RabbitMQ:"
            docker-compose exec -T rabbitmq rabbitmq-diagnostics status 2>/dev/null | grep "Status" || echo "   (No disponible)"
            
            echo ""
            echo "2. Exchanges:"
            docker-compose exec -T rabbitmq rabbitmqctl list_exchanges 2>/dev/null | grep "sentinel" || echo "   (Ninguno)"
            
            echo ""
            echo "3. Queues:"
            docker-compose exec -T rabbitmq rabbitmqctl list_queues 2>/dev/null | grep "scan\|security\|code\|vulnerability" || echo "   (Ninguno)"
            
            echo ""
            echo "4. Bindings:"
            docker-compose exec -T rabbitmq rabbitmqctl list_bindings 2>/dev/null | grep "scan" | head -10 || echo "   (Ninguno)"
            
            echo ""
            echo "Management UI: http://localhost:15672 (guest/guest)"
            ;;
        8)
            echo ""
            echo -e "${YELLOW}Diagnosticando Kong...${NC}"
            echo ""
            
            echo "1. Status de Kong:"
            curl -s http://localhost:8001/status 2>/dev/null | grep -q "ok" && echo "   ✅ Kong está funcionando" || echo "   ❌ Kong no responde"
            
            echo ""
            echo "2. Services registrados:"
            curl -s http://localhost:8001/services 2>/dev/null | grep '"name"' || echo "   (No disponible)"
            
            echo ""
            echo "3. Routes registradas:"
            curl -s http://localhost:8001/routes 2>/dev/null | grep '"name"' || echo "   (No disponible)"
            
            echo ""
            echo "Admin UIs:"
            echo "   • Kong Admin: http://localhost:8001"
            echo "   • Konga: http://localhost:1337 (admin/admin)"
            ;;
        9)
            echo ""
            echo -e "${YELLOW}Estadísticas de Docker:${NC}"
            docker stats --no-stream
            ;;
        10)
            echo -e "${GREEN}¡Hasta luego!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Opción inválida${NC}"
            ;;
    esac
    
    echo ""
    read -p "Presiona Enter para continuar..."
    clear
done
