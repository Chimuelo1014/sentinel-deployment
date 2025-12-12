# 📚 ÍNDICE DE DOCUMENTACIÓN - SENTINEL PROJECT

**Última actualización**: 12 de Diciembre 2025

---

## 🎯 ACCESO RÁPIDO POR ROL

### 👨‍💻 Para Desarrolladores Frontend

**Inicio**: Leer esto primero
1. 📄 `RUTAS_API_FRONTEND.md` - **DOCUMENTO PRINCIPAL PARA FRONTEND**
   - 30+ endpoints documentados
   - Ejemplos completos de request/response
   - Códigos de error
   - Flujo de ejemplo end-to-end

2. 📄 `REVISION_COMPLETA_PROYECTO.md` - Referencia arquitectura
   - Cómo funcionan los flujos internos
   - Qué datos esperar
   - Headers y convenciones

3. 🔗 Variables de entorno a usar:
   ```javascript
   API_BASE = http://localhost:8086/api  // desarrollo
   API_BASE = http://backend-for-frontend-service:8086/api  // docker
   ```

---

### 👨‍💼 Para Desarrolladores Backend (BFF)

**Prioridad inmediata**: Completar BFF Controllers

1. 📄 `CHECKLIST_EJECUTIVO.md` - **TAREAS A HACER**
   - Fase 2 checklist (BFF Controllers)
   - Endpoints faltantes
   - Servicios a conectar

2. 📄 `REVISION_COMPLETA_PROYECTO.md` - Referencia técnica
   - Sección "Backend for Frontend (BFF)"
   - Controllers a implementar
   - Endpoint signatures esperadas

3. 📄 `RUTAS_API_FRONTEND.md` - Especificación de endpoints
   - Exacto formato de request/response
   - Headers requeridos
   - Códigos de error

4. 🛠️ Stack:
   - Spring Boot 3.4.1
   - FeignClient para inter-service
   - PostgreSQL + MongoDB
   - RabbitMQ para eventos

---

### 🔧 Para DevOps / Infrastructure

**Enfoque**: Infraestructura y deployment

1. 📄 `CAMBIOS_APLICADOS.md` - Setup de RabbitMQ
   - Comandos para crear exchanges/queues
   - Verificación manual
   - Troubleshooting

2. 📄 `FLUJOS_VALIDADOS.md` - Validación de flujos
   - Cómo verificar que todo está conectado
   - Comandos de testing
   - Queue status

3. 📄 `CHECKLIST_EJECUTIVO.md` - Fases de producción
   - Fase 5: Production deployment
   - Security, monitoring, backups
   - Comandos útiles

4. 🐳 Docker:
   - RabbitMQ: `sentinel-rabbitmq`
   - PostgreSQL: Puerto 5432
   - MongoDB: Puerto 27017

---

### 🤖 Para QA / Testing

**Enfoque**: Validación y testing

1. 📄 `FLUJOS_VALIDADOS.md` - Flujos probados
   - 6 flujos documentados
   - Payloads esperados
   - Queue status
   - Log verification

2. 📄 `CHECKLIST_EJECUTIVO.md` - Fase 3 (n8n)
   - Integration testing plan
   - End-to-end testing
   - Performance benchmarks

3. 📄 `RUTAS_API_FRONTEND.md` - API testing
   - Todos los endpoints
   - Códigos de error
   - Ejemplo de flujo completo

---

### 📊 Para Project Managers / Stakeholders

**Enfoque**: Progreso y timeline

1. 📄 `CHECKLIST_EJECUTIVO.md` - **RESUMEN PRINCIPAL**
   - Status de 5 fases
   - Métricas de completitud
   - Próximos pasos
   - Tareas por prioridad

2. 📄 `REVISION_COMPLETA_PROYECTO.md` - Visión general
   - Arquitectura del sistema
   - Componentes implementados
   - Timeline estimado

3. 📊 Métricas clave:
   - Fase 1: ✅ 100% (Java ↔ C#)
   - Fase 2: 🔄 30% (BFF)
   - Fase 3: ⏳ 0% (n8n)
   - Fase 4: ⏳ 0% (IA)
   - Fase 5: ⏳ 0% (Producción)

---

## 📋 ESTRUCTURA DE DOCUMENTOS

```
sentinel/
├─ RUTAS_API_FRONTEND.md ..................... Endpoints para consumir
├─ REVISION_COMPLETA_PROYECTO.md ............ Análisis exhaustivo
├─ CHECKLIST_EJECUTIVO.md ................... Tasks y progreso
├─ README_INTEGRACION.md .................... n8n integration guide
├─ FLUJOS_VALIDADOS.md ...................... Flujos probados
├─ CAMBIOS_APLICADOS.md ..................... Changelog técnico
└─ DOCUMENTACION_INDICE.md .................. Este documento
```

---

## 🔍 BÚSQUEDA RÁPIDA POR TEMA

### Autenticación
- `RUTAS_API_FRONTEND.md` → Sección "AUTENTICACIÓN"

### Dashboard
- `RUTAS_API_FRONTEND.md` → Sección "DASHBOARD"
- `REVISION_COMPLETA_PROYECTO.md` → Backend for Frontend Service

### Escaneos (Requests)
- `RUTAS_API_FRONTEND.md` → Sección "ESCANEOS"
- `FLUJOS_VALIDADOS.md` → Flujo 1: Solicitud de Escaneo

### Resultados de Escaneo
- `RUTAS_API_FRONTEND.md` → GET /bff/scans/{scanId}/results
- `FLUJOS_VALIDADOS.md` → Flujos 2, 3, 4: Resultados

### Proyectos
- `RUTAS_API_FRONTEND.md` → Sección "PROYECTOS"
- `REVISION_COMPLETA_PROYECTO.md` → Project Service

### Analytics
- `RUTAS_API_FRONTEND.md` → Sección "ANALYTICS"
- `REVISION_COMPLETA_PROYECTO.md` → Analytics endpoints

### RabbitMQ / Mensajería
- `CAMBIOS_APLICADOS.md` → RabbitMQ Setup
- `FLUJOS_VALIDADOS.md` → Todos los flujos
- `REVISION_COMPLETA_PROYECTO.md` → Sección "Flujos de Mensajería"

### Multi-tenant
- `REVISION_COMPLETA_PROYECTO.md` → Tenant Service
- `RUTAS_API_FRONTEND.md` → X-Tenant-Id header

### Error Handling
- `RUTAS_API_FRONTEND.md` → Sección "CÓDIGOS DE ERROR"
- `REVISION_COMPLETA_PROYECTO.md` → Error Handling

### n8n Integration
- `README_INTEGRACION.md` → Guía completa de n8n
- `CHECKLIST_EJECUTIVO.md` → Fase 3
- `FLUJOS_VALIDADOS.md` → Cómo se conectan n8n + servicios

### IA/ML
- `CHECKLIST_EJECUTIVO.md` → Fase 4
- `REVISION_COMPLETA_PROYECTO.md` → Próximos Pasos

### Deployment / Producción
- `CHECKLIST_EJECUTIVO.md` → Fase 5
- `CAMBIOS_APLICADOS.md` → Post-Deploy verification

---

## 📞 SERVICIOS Y PUERTOS

| Servicio | Puerto | URL | Docs |
|----------|--------|-----|------|
| Frontend | 3000 | http://localhost:3000 | `RUTAS_API_FRONTEND.md` |
| BFF (Backend) | 8086 | http://localhost:8086/api | `REVISION_COMPLETA_PROYECTO.md` |
| SecurityGate | 5275 | http://localhost:5275 | `REVISION_COMPLETA_PROYECTO.md` |
| CodeQuality | 5001 | http://localhost:5001 | `REVISION_COMPLETA_PROYECTO.md` |
| Vulnerability | 5002 | http://localhost:5002 | `REVISION_COMPLETA_PROYECTO.md` |
| Orchestrator | 8086 | http://localhost:8086 | `REVISION_COMPLETA_PROYECTO.md` |
| Results Aggregator | 8087 | http://localhost:8087 | `REVISION_COMPLETA_PROYECTO.md` |
| Auth Service | 8081 | http://localhost:8081 | `RUTAS_API_FRONTEND.md` |
| Tenant Service | 8082 | http://localhost:8082 | `REVISION_COMPLETA_PROYECTO.md` |
| Project Service | 8083 | http://localhost:8083 | `REVISION_COMPLETA_PROYECTO.md` |
| RabbitMQ | 5672 | amqp://localhost:5672 | `CAMBIOS_APLICADOS.md` |
| RabbitMQ UI | 15672 | http://localhost:15672 | `CAMBIOS_APLICADOS.md` |
| PostgreSQL | 5432 | localhost:5432 | - |
| MongoDB | 27017 | localhost:27017 | - |

---

## 🎯 FLUJOS DE TRABAJO TÍPICOS

### "Necesito desarrollar el frontend"
1. Leer: `RUTAS_API_FRONTEND.md`
2. Usar: Headers y ejemplos
3. Referencia: `REVISION_COMPLETA_PROYECTO.md` si tienes dudas

### "Necesito completar el BFF"
1. Leer: `CHECKLIST_EJECUTIVO.md` → Fase 2
2. Implementar: Usando `RUTAS_API_FRONTEND.md` como spec
3. Conectar: Services según `REVISION_COMPLETA_PROYECTO.md`

### "Necesito configurar n8n"
1. Leer: `README_INTEGRACION.md`
2. Validar: Con `FLUJOS_VALIDADOS.md`
3. Troubleshoot: Con `CAMBIOS_APLICADOS.md`

### "Necesito ver el estado actual"
1. Leer: `CHECKLIST_EJECUTIVO.md` → Sección "STATUS DE COMPONENTES"
2. Validar: Con `FLUJOS_VALIDADOS.md`

### "Necesito desplegar a producción"
1. Leer: `CHECKLIST_EJECUTIVO.md` → Fase 5
2. Preparar: Usando `CAMBIOS_APLICADOS.md`
3. Validar: Con `FLUJOS_VALIDADOS.md`

---

## 📈 Timeline DEL PROYECTO

```
HITO 1: Java ↔ C# Integration
Status: ✅ COMPLETADO (12 Diciembre)
Deliverables: FLUJOS_VALIDADOS.md, CAMBIOS_APLICADOS.md, README_INTEGRACION.md

HITO 2: Backend for Frontend
Status: 🔄 EN DESARROLLO (Próximos 2 días)
Deliverables: BFF Controllers completados
Bloqueador: Crítico para Hito 3

HITO 3: n8n Integration
Status: ⏳ PLANIFICADO (Próximos 3-5 días después de Hito 2)
Deliverables: 4 workflows (Semgrep, ZAP, Trivy, SBOM)
Bloqueador: Crítico para scanning real

HITO 4: Frontend Development
Status: ⏳ PLANIFICADO (Próxima semana)
Requiere: Hito 2 (BFF) completado
Usa: RUTAS_API_FRONTEND.md

HITO 5: IA/ML Integration
Status: ⏳ PLANIFICADO (Después de Hito 3)
Requiere: Resultados de n8n

HITO 6: Production Deployment
Status: ⏳ PLANIFICADO (Final)
Usa: CHECKLIST_EJECUTIVO.md → Fase 5
```

---

## ⚠️ COSAS CRÍTICAS A RECORDAR

1. **RabbitMQ DEBE estar corriendo**
   ```bash
   docker start sentinel-rabbitmq
   ```

2. **n8n es BLOQUEADOR para scanning real**
   - Sin n8n: solo eventos, sin ejecución

3. **BFF completo es BLOQUEADOR para frontend**
   - Sin BFF endpoints: frontend no puede consumir datos

4. **JWT expira en 1 hora**
   - Usar refresh endpoint para nuevo token

5. **X-Tenant-Id es requerido en requests**
   - Incluir en headers para multi-tenant

6. **Todos los endpoints esperan JSON**
   - Header: `Content-Type: application/json`

---

## 🔗 REFERENCIAS CRUZADAS PRINCIPALES

| Concepto | Dónde encontrar |
|----------|-----------------|
| API Endpoints | `RUTAS_API_FRONTEND.md` + `REVISION_COMPLETA_PROYECTO.md` |
| Flujos internos | `FLUJOS_VALIDADOS.md` + `REVISION_COMPLETA_PROYECTO.md` |
| Tareas a hacer | `CHECKLIST_EJECUTIVO.md` |
| Comandos útiles | `CAMBIOS_APLICADOS.md` + `CHECKLIST_EJECUTIVO.md` |
| Troubleshooting | `FLUJOS_VALIDADOS.md` + `CAMBIOS_APLICADOS.md` |
| Arquitectura | `REVISION_COMPLETA_PROYECTO.md` |
| Error codes | `RUTAS_API_FRONTEND.md` |

---

## 📞 SOPORTE RÁPIDO

**¿Cuál es el estado actual?**
→ `CHECKLIST_EJECUTIVO.md` → Sección "STATUS DE COMPONENTES"

**¿Cómo consumo los endpoints desde frontend?**
→ `RUTAS_API_FRONTEND.md` (TODO está aquí)

**¿Cómo configuro RabbitMQ?**
→ `CAMBIOS_APLICADOS.md` → Sección "RabbitMQ Setup"

**¿Cómo hago un endpoint del BFF?**
→ `CHECKLIST_EJECUTIVO.md` → Fase 2 + `RUTAS_API_FRONTEND.md` spec

**¿Cómo verifico que todo funciona?**
→ `FLUJOS_VALIDADOS.md` → Comandos de testing

**¿Cuál es el siguiente paso?**
→ `CHECKLIST_EJECUTIVO.md` → Sección "PRÓXIMOS PASOS INMEDIATOS"

---

## 📝 VERSIÓN DEL DOCUMENTO

- **Versión**: 1.0
- **Fecha**: 12 de Diciembre 2025
- **Status**: En Implementación (Fase 2)
- **Próxima actualización**: Cuando se complete Hito 2 (BFF)

---

**Generado por**: GitHub Copilot  
**Para**: Equipo Sentinel  
**Duración de validez**: Hasta producción (v1.0)
