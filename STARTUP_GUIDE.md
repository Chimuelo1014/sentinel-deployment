# 🚀 GUÍA COMPLETA: Levantar Sentinel con Docker Compose

**Fecha**: 15 de Diciembre 2025  
**Status**: Pasos de levantamiento paso a paso

---

## ✅ PREREQUISITOS

```bash
# Verificar Docker
docker --version
docker-compose --version

# Asegurar estar en el directorio correcto
cd /home/jb/Documentos/sentinel
```

---

## 📋 ORDEN DE LEVANTAMIENTO

### **PASO 1: Infraestructura Base (Bases de Datos + Message Broker)**

```bash
# Iniciar PostgreSQL, MongoDB, RabbitMQ
docker-compose up -d postgres mongodb rabbitmq mailhog

# Verificar que están saludables
docker-compose ps

# Esperar a que pasen health checks (⏱️ ~30-60 segundos)
docker-compose logs postgres | grep "database system is ready"
docker-compose logs mongodb | grep "ready for connections"
docker-compose logs rabbitmq | grep "Server startup complete"
```

**Puertos Disponibles**:
- PostgreSQL: `localhost:5432`
- MongoDB: `localhost:27017`
- RabbitMQ: `localhost:5672` (AMQP)
- RabbitMQ Management: `http://localhost:15672` (guest/guest)
- MailHog: `http://localhost:8025`

---

### **PASO 2: Kong API Gateway + Konga**

```bash
# Iniciar Kong DB
docker-compose up -d kong-db

# Esperar health check
docker-compose logs kong-db | grep "database system is ready"

# Ejecutar migraciones de Kong
docker-compose up kong-migration

# Iniciar Kong + Konga
docker-compose up -d kong konga

# Verificar Kong
curl http://localhost:8001/status
# Expected: {"database":{"reachable":true},...}
```

**Puertos Kong**:
- Kong Proxy: `http://localhost:8000` (API Gateway)
- Kong Admin: `http://localhost:8001` (REST API)
- Kong Manager: `http://localhost:8002` (Web UI)
- Konga: `http://localhost:1337` (Kong Admin UI)

---

### **PASO 3: Servicios Java (Auth, Tenant, Project, Billing, Orchestrator, Aggregator, BFF)**

```bash
# Construir imágenes Docker
docker-compose build auth-service tenant-service project-service billing-service \
  scanner-orchestrator-service results-aggregator-service backend-for-frontend-service

# Iniciar servicios Java
docker-compose up -d auth-service tenant-service project-service billing-service \
  scanner-orchestrator-service results-aggregator-service backend-for-frontend-service

# Verificar que iniciaron correctamente
docker-compose ps
```

**Puertos Java**:
- Auth Service: `http://localhost:8081`
- Tenant Service: `http://localhost:8082`
- Project Service: `http://localhost:8084`
- Billing Service: `http://localhost:8084`
- Scanner Orchestrator: `http://localhost:8086`
- Results Aggregator: `http://localhost:8087`
- **BFF Service**: `http://localhost:8080` ⭐

**Healthchecks** (esperar 1-2 minutos):
```bash
curl http://localhost:8081/api/auth/health
curl http://localhost:8080/api/bff/health
```

---

### **PASO 4: Servicios .NET (SecurityGate, CodeQuality, Vulnerability)**

```bash
# Construir servicios .NET
docker-compose build security-gate-service code-quality-service vulnerability-service

# Iniciar servicios .NET
docker-compose up -d security-gate-service code-quality-service vulnerability-service

# Verificar
docker-compose ps
```

**Puertos .NET**:
- SecurityGate: `http://localhost:5000`
- CodeQuality: `http://localhost:5001`
- Vulnerability: `http://localhost:5002`

---

### **PASO 5: User Management Service**

```bash
# Iniciar
docker-compose up -d user-management-service

# Verificar
docker-compose ps
```

**Puerto**: `http://localhost:8088`

---

## 🔍 VERIFICACIÓN COMPLETA

### **Todos los Servicios Levantados**

```bash
docker-compose ps
```

**Output Esperado**:
```
NAME                              STATUS              PORTS
sentinel-postgres                 Up 2 minutes        0.0.0.0:5432->5432/tcp
sentinel-mongodb                  Up 2 minutes        0.0.0.0:27017->27017/tcp
sentinel-rabbitmq                 Up 2 minutes        0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
sentinel-mailhog                  Up 2 minutes        0.0.0.0:1025->1025/tcp, 0.0.0.0:8025->8025/tcp
sentinel-kong-db                  Up 2 minutes        0.0.0.0:5433->5432/tcp
sentinel-kong                     Up 1 minute         0.0.0.0:8000->8000/tcp, 0.0.0.0:8001->8001/tcp
sentinel-konga                    Up 1 minute         0.0.0.0:1337->1337/tcp
sentinel-auth-service             Up 1 minute         0.0.0.0:8081->8081/tcp
sentinel-tenant-service           Up 1 minute         0.0.0.0:8082->5433/tcp
sentinel-project-service          Up 1 minute         0.0.0.0:8084->8084/tcp
sentinel-scanner-orchestrator     Up 1 minute         0.0.0.0:8086->8086/tcp
sentinel-results-aggregator       Up 1 minute         0.0.0.0:8087->8087/tcp
sentinel-backend-for-frontend     Up 1 minute         0.0.0.0:8080->8080/tcp
sentinel-security-gate-service    Up 1 minute         0.0.0.0:5000->5000/tcp
sentinel-code-quality-service     Up 1 minute         0.0.0.0:5001->5001/tcp
sentinel-vulnerability-service    Up 1 minute         0.0.0.0:5002->5002/tcp
sentinel-user-management-service  Up 1 minute         0.0.0.0:8088->8088/tcp
```

### **Test de Conectividad**

```bash
# Test PostgreSQL
docker-compose exec postgres pg_isready -U sentinel
# Expected: accepting connections

# Test MongoDB
docker-compose exec mongodb mongosh --eval "db.adminCommand('ping')"
# Expected: { ok: 1 }

# Test RabbitMQ
docker-compose exec rabbitmq rabbitmq-diagnostics ping
# Expected: Diagnostic information

# Test Kong Admin
curl http://localhost:8001/status | jq .

# Test BFF Health
curl http://localhost:8080/api/bff/health 2>/dev/null || echo "BFF iniciando..."
```

---

## 📊 Flujos de Integración Java ↔ C# (Validados)

### **Flujo 1: Java → C# (Request)**
```
Scanner-Orchestrator (Java) 
  → RabbitMQ (sentinel.scan.requests) 
  → SecurityGate (C#) 
  → ScanRequestListener consume
```

### **Flujo 2: C# → Java (Result)**
```
CodeQuality/Vulnerability (C#)
  → RabbitMQ (sentinel.scan.results)
  → Results-Aggregator (Java) consume
```

### **Validar Flujos**
```bash
# Ver exchanges y queues
docker-compose exec rabbitmq rabbitmqctl list_exchanges
docker-compose exec rabbitmq rabbitmqctl list_queues

# Ver mensajes en queues
docker-compose exec rabbitmq rabbitmqctl list_queues messages consumers
```

---

## 🛠️ TROUBLESHOOTING

### **Error: "Port already in use"**
```bash
# Encontrar proceso usando puerto
lsof -i :8080
# Matar proceso o cambiar puerto en docker-compose.yml
```

### **Error: "Connection refused"**
```bash
# Verificar que el contenedor está corriendo
docker-compose ps NOMBRE_CONTENEDOR

# Ver logs
docker-compose logs NOMBRE_CONTENEDOR

# Reiniciar
docker-compose restart NOMBRE_CONTENEDOR
```

### **Error: "Network issues"**
```bash
# Verificar que la red existe
docker network ls | grep sentinel

# Recrear red
docker-compose down
docker network rm sentinel_sentinel-network
docker-compose up -d
```

---

## 🧹 LIMPIEZA & RESET

### **Detener Todo (sin eliminar volúmenes)**
```bash
docker-compose down
```

### **Detener y Eliminar Datos**
```bash
docker-compose down -v
# ⚠️ Esto elimina TODAS las bases de datos
```

### **Ver Logs en Tiempo Real**
```bash
# Todos los servicios
docker-compose logs -f

# Un servicio específico
docker-compose logs -f backend-for-frontend-service
docker-compose logs -f security-gate-service
```

---

## 📝 PRÓXIMOS PASOS (Una vez levantado)

1. **Configurar Kong Gateway**
   ```bash
   ./kong-setup.sh
   ```

2. **Test de Integración**
   ```bash
   ./test-integration.sh
   ```

3. **Levantar Frontend** (en otra terminal)
   ```bash
   cd sentinel_front
   npm install
   npm run dev
   ```

4. **Acceder a aplicación**
   - Frontend: `http://localhost:5173`
   - Kong Gateway: `http://localhost:8000`
   - Swagger API: `http://localhost:8080/swagger-ui.html`

---

## 📞 SERVICIOS DISPONIBLES

| Servicio | URL | Username | Password |
|----------|-----|----------|----------|
| Kong Admin | http://localhost:8001 | - | - |
| Konga | http://localhost:1337 | admin | admin |
| RabbitMQ | http://localhost:15672 | guest | guest |
| MailHog | http://localhost:8025 | - | - |
| BFF Swagger | http://localhost:8080/swagger-ui.html | - | - |
| PostgreSQL | localhost:5432 | sentinel | sentinel123 |
| MongoDB | localhost:27017 | sentinel | sentinel123 |

---

✅ **¡Listo!** Una vez completados todos los pasos, tu plataforma Sentinel estará completamente levantada con todos los microservicios Java y C# comunicándose a través de eventos RabbitMQ.
