package com.sentinel.billing.controller;

import com.sentinel.billing.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for MercadoPago checkout operations.
 * Handles creating checkout preferences for plan upgrades.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoCheckoutController {

    private final MercadoPagoService mercadoPagoService;

    /**
     * Create a MercadoPago checkout preference for plan upgrade.
     * 
     * @param request containing planId
     * @param userId  from X-User-Id header (forwarded by BFF)
     * @return checkout preference with initPoint URL
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckout(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        String planId = (String) request.get("planId");
        String tenantId = (String) request.getOrDefault("tenantId", "default");

        log.info("💳 Creating MercadoPago checkout for plan: {}, user: {}", planId, userId);

        if (planId == null || planId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "planId is required"));
        }

        if (userId == null || userId.isBlank()) {
            userId = "anonymous_" + System.currentTimeMillis();
            log.warn("No userId provided, using: {}", userId);
        }

        try {
            Map<String, String> preference = mercadoPagoService.createPreference(planId, userId, tenantId);

            log.info("✅ Checkout preference created: {}", preference.get("preferenceId"));

            return ResponseEntity.ok(Map.of(
                    "initPoint", preference.get("initPoint"),
                    "preferenceId", preference.get("preferenceId"),
                    "paymentId", preference.get("paymentId"),
                    "status", "CHECKOUT_CREATED"));
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating checkout", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create checkout", "message", e.getMessage()));
        }
    }
}
