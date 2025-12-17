package com.sentinel.backend_for_frontend_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bff/analytics")
@Tag(name = "Analytics", description = "Analytics and insights endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AnalyticsController {

        private final com.sentinel.backend_for_frontend_service.client.ResultsAggregatorClient resultsClient;

        /**
         * Get vulnerability analytics
         */
        @GetMapping("/vulnerabilities")
        @Operation(summary = "Get vulnerability analytics", description = "Retrieve vulnerability trends and statistics")
        public ResponseEntity<Map<String, Object>> getVulnerabilityAnalytics(
                        @RequestParam(required = false) String projectId,
                        @RequestParam(defaultValue = "30") int days,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
                log.info("🛡️ BFF: Get vulnerability analytics - Days: {}, ProjectId: {}", days, projectId);
                return ResponseEntity.ok(resultsClient.getVulnerabilityAnalytics(projectId, days, token));
        }

        /**
         * Get code quality analytics
         */
        @GetMapping("/code-quality")
        @Operation(summary = "Get code quality analytics", description = "Retrieve code quality trends and metrics")
        public ResponseEntity<Map<String, Object>> getCodeQualityAnalytics(
                        @RequestParam(required = false) String projectId,
                        @RequestParam(defaultValue = "30") int days,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
                log.info("📈 BFF: Get code quality analytics - Days: {}, ProjectId: {}", days, projectId);
                return ResponseEntity.ok(resultsClient.getCodeQualityAnalytics(projectId, days, token));
        }

        /**
         * Get compliance analytics
         */
        @GetMapping("/compliance")
        @Operation(summary = "Get compliance analytics", description = "Retrieve compliance status and standards coverage")
        public ResponseEntity<Map<String, Object>> getComplianceAnalytics(
                        @RequestParam(required = false) String projectId,
                        @RequestParam(defaultValue = "GDPR,PCI-DSS,HIPAA") String standards,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
                log.info("✅ BFF: Get compliance analytics - Standards: {}, ProjectId: {}", standards, projectId);
                return ResponseEntity.ok(resultsClient.getComplianceAnalytics(projectId, standards, token));
        }
}
