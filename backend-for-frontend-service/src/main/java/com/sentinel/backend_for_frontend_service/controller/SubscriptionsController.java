package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.client.BillingClient;
import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final BillingClient billingClient;
    private final JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<?> getMySubscription(@RequestHeader("Authorization") String token) {
        log.info("💰 BFF: Getting user's subscription...");

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> subscription = billingClient.getMySubscription(token, userId);
            return ResponseEntity.ok(subscription);
        } catch (feign.FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.ok(Map.of(
                        "plan", "FREE",
                        "planName", "Free",
                        "status", "ACTIVE",
                        "message", "No paid subscription - using FREE plan"));
            }
            log.error("❌ Error fetching subscription: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch subscription", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createSubscription(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        log.info("💰 BFF: Creating subscription for plan: {}", request.get("planId"));

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            // Add provider field if not present (required by billing-service)
            Map<String, Object> billingRequest = new java.util.HashMap<>(request);
            if (!billingRequest.containsKey("provider")) {
                billingRequest.put("provider", "MERCADOPAGO");
            }

            Map<String, Object> subscription = billingClient.createSubscription(token, userId, billingRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (feign.FeignException e) {
            log.error("❌ Error creating subscription: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to create subscription", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<?> updateSubscription(
            @PathVariable String subscriptionId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        log.info("💰 BFF: Updating subscription: {}", subscriptionId);

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> subscription = billingClient.updateSubscription(subscriptionId, token, userId, request);
            return ResponseEntity.ok(subscription);
        } catch (feign.FeignException e) {
            log.error("❌ Error updating subscription: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to update subscription", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable String subscriptionId,
            @RequestHeader("Authorization") String token) {
        log.info("💰 BFF: Cancelling subscription: {}", subscriptionId);

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> result = billingClient.cancelSubscription(subscriptionId, token, userId);
            return ResponseEntity.ok(result);
        } catch (feign.FeignException e) {
            log.error("❌ Error cancelling subscription: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to cancel subscription", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Create MercadoPago checkout preference.
     * Returns init_point URL for redirect to MercadoPago checkout.
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        log.info("💳 BFF: Creating MercadoPago checkout for plan: {}", request.get("planId"));

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> checkout = billingClient.createMercadoPagoCheckout(token, userId, request);
            return ResponseEntity.ok(checkout);
        } catch (feign.FeignException e) {
            log.error("❌ Error creating checkout: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to create checkout", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Test webhook endpoint for simulating MercadoPago payment approval.
     * Proxies to billing-service webhook which updates user plan.
     */
    @PostMapping("/webhook/test")
    public ResponseEntity<?> testWebhook(
            @RequestParam String paymentId,
            @RequestParam String planId,
            @RequestParam String userId) {
        log.info("🧪 BFF: Test webhook for user: {}, plan: {}", userId, planId);

        try {
            Map<String, Object> result = billingClient.testWebhook(paymentId, planId, userId);
            return ResponseEntity.ok(result);
        } catch (feign.FeignException e) {
            log.error("❌ Error calling webhook: {} - {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Webhook failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
