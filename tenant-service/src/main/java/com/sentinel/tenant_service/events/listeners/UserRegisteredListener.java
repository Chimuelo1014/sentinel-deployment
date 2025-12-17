package com.sentinel.tenant_service.events.listeners;

import com.sentinel.tenant_service.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listener para el evento auth.user.registered.
 * Crea automáticamente un tenant cuando un usuario se registra.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final TenantService tenantService;

    /**
     * Consume evento auth.user.registered desde auth-service.
     * 
     * La queue "auth.user.registered.queue" se configura en RabbitMQListenerConfig.
     */
    @RabbitListener(queues = "auth.user.registered.queue")
    public void handleUserRegistered(Map<String, Object> event) {
        try {
            log.info("Received auth.user.registered event: {}", event);

            String eventType = (String) event.get("eventType");

            if (!"auth.user.registered".equals(eventType)) {
                log.warn("Unexpected event type: {}", eventType);
                return;
            }

            String userIdStr = (String) event.get("userId");
            String email = (String) event.get("email");

            if (userIdStr == null || email == null) {
                log.error("Missing required fields in event: userId={}, email={}", userIdStr, email);
                return;
            }

            UUID userId = UUID.fromString(userIdStr);

            log.info("✅ User registered: {} ({}) - FREE plan", email, userId);
            log.info("ℹ️  NO auto-tenant created. User must:");
            log.info("   1. Upgrade to BASIC/PRO/ENTERPRISE to create workspaces, OR");
            log.info("   2. Accept invitation to existing workspace");

            // DISABLED: Automatic tenant creation
            // FREE plan users cannot create tenants
            // They must either:
            // 1. Buy a plan (BASIC/PRO/ENTERPRISE) to create their own workspace
            // 2. Accept an invitation to an existing workspace
            //
            // tenantService.createTenantForUser(userId, email);

            log.info("User {} ready to upgrade or accept invitations", userId);

        } catch (Exception e) {
            log.error("Error processing auth.user.registered event: {}", e.getMessage(), e);
            // TODO: Implementar retry logic o dead letter queue
            throw e; // Re-lanzar para que RabbitMQ maneje el retry
        }
    }
}