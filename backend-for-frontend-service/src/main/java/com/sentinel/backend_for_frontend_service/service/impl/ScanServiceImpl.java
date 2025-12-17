package com.sentinel.backend_for_frontend_service.service.impl;

import com.sentinel.backend_for_frontend_service.client.OrchestratorClient;
import com.sentinel.backend_for_frontend_service.client.ResultsAggregatorClient;
import com.sentinel.backend_for_frontend_service.dto.ScanRequestDto;
import com.sentinel.backend_for_frontend_service.dto.ScanResponseDto;
import com.sentinel.backend_for_frontend_service.service.ScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    private final OrchestratorClient orchestratorClient;
    private final ResultsAggregatorClient resultsAggregatorClient;

    @Override
    public ScanResponseDto requestScan(ScanRequestDto requestDto, String token, String tenantId) {
        log.info("📋 Service: Requesting scan for project: {}", requestDto.getProjectId());
        try {
            // Forward request to Orchestrator Service
            Map<String, Object> response = orchestratorClient.requestScan(requestDto, token, tenantId);
            
            return ScanResponseDto.builder()
                    .scanId((String) response.getOrDefault("scanId", UUID.randomUUID().toString()))
                    .status((String) response.getOrDefault("status", "ACCEPTED"))
                    .requestedService("ORCHESTRATOR")
                    .acceptanceTimestampUtc(Instant.now().toString())
                    .completionMethod("RABBITMQ_EVENT")
                    .estimatedCompletionTime(900)
                    .message("Scan request received and queued for processing")
                    .build();
        } catch (Exception e) {
            log.error("Error requesting scan", e);
            return ScanResponseDto.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public Page<Map<String, Object>> listScans(Pageable pageable, String status, String type, String startDate, String endDate, String token, String tenantId) {
        log.info("📋 Service: Listing scans with filters");
        try {
            // Forward request to Orchestrator Service
            Page<Map<String, Object>> scans = orchestratorClient.listScans(pageable, status, type, startDate, endDate, token, tenantId);
            return scans;
        } catch (Exception e) {
            log.error("Error listing scans", e);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public Map<String, Object> getScanStatus(String scanId, String token, String tenantId) {
        log.info("🔍 Service: Getting scan status: {}", scanId);
        try {
            return orchestratorClient.getScanStatus(scanId, token, tenantId);
        } catch (Exception e) {
            log.error("Error getting scan status", e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getScanResults(String scanId, String token, String tenantId) {
        log.info("📊 Service: Getting scan results: {}", scanId);
        try {
            return resultsAggregatorClient.getScanResults(scanId, token, tenantId);
        } catch (Exception e) {
            log.error("Error getting scan results", e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, String> cancelScan(String scanId, String token, String tenantId) {
        log.info("❌ Service: Cancelling scan: {}", scanId);
        try {
            orchestratorClient.cancelScan(scanId, token, tenantId);
            return Map.of("message", "Scan cancelled successfully");
        } catch (Exception e) {
            log.error("Error cancelling scan", e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> exportScanResults(String scanId, String format, String token, String tenantId) {
        log.info("📥 Service: Exporting scan results: {}, Format: {}", scanId, format);
        try {
            return resultsAggregatorClient.exportScanResults(scanId, format, token, tenantId);
        } catch (Exception e) {
            log.error("Error exporting scan results", e);
            return Map.of("error", e.getMessage());
        }
    }
}
