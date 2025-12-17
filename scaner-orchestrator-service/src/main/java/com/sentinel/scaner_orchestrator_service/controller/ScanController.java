package com.sentinel.scaner_orchestrator_service.controller;

import com.sentinel.scaner_orchestrator_service.dto.request.ScanRequestDTO;
import com.sentinel.scaner_orchestrator_service.dto.response.ScanResponseDTO;
import com.sentinel.scaner_orchestrator_service.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
@Slf4j
public class ScanController {

    private final ScanService scanService;

    @PostMapping
    public ResponseEntity<ScanResponseDTO> createScan(
            @Valid @RequestBody ScanRequestDTO request,
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        log.info("DEBUG: Request create scan for tenant {}", tenantId);
        log.info("DEBUG: Received X-User-Id header: '{}'", userIdStr);

        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        // Remove quotes if present (dirty fix potential)
        userIdStr = userIdStr.replace("\"", "");

        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(scanService.createScan(request, tenantId, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ScanResponseDTO>> listScans(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scanService.getScans(tenantId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/my-scans")
    public ResponseEntity<Page<ScanResponseDTO>> listMyScans(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(scanService.getScansByUser(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanResponseDTO> getScan(@PathVariable UUID id) {
        return ResponseEntity.ok(scanService.getScan(id));
    }
}
