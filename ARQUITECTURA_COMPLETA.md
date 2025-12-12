# 🏗️ ANÁLISIS COMPLETO - ARQUITECTURA DE SENTINEL

## 📊 Visión General

**Sentinel** es una plataforma **SaaS de seguridad** basada en **microservicios y arquitectura event-driven**. Permite a los usuarios hacer escaneos de seguridad (SAST, DAST, IaC, vulnerabilidades) sobre sus proyectos y código.

### Stack Tecnológico
- **Backend**: Java Spring Boot (microservicios) + .NET (servicios de calidad)
- **Message Broker**: RabbitMQ (comunicación asincrónica)
- **Base de Datos**: PostgreSQL (datos relacionales) + MongoDB (agregación de resultados)
- **API Gateway**: Kong (en desarrollo)
- **Workflow Engine**: n8n (orquestación de escaneos)
- **Herramientas de Seguridad**: Semgrep (SAST), OWASP ZAP (DAST), Trivy (vulnerabilidades)

---

## 🔄 FLUJO DE DATOS (Event-Driven)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SENTINEL EVENT-DRIVEN ARCHITECTURE                   │
└─────────────────────────────────────────────────────────────────────────────┘

1. INICIO (Usuario)
   └─► Frontend/Cliente HTTP
       └─► API-Gateway (Kong)

2. AUTENTICACIÓN
   └─► Auth-Service (Login, Register, 2FA, JWT)
       └─► Publica: auth.user.registered
       └─► Publica: auth.user.login
           └─► RabbitMQ (auth-exchange)

3. CREACIÓN DE TENANTS/PROYECTOS
   └─► Tenant-Service (Crea tenant al registrarse)
       └─► Project-Service (Crea proyectos dentro del tenant)
           └─► Publica: project.created
           └─► Publica: domain.added
           └─► Publica: repository.added

4. FACTURACIÓN (Billing)
   └─► Billing-Service (Suscripciones, pagos, planes)
       └─► Publica: billing.payment_succeeded
       └─► Publica: billing.subscription.created
           └─► Consumidores: tenant-service, auth-service

5. ORQUESTACIÓN DE ESCANEOS
   └─► Scanner-Orchestrator-Service (Coordina escaneos)
       ├─► n8n (Ejecuta escaneos: Semgrep, ZAP, Trivy)
       └─► SecurityGate-Service (.NET, coordina flujo)

6. PROCESAMIENTO DE RESULTADOS
   ├─► CodeQuality-Service (.NET)
   │   └─► Evalúa quality gates
   │   └─► Publica: quality.gate.result
   │
   ├─► Vulnerability-Service (.NET)
   │   └─► Agrega vulnerabilidades
   │   └─► Publica: vulnerability.result
   │
   └─► Results-Aggregator-Service
       └─► Almacena en MongoDB
       └─► Disponible para consultas

7. BFF (Backend-For-Frontend)
   └─► Agrega datos de múltiples servicios
   └─► Retorna dashboard consolidado al frontend

8. GESTIÓN DE USUARIOS
   └─► User-Management-Service
       └─► Escucha eventos (tenant.created, project.created)
       └─► Asigna roles automáticamente
```

---

## 📦 SERVICIOS DETALLADOS

### 🔐 **1. AUTH-SERVICE** (Java Spring Boot)

**Puerto**: 8081  
**Responsabilidad**: Gestionar toda la autenticación, autorización y seguridad

#### Funcionalidades:
- ✅ Registro de usuarios (con validación de email)
- ✅ Login/Logout
- ✅ Refresh tokens
- ✅ 2FA (Two-Factor Authentication) con Google Authenticator
- ✅ OAuth2 (integración con proveedores externos)
- ✅ Password reset y password change
- ✅ Audit logs (registra todas las acciones de seguridad)
- ✅ Rate limiting
- ✅ JWT tokens

#### Eventos que Publica:
- `auth.user.registered` → Para que tenant-service cree tenant automáticamente
- `auth.user.login` → Para auditoría
- `auth.password.changed` → Notificación

#### Eventos que Consume:
- `billing.subscription.*` → Actualizar plan del usuario

#### Base de Datos:
- **PostgreSQL**: usuarios, tokens refresh, audit logs, password reset tokens

---

### 🏢 **2. TENANT-SERVICE** (Java Spring Boot)

**Puerto**: 8082  
**Responsabilidad**: Gestionar tenants (organizaciones) y sus límites de recursos

#### Funcionalidades:
- ✅ Crear/Actualizar/Eliminar tenants
- ✅ Gestionar planes y límites por tenant
- ✅ Rastrear uso de recursos (proyectos, scans, etc.)
- ✅ Validar límites antes de operaciones

#### Eventos que Consume:
- `auth.user.registered` → Crea tenant automáticamente
- `billing.subscription.created` → Asigna plan y límites
- `billing.subscription.upgraded` → Actualiza límites

#### Eventos que Publica:
- `tenant.created` → Para que user-management asigne roles
- `tenant.plan.upgraded` → Para notificar al proyecto

#### Base de Datos:
- **PostgreSQL**: tenants, planes, límites

---

### 📋 **3. PROJECT-SERVICE** (Java Spring Boot)

**Puerto**: 8084  
**Responsabilidad**: Gestionar proyectos, dominios y repositorios dentro de tenants

#### Funcionalidades:
- ✅ CRUD de proyectos
- ✅ Gestión de dominios (verificación de propiedad)
- ✅ Gestión de repositorios (Git)
- ✅ Validar límites del tenant
- ✅ Validación de acceso (RBAC)

#### Eventos que Publica:
- `project.created` → Para user-management asigne roles
- `project.deleted` → Para cancelar escaneos activos
- `domain.added` → Para verificación en C#
- `repository.added` → Auditoría

#### Eventos que Consume:
- `domain.verified` (desde C#) → Marcar dominio como verificado
- `billing.payment_succeeded` → Habilitar funcionalidades

#### Base de Datos:
- **PostgreSQL**: proyectos, dominios, repositorios

---

### 👥 **4. USER-MANAGEMENT-SERVICE** (Java Spring Boot)

**Puerto**: 8085  
**Responsabilidad**: Gestionar usuarios dentro de organizaciones y proyectos

#### Funcionalidades:
- ✅ Agregar/Remover usuarios a tenants
- ✅ Agregar/Remover usuarios a proyectos
- ✅ Gestión de roles (TENANT_ADMIN, PROJECT_ADMIN, DEVELOPER, VIEWER)
- ✅ Validación de permisos
- ✅ Asignación automática de roles al crear entidades

#### Eventos que Consume:
- `tenant.created` → Asignar owner como TENANT_ADMIN
- `project.created` → Asignar owner como PROJECT_ADMIN
- `auth.user.registered` → Crear membership en tenant

#### Base de Datos:
- **PostgreSQL**: tenant_members, project_members, roles

---

### 💰 **5. BILLING-SERVICE** (Java Spring Boot)

**Puerto**: 8086  
**Responsabilidad**: Gestionar suscripciones, pagos y facturación

#### Funcionalidades:
- ✅ Integración con MercadoPago
- ✅ Integración con PayPal
- ✅ Integración con Crypto (USDT, USDC, BTC Lightning)
- ✅ Gestión de planes y precios
- ✅ Renovación automática de suscripciones
- ✅ Reintentos de cobro
- ✅ Historial de facturas
- ✅ Facturación en blockchain (opcional)

#### Eventos que Publica:
- `billing.payment_succeeded` → Para activar recursos
- `billing.subscription.created` → Para tenant-service
- `billing.subscription.upgraded` → Para actualizar límites

#### Base de Datos:
- **PostgreSQL**: suscripciones, pagos, facturas, planes

---

### 🔍 **6. SCANNER-ORCHESTRATOR-SERVICE** (Java Spring Boot)

**Puerto**: 8087  
**Responsabilidad**: Coordinar y orquestar los escaneos de seguridad

#### Funcionalidades:
- ✅ Crear solicitudes de escaneo
- ✅ Coordinar con n8n
- ✅ Rastrear estado de escaneos
- ✅ Cancelar escaneos
- ✅ Manejar reintentos

#### Eventos que Consume:
- `project.deleted` → Cancelar escaneos del proyecto

#### Eventos que Publica:
- `scan.requested` → Para que n8n inicie escaneo
- `scan.progress` → Progreso en tiempo real
- `scan.completed` → Escaneo terminado

#### Base de Datos:
- **PostgreSQL**: escaneos, estado, logs

---

### 📊 **7. RESULTS-AGGREGATOR-SERVICE** (Java Spring Boot)

**Puerto**: 8088  
**Responsabilidad**: Agregar y almacenar resultados de escaneos

#### Funcionalidades:
- ✅ Recibe resultados de múltiples escaneos
- ✅ Agrega por ScanId
- ✅ Almacena en MongoDB (mejor para documentos JSON grandes)
- ✅ Proporciona API de consulta

#### Eventos que Consume:
- `scan.completed` → Guarda resultados

#### Base de Datos:
- **MongoDB**: resultados de escaneos (JSON documents)

---

### 🛡️ **8. BACKEND-FOR-FRONTEND (BFF)** (Java Spring Boot)

**Puerto**: 8089  
**Responsabilidad**: Agregar datos de múltiples servicios para el frontend

#### Funcionalidades:
- ✅ API única para el frontend
- ✅ Agregación de datos (tenants, proyectos, escaneos)
- ✅ Llamadas paralelas usando Feign + CompletableFuture
- ✅ Circuit breaker con Resilience4j
- ✅ Manejo de errores centralizado

#### Usa Feign Clients para:
- TenantClient → tenant-service
- ProjectClient → project-service
- ScanClient → scanner-orchestrator-service

#### Ejemplo Endpoint:
```java
GET /api/bff/dashboard
Headers: Authorization: Bearer <JWT>
Response: {
  tenants: [...],
  projects: [...],
  recentScans: [...],
  stats: {...}
}
```

---

### 🛡️ **9. SECURITY-GATE-SERVICE** (.NET Core)

**Puerto**: 5000  
**Responsabilidad**: Orquestación central de seguridad (puente Java ↔ n8n)

#### Funcionalidades:
- ✅ Recibe solicitudes de escaneo
- ✅ Dispara workflows en n8n
- ✅ Escucha resultados en RabbitMQ
- ✅ Notifica de vuelta al BFF
- ✅ Health checks

#### Flujo:
```
Java BFF
  ├─► POST /api/scan/request
  └─► SecurityGate
      ├─► Dispara n8n webhook
      ├─► Publica en RabbitMQ
      └─► Retorna scan ID
```

---

### ✅ **10. CODE-QUALITY-SERVICE** (.NET Core)

**Puerto**: 5001  
**Responsabilidad**: Evaluar calidad del código con Semgrep

#### Funcionalidades:
- ✅ Recibe notificación de n8n (Semgrep completó)
- ✅ Lee JSON de resultados del volumen compartido
- ✅ Mapea hallazgos de Semgrep a estructura interna
- ✅ Evalúa quality gates
- ✅ Publica PASS/FAIL a RabbitMQ

#### Quality Gate Rules:
```
if (secretos detectados) → FAIL
if (hallazgos críticos > 5) → FAIL
else → PASS
```

#### RabbitMQ:
- Consume: notificación de n8n
- Publica: `quality.gate.result` a sentinel.scan.results exchange

---

### 🔍 **11. VULNERABILITY-SERVICE** (.NET Core)

**Puerto**: 5002  
**Responsabilidad**: Procesar y agregar vulnerabilidades

#### Funcionalidades:
- ✅ Recibe resultados de Trivy (IaC/contenedores)
- ✅ Recibe resultados de OWASP ZAP (DAST)
- ✅ Normaliza diferentes formatos
- ✅ Agrega por ScanId
- ✅ Categoriza por severidad
- ✅ Publica a RabbitMQ

#### Flujo:
```
n8n (Trivy) + n8n (ZAP)
  └─► POST /api/v1/n8n/vulnerability-ready
  └─► Vulnerability-Service
      ├─► Lee resultados de volumen
      ├─► Normaliza y agrega
      ├─► Evalúa quality gates
      └─► Publica en RabbitMQ
```

---

### 🌐 **12. API-GATEWAY** (Kong)

**Puerto**: 8000  
**Responsabilidad**: Puerta de entrada única a toda la plataforma

#### Funcionalidades:
- ✅ Routing de requests a microservicios
- ✅ SSL/TLS termination
- ✅ Rate limiting
- ✅ Autenticación centralizada
- ✅ Logs y métricas

#### Configuración:
```
Kong en localhost:8000
  ├─► /api/auth/* → auth-service:8081
  ├─► /api/tenants/* → tenant-service:8082
  ├─► /api/projects/* → project-service:8084
  ├─► /api/scans/* → scanner-orchestrator:8087
  └─► /api/bff/* → backend-for-frontend:8089
```

---

## 🔌 RABBITMQ - SISTEMA DE EVENTOS

### Exchanges (Topic)

```
1. auth-exchange
   ├─ auth.user.registered
   ├─ auth.user.login
   └─ auth.password.changed

2. tenant-exchange
   ├─ tenant.created
   └─ tenant.plan.upgraded

3. project-exchange
   ├─ project.created
   ├─ project.deleted
   ├─ domain.added
   └─ repository.added

4. billing-exchange
   ├─ billing.payment_succeeded
   ├─ billing.payment_failed
   └─ billing.subscription.created

5. scan-exchange (.NET ↔ Java)
   ├─ scan.requested
   ├─ scan.progress
   ├─ scan.completed
   └─ scan.failed
```

### Consumer-Producer Map

```
auth-service (Producer)
  ├─► auth.user.registered
  │   └─► tenant-service (Consumer) → Crea tenant
  │   └─► user-management (Consumer) → Asigna roles
  └─► auth.user.login (Auditoría)

tenant-service (Producer)
  └─► tenant.created
      └─► user-management (Consumer) → Asigna TENANT_ADMIN

project-service (Producer)
  ├─► project.created
  │   └─► user-management (Consumer) → Asigna PROJECT_ADMIN
  │   └─► scanner-orchestrator (Consumer)
  ├─► domain.added
  │   └─► C# DomainVerification
  └─► domain.verified (Consume desde C#)

billing-service (Producer)
  ├─► billing.payment_succeeded
  │   └─► project-service (Consumer) → Habilita features
  │   └─► auth-service (Consumer)
  └─► billing.subscription.created
      └─► tenant-service (Consumer) → Asigna plan

SecurityGate + CodeQuality/Vulnerability (.NET)
  ├─► Publican: scan results
  └─► Results-Aggregator (Consumer)
```

---

## 📱 FLUJO COMPLETO DE UN USUARIO

### 1️⃣ **Registro**
```
Usuario → Frontend
    ↓
POST /api/auth/register
    ↓
Auth-Service
    ├─► Valida email
    ├─► Crea usuario en PostgreSQL
    ├─► Publica: auth.user.registered → RabbitMQ
    └─► Retorna JWT
        │
        ├─► Tenant-Service (consume) → Crea tenant automáticamente
        └─► User-Management (consume) → Asigna TENANT_ADMIN
```

### 2️⃣ **Login**
```
Usuario → Frontend
    ↓
POST /api/auth/login
    ↓
Auth-Service
    ├─► Valida credenciales
    ├─► Verifica 2FA si está habilitado
    ├─► Genera JWT + Refresh Token
    ├─► Publica: auth.user.login (para auditoría)
    └─► Retorna tokens
```

### 3️⃣ **Crear Proyecto**
```
POST /api/projects
Headers: Authorization: Bearer <JWT>
Body: { name, description, tenantId }
    ↓
Project-Service
    ├─► Valida token
    ├─► Verifica límites del tenant (rabbitmq cache)
    ├─► Crea proyecto en PostgreSQL
    ├─► Publica: project.created → RabbitMQ
    └─► Retorna projectId
        │
        ├─► User-Management (consume) → Asigna PROJECT_ADMIN
        └─► Scanner-Orchestrator (consume)
```

### 4️⃣ **Solicitar Escaneo**
```
POST /api/scans
Body: { projectId, repositoryUrl, branchName }
    ↓
Scanner-Orchestrator-Service
    ├─► Valida acceso
    ├─► Crea registro de scan
    ├─► Publica: scan.requested
    ├─► Dispara webhook en n8n
    └─► Retorna scanId
        │
        ├─► n8n inicia Semgrep
        ├─► n8n inicia Trivy
        └─► n8n inicia OWASP ZAP
```

### 5️⃣ **Procesar Resultados**
```
Escaneo completado en n8n
    ↓ (notifica a SecurityGate)
SecurityGate-Service (.NET)
    ├─► POST /api/codeQuality/semgrep-ready
    └─► Notifica CodeQuality-Service
        │
        ├─ CodeQuality-Service (.NET)
        │   ├─► Lee JSON de Semgrep
        │   ├─► Evalúa quality gates
        │   ├─► Publica: scan.results
        │   └─► RabbitMQ
        │
        ├─ Vulnerability-Service (.NET)
        │   ├─► Lee JSON de Trivy + ZAP
        │   ├─► Normaliza hallazgos
        │   ├─► Agrega resultados
        │   ├─► Publica: scan.results
        │   └─► RabbitMQ
        │
        └─► Results-Aggregator-Service (Java)
            ├─► Consume scan.results
            └─► Guarda en MongoDB
```

### 6️⃣ **Ver Resultados en Dashboard**
```
GET /api/bff/dashboard
Headers: Authorization: Bearer <JWT>
    ↓
Backend-For-Frontend (BFF)
    ├─► Feign: TenantClient.getMyTenants()
    ├─► Feign: ProjectClient.getMyProjects()
    ├─► Feign: ScanClient.getScans()
    └─► (Llamadas paralelas con CompletableFuture)
        │
        ├─► Tenant-Service (PostgreSQL)
        ├─► Project-Service (PostgreSQL)
        └─► Scanner-Orchestrator (PostgreSQL)
            │
            └─► Consulta Results-Aggregator (MongoDB)
                └─► Retorna resultados consolidados
```

---

## 🏗️ PATRONES ARQUITECTÓNICOS

### 1. **Event-Driven Architecture**
- Servicios no se llamanDirectamente
- Comunican via RabbitMQ topics
- Desacoplamiento = Escalabilidad

### 2. **Saga Pattern** (para transacciones distribuidas)
```
Ejemplo: Cuando un usuario se registra
Paso 1: Auth-Service crea usuario
    ↓ (publica evento)
Paso 2: Tenant-Service crea tenant
    ↓ (publica evento)
Paso 3: User-Management asigna roles
    ↓ (publica evento)
Paso 4: Billing-Service crea plan free
    (Si alguno falla, hay rollback implícito)
```

### 3. **Circuit Breaker** (BFF con Resilience4j)
```
Si Project-Service está caído:
BFF → fallback → retorna datos en caché
```

### 4. **CQRS** (Command Query Responsibility Segregation)
```
Commands (mutables):
  - POST /projects → Project-Service
  - POST /scans → Scanner-Orchestrator

Queries (de lectura):
  - GET /bff/dashboard → BFF (agrega múltiples fuentes)
```

### 5. **Asincronía con @Async**
```java
@Async
public void publishEvent() {
    // Se ejecuta en thread pool
    // No bloquea la request
}
```

---

## 🗄️ BASES DE DATOS

### PostgreSQL (OLTP - Transaccional)
```
auth-service/
├─ users (id, email, password_hash, 2fa, roles)
├─ audit_logs (user_id, action, timestamp)
└─ refresh_tokens

tenant-service/
├─ tenants (id, name, plan_id, owner_id)
├─ tenant_limits (max_projects, max_users, max_scans)

project-service/
├─ projects (id, tenant_id, name, owner_id)
├─ domains (id, project_id, url, verified, token)
└─ repositories (id, project_id, url, branch, type)

billing-service/
├─ plans (id, name, price, features)
├─ subscriptions (id, tenant_id, plan_id, status)
└─ payments (id, subscription_id, amount, gateway, status)

scanner-orchestrator-service/
├─ scans (id, project_id, status, created_at)
└─ scan_logs (id, scan_id, message, severity)

user-management-service/
├─ tenant_members (tenant_id, user_id, role)
└─ project_members (project_id, user_id, role)
```

### MongoDB (OLAP - Análisis)
```
results-aggregator-service/
├─ scan_results
│   {
│     _id: ScanId,
│     projectId: String,
│     timestamp: DateTime,
│     findings: [{
│       id, type, severity, description, file, line
│     }],
│     summary: {
│       critical: 5,
│       high: 10,
│       medium: 20
│     },
│     qualityGateResult: "PASS" | "FAIL"
│   }
```

---

## 🔐 SEGURIDAD

### Autenticación
- JWT (JSON Web Tokens)
- Refresh tokens (corta duración)
- 2FA con Google Authenticator
- OAuth2 para proveedores terceros

### Autorización (RBAC)
```
Roles Global (Auth):
  - SYSTEM_ADMIN (plataforma)

Roles por Tenant:
  - TENANT_ADMIN (gestor de organización)
  - TENANT_USER (usuario regular)

Roles por Proyecto:
  - PROJECT_ADMIN (gestor del proyecto)
  - PROJECT_USER (usuario con acceso)
  - VIEWER (solo lectura)
```

### Rate Limiting
- Auth-Service: Rate limiting en login (protege fuerza bruta)
- Kong: Rate limiting global (100 req/min)

### Encriptación
- Passwords: bcrypt
- Datos sensibles en tránsito: HTTPS/TLS

---

## 📈 ESCALABILIDAD

### Horizontal Scaling
```
- Cada microservicio en contenedor Docker
- Múltiples instancias con load balancer
- RabbitMQ maneja miles de mensajes/segundo

Ejemplo:
  docker-compose up -d --scale project-service=3
```

### Caching
- Tenant-Service: caché de límites en memory
- BFF: caché con fallback si servicios caen

### Bases de Datos
- PostgreSQL: replicas para read-heavy queries
- MongoDB: sharding para resultados masivos

---

## 🚀 DESPLIEGUE

### Docker Compose
```yaml
version: '3.8'
services:
  auth-service:
    image: sentinel/auth-service:latest
    ports: ["8081:8081"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/auth
      
  project-service:
    image: sentinel/project-service:latest
    ports: ["8084:8084"]
    depends_on: [postgres, rabbitmq]
    
  security-gate-service:
    image: sentinel/security-gate-service:latest
    ports: ["5000:5000"]
    
  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]
    
  postgres:
    image: postgres:15
    environment:
      POSTGRES_PASSWORD: password
      
  mongodb:
    image: mongo:6
    ports: ["27017:27017"]
```

---

## 🔍 MONITOREO Y OBSERVABILIDAD

### Logs
- Cada servicio escribe a stdout (Docker logs)
- ELK Stack (Elasticsearch, Logstash, Kibana) opcional

### Métricas
- Spring Boot Actuator → Prometheus
- Kong Admin API para monitoreo de gateway

### Tracing
- Spring Cloud Sleuth (incluido en project-service)
- Zipkin para distributed tracing

### Health Checks
```
GET /actuator/health → Todos los servicios
GET /api/health → Health custom
```

---

## 📝 FLUJOS ADICIONALES

### Cambiar de Plan (Billing)
```
Usuario → POST /api/billing/upgrade
    ↓
Billing-Service
    ├─► Valida pago con gateway
    ├─► Crea nueva suscripción
    ├─► Publica: billing.subscription.upgraded
    └─► RabbitMQ
        │
        ├─► Tenant-Service (consume)
        │   └─► Actualiza plan y límites
        │
        └─► Auth-Service (consume)
            └─► Actualiza features del usuario
```

### Verificación de Dominio
```
Usuario → POST /api/projects/{id}/domains
Body: { domainUrl: "example.com" }
    ↓
Project-Service
    ├─► Crea registro de dominio
    ├─► Publica: domain.added
    └─► RabbitMQ
        │
        └─► C# Domain-Verification-Service
            ├─► Envía email con token
            ├─► Espera verificación (DNS o email)
            ├─► Publica: domain.verified (de vuelta a Java)
            └─► RabbitMQ
                │
                └─► Project-Service (consume)
                    └─► Marca como verificado
```

---

## 🎯 RESUMEN EJECUTIVO

**Sentinel es:**
- ✅ Plataforma SaaS modular y escalable
- ✅ Event-driven con RabbitMQ (desacoplada)
- ✅ Multi-tenant con límites de recursos
- ✅ Segura (JWT, 2FA, RBAC)
- ✅ Extensible (fácil agregar nuevos servicios)
- ✅ Polyglot (Java + .NET)
- ✅ Automatizada (workflows con n8n)
- ✅ Resiliente (circuit breaker, retry, async)

**Principales Innovaciones:**
1. Event-driven entre equipos (Java ↔ .NET)
2. Quality gates automáticos
3. Multi-gateway de pago (Fiat + Crypto)
4. Orquestación flexible con n8n
5. BFF para agregación de datos

---

**Documento Generado**: 2025-12-12  
**Versión**: 1.0
