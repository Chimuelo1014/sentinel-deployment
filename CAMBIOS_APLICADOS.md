# 📝 Cambios Aplicados - Java ↔ C# Integration (12 de Diciembre 2025)

## Resumen
Integración completa de mensajería RabbitMQ entre servicios Java (Scanner Orchestrator, Results Aggregator) y C# (.NET 8) para permitir que las solicitudes de escaneo fluyan desde Java → C# y los resultados regresen → Java.

---

## Archivos Modificados

### 1. **scaner-orchestrator-service** (Java)

#### `src/main/java/com/sentinel/scaner_orchestrator_service/controller/InternalScanController.java`
**Cambio**: Corregir referencias a enum `ScanStatus`
```java
// ANTES:
if (s == ScanStatus.IN_PROGRESS) job.setStartedAt(LocalDateTime.now());
if (s == ScanStatus.DONE || s == ScanStatus.FAILED) job.setFinishedAt(LocalDateTime.now());

// DESPUÉS:
if (s == ScanStatus.RUNNING) job.setStartedAt(LocalDateTime.now());
if (s == ScanStatus.COMPLETED || s == ScanStatus.FAILED) job.setFinishedAt(LocalDateTime.now());
```
**Razón**: El enum `ScanStatus` define `RUNNING` y `COMPLETED`, no `IN_PROGRESS` y `DONE`.

#### `src/main/resources/application.properties`
**Cambio**: Actualizar exchange de RabbitMQ
```properties
# ANTES:
app.rabbitmq.exchange.scan=sentinel.scan.exchange

# DESPUÉS:
app.rabbitmq.exchange.scan=sentinel.scan.requests
```
**Razón**: Alineación con convención de nombres (requests vs results exchanges).

---

### 2. **Sentinel.SeurityGate.Service** (C# .NET 8)

#### `appsettings.json`
**Cambios**:
1. Corregir URL del ScannerOrchestrator (puerto 8086, no 8087)
```json
"ScannerOrchestrator": {
  "BaseUrl": "http://scanner-orchestrator-service:8086"
}
```

2. Verificar configuración RabbitMQ (ya correcta)
```json
"ScanRequestExchange": "sentinel.scan.requests",
"ScanResultExchange": "sentinel.scan.results"
```

#### `Services/RabbitMqService.cs`
**Cambios principales**:
1. **Cambiar tipo de exchange para resultados**:
   ```csharp
   // ANTES: ExchangeType.Fanout
   // DESPUÉS: ExchangeType.Topic
   _channel.ExchangeDeclare(
       exchange: _config.ScanResultExchange,
       type: ExchangeType.Topic,  // ← Cambio crítico
       durable: true,
       autoDelete: false);
   ```

2. **Corregir binding del queue de resultados**:
   ```csharp
   // ANTES: routingKey: ""
   // DESPUÉS: routingKey: "scan.*.*"
   _channel.QueueBind(
       queue: _config.ScanResultQueue,
       exchange: _config.ScanResultExchange,
       routingKey: "scan.*.*");  // ← Ahora recibe todos los resultados
   ```

3. **Agregar método para escuchar requests**:
   ```csharp
   public void StartListeningForRequests(Func<string, Task> messageHandler)
   {
       // Consume mensajes de scan.requested
       // Ejecuta el handler (típicamente ProcessMessageAsync)
   }
   ```

4. **Modificar PublishScanResultAsync para inferir routing key**:
   ```csharp
   // Intenta extraer tipo de scan del JSON payload
   // Publica a routing key "scan.{type}.completed"
   // Ejemplo: scan.sast.completed, scan.codeQuality.completed
   ```

#### `BackgroundServices/ScanRequestListener.cs` (NUEVO)
**Propósito**: Escuchar requests de Java Orchestrator y desencadenar workflows
```csharp
public class ScanRequestListener : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        // 1. Obtener IRabbitMqService
        // 2. Llamar StartListeningForRequests()
        // 3. En callback: parsear JSON, mapear a ScanCommandDto
        // 4. Llamar IScanOrchestrator.StartScanWorkflowAsync(cmd)
    }
}
```
**Registrado en**: `Program.cs`
```csharp
builder.Services.AddHostedService<ScanRequestListener>();
```

#### `BackgroundServices/ScanResultListener.cs` (NUEVO)
**Propósito**: Escuchar resultados de escaneos y procesarlos
```csharp
public class ScanResultListener : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        // 1. Obtener IRabbitMqService
        // 2. Llamar StartListeningForResults()
        // 3. En callback: deserializar a ScanResult
        // 4. Procesar: guardar en BD, notificar a Java BFF, etc.
    }
}
```

#### `Models/ScanResult.cs`
**Cambio**: Hacer campos más flexibles
```csharp
// ANTES:
public required string ScanType { get; set; }
public required string Target { get; set; }
public required string ClientId { get; set; }

// DESPUÉS:
public string? ScanType { get; set; }
public string? Target { get; set; }
public string? ClientId { get; set; }
```
**Razón**: Permitir que payloads parciales de diferentes tipos de scanners (SAST, DAST, CodeQuality, etc.) se deserialicen sin errores.

---

### 3. **Sentinel.CodeQuality.Service** (C#)

#### `appsettings.json` (Actualizado)
```json
"RabbitMQ": {
  "HostName": "localhost",
  "Port": 5672,
  "UserName": "guest",
  "Password": "guest",
  "VirtualHost": "/",
  "Exchange": "sentinel.scan.results",
  "RoutingKey": "scan.codeQuality.completed",
  "QualityGateQueue": "codeQuality.scan.results.queue"
}
```

#### `Publishers/ReportPublisher.cs` (Sin cambios, ya funciona)
- Publica a `RabbitMQ:Exchange` con `RabbitMQ:RoutingKey`
- Publisher confirms habilitados
- Retry/backoff implementados

---

### 4. **Sentinel.Vulnerability.Service** (C#)

#### `appsettings.json` (Actualizado)
```json
"RabbitMQ": {
  "HostName": "localhost",
  "Port": 5672,
  "UserName": "guest",
  "Password": "guest",
  "VirtualHost": "/",
  "Exchange": "sentinel.scan.results",
  "RoutingKey": "scan.vulnerability.completed",
  "VulnerabilityQueue": "vulnerability.scan.results.queue"
}
```

---

### 5. **results-aggregator-service** (Java)

#### `src/main/resources/application.properties` (Actualizado)
```properties
# ANTES:
app.rabbitmq.exchange.scan=sentinel.scan.exchange
app.rabbitmq.routing-key.scan-completed=scan.results.aggregator

# DESPUÉS:
app.rabbitmq.exchange.scan=sentinel.scan.results
app.rabbitmq.routing-key.scan-completed=scan.*.completed
app.rabbitmq.routing-key.scan-failed=scan.*.failed
```

---

## Archivos Nuevos Creados

### 1. `FLUJOS_VALIDADOS.md` (Este repositorio)
Documentación completa de todos los flujos validados y sus estados.

### 2. `CAMBIOS_APLICADOS.md` (Este archivo)
Detalle de cambios aplicados para fácil referencia.

---

## RabbitMQ Setup (Manual)

Si RabbitMQ no tienes exchanges/queues creados, ejecuta:

```bash
#!/bin/bash
BASE_URL="http://localhost:15672/api"
AUTH="-u guest:guest"

# Crear exchanges
curl $AUTH -X PUT -H "Content-Type: application/json" \
  -d '{"type":"topic","durable":true}' \
  "$BASE_URL/exchanges/%2F/sentinel.scan.requests"

curl $AUTH -X PUT -H "Content-Type: application/json" \
  -d '{"type":"topic","durable":true}' \
  "$BASE_URL/exchanges/%2F/sentinel.scan.results"

# Crear queues
for queue in security-gate.scan.requests.queue security-gate.scan.results.queue scan.results.aggregator; do
  curl $AUTH -X PUT -H "Content-Type: application/json" \
    -d '{"durable":true}' \
    "$BASE_URL/queues/%2F/$queue"
done

# Crear bindings
curl $AUTH -X POST -H "Content-Type: application/json" \
  -d '{"routing_key":"scan.*","arguments":{}}' \
  "$BASE_URL/bindings/%2F/e/sentinel.scan.requests/q/security-gate.scan.requests.queue"

curl $AUTH -X POST -H "Content-Type: application/json" \
  -d '{"routing_key":"scan.*.*","arguments":{}}' \
  "$BASE_URL/bindings/%2F/e/sentinel.scan.results/q/security-gate.scan.results.queue"

curl $AUTH -X POST -H "Content-Type: application/json" \
  -d '{"routing_key":"scan.*.completed","arguments":{}}' \
  "$BASE_URL/bindings/%2F/e/sentinel.scan.results/q/scan.results.aggregator"
```

---

## Verificación Post-Deploy

```bash
# 1. Verificar RabbitMQ exchanges
curl -u guest:guest http://localhost:15672/api/exchanges/%2F | jq '.[] | .name'

# 2. Verificar queues
curl -u guest:guest http://localhost:15672/api/queues/%2F | jq '.[] | {name, messages_ready, consumers}'

# 3. Comprobar bindings
curl -u guest:guest http://localhost:15672/api/bindings | jq '.[] | select(.source | contains("sentinel")) | {source, destination, routing_key}'

# 4. Test rápido (publicar mensaje)
curl -u guest:guest -X POST \
  -H "Content-Type: application/json" \
  -d '{"properties":{},"routing_key":"scan.requested","payload":"{\"scanId\":\"test-id\",\"requestedService\":\"SAST\",\"targetRepo\":\"https://github.com/test/repo\"}","payload_encoding":"string"}' \
  http://localhost:15672/api/exchanges/%2F/sentinel.scan.requests/publish
```

---

## Notas de Importancia

1. **Convención de Naming**:
   - Requests: `sentinel.scan.requests` (Topic)
   - Results: `sentinel.scan.results` (Topic)
   - Routing keys: `scan.{type}.{status}` (ej: `scan.sast.completed`, `scan.codeQuality.completed`)

2. **Type Safety**:
   - C# services usan Topic exchanges para routing por tipo/status
   - Java aggregator se suscribe con patrón `scan.*.completed` para recibir todos

3. **Flexibility**:
   - ScanResult model ahora permite campos nulos para soportar diferentes tipos de scanners
   - Routing key inference en PublishScanResultAsync hace que cada tipo de scanner publique automáticamente a su routing key

4. **Error Handling**:
   - Background services implementan re-try/nack de mensajes en caso de error
   - Publisher confirms en ReportPublisher aseguran entrega

---

## Siguiente Fase: n8n + IA

Ahora que la infraestructura RabbitMQ está lista:

1. **Implementar n8n workflows** para ejecutar scans reales (Semgrep, ZAP, etc.)
2. **Integrar modelos IA** para análisis y recommendations
3. **Extender payloads** con metadata IA (scores, risks, etc.)
4. **Testing de carga** y manejo de fallos en producción

