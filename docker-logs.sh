#!/bin/bash

# Sentinel Project Docker Logs Script
# This script displays logs from services

set -e

if [ -z "$1" ]; then
    echo "📋 Sentinel Services Logs"
    echo "=========================="
    echo ""
    echo "Usage: ./docker-logs.sh [service-name] [options]"
    echo ""
    echo "Available services:"
    echo "  - auth-service                 (Java - Port 8081)"
    echo "  - tenant-service               (Java - Port 8082)"
    echo "  - project-service              (Java - Port 8083)"
    echo "  - scaner-orchestrator-service  (Java - Port 8086)"
    echo "  - results-aggregator-service   (Java - Port 8087)"
    echo "  - user-management-service      (Java - Port 8088)"
    echo "  - backend-for-frontend-service (Java - Port 8080)"
    echo "  - security-gate-service        (.NET - Port 5001)"
    echo "  - code-quality-service         (.NET - Port 5002)"
    echo "  - vulnerability-service        (.NET - Port 5003)"
    echo "  - postgres                     (Database)"
    echo "  - mongodb                      (Document Database)"
    echo "  - rabbitmq                     (Message Broker)"
    echo "  - kong                         (API Gateway)"
    echo "  - konga                        (Kong Manager UI)"
    echo ""
    echo "Options:"
    echo "  -f, --follow                   Follow log output"
    echo "  -n <number>                    Number of lines to show"
    echo ""
    echo "Examples:"
    echo "  ./docker-logs.sh auth-service"
    echo "  ./docker-logs.sh auth-service -f"
    echo "  ./docker-logs.sh postgres -n 50"
else
    docker-compose logs "$@"
fi
