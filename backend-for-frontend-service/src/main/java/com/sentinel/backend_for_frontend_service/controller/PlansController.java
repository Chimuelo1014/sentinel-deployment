package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.client.BillingClient;
import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlansController {

    private final BillingClient billingClient;

    @GetMapping
    public ResponseEntity<?> getPlans(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("💰 BFF: Getting available plans...");
        try {
            List<Map<String, Object>> plans = billingClient.getPlans(token != null ? token : "");
            return ResponseEntity.ok(plans);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching plans: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch plans", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlanById(
            @PathVariable String planId,
            @RequestHeader("Authorization") String token) {
        log.info("💰 BFF: Getting plan: {}", planId);
        try {
            Map<String, Object> plan = billingClient.getPlanById(planId, token);
            return ResponseEntity.ok(plan);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching plan: {}", e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch plan", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
