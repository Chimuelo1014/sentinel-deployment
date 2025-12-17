package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.dto.ScanRequestDto;
import com.sentinel.backend_for_frontend_service.dto.ScanResponseDto;
import com.sentinel.backend_for_frontend_service.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bff/scans")
@Tag(name = "Scans", description = "Scan management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    /**
     * Request a new scan for a project
     */
    @PostMapping("/request")
    @Operation(summary = "Request a new scan", description = "Submits a new scan request for the specified project")
    public ResponseEntity<ScanResponseDto> requestScan(
            @RequestBody ScanRequestDto requestDto,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📋 BFF: Request new scan for project: {}", requestDto.getProjectId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(scanService.requestScan(requestDto, token, tenantId));
    }

    /**
     * List all scans with pagination and filters
     */
    @GetMapping
    @Operation(summary = "List scans", description = "Retrieve paginated list of scans with optional filters")
    public ResponseEntity<Page<Map<String, Object>>> listScans(
            @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📋 BFF: List scans - Page: {}, Size: {}, Status: {}, Type: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), status, type);
        return ResponseEntity.ok(scanService.listScans(pageable, status, type, startDate, endDate, token, tenantId));
    }

    /**
     * Get scan status
     */
    @GetMapping("/{scanId}")
    @Operation(summary = "Get scan status", description = "Retrieve the current status and progress of a scan")
    public ResponseEntity<Map<String, Object>> getScanStatus(
            @PathVariable String scanId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("🔍 BFF: Get scan status: {}", scanId);
        return ResponseEntity.ok(scanService.getScanStatus(scanId, token, tenantId));
    }

    /**
     * Get scan results
     */
    @GetMapping("/{scanId}/results")
    @Operation(summary = "Get scan results", description = "Retrieve detailed results of a completed scan")
    public ResponseEntity<Map<String, Object>> getScanResults(
            @PathVariable String scanId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📊 BFF: Get scan results: {}", scanId);
        return ResponseEntity.ok(scanService.getScanResults(scanId, token, tenantId));
    }

    /**
     * Cancel a scan
     */
    @DeleteMapping("/{scanId}")
    @Operation(summary = "Cancel scan", description = "Cancel a pending or running scan")
    public ResponseEntity<Map<String, String>> cancelScan(
            @PathVariable String scanId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("❌ BFF: Cancel scan: {}", scanId);
        return ResponseEntity.ok(scanService.cancelScan(scanId, token, tenantId));
    }

    /**
     * Export scan results
     */
    @GetMapping("/{scanId}/export")
    @Operation(summary = "Export scan results", description = "Export scan results as PDF or JSON")
    public ResponseEntity<?> exportScanResults(
            @PathVariable String scanId,
            @RequestParam(defaultValue = "PDF") String format,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📥 BFF: Export scan results - ID: {}, Format: {}", scanId, format);
        return ResponseEntity.ok(scanService.exportScanResults(scanId, format, token, tenantId));
    }
}
