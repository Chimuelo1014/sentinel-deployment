package com.sentinel.billing.controller;

import com.sentinel.billing.dto.PlanLimitsDTO;
import com.sentinel.billing.dto.SubscriptionDTO;
import com.sentinel.billing.dto.TenantSubscriptionLimitsDTO;
import com.sentinel.billing.model.PlanEntity;
import com.sentinel.billing.model.SubscriptionEntity;
import com.sentinel.billing.model.SubscriptionStatus;
import com.sentinel.billing.repository.PlanRepository;
import com.sentinel.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador interno para que otros microservicios consulten información de
 * planes y suscripciones.
 * Expone endpoints para validación de límites.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalBillingController {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Obtiene detalles de un plan específico.
     * GET /api/internal/plans/{planId}
     */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<PlanLimitsDTO> getPlan(@PathVariable String planId) {
        log.debug("Received request to get plan: {}", planId);

        return planRepository.findById(planId)
                .map(PlanLimitsDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Plan not found: {}", planId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene solo los límites de un plan.
     * GET /api/internal/plans/{planId}/limits
     */
    @GetMapping("/plans/{planId}/limits")
    public ResponseEntity<PlanLimitsDTO> getPlanLimits(@PathVariable String planId) {
        return getPlan(planId);
    }

    /**
     * Obtiene la suscripción activa de un tenant.
     * GET /api/internal/subscriptions/tenant/{tenantId}
     */
    @GetMapping("/subscriptions/tenant/{tenantId}")
    public ResponseEntity<SubscriptionDTO> getTenantSubscription(@PathVariable String tenantId) {
        log.debug("Received request to get subscription for tenant: {}", tenantId);

        Optional<SubscriptionEntity> subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE);

        if (subscription.isEmpty()) {
            log.warn("No active subscription found for tenant: {}", tenantId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(SubscriptionDTO.fromEntity(subscription.get()));
    }

    /**
     * Obtiene los límites del plan activo de un tenant.
     * Este es el endpoint principal que usarán los otros servicios.
     * GET /api/internal/subscriptions/tenant/{tenantId}/limits
     * 
     * @return TenantSubscriptionLimitsDTO con toda la info de suscripción y límites
     */
    @GetMapping("/subscriptions/tenant/{tenantId}/limits")
    public ResponseEntity<TenantSubscriptionLimitsDTO> getTenantLimits(@PathVariable String tenantId) {
        log.debug("Received request to get limits for tenant: {}", tenantId);

        // Buscar suscripción activa
        Optional<SubscriptionEntity> subscriptionOpt = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE);

        if (subscriptionOpt.isEmpty()) {
            log.warn("No active subscription found for tenant: {}", tenantId);
            return ResponseEntity.notFound().build();
        }

        SubscriptionEntity subscription = subscriptionOpt.get();

        // Buscar plan
        Optional<PlanEntity> planOpt = planRepository.findById(subscription.getPlanId());

        if (planOpt.isEmpty()) {
            log.error("Plan not found for subscription: {} - planId: {}",
                    subscription.getId(), subscription.getPlanId());
            return ResponseEntity.notFound().build();
        }

        PlanEntity plan = planOpt.get();

        // Construir respuesta combinada
        TenantSubscriptionLimitsDTO response = TenantSubscriptionLimitsDTO.builder()
                .tenantId(tenantId)
                .subscriptionId(subscription.getId())
                .planId(plan.getId())
                .planName(plan.getName())
                .subscriptionStatus(subscription.getStatus().name())
                .maxUsers(plan.getMaxUsers())
                .maxProjects(plan.getMaxProjects())
                .maxDomains(plan.getMaxDomains())
                .maxRepos(plan.getMaxRepos())
                .includesBlockchain(plan.isIncludesBlockchain())
                .build();

        return ResponseEntity.ok(response);
    }
}
