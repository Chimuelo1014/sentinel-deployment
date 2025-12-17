package com.sentinel.results_aggregator_service.service;

import com.sentinel.results_aggregator_service.client.ScanOrchestratorClient;
import com.sentinel.results_aggregator_service.domain.ScanResult;
import com.sentinel.results_aggregator_service.domain.Vulnerability;
import com.sentinel.results_aggregator_service.domain.enums.ScanStatus;
import com.sentinel.results_aggregator_service.dto.ScanResultEventDTO;
import com.sentinel.results_aggregator_service.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultsService {

    private final ScanResultRepository repository;
    private final ScanOrchestratorClient orchestratorClient;

    public void processScanResult(ScanResultEventDTO event) {
        log.info("Processing scan result for scanId: {}", event.getScanId());

        // 1. Save detailed result in Mongo
        ScanResult result = ScanResult.builder()
                .scanId(event.getScanId())
                .tenantId(event.getTenantId())
                .type(event.getType())
                .status(event.getStatus())
                .findings(event.getFindings())
                .completedAt(event.getCompletedAt() != null ? event.getCompletedAt() : LocalDateTime.now())
                .detectedAt(LocalDateTime.now())
                .criticalCount((int) event.getFindings().stream().filter(v -> "HIGH".equalsIgnoreCase(v.getSeverity()))
                        .count())
                .highCount((int) event.getFindings().stream().filter(v -> "MEDIUM".equalsIgnoreCase(v.getSeverity()))
                        .count())
                .mediumCount(
                        (int) event.getFindings().stream().filter(v -> "LOW".equalsIgnoreCase(v.getSeverity())).count())
                .lowCount(0)
                .build();

        repository.save(result);
        log.info("Saved scan result to MongoDB");

        // 2. Update Orchestrator status
        try {
            orchestratorClient.updateScanStatus(event.getScanId(), event.getStatus(), null);
            log.info("Updated Orchestrator status for scanId: {}", event.getScanId());
        } catch (Exception e) {
            log.error("Failed to update Orchestrator status", e);
        }
    }

    public ScanResult getResult(java.util.UUID scanId) {
        return repository.findByScanId(scanId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
    }

    public java.util.Map<String, Object> getVulnerabilityAnalytics(String projectId, int days) {
        // Mock analytics data
        return java.util.Map.of(
                "projectId", projectId != null ? projectId : "all",
                "period", "Last " + days + " days",
                "totalVulnerabilities", 150,
                "severityBreakdown", java.util.Map.of("CRITICAL", 5, "HIGH", 25, "MEDIUM", 50, "LOW", 70),
                "trend", java.util.List.of(10, 15, 8, 12, 20));
    }

    public java.util.Map<String, Object> getCodeQualityAnalytics(String projectId, int days) {
        return java.util.Map.of(
                "projectId", projectId != null ? projectId : "all",
                "averageGrade", "B",
                "maintainabilityIndex", 85,
                "issues", java.util.Map.of("bugs", 12, "codeSmells", 45, "duplications", 3),
                "coverage", 78.5);
    }

    public java.util.Map<String, Object> getComplianceAnalytics(String projectId, String standards) {
        return java.util.Map.of(
                "projectId", projectId != null ? projectId : "all",
                "standards", standards,
                "score", 92,
                "passingRequirements", 45,
                "failingRequirements", 4,
                "status", "COMPLIANT");
    }
}
