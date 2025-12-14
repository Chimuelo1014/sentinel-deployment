#!/bin/bash

# Sentinel Project Docker Startup Script
# This script starts all services in the correct order

set -e

echo "🚀 Starting Sentinel Services..."
echo "================================"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Build and start all services
echo "📦 Building and starting all services..."
docker-compose up -d

echo ""
echo "✅ All services started successfully!"
echo ""
echo "📋 Service URLs:"
echo "   - API Gateway (Kong):        http://localhost:8000"
echo "   - Kong Admin API:             http://localhost:8001"
echo "   - Kong Manager UI:            http://localhost:1337"
echo "   - Auth Service:               http://localhost:8081"
echo "   - Backend for Frontend:       http://localhost:8080"
echo "   - Tenant Service:             http://localhost:8082"
echo "   - Project Service:            http://localhost:8083"
echo "   - Scanner Orchestrator:       http://localhost:8086"
echo "   - Results Aggregator:         http://localhost:8087"
echo "   - User Management:            http://localhost:8088"
echo "   - Security Gate Service:      http://localhost:5001"
echo "   - Code Quality Service:       http://localhost:5002"
echo "   - Vulnerability Service:      http://localhost:5003"
echo ""
echo "📊 Infrastructure:"
echo "   - PostgreSQL:                 localhost:5432 (user: sentinel, pass: sentinel123)"
echo "   - MongoDB:                    localhost:27017 (user: sentinel, pass: sentinel123)"
echo "   - RabbitMQ:                   localhost:5672 (user: sentinel, pass: sentinel123)"
echo "   - RabbitMQ Management UI:     http://localhost:15672"
echo ""
echo "💡 Useful commands:"
echo "   - View logs:                  docker-compose logs -f [service-name]"
echo "   - Stop services:              docker-compose down"
echo "   - Remove volumes:             docker-compose down -v"
echo "   - Restart a service:          docker-compose restart [service-name]"
