package com.sentinel.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client para comunicarse con Tenant-Service.
 * Permite obtener información del tenant.
 */
@FeignClient(
        name = "tenantService",
        url = "${services.tenant.url:http://localhost:8082}"
)
public interface TenantServiceClient {

    @GetMapping("/api/internal/tenants/{tenantId}")
    Map<String, Object> getTenant(@PathVariable("tenantId") String tenantId);

    @GetMapping("/api/internal/tenants/{tenantId}/plan")
    Map<String, Object> getTenantPlan(@PathVariable("tenantId") String tenantId);
}
