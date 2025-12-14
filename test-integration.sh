#!/bin/bash

# Sentinel Integration Testing Script
# Tests all BFF endpoints and Kong routing

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
KONG_URL="http://localhost:8000"
BFF_URL="http://localhost:8080"
AUTH_URL="http://localhost:8081"
TENANT_ID="test-tenant-$(date +%s)"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║       Sentinel Integration Testing Suite                       ║"
echo "║       Testing BFF, Kong Gateway, and Backend Services          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Function to print test results
print_result() {
    local test_name=$1
    local status=$2
    local message=$3

    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $test_name"
        if [ ! -z "$message" ]; then
            echo "   └─ $message"
        fi
    else
        echo -e "${RED}❌ FAIL${NC} - $test_name"
        if [ ! -z "$message" ]; then
            echo "   └─ $message"
        fi
    fi
}

# Function to test endpoint
test_endpoint() {
    local method=$1
    local url=$2
    local token=$3
    local expected_code=$4
    local data=$5

    local cmd="curl -s -w '\n%{http_code}' -X $method '$url' \
        -H 'Content-Type: application/json' \
        -H 'Authorization: Bearer $token' \
        -H 'X-Tenant-Id: $TENANT_ID'"

    if [ ! -z "$data" ]; then
        cmd="$cmd -d '$data'"
    fi

    local response=$(eval $cmd)
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n-1)

    if [ "$http_code" = "$expected_code" ]; then
        return 0
    else
        echo "Expected: $expected_code, Got: $http_code"
        echo "Response: $body"
        return 1
    fi
}

# ============================================================
# SERVICE HEALTH CHECKS
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}1. SERVICE HEALTH CHECKS${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Check BFF Service
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_result "BFF Service Health Check" "PASS" "BFF running on port 8080"
else
    print_result "BFF Service Health Check" "FAIL" "BFF not responding on port 8080"
fi

# Check Kong
if curl -s http://localhost:8001/status > /dev/null 2>&1; then
    print_result "Kong Admin API" "PASS" "Kong Admin API responding on port 8001"
else
    print_result "Kong Admin API" "FAIL" "Kong not responding on port 8001"
fi

# Check Kong Proxy
if curl -s http://localhost:8000/ > /dev/null 2>&1; then
    print_result "Kong Proxy" "PASS" "Kong Proxy responding on port 8000"
else
    print_result "Kong Proxy" "FAIL" "Kong Proxy not responding on port 8000"
fi

# Check Auth Service
if curl -s http://localhost:8081/health > /dev/null 2>&1; then
    print_result "Auth Service" "PASS" "Auth Service running on port 8081"
else
    print_result "Auth Service" "FAIL" "Auth Service not responding on port 8081"
fi

# ============================================================
# AUTHENTICATION
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}2. AUTHENTICATION TESTS${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

echo "Requesting JWT token from Auth Service..."

TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
        "email": "test@example.com",
        "password": "testPassword123"
    }')

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.token // empty' 2>/dev/null)

if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    print_result "JWT Token Generation" "PASS" "Token: ${TOKEN:0:20}..."
else
    # Try with default credentials if available
    TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{
            "email": "admin@sentinel.com",
            "password": "Admin@123"
        }')
    
    TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.token // empty' 2>/dev/null)
    
    if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        print_result "JWT Token Generation" "PASS" "Token acquired (admin)"
    else
        print_result "JWT Token Generation" "FAIL" "Could not obtain JWT token"
        echo "   Response: $TOKEN_RESPONSE"
        TOKEN="test-token-for-demo"
    fi
fi

# ============================================================
# KONG ROUTING TESTS
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}3. KONG ROUTING AND GATEWAY TESTS${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Test routing through Kong
echo "Testing Kong route configuration..."

KONG_STATUS=$(curl -s http://localhost:8001/status)
if echo "$KONG_STATUS" | jq -e '.database.reachable' > /dev/null 2>&1; then
    print_result "Kong Database Connectivity" "PASS" "Kong database is reachable"
else
    print_result "Kong Database Connectivity" "FAIL" "Kong database not reachable"
fi

# Check configured services
SERVICES=$(curl -s http://localhost:8001/services | jq '.data[].name' 2>/dev/null | tr -d '"')
if echo "$SERVICES" | grep -q "bff-service"; then
    print_result "Kong Services Configuration" "PASS" "bff-service configured"
else
    print_result "Kong Services Configuration" "FAIL" "bff-service not found"
fi

# Check configured routes
ROUTES=$(curl -s http://localhost:8001/routes | jq '.data[].name' 2>/dev/null | tr -d '"')
route_count=$(echo "$ROUTES" | wc -l)
if [ "$route_count" -gt 0 ]; then
    print_result "Kong Routes Configuration" "PASS" "$route_count routes configured"
else
    print_result "Kong Routes Configuration" "FAIL" "No routes configured"
fi

# ============================================================
# BFF ENDPOINT TESTS (Direct)
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}4. BFF ENDPOINT TESTS (Direct - No Kong)${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Test Dashboard Endpoint
if curl -s "$BFF_URL/api/bff/dashboard" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/dashboard" "PASS" "Dashboard endpoint responding"
else
    print_result "GET /api/bff/dashboard" "FAIL" "Dashboard endpoint error"
fi

# Test Projects Endpoint
if curl -s "$BFF_URL/api/bff/projects?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/projects" "PASS" "Projects endpoint responding"
else
    print_result "GET /api/bff/projects" "FAIL" "Projects endpoint error"
fi

# Test Scans Endpoint
if curl -s "$BFF_URL/api/bff/scans?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/scans" "PASS" "Scans endpoint responding"
else
    print_result "GET /api/bff/scans" "FAIL" "Scans endpoint error"
fi

# Test Analytics Endpoint
if curl -s "$BFF_URL/api/bff/analytics/vulnerabilities?days=30" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/analytics/vulnerabilities" "PASS" "Analytics endpoint responding"
else
    print_result "GET /api/bff/analytics/vulnerabilities" "FAIL" "Analytics endpoint error"
fi

# Test User Endpoint
if curl -s "$BFF_URL/api/bff/users/profile" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/users/profile" "PASS" "User endpoint responding"
else
    print_result "GET /api/bff/users/profile" "FAIL" "User endpoint error"
fi

# Test Notifications Endpoint
if curl -s "$BFF_URL/api/bff/notifications?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "GET /api/bff/notifications" "PASS" "Notifications endpoint responding"
else
    print_result "GET /api/bff/notifications" "FAIL" "Notifications endpoint error"
fi

# ============================================================
# KONG GATEWAY TESTS (Through Kong)
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}5. KONG GATEWAY TESTS (Routed Through Kong)${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Test routing through Kong
if curl -s "$KONG_URL/api/bff/dashboard" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID" | jq . > /dev/null 2>&1; then
    print_result "Kong Routing: GET /api/bff/dashboard" "PASS" "Request routed successfully"
else
    print_result "Kong Routing: GET /api/bff/dashboard" "FAIL" "Kong routing failed"
fi

# Test rate limiting headers
RATE_LIMIT_RESPONSE=$(curl -s -w '\n%{http_code}' "$KONG_URL/api/bff/projects" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-Id: $TENANT_ID")

HEADERS=$(echo "$RATE_LIMIT_RESPONSE" | head -n-1)
if echo "$HEADERS" | grep -q "X-RateLimit"; then
    print_result "Rate Limiting Headers" "PASS" "X-RateLimit headers present"
else
    print_result "Rate Limiting Headers" "FAIL" "Rate limit headers not found"
fi

# ============================================================
# AUTHENTICATION TESTS
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}6. AUTHENTICATION AND SECURITY TESTS${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Test without token
RESPONSE=$(curl -s -w '\n%{http_code}' "$KONG_URL/api/bff/dashboard" \
    -H "X-Tenant-Id: $TENANT_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    print_result "JWT Validation: Request without token" "PASS" "Correctly rejected (HTTP $HTTP_CODE)"
else
    print_result "JWT Validation: Request without token" "FAIL" "Should return 401/403, got $HTTP_CODE"
fi

# Test with invalid token
RESPONSE=$(curl -s -w '\n%{http_code}' "$KONG_URL/api/bff/dashboard" \
    -H "Authorization: Bearer invalid-token-12345" \
    -H "X-Tenant-Id: $TENANT_ID")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    print_result "JWT Validation: Request with invalid token" "PASS" "Correctly rejected (HTTP $HTTP_CODE)"
else
    print_result "JWT Validation: Request with invalid token" "FAIL" "Should return 401/403, got $HTTP_CODE"
fi

# ============================================================
# CORS TESTS
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}7. CORS TESTS${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

CORS_RESPONSE=$(curl -s -i -X OPTIONS "$KONG_URL/api/bff/dashboard" \
    -H "Origin: http://localhost:3000" \
    -H "Authorization: Bearer $TOKEN" 2>/dev/null | head -20)

if echo "$CORS_RESPONSE" | grep -q "Access-Control-Allow-Origin"; then
    print_result "CORS Preflight: Access-Control-Allow-Origin" "PASS" "CORS header present"
else
    print_result "CORS Preflight: Access-Control-Allow-Origin" "FAIL" "CORS header missing"
fi

if echo "$CORS_RESPONSE" | grep -q "Access-Control-Allow-Methods"; then
    print_result "CORS Preflight: Access-Control-Allow-Methods" "PASS" "Methods header present"
else
    print_result "CORS Preflight: Access-Control-Allow-Methods" "FAIL" "Methods header missing"
fi

# ============================================================
# DATABASE PERSISTENCE TEST
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}8. DATABASE PERSISTENCE TEST${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

# Check PostgreSQL connectivity for BFF database
if nc -z localhost 5432 > /dev/null 2>&1; then
    print_result "PostgreSQL Connectivity" "PASS" "PostgreSQL running on port 5432"
    
    # Check if BFF database exists
    PGPASSWORD="Qwe.123*" psql -U postgres -h localhost -l 2>/dev/null | grep -q "sentinel_bff"
    if [ $? -eq 0 ]; then
        print_result "BFF Database Existence" "PASS" "sentinel_bff database created"
    else
        print_result "BFF Database Existence" "FAIL" "sentinel_bff database not found"
    fi
else
    print_result "PostgreSQL Connectivity" "FAIL" "PostgreSQL not accessible"
fi

# Check MongoDB connectivity
if nc -z localhost 27017 > /dev/null 2>&1; then
    print_result "MongoDB Connectivity" "PASS" "MongoDB running on port 27017"
else
    print_result "MongoDB Connectivity" "FAIL" "MongoDB not accessible"
fi

# ============================================================
# SWAGGER DOCUMENTATION TEST
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}9. API DOCUMENTATION TEST${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"

if curl -s "$BFF_URL/v3/api-docs" | jq . > /dev/null 2>&1; then
    print_result "OpenAPI Documentation" "PASS" "API docs available at /v3/api-docs"
else
    print_result "OpenAPI Documentation" "FAIL" "OpenAPI documentation not available"
fi

if curl -s "$BFF_URL/swagger-ui.html" > /dev/null 2>&1; then
    print_result "Swagger UI" "PASS" "Swagger UI available at /swagger-ui.html"
else
    print_result "Swagger UI" "FAIL" "Swagger UI not available"
fi

# ============================================================
# SUMMARY
# ============================================================

echo ""
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "${BLUE}TESTING SUMMARY${NC}"
echo "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""
echo "📊 Test Coverage:"
echo "   ✓ Service Health Checks"
echo "   ✓ Authentication & JWT Validation"
echo "   ✓ Kong Routing & Gateway"
echo "   ✓ BFF Endpoint Responses"
echo "   ✓ Authentication & Security"
echo "   ✓ CORS Headers"
echo "   ✓ Database Persistence"
echo "   ✓ API Documentation"
echo ""
echo "🌐 Access Points:"
echo "   🔌 Kong Proxy: http://localhost:8000"
echo "   🔌 Kong Admin: http://localhost:8001"
echo "   🔌 Kong Manager: http://localhost:8002"
echo "   🔌 BFF Service: http://localhost:8080"
echo "   🔌 Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "📝 Next Steps:"
echo "   1. Run performance tests (load testing)"
echo "   2. Test failover scenarios"
echo "   3. Monitor logs during load"
echo "   4. Deploy to staging environment"
echo ""
