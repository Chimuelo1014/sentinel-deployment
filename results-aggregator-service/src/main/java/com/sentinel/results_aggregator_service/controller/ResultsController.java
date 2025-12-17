package com.sentinel.results_aggregator_service.controller;

import com.sentinel.results_aggregator_service.domain.ScanResult;
import com.sentinel.results_aggregator_service.service.ResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultsController {

    private final ResultsService resultsService;

    @GetMapping("/{scanId}")
    public ResponseEntity<ScanResult> getScanResult(@PathVariable UUID scanId) {
        return ResponseEntity.ok(resultsService.getResult(scanId));
    }

    @GetMapping("/analytics/vulnerabilities")
    public ResponseEntity<java.util.Map<String, Object>> getVulnerabilityAnalytics(
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(resultsService.getVulnerabilityAnalytics(projectId, days));
    }

    @GetMapping("/analytics/code-quality")
    public ResponseEntity<java.util.Map<String, Object>> getCodeQualityAnalytics(
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(resultsService.getCodeQualityAnalytics(projectId, days));
    }

    @GetMapping("/analytics/compliance")
    public ResponseEntity<java.util.Map<String, Object>> getComplianceAnalytics(
            @RequestParam(required = false) String projectId,
            @RequestParam(defaultValue = "GDPR") String standards) {
        return ResponseEntity.ok(resultsService.getComplianceAnalytics(projectId, standards));
    }
}
