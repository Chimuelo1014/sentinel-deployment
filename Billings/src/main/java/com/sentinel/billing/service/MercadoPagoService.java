package com.sentinel.billing.service;

import com.sentinel.billing.model.PaymentEntity;
import com.sentinel.billing.model.PaymentStatus;
import com.sentinel.billing.model.PlanEntity;
import com.sentinel.billing.model.SubscriptionEntity;
import com.sentinel.billing.model.SubscriptionStatus;
import com.sentinel.billing.repository.PaymentRepository;
import com.sentinel.billing.repository.PlanRepository;
import com.sentinel.billing.repository.SubscriptionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MercadoPagoService {

        private final PaymentRepository paymentRepository;
        private final SubscriptionRepository subscriptionRepository;
        private final PlanRepository planRepository;
        private final RabbitTemplate rabbitTemplate;

        @Value("${mercadopago.access_token}")
        private String accessToken;

        @Value("${app.url:http://localhost:3001}")
        private String appUrl;

        public MercadoPagoService(
                        PaymentRepository paymentRepository,
                        SubscriptionRepository subscriptionRepository,
                        PlanRepository planRepository,
                        RabbitTemplate rabbitTemplate) {
                this.paymentRepository = paymentRepository;
                this.subscriptionRepository = subscriptionRepository;
                this.planRepository = planRepository;
                this.rabbitTemplate = rabbitTemplate;
        }

        /**
         * Creates MercadoPago preference for plan upgrade
         * Configured for Colombia (COP) with test cards
         */
        public Map<String, String> createPreference(String planId, String userId, String tenantId) {
                try {
                        if (accessToken == null || accessToken.isBlank()) {
                                throw new IllegalStateException("MercadoPago access token is not configured");
                        }

                        log.info("🇨🇴 Creating MercadoPago preference for Colombia (COP)");

                        // Get plan details
                        PlanEntity plan = planRepository.findById(planId)
                                        .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

                        // Use COP price (pesos colombianos)
                        java.math.BigDecimal priceCop = plan.getMonthlyPriceCop();
                        if (priceCop == null || priceCop.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                                priceCop = new java.math.BigDecimal("10000"); // Default 10,000 COP
                        }

                        // Create payment record in PENDING status
                        String paymentId = "pay_" + UUID.randomUUID().toString();
                        PaymentEntity payment = new PaymentEntity()
                                        .setId(paymentId)
                                        .setProvider("MERCADOPAGO")
                                        .setTenantId(tenantId)
                                        .setUserId(userId)
                                        .setPlanId(planId)
                                        .setAmount(priceCop)
                                        .setCurrency("COP")
                                        .setStatus(PaymentStatus.PENDING);
                        paymentRepository.save(payment);

                        String description = plan.getDescription() != null
                                        ? plan.getDescription()
                                        : "Sentinel " + plan.getName() + " Plan";

                        log.info("💰 Price: ${} COP (${} USD equivalent)",
                                        priceCop.intValue(), plan.getMonthlyPriceUsd());

                        // Preference JSON for Colombia
                        String preferenceJson = String.format("""
                                        {
                                                "items": [{
                                                        "title": "Sentinel %s",
                                                        "description": "%s",
                                                        "quantity": 1,
                                                        "currency_id": "COP",
                                                        "unit_price": %d
                                                }],
                                                "payer": {
                                                        "email": "test_user_%s@testuser.com"
                                                },
                                                "payment_methods": {
                                                        "installments": 12
                                                },
                                                "back_urls": {
                                                        "success": "%s/billing?status=success",
                                                        "failure": "%s/billing?status=failure",
                                                        "pending": "%s/billing?status=pending"
                                                },
                                                "statement_descriptor": "SENTINEL",
                                                "external_reference": "%s"
                                        }
                                        """,
                                        plan.getName(),
                                        description.replace("\"", "\\\""),
                                        priceCop.intValue(),
                                        userId.substring(0, Math.min(8, userId.length())),
                                        appUrl,
                                        appUrl,
                                        appUrl,
                                        paymentId);

                        log.debug("Sending preference: {}", preferenceJson);

                        // Call MercadoPago API
                        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                        .uri(java.net.URI.create("https://api.mercadopago.com/checkout/preferences"))
                                        .header("Content-Type", "application/json")
                                        .header("Authorization", "Bearer " + accessToken)
                                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(preferenceJson))
                                        .build();

                        java.net.http.HttpResponse<String> response = httpClient.send(request,
                                        java.net.http.HttpResponse.BodyHandlers.ofString());

                        log.info("MercadoPago Response: {}", response.statusCode());

                        if (response.statusCode() != 200 && response.statusCode() != 201) {
                                String errorBody = response.body();
                                log.error("❌ MercadoPago Error [{}]: {}", response.statusCode(), errorBody);
                                throw new RuntimeException(
                                                "MercadoPago API error: " + response.statusCode() + " - " + errorBody);
                        }

                        String responseBody = response.body();
                        String preferenceId = extractJsonValue(responseBody, "id");
                        String initPoint = extractJsonValue(responseBody, "init_point");
                        String sandboxInitPoint = extractJsonValue(responseBody, "sandbox_init_point");

                        String checkoutUrl = accessToken.startsWith("TEST-") ? sandboxInitPoint : initPoint;

                        log.info("✅ Preference created - ID: {}", preferenceId);

                        return Map.of(
                                        "preferenceId", preferenceId,
                                        "initPoint", checkoutUrl,
                                        "paymentId", paymentId);

                } catch (Exception e) {
                        log.error("❌ Error creating preference", e);
                        throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
                }
        }

        private String extractJsonValue(String json, String key) {
                String searchKey = "\"" + key + "\":";
                int keyIndex = json.indexOf(searchKey);
                if (keyIndex == -1)
                        return "";

                int valueStart = keyIndex + searchKey.length();
                while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                        valueStart++;
                }

                if (valueStart >= json.length())
                        return "";

                char startChar = json.charAt(valueStart);
                if (startChar == '"') {
                        int valueEnd = json.indexOf('"', valueStart + 1);
                        return valueEnd > valueStart ? json.substring(valueStart + 1, valueEnd) : "";
                } else {
                        int valueEnd = valueStart;
                        while (valueEnd < json.length() && !",}]".contains(String.valueOf(json.charAt(valueEnd)))) {
                                valueEnd++;
                        }
                        return json.substring(valueStart, valueEnd).trim();
                }
        }

        @Transactional
        public void handleWebhook(Map<String, Object> payload) {
                try {
                        String type = (String) payload.get("type");
                        if (!"payment".equals(type)) {
                                return;
                        }

                        Map<String, Object> data = (Map<String, Object>) payload.get("data");
                        String mpPaymentId = data.get("id").toString();

                        PaymentEntity payment = paymentRepository.findByExternalPaymentId(mpPaymentId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Payment not found: " + mpPaymentId));

                        String action = (String) payload.get("action");

                        if ("payment.updated".equals(action) || "payment.created".equals(action)) {
                                payment.setStatus(PaymentStatus.SUCCEEDED);
                                payment.setPaidAt(OffsetDateTime.now());
                                paymentRepository.save(payment);

                                createOrUpdateSubscription(payment);
                                publishPaymentSuccessEvent(payment);
                        }

                } catch (Exception e) {
                        throw new RuntimeException("Error handling webhook: " + e.getMessage(), e);
                }
        }

        private void createOrUpdateSubscription(PaymentEntity payment) {
                SubscriptionEntity subscription = subscriptionRepository.findByUserId(payment.getUserId())
                                .orElse(null);

                OffsetDateTime now = OffsetDateTime.now();
                OffsetDateTime periodEnd = now.plusMonths(1);

                if (subscription == null) {
                        subscription = new SubscriptionEntity()
                                        .setId("sub_" + UUID.randomUUID().toString())
                                        .setUserId(payment.getUserId())
                                        .setTenantId(payment.getTenantId())
                                        .setPlanId(payment.getPlanId())
                                        .setStatus(SubscriptionStatus.ACTIVE)
                                        .setCurrentPeriodStart(now)
                                        .setCurrentPeriodEnd(periodEnd);
                } else {
                        subscription.setPlanId(payment.getPlanId())
                                        .setStatus(SubscriptionStatus.ACTIVE)
                                        .setCurrentPeriodStart(now)
                                        .setCurrentPeriodEnd(periodEnd);
                }

                subscriptionRepository.save(subscription);
        }

        private void publishPaymentSuccessEvent(PaymentEntity payment) {
                Map<String, Object> event = Map.of(
                                "userId", payment.getUserId(),
                                "tenantId", payment.getTenantId(),
                                "planId", payment.getPlanId(),
                                "paymentId", payment.getId(),
                                "amount", payment.getAmount(),
                                "timestamp", OffsetDateTime.now().toString());

                rabbitTemplate.convertAndSend(
                                "billing-exchange",
                                "billing.payment.succeeded",
                                event);
        }
}