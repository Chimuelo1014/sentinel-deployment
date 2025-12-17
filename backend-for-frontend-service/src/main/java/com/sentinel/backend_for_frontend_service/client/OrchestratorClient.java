package com.sentinel.backend_for_frontend_service.client;

import com.sentinel.backend_for_frontend_service.dto.ScanRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "orchestrator-client", url = "${app.services.orchestrator-url}")
public interface OrchestratorClient {

    @PostMapping("/api/scans/request")
    Map<String, Object> requestScan(
            @RequestBody ScanRequestDto requestDto,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

    @GetMapping("/api/scans")
    Page<Map<String, Object>> listScans(
            Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

    @GetMapping("/api/scans/{scanId}")
    Map<String, Object> getScanStatus(
            @PathVariable String scanId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

    @DeleteMapping("/api/scans/{scanId}")
    void cancelScan(
            @PathVariable String scanId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);
}
