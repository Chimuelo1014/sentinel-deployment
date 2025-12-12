# 🚀 RUTAS API PARA FRONTEND - SENTINEL

**Generado**: 12 de Diciembre 2025  
**Versión**: 1.0  
**Status**: Ready for Frontend Integration

---

## 📋 TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Estructura Base](#estructura-base)
3. [Autenticación](#autenticación)
4. [Dashboard](#dashboard)
5. [Escaneos](#escaneos)
6. [Proyectos](#proyectos)
7. [Resultados](#resultados)
8. [Analytics](#analytics)
9. [Usuarios & Tenants](#usuarios--tenants)
10. [Notificaciones](#notificaciones)
11. [Códigos de Error](#códigos-de-error)
12. [Ejemplo de Flujo Completo](#ejemplo-de-flujo-completo)

---

## 📊 RESUMEN EJECUTIVO

### URLs Base Configuradas

```javascript
// DESARROLLO LOCAL
const API_BASE = 'http://localhost:8086/api';
const RABBITMQ_API = 'http://localhost:15672/api';

// DOCKER (local containers)
const API_BASE_DOCKER = 'http://backend-for-frontend-service:8086/api';

// PRODUCCIÓN (AWS/GCP/etc)
const API_BASE_PROD = 'https://api.sentinel.example.com/api';
```

### Headers Estándar para Todas las Rutas

```javascript
// Ejemplo con fetch/axios
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`,
  'X-Tenant-Id': tenantId,  // Requerido para multi-tenant
  'X-Client-Version': '1.0'  // Opcional, para versioning
};
```

### Convenciones

- **202 Accepted**: Operación asincrónica (ej: escaneo)
- **200 OK**: Operación sincrónica exitosa
- **201 Created**: Recurso creado
- **204 No Content**: Borrado exitoso
- **400 Bad Request**: Validación fallida
- **401 Unauthorized**: Token inválido o expirado
- **403 Forbidden**: No tiene permisos
- **404 Not Found**: Recurso no existe
- **429 Too Many Requests**: Rate limit excedido
- **500 Internal Server Error**: Error del servidor

---

## 🔐 AUTENTICACIÓN

### 1. Login

**Endpoint:**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "avatar": "https://..."
  }
}
```

**Error (401 Unauthorized):**
```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Email or password is incorrect"
}
```

---

### 2. Register

**Endpoint:**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

---

### 3. Refresh Token

**Endpoint:**
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

---

### 4. OAuth2 (GitHub)

**Endpoint:**
```http
GET /auth/oauth/github?code=github_authorization_code
```

**Response (200 OK):** Redirect a aplicación con token en URL
```
http://frontend.local/callback?token=eyJhbGc...&user=...
```

---

## 📊 DASHBOARD

### GET /bff/dashboard

**Descripción**: Obtiene datos consolidados para el dashboard principal

**Parámetros**: Ninguno

**Response (200 OK):**
```json
{
  "tenant": {
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Acme Corp",
    "plan": "PROFESSIONAL",
    "createdAt": "2025-01-01T10:00:00Z"
  },
  "metrics": {
    "totalScans": 150,
    "totalProjects": 25,
    "activeRepositories": 45,
    "vulnerabilities": {
      "critical": 5,
      "high": 12,
      "medium": 45,
      "low": 120
    },
    "codeQualityScore": 82,
    "securityScore": 78
  },
  "recentScans": [
    {
      "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "projectId": "123e4567-e89b-12d3-a456-426614174000",
      "projectName": "My Backend API",
      "type": "SAST",
      "status": "COMPLETED",
      "startedAt": "2025-12-12T10:00:00Z",
      "completedAt": "2025-12-12T10:15:00Z",
      "findingsCount": {
        "critical": 1,
        "high": 3,
        "medium": 8
      }
    }
  ],
  "recentProjects": [
    {
      "projectId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "My Backend API",
      "language": "python",
      "lastScanDate": "2025-12-12T10:00:00Z",
      "vulnerabilities": {
        "critical": 0,
        "high": 2
      },
      "qualityScore": 85
    }
  ],
  "pendingScans": 3,
  "failedScans": 0
}
```

---

## 🔍 ESCANEOS

### 1. Solicitar Nuevo Escaneo

**Endpoint:**
```http
POST /bff/scans/request
Content-Type: application/json
Authorization: Bearer <token>

{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "scanTypes": ["SAST", "DAST"],
  "targetUrl": "https://github.com/user/repo",
  "clientGitToken": "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "branch": "main",
  "commitSha": "abc123def456"
}
```

**Response (202 Accepted):**
```json
{
  "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "status": "ACCEPTED",
  "requestedService": "SAST",
  "acceptanceTimestampUtc": "2025-12-12T10:00:00Z",
  "completionMethod": "RABBITMQ_EVENT",
  "estimatedCompletionTime": 900,
  "message": "Scan request received and queued for processing"
}
```

**Error (400 Bad Request):**
```json
{
  "code": "INVALID_PROJECT",
  "message": "Project with ID ... does not exist or you don't have access"
}
```

---

### 2. Listar Escaneos

**Endpoint:**
```http
GET /bff/scans?page=0&size=10&status=COMPLETED&type=SAST&sort=createdAt,desc
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Parámetros Query:**
- `page` (int): Número de página (default: 0)
- `size` (int): Elementos por página (default: 10, max: 100)
- `status` (string): PENDING, RUNNING, COMPLETED, FAILED
- `type` (string): SAST, DAST, SBOM, all
- `sort` (string): createdAt, status, type (default: createdAt,desc)
- `startDate` (ISO8601): Filtrar desde fecha
- `endDate` (ISO8601): Filtrar hasta fecha

**Response (200 OK):**
```json
{
  "content": [
    {
      "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "projectId": "123e4567-e89b-12d3-a456-426614174000",
      "projectName": "My Backend API",
      "type": "SAST",
      "status": "COMPLETED",
      "createdAt": "2025-12-12T10:00:00Z",
      "completedAt": "2025-12-12T10:30:00Z",
      "totalFindingsCount": 12,
      "criticalFindings": 1,
      "highFindings": 3,
      "durationSeconds": 1800
    }
  ],
  "totalElements": 150,
  "totalPages": 15,
  "currentPage": 0,
  "size": 10
}
```

---

### 3. Obtener Detalles de Escaneo

**Endpoint:**
```http
GET /bff/scans/{scanId}
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "projectName": "My Backend API",
  "status": "RUNNING",
  "progress": 65,
  "startedAt": "2025-12-12T10:00:00Z",
  "completedAt": null,
  "estimatedCompletionTime": 900,
  "scanTypes": ["SAST"],
  "targetUrl": "https://github.com/user/repo",
  "branch": "main",
  "message": "Scanning with Semgrep... 65% complete",
  "errors": null
}
```

---

### 4. Obtener Resultados de Escaneo

**Endpoint:**
```http
GET /bff/scans/{scanId}/results
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "COMPLETED",
  "completedAt": "2025-12-12T10:30:00Z",
  "results": {
    "codeQuality": {
      "tool": "semgrep",
      "status": "SUCCESS",
      "findings": [
        {
          "id": "python.django.security.sql-injection",
          "title": "SQL Injection Risk",
          "description": "User input is used in raw SQL query without parameterization",
          "severity": "CRITICAL",
          "file": "app/models.py",
          "line": 42,
          "column": 10,
          "code": "SELECT * FROM users WHERE id = \" + user_id",
          "recommendation": "Use parameterized queries or ORM",
          "cwe": "CWE-89",
          "link": "https://semgrep.dev/r/python.django.security.sql-injection"
        }
      ],
      "summary": {
        "totalFindings": 12,
        "critical": 1,
        "high": 3,
        "medium": 5,
        "low": 3
      }
    },
    "vulnerability": {
      "tool": "trivy",
      "status": "SUCCESS",
      "findings": [
        {
          "id": "CVE-2024-1234",
          "severity": "HIGH",
          "package": "django",
          "version": "4.0.0",
          "fixedVersion": "4.0.10",
          "description": "Django XSS vulnerability",
          "affectedLibraries": 3,
          "recommendation": "Update django to version 4.0.10 or higher"
        }
      ],
      "summary": {
        "totalVulnerabilities": 8,
        "critical": 0,
        "high": 2,
        "medium": 4,
        "low": 2
      }
    },
    "aggregated": {
      "totalFindings": 20,
      "riskScore": 78,
      "qualityScore": 82,
      "securityScore": 76,
      "complianceStatus": "PARTIAL"
    }
  }
}
```

---

## 📁 PROYECTOS

### 1. Listar Proyectos

**Endpoint:**
```http
GET /bff/projects?page=0&size=20&sort=name,asc
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "projectId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "My Backend API",
      "description": "RESTful API built with Django",
      "repositoryUrl": "https://github.com/user/repo",
      "language": "python",
      "owner": "John Doe",
      "createdAt": "2025-01-15T10:00:00Z",
      "lastScanDate": "2025-12-12T10:30:00Z",
      "scanCount": 45,
      "statistics": {
        "vulnerabilities": {
          "critical": 0,
          "high": 2,
          "medium": 5
        },
        "qualityScore": 85,
        "trend": "improving"
      }
    }
  ],
  "totalElements": 25,
  "totalPages": 2
}
```

---

### 2. Obtener Detalle de Proyecto

**Endpoint:**
```http
GET /bff/projects/{projectId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "My Backend API",
  "description": "RESTful API built with Django",
  "repositoryUrl": "https://github.com/user/repo",
  "language": "python",
  "owner": "John Doe",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-12-12T10:30:00Z",
  "statistics": {
    "totalScans": 45,
    "averageQualityScore": 85,
    "totalVulnerabilities": 7,
    "trend": {
      "direction": "improving",
      "change": 3.5
    },
    "lastScanDate": "2025-12-12T10:30:00Z"
  },
  "recentScans": [
    {
      "scanId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "type": "SAST",
      "status": "COMPLETED",
      "completedAt": "2025-12-12T10:30:00Z",
      "findingsCount": 12
    }
  ]
}
```

---

### 3. Crear Proyecto

**Endpoint:**
```http
POST /bff/projects
Content-Type: application/json
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000

{
  "name": "New Project",
  "description": "My new security project",
  "repositoryUrl": "https://github.com/user/newrepo",
  "language": "javascript",
  "gitProvider": "GITHUB"
}
```

**Response (201 Created):**
```json
{
  "projectId": "123e4567-e89b-12d3-a456-426614174001",
  "name": "New Project",
  "createdAt": "2025-12-12T11:00:00Z"
}
```

---

### 4. Actualizar Proyecto

**Endpoint:**
```http
PUT /bff/projects/{projectId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Updated Name",
  "description": "Updated description"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Project updated successfully"
}
```

---

### 5. Eliminar Proyecto

**Endpoint:**
```http
DELETE /bff/projects/{projectId}
Authorization: Bearer <token>
```

**Response (204 No Content):**
```
(sin body)
```

---

## 📊 ANALYTICS

### 1. Vulnerabilidades

**Endpoint:**
```http
GET /bff/analytics/vulnerabilities?days=30&projectId=optional
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Parámetros:**
- `days` (int): Últimos N días (default: 30)
- `projectId` (uuid): Filtrar por proyecto (opcional)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-11-12",
    "endDate": "2025-12-12",
    "days": 30
  },
  "trend": [
    {
      "date": "2025-11-12",
      "critical": 8,
      "high": 15,
      "medium": 30,
      "low": 50
    },
    {
      "date": "2025-11-13",
      "critical": 7,
      "high": 14,
      "medium": 29,
      "low": 48
    }
  ],
  "distribution": {
    "critical": 5,
    "high": 12,
    "medium": 45,
    "low": 120
  },
  "topCves": [
    {
      "cveId": "CVE-2024-1234",
      "severity": "CRITICAL",
      "affectedComponents": 3,
      "description": "Remote Code Execution vulnerability",
      "cvssScore": 9.8
    }
  ],
  "comparison": {
    "change": -2,
    "direction": "improving",
    "changePercentage": 1.2
  }
}
```

---

### 2. Calidad de Código

**Endpoint:**
```http
GET /bff/analytics/code-quality?days=30&projectId=optional
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-11-12",
    "endDate": "2025-12-12"
  },
  "trend": [
    {
      "date": "2025-11-12",
      "score": 78,
      "issues": 32
    },
    {
      "date": "2025-11-13",
      "score": 79,
      "issues": 30
    }
  ],
  "currentScore": 82,
  "issuesByCategory": {
    "codeSmell": 10,
    "security": 5,
    "bug": 3,
    "duplication": 8,
    "complexity": 12
  },
  "topIssues": [
    {
      "id": "complexity.high",
      "title": "High Cyclomatic Complexity",
      "count": 5,
      "severity": "MEDIUM"
    }
  ]
}
```

---

### 3. Compliance

**Endpoint:**
```http
GET /bff/analytics/compliance?days=30&projectId=optional
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "standards": {
    "pci_dss": {
      "status": "COMPLIANT",
      "passingTests": 12,
      "totalTests": 13,
      "failingTests": ["PCI-3.2.1"],
      "percentage": 92.3
    },
    "owasp_top_10": {
      "status": "COMPLIANT",
      "passingTests": 10,
      "totalTests": 10,
      "percentage": 100
    },
    "cis_docker": {
      "status": "PARTIAL",
      "passingTests": 18,
      "totalTests": 25,
      "failingTests": ["CIS-4.1", "CIS-5.2"],
      "percentage": 72
    }
  },
  "overallCompliance": 88.1
}
```

---

## 👤 USUARIOS & TENANTS

### 1. Obtener Perfil Actual

**Endpoint:**
```http
GET /bff/user
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "avatar": "https://example.com/avatars/john.jpg",
  "joinedAt": "2025-01-01T10:00:00Z",
  "role": "ADMIN",
  "tenant": {
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Acme Corp",
    "role": "ADMIN"
  }
}
```

---

### 2. Actualizar Perfil

**Endpoint:**
```http
PUT /bff/user
Content-Type: application/json
Authorization: Bearer <token>

{
  "firstName": "John",
  "lastName": "Doe",
  "avatar": "https://example.com/avatars/john.jpg"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile updated successfully"
}
```

---

### 3. Cambiar Contraseña

**Endpoint:**
```http
POST /bff/user/change-password
Content-Type: application/json
Authorization: Bearer <token>

{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

---

### 4. Obtener Tenant Actual

**Endpoint:**
```http
GET /bff/tenant
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Acme Corp",
  "plan": "PROFESSIONAL",
  "createdAt": "2025-01-01T10:00:00Z",
  "limits": {
    "scansPerMonth": 500,
    "projects": 50,
    "repositories": 200,
    "users": 25
  },
  "usage": {
    "scans": {
      "used": 245,
      "remaining": 255
    },
    "projects": {
      "used": 15,
      "remaining": 35
    },
    "repositories": {
      "used": 35,
      "remaining": 165
    },
    "users": {
      "used": 8,
      "remaining": 17
    }
  }
}
```

---

### 5. Invitar Usuario

**Endpoint:**
```http
POST /bff/tenant/invitations
Content-Type: application/json
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000

{
  "email": "newuser@example.com",
  "role": "DEVELOPER"
}
```

**Response (201 Created):**
```json
{
  "invitationId": "inv_123456",
  "email": "newuser@example.com",
  "role": "DEVELOPER",
  "invitationUrl": "https://app.sentinel.io/invitations/inv_123456?token=...",
  "expiresAt": "2025-12-19T11:00:00Z"
}
```

---

### 6. Listar Miembros

**Endpoint:**
```http
GET /bff/tenant/members
Authorization: Bearer <token>
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "ADMIN",
    "joinedAt": "2025-01-01T10:00:00Z",
    "status": "ACTIVE"
  },
  {
    "email": "newuser@example.com",
    "role": "DEVELOPER",
    "status": "PENDING",
    "invitedAt": "2025-12-12T11:00:00Z"
  }
]
```

---

## 🔔 NOTIFICACIONES

### 1. Listar Notificaciones

**Endpoint:**
```http
GET /bff/notifications?page=0&size=10&unreadOnly=false
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "notificationId": "notif_123456",
      "type": "SCAN_COMPLETED",
      "title": "Escaneo Completado",
      "message": "El escaneo SAST del proyecto 'My Backend API' completó con 12 hallazgos",
      "severity": "INFO",
      "createdAt": "2025-12-12T10:30:00Z",
      "read": false,
      "actionUrl": "/scans/f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "relatedId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    }
  ],
  "totalUnread": 5
}
```

---

### 2. Marcar Notificación como Leída

**Endpoint:**
```http
PUT /bff/notifications/{notificationId}/read
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "success": true
}
```

---

### 3. Marcar Todas como Leídas

**Endpoint:**
```http
PUT /bff/notifications/read-all
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "markedAsRead": 5
}
```

---

## ❌ CÓDIGOS DE ERROR

### Errores Generales

| Code | HTTP | Mensaje | Solución |
|------|------|---------|----------|
| `INVALID_CREDENTIALS` | 401 | Email o contraseña incorrectos | Verificar credenciales |
| `TOKEN_EXPIRED` | 401 | Token expirado | Usar refresh token |
| `INVALID_TOKEN` | 401 | Token inválido | Re-login |
| `FORBIDDEN` | 403 | No tiene permisos | Contactar admin |
| `NOT_FOUND` | 404 | Recurso no encontrado | Verificar ID |
| `VALIDATION_ERROR` | 400 | Datos inválidos | Ver detalles |
| `RATE_LIMIT` | 429 | Demasiadas solicitudes | Esperar y reintentar |
| `INTERNAL_ERROR` | 500 | Error del servidor | Contactar soporte |

### Errores de Escaneo

| Code | Mensaje | Causa |
|------|---------|-------|
| `INVALID_PROJECT` | Proyecto no existe | ID incorrecto o sin acceso |
| `INVALID_GIT_TOKEN` | Token git inválido | Permisos insuficientes |
| `SCAN_TIMEOUT` | Escaneo excedió timeout | Repositorio muy grande |
| `SCAN_FAILED` | Error durante escaneo | Ver logs |
| `WEBHOOK_FAILED` | Error invocando webhook | n8n inaccesible |

---

## 📝 EJEMPLO DE FLUJO COMPLETO

### Escenario: Usuario nuevo quiere escanear su primer repositorio

```javascript
// 1. REGISTER
POST /auth/register
{
  "email": "newuser@example.com",
  "password": "secure123",
  "firstName": "Jane",
  "lastName": "Doe"
}
→ Response: { token, userId }

// 2. GET DASHBOARD (vacio)
GET /bff/dashboard
Authorization: Bearer <token>
→ Response: { metrics: { totalScans: 0, totalProjects: 0 } }

// 3. CREATE PROJECT
POST /bff/projects
{
  "name": "My First Project",
  "repositoryUrl": "https://github.com/newuser/app",
  "language": "python"
}
→ Response: { projectId }

// 4. REQUEST SCAN
POST /bff/scans/request
{
  "projectId": "<projectId from step 3>",
  "scanTypes": ["SAST"],
  "targetUrl": "https://github.com/newuser/app",
  "clientGitToken": "ghp_..."
}
→ Response: 202 ACCEPTED { scanId, status: "ACCEPTED" }

// 5. POLL SCAN STATUS
GET /bff/scans/{scanId}
Authorization: Bearer <token>
→ Response: { status: "RUNNING", progress: 45 }
(repeat until status === "COMPLETED")

// 6. GET SCAN RESULTS
GET /bff/scans/{scanId}/results
Authorization: Bearer <token>
→ Response: { results: { codeQuality: { findings: [...] } } }

// 7. VIEW DASHBOARD (actualizado)
GET /bff/dashboard
Authorization: Bearer <token>
→ Response: { metrics: { totalScans: 1, totalProjects: 1, vulnerabilities: {...} } }

// 8. CHECK ANALYTICS
GET /bff/analytics/vulnerabilities?days=30
Authorization: Bearer <token>
→ Response: { trend: [...], distribution: {...} }
```

---

## 🔑 Notas Importantes

1. **Autenticación**: Todos los endpoints excepto `/auth/*` requieren `Authorization: Bearer <token>`

2. **Multi-Tenant**: Para operaciones en contexto de tenant, incluir `X-Tenant-Id` header

3. **Paginación**: El tamaño máximo de página es 100. Defaults a 10 si no se especifica.

4. **Rate Limiting**: 100 requests/minuto por usuario. Si excede, recibe 429.

5. **Webhooks**: Los escaneos usan webhooks. Asegúrese de que n8n sea accesible desde SecurityGate

6. **Timeouts**: Los escaneos pueden demorar desde 5 minutos (SAST) hasta 30 minutos (DAST)

7. **Errores**: Siempre revisar el campo `message` en respuesta de error para detalles adicionales

---

**Última actualización**: 12 de Diciembre 2025  
**Versión de API**: 1.0  
**Status**: Production Ready
