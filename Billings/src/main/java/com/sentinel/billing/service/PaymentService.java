package com.sentinel.billing.service;

import com.sentinel.billing.events.BillingEventPublisher;
import com.sentinel.billing.model.PaymentEntity;
import com.sentinel.billing.model.PaymentStatus;
import com.sentinel.billing.model.PlanEntity;
import com.sentinel.billing.model.SubscriptionEntity;
import com.sentinel.billing.model.SubscriptionStatus;
import com.sentinel.billing.repository.PaymentRepository;
import com.sentinel.billing.repository.PlanRepository;
import com.sentinel.billing.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Servicio de negocio para pagos.
 *
 * Por ahora:
 * - Usa datos MOCK de userId/tenantId.
 * - Usa precios reales desde la tabla "plans" (ya no están hardcodeados).
 * - Guarda en la BD el payment y la suscripción asociada.
 * - Publica un evento estándar de "payment_succeeded".
 *
 * Más adelante:
 * - Usará info real del webhook y del JWT (userId/tenantId, planId dinámico).
 */
@Service
public class PaymentService {

        private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

        private final BillingEventPublisher billingEventPublisher;
        private final PaymentRepository paymentRepository;
        private final SubscriptionRepository subscriptionRepository;
        private final PlanRepository planRepository;

        public PaymentService(BillingEventPublisher billingEventPublisher,
                        PaymentRepository paymentRepository,
                        SubscriptionRepository subscriptionRepository,
                        PlanRepository planRepository) {
                this.billingEventPublisher = billingEventPublisher;
                this.paymentRepository = paymentRepository;
                this.subscriptionRepository = subscriptionRepository;
                this.planRepository = planRepository;
        }

        /**
         * Método usado actualmente por los webhooks/mock existentes.
         * Por compatibilidad mantiene el plan "STANDARD", pero:
         * - Toma el precio real desde la tabla "plans".
         */
        public void confirmMockPayment(String provider, String userId) {
                // TODO: info real tenant
                String mockTenantId = "00000000-0000-0000-0000-000000000000";
                processMockPayment(provider, "STANDARD", userId, mockTenantId);
        }

        public void confirmMockPayment(String provider, String planId, String userId) {
                String mockTenantId = "00000000-0000-0000-0000-000000000000";
                processMockPayment(provider, planId, userId, mockTenantId);
        }

        private void processMockPayment(String provider, String planId, String userId, String tenantId) {
                String normalizedProvider = provider == null ? "UNKNOWN" : provider.toUpperCase(Locale.ROOT);

                // USANDO UUID REAL DEL USUARIO (pasado por parámetro)
                // String mockUserId = "2317b4b6-a162-44ab-9fed-06417427dd5b"; // REMOVED
                // String mockTenantId = "00000000-0000-0000-0000-000000000000"; // REMOVED

                // Obtener el plan desde BD para usar su precio
                PlanEntity plan = planRepository.findById(planId)
                                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

                double amount = plan.getMonthlyPriceUsd().doubleValue();
                String currency = "USD";

                String externalPaymentId = normalizedProvider.toLowerCase(Locale.ROOT) + "_ext_" + UUID.randomUUID();
                String paymentId = "pay_" + UUID.randomUUID();

                log.info("Confirmando pago MOCK provider={} paymentId={} planId={} amount={} {}",
                                normalizedProvider, paymentId, planId, amount, currency);

                // 1) Guardar pago en BD
                PaymentEntity payment = new PaymentEntity()
                                .setId(paymentId)
                                .setProvider(normalizedProvider)
                                .setExternalPaymentId(externalPaymentId)
                                .setTenantId(tenantId)
                                .setUserId(userId)
                                .setPlanId(planId)
                                .setAmount(BigDecimal.valueOf(amount))
                                .setCurrency(currency)
                                .setStatus(PaymentStatus.SUCCEEDED)
                                .setPaidAt(OffsetDateTime.now());

                paymentRepository.save(payment);

                // 2) Crear o actualizar la suscripción
                SubscriptionEntity subscription = subscriptionRepository
                                .findFirstByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId)
                                .orElseGet(() -> new SubscriptionEntity()
                                                .setId("sub_" + UUID.randomUUID())
                                                .setTenantId(tenantId)
                                                .setUserId(userId));

                OffsetDateTime now = OffsetDateTime.now();
                OffsetDateTime nextMonth = now.plusMonths(1);

                subscription
                                .setPlanId(planId)
                                .setStatus(SubscriptionStatus.ACTIVE)
                                .setCurrentPeriodStart(now)
                                .setCurrentPeriodEnd(nextMonth);

                subscription = subscriptionRepository.save(subscription);

                // 3) Publicar evento de pago exitoso
                billingEventPublisher.publishPaymentSucceeded(
                                normalizedProvider,
                                externalPaymentId,
                                payment.getId(),
                                subscription.getId(),
                                userId,
                                tenantId,
                                plan.getName(), // Send Plan NAME (e.g. "STANDARD") not ID
                                amount,
                                currency);

                // 4) CRÍTICO: Publicar evento de suscripción creada para tenant-service
                log.info("Publishing subscription.created event for tenant: {}", tenantId);

                // Preparar límites del plan para el evento
                java.util.Map<String, Object> planLimits = new java.util.HashMap<>();
                planLimits.put("maxUsers", plan.getMaxUsers());
                planLimits.put("maxProjects", plan.getMaxProjects());
                planLimits.put("maxDomains", plan.getMaxDomains());
                planLimits.put("maxRepos", plan.getMaxRepos());
                planLimits.put("maxTenants", plan.getMaxTenants());
                planLimits.put("blockchainEnabled", plan.isIncludesBlockchain());

                billingEventPublisher.publishSubscriptionCreated(
                                subscription.getId(),
                                tenantId,
                                userId,
                                plan.getName(), // Send Plan NAME (e.g. "STANDARD") not ID
                                planLimits);

                log.info("Payment and subscription processing completed successfully");
        }

        /**
         * Admin: Obtener historial global de pagos (paginado).
         */
        public org.springframework.data.domain.Page<PaymentEntity> getAllPayments(
                        org.springframework.data.domain.Pageable pageable) {
                return paymentRepository.findAll(pageable);
        }
}
