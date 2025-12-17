package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.client.ProjectClient;
import com.sentinel.backend_for_frontend_service.client.TenantClient;
import com.sentinel.backend_for_frontend_service.client.ScanClient;
import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/bff")
@RequiredArgsConstructor
public class DashboardController {

    private final TenantClient tenantClient;
    private final ProjectClient projectClient;
    private final ScanClient scanClient;
    private final JwtUtils jwtUtils;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestHeader("Authorization") String token) {
        log.info("🔍 BFF: Aggregating Dashboard Data...");

        String userId = jwtUtils.extractUserId(token);
        log.info("Extracted userId: {}", userId);

        // Execute calls in parallel using CompletableFuture
        CompletableFuture<List<Map<String, Object>>> tenantsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return tenantClient.getMyTenants(token, userId);
            } catch (Exception e) {
                log.error("Failed to fetch tenants", e);
                return List.of();
            }
        });

        CompletableFuture<List<Map<String, Object>>> projectsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return projectClient.getMyProjects(token);
            } catch (Exception e) {
                log.error("Failed to fetch projects", e);
                return List.of();
            }
        });

        CompletableFuture<Map<String, Object>> scansFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return scanClient.getMyScans(token);
            } catch (Exception e) {
                log.error("Failed to fetch scans", e);
                return Map.of("content", List.of());
            }
        });

        // Wait for all
        CompletableFuture.allOf(tenantsFuture, projectsFuture, scansFuture).join();

        Map<String, Object> response = new HashMap<>();
        try {
            response.put("tenants", tenantsFuture.get());
            response.put("projects", projectsFuture.get());

            // Extract content from Page object
            Map<String, Object> scanPage = scansFuture.get();
            response.put("recentScans", scanPage.getOrDefault("content", List.of()));

            response.put("stats", Map.of(
                    "totalTenants", tenantsFuture.get().size(),
                    "totalProjects", projectsFuture.get().size(),
                    "totalScans", ((List<?>) scanPage.getOrDefault("content", List.of())).size()));
        } catch (Exception e) {
            log.error("Error building response", e);
        }

        return ResponseEntity.ok(response);
    }
}
