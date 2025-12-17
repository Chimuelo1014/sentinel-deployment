package com.sentinel.billing.listeners;

import com.sentinel.billing.model.SubscriptionEntity;
import com.sentinel.billing.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Listener para eventos de upgrade de plan en Tenant-Service.
 * 
 * Evento: tenant.plan.upgraded
 * - tenantId
 * - newPlanId
 * 
 * Acción: Actualizar la suscripción del tenant si existe
 * 
 * DESACTIVADO TEMPORALMENTE: En desarrollo, estas queues se crean bajo demanda
 */
//@Component
public class TenantPlanUpgradedListener {

    private static final Logger log = LoggerFactory.getLogger(TenantPlanUpgradedListener.class);

    private final SubscriptionRepository subscriptionRepository;

    public TenantPlanUpgradedListener(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    //@RabbitListener(queues = "${billing.listeners.tenant.queue:billing-tenant-events-queue}")
    public void handleTenantPlanUpgraded(@Payload Map<String, Object> payload) {
        log.info("📨 Evento recibido: tenant.plan.upgraded -> Payload: {}", payload);

        String tenantId = (String) payload.get("tenantId");
        String newPlanId = (String) payload.get("newPlanId");

        if (tenantId == null || newPlanId == null) {
            log.warn("⚠️ Payload incompleto en tenant.plan.upgraded. tenantId={}, newPlanId={}",
                    tenantId, newPlanId);
            return;
        }

        // Obtener la suscripción activa del tenant
        Optional<SubscriptionEntity> optional = subscriptionRepository
                .findFirstByTenantIdOrderByCreatedAtDesc(tenantId);

        if (optional.isEmpty()) {
            log.warn("⚠️ No se encontró suscripción para tenant {} en evento tenant.plan.upgraded",
                    tenantId);
            return;
        }

        SubscriptionEntity subscription = optional.get();
        String oldPlanId = subscription.getPlanId();

        // Actualizar el plan en la suscripción
        subscription.setPlanId(newPlanId);
        subscriptionRepository.save(subscription);

        log.info("✅ Suscripción de tenant {} actualizada: {} -> {}", tenantId, oldPlanId, newPlanId);
    }
}
