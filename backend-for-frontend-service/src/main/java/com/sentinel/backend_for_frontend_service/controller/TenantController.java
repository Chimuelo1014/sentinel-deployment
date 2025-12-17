package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.client.TenantClient;
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
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantClient tenantClient;
    private final JwtUtils jwtUtils;

    /**
     * Get all tenants for the current user
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyTenants(@RequestHeader("Authorization") String token) {
        log.info("📋 BFF: Getting user's tenants...");

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token", "message", "Could not extract user ID from token"));
        }

        try {
            List<Map<String, Object>> tenants = tenantClient.getMyTenants(token, userId);
            log.info("✅ Found {} tenants for user", tenants.size());
            return ResponseEntity.ok(tenants);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching tenants: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch tenants", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error fetching tenants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Create a new tenant (workspace)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTenant(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {

        log.info("🏢 BFF: Creating tenant with name: {}", request.get("name"));

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token", "message", "Could not extract user ID from token"));
        }

        try {
            Map<String, Object> response = tenantClient.createTenant(token, userId, request);
            log.info("✅ Tenant created successfully: {}", response.get("id"));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (feign.FeignException e) {
            if (e.status() == 402) {
                log.warn("⚠️ User needs to upgrade plan to create tenant");
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of(
                                "error", "PLAN_UPGRADE_REQUIRED",
                                "message",
                                "Free plan users cannot create workspaces. Please upgrade to BASIC, PRO or ENTERPRISE plan.",
                                "upgradeUrl", "/api/billing/plans"));
            }
            log.error("❌ Error creating tenant: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to create tenant", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error creating tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Get tenant by ID
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<?> getTenantById(
            @PathVariable String tenantId,
            @RequestHeader("Authorization") String token) {
        log.info("🔍 BFF: Getting tenant: {}", tenantId);

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> tenant = tenantClient.getTenantById(tenantId, token, userId);
            return ResponseEntity.ok(tenant);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching tenant: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch tenant", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Update tenant
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<?> updateTenant(
            @PathVariable String tenantId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        log.info("✏️ BFF: Updating tenant: {}", tenantId);

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            Map<String, Object> tenant = tenantClient.updateTenant(tenantId, token, userId, request);
            return ResponseEntity.ok(tenant);
        } catch (feign.FeignException e) {
            log.error("❌ Error updating tenant: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to update tenant", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Delete tenant
     */
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<?> deleteTenant(
            @PathVariable String tenantId,
            @RequestHeader("Authorization") String token) {
        log.info("🗑️ BFF: Deleting tenant: {}", tenantId);

        String userId = jwtUtils.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }

        try {
            tenantClient.deleteTenant(tenantId, token, userId);
            return ResponseEntity.ok(Map.of("message", "Tenant deleted successfully"));
        } catch (feign.FeignException e) {
            log.error("❌ Error deleting tenant: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to delete tenant", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
