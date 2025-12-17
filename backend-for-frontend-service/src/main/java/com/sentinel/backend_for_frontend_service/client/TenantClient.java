package com.sentinel.backend_for_frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "tenant-service", url = "${services.tenant.url:http://localhost:8082}")
public interface TenantClient {

        @GetMapping("/api/tenants/me")
        List<Map<String, Object>> getMyTenants(
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId);

        @GetMapping("/api/tenants/{tenantId}")
        Map<String, Object> getTenantById(
                        @PathVariable String tenantId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId);

        @PostMapping("/api/tenants")
        Map<String, Object> createTenant(
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestBody Map<String, Object> request);

        @PutMapping("/api/tenants/{tenantId}")
        Map<String, Object> updateTenant(
                        @PathVariable String tenantId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestBody Map<String, Object> request);

        @DeleteMapping("/api/tenants/{tenantId}")
        void deleteTenant(
                        @PathVariable String tenantId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader("X-User-Id") String userId);
}
