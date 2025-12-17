package com.sentinel.billing.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Publicador de eventos de billing hacia RabbitMQ.
 *
 * 📌 Formato FINAL del evento "billing.payment_succeeded"
 *
 * {
 * "eventType": "billing.payment_succeeded",
 * "provider": "MERCADOPAGO" | "PAYPAL",
 * "externalPaymentId": "mp_12345",
 * "paymentId": "pay_001",
 * "subscriptionId": "sub_001",
 * "userId": "user-123",
 * "tenantId": "tenant-abc",
 * "planId": "PRO",
 * "amount": 39.0,
 * "currency": "USD",
 * "paidAt": "2025-12-01T19:00:00Z"
 * }
 *
 * 👉 Este evento lo consumirá, entre otros, el microservicio Blockchain-Writer.
 */
@Component
public class BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingEventPublisher.class);

    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;
    private final String routingKeyPaymentSucceeded;

    public BillingEventPublisher(
            AmqpTemplate amqpTemplate,
            @Value("${billing.events.exchange:billing-exchange}") String exchangeName,
            @Value("${billing.events.payment-succeeded-routing-key:billing.payment_succeeded}") String routingKeyPaymentSucceeded) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.routingKeyPaymentSucceeded = routingKeyPaymentSucceeded;
    }

    /**
     * Publica un evento de pago exitoso.
     *
     * @param provider          MERCADOPAGO, PAYPAL, etc.
     * @param externalPaymentId Id de pago del proveedor externo.
     * @param paymentId         Id interno de pago (cuando exista, por ahora puede
     *                          ser mock).
     * @param subscriptionId    Id interno de suscripción (mock por ahora).
     * @param userId            Id de usuario (vendrá del JWT más adelante).
     * @param tenantId          Id de tenant/cliente.
     * @param planId            Id del plan (BASIC, PRO, ENTERPRISE).
     * @param amount            Monto cobrado.
     * @param currency          Moneda (ej: USD).
     */
    public void publishPaymentSucceeded(String provider,
            String externalPaymentId,
            String paymentId,
            String subscriptionId,
            String userId,
            String tenantId,
            String planId,
            double amount,
            String currency) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "billing.payment_succeeded");
        payload.put("provider", provider);
        payload.put("externalPaymentId", externalPaymentId);
        payload.put("paymentId", paymentId);
        payload.put("subscriptionId", subscriptionId);
        payload.put("userId", userId);
        payload.put("tenantId", tenantId);
        payload.put("planId", planId);
        payload.put("amount", amount);
        payload.put("currency", currency);
        payload.put("paidAt", OffsetDateTime.now(ZoneOffset.UTC).toString());

        log.info("Publicando evento de pago exitoso hacia RabbitMQ: exchange={}, routingKey={}, payload={}",
                exchangeName, routingKeyPaymentSucceeded, payload);

        amqpTemplate.convertAndSend(exchangeName, routingKeyPaymentSucceeded, payload);
    }

    /**
     * Publica evento cuando se crea una nueva suscripción.
     * billing.subscription.created
     */
    public void publishSubscriptionCreated(String subscriptionId,
            String tenantId,
            String userId,
            String planId,
            Map<String, Object> planLimits) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "billing.subscription.created");
        payload.put("subscriptionId", subscriptionId);
        payload.put("tenantId", tenantId);
        payload.put("userId", userId);
        payload.put("planId", planId);
        payload.put("planLimits", planLimits);
        payload.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());

        log.info("Publishing subscription.created event: {}", payload);
        amqpTemplate.convertAndSend(exchangeName, "billing.subscription.created", payload);
    }

    /**
     * Publica evento cuando se actualiza/mejora una suscripción.
     * billing.subscription.upgraded
     */
    public void publishSubscriptionUpgraded(String subscriptionId,
            String tenantId,
            String oldPlanId,
            String newPlanId,
            Map<String, Object> newPlanLimits) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "billing.subscription.upgraded");
        payload.put("subscriptionId", subscriptionId);
        payload.put("tenantId", tenantId);
        payload.put("oldPlanId", oldPlanId);
        payload.put("newPlanId", newPlanId);
        payload.put("newPlanLimits", newPlanLimits);
        payload.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());

        log.info("Publishing subscription.upgraded event: {}", payload);
        amqpTemplate.convertAndSend(exchangeName, "billing.subscription.upgraded", payload);
    }

    /**
     * Publica evento cuando se cancela una suscripción.
     * billing.subscription.cancelled
     */
    public void publishSubscriptionCancelled(String subscriptionId,
            String tenantId,
            String planId) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "billing.subscription.cancelled");
        payload.put("subscriptionId", subscriptionId);
        payload.put("tenantId", tenantId);
        payload.put("planId", planId);
        payload.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());

        log.info("Publishing subscription.cancelled event: {}", payload);
        amqpTemplate.convertAndSend(exchangeName, "billing.subscription.cancelled", payload);
    }
}
