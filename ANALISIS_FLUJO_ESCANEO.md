# Análisis Completo del Flujo de Escaneo en Sentinel

**Fecha de Análisis:** 12 de diciembre de 2025  
**Analista:** GitHub Copilot

---

## 📊 RESUMEN EJECUTIVO

El proyecto Sentinel implementa un flujo de escaneo distribuido con **5 componentes principales** que se comunican a través de **RabbitMQ y webhooks HTTP**. El flujo está bien estructurado pero tiene **varios problemas críticos de integración y configuración** que revierto en este documento.

---

## 🏗️ ARQUITECTURA DEL FLUJO

```
┌─────────────────────────────────────────────────────────────────┐
│                    FLUJO DE ESCANEO SENTINEL                     │
└─────────────────────────────────────────────────────────────────┘

1. INICIO: Cliente → Scanner-Orchestrator-Service
   ├─ POST /api/scans
   ├─ Headers: X-Tenant-Id, X-User-Id
   └─ Payload: ScanRequestDTO
      ├─ type (SAST, DAST, etc)
      ├─ targetUrl
      ├─ targetRepo
      └─ gitToken

2. PUBLICAR EVENTO: Scanner-Orchestrator-Service → RabbitMQ
   ├─ Crea ScanJob (DB PostgreSQL)
   ├─ Publica ScanEventDTO
   ├─ Exchange: sentinel.scan.exchange
   ├─ Routing Key: scan.requested
   └─ Datos: scanId, type, targetUrl, targetRepo, tenantId, gitToken

3. WEBHOOK INMEDIATO: Security-Gate-Service escucha y llama n8n
   ├─ HttpScanOrchestrator.StartScanWorkflowAsync()
   ├─ POST http://n8n:5678/webhook/...
   ├─ Payload según tipo:
   │  ├─ SAST/FULL_SAST → CreateSastPayload()
   │  └─ DAST/DAST_BASIC → CreateDastPayload()
   └─ n8n inicia ejecutables (semgrep, trivy, zap, etc)

4. RESULTADOS PARCIALES: n8n → Code-Quality-Service & Vulnerability-Service
   ├─ Code-Quality-Service recibe resultados de Semgrep
   │  ├─ POST /api/n8n/semgrep/result-ready
   │  ├─ Payload: SemgrepNotificationDto
   │  │  ├─ scanId
   │  │  ├─ filePath (volumen compartido)
   │  │  ├─ repository, branch
   │  │  └─ timestamp
   │  ├─ Procesa:
   │  │  ├─ Lee archivo del volumen
   │  │  ├─ Mapea SemgrepRawOutput → ScanResult
   │  │  ├─ Evalúa QualityGate
   │  │  └─ Publica ScanFinalResultDto
   │  └─ Exchange: scan.results
   │
   └─ Vulnerability-Service recibe resultados de Trivy/ZAP
      ├─ POST /api/v1/n8n/vulnerability-ready
      ├─ Payload: VulnerabilityNotificationDto
      │  ├─ scanId
      │  ├─ projectName
      │  ├─ tool (TRIVY|ZAP)
      │  └─ filePath
      ├─ Procesa:
      │  ├─ Lee archivo
      │  ├─ Mapea según herramienta
      │  ├─ Agrega resultados parciales
      │  └─ Publica ScanFinalResultDto
      └─ Exchange: scan.results

5. AGREGACIÓN FINAL: Results-Aggregator-Service consume resultados
   ├─ Escucha RabbitMQ
   ├─ Queue: scan.results.aggregator
   ├─ Recibe ScanFinalResultDto
   ├─ Procesa:
   │  ├─ Guarda en MongoDB (ScanResult)
   │  ├─ Actualiza estado en Orchestrator
   │  └─ Notifica Security-Gate-Service
   └─ Security-Gate-Service actualiza UI/BD

```

---

## 📝 ANÁLISIS POR COMPONENTE

### 1️⃣ **Scanner-Orchestrator-Service (Java) - Puerto 8086**

#### ✅ LO QUE FUNCIONA BIEN:

```java
// ScanController.java
@PostMapping
public ResponseEntity<ScanResponseDTO> createScan(
    @Valid @RequestBody ScanRequestDTO request,
    @RequestHeader("X-Tenant-Id") UUID tenantId,
    @RequestHeader("X-User-Id") UUID userId)
```
- ✅ Validación de tenant via TenantClient (Feign)
- ✅ Creación de ScanJob con estado PENDING
- ✅ Publicación inmediata de evento ScanEventDTO via RabbitMQ
- ✅ Endpoints de listado y consulta de escaneos

#### 🔧 CONFIGURACIÓN:

```properties
# application.properties
app.rabbitmq.exchange.scan=sentinel.scan.exchange
app.rabbitmq.routing-key.scan-requested=scan.requested
app.services.tenant-url=http://localhost:8082
```

#### ⚠️ PROBLEMAS IDENTIFICADOS:

| Problema | Severidad | Detalles |
|----------|-----------|---------|
| **N8n no se dispara automáticamente** | 🔴 CRÍTICA | No hay consumidor del evento `scan.requested` en esta aplicación. El evento se publica pero nadie lo consume. |
| **No hay listener de ScanListener.java** | 🔴 CRÍTICA | Existe el archivo `ScanListener.java` pero no está siendo usado ni registrado. |
| **Sin validación de permisos** | 🟡 MEDIA | No verifica que el usuario pertenezca al tenant. |
| **PostgreSQL port mismatch** | 🟡 MEDIA | Configurado `localhost:5432` pero Docker usa `postgres:5432` |

---

### 2️⃣ **Security-Gate-Service (C#) - Puerto 5000**

#### ✅ LO QUE FUNCIONA BIEN:

```csharp
// HttpScanOrchestrator.cs
public async Task<Guid> StartScanWorkflowAsync(ScanCommandDto command)
{
    var scanId = command.ScanId != Guid.Empty ? command.ScanId : Guid.NewGuid();
    var n8nWebhookUrl = GetN8nWebhookUrl(command.ScanType);
    
    object payload = command.ScanType.ToUpperInvariant() switch
    {
        "SAST" or "FULL_SAST" => CreateSastPayload(scanId, command),
        "DAST" or "DAST_BASIC" => CreateDastPayload(scanId, command),
        _ => CreateGenericPayload(scanId, command)
    };
    
    await httpClient.PostAsync(n8nWebhookUrl, jsonContent);
}
```
- ✅ Dispara workflows en n8n con payload específico por tipo de scan
- ✅ Configurable para múltiples tipos (SAST, DAST, Port Scan, Secret Scan)
- ✅ RabbitMqService bien estructurado para comunicación
- ✅ Listener en background para resultados

#### 🔧 CONFIGURACIÓN:

```json
// appsettings.json
"RabbitMQ": {
  "ScanRequestExchange": "sentinel.scan.requests",
  "ScanResultExchange": "sentinel.scan.results",
  "SastRoutingKey": "scan.sast",
  "DastRoutingKey": "scan.dast"
},
"N8n": {
  "BaseUrl": "http://localhost:5678"
}
```

#### ⚠️ PROBLEMAS IDENTIFICADOS:

| Problema | Severidad | Detalles |
|----------|-----------|---------|
| **No consume de scanner-orchestrator-service** | 🔴 CRÍTICA | Security-Gate no tiene conexión con Scanner-Orchestrator. El evento `scan.requested` nunca llega a Security-Gate. |
| **RabbitMQ exchanges mal mapeados** | 🔴 CRÍTICA | `ScanRequestExchange: "sentinel.scan.requests"` pero Scanner-Orchestrator publica en `"sentinel.scan.exchange"` |
| **Falta conexión con Code-Quality/Vulnerability services** | 🟡 MEDIA | No hay feedback loop para actualizar estado de escaneos |
| **HealthCheck sin validaciones reales** | 🟡 MEDIA | No verifica RabbitMQ, DB, n8n disponibilidad |

---

### 3️⃣ **Code-Quality-Service (C#) - Puerto 5001**

#### ✅ LO QUE FUNCIONA BIEN:

```csharp
// N8nNotificationController.cs
[HttpPost("semgrep/result-ready")]
public async Task<IActionResult> OnSemgrepResultReady(
    [FromBody] SemgrepNotificationDto dto)
{
    // 1. Leer el contenido
    var raw = await _reader.ReadAsync<SemgrepRawOutput>(normalizedRequested);
    
    // 2. Mapear
    var scanResult = _mapper.Map(raw, dto.ScanId);
    
    // 3. Evaluar QG
    var finalDecision = _evaluator.Evaluate(scanResult);
    
    // 4. Publicar
    await _publisher.PublishFinalResultAsync(finalDecision);
}
```
- ✅ Recibe webhooks de n8n correctamente
- ✅ Validación de path traversal (seguridad)
- ✅ Pipeline claro: leer → mapear → evaluar → publicar
- ✅ QualityGateEvaluator con reglas de negocio
- ✅ RabbitMQ publisher con reintentos y fallback

#### 🔧 CONFIGURACIÓN:

```json
// appsettings.json
"RabbitMQ": {
  "Host": "rabbitmq",
  "Exchange": "scan.results",
  "RoutingKey": "scan.result.final"
}
```

#### ⚠️ PROBLEMAS IDENTIFICADOS:

| Problema | Severidad | Detalles |
|----------|-----------|---------|
| **Falta DTOs para Response** | 🟡 MEDIA | No retorna estado de scan al cliente de forma consistente |
| **Path del volumen hardcodeado** | 🟡 MEDIA | `/mnt/semgrep/results` podría variar en staging/prod |
| **Sin rollback si RabbitMQ falla** | 🟡 MEDIA | Si publica falla tras 3 intentos, el resultado se pierde |
| **No se persisten resultados parciales** | 🟡 MEDIA | Si el servicio cae, se pierden datos procesados |

---

### 4️⃣ **Vulnerability-Service (C#) - Puerto 5002**

#### ✅ LO QUE FUNCIONA BIEN:

```csharp
// N8nNotificationController.cs
[HttpPost("vulnerability-ready")]
public async Task<IActionResult> Receive(
    [FromBody] VulnerabilityNotificationDto dto)
{
    var findings = dto.Tool.ToUpper() switch
    {
        "TRIVY" => _trivyMapper.Map(...),
        "ZAP" => _zapMapper.Map(...),
        _ => throw new Exception("Unknown tool")
    };
    
    var aggregated = _aggregator.AddPartialResult(
        dto.ScanId, dto.ProjectName, findings);
    
    await _publisher.PublishFinalResultAsync(...);
}
```
- ✅ Mapea múltiples formatos (Trivy, ZAP)
- ✅ Agrega resultados parciales correctamente
- ✅ Publica a RabbitMQ
- ✅ Manejo de severidades (HIGH/MEDIUM/LOW)

#### ⚠️ PROBLEMAS IDENTIFICADOS:

| Problema | Severidad | Detalles |
|----------|-----------|---------|
| **appsettings.json incompleto** | 🔴 CRÍTICA | No tiene configuración de RabbitMQ |
| **Sin configuración de Exchange/Queue** | 🔴 CRÍTICA | No publica resultados a RabbitMQ |
| **ResultAggregator nunca completa** | 🟡 MEDIA | Siempre retorna `Passed: false` (línea 47) |
| **DTOs no matchean con otros servicios** | 🟡 MEDIA | Usa `ScanFinalResultDto` con campos diferentes |

---

### 5️⃣ **Results-Aggregator-Service (Java) - Puerto 8087**

#### ✅ LO QUE FUNCIONA BIEN:

```java
// ScanResultListener.java
@RabbitListener(queues = "${app.rabbitmq.queue.scan-results}")
public void handleScanResult(ScanResultEventDTO event) {
    resultsService.processScanResult(event);
}

// ResultsService.java
public void processScanResult(ScanResultEventDTO event) {
    // 1. Save detailed result in Mongo
    ScanResult result = ScanResult.builder()
        .scanId(event.getScanId())
        .findings(event.getFindings())
        .build();
    
    repository.save(result);
    
    // 2. Update Orchestrator status
    orchestratorClient.updateScanStatus(...);
}
```
- ✅ Listener configurado correctamente
- ✅ Persiste en MongoDB
- ✅ Actualiza estado en Scanner-Orchestrator
- ✅ Manejo de excepciones

#### 🔧 CONFIGURACIÓN:

```properties
# application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/sentinel_results
app.rabbitmq.exchange.scan=sentinel.scan.exchange
app.rabbitmq.queue.scan-results=scan.results.aggregator
app.services.scan-orchestrator-url=http://localhost:8084
```

#### ⚠️ PROBLEMAS IDENTIFICADOS:

| Problema | Severidad | Detalles |
|----------|-----------|---------|
| **No existe cliente HTTP para actualizar Orchestrator** | 🔴 CRÍTICA | `orchestratorClient.updateScanStatus()` no está implementado |
| **MongoDB URL con localhost** | 🟡 MEDIA | Debería ser `mongo:27017` en Docker |
| **Sin DLQ para mensajes fallidos** | 🟡 MEDIA | Mensajes que causan excepción no van a DLQ |

---

## 🔗 ANÁLISIS DE CONEXIONES

### A. FLUJO RabbitMQ

```
┌──────────────────────────────────┐
│ Scanner-Orchestrator-Service     │
│ Publica: scan.requested          │
│ Exchange: sentinel.scan.exchange │
└────────────┬─────────────────────┘
             │
             v
    sentinel.scan.exchange (Topic)
             │
     ┌───────┼───────┐
     │               │
     v               v
┌────────────────┐  ┌──────────────────────┐
│ Security-Gate  │  │ (SIN CONSUMIDOR) ❌  │
│ Busca:         │  │                      │
│ scan.sast,     │  │ Debería consumir     │
│ scan.dast      │  │ scan.requested       │
└────────────────┘  └──────────────────────┘

Problem: Routing keys NO MATCHEAN
         Scanner-Orchestrator publica "scan.requested"
         Security-Gate escucha "scan.sast" / "scan.dast"
         
         ❌ DESCONEXIÓN CRÍTICA
```

### B. FLUJO DE RESULTADOS

```
Code-Quality-Service               Vulnerability-Service
      ↓ Publica                              ↓ Publica
      │ ScanFinalResultDto                  │ ScanFinalResultDto
      └────────────────────────────────────┘
                  ↓
         RabbitMQ (scan.results)
                  ↓
         Results-Aggregator-Service
                  ↓
         MongoDB + Update Orchestrator
                  ↓
         ❌ Orchestrator no tiene endpoint
            para recibir actualizaciones
```

---

## 🚨 PROBLEMAS CRÍTICOS ENCONTRADOS

### 🔴 TIER 1: BLOQUEADORES (Deben fixearse inmediatamente)

#### 1. **Security-Gate no recibe eventos de escaneo**
```
CAUSA: Desalineación de RabbitMQ exchanges y routing keys

Scanner-Orchestrator:
├─ Exchange: "sentinel.scan.exchange"
└─ Routing Key: "scan.requested"

Security-Gate:
├─ Exchange: "sentinel.scan.requests" ❌ DIFERENTE
├─ SastRoutingKey: "scan.sast" ❌ DIFERENTE
└─ DastRoutingKey: "scan.dast" ❌ DIFERENTE

IMPACTO: N8N NUNCA SE DISPARA
```

#### 2. **Vulnerability-Service no está configurado**
```
CAUSA: appsettings.json incompleto

FALTA:
- RabbitMQ Host/Port/Credentials
- Exchange y Routing Key
- MongoDB connection string (si lo necesita)

IMPACTO: No publica resultados a RabbitMQ
```

#### 3. **Results-Aggregator no puede actualizar Orchestrator**
```
CAUSA: Endpoint no existe en Scanner-Orchestrator

Código en ResultsService.java:
orchestratorClient.updateScanStatus(event.getScanId(), ...)

PROBLEMA: ScanController no tiene:
- PUT /api/scans/{id}/status
- PATCH /api/scans/{id}

IMPACTO: Escaneos quedan en estado PENDING para siempre
```

---

### 🟡 TIER 2: PROBLEMAS DE CONFIGURACIÓN

#### 4. **Hosts mapeados a localhost en Docker**
```
Archivos afectados:
- Scanner-Orchestrator: PostgreSQL → localhost:5432
- Results-Aggregator: MongoDB → localhost:27017
- Security-Gate: N8n → localhost:5678

DEBERÍA SER:
- PostgreSQL → postgres:5432
- MongoDB → mongo:27017
- N8n → n8n:5678

IMPACTO: Servicios no se comunican en Docker Compose
```

#### 5. **Rutas de volúmenes mapeadas a hard-paths**
```
Code-Quality-Service:
_allowedBasePath = "/mnt/semgrep/results"

PROBLEMA: No es configurable
DEBERÍA: Leer de appsettings.json

IMPACTO: No funciona si cambia la estructura de volúmenes
```

---

### 🟣 TIER 3: PROBLEMAS DE ARQUITECTURA

#### 6. **Sin compensating transactions**
```
Si un paso falla:
Scan → RabbitMQ (OK) → N8n (FALLA) → ¿Quién notifica al usuario?

FALTA:
- DLQ (Dead Letter Queue)
- Retry policies configurables
- Compensating actions para fallos
```

#### 7. **Sin correlación de resultados parciales**
```
N8n puede enviar:
- Semgrep primero
- Trivy segundo
- ZAP tercero

FALTA:
- Mecanismo para agregar todos antes de marcar como "COMPLETO"
- Timeout si falta alguno
- Notification para resultados parciales
```

#### 8. **Falta persistencia en estadios intermedios**
```
Si Code-Quality-Service crashea DESPUÉS de procesar pero ANTES de publicar:
- El resultado de Semgrep se pierde
- No hay replay posible

FALTA:
- Guardar resultados procesados en BD temporal
- CDC (Change Data Capture) para auditoría
```

---

## 📋 TABLA DE CONFIGURACIONES

### RabbitMQ Configuration Mismatch

| Servicio | Exchange | Routing Key | Queue | Estado |
|----------|----------|-------------|-------|--------|
| Scanner-Orchestrator (publica) | `sentinel.scan.exchange` | `scan.requested` | - | ✅ |
| Security-Gate (consume) | `sentinel.scan.requests` | - | - | ❌ Desalineado |
| Code-Quality (publica) | `scan.results` | `scan.result.final` | - | ✅ |
| Vulnerability (publica) | ❌ SIN CONFIG | - | - | 🔴 Falta |
| Results-Aggregator (consume) | `sentinel.scan.exchange` | - | `scan.results.aggregator` | ✅ |

### Database Configurations

| Servicio | Tipo | Host Configurado | Debería ser | Estado |
|----------|------|------------------|-------------|--------|
| Scanner-Orchestrator | PostgreSQL | `localhost:5432` | `postgres:5432` | ⚠️ |
| Results-Aggregator | MongoDB | `localhost:27017` | `mongo:27017` | ⚠️ |
| Security-Gate | SQL Server | `localhost` | `sqlserver:1433` | ⚠️ |

---

## ✅ DIAGRAMA DE ESTADO DEL SCAN

```
┌─────────────────────────────────────────────────────────┐
│              CICLO DE VIDA DEL ESCANEO                  │
└─────────────────────────────────────────────────────────┘

1. CREATED (Scanner-Orchestrator)
   │
   ├─ createScan()
   ├─ ScanJob.status = PENDING
   └─ Publica ScanEventDTO

2. IN_PROGRESS (n8n ejecuta)
   │
   ├─ N8n recibe webhook
   ├─ Ejecuta semgrep/trivy/zap
   └─ Descarga resultados a volumen

3. PROCESSING_RESULTS (Code-Quality & Vulnerability)
   │
   ├─ Reciben webhooks de n8n
   ├─ Leen archivos de volumen
   ├─ Mapean a formato unificado
   ├─ Evalúan quality gates
   └─ Publican ScanFinalResultDto

4. COMPLETED (Results-Aggregator)
   │
   ├─ Consume ScanFinalResultDto
   ├─ Guarda en MongoDB
   └─ ❌ NUNCA ACTUALIZA Scanner-Orchestrator
       (falta endpoint)

5. FAILED (Si ocurre error)
   │
   └─ ❌ SIN MECANISMO DE ERROR HANDLING
```

---

## 🎯 RECOMENDACIONES POR PRIORIDAD

### P0: CRÍTICA (Hacer hoy)

- [ ] **Alinear RabbitMQ configuration**
  - [ ] Cambiar Security-Gate exchange a `sentinel.scan.exchange`
  - [ ] Cambiar routing keys a `scan.requested`
  - [ ] Testear flujo completo

- [ ] **Implementar endpoint de actualización en Scanner-Orchestrator**
  ```java
  @PatchMapping("/{id}/status")
  public ResponseEntity<ScanResponseDTO> updateScanStatus(
      @PathVariable UUID id,
      @RequestBody UpdateScanStatusRequest request)
  ```

- [ ] **Completar configuración de Vulnerability-Service**
  - [ ] Agregar RabbitMQ config a appsettings.json
  - [ ] Implementar publisher

### P1: ALTA (Esta semana)

- [ ] Cambiar hosts de localhost a service names en Docker
- [ ] Implementar DLQ para mensajes fallidos
- [ ] Agregar compensating transactions para fallos

### P2: MEDIA (Próximas 2 semanas)

- [ ] Agregar mecanismo de timeout para resultados parciales
- [ ] Implementar persistencia en estadios intermedios
- [ ] Mejorar validaciones de seguridad

---

## 📦 ARCHIVOS ENCONTRADOS

### Scanner-Orchestrator-Service
```
src/main/java/com/sentinel/scaner_orchestrator_service/
├── controller/
│   └── ScanController.java ✅
├── service/
│   └── ScanService.java ✅
├── messaging/
│   ├── ScanPublisher.java ✅
│   └── ScanListener.java ❌ (NO USADO)
├── dto/
│   ├── message/
│   │   └── ScanEventDTO.java ✅
│   ├── request/
│   │   └── ScanRequestDTO.java ✅
│   └── response/
│       └── ScanResponseDTO.java ✅
└── client/
    └── TenantClient.java ✅
```

### Security-Gate-Service
```
Controllers/
└── HealthCheckController.cs ✅ (Incompleto)

Services/
├── RabbitMqService.cs ✅ (Bien implementado)
├── HttpScanOrchestrator.cs ✅ (Bien implementado)
└── IScanOrchestrator.cs ✅

BackgroundServices/
└── ScanRequestListener.cs ✅ (No consume de Orchestrator)
```

### Code-Quality-Service
```
Controllers/
└── N8nNotificationController.cs ✅

DTOs/
├── SemgrepNotificationDto.cs ✅
└── ScanFinalResultDto.cs ✅

Services/
├── QualityGateEvaluator.cs ✅
├── SemgrepMapper.cs ✅
├── IResultReader.cs ✅
└── VolumeFileReader.cs ✅

Publishers/
├── IReportPublisher.cs ✅
└── ReportPublisher.cs ✅
```

### Vulnerability-Service
```
Controllers/
└── N8nNotificationController.cs ✅

DTOs/
├── VulnerabilityNotificationDto.cs ✅
└── ScanFinalResultDto.cs ⚠️ (Diferente)

Services/
├── IResultReader.cs ✅
├── ResultAggregator.cs ✅
├── TrivyMapper.cs ✅
└── ZapMapper.cs ✅

Publishers/
├── IReportPublisher.cs ✅
└── ReportPublisher.cs ⚠️ (Sin config RabbitMQ)
```

### Results-Aggregator-Service
```
src/main/java/com/sentinel/results_aggregator_service/
├── messaging/
│   └── ScanResultListener.java ✅
├── service/
│   └── ResultsService.java ⚠️ (Falta updateScanStatus)
├── repository/
│   └── ScanResultRepository.java ✅
└── dto/
    └── ScanResultEventDTO.java ✅
```

---

## 🔍 QUE ESTÁ FALTANDO O MAL CONECTADO

### Matriz de Conectividad

```
┌────────────────────┬──────────────┬─────────────────────────────┐
│ Origen             │ Destino      │ Estado                      │
├────────────────────┼──────────────┼─────────────────────────────┤
│ Scanner-Orchestr.  │ Security-Gate│ ❌ Config desalineada       │
│ Scanner-Orchestr.  │ n8n          │ ❌ No hay conexión directa  │
│ Security-Gate      │ n8n          │ ✅ Bien (via HttpScanOrch.) │
│ n8n                │ Code-Quality │ ✅ Webhooks OK              │
│ n8n                │ Vulnerability│ ✅ Webhooks OK              │
│ Code-Quality       │ RabbitMQ     │ ✅ Publica OK               │
│ Vulnerability      │ RabbitMQ     │ ❌ Sin config               │
│ Results-Aggregator │ RabbitMQ     │ ✅ Consume OK               │
│ Results-Aggregator │ Scanner-Orch.│ ❌ Endpoint no existe       │
└────────────────────┴──────────────┴─────────────────────────────┘
```

---

## 📊 RESUMEN FINAL

| Métrica | Resultado |
|---------|-----------|
| **Componentes Analizados** | 5 servicios |
| **Problemas Críticos** | 3 bloqueadores |
| **Problemas de Configuración** | 5 ⚠️ |
| **Problemas de Arquitectura** | 3 🔧 |
| **Servicios Funcionales** | 2/5 (40%) |
| **Flujo Completo Operativo** | ❌ NO |

### 🎬 SIGUIENTE PASO RECOMENDADO

1. **Hoy:** Fijar problemas P0 (RabbitMQ alignment, endpoints faltantes)
2. **Mañana:** Testear flujo completo de escaneo
3. **Esta semana:** Implementar error handling y retry policies
4. **Próxima semana:** Mejorar seguridad y observabilidad

---

*Este análisis fue generado automáticamente el 12 de diciembre de 2025.*
*Para preguntas técnicas específicas, revisar los archivos mencionados en la sección "ARCHIVOS ENCONTRADOS".*
