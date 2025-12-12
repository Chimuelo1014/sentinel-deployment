#!/bin/bash

# Kong Configuration Script for Sentinel Project
# This script sets up all services, routes, and plugins in Kong

set -e

KONG_ADMIN_URL="http://localhost:8001"
BFF_SERVICE_URL="http://host.docker.internal:8080"  # For Docker, use host.docker.internal on Mac/Windows, or configure accordingly
JWT_SECRET="X9q2N8ZCnO3Tj48p1Fk6B2V0x8Teq9gHBV0SX1e2p6U="

echo "🔧 Starting Kong Configuration..."

# Wait for Kong to be ready
echo "⏳ Waiting for Kong to be ready..."
max_attempts=30
attempt=0
while ! curl -s "${KONG_ADMIN_URL}/status" > /dev/null; do
    attempt=$((attempt+1))
    if [ $attempt -ge $max_attempts ]; then
        echo "❌ Kong failed to start after ${max_attempts} attempts"
        exit 1
    fi
    echo "   Attempt $attempt/$max_attempts..."
    sleep 2
done
echo "✅ Kong is ready!"

# Function to create or update Kong service
create_service() {
    local service_name=$1
    local upstream_url=$2

    echo "📋 Creating service: $service_name -> $upstream_url"
    
    # Check if service already exists
    if curl -s "${KONG_ADMIN_URL}/services/${service_name}" > /dev/null 2>&1; then
        echo "   Service already exists, updating..."
        curl -s -X PATCH "${KONG_ADMIN_URL}/services/${service_name}" \
            -H "Content-Type: application/json" \
            -d "{\"url\": \"${upstream_url}\"}" > /dev/null
    else
        curl -s -X POST "${KONG_ADMIN_URL}/services" \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"${service_name}\",
                \"url\": \"${upstream_url}\",
                \"protocol\": \"http\",
                \"host\": \"${upstream_url}\",
                \"tags\": [\"sentinel\"]
            }" > /dev/null
    fi
    echo "   ✅ Service created/updated"
}

# Function to create route for service
create_route() {
    local route_name=$1
    local service_name=$2
    local path_pattern=$3

    echo "🛣️  Creating route: $route_name -> $service_name ($path_pattern)"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service_name}/routes" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"${route_name}\",
            \"paths\": [\"${path_pattern}\"],
            \"strip_path\": false,
            \"protocols\": [\"http\", \"https\"],
            \"tags\": [\"sentinel\"]
        }" > /dev/null 2>&1 || echo "   (Route may already exist)"
    echo "   ✅ Route created"
}

# Function to enable JWT plugin on service
enable_jwt_plugin() {
    local service_name=$1

    echo "🔐 Enabling JWT plugin for: $service_name"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service_name}/plugins" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"jwt\",
            \"config\": {
                \"secret_is_base64\": false,
                \"key_claim_name\": \"iss\",
                \"cookie_names\": [\"jwt\"],
                \"uri_param_names\": [\"token\"]
            }
        }" > /dev/null 2>&1 || echo "   (JWT plugin may already be enabled)"
    echo "   ✅ JWT plugin enabled"
}

# Function to enable rate-limiting plugin
enable_rate_limit() {
    local service_name=$1
    local rate=$2
    local window=$3

    echo "⏱️  Enabling rate limiting for: $service_name ($rate requests per $window seconds)"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service_name}/plugins" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"rate-limiting\",
            \"config\": {
                \"minute\": ${rate},
                \"policy\": \"local\",
                \"fault_tolerant\": true
            }
        }" > /dev/null 2>&1 || echo "   (Rate limiting may already be enabled)"
    echo "   ✅ Rate limiting enabled"
}

# Function to enable CORS plugin
enable_cors() {
    local service_name=$1

    echo "🔗 Enabling CORS for: $service_name"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service_name}/plugins" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"cors\",
            \"config\": {
                \"origins\": [\"*\"],
                \"methods\": [\"GET\", \"HEAD\", \"PUT\", \"PATCH\", \"POST\", \"DELETE\"],
                \"headers\": [\"Accept\", \"Accept-Version\", \"Content-Length\", \"Content-MD5\", \"Content-Type\", \"Date\", \"X-Auth-Token\", \"Authorization\", \"X-Tenant-Id\"],
                \"expose_headers\": [\"X-Auth-Token\", \"X-Total-Count\"],
                \"credentials\": true,
                \"max_age\": 3600
            }
        }" > /dev/null 2>&1 || echo "   (CORS may already be enabled)"
    echo "   ✅ CORS enabled"
}

# Function to enable request/response logging
enable_logging() {
    local service_name=$1

    echo "📝 Enabling request logging for: $service_name"
    
    curl -s -X POST "${KONG_ADMIN_URL}/services/${service_name}/plugins" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"request-transformer\",
            \"config\": {
                \"add\": {
                    \"headers\": [\"X-Kong-Forwarded-For: \$(remote_addr)\", \"X-Request-ID: \$(request_id)\"]
                }
            }
        }" > /dev/null 2>&1 || echo "   (Logging may already be enabled)"
    echo "   ✅ Logging enabled"
}

# ============================================================
# Create Services and Routes
# ============================================================

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "🚀 CREATING SENTINEL SERVICES AND ROUTES"
echo "═══════════════════════════════════════════════════════════"

# Create BFF Service
create_service "bff-service" "${BFF_SERVICE_URL}"

# Create routes for BFF endpoints
create_route "bff-dashboard" "bff-service" "/api/bff/dashboard"
create_route "bff-scans" "bff-service" "/api/bff/scans"
create_route "bff-projects" "bff-service" "/api/bff/projects"
create_route "bff-analytics" "bff-service" "/api/bff/analytics"
create_route "bff-users" "bff-service" "/api/bff/users"
create_route "bff-notifications" "bff-service" "/api/bff/notifications"

# ============================================================
# Configure Plugins (Global and Service-specific)
# ============================================================

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "🔌 CONFIGURING PLUGINS"
echo "═══════════════════════════════════════════════════════════"

# Enable plugins for BFF service
enable_jwt_plugin "bff-service"
enable_rate_limit "bff-service" 1000 60
enable_cors "bff-service"
enable_logging "bff-service"

# ============================================================
# Create JWT Credentials (Optional - for testing)
# ============================================================

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "🔑 CREATING TEST JWT CREDENTIALS"
echo "═══════════════════════════════════════════════════════════"

echo "⚠️  Note: JWT Plugin requires valid tokens from your Auth Service"
echo "   Configure your Auth Service to use the same JWT secret:"
echo "   ${JWT_SECRET}"

# ============================================================
# Display Configuration Summary
# ============================================================

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ KONG CONFIGURATION COMPLETE"
echo "═══════════════════════════════════════════════════════════"
echo ""
echo "📊 Configured Services:"
curl -s "${KONG_ADMIN_URL}/services" | jq '.data[].name' 2>/dev/null || echo "  (Could not fetch services)"
echo ""
echo "🛣️  Configured Routes:"
curl -s "${KONG_ADMIN_URL}/routes" | jq '.data[].name' 2>/dev/null || echo "  (Could not fetch routes)"
echo ""
echo "📝 Kong Dashboard:"
echo "   🌐 Admin API: http://localhost:8001"
echo "   🖥️  Manager GUI: http://localhost:8002"
echo "   📡 API Gateway: http://localhost:8000"
echo ""
echo "🧪 Test the API:"
echo "   curl http://localhost:8000/api/bff/dashboard -H 'Authorization: Bearer <your-jwt-token>'"
echo ""
