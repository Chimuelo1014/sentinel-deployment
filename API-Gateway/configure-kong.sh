#!/bin/bash

# =============================================================================
# Kong Configuration Script for Sentinel Project
# =============================================================================
# This script configures Kong API Gateway with all Sentinel microservices
# Run this after Kong is healthy and all services are running
# =============================================================================

set -e

KONG_ADMIN_URL="${KONG_ADMIN_URL:-http://localhost:8001}"

echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║        SENTINEL - Kong API Gateway Configuration                      ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""

# -----------------------------------------------------------------------------
# Wait for Kong to be ready
# -----------------------------------------------------------------------------
wait_for_kong() {
    echo "⏳ Waiting for Kong to be ready..."
    local max_attempts=30
    local attempt=0
    
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
    echo ""
}

# -----------------------------------------------------------------------------
# Create or Update Service
# -----------------------------------------------------------------------------
create_service() {
    local name=$1
    local url=$2
    local tags=$3

    echo "📋 Service: $name -> $url"
    
    # Check if exists
    if curl -s "${KONG_ADMIN_URL}/services/${name}" | grep -q "name"; then
        curl -s -X PATCH "${KONG_ADMIN_URL}/services/${name}" \
            -H "Content-Type: application/json" \
            -d "{\"url\": \"${url}\"}" > /dev/null
        echo "   ↻ Updated"
    else
        curl -s -X POST "${KONG_ADMIN_URL}/services" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"${name}\",
                \"url\": \"${url}\",
                \"tags\": [\"sentinel\", \"${tags}\"]
            }" > /dev/null
        echo "   ✓ Created"
    fi
}

# -----------------------------------------------------------------------------
# Create Route
# -----------------------------------------------------------------------------
create_route() {
    local service=$1
    local name=$2
    local path=$3

    echo "   🛣️  Route: $name -> $path"
    
    # Delete existing route if any
    curl -s -X DELETE "${KONG_ADMIN_URL}/routes/${name}" > /dev/null 2>&1 || true
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service}/routes" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"${name}\",
            \"paths\": [\"${path}\"],
            \"strip_path\": false,
            \"protocols\": [\"http\", \"https\"]
        }" > /dev/null 2>&1 || echo "      (may already exist)"
}

# -----------------------------------------------------------------------------
# Enable Plugin
# -----------------------------------------------------------------------------
enable_plugin() {
    local service=$1
    local plugin=$2
    local config=$3

    echo "   🔌 Plugin: $plugin"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service}/plugins" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"${plugin}\",
            \"config\": ${config}
        }" > /dev/null 2>&1 || echo "      (may already exist)"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

wait_for_kong

echo "═══════════════════════════════════════════════════════════════════════"
echo "📦 CREATING SERVICES AND ROUTES"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""

# BFF Service (Main frontend entry point)
create_service "bff-service" "http://backend-for-frontend-service:8080" "bff"
create_route "bff-service" "bff-dashboard" "/api/bff/dashboard"
create_route "bff-service" "bff-scans" "/api/bff/scans"
create_route "bff-service" "bff-projects" "/api/bff/projects"
create_route "bff-service" "bff-analytics" "/api/bff/analytics"
create_route "bff-service" "bff-users" "/api/bff/users"
create_route "bff-service" "bff-notifications" "/api/bff/notifications"
echo ""

# Auth Service
create_service "auth-service" "http://auth-service:8081" "auth"
create_route "auth-service" "auth-routes" "/api/auth"
echo ""

# Tenant Service
create_service "tenant-service" "http://tenant-service:8082" "tenant"
create_route "tenant-service" "tenant-routes" "/api/tenants"
echo ""

# Project Service
create_service "project-service" "http://project-service:8083" "project"
create_route "project-service" "project-routes" "/api/projects"
echo ""

# Billing Service
create_service "billing-service" "http://billing-service:8084" "billing"
create_route "billing-service" "billing-routes" "/api/billing"
echo ""

# Orchestrator Service
create_service "orchestrator-service" "http://scaner-orchestrator-service:8086" "orchestrator"
create_route "orchestrator-service" "orchestrator-scans" "/api/scans"
create_route "orchestrator-service" "orchestrator-routes" "/api/orchestrator"
echo ""

# Results Aggregator Service
create_service "results-service" "http://results-aggregator-service:8087" "results"
create_route "results-service" "results-routes" "/api/results"
echo ""

# User Management Service
create_service "user-service" "http://user-management-service:8088" "users"
create_route "user-service" "user-routes" "/api/users"
echo ""

# Security Gate Service (C#)
create_service "security-gate-service" "http://security-gate-service:5001" "security"
create_route "security-gate-service" "security-gate-routes" "/api/security-gate"
echo ""

# Code Quality Service (C#)
create_service "code-quality-service" "http://code-quality-service:5001" "quality"
create_route "code-quality-service" "code-quality-routes" "/api/code-quality"
echo ""

# Vulnerability Service (C#)
create_service "vulnerability-service" "http://vulnerability-service:5001" "vulnerability"
create_route "vulnerability-service" "vulnerability-routes" "/api/vulnerabilities"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "🔌 CONFIGURING GLOBAL PLUGINS"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""

# Global CORS Plugin
echo "🔗 Enabling global CORS..."
curl -s -X POST "${KONG_ADMIN_URL}/plugins" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "cors",
        "config": {
            "origins": ["*"],
            "methods": ["GET", "HEAD", "PUT", "PATCH", "POST", "DELETE", "OPTIONS"],
            "headers": ["Accept", "Content-Type", "Authorization", "X-Tenant-Id", "X-Request-ID"],
            "exposed_headers": ["X-Auth-Token", "X-Total-Count"],
            "credentials": true,
            "max_age": 3600
        }
    }' > /dev/null 2>&1 || echo "   (may already exist)"

# Global Rate Limiting
echo "⏱️  Enabling global rate limiting (1000 req/min)..."
curl -s -X POST "${KONG_ADMIN_URL}/plugins" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "rate-limiting",
        "config": {
            "minute": 1000,
            "policy": "local",
            "fault_tolerant": true
        }
    }' > /dev/null 2>&1 || echo "   (may already exist)"

echo ""
echo "═══════════════════════════════════════════════════════════════════════"
echo "✅ KONG CONFIGURATION COMPLETE"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
echo "📊 Services configured:"
curl -s "${KONG_ADMIN_URL}/services" | grep -o '"name":"[^"]*"' | cut -d'"' -f4 | while read name; do
    echo "   • $name"
done
echo ""
echo "🔗 Access Points:"
echo "   • Kong Proxy:    http://localhost:8000"
echo "   • Kong Admin:    http://localhost:8001"
echo "   • Kong Manager:  http://localhost:8002"
echo "   • Konga UI:      http://localhost:1337"
echo ""
echo "🧪 Test Commands:"
echo "   curl http://localhost:8000/api/bff/dashboard -H 'Authorization: Bearer <token>'"
echo "   curl http://localhost:8000/api/auth/health"
echo ""
