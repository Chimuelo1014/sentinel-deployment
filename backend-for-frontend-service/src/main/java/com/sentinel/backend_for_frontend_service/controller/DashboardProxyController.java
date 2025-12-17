package com.sentinel.backend_for_frontend_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardProxyController {

    @GetMapping("/top-risk-projects")
    public ResponseEntity<?> getTopRiskProjects(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("📊 BFF: Getting top risk projects...");
        // Return empty list for now - will be connected to analytics later
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("📊 BFF: Getting dashboard summary...");
        return ResponseEntity.ok(Map.of(
                "totalProjects", 0,
                "totalScans", 0,
                "criticalVulnerabilities", 0,
                "highVulnerabilities", 0,
                "mediumVulnerabilities", 0,
                "lowVulnerabilities", 0));
    }
}
