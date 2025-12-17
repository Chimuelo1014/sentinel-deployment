package com.sentinel.scaner_orchestrator_service.controller;

import com.sentinel.scaner_orchestrator_service.domain.ScanJob;
import com.sentinel.scaner_orchestrator_service.domain.enums.ScanStatus;
import com.sentinel.scaner_orchestrator_service.repository.ScanJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/scans")
@RequiredArgsConstructor
@Slf4j
public class InternalScanController {

    private final ScanJobRepository repository;

    @PutMapping("/{scanId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID scanId, @RequestBody Map<String, String> body) {
        log.info("Internal: update status for scan {} with payload {}", scanId, body);
        var opt = repository.findById(scanId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ScanJob job = opt.get();
        String status = body.getOrDefault("status", "");
        String reason = body.get("failureReason");
        try {
            ScanStatus s = ScanStatus.valueOf(status);
            job.setStatus(s);
            if (s == ScanStatus.RUNNING) job.setStartedAt(LocalDateTime.now());
            if (s == ScanStatus.COMPLETED || s == ScanStatus.FAILED) job.setFinishedAt(LocalDateTime.now());
            if (reason != null) job.setFailureReason(reason);
            repository.save(job);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }
    }

    @PostMapping("/{scanId}/results")
    public ResponseEntity<?> submitResults(@PathVariable UUID scanId, @RequestBody Map<String, Object> body) {
        log.info("Internal: submit results for scan {} (summary keys: {})", scanId, body.keySet());
        var opt = repository.findById(scanId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ScanJob job = opt.get();
        job.setStatus(ScanStatus.COMPLETED);
        job.setFinishedAt(LocalDateTime.now());
        repository.save(job);
        return ResponseEntity.ok().build();
    }
}
