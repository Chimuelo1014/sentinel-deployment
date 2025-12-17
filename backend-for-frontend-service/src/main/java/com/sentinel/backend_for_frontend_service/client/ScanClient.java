package com.sentinel.backend_for_frontend_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "scan-orchestrator-service", url = "${services.scanner.url:http://localhost:8086}")
public interface ScanClient {

    // Returns a Page object (Map)
    @GetMapping("/api/scans/my-scans")
    Map<String, Object> getMyScans(@RequestHeader("Authorization") String token);
}
