# 📱 Sentinel Frontend API Documentation

**Version**: 1.0  
**Base URL**: `http://localhost:8000` (Kong Gateway)  
**Date**: December 12, 2025

---

## 🌐 Frontend Communication Flow

```
Frontend (React/Vue/Angular)
       ↓ HTTP Request
Kong API Gateway (localhost:8000)
       ↓ Routes request
BFF Service (localhost:8080)
       ↓ Aggregates data
Microservices
```

### CORS Configuration

✅ **CORS is fully configured** in both Kong and BFF:

- **Kong**: Global CORS plugin enabled (all origins allowed)
- **BFF**: `WebSecurityConfig.java` + `WebMvcConfig.java` configured

### Frontend Integration Example (JavaScript/React)

```javascript
const API_BASE_URL = 'http://localhost:8000';  // Kong Gateway

// Axios configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const tenantId = localStorage.getItem('tenantId');
  if (tenantId) {
    config.headers['X-Tenant-Id'] = tenantId;
  }
  return config;
});

// Usage
const fetchDashboard = async () => {
  const response = await api.get('/api/bff/dashboard');
  return response.data;
};
```

---

## 🔑 Authentication

All requests (except login/register) require:
```
Authorization: Bearer <jwt_token>
X-Tenant-Id: <tenant_id>  (optional, for multi-tenant operations)
```

---

## 📡 API Endpoints

### 🔐 Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | User login | ❌ |
| POST | `/api/auth/refresh` | Refresh access token | ❌ |
| POST | `/api/auth/revoke` | Revoke refresh token | ❌ |
| POST | `/api/auth/logout` | Logout (revoke all tokens) | ✅ |

#### Register Request
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login Request
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

#### Auth Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

---

### 📊 Dashboard (`/api/bff/dashboard`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bff/dashboard` | Get aggregated dashboard data |

#### Response
```json
{
  "tenants": [...],
  "projects": [...],
  "recentScans": [...],
  "stats": {
    "totalTenants": 5,
    "totalProjects": 12,
    "totalScans": 45
  }
}
```

---

### 📁 Projects (`/api/bff/projects`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bff/projects` | List all projects (paginated) |
| GET | `/api/bff/projects/{id}` | Get project details |
| POST | `/api/bff/projects` | Create new project |
| PUT | `/api/bff/projects/{id}` | Update project |
| DELETE | `/api/bff/projects/{id}` | Delete project |
| GET | `/api/bff/projects/{id}/statistics` | Get project statistics |

#### Query Parameters (List)
- `page` (default: 0)
- `size` (default: 20)
- `sort` (default: name,asc)

#### Project DTO
```json
{
  "projectId": "uuid",
  "name": "My Project",
  "description": "Project description",
  "repositoryUrl": "https://github.com/user/repo",
  "language": "Java",
  "owner": "user@example.com",
  "createdAt": "2025-01-01T10:00:00Z",
  "lastScanDate": "2025-12-01T10:00:00Z",
  "scanCount": 15,
  "statistics": {
    "vulnerabilities": 5,
    "codeSmells": 12,
    "coverage": 85.5
  }
}
```

---

### 🔍 Scans (`/api/bff/scans`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bff/scans/request` | Request new scan |
| GET | `/api/bff/scans` | List scans (paginated) |
| GET | `/api/bff/scans/{id}` | Get scan status |
| GET | `/api/bff/scans/{id}/results` | Get scan results |
| DELETE | `/api/bff/scans/{id}` | Cancel scan |
| GET | `/api/bff/scans/{id}/export` | Export results (PDF/JSON) |

#### Query Parameters (List)
- `page`, `size`, `sort`
- `status` (PENDING, RUNNING, COMPLETED, FAILED)
- `type` (VULNERABILITY, CODE_QUALITY, SECURITY)
- `startDate`, `endDate`

#### Scan Request DTO
```json
{
  "projectId": "uuid",
  "scanTypes": ["VULNERABILITY", "CODE_QUALITY", "SECURITY"],
  "targetUrl": "https://github.com/user/repo",
  "clientGitToken": "ghp_xxx...",
  "branch": "main",
  "commitSha": "abc123..."
}
```

#### Scan Response DTO
```json
{
  "scanId": "uuid",
  "status": "PENDING",
  "requestedService": "ORCHESTRATOR",
  "acceptanceTimestampUtc": "2025-12-12T22:00:00Z",
  "completionMethod": "ASYNC",
  "estimatedCompletionTime": 300,
  "message": "Scan queued successfully"
}
```

#### Export Parameters
- `format`: `PDF` | `JSON` (default: PDF)

---

### 📈 Analytics (`/api/bff/analytics`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bff/analytics/vulnerabilities` | Vulnerability trends |
| GET | `/api/bff/analytics/code-quality` | Code quality metrics |
| GET | `/api/bff/analytics/compliance` | Compliance status |

#### Query Parameters
- `projectId` (optional)
- `days` (default: 30)
- `standards` (for compliance: GDPR,PCI-DSS,HIPAA)

---

### 👤 Users (`/api/bff/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bff/users/profile` | Get user profile |
| PUT | `/api/bff/users/profile` | Update profile |
| GET | `/api/bff/users/preferences` | Get preferences |
| PUT | `/api/bff/users/preferences` | Update preferences |
| POST | `/api/bff/users/change-password` | Change password |

#### User Profile DTO
```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "avatar": "https://...",
  "role": "ADMIN",
  "createdAt": "2025-01-01T10:00:00Z"
}
```

#### Preferences
```json
{
  "notifications": {
    "scanCompletion": true,
    "criticalVulnerabilities": true,
    "weeklyReport": true
  },
  "theme": "DARK",
  "language": "en"
}
```

---

### 🔔 Notifications (`/api/bff/notifications`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bff/notifications` | Get notifications (paginated) |
| PUT | `/api/bff/notifications/{id}/read` | Mark as read |
| PUT | `/api/bff/notifications/read-all` | Mark all as read |
| DELETE | `/api/bff/notifications/{id}` | Delete notification |
| GET | `/api/bff/notifications/preferences` | Get notification prefs |
| PUT | `/api/bff/notifications/preferences` | Update notification prefs |

#### Query Parameters
- `page`, `size`
- `type` (optional)
- `unreadOnly` (boolean, default: false)

---

### 💳 Billing (`/api/billing`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/plans` | List available plans |
| GET | `/api/plans/{id}` | Get plan details |
| GET | `/api/subscriptions` | List subscriptions |
| POST | `/api/subscriptions` | Create subscription |
| PUT | `/api/subscriptions/{id}` | Update subscription |
| DELETE | `/api/subscriptions/{id}` | Cancel subscription |
| GET | `/api/payments` | List payments |
| POST | `/api/payments` | Create payment |
| GET | `/api/payments-history` | Payment history |

---

## 🎨 Frontend Pages to Implement

### 1. Authentication
- [ ] Login page
- [ ] Register page
- [ ] Forgot password
- [ ] Reset password
- [ ] 2FA setup/verification

### 2. Dashboard
- [ ] Main dashboard with stats
- [ ] Recent scans widget
- [ ] Projects overview
- [ ] Quick actions

### 3. Projects
- [ ] Project list (table with pagination)
- [ ] Project details page
- [ ] Create/Edit project form
- [ ] Project statistics view

### 4. Scans
- [ ] Scan list (filterable table)
- [ ] Request new scan form
- [ ] Scan status/progress view
- [ ] Scan results viewer
- [ ] Export results

### 5. Analytics
- [ ] Vulnerability trends chart
- [ ] Code quality metrics
- [ ] Compliance dashboard

### 6. User Settings
- [ ] Profile page
- [ ] Preferences page
- [ ] Change password
- [ ] Notification settings

### 7. Billing
- [ ] Plan selection
- [ ] Subscription management
- [ ] Payment history
- [ ] Invoices

---

## 🔧 HTTP Headers Required

```javascript
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${accessToken}`,
  'X-Tenant-Id': tenantId  // Optional
};
```

---

## ⚠️ Error Responses

```json
{
  "timestamp": "2025-12-12T22:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "Invalid email format" }
  ]
}
```

### Status Codes
- `200` - Success
- `201` - Created
- `202` - Accepted (async operations)
- `204` - No Content
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `429` - Too Many Requests
- `500` - Internal Server Error

---

## 📚 Swagger/OpenAPI

Access interactive API documentation:
- **BFF Swagger**: http://localhost:8080/swagger-ui.html
- **Auth Swagger**: http://localhost:8081/swagger-ui.html
