package com.sentinel.results_aggregator_service.client;

import com.sentinel.results_aggregator_service.domain.enums.ScanStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "scan-orchestrator-service", url = "${app.services.scan-orchestrator-url}")
public interface ScanOrchestratorClient {

    @PutMapping("/api/scans/{id}/status")
    void updateScanStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") ScanStatus status,
            @RequestParam(value = "reason", required = false) String reason
    // Internal call, might need security headers or use Interceptor
    );
}
