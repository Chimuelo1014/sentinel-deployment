#!/bin/bash

# Kong Routes Setup Script for Sentinel
# Run this script after docker-compose up

set -e

KONG_ADMIN_URL="http://localhost:8001"

echo "⏳ Waiting for Kong to be ready..."
max_attempts=30
attempt=0
while ! curl -s "${KONG_ADMIN_URL}/status" > /dev/null 2>&1; do
    attempt=$((attempt+1))
    if [ $attempt -ge $max_attempts ]; then
        echo "❌ Kong failed to start after ${max_attempts} attempts"
        exit 1
    fi
    echo "   Attempt $attempt/$max_attempts..."
    sleep 2
done
echo "✅ Kong is ready!"

# Get service IPs
BFF_IP=$(docker inspect sentinel-bff-service --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')
AUTH_IP=$(docker inspect sentinel-auth-service --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}')

echo "📡 BFF Service IP: $BFF_IP"
echo "📡 Auth Service IP: $AUTH_IP"

# ============================================
# CREATE SERVICES
# ============================================

echo "📋 Creating services..."

# BFF Service
curl -s -X POST "${KONG_ADMIN_URL}/services" \
    -d "name=bff-service" \
    -d "url=http://${BFF_IP}:8080" > /dev/null 2>&1 || \
curl -s -X PATCH "${KONG_ADMIN_URL}/services/bff-service" \
    -d "url=http://${BFF_IP}:8080" > /dev/null 2>&1

# Auth Service (direct access for OAuth2)
curl -s -X POST "${KONG_ADMIN_URL}/services" \
    -d "name=auth-service" \
    -d "url=http://${AUTH_IP}:8081" > /dev/null 2>&1 || \
curl -s -X PATCH "${KONG_ADMIN_URL}/services/auth-service" \
    -d "url=http://${AUTH_IP}:8081" > /dev/null 2>&1

echo "   ✅ Services created"

# ============================================
# ENABLE CORS
# ============================================

echo "🔗 Enabling CORS..."
curl -s -X POST "${KONG_ADMIN_URL}/services/bff-service/plugins" \
    -d "name=cors" \
    -d "config.origins=*" \
    -d "config.credentials=true" > /dev/null 2>&1 || echo "   (BFF CORS already enabled)"

curl -s -X POST "${KONG_ADMIN_URL}/services/auth-service/plugins" \
    -d "name=cors" \
    -d "config.origins=*" \
    -d "config.credentials=true" > /dev/null 2>&1 || echo "   (Auth CORS already enabled)"

echo "   ✅ CORS enabled"

# ============================================
# CREATE ROUTES
# ============================================

echo "🛣️  Creating routes..."

# Auth routes -> auth-service (para OAuth2, login, etc)
curl -s -X POST "${KONG_ADMIN_URL}/services/auth-service/routes" \
    -d "name=auth-routes" \
    -d "paths[]=/api/auth" \
    -d "strip_path=false" > /dev/null 2>&1 || echo "   (auth-routes may exist)"
echo "   ✅ auth-routes -> auth-service (OAuth2 direct)"

# BFF Routes
BFF_ROUTES=(
    "tenants-routes:/api/tenants"
    "projects-routes:/api/projects"
    "plans-routes:/api/plans"
    "subscriptions-routes:/api/subscriptions"
    "payments-routes:/api/payments-history"
    "dashboard-routes:/api/dashboard"
    "billing-routes:/api/billing"
    "bff-routes:/api/bff"
)

for route in "${BFF_ROUTES[@]}"; do
    name="${route%%:*}"
    path="${route##*:}"
    curl -s -X POST "${KONG_ADMIN_URL}/services/bff-service/routes" \
        -d "name=${name}" \
        -d "paths[]=${path}" \
        -d "strip_path=false" > /dev/null 2>&1 || echo "   (${name} may exist)"
    echo "   ✅ ${name} -> bff-service"
done

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ KONG CONFIGURATION COMPLETE"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "🧪 Test the API:"
echo "   curl http://localhost:8000/api/plans"
echo "   curl http://localhost:8000/api/auth/health"
echo ""
