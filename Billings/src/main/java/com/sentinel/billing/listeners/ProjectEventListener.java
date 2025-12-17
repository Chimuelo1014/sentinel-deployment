package com.sentinel.billing.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener para eventos de proyectos.
 * 
 * Eventos escuchados:
 * - project.created: Se registra para auditoría/estadísticas
 * - project.deleted: Se registra para limpieza
 * 
 * DESACTIVADO TEMPORALMENTE: En desarrollo, estas queues se crean bajo demanda
 */
//@Component
public class ProjectEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProjectEventListener.class);

    //@RabbitListener(queues = "${billing.listeners.project.queue:billing-project-events-queue}")
    public void handleProjectEvent(@Payload Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String projectId = (String) payload.get("projectId");
        String tenantId = (String) payload.get("tenantId");

        log.info("📨 Evento de Proyecto recibido: {} para proyecto {} en tenant {}", 
                eventType, projectId, tenantId);

        switch (eventType) {
            case "project.created" -> handleProjectCreated(payload);
            case "project.deleted" -> handleProjectDeleted(payload);
            case "domain.added" -> handleDomainAdded(payload);
            case "repository.added" -> handleRepositoryAdded(payload);
            default -> log.warn("⚠️ Evento de proyecto desconocido: {}", eventType);
        }
    }

    private void handleProjectCreated(Map<String, Object> payload) {
        String projectId = (String) payload.get("projectId");
        String tenantId = (String) payload.get("tenantId");
        
        log.info("✅ Proyecto creado registrado: projectId={}, tenantId={}", projectId, tenantId);
        // TODO: Registrar en base de datos de auditoría de billing si es necesario
    }

    private void handleProjectDeleted(Map<String, Object> payload) {
        String projectId = (String) payload.get("projectId");
        String tenantId = (String) payload.get("tenantId");
        
        log.info("✅ Proyecto eliminado registrado: projectId={}, tenantId={}", projectId, tenantId);
        // TODO: Limpiar registros asociados si es necesario
    }

    private void handleDomainAdded(Map<String, Object> payload) {
        String projectId = (String) payload.get("projectId");
        String domain = (String) payload.get("domain");
        
        log.info("✅ Dominio agregado: domain={} en project={}", domain, projectId);
    }

    private void handleRepositoryAdded(Map<String, Object> payload) {
        String projectId = (String) payload.get("projectId");
        String repoName = (String) payload.get("repoName");
        
        log.info("✅ Repositorio agregado: repo={} en project={}", repoName, projectId);
    }
}
