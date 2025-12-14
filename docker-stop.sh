#!/bin/bash

# Sentinel Project Docker Stop Script
# This script stops all running services and optionally removes volumes

set -e

REMOVE_VOLUMES=false

if [ "$1" == "-v" ] || [ "$1" == "--volumes" ]; then
    REMOVE_VOLUMES=true
fi

echo "🛑 Stopping Sentinel Services..."
echo "================================"

if [ "$REMOVE_VOLUMES" = true ]; then
    echo "⚠️  Removing volumes (database data will be deleted)..."
    docker-compose down -v
    echo "✅ All services stopped and volumes removed."
else
    docker-compose down
    echo "✅ All services stopped."
fi

echo ""
echo "💡 To start services again, run: ./docker-start.sh"
echo "💡 To remove volumes on next stop, run: ./docker-stop.sh -v"
