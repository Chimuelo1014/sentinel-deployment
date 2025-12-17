package com.sentinel.backend_for_frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "billing-service", url = "${app.services.billing-url:http://billing-service:8084}")
public interface BillingClient {

        @GetMapping("/api/plans")
        List<Map<String, Object>> getPlans(@RequestHeader("Authorization") String token);

        @GetMapping("/api/plans/{planId}")
        Map<String, Object> getPlanById(
                        @PathVariable String planId,
                        @RequestHeader("Authorization") String token);

        @PostMapping("/api/payments/confirm")
        Map<String, Object> createSubscription(
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestBody Map<String, Object> request);

        @GetMapping("/api/subscriptions/me")
        Map<String, Object> getMySubscription(
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId);

        @PutMapping("/api/subscriptions/{subscriptionId}")
        Map<String, Object> updateSubscription(
                        @PathVariable String subscriptionId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestBody Map<String, Object> request);

        @DeleteMapping("/api/subscriptions/{subscriptionId}")
        Map<String, Object> cancelSubscription(
                        @PathVariable String subscriptionId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId);

        @PostMapping("/api/payments/mercadopago/checkout")
        Map<String, Object> createMercadoPagoCheckout(
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestBody Map<String, Object> request);

        @PostMapping("/api/webhooks/mercadopago/test")
        Map<String, Object> testWebhook(
                        @RequestParam("paymentId") String paymentId,
                        @RequestParam("planId") String planId,
                        @RequestParam("userId") String userId);
}
