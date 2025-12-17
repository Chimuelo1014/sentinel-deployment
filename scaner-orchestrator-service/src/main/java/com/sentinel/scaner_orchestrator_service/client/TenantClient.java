package com.sentinel.scaner_orchestrator_service.client;

import com.sentinel.scaner_orchestrator_service.dto.TenantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "tenant-service", url = "${app.services.tenant-url}")
public interface TenantClient {

    @GetMapping("/api/tenants/{id}")
    TenantDTO getTenantById(@PathVariable("id") UUID id);
}
